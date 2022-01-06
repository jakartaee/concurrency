/*
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.resourcedef;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import ee.jakarta.tck.concurrent.spec.ManagedExecutorService.asynchronous.AppBean;
import ee.jakarta.tck.concurrent.spec.context.IntContext;
import ee.jakarta.tck.concurrent.spec.context.StringContext;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
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

@ManagedExecutorDefinition(name = "java:app/concurrent/ExecutorA",
                           context = "java:app/concurrent/ContextA",
                           maxAsync = 2,
                           hungTaskThreshold = 300000)
@ManagedExecutorDefinition(name = "java:module/concurrent/ExecutorB",
                           context = "java:module/concurrent/ContextB",
                           maxAsync = 1)
@ManagedExecutorDefinition(name = "java:comp/concurrent/ExecutorC")
@WebServlet("/ManagedExecutorDefinitionServlet")
public class ManagedExecutorDefinitionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);
    private static final String SUCCESS = "success";

    @Inject AppBean appBean;

    @Resource
    UserTransaction tx;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        System.out.println("STARTING " + getClass().getName() + "." + action);
        try {
            String result;

            if ("testAsyncCompletionStage".equals(action))
                result = testAsyncCompletionStage();
            else if ("testAsynchronousMethodReturnsCompletableFuture".equals(action))
                result = testAsynchronousMethodReturnsCompletableFuture();
            else if ("testAsynchronousMethodReturnsCompletionStage".equals(action))
                result = testAsynchronousMethodReturnsCompletionStage();
            else if ("testAsynchronousMethodVoidReturnType".equals(action))
                result = testAsynchronousMethodVoidReturnType();
            else if ("testCompletedFuture".equals(action))
                result = testCompletedFuture();
            else if ("testCopyCompletableFuture".equals(action))
                result = testCopyCompletableFuture();
            else if ("testIncompleteFuture".equals(action))
                result = testIncompleteFuture();
            else if ("testManagedExecutorDefinitionAllAttributes".equals(action))
                result = testManagedExecutorDefinitionAllAttributes();
            else if ("testManagedExecutorDefinitionDefaults".equals(action))
                result = testManagedExecutorDefinitionDefaults();
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
     * ManagedExecutorService submits an action to run asynchronously as a CompletionStage.
     * Dependent stages can be chained to the CompletionStage, and all stages run with the
     * thread context of the thread from which they were created, per
     * ManagedExecutorDefinition config.
     */
    private String testAsyncCompletionStage() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorA");

        try {
            IntContext.set(10);
            StringContext.set("testAsyncCompletionStage-1");

            CompletableFuture<String> future = executor.supplyAsync(() -> {
                try {
                    ManagedExecutorService mes = InitialContext.doLookup("java:app/concurrent/ExecutorA");
                    return "Application context " + (mes == null ? "incorrect" : "propagated");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }
            }).thenApplyAsync(status -> {
                int i = IntContext.get();
                return status + ";IntContext " + (i == 10 ? "propagated" : "incorrect:" + i); 
            }).thenCombine(CompletableFuture.completedFuture(";"), (status, sep) -> {
                String s = StringContext.get();
                return status + sep + "StringContext " + ("".equals(s) ? "cleared" : "incorrect:" + s);
            });

            IntContext.set(25);

            String result = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals("Application context propagated;IntContext propagated;StringContext cleared", result,
                         "Application context and IntContext must be propagated and StringContext must be cleared " +
                         "per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * Asynchronous method that returns CompletableFuture runs asynchronously and can run successfully to completion
     * or be signaled to end prematurely (if so implemented) by completing its CompletableFuture.
     */
    private String testAsynchronousMethodReturnsCompletableFuture() throws Exception {
        CountDownLatch blocker = new CountDownLatch(1);
        Semaphore invocationsStarted = new Semaphore(0);

        CompletableFuture<Integer> future1;
        CompletableFuture<Integer> future2;
        CompletableFuture<Integer> future3;
        CompletableFuture<Integer> future4;
        try {
            IntContext.set(1215);
            future1 = appBean.waitAndGetIntContext(invocationsStarted, blocker);
            future2 = appBean.waitAndGetIntContext(invocationsStarted, blocker);
            future3 = appBean.waitAndGetIntContext(invocationsStarted, blocker);
            future4 = appBean.waitAndGetIntContext(invocationsStarted, blocker);

            assertEquals(true, invocationsStarted.tryAcquire(2, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run 2 asynchronous methods in parallel.");

            assertEquals(true, future1.complete(1000),
                         "Must be able to complete the CompletableFuture of an asynchronous method.");

            assertEquals(true, invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run another asynchronous method in parallel after forcibly " +
                         "completing the first.");

            assertEquals(true, future2.completeExceptionally(new CloneNotSupportedException(
                               "Not a real error. This is only testing exceptional completion.")),
                         "Must be able to complete the CompletableFuture of an asynchronous method exceptionally.");

            assertEquals(true, invocationsStarted.tryAcquire(1, MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "Must be able to run another asynchronous method in parallel after forcibly " +
                         "completing the second exceptionally.");

            assertEquals(false, invocationsStarted.tryAcquire(1, 1, TimeUnit.SECONDS),
                         "Must not be able to run another asynchronous method in parallel.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals(Integer.valueOf(1000), future1.getNow(1234),
                     "Asynchronous method's CompletableFuture must report the value with which it was " +
                     "forcibly completed.");

        try {
            Integer result = future2.join();
            throw new AssertionError("Asynchronous method's CompletableFuture must not return result " + result +
                                     "after being forcibly completed with an exception.");
        } catch (CompletionException x) {
            if (!(x.getCause() instanceof CloneNotSupportedException)) // expected due to forced exceptional completion
                throw x;
        }

        assertEquals(Integer.valueOf(1215), future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated to asynchronous method " +
                     "per ManagedExecutorDefinition and ContextServiceDefinition.");

        assertEquals(Integer.valueOf(1215), future4.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Third-party context type IntContext must be propagated to asynchronous method " +
                     "per ManagedExecutorDefinition and ContextServiceDefinition.");

        return SUCCESS;
    }

    /**
     * Asynchronous method that returns a CompletionStage runs asynchronously on the specified executor.
     */
    private String testAsynchronousMethodReturnsCompletionStage() throws Exception {
        CountDownLatch blocker = new CountDownLatch(1);
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        CompletionStage<String> stage1;
        CompletionStage<String> stage2;
        try {
            StringContext.set("testAsynchronousMethodReturnsCompletionStage-1");

            stage1 = appBean.addStringContextAndWait(queue, blocker);
            stage2 = appBean.addStringContextAndWait(queue, blocker);

            StringContext.set("testAsynchronousMethodReturnsCompletionStage-2");

            assertEquals("testAsynchronousMethodReturnsCompletionStage-1",
                         queue.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "One of the asynchronous method invocations should run per maxAsync=1.");

            assertEquals(null, queue.poll(1, TimeUnit.SECONDS),
                            "Two asynchronous method invocations should not run at same time per maxAsync=1.");

            stage1.thenAcceptBoth(stage2, (result1, result2) -> {
                if (result1.equals(result2))
                    queue.add(StringContext.get());
                else
                    queue.add("Both asynchronous method invocations must have same result. Instead: " +
                              result1 + " and " + result2);
            });
        } finally {
            StringContext.set(null);
            blocker.countDown();
        }

        assertEquals("testAsynchronousMethodReturnsCompletionStage-1", queue.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "The other asynchronous method invocation should run after the first is no longer running.");

        assertEquals("testAsynchronousMethodReturnsCompletionStage-2", queue.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "Completion stage that is created from an asynchronous method completion stage must run " +
                     "with the same executor and therefore propagate the same third-party context type StringContext.");

        return SUCCESS;
    }

    /**
     * Asynchronous method with no return type (void) runs asynchronously.
     */
    private String testAsynchronousMethodVoidReturnType() throws Exception {
        Exchanger<String> exchanger = new Exchanger<String>();
        appBean.exchange(exchanger, "RUNNING");

        String status = exchanger.exchange("WAITING", MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        assertEquals(status, "RUNNING",
                     "Asynchronous method with void return type must be able to run asynchronously. " + status);

        return SUCCESS;
    }

    /**
     * ManagedExecutorService creates a completed CompletableFuture
     * to which async dependent stages can be chained.
     * The dependent stages all run with the thread context of the thread
     * from which they were created, per ManagedExecutorDefinition config.
     */
    private String testCompletedFuture() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:module/concurrent/ExecutorB");

        IntContext.set(81);
        StringContext.set("testCompletedFuture-1");
        try {
            CompletableFuture<String> stage1 = executor.completedFuture("java:module/concurrent/ExecutorB");

            StringContext.set("testCompletedFuture-2");

            CompletableFuture<Void> stage2 = stage1.thenAcceptAsync(jndiName -> {
                try {
                    InitialContext.doLookup(jndiName);
                    throw new AssertionError("Application context must be left unchanged per " +
                                             "ManagedExecutorDefinition and ContextServiceDefinition config.");
                } catch (NamingException x) {
                    throw new CompletionException(x); // expected because Application context is unchanged
                }
            });

            StringContext.set("testCompletedFuture-3");

            CompletableFuture<String> stage3 = stage2.handleAsync((result, failure) -> {
                int i = IntContext.get();
                String s = StringContext.get();

                // CompletionException with chained NamingException is expected due to Application context
                // remaining unchanged (absent) on the async completion stage action
                if (failure instanceof CompletionException && failure.getCause() instanceof NamingException)
                    return "StringContext " + ("testCompletedFuture-3".equals(s) ? "propagated" : "incorrect:" + s) +
                           ";IntContext " + (i == 0 ? "unchanged" : "incorrect:" + i);
                else if (failure == null)
                    throw new AssertionError("Missing Throwable argument to handleAsync");
                else
                    throw new CompletionException(failure);
            });

            StringContext.set("testCompletedFuture-4");

            String result = stage3.join();
            assertEquals("StringContext propagated;IntContext unchanged", result,
                         "StringContext must be propagated and Application context and IntContext must be left " +
                         "unchanged per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * ManagedExecutorService can create a contextualized copy of an unmanaged CompletableFuture.
     */
    private String testCopyCompletableFuture() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:module/concurrent/ExecutorB");

        IntContext.set(271);
        StringContext.set("testCopyCompletableFuture-1");
        try {
            CompletableFuture<Character> stage1unmanaged = new CompletableFuture<Character>();
            CompletableFuture<Character> stage1copy = executor.copy(stage1unmanaged);
            CompletableFuture<Character> permanentlyIncompleteStage = new CompletableFuture<Character>();

            StringContext.set("testCopyCompletableFuture-2");

            CompletableFuture<String> stage2 = stage1copy.applyToEitherAsync(permanentlyIncompleteStage, sep -> {
                String s = StringContext.get();
                return "StringContext " + ("testCopyCompletableFuture-2".equals(s) ? "propagated" : "incorrect:" + s)
                                + sep;
            });

            StringContext.set("testCopyCompletableFuture-3");

            CompletableFuture<String> stage3 = stage2.handleAsync((result, failure) -> {
                if (failure == null) {
                    int i = IntContext.get();
                    return result + "IntContext " + (i == 0 ? "unchanged" : "incorrect:" + i);
                } else {
                    throw (AssertionError) new AssertionError().initCause(failure);
                }
            });

            assertTrue(stage1unmanaged.complete(';'),
                       "Completation stage that is supplied to copy must not be modified by the " +
                       "ManagedExecutorService.");

            String result = stage3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals("StringContext propagated;IntContext unchanged", result,
                         "StringContext must be propagated and Application context and IntContext must be left " +
                         "unchanged per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * ManagedExecutorService creates an incomplete CompletableFuture to which dependent stages
     * can be chained. The CompletableFuture can be completed from another thread lacking the
     * same context, but the dependent stages all run with the thread context of the thread
     * from which they were created, per ManagedExecutorDefinition config.
     */
    private String testIncompleteFuture() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorA");

        try {
            IntContext.set(181);
            StringContext.set("testIncompleteFuture-1");

            CompletableFuture<String> stage1 = executor.newIncompleteFuture();

            IntContext.set(182);

            CompletableFuture<String> stage2a = stage1.thenApplyAsync(sep -> {
                int i = IntContext.get();
                return "IntContext " + (i == 182 ? "propagated" : "incorrect:" + i) + sep;
            });

            CompletableFuture<String> stage2b = stage1.thenApply(sep -> {
                String s = StringContext.get();
                return "StringContext " + ("".equals(s) ? "cleared" : "incorrect:" + s) + sep;
            });

            IntContext.set(183);

            CompletableFuture<String> stage3 = stage2a.thenCombineAsync(stage2b, (status1, status2) -> {
                try {
                    ManagedExecutorService mes = InitialContext.doLookup("java:app/concurrent/ExecutorA");
                    return status1 + status2 + "Application context " + (mes == null ? "incorrect" : "propagated");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }                
            });

            stage1.complete(";");

            String result = stage3.join();
            assertEquals("IntContext propagated;StringContext cleared;Application context propagated", result,
                         "Application context and IntContext must be propagated and StringContext must be cleared " +
                         "per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }

        return SUCCESS;
    }

    /**
     * A ManagedExecutorDefinition with all attributes configured enforces maxAsync and propagates context.
     */
    private String testManagedExecutorDefinitionAllAttributes() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorA");

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
            IntContext.set(22);

            executor.runAsync(task);
            executor.submit(task);
            executor.execute(task);

            assertEquals(Integer.valueOf(22), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "ManagedExecutorService with maxAsync=2 must be able to run an async task.");

            assertEquals(Integer.valueOf(22), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                         "ManagedExecutorService with maxAsync=2 must be able to run 2 async tasks concurrently.");

            assertEquals(null, results.poll(1, TimeUnit.SECONDS),
                         "ManagedExecutorService with maxAsync=2 must not run 3 async tasks concurrently.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }

        assertEquals(Integer.valueOf(22), results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                     "ManagedExecutorService with maxAsync=2 must be able to run 3rd task after 1st completes.");

        return SUCCESS;
    }

    /**
     * A ManagedExecutorDefinition with minimal attributes can run multiple async tasks concurrently
     * and uses java:comp/DefaultContextService to determine context propagation and clearing.
     */
    private String testManagedExecutorDefinitionDefaults() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:comp/concurrent/ExecutorC");

        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch allTasksRunning = new CountDownLatch(3);

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
            Future<Integer> txFuture = executor.submit(txCallable);

            CompletableFuture<?> lookupFuture1 = executor.completedFuture("java:comp/concurrent/ExecutorC")
                            .thenApplyAsync(lookupFunction);

            CompletableFuture<?> lookupFuture2 = executor.completedFuture("java:module/concurrent/ExecutorB")
                            .thenApplyAsync(lookupFunction);

            assertTrue(allTasksRunning.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                       "ManagedExecutorService without maxAsync must be able to run async tasks concurrently.");

            blocker.countDown();

            int status = txFuture.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(Status.STATUS_NO_TRANSACTION, status,
                         "Transaction context must be cleared from async Callable task " +
                         "per java:comp/concurrent/ExecutorC configuration.");

            assertTrue(lookupFuture1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedExecutorService,
                       "Application context must be propagated to first async Function " +
                       "per java:comp/concurrent/ExecutorC configuration.");

            assertTrue(lookupFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedExecutorService,
                       "Application context must be propagated to second async Function " +
                       "per java:comp/concurrent/ExecutorC configuration.");
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
                         "per java:comp/concurrent/ExecutorC configuration.");
        } finally {
            tx.rollback();
        }

        return SUCCESS;
    }
}
