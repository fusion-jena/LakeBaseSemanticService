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
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Vector;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.util.jooq.StringToIRIConverter;

public class ConceptManager {

	private final DataSource dataSource;
	private final Collection<EventListener> listeners = new Vector<EventListener>();

	public ConceptManager(DataSource dataSource) throws ConceptManagerException {
		this.dataSource = dataSource;
	}

	public void registerListener(EventListener listener) {
		this.listeners.add(listener);
	}

	public boolean add(IRI iri) throws ConceptManagerException {
		return this.add(iri, null);
	}

	public boolean add(Connection connection, IRI iri) throws ConceptManagerException {
		return this.add(connection, iri, null);
	}

	public boolean add(IRI iri, ConceptManagerListener ignoringListener) throws ConceptManagerException {
		try (Connection connection = dataSource.getConnection()) {
			return this.add(connection, iri, ignoringListener);
		} catch (SQLException e) {
			throw new ConceptManagerException("Failed to add IRI \"" + iri + "\".", e);
		}
	}

	public boolean add(Connection connection, IRI iri, ConceptManagerListener ignoringListener)
			throws ConceptManagerException {
		try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5);) {

			int result = sql.insertInto(table("semantic.concept"), field("concept_iri")).values(iri.getIRIString())
					.onDuplicateKeyIgnore().execute();

			if (result > 0) {
				for (EventListener listener : listeners) {
					if (listener instanceof ConceptManagerListener
							&& (ignoringListener == null || ignoringListener != listener)) {
						((ConceptManagerListener) listener).newConcept(iri, connection);
					}
				}
			}

			return result > 0;

		} catch (DataAccessException | ConceptManagerListenerException e) {
			throw new ConceptManagerException("Failed to add IRI \"" + iri + "\".", e);
		}
	}

	public boolean addAll(Collection<IRI> iris) throws ConceptManagerException {
		Collection<IRI> newIris = new ArrayList<IRI>();
		try (Connection connection = dataSource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5);) {

				// TODO use batch for performance? But how to get new iris?
				for (IRI iri : iris) {
					if (sql.insertInto(table("semantic.concept"), field("concept_iri")).values(iri.getIRIString())
							.onDuplicateKeyIgnore().execute() > 0) {
						newIris.add(iri);
					}
				}
			}

			for (EventListener listener : listeners) {
				if (listener instanceof ConceptManagerListener) {
					((ConceptManagerListener) listener).newConcepts(newIris, connection);
				}
			}

			return !newIris.isEmpty();

		} catch (DataAccessException | SQLException | ConceptManagerListenerException e) {
			throw new ConceptManagerException("Failed to add IRIs \"" + iris + "\".", e);
		}
	}

	public Collection<IRI> getAll() throws ConceptManagerException {
		try (Connection connection = dataSource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5);) {
				return sql.select(field("concept_iri")).from("semantic.concept").fetch()
						.intoSet(field("concept_iri", String.class), new StringToIRIConverter());
			}
		} catch (DataAccessException | SQLException e) {
			throw new ConceptManagerException(e);
		}
	}

	public Collection<IRI> getAllUsed() throws ConceptManagerException {
		try (Connection connection = dataSource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5);) {
				return sql.select(field("concept_iri")).from("semantic.used_concept").fetch()
						.intoSet(field("concept_iri", String.class), new StringToIRIConverter());
			}
		} catch (DataAccessException | SQLException e) {
			throw new ConceptManagerException(e);
		}
	}
}
