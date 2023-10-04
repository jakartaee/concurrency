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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.managed.forbiddenapi;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;

import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;

@Web
@Common({ PACKAGE.FIXED_COUNTER })
public class ForbiddenAPIServletTests {

    @Deployment(name = "ForbiddenAPIServletTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.defaultManagedScheduledExecutorService)
    private ManagedScheduledExecutorService scheduledExecutor;

    @BeforeEach
    protected void before() {
        StaticCounter.reset();
    }

    @Assertion(id = "SPEC:56 SPEC:57.1", strategy = "Test basic function for ManagedScheduledExecutorService: awaitTermination")
    public void testAwaitTermination() {
        assertThrows(IllegalStateException.class, () -> {
            scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
        });
    }

    @Assertion(id = "SPEC:56 SPEC:57.2", strategy = "Test basic function for ManagedScheduledExecutorService: isShutdown")
    public void testIsShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            scheduledExecutor.isShutdown();
        });
    }

    @Assertion(id = "SPEC:56 SPEC:57.3", strategy = "Test basic function for ManagedScheduledExecutorService: isTerminated")
    public void testIsTerminated() {
        assertThrows(IllegalStateException.class, () -> {
            scheduledExecutor.isTerminated();
        });
    }

    @Assertion(id = "SPEC:56 SPEC:57.4", strategy = "Test basic function for ManagedScheduledExecutorService: shutdown")
    public void testShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            scheduledExecutor.shutdown();
        });
    }

    @Assertion(id = "SPEC:56 SPEC:57.5", strategy = "Test basic function for ManagedScheduledExecutorService: shutdownNow")
    public void testShutdownNow() {
        assertThrows(IllegalStateException.class, () -> {
            scheduledExecutor.shutdownNow();
        });
    }
}
