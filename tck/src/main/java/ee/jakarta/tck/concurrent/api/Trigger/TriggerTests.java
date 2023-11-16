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

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.common.tasks.CommonTasks;
import ee.jakarta.tck.concurrent.common.tasks.CommonTriggers;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Assertions;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.SkippedException;

@Web
@Common({ PACKAGE.FIXED_COUNTER, PACKAGE.TASKS })
public class TriggerTests {

    @Deployment(name = "TriggerTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addPackages(true, TriggerTests.class.getPackage());
    }

    @Resource(lookup = TestConstants.defaultManagedScheduledExecutorService)
    private ManagedScheduledExecutorService scheduledExecutor;

    @BeforeEach
    public void reset() {
        StaticCounter.reset();
    }

    @Assertion(id = "JAVADOC:46", strategy = "Retrieve the next time that the task should run after.")
    public void triggerGetNextRunTimeTest() throws Exception {
        Future<?> result = scheduledExecutor.schedule(new CounterRunnableTask(),
                new CommonTriggers.TriggerFixedRate(new Date(), TestConstants.pollInterval.toMillis()));
        
        /**
         * This test is susceptible to timing issues depending on the hardware running the test.
         * Therefore, if immediately after scheduling this task the count is already > 0
         * Then, be more liberal with the range of acceptable values that signify a passing test.
         */
        int rangeOffset = StaticCounter.getCount() == 0 ? 2 : 3;

        try {
            Wait.sleep(TestConstants.waitTimeout);
            Assertions.assertBetween(StaticCounter.getCount(),
                    TestConstants.pollsPerTimeout - rangeOffset,
                    TestConstants.pollsPerTimeout + rangeOffset);
        } finally {
            Wait.waitForTaskComplete(result);
        }
    }

    @Assertion(id = "JAVADOC:47", strategy = "Return true if this run instance should be skipped."
            + " This is useful if the task shouldn't run because it is late or if the task is paused or suspended."
            + " Once this task is skipped, the state of it's Future's result will throw a SkippedException."
            + " Unchecked exceptions will be wrapped in a SkippedException.")
    public void triggerSkipRunTest() {
        ScheduledFuture<?> sf = scheduledExecutor.schedule(new CommonTasks.SimpleCallable(),
                new CommonTriggers.OnceTriggerDelaySkip(TestConstants.pollInterval));
        
        try {
            Wait.waitTillFutureThrowsException(sf, SkippedException.class);
        } finally {
            sf.cancel(true);
        }
    }
}
