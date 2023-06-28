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

package ee.jakarta.tck.concurrent.api.ContextService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.WorkInterface;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTaskListener;

@Web
@Common( { PACKAGE.FIXED_COUNTER } )
public class ContextServiceTests {

    // TODO deploy as EJB and JSP artifacts
    @Deployment(name = "ContextServiceTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.DefaultContextService)
    public ContextService context;

    /*
     * @testName: ContextServiceWithIntf
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:5
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using instance and interface.
     */
    @Test
    public void ContextServiceWithIntf() {
        assertAll(() -> {
            Runnable proxy = (Runnable) context.createContextualProxy(new CounterRunnableTask(), Runnable.class);
            assertNotNull(proxy);
        });
    }

    /*
     * @testName: ContextServiceWithIntfAndIntfNoImplemented
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:6
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using instance and interface. if the instance does not implement the
     * specified interface, IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithIntfAndIntfNoImplemented() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.createContextualProxy(new Object(), Runnable.class);
        });
    }

    /*
     * @testName: ContextServiceWithIntfAndInstanceIsNull
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:6
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using instance and interface. if the instance is null,
     * IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithIntfAndInstanceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.createContextualProxy(null, Runnable.class);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfs
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:7
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using instance and multiple interfaces.
     */
    @Test
    public void ContextServiceWithMultiIntfs() {
        assertAll(() -> {
            Object proxy = context.createContextualProxy(new CounterRunnableTask(), Runnable.class,
                    WorkInterface.class);
            assertNotNull(proxy);
            assertTrue(proxy instanceof Runnable);
            assertTrue(proxy instanceof WorkInterface);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfsAndIntfNoImplemented
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:8
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using instance and multi interfaces. if the instance does not implement the
     * specified interface, IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithMultiIntfsAndIntfNoImplemented() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.createContextualProxy(new CounterRunnableTask(), Runnable.class, WorkInterface.class,
                    ManagedTaskListener.class);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfsAndInstanceIsNull
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:8
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using object and multi interfaces. if the instance is null,
     * IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithMultiIntfsAndInstanceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            context.createContextualProxy(null, Runnable.class, WorkInterface.class);
        });
    }

    /*
     * @testName: ContextServiceWithIntfAndProperties
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:9
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and interface.
     */
    @Test
    public void ContextServiceWithIntfAndProperties() {
        assertAll(() -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            Runnable proxy = (Runnable) context.createContextualProxy(new CounterRunnableTask(), execProps,
                    Runnable.class);
            assertNotNull(proxy);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfsAndProperties
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:11
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and multiple interfaces.
     */
    @Test
    public void ContextServiceWithMultiIntfsAndProperties() {
        assertAll(() -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            Object proxy = context.createContextualProxy(new CounterRunnableTask(), execProps, Runnable.class,
                    WorkInterface.class);
            assertNotNull(proxy);
            assertTrue(proxy instanceof Runnable);
            assertTrue(proxy instanceof WorkInterface);
        });
    }

    /*
     * @testName: ContextServiceWithIntfAndPropertiesAndIntfNoImplemented
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:10
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and interface. if the instance does not implement
     * the specified interface, IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithIntfAndPropertiesAndIntfNoImplemented() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            context.createContextualProxy(new CounterRunnableTask(), execProps, Runnable.class, ManagedTaskListener.class);
        });
    }

    /*
     * @testName: ContextServiceWithIntfsAndPropertiesAndInstanceIsNull
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:10
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and interfaces. if the instance is null,
     * IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithIntfsAndPropertiesAndInstanceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            context.createContextualProxy(null, execProps, Runnable.class);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfsAndPropertiesAndIntfNoImplemented
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:12
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and multiple interfaces. if the instance does not
     * implement the specified interface, IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithMultiIntfsAndPropertiesAndIntfNoImplemented() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            context.createContextualProxy(new CounterRunnableTask(), execProps, Runnable.class, WorkInterface.class,
                    ManagedTaskListener.class);
        });
    }

    /*
     * @testName: ContextServiceWithMultiIntfsAndPropertiesAndInstanceIsNull
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:12
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and multiple interfaces. if the instance is null,
     * IllegalArgumentException will be thrown
     */
    @Test
    public void ContextServiceWithMultiIntfsAndPropertiesAndInstanceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("vendor_a.security.tokenexpiration", "15000");
            execProps.put("USE_PARENT_TRANSACTION", "true");

            context.createContextualProxy(null, execProps, Runnable.class, CounterRunnableTask.class);
        });
    }

    /*
     * @testName: GetExecutionProperties
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:13
     * 
     * @test_Strategy: Lookup default ContextService object and create proxy object
     * using ExecutionProperties and multiple interfaces. Retrieve
     * ExecutionProperties from proxy object and verify property value.
     */
    @Test
    public void GetExecutionProperties() {
        assertAll(() -> {
            Map<String, String> execProps = new HashMap<String, String>();
            execProps.put("USE_PARENT_TRANSACTION", "true");

            Object proxy = context.createContextualProxy(new CounterRunnableTask(), execProps, Runnable.class,
                    WorkInterface.class);
            assertNotNull(proxy);
            
            Map<String, String> returnedExecProps = context.getExecutionProperties(proxy);
            assertEquals("true", returnedExecProps.get("USE_PARENT_TRANSACTION"));
        });
    }

    /*
     * @testName: GetExecutionPropertiesNoProxy
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:14
     * 
     * @test_Strategy: Lookup default ContextService object. Retrieve
     * ExecutionProperties from plain object.
     */
    @Test
    public void GetExecutionPropertiesNoProxy() {
        assertAll(() -> {
            try {
                context.getExecutionProperties(new Object());
            } catch (IllegalArgumentException ie) {
                // Pass if IAE is thrown, but fail otherwise.
            }
        });
    }
}