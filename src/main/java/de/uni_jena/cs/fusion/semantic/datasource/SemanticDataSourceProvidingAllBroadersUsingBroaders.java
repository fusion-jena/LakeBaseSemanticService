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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * This interface provides an default implementation of
 * {@link #getAllBroaderConcepts} that uses {@link #getBroaderConcepts}.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public interface SemanticDataSourceProvidingAllBroadersUsingBroaders extends SemanticDataSource {

	@Override
	default Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		Collection<IRI> broaders = new HashSet<IRI>(getBroaders(iri));
		List<IRI> unprocessedBroaders = new LinkedList<IRI>(broaders);

		while (!unprocessedBroaders.isEmpty()) {
			if (isPresent(unprocessedBroaders.get(0))) {
				for (IRI newBroader : getBroaders(unprocessedBroaders.get(0))) {
					if (broaders.add(newBroader)) {
						// is a new broader

						// schedule for processing the new broader
						unprocessedBroaders.add(newBroader);
					}
				}
			}
			unprocessedBroaders.remove(0);
		}

		return broaders;
	}

	@Override
	default boolean providingAllBroaders() {
		return providingBroaders();
	}

}
