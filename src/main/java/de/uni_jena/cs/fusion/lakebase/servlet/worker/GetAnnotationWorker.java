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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.AnnotationManager;
import de.uni_jena.cs.fusion.lakebase.AnnotationManagerException;
import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.model.Annotation;
import de.uni_jena.cs.fusion.lakebase.model.EntitiesAnnotation;
import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class GetAnnotationWorker implements ServiceWorker {

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {
		try {

			// load environment
			AnnotationManager annotationManager = environment.getAnnotationManager();
			SemanticDataSource semanticDataSource = environment.getSemanticDataSource();

			List<EntitiesAnnotation> entitiesAnnotations = new ArrayList<EntitiesAnnotation>();
			for (Entity entity : (Entity[]) input) {
				EntitiesAnnotation entitiesAnnotation = new EntitiesAnnotation();
				entitiesAnnotations.add(entitiesAnnotation);
				entitiesAnnotation.entities = Collections.singletonList(entity);
				entitiesAnnotation.accepted = new ArrayList<Annotation>();
				for (Entry<String, IRI> entry : annotationManager.getAcceptedAnnotations(entity.packageID,
						entity.objectID, entity.columnID, entity.rowID, entity.metaID).entrySet()) {
					String term = entry.getKey();
					IRI iri = entry.getValue();
					String label = semanticDataSource.getLabel(iri).orElse(null);
					entitiesAnnotation.accepted.add(new Annotation(term, iri, label));
				}
				entitiesAnnotation.rejected = new ArrayList<Annotation>();
				for (Entry<String, List<IRI>> entry : annotationManager.getRejectedAnnotations(entity.packageID,
						entity.objectID, entity.columnID, entity.rowID, entity.metaID).entrySet()) {
					String term = entry.getKey();
					for (IRI iri : entry.getValue()) {
						String label = semanticDataSource.getLabel(iri).orElse(null);
						entitiesAnnotation.rejected.add(new Annotation(term, iri, label));
					}
				}
			}

			return entitiesAnnotations;

		} catch (AnnotationManagerException | SemanticDataSourceException e) {
			throw new ServiceWorkerException(500, e);
		}
	}
}
