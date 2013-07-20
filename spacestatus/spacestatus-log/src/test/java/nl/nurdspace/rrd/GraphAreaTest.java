package nl.nurdspace.rrd;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.awt.Color;
import java.io.IOException;

import org.junit.Test;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public class GraphAreaTest {
	@Test
	public void createGraph() throws IOException {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(600);
		gDef.setHeight(400);
		gDef.setFilename("areagraph.png");
		gDef.setStartTime(Util.getTimestamp(2012, 11, 1));
		gDef.setEndTime(Util.getTimestamp(2012, 11, 15));
		gDef.setTitle("Temperatuur en openingstijden");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("temperature", "/Users/bjolamme/status.rrd", "temperature", AVERAGE);
		gDef.datasource("open", "/Users/bjolamme/status.rrd", "open", AVERAGE);
		gDef.line("temperature", Color.GREEN, "temperature");
		gDef.stack("open", Color.BLUE, "open");
//		gDef.gprint("sun", MAX, "maxSun = %.3f%s");
//		gDef.gprint("sun", AVERAGE, "avgSun = %.3f%S\\r");
//		gDef.gprint("shade", MAX, "maxShade = %.3f%S");
//		gDef.gprint("shade", AVERAGE, "avgShade = %.3f%S\\r");
//		gDef.print("sun", MAX, "maxSun = %.3f%s");
//		gDef.print("sun", AVERAGE, "avgSun = %.3f%S\\r");
//		gDef.print("shade", MAX, "maxShade = %.3f%S");
//		gDef.print("shade", AVERAGE, "avgShade = %.3f%S\\r");
		gDef.setImageInfo("");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		// create graph finally
		RrdGraph graph = new RrdGraph(gDef);

	}
}
