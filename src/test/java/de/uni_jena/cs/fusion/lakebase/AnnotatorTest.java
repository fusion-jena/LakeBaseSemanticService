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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.MatchEverythingTestDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceManager;
import de.uni_jena.cs.fusion.semantic.datasource.ontology.OntologyDataSourceFactory;

/**
 * 
 * @since 0.1
 *
 */
public class AnnotatorTest {

	protected static Annotator annotator;

	@BeforeClass
	public static void init() throws SemanticDataSourceException {
		SemanticDataSourceManager adapterManager = new SemanticDataSourceManager();
		adapterManager.registerAdapter(annotationTestAdapter());
		adapterManager.registerAdapter(annotationTestUnitAdapter());
		annotator = new Annotator(adapterManager);

	}

	@Test
	public void determineAnnotationsExcludedParameter() throws AnnotatorException {
		Map<String, IRI> determined;
		String query = "alfa bravo charlie";

		determined = annotator.determineAnnotations(query, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertEquals(2, determined.keySet().size());
		assertTrue(determined.containsKey("alfa bravo"));
		assertTrue(determined.containsKey("charlie"));

		determined = annotator.determineAnnotations(query, Collections.emptyMap(),
				Collections.singletonMap("alfa bravo",
						Collections
								.singleton(IRI.create("http://www.example.org/ontologies/annotator/test/alfa_bravo"))),
				Collections.singleton(Scope.all));
		assertEquals(3, determined.keySet().size());
		assertTrue(determined.containsKey("alfa"));
		assertTrue(determined.containsKey("bravo"));
		assertTrue(determined.containsKey("charlie"));
	}

	@Test
	public void determineAnnotationsPredefinedParameter() throws AnnotatorException {
		Map<String, IRI> determined;
		String query = "alfa bravo charlie";

		determined = annotator.determineAnnotations(query,
				Collections.singletonMap("alfa bravo",
						IRI.create("http://www.example.org/ontologies/annotator/test/charlie")),
				Collections.emptyMap(), Collections.singleton(Scope.all));
		assertEquals(2, determined.keySet().size());
		assertTrue(determined.containsKey("alfa bravo"));
		assertEquals(IRI.create("http://www.example.org/ontologies/annotator/test/charlie"),
				determined.get("alfa bravo"));
		assertTrue(determined.containsKey("charlie"));
		assertEquals(IRI.create("http://www.example.org/ontologies/annotator/test/charlie"), determined.get("charlie"));
	}

	@Test
	/**
	 * Predefined annotations must survive, if the term is not contained in the
	 * text.
	 * 
	 * @throws AnnotatorException
	 */
	public void determineAnnotationsNotContainedPredefined() throws AnnotatorException {
		Map<String, IRI> determined;
		String query = "alfa bravo";

		determined = annotator.determineAnnotations(query,
				Collections.singletonMap("charlie",
						IRI.create("http://www.example.org/ontologies/annotator/test/charlie")),
				Collections.emptyMap(), Collections.singleton(Scope.all));
		assertEquals(2, determined.keySet().size());
		assertTrue(determined.containsKey("charlie"));
		assertEquals(IRI.create("http://www.example.org/ontologies/annotator/test/charlie"), determined.get("charlie"));
	}

	@Test
	public void determineAnnotationsScopesParameter() throws AnnotatorException {
		Map<String, IRI> determined;
		String query = "alfa bravo charlie";

		determined = annotator.determineAnnotations(query, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertEquals(2, determined.keySet().size());
		assertTrue(determined.containsKey("alfa bravo"));
		assertTrue(determined.containsKey("charlie"));

		determined = annotator.determineAnnotations(query, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.unit));
		assertEquals(0, determined.keySet().size());
	}

	@Test
	public void getAnnotationsDistinctParameter() throws AnnotatorException {
		Map<String, Map<IRI, Double>> annotationResult;
		String query = "alfa bravo charlie";

		annotationResult = annotator.getAnnotations(query, true, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertEquals(2, annotationResult.keySet().size());
		assertTrue(annotationResult.containsKey("alfa bravo"));
		assertTrue(annotationResult.containsKey("charlie"));

		annotationResult = annotator.getAnnotations(query, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertEquals(4, annotationResult.keySet().size());
		assertTrue(annotationResult.containsKey("alfa"));
		assertTrue(annotationResult.containsKey("alfa bravo"));
		assertTrue(annotationResult.containsKey("bravo"));
		assertTrue(annotationResult.containsKey("charlie"));
	}

	@Test
	public void getAnnotationsScopeParameter() throws AnnotatorException {
		Map<String, Map<IRI, Double>> annotationResult;
		String query = "alfa bravo charlie metre";

		annotationResult = annotator.getAnnotations(query, true, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.species));
		assertEquals(2, annotationResult.keySet().size());
		assertTrue(annotationResult.containsKey("alfa bravo"));
		assertTrue(annotationResult.containsKey("charlie"));

		annotationResult = annotator.getAnnotations(query, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.unit));
		assertEquals(1, annotationResult.keySet().size());
		assertTrue(annotationResult.containsKey("metre"));
	}

	@Test
	public void getAnnotationsSlightlyMisspelledTerms() throws AnnotatorException {
		Map<String, Map<IRI, Double>> annotationResult;
		String query = "meter";

		annotationResult = annotator.getAnnotations(query, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertEquals(1, annotationResult.keySet().size());
		assertTrue(annotationResult.containsKey("meter"));
	}

	@Test
	public void getAnnotationsPunctuationBreaksTerms() throws AnnotatorException {
		String characterList = ",;.?!\"";
		for (int i = 0; i < characterList.length(); i++) {
			String query = "alfa" + characterList.charAt(i) + "bravo";
			Map<String, Map<IRI, Double>> result = AnnotatorTest.annotator.getAnnotations(query, false, false,
					Collections.emptyMap(), Collections.emptyMap(), Collections.singleton(Scope.all));
			assertTrue(result.containsKey("alfa"));
			assertTrue(result.containsKey("bravo"));
			assertFalse(result.containsKey("alfa bravo"));
		}
	}

	@Test
	public void getAnnotationsDecimalMarks() throws AnnotatorException, SemanticDataSourceException {
		SemanticDataSourceManager adapterManager = new SemanticDataSourceManager();
		adapterManager.registerAdapter(new MatchEverythingTestDataSource());
		Annotator annotator = new Annotator(adapterManager);
		String text;
		Map<String, Map<IRI, Double>> result;

		text = "alfa 123.456 bravo 789.";
		result = annotator.getAnnotations(text, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertTrue(result.containsKey("alfa"));
		assertTrue(result.containsKey("123.456"));
		assertTrue(result.containsKey("bravo"));
		assertTrue(result.containsKey("789"));

		text = "alfa 123.456 789 bravo.";
		result = annotator.getAnnotations(text, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertTrue(result.containsKey("alfa"));
		assertTrue(result.containsKey("123.456"));
		assertTrue(result.containsKey("bravo"));
		assertTrue(result.containsKey("789"));

		text = "alfa 123,456 bravo 789,";
		result = annotator.getAnnotations(text, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertTrue(result.containsKey("alfa"));
		assertTrue(result.containsKey("123,456"));
		assertTrue(result.containsKey("bravo"));
		assertTrue(result.containsKey("789"));

		text = "alfa 123,456 789 bravo,";
		result = annotator.getAnnotations(text, false, false, Collections.emptyMap(), Collections.emptyMap(),
				Collections.singleton(Scope.all));
		assertTrue(result.containsKey("alfa"));
		assertTrue(result.containsKey("123,456"));
		assertTrue(result.containsKey("bravo"));
		assertTrue(result.containsKey("789"));
	}

	@Test
	public void getAnnotationsVeryLongQuery() throws AnnotatorException {
		// create very long query
		String query = "Test";
		for (int i = 0; i < 10; i++) {
			query = query + " " + query;
		} // try to annotate
		boolean thrown = false;
		try {
			AnnotatorTest.annotator.getAnnotations(query, false, false, Collections.emptyMap(), Collections.emptyMap(),
					Collections.singleton(Scope.all));
		} catch (StackOverflowError e) {
			thrown = true;
		}
		assertFalse(thrown);
	}

	private static SemanticDataSource annotationTestAdapter() throws SemanticDataSourceException {
		return OntologyDataSourceFactory
				.ontology(new File(Thread.currentThread().getContextClassLoader()
						.getResource("ontology/annotationTest.ttl").getFile()))
				.language("en").scope(Scope.species.getIri())
				.labelProperty("http://www.w3.org/2000/01/rdf-schema#label").build();
	}

	private static SemanticDataSource annotationTestUnitAdapter() throws SemanticDataSourceException {
		return OntologyDataSourceFactory
				.ontology(new File(Thread.currentThread().getContextClassLoader()
						.getResource("ontology/annotationTestUnit.ttl").getFile()))
				.language("en").scope(Scope.unit.getIri()).labelProperty("http://www.w3.org/2000/01/rdf-schema#label")
				.build();
	}
}
