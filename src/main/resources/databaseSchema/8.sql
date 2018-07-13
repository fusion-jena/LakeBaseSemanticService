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
/*
CREATE VIEW semantic.studysite AS (
SELECT 
	s.id AS id,
	s.name as name,
	s.type AS type,
	s.county AS location,
	s.document AS json
	FROM data.ta_site s
);
*/
CREATE VIEW semantic.studysite AS (SELECT 1)
