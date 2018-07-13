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
 * {@link #getNarrowerConcepts} that uses {@link #getAllNarrowerConcepts}.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public interface SemanticDataSourceProvidingAllNarrowersUsingNarrowers extends SemanticDataSource {

	@Override
	default Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		Collection<IRI> narrowers = new HashSet<IRI>(getNarrowers(iri));
		List<IRI> unprocessedNarrowers = new LinkedList<IRI>(narrowers);

		while (!unprocessedNarrowers.isEmpty()) {
			for (IRI newNarrower : getNarrowers(unprocessedNarrowers.get(0))) {
				if (narrowers.add(newNarrower)) {
					// is a new narrower

					// schedule for processing the new narrower
					unprocessedNarrowers.add(newNarrower);
				}
			}
			unprocessedNarrowers.remove(0);
		}

		return narrowers;
	}
	
	@Override
	default boolean providingAllNarrowers() {
		return providingNarrowers();
	}
}
