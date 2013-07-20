/* ============================================================
 * Rrd4j : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.rrd4j.org
 * Project Lead:  Mathias Bogaert (m.bogaert@memenco.com)
 *
 * (C) Copyright 2003-2007, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic
 *
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package nl.nurdspace.rrd;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.junit.Test;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;

/**
 * Simple demo just to check that everything is OK with this library. Creates
 * two files in your $HOME/rrd4j-demo directory: demo.rrd and demo.png.
 */
public class InMemoryTest {
	static final long SEED = 1909752002L;
	static final Random RANDOM = new Random(SEED);
	static final String FILE = "demo";

	static final long START = Util.getTimestamp(2003, 4, 1);
	static final long END = Util.getTimestamp(2003, 5, 1);
	static final int MAX_STEP = 300;

	static final int IMG_WIDTH = 500;
	static final int IMG_HEIGHT = 300;

	@Test
	public void test() throws IOException {
		println("== Starting demo");
		long start = START;
		System.out.println(System.currentTimeMillis());
		System.out.println(start);
		long end = END;
		String rrdPath = Util.getRrd4jDemoPath(FILE + ".rrd");
		String logPath = Util.getRrd4jDemoPath(FILE + ".log");
		PrintWriter log = new PrintWriter(new BufferedOutputStream(new FileOutputStream(logPath, false)));
		// creation
		println("== Creating RRD file " + rrdPath);
		RrdDef rrdDef = new RrdDef(rrdPath, start - 1, 300);
		rrdDef.addDatasource("temperature", DsType.GAUGE, 600, -30, 50);
		rrdDef.addDatasource("open", DsType.GAUGE, 600, 0, 1);
		rrdDef.addDatasource("lightlevel", DsType.GAUGE, 600, 0, 1024);
		rrdDef.addArchive(AVERAGE, 0.5, 1, 600);
		rrdDef.addArchive(AVERAGE, 0.5, 6, 700);
		rrdDef.addArchive(AVERAGE, 0.5, 24, 775);
		rrdDef.addArchive(AVERAGE, 0.5, 288, 797);
		rrdDef.addArchive(MAX, 0.5, 1, 600);
		rrdDef.addArchive(MAX, 0.5, 6, 700);
		rrdDef.addArchive(MAX, 0.5, 24, 775);
		rrdDef.addArchive(MAX, 0.5, 288, 797);
		println(rrdDef.dump());
		log.println(rrdDef.dump());
		println("Estimated file size: " + rrdDef.getEstimatedSize());
		RrdDb.setDefaultFactory("MEMORY");
		RrdDb rrdDb = new RrdDb(rrdDef);
		println("== RRD file created.");
		if (rrdDb.getRrdDef().equals(rrdDef)) {
			println("Checking RRD file structure... OK");
		} else {
			println("Invalid RRD file created. This is a serious bug, bailing out");
			return;
		}

		rrdDb.close();
		println("== RRD file closed.");

		// update database
		GaugeSource lightSource = new GaugeSource(500, 5);
		println("== Simulating one month of RRD file updates with step not larger than " + MAX_STEP + " seconds (* denotes 1000 updates)");
		long t = start;
		int n = 0;
		rrdDb = new RrdDb(rrdPath);
		Sample sample = rrdDb.createSample();
		while (t <= end + 86400L) {
			sample.setTime(t);
//			sample.setValue("temperature", temperatureSource.getValue() / 10);
			String temp = "22.4C";
			String tempValue = temp.substring(0, temp.length() - 1);
			sample.setValue("temperature", Float.parseFloat(tempValue));
			sample.setValue("open", 1);
			sample.setValue("lightlevel", lightSource.getValue()/ 10);
			log.println(sample.dump());
			sample.update();
			t += RANDOM.nextDouble() * MAX_STEP + 1;
			if (((++n) % 1000) == 0) {
				System.out.print("*");
			}
		}
		rrdDb.close();
		println("");
		println("== Finished. RRD file updated " + n + " times");

		// test read-only access!
		rrdDb = new RrdDb(rrdPath, true);
		println("File reopen in read-only mode");
		println("== Last update time was: " + rrdDb.getLastUpdateTime());
		println("== Last info was: " + rrdDb.getInfo());

		// fetch data
		println("== Fetching data for the whole month");
		FetchRequest request = rrdDb.createFetchRequest(AVERAGE, start, end);
		println(request.dump());
		log.println(request.dump());
		FetchData fetchData = request.fetchData();
		println("== Data fetched. " + fetchData.getRowCount()
				+ " points obtained");
		println(fetchData.toString());
		println("== Dumping fetched data to XML format");
		println("== Fetch completed");
		double[] temperatures = fetchData.getValues("temperature");
		double[] lightlevels = fetchData.getValues("lightlevel");
		for (int i = 0; i < lightlevels.length; i++) {
			System.out.print("[" + lightlevels[i] + "]");
		}
		System.out.println();
		for (int i = 0; i < temperatures.length; i++) {
			System.out.print("[" + temperatures[i] + "]");
		}
		System.out.println();

	}

	static void println(String msg) {
		// System.out.println(msg + " " + Util.getLapTime());
		System.out.println(msg);

	}

	static void print(String msg) {
		System.out.print(msg);
	}

	static class GaugeSource {
		private double value;
		private double step;
		GaugeSource(double value, double step) {
			this.value = value;
			this.step = step;
		}
		long getValue() {
			double oldValue = value;
			double increment = RANDOM.nextDouble() * step;
			if (RANDOM.nextDouble() > 0.5) {
				increment *= -1;
			}
			value += increment;
			if (value <= 0) {
				value = 0;
			}
			return Math.round(oldValue);
		}
	}
}