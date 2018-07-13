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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

public class SemanticDataSourceManagerTest {
	
	@Test
	public void getAllBroaders() throws SemanticDataSourceException {
		HierarchyTestDataSource sourceA = new HierarchyTestDataSource();
		sourceA.setRelation("a", "b");
		HierarchyTestDataSource sourceB = new HierarchyTestDataSource();
		sourceA.setRelation("b", "c");
		SemanticDataSourceManager manager = new SemanticDataSourceManager();
		manager.registerAdapter(sourceA);
		manager.registerAdapter(sourceB);
		assertTrue(manager.getAllBroaders(IRI.create("c")).contains(IRI.create("b")));
		assertTrue(manager.getAllBroaders(IRI.create("c")).contains(IRI.create("a")));
		assertFalse(manager.getAllBroaders(IRI.create("c")).contains(IRI.create("c")));
	}
	
	@Test
	public void getAllNarrowers() throws SemanticDataSourceException {
		HierarchyTestDataSource sourceA = new HierarchyTestDataSource();
		sourceA.setRelation("a", "b");
		HierarchyTestDataSource sourceB = new HierarchyTestDataSource();
		sourceA.setRelation("b", "c");
		SemanticDataSourceManager manager = new SemanticDataSourceManager();
		manager.registerAdapter(sourceA);
		manager.registerAdapter(sourceB);
		assertTrue(manager.getAllNarrowers(IRI.create("a")).contains(IRI.create("b")));
		assertTrue(manager.getAllNarrowers(IRI.create("a")).contains(IRI.create("c")));
		assertFalse(manager.getAllNarrowers(IRI.create("a")).contains(IRI.create("a")));
	}
	
}
