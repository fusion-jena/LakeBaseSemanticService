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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceManager;
import de.uni_jena.cs.fusion.semantic.datasource.ontology.OntologyDataSourceTest;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * TODO test rejections
 * 
 * @since 0.1
 *
 */
public class AnnotationManagerTest {

	static DataSource dbm;
	static AnnotationManager am;

	@BeforeClass
	public static void init() throws IllegalArgumentException, SQLException, IOException, ConceptManagerException,
			SemanticDataSourceException {
		dbm = DatabaseManagerTest.createTestDatabaseManager();
		am = new AnnotationManager(dbm, new SemanticDataSourceManager(), new ConceptManager(dbm));
	}

	@Test
	public void cellAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(15L, 15L, 15L, 15L, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(15L, 15L, 15L, 15L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(15L, 15L, 15L, 15L, null, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(15L, 15L, 15L, 15L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(15L, 15L, 15L, 15L, null, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(15L, 15L, 15L, 15L, null);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(15L, 15L, 15L, 15L, null);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(15L, 15L, 15L, 15L, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(15L, 15L, 15L, 15L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(15L, 15L, 15L, 15L, null);
		annotations = am.getAcceptedAnnotations(15L, 15L, 15L, 15L, null);
		rejections = am.getRejectedAnnotations(15L, 15L, 15L, 15L, null);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void cellMetaAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(20L, 20L, 20L, 20L, 20L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(20L, 20L, 20L, 20L, 20L, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(20L, 20L, 20L, 20L, 20L, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(20L, 20L, 20L, 20L, 20L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(20L, 20L, 20L, 20L, 20L);
		annotations = am.getAcceptedAnnotations(20L, 20L, 20L, 20L, 20L);
		rejections = am.getRejectedAnnotations(20L, 20L, 20L, 20L, 20L);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void columnAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(13L, 13L, 13L, null, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(13L, 13L, 13L, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(13L, 13L, 13L, null, null, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(13L, 13L, 13L, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(13L, 13L, 13L, null, null, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(13L, 13L, 13L, null, null);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(13L, 13L, 13L, null, null);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(13L, 13L, 13L, null, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(13L, 13L, 13L, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(13L, 13L, 13L, null, null);
		annotations = am.getAcceptedAnnotations(13L, 13L, 13L, null, null);
		rejections = am.getRejectedAnnotations(13L, 13L, 13L, null, null);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void columnMetaAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(18L, 18L, 18L, null, 18L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(18L, 18L, 18L, null, 18L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(18L, 18L, 18L, null, 18L, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(18L, 18L, 18L, null, 18L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(18L, 18L, 18L, null, 18L, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(18L, 18L, 18L, null, 18L);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(18L, 18L, 18L, null, 18L);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(18L, 18L, 18L, null, 18L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(18L, 18L, 18L, null, 18L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(18L, 18L, 18L, null, 18L);
		annotations = am.getAcceptedAnnotations(18L, 18L, 18L, null, 18L);
		rejections = am.getRejectedAnnotations(18L, 18L, 18L, null, 18L);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void objectAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(12L, 12L, null, null, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(12L, 12L, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(12L, 12L, null, null, null, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(12L, 12L, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(12L, 12L, null, null, null, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(12L, 12L, null, null, null);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(12L, 12L, null, null, null);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(12L, 12L, null, null, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(12L, 12L, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(12L, 12L, null, null, null);
		annotations = am.getAcceptedAnnotations(12L, 12L, null, null, null);
		rejections = am.getRejectedAnnotations(12L, 12L, null, null, null);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void objectMetaAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(17L, 17L, null, null, 17L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(17L, 17L, null, null, 17L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(17L, 17L, null, null, 17L, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(17L, 17L, null, null, 17L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(17L, 17L, null, null, 17L, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(17L, 17L, null, null, 17L);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(17L, 17L, null, null, 17L);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(17L, 17L, null, null, 17L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(17L, 17L, null, null, 17L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(17L, 17L, null, null, 17L);
		annotations = am.getAcceptedAnnotations(17L, 17L, null, null, 17L);
		rejections = am.getRejectedAnnotations(17L, 17L, null, null, 17L);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void packageAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(11L, null, null, null, null,
				Collections.singletonMap("a", IRI.create("http://example.org/a")), Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(11L, null, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(11L, null, null, null, null,
				Collections.singletonMap("a", IRI.create("http://example.org/b")), Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(11L, null, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(11L, null, null, null, null, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(11L, null, null, null, null);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(11L, null, null, null, null);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(11L, null, null, null, null,
				Collections.singletonMap("a", IRI.create("http://example.org/a")), Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(11L, null, null, null, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(11L, null, null, null, null);
		annotations = am.getAcceptedAnnotations(11L, null, null, null, null);
		rejections = am.getRejectedAnnotations(11L, null, null, null, null);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void packageMetaAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(16L, null, null, null, 16L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(16L, null, null, null, 16L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(16L, null, null, null, 16L, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(16L, null, null, null, 16L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(16L, null, null, null, 16L, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(16L, null, null, null, 16L);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(16L, null, null, null, 16L);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(16L, null, null, null, 16L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(16L, null, null, null, 16L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(16L, null, null, null, 16L);
		annotations = am.getAcceptedAnnotations(16L, null, null, null, 16L);
		rejections = am.getRejectedAnnotations(16L, null, null, null, 16L);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void rowAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(14L, 14L, null, 14L, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(14L, 14L, null, 14L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(14L, 14L, null, 14L, null, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(14L, 14L, null, 14L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(14L, 14L, null, 14L, null, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(14L, 14L, null, 14L, null);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(14L, 14L, null, 14L, null);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(14L, 14L, null, 14L, null, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(14L, 14L, null, 14L, null);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(14L, 14L, null, 14L, null);
		annotations = am.getAcceptedAnnotations(14L, 14L, null, 14L, null);
		rejections = am.getRejectedAnnotations(14L, 14L, null, 14L, null);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void rowMetaAnnotations() throws AnnotationManagerException {
		// add
		am.setAnnotations(19L, 19L, null, 19L, 19L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		Map<String, IRI> annotations = am.getAcceptedAnnotations(19L, 19L, null, 19L, 19L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// update
		am.setAnnotations(19L, 19L, null, 19L, 19L, Collections.singletonMap("a", IRI.create("http://example.org/b")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(19L, 19L, null, 19L, 19L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/b"), annotations.get("a"));

		// reject
		am.setAnnotations(19L, 19L, null, 19L, 19L, Collections.emptyMap(),
				Collections.singletonMap("a", Collections.singletonList(IRI.create("http://example.org/b"))));
		annotations = am.getAcceptedAnnotations(19L, 19L, null, 19L, 19L);
		assertFalse(annotations.containsKey("a"));
		Map<String, ? extends Collection<IRI>> rejections = am.getRejectedAnnotations(19L, 19L, null, 19L, 19L);
		assertTrue(rejections.containsKey("a"));
		assertTrue(rejections.get("a").contains(IRI.create("http://example.org/b")));

		// add
		am.setAnnotations(19L, 19L, null, 19L, 19L, Collections.singletonMap("a", IRI.create("http://example.org/a")),
				Collections.emptyMap());
		annotations = am.getAcceptedAnnotations(19L, 19L, null, 19L, 19L);
		assertTrue(annotations.containsKey("a"));
		assertEquals(IRI.create("http://example.org/a"), annotations.get("a"));

		// remove
		am.removeAnnotations(19L, 19L, null, 19L, 19L);
		annotations = am.getAcceptedAnnotations(19L, 19L, null, 19L, 19L);
		rejections = am.getRejectedAnnotations(19L, 19L, null, 19L, 19L);
		assertFalse(annotations.containsKey("a"));
		assertFalse(rejections.containsKey("a"));
	}

	@Test
	public void maintain() throws MaintenanceException, AnnotationManagerException, SemanticDataSourceException,
			ConceptManagerException, IOException {
		ConceptManager cm = new ConceptManager(dbm);
		SemanticDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		AnnotationManager am = new AnnotationManager(dbm, adapter, cm);

		IRI oldIri = IRI.create("http://example.org/ontologies/test#removed");
		IRI newIri = IRI.create("http://example.org/ontologies/test#added");

		am.setAnnotations(1L, 1L, 1L, 1L, 1L, Collections.singletonMap("a", oldIri),
				Collections.singletonMap("b", Collections.singleton(oldIri)));

		am.maintain();

		assertEquals(newIri, am.getAcceptedAnnotations(1L, 1L, 1L, 1L, 1L).get("a"));
		assertTrue(am.getRejectedAnnotations(1L, 1L, 1L, 1L, 1L).get("b").contains(newIri));
	}

}
