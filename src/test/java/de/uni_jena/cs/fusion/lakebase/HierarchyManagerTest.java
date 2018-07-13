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
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.sql.DataSource;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.ConceptManager;
import de.uni_jena.cs.fusion.semantic.datasource.HierarchyTestDataSource;

public class HierarchyManagerTest {
	private HierarchyManager hm;
	private HierarchyTestDataSource sds;
	private DataSource ds;
	private ConceptManager cm;

	@Test
	public void test() throws Exception {
		ds = DatabaseManagerTest.createTestDatabaseManager();
		sds = new HierarchyTestDataSource();
		cm = new ConceptManager(ds);

		sds.setRelation("a", "b");
		sds.setRelation("a", "c");
		sds.setRelation("c", "g");
		sds.setRelation("g", "h");
		sds.setRelation("g", "i");
		sds.setRelation("b", "e");
		sds.setRelation("e", "f");
		sds.setRelation("f", "g");
		sds.setRelation("c", "d");
		sds.setRelation("d", "c");

		hm = new HierarchyManager(ds, sds, cm);

		hm.add(IRI.create("a"));
		hm.add(IRI.create("b"));
		hm.add(IRI.create("c"));
		hm.add(IRI.create("d"));
		hm.add(IRI.create("e"));
		hm.add(IRI.create("f"));
		hm.add(IRI.create("g"));
		hm.add(IRI.create("h"));
		hm.add(IRI.create("i"));

		Collection<IRI> result;

		result = hm.getNarrowers(IRI.create("a"));
		assertEquals(8, result.size());
		assertTrue(result.contains(IRI.create("b")));
		assertTrue(result.contains(IRI.create("c")));
		assertTrue(result.contains(IRI.create("d")));
		assertTrue(result.contains(IRI.create("e")));
		assertTrue(result.contains(IRI.create("f")));
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("b"));
		assertEquals(5, result.size());
		assertTrue(result.contains(IRI.create("e")));
		assertTrue(result.contains(IRI.create("f")));
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("c"));
		assertEquals(3, result.size());
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("d"));
		assertEquals(3, result.size());
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("e"));
		assertEquals(4, result.size());
		assertTrue(result.contains(IRI.create("f")));
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("f"));
		assertEquals(3, result.size());
		assertTrue(result.contains(IRI.create("g")));
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("g"));
		assertEquals(2, result.size());
		assertTrue(result.contains(IRI.create("h")));
		assertTrue(result.contains(IRI.create("i")));

		result = hm.getNarrowers(IRI.create("h"));
		assertEquals(0, result.size());

		result = hm.getNarrowers(IRI.create("i"));
		assertEquals(0, result.size());

		result = hm.getBroaders(IRI.create("a"));
		assertEquals(1, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));

		result = hm.getBroaders(IRI.create("b"));
		assertEquals(2, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));

		result = hm.getBroaders(IRI.create("c"));
		assertEquals(2, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));

		result = hm.getBroaders(IRI.create("d"));
		assertEquals(2, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));

		result = hm.getBroaders(IRI.create("e"));
		assertEquals(3, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));
		assertTrue(result.contains(IRI.create("b")));

		result = hm.getBroaders(IRI.create("f"));
		assertEquals(4, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));
		assertTrue(result.contains(IRI.create("b")));
		assertTrue(result.contains(IRI.create("e")));

		result = hm.getBroaders(IRI.create("g"));
		assertEquals(7, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));
		assertTrue(result.contains(IRI.create("b")));
		assertTrue(result.contains(IRI.create("c")));
		assertTrue(result.contains(IRI.create("d")));
		assertTrue(result.contains(IRI.create("e")));
		assertTrue(result.contains(IRI.create("f")));

		result = hm.getBroaders(IRI.create("h"));
		assertEquals(8, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));
		assertTrue(result.contains(IRI.create("b")));
		assertTrue(result.contains(IRI.create("c")));
		assertTrue(result.contains(IRI.create("d")));
		assertTrue(result.contains(IRI.create("e")));
		assertTrue(result.contains(IRI.create("f")));
		assertTrue(result.contains(IRI.create("g")));

		result = hm.getBroaders(IRI.create("i"));
		assertEquals(8, result.size());
		assertTrue(result.contains(HierarchyManager.ROOT));
		assertTrue(result.contains(IRI.create("a")));
		assertTrue(result.contains(IRI.create("b")));
		assertTrue(result.contains(IRI.create("c")));
		assertTrue(result.contains(IRI.create("d")));
		assertTrue(result.contains(IRI.create("e")));
		assertTrue(result.contains(IRI.create("f")));
		assertTrue(result.contains(IRI.create("g")));
	}

}
