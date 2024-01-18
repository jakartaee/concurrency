/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.framework.junit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

/**
 * Test metadata to track what assertion from GitHub is being tested, and the
 * strategy used to test that assertion.
 *
 * Aggregates the @Test annotation from Junit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface Assertion {
    /**
     * Identifies where the assertion comes from consisting of two components
     * 1. JAVADOC, SPEC, or GIT - Identifies the source document
     * 2. Unique ID - A unique ID for further specification
     *
     * The Unique ID for JAVADOC and SPEC is a legacy artifact from the TCK Platform
     * and might map to a specific line number or document section, however, a meaningful link cannot be guaranteed.
     *
     * The Unique ID for GIT will map to an issue or pull request in the Jakarta Concurrency
     * GitHub repository https://github.com/jakartaee/concurrency
     */
    String id();

    /**
     * A longer description of the assertion being made to keep method names concise
     */
    String strategy() default "";
}
