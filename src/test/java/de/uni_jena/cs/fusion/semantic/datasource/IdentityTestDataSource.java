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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

public class IdentityTestDataSource implements SemanticDataSource {

	private Collection<IRI> concepts;

	public IdentityTestDataSource(Collection<IRI> concepts) {
		this.concepts = concepts;
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri);
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		return Collections.unmodifiableCollection(this.concepts);
	}

	@Override
	public Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri);
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri);
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri.getIRIString());
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri.getIRIString());
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri.getIRIString());
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		try {
			return Collections.singletonList(iri.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public Collection<String> getNamespaces() {
		return Collections.emptyList();
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri);
	}

	@Override
	public Collection<IRI> getScopes() {
		return Collections.emptyList();
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		return Collections.singleton(iri);
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return concepts.contains(iri);
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		return Collections.singletonMap(IRI.create(term), 1D);
	}

	@Override
	public void setMatchThreshold(double threshold) {
		// do nothing
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		return Collections.singletonMap(IRI.create(stump), stump);
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

}
