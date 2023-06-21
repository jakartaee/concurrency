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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Web
public class ForbiddenAPIServletTests {

    @Deployment(name = "ForbiddenAPIServletTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.DefaultManagedExecutorService)
    public ManagedExecutorService mes;

    /*
     * @testName: testAwaitTermination
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.1;
     * 
     * @test_Strategy:
     */
    @Test
    public void testAwaitTermination() {
        try {
            mes.awaitTermination(10, TimeUnit.SECONDS);
        } catch (IllegalStateException e) { // what expected.
            return;
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /*
     * @testName: testIsShutdown
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.2;
     * 
     * @test_Strategy:
     */
    @Test
    public void testIsShutdown() {
        try {
            mes.isShutdown();
        } catch (IllegalStateException e) { // what expected
            return;
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /*
     * @testName: testIsTerminated
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.3;
     * 
     * @test_Strategy:
     */
    @Test
    public void testIsTerminated() {
        try {
            mes.isTerminated();
        } catch (IllegalStateException e) { // what expected
            return;
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /*
     * @testName: testShutdown
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.4;
     * 
     * @test_Strategy:
     */
    @Test
    public void testShutdown() {
        try {
            mes.shutdown();
        } catch (IllegalStateException e) { // what expected
            return;
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /*
     * @testName: testShutdownNow
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.5;
     * 
     * @test_Strategy:
     */
    @Test
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
