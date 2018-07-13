package de.uni_jena.cs.fusion.semantic.datasource.gfbio;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.MoreExecutors;

import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.BroaderResults;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.Capabilities;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.HierarchyResults;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.ListResult;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.MatchType;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.NarrowerResults;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.SearchResults;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.Service;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.SingleTerminologyResult;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.SuggestResults;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.TermInformationResult;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.TermResult;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.TerminologyCapabilitiesResult;
import de.uni_jena.cs.fusion.gfbio.terminologyserver.client.TerminologyServerClient;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllBroadersUsingBroaders;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllNarrowersUsingNarrowers;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingBroadersUsingAllBroaders;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingNarrowersUsingAllNarrowers;
import de.uni_jena.cs.fusion.similarity.TrieJaroWinklerSimilarityMatcher;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;
import de.uni_jena.cs.fusion.util.stopwords.StopWords;

/**
 * <p>
 * An {@link SemanticDataSource} implementing the
 * <a href="http://terminologies.gfbio.org/api/">GFBio Terminology Server
 * API</a>.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public class TerminologyServerDataSource implements SemanticDataSourceProvidingAllBroadersUsingBroaders,
		SemanticDataSourceProvidingAllNarrowersUsingNarrowers, SemanticDataSourceProvidingNarrowersUsingAllNarrowers,
		SemanticDataSourceProvidingBroadersUsingAllBroaders, Maintainable {

	private final static int HTTP_TIMEOUT = 30000; // milliseconds
	private final static int HTTP_MAX_CONNECTIONS = 10;
	private final static int HTTP_CACHE_MAX_OBJECT_NUMBER = 10000;
	private final static int HTTP_CACHE_MAX_OBJECT_AGE = 604800; // seconds
	private final static int HTTP_CACHE_MAX_OBJECT_SIZE = 8192; // bytes

	private final static Logger logger = Logger.getLogger(TerminologyServerDataSource.class.getName());

	private static TerminologyServerClient client;
	{
		CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(HTTP_CACHE_MAX_OBJECT_NUMBER)
				.setMaxObjectSize(HTTP_CACHE_MAX_OBJECT_SIZE).setHeuristicCachingEnabled(true)
				.setHeuristicDefaultLifetime(HTTP_CACHE_MAX_OBJECT_AGE).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HTTP_TIMEOUT)
				.setSocketTimeout(HTTP_TIMEOUT).build();
		@SuppressWarnings("resource")
		CloseableHttpClient httpClient = CachingHttpClients.custom().setCacheConfig(cacheConfig)
				.setDefaultRequestConfig(requestConfig).setMaxConnTotal(HTTP_MAX_CONNECTIONS).build();
		client = new TerminologyServerClient("https://terminologies.gfbio.org/api/terminologies/", httpClient);
	}

	private final static Cache<IRI, String> labels = CacheBuilder.newBuilder().maximumSize(10000)
			.expireAfterAccess(7, TimeUnit.DAYS).build();
	private final static Map<MatchType, Collection<String>> terminologiesBySearchMode = new HashMap<MatchType, Collection<String>>();
	private final static Collection<String> suggestTerminologies = new HashSet<String>();
	private static ExecutorService executor = MoreExecutors
			.listeningDecorator(Executors.newSingleThreadScheduledExecutor());

	/**
	 * Returns a <code>Collection</code> of <code>Adapter</code>s for each
	 * terminology in the GFBio Terminology Server except the excluded
	 * terminologies.
	 * 
	 * @param excludedTerminologies
	 *            <code>Collection</code> of terminologies to exclude
	 * @return <code>Collection</code> of the <code>Adapter</code>s
	 * @throws SemanticDataSourceException
	 */
	public static Collection<SemanticDataSource> getAllAdapters(Collection<String> excludedTerminologies)
			throws SemanticDataSourceException {
		try {
			ListResult result = client.terminologies();
			TerminologyServerClient.requireNoError(result);
			return result.results.stream().filter(ti -> !excludedTerminologies.contains(ti.acronym)).map(ti -> {
				return new TerminologyServerDataSource(ti.acronym);
			}).collect(Collectors.toSet());
		} catch (Throwable e) {
			throw new SemanticDataSourceException(e);
		}
	}

	public static void setExecutor(ExecutorService executor) {
		TerminologyServerDataSource.executor = executor;
	}

	private final String terminology;
	private Collection<String> namespaces;
	private Collection<Service> services;
	private Collection<MatchType> searchModes;
	private Collection<IRI> scopes;
	private double matchThreshold = 0.95;

	private boolean initialized;

	public TerminologyServerDataSource(String terminology) {
		this.terminology = terminology;
		try {
			this.load();
		} catch (SemanticDataSourceException e) {
			TerminologyServerDataSource.logger.log(Level.SEVERE,
					"Failed to load terminology \"" + this.terminology + "\".", e);
			TerminologyServerDataSource.logger.info("Trying again during next maintenance.");
		}
	}

	private void load() throws SemanticDataSourceException {
		try {
			// load terminology capabilities
			TerminologyCapabilitiesResult capabilitiesResults = client.capabilities(terminology);
			TerminologyServerClient.requireNoError(capabilitiesResults);
			Capabilities capabilities = capabilitiesResults.results.iterator().next();
			this.services = capabilities.availableServices;

			if (this.services.contains(Service.search)) {
				this.searchModes = capabilities.searchModes;
			} else {
				this.searchModes = Collections.emptyList();
			}
			terminologiesBySearchMode.putIfAbsent(this.preferredSearchMode(), new HashSet<String>());
			terminologiesBySearchMode.get(this.preferredSearchMode()).add(this.terminology);

			if (this.services.contains(Service.suggest)) {
				suggestTerminologies.add(terminology);
			}

			// load terminology information
			SingleTerminologyResult informationResults = client.terminology(terminology);
			TerminologyServerClient.requireNoError(informationResults);
			Map<String, Collection<String>> information = informationResults.results.iterator().next();

			// cache scopes
			this.scopes = information.get("hasDomain").stream().map(d -> IRI.create(d)).collect(Collectors.toList());

			// cache namespaces
			switch (this.terminology) {
			// TODO remove if namespaces is granted for each terminology
			case "CHEBI":
				this.namespaces = Collections.singleton("http://purl.obolibrary.org/obo/CHEBI");
				break;
			default:
				this.namespaces = information.get("namespaces");
			}

			this.initialized = true;

		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to initialize adapter for \"" + terminology + "\".", e);
		}
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.allterms);
			TermResult result = client.allTerms(terminology);
			TerminologyServerClient.requireNoError(result);
			return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get signature of " + terminology + ".", e);
		}
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			if (this.services.contains(Service.allbroader)) {
				BroaderResults result = client.allBroader(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
			} else if (this.initialized && this.services.contains(Service.hierarchy)) {
				HierarchyResults result = client.hierarchy(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
			} else if (this.initialized && this.services.contains(Service.broader)) {
				// getBroaderConcepts fall back
				return SemanticDataSourceProvidingAllBroadersUsingBroaders.super.getAllBroaders(iri);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get all broaders from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			if (this.services.contains(Service.allnarrower)) {
				NarrowerResults result = client.allNarrower(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
			} else if (this.initialized && this.services.contains(Service.narrower)) {
				// getNarrowerConcepts fall back
				return SemanticDataSourceProvidingAllNarrowersUsingNarrowers.super.getAllNarrowers(iri);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get all narrowers from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.term);
			TermInformationResult result = client.term(terminology, iri.getIRIString());
			TerminologyServerClient.requireNoError(result);
			return result.results.iterator().next().getOrDefault("synonyms", Collections.emptyList());
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get alternative labels from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			if (this.services.contains(Service.broader)) {
				BroaderResults result = client.broader(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
			} else if (this.initialized && this.services.contains(Service.allbroader)) {
				// getAllBroaderConcepts fall back
				return SemanticDataSourceProvidingBroadersUsingAllBroaders.super.getBroaders(iri);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get broaders from " + terminology + ".", e);
		}
	}

	@Override
	public int getDataUID() {
		return this.terminology.hashCode();
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		try {
			String label = labels.getIfPresent(iri);
			if (label == null) {
				requireInitialization();
				requireService(Service.term);
				TermInformationResult result = client.term(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.iterator().next().getOrDefault("label", Collections.emptyList());
			} else {
				return Collections.singletonList(label);
			}
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get labels from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		requireInitialization();
		try {
			return Collections.unmodifiableCollection(this.namespaces);
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get namespaces from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			if (this.services.contains(Service.narrower)) {
				NarrowerResults result = client.narrower(terminology, iri.getIRIString());
				TerminologyServerClient.requireNoError(result);
				return result.results.stream().map(t -> IRI.create(t.uri)).collect(Collectors.toSet());
			} else if (this.initialized && this.services.contains(Service.allnarrower)) {
				// getAllNarrowerConcepts fall back
				return SemanticDataSourceProvidingNarrowersUsingAllNarrowers.super.getNarrowers(iri);
			} else {
				throw new UnsupportedOperationException();
			}
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get narrowers from " + terminology + ".", e);
		}
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		requireInitialization();
		try {
			return Collections.unmodifiableCollection(this.scopes);
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get scopes from " + terminology + ".", e);
		}
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.term);
			TermInformationResult result = client.term(terminology, iri.getIRIString());
			TerminologyServerClient.requireNoError(result);
			Map<String, Collection<String>> term = result.results.iterator().next();
			List<URL> urls = new ArrayList<URL>();
			// TODO do not provide URI as URL?
			for (String url : (term.containsKey("url")) ? term.get("url") : term.get("uri")) {
				urls.add(new URL(url));
			}
			return urls;
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get concept URLs from " + terminology + ".", e);
		}
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.term);
			TermInformationResult result = client.term(terminology, iri.getIRIString());
			TerminologyServerClient.requireNoError(result);
			return result.results.iterator().hasNext();
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to determine presence in " + terminology + ".", e);
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.search);
			MatchType searchMode = this.preferredSearchMode();
			String[] termParts = term.split(" ");
			String query;
			switch (searchMode) {
			case included:
				// only search for first word(s) (speeds up by saving
				// requests)
				if (!StopWords.isStopWord(termParts[0])) {
					query = termParts[0];
				} else if (termParts.length > 1) {
					query = termParts[0] + termParts[1];
				} else {
					return Collections.emptyMap();
				}
				break;
			case exact:
				if (!StopWords.isStopWord(term)) {
					query = term;
				} else {
					return Collections.emptyMap();
				}
				break;
			case regex:
				// only search for first word(s) (speeds up by saving
				// requests)
				if (!StopWords.isStopWord(termParts[0])) {
					query = termParts[0];
				} else if (termParts.length > 1) {
					query = termParts[0] + termParts[1];
				} else {
					return Collections.emptyMap();
				}
				query = "^" + query;
				break;
			// TODO add method "synonyms"
			default:
				return Collections.emptyMap();
			}

			// get search result for all active terminologies
			SearchResults result = client.search(terminologiesBySearchMode.get(searchMode), query, searchMode);
			TerminologyServerClient.requireNoError(result);
			Map<IRI, Double> match = new HashMap<IRI, Double>();
			result.results.stream()
					// filter results for given terminology
					.filter(r -> r.sourceTerminology.equals(terminology))
					// remove results without IRI or label
					.filter(r -> Objects.nonNull(r.uri) && Objects.nonNull(r.label)).forEach(r -> {
						// get iri
						IRI iri = IRI.create(r.uri);
						// get rating
						double rating;
						if (searchMode == MatchType.exact) {
							rating = 1.0;
						} else {
							rating = TrieJaroWinklerSimilarityMatcher.match(term, r.label);
						}

						if (rating >= this.matchThreshold) {
							// add to match
							match.put(iri, rating);
							// add to label cache
							labels.put(iri, r.label);
						}
					});
			return match;
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get matches from " + terminology + ".", e);
		}
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		requireInitialization();
		Map<String, Future<Map<IRI, Double>>> futures = new HashMap<String, Future<Map<IRI, Double>>>();
		for (String term : terms) {
			futures.put(term, executor.submit(new Callable<Map<IRI, Double>>() {
				@Override
				public Map<IRI, Double> call() throws Exception {
					return getMatches(term);
				}
			}));
		}

		Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();
		for (String term : futures.keySet()) {
			try {
				Map<IRI, Double> result = futures.get(term).get();
				if (!result.isEmpty()) {
					results.put(term, result);
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new SemanticDataSourceException(e);
			}
		}

		return results;
	}

	private MatchType preferredSearchMode() throws SemanticDataSourceException {
		if (this.searchModes.contains(MatchType.included)) {
			return MatchType.included;
		} else if (this.searchModes.contains(MatchType.regex)) {
			return MatchType.regex;
		} else if (this.searchModes.contains(MatchType.exact)) {
			return MatchType.exact;
			// } else if (this.searchModes.contains(MatchType.synonyms)) {
			// return MatchType.synonyms;
		} else {
			throw new SemanticDataSourceException(
					"Failed to determine prefferred search mode for " + terminology + ".");
		}
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		requireInitialization();
		try {
			requireService(Service.suggest);
			// TODO fix: also provides results containing the stump, but not at
			// the end

			if (stump.length() < 4) {
				// stump fails to comply min length by terminology server
				return Collections.emptyMap();
			} else if (stump.contains(" ") && stump.length() - (stump.indexOf(" ") + 1) < 4) {
				// TODO remote fix required (#4): remove case
				return Collections.emptyMap();
			} else if (stump.contains(" ")) {
				String suffix = stump.substring(stump.indexOf(" ") + 1, stump.length());
				if (suffix.length() < 4 && getSuggestions(suffix).entrySet().isEmpty()) {
					// suffix of the stump can not by completed
					return Collections.emptyMap();
				}
			}

			// get results for all active terminologies
			SuggestResults result = client.suggest(suggestTerminologies, stump);
			TerminologyServerClient.requireNoError(result);
			Map<IRI, String> filteredResults = new HashMap<IRI, String>();
			result.results.stream()
					// filter results for given terminology
					.filter(s -> s.sourceTerminology.equals(terminology))
					.forEach(s -> filteredResults.put(IRI.create(s.uri), s.label));
			return filteredResults;
		} catch (Throwable e) {
			throw new SemanticDataSourceException("Failed to get suggestions from " + terminology + ".", e);
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		if (!this.initialized) {
			try {
				this.load();
			} catch (SemanticDataSourceException e) {
				throw new MaintenanceException(e);
			}
		}
	}

	@Override
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
	}

	@Override
	public boolean providingSignature() {
		return this.services.contains(Service.allterms);
	}

	@Override
	public boolean providingAllBroaders() {
		return this.services.contains(Service.allbroader) || this.services.contains(Service.hierarchy)
				|| this.services.contains(Service.broader);
	}

	@Override
	public boolean providingBroaders() {
		return this.services.contains(Service.allbroader) || services.contains(Service.broader);
	}

	@Override
	public boolean providingAllNarrowers() {
		return this.services.contains(Service.allnarrower) || this.services.contains(Service.narrower);
	}

	@Override
	public boolean providingNarrowers() {
		return this.services.contains(Service.allnarrower) || this.services.contains(Service.narrower);
	}

	@Override
	public boolean providingSuggest() {
		return this.services.contains(Service.suggest);
	}

	@Override
	public boolean providingMatch() {
		return this.services.contains(Service.search);
	}

	@Override
	public boolean providingLabels() {
		return this.services.contains(Service.term);
	}

	@Override
	public boolean providingURLs() {
		return this.services.contains(Service.term);
	}

	@Override
	public boolean providingAlternativeLabels() {
		return this.services.contains(Service.term);
	}

	private void requireInitialization() throws SemanticDataSourceException {
		if (!this.initialized) {
			throw new SemanticDataSourceException("Not initialized for " + terminology + ".");
		}
	}

	private void requireService(Service service) throws UnsupportedOperationException {
		if (!this.services.contains(service)) {
			throw new UnsupportedOperationException("Service not supported by " + terminology + ".");
		}
	}
}
