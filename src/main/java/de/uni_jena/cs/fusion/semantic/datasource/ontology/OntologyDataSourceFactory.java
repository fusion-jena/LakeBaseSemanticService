package de.uni_jena.cs.fusion.semantic.datasource.ontology;

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

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.ontology.OntologyDataSource.PropertySubject;

public class OntologyDataSourceFactory {

	public static OntologyDataSourceFactory ontology(IRI ontologyIRI) throws SemanticDataSourceException {
		return new OntologyDataSourceFactory(new IRIDocumentSource(ontologyIRI));
	}

	public static OntologyDataSourceFactory ontology(File ontologyFile) throws SemanticDataSourceException {
		return new OntologyDataSourceFactory(new FileDocumentSource(ontologyFile));
	}

	public OntologyDataSourceFactory fallback(File ontologyFile) {
		this.ontologyDataSource.ontologyFallbackSource = Optional.of(new FileDocumentSource(ontologyFile));
		return this;
	}

	public OntologyDataSourceFactory fallback(IRI ontologyIRI) {
		this.ontologyDataSource.ontologyFallbackSource = Optional.of(new IRIDocumentSource(ontologyIRI));
		return this;
	}

	private OntologyDataSource ontologyDataSource;

	private OntologyDataSourceFactory(OWLOntologyDocumentSource ontologySource) {
		this.ontologyDataSource = new OntologyDataSource(ontologySource);
	}

	private void addProperty(IRI iri, PropertySubject subject) {
		this.ontologyDataSource.propertyIRIs.get(subject).add(iri);
	}

	public OntologyDataSourceFactory alternativLabelProperty(IRI iri) {
		addProperty(iri, PropertySubject.ALTERNATIVE_LABEL);
		return this;
	}

	public OntologyDataSourceFactory alternativLabelProperty(String iri) {
		return alternativLabelProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory broaderProperty(IRI iri) {
		addProperty(iri, PropertySubject.BROADER);
		return this;
	}

	public OntologyDataSourceFactory broaderProperty(String iri) {
		return broaderProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory broaderPropertyDctermsIsPartOf() {
		return broaderProperty(IRI.create("http://purl.org/dc/terms/isPartOf"));
	}

	public OntologyDataSource build() throws SemanticDataSourceException {
		this.ontologyDataSource.load();

		// protect adapter against later changes
		OntologyDataSource adapter = this.ontologyDataSource;
		this.ontologyDataSource = null;
		return adapter;
	}

	/**
	 * States that the IRIs of this {@link OntologyDataSource}s ontology are valid
	 * URLs.
	 * 
	 * @return this {@link OntologyDataSourceFactory}
	 */
	public OntologyDataSourceFactory dereferencing() {
		this.ontologyDataSource.dereferencing = true;
		return this;
	}

	/**
	 * States that the {@link OntologyDataSource} should reload the ontology at
	 * maintenance and thereby automatically receive updates.
	 * 
	 * @return this {@link OntologyDataSourceFactory}
	 */
	public OntologyDataSourceFactory reloading() {
		this.ontologyDataSource.reloading = true;
		return this;
	}

	public OntologyDataSourceFactory descriptionProperty(IRI iri) {
		addProperty(iri, PropertySubject.DESCRIPTION);
		return this;
	}

	public OntologyDataSourceFactory descriptionProperty(String iri) {
		return descriptionProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory descriptionPropertyRdfsComment() {
		return descriptionProperty(IRI.create("http://www.w3.org/2000/01/rdf-schema#comment"));
	}

	public OntologyDataSourceFactory descriptionPropertySkosDefinition() {
		return descriptionProperty(IRI.create("http://www.w3.org/2004/02/skos/core#definition"));
	}

	public OntologyDataSourceFactory labelProperty(IRI iri) {
		addProperty(iri, PropertySubject.LABEL);
		return this;
	}

	public OntologyDataSourceFactory labelProperty(String iri) {
		return labelProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory labelPropertyRdfsLabel() {
		return labelProperty(IRI.create("http://www.w3.org/2000/01/rdf-schema#label"));
	}

	public OntologyDataSourceFactory labelPropertySkosPrefLabel() {
		return labelProperty(IRI.create("http://www.w3.org/2004/02/skos/core#prefLabel"));
	}

	public OntologyDataSourceFactory language(Collection<String> languages) {
		this.ontologyDataSource.languages.addAll(languages);
		return this;
	}

	public OntologyDataSourceFactory language(String language) {
		this.ontologyDataSource.languages.add(language);
		return this;
	}

	public OntologyDataSourceFactory narrowerProperty(IRI iri) {
		addProperty(iri, PropertySubject.NARROWER);
		return this;
	}

	public OntologyDataSourceFactory narrowerProperty(String iri) {
		return narrowerProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory narrowerPropertyDctermsHasPart() {
		return narrowerProperty(IRI.create("http://purl.org/dc/terms/hasPart"));
	}

	public OntologyDataSourceFactory replacedByProperty(String iri) {
		return replacedByProperty(IRI.create(iri));
	}

	public OntologyDataSourceFactory replacedByProperty(IRI iri) {
		addProperty(iri, PropertySubject.REPLACED_BY);
		return this;
	}

	public OntologyDataSourceFactory scope(Collection<IRI> scopes) {
		this.ontologyDataSource.scopes.addAll(scopes);
		return this;
	}

	public OntologyDataSourceFactory scope(IRI scope) {
		this.ontologyDataSource.scopes.add(scope);
		return this;
	}

	public OntologyDataSourceFactory synonymProperty(IRI iri) {
		addProperty(iri, PropertySubject.SYNONYM);
		return this;
	}

	public OntologyDataSourceFactory synonymProperty(String iri) {
		return synonymProperty(IRI.create(iri));
	}
}
