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

package ee.jakarta.tck.concurrent.api.SkippedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.SkippedException;

@Web // TODO couldn't this be a unit test?
public class SkippedExceptionTests {

    // TODO deploy as EJB and JSP artifacts
    @Deployment(name = "SkippedExceptionTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addPackages(true, SkippedExceptionTests.class.getPackage());
    }

    @Assertion(id = "JAVADOC:42", strategy = "Constructs an SkippedException.")
    public void skippedExceptionNoArgTest() {
        SkippedException thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException();
        });

        assertNull(thrown.getMessage());
    }

    @Assertion(id = "JAVADOC:43", strategy = "Constructs an SkippedException.")
    public void skippedExceptionStringTest() {
        final String expected = "thisisthedetailmessage";

        SkippedException thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(expected);
        });

        assertNotNull(thrown.getMessage());
        assertEquals(expected, thrown.getMessage());
    }

    @Assertion(id = "JAVADOC:45", strategy = "Constructs an SkippedException.")
    public void skippedExceptionThrowableTest() {
        SkippedException thrown;

        final Throwable expected = new Throwable("thisisthethrowable");

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(expected);
        });

        assertNotNull(thrown.getCause());
        assertEquals(expected, thrown.getCause());

        final Throwable expectedNull = null;

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(expectedNull);
        });

        assertNull(thrown.getCause());
    }

    @Assertion(id = "JAVADOC:44", strategy = "Constructs an SkippedException.")
    public void skippedExceptionStringThrowableTest() {
        SkippedException thrown;

        String sExpected = "thisisthedetailmessage";
        String sExpectedNull = null;
        final Throwable tExpected = new Throwable("thisisthethrowable");
        final Throwable tExpectedNull = null;

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(sExpected, tExpected);
        });

        assertNotNull(thrown.getMessage());
        assertNotNull(thrown.getCause());
        assertEquals(sExpected, thrown.getMessage());
        assertEquals(tExpected, thrown.getCause());

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(sExpected, tExpectedNull);
        });

        assertNotNull(thrown.getMessage());
        assertNull(thrown.getCause());
        assertEquals(sExpected, thrown.getMessage());

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(sExpectedNull, tExpected);
        });

        assertNull(thrown.getMessage());
        assertNotNull(thrown.getCause());
        assertEquals(tExpected, thrown.getCause());

        thrown = assertThrows(SkippedException.class, () -> {
            throw new SkippedException(sExpectedNull, tExpectedNull);
        });

        assertNull(thrown.getMessage());
        assertNull(thrown.getCause());
    }
}
