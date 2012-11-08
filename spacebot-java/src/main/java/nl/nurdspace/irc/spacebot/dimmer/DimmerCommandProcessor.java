package nl.nurdspace.irc.spacebot.dimmer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimmerCommandProcessor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(DimmerCommandProcessor.class);
	private BlockingQueue<DimmerCommand> commands;
	private boolean stop = false;
	
	public DimmerCommandProcessor(BlockingQueue<DimmerCommand> commands) {
		this.commands = commands;
	}
	
	public void stopProcessor() {
		this.stop = true;
	}
	
	@Override
	public void run() {
		while (!stop) {
			try {
				LOG.debug("Haal command...");
				DimmerCommand command = commands.poll(1, TimeUnit.SECONDS);
				if (command != null) {
					LOG.debug("Execute command");
					command.executeCommand();
				} else {
					LOG.debug("No command");
				}
			} catch (InterruptedException e) {
				LOG.error("Interrupted while taking command", e);
			}
		}
		LOG.info("Stopping...");
	}
}
