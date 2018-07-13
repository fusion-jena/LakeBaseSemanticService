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
ALTER INDEX semantic.name RENAME TO annotation_unique;

CREATE TABLE semantic.rejected_annotation (
	package_id bigint NOT NULL,
	object_id bigint,
	column_id bigint,
	row_id bigint,
	meta_id bigint,
	term character varying,
	concept_id bigint NOT NULL,
	CONSTRAINT rejection_fk1 
		FOREIGN KEY (concept_id) 
		REFERENCES semantic.concept (concept_id) 
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);
COMMENT ON TABLE semantic.rejected_annotation IS 'Contains rejected assignments of concepts and the annotated text to packages, objects, columns, rows or meta data propertie. Not relevant ID attributes for an object type have to be NULL.';
COMMENT ON COLUMN semantic.rejected_annotation.package_id IS 'ID of the annotated package';
COMMENT ON COLUMN semantic.rejected_annotation.object_id IS 'ID of the annotated package.';
COMMENT ON COLUMN semantic.rejected_annotation.column_id IS 'ID of the annotated column.';
COMMENT ON COLUMN semantic.rejected_annotation.row_id IS 'ID of the annotated row.';
COMMENT ON COLUMN semantic.rejected_annotation.meta_id IS 'ID of the annotated meta data propertie.';
COMMENT ON COLUMN semantic.rejected_annotation.concept_id IS 'ID of the concept';
COMMENT ON COLUMN semantic.rejected_annotation.term IS 'Term that has been annotated.';
COMMENT ON COLUMN semantic.rejected_annotation.concept_id IS 'ID of the concept.';

CREATE UNIQUE INDEX rejection_unique ON semantic.rejected_annotation (package_id, object_id, column_id, row_id, meta_id, term, concept_id);
