/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.api.ContextService;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.ArquillianTests;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.enterprise.concurrent.ManagedTaskListener;

public class ContextServiceTests extends ArquillianTests {

	private static final TestLogger log = TestLogger.get(ContextServiceTests.class);
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="ContextServiceTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), ContextServiceTests.class.getPackage());
	}

	/*
	 * @testName: ContextServiceWithIntf
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:5
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using instance and interface.
	 */
	@Test
	public void ContextServiceWithIntf() {
		boolean pass = false;
		try {
			Runnable proxy = (Runnable) TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), Runnable.class);
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithIntfAndIntfNoImplemented
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:6
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using instance and interface. if the instance does not implement the
	 * specified interface, IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithIntfAndIntfNoImplemented() {
		boolean pass = false;
		try {
			Object proxy = TestUtil.getContextService().createContextualProxy(new Object(), Runnable.class);
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithIntfAndInstanceIsNull
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:6
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using instance and interface. if the instance is null,
	 * IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithIntfAndInstanceIsNull() {
		boolean pass = false;
		try {
			Object proxy = TestUtil.getContextService().createContextualProxy(null, Runnable.class);
			log.info(proxy.toString());
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfs
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:7
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using instance and multiple interfaces.
	 */
	@Test
	public void ContextServiceWithMultiIntfs() {
		boolean pass = false;
		try {
			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), Runnable.class, TestWorkInterface.class);
			pass = proxy instanceof Runnable && proxy instanceof TestWorkInterface;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfsAndIntfNoImplemented
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:8
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using instance and multi interfaces. if the instance does not implement the
	 * specified interface, IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithMultiIntfsAndIntfNoImplemented() {
		boolean pass = false;
		try {
			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), Runnable.class, TestWorkInterface.class,
					ManagedTaskListener.class);
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfsAndInstanceIsNull
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:8
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using object and multi interfaces. if the instance is null,
	 * IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithMultiIntfsAndInstanceIsNull() {
		boolean pass = false;
		try {
			Object proxy = TestUtil.getContextService().createContextualProxy(null, Runnable.class, TestWorkInterface.class);
			log.info(proxy.toString());
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithIntfAndProperties
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:9
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and interface.
	 */
	@Test
	public void ContextServiceWithIntfAndProperties() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Runnable proxy = (Runnable) TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), execProps, Runnable.class);
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfsAndProperties
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:11
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and multiple interfaces.
	 */
	@Test
	public void ContextServiceWithMultiIntfsAndProperties() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), execProps, Runnable.class,
					TestWorkInterface.class);
			pass = proxy instanceof Runnable && proxy instanceof TestWorkInterface;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithIntfAndPropertiesAndIntfNoImplemented
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:10
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and interface. if the instance does not implement
	 * the specified interface, IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithIntfAndPropertiesAndIntfNoImplemented() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), execProps, Runnable.class,
					ManagedTaskListener.class);
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithIntfsAndPropertiesAndInstanceIsNull
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:10
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and interfaces. if the instance is null,
	 * IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithIntfsAndPropertiesAndInstanceIsNull() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(null, execProps, Runnable.class);
			log.info(proxy.toString());
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfsAndPropertiesAndIntfNoImplemented
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:12
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and multiple interfaces. if the instance does not
	 * implement the specified interface, IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithMultiIntfsAndPropertiesAndIntfNoImplemented() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), execProps, Runnable.class,
					TestWorkInterface.class, ManagedTaskListener.class);
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: ContextServiceWithMultiIntfsAndPropertiesAndInstanceIsNull
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:12
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and multiple interfaces. if the instance is null,
	 * IllegalArgumentException will be thrown
	 */
	@Test
	public void ContextServiceWithMultiIntfsAndPropertiesAndInstanceIsNull() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("vendor_a.security.tokenexpiration", "15000");
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(null, execProps, Runnable.class, TestWorkInterface.class);
			log.info(proxy.toString());
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: GetExecutionProperties
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:13
	 * 
	 * @test_Strategy: Lookup default ContextService object and create proxy object
	 * using ExecutionProperties and multiple interfaces. Retrieve
	 * ExecutionProperties from proxy object and verify property value.
	 */
	@Test
	public void GetExecutionProperties() {
		boolean pass = false;
		try {
			Map<String, String> execProps = new HashMap<String, String>();
			execProps.put("USE_PARENT_TRANSACTION", "true");

			Object proxy = TestUtil.getContextService().createContextualProxy(new TestRunnableWork(), execProps, Runnable.class,
					TestWorkInterface.class);
			Map<String, String> returnedExecProps = TestUtil.getContextService().getExecutionProperties(proxy);

			if (!"true".equals(returnedExecProps.get("USE_PARENT_TRANSACTION"))) {
				log.severe("Expected:true, actual message=" + returnedExecProps.get("USE_PARENT_TRANSACTION"));
			} else {
				pass = true;
			}
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}

	/*
	 * @testName: GetExecutionPropertiesNoProxy
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:14
	 * 
	 * @test_Strategy: Lookup default ContextService object. Retrieve
	 * ExecutionProperties from plain object.
	 */
	@Test
	public void GetExecutionPropertiesNoProxy() {
		boolean pass = false;
		try {
			Map<String, String> returnedExecProps = TestUtil.getContextService().getExecutionProperties(new Object());
			pass = true;
		} catch (IllegalArgumentException ie) {
			pass = true;
		} catch (Exception e) {
			log.severe("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}
}
