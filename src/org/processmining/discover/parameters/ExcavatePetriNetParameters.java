package org.processmining.discover.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;

public class ExcavatePetriNetParameters implements ClassifierParameter{

	private XLog log;
	private XEventClassifier classifier;
	private List<Integer> absValues;
	private List<Integer> relValues;
	private double fitnessFactor;
	private double precisionFactor;
	private double simplicityFactor;
	private double sizeFactor;
	private double coverageFactor;
	private int nofThreads;
	private int maxNofTransitions;
	private boolean preferWFnet;
	private int discoveryPerc;
	private boolean useRecursiveExclusion;
	private int timeoutMillis;
//	private boolean preferContainAll; // Whether to keep all activities while filtering the log.

	/**
	 * Parameter settings selected last by the user.
	 * 
	 * EV 2025-07-23: Updated due to results of PDC 2024
	 */
	private XEventClassifier lastClassifier = null;
	private static List<Integer> lastAbsValues = null;
	private static List<Integer> lastRelValues = null;
	private static double lastFitnessFactor = 1.0;
	private static double lastPrecisionFactor = 0.05;
	private static double lastSimplicityFactor = 0.02;
	private static double lastSizeFactor = 0.02;
	private static double lastCoverageFactor = 0.02;
	private static int lastNofThreads = 4;
	private static int lastMaxNofTransitions = 200;
	private static boolean lastPreferWFnet = true;
	private static int lastDiscoveryPerc = 90;
	private static boolean lastUseRecursiveExclusion = false;
	private static int lastTimeoutMillis = 100 * 1000;
//	private static boolean lastPreferContainAll = true;

	public ExcavatePetriNetParameters(XLog log) {
		this.setLog(log);
		if (lastClassifier == null || log.getClassifiers().contains(lastClassifier)) {
			if (log.getClassifiers().isEmpty()) {
				lastClassifier = new XEventNameClassifier();
			} else {
				lastClassifier = log.getClassifiers().get(0);
			}
		}
		classifier = lastClassifier;
		if (lastAbsValues == null) {
			lastAbsValues = new ArrayList<Integer>();
			lastAbsValues.add(1);
			lastAbsValues.add(2);
			lastAbsValues.add(5);
		}
		absValues = new ArrayList<Integer>(lastAbsValues);
		if (lastRelValues == null) {
			lastRelValues = new ArrayList<Integer>();
			lastRelValues.add(1);
			lastRelValues.add(2);
		}
		relValues = new ArrayList<Integer>(lastRelValues);
		fitnessFactor = lastFitnessFactor;
		precisionFactor = lastPrecisionFactor;
		simplicityFactor = lastSimplicityFactor;
		sizeFactor = lastSizeFactor;
		coverageFactor = lastCoverageFactor;
		nofThreads = lastNofThreads;
		maxNofTransitions = lastMaxNofTransitions;
		preferWFnet = lastPreferWFnet;
		setDiscoveryPerc(lastDiscoveryPerc);
		setUseRecursiveExclusion(lastUseRecursiveExclusion);
		setTimeoutMillis(lastTimeoutMillis);
//		setPreferContainAll(lastPreferContainAll);
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		lastClassifier = classifier;
		this.classifier = classifier;
	}

	public List<Integer> getAbsValues() {
		return absValues;
	}

	public void setAbsValues(List<Integer> absValues) {
		lastAbsValues = new ArrayList<Integer>(absValues);
		this.absValues = new ArrayList<Integer>(absValues);
	}

	public List<Integer> getRelValues() {
		return relValues;
	}

	public void setRelValues(List<Integer> relValues) {
		lastRelValues = new ArrayList<Integer>(relValues);
		this.relValues = new ArrayList<Integer>(relValues);
	}

	public double getFitnessFactor() {
		return fitnessFactor;
	}

	public void setFitnessFactor(double fitnessFactor) {
		lastFitnessFactor = fitnessFactor;
		this.fitnessFactor = fitnessFactor;
	}

	public double getPrecisionFactor() {
		return precisionFactor;
	}

	public void setPrecisionFactor(double precisionFactor) {
		lastPrecisionFactor = precisionFactor;
		this.precisionFactor = precisionFactor;
	}

	public double getSimplicityFactor() {
		return simplicityFactor;
	}

	public void setSimplicityFactor(double simplicityFactor) {
		lastSimplicityFactor = simplicityFactor;
		this.simplicityFactor = simplicityFactor;
	}

	public double getSizeFactor() {
		return sizeFactor;
	}

	public void setSizeFactor(double sizeFactor) {
		lastSizeFactor = sizeFactor;
		this.sizeFactor = sizeFactor;
	}

	public int getNofThreads() {
		return nofThreads;
	}

	public void setNofThreads(int nofThreads) {
		lastNofThreads = nofThreads;
		this.nofThreads = nofThreads;
	}

	public int getMaxNofTransitions() {
		return maxNofTransitions;
	}

	public void setMaxNofTransitions(int maxNofTransitions) {
		lastMaxNofTransitions = maxNofTransitions;
		this.maxNofTransitions = maxNofTransitions;
	}

	public boolean isPreferWFnet() {
		return preferWFnet;
	}

	public void setPreferWFnet(boolean preferWFnet) {
		lastPreferWFnet = preferWFnet;
		this.preferWFnet = preferWFnet;
	}

	public XLog getLog() {
		return log;
	}

	public void setLog(XLog log) {
		this.log = log;
	}

	public int getDiscoveryPerc() {
		return discoveryPerc;
	}

	public void setDiscoveryPerc(int discoveryPerc) {
		lastDiscoveryPerc = discoveryPerc;
		this.discoveryPerc = discoveryPerc;
	}

//	public boolean isPreferContainAll() {
//		return preferContainAll;
//	}
//
//	public void setPreferContainAll(boolean preferContainAll) {
//		lastPreferContainAll = preferContainAll;
//		this.preferContainAll = preferContainAll;
//	}

	public double getCoverageFactor() {
		return coverageFactor;
	}

	public void setCoverageFactor(double coverageFactor) {
		lastCoverageFactor = coverageFactor;
		this.coverageFactor = coverageFactor;
	}

	public boolean isUseRecursiveExclusion() {
		return useRecursiveExclusion;
	}

	public void setUseRecursiveExclusion(boolean useRecursiveExclusion) {
		lastUseRecursiveExclusion = useRecursiveExclusion;
		this.useRecursiveExclusion = useRecursiveExclusion;
	}

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(int timeoutMillis) {
		lastTimeoutMillis = timeoutMillis;
		this.timeoutMillis = timeoutMillis;
	}

}
