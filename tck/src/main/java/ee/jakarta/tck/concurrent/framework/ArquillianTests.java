/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.concurrent.framework;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test superclass that should be extended by all test classes in this TCK. This
 * will allow for easier serviceability by abstracting out the testNG specific
 * work and allowing test classes to just focus on test logic.
 * 
 * This class is also the entrypoint for Arquillian to do archive deployment.
 * 
 * Additionally, if in the future we want to replace testNG with another testing
 * framework, only this class will need to be updated.
 * 
 * Uses JUnit method param ordering for ease of portability
 */
public abstract class ArquillianTests {
	
	//##### Common test packages #####
	
	protected static final Package getFrameworkPackage() {
		return ee.jakarta.tck.concurrent.framework.ArquillianTests.class.getPackage();
	}
	
	protected static final Package getCommonPackage() {
		return ee.jakarta.tck.concurrent.common.CommonTasks.class.getPackage();
	}
	
	protected static final Package getCommonCounterPackage() {
		return ee.jakarta.tck.concurrent.common.counter.CounterCallableTask.class.getPackage();
	}
	
	protected static final Package getCommonFixedCounterPackage() {
		return ee.jakarta.tck.concurrent.common.fixed.counter.CounterCallableTask.class.getPackage();
	}
	
	protected static final Package getCommonManagedTaskListener() {
		return ee.jakarta.tck.concurrent.common.managedTaskListener.ManagedTaskListenerImpl.class.getPackage();
	}
	
	protected static final Package getContextPackage() {
		return ee.jakarta.tck.concurrent.common.context.StringContext.class.getPackage();
	}
	
	protected static final Package getContextProvidersPackage() {
		return ee.jakarta.tck.concurrent.spi.context.IntContextProvider.class.getPackage();
	}
	
	protected static final Package getSignaturePackage() {
		return ee.jakarta.tck.concurrent.framework.signaturetest.SigTestEE.class.getPackage();
	}
	
	protected void assertInRange(int value, int min, int max) {
		assertTrue(value > min && value < max, "Expected " + value + " to be in the exclusive range ( " + min + " - " + max + " )");
	}
}
