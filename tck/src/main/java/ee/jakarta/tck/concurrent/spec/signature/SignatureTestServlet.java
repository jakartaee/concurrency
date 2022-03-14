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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("SignatureTestServlet")
public class SignatureTestServlet extends TestServlet {
	private static final long serialVersionUID = 1L;
	
	private static final TestLogger log = TestLogger.get(SignatureTestServlet.class);

	public void testSignatures(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		
		//Ensure that jimage directory is set.
		//This is where modules will be converted back to .class files for use in signature testing	
		assertNotNull(System.getProperty("jimage.dir"), "The system property jimage.dir must be set in order to run the Signature test.");
		
		//Ensure user to running on JDK 11, this has to be exact, since the sigtest-maven-plugin output will change depending on JDK level
		assertTrue(Integer.parseInt(System.getProperty("java.specification.version")) == 11, "The signature tests must be run on an application server using JDK 11 exactly.");
		
		//Ensure TCK users have the correct security/JDK settings to allow the plugin access to internal JDK classes
        Class intf = Class.forName("jdk.internal.vm.annotation.Contended");        
        Method[] mm = intf.getDeclaredMethods();
        
        for (Method m : mm) {
        	try {
        		m.setAccessible(true);
        	} catch (InaccessibleObjectException ioe) {
        		//This means that this application (module) does not have access to JDK internals via reflection 
        		String message = "Tried to call setAccessible on JDK internal method and received an InaccessibleObjectException from the JDK. "
        				+ "Give this application (module) access to internal messages using the following JVM properties: "
        				+ "--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED "
        				+ "--add-opens java.base/jdk.internal.vm.annotation=ALL-UNNAMED";
        		
        		fail(message);
        	} catch (SecurityException se) {
        		//This means that this application was running under a security manager that did not allow the method call
        		String message = "Tried to call setAccessible on JDK internal method and received SecurityException from the security manager. "
        				+ "Give this application permission to make this method call with the security manager using the following permissions:"
        				+ "permission java.lang.RuntimePermission \"accessClassInPackage.jdk.internal\"; "
        				+ "permission java.lang.RuntimePermission \"accessClassInPackage.jdk.internal.reflect\"; "
        				+ "permission java.lang.RuntimePermission \"accessClassInPackage.jdk.internal.vm.annotation\";";
        		fail(message);
        	}
        }


		ConcurrencySigTest sigTest = new ConcurrencySigTest();
		sigTest.signatureTest();
	}
}
