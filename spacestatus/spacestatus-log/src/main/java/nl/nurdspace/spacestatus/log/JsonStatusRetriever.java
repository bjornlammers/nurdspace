package nl.nurdspace.spacestatus.log;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

@Startup
@Singleton
@LocalBean
public class JsonStatusRetriever {
    private static final String RRD_PATH = "/home/spacebot/rrd/status.rrd";
    private static final String JSON_PATH = "/home/spacebot/webdir/spaceapi/status.json";

	private static final Logger LOG = Logger.getLogger(JsonStatusRetriever.class);

    private final HttpClient client;
    private final RrdDef rrdDef;
    
    public JsonStatusRetriever() {
		client = new HttpClient();
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(1000);
    	client.setParams(params);
    	rrdDef = createRrdDef();
    }
    
    @Schedule(hour = "*", minute = "*", second = "0,10,20,30,40,50", persistent = false)
    public void removeOldReports() {
    	String json = getRemoteStatus();
    	if (json == null) {
    		LOG.warn("status kon niet opgehaald worden");
    	} else {
    		LOG.debug("json: " + json);
    		saveSpaceApi(json);
    		try {
        		JSONParser parser = new JSONParser();
        		JSONObject status = (JSONObject) parser.parse(json);
        		Boolean open = (Boolean) status.get("open");
        		JSONArray sensors = (JSONArray) status.get("sensors");
        		String temp = null;
        		for (Object sensorObject : sensors) {
					JSONObject sensorJson = (JSONObject) sensorObject;
					JSONObject tempObject = (JSONObject) sensorJson.get("temp");
					temp = (String) tempObject.get("space");
				}
        		LOG.info("space: " + (Boolean.TRUE.equals(open) ? "open" : "closed") + ", temperature: " + temp);
        		try {
        			if (!new File(RRD_PATH).exists()) {
        				RrdDb rrd = new RrdDb(rrdDef);
        				LOG.info("RRD file opnieuw aangemaakt");
        				rrd.close();
        			}
					RrdDb rrdDb = new RrdDb(RRD_PATH);
					Sample sample = rrdDb.createSample();
					long timestamp = Util.getTimestamp(new Date());
					LOG.info("timestamp: " + timestamp);
					sample.setTime(timestamp);
					if (temp != null) {
						String tempValue = temp.substring(0, temp.length() - 1);
						sample.setValue("temperature", Double.parseDouble(tempValue));
					}
					sample.setValue("open", Boolean.TRUE.equals(open) ? 1 : 0);
//					sample.setValue("lightlevel", lightSource.getValue()/ 10);
					sample.update();
					rrdDb.close();
				} catch (IOException e) {
					LOG.error("waarden kunnen niet naar rrdDb geschreven worden");
					e.printStackTrace();
				}
    		} catch (ParseException e) {
    			LOG.error("error parsing json string", e);
    		}
    	}
    }
    
//    @Schedule(hour = "*", minute = "0,5,10,15,20,25,30,35,40,45,50,55", second = "15")
    @Schedule(hour = "*", minute = "*", second = "15", persistent = false)
    public void createGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/webdir/hourly-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneHourEarlier = new GregorianCalendar();
		oneHourEarlier.add(Calendar.HOUR, -1);
		gDef.setStartTime(Util.getTimestamp(oneHourEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last hour");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Uur-graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon uur-graph niet schrijven", e);
		}
		
		gDef = new RrdGraphDef();
		gDef.setWidth(160);
		gDef.setHeight(96);
		gDef.setFilename("/home/spacebot/webdir/hourly-temp-small.png");
		gDef.setStartTime(Util.getTimestamp(oneHourEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");
		gDef.setNoLegend(true);

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Small week graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon small week graph niet schrijven", e);
		}
    }
    
    @Schedule(hour = "*", minute = "0,15,30,45", persistent = false)
    public void createDayGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/webdir/daily-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneDayEarlier = new GregorianCalendar();
		oneDayEarlier.add(Calendar.DAY_OF_YEAR, -1);
		gDef.setStartTime(Util.getTimestamp(oneDayEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last day");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Dag-graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon dag-graph niet schrijven", e);
		}
		
		
		gDef = new RrdGraphDef();
		gDef.setWidth(160);
		gDef.setHeight(96);
		gDef.setFilename("/home/spacebot/webdir/daily-temp-small.png");
		gDef.setStartTime(Util.getTimestamp(oneDayEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");
		gDef.setNoLegend(true);

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Small day graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon small day graph niet schrijven", e);
		}
    }
    
    @Schedule(hour = "1,4,7,10,13,16,19,22", minute = "25", persistent = false)
    public void createWeekGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/webdir/weekly-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneWeekEarlier = new GregorianCalendar();
		oneWeekEarlier.add(Calendar.DAY_OF_YEAR, -7);
		gDef.setStartTime(Util.getTimestamp(oneWeekEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last week");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Week-graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon week-graph niet schrijven", e);
		}
		
		gDef = new RrdGraphDef();
		gDef.setWidth(160);
		gDef.setHeight(96);
		gDef.setFilename("/home/spacebot/webdir/weekly-temp-small.png");
		gDef.setStartTime(Util.getTimestamp(oneWeekEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");
		gDef.setNoLegend(true);

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Small week graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon small week graph niet schrijven", e);
		}
    }
    
    
    @Schedule(hour = "13", minute = "20", persistent = false)
    public void createMonthGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/webdir/monthly-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneMonthEarlier = new GregorianCalendar();
		oneMonthEarlier.add(Calendar.MONTH, -1);
		gDef.setStartTime(Util.getTimestamp(oneMonthEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last month");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Month-graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon month-graph niet schrijven", e);
		}

		gDef = new RrdGraphDef();
		gDef.setWidth(160);
		gDef.setHeight(96);
		gDef.setFilename("/home/spacebot/webdir/monthly-temp-small.png");
		gDef.setStartTime(Util.getTimestamp(oneMonthEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.datasource("open", RRD_PATH, "open", AVERAGE);
		gDef.background("open", new Color(200, 255, 200));
		gDef.line("temperature", Color.RED, "temperature", 3f);
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");
		gDef.setNoLegend(true);

		// create graph finally
		try {
			RrdGraph graph = new RrdGraph(gDef);
			LOG.info("Small week graph weggeschreven");
		} catch (IOException e) {
			LOG.error("Kon small week graph niet schrijven", e);
		}
    }
    
    private String getRemoteStatus() {
		HttpMethod method = new GetMethod("http://dockstar:8000/status");
		String response = null;
		try {
			client.executeMethod(method);
			response = new String(method.getResponseBody());
			method.releaseConnection();
		} catch (HttpException e) {
			LOG.error("fout bij ophalen remote status");
		} catch (IOException e) {
			LOG.error("fout bij ophalen remote status");
		}
		return response;
    }
    
    private RrdDef createRrdDef() {
    	String rrdPath = RRD_PATH;
		RrdDef rrdDef = new RrdDef(rrdPath, Util.getTimestamp(2012, 1, 1) -1, 60);
		rrdDef.addDatasource("temperature", DsType.GAUGE, 60, -30, 50);
		rrdDef.addDatasource("open", DsType.GAUGE, 60, 0, 1);
		rrdDef.addDatasource("lightlevel", DsType.GAUGE, 60, 0, 1024);
		rrdDef.addArchive(AVERAGE, 0.5, 1, 600);
		rrdDef.addArchive(AVERAGE, 0.5, 6, 700);
		rrdDef.addArchive(AVERAGE, 0.5, 24, 775);
		rrdDef.addArchive(AVERAGE, 0.5, 288, 797);
		rrdDef.addArchive(MAX, 0.5, 1, 600);
		rrdDef.addArchive(MAX, 0.5, 6, 700);
		rrdDef.addArchive(MAX, 0.5, 24, 775);
		rrdDef.addArchive(MAX, 0.5, 288, 797);
		rrdDef.addArchive(MIN, 0.5, 1, 600);
		rrdDef.addArchive(MIN, 0.5, 6, 700);
		rrdDef.addArchive(MIN, 0.5, 24, 775);
		rrdDef.addArchive(MIN, 0.5, 288, 797);
		return rrdDef;
    }
    
    private void saveSpaceApi(final String json) {
    	File jsonFile = new File(JSON_PATH);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(jsonFile, false));
			writer.write(json + "\n");
			writer.flush();
			LOG.info("saveSpaceApi: succesfully updated JSON");
		} catch (IOException ioe) {
			LOG.error("saveSpaceApi: error while writing status file", ioe);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ioe) {
					LOG.error("saveSpaceApi: error while closing writer", ioe);
				}
			}
		}
    }
}