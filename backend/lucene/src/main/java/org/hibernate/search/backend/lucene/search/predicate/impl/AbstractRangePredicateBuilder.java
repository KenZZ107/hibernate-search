/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.predicate.impl;

import org.hibernate.search.backend.lucene.types.formatter.impl.LuceneFieldFormatter;
import org.hibernate.search.engine.search.predicate.spi.RangePredicateBuilder;


public abstract class AbstractRangePredicateBuilder<T> extends AbstractSearchPredicateBuilder implements RangePredicateBuilder<LuceneSearchPredicateCollector> {

	protected final String absoluteFieldPath;

	protected final LuceneFieldFormatter<T> formatter;

	protected T lowerLimit;

	protected boolean excludeLowerLimit = false;

	protected T upperLimit;

	protected boolean excludeUpperLimit = false;

	protected AbstractRangePredicateBuilder(String absoluteFieldPath, LuceneFieldFormatter<T> formatter) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.formatter = formatter;
	}

	@Override
	public void lowerLimit(Object value) {
		lowerLimit = formatter.format( value );
	}

	@Override
	public void excludeLowerLimit() {
		excludeLowerLimit = true;
	}

	@Override
	public void upperLimit(Object value) {
		upperLimit = formatter.format( value );
	}

	@Override
	public void excludeUpperLimit() {
		excludeUpperLimit = true;
	}
}