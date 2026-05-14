/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
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

import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import ee.jakarta.tck.concurrent.framework.TestPropertyHandler;
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
public class TCKFrameworkAppender extends CachedAuxilliaryArchiveAppender {
    
    private static final Package utilPackage = TestServlet.class.getPackage();
    private static final Package annoPackage = Common.class.getPackage();
    private static final Package extePackage = AssertionExtension.class.getPackage();

    @Override
    public Archive<?> buildArchive() {
        JavaArchive framework = ShrinkWrap.create(JavaArchive.class, "jakarta-concurrent-framework.jar");
        framework.addPackages(false, utilPackage, annoPackage, extePackage);
        return TestPropertyHandler.storeProperties(framework);
    }

}
