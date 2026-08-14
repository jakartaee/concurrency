/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.api.Lock;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@RunAsClient
public class LockTests extends TestClient {

    @ArquillianResource(LockServlet.class)
    private URL baseURL;

    @Deployment(name = "LockTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "LockTests_web.war")
                .addPackages(false, LockTests.class.getPackage());
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "LockServlet";
    }

    // Numbers in assertion ids are line numbers in the Lock Javadoc source

    @Assertion(id = "JAVADOC:36", strategy = """
            Tests invocation of an asynchronous method with Lock annotation
            """)
    public void testLockAsyncMethod() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:65", strategy = """
            Tests multiple threads sharing a READ lock
            """)
    public void testMultipleThreadsShareReadLock() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:36", strategy = """
            Tests that WRITE lock on one bean does not block a WRITE lock
            on a different bean
            """)
    public void testMultipleThreadsWithWriteLocksOnDifferentBeans()
            throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:45", strategy = """
            Tests that a method without a Lock annotation is not blocked
            by a WRITE lock on the same bean
            """)
    public void testNoLock() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:75", strategy = """
            Tests that READ lock does not upgrade to WRITE lock
            """)
    public void testReadLockDoesNotUpgradeToWriteLock() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:38", strategy = """
            Tests READ lock re-entry from the same thread
            """)
    public void testReadLockReentryFromSameThread() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:71", strategy = """
            Tests WRITE lock blocking another thread from acquiring
            READ lock
            """)
    public void testWriteLockBlocksOtherThreadFromRead() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:71", strategy = """
            Tests WRITE lock blocking another thread from acquiring
            WRITE lock
            """)
    public void testWriteLockBlocksOtherThreadFromWrite() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:79", strategy = """
            Tests WRITE lock re-entry from the same thread
            """)
    public void testWriteLockReentryFromSameThread() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "JAVADOC:79", strategy = """
            Tests WRITE lock to READ lock re-entry from the same thread
            """)
    public void testWriteLockToReadLockReentryFromSameThread()
            throws Exception {
        runTest(baseURL, testname);
    }
}
