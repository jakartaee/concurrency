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
package ee.jakarta.tck.concurrent.framework.junit.extensions;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Challenge;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;

/**
 * Logs before and after test execution, and injects the name of the test into
 * the @TestName field.
 */
public class AssertionExtension implements TestWatcher, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Logger log = Logger.getLogger(AssertionExtension.class.getCanonicalName());
    private static final String nl = System.lineSeparator();
    
    @Override
    public void testFailed(final ExtensionContext context, final Throwable cause) {
        Method testMethod = context.getRequiredTestMethod();
        Assertion instance = testMethod.getAnnotation(Assertion.class);
        if (instance != null) {
            log.warning(testMethod.getName() + " failed " + nl
                    + " @Assertion.ids: " + instance.id() + nl
                    + " @Assertion.strategy: " + instance.strategy() + nl
                    + " Throwable.cause: " + cause.getLocalizedMessage());
        }
    }
    
    @Override
    public void testAborted(final ExtensionContext context, final Throwable cause) {
        Method testMethod = context.getRequiredTestMethod();
        Assertion instance = testMethod.getAnnotation(Assertion.class);
        if (instance != null) {
            log.warning(testMethod.getName() + " was aborted " + nl
                    + " @Assertion.ids: " + instance.id() + nl
                    + " @Assertion.strategy: " + instance.strategy() + nl
                    + " Throwable.cause: " + cause.getLocalizedMessage());
        }
    }
    
    @Override
    public void testDisabled(final ExtensionContext context, final Optional<String> reason) {
        Method testMethod = context.getRequiredTestMethod();
        Assertion instance = testMethod.getAnnotation(Assertion.class);
        boolean isChallenge = testMethod.isAnnotationPresent(Challenge.class);
        Challenge challenge =  isChallenge ? testMethod.getAnnotation(Challenge.class) : null;
        if (instance != null) {
            log.warning(testMethod.getName() + " is disabled" + nl
                    + " @Assertion.id: #" + instance.id() + nl
                    + " @Assertion.strategy: " + instance.strategy() + nl
                    + (isChallenge
                    ? " @Challenge.issue:" + challenge.link() + nl
                    + " @Challenge.version: " + challenge.version()
                    : " @Disabled.reason:" + reason.get()));
        }
    }

    @Override
    public void beforeTestExecution(final ExtensionContext context) throws Exception {
        log.info(">>> Begin test: " + context.getDisplayName());
        injectTestName(context, context.getRequiredTestMethod().getName());
    }

    @Override
    public void afterTestExecution(final ExtensionContext context) throws Exception {
        log.info("<<< End test: " + context.getDisplayName());
        injectTestName(context, null);
    }

    // TODO could consider using getFields to allow for injection into superclass,
    // but will affect performance.
    private void injectTestName(final ExtensionContext context, final String testname) {
        Class<?> testClass = context.getRequiredTestClass();

        Stream.of(testClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(TestName.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(context.getRequiredTestInstance(), testname);
                    } catch (Exception e) {
                        log.warning("Unable to set TestName field on test class: " + testClass.getCanonicalName()
                                + " Error:" + e.getLocalizedMessage());
                    }
                });
    }

}
