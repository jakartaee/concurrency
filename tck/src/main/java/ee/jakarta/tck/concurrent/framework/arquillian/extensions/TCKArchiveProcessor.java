/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import ee.jakarta.tck.concurrent.framework.junit.anno.Common;

/**
 * This extension will intercept archives before they are deployed to the container and append 
 * the packages from the @Common annotation.
 */
public class TCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKArchiveProcessor.class.getCanonicalName());
    
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        String applicationName = applicationArchive.getName() == null ? applicationArchive.getId() : applicationArchive.getName();
        
        if( ! testClass.isAnnotationPresent(Common.class) ) {
            return;
        }
        
        Package[] packages = (Package[]) 
                Stream.of(testClass.getAnnotation(Common.class).value())
                .map(PACKAGE -> PACKAGE.getPackage())
                .collect(Collectors.toList()).toArray();
        
        // If archive is a JAR or WAR
        if (applicationArchive instanceof ClassContainer) {
            log.info("Application Archive [" + applicationName + "] is being appended with packages [" + packages +"]");
            ((ClassContainer<?>) applicationArchive).addPackages(true, packages);
        }
        
        // If archive is an EAR
        if (applicationArchive instanceof LibraryContainer) {
            log.info("Application Archive [" + applicationName + "] is being appended with a library containing packages [" + packages +"]");
            ((LibraryContainer<?>)applicationArchive).addAsLibrary(ShrinkWrap.create(JavaArchive.class).addPackages(true, packages));
        }
    }
}
