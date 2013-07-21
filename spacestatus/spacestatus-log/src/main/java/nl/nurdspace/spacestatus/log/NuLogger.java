package nl.nurdspace.spacestatus.log;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@LocalBean
public class NuLogger {
    private static final String GAUGE_BENZINEPRIJS = "benzineprijs";

	private static final String GAUGE_BEURSWINST = "beurswinst";

	private static final String GAUGE_BEURSKOERS = "beurskoers";

	private static final String GAUGE_WERKZAAMHEDEN_TREIN = "werkzaamhedenTrein";

	private static final String GAUGE_STORINGEN_TREIN = "storingenTrein";

	private static final String GAUGE_LENGTE_FILES = "lengteFiles";

	private static final String GAUGE_AANTAL_FILES = "aantalFiles";

	private static final String GAUGE_TEMPERATUUR = "temperatuur";

	private static final String RRD_PATH = "/Users/bjolamme/nu.rrd";

	private static final Logger LOG = Logger.getLogger(NuLogger.class);

    private HttpClient client;
    private RrdDef rrdDef;
    
    @PostConstruct
    public void init() {
    	LOG.info("init");
		client = new HttpClient();
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(1000);
    	client.setParams(params);
    	rrdDef = createRrdDef();
    	checkRrdFile();
    }
    
    @Schedule(hour = "*", minute = "*", second = "0,10,20,30,40,50", persistent = false)
    public void retrieveData() {
    	LOG.debug("Retrieving data");
    	Document nurdspace = getRemoteStatus();
    	if (nurdspace == null) {
    		LOG.warn("Data could not be retrieved");
    	} else {
        	NuSiteParser parser = new NuSiteParser(nurdspace);
    		try {
				RrdDb rrdDb = new RrdDb(RRD_PATH);
				Sample sample = rrdDb.createSample();
				long timestamp = Util.getTimestamp(new Date());
				LOG.info("creating sample with timestamp: " + timestamp);
				sample.setTime(timestamp);
				Integer temperatuur = parser.getTemperatuur();
				if (temperatuur != null) {
					sample.setValue(GAUGE_TEMPERATUUR, temperatuur);
				}
				Integer aantalFiles = parser.getAantalFiles();
				if (aantalFiles != null) {
					sample.setValue(GAUGE_AANTAL_FILES, aantalFiles);
				}
				Integer lengteFiles = parser.getLengteFiles();
				if (lengteFiles != null) {
					sample.setValue(GAUGE_LENGTE_FILES, lengteFiles);
				}
				Integer storingenTrein = parser.getAantalTreinstoringen();
				if (storingenTrein != null) {
					sample.setValue(GAUGE_STORINGEN_TREIN, storingenTrein);
				}
				Integer werkzaamhedenTrein = parser.getAantalWerkzaamheden();
				if (werkzaamhedenTrein != null) {
					sample.setValue(GAUGE_WERKZAAMHEDEN_TREIN, werkzaamhedenTrein);
				}
				BigDecimal beurskoers = parser.getBeurskoers();
				if (beurskoers != null) {
					sample.setValue(GAUGE_BEURSKOERS, beurskoers.doubleValue());
				}
				BigDecimal beurswinst = parser.getBeurswinst();
				if (beurswinst != null) {
					sample.setValue(GAUGE_BEURSWINST, beurswinst.doubleValue());
				}
				BigDecimal benzineprijs = parser.getBenzineprijs();
				if (benzineprijs != null) {
					sample.setValue(GAUGE_BENZINEPRIJS, benzineprijs.doubleValue());
				}
				sample.update();
				rrdDb.close();
			} catch (IOException e) {
				LOG.error("Could not create sample in RRD DB");
				e.printStackTrace();
			}
    	}
    }
    
    @Schedule(hour = "*", minute = "*", second = "15", persistent = false)
    public void createHourlyGraph() {
		LOG.info("Creating hourly graph");
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/Users/bjolamme/hourly.png");
		Calendar now = new GregorianCalendar();
		Calendar oneHourEarlier = new GregorianCalendar();
		oneHourEarlier.add(Calendar.HOUR, -1);
		gDef.setStartTime(Util.getTimestamp(oneHourEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Last hour");
		gDef.datasource("temperatuur", RRD_PATH, GAUGE_TEMPERATUUR, AVERAGE);
		gDef.datasource("filelengte", RRD_PATH, GAUGE_LENGTE_FILES, AVERAGE);
		gDef.datasource("aantal files", RRD_PATH, GAUGE_AANTAL_FILES, AVERAGE);
		gDef.area("aantal files", Color.GREEN, "aantal files");
		gDef.line("temperatuur", Color.RED, "temperatuur", 3f);
		gDef.line("filelengte", Color.BLUE, "filelengte", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		try {
			new RrdGraph(gDef);
			LOG.info("Wrote new hourly graph");
		} catch (IOException e) {
			LOG.error("Couldn't write hourly graph", e);
		}
    }
    
    private Document getRemoteStatus() {
    	Document doc;
		try {
			doc = Jsoup.connect("http://www.nu.nl/").get();
		} catch (IOException e) {
			LOG.error("Kon nu.nl HTML niet ophalen", e);
			doc = null;
		}
		return doc;
    }
    
    private RrdDef createRrdDef() {
    	String rrdPath = RRD_PATH;
		RrdDef rrdDef = new RrdDef(rrdPath, 60);
		rrdDef.addDatasource(GAUGE_TEMPERATUUR, DsType.GAUGE, 60, -30, 50);
		rrdDef.addDatasource(GAUGE_AANTAL_FILES, DsType.GAUGE, 60, 0, 200);
		rrdDef.addDatasource(GAUGE_LENGTE_FILES, DsType.GAUGE, 60, 0, 1500);
		rrdDef.addDatasource(GAUGE_STORINGEN_TREIN, DsType.GAUGE, 60, 0, 100);
		rrdDef.addDatasource(GAUGE_WERKZAAMHEDEN_TREIN, DsType.GAUGE, 60, 0, 100);
		rrdDef.addDatasource(GAUGE_BEURSKOERS, DsType.GAUGE, 60, 0, 2000);
		rrdDef.addDatasource(GAUGE_BEURSWINST, DsType.GAUGE, 60, 0, 100);
		rrdDef.addDatasource(GAUGE_BENZINEPRIJS, DsType.GAUGE, 60, 0, 5);
		// Keep all data for 1440 rows (= 1440 minutes = 24 hours = 1 day); half the data points must be valid
		rrdDef.addArchive(AVERAGE, 0, 1, 24 * 60);
		// Keep the aggregates of 5 minutes for 1 week
		rrdDef.addArchive(AVERAGE, 0.5, 5, 24 * 7 * (60 / 5));
		// Keep the aggregates of one hour for a year
		rrdDef.addArchive(AVERAGE, 0.5, 60, 366 * 24 * 7);
		return rrdDef;
    }

    private void checkRrdFile() {
    	File rrdFile = new File(rrdDef.getPath());
    	LOG.debug("checkRrdFile: " + rrdFile.getAbsolutePath());
    	if (rrdFile.exists()) {
    		LOG.info("RRD file exists");
    		
    		// TODO check validity
    	} else {
    		LOG.warn("RRD file does not exist");
    		try {
    			RrdDb db = new RrdDb(rrdDef);
    			db.close();
    		} catch (IOException e) {
    			LOG.fatal("Unable to create RRD file " + rrdFile.getAbsolutePath(), e);
    			throw new IllegalStateException("RRD file could not be created");
    		}
    	}
    }
}