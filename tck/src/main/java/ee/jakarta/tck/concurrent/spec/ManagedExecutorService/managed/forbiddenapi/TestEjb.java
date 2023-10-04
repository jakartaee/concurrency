/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.managed.forbiddenapi;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Stateless
public class TestEjb implements TestEjbInterface {

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    private ManagedExecutorService executor;

    public void testAwaitTermination() {
        assertThrows(IllegalStateException.class, () -> {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        });
    }

    public void testIsShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            executor.isShutdown();
        });
    }

    public void testIsTerminated() {
        assertThrows(IllegalStateException.class, () -> {
            executor.isTerminated();
        });
    }

    public void testShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            executor.shutdown();
        });
    }

    public void testShutdownNow() {
        assertThrows(IllegalStateException.class, () -> {
            executor.shutdownNow();
        });
    }

}
