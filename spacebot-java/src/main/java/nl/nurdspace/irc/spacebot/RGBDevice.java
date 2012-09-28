package nl.nurdspace.irc.spacebot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RGBDevice extends DimmerDevice {
	private final int roodChannel;
	private final int groenChannel;
	private final int blauwChannel;
	
	public RGBDevice(int rood, int groen, int blauw) {
		this.roodChannel = rood;
		this.groenChannel = groen;
		this.blauwChannel = blauw;
	}

	public Collection<Integer> getChannels() {
		List<Integer> channels = new ArrayList<Integer>(3);
		channels.add(roodChannel);
		channels.add(groenChannel);
		channels.add(blauwChannel);
		return channels;
	}

	@Override
	public String toString() {
		return "RGB device (" + roodChannel + ", " + groenChannel + ", " + blauwChannel + ")";
	}
}
