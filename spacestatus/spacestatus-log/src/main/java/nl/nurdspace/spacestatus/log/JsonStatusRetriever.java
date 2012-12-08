package nl.nurdspace.spacestatus.log;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

import java.io.IOException;

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

@Startup
@Singleton
@LocalBean
public class JsonStatusRetriever {
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
    
    @Schedule(hour = "*", minute = "*")
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
					RrdDb rrdDb = new RrdDb(rrdDef);
					Sample sample = rrdDb.createSample();
					sample.setTime(System.currentTimeMillis());
					if (temp != null) {
						String tempValue = temp.substring(0, temp.length() - 1);
						sample.setValue("temperature", Float.parseFloat(tempValue));
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
    	String rrdPath = "/home/spacebot/rrd/status.rrd";
		RrdDef rrdDef = new RrdDef(rrdPath, 60);
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