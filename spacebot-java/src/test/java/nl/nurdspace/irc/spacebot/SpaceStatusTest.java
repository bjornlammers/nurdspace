package nl.nurdspace.irc.spacebot;

import junit.framework.TestCase;

public class SpaceStatusTest extends TestCase {
	/**
	 * Check that the space is opened after five consecutive "on" readings from the LDR and closed
	 * after 5 consecutive "off" readings.
	 */
	public void testOpenAndCloseSpace() {
		SpaceStatus status = SpaceStatus.getInstance();
		status.setOpenAutomatically(true);
		// First, feed it some "off"s; default switch is at 300
		status.setFluorescentLighting(100);
		status.setFluorescentLighting(50);
		status.setFluorescentLighting(250);
		status.setFluorescentLighting(100);
		assertEquals(false, status.isOpen());
		// Now, feed it some "on"s
		status.setFluorescentLighting(400);
		assertEquals(false, status.isOpen());
		status.setFluorescentLighting(410);
		assertEquals(false, status.isOpen());
		status.setFluorescentLighting(395);
		assertEquals(false, status.isOpen());
		status.setFluorescentLighting(420);
		assertEquals(false, status.isOpen());
		status.setFluorescentLighting(400);
		assertEquals(true, status.isOpen());
		// Some more "off"s
		status.setFluorescentLighting(230);
		assertEquals(true, status.isOpen());
		status.setFluorescentLighting(230);
		assertEquals(true, status.isOpen());
		status.setFluorescentLighting(230);
		assertEquals(true, status.isOpen());
		status.setFluorescentLighting(230);
		assertEquals(true, status.isOpen());
		status.setFluorescentLighting(230);
		assertEquals(false, status.isOpen());
	}
}
