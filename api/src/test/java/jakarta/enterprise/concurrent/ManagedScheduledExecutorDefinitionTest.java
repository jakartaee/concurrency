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

import static jakarta.enterprise.concurrent.ContextServiceDefinition.ALL_REMAINING;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;
import static org.junit.Assert.*;

import jakarta.annotation.Resource;
import org.junit.Test;

@ManagedScheduledExecutorDefinition( // from ManagedScheduledExecutorDefinition JavaDoc
        name = "java:comp/concurrent/MyScheduledExecutor",
        context = "java:comp/concurrent/MyScheduledExecutorContext",
        hungTaskThreshold = 30000,
        maxAsync = 3)
@ContextServiceDefinition( // from ManagedScheduledExecutorDefinition JavaDoc, used by above
        name = "java:comp/concurrent/MyScheduledExecutorContext",
        propagated = APPLICATION)
@ManagedScheduledExecutorDefinition(
        name = "java:global/concurrent/ManagedScheduledExecutorDefinitionDefaults")
public class ManagedScheduledExecutorDefinitionTest {

    // from ManagedScheduledExecutorDefinition JavaDoc
    @Resource(lookup = "java:comp/concurrent/MyScheduledExecutor",
              name = "java:comp/concurrent/env/MyScheduledExecutorRef")
    ManagedScheduledExecutorService myScheduledExecutor;

    /**
     * Validate the default values for ManagedScheduledExecutorDefinition.
     */
    @Test
    public void testManagedScheduledExecutorDefinitionDefaultValues() throws Exception {
        ManagedScheduledExecutorDefinition def = null;
        for (ManagedScheduledExecutorDefinition anno : ManagedScheduledExecutorDefinitionTest.class
                .getAnnotationsByType(ManagedScheduledExecutorDefinition.class))
            if ("java:global/concurrent/ManagedScheduledExecutorDefinitionDefaults".equals(anno.name()))
                def = anno;
        assertNotNull(def);
        assertEquals(-1, def.hungTaskThreshold());
        assertEquals(-1, def.maxAsync());
        assertEquals("java:comp/DefaultContextService", def.context());
    }

    /**
     * Validate the example that is used in ManagedScheduledExecutorDefinition JavaDoc.
     */
    @Test
    public void testManagedScheduledExecutorDefinitionJavaDocExample() throws Exception {
        ManagedScheduledExecutorDefinition def = null;
        for (ManagedScheduledExecutorDefinition anno : ManagedScheduledExecutorDefinitionTest.class
                .getAnnotationsByType(ManagedScheduledExecutorDefinition.class))
            if ("java:comp/concurrent/MyScheduledExecutor".equals(anno.name()))
                def = anno;
        assertNotNull(def);
        assertEquals(30000, def.hungTaskThreshold());
        assertEquals(3, def.maxAsync());
        assertEquals("java:comp/concurrent/MyScheduledExecutorContext", def.context());
    }
}
