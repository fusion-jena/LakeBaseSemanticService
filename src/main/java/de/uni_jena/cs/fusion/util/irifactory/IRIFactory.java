package de.uni_jena.cs.fusion.util.irifactory;

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

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.rdf.rdfxml.parser.IRIProvider;

/**
 * 
 * A factory of {@link IRI} instance returning the same instance for equal IRIs
 * to avoid unnecessary storage consumption for multiple equal IRI instances.
 * 
 * <p>
 * <b>Note</b>: The factory consumes storage, too. Only used it if a significant
 * amount of duplicated {@link IRI} instances is to be expected.
 * 
 * @author Jan Martin Keil
 *
 */
public class IRIFactory implements IRIProvider, AutoCloseable {

	private Map<IRI, IRI> map = new HashMap<IRI, IRI>();

	@Override
	public IRI getIRI(String s) {
		if (this.map != null) {
			IRI iri = IRI.create(s);
			this.map.putIfAbsent(iri, iri);
			return this.map.get(iri);
		} else {
			throw new RuntimeException("Failed to get IRI. Factory has been closed.");
		}
	}

	@Override
	public synchronized void close() {
		map.clear();
		this.map = null;
	}

}
