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
ALTER TABLE semantic.concept RENAME COLUMN concept_uri TO concept_iri;
COMMENT ON COLUMN semantic.concept.concept_iri IS 'IRI of the concept. The IRI is valid globally.';
ALTER TABLE semantic.adapter_cache_concept RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_broader RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_broader RENAME COLUMN broader_uri TO broader_iri;
ALTER TABLE semantic.adapter_cache_label RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_narrower RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_narrower RENAME COLUMN narrower_uri TO narrower_iri;
ALTER TABLE semantic.adapter_cache_synonym RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_synonym RENAME COLUMN synonym_uri TO synonym_iri;
ALTER TABLE semantic.adapter_cache_url RENAME COLUMN concept_uri TO concept_iri;
ALTER TABLE semantic.adapter_cache_match RENAME COLUMN concept_uri TO concept_iri;

