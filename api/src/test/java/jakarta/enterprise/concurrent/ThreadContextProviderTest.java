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

package jakarta.enterprise.concurrent;

import static org.junit.Assert.*;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * Tests of the ThreadContextProvider examples from the specification and JavaDoc.
 */
@ManagedExecutorDefinition( // from JavaDoc
        name = "java:module/concurrent/MyCustomContextExecutor",
        maxAsync = 3,
        context = @ContextServiceDefinition(
                  name = "java:module/concurrent/MyCustomContext",
                  propagated = ThreadContextProviderTest.CONTEXT_NAME,
                  cleared = { ContextServiceDefinition.SECURITY, ContextServiceDefinition.TRANSACTION },
                  unchanged = ContextServiceDefinition.ALL_REMAINING))
@ManagedExecutorDefinition( // from spec ThreadContextProvider example
        name = "java:module/concurrent/PriorityExec",
        context = @ContextServiceDefinition(
                  name = "java:module/concurrent/PriorityContext",
                  propagated = "ThreadPriority"))
public class ThreadContextProviderTest {
    public static final String CONTEXT_NAME = "MyCustomContext";

    @Resource(lookup = "java:module/concurrent/PriorityExec")
    ManagedExecutorService executor;

    // To confirm compilation of spec example only:
    protected void doGet() {

        Thread.currentThread().setPriority(3);

        executor.runAsync(() -> {
            System.out.println("Running with priority of " +
                Thread.currentThread().getPriority());
        });
    }

    @Test
    public void testCaptureCurrentThreadContext() throws Exception {
        ThreadContextProvider provider = new ThreadPriorityContextProvider();
       
        Thread.currentThread().setPriority(2);
        ThreadContextSnapshot snapshot = provider.currentContext(Collections.emptyMap());

        Thread.currentThread().setPriority(3);

        ThreadContextRestorer restorer = snapshot.begin();
        assertEquals(2, Thread.currentThread().getPriority());
        restorer.endContext();

        assertEquals(3, Thread.currentThread().getPriority());
    }

    @Test
    public void testClearThreadContext() throws Exception {
        ThreadContextProvider provider = new ThreadPriorityContextProvider();
       
        Thread.currentThread().setPriority(8);
        ThreadContextSnapshot snapshot = provider.clearedContext(Collections.emptyMap());

        ThreadContextRestorer restorer = snapshot.begin();
        assertEquals(Thread.NORM_PRIORITY, Thread.currentThread().getPriority());
        restorer.endContext();

        assertEquals(8, Thread.currentThread().getPriority());
    }

    @Test
    public void testSerializeThreadContext() throws Exception {
        ThreadContextProvider provider = new ThreadPriorityContextProvider();
       
        Thread.currentThread().setPriority(7);
        ThreadContextSnapshot snapshot = provider.currentContext(Collections.emptyMap());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(snapshot);
        byte[] bytes = bout.toByteArray();
        out.close();

        Thread.currentThread().setPriority(6);

        snapshot = provider.deserialize(bytes);
        ThreadContextRestorer restorer = snapshot.begin();
        assertEquals(7, Thread.currentThread().getPriority());
        restorer.endContext();

        assertEquals(6, Thread.currentThread().getPriority());
    }

    /**
     * Validate the example that is used in the specification ThreadContextProvider example.
     */
    @Test
    public void testSpecThreadContextProviderExample() throws Exception {
        ManagedExecutorDefinition def = null;
        for (ManagedExecutorDefinition anno : getClass().getAnnotationsByType(ManagedExecutorDefinition.class))
            if ("java:module/concurrent/PriorityExec".equals(anno.name()))
                def = anno;
        assertNotNull(def);
        assertEquals(-1, def.hungTaskThreshold());
        assertEquals(-1, def.maxAsync());
        ContextServiceDefinition csd = def.context();
        assertEquals("java:module/concurrent/PriorityContext", csd.name());
        assertArrayEquals(new String[] { "ThreadPriority" }, csd.propagated());
        assertArrayEquals(new String[] { TRANSACTION }, csd.cleared());
        assertArrayEquals(new String[] {}, csd.unchanged());
    }
}