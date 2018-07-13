package de.uni_jena.cs.fusion.lakebase;

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

import java.sql.Connection;
import java.util.Collection;
import java.util.EventListener;

import org.semanticweb.owlapi.model.IRI;

public interface ConceptManagerListener extends EventListener {

	public void newConcept(IRI iri) throws ConceptManagerListenerException;

	default public void newConcept(IRI iri, Connection connection) throws ConceptManagerListenerException {
		newConcept(iri);
	}

	default public void newConcepts(Collection<IRI> iris) throws ConceptManagerListenerException {
		for (IRI iri : iris) {
			newConcept(iri);
		}
	}

	default public void newConcepts(Collection<IRI> iris, Connection connection)
			throws ConceptManagerListenerException {
		for (IRI iri : iris) {
			newConcept(iri, connection);
		}
	}

}
