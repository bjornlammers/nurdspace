package nl.nurdspace.irc.spacebot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Maintains a database of chemical elements.
 * 
 * @author bjornl
 * 
 */
public class Elements {
	/** The instance. */
	private static Elements instance;
	/** The elements. */
	private final Map<String, Element> elementsByAbbreviation;
	/** The elements. */
	private final Map<Integer, Element> elementsByNumber;
	/** The elements. */
	private final Map<String, Element> elementsByName;

	private Elements() {
		elementsByAbbreviation = new HashMap<String, Element>();
		elementsByNumber = new HashMap<Integer, Element>();
		elementsByName = new HashMap<String, Element>();
		loadElements();
	}

	public static synchronized Elements getInstance() {
		if (instance == null) {
			instance = new Elements();
		}
		return instance;
	}
	
	public Element getElementByAbbreviation(String abbreviation) {
		return elementsByAbbreviation.get(abbreviation.toUpperCase());
	}
	
	public Element getElementByNumber(int number) {
		return elementsByNumber.get(number);
	}
	
	public Element getElementByName(String name) {
		return elementsByName.get(name.toUpperCase());
	}
	
	private void loadElements() {
		String elementLine;
	    Scanner scanner = null;
	    try {
		    scanner = new Scanner(new FileInputStream("elements.csv"));
			while (scanner.hasNextLine()){
			    elementLine = scanner.nextLine();
			    StringTokenizer tokenizer = new StringTokenizer(elementLine, ",");
			    Element anElement = new Element(Integer.parseInt(tokenizer.nextToken()), Float.parseFloat(tokenizer.nextToken()), tokenizer.nextToken(), tokenizer.nextToken());
			    elementsByAbbreviation.put(anElement.getAbbreviation().toUpperCase(), anElement);
			    elementsByNumber.put(anElement.getNumber(), anElement);
			    elementsByName.put(anElement.getName().toUpperCase(), anElement);
			}
	    } catch (FileNotFoundException e) {
			// not important, just won't work
	    	// TODO log error
		} finally{
	    	if (scanner != null) {
	    		scanner.close();
	    	}
	    }
	}
}
