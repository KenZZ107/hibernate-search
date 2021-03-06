/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.projection.impl;

import org.hibernate.search.backend.elasticsearch.search.extraction.impl.DocumentReferenceExtractorHelper;
import org.hibernate.search.engine.search.DocumentReference;
import org.hibernate.search.engine.search.SearchProjection;
import org.hibernate.search.engine.search.projection.spi.DocumentReferenceSearchProjectionBuilder;


class DocumentReferenceSearchProjectionBuilderImpl implements DocumentReferenceSearchProjectionBuilder {

	private final DocumentReferenceSearchProjectionImpl projection;

	DocumentReferenceSearchProjectionBuilderImpl(DocumentReferenceExtractorHelper helper) {
		this.projection = new DocumentReferenceSearchProjectionImpl( helper );
	}

	@Override
	public SearchProjection<DocumentReference> build() {
		return projection;
	}
}
