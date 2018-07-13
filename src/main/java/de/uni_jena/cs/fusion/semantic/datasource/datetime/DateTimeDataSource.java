package de.uni_jena.cs.fusion.semantic.datasource.datetime;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Lists;

import de.uni_jena.cs.fusion.collection.LinkedListTrieMap;
import de.uni_jena.cs.fusion.collection.TrieMap;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.similarity.JaroWinklerSimilarityMatcher;
import de.uni_jena.cs.fusion.similarity.TrieJaroWinklerSimilarityMatcher;

/**
 * 
 * 
 * 
 * @author Jan Martin Keil
 *
 */
public class DateTimeDataSource implements SemanticDataSource {

	/**
	 * Internal representation of a DateTime
	 *
	 */
	private static class DateTime {
		TemporalAccessor temporal;
		/**
		 * Pattern used to instantiate the {@link #temporal}.
		 */
		String pattern;

		DateTime(TemporalAccessor temporal, String pattern) {
			this.temporal = temporal;
			this.pattern = pattern;
		}
	}

	private final static JaroWinklerSimilarityMatcher<Callable<DateTime>> KEYWORD_MATCHER;
	static {
		String[] lastSynonyms = { "last", "final", "recent", "past", "latter", "hindmost" };
		String[] currentSynonyms = { "current", "this", "present", "ongoing", "actual" };
		String[] nextSynonyms = { "next", "following", "subsequent", "succeeding", "ensuing", "tailing" };

		TrieMap<Callable<DateTime>> keywordMapping = new LinkedListTrieMap<Callable<DateTime>>();
		keywordMapping.put("now", () -> {
			return new DateTime(LocalDateTime.now(), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
		});
		keywordMapping.put("today", () -> {
			return new DateTime(LocalDate.now(), "uuuu-MM-dd");
		});
		keywordMapping.put("yesterday", () -> {
			return new DateTime(LocalDate.now().minus(1, ChronoUnit.DAYS), "uuuu-MM-dd");
		});
		keywordMapping.put("tomorrow", () -> {
			return new DateTime(LocalDate.now().plus(1, ChronoUnit.DAYS), "uuuu-MM-dd");
		});
		keywordMapping.put("day before yesterday", () -> {
			return new DateTime(LocalDate.now().minus(2, ChronoUnit.DAYS), "uuuu-MM-dd");
		});
		keywordMapping.put("day after tomorrow", () -> {
			return new DateTime(LocalDate.now().plus(2, ChronoUnit.DAYS), "uuuu-MM-dd");
		});

		for (String lastSynonym : lastSynonyms) {
			keywordMapping.put(lastSynonym + " second", () -> {
				return new DateTime(LocalDateTime.now().minus(1, ChronoUnit.SECONDS), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
			});
			keywordMapping.put(lastSynonym + " minute", () -> {
				return new DateTime(LocalDateTime.now().minus(1, ChronoUnit.MINUTES), "uuuu-MM-dd'T'HH:mmZZZZZ");
			});
			keywordMapping.put(lastSynonym + " hour", () -> {
				return new DateTime(LocalDateTime.now().minus(1, ChronoUnit.HOURS), "uuuu-MM-dd'T'HHZZZZZ");
			});
			keywordMapping.put(lastSynonym + " day", () -> {
				return new DateTime(LocalDate.now().minusDays(1), "uuuu-MM-dd");
			});
			keywordMapping.put(lastSynonym + " week", () -> {
				return new DateTime(LocalDate.now().minusWeeks(1), "YYYY-'W'ww");
			});
			keywordMapping.put(lastSynonym + " month", () -> {
				return new DateTime(LocalDate.now().minusMonths(1), "uuuu-MM");
			});
			keywordMapping.put(lastSynonym + " year", () -> {
				return new DateTime(Year.now().minusYears(1), "uuuu");
			});
		}

		for (String currentSynonym : currentSynonyms) {
			keywordMapping.put(currentSynonym + " second", () -> {
				return new DateTime(LocalDateTime.now(), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
			});
			keywordMapping.put(currentSynonym + " minute", () -> {
				return new DateTime(LocalDateTime.now(), "uuuu-MM-dd'T'HH:mmZZZZZ");
			});
			keywordMapping.put(currentSynonym + " hour", () -> {
				return new DateTime(LocalDateTime.now(), "uuuu-MM-dd'T'HHZZZZZ");
			});
			keywordMapping.put(currentSynonym + " day", () -> {
				return new DateTime(LocalDate.now(), "uuuu-MM-dd");
			});
			keywordMapping.put(currentSynonym + " week", () -> {
				return new DateTime(LocalDate.now(), "YYYY-'W'ww");
			});
			keywordMapping.put(currentSynonym + " month", () -> {
				return new DateTime(LocalDate.now(), "uuuu-MM");
			});
			keywordMapping.put(currentSynonym + " year", () -> {
				return new DateTime(Year.now(), "uuuu");
			});
		}

		for (String nextSynonym : nextSynonyms) {
			keywordMapping.put(nextSynonym + " second", () -> {
				return new DateTime(LocalDateTime.now().plus(1, ChronoUnit.SECONDS), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
			});
			keywordMapping.put(nextSynonym + " minute", () -> {
				return new DateTime(LocalDateTime.now().plus(1, ChronoUnit.MINUTES), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
			});
			keywordMapping.put(nextSynonym + " hour", () -> {
				return new DateTime(LocalDateTime.now().plus(1, ChronoUnit.HOURS), "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
			});
			keywordMapping.put(nextSynonym + " day", () -> {
				return new DateTime(LocalDate.now().plusDays(1), "uuuu-MM-dd");
			});
			keywordMapping.put(nextSynonym + " week", () -> {
				return new DateTime(LocalDate.now().plusWeeks(1), "YYYY-'W'ww");
			});
			keywordMapping.put(nextSynonym + " month", () -> {
				return new DateTime(LocalDate.now().plusMonths(1), "uuuu-MM");
			});
			keywordMapping.put(nextSynonym + " year", () -> {
				return new DateTime(Year.now().plusYears(1), "uuuu");
			});
		}

		// TODO "last monday", "next december", ...

		// TODO three days ago, ... (one ... twelfth)

		KEYWORD_MATCHER = new TrieJaroWinklerSimilarityMatcher<Callable<DateTime>>(keywordMapping);
	}

	private double matchThreshold = 0.95;

	private final static String NAMESPACE = "datetime:dt";

	// TODO update scope IRI
	private final static IRI SCOPE = IRI.create("http://example.org/scope/temporal");

	private static final HashMap<String, Collection<String>> PATTERN_BROADERS = new HashMap<String, Collection<String>>();

	{
		PATTERN_BROADERS.put("'d'dd", Collections.emptySet());
		PATTERN_BROADERS.put("'d'ddZZZZZ", Lists.newArrayList("'d'dd"));
		PATTERN_BROADERS.put("'D'DDD", Collections.emptySet());
		PATTERN_BROADERS.put("'D'DDDZZZZZ", Lists.newArrayList("'D'DDD"));
		PATTERN_BROADERS.put("'e'e", Collections.emptySet());
		PATTERN_BROADERS.put("'e'eZZZZZ", Lists.newArrayList("'e'e"));
		PATTERN_BROADERS.put("'H'HH", Collections.emptySet());
		PATTERN_BROADERS.put("'H'HHZZZZZ", Lists.newArrayList("'H'HH"));
		PATTERN_BROADERS.put("'M'MM", Collections.emptySet());
		PATTERN_BROADERS.put("'M'MMZZZZZ", Lists.newArrayList("'M'MM"));
		PATTERN_BROADERS.put("'W'ww", Collections.emptySet());
		PATTERN_BROADERS.put("'W'wwZZZZZ", Lists.newArrayList("'W'ww"));
		PATTERN_BROADERS.put("'W'ww-e", Lists.newArrayList("'W'ww", "'e'e"));
		PATTERN_BROADERS.put("'W'ww-eZZZZZ", Lists.newArrayList("'W'wwZZZZZ", "'e'eZZZZZ", "'W'ww-e"));
		PATTERN_BROADERS.put("HH:mm", Lists.newArrayList("'H'HH"));
		PATTERN_BROADERS.put("HH:mmZZZZZ", Lists.newArrayList("'H'HHZZZZZ", "HH:mm"));
		PATTERN_BROADERS.put("HH:mm:ss", Lists.newArrayList("HH:mm"));
		PATTERN_BROADERS.put("HH:mm:ssZZZZZ", Lists.newArrayList("HH:mmZZZZZ", "HH:mm:ss"));
		PATTERN_BROADERS.put("MM-dd", Lists.newArrayList("'M'MM", "'d'dd"));
		PATTERN_BROADERS.put("MM-ddZZZZZ", Lists.newArrayList("'M'MMZZZZZ", "'d'ddZZZZZ", "MM-dd"));
		PATTERN_BROADERS.put("uuuu", Collections.emptySet());
		PATTERN_BROADERS.put("uuuuZZZZZ", Lists.newArrayList("uuuu"));
		PATTERN_BROADERS.put("YYYY-'W'ww", Lists.newArrayList("'W'ww"));
		PATTERN_BROADERS.put("YYYY-'W'wwZZZZZ", Lists.newArrayList("'W'wwZZZZZ", "YYYY-'W'ww"));
		PATTERN_BROADERS.put("uuuu-MM", Lists.newArrayList("uuuu", "'M'MM"));
		PATTERN_BROADERS.put("uuuu-MMZZZZZ", Lists.newArrayList("uuuuZZZZZ", "'M'MMZZZZZ", "uuuu-MM"));
		PATTERN_BROADERS.put("uuuu-MM-dd", Lists.newArrayList("uuuu-MM", "MM-dd", "YYYY-'W'ww", "'D'DDD", "'W'ww-e"));
		PATTERN_BROADERS.put("uuuu-MM-ddZZZZZ", Lists.newArrayList("uuuu-MMZZZZZ", "MM-ddZZZZZ", "uuuu-MM-dd",
				"YYYY-'W'wwZZZZZ", "'D'DDDZZZZZ", "'W'ww-eZZZZZ"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HH", Lists.newArrayList("uuuu-MM-dd", "'H'HH"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HHZZZZZ",
				Lists.newArrayList("uuuu-MM-ddZZZZZ", "'H'HHZZZZZ", "uuuu-MM-dd'T'HH"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HH:mm", Lists.newArrayList("uuuu-MM-dd'T'HH", "HH:mm"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HH:mmZZZZZ",
				Lists.newArrayList("uuuu-MM-dd'T'HHZZZZZ", "uuuu-MM-dd'T'HH:mm", "HH:mmZZZZZ"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HH:mm:ss", Lists.newArrayList("uuuu-MM-dd'T'HH:mm", "HH:mm:ss"));
		PATTERN_BROADERS.put("uuuu-MM-dd'T'HH:mm:ssZZZZZ",
				Lists.newArrayList("uuuu-MM-dd'T'HH:mmZZZZZ", "uuuu-MM-dd'T'HH:mm:ss", "HH:mm:ssZZZZZ"));
	}

	private static final HashMap<String, Collection<String>> PATTERN_ALL_BROADERS = new HashMap<String, Collection<String>>();

	{
		for (String basePattern : PATTERN_BROADERS.keySet()) {
			Collection<String> allBroaderPatterns = new HashSet<String>();
			List<String> unprocessedBroaderPatterns = new LinkedList<String>(PATTERN_BROADERS.get(basePattern));
			while (!unprocessedBroaderPatterns.isEmpty()) {
				String broaderPattern = unprocessedBroaderPatterns.remove(0);
				if (!allBroaderPatterns.contains(broaderPattern)) {
					allBroaderPatterns.add(broaderPattern);
					unprocessedBroaderPatterns.addAll(PATTERN_BROADERS.get(broaderPattern));
				}
			}
			PATTERN_ALL_BROADERS.put(basePattern, allBroaderPatterns);
		}
	}
	private static final HashMap<String, String> PATTERN_MATCHER = new HashMap<String, String>();

	{
		PATTERN_MATCHER.put("uuuu-DDD'T'HH:mm:ssZZZZZ", "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HH:mm:ssZZZZZ", "uuuu-MM-dd'T'HH:mm:ssZZZZZ");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HH:mmZZZZZ", "uuuu-MM-dd'T'HH:mmZZZZZ");
		PATTERN_MATCHER.put("uuuu-DDD'T'HH:mmZZZZZ", "uuuu-MM-dd'T'HH:mmZZZZZ");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HHZZZZZ", "uuuu-MM-dd'T'HHZZZZZ");
		PATTERN_MATCHER.put("uuuu-DDD'T'HHZZZZZ", "uuuu-MM-dd'T'HHZZZZZ");
		PATTERN_MATCHER.put("YYYY-'W'ww-eZZZZZ", "uuuu-MM-ddZZZZZ");
		PATTERN_MATCHER.put("uuuu-DDDZZZZZ", "uuuu-MM-ddZZZZZ");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HH:mm:ss", "uuuu-MM-dd'T'HH:mm:ss");
		PATTERN_MATCHER.put("uuuu-DDD'T'HH:mm:ss", "uuuu-MM-dd'T'HH:mm:ss");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HH:mm", "uuuu-MM-dd'T'HH:mm");
		PATTERN_MATCHER.put("uuuu-DDD'T'HH:mm", "uuuu-MM-dd'T'HH:mm");
		PATTERN_MATCHER.put("YYYY-'W'ww-e'T'HH", "uuuu-MM-dd'T'HH");
		PATTERN_MATCHER.put("uuuu-DDD'T'HH", "uuuu-MM-dd'T'HH");
		PATTERN_MATCHER.put("YYYY-'W'ww-e", "uuuu-MM-dd");
		PATTERN_MATCHER.put("uuuu-DDD", "uuuu-MM-dd");
	}

	private static IRI getDateTimeIRI(DateTime dateTime) throws SemanticDataSourceException {
		return getDateTimeIRI(dateTime, dateTime.pattern);
	}

	private static IRI getDateTimeIRI(DateTime dateTime, String pattern) throws SemanticDataSourceException {
		return IRI.create(NAMESPACE + getTimestamp(dateTime, pattern));
	}

	private static DateTimeFormatter getFormatter(String pattern) {
		// TODO adjust locale
		return DateTimeFormatter.ofPattern(pattern, new Locale("en"));
	}

	private static String getTimestamp(DateTime dateTime) throws SemanticDataSourceException {
		return getTimestamp(dateTime, dateTime.pattern);
	}

	private static String getTimestamp(DateTime dateTime, String pattern) throws SemanticDataSourceException {
		return getFormatter(pattern).format(dateTime.temporal);
	}

	@Override
	public Collection<IRI> getAllBroaders(IRI iri) throws SemanticDataSourceException {
		Optional<DateTime> optDateTime = getDateTime(iri);

		if (optDateTime.isPresent()) {
			DateTime dateTime = optDateTime.get();
			Collection<IRI> broaders = new HashSet<IRI>();
			for (String broaderPattern : PATTERN_ALL_BROADERS.get(dateTime.pattern)) {
				broaders.add(getDateTimeIRI(dateTime, broaderPattern));
			}
			return broaders;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Collection<IRI> getBroaders(IRI iri) throws SemanticDataSourceException {
		Optional<DateTime> optDateTime = getDateTime(iri);

		if (optDateTime.isPresent()) {
			DateTime dateTime = optDateTime.get();
			Collection<IRI> broaders = new HashSet<IRI>();
			for (String broaderPattern : PATTERN_BROADERS.get(dateTime.pattern)) {
				broaders.add(getDateTimeIRI(dateTime, broaderPattern));
			}
			return broaders;
		} else {
			return Collections.emptySet();
		}
	}

	private Optional<DateTime> getDateTime(IRI iri) {
		if (!iri.getIRIString().startsWith(NAMESPACE)) {
			// unexpected namespace
			return Optional.empty();
		} else {
			String timestamp = iri.getIRIString().substring(NAMESPACE.length());
			for (String pattern : PATTERN_BROADERS.keySet()) {
				Optional<DateTime> dateTime = parseTimestamp(timestamp, pattern);
				if (dateTime.isPresent()) {
					return dateTime;
				}
			}
			return Optional.empty();
		}
	}

	private Optional<DateTime> getDateTime(String term) {

		for (String pattern : PATTERN_BROADERS.keySet()) {
			Optional<DateTime> dateTime = parseTimestamp(term, pattern);
			if (dateTime.isPresent()) {
				return dateTime;
			}
		}

		for (String pattern : PATTERN_MATCHER.keySet()) {
			Optional<DateTime> dateTime = parseTimestamp(term, pattern, PATTERN_MATCHER.get(pattern));
			if (dateTime.isPresent()) {
				return dateTime;
			}
		}

		return Optional.empty();
	}

	@Override
	public Collection<String> getLabels(IRI iri) throws SemanticDataSourceException {
		Optional<DateTime> dateTime = getDateTime(iri);
		if (dateTime.isPresent()) {
			return Collections.singleton(getTimestamp(dateTime.get()));
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Map<IRI, Double> getMatches(String term) throws SemanticDataSourceException {
		Optional<DateTime> dateTime = getDateTime(term);
		if (dateTime.isPresent()) {
			return Collections.singletonMap(getDateTimeIRI(dateTime.get()), 1.0);
		} else {
			Map<Callable<DateTime>, Double> matches = KEYWORD_MATCHER.match(matchThreshold, term);
			if (!matches.isEmpty()) {
				Map<IRI, Double> result = new HashMap<IRI, Double>();
				for (Map.Entry<Callable<DateTime>, Double> match : matches.entrySet()) {
					try {
						result.put(getDateTimeIRI(match.getKey().call()), match.getValue());
					} catch (Exception e) {
						throw new SemanticDataSourceException(
								"Failed to calculate temporal value for \"" + term + "\".", e);
					}
				}
				return result;
			} else {
				return Collections.emptyMap();
			}
		}
	}

	@Override
	public Collection<String> getNamespaces() throws SemanticDataSourceException {
		return Collections.singleton(NAMESPACE);
	}

	@Override
	public Collection<IRI> getScopes() throws SemanticDataSourceException {
		return Collections.singleton(SCOPE);
	}

	@Override
	public boolean isPresent(IRI iri) throws SemanticDataSourceException {
		return getDateTime(iri).isPresent();
	}

	private Optional<DateTime> parseTimestamp(String timestamp, String pattern) {
		return parseTimestamp(timestamp, pattern, pattern);
	}

	private Optional<DateTime> parseTimestamp(String timestamp, String usedPattern, String storedPattern) {
		try {
			return Optional.of(new DateTime(getFormatter(usedPattern).parse(timestamp), storedPattern));
		} catch (DateTimeParseException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean providingAllBroaders() {
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
	public void setMatchThreshold(double threshold) {
		this.matchThreshold = threshold;
	}
}
