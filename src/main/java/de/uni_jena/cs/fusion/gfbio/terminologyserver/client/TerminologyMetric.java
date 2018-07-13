package de.uni_jena.cs.fusion.gfbio.terminologyserver.client;

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

public class TerminologyMetric {
	public Integer averageNumberOfChildren;
	public Integer classesWithASingleChild;
	public Integer classesWithMoreThan1Parent;
	public Integer classesWithMoreThan25Children;
	public Integer classesWithoutDefinition;
	public Integer classesWithoutLabel;
	public Integer maximumDepth;
	public Integer maximumNumberOfChildren;
	public Integer numberOfLeaves;
	public Integer numClasses;
	public Integer numIndividuals;
	public Integer numProperties;
}
