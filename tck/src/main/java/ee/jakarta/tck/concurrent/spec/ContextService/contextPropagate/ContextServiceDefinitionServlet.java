/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.ALL_REMAINING;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

@ContextServiceDefinition(name = "java:app/concurrent/ContextA",
                          propagated = { APPLICATION, IntContext.NAME },
                          cleared = StringContext.NAME,
                          unchanged = TRANSACTION)
@ContextServiceDefinition(name = "java:module/concurrent/ContextB",
                          cleared = TRANSACTION,
                          unchanged = { APPLICATION, IntContext.NAME },
                          propagated = ALL_REMAINING)
@ContextServiceDefinition(name = "java:comp/concurrent/ContextC")
@WebServlet("ContextServiceDefinitionServlet")
public class ContextServiceDefinitionServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    /**
     * Java SE thread pool with threads that lack context.
     */
    private ExecutorService unmanagedThreads;

    @Resource
    UserTransaction tx;

    @Override
    public void destroy() {
        unmanagedThreads.shutdownNow();
    }

    @Override
    public void before() throws ServletException {
        unmanagedThreads = Executors.newFixedThreadPool(10);
    }

    /**
     * A ContextServiceDefinition with all attributes configured
     * propagates/clears/ignores context types as configured.
     * ContextA, which is tested here, propagates Application context and IntContext,
     * clears StringContext, and leaves Transaction context unchanged.
     */
    public void testContextServiceDefinitionAllAttributes() throws Throwable {
        ContextService contextServiceA = InitialContext.doLookup("java:app/concurrent/ContextA");

        BiFunction<Object[], String, Object[]> fn = (results, jndiName) -> {
            try {
                results[0] = InitialContext.doLookup(jndiName);
            } catch (Throwable x) {
                results[0] = x;
            }
            results[1] = IntContext.get();
            results[2] = StringContext.get();
            try {
                results[3] = tx.getStatus();
            } catch (SystemException x) {
                throw new CompletionException(x);
            }
            return results;
        };

        CompletableFuture<Object[]> future;
        try {
            StringContext.set("testContextServiceDefinitionAllAttributes-1");
            IntContext.set(101);

            BiFunction<Object[], String, Object[]> contextualFunction = contextServiceA.contextualFunction(fn);

            future = CompletableFuture.completedFuture(new Object[4]).thenCombineAsync(
                    CompletableFuture.completedFuture("java:app/concurrent/ContextA"),
                    contextualFunction,
                    unmanagedThreads);

            // change context of the current thread
            StringContext.set("testContextServiceDefinitionAllAttributes-2");
            IntContext.set(102);
            tx.begin();

            // run inline
            Object[] results = contextualFunction.apply(new Object[4], "java:app/concurrent/ContextA");
            if (results[0] instanceof Throwable)
                throw new AssertionError("Application context must be propagated to inline contextual BiFunction " +
                    "to perform lookup of java:app/concurrent/ContextA").initCause((Throwable) results[0]);
            assertTrue(results[0] instanceof ContextService,
                    "Application context must be propagated to inline contextual BiFunction " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(results[1], Integer.valueOf(101), 
                    "Third-party context type IntContext must be propagated to inline contextual BiFunction " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(results[2], "", 
                    "Third-party context type StringContext must be cleared from inline contextual BiFunction " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(results[3], Integer.valueOf(Status.STATUS_ACTIVE), 
                    "Transaction context must be left unchanged on inline contextual BiFunction " +
                    "per java:app/concurrent/ContextA configuration.");

            // context from before the inline contextual BiFunction must be restored to thread
            assertNotNull(InitialContext.doLookup("java:app/concurrent/ContextA"),
                    "Previous Application context must be present after inline contextual BiFunction.");
            assertEquals(IntContext.get(), 102, 
                    "Third-party context type IntContext must be restored after inline contextual BiFunction.");
            assertEquals(StringContext.get(), "testContextServiceDefinitionAllAttributes-2", 
                    "Third-party context type StringContext must be restored after inline contextual BiFunction.");
            assertEquals(tx.getStatus(), Status.STATUS_ACTIVE, 
                    "Transaction context must remain on thread after inline contextual BiFunction " +
                    "because it is to be left unchanged per java:app/concurrent/ContextA configuration.");
        } finally {
            StringContext.set("");
            IntContext.set(0);
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                tx.rollback();
        }

        Object[] results = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);

        if (results[0] instanceof Throwable)
            throw new AssertionError("Application context must be propagated to async contextual BiFunction " +
                "to perform lookup of java:app/concurrent/ContextA").initCause((Throwable) results[0]);
        assertTrue(results[0] instanceof ContextService,
                "Application context must be propagated to async contextual BiFunction " +
                "per java:app/concurrent/ContextA configuration.");
        assertEquals(results[1], Integer.valueOf(101), 
                "Third-party context type IntContext must be propagated to async contextual BiFunction " +
                "per java:app/concurrent/ContextA configuration.");
        assertEquals(results[2], "", 
                "Third-party context type StringContext must be cleared from async contextual BiFunction " +
                "per java:app/concurrent/ContextA configuration.");
        assertEquals(results[3], Integer.valueOf(Status.STATUS_NO_TRANSACTION), 
                "Transaction context must be left unchanged on async contextual BiFunction " +
                "per java:app/concurrent/ContextA configuration.");
    }

    /**
     * A ContextServiceDefinition with minimal attributes configured
     * clears transaction context and propagates other types.
     */
    public void testContextServiceDefinitionDefaults() throws Throwable {
        ContextService contextService = InitialContext.doLookup("java:comp/concurrent/ContextC");

        LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
        try {
            IntContext.set(13);
            new Thread(contextService.contextualRunnable(() -> {
                results.add(IntContext.get());
                try {
                    results.add(InitialContext.doLookup("java:comp/concurrent/ContextC"));
                } catch (Throwable x) {
                    results.add(x);
                }
            })).start();
        } finally {
            IntContext.set(0);
        }

        tx.begin();
        try {
            StringContext.set("testContextServiceDefinitionDefaults-1");

            Callable<String> callable = contextService.contextualCallable(() -> {
                // Transaction context is cleared by default, so we must be
                // able to start another transaction inline:
                UserTransaction tran = InitialContext.doLookup("java:comp/UserTransaction");
                tran.begin();
                tran.commit();
                return StringContext.get();
            });

            StringContext.set("testContextServiceDefinitionDefaults-2");

            assertEquals(callable.call(), "testContextServiceDefinitionDefaults-1", 
                    "Third-party context type StringContext must be propagated to contextual Callable.");

            assertEquals(tx.getStatus(), Status.STATUS_ACTIVE, 
                    "Transaction must be restored on thread after contextual proxy completes.");
        } finally {
            StringContext.set(null);
            tx.rollback();
        }

        Object result;
        assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                "Contextual runnable did not start on thread.");
        assertEquals(result, Integer.valueOf(13), 
                "Third-party context type IntContext must be propagated to contextual Runnable.");

        assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                "Contextual runnable did not complete on thread.");
        if (result instanceof Throwable)
            throw new AssertionError("Unable to look up java:comp name from contextual Runnable.")
                .initCause((Throwable) result);
        assertTrue(result instanceof ContextService,
                "Application context must be propagated to contextual Runnable, but instead lookup found: " +
                result);
    }

    /**
     * A ContextServiceDefinition can specify a third-party context type to be propagated/cleared/ignored.
     * This test uses 2 ContextServiceDefinitions:
     * ContextA with IntContext propagated and StringContext cleared.
     * ContextB with IntContext unchanged and StringContext propagated (per ALL_REMAINING).
     */
    public void testContextServiceDefinitionWithThirdPartyContext() throws Throwable {
        try {
            StringContext.set("testCSDWithThirdPartyContext-1");
            IntContext.set(31);

            ContextService contextServiceA = InitialContext.doLookup("java:app/concurrent/ContextA");
            BiConsumer<Queue<Object>, Queue<Object>> consumerA =
                            contextServiceA.contextualConsumer((intQ, strQ) -> {
                                intQ.add(IntContext.get());
                                strQ.add(StringContext.get());
                            });
            BlockingQueue<Object> queueA = new LinkedBlockingQueue<Object>();
            CompletableFuture<Queue<Object>> qFutureA = CompletableFuture.completedFuture(queueA);
            qFutureA.thenAcceptBothAsync(qFutureA, consumerA, unmanagedThreads);

            ContextService contextServiceB = InitialContext.doLookup("java:module/concurrent/ContextB");
            BiConsumer<Queue<Object>, Queue<Object>> consumerB =
                            contextServiceB.contextualConsumer((intQ, strQ) -> {
                                intQ.add(IntContext.get());
                                strQ.add(StringContext.get());
                            });
            BlockingQueue<Object> queueB = new LinkedBlockingQueue<Object>();
            CompletableFuture<Queue<Object>> qFutureB = CompletableFuture.completedFuture(queueB);
            qFutureB.thenAcceptBothAsync(qFutureB, consumerB);

            StringContext.set("testCSDWithThirdPartyContext-2");
            IntContext.set(32);

            // Run inline
            Queue<Object> results = new LinkedList<Object>();
            consumerA.accept(results, results);
            assertEquals(results.poll(), Integer.valueOf(31),
                    "Third-party context type IntContext must be propagated to inline contextual BiConsumer " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(results.poll(), "", 
                    "Third-party context type StringContext must be cleared from inline contextual BiConsumer " +
                    "per java:app/concurrent/ContextA configuration.");

            consumerB.accept(results, results);
            assertEquals(results.poll(), Integer.valueOf(32), 
                    "Third-party context type IntContext must be left unchanged on inline contextual BiConsumer " +
                    "per java:module/concurrent/ContextB configuration.");
            assertEquals(results.poll(), "testCSDWithThirdPartyContext-1", 
                    "Third-party context type StringContext must be propagated to inline contextual BiConsumer " +
                    "per java:module/concurrent/ContextB configuration.");

            // Check the thread context of async consumers
            assertEquals(queueA.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(31), 
                    "Third-party context type IntContext must be propagated to async contextual BiConsumer " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(queueA.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "", 
                    "Third-party context type StringContext must be cleared from async contextual BiConsumer " +
                    "per java:app/concurrent/ContextA configuration.");

            assertEquals(queueB.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0), 
                    "Third-party context type IntContext must be left unchanged on async contextual BiConsumer " +
                    "per java:module/concurrent/ContextB configuration.");
            assertEquals(queueB.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "testCSDWithThirdPartyContext-1", 
                    "Third-party context type StringContext must be propagated to async contextual BiConsumer " +
                    "per java:module/concurrent/ContextB configuration.");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
        }
    }

    /**
     * A ContextService contextualizes a Consumer, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Consumer, per the configuration of the ContextServiceDefinition.
     */
    public void testContextualConsumer() throws Throwable {
        ContextService contextService = InitialContext.doLookup("java:app/concurrent/ContextA");
        LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
        try {
            StringContext.set("testContextualConsumer-1");
            IntContext.set(121);

            Consumer<Queue<Object>> contextualConsumer = contextService.contextualConsumer(queue -> {
                queue.add(IntContext.get());
                queue.add(StringContext.get());
                try {
                    queue.add(InitialContext.doLookup("java:app/concurrent/ContextA"));
                } catch (Throwable x) {
                    queue.add(x);
                }
            });

            CompletableFuture.completedFuture(results).thenAcceptAsync(contextualConsumer, unmanagedThreads);
        } finally {
            StringContext.set(null);
            IntContext.set(0);
        }

        Object result;
        assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                "Contextual Consumer did not start running.");
        assertEquals(result, Integer.valueOf(121),
                "Third-party context type IntContext must be propagated to contextual Consumer " +
                "per java:app/concurrent/ContextA configuration.");
        assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                "Contextual Consumer did not continue running.");
        assertEquals(result, "", 
                "Third-party context type StringContext must be cleared on contextual Consumer " +
                "per java:app/concurrent/ContextA configuration.");
        assertNotNull(result = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                "Contextual Consumer did not complete.");
        if (result instanceof Throwable)
            throw new AssertionError("Application context must be propagated to contextual Consumer")
                .initCause((Throwable) result);
        assertTrue(result instanceof ContextService,
                "Application context must be propagated to contextual Consumer " +
                "per java:app/concurrent/ContextA configuration.");
    }

    /**
     * A ContextService contextualizes a Function, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Function, per the configuration of the ContextServiceDefinition.
     */
    public void testContextualFunction() throws Throwable {
        ContextService contextService = InitialContext.doLookup("java:module/concurrent/ContextB");
        CompletableFuture<Object[]> future;
        tx.begin();
        try {
            StringContext.set("testContextualFunction-1");
            IntContext.set(151);

            Function<String, Object[]> contextualFunction = contextService.contextualFunction(jndiName -> {
                Object[] results = new Object[4];
                try {
                    results[0] = InitialContext.doLookup(jndiName);
                } catch (NamingException x) {
                    results[0] = x;
                }
                results[1] = IntContext.get();
                results[2] = StringContext.get();
                try {
                    results[3] = tx.getStatus();
                } catch (SystemException x) {
                    throw new CompletionException(x);
                }
                return results;
            });

            future = CompletableFuture.completedFuture("java:module/concurrent/ContextB")
                                      .thenApplyAsync(contextualFunction);
        } finally {
            StringContext.set(null);
            IntContext.set(0);
            tx.rollback();
        }

        Object[] results = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        assertTrue(results[0] instanceof NamingException,
                "Application context must remain unchanged on contextual Function " +
                "per java:module/concurrent/ContextB configuration. Result: " + results[0]);
        assertEquals(results[1], Integer.valueOf(0), 
                "Third-party context type IntContext must remain unchanged on contextual Function " +
                "per java:module/concurrent/ContextB configuration.");
        assertEquals(results[2], "testContextualFunction-1",
                "Third-party context type StringContext must be propagated to contextual Function " +
                "per java:module/concurrent/ContextB configuration.");
        assertEquals(results[3], Integer.valueOf(Status.STATUS_NO_TRANSACTION), 
                "Transaction context must be cleared from contextual Function " +
                "per java:module/concurrent/ContextB configuration.");
    }

    /**
     * A ContextService contextualizes a Supplier, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Supplier, per the configuration of the ContextServiceDefinition.
     */
    public void testContextualSupplier() throws Throwable {
        try {
            StringContext.set("testContextualSupplier-1");
            IntContext.set(61);

            ContextService contextServiceA = InitialContext.doLookup("java:app/concurrent/ContextA");
            Supplier<Map.Entry<Integer, String>> supplierA = contextServiceA.contextualSupplier(() ->
                new SimpleEntry<Integer, String>(IntContext.get(), StringContext.get()));
            CompletableFuture<Map.Entry<Integer, String>> futureA =
                    CompletableFuture.supplyAsync(supplierA, unmanagedThreads);

            StringContext.set("testContextualSupplier-2");
            IntContext.set(62);

            Map.Entry<Integer, String> results = futureA.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(results.getKey(), Integer.valueOf(61), 
                    "Third-party context type IntContext must be propagated to async contextual Supplier " +
                    "per java:app/concurrent/ContextA configuration.");
            assertEquals(results.getValue(), "", 
                    "Third-party context type StringContext must be cleared from async contextual Supplier " +
                    "per java:app/concurrent/ContextA configuration.");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
        }
    }

    /**
     * ContextService can create a contextualized copy of an unmanaged CompletableFuture.
     */
    public void testCopyWithContextCapture() throws Throwable {
        ContextService contextService = InitialContext.doLookup("java:app/concurrent/ContextA");
        try {
            StringContext.set("testCopyWithContextCapture-1");
            IntContext.set(171);

            CompletableFuture<String> stage1unmanaged = new CompletableFuture<String>();
            CompletableFuture<String> stage1copy = contextService.withContextCapture(stage1unmanaged);

            IntContext.set(172);

            CompletableFuture<String> stage2 = stage1copy.exceptionally(failure -> {
                assertEquals(IntContext.get(), 172, 
                        "Third-party context type IntContext must be propagated to the .exceptionally Function " +
                        "per java:app/concurrent/ContextA configuration.");
                return "java:app/concurrent/ContextA";
            });

            IntContext.set(173);

            CompletableFuture<Void> stage3 = stage2.thenAcceptAsync(jndiName -> {
                assertEquals(StringContext.get(), "", 
                        "Third-party context type StringContext must be cleared from async contextual Consumer " +
                        "per java:app/concurrent/ContextA configuration.");
                try {
                    assertNotNull(InitialContext.doLookup("java:app/concurrent/ContextA"),
                            "Application context must be propagated to async contextual Consumer " +
                            "per java:app/concurrent/ContextA configuration.");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }
            });

            stage1unmanaged.completeExceptionally(new UnsupportedOperationException(
                    "Intentionally raised to force the .exceptionally code path"));

            assertEquals(stage3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), null, 
                    "Dependent stages should complete in response to the completion of the stage " +
                    "from which the first stage was copied.");

            assertTrue(stage1copy.isCompletedExceptionally(),
                    "Copied stage must complete the same way as the stage from which it is copied.");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
        }
    }
}
