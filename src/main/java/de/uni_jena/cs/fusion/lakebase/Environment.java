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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceManager;
import de.uni_jena.cs.fusion.semantic.datasource.cache.database.DatabaseCacheWrapper;
import de.uni_jena.cs.fusion.semantic.datasource.datetime.DateTimeDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.fallback.KeywordFallbackWrapper;
import de.uni_jena.cs.fusion.semantic.datasource.gfbio.TerminologyServerDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.lakebase.Parameter;
import de.uni_jena.cs.fusion.semantic.datasource.lakebase.StudySite;
import de.uni_jena.cs.fusion.semantic.datasource.ontology.OntologyDataSourceFactory;
import de.uni_jena.cs.fusion.semantic.datasource.wikidata.WikidataDataSourceFactory;
import de.uni_jena.cs.fusion.semantic.datasource.worms.Worms;
import de.uni_jena.cs.fusion.util.maintainer.Maintainer;

/**
 * @since 0.1
 *
 */
public class Environment implements Closeable {

	private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// logger cache to avoid garbage collection of configured loggers
	private final static Collection<java.util.logging.Logger> configuredLoggers = new ArrayList<java.util.logging.Logger>();

	private final ListeningScheduledExecutorService executor;
	private final Annotator annotator;
	private final Completer suggestor;
	private final Searcher searcher;
	private final SemanticDataSource semanticDataSource;
	private final DataSource databaseManager;
	private final HierarchyManager hierarchyManager;
	private final AnnotationManager annotationManager;
	private final ConceptManager conceptManager;

	private final Maintainer dailyMaintainer;
	private final Maintainer hourlyMaintainer;

	public Environment(DataSource dataSource) throws Exception {
		logger.info("Initialization started.");

		/*
		 * Set SAXParserFactory to default factory to avoid log warning by OwlApi.
		 * Apache Jena ARQ brings a SAXParserFactory implementation that does not
		 * support all features used by OwlApi. See:
		 * https://docs.oracle.com/javase/8/docs/api/javax/xml/parsers/
		 * SAXParserFactory.html#newInstance--
		 */
		// TODO clean solution: http://projects.lidalia.org.uk/slf4j-test/
		System.setProperty("javax.xml.parsers.SAXParserFactory",
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

		// filter some logging noise
		System.setProperty("org.jooq.no-logo", "true");
		List<String> noisyLoggerNames = Lists.newArrayList("org.apache.http.client.protocol.ResponseProcessCookies",
				"org.semanticweb.owlapi.rdf.rdfxml.parser.OWLRDFConsumer");
		for (String noisyLoggerName : noisyLoggerNames) {
			java.util.logging.Logger noisyLogger = java.util.logging.Logger.getLogger(noisyLoggerName);
			noisyLogger.setLevel(Level.SEVERE);
			configuredLoggers.add(noisyLogger);
		}

		Collection<String> languages = new ArrayList<String>(3);
		languages.add("");
		languages.add("en");
		languages.add("de");
		Collection<Locale> locales = new ArrayList<Locale>(2);
		locales.add(new Locale("en"));
		locales.add(new Locale("de"));

		ThreadFactory deamonThreadFactory = new ThreadFactory() {
			ThreadFactory threadFactory = MoreExecutors.platformThreadFactory();
			// ThreadFactory threadFactory = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = threadFactory.newThread(runnable);
				thread.setDaemon(true);
				return thread;
			}
		};
		// TODO adjust thread pool size
		// NOTE: thread pool size should be large enough to allow some threads
		// to wait for service responses
		executor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(100, deamonThreadFactory));
		TerminologyServerDataSource.setExecutor(executor);

		databaseManager = new DatabaseManager(dataSource);

		// initialize adapter
		SemanticDataSourceManager semanticDataSourceManager = new SemanticDataSourceManager(executor);
		// WORMS with cache (604800 = one week)
		SemanticDataSource worms = new Worms(false).useExternalIRIs("ncbi");
		DatabaseCacheWrapper wormsCache = DatabaseCacheWrapper.wrap(worms, databaseManager, "semantic.adapter_cache",
				604800);
		semanticDataSourceManager.registerAdapter(wormsCache);
		// OM 2
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory
				.ontology(IRI.create("https://raw.githubusercontent.com/HajoRijgersberg/OM/master/om-2.0.rdf"))
				.fallback(file("ontology/om-2.0.rdf")).reloading().scope(Scope.unit.getIris())
				.scope(Scope.quantityKind.getIris()).language(languages).labelPropertyRdfsLabel()
				.descriptionPropertyRdfsComment()
				.alternativLabelProperty("http://www.ontology-of-units-of-measure.org/resource/om-2/symbol")
				.alternativLabelProperty("http://www.ontology-of-units-of-measure.org/resource/om-2/abbreviation")
				.alternativLabelProperty("http://www.ontology-of-units-of-measure.org/resource/om-2/alternativeLabel")
				.alternativLabelProperty("http://www.ontology-of-units-of-measure.org/resource/om-2/alternativeSymbol")
				.alternativLabelProperty("http://www.ontology-of-units-of-measure.org/resource/om-2/unofficialLabel")
				.alternativLabelProperty(
						"http://www.ontology-of-units-of-measure.org/resource/om-2/unofficialAbbreviation")
				.build());
		// ENVO
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory
				.ontology(IRI.create("http://purl.obolibrary.org/obo/envo.owl")).fallback(file("ontology/envo.owl"))
				.reloading().language(languages).dereferencing().scope(Scope.biology.getIris()).labelPropertyRdfsLabel()
				.descriptionProperty("http://purl.obolibrary.org/obo/IAO_0000115")
				.alternativLabelProperty("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym").build());
		// OWL-Time
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/time.owl"))
				.scope(Scope.datetime.getIris()).language(languages).labelPropertyRdfsLabel()
				.labelPropertySkosPrefLabel().descriptionPropertySkosDefinition().build());
		// mapping OWL-Time and OM 2
		semanticDataSourceManager
				.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/mappingTimeAndOM2.owl")).build());
		// mapping PATO (partially used in ENVO) and OM 2
		semanticDataSourceManager
				.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/mappingPatoAndOm2.ttl")).build());
		// months
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/months.owl"))
				.language(languages).scope(Scope.datetime.getIris()).labelPropertyRdfsLabel().build());
		// daytime
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/daytime.owl"))
				.language(languages).scope(Scope.datetime.getIris()).labelPropertyRdfsLabel().build());
		// periods
		semanticDataSourceManager.registerAdapter(OntologyDataSourceFactory.ontology(file("ontology/periods.owl"))
				.language(languages).scope(Scope.datetime.getIris()).labelPropertyRdfsLabel()
				.broaderPropertyDctermsIsPartOf().build());
		// GEONAMES
		semanticDataSourceManager.registerAdapter(new TerminologyServerDataSource("GEONAMES"));
		// CHEBI
		semanticDataSourceManager.registerAdapter(new TerminologyServerDataSource("CHEBI"));
		// datetime
		semanticDataSourceManager.registerAdapter(new DateTimeDataSource());
		// Wikidata lakes
		semanticDataSourceManager.registerAdapter(WikidataDataSourceFactory
				// lakes in Germany
				.pattern("{{? wdt:P31/wdt:P279* wd:Q23397; wdt:P17 wd:Q183.} UNION {? wdt:P279* wd:Q23397}}").defaults()
				.synonymIDProperty("wdt:P1566").local(locales).namespace("http://sws.geonames.org/")
				.scope(Scope.location.getIris()).build());
		// study site
		StudySite studySites = new StudySite(databaseManager);
		semanticDataSourceManager.registerAdapter(studySites);
		// parameter
		Parameter parameter = new Parameter(databaseManager);
		semanticDataSourceManager.registerAdapter(parameter);

		// fallback wrapper
		semanticDataSource = new KeywordFallbackWrapper(0.1, databaseManager, "semantic.concept", "concept_iri",
				semanticDataSourceManager);

		// initialize services
		annotator = new Annotator(semanticDataSource);
		suggestor = new Completer(semanticDataSource);
		searcher = new Searcher(databaseManager);
		conceptManager = new ConceptManager(databaseManager);
		annotationManager = new AnnotationManager(databaseManager, semanticDataSource, conceptManager);
		hierarchyManager = new HierarchyManager(databaseManager, semanticDataSource, conceptManager);

		// initialize maintenance
		hourlyMaintainer = new Maintainer(executor, "hourly");
		dailyMaintainer = new Maintainer(executor, "daily");
		hourlyMaintainer.register(wormsCache).register(hierarchyManager).register(studySites).register(parameter)
				.schedule(1, TimeUnit.HOURS);
		dailyMaintainer.register(semanticDataSourceManager).register(annotationManager).schedule(1, TimeUnit.DAYS);

		logger.info("Initialization completed.");
	}

	@Override
	public void close() throws IOException {
		logger.info("shutdown");
		hourlyMaintainer.close();
		dailyMaintainer.close();
		executor.shutdownNow();
	}

	public ListeningScheduledExecutorService getExecutor() {
		return executor;
	}

	public Annotator getAnnotator() {
		return annotator;
	}

	public Completer getSuggestor() {
		return suggestor;
	}

	public SemanticDataSource getSemanticDataSource() {
		return semanticDataSource;
	}

	public DataSource getDatabaseManager() {
		return databaseManager;
	}

	public HierarchyManager getHierarchyManager() {
		return hierarchyManager;
	}

	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public static File file(String path) {
		return new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());
	}

}
