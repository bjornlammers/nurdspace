package nl.nurdspace.irc.spacebot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.nurdspace.irc.spacebot.dimmer.Dimmer;
import nl.nurdspace.irc.spacebot.dimmer.DimmerDevice;
import nl.nurdspace.irc.spacebot.dimmer.RGBDevice;
import nl.nurdspace.irc.spacebot.dimmer.SimpleDevice;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;
import java.nio.charset.Charset;

/**
 * Control class for spacebot. Assembles the parts and starts the bot.
 * Also monitors the console for commands.
 * @author bjornl
 */
public class SpaceBotControl {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(SpaceBotControl.class);
	
	private static final String SETTING_CHANNEL = "channel";
	private static final String SETTING_NICK = "nick";
	private static final String SETTING_DIMMER_HOST = "dimmer.host";
	private static final String SETTING_DIMMER_CHANNEL = "dimmer.channel";
	private static final String SETTING_DIMMER_CHANNELS = "dimmer.channels";
	private static final String SETTING_DIMMER_DEVICES = "dimmer.devices";
	private static final String SETTING_FLASH_REPEATS = "flash.repeats";
	private static final String SETTING_FLASH_TIME_ON = "flash.time.on";
	private static final String SETTING_FLASH_TIME_OFF = "flash.time.off";
	private static final String SETTING_LIGHTS_ON_LEVEL = "lights.on.level";
	private static final String SETTING_LIGHTS_OFF_DELAY = "lights.off.delay";
	private static final String SETTING_MPD_HOST = "mpd.host";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// Create a new bot
		PircBotX bot = new PircBotX();
		
	    bot.setEncoding(Charset.forName("UTF-8")); // Or other suitable character set
	    
	    // Configurable settings
		Map settings = createInitialSettings();
		
		// Setup http server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/status", new SpaceStatusHttpHandler());
        server.setExecutor(null);
        server.start();
		
		// Setup this bot
		bot.setName((String) settings.get(SETTING_NICK));
		bot.setVerbose(false);
		bot.setAutoNickChange(true);
		bot.setLogin("SpaceBot");
		bot.setVersion("v2.0");

		try {
			// Connect to the freenode IRC network
			bot.connect("irc.oftc.net");
			bot.joinChannel((String) settings.get(SETTING_CHANNEL));
			
			// Set up the object structure
			SerialMonitor serial = new SerialMonitor();
			Dimmer dimmer = new Dimmer();
			dimmer.setHost((String) settings.get(SETTING_DIMMER_HOST));
			SpaceBot spaceBot = new SpaceBot(bot.getChannel((String) settings.get(SETTING_CHANNEL)), dimmer, (String) settings.get(SETTING_MPD_HOST), (Integer) settings.get(SETTING_DIMMER_CHANNEL), (List<Integer>) settings.get(SETTING_DIMMER_CHANNELS), (List<DimmerDevice>) settings.get(SETTING_DIMMER_DEVICES));
			spaceBot.setSerialMonitor(serial);
			SpaceStatus.getInstance().addListener(spaceBot);
			TemperatureLogger tempLogger = new TemperatureLogger();
			SpaceStatus.getInstance().addListener(tempLogger);
			TwitterUpdater twitter = new TwitterUpdater();
			SpaceStatus.getInstance().addListener(twitter);
			SpaceAPIUpdater api = new SpaceAPIUpdater();
			SpaceStatus.getInstance().addListener(api);
			bot.getListenerManager().addListener(spaceBot);
			
			// Propagate the settings
			// TODO all this must be done with a listener pattern or something like that
			propagateSettings(settings, bot, spaceBot, dimmer);

			// Initialize serial monitor
			serial.initialize();
			
			boolean quit = false;
			do {
				// monitor keyboard
				System.out.println("SpaceOpen=" + SpaceStatus.getInstance().isOpen() + "; automatically: " + SpaceStatus.getInstance().isOpenAutomatically() + "\nReady...\ntype 'a' for Automatic (locks)\n'o' to Open\n'd' or 'c' to Close\n'q' to Quit the channel\n'r' to reload properties");
				try {
					InputStreamReader converter = new InputStreamReader(System.in);
					BufferedReader in = new BufferedReader(converter);
					String input = in.readLine();
					if (input.equalsIgnoreCase("O")) {
						SpaceStatus.getInstance().setOpenAutomatically(false);
						SpaceStatus.getInstance().setOpen(true);
					} else if (input.equalsIgnoreCase("D") || input.equalsIgnoreCase("C")) {
						SpaceStatus.getInstance().setOpenAutomatically(false);
						SpaceStatus.getInstance().setOpen(false);
					} else if (input.equalsIgnoreCase("Q")) {
						quit = true;
					} else if (input.equalsIgnoreCase("A")) {
						SpaceStatus.getInstance().setOpenAutomatically(true);
					} else if (input.equalsIgnoreCase("R")) {
						loadProperties(settings);
						propagateSettings(settings, bot, spaceBot, dimmer);
					}
				} catch (IOException ioe) {
					
				}
			} while (!quit);
			System.out.println("QUIT!");
			
			// Close serial monitor
			serial.close();
			
			// Disconnect from channel
			bot.disconnect();
		} catch (NickAlreadyInUseException ex) {
			LOG.error("main: nick already in use", ex);
		} catch (IrcException ex) {
			LOG.error("main: error connecting", ex);
		} catch (IOException ex) {
			LOG.error("main: error connecting", ex);
		}
		if (server != null) {
			server.stop(0);
		}
		System.exit(0);
	}

	private static void propagateSettings(Map settings, PircBotX bot, SpaceBot spaceBot, Dimmer dimmer) {
		dimmer.setHost((String) settings.get(SETTING_DIMMER_HOST));
		spaceBot.setDimmerChannel((Integer) settings.get(SETTING_DIMMER_CHANNEL));
		spaceBot.setFlashRepeats((Integer) settings.get(SETTING_FLASH_REPEATS));
		spaceBot.setFlashTimeOn((Integer) settings.get(SETTING_FLASH_TIME_ON));
		spaceBot.setFlashTimeOff((Integer) settings.get(SETTING_FLASH_TIME_OFF));
		spaceBot.setLightsOffDelay((Integer) settings.get(SETTING_LIGHTS_OFF_DELAY));
		bot.changeNick((String) settings.get(SETTING_NICK));
		SpaceStatus.getInstance().setSwitchLevel((Integer) settings.get(SETTING_LIGHTS_ON_LEVEL));
	}

	private static Map createInitialSettings() {
		Map settings = new HashMap();
		settings.put(SETTING_DIMMER_CHANNEL, 5);
		settings.put(SETTING_DIMMER_CHANNELS, new int[] {1, 2, 5});
		settings.put(SETTING_DIMMER_HOST, "192.168.80.77");
		settings.put(SETTING_NICK, "SpaceBotTest");
		settings.put(SETTING_CHANNEL, "#nurdstest");
		settings.put(SETTING_FLASH_REPEATS, 15);
		settings.put(SETTING_FLASH_TIME_ON, 100);
		settings.put(SETTING_FLASH_TIME_OFF, 100);
		settings.put(SETTING_LIGHTS_ON_LEVEL, 300);
		settings.put(SETTING_LIGHTS_OFF_DELAY, 30);
		settings.put(SETTING_MPD_HOST, "dockstar");

		loadProperties(settings);
		return settings;
	}
	
	private static void loadProperties(Map settings) {
		Properties props = new Properties();
		try {
			File properties = new File("/root/spacebot/spacebot.properties");
//			File properties = new File("/tmp/spacebot.properties");
			if (properties.exists()) {
				props.load(new FileInputStream(properties));
				if (props.containsKey(SETTING_MPD_HOST)) {
					System.out.println("Setting " + SETTING_MPD_HOST + " to " + props.getProperty(SETTING_MPD_HOST));
					settings.put(SETTING_MPD_HOST, props.getProperty(SETTING_MPD_HOST));
				}
				if (props.containsKey(SETTING_CHANNEL)) {
					System.out.println("Setting " + SETTING_CHANNEL + " to " + props.getProperty(SETTING_CHANNEL));
					settings.put(SETTING_CHANNEL, props.getProperty(SETTING_CHANNEL));
				}
				if (props.containsKey(SETTING_DIMMER_CHANNELS)) {
					System.out.println("Setting " + SETTING_DIMMER_CHANNELS + " to " + props.getProperty(SETTING_DIMMER_CHANNELS));
					String[] channels = props.getProperty(SETTING_DIMMER_CHANNELS).split(",");
					List<Integer> channelNumbers = new ArrayList<Integer>();
					List<DimmerDevice> devices = new ArrayList<DimmerDevice>();
					for (int i = 0; i < channels.length; i++) {
						DimmerDevice device;
						if (channels[i].startsWith("[")) {
							// Device [r-channel,g-channel,b-channel]
							String rgb = channels[i].trim().substring(1, channels[i].length() - 1);
							LOG.info("RGB: " + rgb);
							String[] rgbArray = rgb.split("\\|");
							LOG.info("R: " + rgbArray[0].trim());
							LOG.info("G: " + rgbArray[1].trim());
							LOG.info("B: " + rgbArray[2].trim());
							device = new RGBDevice(Integer.parseInt(rgbArray[0].trim()), Integer.parseInt(rgbArray[1].trim()), Integer.parseInt(rgbArray[2].trim()));
						} else {
							device = new SimpleDevice(Integer.parseInt(channels[i].trim()));
						}
						LOG.info("Dimmer device: " + device.toString());
						channelNumbers.addAll(device.getChannels());
						devices.add(device);
					}
					settings.put(SETTING_DIMMER_CHANNELS, channelNumbers);
					settings.put(SETTING_DIMMER_DEVICES, devices);
				}
				if (props.containsKey(SETTING_DIMMER_CHANNEL)) {
					System.out.println("Setting " + SETTING_DIMMER_CHANNEL + " to " + props.getProperty(SETTING_DIMMER_CHANNEL));
					settings.put(SETTING_DIMMER_CHANNEL, Integer.parseInt(props.getProperty(SETTING_DIMMER_CHANNEL)));
				}
				if (props.containsKey(SETTING_DIMMER_HOST)) {
					System.out.println("Setting " + SETTING_DIMMER_HOST + " to " + props.getProperty(SETTING_DIMMER_HOST));
					settings.put(SETTING_DIMMER_HOST, props.getProperty(SETTING_DIMMER_HOST));
				}
				if (props.containsKey(SETTING_FLASH_REPEATS)) {
					System.out.println("Setting " + SETTING_FLASH_REPEATS + " to " + props.getProperty(SETTING_FLASH_REPEATS));
					settings.put(SETTING_FLASH_REPEATS, Integer.parseInt(props.getProperty(SETTING_FLASH_REPEATS)));
				}
				if (props.containsKey(SETTING_FLASH_TIME_OFF)) {
					System.out.println("Setting " + SETTING_FLASH_TIME_OFF + " to " + props.getProperty(SETTING_FLASH_TIME_OFF));
					settings.put(SETTING_FLASH_TIME_OFF, Integer.parseInt(props.getProperty(SETTING_FLASH_TIME_OFF)));
				}
				if (props.containsKey(SETTING_FLASH_TIME_ON)) {
					System.out.println("Setting " + SETTING_FLASH_TIME_ON + " to " + props.getProperty(SETTING_FLASH_TIME_ON));
					settings.put(SETTING_FLASH_TIME_ON, Integer.parseInt(props.getProperty(SETTING_FLASH_TIME_ON)));
				}
				if (props.containsKey(SETTING_NICK)) {
					System.out.println("Setting " + SETTING_NICK + " to " + props.getProperty(SETTING_NICK));
					settings.put(SETTING_NICK, props.getProperty(SETTING_NICK));
				}
				if (props.containsKey(SETTING_LIGHTS_ON_LEVEL)) {
					System.out.println("Setting " + SETTING_LIGHTS_ON_LEVEL + " to " + props.getProperty(SETTING_LIGHTS_ON_LEVEL));
					settings.put(SETTING_LIGHTS_ON_LEVEL, Integer.parseInt(props.getProperty(SETTING_LIGHTS_ON_LEVEL)));
				}
				if (props.containsKey(SETTING_LIGHTS_OFF_DELAY)) {
					System.out.println("Setting " + SETTING_LIGHTS_OFF_DELAY + " to " + props.getProperty(SETTING_LIGHTS_OFF_DELAY));
					settings.put(SETTING_LIGHTS_OFF_DELAY, Integer.parseInt(props.getProperty(SETTING_LIGHTS_OFF_DELAY)));
				}
			} else {
				System.out.println("loadProperties: no properties file found, using previously set properties");
			}
		} catch (IOException e) {
			LOG.error("loadProperties: error while loading", e);
		}
		System.out.println("New settings: " + settings);
	}
}
