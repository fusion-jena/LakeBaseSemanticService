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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.lakebase.model.SearchResponse;
import de.uni_jena.cs.fusion.semantic.datasource.HierarchyTestDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * 
 * @since 0.1
 *
 */
public class SearcherTest {
	private HierarchyTestDataSource semanticDataSource;
	private DataSource dbm;
	private Searcher search;
	private AnnotationManager am;
	private HierarchyManager hm;
	private ConceptManager cm;

	@Before
	public void init() throws Exception {
		dbm = DatabaseManagerTest.createTestDatabaseManager();
		cm = new ConceptManager(dbm);
		semanticDataSource = new HierarchyTestDataSource();
		search = new Searcher(dbm);
		am = new AnnotationManager(dbm, semanticDataSource, cm);
	}

	@Test
	public void searchRanking() throws IllegalArgumentException, SQLException, IOException, HierarchyException,
			SemanticDataSourceException, SearcherException, AnnotationManagerException, MaintenanceException {
		semanticDataSource = new HierarchyTestDataSource();
		semanticDataSource.setRelation("http://www.example.org/a", "http://www.example.org/aa");
		semanticDataSource.setRelation("http://www.example.org/a", "http://www.example.org/ab");
		semanticDataSource.setRelation("http://www.example.org/ab", "http://www.example.org/aba");
		semanticDataSource.setRelation("http://www.example.org/a", "http://www.example.org/ac");
		semanticDataSource.setRelation("http://www.example.org/a", "http://www.example.org/ad");
		semanticDataSource.setRelation("http://www.example.org/b", "http://www.example.org/ba-ca");
		semanticDataSource.setRelation("http://www.example.org/c", "http://www.example.org/ba-ca");
		hm = new HierarchyManager(dbm, semanticDataSource, cm);

		this.am.setAnnotations(1L, 1L, 1L, 1L, 1L,
				Collections.singletonMap("a", IRI.create("http://www.example.org/a")), Collections.emptyMap());
		this.am.setAnnotations(2L, 1L, 1L, 1L, 1L,
				Collections.singletonMap("ba-ca", IRI.create("http://www.example.org/ba-ca")), Collections.emptyMap());
		Map<String, IRI> map = new HashMap<String, IRI>();
		map.put("aa", IRI.create("http://www.example.org/aa"));
		map.put("aba", IRI.create("http://www.example.org/aba"));
		this.am.setAnnotations(3L, 1L, 1L, 1L, 1L, map, Collections.emptyMap());
		this.am.setAnnotations(4L, 1L, 1L, 1L, 1L,
				Collections.singletonMap("ab", IRI.create("http://www.example.org/ab")), Collections.emptyMap());
		this.am.setAnnotations(5L, 1L, 1L, 1L, 1L,
				Collections.singletonMap("aba", IRI.create("http://www.example.org/aba")), Collections.emptyMap());

		// write index
		this.hm.maintain();

		HashSet<IRI> include = new HashSet<IRI>();
		HashSet<IRI> exclude = new HashSet<IRI>();
		List<SearchResponse> result;

		Entity package1 = new Entity(1, null, null, null, null);
		Entity package2 = new Entity(2, null, null, null, null);
		Entity package3 = new Entity(3, null, null, null, null);
		Entity package4 = new Entity(4, null, null, null, null);
		Entity package5 = new Entity(5, null, null, null, null);

		include.clear();
		include.add(IRI.create("http://www.example.org/aa"));
		result = this.search.search(include, Collections.emptySet(), null);
		assertTrue(result.contains(new SearchResponse(package3, 1d)));
		assertEquals(1, result.size());

		include.clear();
		include.add(IRI.create("http://www.example.org/a"));
		result = this.search.search(include, Collections.emptySet(), null);
		assertTrue(result.contains(new SearchResponse(package1, 1d)));
		assertTrue(result.contains(new SearchResponse(package3, 2 / 4d)));
		assertTrue(result.contains(new SearchResponse(package4, 2 / 4d)));
		assertTrue(result.contains(new SearchResponse(package5, 1 / 4d)));
		assertEquals(4, result.size());

		include.clear();
		include.add(IRI.create("http://www.example.org/ab"));
		include.add(IRI.create("http://www.example.org/b"));
		result = this.search.search(include, Collections.emptySet(), null);
		assertTrue(result.contains(new SearchResponse(package2, 0 / 2d + 1 / 2d)));
		assertTrue(result.contains(new SearchResponse(package3, 1 / 2d + 0 / 2d)));
		assertTrue(result.contains(new SearchResponse(package4, 2 / 2d + 0 / 2d)));
		assertTrue(result.contains(new SearchResponse(package5, 1 / 2d + 0 / 2d)));
		assertEquals(4, result.size());

		include.clear();
		include.add(IRI.create("http://www.example.org/a"));
		exclude.clear();
		exclude.add(IRI.create("http://www.example.org/ab"));
		result = this.search.search(include, exclude, null);
		assertTrue(result.contains(new SearchResponse(package1, 1d)));
		assertEquals(1, result.size());

	}

}
