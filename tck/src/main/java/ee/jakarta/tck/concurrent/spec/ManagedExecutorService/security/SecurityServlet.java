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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Future;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/SecurityServlet")
public class SecurityServlet extends TestServlet {

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    public ManagedExecutorService executor;

    public void managedExecutorServiceAPISecurityTest(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        req.login("javajoe", "javajoe");
        Future<?> future = executor.submit(new SecurityTestTask());
        Object result = Wait.waitForTaskComplete(future);
        assertEquals(result, TestConstants.simpleReturnValue);
    }

}
