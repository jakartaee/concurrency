package jakarta.enterprise.concurrent.api.ManagedExecutors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.api.common.CallableTask;
import jakarta.enterprise.concurrent.api.common.RunnableTask;
import jakarta.enterprise.concurrent.api.common.managedTaskListener.ListenerEvent;
import jakarta.enterprise.concurrent.api.common.managedTaskListener.ManagedTaskListenerImpl;
import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("ManagedExecutorsServlet")
public class ManagedExecutorsServlet extends TestServlet{
	
	private static final TestLogger log = TestLogger.get(ManagedExecutorsServlet.class);
	
	private static final String ENV_ENTRY_JNDI_NAME = "java:comp/env/StringValue";

	private static final String ENV_ENTRY_VALUE = "something";

	private ManagedTaskListenerImpl managedTaskListener = new ManagedTaskListenerImpl();

	private boolean shutdown = true;

	@Override
	public void after() {
		managedTaskListener.clearEvents();
	}
	
	private RunnableTask createRunnableTask() {
		return new RunnableTask(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName());
	}

	private CallableTask<String> createCallableTask(String expectedReturnValue) {
		return new CallableTask<String>(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName(),
				expectedReturnValue);
	}

	public void IsCurrentThreadShutdown() {
		Thread createdThread = TestUtil.getManagedThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
				shutdown = ManagedExecutors.isCurrentThreadShutdown();
			}
		});
		// Executors.newSingleThreadExecutor() uses Executors.defaultThreadFactory()
		// to create new thread. So the thread used in this test is a non Manageable
		// Thread.
		Future<?> future = Executors.newSingleThreadExecutor().submit(createdThread);
		TestUtil.waitForTaskComplete(future);
		if(shutdown) {
			throw new RuntimeException("Failed because shutdown is set to be true when running job");
		}
	}

	public void IsCurrentThreadShutdown_ManageableThread() {
		Thread createdThread = TestUtil.getManagedThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
				shutdown = ManagedExecutors.isCurrentThreadShutdown();
			}
		});
		// Executors.newSingleThreadExecutor(managedThreadFactory) uses
		// ManagedThreadFactory
		// to create new (Manageable) thread.
		Future<?> future = Executors.newSingleThreadExecutor(TestUtil.getManagedThreadFactory()).submit(createdThread);
		TestUtil.waitForTaskComplete(future);
		if(shutdown) {
			throw new RuntimeException("Failed because shutdown is set to be true when running job");
		}
	}

	public void ManageRunnableTaskWithTaskListener() {
		RunnableTask runnableTask = createRunnableTask();
		Runnable taskWithListener = ManagedExecutors.managedTask(runnableTask, managedTaskListener);
		Future<?> futureResult = TestUtil.getManagedExecutorService().submit(taskWithListener);
		assertTaskAndListenerComplete(futureResult, runnableTask);
	}

	public void ManageRunnableTaskWithNullArg() {
		Runnable nullTask = null;
		try {
			ManagedExecutors.managedTask(nullTask, managedTaskListener);
		} catch (IllegalArgumentException e) {
			return; //expected
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		
		throw new RuntimeException("Failed to get expected exception");
	}

	public void ManageRunnableTaskWithTaskListenerAndMap() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("key", "value");
		RunnableTask runnableTask = createRunnableTask();
		Runnable task = ManagedExecutors.managedTask(runnableTask, properties, managedTaskListener);
		boolean pass = false;
		if (task instanceof ManagedTask) {
			ManagedTask managedTask = (ManagedTask) task;
			if (managedTask.getExecutionProperties().get("key") != "value")
				throw new RuntimeException("Failed to get expected property");
		}

		assertTaskAndListenerComplete(TestUtil.getManagedExecutorService().submit(task), runnableTask);
	}

	public void ManageRunnableTaskWithMapAndNullArg() {
		Runnable nullTask = null;
		Map<String, String> properties = new HashMap<String, String>();
		try {
			ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
		} catch (IllegalArgumentException e) {
			return; //expected
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}		
		throw new RuntimeException("Failed to get expected exception");
	}

	public void ManageCallableTaskWithTaskListener() {
		String expectedResultStr = "expected something";
		CallableTask<String> callableTask = createCallableTask(expectedResultStr);
		Callable<String> taskWithListener = ManagedExecutors.managedTask((Callable<String>) callableTask,
				managedTaskListener);
		Future<String> futureResult = TestUtil.getManagedExecutorService().submit(taskWithListener);
		assertTaskAndListenerComplete(expectedResultStr, futureResult, callableTask);
	}

	public void ManageCallableTaskWithNullArg() {
		Callable<?> nullTask = null;
		try {
			ManagedExecutors.managedTask(nullTask, managedTaskListener);
		} catch (IllegalArgumentException e) {
			return; //expected
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		throw new RuntimeException("Failed to get expected exception");
	}

	public void ManageCallableTaskWithTaskListenerAndMap() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("key", "value");
		properties.put(ManagedTask.IDENTITY_NAME, "id");
		String expectedResultStr = "expected something";

		CallableTask<String> callableTask = createCallableTask(expectedResultStr);
		Callable<String> task = ManagedExecutors.managedTask((Callable<String>) callableTask, properties,
				managedTaskListener);

		boolean pass = false;
		if (task instanceof ManagedTask) {
			ManagedTask managedTask = (ManagedTask) task;
			if (managedTask.getExecutionProperties().get("key") != "value")
				throw new RuntimeException("Failed to get expected property");
		}
		assertTaskAndListenerComplete(expectedResultStr, TestUtil.getManagedExecutorService().submit(task), callableTask);
	}

	public void ManageCallableTaskWithMapAndNullArg() {
		Callable<?> nullTask = null;
		Map<String, String> properties = new HashMap<String, String>();
		try {
			ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
		} catch (IllegalArgumentException e) {
			return; //expected
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		throw new RuntimeException("Failed to get expected exception");
	}

	private void assertTaskAndListenerComplete(Future<?> future, RunnableTask runnableTask) {
		TestUtil.waitForTaskComplete(future);
		assertListenerComplete(runnableTask);
	}

	private void assertTaskAndListenerComplete(String expectedResult, Future<String> future,
			CallableTask<?> callableTask) {
		String result = TestUtil.waitForTaskComplete(future);
		if(!expectedResult.endsWith(result))
			throw new RuntimeException("Task return different value with expected one.");
		assertListenerComplete(callableTask);
	}

	private void assertListenerComplete(RunnableTask task) {
		// wait for the listener run done.
		TestUtil.waitForListenerComplete(managedTaskListener);
		// check listener status.
		if (!(managedTaskListener.eventCalled(ListenerEvent.SUBMITTED)
				&& managedTaskListener.eventCalled(ListenerEvent.STARTING)
				&& managedTaskListener.eventCalled(ListenerEvent.DONE))) {
			throw new RuntimeException("TaskListener is not completely executed.");
		}
	}
}
