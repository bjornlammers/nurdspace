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
		Element lengteFilesElement = verkeerLink.getElementsByTag("small").first();
		
		lengteFiles = getIntegerFromTextNodeBefore(lengteFilesElement, "km");
		aantalFiles = getIntegerFromTextNodeBefore(aantalFilesElement, "files");

		Element trein = content.getElementsByAttributeValue("data-vr-contentbox", "PositieTrain").first();
		Element treinLink = trein.getElementsByTag("a").first();
		Element aantalStoringenElement = treinLink.getElementsByTag("span").first();
		Element aantalWerkzaamhedenElement = treinLink.getElementsByTag("span").get(2);
		
		aantalTreinstoringen = getIntegerFromFirstTextNode(aantalStoringenElement);
		aantalWerkzaamheden = getIntegerFromFirstTextNode(aantalWerkzaamhedenElement);
		
		temperatuur = null;
		beurskoers = null;
		beurswinst = null;
		benzineprijs = null;
	}

	private Integer getIntegerFromTextNodeBefore(final Element element, final String beforeText) {
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
					if (beforeText.equals(currentToken) /* && previousToken != null */) {
						LOG.debug("parsing integer [" + previousToken + "]");
						try {
							number = Integer.parseInt(previousToken);
							break;
						} catch (NumberFormatException e) {
							LOG.warn("Could not parse an int from [" + previousToken + "]; before [" + beforeText + "]");
						}
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
				StringTokenizer tokenizer = new StringTokenizer(text, " ");
				while (tokenizer.hasMoreElements()) {
					String token = tokenizer.nextToken();
					try {
						number = Integer.parseInt(token);
						break;
					} catch (NumberFormatException e) {
						LOG.warn("Could not parse an int from [" + token + "]");
					}
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
