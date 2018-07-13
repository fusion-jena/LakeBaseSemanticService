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

public class Source {
	/**
	 * Unique identifier for the source within WoRMS.
	 */
	@JsonProperty("source_id")
	public int sourceId;
	/**
	 * Usage of the source for this taxon. See
	 * http://www.marinespecies.org/aphia.php?p=manual#topic6 for all values
	 */
	public String use;
	/**
	 * Full citation string
	 */
	public String reference;
	/**
	 * Page(s) where the taxon is mentioned
	 */
	public String page;
	/**
	 * Direct link to the source record
	 */
	public String url;
	/**
	 * External link (i.e. journal, data system, etc…)
	 */
	public String link;
	/**
	 * Full text link (PDF)
	 */
	public String fulltext;
	/**
	 * Digital Object Identifier
	 */
	public String doi;
}
