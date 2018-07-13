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
import static org.jooq.impl.DSL.val;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectField;
import org.jooq.impl.DSL;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.lakebase.util.jooq.StringToIRIMapper;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * @since 0.1
 *
 */
public class AnnotationManager implements Maintainable {

	private static final Logger log = LoggerFactory.getLogger(AnnotationManager.class);
	private final DataSource dataSource;
	private final ConceptManager conceptManager;
	private final SemanticDataSource semanticDataSource;

	public AnnotationManager(DataSource dataSource, SemanticDataSource semanticDataSource,
			ConceptManager conceptManager) {
		this.dataSource = dataSource;
		this.conceptManager = conceptManager;
		this.semanticDataSource = semanticDataSource;
	}

	private Collection<Condition> entityConditions(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID, boolean deep) {
		Collection<Condition> conditions = new ArrayList<Condition>();

		conditions.add(field("package_id").eq(packageId));

		if (objectId != null) {
			conditions.add(field("object_id").eq(objectId));
		} else if (!deep) {
			conditions.add(field("object_id").isNull());
		}

		if (columnId != null) {
			if (objectId != null) {
				conditions.add(field("column_id").eq(columnId));
			} else {
				throw new IllegalArgumentException(
						"Failed to determine affected annotations: objectId must not be null if columnId is not null.");
			}
		} else if (!deep || rowId != null) {
			conditions.add(field("column_id").isNull());
		}

		if (rowId != null) {
			if (objectId != null) {
				conditions.add(field("row_id").eq(rowId));
			} else {
				throw new IllegalArgumentException(
						"Failed to determine affected annotations: objectId must not be null if rowId is not null.");
			}
		} else if (!deep || columnId != null) {
			conditions.add(field("row_id").isNull());
		}

		if (metaID != null) {
			conditions.add(field("meta_id").eq(metaID));
		} else if (!deep) {
			conditions.add(field("meta_id").isNull());
		}

		return conditions;
	}

	/**
	 * Returns a {@link Map} of terms and their accepted annotations of the
	 * specified field.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param deep
	 *            determines if annotations of specified field (FALSE) or the
	 *            specified field an its subordinate fields should be returned
	 * @return a {@link Map} of terms and their annotations
	 * @throws AnnotationManagerException
	 */
	private Map<String, IRI> getAcceptedAnnotations(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID, boolean deep) throws AnnotationManagerException {

		Collection<Condition> conditions = entityConditions(packageId, objectId, columnId, rowId, metaID, deep);

		try (Connection connection = this.dataSource.getConnection()) {
			try (DSLContext create = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {

				return create.selectDistinct(field("annotation.term"), field("concept.concept_iri"))
						.from(table("semantic.annotation")).join(table("semantic.concept"))
						.on(field("concept.concept_id").equal(field("annotation.concept_id"))).where(conditions).fetch()
						.intoMap(field("annotation.term", String.class), new StringToIRIMapper("concept.concept_iri"));

			}
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	/**
	 * Returns a {@link Map} of terms and their accepted annotations of the
	 * specified field.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @return a {@link Map} of terms and their annotations
	 * @throws AnnotationManagerException
	 */
	public Map<String, IRI> getAcceptedAnnotations(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID) throws AnnotationManagerException {
		return getAcceptedAnnotations(packageId, objectId, columnId, rowId, metaID, false);
	}

	/**
	 * Returns a {@link Map} of terms and their accepted annotations of the
	 * specified field and its subordinated fields.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @throws AnnotationManagerException
	 */
	public Map<String, IRI> getAcceptedAnnotationsWithin(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID) throws AnnotationManagerException {
		return getAcceptedAnnotations(packageId, objectId, columnId, rowId, metaID, true);
	}

	/**
	 * Returns a {@link Map} of terms and their rejected annotations of the
	 * specified field.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @return a {@link Map} of terms and their annotations
	 * @throws AnnotationManagerException
	 */
	public Map<String, List<IRI>> getRejectedAnnotations(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID) throws AnnotationManagerException {
		return getRejectedAnnotations(packageId, objectId, columnId, rowId, metaID, false);
	}

	/**
	 * Returns a {@link Map} of terms and their rejected annotations of the
	 * specified field.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param deep
	 *            determines if annotations of specified field (FALSE) or the
	 *            specified field an its subordinate fields should be returned
	 * @return a {@link Map} of terms and their annotations
	 * @throws AnnotationManagerException
	 */
	private Map<String, List<IRI>> getRejectedAnnotations(long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID, boolean deep) throws AnnotationManagerException {

		Collection<Condition> conditions = entityConditions(packageId, objectId, columnId, rowId, metaID, deep);

		try (Connection connection = this.dataSource.getConnection()) {
			try (DSLContext create = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				return create.selectDistinct(field("rejected_annotation.term"), field("concept.concept_iri"))
						.from(table("semantic.rejected_annotation")).join(table("semantic.concept"))
						.on(field("concept.concept_id").equal(field("rejected_annotation.concept_id")))
						.where(conditions).fetchGroups(field("rejected_annotation.term", String.class),
								new StringToIRIMapper("concept.concept_iri"));
			}
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	/**
	 * Returns a {@link Map} of terms and their rejected annotations of the
	 * specified field and its subordinated fields.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @return a {@link Map} of terms and their annotations
	 * @throws AnnotationManagerException
	 */
	public Map<String, ? extends Collection<IRI>> getRejectedAnnotationsWithin(long packageId, Long objectId,
			Long columnId, Long rowId, Long metaID) throws AnnotationManagerException {
		return getRejectedAnnotations(packageId, objectId, columnId, rowId, metaID, true);
	}

	/**
	 * Removes the accepted and rejected concept associations of all terms.
	 * 
	 * @param context
	 *            {@link DSLContext} to use
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param term
	 *            the annotated term
	 * @param deep
	 *            determines if annotations of specified field (FALSE) or the
	 *            specified field an its subordinate fields should be removed
	 * @throws AnnotationManagerException
	 */
	private void removeAnnotations(DSLContext context, long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID, boolean deep) throws AnnotationManagerException {

		Collection<Condition> conditions = entityConditions(packageId, objectId, columnId, rowId, metaID, deep);

		context.deleteFrom(table("semantic.annotation")).where(conditions).execute();
		context.deleteFrom(table("semantic.rejected_annotation")).where(conditions).execute();
	}

	/**
	 * Removes the concept associations of all terms of the specified field.
	 * 
	 * @param connection
	 *            {@link Connection} to use
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param term
	 *            the annotated term
	 * @throws AnnotationManagerException
	 */
	public void removeAnnotations(Connection connection, long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID) throws AnnotationManagerException {
		try (DSLContext context = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			removeAnnotations(context, packageId, objectId, columnId, rowId, metaID, false);
		}
	}

	/**
	 * Removes the concept associations of all terms of the specified field.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param term
	 *            the annotated term
	 * @throws AnnotationManagerException
	 */
	public void removeAnnotations(long packageId, Long objectId, Long columnId, Long rowId, Long metaID)
			throws AnnotationManagerException {
		try (Connection connection = this.dataSource.getConnection()) {
			removeAnnotations(connection, packageId, objectId, columnId, rowId, metaID);
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	/**
	 * Removes the concept associations of all terms of the specified field and
	 * its subordinated fields..
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaID
	 *            ID of the meta data property
	 * @param term
	 *            the annotated term
	 * @throws AnnotationManagerException
	 */
	public void removeAnnotationsWithin(long packageId, Long objectId, Long columnId, Long rowId, Long metaID)
			throws AnnotationManagerException {
		try (Connection connection = this.dataSource.getConnection()) {
			try (DSLContext context = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				removeAnnotations(context, packageId, objectId, columnId, rowId, metaID, true);
			}
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	/**
	 * Associates a concept with a term.
	 * 
	 * @param connection
	 *            {@link Connection} to use
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the column
	 * @param rowId
	 *            ID of the row
	 * @param metaId
	 *            ID of the meta data property
	 * @param term
	 *            the annotated term
	 * @param iri
	 *            the annotated concept
	 * @param rejected
	 *            determines if a rejected (TRUE) or accepted (FALSE) annotation
	 *            should be added
	 * @throws AnnotationManagerException
	 */
	private void setAnnotation(Connection connection, long packageId, Long objectId, Long columnId, Long rowId,
			Long metaId, String term, IRI iri, boolean rejected) throws AnnotationManagerException {

		try (DSLContext context = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
			Collection<Field<Object>> insertFields = new LinkedList<Field<Object>>();
			Collection<SelectField<Object>> selectFields = new LinkedList<SelectField<Object>>();

			insertFields.add(field("package_id"));
			selectFields.add(val((Object) packageId));

			if (objectId != null) {
				insertFields.add(field("object_id"));
				selectFields.add(val((Object) objectId));
			}
			if (columnId != null) {
				insertFields.add(field("column_id"));
				selectFields.add(val((Object) columnId));
			}
			if (rowId != null) {
				insertFields.add(field("row_id"));
				selectFields.add(val((Object) rowId));
			}
			if (metaId != null) {
				insertFields.add(field("meta_id"));
				selectFields.add(val((Object) metaId));
			}
			insertFields.add(field("concept_id"));
			selectFields.add(field("concept.concept_id"));
			insertFields.add(field("term"));
			selectFields.add(val((Object) term));

			this.conceptManager.add(connection, iri);

			if (rejected) {
				context.insertInto(table("semantic.rejected_annotation")).columns(insertFields)
						.select(select(selectFields).from(table("semantic.concept"))
								.where(field("concept.concept_iri").eq(iri.getIRIString())))
						.execute();
			} else {
				context.insertInto(table("semantic.annotation")).columns(insertFields).select(select(selectFields)
						.from(table("semantic.concept")).where(field("concept.concept_iri").eq(iri.getIRIString())))
						.execute();
			}
		} catch (ConceptManagerException e) {
			throw new AnnotationManagerException(e);
		}

	}

	private void copyAnnotation(DSLContext context, Entity source, Entity target) {
		Collection<Condition> conditions = entityConditions(source.packageID, source.objectID, source.columnID,
				source.rowID, source.metaID, true);
		Collection<Field<Object>> into = Lists.newArrayList(field("package_id"), field("object_id"), field("column_id"),
				field("row_id"), field("meta_id"), field("concept_id"), field("term"));
		Collection<Field<Object>> select = new LinkedList<Field<Object>>();

		select.add(val((Object) target.packageID));
		if (target.objectID != null) {
			select.add(val((Object) target.objectID));
		} else {
			select.add(field("object_id"));
		}
		if (target.columnID != null) {
			select.add(val((Object) target.columnID));
		} else {
			select.add(field("column_id"));
		}
		if (target.rowID != null) {
			select.add(val((Object) target.rowID));
		} else {
			select.add(field("row_id"));
		}
		if (target.metaID != null) {
			select.add(val((Object) target.metaID));
		} else {
			select.add(field("meta_id"));
		}
		select.add(field("concept_id"));
		select.add(field("term"));

		context.insertInto(table("semantic.annotation")).columns(into)
				.select(select(select).from(table("semantic.annotation")).where(conditions)).execute();
		context.insertInto(table("semantic.rejected_annotation")).columns(into)
				.select(select(select).from(table("semantic.rejected_annotation")).where(conditions)).execute();
	}

	public void copyAnnotation(Entity source, Entity target) throws AnnotationManagerException {
		try (Connection connection = this.dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try (DSLContext context = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				copyAnnotation(context, source, target);
			}
			connection.commit();
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	/**
	 * Associates concepts with terms.
	 * 
	 * @param connection
	 *            {@link Connection} to use
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the row
	 * @param rowId
	 *            ID of the column
	 * @param metaID
	 *            ID of the meta data property
	 * @param annotations
	 *            a {@link Map} of terms and their annotations
	 * @param rejections
	 *            a {@link Map} of terms and their rejected annotations
	 * @throws AnnotationManagerException
	 */
	public void setAnnotations(Connection connection, long packageId, Long objectId, Long columnId, Long rowId,
			Long metaID, Map<String, IRI> annotations, Map<String, Collection<IRI>> rejections)
			throws AnnotationManagerException {
		// remove previous annotations and rejections
		removeAnnotations(connection, packageId, objectId, columnId, rowId, metaID);

		for (String term : annotations.keySet()) {
			setAnnotation(connection, packageId, objectId, columnId, rowId, metaID, term, annotations.get(term), false);
		}
		for (String term : rejections.keySet()) {
			for (IRI iri : rejections.get(term)) {
				setAnnotation(connection, packageId, objectId, columnId, rowId, metaID, term, iri, true);
			}
		}
	}

	/**
	 * Associates concepts with terms.
	 * 
	 * @param packageId
	 *            ID of the package
	 * @param objectId
	 *            ID of the object
	 * @param columnId
	 *            ID of the row
	 * @param rowId
	 *            ID of the column
	 * @param metaID
	 *            ID of the meta data property
	 * @param annotations
	 *            a {@link Map} of terms and their annotations
	 * @param rejections
	 *            a {@link Map} of terms and their rejected annotations
	 * @throws AnnotationManagerException
	 */
	public void setAnnotations(long packageId, Long objectId, Long columnId, Long rowId, Long metaID,
			Map<String, IRI> annotations, Map<String, Collection<IRI>> rejections) throws AnnotationManagerException {
		try (Connection connection = this.dataSource.getConnection()) {
			connection.setAutoCommit(false);
			setAnnotations(connection, packageId, objectId, columnId, rowId, metaID, annotations, rejections);
			connection.commit();
		} catch (SQLException e) {
			throw new AnnotationManagerException(e);
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		if (semanticDataSource.providingDeprecation()) {
			try (Connection connection = this.dataSource.getConnection()) {
				try (DSLContext context = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
					// get IRIs used for annotation
					Iterator<Record1<Object>> iriIterator = context.select(field("concept.concept_iri"))
							.from(table("semantic.annotation")).naturalJoin(table("semantic.concept"))
							.union(context.select(field("concept.concept_iri"))
									.from(table("semantic.rejected_annotation")).naturalJoin(table("semantic.concept")))
							.iterator();
					while (iriIterator.hasNext()) {
						IRI oldIri = IRI.create(iriIterator.next().get(0).toString());
						if (semanticDataSource.isDeprecated(oldIri)) {
							Collection<IRI> newIris = semanticDataSource.getReplacedBy(oldIri);
							if (!newIris.isEmpty()) {
								this.replaceAnnotation(context, oldIri, newIris.iterator().next());
							}
						}
					}
				}
			} catch (SQLException | SemanticDataSourceException | AnnotationManagerException e) {
				throw new MaintenanceException(e);
			}
		}
	}

	private void replaceAnnotation(DSLContext sql, IRI oldIri, IRI newIri) throws AnnotationManagerException {
		try {
			this.conceptManager.add(newIri);
			log.info(
					"Replacing deprecated IRI \"" + oldIri.getIRIString() + "\" by \"" + newIri.getIRIString() + "\".");

			// replace annotations
			// Select<Record1<Object>> i = ;

			sql.update(table("semantic.annotation"))
					.set(field("concept_id", Long.class),
							sql.select(field("concept_id", Long.class)).from(table("semantic.concept"))
									.where(field("concept_iri").eq(newIri.getIRIString())))
					.where(field("concept_id").eq(sql.select(field("concept_id")).from(table("semantic.concept"))
							.where(field("concept_iri").eq(oldIri.getIRIString()))))
					.execute();
			// replace rejected annotations
			sql.update(table("semantic.rejected_annotation"))
					.set(field("concept_id", Long.class),
							sql.select(field("concept_id", Long.class)).from(table("semantic.concept"))
									.where(field("concept_iri").eq(newIri.getIRIString())))
					.where(field("concept_id").eq(sql.select(field("concept_id")).from(table("semantic.concept"))
							.where(field("concept_iri").eq(oldIri.getIRIString()))))
					.execute();
		} catch (ConceptManagerException e) {
			throw new AnnotationManagerException("Failed to replace deprecated IRI \"" + oldIri.getIRIString() + "\".",
					e);
		}

	}
}
