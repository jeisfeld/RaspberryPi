package de.jeisfeld.pi.util;

/**
 * Helper class for logging.
 */
public final class Logger {
	/**
	 * Flag indicating if details should be logged.
	 */
	private static boolean mLogDetails = false;

	/**
	 * Indicate if details should be logged.
	 *
	 * @param logDetails Flag indicating if details should be logged.
	 */
	public static void setLogDetails(final boolean logDetails) {
		Logger.mLogDetails = logDetails;
	}

	// SYSTEMOUT:OFF
	/**
	 * Hide the default constructor.
	 */
	private Logger() {
	}

	/**
	 * Log an exception.
	 *
	 * @param e The exception
	 */
	public static void error(final Exception e) {
		if (Logger.mLogDetails) {
			e.printStackTrace();
		}
		else {
			System.err.println(e.toString());
		}
	}

	/**
	 * Log a message.
	 *
	 * @param message the message
	 */
	public static void info(final String message) {
		if (Logger.mLogDetails) {
			System.out.println(message);
		}
	}

	/**
	 * Log a message - can be used temporarily during debugging phase.
	 *
	 * @param message the message
	 */
	public static void log(final String message) {
		System.out.println(message);
	}
}
