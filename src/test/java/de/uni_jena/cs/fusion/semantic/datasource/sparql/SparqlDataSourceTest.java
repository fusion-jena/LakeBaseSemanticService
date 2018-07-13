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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class SparqlDataSourceTest {

	@Test
	public void getAll() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.iriQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?key WHERE { ?key wdt:P279 wd:Q18602249. }")
				.build();

		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q108")));
		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q109")));
		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q110")));
	}

	@Test
	public void getAlternativeLabels() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.alternativeLabelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?key ?altLabel WHERE { ?key wdt:P279 wd:Q18602249. ?key skos:altLabel ?altLabel. FILTER((LANG(?altLabel)) = \"en\") }")
				.build();

		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q108")).contains("Jan"));
		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q109")).contains("Feb"));
		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q110")).contains("Mar"));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.broaderQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> SELECT ?key ?value WHERE { ?key wdt:P279* wd:Q23397. ?value wdt:P279* wd:Q23397. ?key wdt:P279 ?value. }")
				.build();

		assertTrue(adapter.getBroaders(IRI.create("http://www.wikidata.org/entity/Q131681"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q3215290")));
		assertTrue(adapter.getAllBroaders(IRI.create("http://www.wikidata.org/entity/Q131681"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q23397")));
	}

	@Test
	public void getDataUID() throws SemanticDataSourceException {
		SparqlDataSource adapter1 = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.alternativeLabelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?key ?altLabel WHERE { ?key wdt:P279 wd:Q18602249. ?key skos:altLabel ?altLabel. FILTER((LANG(?altLabel)) = \"en\") }")
				.build();

		SparqlDataSource adapter2 = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.labelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?key ?label WHERE { ?key wdt:P279 wd:Q18602249. ?key rdfs:label ?label. FILTER((LANG(?label)) = \"en\") }")
				.build();
		assertNotEquals(adapter1.getDataUID(), adapter2.getDataUID());
	}

	@Test
	public void getDescriptions() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.descriptionQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix schema: <http://schema.org/> SELECT ?key ?value WHERE { ?key wdt:P279 wd:Q18602249. ?key schema:description ?value. FILTER((LANG(?value)) = \"en\") }")
				.build();

		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q108")).iterator().next()
				.contains("month"));
		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q109")).iterator().next()
				.contains("month"));
		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q110")).iterator().next()
				.contains("month"));
	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.labelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?key ?label WHERE { ?key wdt:P279 wd:Q18602249. ?key rdfs:label ?label. FILTER((LANG(?label)) = \"en\") }")
				.build();

		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q108")).contains("January"));
		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q109")).contains("February"));
		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q110")).contains("March"));
	}

	@Test
	public void getNamespaces() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("").namespace("http://www.wikidata.org/").build();

		assertTrue(adapter.getNamespaces().contains("http://www.wikidata.org/"));
	}

	@Test
	public void getNarrowers() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.narrowerQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> SELECT ?key ?value WHERE { ?key wdt:P279* wd:Q23397. ?value wdt:P279* wd:Q23397. ?value wdt:P279 ?key. }")
				.build();

		assertTrue(adapter.getNarrowers(IRI.create("http://www.wikidata.org/entity/Q3215290"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q131681")));
		assertTrue(adapter.getAllNarrowers(IRI.create("http://www.wikidata.org/entity/Q23397"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q131681")));
	}

	@Test
	public void getReplacedBy() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.replacedByQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix owl: <http://www.w3.org/2002/07/owl#> SELECT ?key ?value WHERE { ?value wdt:P31?/wdt:P279* wd:Q23397. ?key owl:sameAs ?value. }")
				.build();

		assertTrue(adapter.getReplacedBy(IRI.create("http://www.wikidata.org/entity/Q32511772"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q694789")));
	}

	@Test
	public void getScopes() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("").scope(IRI.create("http://www.wikidata.org/")).build();

		assertTrue(adapter.getScopes().contains(IRI.create("http://www.wikidata.org/")));
	}

	@Test
	public void getSynonyms() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.synonymQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> SELECT ?key ?value WHERE { ?key wdt:P279* wd:Q23397; wdt:P1709 ?value. }")
				.build();

		assertTrue(adapter.getSynonyms(IRI.create("http://www.wikidata.org/entity/Q23397"))
				.contains(IRI.create("http://dbpedia.org/ontology/Lake")));
	}

	@Test
	public void getUrls() throws SemanticDataSourceException, MalformedURLException {
		// by query
		SparqlDataSource queryAdapter = SparqlDataSourceFactory
				.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.urlQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix schema: <http://schema.org/> SELECT ?key ?value WHERE { ?key wdt:P279 wd:Q18602249. ?value schema:about ?key. ?value schema:isPartOf <https://en.wikipedia.org/>. }")
				.build();

		assertTrue(queryAdapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://en.wikipedia.org/wiki/January")));

		// by dereferencing IRI
		SparqlDataSource dereferencingAdapter = SparqlDataSourceFactory
				.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.iriQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> SELECT ?key WHERE { ?key wdt:P279 wd:Q18602249. }")
				.dereferencing().build();

		assertTrue(dereferencingAdapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("http://www.wikidata.org/entity/Q108")));

		// by query and dereferencing IRI
		SparqlDataSource bothAdapter = SparqlDataSourceFactory
				.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.urlQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix schema: <http://schema.org/> SELECT ?key ?value WHERE { ?key wdt:P279 wd:Q18602249. ?value schema:about ?key. ?value schema:isPartOf <https://en.wikipedia.org/>. }")
				.dereferencing().build();

		assertTrue(bothAdapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://en.wikipedia.org/wiki/January")));
		assertTrue(dereferencingAdapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("http://www.wikidata.org/entity/Q108")));
	}

	@Test
	public void isDeprecated() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.replacedByQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix owl: <http://www.w3.org/2002/07/owl#> SELECT ?key ?value WHERE { ?value wdt:P31?/wdt:P279* wd:Q23397. ?key owl:sameAs ?value. }")
				.build();

		assertTrue(adapter.isDeprecated(IRI.create("http://www.wikidata.org/entity/Q32511772")));
	}

	@Test
	public void match() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.labelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?key ?label WHERE { ?key wdt:P279 wd:Q18602249. ?key rdfs:label ?label. FILTER((LANG(?label)) = \"en\") }")
				.build();

		assertTrue(adapter.getMatches("Januar").containsKey(IRI.create("http://www.wikidata.org/entity/Q108")));
		assertTrue(adapter.getMatches("Februar").containsKey(IRI.create("http://www.wikidata.org/entity/Q109")));
	}

	@Test
	public void suggest() throws SemanticDataSourceException {
		SparqlDataSource adapter = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.labelQuery(
						"prefix wd: <http://www.wikidata.org/entity/> prefix wdt: <http://www.wikidata.org/prop/direct/> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?key ?label WHERE { ?key wdt:P279 wd:Q18602249. ?key rdfs:label ?label. FILTER((LANG(?label)) = \"en\") }")
				.build();

		assertTrue(adapter.getSuggestions("Januar").containsKey(IRI.create("http://www.wikidata.org/entity/Q108")));
	}
}
