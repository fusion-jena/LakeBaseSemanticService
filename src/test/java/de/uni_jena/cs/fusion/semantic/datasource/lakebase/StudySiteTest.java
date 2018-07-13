package de.uni_jena.cs.fusion.semantic.datasource.lakebase;

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.DatabaseManager;
import de.uni_jena.cs.fusion.lakebase.DatabaseManagerTest;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class StudySiteTest {

	private final static String NAMESPACE = "http://fred.igb-berlin.de/Studysites/view/";
	private final static String URL_BASE = "https://fred.igb-berlin.de/Studysites/view/";

	private static DataSource dataSource;
	private static SemanticDataSource studySites;

	@BeforeClass
	public static void init()
			throws IllegalArgumentException, SQLException, IOException, SemanticDataSourceException, SQLException {
		dataSource = DatabaseManagerTest.createTestDatabaseManager();
		truncateDataSchema(dataSource);
		prepareStudySiteData(dataSource);
		studySites = new StudySite(dataSource);
	}
	
	public static void truncateDataSchema(DataSource dataSource) throws SQLException, IOException {
		try (Connection connection = dataSource.getConnection()) {
			// truncate data Schema
			try {
				connection.createStatement().execute("DROP SCHEMA data CASCADE;");
			} catch (Exception e) {
				e.printStackTrace();
			}
			connection.createStatement().execute("CREATE SCHEMA data;");
		}
	}
	
	public static void prepareStudySiteData(DataSource dataSource) throws SQLException, IOException {
		try (Connection connection = dataSource.getConnection()) {
			// add test data
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			try (InputStream inputStream = classLoader.getResourceAsStream("testdata/data.ta_site.sql")) {
				DatabaseManager.execute(connection, inputStream);
			}
		}
	}

	@Test
	public void getAlternativeLabels() throws SemanticDataSourceException {
		assertTrue(studySites.getAlternativeLabels(IRI.create(NAMESPACE + "6")).contains("Saidenbachtalsperre"));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		// type
		assertTrue(studySites.getBroaders(IRI.create(NAMESPACE + "6"))
				.contains(IRI.create("http://purl.obolibrary.org/obo/ENVO_00000025")));
		// trophy
		assertTrue(studySites.getBroaders(IRI.create(NAMESPACE + "6"))
				.contains(IRI.create("http://purl.obolibrary.org/obo/ENVO_01000775")));
		// broader
		// TODO
		// mixing
		assertTrue(studySites.getBroaders(IRI.create(NAMESPACE + "3"))
				.contains(IRI.create("http://purl.obolibrary.org/obo/ENVO_01000287")));
	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		assertTrue(studySites.getLabels(IRI.create(NAMESPACE + "38")).contains("Nieplitzniederung"));
		// not existing should not throw exception
		assertTrue(studySites.getLabels(IRI.create(NAMESPACE + "9999999999999")).isEmpty());
	}

	@Test
	public void getMatches() throws SemanticDataSourceException {
		// exact match
		assertTrue(studySites.getMatches("Nieplitzniederung").containsKey(IRI.create(NAMESPACE + "38")));
		// with typo
		assertTrue(studySites.getMatches("Niepiltzniederung").containsKey(IRI.create(NAMESPACE + "38")));
	}

	@Test
	public void getSignature() throws SemanticDataSourceException {
		assertTrue(studySites.getSignature().contains(IRI.create(NAMESPACE + "38")));
		assertEquals(67, studySites.getSignature().size());
	}

	@Test
	public void getUrls() throws SemanticDataSourceException, MalformedURLException {
		assertTrue(studySites.getUrls(IRI.create(NAMESPACE + "38")).contains(new URL(URL_BASE + "38")));
	}

	@Test
	public void isPresent() throws SemanticDataSourceException {
		assertTrue(studySites.isPresent(IRI.create(NAMESPACE + "38")));
		assertFalse(studySites.isPresent(IRI.create(NAMESPACE + "9999999999999")));
	}
}
