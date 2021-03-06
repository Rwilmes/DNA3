package dna.series.aggdata;

import java.io.IOException;
import java.util.ArrayList;

import dna.io.Reader;
import dna.io.Writer;
import dna.util.Config;

public class AggregatedBinnedDistribution extends AggregatedDistribution {

	private double binsize;

	// constructor
	public AggregatedBinnedDistribution(String name) {
		super(name);
	}

	public AggregatedBinnedDistribution(String name, AggregatedValue[] values) {
		super(name, values);
		this.binsize = 1;
	}

	public AggregatedBinnedDistribution(String name, AggregatedValue[] values,
			double binsize) {
		super(name, values);
		this.binsize = binsize;
	}

	// IO methods
	public void write(String dir, String filename) throws IOException {
		Writer w = Writer.getWriter(dir, filename);

		for (int i = 0; i < super.getValues().length; i++) {
			String temp = "";
			for (int j = 0; j < super.getValues()[i].getValues().length; j++) {
				if (j == 0) {
					String v = Double
							.toString(super.getValues()[i].getValues()[j]
									* this.binsize);
					temp += v + Config.get("AGGREGATED_DATA_DELIMITER");
				} else {
					if (j == super.getValues()[i].getValues().length - 1)
						temp += super.getValues()[i].getValues()[j];
					else
						temp += super.getValues()[i].getValues()[j]
								+ Config.get("AGGREGATED_DATA_DELIMITER");
				}
			}
			w.writeln(temp);
		}
		w.close();
	}

	/**
	 * @param dir
	 *            String which contains the path to the directory the
	 *            AggregatedDistribution will be read from.
	 * 
	 * @param filename
	 *            String representing the filename the Distribution will be read
	 *            from.
	 * 
	 * @param readValues
	 *            Boolean. True: values from the file will be read. False: empty
	 *            Distribution will be created.
	 */
	public static AggregatedBinnedDistribution read(String dir,
			String filename, String name, boolean readValues)
			throws IOException {
		if (!readValues) {
			return new AggregatedBinnedDistribution(name, null);
		}
		Reader r = Reader.getReader(dir, filename);
		ArrayList<AggregatedValue> list = new ArrayList<AggregatedValue>();
		String line = null;

		while ((line = r.readString()) != null) {
			String[] temp = line.split(Config.get("AGGREGATED_DATA_DELIMITER"));
			double[] tempDouble = new double[temp.length];
			for (int i = 0; i < tempDouble.length; i++) {
				tempDouble[i] = Double.parseDouble(temp[i]);
			}

			AggregatedValue tempV = new AggregatedValue(name + temp[0],
					tempDouble);
			list.add(tempV);
		}
		AggregatedValue[] values = new AggregatedValue[list.size()];
		for (int i = 0; i < list.size(); i++) {
			values[i] = list.get(i);
		}
		r.close();
		return new AggregatedBinnedDistribution(name, values);
	}

	public static void write(String dir, String filename, double binsize,
			double[][] values) throws IOException {
		Writer w = Writer.getWriter(dir, filename);

		for (int i = 0; i < values.length; i++) {
			String temp = "";
			for (int j = 0; j < values[i].length; j++) {
				if (j == 0) {
					String v = Double.toString(values[i][j] * binsize);
					temp += v + Config.get("AGGREGATED_DATA_DELIMITER");
				} else {
					if (j == values[i].length - 1)
						temp += values[i][j];
					else
						temp += values[i][j]
								+ Config.get("AGGREGATED_DATA_DELIMITER");
				}
			}
			w.writeln(temp);
		}
		w.close();
	}
}
