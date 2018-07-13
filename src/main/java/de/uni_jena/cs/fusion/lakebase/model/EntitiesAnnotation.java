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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uni_jena.cs.fusion.lakebase.Scope;

@JsonInclude(value = Include.NON_NULL)
public class EntitiesAnnotation {
	/**
	 * used in
	 * <ul>
	 * <li>search</li>
	 * <li>set annotations request</li>
	 * <li>get annotations response</li>
	 * </ul>
	 */
	@JsonProperty(required = true)
	public String text;
	/**
	 * used in
	 * <ul>
	 * <li>set annotations request</li>
	 * </ul>
	 */
	@JsonInclude(Include.NON_EMPTY)
	public Collection<Scope> scopes = Collections.emptyList();
	/**
	 * used in
	 * <ul>
	 * <li>set annotations request</li>
	 * <li>get annotations response</li>
	 * </ul>
	 */
	public List<Entity> entities;
	/**
	 * used in
	 * <ul>
	 * <li>search request</li>
	 * <li>set annotations request</li>
	 * <li>get annotations response</li>
	 * </ul>
	 */
	public List<Annotation> accepted = Collections.emptyList();
	/**
	 * used in
	 * <ul>
	 * <li>search request</li>
	 * <li>set annotations request</li>
	 * <li>get annotations response</li>
	 * </ul>
	 */
	public List<Annotation> rejected = Collections.emptyList();

	public EntitiesAnnotation() {
	}

	public EntitiesAnnotation(String text, Collection<Scope> scopes, List<Entity> entities, List<Annotation> accepted,
			List<Annotation> rejected) {
		this.text = text;
		this.scopes = scopes;
		this.entities = entities;
		this.accepted = accepted;
		this.rejected = rejected;
	}

	@JsonIgnore
	public Map<String, IRI> getAcceptedAsMap() {

		Map<String, IRI> acceptedMap = new HashMap<String, IRI>();

		for (Annotation annotation : this.accepted) {
			if (acceptedMap.put(annotation.term, annotation.iri) != null) {
				throw new IllegalArgumentException("Term \"" + annotation.term + "\" appeared twice in accepted.");
			}
		}

		return acceptedMap;
	}

	@JsonIgnore
	public Map<String, Collection<IRI>> getRejectedAsMap() {

		Map<String, Collection<IRI>> rejectedMap = new HashMap<String, Collection<IRI>>();

		for (Annotation annotation : this.rejected) {
			rejectedMap.putIfAbsent(annotation.term, new ArrayList<IRI>());
			rejectedMap.get(annotation.term).add(annotation.iri);
		}

		return rejectedMap;
	}

	@Override
	public String toString() {
		return "{text: " + ((text != null) ? "\"" + text + "\"" : text) + ", scopes: " + scopes + ", entities: "
				+ entities + ", accepted: " + accepted + ", rejected: " + rejected + "}";
	}
}
