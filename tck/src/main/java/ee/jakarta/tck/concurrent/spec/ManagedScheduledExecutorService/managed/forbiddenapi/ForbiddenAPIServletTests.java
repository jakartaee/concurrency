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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.TestConstants;
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

    private static final String DIDNOT_CATCH_ILLEGALSTATEEXCEPTION = "IllegalStateException expected";

    @Resource(lookup = TestConstants.DefaultManagedScheduledExecutorService)
    public ManagedScheduledExecutorService scheduledExecutor;

    @BeforeEach
    protected void before() {
        StaticCounter.reset();
    }

    /*
     * @testName: testAwaitTermination
     * 
     * @assertion_ids: CONCURRENCY:SPEC:57.1
     * 
     * @test_Strategy:
     */
    @Test
    public void testAwaitTermination() {
        try {
            scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (IllegalStateException e) {
            return;
        }
        fail(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
    }

    /*
     * @testName: testIsShutdown
     * 
     * @assertion_ids: CONCURRENCY:SPEC:57.2
     * 
     * @test_Strategy:
     */
    @Test
    public void testIsShutdown() {
        try {
            scheduledExecutor.isShutdown();
        } catch (IllegalStateException e) {
            return;
        }
        fail(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
    }

    /*
     * @testName: testIsTerminated
     * 
     * @assertion_ids: CONCURRENCY:SPEC:57.3
     * 
     * @test_Strategy:
     */
    @Test
    public void testIsTerminated() {
        try {
            scheduledExecutor.isTerminated();
        } catch (IllegalStateException e) {
            return;
        }
        fail(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
    }

    /*
     * @testName: testShutdown
     * 
     * @assertion_ids: CONCURRENCY:SPEC:57.4
     * 
     * @test_Strategy:
     */
    @Test
    public void testShutdown() {
        try {
            scheduledExecutor.shutdown();
        } catch (IllegalStateException e) {
            return;
        }
        fail(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
    }

    /*
     * @testName: testShutdownNow
     * 
     * @assertion_ids: CONCURRENCY:SPEC:57.5
     * 
     * @test_Strategy:
     */
    @Test
    public void testShutdownNow() {
        try {
            scheduledExecutor.shutdownNow();
        } catch (IllegalStateException e) {
            return;
        }
        fail(DIDNOT_CATCH_ILLEGALSTATEEXCEPTION);
    }
}
