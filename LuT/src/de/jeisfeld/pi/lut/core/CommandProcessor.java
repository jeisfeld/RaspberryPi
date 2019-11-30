package de.jeisfeld.pi.lut.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jeisfeld.pi.lut.core.command.AnalogRead;
import de.jeisfeld.pi.lut.core.command.Command;
import de.jeisfeld.pi.lut.core.command.DigitalRead;
import de.jeisfeld.pi.lut.core.command.ReadCommand;
import de.jeisfeld.pi.lut.core.command.WriteCommand;

/**
 * Processor for commands to LuT.
 */
public class CommandProcessor {
	/**
	 * The Pattern used for processing the response.
	 */
	private static final Pattern RESPONSE_PATTERN = Pattern.compile("^(.*?)(OK|FAILED)(.*)$", Pattern.DOTALL);
	/**
	 * A list with both read commands.
	 */
	private static final List<Command> ALL_READ_COMMADS = Arrays.asList(new Command[] {new AnalogRead(), new DigitalRead()});

	/**
	 * The sender used.
	 */
	private final Sender mSender;
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
	 * Flag indicating if the processor is closed.
	 */
	private boolean mIsClosed = false;
	/**
	 * Flag indicating if the thread is running.
	 */
	private boolean mIsThreadRunning;

	/**
	 * Create a command processor.
	 *
	 * @param sender The sender used.
	 */
	public CommandProcessor(final Sender sender) {
		mSender = sender;

		try {
			// Do initial read, so that ButtonStatus is initialized on first read.
			doProcessCommands(CommandProcessor.ALL_READ_COMMADS);
		}
		catch (IOException e) {
			// ignore
		}

		new ProcessingThread().start();
	}

	/**
	 * Close this command processor.
	 *
	 * @param commands The final commands to be executed
	 */
	protected void close(final List<WriteCommand> commands) {
		synchronized (mQueuedCommands) {
			mQueuedCommands.clear();
			mQueuedCommands.addAll(commands);
		}
		mIsClosed = true;
	}

	/**
	 * Process a list of commands.
	 *
	 * @param commands The commands to be processed.
	 * @return The result.
	 * @throws IOException issues with connection.
	 */
	private ButtonStatus doProcessCommands(final List<Command> commands) throws IOException {
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
				mSender.write(command.getSerialString());
			}

			ButtonStatus buttonStatus = new ButtonStatus();

			int missingResponses = commands.size();
			do {
				String input = mSender.read();
				while (input != null) {
					Matcher matcher = CommandProcessor.RESPONSE_PATTERN.matcher(input);
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
			return buttonStatus;
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
	 * Get the button status.
	 *
	 * @return the button status.
	 */
	protected ButtonStatus getButtonStatus() {
		return mButtonStatus;
	}

	/**
	 * Get information if the processing thread is running.
	 *
	 * @return True if the processing thread is running.
	 */
	protected boolean isThreadRunning() {
		return mIsThreadRunning;
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
			while (!mIsClosed || mQueuedCommands.size() > 0) {
				try {
					List<Command> commandsForProcessing = new ArrayList<>();

					synchronized (mQueuedCommands) {
						if (mQueuedCommands.size() == 0) {
							commandsForProcessing = CommandProcessor.ALL_READ_COMMADS;
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
			synchronized (mSender) {
				mIsThreadRunning = false;
				mSender.notifyAll();
			}
		}
	}

}
