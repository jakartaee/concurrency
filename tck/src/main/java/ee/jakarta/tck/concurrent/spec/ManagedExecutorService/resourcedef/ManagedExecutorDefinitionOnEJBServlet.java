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
package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.resourcedef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.UserTransaction;

@WebServlet("/ManagedExecutorDefinitionOnEJBServlet")
public class ManagedExecutorDefinitionOnEJBServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Inject
    private AppBean appBean;

    @Resource
    private UserTransaction tx;

    @EJB
    private ManagedExecutorDefinitionInterface managedExecutorDefinitionBean;

    // Needed to initialize the ContextServiceDefinitions
    @EJB
    private ContextServiceDefinitionInterface contextServiceDefinitionBean;

    /**
     * ManagedExecutorService creates an incomplete CompletableFuture to which
     * dependent stages can be chained. The CompletableFuture can be completed from
     * another thread lacking the same context, but the dependent stages all run
     * with the thread context of the thread from which they were created, per
     * ManagedExecutorDefinition config.
     */
    public void testIncompleteFutureEJB() throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/EJBExecutorA");

        try {
            IntContext.set(181);
            StringContext.set("testIncompleteFutureEJB-1");

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
                    ManagedExecutorService mes = InitialContext.doLookup("java:app/concurrent/EJBExecutorA");
                    return status1 + status2 + "Application context " + (mes == null ? "incorrect" : "propagated");
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }
            });

            stage1.complete(";");

            String result = stage3.join();
            assertEquals(result, "IntContext propagated;StringContext cleared;Application context propagated",
                    "Application context and IntContext must be propagated and StringContext must be cleared "
                            + "per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }

    /**
     * ManagedExecutorService can create a contextualized copy of an unmanaged
     * CompletableFuture.
     */
    public void testCopyCompletableFutureEJB() throws Throwable {
        ManagedExecutorService executor = (ManagedExecutorService) managedExecutorDefinitionBean
                .doLookup("java:module/concurrent/ExecutorB");

        IntContext.set(271);
        StringContext.set("testCopyCompletableFutureEJB-1");

        try {
            CompletableFuture<Character> stage1unmanaged = new CompletableFuture<Character>();
            CompletableFuture<Character> stage1copy = executor.copy(stage1unmanaged);
            CompletableFuture<Character> permanentlyIncompleteStage = new CompletableFuture<Character>();

            StringContext.set("testCopyCompletableFutureEJB-2");

            CompletableFuture<String> stage2 = stage1copy.applyToEitherAsync(permanentlyIncompleteStage, sep -> {
                String s = StringContext.get();
                return "StringContext " + ("testCopyCompletableFutureEJB-2".equals(s) ? "propagated" : "incorrect:" + s)
                        + sep;
            });

            StringContext.set("testCopyCompletableFutureEJB-3");

            CompletableFuture<String> stage3 = stage2.handleAsync((result, failure) -> {
                if (failure == null) {
                    int i = IntContext.get();
                    return result + "IntContext " + (i == 0 ? "unchanged" : "incorrect:" + i);
                } else {
                    throw (AssertionError) new AssertionError().initCause(failure);
                }
            });

            assertTrue(stage1unmanaged.complete(';'),
                    "Completation stage that is supplied to copy must not be modified by the "
                            + "ManagedExecutorService.");

            String result = stage3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(result, "StringContext propagated;IntContext unchanged",
                    "StringContext must be propagated and Application context and IntContext must be left "
                            + "unchanged per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);
            ;
            StringContext.set(null);
        }
    }
}
