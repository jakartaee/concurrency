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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

@ContextServiceDefinition( // from ContextServiceDefinition JavaDoc
        name = "java:app/concurrent/MyContext",
        propagated = APPLICATION,
        unchanged = TRANSACTION,
        cleared = ALL_REMAINING)
@ContextServiceDefinition(
        name = "java:comp/concurrent/ContextServiceDefinitionDefaults")
@ContextServiceDefinition( // from ALL_REMAINING JavaDoc
        name = "java:module/concurrent/SecurityContext",
        propagated = SECURITY,
        unchanged = TRANSACTION,
        cleared = ALL_REMAINING)
class ContextServiceDefinitionTest {

    // from ContextServiceDefinition JavaDoc
    @Resource(lookup = "java:app/concurrent/MyContext",
              name = "java:app/concurrent/env/MyContextRef")
    ContextService appContextSvc;

    /**
     * Validate the example that is used in the ALL_REMAINING JavaDoc.
     */
    @Test
    void testContextServiceDefinitionALL_REMAININGJavaDocExample() throws Exception {
        ContextServiceDefinition csdSecurityContext = null;
        for (ContextServiceDefinition anno : ContextServiceDefinitionTest.class.getAnnotationsByType(ContextServiceDefinition.class))
            if ("java:module/concurrent/SecurityContext".equals(anno.name()))
                csdSecurityContext = anno;
        assertNotNull(csdSecurityContext);
        assertArrayEquals(new String[] { SECURITY }, csdSecurityContext.propagated());
        assertArrayEquals(new String[] { TRANSACTION }, csdSecurityContext.unchanged());
        assertArrayEquals(new String[] { ALL_REMAINING }, csdSecurityContext.cleared());
    }

    /**
     * Validate the default values for ContextServiceDefinition.
     */
    @Test
    void testContextServiceDefinitionDefaultValues() throws Exception {
        ContextServiceDefinition csdDefaults = null;
        for (ContextServiceDefinition anno : ContextServiceDefinitionTest.class.getAnnotationsByType(ContextServiceDefinition.class))
            if ("java:comp/concurrent/ContextServiceDefinitionDefaults".equals(anno.name()))
                csdDefaults = anno;
        assertNotNull(csdDefaults);
        assertArrayEquals(new String[] { TRANSACTION }, csdDefaults.cleared());
        assertArrayEquals(new String[] {}, csdDefaults.unchanged());
        assertArrayEquals(new String[] { ALL_REMAINING }, csdDefaults.propagated());
    }

    /**
     * Validate the example that is used in ContextServiceDefinition JavaDoc.
     */
    @Test
    void testContextServiceDefinitionJavaDocExample() throws Exception {
        ContextServiceDefinition csdMyContext = null;
        for (ContextServiceDefinition anno : ContextServiceDefinitionTest.class.getAnnotationsByType(ContextServiceDefinition.class))
            if ("java:app/concurrent/MyContext".equals(anno.name()))
                csdMyContext = anno;
        assertNotNull(csdMyContext);
        assertArrayEquals(new String[] { APPLICATION }, csdMyContext.propagated());
        assertArrayEquals(new String[] { TRANSACTION }, csdMyContext.unchanged());
        assertArrayEquals(new String[] { ALL_REMAINING }, csdMyContext.cleared());
    }
}
