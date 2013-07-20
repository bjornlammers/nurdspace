package nl.nurdspace.spacestatus.log;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class NurdspaceSiteParser {
	private static final Logger LOG = Logger.getLogger(NurdspaceSiteParser.class);
	
	private final Float temperatuur;
	private final Boolean open;
	
	public NurdspaceSiteParser(final Document site) {
		Element wrapper = site.getElementById("wrapper");
		Element statusblok = wrapper.getElementById("statusblokje");
		Element content = statusblok.getElementsByTag("p").get(0);
		String[] texts = new String[10];
		int i = 0;
		for (Node node : content.childNodes()) {
			if (node instanceof TextNode) {
				texts[i] = ((TextNode) node).text().trim();
				if (LOG.isDebugEnabled()) {
					LOG.debug(i + ": [" + texts[i] + "]");
				}
				i++;
			}
		}
		String openString = texts[1];
		String temperatuurString = texts[2];
		if (LOG.isDebugEnabled()) {
			LOG.debug("open: " + openString);
			LOG.debug("temperatuur: " + temperatuurString);
		}
		temperatuur = Float.parseFloat(temperatuurString.substring(0, temperatuurString.length() - 1));
		if (openString.contains("CLOSED")) {
			open = false;
		} else if (openString.contains("OPEN")) {
			open = true;
		} else {
			open = null;
		}
	}
	
	public Float getTemperatuur() {
		return temperatuur;
	}
	
	public Boolean isOpen() {
		return open;
	}
}
