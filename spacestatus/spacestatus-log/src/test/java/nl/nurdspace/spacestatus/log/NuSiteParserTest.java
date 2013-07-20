package nl.nurdspace.spacestatus.log;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class NuSiteParserTest {

	@Test
	public void testGetLengteFiles() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new Integer(4), parser.getAantalFiles());
	}

	@Test
	public void testGetAantalFiles() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new Integer(0), parser.getLengteFiles());
	}

	@Test
	public void testGetAantalTreinstoringen() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new Integer(0), parser.getAantalTreinstoringen());
	}

	@Test
	public void testGetAantalWerkzaamheden() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new Integer(29), parser.getAantalWerkzaamheden());
	}

	@Test
	public void testGetTemperatuur() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new Integer(22), parser.getTemperatuur());
	}

	@Test
	public void testGetBeurskoers() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new BigDecimal("369.76"), parser.getBeurskoers());
	}

	@Test
	public void testGetBeurswinst() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new BigDecimal("0.27"), parser.getBeurswinst());
	}

	@Test
	public void testGetBenzineprijs() throws IOException, URISyntaxException {
		String closed = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("nu.html").toURI());
    	Document doc = Jsoup.parse(closed);
		NuSiteParser parser = new NuSiteParser(doc);
		assertEquals(new BigDecimal("1.839"), parser.getBenzineprijs());
	}
}
