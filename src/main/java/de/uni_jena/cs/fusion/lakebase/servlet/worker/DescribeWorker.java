package de.uni_jena.cs.fusion.lakebase.servlet.worker;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.HierarchyException;
import de.uni_jena.cs.fusion.lakebase.HierarchyManager;
import de.uni_jena.cs.fusion.lakebase.model.Description;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class DescribeWorker implements ServiceWorker {

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {
		try {

			// load environment
			SemanticDataSource semanticDataSource = environment.getSemanticDataSource();
			HierarchyManager hierarchyManager = environment.getHierarchyManager();

			List<Description> descriptions = new ArrayList<Description>();

			for (IRI iri : (IRI[]) input) {
				if (semanticDataSource.isPresent(iri)) {
					Description description = new Description();
					descriptions.add(description);

					if (!hierarchyManager.contains(iri)) {
						hierarchyManager.add(iri);
					}

					description.iri = iri;
					description.labels = semanticDataSource.getLabels(iri);
					description.alternativLabels = semanticDataSource.getAlternativeLabels(iri);
					description.descriptions = semanticDataSource.getDescriptions(iri);
					// make URLs distinct
					TreeSet<URL> urlSet = new TreeSet<URL>((e1, e2) -> e1.toString().compareTo(e2.toString()));
					urlSet.addAll(semanticDataSource.getUrls(iri));
					description.urls = new ArrayList<URL>(urlSet);
					description.types = new ArrayList<String>();
					if (hierarchyManager.isBroader(iri,
							IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Unit"))) {
						description.types.add("unit");
					}
					if (hierarchyManager.isBroader(iri,
							IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Quantity"))) {
						description.types.add("quantityKind");
					}
					if (hierarchyManager.isBroader(iri, IRI.create("http://sws.geonames.org/6295630/"))) {
						description.types.add("location");
					}
					if (hierarchyManager.isBroader(iri, IRI.create("urn:lsid:marinespecies.org:taxname:1"))) {
						description.types.add("species");
					}
					if (hierarchyManager.isBroader(iri, IRI.create("http://www.w3.org/2006/time#DateTimeInterval"))) {
						description.types.add("datetime");
					}
					description.synonyms = hierarchyManager.getSynonyms(iri);
					description.broaders = hierarchyManager.getBroaders(iri);
					// filter fall back entities
					description.broaders.removeIf(p -> p.getIRIString().startsWith("string:"));
				}
			}

			return descriptions;

		} catch (SemanticDataSourceException | HierarchyException e) {
			throw new ServiceWorkerException(500, e);
		}
	}
}
