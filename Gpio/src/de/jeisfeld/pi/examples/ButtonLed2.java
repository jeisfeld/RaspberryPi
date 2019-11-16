package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.DigitalOutput;

/**
 * An LED that can be switched between multiple blinking modes.
 */
public class ButtonLed2 {
	/**
	 * The configuration of the blinking modes.
	 */
	public static final double[][] MODES =
			{{0}, {1}, {1, 0.2, 0.2}, {1, 0.2, 0.8}, {0, 1, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 1, 0.3, 1, 0.3, 1, 0.3, 0.2, 0.2, 0.2, 0.2, 0.2, 1}};

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new ButtonLed2().run();
	}

	/**
	 * Run the multi-mode LED.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final DigitalOutput led = new DigitalOutput(RaspiPin.GPIO_01, true);
		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_05, false);

		final Status status = new Status();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				synchronized (status) {
					status.mMode = -1;
					status.notifyAll();
				}
			}
		});

		button.addListener(new DigitalInput.OnStableStateChangeListener() {
			@Override
			public void handleTriggerOn() {
				status.nextMode();
				synchronized (status) {
					status.notifyAll();
				}
			}

			@Override
			public void handleTriggerOff() {
				// do nothing
			}
		});

		while (status.mMode >= 0) {
			synchronized (status) {
				int currentMode = status.mMode;
				if (MODES[currentMode].length < 2) {
					status.wait();
				}
				else {
					long currentWaitingTime = (long) (MODES[currentMode][status.mPhase + 1] * 1000); // MAGIC_NUMBER
					long remainingTime = currentWaitingTime - (System.currentTimeMillis() - status.mLedTime);
					if (remainingTime > 0) {
						status.wait(remainingTime);
					}
				}
				if (status.mMode < 0) {
					return;
				}

				status.nextPhase(status.mMode != currentMode);

				led.setValue(status.mLedState);
			}
		}
	}

	/**
	 * Helper class storing the status of the blinking mode.
	 */
	public static class Status {
		/**
		 * The current blinking mode.
		 */
		private int mMode = 0;
		/**
		 * The current phase in the blinking mode.
		 */
		private int mPhase = 0;
		/**
		 * The current LED state.
		 */
		private boolean mLedState = false;
		/**
		 * The last LED change time.
		 */
		private long mLedTime = System.currentTimeMillis();

		/**
		 * Switch to the next LED mode.
		 */
		public void nextMode() {
			mMode = (mMode + 1) % MODES.length;
			// SYSTEMOUT:OFF
			System.out.println("LED Mode: " + mMode);
			// SYSTEMOUT:ON
		}

		/**
		 * Switch do the next LED phase.
		 *
		 * @param changedMode true if the mode should be changed.
		 */
		public void nextPhase(final boolean changedMode) {
			mPhase = changedMode ? 0 : (mPhase + 1) % (MODES[mMode].length - 1);
			mLedState = mPhase == 0 ? MODES[mMode][0] > 0 : !mLedState;
			mLedTime = System.currentTimeMillis();
		}
	}

}
