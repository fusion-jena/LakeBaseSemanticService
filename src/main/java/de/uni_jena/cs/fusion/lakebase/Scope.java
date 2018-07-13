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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Lists;

public enum Scope {
	// TODO update IRIs
	/**
	 * 
	 */
	species(IRI.create("http://example.org/scope/species"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * including species
	 */
	biology(IRI.create("http://terminologies.gfbio.org/terms/ontology/Biology"),
			Collections.singletonList(IRI.create("http://terminologies.gfbio.org/terms/ontology#Biology")),
			Collections.singleton(Scope.species)),
	/**
	 * 
	 */
	chemistry(IRI.create("http://terminologies.gfbio.org/terms/ontology#Chemistry"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * 
	 */
	datetime(IRI.create("http://example.org/scope/temporal"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * 
	 */
	location(IRI.create("http://example.org/scope/location"),
			Collections.singletonList(IRI.create("http://terminologies.gfbio.org/terms/ontology/Geo-Sciences")),
			Collections.emptyList()),
	/**
	 * 
	 */
	unit(IRI.create("http://example.org/scope/unit"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * 
	 */
	quantityKind(IRI.create("http://example.org/scope/quantityKind"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * 
	 */
	fallback(IRI.create("http://example.org/scope/fallback"), Collections.emptyList(), Collections.emptyList()),
	/**
	 * All other scopes excluding fallback.
	 */
	all(IRI.create("http://example.org/scope/all"), Collections.emptyList(),
			Lists.newArrayList(biology, chemistry, datetime, location, unit, quantityKind)),
	/**
	 * All other scopes including fallback.
	 */
	allIncludingFallback(IRI.create("http://example.org/scope/all"), Collections.emptyList(),
			Lists.newArrayList(all, fallback));

	private final IRI iri;
	private final Collection<IRI> allIris;

	private Scope(IRI iri, Collection<IRI> furtherIris, Collection<Scope> childs) {
		this.iri = iri;
		if (furtherIris.isEmpty() && childs.isEmpty()) {
			this.allIris = Collections.singleton(iri);
		} else {
			Collection<IRI> allIris = new ArrayList<IRI>(furtherIris);
			allIris.add(iri);
			allIris.addAll(furtherIris);
			for (Scope child : childs) {
				allIris.addAll(child.allIris);
			}
			this.allIris = Collections.unmodifiableCollection(allIris);
		}
	}

	public IRI getIri() {
		return this.iri;
	}

	public Collection<IRI> getIris() {
		return this.allIris;
	}
	
	public static Collection<IRI> getIris(Collection<Scope> scopes) {
		return scopes.stream().flatMap(scope -> scope.allIris.stream()).collect(Collectors.toSet());
	}
}
