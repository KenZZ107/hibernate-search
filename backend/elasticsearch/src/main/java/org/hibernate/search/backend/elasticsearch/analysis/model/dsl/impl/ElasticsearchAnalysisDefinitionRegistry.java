/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.analysis.model.dsl.impl;

import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.AnalyzerDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.CharFilterDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.NormalizerDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.TokenFilterDefinition;
import org.hibernate.search.backend.elasticsearch.analysis.model.impl.esnative.TokenizerDefinition;
import org.hibernate.search.util.SearchException;

/**
 * A registry of analysis-related definitions for Elasticsearch.
 *
 * @author Yoann Rodiere
 */
public interface ElasticsearchAnalysisDefinitionRegistry {

	/**
	 * Register an analyzer definition.
	 * @param name The name of the definition to be registered.
	 * @param definition The definition to be registered.
	 * @throws SearchException if the name is already associated with a different definition.
	 */
	void register(String name, AnalyzerDefinition definition);

	/**
	 * Register a normalizer definition.
	 * @param name The name of the definition to be registered.
	 * @param definition The definition to be registered.
	 * @throws SearchException if the name is already associated with a different definition.
	 */
	void register(String name, NormalizerDefinition definition);

	/**
	 * Register a tokenizer definition.
	 * @param name The name of the definition to be registered.
	 * @param definition The definition to be registered.
	 * @throws SearchException if the name is already associated with a different definition.
	 */
	void register(String name, TokenizerDefinition definition);

	/**
	 * Register a token filter definition.
	 * @param name The name of the definition to be registered.
	 * @param definition The definition to be registered.
	 * @throws SearchException if the name is already associated with a different definition.
	 */
	void register(String name, TokenFilterDefinition definition);

	/**
	 * Register a char filter definition.
	 * @param name The name of the definition to be registered.
	 * @param definition The definition to be registered.
	 * @throws SearchException if the name is already associated with a different definition.
	 */
	void register(String name, CharFilterDefinition definition);

	/**
	 * @param name An analyzer name
	 * @return The analyzer definition associated with the given name,
	 * or {@code null} if there isn't any.
	 */
	AnalyzerDefinition getAnalyzerDefinition(String name);

	/**
	 * @param name A normalizer name
	 * @return The normalizer definition associated with the given name,
	 * or {@code null} if there isn't any.
	 */
	NormalizerDefinition getNormalizerDefinition(String name);

	/**
	 * @param name A tokenizer name
	 * @return The tokenizer definition associated with the given name,
	 * or {@code null} if there isn't any.
	 */
	TokenizerDefinition getTokenizerDefinition(String name);

	/**
	 * @param name A token filter name
	 * @return The token filter definition associated with the given name,
	 * or {@code null} if there isn't any.
	 */
	TokenFilterDefinition getTokenFilterDefinition(String name);

	/**
	 * @param name A char filter name
	 * @return The char filter definition associated with the given name,
	 * or {@code null} if there isn't any.
	 */
	CharFilterDefinition getCharFilterDefinition(String name);

}