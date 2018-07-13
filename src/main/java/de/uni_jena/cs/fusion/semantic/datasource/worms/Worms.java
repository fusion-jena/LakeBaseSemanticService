package de.uni_jena.cs.fusion.semantic.datasource.worms;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.similarity.TrieJaroWinklerSimilarityMatcher;
import de.uni_jena.cs.fusion.worms.client.AphiaRecord;
import de.uni_jena.cs.fusion.worms.client.Classification;
import de.uni_jena.cs.fusion.worms.client.ExternalIdentifierSource;
import de.uni_jena.cs.fusion.worms.client.WormsClient;
import de.uni_jena.cs.fusion.worms.client.WormsClientException;

/**
 * <p>
 * A {@link SemanticDataSource} implementing the
 * <a href="http://www.marinespecies.org/rest/">WoRMS REST webservice</a>.
 * </p>
 * 
 * <ul>
 * <li>TODO parallelize {@link #getMatches(Collection)} and
 * {@link #getSuggestions(Collection)}
 * <li>TODO implement {@link #getNarrowers(IRI)} and
 * {@link #getAllNarrowers(IRI)}
 * </ul>
 * 
 * @author Jan Martin Keil
 *
 */
public class Worms implements SemanticDataSource {

	private final static int HTTP_TIMEOUT = 30000; // milliseconds
	private final static int HTTP_CACHE_MAX_OBJECT_NUMBER = 10000;
	private final static int HTTP_CACHE_MAX_OBJECT_AGE = 604800; // seconds
	private final static int HTTP_CACHE_MAX_OBJECT_SIZE = 8192; // bytes

	private final static String NAMESPACE_WORMS = "urn:lsid:marinespecies.org:taxname:";
	private static Map<ExternalIdentifierSource, String> NAMESPACES_MAP_MODIFIABLE = new HashMap<ExternalIdentifierSource, String>();
	private final static Map<ExternalIdentifierSource, String> NAMESPACES_MAP = Collections
			.unmodifiableMap(NAMESPACES_MAP_MODIFIABLE);
	private static Collection<String> NAMESPACES_MODIFIABLE = Lists.newArrayList(NAMESPACE_WORMS);
	private final static Collection<String> NAMESPACES = Collections.unmodifiableCollection(NAMESPACES_MODIFIABLE);

	private static IRI getIRI(long aphiaID) {
		return IRI.create(NAMESPACE_WORMS + aphiaID);
	}

	private WormsClient client;

	{
		CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(HTTP_CACHE_MAX_OBJECT_NUMBER)
				.setMaxObjectSize(HTTP_CACHE_MAX_OBJECT_SIZE).setHeuristicCachingEnabled(true)
				.setHeuristicDefaultLifetime(HTTP_CACHE_MAX_OBJECT_AGE).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(HTTP_TIMEOUT)
				.setSocketTimeout(HTTP_TIMEOUT).build();
		@SuppressWarnings("resource")
		CloseableHttpClient httpClient = CachingHttpClients.custom().setCacheConfig(cacheConfig)
				.setDefaultRequestConfig(requestConfig).build();
		client = new WormsClient(httpClient);
	}
	private double matchThreshold = 0.95;

	private final boolean useTaxaMatch;

	public Worms() {
		this(false);
	}

	public Worms(boolean useTaxaMatch) {
		this.useTaxaMatch = useTaxaMatch;
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		try {
			long aphiaId = getAphiaId(iri);
			Classification classification = client.aphiaClassificationByAphiaId(aphiaId);
			if (Objects.requireNonNull(classification).aphiaId == aphiaId) {
				return Collections.emptySet();
			} else {
				Collection<IRI> allBroaders = new ArrayList<IRI>();
				while (Objects.requireNonNull(classification,
						"Invalid result for \"" + iri + "\".").aphiaId != aphiaId) {
					allBroaders.add(getIRI(classification.aphiaId));
					classification = classification.child;
				}
				return allBroaders;
			}
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		try {
			// TODO filter language
			return client.aphiaVernacularsByAphiaId(getAphiaId(iri)).stream().map(x -> x.vernacular)
					.collect(Collectors.toList());
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private long getAphiaId(IRI iri) throws SemanticDataSourceException {
		if (isInternal(iri)) {
			return getInternalId(iri);
		} else {
			return getInternalIdOfExternal(iri, getExternalSource(iri)
					.orElseThrow(() -> new SemanticDataSourceException("Unknown external IRI \"" + iri + "\".")));
		}
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		try {
			long aphiaId = getAphiaId(iri);
			Classification classification = client.aphiaClassificationByAphiaId(aphiaId);
			if (Objects.requireNonNull(classification).aphiaId == aphiaId) {
				return Collections.emptySet();
			} else {
				while (Objects.requireNonNull(classification.child,
						"Invalid result for \"" + iri + "\".").aphiaId != aphiaId) {
					classification = classification.child;
				}
				return Collections.singleton(getIRI(classification.aphiaId));
			}
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		try {
			AphiaRecord record = Objects.requireNonNull(client.aphiaRecordByAphiaId(getAphiaId(iri)),
					"Record for " + iri + " not present.");
			Collection<String> descriptions = new ArrayList<String>();
			if (Objects.nonNull(record.taxonomicRank)) {
				descriptions.add("Rank: " + record.taxonomicRank);
			}
			if (Objects.nonNull(record.authority)) {
				descriptions.add("Authority: " + record.authority);
			}
			if (Objects.nonNull(record.citation)) {
				descriptions.add("Source: " + record.citation);
			}
			return descriptions;
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private String getExternalId(IRI iri, ExternalIdentifierSource source) {
		return iri.getIRIString().substring(NAMESPACES_MAP.get(source).length());
	}

	private Optional<ExternalIdentifierSource> getExternalSource(IRI iri) throws SemanticDataSourceException {
		return NAMESPACES_MAP.entrySet().stream()
				.filter(namespace -> iri.getIRIString().startsWith(namespace.getValue()))
				.map(namespace -> namespace.getKey()).findAny();
	}

	private long getInternalId(IRI iri) throws SemanticDataSourceException {
		try {
			Long aphiaID = Long.parseUnsignedLong(iri.getIRIString().substring(NAMESPACE_WORMS.length()));
			if (!aphiaID.equals(0L)) {
				return aphiaID;
			} else {
				throw new SemanticDataSourceException("Unknown internal IRI \"" + iri + "\".");
			}
		} catch (NumberFormatException e) {
			throw new SemanticDataSourceException("Invalid internal IRI \"" + iri + "\".");
		}
	}

	private long getInternalIdOfExternal(IRI iri, ExternalIdentifierSource source) throws SemanticDataSourceException {
		try {
			String id = getExternalId(iri, source);
			return Objects.requireNonNull(client.aphiaRecordByExternalId(id, source)).aphiaId;
		} catch (NullPointerException e) {
			throw new SemanticDataSourceException("Unknown external IRI \"" + iri + "\".", e);
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException("Failed to retrieve external IRI \"" + iri + "\".", e);
		}
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		try {
			AphiaRecord record = Objects.requireNonNull(client.aphiaRecordByAphiaId(getAphiaId(iri)),
					"Record for " + iri + " not present.");
			Collection<String> labels = new ArrayList<String>(2);
			if (Objects.nonNull(record.scientificName)) {
				labels.add(record.scientificName);
				if (Objects.nonNull(record.authority)) {
					labels.add(record.scientificName + " " + record.authority);
				}
			}
			return labels;
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		try {
			List<String> termsList;
			if (terms instanceof List) {
				termsList = (List<String>) terms;
			} else {
				termsList = new ArrayList<String>(terms);
			}
			List<Collection<AphiaRecord>> responses;
			if (this.useTaxaMatch) {
				responses = client.aphiaRecordsByMatchNames(termsList, false);
			} else {
				responses = client.aphiaRecordsByNames(termsList, false, false);
			}

			Iterator<String> termsIterator = termsList.iterator();
			Iterator<Collection<AphiaRecord>> responsesIterator = responses.iterator();
			Map<String, Map<IRI, Double>> matches = new HashMap<String, Map<IRI, Double>>();

			while (termsIterator.hasNext() && responsesIterator.hasNext()) {
				String term = termsIterator.next();
				matches.put(term, responsesIterator.next().stream()
						.filter(record -> (!record.status.equals("quarantined") && !record.status.equals("deleted")))
						.map(record -> Collections.singletonMap(IRI.create(record.lsid),
								TrieJaroWinklerSimilarityMatcher.match(term, record.scientificName)))
						.filter(e -> e.values().iterator().next() >= this.matchThreshold)
						.flatMap(map -> map.entrySet().stream())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			}

			return matches;
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		return getMatches(Collections.singleton(term)).get(term);
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Worms.NAMESPACES;
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		try {
			AphiaRecord record = Objects.requireNonNull(client.aphiaRecordByAphiaId(getAphiaId(iri)),
					"Record for " + iri + " not present.");
			if (record.validAphiaId != record.aphiaId) {
				return Collections.singleton(getIRI(record.validAphiaId));
			} else {
				return Collections.emptyList();
			}
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return Collections.singleton(IRI.create("http://example.org/scope/species"));
	}

	@Override
	public Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps) throws SemanticDataSourceException {
		try {
			List<String> stumpsList;
			if (stumps instanceof List) {
				stumpsList = (List<String>) stumps;
			} else {
				stumpsList = new ArrayList<String>(stumps);
			}

			Iterator<String> stumpsIterator = stumpsList.iterator();
			Iterator<Collection<AphiaRecord>> responsesIterator = client.aphiaRecordsByNames(stumpsList, true, false)
					.iterator();
			Map<String, Map<IRI, String>> suggestions = new HashMap<String, Map<IRI, String>>();

			while (stumpsIterator.hasNext() && responsesIterator.hasNext()) {
				suggestions.put(stumpsIterator.next(), responsesIterator.next().stream()
						.filter(record -> (!record.status.equals("quarantined") && !record.status.equals("deleted")))
						.collect(Collectors.toMap(record -> IRI.create(record.lsid), record -> record.scientificName)));
			}

			return suggestions;
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		return getSuggestions(Collections.singleton(stump)).get(stump);
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		try {
			long aphiaID = getAphiaId(iri);
			Collection<IRI> synonyms = new HashSet<IRI>();

			// add WORMS IRI (might not be the the ID from the given IRI)
			synonyms.add(IRI.create(NAMESPACE_WORMS + aphiaID));

			// add WOMRS synonyms
			client.aphiaSynonymsByAphiaId(aphiaID).stream().map(record -> IRI.create(record.lsid))
					.forEach(synonyms::add);

			// add WORMS valid
			synonyms.add(getIRI(client.aphiaRecordByAphiaId(aphiaID).validAphiaId));

			// add external synonyms
			for (Entry<ExternalIdentifierSource, String> namespace : NAMESPACES_MAP.entrySet()) {
				// TODO remote Issue: e.g. for tsn externalID is an number
				client.aphiaExternalIdByAphiaId(aphiaID, namespace.getKey()).stream()
						.map(externalID -> IRI.create(namespace.getValue() + externalID)).forEach(synonyms::add);
			}

			// add external LSIDs
			client.aphiaExternalIdByAphiaId(aphiaID, ExternalIdentifierSource.lsid).stream()
					.map(externalID -> IRI.create(externalID)).forEach(synonyms::add);

			// remove given IRI
			synonyms.remove(iri);

			return synonyms;
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		try {
			return Collections
					.singletonList(new URL(Objects.requireNonNull(client.aphiaRecordByAphiaId(getAphiaId(iri)),
							"Record for " + iri + " not present.").url));
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		} catch (MalformedURLException e) {
			throw new SemanticDataSourceException("Failed to get URL of IRI \"" + iri + "\".", e);
		}
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		try {
			AphiaRecord record = Objects.requireNonNull(client.aphiaRecordByAphiaId(getAphiaId(iri)),
					"Record for " + iri + " not present.");
			return record.aphiaId != record.validAphiaId;
		} catch (NullPointerException | WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private boolean isInternal(IRI iri) {
		return iri.getIRIString().startsWith(NAMESPACE_WORMS);
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		try {
			if (isInternal(iri)) {
				long aphiaID;
				try {
					aphiaID = getInternalId(iri);
					return Objects.nonNull(client.aphiaRecordByAphiaId(aphiaID));
				} catch (SemanticDataSourceException | NumberFormatException | WormsClientException e) {
					return false;
				}
			} else {
				Optional<ExternalIdentifierSource> source = getExternalSource(iri);
				return source.isPresent() && Objects
						.nonNull(client.aphiaRecordByExternalId(getExternalId(iri, source.get()), source.get()));
			}
		} catch (WormsClientException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public boolean providingAllBroaders() {
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

	@Override
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
	}

	private void use(ExternalIdentifierSource external, String namespace) {
		NAMESPACES_MAP_MODIFIABLE.put(external, namespace);
		NAMESPACES_MODIFIABLE.add(namespace);
	}

	/**
	 * <p>
	 * Activates the usage of external IRIs of the given type in
	 * {@link #getSynonyms(IRI)}. Supported types:
	 * </p>
	 * <ul>
	 * <li>{@code eol}
	 * <li>{@code tsn}
	 * <li>{@code ncbi}
	 * <li>{@code dyntaxa}
	 * <li>{@code fishbase}
	 * <li>{@code gisd}
	 * </ul>
	 * 
	 * @param externalType
	 *            type of external IRIs to use
	 * @return this
	 * @throws SemanticDataSourceException
	 */
	public Worms useExternalIRIs(String externalType) throws SemanticDataSourceException {
		switch (externalType.toLowerCase()) {
		case "eol":
			use(ExternalIdentifierSource.eol, "http://www.eol.org/pages/");
			break;
		case "tsn":
			use(ExternalIdentifierSource.tsn,
					"http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=");
			break;
		case "ncbi":
			use(ExternalIdentifierSource.ncbi, "http://purl.obolibrary.org/obo/NCBITaxon_");
			break;
		case "dyntaxa":
			use(ExternalIdentifierSource.dyntaxa, "urn:lsid:dyntaxa.se:Taxon:");
			break;
		case "fishbase":
			use(ExternalIdentifierSource.fishbase, "http://www.fishbase.org/summary/");
			break;
		case "gisd":
			use(ExternalIdentifierSource.gisd, "http://www.iucngisd.org/gisd/species.php?sc=");
			break;
		case "bold":
			throw new SemanticDataSourceException("Unsupported external type.");
			// TODO namespace of Barcode of Life Database (BOLD)
			// use(ExternalIdentifierSource.bold, "");
			// break;
		case "iucn":
			throw new SemanticDataSourceException("Unsupported external type.");
			// TODO namespace of IUCN Red List Identifier
			// use(ExternalIdentifierSource.iucn, "");
			// break;
		default:
			throw new SemanticDataSourceException("Unknown external type.");
		}
		return this;
	}
}
