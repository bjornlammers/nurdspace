package nl.nurdspace.irc.spacebot;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommandTest {
	@Test
	public void testIsCommand() {
		assertTrue(Command.isCommand("!q"));
		assertFalse(Command.isCommand("! "));
		assertFalse(Command.isCommand("?q"));
		assertTrue(Command.isCommand("!q "));
		assertTrue(Command.isCommand("!q qwert"));
	}
	
	@Test
	public void testArgumentString() {
		assertNull(new Command("!q  ").getArgumentString());
		assertEquals("qwert", new Command("!q qwert ").getArgumentString());
		assertEquals("qwert y", new Command("!q qwert y   ").getArgumentString());
	}
	
	@Test
	public void testArgs() {
		assertNull(new Command("!q  ").getArgs());
		assertEquals(new String[] {"qwert"}, new Command("!q qwert ").getArgs());
		assertEquals(new String[] {"qwert", "y"}, new Command("!q qwert y   ").getArgs());
	}
	
	@Test
	public void testCommand() {
		assertEquals("q", new Command("!q qwert ").getCommand());
		assertEquals("q", new Command("!q     qwert y   ").getCommand());
	}
}
