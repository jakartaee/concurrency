/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.framework;

import java.time.Duration;

/**
 * Constants that are used within the TCK to ensure consistency in test infrastructure.
 */
public final class TestConstants {
	
	//JNDI Names
	public static final String DefaultContextService = "java:comp/DefaultContextService";
	public static final String DefaultManagedScheduledExecutorService = "java:comp/DefaultManagedScheduledExecutorService";
	public static final String DefaultManagedExecutorService = "java:comp/DefaultManagedExecutorService";
	public static final String DefaultManagedThreadFactory = "java:comp/DefaultManagedThreadFactory";
	public static final String UserTransaction = "java:comp/UserTransaction";

	
	//Durations
	/** 1 second */
	public static final Duration PollInterval = Duration.ofSeconds(1);
	
	/** 15 seconds */
	public static final Duration WaitTimeout = Duration.ofSeconds(15);
	
	/** Approximate number of polls performed before timeout */
	public static final int PollsPerTimeout = (int) (WaitTimeout.getSeconds() / PollInterval.getSeconds());
	
	//Return values
	public static final String SimpleReturnValue = "ok";
	public static final String ComplexReturnValue = "ConcurrentResultOkay";
}
