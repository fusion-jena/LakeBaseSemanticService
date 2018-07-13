package de.uni_jena.cs.fusion.semantic.datasource.fallback;

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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.lakebase.DatabaseManagerTest;
import de.uni_jena.cs.fusion.semantic.datasource.IdentityTestDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;

public class KeywordFallbackWrapperTest {

	@Test
	public void match() throws IllegalArgumentException, SQLException, IOException, SemanticDataSourceException {
		DataSource dbm = DatabaseManagerTest.createTestDatabaseManager();
		KeywordFallbackWrapper adapter = new KeywordFallbackWrapper(1.0, dbm, "semantic.concept", "concept_iri",
				new IdentityTestDataSource(Collections.emptySet()));

		// multiple words
		assertEquals(1, adapter.getMatches("multiple words").size());
		// a single word
		assertEquals(2, adapter.getMatches("word").size());
	}

	@Test
	public void getConceptLabels()
			throws IllegalArgumentException, SQLException, IOException, SemanticDataSourceException {
		DataSource dbm = DatabaseManagerTest.createTestDatabaseManager();
		KeywordFallbackWrapper adapter = new KeywordFallbackWrapper(1.0, dbm, "semantic.concept", "concept_iri",
				new IdentityTestDataSource(Collections.emptySet()));

		assertEquals(1, adapter.getLabels(IRI.create(KeywordFallbackWrapper.namespace + "word")).size());
		assertTrue(adapter.getLabels(IRI.create(KeywordFallbackWrapper.namespace + "word")).contains("word"));
	}

}
