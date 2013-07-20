package nl.nurdspace.spacestatus.log;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class NurdspaceSiteParserTest {

	@Test
	public void testGetTemperatuur() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nurdspace-closed.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NurdspaceSiteParser parser = new NurdspaceSiteParser(doc);
		assertEquals(new Float(Float.parseFloat("26.0")), parser.getTemperatuur());
	}
}
