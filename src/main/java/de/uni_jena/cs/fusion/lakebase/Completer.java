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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.model.Annotation;
import de.uni_jena.cs.fusion.lakebase.model.Completion;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

/**
 * 
 * @since 0.1
 *
 */
public class Completer {

	private static final int minStumpLength = 4;

	private SemanticDataSource semanticDataSource;

	public Completer(SemanticDataSource adapterManager) {
		this.semanticDataSource = adapterManager;
	}

	/**
	 * TODO docu
	 * 
	 * @param query
	 * @return {@link List} of {@link Completion}s
	 * @throws SemanticDataSourceException
	 */
	public List<Completion> complete(String query, Collection<Scope> scopes) throws SemanticDataSourceException {

		// TODO consider punctuation

		// generate word list
		String[] words = query.split("\\s+");

		// initialize stump list
		List<String> stumps = new ArrayList<String>();

		// iterate first word of stump
		for (int i = 0; i < words.length; i++) {

			// build stump
			String stump = StringUtils.join(words, " ", i, words.length);

			if (stump.length() >= minStumpLength) {
				// stump has min length

				// add stump to stump list
				stumps.add(stump);
			}
		}

		List<Completion> completions = new ArrayList<Completion>();

		for (Entry<String, Map<IRI, String>> suggestions : semanticDataSource
				.getSuggestions(stumps, Scope.getIris(scopes)).entrySet()) {
			Completion completion = new Completion();

			completion.stump = suggestions.getKey();
			completion.completions = new ArrayList<Annotation>();

			for (Entry<IRI, String> suggestion : suggestions.getValue().entrySet()) {
				completion.completions.add(new Annotation(suggestion.getKey(), suggestion.getValue()));
			}

			completions.add(completion);
		}

		return completions;
	}

}
