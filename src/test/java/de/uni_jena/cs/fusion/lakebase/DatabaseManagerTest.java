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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import de.uni_jena.cs.fusion.lakebase.DatabaseManager;

/**
 * 
 * @since 0.1
 *
 */
public class DatabaseManagerTest {

	private DataSource dbm;

	@Before
	public void initDatabaseManager() throws Exception {
		this.dbm = DatabaseManagerTest.createTestDatabaseManager();
	}

	@Test
	public void schemaIsInitialized() throws SQLException {
		try (Connection connection = dbm.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery("select version from semantic.schemainfo")) {
					assertTrue(resultSet.next());
					assertTrue(resultSet.getInt("version") > 0);
				}
			}
		}
	}

	/**
	 * Returns a DatabaseManager for testing purposes, that uses a test database
	 * provided by system properties.
	 * 
	 * @return a DatabaseManager for testing purposes
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static DataSource createTestDatabaseManager() throws SQLException, IllegalArgumentException, IOException {

		BasicDataSource connectionPool = new BasicDataSource();
		// Set database driver name
		connectionPool.setDriverClassName("org.postgresql.Driver");
		// Set database url
		connectionPool.setUrl(System.getProperty("database.url"));
		// Set database user
		connectionPool.setUsername(System.getProperty("database.user"));
		// Set database password
		connectionPool.setPassword(System.getProperty("database.password"));
		// Set the connection pool size
		connectionPool.setInitialSize(1);

		// truncate Schema
		try (Connection connection = connectionPool.getConnection()) {
			try {
				connection.createStatement().execute("DROP SCHEMA semantic CASCADE;");
			} catch (Exception e) {
				e.printStackTrace();
			}
			connection.createStatement().execute("CREATE SCHEMA semantic;");
		}

		return new DatabaseManager(connectionPool);
	}

	public static void truncateTable(DataSource databaseManager, String tableName) throws SQLException {
		try (Connection connection = databaseManager.getConnection()) {
			Statement statement = connection.createStatement();
			statement.execute("TRUNCATE TABLE " + tableName + ";");
			statement.close();
		}
	}

}
