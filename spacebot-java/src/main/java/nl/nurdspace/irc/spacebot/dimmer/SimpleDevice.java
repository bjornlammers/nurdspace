package nl.nurdspace.irc.spacebot.dimmer;

import java.util.ArrayList;
import java.util.List;

public class SimpleDevice extends DimmerDevice {
	private final int channel;
	
	public SimpleDevice(int channel) {
		this.channel = channel;
	}

	@Override
	public List<Integer> getChannels() {
		List<Integer> channels = new ArrayList<Integer>(1);
		channels.add(channel);
		return channels;
	}

	@Override
	public String toString() {
		return "Dimmerdevice (" + channel + ")";
	}
}
