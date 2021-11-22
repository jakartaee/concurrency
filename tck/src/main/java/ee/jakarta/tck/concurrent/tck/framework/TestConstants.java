package jakarta.enterprise.concurrent.tck.framework;

import java.time.Duration;

/**
 * Constants that are used within the TCK to ensure consistency in test infrastructure.
 */
public final class TestConstants {
	
	//JNDI Names
	public static final String DefaultContextService = "java:comp/DefaultContextService";
	public static final String DefaultManagedScheduledExecutorService = "java:comp/DefaultManagedScheduledExecutorService";
	public static final String DefaultManagedExecutorService = "java:comp/DefaultManagedExecutorService";
	public static final String DefaultManagedThreadFactory = "java:comp/DefaultManagedThreadFactory";
	public static final String UserTransaction = "java:comp/UserTransaction";

	
	//Durations
	/** 1 second */
	public static final Duration PollInterval = Duration.ofSeconds(1);
	
	/** 15 seconds */
	public static final Duration WaitTimeout = Duration.ofSeconds(15);
	
	/** Approximate number of polls performed before timeout */
	public static final int PollsPerTimeout = (int) (WaitTimeout.getSeconds() / PollInterval.getSeconds());
	
	//Return values
	public static final String SimpleReturnValue = "ok";
	public static final String ComplexReturnValue = "ConcurrentResultOkay";
}
