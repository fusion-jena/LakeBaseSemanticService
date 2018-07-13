package de.uni_jena.cs.fusion.semantic.datasource.gfbio;

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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

/**
 * 
 * @since 0.1
 *
 */
public class TerminologyServerDataSourceTest {

	private static final double epsilon = 0.001;
	static TerminologyServerDataSource bco;
	static TerminologyServerDataSource chebi;
	static TerminologyServerDataSource envo;
	static TerminologyServerDataSource worms;
	static TerminologyServerDataSource geonames;

	@BeforeClass
	public static void init() throws SemanticDataSourceException {
		bco = new TerminologyServerDataSource("BCO");
		chebi = new TerminologyServerDataSource("CHEBI");
		envo = new TerminologyServerDataSource("ENVO");
		worms = new TerminologyServerDataSource("WORMS");
		geonames = new TerminologyServerDataSource("GEONAMES");
	}

	@Test
	public void getAdapterUID() throws Exception {
		assertNotEquals(bco.getUID(), chebi.getUID());
	}

	@Test
	public void getAll() throws Exception {
		assertTrue(bco.getSignature().size() > 100);
	}

	@Test
	public void getAllBroaders() throws Exception {
		assertFalse(envo.getAllBroaders(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")).isEmpty());

		geonames.getAllBroaders(IRI.create("http://sws.geonames.org/5889167/"));
		// TODO hierarchy case
		// TODO fall back case
	}

	@Test
	public void getAllNarrowers() throws Exception {
		assertFalse(envo.getAllNarrowers(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")).isEmpty());
		// TODO fall back case
	}

	public void getBroaders() throws Exception {
		assertFalse(envo.getBroaders(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")).isEmpty());
		// TODO fall back case
	}

	@Test
	public void getLabels() throws Exception {
		Collection<String> labels;

		// case term has a single label
		// http://terminologies.gfbio.org/api/terminologies/BCO/term?uri=http://purl.obolibrary.org/obo/IAO_0000224
		labels = bco.getLabels(IRI.create("http://purl.obolibrary.org/obo/IAO_0000224"));
		assertEquals(1, labels.size());
		assertTrue(labels.contains("core"));

		assertTrue(geonames.getLabels(IRI.create("http://sws.geonames.org/5889167/")).contains("Baden"));

		// case term has multiple label
		// http://terminologies.gfbio.org/api/terminologies/BCO/term?uri=http://purl.obolibrary.org/obo/IAO_0000120
		/*
		 * find new testcase
		 *
		 * labels = bco.getConceptLabels(IRI.create(
		 * "http://purl.obolibrary.org/obo/IAO_0000120")); assertEquals(2,
		 * labels.size()); assertTrue(labels.contains("metadata complete"));
		 * assertTrue(labels.contains("FossilSpecimen"));
		 */
	}

	@Test
	public void getNamespaces() throws Exception {
		assertTrue(worms.getNamespaces().contains("urn:lsid:marinespecies.org:taxname:"));
		assertTrue(geonames.getNamespaces().contains("http://sws.geonames.org/"));
		// TODO remote fix required: activate
		// assertTrue(envo.getNamespaces().contains("http://purl.obolibrary.org/obo/ENVO"));
		// TODO add other terminologies
	}

	public void getNarrowers() throws Exception {
		assertFalse(envo.getNarrowers(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")).isEmpty());
		// TODO fall back case
	}

	@Test
	public void getScope() throws Exception {
		assertTrue(worms.getScopes().contains(IRI.create("http://terminologies.gfbio.org/terms/ontology/Biology")));
		assertTrue(
				envo.getScopes().contains(IRI.create("http://terminologies.gfbio.org/terms/ontology#Environmental")));
	}

	@Test
	public void getUrls() throws Exception {
		assertTrue(bco.getUrls(IRI.create("http://purl.obolibrary.org/obo/IAO_0000120"))
				.contains(new URL("http://purl.obolibrary.org/obo/IAO_0000120")));
		// TODO remote fix required: activate
		// assertTrue(worms.getConceptUrls(IRI.create("urn:lsid:marinespecies.org:taxname:1"))
		// .contains(new
		// URL("http://marinespecies.org/aphia.php?p=taxdetails&id=1")));
	}

	@Test
	public void initTerminologyAgain() throws SemanticDataSourceException {
		// http://terminologies.gfbio.org/api/terminologies/BCO/capabilities
		bco = new TerminologyServerDataSource("BCO");
	}

	@Test
	public void invalidInitialization() throws SemanticDataSourceException {
		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Handler testHandler = new StreamHandler(logStream, new SimpleFormatter());
		Logger logger = Logger.getLogger("de.uni_jena.cs.fusion.semantic.datasource.gfbio.TerminologyServerDataSource");
		Handler[] handlers = logger.getHandlers();
		boolean useParentHandlers = logger.getUseParentHandlers();
		logger.setUseParentHandlers(false);
		for (Handler handler : handlers) {
			logger.removeHandler(handler);
		}
		logger.addHandler(testHandler);
		try {
			// initialization of a non existing terminology
			new TerminologyServerDataSource("NOT-EXISTING-TERMINOLOGY");

		} finally {
			logger.removeHandler(testHandler);
			logger.setUseParentHandlers(useParentHandlers);
			for (Handler handler : handlers) {
				logger.addHandler(handler);
			}
		}
		testHandler.flush();
		assertTrue(logStream.toString().contains("Failed to load terminology \"NOT-EXISTING-TERMINOLOGY\"."));
	}

	@Test
	public void isPresent() throws Exception {
		assertTrue(envo.isPresent(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")));
	}

	@Test
	public void match() throws SemanticDataSourceException {
		// http://terminologies.gfbio.org/api/terminologies/search?query=water&terminologies=ENVO,CHEBI&match_type=included
		assertTrue(chebi.getMatches("water").containsKey(IRI.create("http://purl.obolibrary.org/obo/CHEBI_15377")));
		assertEquals(1.0, chebi.getMatches("water").get(IRI.create("http://purl.obolibrary.org/obo/CHEBI_15377")), epsilon);
		assertTrue(envo.getMatches("water").containsKey(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")));
		assertEquals(1.0, envo.getMatches("water").get(IRI.create("http://purl.obolibrary.org/obo/ENVO_00002006")), epsilon);
		assertTrue(geonames.getMatches("Berlin").containsKey(IRI.create("http://sws.geonames.org/2950159/")));
		assertEquals(1.0, geonames.getMatches("Berlin").get(IRI.create("http://sws.geonames.org/2950159/")), epsilon);
		
	}

	@Test
	public void suggest() throws SemanticDataSourceException {
		Map<IRI, String> suggestion = envo.getSuggestions("prov");
		assertFalse(suggestion.containsKey(IRI.create("http://purl.bioontology.org/ontology/NCBITAXON/263136")));
		assertTrue(suggestion.containsKey(IRI.create("http://purl.obolibrary.org/obo/RO_0002469")));
	}

}
