package de.uni_jena.cs.fusion.semantic.datasource.sparql;

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
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllBroadersUsingBroaders;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllNarrowersUsingNarrowers;
import de.uni_jena.cs.fusion.similarity.jarowinkler.JaroWinklerSimilarity;
import de.uni_jena.cs.fusion.util.irifactory.IRIFactory;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

public class SparqlDataSource implements Maintainable, SemanticDataSourceProvidingAllBroadersUsingBroaders,
		SemanticDataSourceProvidingAllNarrowersUsingNarrowers {

	public enum Feature {
		ALTERNATIVE_LABELS, BROADERS, DESCRIPTIONS, IRIS, LABELS, NARROWERS, REPLACEMENTS, SYNONYMS, URLS
	}

	private static final Logger log = LoggerFactory.getLogger(SparqlDataSource.class);
	private Map<IRI, Collection<String>> alternativeLabels = new HashMap<IRI, Collection<String>>();

	private Map<IRI, Collection<IRI>> broaders = new HashMap<IRI, Collection<IRI>>();
	private Integer dataUID;
	boolean dereferencingIris = false;
	private Map<IRI, Collection<String>> descriptions = new HashMap<IRI, Collection<String>>();
	private boolean initilized;

	private Collection<IRI> iris = new HashSet<IRI>();
	private NavigableMap<String, Set<IRI>> labelIndex;
	private Map<IRI, Collection<String>> labels = new HashMap<IRI, Collection<String>>();
	private JaroWinklerSimilarity<Set<IRI>> matcher;
	private double matchThreshold = 0.95;
	Collection<String> namespaces = new HashSet<String>();
	private Map<IRI, Collection<IRI>> narrowers = new HashMap<IRI, Collection<IRI>>();
	Map<Feature, Query> queries = new HashMap<Feature, Query>();
	private Map<IRI, Collection<IRI>> replacements = new HashMap<IRI, Collection<IRI>>();

	Collection<IRI> scopes = new HashSet<IRI>();
	String service;
	private Map<IRI, Collection<IRI>> synonyms = new HashMap<IRI, Collection<IRI>>();

	private Map<IRI, List<URL>> urls = new HashMap<IRI, List<URL>>();

	SparqlDataSource() {
		// do nothing
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.alternativeLabels.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.broaders.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public int getDataUID() {
		return this.dataUID;
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.descriptions.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.labels.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public Map<IRI, Double> getMatches(String term) {
		term = term.toLowerCase();
		Map<IRI, Double> result = new HashMap<IRI, Double>();
		Map<Set<IRI>, Double> match = this.matcher.apply(term);
		for (Set<IRI> iris : match.keySet()) {
			for (IRI iri : iris) {
				result.put(iri, match.get(iris));
			}
		}
		return result;
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.namespaces);
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.narrowers.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.replacements.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.scopes);
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.iris);
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		Map<IRI, String> result = new HashMap<>();
		stump = stump.toLowerCase();
		String bound = stump.substring(0, stump.length() - 1) + (char) (stump.charAt(stump.length() - 1) + 1);
		Map<String, Set<IRI>> suggestionTrie = this.labelIndex.subMap(stump.toLowerCase(), bound);
		Iterator<Entry<String, Set<IRI>>> suggestionIterator = suggestionTrie.entrySet().iterator();
		for (int i = 0; i < 10 && suggestionIterator.hasNext(); i++) {
			Entry<String, Set<IRI>> current = suggestionIterator.next();
			for (IRI value : current.getValue()) {
				result.put(value, current.getKey());
			}
		}
		return result;
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.synonyms.getOrDefault(iri, Collections.emptySet()));
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		return Collections.unmodifiableList(this.urls.getOrDefault(iri, Collections.emptyList()));
	}

	synchronized void init() throws SemanticDataSourceException {
		log.info("initializing ...");

		setDataUID();

		// backup current state
		Collection<IRI> irisBackup = this.iris;
		Map<IRI, Collection<String>> alternativeLabelsBackup = this.alternativeLabels;
		Map<IRI, Collection<IRI>> broadersBackup = this.broaders;
		Map<IRI, Collection<String>> descriptionsBackup = this.descriptions;
		Map<IRI, Collection<String>> labelsBackup = this.labels;
		Map<IRI, Collection<IRI>> narrowersBackup = this.narrowers;
		Map<IRI, Collection<IRI>> replacementsBackup = this.replacements;
		Map<IRI, Collection<IRI>> synonymsBackup = this.synonyms;
		Map<IRI, List<URL>> urlsBackup = this.urls;
		NavigableMap<String, Set<IRI>> labelIndexBackup = this.labelIndex;
		JaroWinklerSimilarity<Set<IRI>> matcherBackup = this.matcher;

		try (IRIFactory factory = new IRIFactory()) {

			// load data
			initAlternativLabels(factory);
			initBroaders(factory);
			initDescriptions(factory);
			initIris(factory);
			initLabels(factory);
			initNarrowers(factory);
			initReplacements(factory);
			initSynonyms(factory);
			initUrls(factory);

			// introduce reflexivity
			for (IRI narrower : this.broaders.keySet()) {
				for (IRI broader : this.broaders.get(narrower)) {
					this.narrowers.putIfAbsent(broader, new HashSet<IRI>());
					this.narrowers.get(broader).add(narrower);
				}
			}

			for (IRI broader : this.narrowers.keySet()) {
				for (IRI narrower : this.narrowers.get(broader)) {
					this.broaders.putIfAbsent(narrower, new HashSet<IRI>());
					this.broaders.get(narrower).add(broader);
				}
			}

			for (IRI synonym1 : new HashSet<IRI>(this.synonyms.keySet())) {
				for (IRI synonym2 : this.synonyms.get(synonym1)) {
					this.synonyms.putIfAbsent(synonym2, new HashSet<IRI>());
					this.synonyms.get(synonym2).add(synonym1);
				}
			}

			// add missing iris
			this.iris.addAll(this.alternativeLabels.keySet());
			this.iris.addAll(this.broaders.keySet());
			this.iris.addAll(this.descriptions.keySet());
			this.iris.addAll(this.labels.keySet());
			this.iris.addAll(this.narrowers.keySet());
			this.iris.addAll(this.synonyms.keySet());
			this.iris.addAll(this.urls.keySet());

			// add dereferencing urls
			if (dereferencingIris) {
				for (IRI iri : iris) {
					this.urls.putIfAbsent(iri, new ArrayList<URL>());
					try {
						this.urls.get(iri).add(iri.toURI().toURL());
					} catch (MalformedURLException e) {
						throw new SemanticDataSourceException(e);
					}
				}
			}

			// preparing match and suggest
			this.labelIndex = new TreeMap<String, Set<IRI>>();
			for (IRI iri : this.getSignature()) {
				for (String label : this.getLabels(iri)) {
					String caseInsensitiveLabel = label.toLowerCase();
					this.labelIndex.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
					this.labelIndex.get(caseInsensitiveLabel).add(iri);
				}
				for (String label : this.getAlternativeLabels(iri)) {
					String caseInsensitiveLabel = label.toLowerCase();
					this.labelIndex.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
					this.labelIndex.get(caseInsensitiveLabel).add(iri);
				}
			}
			this.matcher = JaroWinklerSimilarity.with(this.labelIndex, this.matchThreshold);

			this.initilized = true;
		} catch (Exception e) {
			SparqlDataSource.log.error("Failed to load data.");

			// restore backup
			this.iris = irisBackup;
			this.alternativeLabels = alternativeLabelsBackup;
			this.broaders = broadersBackup;
			this.descriptions = descriptionsBackup;
			this.labels = labelsBackup;
			this.narrowers = narrowersBackup;
			this.replacements = replacementsBackup;
			this.synonyms = synonymsBackup;
			this.urls = urlsBackup;
			this.labelIndex = labelIndexBackup;
			this.matcher = matcherBackup;

			if (this.initilized) {
				SparqlDataSource.log.info("Previous state restored.");
			} else {
				SparqlDataSource.log.info("Trying again during next maintenance.");
			}

		}
	}

	private void initAlternativLabels(IRIFactory factory) {
		this.alternativeLabels = new HashMap<IRI, Collection<String>>();

		if (this.queries.containsKey(Feature.ALTERNATIVE_LABELS)) {
			this.alternativeLabels = loadStringMap(factory, queries.get(Feature.ALTERNATIVE_LABELS));
		}
	}

	private void initBroaders(IRIFactory factory) {
		this.broaders = new HashMap<IRI, Collection<IRI>>();

		if (this.queries.containsKey(Feature.BROADERS)) {
			this.broaders = loadIRIMap(factory, queries.get(Feature.BROADERS));
		}
	}

	private void initDescriptions(IRIFactory factory) {
		this.descriptions = new HashMap<IRI, Collection<String>>();

		if (this.queries.containsKey(Feature.DESCRIPTIONS)) {
			this.descriptions = loadStringMap(factory, queries.get(Feature.DESCRIPTIONS));
		}
	}

	private void initIris(IRIFactory factory) {
		this.iris = new HashSet<IRI>();

		if (this.queries.containsKey(Feature.IRIS)) {
			this.iris = loadIRISet(factory, queries.get(Feature.IRIS));
		}
	}

	private void initLabels(IRIFactory factory) {
		this.labels = new HashMap<IRI, Collection<String>>();

		if (this.queries.containsKey(Feature.LABELS)) {
			this.labels = loadStringMap(factory, queries.get(Feature.LABELS));
		}
	}

	private void initNarrowers(IRIFactory factory) {
		this.narrowers = new HashMap<IRI, Collection<IRI>>();

		if (this.queries.containsKey(Feature.NARROWERS)) {
			this.narrowers = loadIRIMap(factory, queries.get(Feature.NARROWERS));
		}
	}

	private void initReplacements(IRIFactory factory) {
		this.replacements = new HashMap<IRI, Collection<IRI>>();

		if (this.queries.containsKey(Feature.REPLACEMENTS)) {
			this.replacements = loadIRIMap(factory, queries.get(Feature.REPLACEMENTS));
		}
	}

	private void initSynonyms(IRIFactory factory) {
		this.synonyms = new HashMap<IRI, Collection<IRI>>();

		if (this.queries.containsKey(Feature.SYNONYMS)) {
			this.synonyms = loadIRIMap(factory, queries.get(Feature.SYNONYMS));
		}
	}

	private void initUrls(IRIFactory factory) throws SemanticDataSourceException {
		this.urls = new HashMap<IRI, List<URL>>();

		if (this.queries.containsKey(Feature.URLS)) {
			this.urls = loadURLMap(factory, queries.get(Feature.URLS));
		}
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		return this.replacements.containsKey(iri);
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return this.iris.contains(iri);
	}

	private Map<IRI, Collection<IRI>> loadIRIMap(IRIFactory factory, Query query) {
		log.debug(query.toString(Syntax.syntaxSPARQL_11));
		Map<IRI, Collection<IRI>> resultMap = new HashMap<IRI, Collection<IRI>>();
		try (QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(service, query)) {
			ResultSet results = engine.execSelect();
			String keyColumn = results.getResultVars().get(0);
			String valueColumn = results.getResultVars().get(1);
			while (results.hasNext()) {
				QuerySolution result = results.next();
				IRI key = factory.getIRI(result.getResource(keyColumn).getURI());
				resultMap.putIfAbsent(key, new HashSet<IRI>());
				resultMap.get(key).add(factory.getIRI(result.getResource(valueColumn).getURI()));
			}
		}
		return resultMap;
	}

	private Set<IRI> loadIRISet(IRIFactory factory, Query query) {
		log.debug(query.toString(Syntax.syntaxSPARQL_11));
		Set<IRI> resultSet = new HashSet<IRI>();
		try (QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(service, query)) {
			ResultSet results = engine.execSelect();
			String keyColumn = results.getResultVars().get(0);
			while (results.hasNext()) {
				QuerySolution result = results.next();
				resultSet.add(factory.getIRI(result.getResource(keyColumn).getURI()));
			}
		}
		return resultSet;
	}

	private Map<IRI, Collection<String>> loadStringMap(IRIFactory factory, Query query) {
		log.debug(query.toString(Syntax.syntaxSPARQL_11));
		Map<IRI, Collection<String>> resultMap = new HashMap<IRI, Collection<String>>();
		try (QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(service, query)) {
			ResultSet results = engine.execSelect();
			String keyColumn = results.getResultVars().get(0);
			String valueColumn = results.getResultVars().get(1);
			while (results.hasNext()) {
				QuerySolution result = results.next();
				IRI key = factory.getIRI(result.getResource(keyColumn).getURI());
				resultMap.putIfAbsent(key, new HashSet<String>());
				resultMap.get(key).add(result.getLiteral(valueColumn).getString());
			}
		}
		return resultMap;
	}

	private Map<IRI, List<URL>> loadURLMap(IRIFactory factory, Query query) throws SemanticDataSourceException {
		log.debug(query.toString(Syntax.syntaxSPARQL_11));
		Map<IRI, List<URL>> resultMap = new HashMap<IRI, List<URL>>();
		try (QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(service, query)) {
			ResultSet results = engine.execSelect();
			String keyColumn = results.getResultVars().get(0);
			String valueColumn = results.getResultVars().get(1);
			while (results.hasNext()) {
				QuerySolution result = results.next();
				IRI key = factory.getIRI(result.getResource(keyColumn).getURI());
				// NOTE: Don't use HashSet or similar here, because literally
				// unequal URLs might be considered as equal because of
				// resolving the host to IPs during comparison.
				resultMap.putIfAbsent(key, new ArrayList<URL>());
				try {
					if (result.get(valueColumn).isURIResource()) {
						resultMap.get(key).add(new URL(result.getResource(valueColumn).getURI()));
					} else if (result.get(valueColumn).isLiteral()) {
						resultMap.get(key).add(new URL(result.getLiteral(valueColumn).getString()));
					}
				} catch (MalformedURLException e) {
					throw new SemanticDataSourceException(e);
				}
			}
		}
		return resultMap;
	}

	@Override
	public void maintain() throws MaintenanceException {
		try {
			this.init();
		} catch (SemanticDataSourceException e) {
			throw new MaintenanceException(e);
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

	void setDataUID() {
		// generate list of queries
		List<String> queryStrings = new ArrayList<String>();
		for (Query query : queries.values()) {
			queryStrings.add(query.serialize());
		}

		// sort queries to become deterministic
		Collections.sort(queryStrings);

		// generate hash code using concatenated service string and queries
		this.dataUID = (this.service + String.join("", queryStrings)).hashCode();
	}

	@Override
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
		this.matcher.setThreshold(threshold);
	}

}
