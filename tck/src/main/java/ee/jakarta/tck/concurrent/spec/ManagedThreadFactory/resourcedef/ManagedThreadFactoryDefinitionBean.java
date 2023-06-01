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
package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.resourcedef;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;

/**
 * @ContextServiceDefinitions are defined under {@link ContextServiceDefinitionServlet}
 */
@ManagedThreadFactoryDefinition(name = "java:app/concurrent/EJBThreadFactoryA",
                                context = "java:app/concurrent/EJBContextA",
                                priority = 4)
@ManagedThreadFactoryDefinition(name = "java:comp/concurrent/EJBThreadFactoryB")
@Local(ManagedThreadFactoryDefinitionInterface.class)
@Stateless
public class ManagedThreadFactoryDefinitionBean implements ManagedThreadFactoryDefinitionInterface {

	@Override
	public Object doLookup(String name) throws NamingException {
		return InitialContext.doLookup(name);
	}
}
