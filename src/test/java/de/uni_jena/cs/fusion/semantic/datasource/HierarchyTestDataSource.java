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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public class HierarchyTestDataSource implements SemanticDataSourceProvidingAllNarrowersUsingNarrowers,
		SemanticDataSourceProvidingAllBroadersUsingBroaders {

	Map<IRI, Set<IRI>> broader = new HashMap<IRI, Set<IRI>>();
	Map<IRI, Set<IRI>> narrower = new HashMap<IRI, Set<IRI>>();

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Collections.singleton("");
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return Collections.emptyList();
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return this.broader.containsKey(iri) || this.narrower.containsKey(iri);
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		Collection<IRI> result = new HashSet<IRI>(this.narrower.getOrDefault(iri, Collections.emptySet()));
		result.remove(iri);
		return result;
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		Collection<IRI> result = new HashSet<IRI>(this.broader.getOrDefault(iri, Collections.emptySet()));
		result.remove(iri);
		return result;
	}

	/**
	 * @param broader
	 * @param narrower
	 */
	public void setRelation(String broader, String narrower) {
		setRelation(IRI.create(broader), IRI.create(narrower));
	}

	/**
	 * @param broader
	 * @param narrower
	 */
	public void setRelation(IRI broader, IRI narrower) {
		this.broader.putIfAbsent(narrower, new HashSet<IRI>());
		this.broader.get(narrower).add(broader);
		this.narrower.putIfAbsent(broader, new HashSet<IRI>());
		this.narrower.get(broader).add(narrower);
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		Collection<IRI> result = new HashSet<IRI>(this.broader.keySet());
		result.addAll(this.narrower.keySet());
		return result;
	}
	
	@Override
	public boolean providingSignature() {
		return true;
	}
	
	@Override
	public boolean providingNarrowers() {
		return true;
	}
	
	@Override
	public boolean providingBroaders() {
		return true;
	}

}
