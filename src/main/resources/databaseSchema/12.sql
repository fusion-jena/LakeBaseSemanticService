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
DROP VIEW semantic.concept_narrower;
DROP VIEW semantic.concept_equal;
DROP VIEW semantic.search;
DROP TABLE semantic.concept_hierarchy;

CREATE TABLE semantic.concept_hierarchy (
	broader_concept_id bigint NOT NULL,
	narrower_concept_id bigint NOT NULL,
	CONSTRAINT concept_hierarchy_pk 
		PRIMARY KEY (broader_concept_id, narrower_concept_id),
	CONSTRAINT concept_hierarchy_fk_broader
		FOREIGN KEY (broader_concept_id) 
		REFERENCES semantic.concept (concept_id),
	CONSTRAINT concept_hierarchy_fk_narrower
		FOREIGN KEY (narrower_concept_id) 
		REFERENCES semantic.concept (concept_id),
	 CONSTRAINT concept_hierarchy_different
	 	CHECK (broader_concept_id != narrower_concept_id)
);

COMMENT ON TABLE semantic.concept_hierarchy IS 'Contains the hierarchy of the concepts. For each narrower/broader relation, including transitiv relations, between concept there is one entry. For synonym concepts there is one entry for each direction.';
COMMENT ON COLUMN semantic.concept_hierarchy.broader_concept_id IS 'ID of the broader concept.';
COMMENT ON COLUMN semantic.concept_hierarchy.narrower_concept_id IS 'ID of the narrower concept.';

CREATE VIEW semantic.concept_iri_hierarchy AS
	SELECT b.concept_iri AS broader_concept_iri,
		n.concept_iri AS narrower_concept_iri,
		broader_concept_id,
		narrower_concept_id
		FROM semantic.concept_hierarchy h
			INNER JOIN semantic.concept b
				ON b.concept_id = h.broader_concept_id
			INNER JOIN semantic.concept n
				ON n.concept_id = h.narrower_concept_id
;

COMMENT ON VIEW semantic.concept_iri_hierarchy IS 'This is an simplifying view of semantic.concept_hierarchy. Contains the hierarchy of the concepts. For each narrower/broader relation, including transitiv relations, between concept there is one entry. For synonym concepts there is one entry for each direction.';
COMMENT ON COLUMN semantic.concept_iri_hierarchy.broader_concept_iri IS 'IRI of the broader concept.';
COMMENT ON COLUMN semantic.concept_iri_hierarchy.narrower_concept_iri IS 'IRI of the narrower concept.';
COMMENT ON COLUMN semantic.concept_iri_hierarchy.broader_concept_id IS 'ID of the broader concept.';
COMMENT ON COLUMN semantic.concept_iri_hierarchy.narrower_concept_id IS 'ID of the narrower concept.';

CREATE VIEW semantic.concept_equal AS
	SELECT a.broader_concept_id AS concept_id,
		a.narrower_concept_id AS equal_concept_id
		FROM semantic.concept_hierarchy a
			INNER JOIN semantic.concept_hierarchy b
				ON a.broader_concept_id = b.narrower_concept_id
				AND a.narrower_concept_id = b.broader_concept_id
;

COMMENT ON VIEW semantic.concept_equal IS 'Contains synonym concepts. For each synonym relation between concepts, there are two entrys: One for each direction.';
COMMENT ON COLUMN semantic.concept_equal.concept_id IS 'ID of the concept.';
COMMENT ON COLUMN semantic.concept_equal.equal_concept_id IS 'ID of the synonym concept.';

CREATE VIEW semantic.search AS
	SELECT	h.broader_concept_iri AS searched_concept_iri,
			a.package_id AS package_id,
			a.object_id AS object_id,
			a.column_id AS column_id,
			a.row_id AS row_id,
			a.meta_id AS meta_id,
			(SELECT 1.0 + COUNT(*) FROM semantic.concept_hierarchy WHERE broader_concept_id = h.narrower_concept_id)
			/
			(SELECT 1.0 + COUNT(*) FROM semantic.concept_hierarchy WHERE broader_concept_id = h.broader_concept_id)
			AS ranking,
			h.narrower_concept_iri AS annotated_concept_iri
		FROM semantic.concept_iri_hierarchy h
			INNER JOIN semantic.annotation a
				ON h.narrower_concept_id = a.concept_id
	UNION
	SELECT c.concept_iri AS searched_concept_iri,
			a.package_id AS package_id,
			a.object_id AS object_id,
			a.column_id AS column_id,
			a.row_id AS row_id,
			a.meta_id AS meta_id,
			1.0 AS ranking,
			c.concept_iri AS annotated_concept_iri
	FROM semantic.concept c
		INNER JOIN semantic.annotation a
			ON a.concept_id = c.concept_id
;

COMMENT ON VIEW semantic.search IS 'Contains rankings of data entities per concept, based on the concept hierarchy and the annoations.';
COMMENT ON COLUMN semantic.search.searched_concept_iri IS 'IRI of the searched concept.';
COMMENT ON COLUMN semantic.search.package_id IS 'PackageID of the entity.';
COMMENT ON COLUMN semantic.search.object_id IS 'ObjectID of the entity.';
COMMENT ON COLUMN semantic.search.row_id IS 'RowID of the entity.';
COMMENT ON COLUMN semantic.search.meta_id IS 'MetaID of the entity.';
COMMENT ON COLUMN semantic.search.ranking IS 'Ranking of the entity for this concept.';
COMMENT ON COLUMN semantic.search.annotated_concept_iri IS 'IRI of the annotated concept.';
