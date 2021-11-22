
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

package jakarta.enterprise.concurrent.api.Trigger;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.SkippedException;
import jakarta.enterprise.concurrent.api.common.CommonTriggers;
import jakarta.enterprise.concurrent.common.counter.CounterRunnableTask;
import jakarta.enterprise.concurrent.common.counter.StaticCounter;
import jakarta.enterprise.concurrent.tck.framework.ArquillianTests;
import jakarta.enterprise.concurrent.tck.framework.TestConstants;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;

public class TriggerTests extends ArquillianTests {
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="Trigger")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), getAPICommonPackage(), getCommonCounterPackage(), TriggerTests.class.getPackage());
	}

	/*
	 * @testName: triggerGetNextRunTimeTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:46
	 * 
	 * @test_Strategy: Retrieve the next time that the task should run after.
	 */
	@Test
	public void triggerGetNextRunTimeTest() throws Exception {
		ScheduledFuture sf = TestUtil.getManagedScheduledExecutorService().schedule(new CounterRunnableTask(),
				new CommonTriggers.TriggerFixedRate(new Date(), TestConstants.PollInterval.toMillis()));

		try {
			if (StaticCounter.getCount() != 0) {
				throw new RuntimeException("The first trigger is too fast.");
			}

			TestUtil.sleep(TestConstants.WaitTimeout);
			int result = StaticCounter.getCount();
			assertInRange(result, TestConstants.PollsPerTimeout - 2, TestConstants.PollsPerTimeout + 2);
		} finally {
			// make sure the task schedule by this case is stop
			try {
				TestUtil.sleep(TestConstants.WaitTimeout.multipliedBy(2));
			} catch (InterruptedException ignore) {
			}
		}
	}

	/*
	 * @testName: triggerSkipRunTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:47
	 * 
	 * @test_Strategy: Return true if this run instance should be skipped. This is
	 * useful if the task shouldn't run because it is late or if the task is paused
	 * or suspended. Once this task is skipped, the state of it's Future's result
	 * will throw a SkippedException. Unchecked exceptions will be wrapped in a
	 * SkippedException.
	 */
	@Test
	public void triggerSkipRunTest() {
		ScheduledFuture sf = TestUtil.getManagedScheduledExecutorService().schedule(new Callable() {
			public Object call() {
				return "ok";
			}
		}, new CommonTriggers.OnceTriggerDelaySkip(TestConstants.PollInterval.toMillis()));

		long start = System.currentTimeMillis();
		while (!sf.isDone()) {
			try {
				sf.get(100, TimeUnit.MILLISECONDS);
			} catch (SkippedException se) {
				return;
			} catch (ExecutionException ee) {
			} catch (TimeoutException | InterruptedException e) {
			}
			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				fail("wait task timeout");
			}
		}
		fail("SkippedException should be caught.");
	}
}
