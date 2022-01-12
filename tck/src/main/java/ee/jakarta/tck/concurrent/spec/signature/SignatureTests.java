/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import jakarta.enterprise.concurrent.AbortedException;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.CronTrigger;
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.concurrent.SkippedException;
import jakarta.enterprise.concurrent.Trigger;
import jakarta.enterprise.concurrent.ZonedTrigger;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

public class SignatureTests extends TestClient {
	
	private static final String APP_NAME = "SignatureTests";
	
	private static final Class[] classes = new Class[] {
			AbortedException.class,
			Asynchronous.class,
			Asynchronous.Result.class,
			ContextService.class,
			ContextServiceDefinition.class,
			ContextServiceDefinition.List.class,
			CronTrigger.class,
			LastExecution.class,
			ManageableThread.class,
			ManagedExecutorDefinition.class,
			ManagedExecutorDefinition.List.class,
			ManagedExecutors.class,
			ManagedExecutorService.class,
			ManagedScheduledExecutorDefinition.class,
			ManagedScheduledExecutorDefinition.List.class,
			ManagedScheduledExecutorService.class,
			ManagedTask.class,
			ManagedTaskListener.class,
			ManagedThreadFactory.class,
			ManagedThreadFactoryDefinition.class,
			ManagedThreadFactoryDefinition.List.class,
			SkippedException.class,
			ThreadContextProvider.class,
			ThreadContextRestorer.class,
			ThreadContextSnapshot.class,
			Trigger.class,
			ZonedTrigger.class
	};

	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="SignatureTests", testable=false)
	public static WebArchive createDeployment() {
		WebArchive web = ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), SignatureTests.class.getPackage());

		for(Class clazz : classes) {
			String fileName = clazz.getName().replace('$', '-') + ".sig";
			web.addAsWebInfResource(SignatureTests.class.getPackage(), fileName, "/signaturetest/" + fileName);
		}

		return web;
	}
	
	@DataProvider(name="testClasses")
	public static Object[] testClasses() {
		return classes;
	}
	
	@Test(dataProvider = "testClasses")
    public void testSignatures(Class testClass){
			URLBuilder b = URLBuilder.get().withBaseURL(baseURL).withPaths("SignatureTestServlet").withTestName(testName).withQueries("action=" + testClass.getName());
			runTest(b);
    }
}
