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
		"/dev/ttyUSB2", // Linux
		"/dev/ttyUSB3", // Linux
			"COM3" // Windows
	};
	
	private Integer ledPanelPortIndex = null;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	/** The serialport we use. */
	private SerialPort[] serialPorts = new SerialPort[2];

	/** Buffered input stream from the port. */
	private InputStream[] inputs = new InputStream[2];

	/** The input buffer for the serial port. */
	private String[] serialBuffers = new String[2];
	
	public void initialize() {
		//CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		int serialIndex = 0;
		
		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			LOG.info("Current comm port: " + currPortId.getName());
			for (int i = 0; i < PORT_NAMES.length; i++) {
				LOG.debug(PORT_NAMES[i]);
				if (currPortId.getName().equals(PORT_NAMES[i])) {
					LOG.info("Found comm port: " + PORT_NAMES[i]);
					try {
						// open serial port, and use class name for the appName.
						serialPorts[serialIndex] = (SerialPort) currPortId.open(this.getClass().getName(),
								TIME_OUT);

						// set port parameters
						serialPorts[serialIndex].setSerialPortParams(DATA_RATE,
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);

						// open the streams
						inputs[serialIndex] = serialPorts[serialIndex].getInputStream();

						// add event listeners
						serialPorts[serialIndex].addEventListener(this);
						serialPorts[serialIndex].notifyOnDataAvailable(true);
						serialIndex++;
					} catch (Exception e) {
						System.err.println(e.toString());
					}
//					portId = currPortId;
//					break;
				}
			}
		}

		if (serialPorts[0] == null) {
			LOG.warn("Could not find any COM port.");
			return;
		}

	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		for (int i = 0; i < serialPorts.length; i++) {
			if (serialPorts[i] != null) {
				serialPorts[i].removeEventListener();
				serialPorts[i].close();
			}
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and forward it to the handler.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			LOG.debug("Data available");
			byte[] chunk;
			for (int i = 0; i < serialPorts.length; i++) {
				if (inputs[i] != null) {
					try {
						int available = inputs[i].available();
						chunk = new byte[available];
						inputs[i].read(chunk, 0, available);

						// Displayed results are codepage dependent
						serialBuffers[i] += new String(chunk);
						LOG.debug("Buffer: " + this.serialBuffers[i].replaceAll("\n", "[LF]").replaceAll("\r", "[CR]"));
						int firstNewline = serialBuffers[i].indexOf('\n');
						LOG.trace("First newline at: " + firstNewline);
						if (firstNewline >= 0) {
							int secondNewline;
							while ((secondNewline = serialBuffers[i].indexOf('\n', firstNewline + 10)) > 0) {
								String message = serialBuffers[i].substring(firstNewline + 1, secondNewline);
								LOG.debug("Message received: " + message);
								this.serialBuffers[i] = serialBuffers[i].substring(secondNewline);
								LOG.debug("Buffer: " + this.serialBuffers[i].replaceAll("\n", "[LF]").replaceAll("\r", "[CR]"));
								this.handleMessage(i, message);
								firstNewline = serialBuffers[i].indexOf('\n');
							}
						}
					} catch (IOException e) {
						LOG.error("caught exception while reading input", e);
					}
				}
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public void sendToLedPanel(final String message) {
		if (ledPanelPortIndex != null) {
			try {
				serialPorts[ledPanelPortIndex].getOutputStream().write(message.getBytes());
				serialPorts[ledPanelPortIndex].getOutputStream().flush();
			} catch (IOException e) {
				LOG.error("Kon niet naar LED panel schrijven", e);
			}
		} else {
			LOG.warn("LED panel not yet identified");
		}
	}
	
	private void handleMessage(final int serialIndex, final String message) {
		LOG.info("Serial message: " + message);
		if (message.startsWith("LEDPANEL")) {
			LOG.info("LED panel identified itself! index = " + serialIndex);
			ledPanelPortIndex = serialIndex;
		}
		if (message.startsWith("LDR value:")) {
			int value = Integer.parseInt(message.substring(11).trim());
			LOG.trace("*** LDR reading: " + value);
			SpaceStatus.getInstance().setFluorescentLighting(value);
		} else if (message.startsWith("Temp:")) {
			SpaceStatus.getInstance().setTemperature(Float.parseFloat(message.substring(6).trim()));
		} else if (message.startsWith("Lock0: open:")) {
			SpaceStatus.getInstance().setBackDoorLocked(Integer.parseInt(message.substring(13).trim()) == 0);
		} else if (message.startsWith("Lock1: open:")) {
			SpaceStatus.getInstance().setFrontDoorLocked(Integer.parseInt(message.substring(13).trim()) == 0);
		}
	}
}
