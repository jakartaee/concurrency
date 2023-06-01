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

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

/**
 * This extension will intercept all archives before they are deployed to the container and append 
 * a library with the following:
 * 
 * Package - ee.jakarta.tck.concurrent.framework
 * 
 */
public class TCKFrameworkAppender implements AuxiliaryArchiveAppender {
    
    private static final Package utilPackage = TestUtil.class.getPackage();
    private static final Package annoPackage = Web.class.getPackage();

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive framework = ShrinkWrap.create(JavaArchive.class, "jakarta-concurrent-framework.jar");
        framework.addPackages(false, utilPackage, annoPackage);
        return framework;
    }

}
