/*
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spi.context;

import ee.jakarta.tck.concurrent.spec.context.StringContext;

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;

/**
 * Thread context restorer for a mock context type that consists of a String value.
 */
public class StringContextRestorer implements ThreadContextRestorer {
    private final String contextToRestore;
    private boolean restored;

    StringContextRestorer(final String s) {
        contextToRestore = s;
    }

    public void endContext() throws IllegalStateException  {
        if (restored)
            throw new IllegalStateException("already restored");
        StringContext.set(contextToRestore);
        restored = true;
    }

    public String toString() {
        return "StringContextRestorer@" + Integer.toHexString(hashCode()) + "(" + contextToRestore + ")";
    }
}
