package de.uni_jena.cs.fusion.util.javascript;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class JavaScriptValidator {
	private final static String[] jsReservedWordsArray = { "break", "case", "catch", "continue", "debugger", "default",
			"delete", "do", "else", "finally", "for", "function", "if", "in", "instanceof", "new", "return", "switch",
			"this", "throw", "try", "typeof", "var", "void", "while", "with", "class", "const", "enum", "export",
			"extends", "import", "super", "implements", "interface", "let", "package", "private", "protected", "public",
			"static", "yield", "null", "true", "false", "nan", "infinity", "undefined" };
	private final static Collection<String> jsReservedWords = new HashSet<String>(Arrays.asList(jsReservedWordsArray));

	/**
	 * NOTE: This method is more restrictive as ES5.1.
	 * 
	 * @param functionName
	 *            name to test
	 * @return {@code true} if <i>functionName</i> is a valid function name
	 */
	public static boolean validFunctionName(String functionName) {
		return functionName.matches("[a-zA-Z_$][0-9a-zA-Z_$]*")
				&& !jsReservedWords.contains(functionName.toLowerCase());
	}

}
