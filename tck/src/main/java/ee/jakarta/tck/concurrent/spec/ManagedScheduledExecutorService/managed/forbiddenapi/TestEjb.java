/*
 * Copyright (c) 2013, 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.managed.forbiddenapi;

import java.util.concurrent.TimeUnit;

import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;

@Stateless
public class TestEjb implements TestEjbRemote {

	private static final String DIDNOT_CATCH_ILLEGALSTATEEXCEPTION = "IllegalStateException expected";

	public void testAwaitTermination() {
		try {
			TestUtil.getManagedScheduledExecutorService().awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			return;
		}
		throw new RuntimeException(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
	}

	public void testIsShutdown() {
		try {
			TestUtil.getManagedScheduledExecutorService().isShutdown();
		} catch (IllegalStateException e) {
			return;
		}
		throw new RuntimeException(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
	}

	public void testIsTerminated() {
		try {
			TestUtil.getManagedScheduledExecutorService().isTerminated();
		} catch (IllegalStateException e) {
			return;
		}
		throw new RuntimeException(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
	}

	public void testShutdown() {
		try {
			TestUtil.getManagedScheduledExecutorService().shutdown();
		} catch (IllegalStateException e) {
			return;
		}
		throw new RuntimeException(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
	}

	public void testShutdownNow() {
		try {
			TestUtil.getManagedScheduledExecutorService().shutdownNow();
		} catch (IllegalStateException e) {
			return;
		}
		throw new RuntimeException(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
	}

}
