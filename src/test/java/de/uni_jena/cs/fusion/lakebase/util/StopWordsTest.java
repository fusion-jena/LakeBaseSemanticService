package de.uni_jena.cs.fusion.lakebase.util;

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

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_jena.cs.fusion.util.stopwords.StopWords;

public class StopWordsTest {

	@Test
	public void isStopWord() {
		assertTrue(StopWords.isStopWord("is"));
		assertTrue(StopWords.isStopWord("IS"));
		assertTrue(StopWords.isStopWord("Is"));
		assertFalse(StopWords.isStopWord("lake"));
		assertFalse(StopWords.isStopWord("LAKE"));
		assertFalse(StopWords.isStopWord("Lake"));
	}
	
}
