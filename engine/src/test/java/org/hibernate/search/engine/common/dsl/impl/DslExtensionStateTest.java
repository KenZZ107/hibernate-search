/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.common.dsl.impl;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import org.hibernate.search.util.SearchException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.easymock.EasyMockSupport;

public class DslExtensionStateTest extends EasyMockSupport {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@SuppressWarnings("unchecked")
	private Consumer<Object> contextConsumer = createMock( Consumer.class );

	private DslExtensionState state = new DslExtensionState();

	@Test
	public void ifSupported_noSupported() {
		String extensionToString = "EXTENSION_TO_STRING";
		resetAll();
		replayAll();
		state.ifSupported( new MyExtension( extensionToString ), Optional.empty(), contextConsumer );
		verifyAll();
	}

	@Test
	public void ifSupported_supported() {
		Object extendedContext = new Object();

		resetAll();
		contextConsumer.accept( extendedContext );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext ), contextConsumer );
		verifyAll();
	}

	@Test
	public void orElse() {
		Object defaultContext = new Object();

		resetAll();
		contextConsumer.accept( defaultContext );
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void orElseFail() {
		thrown.expect( SearchException.class );
		thrown.expectMessage( "None of the provided extensions can be applied to the current context" );
		thrown.expectMessage( Collections.emptyList().toString() );
		state.orElseFail();
	}

	@Test
	public void ifSupportedThenOrElseFail_noSupported() {
		String extensionToString = "EXTENSION_TO_STRING";
		resetAll();
		replayAll();
		state.ifSupported( new MyExtension( extensionToString ), Optional.empty(), contextConsumer );
		thrown.expect( SearchException.class );
		thrown.expectMessage( "None of the provided extensions can be applied to the current context" );
		thrown.expectMessage( extensionToString );
		try {
			state.orElseFail();
		}
		finally {
			verifyAll();
		}
	}

	@Test
	public void ifSupportedThenOrElseFail_supported() {
		Object extendedContext = new Object();

		resetAll();
		contextConsumer.accept( extendedContext );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElseFail();
		verifyAll();
	}

	@Test
	public void ifSupportedThenOrElse_noSupported() {
		Object defaultContext = new Object();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		contextConsumer.accept( defaultContext );
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void ifSupportedThenOrElse_supported() {
		Object extendedContext = new Object();
		Object defaultContext = new Object();

		resetAll();
		contextConsumer.accept( extendedContext );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void multipleIfSupported_noSupported() {
		String extension1ToString = "EXTENSION1_TO_STRING";
		String extension2ToString = "EXTENSION2_TO_STRING";
		String extension3ToString = "EXTENSION3_TO_STRING";

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension( extension1ToString ), Optional.empty(), contextConsumer );
		state.ifSupported( new MyExtension( extension2ToString ), Optional.empty(), contextConsumer );
		state.ifSupported( new MyExtension( extension3ToString ), Optional.empty(), contextConsumer );
		thrown.expect( SearchException.class );
		thrown.expectMessage( "None of the provided extensions can be applied to the current context" );
		thrown.expectMessage( extension1ToString );
		thrown.expectMessage( extension2ToString );
		thrown.expectMessage( extension3ToString );
		try {
			state.orElseFail();
		}
		finally {
			verifyAll();
		}
	}

	@Test
	public void multipleIfSupported_singleSupported() {
		Object extendedContext1 = new Object();

		resetAll();
		contextConsumer.accept( extendedContext1 );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext1 ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElseFail();
		verifyAll();
	}

	@Test
	public void multipleIfSupported_multipleSupported() {
		Object extendedContext2 = new Object();
		Object extendedContext3 = new Object();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		contextConsumer.accept( extendedContext2 );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext2 ), contextConsumer );
		verifyAll();

		// Only the first supported extension should be applied
		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext3 ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElseFail();
		verifyAll();
	}

	@Test
	public void multipleIfSupportedThenOrElse_noSupported() {
		Object defaultContext = new Object();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		contextConsumer.accept( defaultContext );
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void multipleIfSupportedThenOrElse_singleSupported() {
		Object extendedContext1 = new Object();
		Object defaultContext = new Object();

		resetAll();
		contextConsumer.accept( extendedContext1 );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext1 ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void multipleIfSupportedThenOrElse_multipleSupported() {
		Object extendedContext2 = new Object();
		Object extendedContext3 = new Object();
		Object defaultContext = new Object();

		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		verifyAll();

		resetAll();
		contextConsumer.accept( extendedContext2 );
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext2 ), contextConsumer );
		verifyAll();

		// Only the first supported extension should be applied
		resetAll();
		replayAll();
		state.ifSupported( new MyExtension(), Optional.of( extendedContext3 ), contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();
	}

	@Test
	public void orElseThenIfSupported() {
		Object defaultContext = new Object();

		resetAll();
		contextConsumer.accept( defaultContext );
		replayAll();
		state.orElse( defaultContext, contextConsumer );
		verifyAll();

		resetAll();
		replayAll();
		thrown.expect( SearchException.class );
		thrown.expectMessage( "Cannot call ifSupported(...) after orElse(...)" );
		try {
			state.ifSupported( new MyExtension(), Optional.empty(), contextConsumer );
		}
		finally {
			verifyAll();
		}
	}

	private static class MyExtension {
		private final String name;

		MyExtension() {
			this( null );
		}

		MyExtension(String name) {
			this.name = name;
		}

		public String toString() {
			if ( name != null ) {
				return name;
			}
			else {
				return super.toString();
			}
		}
	}

}