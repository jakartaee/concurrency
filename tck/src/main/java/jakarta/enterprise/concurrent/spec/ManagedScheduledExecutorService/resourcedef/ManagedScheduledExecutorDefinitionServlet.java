/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.resourcedef;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Trigger;
import jakarta.enterprise.concurrent.ZonedTrigger;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.CronTrigger;
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.asynchronous.ReqBean;
import jakarta.enterprise.concurrent.spec.context.IntContext;
import jakarta.enterprise.concurrent.spec.context.StringContext;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@ManagedScheduledExecutorDefinition(name = "java:app/concurrent/ScheduledExecutorA",
                           context = "java:app/concurrent/ContextA",
                           maxAsync = 3,
                           hungTaskThreshold = 360000)
@ManagedScheduledExecutorDefinition(name = "java:module/concurrent/ScheduledExecutorB",
                           context = "java:module/concurrent/ContextB",
                           maxAsync = 4)
@ManagedScheduledExecutorDefinition(name = "java:comp/concurrent/ScheduledExecutorC")
@WebServlet("/ManagedScheduledExecutorDefinitionServlet")
public class ManagedScheduledExecutorDefinitionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);
    private static final String SUCCESS = "success";

    @Inject
    ReqBean reqBean;

    @Resource
    UserTransaction tx;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        System.out.println("STARTING " + getClass().getName() + "." + action);
        try {
            String result;

            if ("testAsyncCompletionStageMSE".equals(action))
                result = testAsyncCompletionStageMSE();
            else if ("testAsynchronousMethodRunsWithContext".equals(action))
                result = testAsynchronousMethodRunsWithContext();
            else if ("testAsynchronousMethodWithMaxAsync3".equals(action))
                result = testAsynchronousMethodWithMaxAsync3();
            else if ("testCompletedFutureMSE".equals(action))
                result = testCompletedFutureMSE();
            else if ("testIncompleteFutureMSE".equals(action))
                result = testIncompleteFutureMSE();
            else if ("testManagedScheduledExecutorDefinitionAllAttributes".equals(action))
                result = testManagedScheduledExecutorDefinitionAllAttributes();
            else if ("testManagedScheduledExecutorDefinitionDefaults".equals(action))
                result = testManagedScheduledExecutorDefinitionDefaults();
            else if ("testNotAnAsynchronousMethod".equals(action))
                result = testNotAnAsynchronousMethod();
            else if ("testScheduleWithCronTrigger".equals(action))
                result = testScheduleWithCronTrigger();
            else if ("testScheduleWithZonedTrigger".equals(action))
                result = testScheduleWithZonedTrigger();
            else
                result = "unknown or missing action for " + getClass().getName() + ": " + action;

            System.out.println((SUCCESS.equals(result) ? "PASSED" : "FAILED") +
                               getClass().getName() + "." + action + ": " + result);
            resp.getWriter().println(result);
        } catch (Throwable x) {
            System.out.print("FAILED " + getClass().getName() + "." + action + ": ");
            x.printStackTrace(System.out);
            x.printStackTrace(resp.getWriter());
        }
    }

    /**
     * ManagedScheduledExecutorService submits an action to run asynchronously as a CompletionStage.
     * Dependent stages can be chained to the CompletionStage, and all stages run with the
     * thread context of the thread from which they were created, per
     * ManagedScheduledExecutorDefinition config.
     */
    private String testAsyncCompletionStageMSE() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/ScheduledExecutorA");

        try {
            IntContext.set(100);
            StringContext.set("testAsyncCompletionStageMSE-1");

            CompletableFuture<String> future = executor.supplyAsync(() -> {
                try {
                    ManagedScheduledExecutorService mes =
                                    InitialContext.doLookup("java:app/concurrent/ScheduledExecutorA");
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
            assertEquals("Application context propagated;IntContext propagated;StringContext cleared", result,
                         "Application context and IntContext must be propagated and StringContext must be cleared " +
                         "per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * Asynchronous method runs with thread context captured from the caller.
     */
    private String testAsynchronousMethodRunsWithContext() throws Throwable {
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
        reqBean.lookUpAContextService().thenAccept(results::add);

        Object result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        if (result instanceof Throwable)
            throw new AssertionError().initCause((Throwable) result);
        assertTrue(result instanceof ContextService,
                   "Application context must be propagated to Asynchronous method " +
                   "per @ManagedScheduledExecutorDefinition config.");

        return SUCCESS;
    }

    /**
     * Asynchronous method execution is constrained by executor's maxAsync, which for ScheduledExecutorA is 3.
     */
    private String testAsynchronousMethodWithMaxAsync3() throws Exception {
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

            assertEquals(true, invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run 1 asynchronous method in parallel when maxAsync=3");

            assertEquals(true, invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run 2 asynchronous methods in parallel when maxAsync=3");

            assertEquals(true, invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run 3 asynchronous methods in parallel when maxAsync=3");

            assertEquals(false, invocationsStarted.tryAcquire(1, 1, TimeUnit.SECONDS),
                         "Must not run 4 asynchronous methods in parallel when maxAsync=3");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals("303", future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated and StringContext must be cleared " +
                     "on first asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals("303", future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated and StringContext must be cleared " +
                     "on second asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals("303", future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated and StringContext must be cleared " +
                     "on third asynchronous method invocation per the executor and ContextServiceDefinition.");

        assertEquals("303", future4.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated and StringContext must be cleared " +
                     "on fourth asynchronous method invocation per the executor and ContextServiceDefinition.");

        return SUCCESS;
    }

    /**
     * ManagedScheduledExecutorService creates a completed CompletableFuture
     * to which async dependent stages can be chained.
     * The dependent stages all run with the thread context of the thread
     * from which they were created, per ManagedScheduledExecutorDefinition config.
     */
    private String testCompletedFutureMSE() throws Throwable {
        ManagedScheduledExecutorService executor =
                        InitialContext.doLookup("java:module/concurrent/ScheduledExecutorB");

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
                throw new AssertionError("Application context must be left unchanged per " +
                                         "ManagedExecutorDefinition and ContextServiceDefinition config. " +
                                         "Instead, was able to look up " + result);
            } catch (CompletionException x) {
                if (x.getCause() instanceof NamingException)
                    ; // expected
                else
                    throw x;
            }

            IntContext.set(43);
            StringContext.set("testCompletedFutureMSE-3");

            CompletableFuture<String> stage3 = stage2.exceptionally(failure -> {
                int i = IntContext.get();
                String s = StringContext.get();

                // CompletionException with chained NamingException is expected due to Application context
                // remaining unchanged (absent) on the async completion stage action
                if (failure instanceof CompletionException && failure.getCause() instanceof NamingException)
                    return "StringContext " + ("testCompletedFutureMSE-3".equals(s) ? "propagated" : "incorrect:" + s)
                           + ";IntContext " + (i == 43 ? "unchanged" : "incorrect:" + i);
                else if (failure == null)
                    throw new AssertionError("Missing Throwable argument to exceptionally");
                else
                    throw new CompletionException(failure);
            });

            StringContext.set("testCompletedFutureMSE-4");

            String result = stage3.join();
            assertEquals("StringContext propagated;IntContext unchanged", result,
                         "StringContext must be propagated and Application context and IntContext must be left " +
                         "unchanged per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * ManagedScheduledExecutorService creates an incomplete CompletableFuture to which dependent stages
     * can be chained. The CompletableFuture can be completed from another thread lacking the
     * same context, but the dependent stages all run with the thread context of the thread
     * from which they were created, per ManagedScheduledExecutorDefinition config.
     */
    private String testIncompleteFutureMSE() throws Throwable {
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

            assertEquals(null, stage3.join(),
                         "CompletableFuture with Void return type must return null from join.");
            String result = results.toString();
            assertEquals("Application context propagated;StringContext cleared;IntContext propagated", result,
                         "Application context and IntContext must be propagated and StringContext must be cleared " +
                         "per ManagedScheduledExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * A ManagedScheduledExecutorDefinition with all attributes configured enforces maxAsync and propagates context.
     */
    private String testManagedScheduledExecutorDefinitionAllAttributes() throws Throwable {
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

            assertEquals(Integer.valueOf(33), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "ManagedScheduledExecutorService with maxAsync=3 must be able to run an async task.");

            assertEquals(Integer.valueOf(33), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "ManagedScheduledExecutorService with maxAsync=3 must be able to run 2 async tasks concurrently.");

            assertEquals(Integer.valueOf(33), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "ManagedScheduledExecutorService with maxAsync=3 must be able to run 3 async tasks concurrently.");

            assertEquals(null, results.poll(1, TimeUnit.SECONDS),
                         "ManagedScheduledExecutorService with maxAsync=3 must not run 4 async tasks concurrently.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals(Integer.valueOf(33), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "ManagedScheduledExecutorService with maxAsync=3 must be able to run 4th task after 1st completes.");

        return SUCCESS;
    }

    /**
     * A ManagedScheduledExecutorDefinition with minimal attributes can run multiple async tasks concurrently
     * and uses java:comp/DefaultContextService to determine context propagation and clearing.
     */
    private String testManagedScheduledExecutorDefinitionDefaults() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch allTasksRunning = new CountDownLatch(4);

        Callable<Integer> txCallable = () -> {
            allTasksRunning.countDown();
            UserTransaction tx = InitialContext.doLookup("java:comp/UserTransaction");
            int initialStatus = tx.getStatus();
            tx.begin();
            try {
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
            } finally {
                tx.rollback();
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
            assertEquals(Status.STATUS_NO_TRANSACTION, status,
                         "Transaction context must be cleared from first async Callable task " +
                         "per java:comp/concurrent/ScheduledExecutorC configuration.");

            status = txFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(Status.STATUS_NO_TRANSACTION, status,
                         "Transaction context must be cleared from second async Callable task " +
                         "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(lookupFuture1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to first async Function " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(lookupFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to second async Function " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        tx.begin();
        try {
            // run inline to verify that transaction context is cleared
            int status = executor.getContextService().contextualCallable(txCallable).call();
            assertEquals(Status.STATUS_NO_TRANSACTION, status,
                         "Transaction context must be cleared from inline contextual Callable " +
                         "per java:comp/concurrent/ScheduledExecutorC configuration.");
        } finally {
            tx.rollback();
        }

        return SUCCESS;
    }

    /**
     * A method that lacks the Asynchronous annotation does not run as an asynchronous method,
     * even if it returns a CompletableFuture.
     */
    private String testNotAnAsynchronousMethod() throws Throwable {
        String threadName = Thread.currentThread().getName();

        CompletableFuture<String> future = reqBean.notAsynchronous();

        assertEquals(threadName, future.join(),
                     "A method that returns CompletableFuture but is not annotated as @Asynchronous " +
                     "must run inline on the same thread.");

        return SUCCESS;
    }

    /**
     * ManagedScheduledExecutorService can schedule a task with a CronTrigger
     */
    private String testScheduleWithCronTrigger() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        ZoneId US_CENTRAL = ZoneId.of("America/Chicago");
        ZoneId US_MOUNTAIN = ZoneId.of("America/Denver");

        Trigger everyOtherSecond = new CronTrigger("*/2 * * * JAN-DEC SUN-SAT", US_CENTRAL);
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();

        ScheduledFuture<?> future = executor.schedule(() -> {
            return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
        }, everyOtherSecond);
        try {
            CronTrigger weekendsAtNoon6MonthsFromNow = new CronTrigger(US_MOUNTAIN)
                            .daysOfWeek(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                            .hours(12)
                            .months(ZonedDateTime.now(US_MOUNTAIN).plusMonths(6).getMonth());
            ScheduledFuture<?> distantFuture = executor.schedule(() -> {
                return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
            }, weekendsAtNoon6MonthsFromNow);

            // Exact number of days until execution will vary, but should be around 5 to 6 months worth of days,
            long days = distantFuture.getDelay(TimeUnit.DAYS);
            assertTrue(days > 140,
                       "Too few days (" + days+ ") until the next execution of " + weekendsAtNoon6MonthsFromNow +
                       " which is used by " + distantFuture);
            assertTrue(days < 190,
                       "Too many days (" + days+ ") until the next execution of " + weekendsAtNoon6MonthsFromNow +
                       " which is used by " + distantFuture);

            assertTrue(distantFuture.cancel(true),
                       "Must be able to cancel a repeating task before it runs: " + future);

            Object result;
            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + everyOtherSecond + " did not run: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to first execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + everyOtherSecond + " did not repeat: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to second execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + everyOtherSecond + " did not run 3 times: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to third execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertTrue(future.cancel(true),
                       "Must be able to cancel a repeating task after it executes a few times: " + future);
        } finally {
            if (!future.isDone())
                future.cancel(true);
        }

        return SUCCESS;
    }

    /**
     * ManagedScheduledExecutorService can schedule a task with a ZonedTrigger implementation
     * that uses the LastExecution methods with ZonedDateTime parameters.
     */
    private String testScheduleWithZonedTrigger() throws Exception {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC");

        ZoneId US_CENTRAL = ZoneId.of("America/Chicago");

        Map<ZonedDateTime, ZonedDateTime> startAndEndTimes = new HashMap<ZonedDateTime, ZonedDateTime>();

        Trigger monthlyOnThe15th = new ZonedTrigger() {
            final Map<Long, ZonedDateTime> schedule = new ConcurrentHashMap<Long, ZonedDateTime>();

            private void initSchedule() {
                // Use times from the past to make the test predictable
                ZonedDateTime sept15 = ZonedDateTime.of(2021, 9, 15, 8, 0, 0, 0, US_CENTRAL);
                ZonedDateTime oct15 = ZonedDateTime.of(2021, 10, 15, 8, 0, 0, 0, US_CENTRAL);
                ZonedDateTime nov15 = ZonedDateTime.of(2021, 11, 15, 8, 0, 0, 0, US_CENTRAL);
                schedule.put(0l, sept15);
                schedule.put(sept15.toEpochSecond(), oct15);
                schedule.put(oct15.toEpochSecond(), nov15);
            }

            @Override
            public ZonedDateTime getNextRunTime(LastExecution lastExecution, ZonedDateTime scheduledAt) {
                if (lastExecution == null)
                    initSchedule();
                else
                    startAndEndTimes.put(lastExecution.getRunStart(US_CENTRAL),
                                         lastExecution.getRunEnd(US_CENTRAL));
                    
                long key = lastExecution == null ? 0l : lastExecution.getScheduledStart(US_CENTRAL).toEpochSecond();
                return schedule.get(key);
            }

            @Override
            public ZoneId getZoneId() {
                return US_CENTRAL;
            }
        };
        BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();

        ScheduledFuture<?> future = executor.schedule(() -> {
            return results.add(InitialContext.doLookup("java:comp/concurrent/ScheduledExecutorC"));
        }, monthlyOnThe15th);
        try {
            Object result;
            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + monthlyOnThe15th + " did not run: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to first execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + monthlyOnThe15th + " did not repeat: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to second execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");

            assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                          "Task scheduled with " + monthlyOnThe15th + " did not run 3 times: " + future);
            assertTrue(result instanceof ManagedScheduledExecutorService,
                       "Application context must be propagated to third execution " +
                       "per java:comp/concurrent/ScheduledExecutorC configuration.");
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

        return SUCCESS;
    }
}
