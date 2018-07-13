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

import java.util.List;

public class Completion {
	public String stump;
	public List<Annotation> completions;

	@Override
	public boolean equals(Object object) {
		if (object instanceof Completion) {
			Completion other = (Completion) object;
			return (this == object || this.stump == null && other.stump == null || this.stump.equals(other.stump)
					|| this.completions == null && other.completions == null
					|| this.completions.size() == other.completions.size()
							&& this.completions.containsAll(other.completions)
							&& other.completions.containsAll(this.completions));
		}
		return false;
	}
}
