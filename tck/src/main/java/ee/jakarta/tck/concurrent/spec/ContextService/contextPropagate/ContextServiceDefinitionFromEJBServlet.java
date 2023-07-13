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
package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

@WebServlet("/ContextServiceDefinitionFromEJBServlet")
public class ContextServiceDefinitionFromEJBServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    /**
     * Java SE thread pool with threads that lack context.
     */
    private ExecutorService unmanagedThreads;

    @Resource
    private UserTransaction tx;

    @EJB
    private ContextServiceDefinitionInterface contextServiceDefinitionBean;

    @Override
    public void destroy() {
        unmanagedThreads.shutdownNow();
    }

    @Override
    public void before() throws ServletException {
        unmanagedThreads = Executors.newFixedThreadPool(10);
    }

    /**
     * A ContextServiceDefinition defined in an EJB with all attributes configured
     * propagates/clears/ignores context types as configured. ContextA, which is
     * tested here, propagates Application context and IntContext, clears
     * StringContext, and leaves Transaction context unchanged.
     */
    public void testContextServiceDefinitionFromEJBAllAttributes() throws Throwable {
        ContextService contextServiceA = InitialContext.doLookup("java:app/concurrent/EJBContextA");

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
            StringContext.set("testContextServiceDefinitionFromEJBAllAttributes-1");
            IntContext.set(101);

            BiFunction<Object[], String, Object[]> contextualFunction = contextServiceA.contextualFunction(fn);

            future = CompletableFuture.completedFuture(new Object[4]).thenCombineAsync(
                    CompletableFuture.completedFuture("java:app/concurrent/EJBContextA"), contextualFunction,
                    unmanagedThreads);

            // change context of the current thread
            StringContext.set("testContextServiceDefinitionFromEJBAllAttributes-2");
            IntContext.set(102);
            tx.begin();

            // run inline
            Object[] results = contextualFunction.apply(new Object[4], "java:app/concurrent/EJBContextA");
            if (results[0] instanceof Throwable)
                throw new AssertionError("Application context must be propagated to inline contextual BiFunction "
                        + "to perform lookup of java:app/concurrent/EJBContextA").initCause((Throwable) results[0]);
            assertTrue(results[0] instanceof ContextService,
                    "Application context must be propagated to inline contextual BiFunction "
                            + "per java:app/concurrent/EJBContextA configuration.");
            assertEquals(results[1], Integer.valueOf(101),
                    "Third-party context type IntContext must be propagated to inline contextual BiFunction "
                            + "per java:app/concurrent/EJBContextA configuration.");
            assertEquals(results[2], "",
                    "Third-party context type StringContext must be cleared from inline contextual BiFunction "
                            + "per java:app/concurrent/EJBContextA configuration.");
            assertEquals(results[3], Integer.valueOf(Status.STATUS_ACTIVE),
                    "Transaction context must be left unchanged on inline contextual BiFunction "
                            + "per java:app/concurrent/EJBContextA configuration.");

            // context from before the inline contextual BiFunction must be restored to
            // thread
            assertNotNull(InitialContext.doLookup("java:app/concurrent/EJBContextA"),
                    "Previous Application context must be present after inline contextual BiFunction.");
            assertEquals(IntContext.get(), 102,
                    "Third-party context type IntContext must be restored after inline contextual BiFunction.");
            assertEquals(StringContext.get(), "testContextServiceDefinitionFromEJBAllAttributes-2",
                    "Third-party context type StringContext must be restored after inline contextual BiFunction.");
            assertEquals(tx.getStatus(), Status.STATUS_ACTIVE,
                    "Transaction context must remain on thread after inline contextual BiFunction "
                            + "because it is to be left unchanged per java:app/concurrent/EJBContextA configuration.");
        } finally {
            StringContext.set("");
            IntContext.set(0);
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                tx.rollback();
        }

        Object[] results = future.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);

        if (results[0] instanceof Throwable)
            throw new AssertionError("Application context must be propagated to async contextual BiFunction "
                    + "to perform lookup of java:app/concurrent/EJBContextA").initCause((Throwable) results[0]);
        assertTrue(results[0] instanceof ContextService,
                "Application context must be propagated to async contextual BiFunction "
                        + "per java:app/concurrent/EJBContextA configuration.");
        assertEquals(results[1], Integer.valueOf(101),
                "Third-party context type IntContext must be propagated to async contextual BiFunction "
                        + "per java:app/concurrent/EJBContextA configuration.");
        assertEquals(results[2], "",
                "Third-party context type StringContext must be cleared from async contextual BiFunction "
                        + "per java:app/concurrent/EJBContextA configuration.");
        assertEquals(results[3], Integer.valueOf(Status.STATUS_NO_TRANSACTION),
                "Transaction context must be left unchanged on async contextual BiFunction "
                        + "per java:app/concurrent/EJBContextA configuration.");
    }

    /**
     * A ContextServiceDefinition defined in an EJB with minimal attributes
     * configured clears transaction context and propagates other types.
     */
    public void testContextServiceDefinitionFromEJBDefaults() throws Throwable {
        ContextService contextService = contextServiceDefinitionBean.getContextC();

        LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
        try {
            IntContext.set(13);
            new Thread(contextService.contextualRunnable(() -> {
                results.add(IntContext.get());
                try {
                    results.add(contextServiceDefinitionBean.getContextC());
                } catch (Throwable x) {
                    results.add(x);
                }
            })).start();
        } finally {
            IntContext.set(0);
        }

        tx.begin();
        try {
            StringContext.set("testContextServiceDefinitionFromeEJBDefaults-1");

            Callable<String> callable = contextService.contextualCallable(() -> {
                // Transaction context is cleared by default, so we must be
                // able to start another transaction inline:
                UserTransaction tran = InitialContext.doLookup("java:comp/UserTransaction");
                tran.begin();
                tran.commit();
                return StringContext.get();
            });

            StringContext.set("testContextServiceDefinitionFromeEJBDefaults-2");

            assertEquals(callable.call(), "testContextServiceDefinitionFromeEJBDefaults-1",
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
                "Application context must be propagated to contextual Runnable, but instead lookup found: " + result);
    }

    /**
     * A ContextService contextualizes a Supplier, which can be supplied as a
     * dependent stage action to an unmanaged CompletableFuture. The dependent stage
     * action runs with the thread context of the thread that contextualizes the
     * Supplier, per the configuration of the ContextServiceDefinition.
     */
    public void testContextualSupplier() throws Throwable {
        try {
            StringContext.set("testContextualSupplier-1");
            IntContext.set(61);

            ContextService contextServiceB = contextServiceDefinitionBean.getContextB();
            Supplier<Map.Entry<Integer, String>> supplierB = contextServiceB
                    .contextualSupplier(() -> new SimpleEntry<Integer, String>(IntContext.get(), StringContext.get()));
            CompletableFuture<Map.Entry<Integer, String>> futureB = CompletableFuture.supplyAsync(supplierB,
                    unmanagedThreads);

            StringContext.set("testContextualSupplier-2");
            IntContext.set(62);

            Map.Entry<Integer, String> results = futureB.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(results.getKey(), Integer.valueOf(0),
                    "Third-party context type IntContext must be cleared from async contextual Supplier "
                            + "per java:module/concurrent/ContextB configuration.");
            assertEquals(results.getValue(), "testContextualSupplier-1",
                    "Third-party context type StringContext must be propagated to async contextual Supplier "
                            + "per java:module/concurrent/ContextB configuration.");
        } finally {
            StringContext.set(null);
            IntContext.set(0);
        }
    }
}
