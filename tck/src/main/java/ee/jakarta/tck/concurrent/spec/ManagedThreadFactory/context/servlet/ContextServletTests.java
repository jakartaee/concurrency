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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.context.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.tasks.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Web
@Common({ PACKAGE.TASKS })
public class ContextServletTests {

    @Deployment(name = "ContextServletTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(ContextServletTests.class.getPackage(), "web.xml", "web.xml");
    }

    private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

    private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedThreadFactory_test_string";

    private static final String TEST_CLASSLOADER_CLASS_NAME = ContextServletTests.class.getCanonicalName();

    /*
     * @testName: jndiClassloaderPropagationTest
     * 
     * @assertion_ids: CONCURRENCY:SPEC:96.7; CONCURRENCY:SPEC:100;
     * CONCURRENCY:SPEC:106;
     * 
     * @test_Strategy:
     */
    @Test
    public void jndiClassloaderPropagationTest() throws Exception {
        ManagedThreadFactory factory = InitialContext.doLookup(TestConstants.DefaultManagedThreadFactory);

        RunnableTask task = new RunnableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE,
                TEST_CLASSLOADER_CLASS_NAME);
        Thread thread = factory.newThread(task);
        thread.start();
        TestUtil.waitTillThreadFinish(thread);
        assertEquals(task.getCount(), 1);
    }

}
