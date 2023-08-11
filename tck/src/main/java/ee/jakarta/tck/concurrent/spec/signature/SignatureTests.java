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
package ee.jakarta.tck.concurrent.spec.signature;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.signature.ConcurrencySignatureTestRunner;
import ee.jakarta.tck.concurrent.framework.junit.anno.Signature;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@Signature
public class SignatureTests {

    @Deployment(name = "SignatureTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "signatureTest.war");
    }

    @Test
    public void testSignatures() throws Exception {
        ConcurrencySignatureTestRunner.assertProjectSetup();
        ConcurrencySignatureTestRunner sigTest = new ConcurrencySignatureTestRunner();
        sigTest.signatureTest();
    }
}
