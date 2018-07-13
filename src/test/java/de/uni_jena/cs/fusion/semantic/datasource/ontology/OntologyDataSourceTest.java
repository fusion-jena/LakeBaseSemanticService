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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uni_jena.cs.fusion.lakebase.Scope;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

/**
 * 
 * @since 0.1
 *
 */
public class OntologyDataSourceTest {

	public static OntologyDataSource createTestOntologyAdapter(List<String> languages)
			throws SemanticDataSourceException {
		return OntologyDataSourceFactory
				.ontology(new File(Thread.currentThread().getContextClassLoader()
						.getResource("ontology/ontologyAdapterTest.ttl").getFile()))
				.language(languages).scope(Scope.unit.getIris())
				.labelProperty("http://www.w3.org/2000/01/rdf-schema#label")
				.labelProperty("http://example.org/ontologies/test#as_number")
				.synonymProperty("http://example.org/ontologies/test#equals")
				.broaderProperty("http://example.org/ontologies/test#name_component")
				.narrowerProperty("http://example.org/ontologies/test#contains")
				.replacedByProperty("http://example.org/ontologies/test#replacedBy").build();
	}

	@Test
	public void adapterShouldConsiderLanguageProperties() throws SemanticDataSourceException {
		Collection<String> labels;
		List<String> languages = new ArrayList<String>();
		OntologyDataSource adapter;

		// without language properties
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("One"));
		assertTrue(labels.contains("1"));
		assertTrue(labels.contains("Eins"));
		assertEquals(3, labels.size());

		// with language properties ""
		languages.clear();
		languages.add("");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("1"));
		assertEquals(1, labels.size());

		// with language properties "en"
		languages.clear();
		languages.add("en");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("One"));
		assertEquals(1, labels.size());

		// with language properties "de"
		languages.clear();
		languages.add("de");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("Eins"));
		assertEquals(1, labels.size());

		// with language properties "en",""
		languages.clear();
		languages.add("en");
		languages.add("");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("1"));
		assertTrue(labels.contains("One"));
		assertEquals(2, labels.size());

		// with language properties "de",""
		languages.clear();
		languages.add("de");
		languages.add("");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("1"));
		assertTrue(labels.contains("Eins"));
		assertEquals(2, labels.size());

		// with language properties "en","de"
		languages.clear();
		languages.add("en");
		languages.add("de");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("One"));
		assertTrue(labels.contains("Eins"));
		assertEquals(2, labels.size());

		// with language properties "en","de",""
		languages.clear();
		languages.add("en");
		languages.add("de");
		languages.add("");
		adapter = OntologyDataSourceTest.createTestOntologyAdapter(languages);
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("One"));
		assertTrue(labels.contains("1"));
		assertTrue(labels.contains("Eins"));
		assertEquals(3, labels.size());
	}

	@Test
	public void getAll() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		IRI[] expectedConcepts = { IRI.create("http://example.org/ontologies/test#number"),
				IRI.create("http://example.org/ontologies/test#even_number"),
				IRI.create("http://example.org/ontologies/test#odd_number"),
				IRI.create("http://example.org/ontologies/test#prime_number"),
				IRI.create("http://example.org/ontologies/test#one"),
				IRI.create("http://example.org/ontologies/test#two"),
				IRI.create("http://example.org/ontologies/test#trey"),
				IRI.create("http://example.org/ontologies/test#four"),
				IRI.create("http://example.org/ontologies/test#divisible_by_two_number"),
				IRI.create("http://example.org/ontologies/test#eins"),
				IRI.create("http://example.org/ontologies/test#zwei"),
				IRI.create("http://example.org/ontologies/test#drei"),
				IRI.create("http://example.org/ontologies/test#vier"),
				IRI.create("http://example.org/ontologies/test#one_earth"),
				IRI.create("http://example.org/ontologies/test#two_feet"),
				IRI.create("http://example.org/ontologies/test#four_seasons"),
				IRI.create("http://example.org/ontologies/test#one_or_two"),
				IRI.create("http://example.org/ontologies/test#one_plus_one") };
		Collection<IRI> concepts = adapter.getSignature();
		assertTrue(concepts.containsAll(Arrays.asList(expectedConcepts)));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		Collection<IRI> concepts;

		// test rdf:type parent relation
		concepts = adapter.getBroaders(IRI.create("http://example.org/ontologies/test#even_number"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#number")));
		assertEquals(1, concepts.size());

		concepts = adapter.getBroaders(IRI.create("http://example.org/ontologies/test#one"));
		// test rdfs:subClassOf parent relation
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#odd_number")));
		// test property defined child inverse relation
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#one_or_two")));
		assertEquals(2, concepts.size());

		// test property defined parent relation
		concepts = adapter.getBroaders(IRI.create("http://example.org/ontologies/test#one_earth"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#one")));
		assertEquals(1, concepts.size());

	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		Collection<String> labels;

		// test rdfs:label label relation
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#number"));
		assertTrue(labels.contains("Number"));
		assertEquals(1, labels.size());

		// test property defined label relation
		labels = adapter.getLabels(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(labels.contains("One"));
		assertTrue(labels.contains("Eins"));
		assertTrue(labels.contains("1"));
		assertEquals(3, labels.size());
	}

	@Test
	public void getNamespaces() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		Collection<String> conceptNamespaces = adapter.getNamespaces();
		assertTrue(conceptNamespaces.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		assertTrue(conceptNamespaces.contains("http://www.w3.org/2002/07/owl#"));
		assertTrue(conceptNamespaces.contains("http://www.w3.org/XML/1998/namespace"));
		assertTrue(conceptNamespaces.contains("http://www.w3.org/2000/01/rdf-schema#"));
		assertTrue(conceptNamespaces.contains("http://example.org/ontologies/test#"));
		assertTrue(conceptNamespaces.contains("http://www.w3.org/2001/XMLSchema#"));
		assertEquals(6, conceptNamespaces.size());
	}

	@Test
	public void getNarrowers() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		Collection<IRI> concepts;

		// test rdfs:subClassOf child relation
		concepts = adapter.getNarrowers(IRI.create("http://example.org/ontologies/test#number"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#even_number")));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#odd_number")));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#prime_number")));
		assertEquals(3, concepts.size());

		// test rdf:type child relation
		concepts = adapter.getNarrowers(IRI.create("http://example.org/ontologies/test#even_number"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#two")));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#four")));
		assertEquals(2, concepts.size());

		// test property defined child relation
		concepts = adapter.getNarrowers(IRI.create("http://example.org/ontologies/test#one_or_two"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#one")));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#two")));
		assertEquals(2, concepts.size());

		// test property defined parent inverse relation
		concepts = adapter.getNarrowers(IRI.create("http://example.org/ontologies/test#one"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#one_earth")));
	}

	@Test
	public void getReplacedBy() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		Collection<IRI> concepts = adapter.getReplacedBy(IRI.create("http://example.org/ontologies/test#removed"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#added")));
		assertEquals(1, concepts.size());
	}

	@Test
	public void isDeprecated() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		assertTrue(adapter.isDeprecated(IRI.create("http://example.org/ontologies/test#removed")));
	}

	@Test
	public void getSynonyms() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());
		Collection<IRI> concepts;

		// test owl:equivalentClass equivalent relation
		concepts = adapter.getSynonyms(IRI.create("http://example.org/ontologies/test#divisible_by_two_number"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#even_number")));
		assertEquals(1, concepts.size());

		// test owl:sameAs equivalent relation
		concepts = adapter.getSynonyms(IRI.create("http://example.org/ontologies/test#eins"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#one")));
		assertEquals(1, concepts.size());

		// test property defined equivalent relation
		concepts = adapter.getSynonyms(IRI.create("http://example.org/ontologies/test#one_plus_one"));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#two")));
		assertTrue(concepts.contains(IRI.create("http://example.org/ontologies/test#zwei")));
		assertEquals(2, concepts.size());

	}

	@Test
	public void hasSuitableNamespace() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		assertTrue(adapter.hasSuitableNamespace(IRI.create("http://example.org/ontologies/test#")));
		assertTrue(adapter.hasSuitableNamespace(IRI.create("http://example.org/ontologies/test#one")));
		assertTrue(adapter.hasSuitableNamespace(IRI.create("http://example.org/ontologies/test#not_exiting_concept")));
		assertFalse(adapter.hasSuitableNamespace(IRI.create("http://example.org/ontologies/not_exiting#")));
		assertFalse(adapter.hasSuitableNamespace(IRI.create("http://example.org/ontologies/not_exiting#one")));
	}

	@Test
	public void match() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		Map<IRI, Double> match = adapter.getMatches("Number");

		assertEquals(1, match.size());
		assertTrue(match.containsKey(IRI.create("http://example.org/ontologies/test#number")));
	}

	/**
	 * Sure that OWL API Issue 555 is fixed.
	 * 
	 * @see https://github.com/owlcs/owlapi/issues/555
	 * @throws OWLOntologyCreationException
	 */
	@Test
	public void owlApiIssue555() throws OWLOntologyCreationException {
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		String ttl = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ "@prefix owl: <http://www.w3.org/2002/07/owl#> ." + ""
				+ "<http://example.org/> rdf:type owl:Ontology ." + "<http://example.org/> rdfs:label \"foo\" ." + ""
				+ "<http://example.org/#bar>    rdfs:label  \"bar\" .";
		InputStream ontoStream = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8));
		OWLOntology ontology = om.loadOntologyFromOntologyDocument(ontoStream);
		assertEquals(ontology.annotationPropertiesInSignature().distinct().count(),
				ontology.annotationPropertiesInSignature().count());
	}

	@Test
	public void suggest() throws SemanticDataSourceException {
		OntologyDataSource adapter = OntologyDataSourceTest.createTestOntologyAdapter(Collections.emptyList());

		Map<IRI, String> match = adapter.getSuggestions("Numb");

		assertEquals(1, match.size());
		assertTrue(match.containsKey(IRI.create("http://example.org/ontologies/test#number")));
	}

	@Ignore
	@Test
	public void useOnlineSource() throws SemanticDataSourceException {
		OntologyDataSource om = OntologyDataSourceFactory
				.ontology(IRI.create("http://www.wurvoc.org/vocabularies/om-1.8/")).labelPropertyRdfsLabel().build();
		assertTrue(om.getLabels(IRI.create("http://www.wurvoc.org/vocabularies/om-1.8/metre")).contains("metre"));

		OntologyDataSource envo = OntologyDataSourceFactory
				.ontology(IRI.create("http://purl.obolibrary.org/obo/envo.owl")).labelPropertyRdfsLabel().build();
		assertTrue(envo.getLabels(IRI.create("http://purl.obolibrary.org/obo/ENVO_00000020")).contains("lake"));
	}

	// TODO: should work with invalid or missing properties

}
