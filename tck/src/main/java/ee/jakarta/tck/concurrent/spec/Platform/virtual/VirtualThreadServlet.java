/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.Platform.virtual;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.servlet.annotation.WebServlet;

@ManagedThreadFactoryDefinition(name = "java:app/concurrent/ThreadFactoryAnnoPlatform", virtual = false)
@ManagedThreadFactoryDefinition(name = "java:app/concurrent/ThreadFactoryAnnoVirtual", virtual = true)
@WebServlet("/VirtualThreadServlet")
public class VirtualThreadServlet extends TestServlet {

    private static final long serialVersionUID = 1L;

    private static final Runnable NOOP_RUNNABLE = () -> {
    };

    // TODO add virtual tests for ManagedExecutor and ManagedScheduledExcecutor

    public void testPlatformThreadFactory() throws Exception {
        ManagedThreadFactory platfromThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoPlatform");
        ManagedThreadFactory platfromThreadFactoryDD = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryDDPlatform");

        assertFalse(isVirtual(platfromThreadFactoryAnno.newThread(NOOP_RUNNABLE)),
                "Thread Factory should not have returned a virtual thread when defined with virtual=false");
        assertFalse(isVirtual(platfromThreadFactoryDD.newThread(NOOP_RUNNABLE)),
                "Thread Factory should not have returned a virtual thread when defined with virtual=false");
    }

    public void testVirtualThreadFactory() throws Exception {
        ManagedThreadFactory virtualThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoVirtual");
        ManagedThreadFactory virtualThreadFactoryDD = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryDDVirtual");

        // If assumeTrue checks pass it means the platform supports virtual threads and
        // we can continue testing,
        // otherwise the test will return successfully at this point and not continue.
        assumeTrue(isVirtual(virtualThreadFactoryAnno.newThread(NOOP_RUNNABLE)));
        assumeTrue(isVirtual(virtualThreadFactoryDD.newThread(NOOP_RUNNABLE)));

        LinkedBlockingQueue<Object> results;
        Object result;

        // Test virtual thread from Annotation
        results = new LinkedBlockingQueue<Object>();
        Thread annoThread = virtualThreadFactoryAnno
                .newThread(new LookupAction(results, "java:app/concurrent/ThreadFactoryAnnoVirtual"));
        annoThread.start();
        result = results.poll(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        assertNotNull(result);
        if (result instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result);

        // Test virtual thread from Deployment Descriptor
        results = new LinkedBlockingQueue<Object>();
        Thread ddThread = virtualThreadFactoryDD
                .newThread(new LookupAction(results, "java:app/concurrent/ThreadFactoryDDVirtual"));
        ddThread.start();
        result = results.poll(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        assertNotNull(result);
        if (result instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result);
    }

    /**
     * Uses reflection to call method isVirtual on on the supplied thread.
     *
     * @param thread - the thread being tested
     * @return true, if the thread is virtual; false, if the thread is not virtual
     *
     * @throws Exception when unable to call the isVirtual method
     */
    private boolean isVirtual(final Thread thread) throws Exception {
        Method isVirtual = Thread.class.getMethod("isVirtual");
        return (boolean) isVirtual.invoke(thread);
    }

    /**
     * A simple lookup action that can pass/fail on a virtual thread
     */
    class LookupAction implements Runnable {

        private BlockingQueue<Object> results;
        private String resource;

        public LookupAction(final BlockingQueue<Object> results, final String resource) {
            this.results = results;
            this.resource = resource;
        }

        @Override
        public void run() {
            try {
                results.add(InitialContext.doLookup(resource));
            } catch (Throwable x) {
                results.add(x);
            }
        }

    }

}
