package org.processmining.discover.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityLog;
import org.processmining.discover.models.ActivityMatrix;
import org.processmining.discover.models.ActivityMatrixCollection;
import org.processmining.discover.models.ActivitySet;
import org.processmining.discover.models.ActivitySets;
import org.processmining.log.parameters.ClassifierParameter;

public class DiscoverPetriNetParameters implements ClassifierParameter {

	/**
	 * Whether to merge on the activities.
	 */
	private boolean merge;

	/**
	 * Whether to reduce the Petri net.
	 */
	private boolean reduce;

	/**
	 * Whether to reduce all simple silent transitions.
	 */
	private boolean reduceAll;

	/**
	 * Whether to reduce restricted simple silent transitions.
	 */
	private boolean reduceRestricted;

	/**
	 * Whether to require unanimity for noise. If set, noise requires all
	 * matrices agree on it.
	 */
	private boolean vetoNoise;

	private boolean filterLog;
	
	/**
	 * The relative threshold to use.
	 */
	private int relativeThreshold;

	/**
	 * The absolute threshold to use.
	 */
	private int absoluteThreshold;

	/**
	 * The relative threshold to use for the sub matrices.
	 */
	private int relativeThreshold2;

	/**
	 * The absolute threshold to use for the sub matrices.
	 */
	private int absoluteThreshold2;

	/*
	 * The maximal number of S-components to take into account (use 0 for no
	 * limit).
	 */
	private int nofSComponents;

	private boolean useILP;

	private boolean useILP2;
	
	private boolean showGraph;

	private boolean addOccurrencePlaces;
	
	private boolean addEquivalencePlaces;
	
	private int nofTraces;
	
	private int maxTraceLength;
	
	private int nofThreads;

	/**
	 * The safety threshold to use.
	 */
	private int safetyThreshold;

	/**
	 * Mode to generate maximal activity sets.
	 */
	private int mode;
	
	/**
	 * The safety threshold to use for the sub matrices.
	 */
	private int safetyThreshold2;
	
	private int maxNofRoutingTransitions;
	/**
	 * The percentage threshold for concurrent pairs 
	 * A concurrent pair is ignored it its score does not exceed this percentage of the maximal score.
	 */
//	private int percentage;

	private XEventClassifier classifier;

	private List<String> activities;

	private ActivityAlphabet alphabet;

	private ActivityLog log;

	private ActivityMatrix matrix;

	private ActivitySets activitySets;

	private ActivitySets allActivitySets;

	private ActivityMatrixCollection matrixCollection;
	
	/**
	 * Parameter settings selected last by the user.
	 * 
	 * EV 2025-07-23: Updated due to results of PDC 2024
	 */
	private static boolean lastMerge = true;
	private static boolean lastReduce = true;
	private static boolean lastReduceAll = false;
	private static boolean lastReduceRestricted = false;
	private static boolean lastVetoNoise = false;
	private static boolean lastFilterLog = true;
	private static int lastAbsoluteThreshold = 0; // These seem reasonable values.
	private static int lastAbsoluteThreshold2 = 0; 
	private static int lastRelativeThreshold = 1;
	private static int lastRelativeThreshold2 = 1;
	private static int lastSafetyThreshold = 95;
	private static int lastSafetyThreshold2 = 95;
//	private static int lastPercentage = 0;
	private static int lastMode = ActivitySets.MODE_ALL;
	private static int LastNofSComponents = 10; // Seems more than enough.
	private static boolean lastUseILP = false;
	private static boolean lastUseILP2 = true;
	private static boolean lastShowGraph = false;
	private static boolean lastAddOccurrencePlaces = true;	
	private static boolean lastAddEquivalencePlaces = true;
	private static int lastNofTraces = 250;
	private static int lastMaxTraceLength = 100;
	private static int lastNofThreads = 4;
	private static int lastMaxNofRoutingTransitions = 50;

	/**
	 * Creates default parameter settings.
	 */
	public DiscoverPetriNetParameters() {
		setMerge(lastMerge);
		setReduce(lastReduce);
		setReduceAll(lastReduceAll);
		setReduceRestricted(lastReduceRestricted);
		setVetoNoise(lastVetoNoise);
		setFilterLog(lastFilterLog);
		setRelativeThreshold(lastRelativeThreshold);
		setRelativeThreshold2(lastRelativeThreshold2);
		setAbsoluteThreshold(lastAbsoluteThreshold);
		setAbsoluteThreshold2(lastAbsoluteThreshold2);
		setSafetyThreshold(lastSafetyThreshold);
		setSafetyThreshold2(lastSafetyThreshold2);
//		setPercentage(lastPercentage);
		setNofSComponents(LastNofSComponents);
		setMode(lastMode);
		setUseILP(lastUseILP);
		setUseILP2(lastUseILP2);
		setShowGraph(lastShowGraph);
		setAddOccurrencePlaces(lastAddOccurrencePlaces);
		setAddEquivalencePlaces(lastAddEquivalencePlaces);
		setClassifier(null, false);
		setActivities(null, false);
		setAlphabet(null, false);
		setLog(null, false);
		setMatrix(null, false);
		setActivitySets(null, false);
		setAllActivitySets(null, false);
		setMatrixCollection(null, false);
		setNofTraces(lastNofTraces);
		setMaxTraceLength(lastMaxTraceLength);
		setNofThreads(lastNofThreads);
		setMaxNofRoutingTransitions(lastMaxNofRoutingTransitions);
	}
	
	/*
	 * Getters and setters
	 */

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		lastMerge = merge;
		this.merge = merge;
	}

	public int getRelativeThreshold() {
		return relativeThreshold;
	}

	public void setRelativeThreshold(int relativeThreshold) {
		lastRelativeThreshold = relativeThreshold;
		this.relativeThreshold = relativeThreshold;
		setRelativeThreshold2(relativeThreshold);
	}

	public int getAbsoluteThreshold() {
		return absoluteThreshold;
	}

	public void setAbsoluteThreshold(int absoluteThreshold) {
		lastAbsoluteThreshold = absoluteThreshold;
		this.absoluteThreshold = absoluteThreshold;
		setAbsoluteThreshold2(absoluteThreshold);
	}

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		lastReduce = reduce;
		this.reduce = reduce;
	}

	public boolean isVetoNoise() {
		return vetoNoise;
	}

	public void setVetoNoise(boolean majority) {
		lastVetoNoise = majority;
		this.vetoNoise = majority;
	}

	public int getNofSComponents() {
		return nofSComponents;
	}

	public void setNofSComponents(int nofSComponents) {
		LastNofSComponents = nofSComponents;
		this.nofSComponents = nofSComponents;
	}

	public int getSafetyThreshold() {
		return safetyThreshold;
	}

	public void setSafetyThreshold(int safetyThreshold) {
		lastSafetyThreshold = safetyThreshold;
		this.safetyThreshold = safetyThreshold;
		setSafetyThreshold2(safetyThreshold);
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		setClassifier(classifier, true);
	}

	private void setClassifier(XEventClassifier classifier, boolean propagate) {
		if (this.classifier == null || !this.classifier.equals(classifier)) {
			this.classifier = classifier;
			if (propagate) {
				setActivities(null);
			}
		}
	}

	public List<String> getActivities() {
		return activities;
	}

	public void setActivities(List<String> activities) {
		setActivities(activities, true);
	}

	private void setActivities(List<String> activities, boolean propagate) {
		if (this.activities == null || !this.activities.equals(activities)) {
			this.activities = (activities == null ? null : new ArrayList<String>(activities));
			if (propagate) {
				setLog(null);
			}
			//			setAlphabet(activities == null ? null : new ActivityAlphabet(activities));
		}
	}

	public ActivityAlphabet getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(ActivityAlphabet alphabet) {
		setAlphabet(alphabet, true);
	}

	private void setAlphabet(ActivityAlphabet alphabet, boolean propagate) {
		if (this.alphabet == null || !this.alphabet.equals(alphabet)) {
			this.alphabet = (alphabet == null ? null : new ActivityAlphabet(alphabet));
			//			setLog(null);
		}
	}

	public ActivityLog getLog() {
		return log;
	}

	public void setLog(ActivityLog log) {
		setLog(log, true);
	}

	private void setLog(ActivityLog log, boolean propagate) {
		if (this.log == null || !this.log.equals(log)) {
			this.log = (log == null ? null : new ActivityLog(log));
			if (propagate) {
				setMatrix(null);
			}
		}
	}

	public ActivityMatrix getMatrix() {
		return matrix;
	}

	public void setMatrix(ActivityMatrix matrix) {
		setMatrix(matrix, true);
	}
	
	public void setMatrix(ActivityMatrix matrix, boolean propagate) {
		if (this.matrix == null || !this.matrix.equals(matrix)) {
			this.matrix = (matrix == null ? null : new ActivityMatrix(matrix));
			if (propagate) {
				setActivitySets(null);
				setAllActivitySets(null);
			}
		}
	}

	public ActivitySets getActivitySets() {
		return activitySets;
	}

	public void setActivitySets(List<ActivitySet> activitySets) {
		setActivitySets(activitySets, true);
	}
	public void setActivitySets(List<ActivitySet> activitySets, boolean propagate) {
		if (this.activitySets == null || !this.activitySets.equals(activitySets)) {
			this.activitySets = (activitySets == null ? null : new ActivitySets(activitySets));
			if (propagate) {
				setMatrixCollection(null);
			}
		}
	}

	public ActivitySets getAllActivitySets() {
		return (allActivitySets == null ? null : new ActivitySets(allActivitySets));
	}

	public void setAllActivitySets(List<ActivitySet> allActivitySets) {
		setAllActivitySets(allActivitySets, true);
	}
	
	public void setAllActivitySets(List<ActivitySet> allActivitySets, boolean propagate) {
		if (this.allActivitySets == null || !this.allActivitySets.equals(allActivitySets)) {
			this.allActivitySets = (allActivitySets == null ? null : new ActivitySets(allActivitySets));
		}
	}

	public ActivityMatrixCollection getMatrixCollection() {
		return matrixCollection;
	}

	public void setMatrixCollection(ActivityMatrixCollection matrixCollection) {
		setMatrixCollection(matrixCollection, true);
	}
	
	private void setMatrixCollection(ActivityMatrixCollection matrixCollection, boolean propagate) {
		if (this.matrixCollection == null || !this.matrixCollection.equals(matrixCollection)) {
			this.matrixCollection = (matrixCollection == null ? null : new ActivityMatrixCollection(matrixCollection));
		}
	}

	public boolean isUseILP() {
		return useILP;
	}

	public void setUseILP(boolean useILP) {
		lastUseILP = useILP;
		this.useILP = useILP;
	}

	public boolean isReduceAll() {
		return reduceAll;
	}

	public void setReduceAll(boolean reduceAll) {
		lastReduceAll = reduceAll;
		this.reduceAll = reduceAll;
	}

	public boolean isReduceRestricted() {
		return reduceRestricted;
	}

	public void setReduceRestricted(boolean reduceRestricted) {
		lastReduceRestricted = reduceRestricted;
		this.reduceRestricted = reduceRestricted;
	}

	public int getAbsoluteThreshold2() {
		return absoluteThreshold2;
	}

	public void setAbsoluteThreshold2(int absoluteThreshold2) {
		lastAbsoluteThreshold2 = absoluteThreshold2;
		this.absoluteThreshold2 = absoluteThreshold2;
	}

	public boolean isUseILP2() {
		return useILP2;
	}

	public void setUseILP2(boolean useILP2) {
		lastUseILP2 = useILP2;
		this.useILP2 = useILP2;
	}

	public int getRelativeThreshold2() {
		return relativeThreshold2;
	}

	public void setRelativeThreshold2(int relativeThreshold2) {
		lastRelativeThreshold2 = relativeThreshold2;
		this.relativeThreshold2 = relativeThreshold2;
	}

	public int getSafetyThreshold2() {
		return safetyThreshold2;
	}

	public void setSafetyThreshold2(int safetyThreshold2) {
		lastSafetyThreshold2 = safetyThreshold2;
		this.safetyThreshold2 = safetyThreshold2;
	}

	public boolean isShowGraph() {
		return showGraph;
	}

	public void setShowGraph(boolean showGraph) {
		lastShowGraph = showGraph;
		this.showGraph = showGraph;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		DiscoverPetriNetParameters.lastMode = mode;
		this.mode = mode;
	}

	public boolean isFilterLog() {
		return filterLog;
	}

	public void setFilterLog(boolean filterLog) {
//		System.out.println("[DiscoverPetriNetParameters] Set filte rlog to " + filterLog);
		lastFilterLog = filterLog;
		this.filterLog = filterLog;
	}

	public boolean isAddOccurrencePlaces() {
		return addOccurrencePlaces;
	}

	public void setAddOccurrencePlaces(boolean addUnaryPlaces) {
		lastAddOccurrencePlaces = addUnaryPlaces;
		this.addOccurrencePlaces = addUnaryPlaces;
	}

	public boolean isAddEquivalencePlaces() {
		return addEquivalencePlaces;
	}

	public void setAddEquivalencePlaces(boolean addBinaryPlaces) {
		lastAddEquivalencePlaces = addBinaryPlaces;
		this.addEquivalencePlaces = addBinaryPlaces;
	}

	public int getNofTraces() {
		return nofTraces;
	}

	public void setNofTraces(int nofTraces) {
		lastNofTraces = nofTraces;
		this.nofTraces = nofTraces;
	}

	public int getMaxTraceLength() {
		return maxTraceLength;
	}

	public void setMaxTraceLength(int maxTraceLength) {
		lastMaxTraceLength = maxTraceLength;
		this.maxTraceLength = maxTraceLength;
	}

	public int getNofThreads() {
		return nofThreads;
	}

	public void setNofThreads(int nofThreads) {
		lastNofThreads = nofThreads;
		this.nofThreads = nofThreads;
	}

	public int getMaxNofRoutingTransitions() {
		return maxNofRoutingTransitions;
	}

	public void setMaxNofRoutingTransitions(int maxNofRoutingTransitions) {
		lastMaxNofRoutingTransitions = maxNofRoutingTransitions;
		this.maxNofRoutingTransitions = maxNofRoutingTransitions;
	}

//	public int getPercentage() {
//		return percentage;
//	}
//
//	public void setPercentage(int percentage) {
//		DiscoverPetriNetParameters.lastPercentage = percentage;
//		this.percentage = percentage;
//	}
}
