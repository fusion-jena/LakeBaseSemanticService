package de.uni_jena.cs.fusion.worms.client;

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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Classification {
	/**
	 * Unique and persistent identifier within WoRMS. Primary key in the
	 * database.
	 */
	@JsonProperty("AphiaID")
	public int aphiaId;
	/**
	 * the taxonomic rank of the most specific name in the scientific name
	 */
	@JsonProperty("rank")
	public String taxonomicRank;
	/**
	 * the full scientific name without authorship
	 */
	@JsonProperty("scientificname")
	public String scientificName;
	public Classification child;
}
