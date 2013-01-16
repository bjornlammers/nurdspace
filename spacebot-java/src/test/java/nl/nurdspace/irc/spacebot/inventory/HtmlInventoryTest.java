package nl.nurdspace.irc.spacebot.inventory;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class HtmlInventoryTest {
	
	private HtmlInventory inventory;
	
	@Test
	public void testCounts() {
		inventory = new HtmlInventory(Thread.currentThread().getContextClassLoader().getResourceAsStream("inventory.html"));
		assertEquals(9, inventory.getColumnCount());
		assertEquals(4, inventory.getRowCount());
	}
	
	@Test
	public void testLocate() {
		inventory = new HtmlInventory(Thread.currentThread().getContextClassLoader().getResourceAsStream("inventory.html"));
		List<InventoryLocation> locations = inventory.locate("pruts");
		assertEquals(1, locations.size());
		assertEquals("4", locations.get(0).getRow());
		assertEquals("B", locations.get(0).getColumn());
	}
}
