package de.jeisfeld.pi.lut.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import de.jeisfeld.pi.lut.core.command.Lob;
import de.jeisfeld.pi.lut.core.command.Tadel;
import de.jeisfeld.pi.lut.core.command.WriteCommand;

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
	 * The duration of a send command in ms.
	 */
	public static final int SEND_DURATION = 200;

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
	 * The command processor used by this class.
	 */
	private final CommandProcessor mCommandProcessor;

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
		mCommandProcessor = new CommandProcessor(this);

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
	 * Close the sender and the command processor.
	 *
	 * @throws IOException issues with connection
	 * @throws InterruptedException when interrupted
	 */
	public void close() throws IOException, InterruptedException {
		List<WriteCommand> finalCommands = new ArrayList<>();
		for (Integer channel : mChannelSenders.keySet()) {
			finalCommands.add(new Lob(channel, 0));
			finalCommands.add(new Tadel(channel, 0, 0, 0));
		}
		mCommandProcessor.close(finalCommands);
	}

	/**
	 * Close the sender.
	 *
	 * @throws IOException issues with connection
	 * @throws InterruptedException when interrupted
	 */
	protected void doClose() throws IOException, InterruptedException {
		synchronized (mSerial) {
			mIsClosed = true;
			mSerial.close();
		}

		GpioFactory.getInstance().shutdown();
	}

	/**
	 * Put commands in the command queue. If they query for response, then wait for the response.
	 *
	 * @param commands The commands.
	 */
	public void processCommands(final WriteCommand... commands) {
		processCommands(true, commands);
	}

	/**
	 * Put commands in the command queue. If they query for response, then wait for the response.
	 *
	 * @param doOverride flag indicating if commands on same channel should be overridden.
	 * @param commands The commands.
	 */
	public void processCommands(final boolean doOverride, final WriteCommand... commands) {
		mCommandProcessor.processCommands(doOverride, commands);
	}

	/**
	 * Write to the sender, in repeated way.
	 *
	 * @param duration The duration. If duration is at least Sender.SEND_DURATION, then it is ensured that each command will be processed, otherwise
	 *            they may be suppressed.
	 * @param commands the commands
	 */
	public void processCommands(final long duration, final WriteCommand... commands) {
		if (mIsClosed) {
			return;
		}
		boolean doOverride = duration < Sender.SEND_DURATION * commands.length;

		boolean done = false;
		final long startTime = System.currentTimeMillis();
		processCommands(doOverride, commands);
		while (!done) {
			final long remainingTime = duration - (System.currentTimeMillis() - startTime);
			if (remainingTime < 2000) { // MAGIC_NUMBER
				if (remainingTime > 0) {
					try {
						Thread.sleep(remainingTime);
					}
					catch (InterruptedException e) {
						// do nothing
					}
				}
				done = true;
			}
			else {
				try {
					Thread.sleep(1000); // MAGIC_NUMBER
				}
				catch (InterruptedException e) {
					// do nothing
				}
				processCommands(doOverride, commands);
			}
		}
	}

	/**
	 * Get the button status.
	 *
	 * @return The button status.
	 */
	public ButtonStatus getButtonStatus() {
		return mCommandProcessor.getButtonStatus();
	}

	/**
	 * Write a message.
	 *
	 * @param message The message
	 * @throws IOException issues with connection
	 */
	protected void write(final String message) throws IOException {
		synchronized (mSerial) {
			if (!mIsClosed) {
				mSerial.write(message + "\r");
			}
		}
	}

	/**
	 * Read a message.
	 *
	 * @return the read message.
	 * @throws IOException issues with connection
	 */
	protected String read() throws IOException {
		synchronized (mSerial) {
			if (!mIsClosed) {
				return new String(mSerial.read());
			}
		}
		return null;
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

}
