/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.predicate.spi;

import org.hibernate.search.engine.search.SearchPredicate;
import org.hibernate.search.util.SearchException;

/**
 * A factory for search predicates.
 * <p>
 * This is the main entry point for the engine
 * to ask the backend to build search predicates.
 *
 * @param <C> The type of query element collector
 * @param <B> The implementation type of builders
 * This type is backend-specific. See {@link SearchPredicateBuilder#toImplementation()}
 */
public interface SearchPredicateFactory<C, B> {

	/**
	 * Convert a predicate builder to a reusable {@link SearchPredicate} object.
	 * <p>
	 * Implementations may decide to just wrap the builder if it is reusable,
	 * or to convert it to another representation if it is not reusable.
	 *
	 * @param builder The predicate builder implementation.
	 * @return The corresponding reusable {@link SearchPredicate} object.
	 */
	SearchPredicate toSearchPredicate(B builder);

	/**
	 * Convert a {@link SearchPredicate} object back to a predicate builder.
	 * <p>
	 * May be called multiple times for a given {@link SearchPredicate} object.
	 *
	 * @param predicate The {@link SearchPredicate} object to convert.
	 * @return The corresponding predicate builder implementation.
	 * @throws SearchException If the {@link SearchPredicate} object was created
	 * by a different, incompatible factory.
	 */
	B toImplementation(SearchPredicate predicate);

	/**
	 * Contribute a predicate builder to a collector.
	 * <p>
	 * Will only ever be called once per collector.
	 *
	 * @param collector The query element collector.
	 * @param builder The predicate builder implementation.
	 */
	void contribute(C collector, B builder);

	MatchAllPredicateBuilder<B> matchAll();

	BooleanJunctionPredicateBuilder<B> bool();

	MatchPredicateBuilder<B> match(String absoluteFieldPath);

	RangePredicateBuilder<B> range(String absoluteFieldPath);

	NestedPredicateBuilder<B> nested(String absoluteFieldPath);

	SpatialWithinCirclePredicateBuilder<B> spatialWithinCircle(String absoluteFieldPath);

	SpatialWithinPolygonPredicateBuilder<B> spatialWithinPolygon(String absoluteFieldPath);

	SpatialWithinBoundingBoxPredicateBuilder<B> spatialWithinBoundingBox(String absoluteFieldPath);

}
