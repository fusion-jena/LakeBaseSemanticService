package de.uni_jena.cs.fusion.lakebase.servlet.worker;

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

/**
 * 
 * @since 0.1
 *
 */
public class ServiceWorkerException extends Exception {
	private static final long serialVersionUID = -4129601407900741569L;
	private final int code;

	public ServiceWorkerException(String message, int code) {
		super(message);
		this.code = code;
	}

	public ServiceWorkerException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public ServiceWorkerException(String message, int code, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public int getCode() {
		return this.code;
	}

}
