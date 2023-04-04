package org.processmining.discover.parameters;

import java.util.ArrayList;
import java.util.List;

public class ExcavatePetriNetParameters {

	private List<Integer> absValues;
	private List<Integer> relValues;
	private double fitnessFactor;
	private double precisionFactor;
	private double simplicityFactor;
	private int nofThreads;

	private static List<Integer> lastAbsValues = null;
	private static List<Integer> lastRelValues = null;
	private static double lastFitnessFactor = 1.0;
	private static double lastPrecisionFactor = 1.0;
	private static double lastSimplicityFactor = 1.0;
	private static int lastNofThreads = 6;

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
			for (int i = 10; i < 30; i += 2) {
				lastRelValues.add(i);
			}
			for (int i = 30; i < 100; i += 5) {
				lastRelValues.add(i);
			}
		}
		relValues = new ArrayList<Integer>(lastRelValues);
		fitnessFactor = lastFitnessFactor;
		precisionFactor = lastPrecisionFactor;
		simplicityFactor = lastSimplicityFactor;
		nofThreads = lastNofThreads;
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

	public int getNofThreads() {
		return nofThreads;
	}

	public void setNofThreads(int nofThreads) {
		this.lastNofThreads = nofThreads;
		this.nofThreads = nofThreads;
	}

}
