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
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

/**
 * 
 * This class provides a skeletal wrapper implementation of the
 * {@link SemanticDataSource} interface, to minimize the effort required to
 * implement this interface. It provides a pass throw implementation for each
 * method of a wrapped {@link SemanticDataSource}. Extending classes must use
 * the methods {@link #getWrapped()} and {@link #setWrapped(SemanticDataSource)}
 * to get or set the wrapped {@link SemanticDataSource}.
 * 
 * @author Jan Martin Keil
 * @since 0.1.2
 *
 */
public abstract class AbstractSemanticDataSourceWrapper implements SemanticDataSource {

	/**
	 * The wrapped {@link SemanticDataSource}.
	 */
	private SemanticDataSource semanticDataSource;

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getAllBroaders(iri);
	}

	@Override
	public Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getAllNarrowers(iri);
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getAlternativeLabels(iri);
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getBroaders(iri);
	}

	@Override
	public Collection<String> getCustomRelationProperties() throws SemanticDataSourceException {
		return semanticDataSource.getCustomRelationProperties();
	}

	@Override
	public Collection<IRI> getCustomRelations(IRI iri, String property) throws SemanticDataSourceException {
		return semanticDataSource.getCustomRelations(iri, property);
	}

	@Override
	public Collection<String> getCustomValueProperties() throws SemanticDataSourceException {
		return semanticDataSource.getCustomValueProperties();
	}

	@Override
	public Collection<String> getCustomValues(IRI iri, String property) throws SemanticDataSourceException {
		return semanticDataSource.getCustomValues(iri, property);
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getDescriptions(iri);
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getLabels(iri);
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		return semanticDataSource.getMatches(term);
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return semanticDataSource.getNamespaces();
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getNarrowers(iri);
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getReplacedBy(iri);
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return semanticDataSource.getScopes();
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		return semanticDataSource.getSignature();
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		return semanticDataSource.getSuggestions(stump);
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getSynonyms(iri);
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.getUrls(iri);
	}

	/**
	 * 
	 * @return the wrapped {@link SemanticDataSource}
	 */
	protected SemanticDataSource getWrapped() {
		return this.semanticDataSource;
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.isDeprecated(iri);
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return semanticDataSource.isPresent(iri);
	}

	@Override
	public boolean providingAllBroaders() {
		return semanticDataSource.providingAllBroaders();
	}

	@Override
	public boolean providingAllNarrowers() {
		return semanticDataSource.providingAllNarrowers();
	}

	@Override
	public boolean providingAlternativeLabels() {
		return semanticDataSource.providingAlternativeLabels();
	}

	@Override
	public boolean providingBroaders() {
		return semanticDataSource.providingBroaders();
	}

	@Override
	public boolean providingCustomProperties() {
		return semanticDataSource.providingCustomProperties();
	}

	@Override
	public boolean providingDeprecation() {
		return semanticDataSource.providingDeprecation();
	}

	@Override
	public boolean providingDescriptions() {
		return semanticDataSource.providingDescriptions();
	}

	@Override
	public boolean providingLabels() {
		return semanticDataSource.providingLabels();
	}

	@Override
	public boolean providingMatch() {
		return semanticDataSource.providingMatch();
	}

	@Override
	public boolean providingNarrowers() {
		return semanticDataSource.providingNarrowers();
	}

	@Override
	public boolean providingSignature() {
		return semanticDataSource.providingSignature();
	}

	@Override
	public boolean providingSuggest() {
		return semanticDataSource.providingSuggest();
	}

	@Override
	public boolean providingSynonyms() {
		return semanticDataSource.providingSynonyms();
	}

	@Override
	public boolean providingURLs() {
		return semanticDataSource.providingURLs();
	}

	@Override
	public void setMatchThreshold(double threshold) {
		semanticDataSource.setMatchThreshold(threshold);
	}

	/**
	 * Sets the {@link SemanticDataSource} to wrap.
	 * 
	 * @param semanticDataSource
	 *            the {@link SemanticDataSource} to wrap
	 */
	protected void setWrapped(SemanticDataSource semanticDataSource) {
		this.semanticDataSource = semanticDataSource;
	}

}
