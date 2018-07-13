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

import de.uni_jena.cs.fusion.lakebase.Completer;
import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.model.CompleteRequest;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class CompleteWorker implements ServiceWorker {

	@Override
	public Object processRequest(Environment environment, Object input) throws ServiceWorkerException {

		// load environment
		Completer suggestor = environment.getSuggestor();

		// respond
		try {
			return suggestor.complete(((CompleteRequest) input).stump, ((CompleteRequest) input).scopes);
		} catch (SemanticDataSourceException e) {
			throw new ServiceWorkerException(500, e);
		}
	}

}
