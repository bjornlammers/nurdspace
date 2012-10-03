package nl.nurdspace.irc.spacebot.dimmer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashCommand implements DimmerCommand, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(FlashCommand.class);

	private final Dimmer dimmer;
	private final List<DimmerDevice> devices;
	private final int repeats;
	private final int timeOn;
	private final int timeOff;
	private final Map<Integer, Integer> currentLevels;

	public FlashCommand(Dimmer dimmer, List<DimmerDevice> devices, int repeats, int timeOn, int timeOff) {
		this.dimmer = dimmer;
		this.devices = devices;
		this.repeats = repeats;
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.currentLevels = new HashMap<Integer, Integer>();
		for (DimmerDevice dimmerDevice : devices) {
			for (Integer channel : dimmerDevice.getChannels()) {
				currentLevels.put(channel, dimmer.getCurrentLevel(channel));
				dimmer.setDimmer(channel, 0);
			}
		}
	}

	public void run() {
		int currentDevice = 0;
		for (int i = 0; i < repeats; i++) {
			for (Integer channel : devices.get(currentDevice).getChannels()) {
				dimmer.setDimmer(channel, 255);
			}
			try {
				Thread.sleep(timeOn);
			} catch (InterruptedException ie) {
				LOG.error("run: error while sleeping", ie);
			}
			for (Integer channel : devices.get(currentDevice).getChannels()) {
				dimmer.setDimmer(channel, 0);
			}
			try {
				Thread.sleep(timeOff);
			} catch (InterruptedException ie) {
				LOG.error("run: error while sleeping", ie);
			}
			currentDevice++;
			if (currentDevice >= devices.size()) {
				currentDevice = 0;
			}
		}
		for (Integer channel : currentLevels.keySet()) {
			dimmer.setDimmer(channel, currentLevels.get(channel));
		}
	}
}
