/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.backend.index.spi;

import org.hibernate.search.engine.backend.document.spi.DocumentState;
import org.hibernate.search.engine.common.spi.SessionContext;

/**
 * @author Yoann Rodiere
 */
public interface IndexManager<D extends DocumentState> {

	ChangesetIndexWorker<D> createWorker(SessionContext context);

	StreamIndexWorker<D> createStreamWorker(SessionContext context);

}