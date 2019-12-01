package de.jeisfeld.pi.lut.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.pi4j.io.serial.impl.SerialImpl;

import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.command.AnalogRead;
import de.jeisfeld.pi.lut.core.command.Command;
import de.jeisfeld.pi.lut.core.command.DigitalRead;
import de.jeisfeld.pi.lut.core.command.Lob;
import de.jeisfeld.pi.lut.core.command.ReadCommand;
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
	 * The Pattern used for processing the response.
	 */
	private static final Pattern RESPONSE_PATTERN = Pattern.compile("^(.*?)(OK|FAILED)(.*)$", Pattern.DOTALL);
	/**
	 * A list with both read commands.
	 */
	private static final List<Command> ALL_READ_COMMADS = Arrays.asList(new Command[] {new AnalogRead(), new DigitalRead()});

	/**
	 * The serial port used for sending.
	 */
	private final Serial mSerial = SerialFactory.createInstance();
	/**
	 * A list of channel senders that has been created.
	 */
	private final Map<Integer, ChannelSender> mChannelSenders = new HashMap<>();
	/**
	 * Flag indicating if the processing thread is closing.
	 */
	private boolean mIsClosing = false;
	/**
	 * Flag indicating if the sender is closed.
	 */
	private boolean mIsClosed = false;
	/**
	 * Flag indicating if the thread is running.
	 */
	private boolean mIsThreadRunning;
	/**
	 * The last button status.
	 */
	private final ButtonStatus mButtonStatus = new ButtonStatus();
	/**
	 * The list of commands currently in process.
	 */
	private final List<Command> mProcessingCommands = new ArrayList<>();
	/**
	 * The list of commands waiting to be processed.
	 */
	private final List<WriteCommand> mQueuedCommands = new ArrayList<>();

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
				if (!mIsClosed) {
					try {
						close();
					}
					catch (IllegalStateException | IOException | InterruptedException e) {
						// do nothing
					}
				}
			}
		});

		try {
			// Do initial read, so that ButtonStatus is initialized on first read.
			doProcessCommands(Sender.ALL_READ_COMMADS);
		}
		catch (IOException e) {
			// ignore
		}

		new ProcessingThread().start();
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
			finalCommands.add(new Tadel(channel, 0, 0, 0));
			finalCommands.add(new Lob(channel, 0));
		}

		synchronized (mQueuedCommands) {
			mQueuedCommands.clear();
			mQueuedCommands.addAll(finalCommands);
		}
		mIsClosing = true;

		synchronized (this) {
			while (mIsThreadRunning) {
				wait();
			}
		}
		synchronized (mSerial) {
			mIsClosed = true;
			if (!mSerial.isClosed()) {
				mSerial.close();
			}
		}
		// remove serial port listener
		if (mSerial instanceof SerialImpl) {
			((SerialImpl) mSerial).removeSerialListener();
		}

		// perform shutdown of any monitoring threads
		SerialFactory.shutdown();
	}

	/**
	 * Process a list of commands.
	 *
	 * @param commands The commands to be processed.
	 * @throws IOException issues with connection.
	 */
	private void doProcessCommands(final List<Command> commands) throws IOException {
		synchronized (mProcessingCommands) {
			if (commands.size() > 2) {
				throw new RuntimeException("Max number of parallel commands is " + 2);
			}
			if (commands.size() == 2 && commands.get(1) instanceof WriteCommand) {
				throw new RuntimeException("Second command may be only read command");
			}

			for (Command command : commands) {
				mProcessingCommands.add(command);
			}

			for (Command command : commands) {
				write(command.getSerialString());
			}

			ButtonStatus buttonStatus = new ButtonStatus();

			int missingResponses = commands.size();
			do {
				String input = read();
				while (input != null) {
					Matcher matcher = Sender.RESPONSE_PATTERN.matcher(input);
					if (!matcher.find()) {
						break;
					}
					missingResponses--;
					String response = matcher.group(1);
					input = matcher.group(3); // MAGIC_NUMBER

					for (Command command : commands) {
						if (command instanceof ReadCommand) {
							((ReadCommand) command).processResponse(response, buttonStatus);
						}
					}

				}
			}
			while (missingResponses > 0);
			mButtonStatus.updateWith(buttonStatus);

			mProcessingCommands.clear();
		}
	}

	/**
	 * Put commands in the command queue. If they query for response, then wait for the response.
	 *
	 * @param doOverride flag indicating if commands on same channel should be overridden.
	 * @param commands The commands.
	 */
	protected void processCommands(final boolean doOverride, final WriteCommand... commands) {
		if (mIsClosed) {
			return;
		}
		synchronized (mQueuedCommands) {
			for (WriteCommand command : commands) {
				boolean isOverride = false;
				if (doOverride) {
					for (WriteCommand other : mQueuedCommands) {
						if (command.overrides(other)) {
							mQueuedCommands.set(mQueuedCommands.indexOf(other), command);
							isOverride = true;
						}
					}
				}
				if (!isOverride) {
					mQueuedCommands.add(command);
				}
			}
		}
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
	 * Write to the sender, in repeated way.
	 *
	 * @param duration The duration. If duration is at least Sender.SEND_DURATION, then it is ensured that each command will be processed, otherwise
	 *            they may be suppressed.
	 * @param commands the commands
	 */
	public void processCommands(final long duration, final WriteCommand... commands) {
		if (mIsClosing) {
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
		return mButtonStatus;
	}

	/**
	 * Set listener for button 1.
	 *
	 * @param listener The listener.
	 */
	public void setButton1Listener(final ButtonListener listener) {
		mButtonStatus.setButton1Listener(listener);
	}

	/**
	 * Set listener for button 2.
	 *
	 * @param listener The listener.
	 */
	public void setButton2Listener(final ButtonListener listener) {
		mButtonStatus.setButton2Listener(listener);
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

	/**
	 * The thread processing the command queue.
	 */
	private class ProcessingThread extends Thread {
		/**
		 * Storage for the next read command that might be processed.
		 */
		private ReadCommand mNextReadCommand = new AnalogRead();

		@Override
		public void run() {
			mIsThreadRunning = true;
			while (!mIsClosing || mQueuedCommands.size() > 0) {
				try {
					List<Command> commandsForProcessing = new ArrayList<>();

					synchronized (mQueuedCommands) {
						if (mQueuedCommands.size() == 0) {
							commandsForProcessing = Sender.ALL_READ_COMMADS;
						}
						else {
							commandsForProcessing.add(mQueuedCommands.get(0));
							mQueuedCommands.remove(0);
							commandsForProcessing.add(mNextReadCommand);
							mNextReadCommand = mNextReadCommand instanceof AnalogRead ? new DigitalRead() : new AnalogRead();
						}

					}
					doProcessCommands(commandsForProcessing);
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			synchronized (Sender.this) {
				mIsThreadRunning = false;
				Sender.this.notifyAll();
			}
		}
	}

}
