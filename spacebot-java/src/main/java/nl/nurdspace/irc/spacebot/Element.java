package nl.nurdspace.irc.spacebot;

/**
 * Represents a chemical element.
 * @author bjornl
 *
 */
public class Element {
	/** Atomic number. */
	private final int number;
	/** Weight. */
	private final float weight;
	/** Abbreviation. */
	private final String abbreviation;
	/** Name. */
	private final String name;
	
	public Element(final int number, final float weight, final String name, final String abbreviation) {
		this.name = name;
		this.number = number;
		this.weight = weight;
		this.abbreviation = abbreviation;
	}

	public int getNumber() {
		return number;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public float getWeight() {
		return weight;
	}

	public String getName() {
		return name;
	}
}
