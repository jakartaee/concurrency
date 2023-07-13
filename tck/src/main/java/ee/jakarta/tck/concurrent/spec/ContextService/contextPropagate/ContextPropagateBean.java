/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Local(ContextPropagateInterface.class)
@Stateless
public class ContextPropagateBean implements ContextPropagateInterface {

    @Resource(lookup = TestConstants.defaultManagedThreadFactory)
    public ManagedThreadFactory threadFactory;

    @Resource(lookup = TestConstants.defaultContextService)
    public ContextService context;

    @Override
    public TestWorkInterface createWorker(final String classname) {
        try {
            return (TestWorkInterface) context.createContextualProxy(
                    Class.forName(classname).getConstructor().newInstance(), Runnable.class, TestWorkInterface.class);
        } catch (Exception en) {
            throw new RuntimeException(en);
        }
    }

    @Override
    public String executeWorker(final TestWorkInterface worker) {
        Thread workThread = threadFactory.newThread(worker);
        workThread.start();
        try {
            workThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return worker.getResult();
    }
}
