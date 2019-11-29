package de.jeisfeld.pi.lut;

import java.io.IOException;

import com.pi4j.io.gpio.GpioFactory;
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
	 * The singleton instance of this class.
	 */
	private static Sender mInstance = null;
	/**
	 * The default serial port used.
	 */
	private static final String DEFAULT_PORT = "/dev/serial0";
	/**
	 * The serial port used for sending.
	 */
	private final Serial mSerial = SerialFactory.createInstance();

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
		GpioFactory.getInstance().shutdown();
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
		if (mSerial.isClosed()) {
			return;
		}
		boolean done = false;
		final long startTime = System.currentTimeMillis();
		mSerial.write(message + "\r");
		while (!done) {
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
		if (mSerial.isClosed()) {
			return null;
		}

		boolean digitalResultRetrieved = false;
		boolean analogResultRetrieved = false;
		ButtonStatus result = new ButtonStatus();
		do {
			if (!digitalResultRetrieved) {
				mSerial.write("S\r");
			}
			if (!analogResultRetrieved) {
				mSerial.write("A\r");
			}
			String input = new String(mSerial.read());
			if (input == null || input.length() < 2) {
				input = new String(mSerial.read());
			}
			digitalResultRetrieved = digitalResultRetrieved || result.setDigitalResult(input);
			analogResultRetrieved = analogResultRetrieved || result.setAnalogResult(input);
		}
		while (!digitalResultRetrieved || !analogResultRetrieved && !mSerial.isClosed());

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
