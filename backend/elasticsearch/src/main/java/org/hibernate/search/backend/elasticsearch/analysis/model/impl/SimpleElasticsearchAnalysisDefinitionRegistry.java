/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.analysis.model.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.AnalyzerDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.CharFilterDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.NormalizerDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.TokenFilterDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.TokenizerDefinition;
import org.hibernate.search.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.util.impl.common.LoggerFactory;

/**
 * A simple implementation of {@link ElasticsearchAnalysisDefinitionRegistry}.
 * <p>
 * This class also provides access to the full mapping from names to definitions
 * (see {@link #getAnalyzerDefinitions} for instance)
 *
 * @author Yoann Rodiere
 */
public class SimpleElasticsearchAnalysisDefinitionRegistry implements ElasticsearchAnalysisDefinitionRegistry {

	private static final Log LOG = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final Map<String, AnalyzerDefinition> analyzerDefinitions = new TreeMap<>();
	private final Map<String, NormalizerDefinition> normalizerDefinitions = new TreeMap<>();
	private final Map<String, TokenizerDefinition> tokenizerDefinitions = new TreeMap<>();
	private final Map<String, TokenFilterDefinition> tokenFilterDefinitions = new TreeMap<>();
	private final Map<String, CharFilterDefinition> charFilterDefinitions = new TreeMap<>();

	private SimpleElasticsearchAnalysisDefinitionRegistry(ElasticsearchAnalysisDefinitionContributor contributor) {
		contributor.contribute( new ElasticsearchAnalysisDefinitionCollector() {
			@Override
			public void collect(String name, AnalyzerDefinition definition) {
				AnalyzerDefinition previous = analyzerDefinitions.putIfAbsent( name, definition );
				if ( previous != null && previous != definition ) {
					throw LOG.analyzerNamingConflict( name );
				}
			}

			@Override
			public void collect(String name, NormalizerDefinition definition) {
				NormalizerDefinition previous = normalizerDefinitions.putIfAbsent( name, definition );
				if ( previous != null && previous != definition ) {
					throw LOG.normalizerNamingConflict( name );
				}
			}

			@Override
			public void collect(String name, TokenizerDefinition definition) {
				TokenizerDefinition previous = tokenizerDefinitions.putIfAbsent( name, definition );
				if ( previous != null && previous != definition ) {
					throw LOG.tokenizerNamingConflict( name );
				}
			}

			@Override
			public void collect(String name, TokenFilterDefinition definition) {
				TokenFilterDefinition previous = tokenFilterDefinitions.putIfAbsent( name, definition );
				if ( previous != null && previous != definition ) {
					throw LOG.tokenFilterNamingConflict( name );
				}
			}

			@Override
			public void collect(String name, CharFilterDefinition definition) {
				CharFilterDefinition previous = charFilterDefinitions.putIfAbsent( name, definition );
				if ( previous != null && previous != definition ) {
					throw LOG.charFilterNamingConflict( name );
				}
			}
		} );
	}

	@Override
	public AnalyzerDefinition getAnalyzerDefinition(String name) {
		return analyzerDefinitions.get( name );
	}

	@Override
	public NormalizerDefinition getNormalizerDefinition(String name) {
		return normalizerDefinitions.get( name );
	}

	@Override
	public TokenizerDefinition getTokenizerDefinition(String name) {
		return tokenizerDefinitions.get( name );
	}

	@Override
	public TokenFilterDefinition getTokenFilterDefinition(String name) {
		return tokenFilterDefinitions.get( name );
	}

	@Override
	public CharFilterDefinition getCharFilterDefinition(String name) {
		return charFilterDefinitions.get( name );
	}

	public Map<String, AnalyzerDefinition> getAnalyzerDefinitions() {
		return Collections.unmodifiableMap( analyzerDefinitions );
	}

	public Map<String, NormalizerDefinition> getNormalizerDefinitions() {
		return Collections.unmodifiableMap( normalizerDefinitions );
	}

	public Map<String, TokenizerDefinition> getTokenizerDefinitions() {
		return Collections.unmodifiableMap( tokenizerDefinitions );
	}

	public Map<String, TokenFilterDefinition> getTokenFilterDefinitions() {
		return Collections.unmodifiableMap( tokenFilterDefinitions );
	}

	public Map<String, CharFilterDefinition> getCharFilterDefinitions() {
		return Collections.unmodifiableMap( charFilterDefinitions );
	}

}