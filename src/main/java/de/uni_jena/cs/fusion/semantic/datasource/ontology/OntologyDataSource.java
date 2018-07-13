package de.uni_jena.cs.fusion.semantic.datasource.ontology;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_jena.cs.fusion.collection.LinkedListTrieMap;
import de.uni_jena.cs.fusion.collection.Trie;
import de.uni_jena.cs.fusion.collection.TrieMap;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllBroadersUsingBroaders;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceProvidingAllNarrowersUsingNarrowers;
import de.uni_jena.cs.fusion.similarity.JaroWinklerSimilarityMatcher;
import de.uni_jena.cs.fusion.similarity.TrieJaroWinklerSimilarityMatcher;
import de.uni_jena.cs.fusion.util.maintainer.Maintainable;
import de.uni_jena.cs.fusion.util.maintainer.MaintenanceException;

/**
 * TODO: rework switch from Stream to collection
 * 
 * TODO improve performance: search for multiple entities at the same time
 * 
 * @since 0.1
 *
 */
public class OntologyDataSource implements SemanticDataSourceProvidingAllBroadersUsingBroaders,
		SemanticDataSourceProvidingAllNarrowersUsingNarrowers, Maintainable {

	static enum PropertySubject {
		BROADER, NARROWER, SYNONYM, LABEL, ALTERNATIVE_LABEL, DESCRIPTION, REPLACED_BY
	}

	private static final Logger log = LoggerFactory.getLogger(OntologyDataSource.class);

	Optional<OWLOntologyDocumentSource> ontologyFallbackSource = Optional.empty();
	OWLOntologyDocumentSource ontologySource;
	OWLOntologyManager manager;
	OWLOntology ontology;

	/**
	 * If true, the IRIs of the ontology are valid URLs.
	 */
	boolean dereferencing;
	boolean reloading;
	boolean initialized;
	Map<PropertySubject, List<IRI>> propertyIRIs = new HashMap<PropertySubject, List<IRI>>();

	Collection<IRI> scopes = new HashSet<IRI>();
	Collection<IRI> scopesUnmodifiable = Collections.unmodifiableCollection(scopes);
	Set<String> namespaces = new HashSet<String>();
	Set<String> namespacesUnmodifiable = Collections.unmodifiableSet(namespaces);
	Set<String> languages = new HashSet<String>();

	TrieMap<Set<IRI>> labelIndex;
	private double matchThreshold = 0.95;
	JaroWinklerSimilarityMatcher<Set<IRI>> matcher;

	/**
	 * language filter
	 * 
	 * if no language is configured, every language passes
	 */
	Predicate<OWLLiteral> languageFilter = literal -> this.languages.isEmpty()
			|| this.languages.contains(literal.getLang());
	Map<PropertySubject, List<OWLAnnotationProperty>> annotationProperties;
	Map<PropertySubject, List<OWLObjectProperty>> objectProperties;
	Map<PropertySubject, List<OWLDataProperty>> dataProperties;

	OntologyDataSource(OWLOntologyDocumentSource ontologySource) {
		this.ontologySource = ontologySource;

		// initialize subject property IRIs collections
		for (PropertySubject subject : EnumSet.allOf(PropertySubject.class)) {
			this.propertyIRIs.put(subject, new ArrayList<IRI>());
		}
	}

	synchronized void load() throws SemanticDataSourceException {
		if (!this.initialized || this.reloading) {
			OntologyDataSource.log.info("Loading ontology from \"" + this.ontologySource.getDocumentIRI() + "\".");

			// backup current state
			OWLOntologyManager owlManagerBackup = this.manager;
			OWLOntology ontologyBackup = this.ontology;

			try {
				// load manager and ontology
				this.manager = OWLManager.createConcurrentOWLOntologyManager();
				try {
					this.ontology = this.manager.loadOntologyFromOntologyDocument(this.ontologySource);
				} catch (OWLOntologyCreationException | OWLRuntimeException e) {
					OntologyDataSource.log
							.error("Failed to load ontology from \"" + this.ontologySource.getDocumentIRI() + "\".", e);
					if (!this.initialized && this.ontologyFallbackSource.isPresent()) {
						OntologyDataSource.log.info("Instead loading ontology from \""
								+ this.ontologyFallbackSource.get().getDocumentIRI() + "\".");
						try {
							this.ontology = this.manager
									.loadOntologyFromOntologyDocument(this.ontologyFallbackSource.get());
						} catch (OWLOntologyCreationException | OWLRuntimeException eFallback) {
							OntologyDataSource.log.error("Failed to load ontology from \""
									+ this.ontologyFallbackSource.get().getDocumentIRI() + "\".");
							throw eFallback;
						}
					} else {
						throw e;
					}
				}

				// set initialized (required to be set before following
				// initialization)
				this.initialized = true;

				// initialize subject properties collections
				this.annotationProperties = new EnumMap<PropertySubject, List<OWLAnnotationProperty>>(
						PropertySubject.class);
				this.objectProperties = new EnumMap<PropertySubject, List<OWLObjectProperty>>(PropertySubject.class);
				this.dataProperties = new EnumMap<PropertySubject, List<OWLDataProperty>>(PropertySubject.class);
				for (PropertySubject subject : EnumSet.allOf(PropertySubject.class)) {
					this.annotationProperties.put(subject, new ArrayList<OWLAnnotationProperty>());
					this.objectProperties.put(subject, new ArrayList<OWLObjectProperty>());
					this.dataProperties.put(subject, new ArrayList<OWLDataProperty>());
					for (IRI iri : this.propertyIRIs.get(subject)) {
						// create IRI filter
						Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iri);
						// add property to collection by type
						this.annotationProperties.get(subject).addAll(this.ontology.annotationPropertiesInSignature()
								.filter(iriFilter).collect(Collectors.toCollection(ArrayList::new)));
						this.objectProperties.get(subject).addAll(this.ontology.objectPropertiesInSignature()
								.filter(iriFilter).collect(Collectors.toCollection(ArrayList::new)));
						this.dataProperties.get(subject).addAll(this.ontology.dataPropertiesInSignature()
								.filter(iriFilter).collect(Collectors.toCollection(ArrayList::new)));
					}
				}

				// cache namespaces
				OWLDocumentFormat ontologyFormat = this.manager.getOntologyFormat(this.ontology);
				this.namespaces.clear();
				if (ontologyFormat.isPrefixOWLDocumentFormat()) {
					this.namespaces
							.addAll(ontologyFormat.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap().values());
				}

				// initialize matcher
				this.labelIndex = new LinkedListTrieMap<Set<IRI>>();
				for (IRI iri : this.getSignature()) {
					for (String label : this.getLabels(iri)) {
						String caseInsensitiveLabel = label.toLowerCase();
						this.labelIndex.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
						this.labelIndex.get(caseInsensitiveLabel).add(iri);
					}
					for (String label : this.getAlternativeLabels(iri)) {
						String caseInsensitiveLabel = label.toLowerCase();
						this.labelIndex.putIfAbsent(caseInsensitiveLabel, new HashSet<IRI>());
						this.labelIndex.get(caseInsensitiveLabel).add(iri);
					}
				}
				this.matcher = new TrieJaroWinklerSimilarityMatcher<Set<IRI>>(this.labelIndex);

			} catch (OWLRuntimeException | OWLOntologyCreationException e) {
				this.manager = owlManagerBackup;
				this.ontology = ontologyBackup;
				if (this.reloading || !this.initialized) {
					OntologyDataSource.log.info("Trying again during next maintenance.");
					if (this.initialized) {
						OntologyDataSource.log.info("Previous state restored.");
					}
				}
			}
		}
	}

	@Override
	public Collection<IRI> getSignature() {
		if (this.initialized) {

			// collect ontology classes iris
			Stream<IRI> classes = this.ontology.classesInSignature().map(OWLClass::getIRI);

			// collect ontology individuals iris
			Stream<IRI> individuals = this.ontology.individualsInSignature().map(OWLIndividual::asOWLNamedIndividual)
					.map(OWLNamedIndividual::getIRI);

			// return concatenated streams (
			// http://stackoverflow.com/a/37436520/3637482 )
			return Stream.of(classes, individuals).flatMap(Function.identity())
					.collect(Collectors.toCollection(ArrayList::new));
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Collection<String> getAlternativeLabels(IRI iri) {
		return getPropertyValues(iri, PropertySubject.ALTERNATIVE_LABEL);
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) {
		if (this.initialized) {

			// create iri filter
			Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iri);

			// initialize result collection
			Collection<IRI> results = new HashSet<IRI>();

			// get named individual
			Optional<OWLNamedIndividual> individualEntity = this.ontology.individualsInSignature().filter(iriFilter)
					.findFirst();

			if (individualEntity.isPresent()) {

				// iterate relevant object properties
				for (OWLObjectProperty objectProperty : this.objectProperties.get(PropertySubject.BROADER)) {
					results.addAll(getObjectPropertyIris(individualEntity.get(), objectProperty, this.ontology));
				}
				
				// iterate relevant inverse object properties
				for (OWLObjectProperty objectProperty : this.objectProperties.get(PropertySubject.NARROWER)) {
					results.addAll(getObjectPropertyInverseIris(individualEntity.get(), objectProperty, this.ontology));
				}

				// iterate relevant data properties
				for (OWLDataProperty dataProperty : this.dataProperties.get(PropertySubject.BROADER)) {
					results.addAll(getDataPropertyValuesAsIri(individualEntity.get(), dataProperty, this.ontology));
				}

				// rdf:type
				results.addAll(getTypeIris(individualEntity.get(), this.ontology));
			}

			// get class
			Optional<OWLClass> classEntity = this.ontology.classesInSignature().filter(iriFilter).findFirst();

			if (classEntity.isPresent()) {

				// inverse rdfs:subClassOf
				results.addAll(getSuperClassIris(classEntity.get(), this.ontology));
			}

			// iterate relevant annotation properties
			for (OWLAnnotationProperty annotationProperty : this.annotationProperties.get(PropertySubject.BROADER)) {
				results.addAll(getAnnotationPropertyIris(iri, annotationProperty, this.ontology));
			}

			return results;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public int getDataUID() {
		return this.ontology.getOntologyID().hashCode();
	}

	@Override
	public Collection<String> getDescriptions(IRI iri) throws SemanticDataSourceException {
		return getPropertyValues(iri, PropertySubject.DESCRIPTION);
	}

	@Override
	public Collection<String> getLabels(IRI iri) {
		return getPropertyValues(iri, PropertySubject.LABEL);
	}

	@Override
	public Map<IRI, Double> getMatches(String term) {
		if (this.initialized) {
			term = term.toLowerCase();
			Map<IRI, Double> result = new HashMap<IRI, Double>();
			Map<Set<IRI>, Double> match = this.matcher.match(this.matchThreshold, term);
			for (Set<IRI> iris : match.keySet()) {
				for (IRI iri : iris) {
					result.put(iri, match.get(iris));
				}
			}
			return result;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public Collection<String> getNamespaces() {
		return this.namespacesUnmodifiable;
	}

	@Override
	public Collection<IRI> getNarrowers(IRI iri) {
		if (this.initialized) {

			// create iri filter
			Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iri);

			// initialize result collection
			Collection<IRI> results = new HashSet<IRI>();

			// get named individual
			Optional<OWLNamedIndividual> individualEntity = this.ontology.individualsInSignature().filter(iriFilter)
					.findFirst();

			if (individualEntity.isPresent()) {

				// iterate relevant object properties
				for (OWLObjectProperty objectProperty : this.objectProperties.get(PropertySubject.NARROWER)) {
					results.addAll(getObjectPropertyIris(individualEntity.get(), objectProperty, this.ontology));
				}
				
				// iterate relevant inverse object properties
				for (OWLObjectProperty objectProperty : this.objectProperties.get(PropertySubject.BROADER)) {
					results.addAll(getObjectPropertyInverseIris(individualEntity.get(), objectProperty, this.ontology));
				}

				// iterate relevant data properties
				for (OWLDataProperty dataProperty : this.dataProperties.get(PropertySubject.NARROWER)) {
					results.addAll(getDataPropertyValuesAsIri(individualEntity.get(), dataProperty, this.ontology));
				}
			}

			// get class
			Optional<OWLClass> classEntity = this.ontology.classesInSignature().filter(iriFilter).findFirst();

			if (classEntity.isPresent()) {

				// rdfs:subClassOf
				results.addAll(getSubClassIris(classEntity.get(), this.ontology));

				// inverse rdf:type
				results.addAll(getIndividualIris(classEntity.get(), this.ontology));
			}

			// iterate relevant annotation properties
			for (OWLAnnotationProperty annotationProperty : this.annotationProperties.get(PropertySubject.NARROWER)) {
				results.addAll(getAnnotationPropertyIris(iri, annotationProperty, this.ontology));
			}

			return results;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Collection<IRI> getReplacedBy(IRI iri) throws SemanticDataSourceException {
		if (this.initialized && this.isDeprecated(iri)) {
			return getPropertyIris(iri, PropertySubject.REPLACED_BY);
		} else {
			return Collections.emptySet();
		}
	}

	public Collection<IRI> getPropertyIris(IRI iri, PropertySubject subject) throws SemanticDataSourceException {
		// create iri filter
		Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iri);

		// initialize result collection
		Collection<IRI> results = new HashSet<IRI>();

		// get named individual
		Optional<OWLNamedIndividual> individualEntity = this.ontology.individualsInSignature().filter(iriFilter)
				.findFirst();

		if (individualEntity.isPresent()) {

			// iterate relevant object properties
			for (OWLObjectProperty objectProperty : this.objectProperties.get(subject)) {
				results.addAll(getObjectPropertyIris(individualEntity.get(), objectProperty, this.ontology));
			}

			// iterate relevant data properties
			for (OWLDataProperty dataProperty : this.dataProperties.get(subject)) {
				results.addAll(getDataPropertyValuesAsIri(individualEntity.get(), dataProperty, this.ontology));
			}
		}

		// iterate relevant annotation properties
		for (OWLAnnotationProperty annotationProperty : this.annotationProperties.get(subject)) {
			results.addAll(getAnnotationPropertyIris(iri, annotationProperty, this.ontology));
		}

		return results;
	}

	@Override
	public Collection<IRI> getScopes() {
		return this.scopesUnmodifiable;
	}

	@Override
	public Map<IRI, String> getSuggestions(String stump) throws SemanticDataSourceException {
		if (this.initialized) {
			Map<IRI, String> result = new HashMap<IRI, String>();
			Trie<Set<IRI>> suggestionTrie = this.labelIndex.getSubTrie(stump.toLowerCase());
			if (suggestionTrie != null) {
				Iterator<? extends Trie<Set<IRI>>> suggestionIterator = suggestionTrie.populatedNodeIterator();
				for (int i = 0; i < 10 && suggestionIterator.hasNext(); i++) {
					Trie<Set<IRI>> current = suggestionIterator.next();
					for (IRI value : current.value()) {
						result.put(value, current.key());
					}
				}
			}
			return result;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public Collection<IRI> getSynonyms(IRI iri) {
		if (this.initialized) {

			// initialize result collections
			Collection<IRI> results = new HashSet<IRI>();
			Collection<IRI> synonyms = new HashSet<IRI>();

			// initialize process queue
			Collection<IRI> irisToProcess = new ArrayList<IRI>();
			irisToProcess.add(iri);

			while (!irisToProcess.isEmpty()) {

				// empty synonyms
				synonyms.clear();

				// get current iri to process
				IRI iriToProcess = irisToProcess.iterator().next();

				// create iri filter
				Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iriToProcess);

				// get named individual
				Optional<OWLNamedIndividual> individualEntity = this.ontology.individualsInSignature().filter(iriFilter)
						.findFirst();

				if (individualEntity.isPresent()) {

					// iterate relevant object properties
					for (OWLObjectProperty objectProperty : this.objectProperties.get(PropertySubject.SYNONYM)) {
						synonyms.addAll(getObjectPropertyIris(individualEntity.get(), objectProperty, this.ontology));
					}

					// iterate relevant data properties
					for (OWLDataProperty dataProperty : this.dataProperties.get(PropertySubject.SYNONYM)) {
						synonyms.addAll(
								getDataPropertyValuesAsIri(individualEntity.get(), dataProperty, this.ontology));
					}

					// owl:sameAs
					synonyms.addAll(getSameIndividualIris(individualEntity.get(), this.ontology));
				}

				// get class
				Optional<OWLClass> classEntity = this.ontology.classesInSignature().filter(iriFilter).findFirst();

				if (classEntity.isPresent()) {

					// owl:equivalentClass
					synonyms.addAll(getEquivalentClassIris(classEntity.get(), this.ontology));
				}

				// iterate relevant annotation properties
				for (OWLAnnotationProperty annotationProperty : this.annotationProperties
						.get(PropertySubject.SYNONYM)) {
					synonyms.addAll(getAnnotationPropertyIris(iriToProcess, annotationProperty, this.ontology));
				}

				// iterate synonyms
				for (IRI synonym : synonyms) {

					// add synonym to results
					if (results.add(synonym)) {
						// it is a new synonym

						// add synonym to process queue
						irisToProcess.add(synonym);
					}
				}

				// remove current iri from queue
				irisToProcess.remove(iriToProcess);
			}

			// remove given concept iri
			results.remove(iri);

			return results;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public List<URL> getUrls(IRI iri) throws SemanticDataSourceException {
		if (this.initialized) {
			try {
				if (this.dereferencing && this.isPresent(iri)) {
					return Collections.singletonList(iri.toURI().toURL());
				} else {
					return Collections.emptyList();
				}
			} catch (MalformedURLException e) {
				throw new SemanticDataSourceException("Failed to get concept URL.", e);
			}
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean isPresent(IRI iri) {
		return this.ontology.containsEntityInSignature(iri);
	}

	@Override
	public boolean isDeprecated(IRI iri) throws SemanticDataSourceException {
		if (this.initialized) {
			return this.ontology.annotationAssertionAxioms(iri)
					.anyMatch(OWLAnnotationAssertionAxiom::isDeprecatedIRIAssertion);
		} else {
			return false;
		}
	}

	@Override
	public void maintain() throws MaintenanceException {
		try {
			this.load();
		} catch (SemanticDataSourceException e) {
			throw new MaintenanceException(e);
		}
	}

	@Override
	public boolean providingAllBroaders() {
		return true;
	}

	@Override
	public boolean providingAllNarrowers() {
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
	public boolean providingDeprecation() {
		return true;
	}

	@Override
	public boolean providingDescriptions() {
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
	public boolean providingNarrowers() {
		return true;
	}

	@Override
	public boolean providingSignature() {
		return true;
	}

	@Override
	public boolean providingSuggest() {
		return true;
	}

	@Override
	public boolean providingSynonyms() {
		return true;
	}

	@Override
	public boolean providingURLs() {
		return true;
	}

	@Override
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
	}

	private ArrayList<IRI> getAnnotationPropertyIris(IRI iri, OWLAnnotationProperty annotationProperty,
			OWLOntology ontology) {
		return EntitySearcher.getAnnotations(iri, ontology, annotationProperty).map(OWLAnnotation::getValue)
				.map(OWLAnnotationValue::asIRI).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<String> getAnnotationPropertyValues(IRI iri, OWLAnnotationProperty annotationProperty,
			OWLOntology ontology) {
		return EntitySearcher.getAnnotations(iri, ontology, annotationProperty).map(OWLAnnotation::getValue)
				.map(OWLAnnotationValue::asLiteral).filter(Optional::isPresent).map(Optional::get)
				.filter(this.languageFilter).map(OWLLiteral::getLiteral)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<String> getDataPropertyValues(OWLNamedIndividual individual, OWLDataProperty dataProperty,
			OWLOntology ontology) {
		return EntitySearcher.getDataPropertyValues(individual, dataProperty, ontology).filter(this.languageFilter)
				.map(OWLLiteral::getLiteral).collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getDataPropertyValuesAsIri(OWLNamedIndividual individual, OWLDataProperty dataProperty,
			OWLOntology ontology) {
		return EntitySearcher.getDataPropertyValues(individual, dataProperty, ontology).filter(this.languageFilter)
				.map(OWLLiteral::getLiteral).map(IRI::create).collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getEquivalentClassIris(OWLClass classEntity, OWLOntology ontology) {
		return EntitySearcher.getEquivalentClasses(classEntity, ontology)
				.filter(equivalentClassExpression -> !equivalentClassExpression.isAnonymous())
				.map(OWLClassExpression::asOWLClass).map(OWLClass::getIRI)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getIndividualIris(OWLClass classEntity, OWLOntology ontology) {
		return EntitySearcher.getIndividuals(classEntity, ontology)
				.filter(individualExpression -> !individualExpression.isAnonymous())
				.map(OWLIndividual::asOWLNamedIndividual).map(OWLNamedIndividual::getIRI)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getObjectPropertyIris(OWLNamedIndividual indivudal, OWLObjectProperty objectProperty,
			OWLOntology ontology) {
		return EntitySearcher.getObjectPropertyValues(indivudal, objectProperty, ontology)
				.filter(individualExpression -> !individualExpression.isAnonymous())
				.map(OWLIndividual::asOWLNamedIndividual).map(OWLNamedIndividual::getIRI)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getObjectPropertyInverseIris(OWLNamedIndividual indivudal, OWLObjectProperty objectProperty,
			OWLOntology ontology) {
		return ontology.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
				.filter(axiom -> axiom.getObject().equals(indivudal) && axiom.getProperty().equals(objectProperty))
				.map(OWLObjectPropertyAssertionAxiom::getSubject)
				.filter(individualExpression -> !individualExpression.isAnonymous())
				.map(OWLIndividual::asOWLNamedIndividual).map(OWLNamedIndividual::getIRI)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private Collection<String> getPropertyValues(IRI iri, PropertySubject subject) {
		if (this.initialized) {
			// create iri filter
			Predicate<OWLEntity> iriFilter = entity -> entity.getIRI().equals(iri);

			// initialize result collection
			Collection<String> results = new HashSet<String>();

			// get named individual
			Optional<OWLNamedIndividual> individualEntity = this.ontology.individualsInSignature().filter(iriFilter)
					.findFirst();

			if (individualEntity.isPresent()) {
				// iterate relevant data properties
				for (OWLDataProperty dataProperty : this.dataProperties.get(subject)) {
					results.addAll(getDataPropertyValues(individualEntity.get(), dataProperty, this.ontology));
				}
			}

			// iterate relevant annotation properties
			for (OWLAnnotationProperty annotationProperty : this.annotationProperties.get(subject)) {
				results.addAll(getAnnotationPropertyValues(iri, annotationProperty, ontology));
			}

			return results;
		} else {
			return Collections.emptySet();
		}
	}

	private ArrayList<IRI> getSameIndividualIris(OWLNamedIndividual individualEntity, OWLOntology ontology) {
		return EntitySearcher.getSameIndividuals(individualEntity, ontology)
				.filter(sameIndividual -> !sameIndividual.isAnonymous()).map(OWLIndividual::asOWLNamedIndividual)
				.map(OWLNamedIndividual::getIRI).collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getSubClassIris(OWLClass classEntity, OWLOntology ontology) {
		return EntitySearcher.getSubClasses(classEntity, ontology)
				.filter(subClassExpression -> !subClassExpression.isAnonymous()).map(OWLClassExpression::asOWLClass)
				.map(OWLClass::getIRI).collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getSuperClassIris(OWLClass classEntity, OWLOntology ontology) {
		return EntitySearcher.getSuperClasses(classEntity, ontology)
				.filter(subClassExpression -> !subClassExpression.isAnonymous()).map(OWLClassExpression::asOWLClass)
				.map(OWLClass::getIRI).collect(Collectors.toCollection(ArrayList::new));
	}

	private ArrayList<IRI> getTypeIris(OWLNamedIndividual individualEntity, OWLOntology ontology) {
		return EntitySearcher.getTypes(individualEntity, ontology)
				.filter(individualExpression -> !individualExpression.isAnonymous()).map(OWLClassExpression::asOWLClass)
				.map(OWLClass::getIRI).collect(Collectors.toCollection(ArrayList::new));
	}
}
