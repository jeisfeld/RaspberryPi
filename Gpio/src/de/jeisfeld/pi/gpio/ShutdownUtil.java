package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

/**
 * Utility to support cleanup of PINs on shutdown.
 */
public final class ShutdownUtil {
	/**
	 * Flag indicating if shutdown is triggered.
	 */
	private static boolean mIsShutdown = false;
	/**
	 * Flag indicating of shutdown handler is prepared.
	 */
	private static boolean mIsShutdownHandlerPrepared = false;

	/**
	 * Dummy constructor to prevent public access.
	 */
	private ShutdownUtil() {
	}

	/**
	 * Prepare the shutdown for certain PINs.
	 *
	 * @param pins The PINs.
	 */
	public static synchronized void prepareShutdown(final GpioPin... pins) {
		if (!ShutdownUtil.mIsShutdownHandlerPrepared) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					ShutdownUtil.mIsShutdown = true;
					GpioFactory.getInstance().shutdown();
				}
			});
			ShutdownUtil.mIsShutdownHandlerPrepared = true;
		}
		for (GpioPin pin : pins) {
			pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN, PinMode.DIGITAL_INPUT);
		}
	}

	/**
	 * Get flag indicating if shutdown is triggered.
	 *
	 * @return The flag indicating if shutdown is triggered.
	 */
	public static boolean isShutdown() {
		return ShutdownUtil.mIsShutdown;
	}

}
