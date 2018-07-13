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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * A client for the <a href="http://terminologies.gfbio.org/api/">GFBio
 * Terminology Server</a>.
 * </p>
 * 
 * @author Jan Martin Keil
 *
 */
public class TerminologyServerClient {

	private String serviceUrl;
	private final static int SERVICE_DEFAULT_MAX_CONNECTIONS = 10;
	private final static ObjectMapper JSON = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	private final CloseableHttpClient httpClient;

	/**
	 * Constructs a {@link TerminologyServerClient} with a default
	 * {@link CloseableHttpClient} configured to a maximum number of 10
	 * connections.
	 * 
	 * @param serviceUrl
	 *            Base URL of the Terminology Server
	 */
	public TerminologyServerClient(String serviceUrl) {
		this(serviceUrl, HttpClientBuilder.create().setMaxConnTotal(SERVICE_DEFAULT_MAX_CONNECTIONS).build());
	}

	/**
	 * Constructs a {@link TerminologyServerClient} using the specified
	 * {@link CloseableHttpClient}.
	 * 
	 * @param serviceUrl
	 *            Base URL of the Terminology Server
	 * @param httpClient
	 *            {@link CloseableHttpClient} to use
	 */
	public TerminologyServerClient(String serviceUrl, CloseableHttpClient httpClient) {
		this.serviceUrl = Objects.requireNonNull(serviceUrl);
		this.httpClient = Objects.requireNonNull(httpClient);
	}

	/**
	 * Retrieves all terms that are broader of a given one including each
	 * possible path to the top. The result set contains the URI and label of
	 * all broader term(s).
	 * 
	 * @param terminology
	 * @param uri
	 * @return All broader terms
	 * @throws TerminologyServerClientException
	 */
	public BroaderResults allBroader(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/allbroader?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), BroaderResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * Retrieves all terms that are narrower of a given one including each
	 * possible path to the leaves of the hierarchy. The result set contains the
	 * URI and label of all narrower terms.
	 * 
	 * @param terminology
	 * @param uri
	 * @return All narrower terms
	 * @throws TerminologyServerClientException
	 */
	public NarrowerResults allNarrower(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/allnarrower?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), NarrowerResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * Returns the list of terms in a given terminology. The result set contains
	 * the label and URI of each term.
	 * 
	 * @param terminology
	 * @return List of terms in a given terminology
	 * @throws TerminologyServerClientException
	 */
	public TermResult allTerms(String terminology) throws TerminologyServerClientException {
		return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/allterms", TermResult.class);
	}

	/**
	 * Retrieves the term(s) that are one level broader than a given one. The
	 * result set contains the URI and label of the broader term(s).
	 * 
	 * @param terminology
	 * @param uri
	 * @return Broader terms
	 * @throws TerminologyServerClientException
	 */
	public BroaderResults broader(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/broader?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), BroaderResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * 
	 * Returns the capabilities of a terminology given its acronym.
	 * 
	 * @param terminology
	 * @return Capabilities
	 * @throws TerminologyServerClientException
	 */
	public TerminologyCapabilitiesResult capabilities(String terminology) throws TerminologyServerClientException {
		return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/capabilities",
				TerminologyCapabilitiesResult.class);
	}

	/**
	 * Retrieves the hierarchical path to the top level for a given term. The
	 * result set contains the URI, label and the direct broader terms URIs of
	 * all terms in the hierarchy.
	 * 
	 * @param terminology
	 * @param uri
	 * @return Hierarchical path to the top level
	 * @throws TerminologyServerClientException
	 */
	public HierarchyResults hierarchy(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/hierarchy?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), HierarchyResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * Returns the metadata contained in the RDF file of a given terminology.
	 * 
	 * @param terminology
	 * @return Metadata of a terminology
	 * @throws TerminologyServerClientException
	 */
	public MetadataResults metadata(String terminology) throws TerminologyServerClientException {
		return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/metadata", MetadataResults.class);
	}

	/**
	 * Returns the metrics of a terminology.
	 * 
	 * @param terminology
	 * @return Metrics of a terminology
	 * @throws TerminologyServerClientException
	 */
	public TerminologyMetricResults metrics(String terminology) throws TerminologyServerClientException {

		return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/metrics",
				TerminologyMetricResults.class);
	}

	/**
	 * Retrieves the term(s) that are one level narrower than a given one. The
	 * result set contains the URI and label of the narrower term(s).
	 * 
	 * @param terminology
	 * @param uri
	 * @return Narrower terms
	 * @throws TerminologyServerClientException
	 */
	public NarrowerResults narrower(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/narrower?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), NarrowerResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	private <T extends Result<?>> T request(String url, Class<T> c) throws TerminologyServerClientException {
		HttpCacheContext context = HttpCacheContext.create();
		HttpGet httpget = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpget, context)) {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new TerminologyServerClientException(
						"\"" + url + "\" returned " + response.getStatusLine().getStatusCode() + ".");
			}
			try (InputStream content = response.getEntity().getContent()) {
				return JSON.readValue(content, c);
			} catch (JsonParseException | JsonMappingException e) {
				throw new TerminologyServerClientException("\"" + url + "\" returned invalid response.", e);
			}
		} catch (IOException e) {
			throw new TerminologyServerClientException("Request failed on \"" + url + "\".", e);
		}
	}

	/**
	 * Ensures an answer by the terminology server does not contain any error
	 * message. Otherwise an Exception will be thrown.
	 * 
	 * @param result
	 *            {@link Result} object of the answer
	 * @throws TerminologyServerClientException
	 */
	public static Result<?> requireNoError(Result<?> result) throws TerminologyServerClientException {
		Optional<Error> error = result.diagnostics.stream().filter(e -> Objects.nonNull(e.error)).findAny();
		if (error.isPresent()) {
			throw new TerminologyServerClientException("Result error: " + error.get().error);
		}
		return result;
	}

	/**
	 * Ensures an answer by the terminology server does not contain any warning
	 * message. Otherwise an Exception will be thrown.
	 * 
	 * @param result
	 *            {@link Result} object of the answer
	 * @throws TerminologyServerClientException
	 */
	public static Result<?> requireNoWarning(Result<?> result) throws TerminologyServerClientException {
		Optional<Error> error = result.diagnostics.stream().filter(e -> Objects.nonNull(e.warning)).findAny();
		if (error.isPresent()) {
			throw new TerminologyServerClientException("Result warning: " + error.get().warning);
		}
		return result;
	}

	/**
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param terminologies
	 * @param query
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(Collection<String> terminologies, String query)
			throws TerminologyServerClientException {
		return search(terminologies, query, MatchType.exact, false, false);
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param terminologies
	 * @param query
	 * @param matchType
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(Collection<String> terminologies, String query, MatchType matchType)
			throws TerminologyServerClientException {
		return search(terminologies, query, matchType, false, false);
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param terminologies
	 * @param query
	 * @param matchType
	 * @param firstHit
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(Collection<String> terminologies, String query, MatchType matchType, boolean firstHit)
			throws TerminologyServerClientException {
		return search(terminologies, query, matchType, firstHit, false);
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param terminologies
	 * @param query
	 * @param matchType
	 * @param firstHit
	 * @param internalOnly
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	private SearchResults search(Collection<String> terminologies, String query, MatchType matchType, boolean firstHit,
			boolean internalOnly) throws TerminologyServerClientException {
		Objects.requireNonNull(terminologies);
		Objects.requireNonNull(query);
		Objects.requireNonNull(matchType);
		try {
			return this.request(serviceUrl + "search?query=" + URLEncoder.encode(query, "UTF-8")
					+ ((!terminologies.isEmpty()) ? "&terminologies=" + String.join(",", terminologies) : "")
					+ "&match_type=" + matchType.name() + "&first_hit=" + ((firstHit) ? "true" : "false")
					+ "&internal_only=" + ((internalOnly) ? "true" : "false"), SearchResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode query.", e);
		}
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param query
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(String query) throws TerminologyServerClientException {
		return search(Collections.emptyList(), query, MatchType.exact, false, false);
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param query
	 * @param matchType
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(String query, MatchType matchType) throws TerminologyServerClientException {
		return search(Collections.emptyList(), query, matchType, false, false);
	}

	/**
	 * 
	 * The search service looks for exact, included or regular expression based
	 * term matches (default exact). The search looks inside labels, synonyms,
	 * common names, accronyms and abreviations. The parameter terminologies can
	 * be used to restrict the search to specific terminologies and the
	 * first_hit parameter can be used to stop at the first terminology where a
	 * match is found (default false). Also searches the external web services
	 * (COL, ITIS, WoRMS, PESI, Geonames, DTNtaxonlists, PNU). Search can be
	 * limited to internal terminologies by providing the parameter
	 * internal_only (default false). If the parameter “terminologies” is empty,
	 * all terminologies are searched. The result set contains the label, URI,
	 * description, rank, kingdom, source terminology and synonyms or common
	 * names of each matching term.
	 * 
	 * @param query
	 * @param matchType
	 * @param firstHit
	 * @param internalOnly
	 * @return Search results
	 * @throws TerminologyServerClientException
	 */
	public SearchResults search(String query, MatchType matchType, boolean firstHit, boolean internalOnly)
			throws TerminologyServerClientException {
		return search(Collections.emptyList(), query, matchType, firstHit, internalOnly);
	}

	/**
	 * Returns all terms containing a given string, limited to 15 suggestions by
	 * default.
	 * 
	 * @param terminologies
	 * @param query
	 * @return Suggestions
	 * @throws TerminologyServerClientException
	 */
	public SuggestResults suggest(Collection<String> terminologies, String query)
			throws TerminologyServerClientException {
		return suggest(terminologies, query, null);
	}

	/**
	 * Returns all terms containing a given string, limited to 15 suggestions by
	 * default.
	 * 
	 * @param terminologies
	 * @param query
	 * @param limit
	 * @return Suggestions
	 * @throws TerminologyServerClientException
	 */
	public SuggestResults suggest(Collection<String> terminologies, String query, Integer limit)
			throws TerminologyServerClientException {
		Objects.requireNonNull(terminologies);
		Objects.requireNonNull(query);
		if (limit != null && limit < 1) {
			throw new IllegalArgumentException("Negative limit value.");
		}
		try {
			return this
					.request(
							serviceUrl + "suggest?query=" + URLEncoder.encode(query, "UTF-8")
									+ ((!terminologies.isEmpty()) ? "&terminologies=" + String.join(",", terminologies)
											: "")
									+ ((limit != null) ? "&limit=" + limit.toString() : ""),
							SuggestResults.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode query.", e);
		}
	}

	/**
	 * Returns all terms containing a given string, limited to 15 suggestions by
	 * default.
	 * 
	 * @param query
	 * @return Suggestions
	 * @throws TerminologyServerClientException
	 */
	public SuggestResults suggest(String query) throws TerminologyServerClientException {
		return suggest(Collections.emptyList(), query, null);
	}

	/**
	 * Returns all terms containing a given string, limited to 15 suggestions by
	 * default.
	 * 
	 * @param query
	 * @param limit
	 * @return Suggestions
	 * @throws TerminologyServerClientException
	 */
	public SuggestResults suggest(String query, Integer limit) throws TerminologyServerClientException {
		return suggest(Collections.emptyList(), query, limit);
	}

	/**
	 * Returns all terms containing a given string, limited to 15 suggestions by
	 * default.
	 * 
	 * @param terminology
	 * @param uri
	 * @return Suggestions
	 * @throws TerminologyServerClientException
	 */
	public TermSynonymResult synonyms(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/synonyms?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), TermSynonymResult.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * Returns the information about a term given its URI.
	 * 
	 * @param terminology
	 * @param uri
	 * @return Information about a term
	 * @throws TerminologyServerClientException
	 */
	public TermInformationResult term(String terminology, String uri) throws TerminologyServerClientException {
		try {
			return this.request(serviceUrl + Objects.requireNonNull(terminology) + "/term?uri="
					+ URLEncoder.encode(Objects.requireNonNull(uri), "UTF-8"), TermInformationResult.class);
		} catch (UnsupportedEncodingException e) {
			throw new TerminologyServerClientException("Failed to encode uri.", e);
		}
	}

	/**
	 * Returns the list of all available terminologies on the GFBio TS. The
	 * result set contains the name, acronym (terminology-id), short description
	 * and URI of each terminology.
	 * 
	 * @returns List of all available terminologies
	 */
	public ListResult terminologies() throws TerminologyServerClientException {
		return this.request(serviceUrl, ListResult.class);
	}

	/**
	 * Returns the information about a terminology given its acronym. The result
	 * set contains the URI, acronym, name, description, domain, ontology
	 * language, creation date and Description Logics Expressivity of the
	 * terminology.
	 * 
	 * @param terminology
	 * @return Information about a terminology
	 * @throws TerminologyServerClientException
	 */
	public SingleTerminologyResult terminology(String terminology) throws TerminologyServerClientException {
		return this.request(serviceUrl + Objects.requireNonNull(terminology), SingleTerminologyResult.class);
	}
}
