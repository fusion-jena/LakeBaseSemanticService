package de.uni_jena.cs.fusion.semantic.datasource.datetime;

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

import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Lists;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class DateTimeDataSourceTest {

	private final static DateTimeDataSource semanticDataSource = new DateTimeDataSource();
	private static String namespace;

	private static Map<String, String> standardLabelEntityMap = new HashMap<String, String>();
	private static Map<String, String> matchingLabelEntityMap = new HashMap<String, String>();

	@BeforeClass
	public static void beforeClass() throws Exception {
		namespace = semanticDataSource.getNamespaces().iterator().next();

		// 'd'dd
		standardLabelEntityMap.put("d03", "d03");
		// 'd'ddZZZZZ
		standardLabelEntityMap.put("d03+01:00", "d03+01:00");
		// 'D'DDD
		standardLabelEntityMap.put("D003", "D003");
		// 'D'DDDZZZZZ
		standardLabelEntityMap.put("D003+01:00", "D003+01:00");
		// 'e'e
		standardLabelEntityMap.put("e3", "e3");
		// 'e'eZZZZZ
		standardLabelEntityMap.put("e3+01:00", "e3+01:00");
		// 'H'HH
		standardLabelEntityMap.put("H10", "H10");
		// 'H'HHZZZZZ
		standardLabelEntityMap.put("H10+01:00", "H10+01:00");
		// 'M'MM
		standardLabelEntityMap.put("M12", "M12");
		// 'M'MMZZZZZ
		standardLabelEntityMap.put("M12+01:00", "M12+01:00");
		// 'W'ww
		standardLabelEntityMap.put("W48", "W48");
		// 'W'wwZZZZZ
		standardLabelEntityMap.put("W48+01:00", "W48+01:00");
		// 'W'ww-e
		standardLabelEntityMap.put("W48-3", "W48-3");
		// 'W'ww-eZZZZZ
		standardLabelEntityMap.put("W48-3+01:00", "W48-3+01:00");
		// HH:mm
		standardLabelEntityMap.put("10:15", "10:15");
		// HH:mmZZZZZ
		standardLabelEntityMap.put("10:15+01:00", "10:15+01:00");
		// HH:mm:ss
		standardLabelEntityMap.put("10:15:30", "10:15:30");
		// HH:mm:ssZZZZZ
		standardLabelEntityMap.put("10:15:30+01:00", "10:15:30+01:00");
		// MM-dd
		standardLabelEntityMap.put("12-03", "12-03");
		// MM-ddZZZZZ
		standardLabelEntityMap.put("12-03+01:00", "12-03+01:00");
		// uuuu
		standardLabelEntityMap.put("2011", "2011");
		standardLabelEntityMap.put("-2011", "-2011");
		// uuuuZZZZZ
		standardLabelEntityMap.put("2011+01:00", "2011+01:00");
		standardLabelEntityMap.put("-2011+01:00", "-2011+01:00");
		// uuuu-'W'ww
		standardLabelEntityMap.put("2011-W48", "2011-W48");
		standardLabelEntityMap.put("-2011-W48", "-2011-W48");
		// uuuu-'W'wwZZZZZ
		standardLabelEntityMap.put("2011-W48+01:00", "2011-W48+01:00");
		standardLabelEntityMap.put("-2011-W48+01:00", "-2011-W48+01:00");
		// uuuu-MM
		standardLabelEntityMap.put("2011-12", "2011-12");
		standardLabelEntityMap.put("-2011-12", "-2011-12");
		// uuuu-MMZZZZZ
		standardLabelEntityMap.put("2011-12+01:00", "2011-12+01:00");
		standardLabelEntityMap.put("-2011-12+01:00", "-2011-12+01:00");
		// uuuu-MM-dd
		standardLabelEntityMap.put("2011-12-03", "2011-12-03");
		standardLabelEntityMap.put("-2011-12-03", "-2011-12-03");
		// uuuu-MM-ddZZZZZ
		standardLabelEntityMap.put("2011-12-03+01:00", "2011-12-03+01:00");
		standardLabelEntityMap.put("-2011-12-03+01:00", "-2011-12-03+01:00");
		// uuuu-MM-dd'T'HH
		standardLabelEntityMap.put("2011-12-03T10", "2011-12-03T10");
		standardLabelEntityMap.put("-2011-12-03T10", "-2011-12-03T10");
		// uuuu-MM-dd'T'HHZZZZZ
		standardLabelEntityMap.put("2011-12-03T10+01:00", "2011-12-03T10+01:00");
		standardLabelEntityMap.put("-2011-12-03T10+01:00", "-2011-12-03T10+01:00");
		// uuuu-MM-dd'T'HH:mm
		standardLabelEntityMap.put("2011-12-03T10:15", "2011-12-03T10:15");
		standardLabelEntityMap.put("-2011-12-03T10:15", "-2011-12-03T10:15");
		// uuuu-MM-dd'T'HH:mmZZZZZ
		standardLabelEntityMap.put("2011-12-03T10:15+01:00", "2011-12-03T10:15+01:00");
		standardLabelEntityMap.put("-2011-12-03T10:15+01:00", "-2011-12-03T10:15+01:00");
		// uuuu-MM-dd'T'HH:mm:ss
		standardLabelEntityMap.put("2011-12-03T10:15:30", "2011-12-03T10:15:30");
		standardLabelEntityMap.put("-2011-12-03T10:15:30", "-2011-12-03T10:15:30");
		// uuuu-MM-dd'T'HH:mm:ssZZZZZ
		standardLabelEntityMap.put("2011-12-03T10:15:30+01:00", "2011-12-03T10:15:30+01:00");
		standardLabelEntityMap.put("-2011-12-03T10:15:30+01:00", "-2011-12-03T10:15:30+01:00");

		matchingLabelEntityMap.put("2011-007"/* uuuu-DDD */, "2011-01-07");
		matchingLabelEntityMap.put("-2011-01-07"/* uuuu-DDD */, "-2011-01-07");
		matchingLabelEntityMap.put("2011-W48-3"/* YYYY-'W'ww-e */, "2011-11-22");
		matchingLabelEntityMap.put("-2011-W48-3"/* YYYY-'W'ww-e */, "-2011-11-28");

	}

	@Test
	public void getAllBroaders() throws SemanticDataSourceException {
		Collection<IRI> broaders = semanticDataSource
				.getAllBroaders(IRI.create(namespace + "2011-12-03T10:15:30+01:00"));

		Collection<String> expectedBroaders = Lists.newArrayList("W49", "W49-7", "W49-7+01:00", "W49+01:00", "M12",
				"M12+01:00", "e7", "e7+01:00", "2011-W49+01:00", "2011-W49", "2011", "2011+01:00", "2011-12+01:00",
				"2011-12-03+01:00", "2011-12-03", "2011-12-03T10+01:00", "2011-12-03T10:15+01:00",
				"2011-12-03T10:15:30", "2011-12-03T10:15", "2011-12-03T10", "2011-12", "12-03", "12-03+01:00", "H10",
				"10:15:30+01:00", "10:15", "10:15:30", "10:15+01:00", "H10+01:00", "d03", "d03+01:00", "D337+01:00",
				"D337");

		for (String expectedBroader : expectedBroaders) {

			assertTrue("Missing \"" + expectedBroader + "\"",
					broaders.contains(IRI.create(namespace + expectedBroader)));

			for (IRI broader : broaders) {
				// ensure no error on parsing returned IRIs
				semanticDataSource.getAllBroaders(broader);
			}
		}

		for (IRI broader : broaders) {

			assertTrue("Unexpected \"" + broader.getIRIString().substring(namespace.length()) + "\"",
					expectedBroaders.contains(broader.getIRIString().substring(namespace.length())));

			for (String expectedBroader : expectedBroaders) {
				// ensure no error on parsing returned IRIs
				semanticDataSource.getAllBroaders(IRI.create(namespace + expectedBroader));
			}
		}

		// "http://fusion.cs.uni-jena.de/ontologies/lakebase/Morning",
		// "http://fusion.cs.uni-jena.de/ontologies/lakebase/2010s",
		// "http://fusion.cs.uni-jena.de/ontologies/lakebase/21th_century",
		// "http://fusion.cs.uni-jena.de/ontologies/lakebase/3rd_millennium",

		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "3rd_millennium")));
		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "21th_century")));
		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "2010s")));
		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "December")));
		// assertTrue(broaders.contains(IRI.create("http://www.w3.org/2006/time#Saturday")));
		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "Morning")));
		// assertTrue(broaders.contains(IRI.create(ontologyNamespace +
		// "10AM")));
	}

	@Test
	public void getLabel() throws Exception {
		for (Entry<String, String> labelEntityEntry : standardLabelEntityMap.entrySet()) {
			IRI iri = IRI.create(namespace + labelEntityEntry.getKey());
			try {
				Collection<String> result = semanticDataSource.getLabels(iri);
				assertEquals("For \"" + iri + "\"", 1, result.size());
				assertEquals("For \"" + iri + "\"", labelEntityEntry.getValue(), result.iterator().next());
			} catch (Exception t) {
				throw new Exception("For \"" + iri + "\"", t);
			}
		}
	}

	@Test
	public void getMatches() throws Exception {
		for (Entry<String, String> labelEntityEntry : standardLabelEntityMap.entrySet()) {
			try {
				Collection<IRI> result = semanticDataSource.getMatches(labelEntityEntry.getKey()).keySet();
				assertEquals("For \"" + labelEntityEntry.getKey() + "\"", 1, result.size());
				assertEquals("For \"" + labelEntityEntry.getKey() + "\"", labelEntityEntry.getKey(),
						result.iterator().next().getIRIString().substring(namespace.length()));
			} catch (Exception t) {
				throw new Exception("For \"" + labelEntityEntry.getKey() + "\"", t);
			}
		}
		for (Entry<String, String> labelEntityEntry : matchingLabelEntityMap.entrySet()) {
			try {
				Collection<IRI> result = semanticDataSource.getMatches(labelEntityEntry.getKey()).keySet();
				assertEquals("For \"" + labelEntityEntry.getKey() + "\"", 1, result.size());
				assertEquals("For \"" + labelEntityEntry.getKey() + "\"", labelEntityEntry.getValue(),
						result.iterator().next().getIRIString().substring(namespace.length()));
			} catch (Exception t) {
				throw new Exception("For \"" + labelEntityEntry.getKey() + "\"", t);
			}
		}
	}

	public static void assertDate(TemporalAccessor temporal, Long year, Long month, Long day, Long offsetSec) {
		assertDateTime(temporal, year, month, day, null, null, null, offsetSec);
	}

	public static void assertTime(TemporalAccessor temporal, Long hour, Long minute, Long second, Long offsetSec) {
		assertDateTime(temporal, null, null, null, hour, minute, second, offsetSec);
	}

	public static void assertWeek(TemporalAccessor temporal, Long year, Long week) {
		assertTemporalField(year, temporal, IsoFields.WEEK_BASED_YEAR);
		assertTemporalField(week, temporal, IsoFields.WEEK_OF_WEEK_BASED_YEAR);
	}

	public static void assertDateTime(TemporalAccessor temporal, Long year, Long month, Long day, Long hour,
			Long minute, Long second, Long offsetSec) {
		assertTemporalField(year, temporal, ChronoField.YEAR);
		assertTemporalField(month, temporal, ChronoField.MONTH_OF_YEAR);
		assertTemporalField(day, temporal, ChronoField.DAY_OF_MONTH);
		assertTemporalField(hour, temporal, ChronoField.HOUR_OF_DAY);
		assertTemporalField(minute, temporal, ChronoField.MINUTE_OF_HOUR);
		assertTemporalField(second, temporal, ChronoField.SECOND_OF_MINUTE);
		assertTemporalField(offsetSec, temporal, ChronoField.OFFSET_SECONDS);
	}

	public static void assertTemporalField(Long expected, TemporalAccessor temporal, TemporalField field) {
		try {
			long actual = temporal.getLong(field);
			assertEquals(expected.longValue(), actual);
		} catch (UnsupportedTemporalTypeException e) {
			if (expected != null) {
				throw new AssertionError("expected <" + expected + ">, but was not supported");
			}
		}
	}
}
