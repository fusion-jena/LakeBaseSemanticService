---
-- #%L
-- LakeBase Semantic Service
-- %%
-- Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
ALTER TABLE semantic.annotation RENAME COLUMN datapackage_id TO package_id;
ALTER TABLE semantic.annotation RENAME COLUMN serie_id TO object_id;
ALTER TABLE semantic.annotation RENAME COLUMN data_column_id TO column_id;
ALTER TABLE semantic.annotation RENAME COLUMN data_row_id TO row_id;
ALTER TABLE semantic.annotation RENAME COLUMN meta_data_id TO meta_id;

COMMENT ON TABLE semantic.annotation IS 'Contains assignments of concepts and the annotated text to packages, objects, columns, rows or meta data propertie. Not relevant ID attributes for an object type have to be NULL.';
COMMENT ON COLUMN semantic.annotation.package_id IS 'ID of the annotated package';
COMMENT ON COLUMN semantic.annotation.object_id IS 'ID of the annotated package.';
COMMENT ON COLUMN semantic.annotation.column_id IS 'ID of the annotated column.';
COMMENT ON COLUMN semantic.annotation.row_id IS 'ID of the annotated row.';

CREATE VIEW semantic.search AS
SELECT	c1.concept_iri AS concept,
		a.package_id AS package_id,
		a.object_id AS object_id,
		a.column_id AS column_id,
		a.row_id AS row_id,
		a.meta_id AS meta_id,
		(1.0 + MAX(ch2.concept_hierarchy_bound) - MAX(ch2.concept_hierarchy_id) ) / (1 + MAX(ch1.concept_hierarchy_bound) - MAX(ch1.concept_hierarchy_id)) AS ranking
	FROM semantic.concept c1
		INNER JOIN semantic.concept_hierarchy ch1
			ON c1.concept_id = ch1.concept_id
		INNER JOIN semantic.concept_hierarchy ch2
			ON ch2.concept_hierarchy_id BETWEEN ch1.concept_hierarchy_id AND ch1.concept_hierarchy_bound
		INNER JOIN semantic.annotation a
			ON a.concept_id = ch2.concept_id
		INNER JOIN semantic.concept c2
			ON c2.concept_id = ch2.concept_id
	GROUP BY c1.concept_iri,
			a.package_id,
			a.object_id,
			a.column_id,
			a.row_id ,
			a.meta_id,
			ch2.concept_id;

CREATE UNIQUE INDEX name ON semantic.annotation (package_id, object_id, column_id, row_id, meta_id, term);

