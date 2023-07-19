/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package ee.jakarta.tck.concurrent.common.signature;

import java.util.Properties;

/**
 * This class holds the data passed to a signature test invocation during the
 * setup phase. This allows us to keep the passed data separate and reuse the
 * data between the signature test framework base classes.
 */
public final class SigTestData {

    private SigTestData() {
        // Helper class
    }

    private static Properties props = System.getProperties();

    public static String getVehicle() {
        return props.getProperty("vehicle", "");
    }

    public static String getBinDir() {
        return props.getProperty("bin.dir", "");
    }

    public static String getTSHome() {
        return props.getProperty("ts_home", "");
    }

    public static String getTestClasspath() {
        return props.getProperty("sigTestClasspath", "");
    }

    public static String getJavaeeLevel() {
        return props.getProperty("javaee.level", "");
    }

    public static String getCurrentKeywords() {
        return props.getProperty("current.keywords", "");
    }

    public static String getProperty(final String prop) {
        return props.getProperty(prop);
    }

    public static String getOptionalTechPackagesToIgnore() {
        return props.getProperty("optional.tech.packages.to.ignore", "");
    }

    public static String getJtaJarClasspath() {
        return props.getProperty("jtaJarClasspath", "");
    }

    public static String getJImageDir() {
        return props.getProperty("jimage.dir", "");
    }
} // end class SigTestData
