package nl.nurdspace.irc.spacebot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class updates the JSON object for the space API as the space status changes.
 * @author bjornl
 *
 */
public class SpaceAPIUpdater implements SpaceStatusChangeListener {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(SpaceAPIUpdater.class);
	
	@Override
	public void spaceStatusChanged(int eventType, Object status) {
		LOG.trace("spaceStatusChanged: eventType=" + eventType);
		if (eventType != EVENT_DIMMED_LIGHT_CHANGED) {
			LOG.trace("spaceStatusChanged: updating JSON");
			// Dump the complete contents of the space status object to JSON
			JSONObject json = SpaceStatus.getInstance().asJSON();
			
			// Put this in a file
			File log = new File("/root/spacebot/webdir/status.json");
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(log, false));
				writer.write(json.toJSONString() + "\n");
				writer.flush();
				LOG.trace("spaceStatusChanged: succesfully updated JSON");
			} catch (IOException ioe) {
				LOG.error("spaceStatusChanged: error while writing status file", ioe);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ioe) {
						LOG.error("spaceStatusChanged: error while closing writer", ioe);
					}
				}
			}
		}
	}
}
