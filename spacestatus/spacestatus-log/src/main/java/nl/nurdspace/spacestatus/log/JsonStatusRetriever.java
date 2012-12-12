package nl.nurdspace.spacestatus.log;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

import java.awt.Color;
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
    
    @Schedule(hour = "*", minute = "*", second = "0")
    public void removeOldReports() {
    	String json = getRemoteStatus();
    	if (json == null) {
    		LOG.warn("status kon niet opgehaald worden");
    	} else {
    		LOG.debug("json: " + json);
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
					RrdDb rrdDb = new RrdDb(RRD_PATH);
					Sample sample = rrdDb.createSample();
					sample.setTime(Util.getTimestamp(new Date()));
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
    
    @Schedule(hour = "*", minute = "*", second = "15")
    public void createGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/rrd/hourly-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneHourEarlier = new GregorianCalendar();
		oneHourEarlier.add(Calendar.HOUR, -1);
		gDef.setStartTime(Util.getTimestamp(oneHourEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last hour");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.line("temperature", Color.GREEN, "temperature");
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
    }
    
    @Schedule(hour = "*", minute = "0,10,20,30,40,50")
    public void createDayGraph() {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(800);
		gDef.setHeight(600);
		gDef.setFilename("/home/spacebot/rrd/daily-temp.png");
		Calendar now = new GregorianCalendar();
		Calendar oneDayEarlier = new GregorianCalendar();
		oneDayEarlier.add(Calendar.DAY_OF_YEAR, -1);
		gDef.setStartTime(Util.getTimestamp(oneDayEarlier));
		gDef.setEndTime(Util.getTimestamp(now));
		gDef.setTitle("Temperatures last day");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", RRD_PATH, "temperature", AVERAGE);
		gDef.line("temperature", Color.GREEN, "temperature");
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
}