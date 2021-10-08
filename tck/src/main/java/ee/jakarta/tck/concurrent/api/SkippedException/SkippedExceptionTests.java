/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent.api.SkippedException;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;

import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.SkippedException;
import jakarta.enterprise.concurrent.tck.framework.TestLogger;

public class SkippedExceptionTests extends TestClient {

	private static final TestLogger log = TestLogger.get(SkippedExceptionTests.class);

	/*
	 * @testName: SkippedExceptionNoArgTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:42
	 * 
	 * @test_Strategy: Constructs an SkippedException.
	 */
	@Test
	public void SkippedExceptionNoArgTest() {
		boolean pass = false;
		try {
			throw new SkippedException();
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			if (se.getMessage() == null) {
				log.info("Received expected null message");
				pass = true;
			} else {
				log.warning("SkippedException should have had null message, actual message=" + se.getMessage());
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: SkippedExceptionStringTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:43
	 * 
	 * @test_Strategy: Constructs an SkippedException.
	 */
	@Test
	public void SkippedExceptionStringTest() {
		boolean pass = false;
		String expected = "thisisthedetailmessage";
		try {
			throw new SkippedException(expected);
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			if (se.getMessage().equals(expected)) {
				log.info("Received expected message");
				pass = true;
			} else {
				log.info("Expected:" + expected + ", actual message=" + se.getMessage());
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: SkippedExceptionThrowableTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:45
	 * 
	 * @test_Strategy: Constructs an SkippedException.
	 */
	@Test
	public void SkippedExceptionThrowableTest() {
		boolean pass1 = false;
		boolean pass2 = false;
		Throwable expected = new Throwable("thisisthethrowable");
		try {
			throw new SkippedException(expected);
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			Throwable cause = se.getCause();
			if (cause.equals(expected)) {
				log.info("Received expected cause");
				pass1 = true;
			} else {
				log.info("Expected:" + expected + ", actual message=" + cause);
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}

		expected = null;
		try {
			throw new SkippedException(expected);
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			Throwable cause = se.getCause();
			if (cause == null) {
				log.info("Received expected null cause");
				pass2 = true;
			} else {
				log.info("Expected:null, actual message=" + cause);
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(pass1);
		assertTrue(pass2);
	}

	/*
	 * @testName: SkippedExceptionStringThrowableTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:44
	 * 
	 * @test_Strategy: Constructs an SkippedException.
	 */
	@Test
	public void SkippedExceptionStringThrowableTest() {
		boolean pass1 = false;
		boolean pass2 = false;
		boolean pass3 = false;
		boolean pass4 = false;
		String sExpected = "thisisthedetailmessage";
		Throwable tExpected = new Throwable("thisisthethrowable");
		try {
			throw new SkippedException(sExpected, tExpected);
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			if (se.getMessage().equals(sExpected)) {
				log.info("Received expected message");
				pass1 = true;
			} else {
				log.info("Expected:" + sExpected + ", actual message=" + se.getMessage());
			}
			Throwable cause = se.getCause();
			if (cause.equals(tExpected)) {
				log.info("Received expected cause");
				pass2 = true;
			} else {
				log.info("Expected:" + tExpected + ", actual message=" + cause);
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}

		tExpected = null;
		try {
			throw new SkippedException(sExpected, tExpected);
		} catch (SkippedException se) {
			log.info("SkippedException Caught as Expected");
			if (se.getMessage().equals(sExpected)) {
				log.info("Received expected message");
				pass3 = true;
			} else {
				log.info("Expected:" + sExpected + ", actual message=" + se.getMessage());
			}
			Throwable cause = se.getCause();
			if (cause == null) {
				log.info("Received expected null cause");
				pass4 = true;
			} else {
				log.info("Expected:null, actual message=" + cause);
			}
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(pass1);
		assertTrue(pass2);
		assertTrue(pass3);
		assertTrue(pass4);
	}
}
