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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * A {@link SemanticDataSource} provides semantic concepts from some kind of
 * data source. A {@link SemanticDataSource} at least has to provide namespace
 * ({@link #getNamespaces()}), scope ({@link #getScopes()}) and concept presence
 * ({@link #isPresent(IRI)}). If one {@link SemanticDataSource} implementation
 * provides access to multiple data sources it must also implement the method
 * {@link #getDataUID()}.
 * </p>
 * <p>
 * A {@link SemanticDataSource} might also provide further data. This must by
 * indicated be the respective {@code providing*()} method.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 */
public interface SemanticDataSource {

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of all broader concepts of
	 * the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingAllBroaders}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of {@link IRI}s of all broader concepts of the
	 *         given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of all narrower concepts of
	 * the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingAllBroaders}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of {@link IRI}s of all narrower concepts of
	 *         the given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of the alternative labels of the given
	 * concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingAlternativeLabels()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the alternative labels of the given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of the concepts that are one
	 * level broader than a given one.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingBroaders()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of {@link IRI}s of the concepts that are one
	 *         level broader than a given one
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of names of available custom relation
	 * properties.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingCustomProperties()}.
	 * </p>
	 * 
	 * @return {@link Collection} of names of available custom relation
	 *         properties
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getCustomRelationProperties() throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of a custom relation
	 * property of the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingCustomProperties()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param property
	 *            name of the custom property
	 * @return {@link Collection} of {@link IRI}s of a custom relation property
	 *         of the given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getCustomRelations(IRI iri, String property) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of names of available custom value
	 * properties.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingCustomProperties()}.
	 * </p>
	 * 
	 * @return {@link Collection} of names of available custom value properties
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getCustomValueProperties() throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of values of a custom value property of the
	 * given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingCustomProperties()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param property
	 *            name of the custom property
	 * @return {@link Collection} of values of a custom value property of the
	 *         given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getCustomValues(IRI iri, String property) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	};

	/**
	 * Returns a unique identifier of the {@link SemanticDataSource}s data. This
	 * method will be used by {@link #getUID} to distinguish
	 * {@link SemanticDataSource} instances of the same class that provide
	 * different data. The identifier must be unique in scope of the
	 * {@link SemanticDataSource} class.
	 * 
	 * @return a unique identifier of the data
	 */
	default int getDataUID() {
		return 0;
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of the descriptions of the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingDescriptions()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the descriptions of the given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	};

	/**
	 * <p>
	 * Returns an {@link Optional} containing one label of the given concept, if
	 * any label exits.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingLabels()}.
	 * </p>
	 * 
	 * <p>
	 * <b>Note</b>: It is not guaranteed that this method always returns the
	 * same label of the concept.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Optional} containing one label of the given concept
	 * @throws SemanticDataSourceException
	 */
	default Optional<String> getLabel(IRI iri) throws SemanticDataSourceException {
		Collection<String> labels = this.getLabels(iri);
		if (labels.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(labels.iterator().next());
		}
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of the labels of the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingLabels()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the labels of the given concept
	 * @throws SemanticDataSourceException
	 */
	default Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Matches a given {@link Collection} of terms with the concepts of this
	 * {@link SemanticDataSource}. The matching concepts are rated between 0
	 * (worst) and 1 (best).
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingMatch()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getMatches(String)}. This may be overwrite
	 * if the Semantic Data Source being implemented admits a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @param terms
	 *            {@link Collection} of terms to match
	 * @return a MultiMap of terms, {@link IRI}s and their rating
	 * @throws SemanticDataSourceException
	 */
	default Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();
		for (String term : terms) {
			Map<IRI, Double> result = getMatches(term);
			if (!result.isEmpty()) {
				results.put(term, result);
			}
		}
		return results;
	}

	/**
	 * <p>
	 * Matches a given {@link Collection} of terms with the concepts of this
	 * {@link SemanticDataSource} in the given scopes. The matching concepts are
	 * rated between 0 (worst) and 1 (best).
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingMatch()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getMatches(String, Collection)}. This may be
	 * overwrite if the Semantic Data Source being implemented admits a more
	 * efficient implementation.
	 * </p>
	 * 
	 * @param terms
	 *            {@link Collection} of terms to match
	 * @param scopes
	 *            {@link Collection} of scopes
	 * @return a MultiMap of terms, {@link IRI}s and their rating
	 * @throws SemanticDataSourceException
	 */
	default Map<String, Map<IRI, Double>> getMatches(Collection<String> terms, Collection<IRI> scopes)
			throws SemanticDataSourceException {
		Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();
		for (String term : terms) {
			Map<IRI, Double> result = getMatches(term, scopes);
			if (!result.isEmpty()) {
				results.put(term, result);
			}
		}
		return results;
	}

	/**
	 * <p>
	 * Matches a given term with the concepts of this
	 * {@link SemanticDataSource}. The matching concepts are rated between 0
	 * (worst) and 1 (best).
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingMatch()}.
	 * </p>
	 * 
	 * @param term
	 *            the term to match
	 * @return a Map of {@link IRI}s and their rating
	 * @throws SemanticDataSourceException
	 */
	default Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Matches a given term with the concepts of this {@link SemanticDataSource}
	 * in the given scopes. The matching concepts are rated between 0 (worst)
	 * and 1 (best).
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingMatch()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getMatches(Collection, Collection)} and
	 * {@link #hasSuitableScoupe(Collection)}. This may be overwrite if the
	 * Semantic Data Source being implemented admits a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @param term
	 *            the term to match
	 * @param scopes
	 *            {@link Collection} of scopes
	 * @return a Map of {@link IRI}s and their rating
	 * @throws SemanticDataSourceException
	 */
	default Map<IRI, Double> getMatches(String term, Collection<IRI> scopes) throws SemanticDataSourceException {
		if (this.hasSuitableScoupe(scopes)) {
			return this.getMatches(term);
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * 
	 * @return {@link Collection} of the namespaces of the available concepts
	 */
	Collection<String> getNamespaces() throws SemanticDataSourceException;

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of the concepts that are one
	 * level narrower than a given one.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingNarrowers()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of {@link IRI}s of the concepts that are one
	 *         level narrower than a given one
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of the {@link IRI}s replacing the given
	 * {@link IRI}.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #getReplacedBy(IRI)}.
	 * </p>
	 * 
	 * @param iri
	 *            a deprecated {@link IRI}
	 * @return {@link Collection} of the {@IRI}s replacing the given {@link IRI}
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return {@link Collection} of the scope {@link IRI}s of the available
	 *         concepts
	 */
	Collection<IRI> getScopes() throws SemanticDataSourceException;

	/**
	 * <p>
	 * Returns a {@link Collection} of the {@link IRI}s of the available
	 * concepts.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSignature()}.
	 * </p>
	 * 
	 * @return {@link Collection} of the {@link IRI}s of the available concepts
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getSignature() throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Suggest completions for a given {@link Collection} of term stumps based
	 * on the concepts of this {@link SemanticDataSource}.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSuggest()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getSuggestions(String)}. This may be
	 * overwrite if the Semantic Data Source being implemented admits a more
	 * efficient implementation.
	 * </p>
	 * 
	 * @param stumps
	 *            {@link Collection} of term stumps to complete
	 * @return a MultiMap of term stumps, {@link IRI}s and their corresponding
	 *         label that is a completion of the term stump
	 * @throws SemanticDataSourceException
	 */
	default Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps) throws SemanticDataSourceException {
		Map<String, Map<IRI, String>> results = new HashMap<String, Map<IRI, String>>();
		for (String stump : stumps) {
			Map<IRI, String> result = getSuggestions(stump);
			if (!result.isEmpty()) {
				results.put(stump, result);
			}
		}
		return results;
	}

	/**
	 * <p>
	 * Suggest completions for a given {@link Collection} of term stumps based
	 * on the concepts of this {@link SemanticDataSource} in the given scopes.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSuggest()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getSuggestions(String, Collection)}. This
	 * may be overwrite if the Semantic Data Source being implemented admits a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @param stumps
	 *            {@link Collection} of term stumps to complete
	 * @param scopes
	 *            {@link Collection} of scopes
	 * @return a MultiMap of term stumps, {@link IRI}s and their corresponding
	 *         label that is a completion of the term stump
	 * @throws SemanticDataSourceException
	 */
	default Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps, Collection<IRI> scopes)
			throws SemanticDataSourceException {
		Map<String, Map<IRI, String>> results = new HashMap<String, Map<IRI, String>>();
		for (String stump : stumps) {
			Map<IRI, String> result = getSuggestions(stump, scopes);
			if (!result.isEmpty()) {
				results.put(stump, result);
			}
		}
		return results;
	}

	/**
	 * <p>
	 * Suggest completions for a given term stump based on the concepts of this
	 * {@link SemanticDataSource}.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSuggest()}.
	 * </p>
	 * 
	 * @param stump
	 *            the term stump to complete
	 * @return a Map of {@link IRI}s and their corresponding label that is a
	 *         completion of the term stump
	 * @throws SemanticDataSourceException
	 */
	default Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Suggest completions for a given term stump based on the concepts of this
	 * {@link SemanticDataSource} in the given scopes.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSuggest()}.
	 * </p>
	 * <p>
	 * The {@link SemanticDataSource} interface provides an default
	 * implementation using {@link #getSuggestions(Collection)} and
	 * {@link #hasSuitableScoupe(Collection)}. This may be overwrite if the
	 * Semantic Data Source being implemented admits a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @param stump
	 *            the term stump to complete
	 * @param scopes
	 *            {@link Collection} of scopes
	 * @return a Map of {@link IRI}s and their corresponding label that is a
	 *         completion of the term stump
	 * @throws SemanticDataSourceException
	 */
	default Map<IRI, String> getSuggestions(String stump, Collection<IRI> scopes) throws SemanticDataSourceException {
		if (this.hasSuitableScoupe(scopes)) {
			return this.getSuggestions(stump);
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * <p>
	 * Returns a {@link Collection} of {@link IRI}s of the concepts that are
	 * equivalent to a given one, except the given concept.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingSignature()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of {@link IRI}s of the concepts that are
	 *         equivalent to a given one
	 * @throws SemanticDataSourceException
	 */
	default Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a unique identifier of the {@link SemanticDataSource}s. The
	 * method {@link #getDataUID} will be utilize to distinguish
	 * {@link SemanticDataSource} instances of the same class that provide
	 * different data.
	 * 
	 * @return a unique identifier of the adapter
	 */
	default long getUID() {
		return (((long) getDataUID()) << 32) | (this.getClass().getName().hashCode() & 0xffffffffL);
	}

	/**
	 * <p>
	 * Returns a {@link List} of the URLs of the given concept.
	 * </p>
	 * <p>
	 * Note: The result <b>might not be distinct</b>. But avoid to use a
	 * {@link Set} of {@link URL}s. Literally unequal {@link URL}s might be
	 * considered as equal, because of resolving the hosts to IPs during
	 * comparison.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingURLs()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the URLs of the given concept
	 * @throws SemanticDataSourceException
	 */
	default List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns {@code true} if a namespace is known that suits for the given
	 * {@link IRI}, otherwise {@code false}.
	 * 
	 * @param iri
	 *            {@link IRI} to test for a suitable namespace
	 * @return {@code true} if a namespace is known that suits for the given
	 *         {@link IRI}, otherwise {@code false}
	 */
	default boolean hasSuitableNamespace(IRI iri) throws SemanticDataSourceException {
		String iriStr = iri.getIRIString();
		for (String namespace : getNamespaces()) {
			if (iriStr.startsWith(namespace)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if the given scope is covered, otherwise
	 * {@code false}.
	 * 
	 * @param scope
	 *            {@link IRI} of the given scope
	 * @return {@code true} if the given scope is covered, otherwise
	 *         {@code false}
	 */
	default boolean hasSuitableScoupe(IRI scope) throws SemanticDataSourceException {
		return this.getScopes().contains(scope);
	}

	/**
	 * Returns {@code true} if one of the given scopes is covered, otherwise
	 * {@code false}.
	 * 
	 * @param scopes
	 *            {@link Collection} of the {@link IRI}s of the given scopes
	 * @return {@code true} if the given scope is covered, otherwise
	 *         {@code false}
	 */
	default boolean hasSuitableScoupe(Collection<IRI> scopes) throws SemanticDataSourceException {
		Collection<IRI> presentScopes = this.getScopes();
		for (IRI scope : scopes) {
			if (presentScopes.contains(scope)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Returns {@code true} if the given {@IRI} is deprecated, otherwise
	 * {@code false}.
	 * </p>
	 * <p>
	 * This is an optional method. The implementation of this method is
	 * indicated by the method {@link #providingDeprecation()}.
	 * </p>
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@code true} if the given {@IRI} is deprecated, otherwise
	 *         {@code false}
	 * @throws SemanticDataSourceException
	 */
	default boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@code true} if information about the given concept is available,
	 *         otherwise {@code false}
	 * @throws SemanticDataSourceException
	 */
	boolean isPresent(IRI iri) throws SemanticDataSourceException;

	/**
	 * @return {@code true} if the method {@link #getAllBroaders(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingAllBroaders() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getAllNarrowers(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingAllNarrowers() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getAlternativeLabels(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingAlternativeLabels() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getBroaders(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingBroaders() {
		return false;
	}

	/**
	 * @return {@code true} if the methods
	 *         {@link #getCustomRelationProperties()},
	 *         {@link #getCustomRelations(IRI, String)},
	 *         {@link #getCustomValueProperties()} and
	 *         {@link #getCustomValues(IRI, String)} are implemented, otherwise
	 *         {@code false}
	 * 
	 */
	default boolean providingCustomProperties() {
		return false;
	}

	/**
	 * @return {@code true} if the methods {@link #getReplacedBy(IRI)} and
	 *         {@link #isDeprecated(IRI)} are implemented, otherwise
	 *         {@code false}
	 * 
	 */
	default boolean providingDeprecation() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getDescriptions(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingDescriptions() {
		return false;
	}

	/**
	 * @return {@code true} if the methods {@link #getLabel(IRI)} and
	 *         {@link #getLabels(IRI)} are implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingLabels() {
		return false;
	}

	/**
	 * @return {@code true} if the methods {@link #getMatches(String)},
	 *         {@link #getMatches(String, Collection)},
	 *         {@link #getMatches(Collection)},
	 *         {@link #getMatches(Collection, Collection)} and
	 *         {@link #setMatchThreshold} are implemented, otherwise
	 *         {@code false}
	 * 
	 */
	default boolean providingMatch() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getNarrowers(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingNarrowers() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getSignature()} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingSignature() {
		return false;
	}

	/**
	 * @return {@code true} if the methods {@link #getSuggestions(String)},
	 *         {@link #getSuggestions(String, Collection)},
	 *         {@link #getSuggestions(Collection)} and
	 *         {@link #getSuggestions(Collection, Collection)} are implemented,
	 *         otherwise {@code false}
	 * 
	 */
	default boolean providingSuggest() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getSynonyms(IRI)} is
	 *         implemented, otherwise {@code false}
	 * 
	 */
	default boolean providingSynonyms() {
		return false;
	}

	/**
	 * @return {@code true} if the method {@link #getUrls(IRI)} is implemented,
	 *         otherwise {@code false}
	 * 
	 */
	default boolean providingURLs() {
		return false;
	}

	/**
	 * Set the minimum rating of match results provided by this
	 * {@link SemanticDataSource}.
	 */
	default void setMatchThreshold(double threshold) {
		throw new UnsupportedOperationException();
	}
}
