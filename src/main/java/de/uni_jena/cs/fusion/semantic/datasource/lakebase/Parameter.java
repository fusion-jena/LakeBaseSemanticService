package de.uni_jena.cs.fusion.semantic.datasource.lakebase;

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

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.jooq.Converter;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.uni_jena.cs.fusion.lakebase.Scope;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllBroadersUsingBroaders;
import de.uni_jena.cs.fusion.similarity.jarowinkler.JaroWinklerSimilarity;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

public class Parameter implements SemanticDataSourceProvidingAllBroadersUsingBroaders, Maintainable {

	private final static String NAMESPACE = "http://fred.igb-berlin.de/Parameter/view/";
	private final static String URL_BASE = "https://fred.igb-berlin.de/Parameter/view/";

	private final static ObjectMapper JSON = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final static Converter<Long, IRI> ID_TO_IRI = new IDToIRIConverter(NAMESPACE);

	private final DataSource datasource;

	private JaroWinklerSimilarity<Set<IRI>> matcher;
	private double matchThreshold = 0.95;

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) throws SemanticDataSourceException {
		Collection<String> result = new ArrayList<String>(json(parameter(iri)).synonyms);
		String symbol = json(parameter(iri)).symbol;
		if (Objects.nonNull(symbol) && !symbol.isEmpty()) {
			result.add(symbol);
		}
		return result;
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		if (isValidIRI(iri)) {
			return Lists.newArrayList(
					IRI.create("http://www.ontology-of-units-of-measure.org/resource/om-2/Quantity"),
					IRI.create("http://purl.obolibrary.org/obo/PATO_0001241"));
		} else {
			LOG.warn("Ignorring  invalid concept \"{}\".", iri);
			return Collections.emptySet();
		}
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		if (isValidIRI(iri)) {
			Record record = parameter(iri);
			if (isValidRecord(record)) {
				return Collections.singleton(record.get("name", String.class));
			}
		}
		return Collections.emptySet();
	}

	@Override
	public Map<IRI, Double> getMatches(String term) {
		term = term.toLowerCase();
		Map<IRI, Double> result = new HashMap<IRI, Double>();
		Map<Set<IRI>, Double> match = this.matcher.apply(term);
		for (Set<IRI> iris : match.keySet()) {
			for (IRI iri : iris) {
				result.put(iri, match.get(iris));
			}
		}
		return result;
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Collections.singleton(NAMESPACE);
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return Collections.singleton(Scope.quantityKind.getIri());
	}

	@Override
	public Collection<IRI> getSignature() throws SemanticDataSourceException {
		try (Connection connection = this.datasource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				return sql.select(field("id")).from(table("data.parameter")).fetchSet(field("id", Long.class), ID_TO_IRI);
			}
		} catch (SQLException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		if (isValidIRI(iri)) {
			try {
				return Collections.singletonList(new URL(iri.getIRIString().replace(NAMESPACE, URL_BASE)));
			} catch (MalformedURLException e) {
				throw new SemanticDataSourceException(e);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		if (isValidIRI(iri)) {
			try (Connection connection = this.datasource.getConnection()) {
				long id = Long.parseUnsignedLong(iri.getIRIString().substring(NAMESPACE.length()));
				try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
					return sql.selectOne().from(table("data.parameter")).where(field("id").eq(id)).fetchOptional()
							.isPresent();
				}
			} catch (SQLException | NumberFormatException | IndexOutOfBoundsException e) {
				throw new SemanticDataSourceException(e);
			}
		} else {
			return false;
		}
	}

	public Parameter(DataSource datasource) throws SemanticDataSourceException {
		this.datasource = datasource;
		this.refreshMatcher();
	}

	private boolean isValidIRI(IRI iri) {
		return iri.getIRIString().startsWith(NAMESPACE)
				&& iri.getIRIString().substring(NAMESPACE.length()).matches("[1-9][0-9]*");
	}

	private boolean isValidRecord(Record record) {
		return record != null;
	}

	private ParameterModel json(Record record) throws SemanticDataSourceException {
		try {
			String json = record.get("document", String.class);
			if (json != null) {
				return JSON.readValue(json, ParameterModel.class);
			} else {
				return new ParameterModel();
			}
		} catch (IOException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		try {
			this.refreshMatcher();
		} catch (SemanticDataSourceException e) {
			throw new MaintenanceException("Failed to refresh matcher.", e);
		}
	}

	@Override
	public boolean providingAllBroaders() {
		return true;
	}

	@Override
	public boolean providingAlternativeLabels() {
		return true;
	}

	@Override
	public boolean providingBroaders() {
		return true;
	}

	@Override
	public boolean providingLabels() {
		return true;
	}

	@Override
	public boolean providingMatch() {
		return true;
	}

	@Override
	public boolean providingSignature() {
		return true;
	}

	@Override
	public boolean providingURLs() {
		return true;
	}

	private void refreshMatcher() throws SemanticDataSourceException {
		// initialize matcher
		Map<String,Set<IRI>> index = new HashMap<>();
		for (IRI iri : this.getSignature()) {
			for (String label : this.getLabels(iri)) {
				String caseInsensitiveLabel = label.toLowerCase();
				index.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
				index.get(caseInsensitiveLabel).add(iri);
			}
			for (String label : this.getAlternativeLabels(iri)) {
				String caseInsensitiveLabel = label.toLowerCase();
				index.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
				index.get(caseInsensitiveLabel).add(iri);
			}
		}
		this.matcher = JaroWinklerSimilarity.with(index,this.matchThreshold);
	}

	@Override
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
		this.matcher.setThreshold(threshold);
	}

	private Record parameter(IRI iri) throws SemanticDataSourceException {
		long id;
		try {
			if (iri.getIRIString().startsWith(NAMESPACE)) {
				id = Long.parseUnsignedLong(iri.getIRIString().substring(NAMESPACE.length()));
			} else {
				throw new SemanticDataSourceException("Invalid Parameter IRI \"" + iri.getIRIString() + "\".");
			}
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new SemanticDataSourceException("Invalid Parameter IRI \"" + iri.getIRIString() + "\".");
		}
		try (Connection connection = this.datasource.getConnection()) {
			try (DSLContext sql = DSL.using(connection, SQLDialect.POSTGRES_9_5)) {
				return sql.selectFrom(table("data.parameter")).where(field("id").eq(id)).fetchOne();
			}
		} catch (SQLException e) {
			throw new SemanticDataSourceException(e);
		}
	}

}
