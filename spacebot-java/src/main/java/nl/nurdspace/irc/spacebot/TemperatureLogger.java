package nl.nurdspace.irc.spacebot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureLogger implements SpaceStatusChangeListener {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(TemperatureLogger.class);
	
	private static final DecimalFormat tempFormat = new DecimalFormat("##0.0");

	@Override
	public void spaceStatusChanged(int eventType, Object status) {
		if (eventType == EVENT_TEMP_READ) {
			// Write to file for logging
			File log = new File("/root/spacebot/temp.txt");
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(log, false));
				writer.write(tempFormat.format(status) + "\n");
				writer.flush();
			} catch (IOException ioe) {
				LOG.error("spaceStatusChanged: error writing file", ioe);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ioe) {
						// Ignore
					}
				}
			}
		}
	}
}
