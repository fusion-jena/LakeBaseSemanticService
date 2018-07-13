package de.uni_jena.cs.fusion.lakebase.model;

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public final class Entity {
	@JsonProperty(value="package",required=true)
	public long packageID;
	@JsonProperty("object")
	public Long objectID;
	@JsonProperty("column")
	public Long columnID;
	@JsonProperty("row")
	public Long rowID;
	@JsonProperty("meta")
	public Long metaID;

	public Entity(){};
	
	public Entity(long packageID, Long objectID, Long columnID, Long rowID, Long metaID) {
		this.packageID = packageID;
		this.objectID = objectID;
		this.columnID = columnID;
		this.rowID = rowID;
		this.metaID = metaID;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Entity) {
			Entity other = (Entity) object;
			return this.packageID == other.packageID
					&& (this.objectID != null && this.objectID.equals(other.objectID)
							|| (this.objectID == null && other.objectID == null))
					&& (this.columnID != null && this.columnID.equals(other.columnID)
							|| (this.columnID == null && other.columnID == null))
					&& (this.rowID != null && this.rowID.equals(other.rowID)
							|| (this.rowID == null && other.rowID == null))
					&& (this.metaID != null && this.metaID.equals(other.metaID)
							|| (this.metaID == null && other.metaID == null));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = (int) packageID;
		// spread values by six power of two
		if (objectID != null) {
			hash += objectID.intValue() * 64;
		}
		if (columnID != null) {
			hash += columnID.intValue() * 4096;
		}
		if (metaID != null) {
			hash += metaID.intValue() * 262144;
		}
		if (rowID != null) {
			// row might become the largest values, so it becomes the
			// remaining seven bits
			hash += rowID.intValue() * 16777216;
		}
		return hash;
	}

	@Override
	public String toString() {
		return "<" + this.packageID + "|" + (this.objectID != null ? this.objectID.toString() : "-") + "|"
				+ (this.columnID != null ? this.columnID.toString() : "-") + "|"
				+ (this.rowID != null ? this.rowID.toString() : "-") + "|"
				+ (this.metaID != null ? this.metaID.toString() : "-") + ">";
	}
}
