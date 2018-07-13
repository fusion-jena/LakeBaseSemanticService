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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WormsClient {

	private final static int HTTP_MAX_URL_BYTES = 7806;
	private final static String SERVICE_URL = "http://www.marinespecies.org/rest/";
	private final static ObjectMapper JSON = new ObjectMapper();
	private final CloseableHttpClient httpClient;

	/**
	 * Constructs a {@link WormsClient} with a default {@link CloseableHttpClient}
	 * configured to a maximum number of 10 connections.
	 * 
	 */
	public WormsClient() {
		this(HttpClientBuilder.create().build());
	}

	/**
	 * Constructs a {@link WormsClient} using the specified
	 * {@link CloseableHttpClient}.
	 * 
	 * @param httpClient
	 *            {@link CloseableHttpClient} to use
	 */
	public WormsClient(CloseableHttpClient httpClient) {
		this.httpClient = Objects.requireNonNull(httpClient);
	}

	/**
	 * Get attribute definitions. To refer to root items specify ID = ‘0’.
	 * 
	 * @param id
	 *            The attribute definition id to search for
	 * @param includeChildren
	 *            Include the tree of children.
	 * @throws WormsClientException
	 */
	public Collection<AttributeKey> aphiaAttributeKeysById(long id, boolean includeChildren)
			throws WormsClientException {
		return this.request(
				SERVICE_URL + "AphiaAttributeKeysByID/" + id + ((includeChildren) ? "?include_inherited=true" : ""),
				new TypeReference<ArrayList<AttributeKey>>() {
				});
	}

	/**
	 * Get a list of attributes for a given AphiaID
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @param includeChildren
	 *            Include attributes inherited from the taxon its parent(s).
	 * @throws WormsClientException
	 */
	public Collection<Attribute> aphiaAttributesByAphiaId(long aphiaId, boolean includeChildren)
			throws WormsClientException {
		return this.request(
				SERVICE_URL + "AphiaAttributesByAphiaID/" + aphiaId
						+ ((includeChildren) ? "?include_inherited=true" : ""),
				new TypeReference<ArrayList<Attribute>>() {
				});
	}

	/**
	 * Get list values that are grouped by an CateogryID
	 * 
	 * @param id
	 *            The CateogryID to search for
	 * @throws WormsClientException
	 */
	public Collection<AttributeValue> aphiaAttributeValuesByCategoryId(long id) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaAttributeValuesByCategoryID/" + id,
				new TypeReference<ArrayList<AttributeValue>>() {
				});

	}

	/**
	 * <p>
	 * Get the direct children for a given AphiaID.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple calls of
	 * {@link #aphiaChildrenByAphiaId(long, boolean, long)} to receive all records
	 * in chunks of 50 records.
	 * </p>
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaChildrenByAphiaId(long aphiaId, boolean marineOnly)
			throws WormsClientException {
		Collection<AphiaRecord> totalResult = new ArrayList<AphiaRecord>();
		Collection<AphiaRecord> singleResult;
		long offset = 1;
		do {
			singleResult = aphiaChildrenByAphiaId(aphiaId, marineOnly, offset);
			totalResult.addAll(singleResult);
			offset += 50;
		} while (singleResult.size() == 50);
		return totalResult;
	}

	/**
	 * Get the direct children (max. 50) for a given AphiaID.
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @param offset
	 *            Starting recordnumber, when retrieving next chunk of 50 records.
	 *            First record has number {@code 1}.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaChildrenByAphiaId(long aphiaId, boolean marineOnly, long offset)
			throws WormsClientException {
		return this.request(
				SERVICE_URL + "AphiaChildrenByAphiaID/" + aphiaId + "?marine_only=" + marineOnly + "&offset=" + offset,
				new TypeReference<ArrayList<AphiaRecord>>() {
				});
	}

	/**
	 * Get the complete classification for one taxon. This also includes any sub or
	 * super ranks.
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @return classification of the taxon
	 * @throws WormsClientException
	 */
	public Classification aphiaClassificationByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaClassificationByAphiaID/" + aphiaId,
				new TypeReference<Classification>() {
				});
	}

	/**
	 * Get all distributions for a given AphiaID External Identifiers
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @return all distributions
	 * @throws WormsClientException
	 */
	public Collection<Distribution> aphiaDistributionsByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaDistributionsByAphiaID/" + aphiaId,
				new TypeReference<ArrayList<Distribution>>() {
				});
	}

	/**
	 * Get any external identifier(s) for a given AphiaID
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @param type
	 *            Type of external identifier to return.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<String> aphiaExternalIdByAphiaId(long aphiaId, ExternalIdentifierSource type)
			throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaExternalIDByAphiaID/" + aphiaId + "?type=" + type.name(),
				new TypeReference<ArrayList<String>>() {
				});
	}

	/**
	 * Get the AphiaID for a given name
	 * 
	 * @param scientificName
	 * @return
	 * @throws WormsClientException
	 */
	public Long aphiaIdByName(String scientificName) throws WormsClientException {
		try {
			return this.request(
					SERVICE_URL + "AphiaIDByName/" + URLEncoder.encode(scientificName, "UTF-8").replace("+", "%20"),
					new TypeReference<Long>() {
					});
		} catch (UnsupportedEncodingException e) {
			throw new WormsClientException(e);
		}
	}

	/**
	 * <p>
	 * Get a list of AphiaIDs with attribute tree for a given attribute definition
	 * ID Distributions.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple calls of
	 * {@link #aphiaIdsByAttributeKeyId(long, long)} to receive all records in
	 * chunks of 50 records.
	 * </p>
	 * 
	 * @param id
	 *            The attribute definition id to search for
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaAttributeSets> aphiaIdsByAttributeKeyId(long id) throws WormsClientException {
		Collection<AphiaAttributeSets> totalResult = new ArrayList<AphiaAttributeSets>();
		Collection<AphiaAttributeSets> singleResult;
		long offset = 1;
		do {
			singleResult = aphiaIdsByAttributeKeyId(id, offset);
			totalResult.addAll(singleResult);
			offset += 50;
		} while (singleResult.size() == 50);
		return totalResult;
	}

	/**
	 * Get a list of AphiaIDs (max 50) with attribute tree for a given attribute
	 * definition ID Distributions.
	 * 
	 * @param id
	 *            The attribute definition id to search for
	 * @param offset
	 *            Starting record number, when retrieving next chunk of (50)
	 *            records. First record has number {@code 1}.
	 * @throws WormsClientException
	 */
	public Collection<AphiaAttributeSets> aphiaIdsByAttributeKeyId(long id, long offset) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaIDsByAttributeKeyID/" + id,
				new TypeReference<ArrayList<AphiaAttributeSets>>() {
				});
	}

	/**
	 * Get the name for a given AphiaID
	 * 
	 * @param aphiaId
	 * @return
	 * @throws WormsClientException
	 */
	public String aphiaNameByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaNameByAphiaID/" + aphiaId, new TypeReference<String>() {
		});
	}

	/**
	 * Get the complete AphiaRecord for a given AphiaID
	 * 
	 * @param aphiaId
	 * @return
	 * @throws WormsClientException
	 */
	public AphiaRecord aphiaRecordByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaRecordByAphiaID/" + aphiaId, new TypeReference<AphiaRecord>() {
		});
	}

	/**
	 * Get the Aphia Record for a given external identifier Sources
	 * 
	 * @param id
	 * @param type
	 * @return
	 * @throws WormsClientException
	 */
	public AphiaRecord aphiaRecordByExternalId(String id, ExternalIdentifierSource type) throws WormsClientException {
		try {
			return this
					.request(
							SERVICE_URL + "AphiaRecordByExternalID/"
									+ URLEncoder.encode(id, "UTF-8").replace("+", "%20") + "?type=" + type.name(),
							new TypeReference<AphiaRecord>() {
							});
		} catch (UnsupportedEncodingException e) {
			throw new WormsClientException(e);
		}
	}

	/**
	 * <p>
	 * Lists all AphiaRecords (max. 50) modified or added between a specific time
	 * interval
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple request to receive all records
	 * in chunks of 50 records.
	 * </p>
	 * 
	 * @param statdate
	 * @param enddate
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByDate(TemporalAccessor statdate, TemporalAccessor enddate,
			boolean marineOnly) throws WormsClientException {
		Collection<AphiaRecord> totalResult = new ArrayList<AphiaRecord>();
		Collection<AphiaRecord> singleResult;
		long offset = 1;
		do {
			singleResult = aphiaRecordsByDate(statdate, enddate, marineOnly, offset);
			totalResult.addAll(singleResult);
			offset += 50;
		} while (singleResult.size() == 50);
		return totalResult;
	}

	/**
	 * Lists all AphiaRecords (max. 50) modified or added between a specific time
	 * interval
	 * 
	 * @param statdate
	 * @param enddate
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @param offset
	 *            Starting record number, when retrieving next chunk of (50)
	 *            records. First record has number {@code 1}.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByDate(TemporalAccessor statdate, TemporalAccessor enddate,
			boolean marineOnly, long offset) throws WormsClientException {
		try {
			return this.request(SERVICE_URL + "AphiaRecordsByDate?startdate="
					+ DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(statdate) + "&enddate="
					+ DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(enddate) + "&marine_only=" + marineOnly + "&offset="
					+ offset, new TypeReference<ArrayList<AphiaRecord>>() {
					});
		} catch (DateTimeException e) {
			throw new WormsClientException(e);
		}
	}

	/**
	 * <p>
	 * For each given scientific name (may include authority), try to find one or
	 * more AphiaRecords, using the TAXAMATCH fuzzy matching algorithm by Tony Rees.
	 * This allows you to (fuzzy) match multiple names in one call.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple calls service request.
	 * </p>
	 * 
	 * @param scientificNames
	 *            Names to search for
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @return
	 * @throws WormsClientException
	 */
	public List<Collection<AphiaRecord>> aphiaRecordsByMatchNames(List<String> scientificNames, boolean marineOnly)
			throws WormsClientException {
		return requestPartitioned(SERVICE_URL + "AphiaRecordsByMatchNames?scientificnames[]=", scientificNames,
				"&scientificnames[]=", 50, "&marine_only=" + marineOnly);
	}

	/**
	 * <p>
	 * Get one or more matching AphiaRecords for a given name.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple calls of
	 * {@link #aphiaRecordsByName(String, boolean, boolean, long)} to receive all
	 * records in chunks of 50 records.
	 * </p>
	 * 
	 * @param scientificName
	 *            Name to search for
	 * @param like
	 *            Add a "%"-sign added after the ScientificName (SQL LIKE function).
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByName(String scientificName, boolean like, boolean marineOnly)
			throws WormsClientException {
		Collection<AphiaRecord> totalResult = new ArrayList<AphiaRecord>();
		Collection<AphiaRecord> singleResult;
		long offset = 1;
		do {
			singleResult = aphiaRecordsByName(scientificName, like, marineOnly, offset);
			totalResult.addAll(singleResult);
			offset += 50;
		} while (singleResult.size() == 50);
		return totalResult;
	}

	/**
	 * Get one or more matching AphiaRecords (max. 50) for a given name.
	 * 
	 * @param scientificName
	 *            Name to search for
	 * @param like
	 *            Add a "%"-sign added after the ScientificName (SQL LIKE function).
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @param offset
	 *            Starting record number, when retrieving next chunk of (50)
	 *            records. First record has number {@code 1}.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByName(String scientificName, boolean like, boolean marineOnly,
			long offset) throws WormsClientException {
		try {
			return this.request(
					SERVICE_URL + "AphiaRecordsByName/"
							+ URLEncoder.encode(Objects.requireNonNull(scientificName), "UTF-8").replace("+", "%20")
							+ "?like=" + like + "&marine_only=" + marineOnly + "&offset=" + offset,
					new TypeReference<ArrayList<AphiaRecord>>() {
					});
		} catch (UnsupportedEncodingException e) {
			throw new WormsClientException(e);
		}
	}

	/**
	 * For each given scientific name, try to find one or more AphiaRecords (max.
	 * 50). This allows you to match multiple names in one call. Limited to 500
	 * names at once for performance reasons.
	 * 
	 * @param scientificNames
	 *            Collection of names to search for
	 * @param like
	 *            Add a "%"-sign added after the ScientificName (SQL LIKE function).
	 * @param marineOnly
	 *            Limit to marine taxa.
	 * @return
	 * @throws WormsClientException
	 */
	public List<Collection<AphiaRecord>> aphiaRecordsByNames(List<String> scientificNames, boolean like,
			boolean marineOnly) throws WormsClientException {
		return requestPartitioned(SERVICE_URL + "AphiaRecordsByNames?scientificnames[]=", scientificNames,
				"&scientificnames[]=", 500, "&like=" + like + "&marine_only=" + marineOnly);
	}

	/**
	 * <p>
	 * Get one or more Aphia Records for a given vernacular.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This methods might cause multiple request to receive all records
	 * in chunks of 50 records.
	 * </p>
	 * 
	 * @param vernacular
	 *            The vernacular to find records for
	 * @param like
	 *            Add a "%"-sign before and after the input (SQL LIKE ‘%vernacular%’
	 *            function).
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByVernacular(String vernacular, boolean like)
			throws WormsClientException {
		Collection<AphiaRecord> totalResult = new ArrayList<AphiaRecord>();
		Collection<AphiaRecord> singleResult;
		long offset = 1;
		do {
			singleResult = aphiaRecordsByVernacular(vernacular, like, offset);
			totalResult.addAll(singleResult);
			offset += 50;
		} while (singleResult.size() == 50);
		return totalResult;
	}

	/**
	 * 
	 * Get one or more Aphia Records (max. 50) for a given vernacular.
	 * 
	 * @param vernacular
	 *            The vernacular to find records for
	 * @param like
	 *            Add a "%"-sign before and after the input (SQL LIKE ‘%vernacular%’
	 *            function).
	 * @param offset
	 *            Starting record number, when retrieving next chunk of (50)
	 *            records. First record has number {@code 1}.
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaRecordsByVernacular(String vernacular, boolean like, long offset)
			throws WormsClientException {
		try {
			return this.request(SERVICE_URL + "AphiaRecordsByVernacular/"
					+ URLEncoder.encode(Objects.requireNonNull(vernacular), "UTF-8").replace("+", "%20") + "?like="
					+ like + "&offset=" + offset, new TypeReference<ArrayList<AphiaRecord>>() {
					});
		} catch (UnsupportedEncodingException e) {
			throw new WormsClientException(e);
		}
	}

	/**
	 * Get one or more sources/references including links, for one AphiaID
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @throws WormsClientException
	 */
	public Collection<Source> aphiaSourcesByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaSourcesByAphiaID/" + aphiaId, new TypeReference<ArrayList<Source>>() {
		});
	}

	/**
	 * Get all synonyms for a given AphiaID.
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @throws WormsClientException
	 */
	public Collection<AphiaRecord> aphiaSynonymsByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaSynonymsByAphiaID/" + aphiaId,
				new TypeReference<ArrayList<AphiaRecord>>() {
				});
	}

	/**
	 * Get all vernaculars for a given AphiaID
	 * 
	 * @param aphiaId
	 *            The AphiaID to search for
	 * @return
	 * @throws WormsClientException
	 */
	public Collection<Vernacular> aphiaVernacularsByAphiaId(long aphiaId) throws WormsClientException {
		return this.request(SERVICE_URL + "AphiaVernacularsByAphiaID/" + aphiaId,
				new TypeReference<ArrayList<Vernacular>>() {
				});
	}

	@SuppressWarnings("unchecked")
	private <T> List<Collection<T>> ensureEmptyCollections(List<Collection<T>> list, long count) {
		if (list.isEmpty()) {
			// add omitted empty collections
			return Stream.generate(() -> {
				return (Collection<T>) Collections.emptyList();
			}).limit(count).collect(Collectors.toList());
		} else {
			return list;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T request(String url, TypeReference<T> t) throws WormsClientException {
		HttpCacheContext context = HttpCacheContext.create();
		HttpGet httpget = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpget, context)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				try (InputStream content = response.getEntity().getContent()) {
					return JSON.readValue(content, t);
				} catch (JsonParseException | JsonMappingException e) {
					throw new WormsClientException("\"" + url + "\" returned invalid response.", e);
				}
			} else if (response.getStatusLine().getStatusCode() == 204) {
				if (JSON.getTypeFactory().constructType(t).isContainerType()) {
					try {
						return (T) JSON.getTypeFactory().constructType(t).getRawClass().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| SecurityException e) {
						throw new WormsClientException(
								"Failed to instantiate empty result for \"" + url + "\".", e);
					}
				} else {
					return null;
				}
			} else {
				throw new WormsClientException(
						"\"" + url + "\" returned " + response.getStatusLine().getStatusCode() + ".");
			}
		} catch (IOException e) {
			throw new WormsClientException("Request failed on \"" + url + "\".", e);
		}
	}

	private List<Collection<AphiaRecord>> requestPartitioned(String requestPrefix, List<String> names,
			String requestNameDelimiter, int maxNamesPerRequest, String requestPostfix) throws WormsClientException {
		List<Collection<AphiaRecord>> totalResult = new ArrayList<Collection<AphiaRecord>>();
		StringBuilder requestUrl = new StringBuilder(requestPrefix);
		long requestNames = 0;
		for (String scientificName : names) {
			try {
				String scientificNameEncoded = URLEncoder.encode(scientificName, "UTF-8").replace("+", "%20");
				if (requestNames == maxNamesPerRequest || requestUrl.length() + requestNameDelimiter.length()
						+ scientificNameEncoded.length() + requestPostfix.length() > HTTP_MAX_URL_BYTES) {
					// reached max number of names per request or max length of URL

					// request partial result and add to total results
					totalResult.addAll(ensureEmptyCollections(this.request(requestUrl.append(requestPostfix).toString(),
							new TypeReference<ArrayList<Collection<AphiaRecord>>>() {
							}), requestNames));
					// reset request
					requestUrl = new StringBuilder(requestPrefix);
					requestNames = 0;
				}
				if (requestNames != 0) {
					requestUrl.append(requestNameDelimiter);
				}
				requestUrl.append(scientificNameEncoded);
				requestNames++;
			} catch (UnsupportedEncodingException e) {
				throw new WormsClientException(e);
			}
		}
		// request last partial result and add to total results
		totalResult.addAll(ensureEmptyCollections(this.request(requestUrl.append(requestPostfix).toString(),
				new TypeReference<ArrayList<Collection<AphiaRecord>>>() {
				}), requestNames));

		return totalResult;
	}
}
