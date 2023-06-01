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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;

/**
 * ContextServiceDefinitions are defined under
 * {@link ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionBean}
 */
@ManagedExecutorDefinition(name = "java:app/concurrent/EJBExecutorA",
                           context = "java:app/concurrent/EJBContextA",
                           maxAsync = 2,
                           hungTaskThreshold = 300000)
@ManagedExecutorDefinition(name = "java:comp/concurrent/EJBExecutorC")
@Local(ManagedExecutorDefinitionInterface.class)
@Stateless
public class ManagedExecutorDefinitionWebBean implements ManagedExecutorDefinitionInterface {
	
	@Override
	public Object doLookup(String name) throws NamingException {
		return InitialContext.doLookup(name);
	}
}
