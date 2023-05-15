package org.processmining.discover.parameters;

import java.util.ArrayList;
import java.util.List;

public class ExcavatePetriNetParameters {

	private List<Integer> absValues;
	private List<Integer> relValues;
	private double fitnessFactor;
	private double precisionFactor;
	private double simplicityFactor;
	private double sizeFactor;
	private int nofThreads;
	private int maxNofTransitions;

	/*
	 * Use dummy values. The constructor will then use the default values.
	 */
	private static List<Integer> lastAbsValues = null;
	private static List<Integer> lastRelValues = null;
	private static double lastFitnessFactor = -1.0;
	private static double lastPrecisionFactor = -1.0;
	private static double lastSimplicityFactor = -1.0;
	private static double lastSizeFactor = -1.0;
	private static int lastNofThreads = -1;
	private static int lastMaxNofTransitions = -1;

	public ExcavatePetriNetParameters() {
		if (lastAbsValues == null) {
			lastAbsValues = new ArrayList<Integer>();
			for (int i = 0; i < 6; i++) {
				lastAbsValues.add(i);
			}
		}
		absValues = new ArrayList<Integer>(lastAbsValues);
		if (lastRelValues == null) {
			lastRelValues = new ArrayList<Integer>();
			for (int i = 0; i < 10; i++) {
				lastRelValues.add(i);
			}
		}
		relValues = new ArrayList<Integer>(lastRelValues);
		if (lastFitnessFactor < 0.0) {
			lastFitnessFactor = 1.0;
		}
		fitnessFactor = lastFitnessFactor;
		if (lastPrecisionFactor < 0.0) {
			lastPrecisionFactor = 1.0;
		}
		precisionFactor = lastPrecisionFactor;
		if (lastSimplicityFactor < 0.0) {
			lastSimplicityFactor = 1.0;
		}
		simplicityFactor = lastSimplicityFactor;
		if (lastSizeFactor < 0.0) {
			lastSizeFactor = 1.0;
		}
		sizeFactor = lastSizeFactor;
		if (lastNofThreads < 0) {
			lastNofThreads = 6;
		}
		nofThreads = lastNofThreads;
		if (lastMaxNofTransitions < 0) {
			lastMaxNofTransitions = 100;
		}
		maxNofTransitions = lastMaxNofTransitions;
	}

	public List<Integer> getAbsValues() {
		return absValues;
	}

	public void setAbsValues(List<Integer> absValues) {
		this.lastAbsValues = new ArrayList<Integer>(absValues);
		this.absValues = new ArrayList<Integer>(absValues);
	}

	public List<Integer> getRelValues() {
		return relValues;
	}

	public void setRelValues(List<Integer> relValues) {
		this.lastRelValues = new ArrayList<Integer>(relValues);
		this.relValues = new ArrayList<Integer>(relValues);
	}

	public double getFitnessFactor() {
		return fitnessFactor;
	}

	public void setFitnessFactor(double fitnessFactor) {
		this.lastFitnessFactor = fitnessFactor;
		this.fitnessFactor = fitnessFactor;
	}

	public double getPrecisionFactor() {
		return precisionFactor;
	}

	public void setPrecisionFactor(double precisionFactor) {
		this.lastPrecisionFactor = precisionFactor;
		this.precisionFactor = precisionFactor;
	}

	public double getSimplicityFactor() {
		return simplicityFactor;
	}

	public void setSimplicityFactor(double simplicityFactor) {
		this.lastSimplicityFactor = simplicityFactor;
		this.simplicityFactor = simplicityFactor;
	}

	public double getSizeFactor() {
		return sizeFactor;
	}

	public void setSizeFactor(double sizeFactor) {
		this.lastSizeFactor = sizeFactor;
		this.sizeFactor = sizeFactor;
	}

	public int getNofThreads() {
		return nofThreads;
	}

	public void setNofThreads(int nofThreads) {
		this.lastNofThreads = nofThreads;
		this.nofThreads = nofThreads;
	}

	public int getMaxNofTransitions() {
		return maxNofTransitions;
	}

	public void setMaxNofTransitions(int maxNofTransitions) {
		this.lastMaxNofTransitions = maxNofTransitions;
		this.maxNofTransitions = maxNofTransitions;
	}

}
