package de.jeisfeld.pi.gpio;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * Management of PCF8591 Analog/Digital Wandler.
 */
public class Pcf8591 {
	/**
	 * The address of PCF8591.
	 */
	private static final int PCF8591_ADDR = 0x48;
	/**
	 * The command for writing to PCF8591.
	 */
	private static final int PCF8591_CMD_WRITE = 0x40;
	/**
	 * The command for writing to PCF8591 channels 0-3.
	 */
	private static final int[] PCF8591_CMD_READ = {0x40, 0x41, 0x42, 0x43};
	/**
	 * The Channel 0.
	 */
	public static final byte CHANNEL_0 = 0;
	/**
	 * The Channel 1.
	 */
	public static final byte CHANNEL_1 = 1;
	/**
	 * The Channel 2.
	 */
	public static final byte CHANNEL_2 = 2;
	/**
	 * The Channel 3.
	 */
	public static final byte CHANNEL_3 = 3;
	/**
	 * The divisor required to transform between byte and double values.
	 */
	private static final double DIVISOR = 255.0;
	/**
	 * The PCF8591 device.
	 */
	private final I2CDevice mDevice;
	/**
	 * The used channel.
	 */
	private final Byte mChannel;
	/**
	 * The minimum positive value to be used. Smaller values will be interpreted as 0.
	 */
	private int mMinValue = 1;

	/**
	 * Initialize the PCF8591 Analog/Digital Wandler.
	 *
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public Pcf8591() throws UnsupportedBusNumberException, IOException {
		this(1);
	}

	/**
	 * Initialize the PCF8591 Analog/Digital Wandler.
	 *
	 * @param minValue The minimum positive value to be used. Smaller values will be interpreted as 0.
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public Pcf8591(final int minValue) throws UnsupportedBusNumberException, IOException {
		this(Pcf8591.CHANNEL_0, minValue);
	}

	/**
	 * Initialize the PCF8591 Analog/Digital Wandler for a fixed input channel.
	 *
	 * @param inputChannel the used input channel.
	 * @param minValue The minimum positive value to be used. Smaller values will be interpreted as 0.
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public Pcf8591(final byte inputChannel, final int minValue) throws UnsupportedBusNumberException, IOException {
		final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		mDevice = bus.getDevice(Pcf8591.PCF8591_ADDR);
		mChannel = inputChannel;
		mMinValue = minValue;
	}

	/**
	 * Read a value from the device.
	 *
	 * @param channel The channel where ti read.
	 * @return the read value in range 0...1
	 * @throws IOException issuew when reading
	 */
	public double read(final byte channel) throws IOException {
		return readByte(channel) / Pcf8591.DIVISOR;
	}

	/**
	 * Read a byte from the default channel of the device.
	 *
	 * @return the read value in range 0...1
	 * @throws IOException issuew when reading
	 */
	public double read() throws IOException {
		return read(mChannel);
	}

	/**
	 * Read a byte from the device.
	 *
	 * @param channel The channel where ti read.
	 * @return the read byte
	 * @throws IOException issuew when reading
	 */
	public int readByte(final byte channel) throws IOException {
		mDevice.read(Pcf8591.PCF8591_CMD_READ[channel]);
		final int value = mDevice.read();
		return value < mMinValue ? 0 : value;
	}

	/**
	 * Read a byte from the default channel of the device.
	 *
	 * @return the read byte
	 * @throws IOException issuew when reading
	 */
	public int readByte() throws IOException {
		return readByte(mChannel);
	}

	/**
	 * Write a byte to the device.
	 *
	 * @param request The byte to be written.
	 * @throws IOException issues when writing
	 */
	public void write(final byte request) throws IOException {
		mDevice.write(Pcf8591.PCF8591_CMD_WRITE, request);
	}
}
