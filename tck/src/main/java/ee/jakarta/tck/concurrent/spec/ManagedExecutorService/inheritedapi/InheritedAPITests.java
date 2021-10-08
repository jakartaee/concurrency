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

package jakarta.enterprise.concurrent.spec.ManagedExecutorService.inheritedapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;

public class InheritedAPITests extends TestClient {

	/*
	 * @class.setup_props: webServerHost; webServerPort;
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup(String[] args, Properties p) {
		loadServerProperties();
	}

	/*
	 * @testName: testBasicManagedExecutorService
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:10.2;CONCURRENCY:SPEC:13;CONCURRENCY:SPEC:13.1;CONCURRENCY
	 * :SPEC:13.2;
	 * CONCURRENCY:SPEC:14;CONCURRENCY:SPEC:14.1;CONCURRENCY:SPEC:14.2;CONCURRENCY
	 * :SPEC:14.3;
	 * CONCURRENCY:SPEC:14.4;CONCURRENCY:SPEC:6.1;CONCURRENCY:SPEC:6.2;CONCURRENCY
	 * :SPEC:8;
	 * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:10;CONCURRENCY:
	 * SPEC:10.2; CONCURRENCY:SPEC:12;CONCURRENCY:SPEC:19;CONCURRENCY:SPEC:27;
	 * 
	 * @test_Strategy: test basic function for ManagedExecutorService include
	 * execute, submit, invokeAny, invokeAll, atMostOnce
	 */
	@Test
	public void testBasicManagedExecutorService() {
		URL url;
		String resp = null;
		try {
			url = new URL(Util.getUrl(Constants.COMMON_SERVLET_URI, host, port));
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, // expected
				resp); // actual
	}
}
