/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

@WebServlet("/ManagedScheduledExecutorDefinitionOnEJBServlet")
public class ManagedScheduledExecutorDefinitionOnEJBServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Inject
    ReqBean reqBean;

    @Resource
    UserTransaction tx;

    @EJB
    private ManagedScheduleExecutorDefinitionInterface managedScheduleExecutorDefinitionBean;

    // Needed to initialize the ContextServiceDefinitions
    @EJB
    private ContextServiceDefinitionInterface contextServiceDefinitionBean;

    /**
     * A ManagedScheduledExecutorDefinition defined on an EJB with all attributes
     * configured enforces maxAsync and propagates context.
     */
    public void testManagedScheduledExecutorDefinitionAllAttributes_EJB() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/EJBScheduledExecutorA");

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
     * A ManagedScheduledExecutorDefinition defined on an EJB with minimal
     * attributes can run multiple async tasks concurrently and uses
     * java:comp/DefaultContextService to determine context propagation and
     * clearing.
     */
    public void testManagedScheduledExecutorDefinitionDefaults_EJB() throws Throwable {
        ManagedScheduledExecutorService executor = (ManagedScheduledExecutorService) managedScheduleExecutorDefinitionBean
                .doLookup("java:comp/concurrent/EJBScheduledExecutorC");

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
                return (ManagedScheduledExecutorService) managedScheduleExecutorDefinitionBean.doLookup(jndiName);
            } catch (InterruptedException | NamingException x) {
                throw new CompletionException(x);
            }
        };

        try {
            Future<Integer> txFuture1 = executor.submit(txCallable);

            Future<Integer> txFuture2 = executor.submit(txCallable);

            CompletableFuture<?> lookupFuture1 = executor.completedFuture("java:comp/concurrent/EJBScheduledExecutorC")
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
                            + "per java:comp/concurrent/EJBScheduledExecutorC configuration.");

            status = txFuture2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(status, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from second async Callable task "
                            + "per java:comp/concurrent/EJBScheduledExecutorC configuration.");

            assertTrue(lookupFuture1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS) instanceof ManagedScheduledExecutorService,
                    "Application context must be propagated to first async Function "
                            + "per java:comp/concurrent/EJBScheduledExecutorC configuration.");

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
                            + "per java:comp/concurrent/EJBScheduledExecutorC configuration.");
        } finally {
            tx.rollback();
        }
    }

    /**
     * ManagedScheduledExecutorService creates an incomplete CompletableFuture to
     * which dependent stages can be chained. The CompletableFuture can be completed
     * from another thread lacking the same context, but the dependent stages all
     * run with the thread context of the thread from which they were created, per
     * ManagedScheduledExecutorDefinition config.
     */
    public void testIncompleteFutureMSE_EJB() throws Throwable {
        ManagedScheduledExecutorService executor = InitialContext.doLookup("java:app/concurrent/EJBScheduledExecutorA");

        StringBuilder results = new StringBuilder();
        try {
            IntContext.set(91);
            StringContext.set("testIncompleteFutureMSE_EJB-1");

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
            stage1b.complete("concurrent/EJBScheduledExecutorA");

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
}
