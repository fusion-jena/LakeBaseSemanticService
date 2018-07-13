package de.uni_jena.cs.fusion.semantic.datasource.cache.database;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.DatabaseManagerTest;
import de.uni_jena.cs.fusion.semantic.datasource.IdentityTestDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.cache.database.DatabaseCacheWrapper;

public class SemanticDataSourceDatabaseCacheWrapperTest {

	private static DataSource datasource;
	private static SemanticDataSource semanticDataSource;
	private static DatabaseCacheWrapper semanticDataSourceCache;
	private static String queryString = "http://example.org/concept";
	private static IRI presentConcept = IRI.create(queryString);
	private static String modifier = "_changed";
	private static String queryStringModified = queryString + modifier;
	private static IRI presentConceptModified = IRI.create(presentConcept.getIRIString() + modifier);

	@BeforeClass
	public static void init() throws IllegalArgumentException, SQLException, IOException, SemanticDataSourceException, SQLException {
		datasource = DatabaseManagerTest.createTestDatabaseManager();
		semanticDataSource = new IdentityTestDataSource(Collections.singleton(presentConcept));
		semanticDataSourceCache = (DatabaseCacheWrapper) DatabaseCacheWrapper.wrap(semanticDataSource, datasource,
				"semantic.adapter_cache",3600);
	}

	private void modifyKeys() throws SQLException {
		try (Connection connection = datasource.getConnection()) {
			String sql = "UPDATE semantic.adapter_cache SET key = CONCAT(key, ?)";
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setString(1, modifier);
				statement.execute();
			}
		}
	}

	@Test
	public void match() throws SemanticDataSourceException, SQLException {
		Map<IRI, Double> result;
		result = semanticDataSourceCache.getMatches(queryString);
		assertTrue(result.containsKey(presentConcept));
		assertEquals(1D, result.get(presentConcept), 0.001);
		result = semanticDataSourceCache.getMatches(queryString);
		assertTrue(result.containsKey(presentConcept));
		assertEquals(1D, result.get(presentConcept), 0.001);
		modifyKeys();
		result = semanticDataSourceCache.getMatches(queryStringModified);
		assertTrue(result.containsKey(presentConcept));
	}

	@Test
	public void isConceptPresent() throws SemanticDataSourceException, SQLException {
		assertTrue(semanticDataSourceCache.isPresent(presentConcept));
		assertTrue(semanticDataSourceCache.isPresent(presentConcept));
		modifyKeys();
		assertTrue(semanticDataSourceCache.isPresent(presentConceptModified));
	}

	@Test
	public void suggest() throws SemanticDataSourceException, SQLException {
		Map<IRI, String> result;
		result = semanticDataSourceCache.getSuggestions(queryString);
		assertTrue(result.containsKey(presentConcept));
		assertEquals(queryString, result.get(presentConcept));
		result = semanticDataSourceCache.getSuggestions(queryString);
		assertTrue(result.containsKey(presentConcept));
		assertEquals(queryString, result.get(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getSuggestions(queryStringModified);
		assertTrue(result.containsKey(presentConcept));
	}

	@Test
	public void getAllConcepts() throws SemanticDataSourceException, SQLException {
		// TODO
	}

	@Test
	public void getConceptLabels() throws SemanticDataSourceException, SQLException {
		Collection<String> result;
		result = semanticDataSourceCache.getLabels(presentConcept);
		assertTrue(result.contains(queryString));
		result = semanticDataSourceCache.getLabels(presentConcept);
		assertTrue(result.contains(queryString));
		modifyKeys();
		result = semanticDataSourceCache.getLabels(presentConceptModified);
		assertTrue(result.contains(queryString));
	}

	@Test
	public void getConceptDescriptions() throws SemanticDataSourceException, SQLException {
		Collection<String> result;
		result = semanticDataSourceCache.getDescriptions(presentConcept);
		assertTrue(result.contains(queryString));
		result = semanticDataSourceCache.getDescriptions(presentConcept);
		assertTrue(result.contains(queryString));
		modifyKeys();
		result = semanticDataSourceCache.getDescriptions(presentConceptModified);
		assertTrue(result.contains(queryString));
	}

	@Test
	public void getConceptUrls() throws SemanticDataSourceException, SQLException, MalformedURLException {
		Collection<URL> result;
		result = semanticDataSourceCache.getUrls(presentConcept);
		assertTrue(result.contains(presentConcept.toURI().toURL()));
		result = semanticDataSourceCache.getUrls(presentConcept);
		assertTrue(result.contains(presentConcept.toURI().toURL()));
		modifyKeys();
		result = semanticDataSourceCache.getUrls(presentConceptModified);
		assertTrue(result.contains(presentConcept.toURI().toURL()));
	}

	@Test
	public void getSynonymConcepts() throws SemanticDataSourceException, SQLException {
		Collection<IRI> result;
		result = semanticDataSourceCache.getSynonyms(presentConcept);
		assertTrue(result.contains(presentConcept));
		result = semanticDataSourceCache.getSynonyms(presentConcept);
		assertTrue(result.contains(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getSynonyms(presentConceptModified);
		assertTrue(result.contains(presentConcept));
	}

	@Test
	public void getConceptAlternativeLabels() throws SemanticDataSourceException, SQLException {
		Collection<String> result;
		result = semanticDataSourceCache.getAlternativeLabels(presentConcept);
		assertTrue(result.contains(queryString));
		result = semanticDataSourceCache.getAlternativeLabels(presentConcept);
		assertTrue(result.contains(queryString));
		modifyKeys();
		result = semanticDataSourceCache.getAlternativeLabels(presentConceptModified);
		assertTrue(result.contains(queryString));
	}

	@Test
	public void getNarrowerConcepts() throws SemanticDataSourceException, SQLException {
		Collection<IRI> result;
		result = semanticDataSourceCache.getNarrowers(presentConcept);
		assertTrue(result.contains(presentConcept));
		result = semanticDataSourceCache.getNarrowers(presentConcept);
		assertTrue(result.contains(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getNarrowers(presentConcept);
		assertTrue(result.contains(presentConcept));
	}

	@Test
	public void getAllNarrowerConcepts() throws SemanticDataSourceException, SQLException {
		Collection<IRI> result;
		result = semanticDataSourceCache.getAllNarrowers(presentConcept);
		assertTrue(result.contains(presentConcept));
		result = semanticDataSourceCache.getAllNarrowers(presentConcept);
		assertTrue(result.contains(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getAllNarrowers(presentConceptModified);
		assertTrue(result.contains(presentConcept));
	}

	@Test
	public void getAllBroaderConcepts() throws SemanticDataSourceException, SQLException {
		Collection<IRI> result;
		result = semanticDataSourceCache.getAllBroaders(presentConcept);
		assertTrue(result.contains(presentConcept));
		result = semanticDataSourceCache.getAllBroaders(presentConcept);
		assertTrue(result.contains(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getAllBroaders(presentConceptModified);
		assertTrue(result.contains(presentConcept));
	}

	@Test
	public void getBroaderConcepts() throws SemanticDataSourceException, SQLException {
		Collection<IRI> result;
		result = semanticDataSourceCache.getBroaders(presentConcept);
		assertTrue(result.contains(presentConcept));
		result = semanticDataSourceCache.getBroaders(presentConcept);
		assertTrue(result.contains(presentConcept));
		modifyKeys();
		result = semanticDataSourceCache.getBroaders(presentConceptModified);
		assertTrue(result.contains(presentConcept));
	}

}
