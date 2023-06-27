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

import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;

/**
 * Logs before and after test execution, and injects the name of the test into the @TestName field.
 */
public class AssertionExtension implements BeforeTestExecutionCallback,  AfterTestExecutionCallback {
    
    private static final Logger log = Logger.getLogger(AssertionExtension.class.getCanonicalName());
    
    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        log.info(">>> Begin test: " + context.getDisplayName());
        injectTestName(context, context.getRequiredTestMethod().getName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        log.info("<<< End test: " + context.getDisplayName());
        injectTestName(context, null);
    }
    
    //TODO could consider using getFields to allow for injection into superclass, but will affect performance.
    private void injectTestName(ExtensionContext context, String testname) {        
        Class<?> testClass = context.getRequiredTestClass();
        
        Stream.of(testClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(TestName.class))
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    field.set(context.getRequiredTestInstance(), testname);
                } catch (Exception e) {
                    log.warning("Unable to set TestName field on test class: " + testClass.getCanonicalName() + " Error:" + e.getLocalizedMessage());
                }
            });
    }

}
