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

package jakarta.enterprise.concurrent.spec.ManagedThreadFactory.apitests;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.api.common.Util;

public class APITests extends TestClient {
	
	@ArquillianResource
	URL baseURL;

	public static final String SERVLET_OP_INTERRUPTTHREADAPITEST = "interruptThreadApiTest";

	public static final String SERVLET_OP_IMPLEMENTSMANAGEABLETHREADINTERFACETEST = "implementsManageableThreadInterfaceTest";

	public static final String SERVLET_OP_ATTR_NAME = "opName";

	/*
	 * @testName: interruptThreadApiTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:83; CONCURRENCY:SPEC:83.1;
	 * CONCURRENCY:SPEC:83.2; CONCURRENCY:SPEC:83.3; CONCURRENCY:SPEC:103;
	 * CONCURRENCY:SPEC:96.5; CONCURRENCY:SPEC:96.6; CONCURRENCY:SPEC:105;
	 * CONCURRENCY:SPEC:96; CONCURRENCY:SPEC:93; CONCURRENCY:SPEC:96.3;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void interruptThreadApiTest() {

		try {
			Properties prop = new Properties();
			prop.put(SERVLET_OP_ATTR_NAME, SERVLET_OP_INTERRUPTTHREADAPITEST);
			URLConnection urlConn = TestUtil.sendPostData(prop, baseURL);
			String s = TestUtil.getResponse(urlConn);
			Util.assertEquals(Util.SERVLET_RETURN_SUCCESS, s.trim());
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: implementsManageableThreadInterfaceTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:97;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void implementsManageableThreadInterfaceTest() {

		try {
			Properties prop = new Properties();
			prop.put(SERVLET_OP_ATTR_NAME, SERVLET_OP_IMPLEMENTSMANAGEABLETHREADINTERFACETEST);
			URLConnection urlConn = TestUtil.sendPostData(prop, baseURL);
			String s = TestUtil.getResponse(urlConn);
			Util.assertEquals(Util.SERVLET_RETURN_SUCCESS, s.trim());
		} catch (Exception e) {
			fail(e);
		}
	}

}
