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

package ee.jakarta.tck.concurrent.api.ManagedExecutors;

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
@Common({PACKAGE.MANAGED_TASK_LISTENER, PACKAGE.TASKS})
public class ManagedExecutorsTests extends TestClient {

	@ArquillianResource
	URL baseURL;
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="ManagedExecutorsTests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, ManagedExecutorsTests.class.getPackage())
				.addAsWebInfResource(ManagedExecutorsTests.class.getPackage(), "web.xml", "web.xml");
	}
	
	@Override
	protected String getServletPath() {
		return "ManagedExecutorsServlet";
	}

	/*
	 * @testName: IsCurrentThreadShutdown
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:21
	 * 
	 * @test_Strategy: Use a regular thread(non-Manageable thread) and verify
	 * isCurrentThreadShutdown() returns false
	 * 
	 */
	@Test
	public void IsCurrentThreadShutdown() {
		runTest(baseURL);
	}

	/*
	 * @testName: IsCurrentThreadShutdown_ManageableThread
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:21
	 * 
	 * @test_Strategy: Create a ManageableThread from ManagedThreadFactory and check
	 * the shutdown status.
	 */
	@Test
	public void IsCurrentThreadShutdown_ManageableThread() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageRunnableTaskWithTaskListener
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:22;CONCURRENCY:SPEC:7;
	 * CONCURRENCY:SPEC:7.1;CONCURRENCY:SPEC:7.2;
	 * CONCURRENCY:SPEC:4;CONCURRENCY:SPEC:4.2; CONCURRENCY:SPEC:18;
	 *
	 * @test_Strategy: Returns a Runnable object that also implements ManagedTask
	 * interface so it can receive notification of life cycle events with the
	 * provided ManagedTaskListener when the task is submitted to a
	 * ManagedExecutorService or a ManagedScheduledExecutorService.
	 */
	@Test
	public void ManageRunnableTaskWithTaskListener() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageRunnableTaskWithNullArg
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:23
	 * 
	 * @test_Strategy: Catch IllegalArgumentException when get the manage task with
	 * null runnable task.
	 */
	@Test
	public void ManageRunnableTaskWithNullArg() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageRunnableTaskWithTaskListenerAndMap
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:24;CONCURRENCY:SPEC:13;
	 * 
	 * @test_Strategy: Returns a Runnable object that also implements ManagedTask
	 * interface so it can receive notification of life cycle events with the
	 * provided ManagedTaskListener and to provide additional execution properties
	 * when the task is submitted to a ManagedExecutorService
	 */
	@Test
	public void ManageRunnableTaskWithTaskListenerAndMap() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageRunnableTaskWithMapAndNullArg
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:25
	 * 
	 * @test_Strategy: Catch IllegalArgumentException when get the manage task with
	 * null runnable task and additional execution properties.
	 */
	@Test
	public void ManageRunnableTaskWithMapAndNullArg() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageCallableTaskWithTaskListener
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:26
	 * 
	 * @test_Strategy: Returns a Callable object that also implements ManagedTask
	 * interface so it can receive notification of life cycle events with the
	 * provided ManagedTaskListener when the task is submitted to a
	 * ManagedExecutorService
	 */
	@Test
	public void ManageCallableTaskWithTaskListener() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageCallableTaskWithNullArg
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:27
	 * 
	 * @test_Strategy: Catch IllegalArgumentException when get the manage task with
	 * null Callable task.
	 */
	@Test
	public void ManageCallableTaskWithNullArg() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageCallableTaskWithTaskListenerAndMap
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:28;CONCURRENCY:SPEC:13.1;
	 * CONCURRENCY:SPEC:45;CONCURRENCY:SPEC:45.1;
	 * 
	 * @test_Strategy: Returns a Callable object that also implements ManagedTask
	 * interface so it can receive notification of life cycle events with the
	 * provided ManagedTaskListener and to provide additional execution properties
	 * when the task is submitted to a ManagedExecutorService
	 */
	@Test
	public void ManageCallableTaskWithTaskListenerAndMap() {
		runTest(baseURL);
	}

	/*
	 * @testName: ManageCallableTaskWithMapAndNullArg
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:29
	 * 
	 * @test_Strategy: Catch IllegalArgumentException when get the manage task with
	 * null Callable task and additional execution properties.
	 */
	@Test
	public void ManageCallableTaskWithMapAndNullArg() {
		runTest(baseURL);
	}
}
