/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.projection.impl;

import org.hibernate.search.backend.elasticsearch.search.extraction.impl.DocumentReferenceExtractorHelper;
import org.hibernate.search.engine.search.DocumentReference;
import org.hibernate.search.engine.search.query.spi.ProjectionHitCollector;

import com.google.gson.JsonObject;

class DocumentReferenceSearchProjectionImpl implements ElasticsearchSearchProjection<DocumentReference> {

	private final DocumentReferenceExtractorHelper helper;

	DocumentReferenceSearchProjectionImpl(DocumentReferenceExtractorHelper helper) {
		this.helper = helper;
	}

	@Override
	public void contributeRequest(JsonObject requestBody, SearchProjectionExecutionContext searchProjectionExecutionContext) {
		helper.contributeRequest( requestBody );
	}

	@Override
	public void extract(ProjectionHitCollector collector, JsonObject responseBody, JsonObject hit, SearchProjectionExecutionContext searchProjectionExecutionContext) {
		collector.collectProjection( helper.extractDocumentReference( hit ) );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
