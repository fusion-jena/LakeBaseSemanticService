package de.uni_jena.cs.fusion.semantic.datasource;

/*-
 * #%L
 * LakeBase Semantic Service
 * %%
 * Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * <p>
 * An {@link SemanticDataSource} that gathers multiple other
 * {@link SemanticDataSource}s to provide a single point of access to multiple
 * {@link SemanticDataSource}s.
 * </p>
 * 
 * <p>
 * A <b>namespace lookup</b> provides fast access to only suitable
 * {@link SemanticDataSource}s providing entities in the given namespace.
 * </p>
 * 
 * <p>
 * A <b>{@link Scope} lookup</b> provides restriction of term search to relevant
 * {@link SemanticDataSource}s containing entities of the given {@link Scope}.
 * </p>
 * 
 * <p>
 * If a <b>multithreaded</b> {@link ExecutorService} is provided the gathered
 * {@link SemanticDataSource}s will be called parallel.
 * </p>
 * 
 * <p>
 * Calling {@link #maintain} will trigger a sequential maintenance of all
 * {@link SemanticDataSource}s implementing {@link Maintainable}.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public final class SemanticDataSourceManager implements Maintainable, SemanticDataSource {

	static final Logger log = LoggerFactory.getLogger(SemanticDataSourceManager.class);
	private final Set<SemanticDataSource> adapters = new HashSet<SemanticDataSource>();
	private Map<IRI, Set<SemanticDataSource>> adaptersPerScoupe = new HashMap<IRI, Set<SemanticDataSource>>();
	private final ExecutorService executor;

	private Collection<String> namespaces = new HashSet<String>();

	public SemanticDataSourceManager() throws SemanticDataSourceException {
		this(Executors.newSingleThreadExecutor());
	}

	public SemanticDataSourceManager(ExecutorService executor) throws SemanticDataSourceException {
		this.executor = executor;
	}

	/**
	 * Returns the result of the given {@link Future}. If an
	 * {@link SemanticDataSourceException} occurred during the execution, the
	 * exception will be logged and the {@code other} value will be returned. If any
	 * other kind of exceptions occurred, an {@link SemanticDataSourceException}
	 * will be thrown.
	 * 
	 * @param future
	 * @param other
	 * @return the result of the given {@link Future}
	 * @throws SemanticDataSourceException
	 */
	private static Boolean ensureBoolean(Future<Boolean> future, Boolean other) throws SemanticDataSourceException {
		try {
			return future.get();
		} catch (ExecutionException e) {
			log.warn("Execution Error, continue processing using other result:", e.getCause());
			return other;
		} catch (Throwable e) {
			throw new SemanticDataSourceException(e);
		}
	}

	/**
	 * Returns the result of the given {@link Future}. If an
	 * {@link ExecutionException} occurred during the execution or the result was
	 * {@code null}, this will be logged and an empty result will be returned.
	 * 
	 * @param future
	 * @return the result of the given {@link Future}
	 * @throws SemanticDataSourceException
	 */
	private static <T> Collection<T> ensureCollection(Future<Collection<T>> future, String callDescription)
			throws SemanticDataSourceException {
		try {
			return Objects.requireNonNull(future.get());
		} catch (ExecutionException e) {
			log.warn("Execution Error for " + callDescription + ". Continue processing using empty result:",
					e.getCause());
			return Collections.emptyList();
		} catch (NullPointerException e) {
			log.warn("SemanticDataSource returned null for " + callDescription
					+ ". Continue processing using empty result.");
			return Collections.emptyList();
		} catch (Throwable e) {
			throw new SemanticDataSourceException(e);
		}
	}

	/**
	 * Returns the result of the given {@link Future}. If an
	 * {@link SemanticDataSourceException} occurred during the execution, the
	 * exception will be logged and an empty result will be returned. If any other
	 * kind of exceptions occurred, an {@link SemanticDataSourceException} will be
	 * thrown.
	 * 
	 * @param future
	 * @return the result of the given {@link Future}
	 * @throws SemanticDataSourceException
	 */
	private static <S, T> Map<S, T> ensureMap(Future<Map<S, T>> future, String callDescription)
			throws SemanticDataSourceException {
		try {
			return Objects.requireNonNull(future.get());
		} catch (ExecutionException e) {
			log.warn("Execution Error for " + callDescription + ". Continue processing using empty result:",
					e.getCause());
			return Collections.emptyMap();
		} catch (NullPointerException e) {
			log.warn("SemanticDataSource returned null for " + callDescription
					+ ". Continue processing using empty result.");
			return Collections.emptyMap();
		} catch (Throwable e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private void logScheduleError(Throwable e) {
		log.error("Scheduling Error, continue scheduling scipping current SemanticDataSource:", e.getCause());
	}

	/**
	 * Returns a set of the managed adapters.
	 * 
	 * @return a set of the managed adapters
	 * @see SemanticDataSource
	 */
	public Collection<SemanticDataSource> getAdapters() {
		return Collections.unmodifiableCollection(this.adapters);
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		// organize SemanticDataSources
		Collection<SemanticDataSource> allBroadersProviders = new ArrayList<SemanticDataSource>();
		Collection<SemanticDataSource> broadersProviders = new ArrayList<SemanticDataSource>();
		for (SemanticDataSource semanticDataSource : adapters) {
			if (semanticDataSource.providingAllBroaders()) {
				allBroadersProviders.add(semanticDataSource);
			} else if (semanticDataSource.providingBroaders()) {
				broadersProviders.add(semanticDataSource);
			}
		}
		// decorate Executor
		ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);
		// prepare result sets
		Collection<IRI> processed = Collections.synchronizedSet(new HashSet<IRI>());
		Collection<IRI> broadersUnsynchronized = new HashSet<IRI>();
		Collection<IRI> broaders = Collections.synchronizedCollection(broadersUnsynchronized);
		// create phaser
		final Phaser phaser = new Phaser();

		/**
		 * parameterized call for scheduling
		 */
		class GetBroadersCall implements Callable<Collection<IRI>> {
			private final SemanticDataSource semanticDataSource;
			private final IRI iri;
			private final boolean all;

			GetBroadersCall(SemanticDataSource semanticDataSource, IRI iri, boolean all) {
				this.semanticDataSource = semanticDataSource;
				this.iri = iri;
				this.all = all;
			}

			@Override
			public Collection<IRI> call() throws Exception {
				if (this.all) {
					return this.semanticDataSource.getAllBroaders(iri);
				} else {
					return this.semanticDataSource.getBroaders(iri);
				}
			}
		}

		/**
		 * parameterized callback for scheduling
		 */
		class GetBroadersCallback implements FutureCallback<Collection<IRI>> {
			private final SemanticDataSource semanticDataSource;
			private final IRI iri;
			private final boolean all;

			GetBroadersCallback(SemanticDataSource semanticDataSource, IRI iri, boolean all) {
				this.semanticDataSource = semanticDataSource;
				this.iri = iri;
				this.all = all;
			}

			@Override
			public void onSuccess(Collection<IRI> result) {
				for (IRI broader : result) {
					if (processed.add(broader)// avoid redundant work: only continue if broader was not processed before
					) {
						// add to results
						broaders.add(broader);
						// schedule calls for all broaders
						for (SemanticDataSource provider : allBroadersProviders) {
							try {
								if (provider != this.semanticDataSource // avoid redundant work: result would be a
																		// subset of current result
										&& provider.hasSuitableNamespace(broader)) {
									try {
										phaser.register();
										Futures.addCallback(
												listeningExecutor.submit(new GetBroadersCall(provider, broader, true)),
												new GetBroadersCallback(provider, broader, true), listeningExecutor);
									} catch (Throwable e) {
										logScheduleError(e);
									}
								}
							} catch (SemanticDataSourceException e) {
								// log and ignore error
								log.warn("Failed to execute hasSuitableNamespace(" + this.iri + ") on " + provider
										+ ". Ignoring failure and continue.", e);
							}
						}
						for (SemanticDataSource provider : broadersProviders) {
							try {
								if (provider.hasSuitableNamespace(broader)) {
									try {
										phaser.register();
										Futures.addCallback(
												listeningExecutor.submit(new GetBroadersCall(provider, broader, false)),
												new GetBroadersCallback(provider, broader, false), listeningExecutor);
									} catch (Throwable e) {
										logScheduleError(e);
									}
								}
							} catch (SemanticDataSourceException e) {
								// log and ignore error
								log.warn("Failed to execute hasSuitableNamespace(" + this.iri + ") on " + provider
										+ ". Ignoring failure and continue.", e);
							}
						}
					}
				}
				phaser.arrive();
			}

			@Override
			public void onFailure(Throwable t) {
				phaser.arrive();
				// log and ignore error
				log.warn("Failed to execute " + ((this.all) ? "all" : "") + "Broaders(" + this.iri + ") on "
						+ this.semanticDataSource + ". Ignoring failure and continue.", t);
			}
		}

		// schedule initial future
		try {
			phaser.register();
			Futures.addCallback(listeningExecutor.submit(new Callable<Collection<IRI>>() {
				@Override
				public Collection<IRI> call() {
					return Collections.singleton(iri);
				}
			}), new GetBroadersCallback(this, iri, true));
		} catch (Throwable e) {
			logScheduleError(e);
		}

		// wait for all futures until timeout
		try {
			phaser.awaitAdvanceInterruptibly(0, 60, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			throw new SemanticDataSourceException("Failed to execute getAllBroaders(" + iri + ").", e);
		}

		// remove given IRI from results
		broaders.remove(iri);

		// return total results as unsynchronized collection
		return broadersUnsynchronized;
	}

	@Override
	public Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		// organize SemanticDataSources
		Collection<SemanticDataSource> allNarrowersProviders = new ArrayList<SemanticDataSource>();
		Collection<SemanticDataSource> narrowersProviders = new ArrayList<SemanticDataSource>();
		for (SemanticDataSource semanticDataSource : adapters) {
			if (semanticDataSource.providingAllNarrowers()) {
				allNarrowersProviders.add(semanticDataSource);
			} else if (semanticDataSource.providingNarrowers()) {
				narrowersProviders.add(semanticDataSource);
			}
		}
		// decorate Executor
		ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);
		// prepare result sets
		Collection<IRI> processed = Collections.synchronizedSet(new HashSet<IRI>());
		Collection<IRI> narrowersUnsynchronized = new HashSet<IRI>();
		Collection<IRI> narrowers = Collections.synchronizedCollection(narrowersUnsynchronized);
		// create phaser
		final Phaser phaser = new Phaser();

		/**
		 * parameterized call for scheduling
		 */
		class GetNarrowersCall implements Callable<Collection<IRI>> {
			private final SemanticDataSource semanticDataSource;
			private final IRI iri;
			private final boolean all;

			GetNarrowersCall(SemanticDataSource semanticDataSource, IRI iri, boolean all) {
				this.semanticDataSource = semanticDataSource;
				this.iri = iri;
				this.all = all;
			}

			@Override
			public Collection<IRI> call() throws Exception {
				if (this.all) {
					return this.semanticDataSource.getAllNarrowers(iri);
				} else {
					return this.semanticDataSource.getNarrowers(iri);
				}
			}
		}

		/**
		 * parameterized callback for scheduling
		 */
		class GetNarrowersCallback implements FutureCallback<Collection<IRI>> {
			private final SemanticDataSource semanticDataSource;
			private final IRI iri;
			private final boolean all;

			GetNarrowersCallback(SemanticDataSource semanticDataSource, IRI iri, boolean all) {
				this.semanticDataSource = semanticDataSource;
				this.iri = iri;
				this.all = all;
			}

			@Override
			public void onSuccess(Collection<IRI> result) {
				for (IRI narrower : result) {
					if (processed.add(narrower)// avoid redundant work: only continue if narrower was not processed before
					) {
						// add to results
						narrowers.add(narrower);
						// schedule calls for all narrowers
						for (SemanticDataSource provider : allNarrowersProviders) {
							try {
								if (provider != this.semanticDataSource // avoid redundant work: result would be a
																		// subset of current result
										&& provider.hasSuitableNamespace(narrower)) {
									try {
										phaser.register();
										Futures.addCallback(
												listeningExecutor.submit(new GetNarrowersCall(provider, narrower, true)),
												new GetNarrowersCallback(provider, narrower, true), listeningExecutor);
									} catch (Throwable e) {
										logScheduleError(e);
									}
								}
							} catch (SemanticDataSourceException e) {
								// log and ignore error
								log.warn("Failed to execute hasSuitableNamespace(" + this.iri + ") on " + provider
										+ ". Ignoring failure and continue.", e);
							}
						}
						for (SemanticDataSource provider : narrowersProviders) {
							try {
								if (provider.hasSuitableNamespace(narrower)) {
									try {
										phaser.register();
										Futures.addCallback(
												listeningExecutor.submit(new GetNarrowersCall(provider, narrower, false)),
												new GetNarrowersCallback(provider, narrower, false), listeningExecutor);
									} catch (Throwable e) {
										logScheduleError(e);
									}
								}
							} catch (SemanticDataSourceException e) {
								// log and ignore error
								log.warn("Failed to execute hasSuitableNamespace(" + this.iri + ") on " + provider
										+ ". Ignoring failure and continue.", e);
							}
						}
					}
				}
				phaser.arrive();
			}

			@Override
			public void onFailure(Throwable t) {
				phaser.arrive();
				// log and ignore error
				log.warn("Failed to execute " + ((this.all) ? "all" : "") + "Narrowers(" + this.iri + ") on "
						+ this.semanticDataSource + ". Ignoring failure and continue.", t);
			}
		}

		// schedule initial future
		try {
			phaser.register();
			Futures.addCallback(listeningExecutor.submit(new Callable<Collection<IRI>>() {
				@Override
				public Collection<IRI> call() {
					return Collections.singleton(iri);
				}
			}), new GetNarrowersCallback(this, iri, true));
		} catch (Throwable e) {
			logScheduleError(e);
		}

		// wait for all futures until timeout
		try {
			phaser.awaitAdvanceInterruptibly(0, 60, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			throw new SemanticDataSourceException("Failed to execute getAllNarrowers(" + iri + ").", e);
		}

		// remove given IRI from results
		narrowers.remove(iri);

		// return total results as unsynchronized collection
		return narrowersUnsynchronized;
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<String>>> futures = new ArrayList<Future<Collection<String>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingAlternativeLabels() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getAlternativeLabels(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<String> results = new HashSet<String>();
		for (Future<Collection<String>> future : futures) {
			results.addAll(ensureCollection(future, "getAlternativeLabels(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<IRI>>> futures = new ArrayList<Future<Collection<IRI>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingBroaders() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getBroaders(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<IRI> results = new HashSet<IRI>();
		for (Future<Collection<IRI>> future : futures) {
			results.addAll(ensureCollection(future, "getBroaders(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<String>>> futures = new ArrayList<Future<Collection<String>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingDescriptions() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getDescriptions(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<String> results = new HashSet<String>();
		for (Future<Collection<String>> future : futures) {
			results.addAll(ensureCollection(future, "getDescriptions(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<String>>> futures = new ArrayList<Future<Collection<String>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingLabels() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getLabels(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<String> results = new HashSet<String>();
		for (Future<Collection<String>> future : futures) {
			results.addAll(ensureCollection(future, "getLabels(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		return getMatchesSpecifiedAdapter(terms, this.adapters);
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms, Collection<IRI> scopes)
			throws SemanticDataSourceException {
		return getMatchesSpecifiedAdapter(terms, getRelevantAdapters(scopes));
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		return getMatchesSpecifiedAdapter(term, this.adapters);
	}

	@Override
	public Map<IRI, Double> getMatches(String term, Collection<IRI> scopes) throws SemanticDataSourceException {
		return getMatchesSpecifiedAdapter(term, getRelevantAdapters(scopes));
	}

	/**
	 * Matches a given collection of terms with the concepts of a given collection
	 * of adapters data sources.
	 * 
	 * @param terms
	 *            collection of terms to match
	 * @param adapters
	 *            collection of adapters to use
	 * @return a MultiMap of terms, IRIs and there suitability ranking
	 * @throws SemanticDataSourceException
	 */
	private Map<String, Map<IRI, Double>> getMatchesSpecifiedAdapter(Collection<String> terms,
			Collection<SemanticDataSource> adapters) throws SemanticDataSourceException {
		Collection<Future<Map<String, Map<IRI, Double>>>> futures = new ArrayList<Future<Map<String, Map<IRI, Double>>>>();
		for (SemanticDataSource adapter : adapters) {
			try {
				if (adapter.providingMatch()) {
					futures.add(this.executor.submit(() -> {
						return adapter.getMatches(terms);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();
		for (Future<Map<String, Map<IRI, Double>>> future : futures) {
			Map<String, Map<IRI, Double>> result = ensureMap(future, "getMatchesSpecifiedAdapter(" + terms + ")");
			for (String key : result.keySet()) {
				results.putIfAbsent(key, new HashMap<IRI, Double>());
				results.get(key).putAll(result.get(key));
			}
		}
		return results;
	}

	/**
	 * Matches a given term with the concepts of a given collection of adapters data
	 * sources.
	 * 
	 * @param term
	 *            the term to match
	 * @param adapters
	 *            collection of adapters to use
	 * @return a Map of IRIs and there suitability ranking
	 * @throws SemanticDataSourceException
	 */
	private Map<IRI, Double> getMatchesSpecifiedAdapter(String term, Collection<SemanticDataSource> adapters)
			throws SemanticDataSourceException {
		Collection<Future<Map<IRI, Double>>> futures = new ArrayList<Future<Map<IRI, Double>>>();
		for (SemanticDataSource adapter : adapters) {
			try {
				if (adapter.providingMatch()) {
					futures.add(this.executor.submit(() -> {
						return adapter.getMatches(term);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Map<IRI, Double> results = new HashMap<IRI, Double>();
		for (Future<Map<IRI, Double>> future : futures) {
			results.putAll(ensureMap(future, "getMatchesSpecifiedAdapter(\"" + term + "\")"));
		}
		return results;
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.namespaces);
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<IRI>>> futures = new ArrayList<Future<Collection<IRI>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingNarrowers() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getNarrowers(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<IRI> results = new HashSet<IRI>();
		for (Future<Collection<IRI>> future : futures) {
			results.addAll(ensureCollection(future, "getNarrowers(\"" + iri + "\")"));
		}
		return results;
	}

	/**
	 * Returns a collection of relevant adapters for a given collection of scopes.
	 * 
	 * @param scopes
	 *            a collection of scopes
	 * @return a collection of relevant adapters
	 */
	private Collection<SemanticDataSource> getRelevantAdapters(Collection<IRI> scopes) {
		Set<SemanticDataSource> relevantAdapters = new HashSet<SemanticDataSource>();
		for (IRI scope : scopes) {
			relevantAdapters.addAll(adaptersPerScoupe.getOrDefault(scope, new HashSet<SemanticDataSource>(0)));
		}
		return relevantAdapters;
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<IRI>>> futures = new ArrayList<Future<Collection<IRI>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingDeprecation() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getReplacedBy(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<IRI> results = new HashSet<IRI>();
		for (Future<Collection<IRI>> future : futures) {
			results.addAll(ensureCollection(future, "getReplacedBy(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public Collection<IRI> getScopes() {
		return Collections.unmodifiableCollection(this.adaptersPerScoupe.keySet());
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		Collection<Future<Collection<IRI>>> futures = new ArrayList<Future<Collection<IRI>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingSignature()) {
					futures.add(this.executor.submit(() -> {
						return adapter.getSignature();
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<IRI> results = new HashSet<IRI>();
		for (Future<Collection<IRI>> future : futures) {
			results.addAll(ensureCollection(future, "getSignature()"));
		}
		return results;
	}

	@Override
	public Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps) throws SemanticDataSourceException {
		return getSuggestionsSpecifiedAdapter(stumps, this.adapters);
	}

	@Override
	public Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps, Collection<IRI> scopes)
			throws SemanticDataSourceException {
		return getSuggestionsSpecifiedAdapter(stumps, getRelevantAdapters(scopes));
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		return getSuggestionsSpecifiedAdapter(stump, this.adapters);
	}

	@Override
	public Map<IRI, String> getSuggestions(String term, Collection<IRI> scopes) throws SemanticDataSourceException {
		return getSuggestionsSpecifiedAdapter(term, getRelevantAdapters(scopes));
	}

	/**
	 * Suggest completions for a given collection of term stumps based on the
	 * concepts of a given collection of adapters data sources.
	 * 
	 * @param stumps
	 *            collection of term stumps to complete
	 * @param adapters
	 *            collection of adapters to use
	 * @return a MultiMap of term stumps, IRIs and their corresponding label that is
	 *         a completion of the term stump
	 * @throws SemanticDataSourceException
	 */
	private Map<String, Map<IRI, String>> getSuggestionsSpecifiedAdapter(Collection<String> stumps,
			Collection<SemanticDataSource> adapters) throws SemanticDataSourceException {
		Collection<Future<Map<String, Map<IRI, String>>>> futures = new ArrayList<Future<Map<String, Map<IRI, String>>>>();
		for (SemanticDataSource adapter : adapters) {
			try {
				if (adapter.providingSuggest()) {
					futures.add(this.executor.submit(() -> {
						return adapter.getSuggestions(stumps);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Map<String, Map<IRI, String>> results = new HashMap<String, Map<IRI, String>>();
		for (Future<Map<String, Map<IRI, String>>> future : futures) {
			Map<String, Map<IRI, String>> result = ensureMap(future, "getSuggestionsSpecifiedAdapter(" + stumps + ")");
			for (String key : result.keySet()) {
				results.putIfAbsent(key, new HashMap<IRI, String>());
				results.get(key).putAll(result.get(key));
			}
		}
		return results;
	}

	/**
	 * Suggest a completion for a given term stump based on the concepts of a given
	 * collection of adapters data sources.
	 * 
	 * @param stump
	 *            the term stump to complete
	 * @param adapters
	 *            collection of adapters to use
	 * @return a Map of IRIs and their corresponding label that is a completion of
	 *         the term stump
	 * @throws SemanticDataSourceException
	 */
	private Map<IRI, String> getSuggestionsSpecifiedAdapter(String stump, Collection<SemanticDataSource> adapters)
			throws SemanticDataSourceException {
		Collection<Future<Map<IRI, String>>> futures = new ArrayList<Future<Map<IRI, String>>>();
		for (SemanticDataSource adapter : adapters) {
			try {
				if (adapter.providingSuggest()) {
					futures.add(this.executor.submit(() -> {
						return adapter.getSuggestions(stump);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Map<IRI, String> results = new HashMap<IRI, String>();
		for (Future<Map<IRI, String>> future : futures) {
			results.putAll(ensureMap(future, "getSuggestionsSpecifiedAdapter(\"" + stump + "\")"));
		}
		return results;
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<IRI>>> futures = new ArrayList<Future<Collection<IRI>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingSynonyms() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getSynonyms(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		Set<IRI> results = new HashSet<IRI>();
		for (Future<Collection<IRI>> future : futures) {
			results.addAll(ensureCollection(future, "getSynonyms(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		Collection<Future<Collection<URL>>> futures = new ArrayList<Future<Collection<URL>>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingURLs() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.getUrls(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		List<URL> results = new ArrayList<URL>();
		for (Future<Collection<URL>> future : futures) {
			results.addAll(ensureCollection(future, "getUrls(\"" + iri + "\")"));
		}
		return results;
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		// TODO improve parallel execution (do not wait for falses)
		Collection<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.providingDeprecation() && adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.isDeprecated(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		for (Future<Boolean> future : futures) {
			if (ensureBoolean(future, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		// TODO improve parallel execution (do not wait for falses)
		Collection<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
		for (SemanticDataSource adapter : this.adapters) {
			try {
				if (adapter.hasSuitableNamespace(iri)) {
					futures.add(this.executor.submit(() -> {
						return adapter.isPresent(iri);
					}));
				}
			} catch (Throwable e) {
				logScheduleError(e);
			}
		}
		for (Future<Boolean> future : futures) {
			if (ensureBoolean(future, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void maintain() throws MaintenanceException {
		for (SemanticDataSource adapter : this.adapters) {
			if (adapter instanceof Maintainable) {
				try {
					((Maintainable) adapter).maintain();
				} catch (MaintenanceException e) {
					log.warn("Failed to maintain \"" + adapter.toString() + "\".", e);
				}
			}
			registerNamespaces(adapter);
			registerScopes(adapter);
		}
	}

	@Override
	public boolean providingAllBroaders() {
		return true;
	}

	@Override
	public boolean providingAllNarrowers() {
		return true;
	}

	@Override
	public boolean providingAlternativeLabels() {
		return true;
	}

	@Override
	public boolean providingBroaders() {
		return true;
	}

	@Override
	public boolean providingDeprecation() {
		return true;
	}

	@Override
	public boolean providingDescriptions() {
		return true;
	}

	@Override
	public boolean providingLabels() {
		return true;
	}

	@Override
	public boolean providingMatch() {
		return true;
	}

	@Override
	public boolean providingNarrowers() {
		return true;
	}

	@Override
	public boolean providingSignature() {
		return true;
	}

	@Override
	public boolean providingSuggest() {
		return true;
	}

	@Override
	public boolean providingSynonyms() {
		return true;
	}

	@Override
	public boolean providingURLs() {
		return true;
	}

	/**
	 * Registers the given {@link SemanticDataSource}.
	 * 
	 * @param adapter
	 *            the {@link SemanticDataSource} to register
	 * @param ranking
	 *            the ranking of the adapter
	 */
	public void registerAdapter(SemanticDataSource adapter) {
		if (adapter instanceof SemanticDataSourceManager) {
			// adapter is an AdapterManager (this can cause endless loops)
			throw new IllegalArgumentException("Not allowed to register an AdapterManager.");
		}

		// add adapter
		this.adapters.add(adapter);

		registerNamespaces(adapter);
		registerScopes(adapter);
	}

	/**
	 * Registers the given {@link SemanticDataSource}s.
	 * 
	 * @param adapters
	 *            {@link Collection} of {@link SemanticDataSource}s to register
	 */
	public void registerAdapters(Collection<SemanticDataSource> adapters) throws SemanticDataSourceException {
		for (SemanticDataSource adapter : adapters) {
			this.registerAdapter(adapter);
		}
	}

	private void registerNamespaces(SemanticDataSource adapter) {
		try {
			// add namespaces
			this.namespaces.addAll(adapter.getNamespaces());
		} catch (SemanticDataSourceException e) {
			log.error("Failed to register namespace of adapter.", e);
			log.info("Trying again during next maintenance.");
		}
	}

	private void registerScopes(SemanticDataSource adapter) {
		try {
			// add adapter to scope specific sets
			for (IRI scope : adapter.getScopes()) {
				this.adaptersPerScoupe.putIfAbsent(scope, new HashSet<SemanticDataSource>());
				this.adaptersPerScoupe.get(scope).add(adapter);
			}
		} catch (SemanticDataSourceException e) {
			log.error("Failed to register scopes of adapter.", e);
			log.info("Trying again during next maintenance.");
		}
	}

	@Override
	public void setMatchThreshold(double threshold) {
		for (SemanticDataSource adapter : this.adapters) {
			if (adapter.providingMatch()) {
				adapter.setMatchThreshold(threshold);
			}
		}
	}
}
