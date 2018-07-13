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
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_jena.cs.fusion.lakebase.model.Completion;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceManager;
import de.uni_jena.cs.fusion.semantic.datasource.ontology.OntologyDataSourceFactory;

/**
 * TODO write Suggestor tests
 * 
 * @since 0.1
 */
public class CompleterTest {

	protected static Completer completer;

	@BeforeClass
	public static void init() throws SemanticDataSourceException {
		SemanticDataSourceManager adapterManager = new SemanticDataSourceManager();
		adapterManager.registerAdapter(suggestionTestAdapter());
		adapterManager.registerAdapter(suggestionTestUnitAdapter());
		completer = new Completer(adapterManager);
	}

	@Test
	public void minStumpLength() throws SemanticDataSourceException {
		assertTrue(completer.complete("s", Collections.singleton(Scope.all)).isEmpty());
		assertTrue(completer.complete("st", Collections.singleton(Scope.all)).isEmpty());
		assertTrue(completer.complete("sta", Collections.singleton(Scope.all)).isEmpty());
		assertFalse(completer.complete("star", Collections.singleton(Scope.all)).isEmpty());
	}

	@Test
	public void complete() throws SemanticDataSourceException {
		List<Completion> completion;

		completion = completer.complete("star", Collections.singleton(Scope.all));
		assertEquals(1, completion.size());
		assertEquals("star", completion.get(0).stump);
		assertEquals(2, completion.get(0).completions.size());

		completion = completer.complete("star ", Collections.singleton(Scope.all));
		assertEquals(1, completion.size());
		assertEquals("star", completion.get(0).stump);
		assertEquals(2, completion.get(0).completions.size());

		completion = completer.complete("star s", Collections.singleton(Scope.all));
		assertEquals(1, completion.size());
		assertEquals("star s", completion.get(0).stump);
		assertEquals(1, completion.get(0).completions.size());

		completion = completer.complete("star st", Collections.singleton(Scope.all));
		assertTrue(completion.isEmpty());

		completion = completer.complete("main seque", Collections.singleton(Scope.all));
		assertEquals(2, completion.size());
		assertTrue(completion.get(0).stump.equals("seque") && completion.get(1).stump.equals("main seque")
				|| completion.get(1).stump.equals("seque") && completion.get(0).stump.equals("main seque"));
		assertEquals(1, completion.get(0).completions.size());
		assertEquals(1, completion.get(1).completions.size());

		completion = completer.complete("main sequence s", Collections.singleton(Scope.all));
		assertEquals(1, completion.size());
		assertEquals("main sequence s", completion.get(0).stump);
		assertEquals(1, completion.get(0).completions.size());

		completion = completer.complete("kilo", Collections.singleton(Scope.all));
		assertEquals(1, completion.size());
		assertEquals("kilo", completion.get(0).stump);
		assertEquals(1, completion.get(0).completions.size());

		completion = completer.complete("kilo", Collections.singleton(Scope.unit));
		assertEquals(1, completion.size());
		assertEquals("kilo", completion.get(0).stump);
		assertEquals(1, completion.get(0).completions.size());

		completion = completer.complete("kilo", Collections.singleton(Scope.species));
		assertTrue(completion.isEmpty());
	}

	private static SemanticDataSource suggestionTestAdapter() throws SemanticDataSourceException {
		return OntologyDataSourceFactory
				.ontology(new File(Thread.currentThread().getContextClassLoader()
						.getResource("ontology/suggestionTest.ttl").getFile()))
				.language("en").scope(Scope.all.getIris()).labelProperty("http://www.w3.org/2000/01/rdf-schema#label")
				.build();
	}

	private static SemanticDataSource suggestionTestUnitAdapter() throws SemanticDataSourceException {
		return OntologyDataSourceFactory
				.ontology(new File(Thread.currentThread().getContextClassLoader()
						.getResource("ontology/suggestionTestUnit.ttl").getFile()))
				.language("en").scope(Scope.unit.getIri()).labelProperty("http://www.w3.org/2000/01/rdf-schema#label")
				.build();
	}

}
