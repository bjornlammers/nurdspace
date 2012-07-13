package nl.nurdspace.irc.spacebot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterUpdater implements SpaceStatusChangeListener {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(TwitterUpdater.class);
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	final Twitter twitter;

	private boolean lastStatus;

	public TwitterUpdater() {
		TwitterFactory tf = new TwitterFactory();
		this.twitter = tf.getInstance();

		// Read last status
		try {
			Scanner scanner = new Scanner(new FileInputStream(
					"/root/spacebot/twitterstatus.txt"));
			try {
				if (scanner.hasNextLine()) {
					this.lastStatus = scanner.nextLine().equals("open");
				}
			} finally {
				scanner.close();
			}
		} catch (FileNotFoundException e) {
			this.lastStatus = false;
		}
		LOG.debug("constructor: last status was: " + this.lastStatus);
	}

	@Override
	public void spaceStatusChanged(int eventType, Object status) {
		LOG.trace("spaceStatusChanged: " + eventType);
		if (eventType == EVENT_SPACE_OPEN_CLOSE) {
			boolean open = ((Boolean) status);
			LOG.trace("spaceStatusChanged: open=" + open);
			if (this.lastStatus != open) {
				LOG.trace("spaceStatusChanged: status changed");
				String message;
				if (open) {
					message = "Space is OPENED on "
							+ DATE_FORMAT.format(new Date());
				} else {
					message = "Space was CLOSED on "
							+ DATE_FORMAT.format(new Date());
				}
				LOG.debug("spaceStatusChanged: updating status to: " + message);
				TwitterUpdateThread thread = new TwitterUpdateThread(message);
				new Thread(thread).start();
				
				this.lastStatus = open;
			}
			
			// Update status file
			File log = new File("/root/spacebot/twitterstatus.txt");
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(log, false));
				writer.write((open ? "open" : "closed") + "\n");
				writer.flush();
				LOG.trace("spaceStatusChanged: wrote new status to file");
			} catch (IOException ioe) {
				LOG.error("spaceStatusChanged: error while writing to file", ioe);
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
		LOG.trace("spaceStatusChanged: exit");
	}

	private class TwitterUpdateThread implements Runnable {

		private final String message;

		TwitterUpdateThread(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			try {
				Status twitterStatus = twitter.updateStatus(this.message);
				LOG.info("run: Successfully updated the status to ["
						+ twitterStatus.getText() + "].");
			} catch (TwitterException e) {
				// TODO: try again
				if (e.getErrorMessage().equals("Status is a duplicate.")) {
					LOG.error("run: ERROR: status is a duplicate!");
				} else {
					LOG.error("run: error posting tweet: " + e.getErrorMessage() + "; message: " + e.getMessage(), e);
				}
			}
		}
	}
}
