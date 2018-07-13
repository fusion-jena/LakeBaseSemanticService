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
-------------------------------------
-- search index
-------------------------------------

CREATE TABLE semantic.concept (
	concept_id bigserial NOT NULL,
	concept_uri varchar NOT NULL,
	CONSTRAINT concept_pk PRIMARY KEY (concept_id),
	CONSTRAINT concept_u1 UNIQUE (concept_uri)
);
COMMENT ON TABLE semantic.concept IS 'Provides a local ID for concepts from ontologies.';
COMMENT ON COLUMN semantic.concept.concept_id IS 'ID of the concept. The ID is valid in this system instance only.';
COMMENT ON COLUMN semantic.concept.concept_uri IS 'URI of the concept. The URI is valid globally.';

CREATE TABLE semantic.annotation (
	datapackage_id bigint NOT NULL,
	serie_id bigint,
	data_column_id bigint,
	data_row_id bigint,
	meta_data_id bigint,
	concept_id bigint NOT NULL,
	CONSTRAINT annotation_fk1 
		FOREIGN KEY (concept_id) 
		REFERENCES semantic.concept (concept_id) 
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);
COMMENT ON TABLE semantic.annotation IS 'Contains assignments of concepts to datapackages, series, columns, cells or meta data propertie. Not relevant ID attributes for an object type have to be NULL.';
COMMENT ON COLUMN semantic.annotation.datapackage_id IS 'ID of the annotated data package';
COMMENT ON COLUMN semantic.annotation.serie_id IS 'ID of the annotated serie.';
COMMENT ON COLUMN semantic.annotation.data_column_id IS 'ID of the annotated column.';
COMMENT ON COLUMN semantic.annotation.data_row_id IS 'ID of the annotated row.';
COMMENT ON COLUMN semantic.annotation.meta_data_id IS 'ID of the annotated meta data propertie.';
COMMENT ON COLUMN semantic.annotation.concept_id IS 'ID of the concept';

CREATE TABLE semantic.concept_hierarchy (
	concept_id bigint NOT NULL,
	concept_hierarchy_id bigint NOT NULL,
	concept_hierarchy_bound bigint NOT NULL,
	CONSTRAINT concept_hierarchy_pk 
		PRIMARY KEY (concept_id, concept_hierarchy_id),
	CONSTRAINT concept_hierarchy_fk1 
		FOREIGN KEY (concept_id) 
		REFERENCES semantic.concept (concept_id) 
		ON UPDATE CASCADE 
		ON DELETE CASCADE
);
COMMENT ON COLUMN semantic.concept_hierarchy.concept_id IS 'TODO: not unique beause can occure several times in hierarchy';
COMMENT ON COLUMN semantic.concept_hierarchy.concept_hierarchy_id IS 'TODO: not unique beause of synonyms';

-------------------------------------
-- adapter cache
-------------------------------------

CREATE TABLE semantic.adapter_cache_concept (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	CONSTRAINT adapter_cache_concept_pk 
		PRIMARY KEY (source_id, concept_uri)
);

CREATE TABLE semantic.adapter_cache_broader (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	broader_uri varchar NOT NULL,
	CONSTRAINT adapter_cache_broader_pk 
		PRIMARY KEY (source_id, concept_uri, broader_uri)
);

CREATE TABLE semantic.adapter_cache_label (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	concept_label varchar NOT NULL,
	CONSTRAINT adapter_cache_label_pk 
		PRIMARY KEY (source_id, concept_uri, concept_label)
);

CREATE TABLE semantic.adapter_cache_narrower (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	narrower_uri varchar NOT NULL,
	CONSTRAINT adapter_cache_narrower_pk 
		PRIMARY KEY (source_id, concept_uri, narrower_uri)
);

CREATE TABLE semantic.adapter_cache_synonym (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	synonym_uri varchar NOT NULL,
	CONSTRAINT adapter_cache_synonym_pk 
		PRIMARY KEY (source_id, concept_uri, synonym_uri)
);

CREATE TABLE semantic.adapter_cache_url (
	source_id varchar NOT NULL,
	concept_uri varchar NOT NULL,
	concept_url varchar NOT NULL,
	CONSTRAINT adapter_cache_url_pk 
		PRIMARY KEY (source_id, concept_uri, concept_url)
);

CREATE TABLE semantic.adapter_cache_match (
	source_id varchar NOT NULL,
	term varchar NOT NULL,
	concept_uri varchar,
	match_rank double precision,
	CONSTRAINT adapter_cache_match_pk 
		PRIMARY KEY (source_id, term, concept_uri)
);
