package de.uni_jena.cs.fusion.lakebase.servlet.worker;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.Annotator;
import de.uni_jena.cs.fusion.lakebase.AnnotatorException;
import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.Searcher;
import de.uni_jena.cs.fusion.lakebase.SearcherException;
import de.uni_jena.cs.fusion.lakebase.model.SearchRequest;
import de.uni_jena.cs.fusion.lakebase.model.SearchResponse;

public class SearchWorker implements ServiceWorker {

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {
		try {

			// load environment
			Annotator annotator = environment.getAnnotator();
			Searcher searcher = environment.getSearcher();

			// parse input
			SearchRequest search = (SearchRequest) input;

			List<SearchResponse> results;

			if (search.include.text != null) {
				// annotate included terms (annotated phrases do not overlap)
				Map<String, Map<IRI, Double>> includeAnnotations = annotator.augmentAnnotations(search.include.text,
						search.include.getAcceptedAsMap(), search.include.getRejectedAsMap());

				// get collection of included searched iris
				Collection<IRI> includeIRIs = new HashSet<IRI>();
				for (String term : includeAnnotations.keySet()) {
					includeIRIs.addAll(includeAnnotations.get(term).keySet());
				}

				Collection<IRI> excludeIRIs;
				if (search.exclude.text != null) {
					// annotate excluded terms (annotated phrases do not
					// overlap)
					Map<String, Map<IRI, Double>> excludeAnnotations = annotator.augmentAnnotations(search.exclude.text,
							search.exclude.getAcceptedAsMap(), search.exclude.getRejectedAsMap());
					excludeIRIs = new HashSet<IRI>();
					// get collection of excluded searched iris
					for (String term : excludeAnnotations.keySet()) {
						excludeIRIs.addAll(excludeAnnotations.get(term).keySet());
					}
				} else {
					excludeIRIs = Collections.emptySet();
				}

				results = searcher.search(includeIRIs, excludeIRIs, search.entity);
			} else {
				results = Collections.emptyList();
			}

			return results;

		} catch (SearcherException | AnnotatorException e) {
			throw new ServiceWorkerException(500, e);
		}
	}

}
