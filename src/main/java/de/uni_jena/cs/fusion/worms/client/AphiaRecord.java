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

public class AphiaRecord {

	/**
	 * Unique and persistent identifier within WoRMS. Primary key in the
	 * database.
	 */
	@JsonProperty("AphiaID")
	public long aphiaId;

	/**
	 * HTTP URL to the AphiaRecord
	 */
	public String url;

	/**
	 * the full scientific name without authorship
	 */
	@JsonProperty("scientificname")
	public String scientificName;

	/**
	 * the authorship information for the scientific name formatted according to
	 * the conventions of the applicable nomenclaturalCode
	 */
	public String authority;

	/**
	 * the taxonomic rank of the most specific name in the scientific name
	 */
	@JsonProperty("rank")
	public String taxonomicRank;

	/**
	 * the status of the use of the scientific name (usually a taxonomic
	 * opinion). Additional technical statuses are (1) quarantined: hidden from
	 * public interface after decision from an editor and (2) deleted: AphiaID
	 * should NOT be used anymore, please replace it by the valid_AphiaID
	 */
	public String status;

	/**
	 * the reason why a scientific name is unaccepted
	 */
	@JsonProperty("unacceptreason")
	public String unacceptReason;

	/**
	 * the AphiaID (for the scientific name) of the currently accepted taxon.
	 * NULL if there is no currently accepted taxon.
	 */
	@JsonProperty("valid_AphiaID")
	public long validAphiaId;

	/**
	 * the scientific name of the currently accepted taxon
	 */
	@JsonProperty("valid_name")
	public String validName;

	/**
	 * the authorship information for the scientific name of the currently
	 * accepted taxon
	 */
	@JsonProperty("valid_authority")
	public String validAuthority;

	/**
	 * the full scientific name of the kingdom in which the taxon is classified
	 */
	@JsonProperty("kingdom")
	public String taxonomicKingdom;

	/**
	 * the full scientific name of the phylum or division in which the taxon is
	 * classified
	 */
	@JsonProperty("phylum")
	public String taxonomicPhylum;

	/**
	 * the full scientific name of the class in which the taxon is classified
	 */
	@JsonProperty("class")
	public String taxonomicClass;

	/**
	 * the full scientific name of the order in which the taxon is classified
	 */
	@JsonProperty("order")
	public String taxonomicOrder;

	/**
	 * the full scientific name of the family in which the taxon is classified
	 */
	@JsonProperty("family")
	public String taxonomicFamily;

	/**
	 * the full scientific name of the genus in which the taxon is classified
	 */
	@JsonProperty("genus")
	public String taxonomicGenus;

	/**
	 * a bibliographic reference for the resource as a statement indicating how
	 * this record should be cited (attributed) when used
	 */
	public String citation;

	/**
	 * LifeScience Identifier. Persistent GUID for an AphiaID
	 */
	public String lsid;

	/**
	 * a boolean flag indicating whether the taxon is a marine organism, i.e.
	 * can be found in/above sea water. Possible values: 0/1/NULL
	 */
	public Boolean isMarine;

	/**
	 * a boolean flag indicating whether the taxon occurrs in brackish habitats.
	 * Possible values: 0/1/NULL
	 */
	public Boolean isBrackish;

	/**
	 * a boolean flag indicating whether the taxon occurrs in freshwater
	 * habitats, i.e. can be found in/above rivers or lakes. Possible values:
	 * 0/1/NULL
	 */
	public Boolean isFreshwater;

	/**
	 * a boolean flag indicating the taxon is a terrestial organism, i.e.
	 * occurrs on land as opposed to the sea. Possible values: 0/1/NULL
	 */
	public Boolean isTerrestrial;

	/**
	 * a flag indicating an extinct organism. Possible values: 0/1/NULL
	 */
	public Boolean isExtinct;

	/**
	 * Type of match.
	 */
	@JsonProperty("match_type")
	public MatchType matchType;

	/**
	 * The most recent date-time in GMT on which the resource was changed
	 */
	public String modified;

}
