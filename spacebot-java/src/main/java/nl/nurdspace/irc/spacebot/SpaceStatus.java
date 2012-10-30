package nl.nurdspace.irc.spacebot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Space status monitor. Listens to serial messages from an Arduino and updates its
 * internal status accordingly.
 * Space open/closed may also be set manually. Whether this is done manually or automatically,
 * and what the setting is if it is set manually, is controlled by SpaceBot. Default is manual/
 * closed.
 * 
 * @author bjornl
 */
public class SpaceStatus {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(SpaceStatus.class);
	
	/** Temperature format. */
	private static final DecimalFormat TEMP_FORMAT = new DecimalFormat("##0.0C");

	/** The only instance. */
	private static SpaceStatus instance = new SpaceStatus();
	
	/** Fluorescent lighting level. */
	private int fluorescentLighting;
	/** Fluorescent lights on or off. */
	private Boolean fluorescentLightingOn;
	/** Temperature. */
	private Float temperature;
	/** Lock of the back door. */
	private Boolean backDoorLocked;
	/** Lock of the front door. */
	private Boolean frontDoorLocked;
	
	/** Whether space open/closed is set by the lights or by spacebot. */
	private boolean openAutomatically;
	/** Whether the space is open. */
	private Boolean open;
	
	/** The level at which the light level is considered "on" */
	private int switchLevel = 300;
	
	/** Listeners that need to be notified when something changes. */
	private List<SpaceStatusChangeListener> listeners = new ArrayList<SpaceStatusChangeListener>();
	
	/**
	 * Private constructor, use getInstance.
	 */
	private SpaceStatus() {
		super();
		open = null;
		openAutomatically = true;
	}

	/**
	 * Get the only instance. 
	 * @return
	 */
	public static SpaceStatus getInstance() {
		return instance;
	}

	public Boolean isBackDoorLocked() {
		return backDoorLocked;
	}
	
	public Boolean isFrontDoorLocked() {
		return frontDoorLocked;
	}
	
	public void setBackDoorLocked(boolean backDoorLocked) {
		if (this.backDoorLocked != null && this.backDoorLocked != backDoorLocked) {
			notifyListeners(SpaceStatusChangeListener.EVENT_BACK_DOOR_LOCK, backDoorLocked);
		}
		this.backDoorLocked = backDoorLocked;
		calculateOpen(this.backDoorLocked, this.frontDoorLocked);
	}
	
	public void setFrontDoorLocked(boolean frontDoorLocked) {
		if (this.frontDoorLocked != null && this.frontDoorLocked != frontDoorLocked) {
			notifyListeners(SpaceStatusChangeListener.EVENT_FRONT_DOOR_LOCK, frontDoorLocked);
		}
		this.frontDoorLocked = frontDoorLocked;
		calculateOpen(this.backDoorLocked, this.frontDoorLocked);
	}
	
	public Integer getFluorescentLighting() {
		return fluorescentLighting;
	}

	public void setFluorescentLighting(int fluorescentLighting) {
		notifyListeners(SpaceStatusChangeListener.EVENT_FLUORESCENT_LIGHT_READ, fluorescentLighting);
		if (this.fluorescentLightingOn != null && this.fluorescentLighting != fluorescentLighting) {
			notifyListeners(SpaceStatusChangeListener.EVENT_FLUORESCENT_LIGHT_CHANGED, fluorescentLighting);
		}
		this.fluorescentLighting = fluorescentLighting;
		calculateLightsOn();
	}

	public Float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		notifyListeners(SpaceStatusChangeListener.EVENT_TEMP_READ, temperature);
		if (this.temperature != null && this.temperature != temperature) {
			notifyListeners(SpaceStatusChangeListener.EVENT_TEMP_CHANGED, temperature);
		}
		this.temperature = temperature;
	}
	
	/**
	 * Tells you whether the fluorescent lights can be considered 'off' or 'on'.
	 * @return true if the lights are 'on'
	 */
	public Boolean isFluorescentLightOn() {
		return this.fluorescentLightingOn;
	}
	
	public void setSwitchLevel(final int switchLevel) {
		this.switchLevel = switchLevel;
		calculateLightsOn();
	}
	
	/**
	 * Calculates whether the fluorescent lights are on.
	 */
	private void calculateLightsOn() {
		boolean newLightsOn = this.fluorescentLighting >= this.switchLevel;
		if (this.fluorescentLightingOn != null && newLightsOn != this.fluorescentLightingOn) {
			notifyListeners(SpaceStatusChangeListener.EVENT_LIGHTS_ON_OFF, newLightsOn);
		}
		this.fluorescentLightingOn = newLightsOn;
	}
	
	/**
	 * Calculate whether the space is open or closed. If a door is unlocked, the space is open.
	 * @param backDoorLocked whether the back door is locked
	 * @param frontDoorLocked whether the front door is locked
	 */
	private void calculateOpen(final Boolean backDoorLocked, final Boolean frontDoorLocked) {
		LOG.trace("calculateOpen: " + backDoorLocked + ", " + frontDoorLocked);
		if (backDoorLocked != null && frontDoorLocked != null) {
			boolean open = !(backDoorLocked && frontDoorLocked);
			if (open && (isOpen() == null || !isOpen())) {
				if (openAutomatically) {
					LOG.trace("calculateOpen: space was not open, is open now");
					setOpen(true);
				} else {
					LOG.debug("calculateOpen: space open/closed is set to manual, ignoring unlocking");
				}
			} else if (!open && (isOpen() == null || isOpen())) {
				if (openAutomatically) {
					LOG.trace("calculateOpen: space was open, is closed now");
					setOpen(false);
				} else {
					LOG.debug("calculateOpen: space open/closed is set to manual, ignoring lights off");
				}
			}
		}
	}
	
	public void addListener(SpaceStatusChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(SpaceStatusChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(int eventType, Object value) {
		for (SpaceStatusChangeListener listener : listeners) {
			listener.spaceStatusChanged(eventType, value);
		}
	}
	
	public Boolean isOpen() {
		return this.open;
	}
	
	public void setOpen(boolean open) {
		if (this.open != null) {
			if (open != this.open) {
				this.open = open;
				notifyListeners(SpaceStatusChangeListener.EVENT_SPACE_OPEN_CLOSE, this.open);
			}
		} else {
			this.open = open;
		}
	}
	
	public boolean isOpenAutomatically() {
		return openAutomatically;
	}

	public void setOpenAutomatically(final boolean openAutomatically) {
		this.openAutomatically = openAutomatically;
	}

	public JSONObject asJSON() {
		JSONObject obj = new JSONObject();
		obj.put("api", "0.12");
		obj.put("space", "NURDSpace");
		obj.put("logo", "http://nurdspace.tk/spaceapi/logo.png");
	 
		JSONObject icons = new JSONObject();
		icons.put("open", "http://nurdspace.tk/spaceapi/icon-open.png");
		icons.put("closed", "http://nurdspace.tk/spaceapi/icon-closed.png");

		obj.put("icon", icons);
		obj.put("url", "http://nurdspace.nl/");
		obj.put("address", "Vadaring 80, 6702 EB Wageningen, The Netherlands");
		
		JSONObject contact = new JSONObject();
		contact.put("irc", "irc://irc.oftc.net/#nurds");
		contact.put("twitter", "@NURDspace");
		contact.put("ml", "nurds@nurdspace.nl");
		
		obj.put("contact", contact);
		obj.put("lat", 51.9643786f);
		obj.put("lon", 5.6571056f);
		obj.put("open", this.isOpen() != null && this.isOpen());
		obj.put("lastchange", System.currentTimeMillis());
		
		JSONArray sensors = new JSONArray();
		JSONObject sensorsObject = new JSONObject();
		JSONObject temp = new JSONObject();
		if (this.temperature != null) {
			temp.put("space", TEMP_FORMAT.format(this.temperature));
		}
		sensorsObject.put("temp", temp);
		sensors.add(sensorsObject);
		obj.put("sensors", sensors);
		return obj;
	}
}
