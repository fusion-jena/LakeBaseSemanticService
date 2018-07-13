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
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDataType;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.lakebase.model.SearchResponse;

public class Searcher {

	private final DataSource dataSource;

	private final RecordMapper<Record, SearchResponse> searchResultMapper = new SearchResultMapper();

	public Searcher(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Collection<String> getIRIStrings(Collection<IRI> iris) {
		Collection<String> strings = new ArrayList<String>();
		for (IRI iri : iris) {
			strings.add(iri.getIRIString());
		}
		return strings;
	}

	private Collection<Condition> includeEntityConditions(@Nullable Entity entity) {
		Collection<Condition> conditions = new ArrayList<Condition>();
		if (entity != null) {
			conditions.add(field("package_id").eq(entity.packageID));
			if (entity.objectID != null) {
				conditions.add(field("object_id").eq(entity.objectID));
			}
			if (entity.columnID != null) {
				conditions.add(field("column_id").eq(entity.columnID));
			}
			if (entity.rowID != null) {
				conditions.add(field("row_id").eq(entity.rowID));
			}
		}
		return conditions;
	}

	private Collection<Condition> excludeEntityConditions(@Nullable Entity entity) {
		Collection<Condition> conditions = new ArrayList<Condition>();
		conditions.add(field("include.package_id").eq(field("exclude.package_id")));
		if (entity != null) {
			conditions.add(field("include.object_id").eq(field("exclude.object_id")));
			if (entity.objectID != null) {
				conditions.add(field("include.column_id").eq(field("exclude.column_id")));
				conditions.add(field("include.row_id").eq(field("exclude.row_id")));
			}
		}
		return conditions;
	}

	private Collection<Field<? extends Object>> entityFields(@Nullable Entity entity) {
		Collection<Field<? extends Object>> fields = new ArrayList<Field<? extends Object>>();
		fields.add(field("package_id"));
		if (entity != null) {
			fields.add(field("object_id"));
			if (entity.objectID != null) {
				fields.add(field("column_id"));
				fields.add(field("row_id"));
			}
		}
		return fields;
	}

	public List<SearchResponse> search(Collection<IRI> include, Collection<IRI> exclude, @Nullable Entity entity)
			throws SearcherException {
		try (Connection connection = this.dataSource.getConnection()) {
			try (DSLContext create = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				// define entity conditions
				Collection<Condition> includeConditions = includeEntityConditions(entity);
				// define include conditions if available
				if (!include.isEmpty()) {
					includeConditions.add(field("searched_concept_iri").in(getIRIStrings(include)));
				}
				// define exclude condition if available
				if (!exclude.isEmpty()) {
					Collection<Condition> excludeConditions = excludeEntityConditions(entity);
					excludeConditions.add(field("exclude.searched_concept_iri").in(getIRIStrings(exclude)));
					includeConditions.add(notExists(
							create.selectOne().from(table("semantic.search").as("exclude")).where(excludeConditions)));
				}
				// define group fields
				Collection<Field<? extends Object>> groupFields = entityFields(entity);
				// define select fields
				Collection<Field<? extends Object>> selectFields = new ArrayList<Field<? extends Object>>(groupFields);

				selectFields.add(field("ranking").sum().cast(PostgresDataType.DOUBLEPRECISION).as("ranking"));

				return create.select(selectFields).from(table("semantic.search").as("include")).where(includeConditions)
						.groupBy(groupFields).fetch(searchResultMapper);
			}
		} catch (SQLException e) {
			throw new SearcherException("Failed to search data package due to a database error.", e);
		}
	}

	private class SearchResultMapper implements RecordMapper<Record, SearchResponse> {
		@Override
		public SearchResponse map(Record record) {
			Long packageID, objectID, columnID, rowID;
			try {
				packageID = record.get("package_id", Long.class);
			} catch (IllegalArgumentException e) {
				packageID = null;
			}
			try {
				objectID = record.get("object_id", Long.class);
			} catch (IllegalArgumentException e) {
				objectID = null;
			}
			try {
				columnID = record.get("column_id", Long.class);
			} catch (IllegalArgumentException e) {
				columnID = null;
			}
			try {
				rowID = record.get("row_id", Long.class);
			} catch (IllegalArgumentException e) {
				rowID = null;
			}
			SearchResponse searchResult = new SearchResponse(new Entity(packageID, objectID, columnID, rowID, null),
					record.get("ranking", Double.class));
			return searchResult;
		}
	}
}
