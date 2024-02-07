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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.servlet.annotation.WebServlet;

@ManagedExecutorDefinition(name = "java:app/concurrent/ManagedExecutorAnnoPlatform", virtual = false)
@ManagedExecutorDefinition(name = "java:app/concurrent/ManagedExecutorAnnoVirtual", virtual = true)

@ManagedScheduledExecutorDefinition(name = "java:app/concurrent/ManagedScheduledExecutorAnnoPlatform", virtual = false)
@ManagedScheduledExecutorDefinition(name = "java:app/concurrent/ManagedScheduledExecutorAnnoVirtual", virtual = true)

@ManagedThreadFactoryDefinition(name = "java:app/concurrent/ThreadFactoryAnnoPlatform", virtual = false)
@ManagedThreadFactoryDefinition(name = "java:app/concurrent/ThreadFactoryAnnoVirtual", virtual = true)

@WebServlet("/VirtualThreadServlet")
public class VirtualThreadServlet extends TestServlet {

    private static final long serialVersionUID = 1L;

    private static final Runnable NOOP_RUNNABLE = () -> {
    };

    public void testPlatformExecutor() throws Exception {
        ManagedExecutorService platformManagedExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorAnnoPlatform");
        ManagedExecutorService platformManagedExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorDDPlatform");

        Future<Thread> future;

        future = platformManagedExecutorAnno.submit(Thread::currentThread);
        assertFalse(isVirtual(future.get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        future = platformManagedExecutorDD.submit(Thread::currentThread);
        assertFalse(isVirtual(future.get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));
    }

    public void testVirtualExecutor() throws Exception {
        ManagedExecutorService virtualManagedExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorAnnoVirtual");
        ManagedExecutorService virtualManagedExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorDDVirtual");

        // If assumeTrue checks pass it means the platform supports virtual threads and
        // we can continue testing, otherwise the test will return successfully at this
        // point and not continue.
        assumeTrue(isVirtual(virtualManagedExecutorAnno.submit(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));
        assumeTrue(isVirtual(virtualManagedExecutorDD.submit(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        // Test invokeAll on virtual threads
        List<Future<Object>> results = virtualManagedExecutorAnno.invokeAll(
                List.of(new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorAnnoVirtual"),
                        new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorAnnoVirtual"),
                        new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorAnnoVirtual")),
                TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);

        assertEquals(3, results.size());

        Object result0 = results.get(0).get(1, TimeUnit.MILLISECONDS);
        Object result1 = results.get(1).get(1, TimeUnit.MILLISECONDS);
        Object result2 = results.get(2).get(1, TimeUnit.MILLISECONDS);

        Set<Thread> virtualThreads = new HashSet<Thread>();

        assertNotNull(result0);
        if (result0 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result0);
        assertTrue(isVirtual((Thread) result0));
        virtualThreads.add((Thread) result0);

        assertNotNull(result1);
        if (result1 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result1);
        assertTrue(isVirtual((Thread) result1));
        virtualThreads.add((Thread) result1);

        assertNotNull(result2);
        if (result2 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result2);
        assertTrue(isVirtual((Thread) result2));
        virtualThreads.add((Thread) result2);

        assertEquals(3, virtualThreads.size(), "Each task execution should have used a different virtual thread");

        // Test invokeAny on virtual threads
        Object result = virtualManagedExecutorDD
                .invokeAny(List.of(new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorDDVirtual"),
                        new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorDDVirtual")));

        assertNotNull(result);
        if (result instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result);
        assertTrue(isVirtual((Thread) result));

    }

    public void testPlatformScheduledExecutor() throws Exception {
        ManagedScheduledExecutorService platformManagedScheduledExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorAnnoPlatform");
        ManagedScheduledExecutorService platformManagedScheduledExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorDDPlatform");

        Future<Thread> future;

        future = platformManagedScheduledExecutorAnno.submit(Thread::currentThread);
        assertFalse(isVirtual(future.get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        future = platformManagedScheduledExecutorDD.submit(Thread::currentThread);
        assertFalse(isVirtual(future.get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));
    }

    public void testVirtualScheduledExecutor() throws Exception {
        ManagedScheduledExecutorService virtualManagedScheduledExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorAnnoVirtual");
        ManagedScheduledExecutorService virtualManagedScheduledExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorDDVirtual");

        // If assumeTrue checks pass it means the platform supports virtual threads and
        // we can continue testing, otherwise the test will return successfully at this
        // point and not continue.
        assumeTrue(isVirtual(virtualManagedScheduledExecutorAnno.submit(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));
        assumeTrue(isVirtual(virtualManagedScheduledExecutorDD.submit(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        // Test schedule on virtual threads
        final LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<>();
        ScheduledFuture<?> oneTimeFuture = virtualManagedScheduledExecutorDD.schedule(
                (Runnable) new LookupActionCaptureThread(results,
                        "java:app/concurrent/ManagedScheduledExecutorDDVirtual"),
                TestConstants.pollInterval.toMillis(), TimeUnit.MILLISECONDS);

        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; 2 != results.size(); Wait.sleep(TestConstants.pollInterval)) {
                // Wait till we have exactly 2 results indicating 1 run of schedule
            }
        });

        assertTrue(oneTimeFuture.isDone());
        assertEquals(2, results.size());

        Thread thread = (Thread) results.poll(1, TimeUnit.MILLISECONDS);
        Object result = results.poll(1, TimeUnit.MILLISECONDS);

        if (result instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result);
        assertTrue(isVirtual((Thread) thread));

        // Test scheduleAtFixedRate on virtual threads
        final LinkedBlockingQueue<Object> resultsFixedRate = new LinkedBlockingQueue<>();
        ScheduledFuture<?> future = virtualManagedScheduledExecutorAnno.scheduleAtFixedRate(
                new LookupActionCaptureThread(resultsFixedRate, "java:app/concurrent/ManagedScheduledExecutorAnnoVirtual"),
                0, TestConstants.pollInterval.toMillis(), TimeUnit.MILLISECONDS);

        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; 6 >= resultsFixedRate.size(); Wait.sleep(TestConstants.pollInterval)) {
                // Wait till we have at least 6 results indicating 3 runs of schedule
            }
        });

        Wait.waitCancelFuture(future); // Cancel execution of timer

        int resultsAfterCancel = resultsFixedRate.size();
        assertTrue(resultsAfterCancel >= 6, "Should have executed schedule at least 3 times.");

        Thread thread0 = (Thread) resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);
        Object result0 = resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);

        Thread thread1 = (Thread) resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);
        Object result1 = resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);

        Thread thread2 = (Thread) resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);
        Object result2 = resultsFixedRate.poll(1, TimeUnit.MILLISECONDS);
        
        assertEquals(resultsAfterCancel - 6, resultsFixedRate.size(),
                "No more results should have been added to queue after task was cancelled");

        Set<Thread> virtualThreads = new HashSet<Thread>();

        if (result0 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result0);
        assertTrue(isVirtual((Thread) thread0));
        virtualThreads.add((Thread) thread0);

        if (result1 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result1);
        assertTrue(isVirtual((Thread) thread1));
        virtualThreads.add((Thread) thread1);

        if (result2 instanceof Throwable)
            throw new AssertionError("An error occured on thread.", (Throwable) result2);
        assertTrue(isVirtual((Thread) thread2));
        virtualThreads.add((Thread) thread2);

        assertEquals(3, virtualThreads.size(), "Each task execution should have used a different virtual thread");
    }

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
        // we can continue testing, otherwise the test will return successfully at this
        // point and not continue.
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
    class LookupAction implements Runnable, Callable<Object> {

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

        @Override
        public Object call() throws Exception {
            try {
                return InitialContext.doLookup(resource);
            } catch (Throwable x) {
                return x;
            }
        }
        
        public BlockingQueue<Object>  getResults() {
            return results;
        }
        
        public String getResource() {
            return resource;
        }
    }

    /**
     * Captures thread this actions is run on in addition to performing the simple
     * {@link LookupAction}
     */
    class LookupActionCaptureThread extends LookupAction {

        public LookupActionCaptureThread(final BlockingQueue<Object> results, final String resource) {
            super(results, resource);
        }

        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            getResults().add(thread);
            super.run();
        }

        @Override
        public Object call() throws Exception {
            try {
                InitialContext.doLookup(getResource());
            } catch (Throwable x) {
                return x;
            }
            return Thread.currentThread();
        }
    }

}
