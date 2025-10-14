/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.injected;

import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;

/**
 * @ContextServiceDefinitions are defined under
 *                            {@link ContextServiceDefinitionServlet}
 */
@ManagedThreadFactoryDefinition(name = "java:app/concurrent/EJBThreadFactoryInjA", context = "java:app/concurrent/EJBContextA", priority = 4)
@ManagedThreadFactoryDefinition(name = "java:comp/concurrent/EJBThreadFactoryInjB")
@Stateless
public class ManagedThreadFactoryDefinitionInjectedBean {
}
