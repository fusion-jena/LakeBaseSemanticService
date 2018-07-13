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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_jena.cs.fusion.lakebase.util.jooq.StringToIRIConverter;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * 
 * @since 0.1
 *
 */
public class HierarchyManager implements Maintainable {

	private final static Logger log = LoggerFactory.getLogger(HierarchyManager.class);
	public final static IRI ROOT = IRI.create("http://www.w3.org/2002/07/owl#Thing");

	private final SemanticDataSource semanticDataSource;
	private final DataSource dataSource;
	private final ConceptManager conceptManager;
	private final ConceptManagerListener listener;

	public HierarchyManager(DataSource dataSource, SemanticDataSource semanticDataSource, ConceptManager conceptManager)
			throws HierarchyException {
		// logging
		log.info("initializing ...");
		long startTime = System.currentTimeMillis();

		// initializations
		this.dataSource = dataSource;
		this.semanticDataSource = semanticDataSource;
		this.conceptManager = conceptManager;
		this.listener = new HierarchyManager.Listener(this);
		conceptManager.registerListener(this.listener);

		// populate
		this.refresh();

		log.info("initialization took " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void refresh() throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				sql.truncate(table("semantic.concept_hierarchy")).execute();
			} catch (DataAccessException e) {
				throw new HierarchyException("Failed to truncate hierarchy.", e);
			}
			this.addAll(this.conceptManager.getAllUsed(), connection);
		} catch (ConceptManagerException e) {
			throw new HierarchyException("Failed to get all concepts from concept manager.", e);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the given {@link IRI} and all its broaders and synonyms provided by
	 * the {@link SemanticDataSource} to the hierarchy.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @see #add(IRI, Connection)
	 * @see #addAll(IRI)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean add(IRI iri) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return this.add(iri, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the given {@link IRI} and all its broaders and synonyms provided by
	 * the {@link SemanticDataSource} to the hierarchy.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param connection
	 *            {@link Connection} to use
	 * @see #add(IRI)
	 * @see #addAll(IRI, Connection)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean add(IRI iri, Connection connection) throws HierarchyException {
		boolean result = this.setBroader(iri, ROOT, connection);

		if (this.semanticDataSource.providingAllBroaders()) {
			try {
				result = this.setBroaders(iri, this.semanticDataSource.getAllBroaders(iri), connection) || result;
			} catch (SemanticDataSourceException e) {
				log.warn("Failed to add all broaders of \"" + iri.getIRIString() + "\".", e);
			}
		} else if (this.semanticDataSource.providingBroaders()) {
			try {
				result = this.setBroaders(iri, this.semanticDataSource.getBroaders(iri), connection) || result;
			} catch (SemanticDataSourceException e) {
				log.warn("Failed to add broaders of \"" + iri.getIRIString() + "\".", e);
			}
		}
		if (this.semanticDataSource.providingSynonyms()) {
			try {
				result = this.setSynonyms(iri, this.semanticDataSource.getSynonyms(iri), connection) || result;
			} catch (SemanticDataSourceException e) {
				log.warn("Failed to add synonyms of \"" + iri.getIRIString() + "\".", e);
			}
		}
		return result;
	}

	/**
	 * Adds the given {@link Collection} of {@link IRI}s and all their broaders
	 * and synonyms provided by the {@link SemanticDataSource} to the hierarchy.
	 * 
	 * @param iris
	 *            {@link Collection} of {@link IRI}s to add
	 * @see #add(IRI)
	 * @see #addAll(IRI, Connection)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean addAll(Collection<IRI> iris) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return this.addAll(iris, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the given {@link Collection} of {@link IRI}s and all their broaders
	 * and synonyms provided by the {@link SemanticDataSource} to the hierarchy.
	 * 
	 * @param iris
	 *            {@link Collection} of {@link IRI}s to add
	 * @param connection
	 *            {@link Connection} to use
	 * @see #add(IRI, Connection)
	 * @see #addAll(IRI)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean addAll(Collection<IRI> iris, Connection connection) throws HierarchyException {
		boolean result = false;
		for (IRI concept : iris) {
			result = this.add(concept, connection) || result;
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if the given {@link IRI} is contained in the
	 * hierarchy, otherwise <code>false</code>.
	 * 
	 * @param iri
	 *            {@link IRI} to check
	 * @return <code>true</code> if <code>concept</code> if the given concept is
	 *         contained in the hierarchy, otherwise <code>false</code>
	 * @see #contains(IRI, Connection)
	 * @throws HierarchyException
	 */
	public boolean contains(IRI iri) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return contains(iri, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns <code>true</code> if the given {@link IRI} is contained in the
	 * hierarchy, otherwise <code>false</code>.
	 * 
	 * @param iri
	 *            {@link IRI} to check
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if <code>concept</code> if the given concept is
	 *         contained in the hierarchy, otherwise <code>false</code>
	 * @see #contains(IRI)
	 * @throws HierarchyException
	 */
	public boolean contains(IRI iri, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql
					.fetchExists(DSL.select().from(table("semantic.concept_hierarchy")).join(table("semantic.concept"))
							.on(field("concept.concept_id").eq(field("concept_hierarchy.broader_concept_id"))
									.or(field("concept.concept_id").eq(field("concept_hierarchy.narrower_concept_id"))))
							.where(field("concept.concept_iri").eq(iri.getIRIString())));
		} catch (DataAccessException e) {
			throw new HierarchyException("Failed to execute contains for \"" + iri + "\".", e);
		}
	}

	/**
	 * Returns <code>true</code> if the second given {@link IRI} is a broader of
	 * the first given {@link IRI}, otherwise <code>false</code>.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @return <code>true</code> if <code>broader</code> is a broader of
	 *         <code>narrower</code>, otherwise <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean isBroader(IRI narrower, IRI broader) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return isBroader(narrower, broader, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns <code>true</code> if the second given {@link IRI} is a broader of
	 * the first given {@link IRI}, otherwise <code>false</code>.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if <code>broader</code> is a broader of
	 *         <code>narrower</code>, otherwise <code>false</code>
	 * @see #isBroader(IRI, IRI)
	 * @see #isNarrower(IRI, IRI, Connection)
	 * @see #isSynonym(IRI, IRI, Connection)
	 * @throws HierarchyException
	 */
	public boolean isBroader(IRI narrower, IRI broader, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql.fetchExists(DSL.select().from(table("semantic.concept_iri_hierarchy"))
					.where(field("broader_concept_iri").eq(broader.getIRIString())
							.and(field("narrower_concept_iri").eq(narrower.getIRIString()))));
		} catch (DataAccessException e) {
			throw new HierarchyException("Failed to execute isBroader for \"" + narrower + "\" (narrower) and  \""
					+ broader + "\" (broader).", e);
		}
	}

	/**
	 * Returns <code>true</code> if the second given {@link IRI} is a narrower
	 * of the first given {@link IRI}, otherwise <code>false</code>.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @return <code>true</code> if <code>narrower</code> is a of
	 *         <code>broader</code>, otherwise <code>false</code>
	 * @see #isBroader(IRI, IRI)
	 * @see #isNarrower(IRI, IRI, Connection)
	 * @see #isSynonym(IRI, IRI)
	 * @throws HierarchyException
	 */
	public boolean isNarrower(IRI broader, IRI narrower) throws HierarchyException {
		return this.isBroader(narrower, broader);
	}

	/**
	 * Returns <code>true</code> if the second given {@link IRI} is a narrower
	 * of the first given {@link IRI}, otherwise <code>false</code>.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if <code>narrower</code> is a of
	 *         <code>broader</code>, otherwise <code>false</code>
	 * @see #isBroader(IRI, IRI, Connection)
	 * @see #isNarrower(IRI, IRI)
	 * @see #isSynonym(IRI, IRI, Connection)
	 * @throws HierarchyException
	 */
	public boolean isNarrower(IRI broader, IRI narrower, Connection connection) throws HierarchyException {
		return this.isBroader(narrower, broader, connection);
	}

	/**
	 * Returns <code>true</code> if both given {@link IRI} are synonyms,
	 * otherwise <code>false</code>.
	 * 
	 * @param iri1
	 *            {@link IRI} of the first concept
	 * @param iri2
	 *            {@link IRI} of the second concept
	 * @return <code>true</code> if both given {@link IRI} are synonyms,
	 *         otherwise <code>false</code>
	 * @see #isBroader(IRI, IRI)
	 * @see #isNarrower(IRI, IRI)
	 * @see #isSynonym(IRI, IRI, Connection)
	 * @throws HierarchyException
	 */
	public boolean isSynonym(IRI iri1, IRI iri2) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return isSynonym(iri1, iri2, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns <code>true</code> if both given {@link IRI} are synonyms,
	 * otherwise <code>false</code>.
	 * 
	 * @param iri1
	 *            {@link IRI} of the first concept
	 * @param iri2
	 *            {@link IRI} of the second concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if both given {@link IRI} are synonyms,
	 *         otherwise <code>false</code>
	 * @see #isBroader(IRI, IRI, Connection)
	 * @see #isNarrower(IRI, IRI, Connection)
	 * @see #isSynonym(IRI, IRI)
	 * @throws HierarchyException
	 */
	public boolean isSynonym(IRI iri1, IRI iri2, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql.fetchExists(DSL.select().from(table("semantic.concept_iri_hierarchy").as("a"))
					.join(table("semantic.concept_iri_hierarchy").as("b"))
					.on(field("a.narrower_concept_id").eq(field("b.broader_concept_id"))
							.and(field("b.narrower_concept_id").eq(field("a.broader_concept_id"))))
					.where(field("a.narrower_concept_iri").eq(iri1.getIRIString())
							.and(field("a.broader_concept_iri").eq(iri2.getIRIString()))));
		} catch (DataAccessException e) {
			throw new HierarchyException(
					"Failed to execute isBroader for \"" + iri1 + "\" (narrower) and  \"" + iri2 + "\" (broader).", e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the broader concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the {@link IRI}s of the broader concepts of
	 *         a given {@link IRI}
	 * @see #getBroaders(IRI, Connection)
	 * @see #getNarrowers(IRI)
	 * @see #getSynonyms(IRI)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getBroaders(IRI iri) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return getBroaders(iri, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the broader concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return {@link Collection} of the {@link IRI}s of the broader concepts of
	 *         a given {@link IRI}
	 * @see #getBroaders(IRI)
	 * @see #getNarrowers(IRI, Connection)
	 * @see #getSynonyms(IRI, Connection)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getBroaders(IRI iri, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql.select().from(table("semantic.concept_iri_hierarchy").as("h"))
					.where(field("h.narrower_concept_iri").eq(iri.getIRIString()))
					.and(field("h.broader_concept_id").ne(field("h.narrower_concept_id")))
					.andNotExists(DSL.selectOne().from(table("semantic.concept_hierarchy").as("e"))
							.where(field("e.broader_concept_id").eq(field("h.narrower_concept_id")))
							.and(field("h.broader_concept_id").eq(field("e.narrower_concept_id"))))
					.fetch("broader_concept_iri", new StringToIRIConverter());
		} catch (DataAccessException e) {
			throw new HierarchyException("Failed to get broaders of \"" + iri + "\".", e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the narrower concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the {@link IRI}s of the narrower concepts
	 *         of a given {@link IRI}
	 * @see #getBroaders(IRI)
	 * @see #getNarrowers(IRI, Connection)
	 * @see #getSynonyms(IRI)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getNarrowers(IRI iri) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return getNarrowers(iri, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the narrower concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return {@link Collection} of the {@link IRI}s of the narrower concepts
	 *         of a given {@link IRI}
	 * @see #getBroaders(IRI, Connection)
	 * @see #getNarrowers(IRI)
	 * @see #getSynonyms(IRI, Connection)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getNarrowers(IRI iri, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql.select().from(table("semantic.concept_iri_hierarchy").as("h"))
					.where(field("h.broader_concept_iri").eq(iri.getIRIString()))
					.and(field("h.broader_concept_id").ne(field("h.narrower_concept_id")))
					.andNotExists(DSL.selectOne().from(table("semantic.concept_hierarchy").as("e"))
							.where(field("e.broader_concept_id").eq(field("h.narrower_concept_id")))
							.and(field("h.broader_concept_id").eq(field("e.narrower_concept_id"))))
					.fetchSet(field("narrower_concept_iri", String.class), new StringToIRIConverter());
		} catch (DataAccessException e) {
			throw new HierarchyException("Failed to get narrowers of \"" + iri + "\".", e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the synonym concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @return {@link Collection} of the {@link IRI}s of the synonym concepts of
	 *         a given {@link IRI}
	 * @see #getBroaders(IRI)
	 * @see #getNarrowers(IRI)
	 * @see #getSynonyms(IRI, Connection)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getSynonyms(IRI iri) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return getSynonyms(iri, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Returns a {@link Collection} of the {@link IRI}s of the synonym concepts
	 * of a given {@link IRI}.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return {@link Collection} of the {@link IRI}s of the synonym concepts of
	 *         a given {@link IRI}
	 * @see #getBroaders(IRI, Connection)
	 * @see #getNarrowers(IRI, Connection)
	 * @see #getSynonyms(IRI)
	 * @throws HierarchyException
	 */
	public Collection<IRI> getSynonyms(IRI iri, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			return sql.select().from(table("semantic.concept_iri_hierarchy").as("a"))
					.join(table("semantic.concept_hierarchy").as("b"))
					.on(field("a.narrower_concept_id").eq(field("b.broader_concept_id"))
							.and(field("b.narrower_concept_id").eq(field("a.broader_concept_id"))))
					.where(field("narrower_concept_iri").eq(iri.getIRIString()))
					.fetch("broader_concept_iri", new StringToIRIConverter());
		} catch (DataAccessException e) {
			throw new HierarchyException("Failed to get synonyms of \"" + iri + "\".", e);
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		try {
			this.refresh();
		} catch (HierarchyException e) {
			throw new MaintenanceException("Failed to refresh concept hierarchy.", e);
		}
	}

	/**
	 * Adds the second concept to the broader concepts of the first concept.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @see #setBroader(IRI, IRI, Connection)
	 * @see #setBroaders(IRI, IRI)
	 * @throws HierarchyException
	 */
	public boolean setBroader(IRI narrower, IRI broader) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return setBroader(narrower, broader, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the second concept to the broader concepts of the first concept.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broader
	 *            {@link IRI} of the broader concept
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @see #setBroader(IRI, IRI, Connection)
	 * @see #setBroaders(IRI, IRI)
	 * @throws HierarchyException
	 */
	public boolean setBroader(IRI narrower, IRI broader, Connection connection) throws HierarchyException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			this.conceptManager.add(connection, broader);
			this.conceptManager.add(connection, narrower, this.listener);
			return sql.insertInto(table("semantic.concept_hierarchy"))
					.columns(field("narrower_concept_id"), field("broader_concept_id"))
					.select(/* direct broader */select(field("n.concept_id"), field("b.concept_id"))
							.from(table("semantic.concept").as("n")).crossJoin(table("semantic.concept").as("b"))
							.where(field("n.concept_iri").eq(narrower.getIRIString()))
							.and(field("b.concept_iri").eq(broader.getIRIString()))
							.and(field("n.concept_id").ne(field("b.concept_id")))
							.andNotExists(DSL.selectOne().from(table("semantic.concept_hierarchy").as("e"))
									.where(field("e.narrower_concept_id").eq(field("n.concept_id"))
											.and(field("e.broader_concept_id").eq(field("b.concept_id")))))
							.union(/* own transitive broaders */select(field("n.concept_id"),
									field("h.broader_concept_id")).from(table("semantic.concept").as("n"))
											.crossJoin(table("semantic.concept").as("b"))
											.join(table("semantic.concept_hierarchy").as("h"))
											.on(field("b.concept_id").eq(field("h.narrower_concept_id")))
											.where(field("n.concept_iri").eq(narrower.getIRIString()))
											.and(field("b.concept_iri").eq(broader.getIRIString()))
											.and(field("n.concept_id").ne(field("h.broader_concept_id")))
											.andNotExists(DSL.selectOne()
													.from(table("semantic.concept_hierarchy").as("e")).where(
															field("e.narrower_concept_id").eq(field("n.concept_id"))
																	.and(field("e.broader_concept_id")
																			.eq(field("h.broader_concept_id"))))))
							.union(/* others transitive broaders */select(field("h.narrower_concept_id"),
									field("b.concept_id")).from(table("semantic.concept").as("n"))
											.crossJoin(table("semantic.concept").as("b"))
											.join(table("semantic.concept_hierarchy").as("h"))
											.on(field("n.concept_id").eq(field("h.broader_concept_id")))
											.where(field("n.concept_iri").eq(narrower.getIRIString()))
											.and(field("b.concept_iri").eq(broader.getIRIString()))
											.and(field("h.narrower_concept_id").ne(field("b.concept_id"))).andNotExists(
													DSL.selectOne().from(table("semantic.concept_hierarchy").as("e"))
															.where(field("e.narrower_concept_id")
																	.eq(field("h.narrower_concept_id"))
																	.and(field("e.broader_concept_id")
																			.eq(field("b.concept_id")))))))
					.execute() > 0;
		} catch (DataAccessException | ConceptManagerException e) {
			throw new HierarchyException("Failed to set broader \"" + broader + "\" of narrower \"" + narrower + "\".",
					e);
		}
	}

	/**
	 * Adds the {@link Collection} of concepts to the broader concepts of the
	 * concept.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broaders
	 *            {@link Collection} of {@link IRI}s of the broader concepts
	 * @see #setBroader(IRI, IRI)
	 * @see #setBroaders(IRI, IRI, Connection)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean setBroaders(IRI narrower, Collection<IRI> broaders) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return setBroaders(narrower, broaders, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the {@link Collection} of concepts to the broader concepts of the
	 * concept.
	 * 
	 * @param narrower
	 *            {@link IRI} of the narrower concept
	 * @param broaders
	 *            {@link Collection} of {@link IRI}s of the broader concepts
	 * @param connection
	 *            {@link Connection} to use
	 * @see #setBroader(IRI, IRI, Connection)
	 * @see #setBroaders(IRI, IRI)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean setBroaders(IRI narrower, Collection<IRI> broaders, Connection connection)
			throws HierarchyException {
		boolean result = false;
		for (IRI broader : broaders) {
			result = this.setBroader(narrower, broader, connection) || result;
		}
		return result;
	}

	/**
	 * Adds the synonym concept to the synonyms of the concept.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param synonym
	 *            {@link IRI} of the synonym
	 * @throws HierarchyException
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @see #setSynonym(IRI, IRI, Connection)
	 * @see #setSynonyms(IRI, IRI)
	 * @throws HierarchyException
	 */
	public boolean setSynonym(IRI iri, IRI synonym) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return setSynonym(iri, synonym, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the synonym concept to the synonyms of the concept.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param synonym
	 *            {@link IRI} of the synonym
	 * @param connection
	 *            {@link Connection} to use
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @see #setSynonym(IRI, IRI)
	 * @see #setSynonyms(IRI, IRI, Connection)
	 * @throws HierarchyException
	 */
	public boolean setSynonym(IRI iri, IRI synonym, Connection connection) throws HierarchyException {
		return this.setBroader(iri, synonym, connection) | this.setBroader(synonym, iri, connection);
	}

	/**
	 * Adds the {@link Collection} of concepts to the synonyms of the concept.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param synonyms
	 *            {@link Collection} of {@link IRI}s of the synonyms
	 * @see #setSynonym(IRI, IRI)
	 * @see #setSynonyms(IRI, IRI, Connection)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean setSynonyms(IRI iri, Collection<IRI> synonyms) throws HierarchyException {
		try (Connection connection = this.dataSource.getConnection()) {
			return setSynonyms(iri, synonyms, connection);
		} catch (SQLException e) {
			throw new HierarchyException(e);
		}
	}

	/**
	 * Adds the {@link Collection} of concepts to the synonyms of the concept.
	 * 
	 * @param iri
	 *            {@link IRI} of the concept
	 * @param synonyms
	 *            {@link Collection} of {@link IRI}s of the synonyms
	 * @param connection
	 *            {@link Connection} to use
	 * @see #setSynonym(IRI, IRI, Connection)
	 * @see #setSynonyms(IRI, IRI)
	 * @return <code>true</code> if the hierarchy has been changed, otherwise
	 *         <code>false</code>
	 * @throws HierarchyException
	 */
	public boolean setSynonyms(IRI iri, Collection<IRI> synonyms, Connection connection) throws HierarchyException {
		boolean result = false;
		for (IRI synonym : synonyms) {
			result = this.setSynonym(iri, synonym, connection) || result;
		}
		return result;
	}

	/**
	 * Listener to trigger hierarchy updates.
	 * 
	 * @since 0.1
	 *
	 */
	private class Listener implements ConceptManagerListener {

		private final HierarchyManager hierarchyManager;

		public Listener(HierarchyManager hierarchyManager) {
			this.hierarchyManager = hierarchyManager;
		}

		@Override
		public void newConcept(IRI iri) throws ConceptManagerListenerException {
			try {
				this.hierarchyManager.add(iri);
			} catch (HierarchyException e) {
				throw new ConceptManagerListenerException("Failed to add new concept \"" + iri.getIRIString() + "\".",
						e);
			}
		}

		@Override
		public void newConcept(IRI iri, Connection connection) throws ConceptManagerListenerException {
			try {
				this.hierarchyManager.add(iri, connection);
			} catch (HierarchyException e) {
				throw new ConceptManagerListenerException("Failed to add new concept \"" + iri.getIRIString() + "\".",
						e);
			}
		}

		@Override
		public void newConcepts(Collection<IRI> iris) throws ConceptManagerListenerException {
			try {
				this.hierarchyManager.addAll(iris);
			} catch (HierarchyException e) {
				throw new ConceptManagerListenerException("Failed to add new concepts.", e);
			}
		}

		@Override
		public void newConcepts(Collection<IRI> iris, Connection connection) throws ConceptManagerListenerException {
			try {
				this.hierarchyManager.addAll(iris, connection);
			} catch (HierarchyException e) {
				throw new ConceptManagerListenerException("Failed to add new concepts.", e);
			}
		}

	}
}
