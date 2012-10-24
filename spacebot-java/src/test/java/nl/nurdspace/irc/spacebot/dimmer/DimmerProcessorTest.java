package nl.nurdspace.irc.spacebot.dimmer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimmerProcessorTest {
	private static final Logger LOG = LoggerFactory.getLogger(DimmerProcessorTest.class);
	
	public static void main(String[] args) throws InterruptedException {
		Dimmer dimmer = new Dimmer();
		dimmer.setHost("192.168.1.6");
		BlockingQueue<DimmerCommand> commands = new LinkedBlockingQueue<DimmerCommand>();
		DimmerCommandProcessor processor = new DimmerCommandProcessor(commands);
		Thread processorThread = new Thread(processor);
		processorThread.start();
		LOG.info("Waiting to send first command...");
		Thread.sleep(5000);
		LOG.info("Sending first command...");
		commands.add(new FadeCommand(dimmer, 2, 255));
		commands.add(new FadeCommand(dimmer, 2, 0));
		commands.add(new FadeCommand(dimmer, 2, 128));
		commands.add(new FadeCommand(dimmer, 2, 0));
		LOG.info("Waiting to stop...");
		Thread.sleep(30000);
		LOG.info("Sending stop command...");
		processor.stopProcessor();
	}
}
