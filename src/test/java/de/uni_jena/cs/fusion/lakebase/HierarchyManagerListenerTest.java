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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.ConceptManager;
import de.uni_jena.cs.fusion.semantic.datasource.EmptyTestDataSource;

/**
 * 
 * @since 0.1
 *
 */
public class HierarchyManagerListenerTest {

	private DataSource dbm;
	private ConceptManager cm;
	private HierarchyManager hm;

	@Before
	public void init() throws Exception {
		dbm = DatabaseManagerTest.createTestDatabaseManager();
		cm = new ConceptManager(dbm);
		hm = new HierarchyManager(dbm, new EmptyTestDataSource(), cm);
	}

	@Test
	public void newConcept() throws Exception {
		assertFalse(hm.contains(IRI.create("http://example.org/1")));
		assertTrue(cm.add(IRI.create("http://example.org/1")));
		assertFalse(cm.add(IRI.create("http://example.org/1")));
		assertTrue(hm.contains(IRI.create("http://example.org/1")));
	}

	@Test
	public void newConcepts() throws Exception {
		assertFalse(hm.contains(IRI.create("http://example.org/1")));
		assertFalse(hm.contains(IRI.create("http://example.org/2")));
		Collection<IRI> iris = new ArrayList<IRI>();
		iris.add(IRI.create("http://example.org/1"));
		iris.add(IRI.create("http://example.org/2"));
		cm.addAll(iris);
		assertTrue(hm.contains(IRI.create("http://example.org/1")));
		assertTrue(hm.contains(IRI.create("http://example.org/2")));
	}

}
