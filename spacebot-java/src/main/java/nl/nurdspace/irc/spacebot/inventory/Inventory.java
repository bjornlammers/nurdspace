package nl.nurdspace.irc.spacebot.inventory;

import java.util.List;

public interface Inventory {
	int getRowCount();
	int getColumnCount();
	List<InventoryLocation> locate(final String object);
}
