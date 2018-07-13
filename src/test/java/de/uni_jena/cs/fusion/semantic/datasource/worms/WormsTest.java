package de.uni_jena.cs.fusion.semantic.datasource.worms;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

/**
 * @author Jan Martin Keil
 *
 */
public class WormsTest {

	static Worms semanticDataSource;

	public static Worms createTestMarinespeciesOrgAdapter() throws Exception {
		return new Worms(true);
	}

	@BeforeClass
	public static void initAdapter() throws Exception {
		WormsTest.semanticDataSource = WormsTest.createTestMarinespeciesOrgAdapter().useExternalIRIs("ncbi");
	}

	@Test
	public void getNamespaces() throws SemanticDataSourceException {
		assertTrue(semanticDataSource.getNamespaces().contains("urn:lsid:marinespecies.org:taxname:"));
	}

	@Test
	public void getLabels() throws SemanticDataSourceException {
		Collection<String> labels;

		labels = semanticDataSource.getLabels(IRI.create("urn:lsid:marinespecies.org:taxname:248099"));
		assertTrue(labels.contains("Aphanizomenon flos-aquae"));

		labels = semanticDataSource.getLabels(IRI.create("urn:lsid:marinespecies.org:taxname:1"));
		assertTrue(labels.contains("Biota"));
	}

	@Test
	public void getAlternativeLabels() throws SemanticDataSourceException {
		Collection<String> labels;

		labels = semanticDataSource.getAlternativeLabels(IRI.create("urn:lsid:marinespecies.org:taxname:248099"));
		assertTrue(labels.contains("knippvattenblom"));

		labels = semanticDataSource.getAlternativeLabels(IRI.create("urn:lsid:marinespecies.org:taxname:1"));
		assertTrue(labels.isEmpty());
	}

	@Test
	public void getAllBroaders() throws SemanticDataSourceException {
		// default case
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:248099"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:160567")));
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:248099"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1")));
		assertEquals(9,
				semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:248099")).size());
		// case broader has an homonym
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:344793"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:154163")));
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:344793"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1")));
		assertEquals(9,
				semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:344793")).size());
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:439481"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:439056")));
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:439481"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1")));
		assertEquals(15,
				semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:439481")).size());
		// case temporary name broaders
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:14780"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:382204")));
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:14780"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1")));
		assertEquals(6,
				semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:14780")).size());
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:382241"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1057430")));
		assertTrue(semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:382241"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1")));
		assertEquals(9,
				semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:382241")).size());
		// case root
		assertEquals(0, semanticDataSource.getAllBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:1")).size());
		// case external
		assertTrue(semanticDataSource.getBroaders(IRI.create("http://purl.obolibrary.org/obo/NCBITaxon_1175"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:146584")));
	}

	@Test
	public void getBroaders() throws SemanticDataSourceException {
		// default case
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:248099"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:160567")));
		assertEquals(1, semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:248099")).size());
		// case broader has an homonym
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:344793"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:154163")));
		assertEquals(1, semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:344793")).size());
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:439481"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:439056")));
		assertEquals(1, semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:439481")).size());
		// case temporary name broaders
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:14780"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:382204")));
		assertEquals(1, semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:14780")).size());
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:382241"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:1057430")));
		assertEquals(1, semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:382241")).size());
		// case root
		assertTrue(semanticDataSource.getBroaders(IRI.create("urn:lsid:marinespecies.org:taxname:1")).isEmpty());
		// case external
		assertTrue(semanticDataSource.getBroaders(IRI.create("http://purl.obolibrary.org/obo/NCBITaxon_1175"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:146584")));
	}

	@Test
	public void getDescriptions() throws SemanticDataSourceException {
		assertTrue(semanticDataSource.getDescriptions(IRI.create("urn:lsid:marinespecies.org:taxname:248099")).stream()
				.filter(x -> x.startsWith("Rank:")).findAny().isPresent());
	}

	@Test
	public void getMachtesSingle() throws SemanticDataSourceException {
		Map<IRI, Double> result = semanticDataSource.getMatches("Aphanizomenon flos-aquae");
		assertTrue(result.containsKey(IRI.create("urn:lsid:marinespecies.org:taxname:248099")));
		assertEquals(1.0, result.get(IRI.create("urn:lsid:marinespecies.org:taxname:248099")), 0.01);
	}

	@Test
	public void getMatchesMulti() throws SemanticDataSourceException {
		List<String> terms = new ArrayList<String>();

		terms.add("Aphanizomenon flexuosum");
		terms.add("Aphanizomenon flos-aquae");
		terms.add("Aphanizomenon gracile");
		terms.add("bla");
		terms.add("blub");
		terms.add("lalala");
		terms.add("Aphanizomenon");
		terms.add("Coelastrum astroideum");
		terms.add("ujhtzzesgfh");
		terms.add("wsefdwsgrfhsd");
		terms.add("gfdghrtjhertgh");
		terms.add("ergergrhdfgd");
		terms.add("gdgdhjdsgfser");
		terms.add("tgrtgergdjhdf");
		terms.add("hsgdshdhsgh");
		terms.add("ghdhfdfhdhd");
		terms.add("uerzwtwet");
		terms.add("dhsdghdhdgs");
		terms.add("gdfgdsghjrtt");
		terms.add("regzewrtgerhget");
		terms.add("hrthrthr");
		terms.add("ehrtrthrth");
		terms.add("rthrth");
		terms.add("rthrhrjkguk");
		terms.add("fhkdkgkh");
		terms.add("hgkhjvk");
		terms.add("tzrtz");
		terms.add("srzhrshtsrth");
		terms.add("dsgdfgdfg");
		terms.add("dfgdgdgd");
		terms.add("gdgasewertznb");
		terms.add("kzgfhgsydvfb");
		terms.add("sdfg");
		terms.add("fghnfdfgbdsrg");
		terms.add("wefsdvs");
		terms.add("erdhbdfbv");
		terms.add("dfgdfgdsgwser");
		terms.add("gerdgbdfgv");
		terms.add("gddfb xd");
		terms.add("fgsdfb yx");
		terms.add("vasdhbfkgh");
		terms.add("mvbnvgncfgdgvxcv yx");
		terms.add("nfn cvbdfgbf");
		terms.add("bvthxfjfyjx");
		terms.add("fgjxrtjer");
		terms.add("rfxjxtjfdtjxftj");
		terms.add("xftjrfxjyj");
		terms.add("yfdtjytjdfytjdtj");
		terms.add("yfjy<rjedjjydj");
		terms.add("drjydrjrdjd");
		terms.add("yrjdyjrdyjdjrydjrjrdyjr");
		terms.add("dfgdgdgdg");

		Map<String, Map<IRI, Double>> result = semanticDataSource.getMatches(terms);

		for (String term : terms) {
			assertTrue(" term \"" + term + "\"", result.containsKey(term));
		}

		assertTrue(result.get("Aphanizomenon flexuosum")
				.containsKey(IRI.create("urn:lsid:marinespecies.org:taxname:612084")));
		assertEquals(1.0,
				result.get("Aphanizomenon flexuosum").get(IRI.create("urn:lsid:marinespecies.org:taxname:612084")),
				0.01);

		assertTrue(result.get("Aphanizomenon gracile")
				.containsKey(IRI.create("urn:lsid:marinespecies.org:taxname:248100")));
		assertEquals(1.0,
				result.get("Aphanizomenon gracile").get(IRI.create("urn:lsid:marinespecies.org:taxname:248100")), 0.01);

		assertTrue(result.get("Aphanizomenon flos-aquae")
				.containsKey(IRI.create("urn:lsid:marinespecies.org:taxname:248099")));
		assertEquals(1.0,
				result.get("Aphanizomenon flos-aquae").get(IRI.create("urn:lsid:marinespecies.org:taxname:248099")),
				0.01);

	}

	@Test
	public void getReplacedBy() throws SemanticDataSourceException {
		assertTrue(semanticDataSource.getReplacedBy(IRI.create("urn:lsid:marinespecies.org:taxname:609460")).isEmpty());
		assertTrue(semanticDataSource.getReplacedBy(IRI.create("urn:lsid:marinespecies.org:taxname:248099"))
				.contains(IRI.create("urn:lsid:marinespecies.org:taxname:609460")));
	}

	@Test
	public void getSuggestionsMulti() throws SemanticDataSourceException {
		Map<String, Map<IRI, String>> result;

		result = semanticDataSource
				.getSuggestions(Lists.newArrayList("yyyyyyyyyyyyyyyyyyyyyy", "zzzzzzzzzzzzzzzzzzzzzz"));
		assertTrue(result.get("yyyyyyyyyyyyyyyyyyyyyy").isEmpty());
		assertTrue(result.get("zzzzzzzzzzzzzzzzzzzzzz").isEmpty());
		result = semanticDataSource.getSuggestions(Lists.newArrayList("Aphanizomenon", "zzzzzzzzzzzzzzzzzzzzzz"));
		assertTrue(!result.isEmpty());
		for (String key : result.get("Aphanizomenon").values()) {
			assertTrue(key, key.startsWith("Aphanizomenon"));
		}
	}

	@Test
	public void getSuggestionsSingle() throws SemanticDataSourceException {
		Map<IRI, String> result;
		result = semanticDataSource.getSuggestions("zzzzzzzzzzzzzzzzzzzzzz");
		assertTrue(result.isEmpty());
		result = semanticDataSource.getSuggestions("Aphanizomenon");
		assertTrue(!result.isEmpty());
		for (String key : result.values()) {
			assertTrue(key, key.startsWith("Aphanizomenon"));
		}
	}

	@Test
	public void getSynonyms() throws SemanticDataSourceException, MalformedURLException {

		semanticDataSource.useExternalIRIs("tsn").useExternalIRIs("ncbi");

		// test no exception (Issue LBDEV-160)
		semanticDataSource.getSynonyms(IRI.create("urn:lsid:marinespecies.org:taxname:9"));
		semanticDataSource.getSynonyms(IRI.create("urn:lsid:marinespecies.org:taxname:147416"));

		// ensure no exceptions for urn:lsid:marinespecies.org:taxname:1
		assertTrue(semanticDataSource.getSynonyms(IRI.create("urn:lsid:marinespecies.org:taxname:1")).isEmpty());

		// test exception
		try {
			// unknown external
			semanticDataSource.getSynonyms(IRI.create("http://purl.obolibrary.org/obo/NCBITaxon_6072"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create("urn:lsid:marinespecies.org:taxname:0"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create(""));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create("a"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create(":"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create("sdfsdf:sdfsfd:sdfsfd:23423"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			semanticDataSource.getSynonyms(IRI.create("http://example.org/some/entity"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}
		try {
			// TODO remote fix required (enable strings as input): remove
			semanticDataSource.getSynonyms(IRI.create("urn:lsid:algaebase.org:taxname:6816"));
			throw new AssertionError("Did not throw expected exception.");
		} catch (Exception e) {
		}

		// define test pairs
		List<Map.Entry<String, String>> pairs = new ArrayList<Map.Entry<String, String>>();
		pairs.add(new AbstractMap.SimpleEntry<String, String>("urn:lsid:marinespecies.org:taxname:160567",
				"urn:lsid:marinespecies.org:taxname:605178"));
		pairs.add(new AbstractMap.SimpleEntry<String, String>("urn:lsid:marinespecies.org:taxname:160567",
				"http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=1191"));
		pairs.add(new AbstractMap.SimpleEntry<String, String>("urn:lsid:marinespecies.org:taxname:160567",
				"http://purl.obolibrary.org/obo/NCBITaxon_1175"));
		// TODO remote fix required (enable strings as input): enable
		// pairs.add(new AbstractMap.SimpleEntry<String,
		// String>("urn:lsid:marinespecies.org:taxname:160567",
		// "urn:lsid:algaebase.org:taxname:6816"));
		// TODO add eol
		// pairs.add(new
		// AbstractMap.SimpleEntry<String,String>("urn:lsid:marinespecies.org:taxname:",
		// "http://www.eol.org/pages/"));
		// TODO add dyntaxa
		// pairs.add(new
		// AbstractMap.SimpleEntry<String,String>("urn:lsid:marinespecies.org:taxname:",
		// "urn:lsid:dyntaxa.se:Taxon:"));
		// TODO add fishbase
		// pairs.add(new
		// AbstractMap.SimpleEntry<String,String>("urn:lsid:marinespecies.org:taxname:",
		// "http://www.fishbase.org/summary/"));
		// TODO add gisd
		// pairs.add(new
		// AbstractMap.SimpleEntry<String,String>("urn:lsid:marinespecies.org:taxname:",
		// "http://www.iucngisd.org/gisd/species.php?sc="));

		// test both directions for test pairs
		for (Entry<String, String> pair : pairs) {
			assertTrue("Missing synonym \"" + pair.getValue() + "\" of \"" + pair.getKey() + "\".",
					semanticDataSource.getSynonyms(IRI.create(pair.getKey())).contains(IRI.create(pair.getValue())));
			assertTrue("Missing synonym \"" + pair.getKey() + "\" of \"" + pair.getValue() + "\".",
					semanticDataSource.getSynonyms(IRI.create(pair.getValue())).contains(IRI.create(pair.getKey())));
		}

	}

	@Test
	public void getUrls() throws SemanticDataSourceException, MalformedURLException {
		assertTrue(semanticDataSource.getUrls(IRI.create("urn:lsid:marinespecies.org:taxname:160567"))
				.contains(new URL("http://www.marinespecies.org/aphia.php?p=taxdetails&id=160567")));
	}

	@Test
	public void isDeprecated() throws SemanticDataSourceException {
		// status accepted
		assertFalse(semanticDataSource.isDeprecated(IRI.create("urn:lsid:marinespecies.org:taxname:609460")));
		// status alternate representation
		assertTrue(semanticDataSource.isDeprecated(IRI.create("urn:lsid:marinespecies.org:taxname:248099")));
	}

	@Test
	public void isPresent() throws SemanticDataSourceException {
		// internal
		assertTrue(semanticDataSource.isPresent(IRI.create("urn:lsid:marinespecies.org:taxname:160567")));
		assertFalse(semanticDataSource.isPresent(IRI.create("urn:lsid:marinespecies.org:taxname:0")));
		assertFalse(semanticDataSource.isPresent(IRI.create("urn:lsid:marinespecies.org:taxname:9999999999999999999")));
		// external
		assertTrue(semanticDataSource.isPresent(IRI.create("http://purl.obolibrary.org/obo/NCBITaxon_1175")));
		assertFalse(semanticDataSource.isPresent(IRI.create("http://purl.obolibrary.org/obo/NCBITaxon_6072")));
	}
}
