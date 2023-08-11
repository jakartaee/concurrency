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
 * Constants that are used within the TCK to ensure consistency in test
 * infrastructure.
 */
public final class TestConstants {
    
    private TestConstants() {
        //utility class
    }

    // JNDI Names
    public static final String defaultContextService = "java:comp/DefaultContextService";
    public static final String defaultManagedScheduledExecutorService = "java:comp/DefaultManagedScheduledExecutorService";
    public static final String defaultManagedExecutorService = "java:comp/DefaultManagedExecutorService";
    public static final String defaultManagedThreadFactory = "java:comp/DefaultManagedThreadFactory";
    public static final String userTransaction = "java:comp/UserTransaction";

    // Durations
    /** 1 second */
    public static final Duration pollInterval = Duration.ofSeconds(1);

    /** 15 seconds */
    public static final Duration waitTimeout = Duration.ofSeconds(15);

    /** Approximate number of polls performed before timeout */
    public static final int pollsPerTimeout = (int) (waitTimeout.getSeconds() / pollInterval.getSeconds());

    // Return values
    public static final String simpleReturnValue = "ok";
    public static final String complexReturnValue = "ConcurrentResultOkay";
}
