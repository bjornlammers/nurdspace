package nl.nurdspace.rrd;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

import java.io.IOException;
import java.util.Date;

import nl.nurdspace.spacestatus.log.JsonStatusRetriever;

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
import org.junit.Test;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;

public class RrdFromStatusToLiveFileTest {
    private static final Logger LOG = Logger.getLogger(JsonStatusRetriever.class);
	
    private final HttpClient client;
    private final RrdDef rrdDef;
    private final long start;
    
    public RrdFromStatusToLiveFileTest() {
		client = new HttpClient();
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(1000);
    	client.setParams(params);
    	start = Util.getTimestamp(new Date());
    	rrdDef = createRrdDef(start);
    }
    
    @Test
    public void testVulDb() throws IOException {
		RrdDb.setDefaultFactory("FILE");
		RrdDb rrdDb = new RrdDb(rrdDef);
		rrdDb.close();
		for (int i = 0; i < 10; i++) {
			JSONObject status = getRemoteStatus();
    		Boolean open = (Boolean) status.get("open");
    		JSONArray sensors = (JSONArray) status.get("sensors");
    		String temp = null;
    		for (Object sensorObject : sensors) {
				JSONObject sensorJson = (JSONObject) sensorObject;
				JSONObject tempObject = (JSONObject) sensorJson.get("temp");
				temp = (String) tempObject.get("space");
			}
    		LOG.info("space: " + (Boolean.TRUE.equals(open) ? "open" : "closed") + ", temperature: " + temp);

    		rrdDb = new RrdDb("/Users/bjolamme/space-status.rrd", false);
    		Sample sample = rrdDb.createSample();
    		sample.setTime(Util.getTimestamp(new Date()));
			String tempValue = temp.substring(0, temp.length() - 1);
			sample.setValue("temperature", Float.parseFloat(tempValue));
			sample.setValue("lightlevel", 12.34);
			sample.setValue("open", Boolean.TRUE.equals(open) ? 1 : 0);
			sample.update();
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ie) {
				LOG.error("Interrupted!", ie);
			}
			rrdDb.close();
		}
		long end = Util.getTimestamp(new Date());
		
		rrdDb = new RrdDb("/Users/bjolamme/space-status.rrd", true);

		// fetch data
		FetchRequest request = rrdDb.createFetchRequest(AVERAGE, start, end);
		FetchData fetchData = request.fetchData();
		double[] temperatures = fetchData.getValues("temperature");
		double[] lightlevels = fetchData.getValues("lightlevel");
		LOG.info("Aantal temperaturen: " + temperatures.length);
		for (int i = 0; i < lightlevels.length; i++) {
			System.out.print("[" + lightlevels[i] + "]");
		}
		System.out.println();
		for (int i = 0; i < temperatures.length; i++) {
			System.out.print("[" + temperatures[i] + "]");
		}
		System.out.println();
		LOG.info("Aantal lightlevels: " + lightlevels.length);
		
		rrdDb.close();
    }
    
    private RrdDef createRrdDef(long start) {
    	String rrdPath = "/Users/bjolamme/space-status.rrd";
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
    
    private JSONObject getRemoteStatus() {
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
		JSONObject status;
		try {
    		JSONParser parser = new JSONParser();
    		status = (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			status = null;
			LOG.error("error parsing json string", e);
		}
		return status;
    }
}
