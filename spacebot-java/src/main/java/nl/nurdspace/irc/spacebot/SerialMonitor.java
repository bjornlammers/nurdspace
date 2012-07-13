package nl.nurdspace.irc.spacebot;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A monitor that catches events from the serial input and updates the status.
 * @author bjornl
 *
 */
public class SerialMonitor implements SerialPortEventListener {
	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(SerialMonitor.class);
	
	/** The ports that are searched for. */
	private static final String PORT_NAMES[] = { 
		"/dev/tty.usbserial-A4001nQr", // Mac OS X
		"/dev/tty.usbserial-A4001nK8", // Mac OS X
		"/dev/ttyUSB0", // Linux
		"/dev/ttyUSB1", // Linux
			"COM3" // Windows
	};
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	/** The serialport we use. */
	private SerialPort serialPort;

	/** Buffered input stream from the port. */
	private InputStream input;

	/** The input buffer for the serial port. */
	private String serialBuffer;
	
	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements() && portId == null) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			LOG.debug("Current comm port: " + currPortId.getName());
			for (int i = 0; i < PORT_NAMES.length; i++) {
				LOG.debug(PORT_NAMES[i]);
				if (currPortId.getName().equals(PORT_NAMES[i])) {
					LOG.info("Found comm port: " + PORT_NAMES[i]);
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			LOG.warn("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and forward it to the handler.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			LOG.debug("Data available");
			byte[] chunk;
			try {
				int available = input.available();
				chunk = new byte[available];
				input.read(chunk, 0, available);

				// Displayed results are codepage dependent
				serialBuffer += new String(chunk);
				LOG.debug("Buffer: " + this.serialBuffer.replaceAll("\n", "[LF]").replaceAll("\r", "[CR]"));
				int firstNewline = serialBuffer.indexOf('\n');
				LOG.trace("First newline at: " + firstNewline);
				if (firstNewline >= 0) {
					int secondNewline;
					while ((secondNewline = serialBuffer.indexOf('\n', firstNewline + 10)) > 0) {
						String message = serialBuffer.substring(firstNewline + 1, secondNewline);
						LOG.debug("Message received: " + message);
						this.serialBuffer = serialBuffer.substring(secondNewline);
						LOG.debug("Buffer: " + this.serialBuffer.replaceAll("\n", "[LF]").replaceAll("\r", "[CR]"));
						this.handleMessage(message);
						firstNewline = serialBuffer.indexOf('\n');
					}
				}
			} catch (IOException e) {
				LOG.error("caught exception while reading input", e);
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	private void handleMessage(String message) {
		LOG.debug("Serial message: " + message);
		if (message.startsWith("LDR value:")) {
			int value = Integer.parseInt(message.substring(11).trim());
			LOG.trace("*** LDR reading: " + value);
			SpaceStatus.getInstance().setFluorescentLighting(value);
		} else if (message.startsWith("Temp:")) {
			SpaceStatus.getInstance().setTemperature(Float.parseFloat(message.substring(6).trim()));
		} else if (message.startsWith("Lock0 open:")) {
			SpaceStatus.getInstance().setBackDoorLocked(Integer.parseInt(message.substring(12).trim()) == 0);
		}
	}
}
