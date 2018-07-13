package de.uni_jena.cs.fusion.lakebase.servlet;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import de.uni_jena.cs.fusion.lakebase.Environment;
import de.uni_jena.cs.fusion.lakebase.Scope;
import de.uni_jena.cs.fusion.lakebase.model.AnnotationCopyRequest;
import de.uni_jena.cs.fusion.lakebase.model.CompleteRequest;
import de.uni_jena.cs.fusion.lakebase.model.EntitiesAnnotation;
import de.uni_jena.cs.fusion.lakebase.model.Entity;
import de.uni_jena.cs.fusion.lakebase.model.SearchRequest;
import de.uni_jena.cs.fusion.lakebase.model.SuggestRequest;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.CompleteWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.AnnotationCopyWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.DeleteAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.DescribeWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.GetAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SearchWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.ServiceWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.ServiceWorkerException;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SetAnnotationWorker;
import de.uni_jena.cs.fusion.lakebase.servlet.worker.SuggestAnnotationWorker;
import de.uni_jena.cs.fusion.util.javascript.JavaScriptValidator;

/**
 * 
 * @since 0.1
 *
 */
public class Servlet extends HttpServlet {
	private final static long serialVersionUID = 9169718181263619042L;
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final static ObjectMapper jsonMapper = new ObjectMapper();
	{
		// do not close target stream after writing JSON (required for JSONP)
		jsonMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	private final Environment getEnvironment() {
		return (Environment) getServletContext().getAttribute("environment");
	}

	private SuggestAnnotationWorker suggestAnnotationWorker = new SuggestAnnotationWorker();
	private DeleteAnnotationWorker deleteAnnotationWorker = new DeleteAnnotationWorker();
	private AnnotationCopyWorker copyAnnotationWorker = new AnnotationCopyWorker();
	private GetAnnotationWorker getAnnotationWorker = new GetAnnotationWorker();
	private SetAnnotationWorker setAnnotationWorker = new SetAnnotationWorker(true);
	private SearchWorker searchWorker = new SearchWorker();
	private CompleteWorker completeWorker = new CompleteWorker();
	private DescribeWorker describeWorker = new DescribeWorker();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			if (request.getServletPath().equals("/")) {
				try (InputStream in = new FileInputStream(Environment.file("running.html"))) {
					try (PrintWriter out = response.getWriter()) {
						response.setContentType("text/html");
						IOUtils.copy(in, out, StandardCharsets.UTF_8);
					}
				}
			} else {
				response.sendError(405);
			}
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			ServiceWorker worker;
			Object input;
			switch (request.getServletPath()) {
			case "/annotation/suggest":
				worker = suggestAnnotationWorker;
				input = new SuggestRequest(getQuery(request), getScope(request));
				break;
			case "/annotation/delete":
				worker = deleteAnnotationWorker;
				input = jsonMapper.readValue(getQuery(request), Entity[].class);
				break;
			case "/annotation/set":
				worker = setAnnotationWorker;
				input = jsonMapper.readValue(getQuery(request), EntitiesAnnotation[].class);
				break;
			case "/annotation/get":
				worker = getAnnotationWorker;
				input = jsonMapper.readValue(getQuery(request), Entity[].class);
				break;
			case "/annotation/copy":
				worker = copyAnnotationWorker;
				input = jsonMapper.readValue(getQuery(request), AnnotationCopyRequest.class);
				break;
			case "/search":
				worker = searchWorker;
				input = jsonMapper.readValue(getQuery(request), SearchRequest.class);
				break;
			case "/complete":
				worker = completeWorker;
				input = new CompleteRequest(getQuery(request), getScope(request));
				break;
			case "/describe":
				worker = describeWorker;
				input = jsonMapper.readValue(getQuery(request), IRI[].class);
				break;
			default:
				throw new IllegalArgumentException("Unknown service.");
			}
			Object content = worker.processRequest(getEnvironment(), input);
			String callback = getCallback(request);

			if (content != null) {
				response.setCharacterEncoding("UTF-8");
				try (PrintWriter out = response.getWriter()) {
					if (content instanceof String) {
						// String as plain, other as JSON
						response.setContentType("text/plain");
						out.println(content.toString());
					} else if (callback != null) {
						// it is an JSONP request
						response.setContentType("application/javascript");
						// encapsulate content in callback function
						out.print(callback + "(");
						jsonMapper.writeValue(out, content);
						out.print(");");
					} else {
						response.setContentType("application/json");
						jsonMapper.writeValue(out, content);
					}
				}
			} else {
				response.setStatus(204); // status "no content"
			}
		} catch (Throwable e) {
			// NOTE: using Throwable to catch RuntimeExceptions too
			int code;
			if (e instanceof ServiceWorkerException) {
				code = ((ServiceWorkerException) e).getCode();
			} else {
				code = 500;
			}
			String url;
			if (request.getQueryString() != null) {
				url = request.getRequestURI() + "?" + request.getQueryString();
			} else {
				url = request.getRequestURI();
			}
			StringBuilder parameters = new StringBuilder();
			for (Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
				parameters.append("\n   ");
				parameters.append(parameter.getKey());
				parameters.append("=");
				parameters.append(
						String.join("\n   " + Strings.repeat(" ", parameter.getKey().length()), parameter.getValue()));
			}
			UUID errorUUID = UUID.randomUUID();
			String message = e.getMessage() + " (Error UUID: " + errorUUID + ")";
			log.error(
					"Failed to reply to request \"" + url + "\". (UUID: " + errorUUID + ")\n Parameters:" + parameters,
					e);
			try {
				response.sendError(code, message);
			} catch (IOException e1) {
				log.error("Failed to send error message. (UUID: " + errorUUID + ")", e1);
				throw new ServletException(message);
			}
		}
	}

	private static String getCallback(HttpServletRequest request) throws ServiceWorkerException {
		String callback = request.getParameter("callback");
		if (callback != null && !JavaScriptValidator.validFunctionName(callback)) {
			throw new ServiceWorkerException("Illegal callback function name.", 400);
		}
		return callback;
	}

	private Collection<Scope> getScope(HttpServletRequest request) throws ServiceWorkerException {
		try {
			String scopeParameter = request.getParameter("s");
			if (Objects.nonNull(scopeParameter) && !scopeParameter.isEmpty()) {
				return Collections.singleton(Scope.valueOf(Scope.class, request.getParameter("s")));
			} else {
				return Collections.singleton(Scope.all);
			}
		} catch (Throwable e) {
			log.info("Illegal scope value.", e);
			throw new ServiceWorkerException("Illegal scope value.", 400);
		}
	}

	private static String getQuery(HttpServletRequest request) throws ServiceWorkerException {
		String query = request.getParameter("q");
		if (query == null) {
			throw new ServiceWorkerException("Illegal query value.", 400);
		}
		return query;
	}
}
