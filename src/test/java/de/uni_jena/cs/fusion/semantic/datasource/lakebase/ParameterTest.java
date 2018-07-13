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

public class ParameterTest {

	private final static String NAMESPACE = "http://fred.igb-berlin.de/Parameter/view/";
	private final static String URL_BASE = "https://fred.igb-berlin.de/Parameter/view/";

	private static DataSource dataSource;
	private static SemanticDataSource parameter;

	@BeforeClass
	public static void init()
			throws IllegalArgumentException, SQLException, IOException, SemanticDataSourceException, SQLException {
		dataSource = DatabaseManagerTest.createTestDatabaseManager();
		truncateDataSchema(dataSource);
		prepareParameterData(dataSource);
		parameter = new Parameter(dataSource);
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
	
	public static void prepareParameterData(DataSource dataSource) throws SQLException, IOException {
		try (Connection connection = dataSource.getConnection()) {
			// add test data
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			try (InputStream inputStream = classLoader.getResourceAsStream("testdata/data.parameter.sql")) {
				DatabaseManager.execute(connection, inputStream);
			}
		}
	}

	@Test
	public void getAlternativeLabels() throws SemanticDataSourceException {
		assertTrue(parameter.getAlternativeLabels(IRI.create(NAMESPACE + "9")).contains("Globalstrahlung"));
		assertTrue(parameter.getAlternativeLabels(IRI.create(NAMESPACE + "9")).contains("Einstrahlung"));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		assertTrue(parameter.getBroaders(IRI.create(NAMESPACE + "1"))
				.contains(IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Quantity")));
		assertTrue(parameter.getBroaders(IRI.create(NAMESPACE + "1"))
				.contains(IRI.create("http://purl.obolibrary.org/obo/PATO_0001241")));
	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		assertTrue(parameter.getLabels(IRI.create(NAMESPACE + "28")).contains("fish abundance and biomass"));
		// not existing should not throw exception
		assertTrue(parameter.getLabels(IRI.create(NAMESPACE + "9999999999999")).isEmpty());
	}

	@Test
	public void getMatches() throws SemanticDataSourceException {
		// exact match
		assertTrue(parameter.getMatches("total dissolved phosphorus").containsKey(IRI.create(NAMESPACE + "19")));
		// with typo
		assertTrue(parameter.getMatches("total dissolved phosphours").containsKey(IRI.create(NAMESPACE + "19")));
	}

	@Test
	public void getSignature() throws SemanticDataSourceException {
		assertTrue(parameter.getSignature().contains(IRI.create(NAMESPACE + "2")));
		assertEquals(41, parameter.getSignature().size());
	}

	@Test
	public void getUrls() throws SemanticDataSourceException, MalformedURLException {
		assertTrue(parameter.getUrls(IRI.create(NAMESPACE + "38")).contains(new URL(URL_BASE + "38")));
	}

	@Test
	public void isPresent() throws SemanticDataSourceException {
		assertTrue(parameter.isPresent(IRI.create(NAMESPACE + "2")));
		assertFalse(parameter.isPresent(IRI.create(NAMESPACE + "9999999999999")));
	}

}
