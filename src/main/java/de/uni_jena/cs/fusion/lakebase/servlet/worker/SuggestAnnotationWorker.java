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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.Annotator;
import de.uni_jena.cs.fusion.lakebase.AnnotatorException;
import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.model.Annotation;
import de.uni_jena.cs.fusion.lakebase.model.AnnotationSuggestion;
import de.uni_jena.cs.fusion.lakebase.model.SuggestRequest;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class SuggestAnnotationWorker implements ServiceWorker {

	/**
	 * Filters suggested annotations (with term t1 and rating r1), for that an
	 * other annotation (with term t2 and rating r2) exists, so that
	 * {@code t1.startsWith(t2)} and {@code t1.equals(t2)} and {@code r2 > r1}.
	 * 
	 * @param annotations
	 */
	private void filterWorseLongerEquals(Map<String, Map<IRI, Double>> annotations) {
		// get terms
		List<String> terms = new ArrayList<String>(annotations.keySet());
		// sort terms by length
		terms.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});

		ListIterator<String> termIterator1 = terms.listIterator();

		// iterate outer terms
		while (termIterator1.hasNext()) {
			String term1 = termIterator1.next();
			// iterate inner terms
			for (String term2 : terms) {
				if (term1.startsWith(term2) && !term1.equals(term2)) {
					// inner term is prefix of outer term
					Map<IRI, Double> annotations2 = annotations.get(term2);
					Iterator<Entry<IRI, Double>> annotationIterator1 = annotations.get(term1).entrySet().iterator();
					// iterate annotations of outer
					while (annotationIterator1.hasNext()) {
						Entry<IRI, Double> annotation1 = annotationIterator1.next();
						// compare to annotations of inner
						if (annotations2.containsKey(annotation1.getKey())
								&& annotations2.get(annotation1.getKey()) > annotation1.getValue()) {
							// remove outer annotation if inner is better
							annotationIterator1.remove();
						}
					}
				}
			}
			// if no annotation left for outer term remove them
			if (annotations.get(term1).isEmpty()) {
				annotations.remove(term1);
				termIterator1.remove();
			}
		}
	}

	private void filterWorseSynonyms(Map<String, Map<IRI, Double>> annotations, SemanticDataSource semanticDataSource)
			throws SemanticDataSourceException {
		for (Map<IRI, Double> termAnnotations : annotations.values()) {
			Iterator<Entry<IRI, Double>> termAnnotationIterator = termAnnotations.entrySet().iterator();
			while (termAnnotationIterator.hasNext()) {
				Entry<IRI, Double> termAnnotation = termAnnotationIterator.next();
				Collection<IRI> synonyms = semanticDataSource.getSynonyms(termAnnotation.getKey());
				synonyms.remove(termAnnotation.getKey());
				synonyms.retainAll(termAnnotations.keySet());
				for (IRI synonym : synonyms) {
					if (termAnnotation.getValue() < termAnnotations.get(synonym)
							|| (termAnnotation.getValue() == termAnnotations.get(synonym)
									&& termAnnotation.getKey().getIRIString().compareTo(synonym.getIRIString()) <= 0)) {
						termAnnotationIterator.remove();
						break;
					}

				}
			}
		}
	}

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {
		// load environment
		Annotator annotator = environment.getAnnotator();
		SemanticDataSource semanticDataSource = environment.getSemanticDataSource();

		// respond
		try {
			Map<String, Map<IRI, Double>> termsSuggestions = annotator
					.proposeAnnotations(((SuggestRequest) input).text, ((SuggestRequest) input).scopes);
			filterWorseLongerEquals(termsSuggestions);
			filterWorseSynonyms(termsSuggestions, semanticDataSource);

			List<AnnotationSuggestion> annotationSuggestions = new ArrayList<AnnotationSuggestion>();
			for (Entry<String, Map<IRI, Double>> termSuggestions : termsSuggestions.entrySet()) {
				AnnotationSuggestion annotationSuggestion = new AnnotationSuggestion();
				annotationSuggestion.term = termSuggestions.getKey();
				annotationSuggestion.annotations = new ArrayList<Annotation>();
				for (Entry<IRI, Double> termSuggestion : termSuggestions.getValue().entrySet()) {
					IRI iri = termSuggestion.getKey();
					annotationSuggestion.annotations.add(new Annotation(iri, termSuggestion.getValue(),
							semanticDataSource.getLabel(iri).orElse(iri.getIRIString())));
				}
				annotationSuggestions.add(annotationSuggestion);
			}

			return annotationSuggestions;

		} catch (SemanticDataSourceException e) {
			throw new ServiceWorkerException("Failed to get a label of an annotation.", 500, e);
		} catch (AnnotatorException e) {
			throw new ServiceWorkerException(500, e);
		}
	}

}
