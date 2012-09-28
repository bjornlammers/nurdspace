package nl.nurdspace.irc.spacebot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleDevice extends DimmerDevice {
	private final int channel;
	
	public SimpleDevice(int channel) {
		this.channel = channel;
	}

	@Override
	public Collection<Integer> getChannels() {
		List<Integer> channels = new ArrayList<Integer>(1);
		channels.add(channel);
		return channels;
	}

	@Override
	public String toString() {
		return "Dimmerdevice (" + channel + ")";
	}
}
