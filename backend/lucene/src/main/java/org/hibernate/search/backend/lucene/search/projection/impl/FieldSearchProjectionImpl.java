/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.projection.impl;

import java.util.Set;

import org.hibernate.search.backend.lucene.search.extraction.impl.LuceneCollectorsBuilder;
import org.hibernate.search.backend.lucene.search.extraction.impl.LuceneResult;
import org.hibernate.search.backend.lucene.types.codec.impl.LuceneFieldCodec;
import org.hibernate.search.backend.lucene.types.converter.impl.LuceneFieldConverter;
import org.hibernate.search.engine.search.query.spi.ProjectionHitCollector;

class FieldSearchProjectionImpl<T> implements LuceneSearchProjection<T> {

	private final String absoluteFieldPath;

	private final LuceneFieldCodec<T> codec;

	private final LuceneFieldConverter<T, ?> converter;

	FieldSearchProjectionImpl(String absoluteFieldPath, LuceneFieldCodec<T> codec,
			LuceneFieldConverter<T, ?> converter) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.codec = codec;
		this.converter = converter;
	}

	@Override
	public void contributeCollectors(LuceneCollectorsBuilder luceneCollectorBuilder) {
		luceneCollectorBuilder.requireTopDocsCollector();
	}

	@Override
	public void contributeFields(Set<String> absoluteFieldPaths) {
		if ( codec.getOverriddenStoredFields().isEmpty() ) {
			absoluteFieldPaths.add( absoluteFieldPath );
		}
		else {
			absoluteFieldPaths.addAll( codec.getOverriddenStoredFields() );
		}
	}

	@Override
	public void extract(ProjectionHitCollector collector, LuceneResult documentResult) {
		T rawValue = codec.decode( documentResult.getDocument(), absoluteFieldPath );
		collector.collectProjection( converter.convertFromProjection( rawValue ) );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "absoluteFieldPath=" ).append( absoluteFieldPath )
				.append( "]" );
		return sb.toString();
	}
}
