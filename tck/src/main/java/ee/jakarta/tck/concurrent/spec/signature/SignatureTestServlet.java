/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import static org.testng.Assert.assertNotNull;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("SignatureTestServlet")
public class SignatureTestServlet extends TestServlet {
	private static final long serialVersionUID = 1L;

	public void testSignatures(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		
		//Ensure that jimage directory is set.
		//This is where modules will be converted back to .class files for use in signature testing	
		assertNotNull(System.getProperty("jimage.dir"), "The system property jimage.dir must be set in order to run this test.");
		
		ConcurrencySigTest sigTest = new ConcurrencySigTest();
		sigTest.signatureTest();
	}
}
