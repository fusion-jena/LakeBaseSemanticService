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

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attribute {
	/**
	 * Unique and persistent identifier within WoRMS. Primary key in the
	 * database.
	 */
	@JsonProperty("AphiaID")
	public int aphiaId;

	/**
	 * The corresponding AttributeKey its measurementTypeID
	 */
	@JsonProperty("measurementTypeID")
	public int measurementTypeId;

	/**
	 * The corresponding AttributeKey its measurementType
	 */
	public String measurementType;

	/**
	 * The value of the measurement, fact, characteristic, or assertion
	 */
	public String measurementValue;

	/**
	 * The identifier for the AphiaSource for this attribute
	 */
	@JsonProperty("source_id")
	public Integer sourceId;

	/**
	 * The AphiaSource reference for this attribute
	 */
	public String reference;

	/**
	 * Quality status of the record. Possible values: 'checked’, ‘trusted’ or
	 * 'unreviewed’. See http://www.marinespecies.org/aphia.php?p=manual#topic22
	 */
	@JsonProperty("qualitystatus")
	public String qualityStatus;

	/**
	 * The category identifier to list possible attribute values for this
	 * attribute definition
	 */
	@JsonProperty("CategoryID")
	public Integer categoryID;

	/**
	 * The AphiaID from where this attribute is inherited
	 */
	@JsonProperty("AphiaID_Inherited")
	public Integer aphiaIdInherited;

	/**
	 * The possible child attributes that help to describe to current attribute
	 */
	public Collection<Attribute> children;
}
