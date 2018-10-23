package de.uni_jena.cs.fusion.gfbio.terminologyserver.client;

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

import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.Test;

public class TerminologyServerClientTest {

	private final static TerminologyServerClient client = new TerminologyServerClient(
			"https://terminologies.gfbio.org/api/terminologies/");

	@Test
	public void allBroader() throws TerminologyServerClientException {
		assertTrue(client.allBroader("ENVO", "http://purl.obolibrary.org/obo/ENVO_00000020").results.stream()
				.filter(b -> b.uri.equals("http://purl.obolibrary.org/obo/BFO_0000001")).findAny().isPresent());
	}

	@Test
	public void allNarrower() throws TerminologyServerClientException {
		assertTrue(client.allNarrower("ENVO", "http://purl.obolibrary.org/obo/ENVO_00000020").results.stream()
				.filter(b -> b.uri.equals("http://purl.obolibrary.org/obo/ENVO_00000487")).findAny().isPresent());
	}

	@Test
	public void allTerms() throws TerminologyServerClientException {
		assertTrue(client.allTerms("KINGDOM").results.stream().filter(
				t -> t.uri.equals("http://terminologies.gfbio.org/terms/KINGDOM/Animalia") & t.label.equals("Animals"))
				.findAny().isPresent());
	}

	@Test
	public void broader() throws TerminologyServerClientException {
		assertTrue(client.allBroader("ENVO", "http://purl.obolibrary.org/obo/BFO_0000002").results.stream()
				.filter(b -> b.uri.equals("http://purl.obolibrary.org/obo/BFO_0000001")).findAny().isPresent());
	}

	@Test
	public void capabilities() throws TerminologyServerClientException {
		for (TerminologyInformation t : client.terminologies().results) {
			TerminologyCapabilitiesResult capabilities = client.capabilities(t.acronym);
			assertTrue(!capabilities.results.iterator().next().availableServices.isEmpty());
			assertTrue(!capabilities.results.iterator().next().searchModes.isEmpty());
		}
	}

	@Test
	public void hierarchy() throws TerminologyServerClientException {
		assertTrue(
				client.hierarchy("ENVO", "http://purl.obolibrary.org/obo/BFO_0000040").results.stream()
						.filter(h -> h.uri.equals("http://purl.obolibrary.org/obo/BFO_0000004")
								&& h.hierarchy.contains("http://purl.obolibrary.org/obo/BFO_0000002"))
						.findAny().isPresent());
	}

	@Test
	public void metadata() throws TerminologyServerClientException {
		assertTrue(client.metadata("ENVO").results.stream().filter(
				t -> t.get("versionIRI").iterator().next().startsWith("http://purl.obolibrary.org/obo/envo/releases/"))
				.findAny().isPresent());
		for (TerminologyInformation t : client.terminologies().results) {
			client.metadata(t.acronym);
		}
	}

	@Test
	public void metrics() throws TerminologyServerClientException {
		assertTrue(client.metrics("ENVO").results.stream().filter(t -> Objects.nonNull(t.numClasses)).findAny()
				.isPresent());
		for (TerminologyInformation t : client.terminologies().results) {
			client.metrics(t.acronym);
		}
	}

	@Test
	public void narrower() throws TerminologyServerClientException {
		assertTrue(client.allNarrower("ENVO", "http://purl.obolibrary.org/obo/ENVO_00000020").results.stream()
				.filter(t -> t.uri.equals("http://purl.obolibrary.org/obo/ENVO_00000488")).findAny().isPresent());
	}

	@Test
	public void search() throws TerminologyServerClientException {
		assertTrue(client.search("Acetobacter aceti").results.stream()
				.filter(t -> t.uri.equals("http://purl.obolibrary.org/obo/NCBITaxon_435")).findAny().isPresent());
	}

	@Test
	public void suggest() throws TerminologyServerClientException {
		assertTrue(client.suggest("Acetobacter aceti").results.stream()
				.filter(t -> t.uri.equals("http://purl.obolibrary.org/obo/NCBITaxon_663932")).findAny().isPresent());
	}

	@Test
	public void synonyms() throws TerminologyServerClientException {
		assertTrue(client.synonyms("NCBITAXON", "http://purl.obolibrary.org/obo/NCBITaxon_45372").results.stream()
				.filter(t -> t.synonyms.contains("Edeltanne") && t.synonyms.contains("silver fir")).findAny()
				.isPresent());
	}

	@Test
	public void term() throws TerminologyServerClientException {
		assertTrue(client.term("NCBITAXON", "http://purl.obolibrary.org/obo/NCBITaxon_45372").results.stream()
				.filter(t -> t.get("uri").contains("http://purl.obolibrary.org/obo/NCBITaxon_45372")
						&& t.get("label").contains("Abies alba"))
				.findAny().isPresent());
	}

	@Test
	public void terminologies() throws TerminologyServerClientException {
		assertTrue(client.terminologies().results.stream().filter(ti -> ti.acronym.equals("ENVO"))
				.filter(t -> Objects.nonNull(t.name)).filter(t -> Objects.nonNull(t.description))
				.filter(t -> Objects.nonNull(t.uri)).findAny().isPresent());
	}

	@Test
	public void terminology() throws TerminologyServerClientException {
		assertTrue(client.terminology("ENVO").results.stream().filter(t -> t.containsKey("acronym"))
				.filter(t -> t.containsKey("creationDate")).filter(t -> t.containsKey("description"))
				.filter(t -> t.containsKey("hasDlExpressivity")).filter(t -> t.containsKey("hasDomain"))
				.filter(t -> t.containsKey("hasOntologyLanguage")).filter(t -> t.containsKey("name"))
				.filter(t -> t.containsKey("uri")).findAny().isPresent());
	}

}
