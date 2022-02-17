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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.managed_servlet.forbiddenapi;

import static org.testng.Assert.fail;

import java.util.concurrent.TimeUnit;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("/ForbiddenServlet")
public class ForbiddenServlet extends TestServlet {
	
	@Resource
	private ManagedExecutorService mes;

	public void testAwaitTermination() {
		try {
			mes.awaitTermination(10, TimeUnit.SECONDS);
		} catch (IllegalStateException e) { // what expected.
			return;
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testIsShutdown() {
		try {
			mes.isShutdown();
		} catch (IllegalStateException e) { // what expected
			return;
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testIsTerminated() {
		try {
			mes.isTerminated();
		} catch (IllegalStateException e) { // what expected
			return;
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testShutdown() {
		try {
			mes.shutdown();
		} catch (IllegalStateException e) { // what expected
			return;
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testShutdownNow() {
		try {
			mes.shutdownNow();
		} catch (IllegalStateException e) { // what expected
			return;
		} catch (Exception e) {
			fail(e.toString());
		}
	}
}
