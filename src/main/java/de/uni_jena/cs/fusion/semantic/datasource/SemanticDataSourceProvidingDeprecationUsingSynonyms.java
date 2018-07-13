
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
import java.util.Iterator;

import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * This interface provides an default implementation of {@link #getReplacedBy}
 * that uses {@link #getSynonymConcepts}.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public interface SemanticDataSourceProvidingDeprecationUsingSynonyms extends SemanticDataSource {

	@Override
	default Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		Collection<IRI> result = this.getSynonyms(iri);
		Iterator<IRI> iterator = result.iterator();
		while (iterator.hasNext()) {
			if (this.isDeprecated(iterator.next())) {
				iterator.remove();
			}
		}
		return result;
	}

	@Override
	default boolean providingDeprecation() {
		return providingSynonyms();
	}

}
