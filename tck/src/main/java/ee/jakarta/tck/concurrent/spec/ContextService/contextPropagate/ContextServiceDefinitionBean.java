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
package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@ContextServiceDefinition(name = "java:app/concurrent/EJBContextA",
                          propagated = { APPLICATION, IntContext.NAME },
                          cleared = StringContext.NAME,
                          unchanged = TRANSACTION)
// Reuse the same name as defined in ContextServiceDefinitionServlet.
@ContextServiceDefinition(name = "java:module/concurrent/ContextB",
                          propagated = { APPLICATION, StringContext.NAME },
                          cleared = IntContext.NAME,
                          unchanged = TRANSACTION)
@ContextServiceDefinition(name = "java:comp/concurrent/EJBContextC")
@Local(ContextServiceDefinitionInterface.class)
@Stateless
public class ContextServiceDefinitionBean implements ContextServiceDefinitionInterface {

	/**
	 * Get java:comp/concurrent/EJBContextC from the bean.
	 */
	@Override
	public ContextService getContextC() throws NamingException {
		return InitialContext.doLookup("java:comp/concurrent/EJBContextC");
	}
	
	/**
	 * Get java:comp/concurrent/ContextB from the bean.
	 */
	@Override
	public ContextService getContextB() throws NamingException {
		return InitialContext.doLookup("java:module/concurrent/ContextB");
	}
}
