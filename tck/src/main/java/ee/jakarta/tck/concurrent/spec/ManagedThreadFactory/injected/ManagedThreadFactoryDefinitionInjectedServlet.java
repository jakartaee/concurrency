/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.injected;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

/**
 * Same tests as in {@link ManagedThreadFactoryDefinitionServlet}, just with
 * injected {@link ManagedThreadFactory}.
 *
 * @ContextServiceDefinitions are defined under
 * *                            {@link ContextServiceDefinitionServlet}
 */
@ManagedThreadFactoryDefinition(name = "java:app/concurrent/ThreadFactoryInjA", context = "java:app/concurrent/ContextA", priority = 4)
@ManagedThreadFactoryDefinition(name = "java:comp/concurrent/ThreadFactoryInjB")
@WebServlet("/ManagedThreadFactoryDefinitionServlet")
public class ManagedThreadFactoryDefinitionInjectedServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Resource(lookup = "java:app/concurrent/ThreadFactoryInjA")
    private ManagedThreadFactory threadFactoryInjA;

    @Resource(lookup = "java:comp/concurrent/ThreadFactoryInjB")
    private ManagedThreadFactory threadFactoryInjB;

    /**
     * A ManagedThreadFactoryDefinition with all attributes configured enforces
     * priority and propagates context.
     */
    public void testManagedThreadFactoryDefinitionAllAttributes() throws Throwable {
        try {
            IntContext.set(161);
            StringContext.set("testManagedThreadFactoryDefinitionAllAttributes-1");
            IntContext.set(162);
            StringContext.set("testManagedThreadFactoryDefinitionAllAttributes-2");

            Thread thread1 = threadFactoryInjA.newThread(() -> {
            });
            assertEquals(thread1.getPriority(), 4, "New threads must be created with the priority that is specified on "
                    + "ManagedThreadFactoryDefinition");

            BlockingQueue<Object> results = new LinkedBlockingQueue<>();

            threadFactoryInjA.newThread(() -> {
                results.add(Thread.currentThread().getPriority());
                results.add(StringContext.get());
                results.add(IntContext.get());
                try {
                    results.add(InitialContext.doLookup("java:app/concurrent/ContextA"));
                } catch (Throwable x) {
                    results.add(x);
                }
            }).start();

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), 4,
                    "ManagedThreadFactory must start threads with the configured priority.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "",
                    "Third-party context type StringContext must be cleared from thread "
                            + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), 161,
                    "Third-party context type IntContext must be propagated to thread "
                            + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration "
                            + "based on the thread context at the time the ManagedThreadFactory was looked up.");

            Object lookupResult = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (lookupResult instanceof Throwable throwable) {
                throw new AssertionError().initCause(throwable);
            }
            assertTrue(lookupResult instanceof ContextService, "Application context must be propagated to thread "
                    + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration.");
        } finally {
            IntContext.set(0);
            StringContext.set("");
        }
    }

    /**
     * ManagedThreadFactoryDefinition with minimal attributes creates threads with
     * normal priority and uses java:comp/DefaultContextService to determine context
     * propagation and clearing.
     */
    public void testManagedThreadFactoryDefinitionDefaults() throws Throwable {

        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch allThreadsRunning = new CountDownLatch(2);
        CompletableFuture<Object> lookupTaskResult = new CompletableFuture<>();
        CompletableFuture<Object> txTaskResult = new CompletableFuture<>();

        Runnable lookupTask = () -> {
            try {
                allThreadsRunning.countDown();
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
                lookupTaskResult.complete(InitialContext.doLookup("java:comp/concurrent/ContextC"));
            } catch (Throwable x) {
                txTaskResult.completeExceptionally(x);
            }
        };

        Runnable txTask = () -> {
            try {
                allThreadsRunning.countDown();
                UserTransaction trans = InitialContext.doLookup("java:comp/UserTransaction");
                int initialStatus = trans.getStatus();
                trans.begin();
                try {
                    blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
                } finally {
                    trans.rollback();
                }
                txTaskResult.complete(initialStatus);
            } catch (Throwable x) {
                txTaskResult.completeExceptionally(x);
            }
        };

        try {
            threadFactoryInjB.newThread(lookupTask).start();
            threadFactoryInjB.newThread(txTask).start();

            assertTrue(allThreadsRunning.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedThreadFactory threads must start running.");

            blocker.countDown();

            Object result;
            
            result = lookupTaskResult.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (result instanceof Throwable)
                throw new AssertionError().initCause((Throwable) result);
            assertTrue(result instanceof ContextService, "Application context must be propagated to first thread "
                    + "per java:comp/concurrent/ThreadFactoryInjB configuration.");

            result = txTaskResult.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (result instanceof Throwable throwable) {
                throw new AssertionError().initCause(throwable);
            }
            assertEquals(result, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from async Callable task "
                    + "per java:comp/concurrent/ThreadFactoryInjB configuration.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }
    }

    /**
     * ManagedThreadFactory can be supplied to a ForkJoinPool, causing ForkJoinPool
     * tasks to run with the thread context and priority as configured.
     */
    public void testParallelStreamBackedByManagedThreadFactory() throws Throwable {
        ForkJoinPool fj = null;
        try {
            IntContext.set(1000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactory-1");

            IntContext.set(2000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactory-2");

            fj = new ForkJoinPool(4, threadFactoryInjA, null, false);

            IntContext.set(3000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactory-3");

            ForkJoinTask<Optional<Integer>> task = fj.submit(() -> {
                return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).parallelStream().map(num -> {
                    assertEquals(StringContext.get(), "",
                            "Third-party context type StringContext must be cleared on " + "ForkJoin thread.");
                    try {
                        assertNotNull(InitialContext.doLookup("java:app/concurrent/ContextA"),
                                "Application context must be propagated to ForkJoin thread");
                    } catch (NamingException x) {
                        throw new CompletionException(x);
                    }
                    return num * Thread.currentThread().getPriority() + IntContext.get();
                }).reduce(Integer::sum);
            });

            Optional<Integer> result = task.join();
            assertEquals(result.get(), Integer.valueOf(9180),
                    "Third-party context type IntContext must propagated to ForkJoin threads "
                            + "(thousands digit should be 9) and thread priority (4) must be enforced "
                            + "on ForkJoin threads (hundreds/tens/ones digits must be 4x5x9=180) "
                            + "per configuration of the ManagedThreadFactoryDefinition and ContextServiceDefinition.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
            if (fj != null)
                fj.shutdown();
        }
    }
}
