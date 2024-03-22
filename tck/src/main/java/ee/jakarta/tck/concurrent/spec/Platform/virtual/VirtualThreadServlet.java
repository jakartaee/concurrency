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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
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
    
    private static final TestLogger log = TestLogger.get(VirtualThreadServlet.class);

    private static final Runnable NOOP_RUNNABLE = () -> {
    };
        
    private static final int VERSION = Runtime.version().feature();

    public void testPlatformExecutor() throws Exception {
        ManagedExecutorService platformManagedExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorAnnoPlatform");
        ManagedExecutorService platformManagedExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorDDPlatform");
        
        assertNotNull(platformManagedExecutorAnno);
        assertNotNull(platformManagedExecutorDD);
        
        Thread annoThread = platformManagedExecutorAnno.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        Thread ddThread = platformManagedExecutorDD.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        assertFalse(isVirtual(annoThread));
        assertFalse(isVirtual(ddThread));
    }

    public void testVirtualExecutor() throws Exception {
        ManagedExecutorService virtualManagedExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorAnnoVirtual");
        ManagedExecutorService virtualManagedExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedExecutorDDVirtual");
        
        assertNotNull(virtualManagedExecutorAnno);
        assertNotNull(virtualManagedExecutorDD);
        
        // Force executor to run task on a new thread, if the platform supports virutal=true we should get a virtual thread
        Thread annoThread = virtualManagedExecutorAnno.supplyAsync(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        Thread ddThread = virtualManagedExecutorDD.supplyAsync(Thread::currentThread)
              .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        // Java 21+
        assumingThat(isVirtual(annoThread), () -> {
            // Test invokeAll on potential virtual threads
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
            if (isVirtual((Thread) result0))
                virtualThreads.add((Thread) result0);

            assertNotNull(result1);
            if (result1 instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result1);
            if (isVirtual((Thread) result1))
                virtualThreads.add((Thread) result1);
            

            assertNotNull(result2);
            if (result2 instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result2);
            if (isVirtual((Thread) result2))
                virtualThreads.add((Thread) result2);
            
            // Avoid assertions of how many tasks were executed on virtual threads since there is no guarantee
            log.info("ManagedExecutorService.invokeAll() resulted in " + virtualThreads.size()
                + " out of 3 tasks were run on virtual threads.");
        });
        
        assumingThat(isVirtual(ddThread), () -> {
            // Test invokeAny on virtual threads
            Object result = virtualManagedExecutorDD
                    .invokeAny(List.of(
                            new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorDDVirtual"),
                            new LookupActionCaptureThread(null, "java:app/concurrent/ManagedExecutorDDVirtual")));

            assertNotNull(result);
            if (result instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result);
            
            // Avoid assertion that a task was executed on a virtual thread since there is no guarantee
            if (isVirtual((Thread) result))
                log.info("ManagedExecutorService.invokeAny() resulted in task being run on a virtual thread.");
        });

    }

    public void testPlatformScheduledExecutor() throws Exception {
        ManagedScheduledExecutorService platformManagedScheduledExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorAnnoPlatform");
        ManagedScheduledExecutorService platformManagedScheduledExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorDDPlatform");
        
        assertNotNull(platformManagedScheduledExecutorAnno);
        assertNotNull(platformManagedScheduledExecutorDD);

        Thread annoThread = platformManagedScheduledExecutorAnno.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        Thread ddThread = platformManagedScheduledExecutorDD.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        assertFalse(isVirtual(annoThread));
        assertFalse(isVirtual(ddThread));
    }

    public void testVirtualScheduledExecutor() throws Exception {
        ManagedScheduledExecutorService virtualManagedScheduledExecutorAnno = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorAnnoVirtual");
        ManagedScheduledExecutorService virtualManagedScheduledExecutorDD = InitialContext
                .doLookup("java:app/concurrent/ManagedScheduledExecutorDDVirtual");

        assertNotNull(virtualManagedScheduledExecutorAnno);
        assertNotNull(virtualManagedScheduledExecutorDD);
        
        // Force executor to run task on a new thread, if the platform supports virtual=true we should get a virtual thread
        Thread annoThread = virtualManagedScheduledExecutorAnno.supplyAsync(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        Thread ddThread = virtualManagedScheduledExecutorDD.supplyAsync(Thread::currentThread)
                .get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        // Java 21+
        assumingThat(isVirtual(ddThread), () -> {
            
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
            
            // Avoid assertion that a task was executed on a virtual thread since there is no guarantee
            if (isVirtual((Thread) thread))
                log.info("ManagedScheduledExecutorService.schedule() resulted in task being run on a virtual thread.");

        });
        
        assumingThat(isVirtual(annoThread), () -> {
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
            if (isVirtual((Thread) thread0))
                virtualThreads.add((Thread) thread0);
    
            if (result1 instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result1);
            if (isVirtual((Thread) thread1))
                virtualThreads.add((Thread) thread1);
    
            if (result2 instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result2);
            if (isVirtual((Thread) thread2))
                virtualThreads.add((Thread) thread2);
            
            // Avoid assertions of how many tasks were executed on virtual threads since there is no guarantee
            log.info("ManagedScheduledExecutorService.scheduleAtFixedRate() resulted in " + virtualThreads.size()
                + " out of 3 tasks were run on virtual threads.");
        });
    }

    public void testPlatformThreadFactory() throws Exception {
        ManagedThreadFactory platfromThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoPlatform");
        ManagedThreadFactory platfromThreadFactoryDD = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryDDPlatform");
        
        assertNotNull(platfromThreadFactoryAnno);
        assertNotNull(platfromThreadFactoryDD);
        
        Thread annoThread = platfromThreadFactoryAnno.newThread(NOOP_RUNNABLE);
        Thread ddThread = platfromThreadFactoryDD.newThread(NOOP_RUNNABLE);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        assertFalse(isVirtual(annoThread),
                "Thread Factory should not have returned a virtual thread when defined with virtual=false");
        assertFalse(isVirtual(ddThread),
                "Thread Factory should not have returned a virtual thread when defined with virtual=false");
    }

    public void testVirtualThreadFactory() throws Exception {
        ManagedThreadFactory virtualThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoVirtual");
        ManagedThreadFactory virtualThreadFactoryDD = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryDDVirtual");
        
        assertNotNull(virtualThreadFactoryAnno);
        assertNotNull(virtualThreadFactoryDD);
        
        Thread annoThread = virtualThreadFactoryAnno.newThread(NOOP_RUNNABLE);
        Thread ddThread = virtualThreadFactoryDD.newThread(NOOP_RUNNABLE);
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(annoThread), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(ddThread), "Should be impossible to get a virtual thread on Java 17");
            return;
        }

        // Java 21+
        assumingThat(isVirtual(annoThread), () -> {
            LinkedBlockingQueue<Object> results;
            Object result;

            // Test virtual thread from Annotation
            results = new LinkedBlockingQueue<Object>();
            Thread thread1 = virtualThreadFactoryAnno
                    .newThread(new LookupAction(results, "java:app/concurrent/ThreadFactoryAnnoVirtual"));
            thread1.start();
            result = results.poll(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
            assertNotNull(result);
            if (result instanceof Throwable)
                throw new AssertionError("An error occured on thread.", (Throwable) result);

        });
        
       assumingThat(isVirtual(ddThread), () -> {
           LinkedBlockingQueue<Object> results;
           Object result;
            
           // Test virtual thread from Deployment Descriptor
           results = new LinkedBlockingQueue<Object>();
           Thread thread2 = virtualThreadFactoryDD
                   .newThread(new LookupAction(results, "java:app/concurrent/ThreadFactoryDDVirtual"));
           thread2.start();
           result = results.poll(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
           assertNotNull(result);
           if (result instanceof Throwable)
               throw new AssertionError("An error occured on thread.", (Throwable) result);
        });
        
    }
    
    public void testVirtualThreadFactoryForkJoinPool() throws Exception {
        ManagedThreadFactory virtualThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoVirtual");
        ManagedThreadFactory platformThreadFactoryAnno = InitialContext
                .doLookup("java:app/concurrent/ThreadFactoryAnnoPlatform");
        
        assertNotNull(virtualThreadFactoryAnno);
        assertNotNull(platformThreadFactoryAnno);
        
        //Test virtual thread factory
        Thread thread1;
        ForkJoinPool virtualPool = new ForkJoinPool(3, virtualThreadFactoryAnno, null, false);
        
        try {
            thread1 = virtualPool.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            virtualPool.shutdown();
        }
        
        //Test platform thread factory
        Thread thread2;
        ForkJoinPool platformPool = new ForkJoinPool(3, platformThreadFactoryAnno, null, false);
        
        try {
            thread2 = platformPool.submit(Thread::currentThread).get(TestConstants.waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            platformPool.shutdown();
        }
        
        if (VERSION == 17) { //TODO remove when Concurrency API supports only 21+
            assertThrows(NoSuchMethodException.class, () -> isVirtual(thread1), "Should be impossible to get a virtual thread on Java 17");
            assertThrows(NoSuchMethodException.class, () -> isVirtual(thread2), "Should be impossible to get a virtual thread on Java 17");
            return;
        }
        
        // Java 21+
        assertFalse(isVirtual(thread1), "Should never get a virtual thread from a ForkJoinPool");
        assertFalse(isVirtual(thread2), "Should never get a virtual thread from a ForkJoinPool");
    }

    /**
     * Uses reflection to call method isVirtual on on the supplied thread.
     *
     * @param thread - the thread being tested
     * @return
     *  true, if the thread is virtual
     *  false, if the thread is not virtual
     *
     * @throws NoSuchMethodException when run on Java 17
     *
     * @throws RuntimeException if a reflection exception occurs
     */
    private static boolean isVirtual(final Thread thread) throws NoSuchMethodException {
            Method isVirtual = Thread.class.getMethod("isVirtual");
            isVirtual.setAccessible(true);
            
            try {
                return (boolean) isVirtual.invoke(thread);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException("Could not invoke isVirtual on thread: " + thread.getName(), e);
            }
    }

    /**
     * A simple lookup action that can pass/fail on a virtual thread
     */
    public class LookupAction implements Runnable, Callable<Object> {

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
     * Captures thread this actions is running on in addition to performing the simple
     * {@link LookupAction}
     */
    public class LookupActionCaptureThread extends LookupAction {

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
