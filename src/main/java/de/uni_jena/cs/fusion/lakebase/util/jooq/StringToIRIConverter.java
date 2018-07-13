package de.uni_jena.cs.fusion.lakebase.util.jooq;

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

public class StringToIRIConverter implements Converter<String, IRI> {

	private static final long serialVersionUID = 3395931572455597902L;

	@Override
	public IRI from(String databaseObject) {
		return IRI.create(databaseObject);
	}

	@Override
	public String to(IRI userObject) {
		return userObject.getIRIString();
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<IRI> toType() {
		return IRI.class;
	}

}
