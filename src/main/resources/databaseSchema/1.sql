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

COMMENT ON TABLE semantic.concept IS 'Provides a local ID for concepts from an adapter data sources.';

DROP TABLE semantic.concept_hierarchy;
CREATE TABLE semantic.concept_hierarchy (
	concept_id bigint NOT NULL,
	concept_hierarchy_id bigint NOT NULL,
	concept_hierarchy_bound bigint NOT NULL,
	CONSTRAINT concept_hierarchy_pk 
		PRIMARY KEY (concept_id, concept_hierarchy_id) DEFERRABLE,
	CONSTRAINT concept_hierarchy_fk1 
		FOREIGN KEY (concept_id) 
		REFERENCES semantic.concept (concept_id)
);

COMMENT ON TABLE semantic.concept_hierarchy IS 'Contains a hierarchical index of the concepts of the data sources for fast search of all narrower concepts (N) of a given concept (G). N is a narrower concept of G if G.concept_hierarchy_id < N.concept_hierarchy_id <= G.concept_hierarchy_bound.';
COMMENT ON COLUMN semantic.concept_hierarchy.concept_id IS 'ID of the concept. Note: Not unique in this table beause a concept may be a narrower concepts of several other concepts and therefore occure several times in the hierarchy.';
COMMENT ON COLUMN semantic.concept_hierarchy.concept_hierarchy_id IS 'ID of the represented concept hierarchy node. Note: Not unique, because it might occure several times - once for eache synonym concept represented by the hierarchy node.';
COMMENT ON COLUMN semantic.concept_hierarchy.concept_hierarchy_bound IS 'Maximum concept_hierarchy_id of the narrower concepts of this concept.';

CREATE VIEW semantic.concept_narrower AS
	SELECT broader.concept_id AS broader_concept_id, narrower.concept_id AS narrower_concept_id
		FROM semantic.concept_hierarchy broader
		INNER JOIN semantic.concept_hierarchy narrower
			ON narrower.concept_hierarchy_id BETWEEN broader.concept_hierarchy_id AND broader.concept_hierarchy_bound;
			
CREATE VIEW semantic.concept_equal AS
	SELECT concept.concept_id AS concept_id, equal.concept_id AS equal_concept_id
		FROM semantic.concept_hierarchy concept
		INNER JOIN semantic.concept_hierarchy equal
			ON concept.concept_hierarchy_id = equal.concept_hierarchy_id;
