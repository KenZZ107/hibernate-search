/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.gson.impl;

import com.google.gson.JsonElement;

/**
 * @author Yoann Rodiere
 */
public class JsonDoubleAccessorImpl extends TypingJsonAccessor<Double> {

	public JsonDoubleAccessorImpl(JsonAccessor<JsonElement> parentAccessor) {
		super( parentAccessor );
	}

	@Override
	protected JsonElementType<Double> getExpectedElementType() {
		return JsonElementTypes.DOUBLE;
	}

}
