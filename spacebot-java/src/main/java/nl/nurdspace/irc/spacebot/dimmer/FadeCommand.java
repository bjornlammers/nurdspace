package nl.nurdspace.irc.spacebot.dimmer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FadeCommand implements DimmerCommand, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(FadeCommand.class);

	private final Dimmer dimmer;
	private final int target;
	private final int channel;

	public FadeCommand(Dimmer dimmer, int channel, int target) {
		this.dimmer = dimmer;
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
		executeCommand();
	}
	
	public void executeCommand() {
		int currentLevel = dimmer.getCurrentLevel(channel);
		if (currentLevel < target) {
			for (int i = currentLevel; i <= target; i++) {
				dimmer.setDimmer(channel, i);
				try {
					Thread.sleep(4);
				} catch (InterruptedException ie) {
					LOG.error("run: error while sleeping", ie);
				}
			}
		} else if (currentLevel > target) {
			for (int i = currentLevel; i >= target; i--) {
				dimmer.setDimmer(channel, i);
				try {
					Thread.sleep(4);
				} catch (InterruptedException ie) {
					LOG.error("run: error while sleeping", ie);
				}
			}
		}
	}
}
