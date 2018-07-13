package de.uni_jena.cs.fusion.semantic.datasource.fallback;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.AbstractSemanticDataSourceWrapper;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.stopwords.StopWords;

/**
 * <p>
 * An {@link SemanticDataSource} that provides an matching concept {@link IRI}
 * for each given term except some stop words as a fall back for terms not
 * covered by any other {@link SemanticDataSource}.
 * </p>
 * 
 * <p>
 * The provided {@link IRI}s will be URNs with the namespace
 * <code>string:</code>.
 * </p>
 * 
 * @author Jan Martin Keil
 * @since 0.1
 *
 */
public class KeywordFallbackWrapper extends AbstractSemanticDataSourceWrapper {

	public final static String namespace = "string:";
	public final static IRI scope = IRI.create("http://example.org/scope/fallback");

	private final DataSource dataSource;
	private final double ranking;
	private String sqlGetConcepts;

	public KeywordFallbackWrapper(double ranking, DataSource dataSource, String getConceptStatementSql,
			SemanticDataSource semanticDataSource) {
		this.ranking = ranking;
		this.dataSource = dataSource;
		this.sqlGetConcepts = getConceptStatementSql;
		this.setWrapped(semanticDataSource);
	}

	public KeywordFallbackWrapper(double ranking, DataSource dataSource, String tableName, String columnName,
			SemanticDataSource semanticDataSource) {
		this(ranking, dataSource, "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName
				+ " LIKE '" + namespace + "%'", semanticDataSource);
	}

	private IRI createIRI(String term) {
		return IRI.create(namespace + term.toLowerCase());
	}

	private String extractLabel(IRI iri) {
		return iri.getIRIString().substring(namespace.length());
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		Set<IRI> allBroaders = new HashSet<IRI>(this.getWrapped().getAllBroaders(iri));
		for (String label : this.getWrapped().getLabels(iri)) {
			for (String term : label.split(" ")) {
				if (validWord(term)) {
					allBroaders.add(createIRI(term));
				}
			}
		}
		return allBroaders;
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		Set<IRI> broaders = new HashSet<IRI>(this.getWrapped().getBroaders(iri));
		for (String label : this.getWrapped().getLabels(iri)) {
			for (String term : label.split(" ")) {
				if (validWord(term)) {
					broaders.add(createIRI(term));
				}
			}
		}
		return broaders;
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		if (iri.getIRIString().startsWith(namespace)) {
			return Collections.singleton(extractLabel(iri));
		} else if (this.getWrapped().providingLabels()) {
			return this.getWrapped().getLabels(iri);
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms) throws SemanticDataSourceException {
		if (this.getWrapped().providingMatch()) {
			Map<String, Map<IRI, Double>> wrappedMatches = this.getWrapped().getMatches(terms);
			Map<String, Map<IRI, Double>> matches = new HashMap<String, Map<IRI, Double>>();
			for (String term : terms) {
				if (validWord(term)) {
					if (wrappedMatches.get(term).isEmpty()) {
						matches.put(term, Collections.singletonMap(createIRI(term), this.ranking));
					} else {
						Map<IRI, Double> match = new HashMap<IRI, Double>();
						match.putAll(wrappedMatches.get(term));
						matches.put(term, match);
					}
				} else {
					matches.put(term, wrappedMatches.get(term));
				}
			}
			return matches;
		} else {
			Map<String, Map<IRI, Double>> matches = new HashMap<String, Map<IRI, Double>>();
			for (String term : terms) {
				if (validWord(term)) {
					matches.put(term, Collections.singletonMap(createIRI(term), this.ranking));
				} else {
					matches.put(term, Collections.emptyMap());
				}
			}
			return matches;
		}
	}

	@Override
	public Map<String, Map<IRI, Double>> getMatches(Collection<String> terms, Collection<IRI> scopes)
			throws SemanticDataSourceException {
		if (this.getWrapped().providingMatch()) {
			if (scopes.contains(scope)) {
				Map<String, Map<IRI, Double>> wrappedMatches = this.getWrapped().getMatches(terms, scopes);
				Map<String, Map<IRI, Double>> matches = new HashMap<String, Map<IRI, Double>>();
				for (String term : terms) {
					if (validWord(term)) {
						if (wrappedMatches.get(term).isEmpty()) {
							matches.put(term, Collections.singletonMap(createIRI(term), this.ranking));
						} else {
							Map<IRI, Double> match = new HashMap<IRI, Double>();
							match.putAll(wrappedMatches.get(term));
							matches.put(term, match);
						}
					} else {
						matches.put(term, wrappedMatches.get(term));
					}
				}
				return matches;
			} else {
				return this.getWrapped().getMatches(terms, scopes);
			}
		} else {
			if (scopes.contains(scope)) {
				Map<String, Map<IRI, Double>> matches = new HashMap<String, Map<IRI, Double>>();
				for (String term : terms) {
					if (validWord(term)) {
						matches.put(term, Collections.singletonMap(createIRI(term), this.ranking));
					} else {
						matches.put(term, Collections.emptyMap());
					}
				}
				return matches;
			} else {
				return Collections.emptyMap();
			}
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		if (this.getWrapped().providingMatch()) {
			if (validWord(term)) {
				Map<IRI, Double> match = new HashMap<IRI, Double>(this.getWrapped().getMatches(term));
				match.put(createIRI(term), this.ranking);
				return match;
			} else {
				return this.getWrapped().getMatches(term);
			}
		} else {
			if (validWord(term)) {
				return Collections.singletonMap(createIRI(term), this.ranking);
			} else {
				return Collections.emptyMap();
			}
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term, Collection<IRI> scopes) throws SemanticDataSourceException {
		if (this.getWrapped().providingMatch()) {
			if (scopes.contains(scope) & validWord(term)) {
				Map<IRI, Double> match = new HashMap<IRI, Double>(this.getWrapped().getMatches(term));
				match.put(createIRI(term), this.ranking);
				return match;
			} else {
				return this.getWrapped().getMatches(term);
			}
		} else {
			if (scopes.contains(scope) & validWord(term)) {
				return Collections.singletonMap(createIRI(term), this.ranking);
			} else {
				return Collections.emptyMap();
			}
		}
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		Set<String> namespaces = new HashSet<String>(this.getWrapped().getNamespaces());
		namespaces.add(namespace);
		return namespaces;
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		Set<IRI> scopes = new HashSet<IRI>(this.getWrapped().getScopes());
		scopes.add(scope);
		return scopes;
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		List<IRI> result = new ArrayList<IRI>(this.getWrapped().getSignature());
		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement pStatement = connection.prepareStatement(sqlGetConcepts)) {
				try (ResultSet resultSet = pStatement.executeQuery()) {
					while (resultSet.next()) {
						result.add(IRI.create(resultSet.getString(1)));
					}
				}
			}
		} catch (SQLException e) {
			throw new SemanticDataSourceException("Error during database communication.", e);
		}
		return result;
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		if (iri.getIRIString().startsWith(namespace)) {
			return iri.getIRIString().startsWith(namespace);
		} else {
			return this.getWrapped().isPresent(iri);
		}
	}

	@Override
	public boolean providingLabels() {
		return true;
	}

	@Override
	public boolean providingMatch() {
		return true;
	}

	@Override
	public void setMatchThreshold(double threshold) {
		this.getWrapped().setMatchThreshold(threshold);
	}

	private boolean validWord(String word) {
		return !word.contains(" ") && !StopWords.isStopWord(word);
	}
}
