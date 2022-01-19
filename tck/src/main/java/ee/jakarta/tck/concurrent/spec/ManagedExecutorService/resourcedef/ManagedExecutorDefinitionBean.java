/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;

/**
 * @ManagedExecutorDefinitions are defined under {@link ContextServiceDefinitionServlet} and {@link ContextServiceDefinitionBean}
 */
@ManagedExecutorDefinition(name = "java:app/concurrent/EJBExecutorA",
                           context = "java:app/concurrent/EJBContextD",
                           maxAsync = 2,
                           hungTaskThreshold = 300000)
// Reuse the same names as defined in the web module.
@ManagedExecutorDefinition(name = "java:module/concurrent/ExecutorB",
                           context = "java:module/concurrent/ContextE",
                           maxAsync = 1)
@ManagedExecutorDefinition(name = "java:comp/concurrent/EJBExecutorC")

//TODO: Can we use context from ContextServiceDefinitionBean?
@ContextServiceDefinition(name = "java:app/concurrent/EJBContextD",
                          propagated = { APPLICATION, IntContext.NAME },
                          cleared = StringContext.NAME,
                          unchanged = TRANSACTION)
@ContextServiceDefinition(name = "java:module/concurrent/ContextE",
                        propagated = { APPLICATION, StringContext.NAME },
                        cleared = IntContext.NAME,
                        unchanged = TRANSACTION)
@Local(ManagedExecutorDefinitionInterface.class)
@Stateless
public class ManagedExecutorDefinitionBean implements ManagedExecutorDefinitionInterface {

	@Resource(lookup = "java:module/concurrent/ExecutorB", name = "java:app/env/concurrent/executorBRef")
    ManagedExecutorService executorB;
	
	@Override
	public Object doLookup(String name) throws NamingException {
		return InitialContext.doLookup(name);
	}
}
