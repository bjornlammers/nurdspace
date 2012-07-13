package nl.nurdspace.irc.spacebot;

/**
 * Interface for objects that are interested in changes in the space changes.
 * @author bjorn
 *
 */
public interface SpaceStatusChangeListener {

	static final int EVENT_TEMP_CHANGED = 1;
	static final int EVENT_TEMP_READ = 2;
	static final int EVENT_FLUORESCENT_LIGHT_CHANGED = 3;
	static final int EVENT_FLUORESCENT_LIGHT_READ = 4;
	static final int EVENT_DIMMED_LIGHT_CHANGED = 5;
	static final int EVENT_LIGHTS_ON_OFF = 6;
	static final int EVENT_SPACE_OPEN_CLOSE = 7;
	static final int EVENT_BACK_DOOR_LOCK = 8;

	/**
	 * Called when the space status is changed.
	 * @param eventType the type of event
	 * @param status the new status value
	 */
	void spaceStatusChanged(int eventType, Object status);
}
