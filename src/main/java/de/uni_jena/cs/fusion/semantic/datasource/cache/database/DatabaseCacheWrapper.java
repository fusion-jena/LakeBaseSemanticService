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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.AbstractSemanticDataSourceWrapper;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * <p>
 * An {@link SemanticDataSource} that wraps an other {@link SemanticDataSource}
 * and caches the results of called methods in a database. Outdated results must
 * be purged using {@link #maintain()}.
 * </p>
 * 
 * <p>
 * The {@link DatabaseCacheWrapper} requires a database table with the following
 * columns:
 * </p>
 * 
 * <ul>
 * <li><i>adapter</i>: bigint, primary key</li>
 * <li><i>task</i>: smallint, primary key</li>
 * <li><i>key</i>: text, primary key</li>
 * <li><i>value</i>: binary</li>
 * <li><i>timestamp</i>: timestamp, default current_timestamp</li>
 * </ul>
 * 
 * <p>
 * SQL Example:
 * </p>
 * <dl>
 * <dt>PostgreSQL</dt>
 * <dd>{@code CREATE TABLE <tableName>
 * (adapter bigint, task smallint, key text, value byte, timestamp timestam DEFAULT current_timestamp, CONSTRAINT adapter_cache_pk PRIMARY KEY (adapter,task,key))}</dd>
 * </dl>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public final class DatabaseCacheWrapper extends AbstractSemanticDataSourceWrapper implements Maintainable {

	private enum Task {
		GET_ALL_BROADERS(1), GET_SIGNATURE(2), GET_ALL_NARROWERS(3), GET_ALTERNATIVE_LABELS(4), GET_BROADERS(
				5), GET_CUSTOM_RELATION_PROPERTIES(16), GET_CUSTOM_RELATIONS(
						17), GET_CUSTOM_VALUE_PROPERTIES(14), GET_CUSTOM_VALUES(15), GET_DESCRIPTIONS(6), GET_LABELS(
								7), GET_NARROWERS(8), GET_REPLACED_BY(16), GET_SYNONYMS(9), GET_URLS(
										10), IS_DEPRECATED(18), IS_PRESENT(11), GET_MATCHES(12), GET_SUGGESTIONS(13);

		private final int id;

		private Task(int id) {
			this.id = id;
		}
	}

	/**
	 * Wraps an {@link SemanticDataSource} into a {@link DatabaseCacheWrapper}.
	 * 
	 * @param semanticDataSource
	 *            the {@link SemanticDataSource} to wrap if neccesary
	 * @param datasource
	 *            the {@link DataSource} to use
	 * @param tableName
	 *            the name of the table in the database to use
	 * @return a {@link DatabaseCacheWrapper} wrapping the given
	 *         {@link SemanticDataSource}
	 * @param maxObjectAge
	 *            max age of cached objects in seconds
	 * @throws SemanticDataSourceException
	 */
	public static DatabaseCacheWrapper wrap(SemanticDataSource semanticDataSource, DataSource datasource,
			String tableName, long maxObjectAge) throws SemanticDataSourceException {
		return new DatabaseCacheWrapper(semanticDataSource, datasource, tableName, maxObjectAge);
	}

	private DataSource datasource;
	private String loadSQL;
	private String putSQL;
	private String cleanSQL;
	private long maxObjectAge;
	private long UID;

	/**
	 * 
	 * @param semanticDataSource
	 * @param datasource
	 * @param tableName
	 * @param maxObjectAge
	 *            max age of cached objects in seconds
	 * @throws SemanticDataSourceException
	 */
	private DatabaseCacheWrapper(SemanticDataSource semanticDataSource, DataSource datasource, String tableName,
			long maxObjectAge) throws SemanticDataSourceException {
		this.setWrapped(semanticDataSource);
		this.UID = getWrapped().getUID();
		this.datasource = datasource;
		this.loadSQL = "SELECT value FROM " + tableName + " WHERE adapter = ? AND task = ? AND key = ?";
		this.putSQL = "INSERT INTO " + tableName + " (adapter, task, key, value) ";
		this.putSQL += "SELECT ?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM  " + tableName + " ";
		this.putSQL += "WHERE adapter = ? AND task = ? AND key = ? )";
		this.cleanSQL = "DELETE FROM " + tableName;
		this.cleanSQL += " WHERE adapter = ? AND current_timestamp - timestamp > ? * interval '1 second'";
		this.maxObjectAge = maxObjectAge;
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingAllBroaders()) {
			Collection<IRI> result = loadCollection(Task.GET_ALL_BROADERS, iri.getIRIString(), IRI.class);
			if (result == null) {
				result = getWrapped().getAllBroaders(iri);
				put(Task.GET_ALL_BROADERS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getAllNarrowers(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingAllNarrowers()) {
			Collection<IRI> result = loadCollection(Task.GET_ALL_NARROWERS, iri.getIRIString(), IRI.class);
			if (result == null) {
				result = getWrapped().getAllNarrowers(iri);
				put(Task.GET_ALL_NARROWERS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingAlternativeLabels()) {
			Collection<String> result = loadCollection(Task.GET_ALTERNATIVE_LABELS, iri.getIRIString(), String.class);
			if (result == null) {
				result = getWrapped().getAlternativeLabels(iri);
				put(Task.GET_ALTERNATIVE_LABELS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingBroaders()) {
			Collection<IRI> result = loadCollection(Task.GET_BROADERS, iri.getIRIString(), IRI.class);
			if (result == null) {
				result = getWrapped().getBroaders(iri);
				put(Task.GET_BROADERS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private byte[] getBytes(Object object) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
		objectStream.writeObject(object);
		objectStream.flush();
		return byteStream.toByteArray();
	}

	@Override
	public Collection<String> getCustomRelationProperties() throws SemanticDataSourceException {
		if (getWrapped().providingCustomProperties()) {
			Collection<String> result = loadCollection(Task.GET_CUSTOM_RELATION_PROPERTIES, "", String.class);
			if (result == null) {
				result = getWrapped().getCustomRelationProperties();
				put(Task.GET_CUSTOM_RELATION_PROPERTIES, "", result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getCustomRelations(IRI iri, String property) throws SemanticDataSourceException {
		if (getWrapped().providingCustomProperties()) {
			String key = iri.getIRIString() + "\n" + property;
			Collection<IRI> result = loadCollection(Task.GET_CUSTOM_RELATIONS, key, IRI.class);
			if (result == null) {
				result = getWrapped().getCustomRelations(iri, property);
				put(Task.GET_CUSTOM_RELATIONS, key, result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getCustomValueProperties() throws SemanticDataSourceException {
		if (getWrapped().providingCustomProperties()) {
			Collection<String> result = loadCollection(Task.GET_CUSTOM_VALUE_PROPERTIES, "", String.class);
			if (result == null) {
				result = getWrapped().getCustomValueProperties();
				put(Task.GET_CUSTOM_VALUE_PROPERTIES, "", result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getCustomValues(IRI iri, String property) throws SemanticDataSourceException {
		if (getWrapped().providingCustomProperties()) {
			String key = iri.getIRIString() + "\n" + property;
			Collection<String> result = loadCollection(Task.GET_CUSTOM_VALUES, key, String.class);
			if (result == null) {
				result = getWrapped().getCustomValues(iri, property);
				put(Task.GET_CUSTOM_VALUES, key, result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingDescriptions()) {
			Collection<String> result = loadCollection(Task.GET_DESCRIPTIONS, iri.getIRIString(), String.class);
			if (result == null) {
				result = getWrapped().getDescriptions(iri);
				put(Task.GET_DESCRIPTIONS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingLabels()) {
			Collection<String> result = loadCollection(Task.GET_LABELS, iri.getIRIString(), String.class);
			if (result == null) {
				result = getWrapped().getLabels(iri);
				put(Task.GET_LABELS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		if (getWrapped().providingMatch()) {
			Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();
			Collection<String> uncachedStumps = new ArrayList<String>();
			for (String term : terms) {
				Map<IRI, Double> result = loadMap(Task.GET_MATCHES, term, IRI.class, Double.class);
				if (result == null) {
					uncachedStumps.add(term);
				} else {
					results.put(term, result);
				}
			}
			if (!uncachedStumps.isEmpty()) {
				Map<String, Map<IRI, Double>> uncachesResults = getWrapped().getMatches(uncachedStumps);
				for (String term : uncachesResults.keySet()) {
					put(Task.GET_MATCHES, term, uncachesResults.get(term));
				}
				results.putAll(uncachesResults);
			}
			return results;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		if (getWrapped().providingMatch()) {
			Map<IRI, Double> result = loadMap(Task.GET_MATCHES, term, IRI.class, Double.class);
			if (result == null) {
				result = getWrapped().getMatches(term);
				put(Task.GET_MATCHES, term, result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return getWrapped().getNamespaces();
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingNarrowers()) {
			Collection<IRI> result = loadCollection(Task.GET_NARROWERS, iri.getIRIString(), IRI.class);
			if (result == null) {
				result = getWrapped().getNarrowers(iri);
				put(Task.GET_NARROWERS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingDeprecation()) {
			Collection<IRI> result = loadCollection(Task.GET_REPLACED_BY, "", IRI.class);
			if (result == null) {
				result = getWrapped().getReplacedBy(iri);
				put(Task.GET_REPLACED_BY, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return getWrapped().getScopes();
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		if (getWrapped().providingSignature()) {
			Collection<IRI> result = loadCollection(Task.GET_SIGNATURE, "", IRI.class);
			if (result == null) {
				result = getWrapped().getSignature();
				put(Task.GET_SIGNATURE, "", result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Map<String, Map<IRI, String>> getSuggestions(Collection<String> stumps) throws SemanticDataSourceException {
		if (getWrapped().providingSuggest()) {
			Map<String, Map<IRI, String>> results = new HashMap<String, Map<IRI, String>>();
			Collection<String> uncachedStumps = new ArrayList<String>();
			for (String stump : stumps) {
				Map<IRI, String> result = loadMap(Task.GET_SUGGESTIONS, stump, IRI.class, String.class);
				if (result == null) {
					uncachedStumps.add(stump);
				} else {
					results.put(stump, result);
				}
			}
			if (!uncachedStumps.isEmpty()) {
				Map<String, Map<IRI, String>> uncachesResults = getWrapped().getSuggestions(uncachedStumps);
				for (String stump : uncachesResults.keySet()) {
					put(Task.GET_SUGGESTIONS, stump, uncachesResults.get(stump));
				}
				results.putAll(uncachesResults);
			}
			return results;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		if (getWrapped().providingSuggest()) {
			Map<IRI, String> result = loadMap(Task.GET_SUGGESTIONS, stump, IRI.class, String.class);
			if (result == null) {
				result = getWrapped().getSuggestions(stump);
				put(Task.GET_SUGGESTIONS, stump, result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingSynonyms()) {
			Collection<IRI> result = loadCollection(Task.GET_SYNONYMS, iri.getIRIString(), IRI.class);
			if (result == null) {
				result = getWrapped().getSynonyms(iri);
				put(Task.GET_SYNONYMS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingURLs()) {
			List<URL> result = loadList(Task.GET_URLS, iri.getIRIString(), URL.class);
			if (result == null) {
				result = getWrapped().getUrls(iri);
				put(Task.GET_URLS, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		if (getWrapped().providingDeprecation()) {
			Boolean result = load(Task.IS_DEPRECATED, iri.getIRIString(), Boolean.class);
			if (result == null) {
				result = getWrapped().isDeprecated(iri);
				put(Task.IS_DEPRECATED, iri.getIRIString(), result);
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		Boolean result = loadBoolean(Task.IS_PRESENT, iri.getIRIString());
		if (result == null) {
			result = getWrapped().isPresent(iri);
			put(Task.IS_PRESENT, iri.getIRIString(), result);
		}
		return result;
	}

	private <T> T load(Task task, String key, Class<T> type) throws SemanticDataSourceException {
		try (Connection connection = datasource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(this.loadSQL)) {
				statement.setLong(1, this.UID);
				statement.setInt(2, task.id);
				statement.setString(3, key);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						try {
							Object deserialized = new ObjectInputStream(resultSet.getBinaryStream(1)).readObject();
							if (type.isAssignableFrom(deserialized.getClass())) {
								return type.cast(deserialized);
							} else {
								throw new SemanticDataSourceException(
										"Failed to deserialize object: Expected \"" + type.getName()
												+ "\", encountered \"" + deserialized.getClass().getName() + "\"");
							}
						} catch (IOException | ClassNotFoundException e) {
							throw new SemanticDataSourceException("Failed to deserialize object.", e);
						}
					} else {
						return null;
					}
				}
			}
		} catch (SQLException e) {
			throw new SemanticDataSourceException("Failed to read cache from database.", e);
		}
	}

	private Boolean loadBoolean(Task task, String key) throws SemanticDataSourceException {
		return load(task, key, Boolean.class);
	}

	@SuppressWarnings("unchecked")
	private <V> Collection<V> loadCollection(Task task, String key,
			@SuppressWarnings("unused") Class<V> collectionValueType) throws SemanticDataSourceException {
		return load(task, key, Collection.class);
	}

	@SuppressWarnings("unchecked")
	private <V> List<V> loadList(Task task, String key, @SuppressWarnings("unused") Class<V> collectionValueType)
			throws SemanticDataSourceException {
		return load(task, key, List.class);
	}

	@SuppressWarnings("unchecked")
	private <K, V> Map<K, V> loadMap(Task task, String key, @SuppressWarnings("unused") Class<K> mapKeyType,
			@SuppressWarnings("unused") Class<V> mapValueType) throws SemanticDataSourceException {
		return load(task, key, Map.class);
	}

	private <T> void put(Task task, String key, T value) throws SemanticDataSourceException {
		try (Connection connection = datasource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(this.putSQL)) {
				statement.setLong(1, this.UID);
				statement.setInt(2, task.id);
				statement.setString(3, key);
				statement.setBytes(4, getBytes(value));
				statement.setLong(5, this.UID);
				statement.setInt(6, task.id);
				statement.setString(7, key);
				statement.execute();
			}
		} catch (SQLException e) {
			throw new SemanticDataSourceException("Failed to write cache into database.", e);
		} catch (IOException e) {
			throw new SemanticDataSourceException("Failed to serialize object.", e);
		}
	}

	@Override
	public void setMatchThreshold(double threshold) {
		if (getWrapped().providingMatch()) {
			getWrapped().setMatchThreshold(threshold);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		try (Connection connection = datasource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(this.cleanSQL)) {
				statement.setLong(1, this.UID);
				statement.setLong(2, this.maxObjectAge);
				statement.execute();
			}
		} catch (SQLException e) {
			throw new MaintenanceException("Failed to purge cache.", e);
		}
	}
}
