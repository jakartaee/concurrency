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
package ee.jakarta.tck.concurrent.framework.arquillian.extensions;

import java.util.Arrays;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;

import ee.jakarta.tck.concurrent.common.signature.ConcurrencySignatureTestRunner;
import ee.jakarta.tck.concurrent.framework.junit.anno.Full;
import ee.jakarta.tck.concurrent.framework.junit.anno.Signature;

/**
 * This extension will intercept archives before they are deployed to the
 * container and append the signature files.
 */
public class TCKSignatureProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKSignatureProcessor.class.getCanonicalName());

    private static final Package signaturePackage = ConcurrencySignatureTestRunner.class.getPackage();

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        String applicationName = applicationArchive.getName() == null ? applicationArchive.getId()
                : applicationArchive.getName();

        if (!testClass.isAnnotationPresent(Signature.class)) {
            return;
        }

        if (testClass.isAnnotationPresent(Full.class)) {
            throw new RuntimeException("Signature tests must be run using the @Web annotation");
        }

        boolean isJava21orAbove = Integer.parseInt(System.getProperty("java.specification.version")) >= 21;

        if (applicationArchive instanceof ClassContainer) {
            log.info("Application Archive [" + applicationName + "] is being appended with packages ["
                    + signaturePackage + "]");
            log.info("Application Archive [" + applicationName + "] is being appended with resources "
                    + Arrays.asList(ConcurrencySignatureTestRunner.SIG_RESOURCES));
            ((ClassContainer<?>) applicationArchive).addPackage(signaturePackage);
            ((ResourceContainer<?>) applicationArchive).addAsResources(signaturePackage,
                    ConcurrencySignatureTestRunner.SIG_MAP_NAME, ConcurrencySignatureTestRunner.SIG_PKG_NAME);
            ((ResourceContainer<?>) applicationArchive).addAsResource(signaturePackage,
                    // Get local resource based on JDK level
                    isJava21orAbove ? ConcurrencySignatureTestRunner.SIG_FILE_NAME + "_21"
                            : ConcurrencySignatureTestRunner.SIG_FILE_NAME + "_17",
                    // Target same package as test
                    signaturePackage.getName().replace(".", "/") + "/" + ConcurrencySignatureTestRunner.SIG_FILE_NAME);
        }

    }
}
