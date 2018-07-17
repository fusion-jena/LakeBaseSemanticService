package de.uni_jena.cs.fusion.worms.client;

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
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.junit.Test;

public class WormsClientTest {

	private final static WormsClient client = new WormsClient();

	@Test
	public void aphiaAttributeKeysById() throws WormsClientException {
		assertTrue(client.aphiaAttributeKeysById(23, false).stream()
				.filter(x -> x.measurementTypeId == 23 && x.categoryId == 13).findAny().isPresent());
	}

	@Test
	public void aphiaAttributesByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaAttributesByAphiaId(127160, false).stream()
				.filter(x -> x.aphiaId == 127160 && x.measurementTypeId == 23).findAny().isPresent());
	}

	@Test
	public void aphiaAttributeValuesByCategoryId() throws WormsClientException {
		assertTrue(client.aphiaAttributeValuesByCategoryId(13).stream()
				.filter(x -> x.measurementValueId == 267 && x.measurementValue.equals("Habitats Directive")).findAny()
				.isPresent());
	}

	@Test
	public void aphiaChildrenByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaChildrenByAphiaId(1, false).stream().filter(x -> x.aphiaId == 2).findAny().isPresent());
	}

	@Test
	public void aphiaClassificationByAphiaId() throws WormsClientException {
		assertEquals(1, client.aphiaClassificationByAphiaId(2).aphiaId);
	}

	@Test
	public void aphiaDistributionsByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaDistributionsByAphiaId(127160).stream()
				.filter(x -> x.locationID.equals("http://marineregions.org/mrgid/2401")).findAny().isPresent());
	}

	@Test
	public void aphiaExternalIdByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaExternalIdByAphiaId(127160, ExternalIdentifierSource.ncbi).contains("90069"));
	}

	@Test
	public void aphiaIdByName() throws WormsClientException {
		assertEquals(127160, client.aphiaIdByName("Solea solea").intValue());
	}

	@Test
	public void aphiaIdsByAttributeKeyId() throws WormsClientException {
		assertTrue(client
				// use offset to save time
				.aphiaIdsByAttributeKeyId(23, 1).stream()
				.filter(x -> x.attributes.stream().filter(y -> y.measurementTypeId == 23).findAny().isPresent())
				.findAny().isPresent());
	}

	@Test
	public void aphiaNameByAphiaId() throws WormsClientException {
		assertEquals("Solea solea", client.aphiaNameByAphiaId(127160));
	}

	@Test
	public void aphiaRecordByAphiaId() throws WormsClientException {
		assertEquals("Solea solea", client.aphiaRecordByAphiaId(127160).scientificName);
	}

	@Test
	public void aphiaRecordByExternalId() throws WormsClientException {
		assertEquals(127160, client.aphiaRecordByExternalId("90069", ExternalIdentifierSource.ncbi).aphiaId);
	}

	@Test
	public void aphiaRecordsByDate() throws WormsClientException {
		ZonedDateTime dateTime = LocalDateTime.of(2018, 4, 4, 0, 0).atZone(ZoneId.of("UTC"));
		assertTrue(client
				// use offset to save time
				.aphiaRecordsByDate(dateTime.minusDays(1), dateTime, false, 1).stream()
				.filter(x -> x.aphiaId == 1214714).findAny().isPresent());
	}

	@Test
	public void aphiaRecordsByMatchNames() throws WormsClientException {
		assertTrue(client.aphiaRecordsByMatchNames(Arrays.asList("Solea solea", "Solea"), false).stream()
				.filter(x -> Objects.nonNull(x) && x.stream().filter(y -> y.aphiaId == 127160).findAny().isPresent())
				.findAny().isPresent());

		// Issue 1
		assertEquals(2,
				client.aphiaRecordsByNames(Arrays.asList("Aphanizomenon", "something providing no result"), true, false)
						.stream().filter(x -> Objects.nonNull(x)).count());
		// Issue 2
		assertEquals(2,
				client.aphiaRecordsByNames(
						Arrays.asList("something providing no result", "something else providing no result"), true,
						false).size());
	}

	@Test
	public void aphiaRecordsByName() throws WormsClientException {
		assertTrue(client.aphiaRecordsByName("Solea solea", true, false).stream().filter(x -> x.aphiaId == 127160)
				.findAny().isPresent());
	}

	@Test
	public void aphiaRecordsByNames() throws WormsClientException {
		assertTrue(client.aphiaRecordsByNames(Arrays.asList("Solea solea", "Solea"), true, false).stream()
				.filter(x -> Objects.nonNull(x) && x.stream().filter(y -> y.aphiaId == 127160).findAny().isPresent())
				.findAny().isPresent());

		// Issue 1
		assertEquals(2,
				client.aphiaRecordsByNames(Arrays.asList("Aphanizomenon", "something providing no result"), true, false)
						.stream().filter(x -> Objects.nonNull(x)).count());
		// Issue 2
		assertEquals(2,
				client.aphiaRecordsByNames(
						Arrays.asList("something providing no result", "something else providing no result"), true,
						false).size());
	}

	@Test
	public void aphiaRecordsByVernacular() throws WormsClientException {
		assertTrue(client.aphiaRecordsByVernacular("animals", false).stream().filter(x -> x.aphiaId == 2).findAny()
				.isPresent());
		assertTrue(client.aphiaRecordsByVernacular("animal", true).stream().filter(x -> x.aphiaId == 2).findAny()
				.isPresent());
	}

	@Test
	public void aphiaSourcesByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaSourcesByAphiaId(1).stream().filter(x -> x.sourceId == 3).findAny().isPresent());
	}

	@Test
	public void aphiaSynonymsByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaSynonymsByAphiaId(1).isEmpty());
		assertTrue(
				client.aphiaSynonymsByAphiaId(160567).stream().filter(x -> x.aphiaId == 605178).findAny().isPresent());
	}

	@Test
	public void aphiaVernacularsByAphiaId() throws WormsClientException {
		assertTrue(client.aphiaVernacularsByAphiaId(1).isEmpty());
		assertTrue(client.aphiaVernacularsByAphiaId(2).stream().filter(
				x -> x.languageCode.equals("eng") && x.language.equals("English") && x.vernacular.equals("animals"))
				.findAny().isPresent());
	}
}
