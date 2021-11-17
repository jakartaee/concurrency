/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.security;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.api.common.Util;

public class SecurityTests extends TestClient {
	
	@ArquillianResource
	URL baseURL;

	/*
	 * @testName: managedScheduledExecutorServiceAPISecurityTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:4.3; CONCURRENCY:SPEC:50;
	 * 
	 * @test_Strategy: login in a servlet with username "javajoe(in role manager)",
	 * then submit a task by ManagedScheduledExecutorService in which call a ejb
	 * that requires role manager.
	 */
	@Test
	public void managedScheduledExecutorServiceAPISecurityTest() {

		try {
			URLConnection urlConn = TestUtil.sendPostData(new Properties(), baseURL);
			String s = TestUtil.getResponse(urlConn);
			Util.assertEquals(Util.SERVLET_RETURN_SUCCESS, s.trim());
		} catch (Exception e) {
			fail(e);
		}
	}

}
