/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.impl;

import org.hibernate.search.mapper.pojo.model.spi.PojoState;
import org.hibernate.search.mapper.pojo.model.spi.PojoModelElementAccessor;

class PojoRootAccessor<T> implements PojoModelElementAccessor<T> {

	private final Class<T> type;

	PojoRootAccessor(Class<T> type) {
		super();
		this.type = type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public T read(PojoState bridgedElement) {
		return type.cast( ((PojoStateImpl) bridgedElement).get() );
	}

}