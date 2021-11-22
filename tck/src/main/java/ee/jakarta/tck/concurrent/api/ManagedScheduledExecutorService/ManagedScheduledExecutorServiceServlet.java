package jakarta.enterprise.concurrent.api.ManagedScheduledExecutorService;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

import jakarta.enterprise.concurrent.api.common.CallableTask;
import jakarta.enterprise.concurrent.api.common.CommonTriggers;
import jakarta.enterprise.concurrent.api.common.RunnableTask;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("ManagedScheduledExecutorServiceServlet")
public class ManagedScheduledExecutorServiceServlet extends TestServlet{
	
	public static final String CALLABLETESTTASK1_RUN_RESULT = "CallableTestTask1";

	private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

	private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedScheduledExecutorService_test_string";

	private static final String TEST_CLASSLOADER_CLASS_NAME = ManagedScheduledExecutorServiceServlet.class.getCanonicalName();


	public void normalScheduleProcess1Test() throws Exception {
		ScheduledFuture result = TestUtil.getManagedScheduledExecutorService().schedule(
				new RunnableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE, TEST_CLASSLOADER_CLASS_NAME),
				new CommonTriggers.OnceTrigger());
		TestUtil.waitForTaskComplete(result);
		
		Object obj = result.get();
		if(obj != null) {
			throw new RuntimeException ("expected null, instead got result: " + obj.toString());
		}
	}

	public void nullCommandScheduleProcessTest() {
		Runnable command = null;

		try {
			TestUtil.getManagedScheduledExecutorService().schedule(command, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		throw new RuntimeException("NullPointerException should be thrown when arg command is null");
	}

	public void normalScheduleProcess2Test() throws Exception {
		ScheduledFuture result = TestUtil.getManagedScheduledExecutorService()
				.schedule(
						(Callable) new CallableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE,
								TEST_CLASSLOADER_CLASS_NAME, CALLABLETESTTASK1_RUN_RESULT),
						new CommonTriggers.OnceTrigger());
		TestUtil.waitForTaskComplete(result);

		Object obj = result.get();

		if (CALLABLETESTTASK1_RUN_RESULT.equals(obj)) {
			return;
		} else {
			throw new RuntimeException("get wrong result:" + obj);
		}

	}

	public void nullCallableScheduleProcessTest() {
		Callable callable = null;

		try {
			TestUtil.getManagedScheduledExecutorService().schedule(callable, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		throw new RuntimeException("NullPointerException should be thrown when arg command is null");
	}

}
