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

package ee.jakarta.tck.concurrent.api.AbortedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.AbortedException;

@Web // TODO couldn't this be a unit test?
public class AbortedExceptionTests {

    // TODO deploy as EJB and JSP artifacts
    @Deployment(name = "AbortedExceptionTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addPackages(true, AbortedExceptionTests.class.getPackage());
    }

    /*
     * @testName: AbortedExceptionNoArgTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:1
     * 
     * @test_Strategy: Constructs an AbortedException.
     */
    @Test
    public void AbortedExceptionNoArgTest() {
        AbortedException thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException();
        });

        assertNull(thrown.getMessage());
    }

    /*
     * @testName: AbortedExceptionStringTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:3
     * 
     * @test_Strategy: Constructs an AbortedException.
     */
    @Test
    public void AbortedExceptionStringTest() {
        final String expected = "thisisthedetailmessage";

        AbortedException thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(expected);
        });

        assertNotNull(thrown.getMessage());
        assertEquals(expected, thrown.getMessage());
    }

    /*
     * @testName: AbortedExceptionThrowableTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:4
     * 
     * @test_Strategy: Constructs an AbortedException.
     */
    @Test
    public void AbortedExceptionThrowableTest() {
        AbortedException thrown;

        final Throwable expected = new Throwable("thisisthethrowable");

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(expected);
        });

        assertNotNull(thrown.getCause());
        assertEquals(expected, thrown.getCause());

        final Throwable expectedNull = null;

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(expectedNull);
        });

        assertNull(thrown.getCause());
    }

    /*
     * @testName: AbortedExceptionStringThrowableTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:2
     * 
     * @test_Strategy: Constructs an AbortedException.
     */
    @Test
    public void AbortedExceptionStringThrowableTest() {
        AbortedException thrown;

        String sExpected = "thisisthedetailmessage";
        String sExpectedNull = null;
        final Throwable tExpected = new Throwable("thisisthethrowable");
        final Throwable tExpectedNull = null;

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(sExpected, tExpected);
        });

        assertNotNull(thrown.getMessage());
        assertNotNull(thrown.getCause());
        assertEquals(sExpected, thrown.getMessage());
        assertEquals(tExpected, thrown.getCause());

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(sExpected, tExpectedNull);
        });

        assertNotNull(thrown.getMessage());
        assertNull(thrown.getCause());
        assertEquals(sExpected, thrown.getMessage());

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(sExpectedNull, tExpected);
        });

        assertNull(thrown.getMessage());
        assertNotNull(thrown.getCause());
        assertEquals(tExpected, thrown.getCause());

        thrown = assertThrows(AbortedException.class, () -> {
            throw new AbortedException(sExpectedNull, tExpectedNull);
        });

        assertNull(thrown.getMessage());
        assertNull(thrown.getCause());
    }
}
