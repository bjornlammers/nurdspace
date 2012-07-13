package nl.nurdspace.irc.spacebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the dimmer pack.
 * 
 * @author bjornl
 * 
 */
public class Dimmer {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(Dimmer.class);
	/** The IP (or hostname) of the dimmer. */
	private String host;

	private boolean connected;
	private Socket requestSocket;
	private PrintWriter out;
	private BufferedReader in;

	public boolean setHost(String host) {
		if (connected) {
			disconnect();
		}
		try {
			InetAddress.getByName(host);
			this.host = host;
			connected = connect();
		} catch (UnknownHostException e) {
			connected = false;
			LOG.error("setHost: invalid host '" + host + "'", e);
		} catch (IOException e) {
			connected = false;
			LOG.error("setHost: error", e);
		}
		return connected;
	}

	public void fade(int channel, int target) {
		float fraction = ((float) target) / 100f;
		int requiredLevel = Math.round(255f * fraction);
		fadeAbsolute(channel, requiredLevel);
	}

	public void fadeIn(int channel) {
		fadeAbsolute(channel, 255);
	}

	public void fadeOut(int channel) {
		fadeOut(channel, 0);
	}

	public void fadeOut(int channel, int delay) {
		try {
			Thread.sleep(1000 * delay);
		} catch (InterruptedException e) {
			LOG.error("fadeOut: error while sleeping", e);
		}
		fadeAbsolute(channel, 0);
	}

	public void flash(int channel) {
		flash(channel, 15, 100, 100);
	}

	public void flash(int channel, int repeats, int timeOn, int timeOff) {
		DimmerFlasher flasher = new DimmerFlasher(5, repeats, timeOn,
				timeOff);
		new Thread(flasher).start();
	}

	private void setDimmer(int channel, int value) {
		// Limit lower value to 5
		int valueToSet;
		if (value < 2) {
			valueToSet = 2;
		} else {
			valueToSet = value;
		}
		String channelHex = channelAsHex(channel);
		String valueHex = StringUtils.leftPad(Integer.toHexString(valueToSet),
				2, "0");
		sendCommand("*C901" + channelHex + valueHex + "#");
	}

	private synchronized String sendCommand(String command) {
		String reply = null;
		if (!connected) {
			LOG.debug("sendCommand: not connected, reconnecting");
			connect();
		}
		if (connected) {
			LOG.debug("sendCommand: connected, sending command [" + command + "]");
			char[] buf;

			out.write(command);
			out.flush();
			LOG.debug("sendCommand: sent command: " + command);
			buf = new char[512];
			try {
				LOG.debug("sendCommand: reading reply");
				in.read(buf);
			} catch (IOException e) {
				LOG.error("sendCommand: could not read reply", e);
			}
			reply = new String(buf).trim();
			LOG.debug("sendCommand: reply==null = " + (reply == null));
			LOG.debug("sendCommand: reply,contains(\">\") = " + reply.contains(">"));
			if (reply == null || !reply.contains(">")) {
				LOG.error("sendCommand: no connection to LANbox");
				disconnect();
				try {
					connect();
					out.write(command);
					out.flush();
					LOG.debug("sendCommand: sent command: " + command);
					buf = new char[512];
					in.read(buf);
				} catch (IOException e) {
					LOG.error("sendCommand: could not reconnect");
				}
			} else {
				LOG.debug("sendCommand: valid reply[" + reply + "]");
			}
			LOG.debug("sendCommand: reply=[" + reply + "]");
		} else {
			LOG.error("sendCommand: failed to connect to the LANbox");
		}
		return reply;
	}

	private class DimmerFader implements Runnable {
		private final int target;
		private final int channel;

		public DimmerFader(int channel, int target) {
			this.channel = channel;
			if (target < 0) {
				this.target = 0;
			} else if (target > 255) {
				this.target = 255;
			} else {
				this.target = target;
			}
		}

		public void run() {
			int currentLevel = getCurrentLevel(channel);
			if (currentLevel < target) {
				for (int i = currentLevel; i <= target; i++) {
					setDimmer(channel, i);
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						LOG.error("run: error while sleeping", ie);
					}
				}
			} else if (currentLevel > target) {
				for (int i = currentLevel; i >= target; i--) {
					setDimmer(channel, i);
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						LOG.error("run: error while sleeping", ie);
					}
				}
			}
		}

	};

	private class DimmerFlasher implements Runnable {
		private final int channel;
		private final int repeats;
		private final int timeOn;
		private final int timeOff;

		DimmerFlasher(int channel, int repeats, int timeOn, int timeOff) {
			this.channel = channel;
			this.repeats = repeats;
			this.timeOn = timeOn;
			this.timeOff = timeOff;
		}

		public void run() {
			int currentLevel = getCurrentLevel(channel);
			for (int i = 0; i < repeats; i++) {
				setDimmer(channel, 255);
				try {
					Thread.sleep(timeOn);
				} catch (InterruptedException ie) {
					LOG.error("run: error while sleeping", ie);
				}
				setDimmer(channel, 0);
				try {
					Thread.sleep(timeOff);
				} catch (InterruptedException ie) {
					LOG.error("run: error while sleeping", ie);
				}
			}
			fadeAbsolute(channel, currentLevel);
		}
	};

	private synchronized void disconnect() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (out != null) {
			out.close();
		}
		if (requestSocket != null) {
			try {
				requestSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized boolean connect() {
		int retries = 0;
		boolean connected = false;
		char[] buf;
		String reply;
		do {
			try {
				requestSocket = new Socket(host, 777);
				LOG.debug("connect: connected to " + host + " on port 777");
				out = new PrintWriter(requestSocket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(
						requestSocket.getInputStream()));
				buf = new char[512];
				in.read(buf);
				reply = new String(buf).trim();
				LOG.debug("connect: reply: [" + reply + "]");
				out.write("777\r");
				out.flush();
				LOG.debug("connect: sent password");
				buf = new char[512];
				in.read(buf);
				reply = new String(buf).trim();
				connected = "connected".equals(reply);
				LOG.debug("connect: reply: [" + reply + "] (connected=" + connected + ")");
				if (!connected) {
					requestSocket.close();
				}
			} catch (IOException e) {
				LOG.error("connect: error while connecting", e);
			}
			if (connected) {
				try {
					out.write("*65FF#");
					out.flush();
					LOG.debug("connect: set mode 16 bit");
					buf = new char[512];
					in.read(buf);
					reply = new String(buf).trim();
					LOG.debug("connect: reply: [" + reply + "]");
					if (!">".equals(reply)) {
						LOG.warn("connect: no correct reply from LANbox: " + reply);
						connected = false;
						requestSocket.close();
					}
					LOG.debug("connect: connected after 16 bot command: " + connected);
				} catch (IOException e) {
					LOG.error("connect: error while sending 16 bit command", e);
					connected = false;
				}
			}
		} while (!connected && retries++ < 5);
		LOG.debug("connect: returning after " + retries + " retries; connected=" + connected);
		return connected;
	}

	private void fadeAbsolute(int channel, int target) {
		new Thread(new DimmerFader(channel, target)).start();
	}
	
	private int getCurrentLevel(int channel) {
		return getCurrentLevel(channel, -1);
	}

	private int getCurrentLevel(int channel, int defaultValue) {
		int currentLevel = defaultValue;
		String channelHex = StringUtils.leftPad(channelAsHex(channel), 4);
		LOG.debug("getCurrentLevel: channelHex=[" + channelHex + "]");
		String reply = sendCommand("*CD01" + channelHex + "01#");
		LOG.debug("getCurrentLevel: received=[" + reply + "]");
		try {
			currentLevel = Integer.parseInt(reply.substring(1, 3), 16);
		} catch (NumberFormatException e) {
			LOG.error("getCurrentLevel: unrecognizable level: [" + reply + "]");
		} catch (StringIndexOutOfBoundsException e) {
			LOG.error("getCurrentLevel: unintelligble reply: [" + reply + "]");
		}
		if (currentLevel == -1) {
			currentLevel = getCurrentLevel(channel, 0);
		}
		LOG.debug("getCurrentLevel: current level=" + currentLevel);
		return currentLevel;
	}

	private String channelAsHex(int channel) {
		return StringUtils.leftPad(Integer.toHexString(channel), 4, '0');
	}
}
