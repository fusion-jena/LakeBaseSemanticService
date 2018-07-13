package de.uni_jena.cs.fusion.lakebase;

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

import static org.junit.Assert.assertNull;

import java.util.Collections;

import javax.sql.DataSource;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_jena.cs.fusion.lakebase.model.AnnotationCopyRequest;
import de.uni_jena.cs.fusion.lakebase.model.CompleteRequest;
import de.uni_jena.cs.fusion.lakebase.model.EntitiesAnnotation;
import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.lakebase.model.SearchRequest;
import de.uni_jena.cs.fusion.lakebase.model.SuggestRequest;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.AnnotationCopyWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.CompleteWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.DeleteAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.DescribeWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.GetAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SearchWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.ServiceWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.ServiceWorkerException;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SetAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SuggestAnnotationWorker;
import de.uni_jena.cs.fusion.semantic.datasource.lakebase.ParameterTest;
import de.uni_jena.cs.fusion.semantic.datasource.lakebase.StudySiteTest;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

public class ServletTest {
	private final static ObjectMapper jsonMapper = new ObjectMapper();
	private static Environment environment;
	private final static AnnotationCopyWorker annotationCopyWorker = new AnnotationCopyWorker();
	private final static SuggestAnnotationWorker suggestAnnotationWorker = new SuggestAnnotationWorker();
	private final static DeleteAnnotationWorker deleteAnnotationWorker = new DeleteAnnotationWorker();
	private final static SetAnnotationWorker setAnnotationWorker = new SetAnnotationWorker(false);
	private final static GetAnnotationWorker getAnnotationWorker = new GetAnnotationWorker();
	private final static SearchWorker searchWorker = new SearchWorker();
	private final static CompleteWorker completeWorker = new CompleteWorker();
	private final static DescribeWorker describeWorker = new DescribeWorker();

	@BeforeClass
	public static void initClass() throws Exception {
		DataSource dataSource = DatabaseManagerTest.createTestDatabaseManager();
		StudySiteTest.truncateDataSchema(dataSource);
		StudySiteTest.prepareStudySiteData(dataSource);
		ParameterTest.prepareParameterData(dataSource);
		// use single thread executor
		environment = new Environment(dataSource);
	}

	@AfterClass
	public static void releaseClass() throws Exception {
		environment.close();
	}

	@Test
	public void annotationSuggest() throws JsonProcessingException, ServiceWorkerException, JSONException {
		Object input;
		String expected;

		input = new SuggestRequest("meter", Collections.singleton(Scope.all));
		expected = "[{\"term\":\"meter\",\"annotations\":[{\"iri\":\"http://purl.obolibrary.org/obo/ENVO_01001008\",\"rank\":0.9666666666666667,\"label\":\"meteor\"},{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/metre\",\"rank\":0.9533333333333334,\"label\":\"metre\"}]}]";
		JSONAssert.assertEquals(expected, outputString(suggestAnnotationWorker, input), JSONCompareMode.LENIENT);

		input = new SuggestRequest("litre mililiter lililiter kilometer per houre pictometere",
				Collections.singleton(Scope.unit));
		expected = "[{\"term\":\"kilometer per houre\",\"annotations\":[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/kilometrePerHour\",\"rank\":0.9783625730994152,\"label\":\"kilometre per hour\"}]},{\"term\":\"kilometer\",\"annotations\":[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/kilometre\",\"rank\":0.9777777777777777,\"label\":\"kilometre\"}]},{\"term\":\"litre\",\"annotations\":[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/litre\",\"rank\":1.0,\"label\":\"litre\"}]},{\"term\":\"houre\",\"annotations\":[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/hour\",\"rank\":0.96,\"label\":\"hour\"}]}]";
		JSONAssert.assertEquals(expected, outputString(suggestAnnotationWorker, input), JSONCompareMode.LENIENT);

	}

	@Test
	public void annotationSetGetCopySearchDelete() throws JsonProcessingException, ServiceWorkerException,
			InterruptedException, MaintenanceException, JSONException {
		Object input;
		String expected;

		input = Lists.newArrayList(
				new EntitiesAnnotation("2011-12-03T10:15:30+02:00", Collections.singleton(Scope.all),
						Collections.singletonList(new Entity(1L, 1L, 1L, null, null)), Collections.emptyList(),
						Collections.emptyList()),
				new EntitiesAnnotation("metre", Collections.singleton(Scope.all),
						Collections.singletonList(new Entity(1L, 1L, 2L, null, null)), Collections.emptyList(),
						Collections.emptyList()),
				new EntitiesAnnotation("meter", Collections.singleton(Scope.all),
						Collections.singletonList(new Entity(1L, 1L, 3L, null, null)), Collections.emptyList(),
						Collections.emptyList()))
				.toArray(new EntitiesAnnotation[0]);
		assertNull(outputString(setAnnotationWorker, input));

		input = Lists.newArrayList(new Entity(1L, 1L, 1L, null, null), new Entity(1L, 1L, 2L, null, null),
				new Entity(1L, 1L, 3L, null, null)).toArray(new Entity[0]);
		expected = "[{\"entities\":[{\"package\":1,\"object\":1,\"column\":1}],\"accepted\":[{\"term\":\"2011-12-03T10:15:30+02:00\",\"iri\":\"datetime:dt2011-12-03T10:15:30+02:00\",\"label\":\"2011-12-03T10:15:30+02:00\"}],\"rejected\":[]},{\"entities\":[{\"package\":1,\"object\":1,\"column\":2}],\"accepted\":[{\"term\":\"metre\",\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/metre\",\"label\":\"metre\"}],\"rejected\":[]},{\"entities\":[{\"package\":1,\"object\":1,\"column\":3}],\"accepted\":[{\"term\":\"meter\",\"iri\":\"http://purl.obolibrary.org/obo/ENVO_01001008\",\"label\":\"meteor\"}],\"rejected\":[]}]";
		JSONAssert.assertEquals(expected, outputString(getAnnotationWorker, input), JSONCompareMode.LENIENT);

		// call index refresh
		environment.getHierarchyManager().maintain();

		input = new SearchRequest(
				new EntitiesAnnotation("meter", null, null, Collections.emptyList(), Collections.emptyList()),
				new EntitiesAnnotation("", null, null, Collections.emptyList(), Collections.emptyList()), null);
		expected = "[{\"entity\":{\"package\":1},\"rank\":2.0}]";
		JSONAssert.assertEquals(expected, outputString(searchWorker, input), JSONCompareMode.LENIENT);

		input = new AnnotationCopyRequest(new Entity(1L, null, null, null, null),
				new Entity(2L, null, null, null, null));
		assertNull(outputString(annotationCopyWorker, input));

		input = Lists.newArrayList(new Entity(2L, 1L, 1L, null, null), new Entity(2L, 1L, 2L, null, null),
				new Entity(2L, 1L, 3L, null, null)).toArray(new Entity[0]);
		expected = "[{\"entities\":[{\"package\":2,\"object\":1,\"column\":1}],\"accepted\":[{\"term\":\"2011-12-03T10:15:30+02:00\",\"iri\":\"datetime:dt2011-12-03T10:15:30+02:00\",\"label\":\"2011-12-03T10:15:30+02:00\"}],\"rejected\":[]},{\"entities\":[{\"package\":2,\"object\":1,\"column\":2}],\"accepted\":[{\"term\":\"metre\",\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/metre\",\"label\":\"metre\"}],\"rejected\":[]},{\"entities\":[{\"package\":2,\"object\":1,\"column\":3}],\"accepted\":[{\"term\":\"meter\",\"iri\":\"http://purl.obolibrary.org/obo/ENVO_01001008\",\"label\":\"meteor\"}],\"rejected\":[]}]";
		JSONAssert.assertEquals(expected, outputString(getAnnotationWorker, input), JSONCompareMode.LENIENT);

		input = Lists.newArrayList(new Entity(1L, 1L, 1L, null, null), new Entity(1L, 1L, 2L, null, null),
				new Entity(1L, 1L, 3L, null, null)).toArray(new Entity[0]);
		assertNull(outputString(deleteAnnotationWorker, input));

		input = Lists.newArrayList(new Entity(1L, 1L, 1L, null, null), new Entity(1L, 1L, 2L, null, null),
				new Entity(1L, 1L, 3L, null, null)).toArray(new Entity[0]);
		expected = "[{\"entities\":[{\"package\":1,\"object\":1,\"column\":1}],\"accepted\":[],\"rejected\":[]},{\"entities\":[{\"package\":1,\"object\":1,\"column\":2}],\"accepted\":[],\"rejected\":[]},{\"entities\":[{\"package\":1,\"object\":1,\"column\":3}],\"accepted\":[],\"rejected\":[]}]";
		JSONAssert.assertEquals(expected, outputString(getAnnotationWorker, input), JSONCompareMode.LENIENT);

	}

	@Test
	public void complete() throws JsonProcessingException, ServiceWorkerException, JSONException {
		Object input;
		String expected;

		input = new CompleteRequest("me", Collections.singleton(Scope.all));
		expected = "[]";
		JSONAssert.assertEquals(expected, outputString(completeWorker, input), JSONCompareMode.LENIENT);

		input = new CompleteRequest("millimetre per", Collections.singleton(Scope.all));
		expected = "[{\"stump\":\"millimetre per\",\"completions\":[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/millimetrePerHour\",\"label\":\"millimetre per hour\"},{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/millimetrePerSecond-Time\",\"label\":\"millimetre per second\"},{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/millimetrePerSecond-TimeSquared\",\"label\":\"millimetre per second squared\"},{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/millimetrePerDay\",\"label\":\"millimetre per day\"}]}]";
		JSONAssert.assertEquals(expected, outputString(completeWorker, input), JSONCompareMode.LENIENT);

	}

	@Test
	public void describe() throws JsonProcessingException, ServiceWorkerException, JSONException {
		Object input;
		String expected;

		input = Lists.newArrayList(IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/metre"),
				IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/hectare"),
				IRI.create("http://purl.obolibrary.org/obo/ENVO_01000596")).toArray(new IRI[0]);
		expected = "[{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/metre\",\"labels\":[\"metre\"],\"alternativLabels\":[\"meter\",\"m\"],\"descriptions\":[\"The metre is a unit of length defined as the length of the path travelled by light in vacuum during a time interval of 1/299 792 458 of a second.\"],\"urls\":[],\"types\":[\"unit\"],\"synonyms\":[],\"broaders\":[\"http://www.w3.org/2002/07/owl#Thing\",\"http://www.ontology-of-units-of-measure.org/resource/om-2/SingularUnit\",\"http://www.ontology-of-units-of-measure.org/resource/om-2/Unit\"]},{\"iri\":\"http://www.ontology-of-units-of-measure.org/resource/om-2/hectare\",\"labels\":[\"hectare\"],\"alternativLabels\":[\"ha\"],\"descriptions\":[\"The hectare is a unit of area defined as 1.0e2 are.\"],\"urls\":[],\"types\":[\"unit\"],\"synonyms\":[],\"broaders\":[\"http://www.w3.org/2002/07/owl#Thing\",\"http://www.ontology-of-units-of-measure.org/resource/om-2/PrefixedUnit\",\"http://www.ontology-of-units-of-measure.org/resource/om-2/Unit\"]},{\"iri\":\"http://purl.obolibrary.org/obo/ENVO_01000596\",\"labels\":[\"clock\"],\"alternativLabels\":[],\"descriptions\":[\"A clock is an instrument which may indicate, keep, and/or co-ordinate time.\"],\"urls\":[\"http://purl.obolibrary.org/obo/ENVO_01000596\"],\"types\":[],\"synonyms\":[],\"broaders\":[\"http://www.w3.org/2002/07/owl#Thing\",\"http://purl.obolibrary.org/obo/BFO_0000001\",\"http://purl.obolibrary.org/obo/BFO_0000002\",\"http://purl.obolibrary.org/obo/BFO_0000004\",\"http://purl.obolibrary.org/obo/BFO_0000030\",\"http://purl.obolibrary.org/obo/BFO_0000040\",\"http://purl.obolibrary.org/obo/ENVO_00002297\",\"http://purl.obolibrary.org/obo/ENVO_00002004\",\"http://purl.obolibrary.org/obo/ENVO_00003075\",\"http://purl.obolibrary.org/obo/ENVO_01000010\",\"http://purl.obolibrary.org/obo/ENVO_00003074\"]}]";
		JSONAssert.assertEquals(expected, outputString(describeWorker, input), JSONCompareMode.LENIENT);
	}

	private String outputString(ServiceWorker worker, Object input)
			throws ServiceWorkerException, JsonProcessingException {
		Object output = worker.processRequest(environment, input);
		if (output == null) {
			return null;
		} else if (output instanceof String) {
			return (String) output;
		} else {
			return jsonMapper.writeValueAsString(output);
		}
	}
}
