package de.uni_jena.cs.fusion.semantic.datasource.lakebase;

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

import org.jooq.Converter;
import org.semanticweb.owlapi.model.IRI;

public class IDToIRIConverter implements Converter<Long, IRI> {
	private static final long serialVersionUID = 314001472735431061L;
	private final String namespace;
	
	public IDToIRIConverter(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public IRI from(Long id) {
		return IRI.create(this.namespace + id);
	}

	@Override
	public Class<Long> fromType() {
		return Long.class;
	}

	@Override
	public Long to(IRI iri) {
		return Long.parseUnsignedLong(iri.getIRIString().substring(this.namespace.length()));
	}

	@Override
	public Class<IRI> toType() {
		return IRI.class;
	}
}
