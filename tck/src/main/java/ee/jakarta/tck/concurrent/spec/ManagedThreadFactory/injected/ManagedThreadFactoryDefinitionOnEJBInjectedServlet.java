/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.resourcedef.ManagedThreadFactoryDefinitionInterface;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

@WebServlet("/ManagedThreadFactoryDefinitionOnEJBServlet")
public class ManagedThreadFactoryDefinitionOnEJBInjectedServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Resource
    private UserTransaction tx;

    @EJB
    private ManagedThreadFactoryDefinitionInterface managedThreadFactoryDefinitionBean;

    // Needed to initialize the ContextServiceDefinitions
    @EJB
    private ContextServiceDefinitionInterface contextServiceDefinitionBean;

    @Resource(lookup = "java:app/concurrent/EJBThreadFactoryA")
    private ManagedThreadFactory threadFactoryA;

    @Resource(lookup = "java:comp/concurrent/EJBThreadFactoryB")
    private ManagedThreadFactory threadFactoryB;

    @Override
    public void init() throws ServletException {
        try {
            managedThreadFactoryDefinitionBean.doLookup("java:module/concurrent/ContextB");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    /**
     * A ManagedThreadFactoryDefinition defined on an EJB with all attributes
     * configured enforces priority and propagates context.
     */
    public void testManagedThreadFactoryDefinitionAllAttributesEJB() throws Throwable {
        try {
            IntContext.set(161);
            StringContext.set("testManagedThreadFactoryDefinitionAllAttributesEJB-1");

            IntContext.set(162);
            StringContext.set("testManagedThreadFactoryDefinitionAllAttributesEJB-2");

            Thread thread1 = threadFactoryA.newThread(() -> {
            });
            assertEquals(thread1.getPriority(), 4, "New threads must be created with the priority that is specified on "
                    + "ManagedThreadFactoryDefinition");

            BlockingQueue<Object> results = new LinkedBlockingQueue<>();

            threadFactoryA.newThread(() -> {
                results.add(Thread.currentThread().getPriority());
                results.add(StringContext.get());
                results.add(IntContext.get());
                try {
                    results.add(InitialContext.doLookup("java:app/concurrent/ContextA"));
                } catch (Throwable x) {
                    results.add(x);
                }
            }).start();

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(4),
                    "ManagedThreadFactory must start threads with the configured priority.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "",
                    "Third-party context type StringContext must be cleared from thread "
                            + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration.");

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(161),
                    "Third-party context type IntContext must be propagated to thread "
                            + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration "
                            + "based on the thread context at the time the ManagedThreadFactory was looked up.");

            Object lookupResult = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (lookupResult instanceof Throwable)
                throw new AssertionError().initCause((Throwable) lookupResult);
            assertTrue(lookupResult instanceof ContextService, "Application context must be propagated to thread "
                    + "per ManagedThreadFactoryDefinition and ContextServiceDefinition configuration.");
        } finally {
            IntContext.set(0);
            StringContext.set("");
        }
    }

    /**
     * ManagedThreadFactoryDefinition defined on an EJB with minimal attributes
     * creates threads with normal priority and uses java:comp/DefaultContextService
     * to determine context propagation and clearing.
     */
    public void testManagedThreadFactoryDefinitionDefaultsEJB() throws Throwable {
        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch allThreadsRunning = new CountDownLatch(2);
        CompletableFuture<Object> lookupTaskResult = new CompletableFuture<>();
        CompletableFuture<Object> txTaskResult = new CompletableFuture<>();

        Runnable lookupTask = () -> {
            try {
                allThreadsRunning.countDown();
                blocker.await(MAX_WAIT_SECONDS * 5, TimeUnit.SECONDS);
                lookupTaskResult.complete(threadFactoryB);
            } catch (Throwable x) {
                lookupTaskResult.completeExceptionally(x);
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
            threadFactoryB.newThread(lookupTask).start();
            threadFactoryB.newThread(txTask).start();

            assertTrue(allThreadsRunning.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "ManagedThreadFactory threads must start running.");

            blocker.countDown();

            Object result;
            
            result = txTaskResult.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (result instanceof Throwable) {
                throw new AssertionError().initCause((Throwable) result);
            }
            assertEquals(result, Status.STATUS_NO_TRANSACTION,
                    "Transaction context must be cleared from async Callable task "
                            + "per java:comp/concurrent/EJBThreadFactoryB configuration.");

            result = lookupTaskResult.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (result instanceof Throwable)
                throw new AssertionError().initCause((Throwable) result);
            assertTrue(result instanceof ManagedThreadFactory, "Application context must be propagated to first thread "
                    + "per java:comp/concurrent/EJBThreadFactoryB configuration.");
        } finally {
            IntContext.set(0);
            blocker.countDown();
        }
    }

    /**
     * ManagedThreadFactory defined on an EJB can be supplied to a ForkJoinPool,
     * causing ForkJoinPool tasks to run with the thread context and priority as
     * configured.
     */
    public void testParallelStreamBackedByManagedThreadFactoryEJB() {
        ForkJoinPool fj = null;
        try {
            IntContext.set(1000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactoryEJB-1");

            IntContext.set(2000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactoryEJB-2");

            fj = new ForkJoinPool(4, threadFactoryA, null, false);

            IntContext.set(3000);
            StringContext.set("testParallelStreamBackedByManagedThreadFactoryEJB-3");

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
