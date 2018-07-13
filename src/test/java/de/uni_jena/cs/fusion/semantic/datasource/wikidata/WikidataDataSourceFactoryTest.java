package de.uni_jena.cs.fusion.semantic.datasource.wikidata;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.sparql.SparqlDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.wikidata.WikidataDataSourceFactory;

public class WikidataDataSourceFactoryTest {

	@Test
	public void getAll() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").build();

		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q108")));
		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q109")));
		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q110")));
	}

	@Test
	public void restrict() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249")
				.restrict("FILTER (? IN ( <http://www.wikidata.org/entity/Q108> ))").build();

		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q108")));
		assertFalse(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q109")));
		assertFalse(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q110")));

		adapter = WikidataDataSourceFactory.root("wd:Q4862348").restrict("? wdt:P17 wd:Q183").build();

		assertTrue(adapter.getSignature().contains(IRI.create("http://www.wikidata.org/entity/Q694789")));

	}

	@Test
	public void getAlternativeLabels() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").local(new Locale("en"))
				.alternativeLabelPropertyLocalized("skos:altLabel").build();

		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q108")).contains("Jan"));
		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q109")).contains("Feb"));
		assertTrue(adapter.getAlternativeLabels(IRI.create("http://www.wikidata.org/entity/Q110")).contains("Mar"));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q23397").broaderProperty("wdt:P279")
				.broaderProperty("wdt:P31").build();

		assertTrue(adapter.getBroaders(IRI.create("http://www.wikidata.org/entity/Q131681"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q3215290")));
		assertTrue(adapter.getAllBroaders(IRI.create("http://www.wikidata.org/entity/Q131681"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q23397")));
	}

	@Test
	public void getDescriptions() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").local(new Locale("en"))
				.descriptionPropertyLocalized("schema:description").build();

		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q108")).iterator().next()
				.contains("month"));
		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q109")).iterator().next()
				.contains("month"));
		assertTrue(adapter.getDescriptions(IRI.create("http://www.wikidata.org/entity/Q110")).iterator().next()
				.contains("month"));
	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").local(new Locale("en"))
				.labelPropertyLocalized("rdfs:label").build();

		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q108")).contains("January"));
		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q109")).contains("February"));
		assertTrue(adapter.getLabels(IRI.create("http://www.wikidata.org/entity/Q110")).contains("March"));
	}

	@Test
	public void getNamespaces() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q11573").build();

		assertTrue(adapter.getNamespaces().contains("http://www.wikidata.org/"));
	}

	@Test
	public void getNarrowers() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q23397").narrowerProperty("^wdt:P279")
				.narrowerProperty("^wdt:P31").build();

		assertTrue(adapter.getNarrowers(IRI.create("http://www.wikidata.org/entity/Q3215290"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q131681")));
		assertTrue(adapter.getAllNarrowers(IRI.create("http://www.wikidata.org/entity/Q23397"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q131681")));
	}

	@Test
	public void getDepricatedBy() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q23397").replacedByProperty("owl:sameAs").build();

		assertTrue(adapter.isDeprecated(IRI.create("http://www.wikidata.org/entity/Q32511772")));
		assertTrue(adapter.getReplacedBy(IRI.create("http://www.wikidata.org/entity/Q32511772"))
				.contains(IRI.create("http://www.wikidata.org/entity/Q694789")));
	}

	@Test
	public void getScopes() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q11573").scope(IRI.create("http://www.wikidata.org/"))
				.build();

		assertTrue(adapter.getScopes().contains(IRI.create("http://www.wikidata.org/")));
	}

	@Test
	public void getSynonyms() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory
				.pattern("{{? wdt:P31/wdt:P279* wd:Q23397; wdt:P17 wd:Q183.} UNION {? wdt:P279* wd:Q23397}}")
				.synonymProperty("wdt:P1709").synonymIDProperty("wdt:P1566").build();

		assertTrue(adapter.getSynonyms(IRI.create("http://www.wikidata.org/entity/Q23397"))
				.contains(IRI.create("http://dbpedia.org/ontology/Lake")));

		assertTrue(adapter.getSynonyms(IRI.create("http://www.wikidata.org/entity/Q4138"))
				.contains(IRI.create("http://sws.geonames.org/2940125/")));
	}

	@Test
	public void getUrls() throws SemanticDataSourceException, MalformedURLException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").urlProperty("^schema:about").build();
		// TODO restrict to: ?value schema:isPartOf <https://en.wikipedia.org/>

		// by query
		assertTrue(adapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://en.wikipedia.org/wiki/January")));

		// by dereferencing IRI
		assertTrue(adapter.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("http://www.wikidata.org/entity/Q108")));
	}

	@Test
	public void urlLocalDefaults() throws SemanticDataSourceException, MalformedURLException {
		SparqlDataSource adapterEn = WikidataDataSourceFactory.root("wd:Q108").local(new Locale("en")).urlLocalDefaults().build();
		SparqlDataSource adapterDe = WikidataDataSourceFactory.root("wd:Q108").local(new Locale("de")).urlLocalDefaults().build();

		assertTrue(adapterEn.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://en.wikipedia.org/wiki/January")));
		assertTrue(adapterDe.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://de.wikipedia.org/wiki/Januar")));
		assertFalse(adapterEn.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://de.wikipedia.org/wiki/Januar")));
		assertFalse(adapterDe.getUrls(IRI.create("http://www.wikidata.org/entity/Q108"))
				.contains(new URL("https://en.wikipedia.org/wiki/January")));
	}
	
	

	@Test
	public void match() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").local(new Locale("en"))
				.labelPropertyLocalized("rdfs:label").build();

		assertTrue(adapter.getMatches("Januar").containsKey(IRI.create("http://www.wikidata.org/entity/Q108")));
		assertTrue(adapter.getMatches("Februar").containsKey(IRI.create("http://www.wikidata.org/entity/Q109")));
	}

	@Test
	public void suggest() throws SemanticDataSourceException {
		SparqlDataSource adapter = WikidataDataSourceFactory.root("wd:Q18602249").local(new Locale("en"))
				.labelPropertyLocalized("rdfs:label").build();

		assertTrue(adapter.getSuggestions("Januar").containsKey(IRI.create("http://www.wikidata.org/entity/Q108")));
	}

}
