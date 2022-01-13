/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.lang.reflect.Method;

import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

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
public abstract class ArquillianTests extends Arquillian {
	private static final TestLogger log = TestLogger.get(ArquillianTests.class);
	
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
		return ee.jakarta.tck.concurrent.spec.signature.SignatureTestServlet.class.getPackage();
	}
	


	/**
	 * Name of the test being executed. Safe to reference inside of a test method.
	 * Otherwise, set to null.
	 */
	protected String testName;

	protected void setupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during setup due to an exception", t);
	}

	protected void cleanupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during cleanup due to an exception", t);
	}

	// LIFECYCLE METHODS
	@BeforeMethod(groups = "arquillian", inheritGroups = true)
	public void testServerTestEntry(Method testMethod) throws Exception {
		testName = testMethod.getName();
		log.enter(testMethod, testName);
	}

	@AfterMethod(groups = "arquillian", inheritGroups = true, alwaysRun = true)
	public void testServerTestExit(Method testMethod) throws Exception {
		testName = null;
		log.exit(testMethod, testName);
	}

	// ASSERTION METHODS
	protected void assertTrue(boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, testName + " failed");
	}

	protected void assertTrue(String message, boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, message);
	}

	protected void assertFalse(boolean isFalse) {
		org.testng.Assert.assertFalse(isFalse, testName + " failed");
	}

	protected void assertFalse(String message, boolean isFalse) {
		org.testng.Assert.assertFalse(isFalse, message);
	}

	protected void assertEquals(String message, int expected, int actual) {
		org.testng.Assert.assertEquals(actual, expected, message);
	}

	protected void assertEquals(String message, String expected, String actual) {
		org.testng.Assert.assertEquals(actual, expected, message);
	}

	protected void assertNull(Object obj) {
		org.testng.Assert.assertNull(obj,
				testName + " failed the task should return null result, actual result=" + obj);
	}

	protected void assertNull(String message, Object obj) {
		org.testng.Assert.assertNull(obj, message);
	}

	protected void assertNotNull(Object obj) {
		org.testng.Assert.assertNotNull(obj,
				testName + " failed the task should return not null result, actual result=" + obj);
	}

	protected void assertNotNull(String message, Object obj) {
		org.testng.Assert.assertNotNull(obj, message);
	}

	protected void fail(String message) {
		org.testng.Assert.fail(message);
	}

	protected void fail(String message, Throwable t) {
		org.testng.Assert.fail(message, t);
	}

	protected void fail(Throwable t) {
		org.testng.Assert.fail(testName + " failed due to an exception", t);
	}
	
	protected void assertInRange(int value, int min, int max) {
		assertTrue("Expected " + value + " to be in the exclusive range ( " + min + " - " + max + " )", 
				value > min && value < max);
	}
}
