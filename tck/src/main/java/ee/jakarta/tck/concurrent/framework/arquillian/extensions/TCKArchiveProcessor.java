/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import ee.jakarta.tck.concurrent.common.signature.ConcurrencySignatureTestRunner;
import ee.jakarta.tck.concurrent.framework.TestProperty;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Signature;

/**
 * This extension will intercept archives before they are deployed to the
 * container and append the packages from the @Common annotation.
 */
public class TCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKArchiveProcessor.class.getCanonicalName());

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        String applicationName = applicationArchive.getName() == null
                ? applicationArchive.getId()
                : applicationArchive.getName();
        
        appendCommonPackages(applicationArchive, testClass, applicationName);
        appendSignaturePackages(applicationArchive, testClass, applicationName);

    }
    
    private static void appendCommonPackages(final Archive<?> applicationArchive, final TestClass testClass, final String applicationName) {
        if (!testClass.isAnnotationPresent(Common.class)) {
            return; //Nothing to append
        }

        List<String> packages = Stream.of(testClass.getAnnotation(Common.class).value())
                .map(pkg -> pkg.getPackageName()).collect(Collectors.toList());
        
        if (packages.isEmpty()) {
            return; //Nothing to append
        }

        // TODO research to see if there is a way around this
        if (applicationArchive instanceof EnterpriseArchive) {
            throw new RuntimeException("Cannot append packages to Enterprise Archives since modules are immutable");
        }

        if (applicationArchive instanceof WebArchive || applicationArchive instanceof JavaArchive) {
            log.info("Application Archive [" + applicationName + "] is being appended with packages " + packages);
            packages.stream().forEach(pkg -> ((ClassContainer<?>) applicationArchive).addPackage(pkg));

        }
    }
    
    private static void appendSignaturePackages(final Archive<?> applicationArchive, final TestClass testClass, final String applicationName) {
        if (!testClass.isAnnotationPresent(Signature.class)) {
            return; //Nothing to append
        }
        final String jdkVersion = TestProperty.javaSpecVer.getValue();
        
        final Package signaturePackage = ConcurrencySignatureTestRunner.class.getPackage();
        final String signatureFileName = ConcurrencySignatureTestRunner.SIG_FILE_NAME + "_" + jdkVersion;
        
        if (!signatureFileExists(signaturePackage, signatureFileName)) {
            throw new RuntimeException("No signature file exists for name: " + signatureFileName);
        }

        if (applicationArchive instanceof ClassContainer) {
            
            // Add the Concurrency runner
            log.info("Application Archive [" + applicationName + "] is being appended with packages [" + signaturePackage + "]");
            ((ClassContainer<?>) applicationArchive).addPackage(signaturePackage);

            // Add the sigtest plugin library
            File sigTestDep = Maven.resolver().resolve("jakarta.tck:sigtest-maven-plugin:2.3").withoutTransitivity().asSingleFile();
            log.info("Application Archive [" + applicationName + "] is being appended with library " + sigTestDep.getName());
            ((LibraryContainer<?>) applicationArchive).addAsLibrary(sigTestDep);
            
            // Add signature resources
            log.info("Application Archive [" + applicationName + "] is being appended with resources "
                    + Arrays.asList(ConcurrencySignatureTestRunner.SIG_RESOURCES));
            ((ResourceContainer<?>) applicationArchive).addAsResources(signaturePackage,
                    ConcurrencySignatureTestRunner.SIG_MAP_NAME, ConcurrencySignatureTestRunner.SIG_PKG_NAME);
            ((ResourceContainer<?>) applicationArchive).addAsResource(signaturePackage,
                    // Get local resource based on JDK level
                    signatureFileName,
                    // Target same package as test
                    signaturePackage.getName().replace(".", "/") + "/" + ConcurrencySignatureTestRunner.SIG_FILE_NAME);
        }
    }
    
    private static boolean signatureFileExists(final Package pkg, final String fileName) {
        String classloaderResourceName = AssetUtil.getClassLoaderResourceName(pkg, fileName);
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(classloaderResourceName) != null;
    }
}
