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

package ee.jakarta.tck.concurrent.api.Trigger;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.CommonTriggers;
import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.ArquillianTests;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.SkippedException;

@Web
public class TriggerTests extends ArquillianTests {
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="TriggerTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), 
						getCommonPackage(), 
						getCommonFixedCounterPackage(), 
						TriggerTests.class.getPackage());
	}

	@BeforeEach
	public void reset() {
		StaticCounter.reset();
	}

	/*
	 * @testName: triggerGetNextRunTimeTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:46
	 * 
	 * @test_Strategy: Retrieve the next time that the task should run after.
         *  fix: https://github.com/jakartaee/concurrency/pull/222
         *  Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/228
         *  Can be reenabled in next release of Jakarta Concurrency
	 */
	@Disabled
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
		try {
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
		} finally {
			sf.cancel(true);
		}

		fail("SkippedException should be caught.");
	}
}
