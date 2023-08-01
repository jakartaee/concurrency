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

import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.extensions.AssertionExtension;

/**
 * This extension will intercept all archives before they are deployed to the
 * container and append a library with the following:
 *
 * Package - ee.jakarta.tck.concurrent.framework
 * Package - ee.jakarta.tck.concurrent.framework.arquillian.extensions
 * Package - ee.jakarta.tck.concurrent.framework.junit.extensions
 *
 */
public class TCKFrameworkAppender implements AuxiliaryArchiveAppender {

    private static final Logger log = Logger.getLogger(TCKFrameworkAppender.class.getCanonicalName());

    private static final Package utilPackage = TestServlet.class.getPackage();
    private static final Package annoPackage = Common.class.getPackage();
    private static final Package extePackage = AssertionExtension.class.getPackage();

    private static final String archiveName = "jakarta-concurrent-framework.jar";

    private static JavaArchive framework = null;

    @Override
    public Archive<?> createAuxiliaryArchive() {
        if (framework != null) {
            return framework;
        }

        log.info("Creating auxiliary archive: " + archiveName);

        framework = ShrinkWrap.create(JavaArchive.class, archiveName);
        framework.addPackages(false, utilPackage, annoPackage, extePackage);
        return framework;
    }

}
