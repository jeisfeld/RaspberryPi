package de.jeisfeld.pi.lut;

import java.io.IOException;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

/**
 * The generic sender for LuT devices.
 */
public final class Sender {
	/**
	 * The serial port used for sending.
	 */
	private final Serial mSerial = SerialFactory.createInstance();
	/**
	 * The singleton instance of this class.
	 */
	private static Sender mInstance = null;
	/**
	 * The default serial port used.
	 */
	private static final String DEFAULT_PORT = "/dev/serial0";

	/**
	 * Constructor for default port.
	 *
	 * @throws IOException issues with connection
	 */
	private Sender() throws IOException {
		this(Sender.DEFAULT_PORT);
	}

	/**
	 * Constructor.
	 *
	 * @param port The port of the serial device
	 * @throws IOException issues with connection
	 */
	private Sender(final String port) throws IOException {
		final SerialConfig config = new SerialConfig();
		config.device(port)
				.baud(Baud._9600)
				.dataBits(DataBits._8)
				.parity(Parity.NONE)
				.stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

		mSerial.open(config);
	}

	/**
	 * Get a sender as singleton.
	 *
	 * @return The sender.
	 * @throws IOException issues with connection
	 */
	public static synchronized Sender getInstance() throws IOException {
		if (Sender.mInstance == null) {
			Sender.mInstance = new Sender();
		}
		return Sender.mInstance;
	}

	/**
	 * Close the sender.
	 *
	 * @throws IOException issues with connection
	 */
	public void close() throws IOException {
		mSerial.discardAll();
		mSerial.close();
	}

	/**
	 * Write to the sender, in repeated way.
	 *
	 * @param message The message to be written.
	 * @param duration The total duration of sending. Message will be repeated every second.
	 * @throws IOException issues with connection
	 * @throws InterruptedException when interrupted
	 */
	public void write(final String message, final long duration) throws IOException, InterruptedException {
		boolean done = false;
		final long startTime = System.currentTimeMillis();
		mSerial.write(message + "\r");
		while (!done) {
			String result;
			do {
				result = new String(mSerial.read());
			}
			while (result == null || !result.contains("OK"));

			final long remainingTime = duration - (System.currentTimeMillis() - startTime);
			if (remainingTime < 2000) { // MAGIC_NUMBER
				if (remainingTime > 0) {
					Thread.sleep(remainingTime);
				}
				done = true;
			}
			else {
				Thread.sleep(1000); // MAGIC_NUMBER
				mSerial.write("\r");
			}
		}
	}

	/**
	 * Read the inputs of the eWeb.
	 *
	 * @return The status of the inputs.
	 * @throws IOException issues with connection
	 */
	public ButtonStatus readInputs() throws IOException {
		ButtonStatus result = new ButtonStatus();

		mSerial.write("S\r");
		String digitalResult;
		do {
			digitalResult = new String(mSerial.read());
		}
		while (digitalResult == null || !digitalResult.startsWith("S"));
		result.setDigitalResult(digitalResult);

		mSerial.write("A\r");
		String analogResult;
		do {
			analogResult = new String(mSerial.read());
		}
		while (analogResult == null || !analogResult.startsWith("A"));
		result.setAnalogResult(analogResult);

		return result;
	}

	/**
	 * Get a channel sender for a channel.
	 *
	 * @param channel The channel.
	 * @return The channel sender.
	 * @throws IOException issues with connection
	 */
	public ChannelSender getChannelSender(final int channel) throws IOException {
		return new ChannelSender(this, channel);
	}

}
