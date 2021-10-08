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

package jakarta.enterprise.concurrent.spec.ManagedThreadFactory.context;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;
import jakarta.enterprise.concurrent.api.common.Util;

public class ContextTests extends TestClient {

	public static final String SERVLET_OP_JNDICLASSLOADERPROPAGATIONTEST = "jndiClassloaderPropagationTest";

	public static final String SERVLET_OP_SECURITYPROPAGATIONTEST = "securityPropagationTest";

	public static final String SERVLET_OP_ATTR_NAME = "opName";

	/*
	 * @class.setup_props: webServerHost; webServerPort;
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/context_web/testServlet");
	}

	/*
	 * @testName: jndiClassloaderPropagationTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:96.7; CONCURRENCY:SPEC:100;
	 * CONCURRENCY:SPEC:106;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void jndiClassloaderPropagationTest() {

		try {
			URL url = new URL(HTTP, host, port, getURLContext());

			Properties prop = new Properties();
			prop.put(SERVLET_OP_ATTR_NAME, SERVLET_OP_JNDICLASSLOADERPROPAGATIONTEST);
			URLConnection urlConn = TestUtil.sendPostData(prop, url);
			String s = TestUtil.getResponse(urlConn);
			Util.assertEquals(Util.SERVLET_RETURN_SUCCESS, s.trim());
		} catch (Exception e) {
			fail(e);
		}
	}

}
