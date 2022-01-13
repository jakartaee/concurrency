/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.api.LastExecution;

import java.lang.reflect.Method;
import java.util.Date;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.Trigger;

/**
 * A trigger that driven by test logic. This trigger is used for test the logic
 * of LastExecution, since trigger can not return value to the client, it is
 * also not ensured to be able to visit jndi. We use the execution times to
 * denote if the test runs successfully. If the trigger is triggered 2 times,
 * the test passes, otherwise the test fails.
 */
public class LogicDrivenTrigger implements Trigger {

	private static final TestLogger log = TestLogger.get(LogicDrivenTrigger.class);

	private long delta;
	private String testName;
	private boolean moreThanTwice = false;
	private Date startTime;

	private static final long TIME_COMPARE_INACCURACY = 2 * 1000;
	public static final int RIGHT_COUNT = 2;
	public static final int WRONG_COUNT = 1;

	public LogicDrivenTrigger(long delta, String testName) {
		this.delta = delta;
		this.testName = testName;
		this.startTime = new Date();
	}

	private String getErrStr4NotEqual(String testName, Object expected, Object real) {
		String result = testName + "failed, ";
		result += "expected " + expected + ",";
		result += "but got " + real;
		return result;
	}

	private boolean validateDateTimeEquals(Date time1, Date time2) {
		long diff = time1.getTime() - time2.getTime();

		if (Math.abs(diff) < TIME_COMPARE_INACCURACY) {
			return true;
		} else {
			return false;
		}
	}

	public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
		if (lastExecutionInfo == null) {
			return new Date();
		}
		
		if(moreThanTwice) {
			return null;
		}
		
		Method testMethod;
		try {
			testMethod = getClass().getMethod(testName, LastExecution.class, Date.class);
			return (Date) testMethod.invoke(this, lastExecutionInfo, taskScheduledTime);
		} catch (Exception e) {
			throw new RuntimeException("Could not run test", e);
		}
		
	}
	

	public Date lastExecutionGetIdentityNameTest(LastExecution lastExecutionInfo, Date taskScheduledTime) {
		if (!LastExecutionTests.IDENTITY_NAME_TEST_ID.equals(lastExecutionInfo.getIdentityName())) {
			log.warning(getErrStr4NotEqual(testName, LastExecutionTests.IDENTITY_NAME_TEST_ID, lastExecutionInfo.getIdentityName()));
			return null;
		}

		moreThanTwice = true;
		return new Date(new Date().getTime() + delta);
	}
	

	public Date lastExecutionGetResultRunnableTest(LastExecution lastExecutionInfo, Date taskScheduledTime) {
		if (lastExecutionInfo.getResult() != null) {
			log.warning(getErrStr4NotEqual(testName, null,
					lastExecutionInfo.getResult()));
			return null;
		}
		
		moreThanTwice = true;
		return new Date(new Date().getTime() + delta);
	}
	

	public Date lastExecutionGetResultCallableTest(LastExecution lastExecutionInfo, Date taskScheduledTime) {
		if (!Integer.valueOf(1).equals(lastExecutionInfo.getResult())) {
			log.warning(getErrStr4NotEqual(testName, 1,
					lastExecutionInfo.getResult()));
			return null;
		}
		moreThanTwice = true;
		return new Date(new Date().getTime() + delta);
	}

	public Date lastExecutionGetRunningTimeTest(LastExecution lastExecutionInfo, Date taskScheduledTime) {
		if (!validateDateTimeEquals(this.startTime, lastExecutionInfo.getScheduledStart())) {
			log.warning(getErrStr4NotEqual(testName, this.startTime,
					lastExecutionInfo.getScheduledStart()));
			return null;
		}
		
		if (lastExecutionInfo.getScheduledStart().getTime() > lastExecutionInfo.getRunStart().getTime()) {
			log.warning(testName
					+ "failed, getRunStart time should not be earlier than getScheduledStart");
			return null;
		}
		
		if ((lastExecutionInfo.getRunEnd().getTime()
				- lastExecutionInfo.getRunStart().getTime()) < TestConstants.PollInterval.toMillis()) {
			log.warning(testName
					+ "failed, the difference between getRunEnd and getRunStart"
					+ "is shorter than the real running time");
			return null;
		}

		moreThanTwice = true;
		return new Date(new Date().getTime() + delta);
	}

	public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
		return false;
	}
}
