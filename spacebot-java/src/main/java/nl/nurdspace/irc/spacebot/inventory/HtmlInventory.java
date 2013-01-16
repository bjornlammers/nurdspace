package nl.nurdspace.irc.spacebot.inventory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import twitter4j.internal.logging.Logger;

public class HtmlInventory implements Inventory {
	private static final Logger LOG = Logger.getLogger(HtmlInventory.class);
	
	private final String[][] inventory;
	
	public HtmlInventory(final InputStream input) {
		String html;
		try {
			html = IOUtils.toString(input);
		} catch (IOException e) {
			LOG.error("Kon HTML niet lezen", e);
			throw new RuntimeException("Kon inventory niet lezen");
		}
		inventory = parseInventory(html);
	}

	@Override
	public int getRowCount() {
		return inventory.length == 0 ? 0 : inventory[0].length - 1;
	}

	@Override
	public int getColumnCount() {
		return inventory.length - 1;
	}
	
	@Override
	public List<InventoryLocation> locate(final String object) {
		List<InventoryLocation> locations = new ArrayList<InventoryLocation>();
		for (int i = 1; i < inventory.length; i++) {
			for (int j = 1; j < inventory[0].length; j++) {
				if (inventory[i][j].toUpperCase().contains(object.toUpperCase())) {
					locations.add(new InventoryLocation(inventory[0][j], inventory[i][0], inventory[i][j]));
				}
			}
		}
		return locations;
	}

	private String[][] parseInventory(final String html) {
		// Find inventory table
		int tableStart = html.indexOf("<table class=\"wikitable\" style=\"text-align: center;\">");
		int tableEnd = html.indexOf("</table", tableStart);
		if (tableStart < 0 || tableEnd < 0) {
			throw new IllegalArgumentException("Not a valid inventory");
		}
		String table = html.substring(tableStart, tableEnd);
		
		// Count headers <th> in first row
		int columnCount = 0;
		int rowStart = table.indexOf("<tr");
		int rowEnd = table.indexOf("</tr");
		if (rowStart < 0 || rowEnd < 0) {
			throw new IllegalArgumentException("Not a valid inventory");
		}
		String firstRow = table.substring(rowStart, rowEnd);
		LOG.debug("First row: " + firstRow.replace('\n', '\\'));
		int index = 0;
		while ((index = firstRow.indexOf("<th", index)) >= 0) {
			columnCount++;
			index += 3;
		}
		
		// Count rows
		int rowCount = 0;
		index = 0;
		while ((index = table.indexOf("<tr", index)) >= 0) {
			rowCount++;
			index += 3;
		}
		
		// Create array
		String[][] inventory = new String[columnCount][rowCount];
		
		// Fill array
		index = 0;
		for (int i = 0; i < rowCount; i++) {
			rowStart = table.indexOf("<tr", index);
			rowEnd = table.indexOf("</tr", rowStart);
			String row = table.substring(rowStart + 3, rowEnd);
			LOG.info("row: [" + row.replace('\n', '\\') + "]");
			int indexInRow = 0;
			for (int j = 0; j < columnCount; j++) {
				int columnStart = row.indexOf("<t", indexInRow);
				columnStart = row.indexOf('>', columnStart);
				int columnEnd = row.indexOf("</t", columnStart);
				String columnContent = row.substring(columnStart + 1, columnEnd).trim();
				indexInRow = columnEnd + 3;
				LOG.debug("content of (" + j + ", " + i + "): [" + columnContent + "]");
				inventory[j][i] = columnContent;
			}
			index = rowEnd + 3;
		}
		return inventory; 
	}
}
