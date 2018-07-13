package de.uni_jena.cs.fusion.lakebase.model;

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

import org.semanticweb.owlapi.model.IRI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.uni_jena.cs.fusion.lakebase.model.deserializer.IRIDeserializer;
import de.uni_jena.cs.fusion.lakebase.model.serializer.IRISerializer;

@JsonInclude(value = Include.NON_EMPTY)
public class Annotation {
	public String term;
	@JsonDeserialize(using = IRIDeserializer.class)
	@JsonSerialize(using = IRISerializer.class)
	public IRI iri;
	public Double rank;
	public String label;

	public Annotation() {
	}

	public Annotation(String term, IRI iri, String label) {
		this.term = term;
		this.iri = iri;
		this.label = label;
	}

	public Annotation(IRI iri, Double rank, String label) {
		this.iri = iri;
		this.rank = rank;
		this.label = label;
	}

	public Annotation(IRI iri, String label) {
		this.iri = iri;
		this.label = label;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Annotation) {
			Annotation other = (Annotation) object;
			return (this == object || this.term == null && other.term == null || this.term.equals(other.term)
					|| this.iri == null && other.iri == null || this.iri.equals(other.iri)
					|| this.rank == null && other.rank == null || this.rank.equals(other.rank)
					|| this.label == null && other.label == null || this.label.equals(other.label));
		}
		return false;
	}

	@Override
	public String toString() {
		return "{term: \"" + ((term != null) ? "\"" + term + "\"" : term) + ", iri: " + iri + ", rank: " + rank
				+ ", label: " + ((label != null) ? "\"" + label + "\"" : label) + "}";
	}
}
