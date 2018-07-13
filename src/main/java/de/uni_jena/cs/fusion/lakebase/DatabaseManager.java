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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A <code>DataSource</code> Wrapper managing the database schema. At
 * instantiation time an database schema update of the database schema will be
 * performed.
 * </p>
 * 
 * <p>
 * The current database schema version will be stored in
 * <code>semantic.schemainfo.version</code>. If that does not exist, it will be
 * initialized with the value <code>0</code>.
 * </p>
 * 
 * <p>
 * To update the database schema the SQL statements in
 * <code>databaseSchema/<i>&lt;current_version&gt;</i>.sql</code> will be
 * executed and the version will be increased by <code>1</code>. This will be
 * repeated until no update file with the suitable version number exists.
 * </p>
 * 
 * @since 0.1
 *
 */
public class DatabaseManager implements DataSource {

	private static Logger log = LoggerFactory.getLogger(DatabaseManager.class);

	private static final String schemaFolder = "databaseSchema/";

	private final DataSource dataSource;

	public DatabaseManager(DataSource dataSource) throws IllegalArgumentException, SQLException, IOException {
		if (dataSource == null) {
			throw new IllegalArgumentException("Null value not allowd for parameter dataSource.");
		}

		log.info("initializing ...");

		this.dataSource = dataSource;

		this.initDatabseSchema();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return this.dataSource.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.dataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.dataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.dataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return this.dataSource.getLoginTimeout();
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return this.dataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.equals(DatabaseManager.class)) {
			return iface.cast(this);
		} else {
			return this.dataSource.unwrap(iface);
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if (iface.equals(DatabaseManager.class)) {
			return true;
		} else {
			return this.dataSource.isWrapperFor(iface);
		}
	}

	/**
	 * Creates the schema version table if neccesary and executes all relevant
	 * schema update scripts.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	private void initDatabseSchema() throws SQLException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try (Connection connection = this.getConnection()) {

			if (!DatabaseManager.tableExists(connection, "semantic", "schemainfo")) {
				// database schema is not initialized

				log.info("initializing database schema ...");

				// set schema version
				try (Statement statement = connection.createStatement()) {
					statement.execute("create table semantic.schemaInfo (version bigint not null)");
					statement.execute("insert into semantic.schemainfo (version) values (0)");
					statement.execute(
							"COMMENT ON TABLE semantic.schemainfo  IS 'Provides meta information about the database schema.'");
					statement.execute(
							"COMMENT ON COLUMN semantic.schemainfo.version IS 'Current version of this database schema. Used and updated in the schema update process.'");
				}
			}

			do {
				// get current schema version
				int schemaVersion;
				try (Statement statement = connection.createStatement()) {
					try (ResultSet resultSet = statement.executeQuery("select version from semantic.schemainfo")) {
						if (resultSet.next()) {
							schemaVersion = resultSet.getInt("version");
						} else {
							schemaVersion = 0;
						}
					}
				}

				// get update script
				String path = schemaFolder + schemaVersion + ".sql";
				try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
					if (inputStream != null) {
						// an update script for the current version exists

						log.info("update database schema to version " + (schemaVersion + 1) + " ...");

						// execute update script
						DatabaseManager.execute(connection, inputStream);

						// increase schema version
						try (Statement statement = connection.createStatement()) {
							statement.execute("update semantic.schemainfo set version = version + 1");
						}

					} else {
						break;
					}
				}
			} while (true);
		}
	}

	/**
	 * Executes the given SQL script using the given connection.
	 * 
	 * @param connection
	 *            connection to use to execute the SQL script
	 * @param sqlInputStream
	 *            stream of the SQL script to execute
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void execute(Connection connection, InputStream sqlInputStream)
			throws SQLException, IOException {
		// read file ( http://stackoverflow.com/a/35446009/3637482 )
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = sqlInputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		String sql = result.toString("UTF-8");

		// execute statements
		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	/**
	 * Returns <code>true</code> if the given table exists in the given schema.
	 * 
	 * @param connection
	 *            connection to use
	 * @param schemaName
	 *            schema to consider
	 * @param tableName
	 *            name of the table
	 * @return <code>true</code> if the given table exists
	 * @throws SQLException
	 */
	private static boolean tableExists(Connection connection, String schemaName, String tableName) throws SQLException {
		boolean exists;
		// get connection meta data
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		// get table list
		try (ResultSet result = databaseMetaData.getTables(connection.getSchema(), schemaName, tableName, null)) {
			exists = result.next();
		}
		return exists;
	}

}
