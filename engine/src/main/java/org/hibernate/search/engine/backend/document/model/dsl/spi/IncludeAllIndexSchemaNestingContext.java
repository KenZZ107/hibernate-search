/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.backend.document.model.dsl.spi;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * @author Yoann Rodiere
 */
class IncludeAllIndexSchemaNestingContext implements IndexSchemaNestingContext {

	static final IncludeAllIndexSchemaNestingContext INSTANCE = new IncludeAllIndexSchemaNestingContext();

	private IncludeAllIndexSchemaNestingContext() {
	}

	@Override
	public <T> T nest(String relativeName, Function<String, T> nestedElementFactoryIfIncluded,
			Function<String, T> nestedElementFactoryIfExcluded) {
		return nestedElementFactoryIfIncluded.apply( relativeName );
	}

	@Override
	public <T> T nest(String relativeName,
			BiFunction<String, IndexSchemaNestingContext, T> nestedElementFactoryIfIncluded,
			BiFunction<String, IndexSchemaNestingContext, T> nestedElementFactoryIfExcluded) {
		return nestedElementFactoryIfIncluded.apply( relativeName, INSTANCE );
	}

}