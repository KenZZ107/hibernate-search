/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.backend.tck.search;

import static org.hibernate.search.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert.assertThat;
import static org.hibernate.search.util.impl.integrationtest.common.stub.mapper.StubMapperUtils.referenceProvider;

import java.util.List;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldAccessor;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.dsl.Sortable;
import org.hibernate.search.engine.backend.document.model.dsl.Store;
import org.hibernate.search.engine.backend.index.spi.IndexWorkPlan;
import org.hibernate.search.engine.mapper.mapping.spi.MappedIndexManager;
import org.hibernate.search.engine.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.engine.backend.index.spi.IndexSearchTargetBuilder;
import org.hibernate.search.engine.common.spi.SessionContext;
import org.hibernate.search.integrationtest.backend.tck.util.rule.SearchSetupHelper;
import org.hibernate.search.engine.logging.spi.EventContexts;
import org.hibernate.search.engine.search.DocumentReference;
import org.hibernate.search.engine.search.SearchQuery;
import org.hibernate.search.util.SearchException;
import org.hibernate.search.util.impl.integrationtest.common.FailureReportUtils;
import org.hibernate.search.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert;
import org.hibernate.search.util.impl.integrationtest.common.assertion.ProjectionsSearchResultAssert;
import org.hibernate.search.util.impl.integrationtest.common.stub.StubSessionContext;
import org.hibernate.search.util.impl.test.SubTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SearchMultiIndexIT {

	private static final String BACKEND_1 = "backend_1";
	private static final String BACKEND_2 = "backend_2";

	private static final String STRING_1 = "string_1";
	private static final String STRING_2 = "string_2";

	// Backend 1 / Index 1

	private static final String INDEX_NAME_1_1 = "IndexName_1_1";

	private static final String DOCUMENT_1_1_1 = "1_1_1";
	private static final String ADDITIONAL_FIELD_1_1_1 = "additional_field_1_1_1";
	private static final String SORT_FIELD_1_1_1 = "1_1_1";
	private static final String DIFFERENT_TYPES_FIELD_1_1_1 = "different_types_field_1_1_1";

	private static final String DOCUMENT_1_1_2 = "1_1_2";
	private static final String ADDITIONAL_FIELD_1_1_2 = "additional_field_1_1_2";
	private static final String SORT_FIELD_1_1_2 = "1_1_2";
	private static final String DIFFERENT_TYPES_FIELD_1_1_2 = "different_types_field_1_1_2";

	// Backend 1 / Index 2

	private static final String INDEX_NAME_1_2 = "IndexName_1_2";

	private static final String DOCUMENT_1_2_1 = "1_2_1";
	private static final String SORT_FIELD_1_2_1 = "1_2_1";
	private static final Integer DIFFERENT_TYPES_FIELD_1_2_1 = 37;

	// Backend 2 / Index 1

	private static final String INDEX_NAME_2_1 = "IndexName_2_1";

	private static final String DOCUMENT_2_1_1 = "2_1_1";

	private static final String DOCUMENT_2_1_2 = "2_1_2";

	@Rule
	public SearchSetupHelper setupHelper = new SearchSetupHelper();

	// Backend 1 / Index 1

	private IndexAccessors_1_1 indexAccessors_1_1;
	private MappedIndexManager<?> indexManager_1_1;

	// Backend 1 / Index 2

	private IndexAccessors_1_2 indexAccessors_1_2;
	private MappedIndexManager<?> indexManager_1_2;

	// Backend 2 / Index 1

	private IndexAccessors_2_1 indexAccessors_2_1;
	private MappedIndexManager<?> indexManager_2_1;

	private SessionContext sessionContext = new StubSessionContext();

	@Before
	public void setup() {
		setupHelper.withDefaultConfiguration( BACKEND_1 )
				.withIndex(
						"MappedType_1_1", INDEX_NAME_1_1,
						ctx -> this.indexAccessors_1_1 = new IndexAccessors_1_1( ctx.getSchemaElement() ),
						indexManager -> this.indexManager_1_1 = indexManager
				)
				.withIndex(
						"MappedType_1_2", INDEX_NAME_1_2,
						ctx -> this.indexAccessors_1_2 = new IndexAccessors_1_2( ctx.getSchemaElement() ),
						indexManager -> this.indexManager_1_2 = indexManager
				)
				.setup();

		setupHelper.withDefaultConfiguration( BACKEND_2 )
				.withIndex(
						"MappedType_2_1", INDEX_NAME_2_1,
						ctx -> this.indexAccessors_2_1 = new IndexAccessors_2_1( ctx.getSchemaElement() ),
						indexManager -> this.indexManager_2_1 = indexManager
				)
				.setup();

		initData();
	}

	@Test
	public void search_across_multiple_indexes() {
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "string" ).matching( STRING_1 ).end()
				.build();

		DocumentReferencesSearchResultAssert.assertThat( query ).hasReferencesHitsAnyOrder( c -> {
			c.doc( INDEX_NAME_1_1, DOCUMENT_1_1_1 );
			c.doc( INDEX_NAME_1_2, DOCUMENT_1_2_1 );
		} );
	}

	@Test
	public void sort_across_multiple_indexes() {
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.sort().byField( "sortField" ).asc().end()
				.build();

		DocumentReferencesSearchResultAssert.assertThat( query ).hasReferencesHitsExactOrder( c -> {
			c.doc( INDEX_NAME_1_1, DOCUMENT_1_1_1 );
			c.doc( INDEX_NAME_1_1, DOCUMENT_1_1_2 );
			c.doc( INDEX_NAME_1_2, DOCUMENT_1_2_1 );
		} );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.sort().byField( "sortField" ).desc().end()
				.build();

		DocumentReferencesSearchResultAssert.assertThat( query ).hasReferencesHitsExactOrder( c -> {
			c.doc( INDEX_NAME_1_2, DOCUMENT_1_2_1 );
			c.doc( INDEX_NAME_1_1, DOCUMENT_1_1_2 );
			c.doc( INDEX_NAME_1_1, DOCUMENT_1_1_1 );
		} );
	}

	@Test
	public void projection_across_multiple_indexes() {
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SearchQuery<List<?>> query = searchTarget.query( sessionContext )
				.asProjections( searchTarget.projection().field( "sortField", String.class ).toProjection() )
				.predicate().matchAll().end()
				.build();

		ProjectionsSearchResultAssert.assertThat( query ).hasProjectionsHitsAnyOrder( c -> {
			c.projection( SORT_FIELD_1_1_1 );
			c.projection( SORT_FIELD_1_1_2 );
			c.projection( SORT_FIELD_1_2_1 );
		} );
	}

	@Test
	public void field_in_one_index_only_is_supported() {
		// Predicate
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "additionalField" ).matching( ADDITIONAL_FIELD_1_1_1 ).end()
				.build();

		DocumentReferencesSearchResultAssert.assertThat( query ).hasReferencesHitsAnyOrder( INDEX_NAME_1_1, DOCUMENT_1_1_1 );

		// Sort

		// It doesn't work with Elasticsearch as Elasticsearch is supposed to throw an error if there is no mapping information.
		// In our case, it does not throw an error but simply ignores the results from the second index.
		// See the additional test in the Lucene backend.

		// Projection

		SearchQuery<List<?>> projectionQuery = searchTarget.query( sessionContext )
				.asProjections( searchTarget.projection().field( "additionalField", String.class ).toProjection() )
				.predicate().matchAll().end()
				.build();

		ProjectionsSearchResultAssert.assertThat( projectionQuery ).hasProjectionsHitsAnyOrder( c -> {
			c.projection( ADDITIONAL_FIELD_1_1_1 );
			c.projection( ADDITIONAL_FIELD_1_1_2 );
			c.projection( (Object) null );
		} );
	}

	@Test
	public void unknown_field_throws_exception() {
		// Predicate
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SubTest.expectException(
				"predicate on unknown field with multiple targeted indexes",
				() -> searchTarget.predicate().match().onField( "unknownField" )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Unknown field 'unknownField'" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexNames(
								INDEX_NAME_1_1,
								INDEX_NAME_1_2
						)
				) );

		// Sort

		SubTest.expectException(
				"sort on unknown field with multiple targeted indexes",
				() -> searchTarget.sort().byField( "unknownField" )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Unknown field 'unknownField'" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexNames(
								INDEX_NAME_1_1,
								INDEX_NAME_1_2
						)
				) );

		// Projection

		SubTest.expectException(
				"projection on unknown field with multiple targeted indexes",
				() -> searchTarget.query( sessionContext ).asProjections(
						searchTarget.projection().field( "unknownField", Object.class ).toProjection()
				)
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Unknown field" )
				.hasMessageContaining( "unknownField" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexNames(
								INDEX_NAME_1_1,
								INDEX_NAME_1_2
						)
				) );
	}

	@Test
	public void search_with_incompatible_types_throws_exception() {
		IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
		indexManager_1_2.addToSearchTarget( searchTargetBuilder );
		IndexSearchTarget searchTarget = searchTargetBuilder.build();

		SubTest.expectException(
				"predicate on field with different type among the targeted indexes",
				() -> searchTarget.predicate().match().onField( "differentTypesField" )
						.matching( DIFFERENT_TYPES_FIELD_1_1_1 )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Multiple conflicting types for field 'differentTypesField'" );
	}

	@Test
	public void search_across_backends_throws_exception() {
		SubTest.expectException(
				"search across multiple backends",
				() -> {
					IndexSearchTargetBuilder searchTargetBuilder = indexManager_1_1.createSearchTarget();
					indexManager_2_1.addToSearchTarget( searchTargetBuilder );
					searchTargetBuilder.build();
				}
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "A search query cannot target multiple" )
				.hasMessageContaining( "backends" );
	}

	private void initData() {
		// Backend 1 / Index 1

		IndexWorkPlan<? extends DocumentElement> workPlan = indexManager_1_1.createWorkPlan( sessionContext );

		workPlan.add( referenceProvider( DOCUMENT_1_1_1 ), document -> {
			indexAccessors_1_1.string.write( document, STRING_1 );
			indexAccessors_1_1.additionalField.write( document, ADDITIONAL_FIELD_1_1_1 );
			indexAccessors_1_1.differentTypesField.write( document, DIFFERENT_TYPES_FIELD_1_1_1 );
			indexAccessors_1_1.sortField.write( document, SORT_FIELD_1_1_1 );
		} );
		workPlan.add( referenceProvider( DOCUMENT_1_1_2 ), document -> {
			indexAccessors_1_1.string.write( document, STRING_2 );
			indexAccessors_1_1.additionalField.write( document, ADDITIONAL_FIELD_1_1_2 );
			indexAccessors_1_1.differentTypesField.write( document, DIFFERENT_TYPES_FIELD_1_1_2 );
			indexAccessors_1_1.sortField.write( document, SORT_FIELD_1_1_2 );
		} );

		workPlan.execute().join();

		IndexSearchTarget searchTarget = indexManager_1_1.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.build();
		assertThat( query ).hasReferencesHitsAnyOrder( INDEX_NAME_1_1, DOCUMENT_1_1_1, DOCUMENT_1_1_2 );

		// Backend 1 / Index 2

		workPlan = indexManager_1_2.createWorkPlan( sessionContext );

		workPlan.add( referenceProvider( DOCUMENT_1_2_1 ), document -> {
			indexAccessors_1_2.string.write( document, STRING_1 );
			indexAccessors_1_2.differentTypesField.write( document, DIFFERENT_TYPES_FIELD_1_2_1 );
			indexAccessors_1_2.sortField.write( document, SORT_FIELD_1_2_1 );
		} );

		workPlan.execute().join();

		searchTarget = indexManager_1_2.createSearchTarget().build();
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.build();
		assertThat( query ).hasReferencesHitsAnyOrder( INDEX_NAME_1_2, DOCUMENT_1_2_1 );

		// Backend 2 / Index 1

		workPlan = indexManager_2_1.createWorkPlan( sessionContext );

		workPlan.add( referenceProvider( DOCUMENT_2_1_1 ), document -> {
			indexAccessors_2_1.string.write( document, STRING_1 );
		} );
		workPlan.add( referenceProvider( DOCUMENT_2_1_2 ), document -> {
			indexAccessors_2_1.string.write( document, STRING_2 );
		} );

		workPlan.execute().join();

		searchTarget = indexManager_2_1.createSearchTarget().build();
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.build();
		assertThat( query ).hasReferencesHitsAnyOrder( INDEX_NAME_2_1, DOCUMENT_2_1_1, DOCUMENT_2_1_2 );
	}

	private static class IndexAccessors_1_1 {
		final IndexFieldAccessor<String> string;
		final IndexFieldAccessor<String> additionalField;
		final IndexFieldAccessor<String> differentTypesField;
		final IndexFieldAccessor<String> sortField;

		IndexAccessors_1_1(IndexSchemaElement root) {
			string = root.field( "string" ).asString().createAccessor();
			additionalField = root.field( "additionalField" ).asString().sortable( Sortable.YES ).store( Store.YES ).createAccessor();
			differentTypesField = root.field( "differentTypesField" ).asString().createAccessor();
			sortField = root.field( "sortField" ).asString().sortable( Sortable.YES ).store( Store.YES ).createAccessor();
		}
	}

	private static class IndexAccessors_1_2 {
		final IndexFieldAccessor<String> string;
		final IndexFieldAccessor<Integer> differentTypesField;
		final IndexFieldAccessor<String> sortField;

		IndexAccessors_1_2(IndexSchemaElement root) {
			string = root.field( "string" ).asString().createAccessor();
			differentTypesField = root.field( "differentTypesField" ).asInteger().createAccessor();
			sortField = root.field( "sortField" ).asString().sortable( Sortable.YES ).store( Store.YES ).createAccessor();
		}
	}

	private static class IndexAccessors_2_1 {
		final IndexFieldAccessor<String> string;

		IndexAccessors_2_1(IndexSchemaElement root) {
			string = root.field( "string" ).asString().createAccessor();
		}
	}
}
