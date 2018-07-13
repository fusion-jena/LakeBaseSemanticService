package de.uni_jena.cs.fusion.semantic.datasource.wikidata;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_StrReplace;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.lang.sparql_11.SPARQLParser11;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.util.ExprUtils;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.sparql.SparqlDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.sparql.SparqlDataSourceFactory;
import de.uni_jena.cs.fusion.semantic.datasource.sparql.SparqlDataSource.Feature;

public class WikidataDataSourceFactory {

	private final static Node KEY_NODE = NodeFactory.createVariable("key");
	private final static Node VALUE_NODE = NodeFactory.createVariable("value");
	private final static Prologue PROLOGUE = new Prologue();

	static {
		// set prefixes known by query.wikidata.org
		PROLOGUE.setPrefix("bd", "http://www.bigdata.com/rdf#");
		PROLOGUE.setPrefix("bds", "http://www.bigdata.com/rdf/search#");
		PROLOGUE.setPrefix("gas", "http://www.bigdata.com/rdf/gas#");
		PROLOGUE.setPrefix("geo", "http://www.opengis.net/ont/geosparql#");
		PROLOGUE.setPrefix("geof", "http://www.opengis.net/def/geosparql/function/");
		PROLOGUE.setPrefix("hint", "http://www.bigdata.com/queryHints#");
		PROLOGUE.setPrefix("owl", "http://www.w3.org/2002/07/owl#");
		PROLOGUE.setPrefix("p", "http://www.wikidata.org/prop/");
		PROLOGUE.setPrefix("pq", "http://www.wikidata.org/prop/qualifier/");
		PROLOGUE.setPrefix("pqn", "http://www.wikidata.org/prop/qualifier/value-normalized/");
		PROLOGUE.setPrefix("pqv", "http://www.wikidata.org/prop/qualifier/value/");
		PROLOGUE.setPrefix("pr", "http://www.wikidata.org/prop/reference/");
		PROLOGUE.setPrefix("prn", "http://www.wikidata.org/prop/reference/value-normalized/");
		PROLOGUE.setPrefix("prov", "http://www.w3.org/ns/prov#");
		PROLOGUE.setPrefix("prv", "http://www.wikidata.org/prop/reference/value/");
		PROLOGUE.setPrefix("ps", "http://www.wikidata.org/prop/statement/");
		PROLOGUE.setPrefix("psn", "http://www.wikidata.org/prop/statement/value-normalized/");
		PROLOGUE.setPrefix("psv", "http://www.wikidata.org/prop/statement/value/");
		PROLOGUE.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		PROLOGUE.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		PROLOGUE.setPrefix("schema", "http://schema.org/");
		PROLOGUE.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		PROLOGUE.setPrefix("wd", "http://www.wikidata.org/entity/");
		PROLOGUE.setPrefix("wdata", "http://www.wikidata.org/wiki/Special:EntityData/");
		PROLOGUE.setPrefix("wdno", "http://www.wikidata.org/prop/novalue/");
		PROLOGUE.setPrefix("wdref", "http://www.wikidata.org/reference/");
		PROLOGUE.setPrefix("wds", "http://www.wikidata.org/entity/statement/");
		PROLOGUE.setPrefix("wdt", "http://www.wikidata.org/prop/direct/");
		PROLOGUE.setPrefix("wdv", "http://www.wikidata.org/value/");
		PROLOGUE.setPrefix("wikibase", "http://wikiba.se/ontology#");
		PROLOGUE.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	}

	private static Element parseGroupGraphPatternSub(String pattern) throws SemanticDataSourceException {
		try {
			InputStream stream = new ByteArrayInputStream(pattern.getBytes());
			SPARQLParser11 parser = new SPARQLParser11(stream);
			parser.setPrologue(WikidataDataSourceFactory.PROLOGUE);
			return parser.GroupGraphPatternSub();
		} catch (ParseException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private static Node parseIri(String node) throws SemanticDataSourceException {
		try {
			InputStream stream = new ByteArrayInputStream(node.getBytes());
			SPARQLParser11 parser = new SPARQLParser11(stream);
			parser.setPrologue(WikidataDataSourceFactory.PROLOGUE);
			return NodeFactory.createURI(parser.iri());
		} catch (ParseException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	private static Path parsePropertyPath(String propertyPath) throws SemanticDataSourceException {
		try {
			InputStream stream = new ByteArrayInputStream(propertyPath.getBytes());
			SPARQLParser11 parser = new SPARQLParser11(stream);
			parser.setPrologue(WikidataDataSourceFactory.PROLOGUE);
			return parser.Path();
		} catch (ParseException e) {
			throw new SemanticDataSourceException(e);
		}
	}

	public static WikidataDataSourceFactory root(IRI root) {
		return WikidataDataSourceFactory.root(root.getIRIString());
	}

	public static WikidataDataSourceFactory root(String root) {
		return new WikidataDataSourceFactory("? wdt:P31?/wdt:P279* " + root);
	}

	public static WikidataDataSourceFactory pattern(String pattern) {
		return new WikidataDataSourceFactory(pattern);
	}

	private final SparqlDataSourceFactory factory;

	private final Collection<Locale> locales = new HashSet<Locale>();

	private final Map<Feature, Collection<String>> properties = new HashMap<Feature, Collection<String>>();
	private final Map<Feature, Collection<String>> propertiesOfIds = new HashMap<Feature, Collection<String>>();
	private final Map<Feature, Collection<String>> propertiesLocalized = new HashMap<Feature, Collection<String>>();
	private final Collection<String> restrictPatterns = new HashSet<String>();
	private boolean useLocalDefaultURLs;

	public WikidataDataSourceFactory(String pattern) {
		this.factory = SparqlDataSourceFactory.service("http://query.wikidata.org/bigdata/namespace/wdq/sparql")
				.namespace("http://www.wikidata.org/").dereferencing();
		this.restrictPatterns.add(pattern);
		for (Feature feature : EnumSet.allOf(Feature.class)) {
			this.properties.put(feature, new HashSet<String>());
			this.propertiesLocalized.put(feature, new HashSet<String>());
			this.propertiesOfIds.put(feature, new HashSet<String>());
		}
	}

	public WikidataDataSourceFactory alternativeLabelProperty(String property) {
		this.properties.get(Feature.ALTERNATIVE_LABELS).add(property);
		return this;
	}

	public WikidataDataSourceFactory alternativeLabelPropertyLocalized(String property) {
		this.propertiesLocalized.get(Feature.ALTERNATIVE_LABELS).add(property);
		return this;
	}

	public WikidataDataSourceFactory broaderProperty(String property) {
		this.properties.get(Feature.BROADERS).add(property);
		return this;
	}

	public SparqlDataSource build() throws SemanticDataSourceException {
		this.factory.iriQuery(keyQuery());
		if (!this.properties.get(Feature.ALTERNATIVE_LABELS).isEmpty()
				|| !this.propertiesLocalized.get(Feature.ALTERNATIVE_LABELS).isEmpty()) {
			this.factory.alternativeLabelQuery(propertyQuery(Feature.ALTERNATIVE_LABELS));
		}
		if (!this.properties.get(Feature.BROADERS).isEmpty()) {
			this.factory.broaderQuery(propertyQuery(Feature.BROADERS));
		}
		if (!this.properties.get(Feature.DESCRIPTIONS).isEmpty()
				|| !this.propertiesLocalized.get(Feature.DESCRIPTIONS).isEmpty()) {
			this.factory.descriptionQuery(propertyQuery(Feature.DESCRIPTIONS));
		}
		if (!this.properties.get(Feature.LABELS).isEmpty() || !this.propertiesLocalized.get(Feature.LABELS).isEmpty()) {
			this.factory.labelQuery(propertyQuery(Feature.LABELS));
		}
		if (!this.properties.get(Feature.NARROWERS).isEmpty()) {
			this.factory.narrowerQuery(propertyQuery(Feature.NARROWERS));
		}
		if (!this.properties.get(Feature.REPLACEMENTS).isEmpty()) {
			this.factory.replacedByQuery(propertyQuery(Feature.REPLACEMENTS));
		}
		if (!this.properties.get(Feature.SYNONYMS).isEmpty() || !this.propertiesOfIds.get(Feature.SYNONYMS).isEmpty()) {
			this.factory.synonymQuery(propertyQuery(Feature.SYNONYMS));
		}
		if (!this.properties.get(Feature.URLS).isEmpty() || this.useLocalDefaultURLs) {
			this.factory.urlQuery(propertyQuery(Feature.URLS));
		}
		return this.factory.build();
	}

	private Element restrictPatternElement(String pattern, Node node) throws SemanticDataSourceException {
		pattern = pattern.replaceAll("\\?( |$)", node.toString() + "$1");
		return parseGroupGraphPatternSub(pattern);
	}

	public WikidataDataSourceFactory defaults() {
		return this.dereferencing().local(Locale.getDefault()).labelPropertyLocalized("rdfs:label")
				.descriptionPropertyLocalized("schema:description").alternativeLabelPropertyLocalized("skos:altLabel")
				.broaderProperty("wdt:P279|wdt:P31").synonymProperty("wdt:P1709").replacedByProperty("owl:sameAs")
				.urlLocalDefaults();
	}

	/**
	 * States that the IRIs of the build {@Code Adapter} are valid URLs.
	 * 
	 * @return this SparqlAdapterFactory
	 */
	public WikidataDataSourceFactory dereferencing() {
		this.factory.dereferencing();
		return this;
	}

	public WikidataDataSourceFactory descriptionProperty(String property) {
		this.properties.get(Feature.DESCRIPTIONS).add(property);
		return this;
	}

	public WikidataDataSourceFactory descriptionPropertyLocalized(String property) {
		this.propertiesLocalized.get(Feature.DESCRIPTIONS).add(property);
		return this;
	}

	private Query keyQuery() throws SemanticDataSourceException {
		Query query = new Query(WikidataDataSourceFactory.PROLOGUE);
		query.setQuerySelectType();
		query.addResultVar(KEY_NODE);
		query.setDistinct(true);
		ElementGroup group = new ElementGroup();
		for (String pattern : restrictPatterns) {
			group.addElement(restrictPatternElement(pattern, KEY_NODE));
		}
		query.setQueryPattern(group);
		return query;
	}

	public WikidataDataSourceFactory labelProperty(String property) {
		this.properties.get(Feature.LABELS).add(property);
		return this;
	}

	public WikidataDataSourceFactory labelPropertyLocalized(String property) {
		this.propertiesLocalized.get(Feature.LABELS).add(property);
		return this;
	}

	public WikidataDataSourceFactory local(Collection<Locale> locales) {
		this.locales.addAll(locales);
		return this;
	}

	public WikidataDataSourceFactory local(Locale locale) {
		this.locales.add(locale);
		return this;
	}

	public WikidataDataSourceFactory namespace(Collection<String> namespaces) {
		this.factory.namespace(namespaces);
		return this;
	}

	public WikidataDataSourceFactory namespace(String namespace) {
		this.factory.namespace(namespace);
		return this;
	}

	public WikidataDataSourceFactory narrowerProperty(String property) {
		this.properties.get(Feature.NARROWERS).add(property);
		return this;
	}

	public WikidataDataSourceFactory replacedByProperty(String property) {
		this.properties.get(Feature.REPLACEMENTS).add(property);
		return this;
	}

	private Query propertyQuery(Feature feature) throws SemanticDataSourceException {
		Query query = new Query(WikidataDataSourceFactory.PROLOGUE);
		query.setQuerySelectType();
		query.setDistinct(true);

		ElementUnion union = new ElementUnion();
		for (String property : properties.get(feature)) {
			switch (feature) {
			case NARROWERS:
				union.addElement(propertyQueryGroup(property, null, true, true, false, false));
				break;
			case BROADERS:
				union.addElement(propertyQueryGroup(property, null, true, true, false, false));
				break;
			case REPLACEMENTS:
				union.addElement(propertyQueryGroup(property, null, false, true, false, false));
				break;
			default:
				union.addElement(propertyQueryGroup(property, null, true, false, false, false));
				break;
			}
		}
		for (String property : propertiesLocalized.get(feature)) {
			if (locales.isEmpty()) {
				throw new SemanticDataSourceException("Locale not defined.");
			} else {
				union.addElement(propertyQueryGroup(property, null, true, false, true, false));
			}
		}
		for (String property : propertiesOfIds.get(feature)) {
			union.addElement(propertyQueryGroup(property, null, true, false, false, true));
		}

		if (this.useLocalDefaultURLs && feature == Feature.URLS) {
			union.addElement(localDefaultUrlsQueryGroup());
		}

		query.setQueryPattern(union);
		query.addResultVar(KEY_NODE);
		query.addResultVar(VALUE_NODE);

		return query;
	}

	private Element localDefaultUrlsQueryGroup() throws SemanticDataSourceException {
		if (this.locales.isEmpty()) {
			throw new SemanticDataSourceException("Locale not defined.");
		}

		final Node VALUE_LANG_NODE = NodeFactory.createVariable("valueLang");

		// get value and language
		ElementGroup group = new ElementGroup();
		ElementPathBlock triples = new ElementPathBlock();
		triples.addTriplePath(propertyTriplePath(parsePropertyPath("^schema:about")));
		triples.addTriplePath(new TriplePath(VALUE_NODE, parsePropertyPath("schema:inLanguage"), VALUE_LANG_NODE));
		group.addElement(triples);

		// filter keys
		for (String pattern : this.restrictPatterns) {
			group.addElement(restrictPatternElement(pattern, KEY_NODE));
		}

		// filter language
		Expr expression = null;
		for (Locale locale : locales) {
			Expr e = new E_LangMatches(ExprUtils.nodeToExpr(VALUE_LANG_NODE),
					ExprUtils.nodeToExpr(NodeValue.makeString(locale.toLanguageTag()).asNode()));
			if (expression == null) {
				expression = e;
			} else {
				expression = new E_LogicalOr(expression, e);
			}
		}
		group.addElement(new ElementFilter(expression));

		return group;
	}

	/**
	 * Returns an {@link ElementGroup} of a {@link Query} for a given property
	 * path.
	 * 
	 * @param propertyPath
	 *            the property path
	 * @param keyScopeFilter
	 *            if {@code true}, the keys are affected by the entity
	 *            restrictions
	 * @param valueScopeFilter
	 *            if {@code true}, the values are affected by the entity
	 *            restrictions
	 * @param valueLocalFilter
	 *            if {@code true}, the values are affected by the locale filter
	 * @param idProperty
	 *            if {@code true}, the property is interpreted as a id property
	 * @return {@link ElementGroup} of and {@link Query} for a given property
	 *         path
	 * @throws SemanticDataSourceException
	 */
	private ElementGroup propertyQueryGroup(String propertyPath, Collection<String> valueFilterPatterns,
			boolean keyScopeFilter, boolean valueScopeFilter, boolean valueLocalFilter, boolean idProperty)
			throws SemanticDataSourceException {
		// validate parameter
		if (valueLocalFilter && (valueScopeFilter || idProperty)) {
			throw new IllegalArgumentException();
		}

		ElementGroup group = new ElementGroup();

		if (idProperty) {

			Node id = NodeFactory.createVariable("id");
			Node formatter = NodeFactory.createVariable("formatter");

			ElementPathBlock triples = new ElementPathBlock();
			triples.addTriple(new Triple(KEY_NODE, parseIri(propertyPath), id));
			triples.addTriple(
					new Triple(parseIri(propertyPath.replaceFirst("^wdt?:", "wd:")), parseIri("wdt:P1921"), formatter));
			group.addElement(triples);
			Expr replace = new E_StrReplace(ExprUtils.nodeToExpr(formatter), new NodeValueString("\\$1"),
					ExprUtils.nodeToExpr(id), new NodeValueString("i"));
			Expr iri = new E_IRI(replace);
			Element bind = new ElementBind(ExprUtils.nodeToExpr(VALUE_NODE).asVar(), iri);
			group.addElement(bind);

		} else {

			ElementPathBlock triples = new ElementPathBlock();
			triples.addTriplePath(propertyTriplePath(parsePropertyPath(propertyPath)));
			group.addElement(triples);

		}

		for (String pattern : this.restrictPatterns) {
			if (keyScopeFilter) {
				group.addElement(restrictPatternElement(pattern, KEY_NODE));
			}
			if (valueScopeFilter) {
				group.addElement(restrictPatternElement(pattern, VALUE_NODE));
			}
		}

		if (valueLocalFilter) {
			group.addElement(valueLocalFilter());
		}

		if (valueFilterPatterns != null) {
			for (String valueFilterPattern : valueFilterPatterns) {
				group.addElement(restrictPatternElement(valueFilterPattern, VALUE_NODE));
			}
		}

		return group;
	}

	private TriplePath propertyTriplePath(Path propertyPath) {
		return new TriplePath(KEY_NODE, propertyPath, VALUE_NODE);
	}

	public WikidataDataSourceFactory restrict(String pattern) throws SemanticDataSourceException {
		this.restrictPatterns.add(pattern);
		return this;
	}

	public WikidataDataSourceFactory scope(Collection<IRI> scopes) {
		this.factory.scope(scopes);
		return this;
	}

	public WikidataDataSourceFactory scope(IRI scope) {
		this.factory.scope(scope);
		return this;
	}

	public WikidataDataSourceFactory synonymIDProperty(String property) {
		this.propertiesOfIds.get(Feature.SYNONYMS).add(property);
		return this;
	}

	public WikidataDataSourceFactory synonymProperty(String property) {
		this.properties.get(Feature.SYNONYMS).add(property);
		return this;
	}

	public WikidataDataSourceFactory urlProperty(String property) {
		this.properties.get(Feature.URLS).add(property);
		return this;
	}

	public WikidataDataSourceFactory urlLocalDefaults() {
		this.useLocalDefaultURLs = true;
		return this;
	}

	private ElementFilter valueLocalFilter() {
		Expr expression = null;
		for (Locale locale : locales) {
			Expr e = new E_LangMatches(new E_Lang(ExprUtils.nodeToExpr(VALUE_NODE)),
					ExprUtils.nodeToExpr(NodeValue.makeString(locale.toLanguageTag()).asNode()));
			if (expression == null) {
				expression = e;
			} else {
				expression = new E_LogicalOr(expression, e);
			}
		}
		return new ElementFilter(expression);
	}

}
