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

public class Distribution {

	/**
	 * The specific description of the place
	 */
	public String locality;

	/**
	 * An identifier for the locality. Using the Marine Regions Geographic
	 * IDentifier (MRGID), see http://www.marineregions.org/mrgid.php
	 */
	public String locationID;

	/**
	 * A geographic name less specific than the information captured in the
	 * locality term. Possible values: an IHO Sea Area or Nation, derived from
	 * the MarineRegions gazetteer
	 */
	public String higherGeography;

	/**
	 * An identifier for the geographic region within which the locality
	 * occurred, using MRGID
	 */
	public String higherGeographyID;

	/**
	 * The status of the distribution record. Possible values are ‘valid’
	 * ,’doubtful’ or 'inaccurate’. See here for explanation of the statuses
	 */
	public String recordStatus;

	/**
	 * The type status of the distribution. Possible values: ‘holotype’ or
	 * empty.
	 */
	public String typeStatus;

	/**
	 * The process by which the biological individual(s) represented in the
	 * Occurrence became established at the location. Possible values: values
	 * listed as Origin in WRIMS
	 */
	public String establishmentMeans;

	/**
	 * The geographic latitude (in decimal degrees, WGS84)
	 */
	public double decimalLatitude;

	/**
	 * The geographic longitude (in decimal degrees, WGS84)
	 */
	public double decimalLongitude;

	/**
	 * Quality status of the record. Possible values: 'checked’, ‘trusted’ or
	 * 'unreviewed’. See http://www.marinespecies.org/aphia.php?p=manual#topic22
	 */
	public String qualityStatus;

}
