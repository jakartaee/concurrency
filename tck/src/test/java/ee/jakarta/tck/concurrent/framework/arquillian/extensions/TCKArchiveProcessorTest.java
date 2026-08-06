/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Signature;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

public class TCKArchiveProcessorTest {
    private static final TCKArchiveProcessor processor = new TCKArchiveProcessor();
    
    @Web
    @Common(Common.PACKAGE.TASKS)
    private static class TestWebClass {}
    
    @Test
    public void processorShouldAppendServletFrameworkForWebArchives() {
        WebArchive testWAR = ShrinkWrap.create(WebArchive.class);
        assertTrue(testWAR.getContent().isEmpty());
        
        processor.process(testWAR, new TestClass(TestWebClass.class));
        assertFalse(testWAR.getContent().isEmpty());
        
        // Should append task packages    
        assertTrue(testWAR.getContent().containsKey(new BasicPath("/WEB-INF/classes/ee/jakarta/tck/concurrent/common/tasks")));    
        
        // Should not append signature packages
        assertFalse(testWAR.getContent().containsKey(new BasicPath("/WEB-INF/classes/ee/jakarta/tck/concurrent/common/signature")));
    }
    
    @Signature
    private static class TestSignatureClass {}
    
    @Test
    public void processorShouldAppendSignatureFrameworkForSignatureArchvies() {
        WebArchive testWAR = ShrinkWrap.create(WebArchive.class);
        assertTrue(testWAR.getContent().isEmpty());
        
        processor.process(testWAR, new TestClass(TestSignatureClass.class));
        assertFalse(testWAR.getContent().isEmpty());
        
        // Should not append framework packages    
        assertFalse(testWAR.getContent().containsKey(new BasicPath("/WEB-INF/classes/ee/jakarta/tck/concurrent/framework")));    
        
        // Should append signature packages
        assertTrue(testWAR.getContent().containsKey(new BasicPath("/WEB-INF/classes/ee/jakarta/tck/concurrent/common/signature")));
        assertTrue(testWAR.getContent().containsKey(new BasicPath("/WEB-INF/lib/sigtest-maven-plugin.jar")));
    }

    @Test
    public void sigTestArchiveShouldIncludePluginClassesAndSignatureResources() {
        assertNotNull(TCKDependencyProcessor.SigTestArchive.get());
        assertTrue(TCKDependencyProcessor.SigTestArchive.get()
                .contains(new BasicPath("/com/sun/tdk/signaturetest/SigTest.class")));
        assertTrue(TCKDependencyProcessor.SigTestArchive.get()
                .contains(new BasicPath("/org/netbeans/apitest/Sigtest.class")));
        assertTrue(TCKDependencyProcessor.SigTestArchive.get()
                .contains(new BasicPath("/jakarta/tck/sigtest_maven_plugin/HelpMojo.class")));
        assertTrue(TCKDependencyProcessor.SigTestArchive.get()
                .contains(new BasicPath("/META-INF/MANIFEST.MF")));
    }
}
