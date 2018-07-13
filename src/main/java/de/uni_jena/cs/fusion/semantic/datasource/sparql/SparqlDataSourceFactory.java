package de.uni_jena.cs.fusion.semantic.datasource.sparql;

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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.sparql.SparqlDataSource.Feature;

public class SparqlDataSourceFactory {
	
	public static SparqlDataSourceFactory service(String service) {
		return new SparqlDataSourceFactory(service);
	}
	
	private SparqlDataSource sparqlDataSource;

	public SparqlDataSourceFactory(String service) {
		this.sparqlDataSource = new SparqlDataSource();
		this.sparqlDataSource.service = service;
	}
	
	public SparqlDataSourceFactory alternativeLabelQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.ALTERNATIVE_LABELS, query);
		return this;
	}
	
	public SparqlDataSourceFactory alternativeLabelQuery(String query) {
		return this.alternativeLabelQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSourceFactory broaderQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.BROADERS, query);
		return this;
	}
	
	public SparqlDataSourceFactory broaderQuery(String query) {
		return this.broaderQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSource build() throws SemanticDataSourceException {
		this.sparqlDataSource.init();
		return this.sparqlDataSource;
	}
	
	/**
	 * States that the IRIs of the build {@Code Adapter} are valid URLs.
	 * 
	 * @return this SparqlAdapterFactory
	 */
	public SparqlDataSourceFactory dereferencing() {
		this.sparqlDataSource.dereferencingIris = true;
		return this;
	}
	
	public SparqlDataSourceFactory descriptionQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.DESCRIPTIONS, query);
		return this;
	}
	
	public SparqlDataSourceFactory descriptionQuery(String query) {
		return this.descriptionQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSourceFactory iriQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.IRIS, query);
		return this;
	}
	
	public SparqlDataSourceFactory iriQuery(String query) {
		return this.iriQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSourceFactory labelQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.LABELS, query);
		return this;
	}
	
	public SparqlDataSourceFactory labelQuery(String query) {
		return this.labelQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSourceFactory namespace(Collection<String> namespaces) {
		this.sparqlDataSource.namespaces.addAll(namespaces);
		return this;
	}
	
	public SparqlDataSourceFactory namespace(String namespace) {
		this.sparqlDataSource.namespaces.add(namespace);
		return this;
	}
	
	public SparqlDataSourceFactory narrowerQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.NARROWERS, query);
		return this;
	}
	
	public SparqlDataSourceFactory narrowerQuery(String query) {
		return this.narrowerQuery(QueryFactory.create(query));
	}
	
	public SparqlDataSourceFactory replacedByQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.REPLACEMENTS, query);
		return this;
	}
	
	public SparqlDataSourceFactory replacedByQuery(String query) {
		return this.replacedByQuery(QueryFactory.create(query));
	}

	public SparqlDataSourceFactory scope(Collection<IRI> scopes) {
		this.sparqlDataSource.scopes.addAll(scopes);
		return this;
	}

	public SparqlDataSourceFactory scope(IRI scope) {
		this.sparqlDataSource.scopes.add(scope);
		return this;
	}

	public SparqlDataSourceFactory synonymQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.SYNONYMS, query);
		return this;
	}

	public SparqlDataSourceFactory synonymQuery(String query) {
		return this.synonymQuery(QueryFactory.create(query));
	}

	public SparqlDataSourceFactory urlQuery(Query query) {
		this.sparqlDataSource.queries.put(Feature.URLS, query);
		return this;
	}
	
	public SparqlDataSourceFactory urlQuery(String query) {
		return this.urlQuery(QueryFactory.create(query));
	}

}
