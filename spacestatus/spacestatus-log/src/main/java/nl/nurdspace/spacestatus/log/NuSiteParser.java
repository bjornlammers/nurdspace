package nl.nurdspace.spacestatus.log;

import java.math.BigDecimal;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class NuSiteParser {
	private static final Logger LOG = Logger.getLogger(NuSiteParser.class);
	
	private final Integer aantalFiles;
	private final Integer lengteFiles;
	private final Integer aantalTreinstoringen;
	private final Integer aantalWerkzaamheden;
	private final Integer temperatuur;
	private final BigDecimal beurskoers;
	private final BigDecimal beurswinst;
	private final BigDecimal benzineprijs;
	
	public NuSiteParser(final Document site) {
		Element page = site.getElementById("page");
		Element pagewrapper = page.getElementById("pagewrapper");
		Element contentwrapper = pagewrapper.getElementById("contentwrapper");
		Element rightcolumn = contentwrapper.getElementById("rightcolumn");
		Element component = rightcolumn.getElementsByClass("component").get(1);
		Element localnews = component.getElementsByClass("localnews").first();
		Element content = localnews.getElementsByClass("content").first();
		
		Element verkeer = content.getElementsByAttributeValue("data-vr-contentbox", "PositieVerkeer").first();
		Element verkeerLink = verkeer.getElementsByTag("a").first();
		Element aantalFilesElement = verkeerLink.getElementsByTag("span").first();
		aantalFiles = getIntegerFromTextNodeBeforeWord(aantalFilesElement, "files");
		Element lengteFilesElement = verkeerLink.getElementsByTag("small").first();
		lengteFiles = getIntegerFromTextNodeBeforeWord(lengteFilesElement, "km");

		Element trein = content.getElementsByAttributeValue("data-vr-contentbox", "PositieTrain").first();
		Element treinLink = trein.getElementsByTag("a").first();
		Element aantalStoringenElement = treinLink.getElementsByTag("span").first();
		aantalTreinstoringen = getIntegerFromFirstTextNode(aantalStoringenElement);
		Element aantalWerkzaamhedenElement = treinLink.getElementsByTag("span").get(2);
		aantalWerkzaamheden = getIntegerFromFirstTextNode(aantalWerkzaamhedenElement);
		
		Element weer = content.getElementsByAttributeValue("data-vr-contentbox", "PositieWeer").first();
		Element weerLink = weer.getElementsByTag("a").first();
		Element temperatuurElement = weerLink.getElementsByTag("span").first();
		temperatuur = getIntegerFromTextNodeBefore(temperatuurElement, "\u00b0C");

		Element beurs = content.getElementsByAttributeValue("data-vr-contentbox", "PositieBeurs").first();
		Element beursLink = beurs.getElementsByTag("a").first();
		Element koersElement = beursLink.getElementsByTag("span").first();
		beurskoers = getDecimalFromFirstTextNode(koersElement);
		Element winstElement = beursLink.getElementsByTag("small").first();
		beurswinst = getDecimalFromTextNodeBefore(winstElement, "%");;

		Element brandstof = content.getElementsByAttributeValue("data-vr-contentbox", "PositieBrandstof").first();
		Element brandstofLink = brandstof.getElementsByTag("a").first();
		Element benzineprijsElement = brandstofLink.getElementsByTag("span").first();
		benzineprijs = getDecimalFromTextNodeAfter(benzineprijsElement, "\u20ac ");
	}

	private Integer getIntegerFromTextNodeBeforeWord(final Element element, final String word) {
		Integer number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				StringTokenizer tokenizer = new StringTokenizer(text, " ");
				String currentToken = null;
				String previousToken;
				while (tokenizer.hasMoreElements()) {
					previousToken = currentToken;
					currentToken = tokenizer.nextToken();
					LOG.debug("current token [" + currentToken + "], previous token [" + previousToken + "]");
					if (word.equals(currentToken) && previousToken != null) {
						LOG.debug("parsing integer [" + previousToken + "]");
						try {
							number = Integer.parseInt(previousToken);
							break;
						} catch (NumberFormatException e) {
							LOG.warn("Could not parse an int from [" + previousToken + "]; before [" + word + "]");
						}
					}
				}
			}
		}
		return number;
	}

	private Integer getIntegerFromTextNodeBefore(final Element element, final String beforeText) {
		Integer number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				if (text.contains(beforeText)) {
					String numberText = text.substring(0, text.indexOf(beforeText));
					try {
						number = Integer.parseInt(numberText);
						break;
					} catch (NumberFormatException e) {
						LOG.warn("Could not parse an int from [" + numberText + "]");
					}
				}
			}
		}
		return number;
	}

	private BigDecimal getDecimalFromTextNodeBefore(final Element element, final String beforeText) {
		BigDecimal number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				if (text.contains(beforeText)) {
					String numberText = text.substring(0, text.indexOf(beforeText));
					try {
						number = new BigDecimal(numberText.replace(',', '.'));
						break;
					} catch (NumberFormatException e) {
						LOG.warn("Could not parse a decimal from [" + numberText + "]");
					}
				}
			}
		}
		return number;
	}

	private BigDecimal getDecimalFromTextNodeAfter(final Element element, final String afterText) {
		BigDecimal number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				if (text.contains(afterText)) {
					String numberText = text.substring(text.indexOf(afterText) + afterText.length());
					try {
						number = new BigDecimal(numberText.replace(',', '.'));
						break;
					} catch (NumberFormatException e) {
						LOG.warn("Could not parse a decimal from [" + numberText + "]");
					}
				}
			}
		}
		return number;
	}

	private Integer getIntegerFromFirstTextNode(final Element element) {
		Integer number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				try {
					number = Integer.parseInt(text);
					break;
				} catch (NumberFormatException e) {
					LOG.warn("Could not parse an int from [" + text + "]");
				}
			}
		}
		return number;
	}

	private BigDecimal getDecimalFromFirstTextNode(final Element element) {
		BigDecimal number = null;
		for (Node node : element.childNodes()) {
			LOG.debug("node: " + node.getClass().getName());
			if (node instanceof TextNode) {
				String text = ((TextNode) node).text().trim();
				LOG.debug("text: " + text);
				try {
					number = new BigDecimal(text.replace(',', '.'));
					break;
				} catch (NumberFormatException e) {
					LOG.warn("Could not parse a decimal from [" + text + "]");
				}
			}
		}
		return number;
	}
	
	public Integer getAantalFiles() {
		return aantalFiles;
	}

	public Integer getLengteFiles() {
		return lengteFiles;
	}

	public Integer getAantalTreinstoringen() {
		return aantalTreinstoringen;
	}

	public Integer getAantalWerkzaamheden() {
		return aantalWerkzaamheden;
	}

	public Integer getTemperatuur() {
		return temperatuur;
	}

	public BigDecimal getBeurskoers() {
		return beurskoers;
	}

	public BigDecimal getBeurswinst() {
		return beurswinst;
	}

	public BigDecimal getBenzineprijs() {
		return benzineprijs;
	}
}
