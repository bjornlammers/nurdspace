package nl.nurdspace.irc.spacebot.inventory;

public class InventoryLocation {
	private final String row;
	private final String column;
	private final String contents;
	
	public InventoryLocation(final String row, final String column, final String contents) {
		this.row = row;
		this.column = column;
		this.contents = contents;
	}

	public String getRow() {
		return row;
	}

	public String getColumn() {
		return column;
	}

	public String getContents() {
		return contents;
	}
}
