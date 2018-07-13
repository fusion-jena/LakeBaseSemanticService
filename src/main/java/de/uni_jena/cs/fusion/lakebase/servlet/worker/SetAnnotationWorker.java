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

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.Scope;
import de.uni_jena.cs.fusion.lakebase.model.EntitiesAnnotation;
import de.uni_jena.cs.fusion.lakebase.model.Entity;

public class SetAnnotationWorker implements ServiceWorker {
	private static Logger log = LoggerFactory.getLogger(SetAnnotationWorker.class);

	private boolean asynchron;

	public SetAnnotationWorker(boolean asynchron) {
		this.asynchron = asynchron;
	}

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {
		// load environment
		ListeningScheduledExecutorService executor = environment.getExecutor();

		for (EntitiesAnnotation annotation : (EntitiesAnnotation[]) input) {
			// generate task
			SaveAnnotationTask task = new SaveAnnotationTask(environment, annotation, log);

			// process asynchrony
			if (asynchron) {
				executor.execute(task);
			} else {
				task.run();
			}
		}

		return null;
	}

	private class SaveAnnotationTask implements Runnable {

		EntitiesAnnotation annotation;
		Environment environment;
		Logger log;

		SaveAnnotationTask(Environment environment, EntitiesAnnotation annotation, Logger log) {
			this.annotation = annotation;
			this.environment = environment;
			this.log = log;
		}

		@Override
		public void run() {
			try {
				// replenish annotations
				Map<String, IRI> determined = environment.getAnnotator().determineAnnotations(annotation.text,
						annotation.getAcceptedAsMap(), annotation.getRejectedAsMap(), ((annotation.scopes.isEmpty())? Collections.singleton(Scope.all) : annotation.scopes));

				try (Connection connection = environment.getDatabaseManager().getConnection()) {
					connection.setAutoCommit(false);

					for (Entity entity : annotation.entities) {
						environment.getAnnotationManager().setAnnotations(connection, entity.packageID, entity.objectID,
								entity.columnID, entity.rowID, entity.metaID, determined,
								annotation.getRejectedAsMap());
					}
					connection.commit();
				}
			} catch (Throwable e) {
				log.error("Set Annotation Task failed for: " + this.annotation, e);
			}
		}
	}
}
