/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.resourcedef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.resourcedef.ReqBean.RETURN;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.CronTrigger;
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Trigger;
import jakarta.enterprise.concurrent.ZonedTrigger;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

/**
 * @ContextServiceDefinitions are defined under
 *                            {@link ContextServiceDefinitionServlet}
 */
@ManagedScheduledExecutorDefinition(name = "java:app/concurrent/ScheduledExecutorA", context = "java:app/concurrent/ContextA", maxAsync = 3, hungTaskThreshold = 360000)
@ManagedScheduledExecutorDefinition(name = "java:module/concurrent/ScheduledExecutorB", context = "java:module/concurrent/ContextB", maxAsync = 4)
@ManagedScheduledExecutorDefinition(name = "java:comp/concurrent/ScheduledExecutorC")
@WebServlet("/ManagedScheduledExecutorDefinitionServlet")
public class ManagedScheduledExecutorDefinitionServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Inject
    private ReqBean reqBean;

    @Resource
    private UserTransaction tx;

    /**
     * ManagedScheduledExecutorService submits an action to run asynchronously as a
     * CompletionStage. Dependent stages can be chained to the CompletionStage, and
     * all stages run with the thread context of the thread from which they were
     * created, per ManagedScheduledExecutorDefinition config.
     */
    public void testAsyncCompletionStageMSE() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/ScheduledExecutorA");

        try {
            IntContext.set(100);
            StringContext.set("testAsyncCompletionStageMSE-1");

            CompletableFuture<String> future = executor.supplyAsync(() -> {
                try {
                    ManagedScheduledExecutorService mes = InitialContext
                            .doLookup("java:app/concurrent/ScheduledExecutorA");
                    return "Application context " + (mes == null ? "incorrect" : "propagated");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }
            }).applyToEitherAsync(executor.newIncompleteFuture(), status -> {
                int i = IntContext.get();
                return status + ";IntContext " + (i == 100 ? "propagated" : "incorrect:" + i);
            }).thenApply(status -> {
                String s = StringContext.get();
                return status + ";StringContext " + ("".equals(s) ? "cleared" : "incorrect:" + s);
            });

            IntContext.set(200);

            String result = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(result, "Application context propagated;IntContext propagated;StringContext cleared",
                    "Application context and IntContext must be propagated and StringContext must be cleared "
                            + "per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }

    }

    /**
     * Asynchronous method runs with thread context captured from the caller.
     */
    public void testAsynchronousMethodRunsWithContext() throws Throwable {
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
        reqBean.lookUpAContextService().thenAccept(results::add);

        Object result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        if (result instanceof Throwable)
            throw new AssertionError().initCause((Throwable) result);
        assertTrue(result instanceof ContextService, "Application context must be propagated to Asynchronous method "
                + "per @ManagedScheduledExecutorDefinition config.");

    }

    /**
     * Asynchronous method execution is constrained by executor's maxAsync, which
     * for ScheduledExecutorA is 3.
     */
    public void testAsynchronousMethodWithMaxAsync3() throws Exception {
        Semaphore invocationsStarted = new Semaphore(0);
        CountDownLatch blocker = new CountDownLatch(1);

        CompletableFuture<String> future1;
        CompletableFuture<String> future2;
        CompletableFuture<String> future3;
        CompletableFuture<String> future4;
        try {
            StringContext.set("testAsynchronousMethodWithMaxAsync3");
            IntContext.set(303);

            future1 = reqBean.awaitAndGetThirdPartyContext(invocationsStarted, blocker);
            future2 = reqBean.awaitAndGetThirdPartyContext(invocationsStarted, blocker);
            future3 = reqBean.awaitAndGetThirdPartyContext(invocationsStarted, blocker);
            future4 = reqBean.awaitAndGetThirdPartyContext(invocationsStarted, blocker);

            assertEquals(invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS), true,
                    "Must be able to run 1 asynchronous method in parallel when maxAsync=3");

            assertEquals(invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS), true,
                    "Must be able to run 2 asynchronous methods in parallel when maxAsync=3");

            assertEquals(invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS), true,
                    "Must be able to run 3 asynchronous methods in parallel when maxAsync=3");

            assertEquals(invocationsStarted.tryAcquire(1, 1, TimeUnit.SECONDS), false,
                    "Must not run 4 asynchronous methods in parallel when maxAsync=3");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals(future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "303",
                "Third-party context type IntContext must be propagated and StringContext must be cleared "
                        + "on first asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals(future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "303",
                "Third-party context type IntContext must be propagated and StringContext must be cleared "
                        + "on second asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals(future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "303",
                "Third-party context type IntContext must be propagated and StringContext must be cleared "
                        + "on third asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals(future4.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "303",
                "Third-party context type IntContext must be propagated and StringContext must be cleared "
                        + "on fourth asynchronous method invocation per the executor and ContextServiceDefinition.");

    }

    /**
     * ManagedScheduledExecutorService creates a completed CompletableFuture to
     * which async dependent stages can be chained. The dependent stages all run
     * with the thread context of the thread from which they were created, per
     * ManagedScheduledExecutorDefinition config.
     */
    public void testCompletedFutureMSE() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:module/concurrent/ScheduledExecutorB");

        IntContext.set(41);
        StringContext.set("testCompletedFutureMSE-1");
        try {
            CompletableFuture<String> stage1a = executor.completedFuture("java:module");
            CompletableFuture<String> stage1b = executor.completedFuture("concurrent/ScheduledExecutorB");

            IntContext.set(42);
            StringContext.set("testCompletedFutureMSE-2");

            CompletableFuture<String> stage2 = stage1a.thenCombineAsync(stage1b, (part1, part2) -> {
                try {
                    return InitialContext.doLookup(part1 + '/' + part2).toString();
                } catch (NamingException x) {
                    throw new CompletionException(x); // expected because Application context is unchanged
                }
            });

            try {
                String result = stage2.join();
                throw new AssertionError("Application context must be left unchanged per "
                        + "ManagedExecutorDefinition and ContextServiceDefinition config. "
                        + "Instead, was able to look up " + result);
            } catch (CompletionException x) {
                if (x.getCause() instanceof NamingException) {
                    //expected
                } else {
                    throw x;
                }
            }

            IntContext.set(43);
            StringContext.set("testCompletedFutureMSE-3");

            CompletableFuture<String> stage3 = stage2.exceptionally(failure -> {
                int i = IntContext.get();
                String s = StringContext.get();

                // CompletionException with chained NamingException is expected due to
                // Application context
                // remaining unchanged (absent) on the async completion stage action
                if (failure instanceof CompletionException && failure.getCause() instanceof NamingException) {
                    return "StringContext " + ("testCompletedFutureMSE-3".equals(s) ? "propagated" : "incorrect:" + s)
                            + ";IntContext " + (i == 43 ? "unchanged" : "incorrect:" + i);
                } else if (failure == null) {
                    throw new AssertionError("Missing Throwable argument to exceptionally");
                } else {
                    throw new CompletionException(failure);
                }
            });

            StringContext.set("testCompletedFutureMSE-4");

            String result = stage3.join();
            assertEquals(result, "StringContext propagated;IntContext unchanged",
                    "StringContext must be propagated and Application context and IntContext must be left "
                            + "unchanged per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }

    }

    /**
     * ManagedScheduledExecutorService creates an incomplete CompletableFuture to
     * which dependent stages can be chained. The CompletableFuture can be completed
     * from another thread lacking the same context, but the dependent stages all
     * run with the thread context of the thread from which they were created, per
     * ManagedScheduledExecutorDefinition config.
     */
    public void testIncompleteFutureMSE() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/ScheduledExecutorA");

        StringBuilder results = new StringBuilder();
        try {
            IntContext.set(91);
            StringContext.set("testIncompleteFutureMSE-1");

            CompletableFuture<String> stage1a = executor.newIncompleteFuture();
            CompletableFuture<String> stage1b = executor.newIncompleteFuture();

            IntContext.set(92);

            CompletableFuture<Void> stage2 = stage1a.thenAcceptBothAsync(stage1b, (part1, part2) -> {
                try {
                    ManagedScheduledExecutorService mes = InitialContext.doLookup(part1 + '/' + part2);
                    results.append("Application context ").append(mes == null ? "incorrect" : "propagated");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }

                String s = StringContext.get();
                results.append(";StringContext ").append("".equals(s) ? "cleared" : "incorrect:" + s);
            });

            IntContext.set(93);

            CompletableFuture<Void> stage3 = stage2.runAfterBothAsync(stage1b, () -> {
                int i = IntContext.get();
                results.append(";IntContext ").append(i == 93 ? "propagated" : "incorrect:" + i);
            });

            stage1a.complete("java:app");
            stage1b.complete("concurrent/ScheduledExecutorA");

            assertEquals(stage3.join(), null, "CompletableFuture with Void return type must return null from join.");
            String result = results.toString();
            assertEquals(result, "Application context propagated;StringContext cleared;IntContext propagated",
                    "Application context and IntContext must be propagated and StringContext must be cleared "
                            + "per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }

    }

    /**
     * A ManagedScheduledExecutorDefinition with all attributes configured enforces
     * maxAsync and propagates context.
     */
    public void testManagedScheduledExecutorDefinitionAllAttributes() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/ScheduledExecutorA");

        BlockingQueue<Integer> results = new LinkedBlockingQueue<Integer>();
        CountDownLatch blocker = new CountDownLatch(1);

        Runnable task = () -> {
            results.add(IntContext.get());
            try {
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
            } catch (InterruptedException x) {
                throw new CompletionException(x);
            }
        };

        try {
            IntContext.set(33);

            executor.execute(task);
            executor.runAsync(task);
            executor.submit(task);
            executor.submit(task, "TaskResult");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(33),
                    "ManagedScheduledExecutorService with maxAsync=3 must be able to run an async task.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(33),
                    "ManagedScheduledExecutorService with maxAsync=3 must be able to run 2 async tasks concurrently.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(33),
                    "ManagedScheduledExecutorService with maxAsync=3 must be able to run 3 async tasks concurrently.");

            assertEquals(results.poll(1, TimeUnit.SECONDS), null,
                    "ManagedScheduledExecutorService with maxAsync=3 must not run 4 async tasks concurrently.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(33),
                "ManagedScheduledExecutorService with maxAsync=3 must be able to run 4th task after 1st completes.");

    }

    /**
     * A ManagedScheduledExecutorDefinition with minimal attributes can run multiple
     * async tasks concurrently and uses java:comp/DefaultContextService to
     * determine context propagation and clearing.
     */
    public void testManagedScheduledExecutorDefinitionDefaults() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch allTasksRunning = new CountDownLatch(4);

        Callable<Integer> txCallable = () -> {
            allTasksRunning.countDown();
            UserTransaction trans = InitialContext.doLookup("java:comp/UserTransaction");
            int initialStatus = trans.getStatus();
            trans.begin();
            try {
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
            } finally {
                trans.rollback();
            }
            return initialStatus;
        };

        Function<String, Object> lookupFunction = jndiName -> {
            allTasksRunning.countDown();
            try {
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
                return InitialContext.doLookup(jndiName);
            } catch (InterruptedException | NamingException x) {
                throw new CompletionException(x);
            }
        };

        try {
            Future<Integer> txFuture1 = executor.submit(txCallable);

            Future<Integer> txFuture2 = executor.submit(txCallable);

            CompletableFuture<?> lookupFuture1 = executor.completedFuture("java:comp/concurrent/ScheduledExecutorC")
                    .thenApplyAsync(lookupFunction);

            CompletableFuture<?> lookupFuture2 = executor.completedFuture("java:module/concurrent/ScheduledExecutorB")
                    .thenApplyAsync(lookupFunction);

            assertTrue(allTasksRunning.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService without maxAsync must be able to run async tasks concurrently.");

            blocker.countDown();

            int status;
            status = txFuture1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(status, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from first async Callable task "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            status = txFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(status, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from second async Callable task "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(lookupFuture1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to first async Function "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(lookupFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to second async Function "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        tx.begin();
        try {
            // run inline to verify that transaction context is cleared
            int status = executor.getContextService().contextualCallable(txCallable).call();
            assertEquals(status, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from inline contextual Callable "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");
        } finally {
            tx.rollback();
        }

    }

    /**
     * A method that lacks the Asynchronous annotation does not run as an
     * asynchronous method, even if it returns a CompletableFuture.
     */
    public void testNotAnAsynchronousMethod() throws Throwable {
        String threadName = Thread.currentThread().getName();

        CompletableFuture<String> future = reqBean.notAsynchronous();

        assertEquals(future.join(), threadName,
                "A method that returns CompletableFuture but is not annotated as @Asynchronous "
                        + "must run inline on the same thread.");

    }

    /**
     * ManagedScheduledExecutorService can schedule a task with a CronTrigger
     */
    public void testScheduleWithCronTrigger() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        ZoneId usCentral = ZoneId.of("America/Chicago");
        ZoneId usMountain = ZoneId.of("America/Denver");

        Trigger everyOtherSecond = new CronTrigger("*/2 * * * JAN-DEC SUN-SAT", usCentral);
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();

        ScheduledFuture<?> future = executor.schedule(() -> {
            return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
        }, everyOtherSecond);
        try {
            CronTrigger weekendsAtNoon6MonthsFromNow = new CronTrigger(usMountain)
                    .daysOfWeek(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).hours(12)
                    .months(ZonedDateTime.now(usMountain).plusMonths(6).getMonth());
            ScheduledFuture<?> distantFuture = executor.schedule(() -> {
                return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
            }, weekendsAtNoon6MonthsFromNow);

            // Exact number of days until execution will vary, but should be around 5 to 6
            // months worth of days,
            long days = distantFuture.getDelay(TimeUnit.DAYS);
            assertTrue(days > 140, "Too few days (" + days + ") until the next execution of "
                    + weekendsAtNoon6MonthsFromNow + " which is used by " + distantFuture);
            assertTrue(days < 190, "Too many days (" + days + ") until the next execution of "
                    + weekendsAtNoon6MonthsFromNow + " which is used by " + distantFuture);

            assertTrue(distantFuture.cancel(true), "Must be able to cancel a repeating task before it runs: " + future);

            Object result;
            
            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + everyOtherSecond + " did not run: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to first execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + everyOtherSecond + " did not repeat: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to second execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + everyOtherSecond + " did not run 3 times: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to third execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(future.cancel(true),
                    "Must be able to cancel a repeating task after it executes a few times: " + future);
        } finally {
            if (!future.isDone())
                future.cancel(true);
        }

    }

    /**
     * ManagedScheduledExecutorService can schedule a task with a ZonedTrigger
     * implementation that uses the LastExecution methods with ZonedDateTime
     * parameters.
     */
    public void testScheduleWithZonedTrigger() throws Exception {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        ZoneId usCentral = ZoneId.of("America/Chicago");

        Map<ZonedDateTime, ZonedDateTime> startAndEndTimes = new ConcurrentHashMap<ZonedDateTime, ZonedDateTime>();

        Trigger monthlyOnThe15th = new ZonedTrigger() {
            private final Map<Long, ZonedDateTime> schedule = new ConcurrentHashMap<Long, ZonedDateTime>();

            private void initSchedule() {
                // Use times from the past to make the test predictable
                ZonedDateTime sept15 = ZonedDateTime.of(2021, 9, 15, 8, 0, 0, 0, usCentral);
                ZonedDateTime oct15 = ZonedDateTime.of(2021, 10, 15, 8, 0, 0, 0, usCentral);
                ZonedDateTime nov15 = ZonedDateTime.of(2021, 11, 15, 8, 0, 0, 0, usCentral);
                schedule.put(0L, sept15);
                schedule.put(sept15.toEpochSecond(), oct15);
                schedule.put(oct15.toEpochSecond(), nov15);
            }

            @Override
            public ZonedDateTime getNextRunTime(final LastExecution lastExecution, final ZonedDateTime scheduledAt) {
                if (lastExecution == null) {
                    initSchedule();
                } else {
                    startAndEndTimes.put(lastExecution.getRunStart(usCentral), lastExecution.getRunEnd(usCentral));
                }

                long key = lastExecution == null ? 0L : lastExecution.getScheduledStart(usCentral).toEpochSecond();
                return schedule.get(key);
            }

            @Override
            public ZoneId getZoneId() {
                return usCentral;
            }
        };
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();

        ScheduledFuture<?> future = executor.schedule(() -> {
            return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
        }, monthlyOnThe15th);
        try {
            Object result;
            
            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + monthlyOnThe15th + " did not run: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to first execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + monthlyOnThe15th + " did not repeat: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to second execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");

            result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(result, "Task scheduled with " + monthlyOnThe15th + " did not run 3 times: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to third execution "
                            + "per java:comp/concurrent/ScheduledExecutorC configuration.");
        } finally {
            if (!future.isDone())
                future.cancel(true);
        }

        // Compare actual start/end times that were recorded from LastExecution
        for (Map.Entry<ZonedDateTime, ZonedDateTime> entry : startAndEndTimes.entrySet()) {
            ZonedDateTime startAt = entry.getKey();
            ZonedDateTime endAt = entry.getValue();
            assertTrue(startAt.isBefore(endAt) || startAt.isEqual(endAt),
                    "LastExecution runStart and runEnd methods returned inconsistent times: " + startAndEndTimes);
        }

    }
    
    public void testScheduledAsynchCompletedFuture() throws Throwable {
        AtomicInteger counter = new AtomicInteger();
        
        // Method returns an incomplete future - stopping schedule because non-null value is returned
        try {
            CompletableFuture<Integer> future = reqBean.scheduledEvery5seconds(1, RETURN.INCOMPLETE, counter);
            assertThrows(TimeoutException.class, () -> { // Slow assertion
                future.get(10, TimeUnit.SECONDS);
            });
            
            assertFalse(future.isCancelled());
            assertFalse(future.isCompletedExceptionally());
            assertFalse(future.isDone());
            assertEquals(1, counter.get(), "Schedule should have executed exactly once.");
            
            future.cancel(false); // Cleanup resources
        } finally {
            counter.set(0);
        }

        
        // Caller completes future before scheduled asynch is completed - stopping schedule because future was cancelled
        try {
            CompletableFuture<Integer> future = reqBean.scheduledEvery5seconds(1, RETURN.NULL, counter);
            
            assertFalse(future.isCancelled());
            assertFalse(future.isCompletedExceptionally());
            assertFalse(future.isDone());
            
            int countBeforeCancel = counter.get();
            future.cancel(false);
            assertThrows(CancellationException.class, () -> {
                future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            });
            int countAfterCancel = counter.get();
            
            assertTrue((countAfterCancel - countBeforeCancel) <= 1, "Schedule should not have executed more than once after cancel was called.");
        } finally {
            counter.set(0);
        }
    }
    
    
    public void testScheduledAsynchCompletedResult() throws Throwable {
        AtomicInteger counter = new AtomicInteger();
        
        // Method returns an expected result - stopping schedule because non-null value is returned
        try {
            int expected = 3;
            CompletableFuture<Integer> future = reqBean.scheduledEvery5seconds(expected, RETURN.COMPLETE_RESULT, counter);
            
            int result = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS); // Slow assertion
            
            assertFalse(future.isCancelled());
            assertFalse(future.isCompletedExceptionally());
            assertTrue(future.isDone());
            assertEquals(expected, result);
        } finally {
            counter.set(0);
        }

    }
    
    /**
     * Ensure completion of scheduled asynch after completing exceptionally
     */
    public void testScheduledAsynchCompletedExceptionally() {
        AtomicInteger counter = new AtomicInteger();
        
        // Method invokes completeExceptionally - stopping schedule because non-null value is returned
        try {
            String expected = "testScheduledAsynchCompletedExceptionally-1";
            CompletableFuture<Integer> future = reqBean.scheduledEvery5seconds(1, RETURN.COMPLETE_EXCEPTIONALLY.withMessage(expected), counter);
            
            ExecutionException cause = assertThrows(ExecutionException.class, () -> {
                future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            });
            
            assertFalse(future.isCancelled());
            assertTrue(future.isCompletedExceptionally());
            assertTrue(future.isDone());
            assertTrue(cause.getMessage().contains(expected));
            assertEquals(1, counter.get(), "Schedule should have executed exactly once.");
            
        } finally {
            counter.set(0);
        }

        // Method throws exception, platform invokes completeExceptionally - stopping schedule because future is completed
        try {
            String expected = "testScheduledAsynchCompletedExceptionally-2";
            CompletableFuture<Integer> future = reqBean.scheduledEvery5seconds(1, RETURN.THROW_EXCEPTION.withMessage(expected), counter);
            
            ExecutionException cause = assertThrows(ExecutionException.class, () -> {
                future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            });
            
            assertFalse(future.isCancelled());
            assertTrue(future.isCompletedExceptionally());
            assertTrue(future.isDone());
            assertTrue(cause.getMessage().contains(expected));
            assertEquals(1, counter.get());
        } finally {
            counter.set(0);
        }
    }
    
    
    public void testScheduledAsynchOverlapSkipping() throws Throwable {
        AtomicInteger counter = new AtomicInteger();
        
        try {
            int expected = 3;
            CompletableFuture<Integer> future = reqBean.scheduledEvery3SecondsTakes5Seconds(expected, counter);
            
            // If scheduled async tasks are not skipped while overlapping this will fail
            assertThrows(TimeoutException.class, () -> { // Slow assertion
                future.get(expected * 3, TimeUnit.SECONDS);
            });
            
            int result = future.get(expected * 5, TimeUnit.SECONDS);
            assertEquals(expected, result);
            
        } finally {
            counter.set(0);
        }
    }
    
    public void testScheduledAsynchIgnoresMaxAsync() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:module/concurrent/ScheduledExecutorB");

        BlockingQueue<Integer> results = new LinkedBlockingQueue<Integer>();
        CountDownLatch blocker = new CountDownLatch(1);

        Runnable task = () -> {
            results.add(IntContext.get());
            try {
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
            } catch (InterruptedException x) {
                throw new CompletionException(x);
            }
        };
        
        AtomicInteger counter = new AtomicInteger();

        try {
            executor.runAsync(task);
            executor.runAsync(task);
            executor.runAsync(task);
            executor.runAsync(task);
            executor.runAsync(task);

            assertEquals(Integer.valueOf(0), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService with maxAsync=4 must be able to run one async task.");
            
            assertEquals(Integer.valueOf(0), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService with maxAsync=4 must be able to run two async tasks.");
            
            assertEquals(Integer.valueOf(0), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService with maxAsync=4 must be able to run three async tasks.");
            
            assertEquals(Integer.valueOf(0), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService with maxAsync=4 must be able to run four async tasks.");
            
            assertEquals(null, results.poll(1, TimeUnit.SECONDS),
                    "ManagedScheduledExecutorService with maxAsync=4 must not run 5 async tasks concurrently.");
        } finally {
            IntContext.set(0);
            counter.set(0);
            blocker.countDown();
        }
    }
    
    public void testScheduledAsynchWithMultipleSchedules() throws Throwable {
        AtomicInteger counter = new AtomicInteger();
        
        try {
            String expected = "testScheduledAsynchWithMultipleSchedules";
            StringContext.set(expected);
            
            CompletableFuture<String> future = reqBean.scheduledEvery3SecondsAnd1Minute(5, counter);
            
            String result = future.get(1, TimeUnit.MINUTES);
            assertEquals(expected, result);
            
        } finally {
            StringContext.set(null);
            counter.set(0);
        }
    }
    
    public void testScheduledAsynchWithInvalidJNDIName() {
        assertThrows(RejectedExecutionException.class, () -> {
            reqBean.scheduledInvalidExecutor();
        });
    }
    
    public void testScheduledAsynchVoidReturn() {
        AtomicInteger counter = new AtomicInteger();
        
        // Test future.complete(null);
        try {
            int expected = 3;
            reqBean.scheduledEvery3SecondsVoidReturn(expected, RETURN.COMPLETE_RESULT, counter);
            assertTimeoutPreemptively(Duration.ofSeconds(MAX_WAIT_SECONDS), () -> {
                for (; expected != counter.get(); Wait.sleep(Duration.ofSeconds(3))) {
                    //empty
                }
            });
        } finally {
            counter.set(0);
        }
        
        // Test future.completeExceptionally();
        try {
            int expected = 3;
            reqBean.scheduledEvery3SecondsVoidReturn(expected, RETURN.COMPLETE_EXCEPTIONALLY, counter);
            assertTimeoutPreemptively(Duration.ofSeconds(MAX_WAIT_SECONDS), () -> {
                for (; expected != counter.get(); Wait.sleep(Duration.ofSeconds(3))) {
                    //empty
                }
            });
        } finally {
            counter.set(0);
        }
        
        // Test method throws exception
        try {
            int expected = 3;
            reqBean.scheduledEvery3SecondsVoidReturn(expected, RETURN.THROW_EXCEPTION, counter);
            assertTimeoutPreemptively(Duration.ofSeconds(MAX_WAIT_SECONDS), () -> {
                for (; expected != counter.get(); Wait.sleep(Duration.ofSeconds(3))) {
                    //empty
                }
            });
        } finally {
            counter.set(0);
        }
    }

}
