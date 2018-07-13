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
CREATE TABLE semantic.adapter_cache (
	adapter bigint,
	task smallint,
	key text,
	value bytea,
	CONSTRAINT adapter_cache_pk PRIMARY KEY (adapter,task,key)
);
COMMENT ON TABLE semantic.adapter_cache IS 'Contains cached results of Adapters wrapped into the DatabaseCacheAdapter';
COMMENT ON COLUMN semantic.adapter_cache.adapter IS 'The UID of the Adapter.';
COMMENT ON COLUMN semantic.adapter_cache.task IS 'ID of the called method.';
COMMENT ON COLUMN semantic.adapter_cache.key IS 'Parameter of the called method.';
COMMENT ON COLUMN semantic.adapter_cache.value IS 'Returned value of the called method.';
