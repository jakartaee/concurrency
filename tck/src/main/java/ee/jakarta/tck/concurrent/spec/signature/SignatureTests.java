/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.signature;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@Common({PACKAGE.SIGNATURE})
public class SignatureTests extends TestClient {

	public static final String SIG_FILE_NAME = "jakarta.enterprise.concurrent.sig";
	public static final String SIG_MAP_NAME = "sig-test.map";
	public static final String SIG_PKG_NAME = "sig-test-pkg-list.txt";

	@ArquillianResource
	URL baseURL;

	@Deployment(name = "SignatureTests", testable = false)
	public static WebArchive createDeployment() {
		WebArchive web = ShrinkWrap.create(WebArchive.class, "signatureTest.war")
				.addPackages(true, SignatureTests.class.getPackage())
				.addAsResources(SignatureTests.class.getPackage(), SIG_MAP_NAME, SIG_PKG_NAME,
						SIG_FILE_NAME);

		return web;
	}

	@Override
	protected String getServletPath() {
		return "SignatureTestServlet";
	}

	@Test
	public void testSignatures() throws Exception {
		runTest(baseURL);
	}
}
