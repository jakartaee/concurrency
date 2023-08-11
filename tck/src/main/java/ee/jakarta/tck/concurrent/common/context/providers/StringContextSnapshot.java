/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.common.context.providers;

import ee.jakarta.tck.concurrent.common.context.StringContext;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

/**
 * Thread context snapshot for a mock context type that consists of a String
 * value.
 */
public class StringContextSnapshot implements ThreadContextSnapshot {
    private final String contextSnapshot;

    StringContextSnapshot(final String s) {
        contextSnapshot = s;
    }

    public ThreadContextRestorer begin() {
        ThreadContextRestorer restorer = new StringContextRestorer(StringContext.get());
        StringContext.set(contextSnapshot);
        return restorer;
    }

    public String toString() {
        return "StringContextSnapshot@" + Integer.toHexString(hashCode()) + "(" + contextSnapshot + ")";
    }
}
