/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.api.AbortedException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.ArquillianTests;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import jakarta.enterprise.concurrent.AbortedException;

public class AbortedExceptionTests extends ArquillianTests {
	
	private static final TestLogger log = TestLogger.get(AbortedExceptionTests.class);
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="AbortedExceptionTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), AbortedExceptionTests.class.getPackage());
	}

	/*
	 * @testName: AbortedExceptionNoArgTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:1
	 * 
	 * @test_Strategy: Constructs an AbortedException.
	 */
	@Test
	public void AbortedExceptionNoArgTest() {
		boolean pass = false;
		try {
			throw new AbortedException();
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			if (ae.getMessage() == null) {
				log.info("Received expected null message");
				pass = true;
			} else {
				log.warning("AbortedException should have had null message, actual message=" + ae.getMessage());
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		assertTrue(pass);
	}

	/*
	 * @testName: AbortedExceptionStringTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:3
	 * 
	 * @test_Strategy: Constructs an AbortedException.
	 */
	@Test
	public void AbortedExceptionStringTest() {
		boolean pass = false;
		String expected = "thisisthedetailmessage";
		try {
			throw new AbortedException(expected);
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			if (ae.getMessage().equals(expected)) {
				log.info("Received expected message");
				pass = true;
			} else {
				log.warning("Expected:" + expected + ", actual message=" + ae.getMessage());
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		assertTrue(pass);
	}

	/*
	 * @testName: AbortedExceptionThrowableTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:4
	 * 
	 * @test_Strategy: Constructs an AbortedException.
	 */
	@Test
	public void AbortedExceptionThrowableTest() {
		boolean pass1 = false;
		boolean pass2 = false;
		Throwable expected = new Throwable("thisisthethrowable");
		try {
			throw new AbortedException(expected);
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			Throwable cause = ae.getCause();
			if (cause.equals(expected)) {
				log.info("Received expected cause");
				pass1 = true;
			} else {
				log.warning("Expected:" + expected + ", actual message=" + cause);
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		expected = null;
		try {
			throw new AbortedException(expected);
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			Throwable cause = ae.getCause();
			if (cause == null) {
				log.info("Received expected null cause");
				pass2 = true;
			} else {
				log.warning("Expected:null, actual message=" + cause);
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		assertTrue(pass1);
		assertTrue(pass2);
	}

	/*
	 * @testName: AbortedExceptionStringThrowableTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:2
	 * 
	 * @test_Strategy: Constructs an AbortedException.
	 */
	@Test
	public void AbortedExceptionStringThrowableTest() {
		boolean pass1 = false;
		boolean pass2 = false;
		boolean pass3 = false;
		boolean pass4 = false;
		String sExpected = "thisisthedetailmessage";
		Throwable tExpected = new Throwable("thisisthethrowable");
		try {
			throw new AbortedException(sExpected, tExpected);
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			if (ae.getMessage().equals(sExpected)) {
				log.info("Received expected message");
				pass1 = true;
			} else {
				log.warning("Expected:" + sExpected + ", actual message=" + ae.getMessage());
			}
			Throwable cause = ae.getCause();
			if (cause.equals(tExpected)) {
				log.info("Received expected cause");
				pass2 = true;
			} else {
				log.warning("Expected:" + tExpected + ", actual message=" + cause);
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		tExpected = null;
		try {
			throw new AbortedException(sExpected, tExpected);
		} catch (AbortedException ae) {
			log.info("AbortedException Caught as Expected");
			if (ae.getMessage().equals(sExpected)) {
				log.info("Received expected message");
				pass3 = true;
			} else {
				log.warning("Expected:" + sExpected + ", actual message=" + ae.getMessage());
			}
			Throwable cause = ae.getCause();
			if (cause == null) {
				log.info("Received expected null cause");
				pass4 = true;
			} else {
				log.warning("Expected:null, actual message=" + cause);
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}

		assertTrue(pass1);
		assertTrue(pass2);
		assertTrue(pass3);
		assertTrue(pass4);
	}
}
