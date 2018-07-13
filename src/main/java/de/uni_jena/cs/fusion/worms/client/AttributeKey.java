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

public class AttributeKey {
	/**
	 * 	An internal identifier for the measurementType
	 */
	@JsonProperty("measurementTypeID")
	public int measurementTypeId;

	/**
	 * The nature of the measurement, fact, characteristic, or assertion http://www.marinespecies.org/traits/wiki
	 */
	public String measurementType;

	/**
	 * The data type that is expected as value for this attribute definition
	 */
	@JsonProperty("input_id")
	public int inputId;

	/**
	 * The category identifier to list possible attribute values for this attribute definition
	 */
	@JsonProperty("CategoryID")
	public int categoryId;

	/**
	 * The possible child attribute keys that help to describe to current attribute
	 */
	public Collection<AttributeKey> children;
}
