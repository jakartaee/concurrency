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

package ee.jakarta.tck.concurrent.api.ManagedThreadFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Web
@Common({ PACKAGE.TASKS, PACKAGE.FIXED_COUNTER })
public class ManagedThreadFactoryTests extends TestClient {

    @Deployment(name = "APITests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addPackages(true, ManagedThreadFactoryTests.class.getPackage());
    }

    @Resource(lookup = TestConstants.defaultManagedThreadFactory)
    private ManagedThreadFactory threadFactory;

    @Assertion(id = "JAVADOC:20 SPEC:99.1",
            strategy = "Lookup default ManagedThreadFactory object and create new thread."
                    + " Check return value of method isShutdown of new thread.")
    public void isShutdown() {
        ManageableThread m = (ManageableThread) threadFactory.newThread(new CounterRunnableTask());
        assertFalse(m.isShutdown());
    }

    @Assertion(id = "SPEC:83 SPEC:83.1 SPEC:83.2 SPEC:83.3 SPEC:103 SPEC:96.5 SPEC:96.6 SPEC:105 SPEC:96 SPEC:93 SPEC:96.3",
            strategy = "Interrupt thread and ensure the thread did not run.")
    public void interruptThreadApiTest() {
        CounterRunnableTask task = new CounterRunnableTask(TestConstants.pollInterval);
        Thread thread = threadFactory.newThread(task);
        thread.start();
        thread.interrupt();
        Wait.waitTillThreadFinish(thread);
        assertEquals(0, StaticCounter.getCount());
    }

    @Assertion(id = "SPEC:97;", strategy = "Create thread and ensure the thread is an instance of ManageableThread")
    public void implementsManageableThreadInterfaceTest() {
        CounterRunnableTask task = new CounterRunnableTask();
        Thread thread = threadFactory.newThread(task);
        assertTrue(thread instanceof ManageableThread,
                "The thread returned by ManagedThreadFactory should be instance of ManageableThread.");
    }

}
