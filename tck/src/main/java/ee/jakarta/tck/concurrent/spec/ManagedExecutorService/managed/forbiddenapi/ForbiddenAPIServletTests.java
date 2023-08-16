/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.managed.forbiddenapi;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Web
public class ForbiddenAPIServletTests {

    @Deployment(name = "ForbiddenAPIServletTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    private ManagedExecutorService mes;

    @Assertion(id = "SPEC:23 SPEC:24 SPEC:24.1", strategy = "Test basic function for ManagedExecutorService: awaitTermination")
    public void testAwaitTermination() {
        assertThrows(IllegalStateException.class, () -> {
            mes.awaitTermination(10, TimeUnit.SECONDS);
        });
    }

    @Assertion(id = "SPEC:23 SPEC:24 SPEC:24.2", strategy = "Test basic function for ManagedExecutorService: isShutdown")
    public void testIsShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            
        });
    }

    @Assertion(id = "SPEC:23 SPEC:24 SPEC:24.3", strategy = "Test basic function for ManagedExecutorService: isTerminated")
    public void testIsTerminated() {
        assertThrows(IllegalStateException.class, () -> {
            
        });
    }

    @Assertion(id = "SPEC:23 SPEC:24 SPEC:24.4", strategy = "Test basic function for ManagedExecutorService: shutdown")
   public void testShutdown() {
        assertThrows(IllegalStateException.class, () -> {
            mes.shutdown();
        });
    }

    @Assertion(id = "SPEC:23 SPEC:24 SPEC:24.5", strategy = "Test basic function for ManagedExecutorService: shutdownNow")
    public void testShutdownNow() {
        assertThrows(IllegalStateException.class, () -> {
            mes.shutdownNow();
        });
    }
}
