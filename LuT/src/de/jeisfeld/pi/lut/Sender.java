package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	 * A list of channel senders that has been created.
	 */
	private final Map<Integer, ChannelSender> mChannelSenders = new HashMap<>();
	/**
	 * Flag indicating if the sender is closed.
	 */
	private boolean mIsClosed = false;

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

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					close();
				}
				catch (IllegalStateException | IOException | InterruptedException e) {
					// do nothing
				}
			}
		});
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
	 * @throws InterruptedException when interrupted
	 */
	public void close() throws IOException, InterruptedException {
		mIsClosed = true;
		if (!mSerial.isClosed()) {
			for (ChannelSender channelSender : mChannelSenders.values()) {
				channelSender.lobOff();
				channelSender.tadelOff();
			}
			synchronized (mSerial) {
				mSerial.close();
			}
		}
		GpioFactory.getInstance().shutdown();
	}

	/**
	 * Write a message.
	 *
	 * @param message The message
	 * @throws IOException issues with connection
	 */
	private void write(final String message) throws IOException {
		synchronized (mSerial) {
			if (!mIsClosed) {
				mSerial.write(message + "\r");
			}
		}
	}

	/**
	 * Write a message in shutdown sequence.
	 *
	 * @param message The message
	 * @throws IOException issues with connection
	 */
	protected void writeFinal(final String message) throws IOException {
		mSerial.write(message + "\r");
	}

	/**
	 * Read a message.
	 *
	 * @return the read message.
	 * @throws IOException issues with connection
	 */
	private String read() throws IOException {
		synchronized (mSerial) {
			if (!mIsClosed) {
				return new String(mSerial.read());
			}
		}
		return null;
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
		if (mIsClosed) {
			Thread.sleep(duration);
		}
		boolean done = false;
		final long startTime = System.currentTimeMillis();
		write(message);
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
				write("");
			}
		}
	}

	/**
	 * Read the inputs of the eWeb.
	 *
	 * @param readType Restrict the inputs which should be read.
	 * @return The status of the inputs.
	 * @throws IOException issues with connection
	 */
	public ButtonStatus readInputs(final ReadType readType) throws IOException {
		if (mIsClosed) {
			return null;
		}

		boolean digitalResultRequired = readType.isDigitalRequired();
		boolean analogResultRequired = readType.isAnalogRequired();
		ButtonStatus result = new ButtonStatus();
		do {
			if (digitalResultRequired) {
				write("S");
			}
			if (analogResultRequired) {
				write("A");
			}
			String input = read();
			if (input == null || input.length() < 2) {
				input = read();
			}
			digitalResultRequired = digitalResultRequired && !result.setDigitalResult(input);
			analogResultRequired = analogResultRequired && !result.setAnalogResult(input);
		}
		while (digitalResultRequired || analogResultRequired && !mSerial.isClosed());

		return result;
	}

	/**
	 * Get a channel sender for a channel.
	 *
	 * @param channel The channel.
	 * @return The channel sender.
	 * @throws IOException issues with connection
	 */
	public ChannelSender getChannelSender(final Integer channel) throws IOException {
		ChannelSender result = mChannelSenders.get(channel);
		if (result == null) {
			result = new ChannelSender(this, channel);
			mChannelSenders.put(channel, result);
		}
		return result;
	}

	/**
	 * Specify the data inputs to be read.
	 */
	public enum ReadType {
		/**
		 * Only digital inputs.
		 */
		DIGITAL,
		/**
		 * Only analog inputs.
		 */
		ANALOG,
		/**
		 * All inputs.
		 */
		ALL;

		/**
		 * Check if digital inputs should be read.
		 *
		 * @return True if digital inputs should be read.
		 */
		public boolean isDigitalRequired() {
			return this == DIGITAL || this == ALL;
		}

		/**
		 * Check if analog inputs should be read.
		 *
		 * @return True if analog inputs should be read.
		 */
		public boolean isAnalogRequired() {
			return this == ANALOG || this == ALL;
		}

	}

}
