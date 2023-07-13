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

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.junit.anno.Common;

/**
 * This extension will intercept archives before they are deployed to the
 * container and append the packages from the @Common annotation.
 */
public class TCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKArchiveProcessor.class.getCanonicalName());

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        String applicationName = applicationArchive.getName() == null ? applicationArchive.getId()
                : applicationArchive.getName();

        if (!testClass.isAnnotationPresent(Common.class)) {
            return;
        }

        List<String> packages = Stream.of(testClass.getAnnotation(Common.class).value())
                .map(pkg -> pkg.getPackageName()).collect(Collectors.toList());

        // TODO research to see if there is a way around this
        if (applicationArchive instanceof EnterpriseArchive && !packages.isEmpty()) {
            throw new RuntimeException("Cannot append packages to Enterprise Archives since modules are immutable");
        }

        if (applicationArchive instanceof WebArchive || applicationArchive instanceof JavaArchive) {
            log.info("Application Archive [" + applicationName + "] is being appended with packages " + packages);
            packages.stream().forEach(pkg -> ((ClassContainer<?>) applicationArchive).addPackage(pkg));

        }

    }
}
