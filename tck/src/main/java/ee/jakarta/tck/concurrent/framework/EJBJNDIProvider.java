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
package ee.jakarta.tck.concurrent.framework;

/**
 * A service provider to pass along EJB JNDI names from test class to servlet,
 * or tasks. This is a necessary provider since the same test packaged as an EAR
 * for full Platform, and a WAR for Web Profile will have different JNDI names
 * for their EJBs.
 */
public interface EJBJNDIProvider {
    /**
     * Provides the EJB JNDI name for the test.
     */
    public String getEJBJNDIName();
}
