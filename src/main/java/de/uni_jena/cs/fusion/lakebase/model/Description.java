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

import java.net.URL;
import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.uni_jena.cs.fusion.lakebase.model.deserializer.IRIDeserializer;
import de.uni_jena.cs.fusion.lakebase.model.serializer.IRISerializer;

public class Description {
	@JsonDeserialize(using = IRIDeserializer.class)
	@JsonSerialize(using = IRISerializer.class)
	public IRI iri;
	public Collection<String> labels;
	public Collection<String> alternativLabels;
	public Collection<String> descriptions;
	public Collection<URL> urls;
	public Collection<String> types;
	@JsonDeserialize(contentUsing = IRIDeserializer.class)
	@JsonSerialize(contentUsing = IRISerializer.class)
	public Collection<IRI> synonyms;
	@JsonDeserialize(contentUsing = IRIDeserializer.class)
	@JsonSerialize(contentUsing = IRISerializer.class)
	public Collection<IRI> broaders;
}
