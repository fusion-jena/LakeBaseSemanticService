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
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.Scope;

public class MatchEverythingTestDataSource implements SemanticDataSource {

	@Override
	public Collection<String> getNamespaces() {
		return Collections.emptyList();
	}

	@Override
	public Collection<IRI> getScopes() {
		return Scope.all.getIris();
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return true;
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		return Collections.singletonMap(IRI.create(term), 1.0);
	}

	@Override
	public void setMatchThreshold(double threshold) {
		// do nothing
	}

	@Override
	public boolean providingMatch() {
		return true;
	}

}
