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

	/**
	 * The absolute threshold to use.
	 */
	private int relativeThreshold;

	/**
	 * The relative threshold to use.
	 */
	private int absoluteThreshold;

	/*
	 * The maximal number of S-components to take into account (use 0 for no
	 * limit).
	 */
	private int nofSComponents;

	private boolean useILP;
	/**
	 * The safety threshold to use.
	 */
	private int safetyThreshold;

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
	 */
	private static boolean lastMerge = true;
	private static boolean lastReduce = true;
	private static boolean lastReduceAll = false;
	private static boolean lastReduceRestricted = false;
	private static boolean lastVetoNoise = false;
	private static int lastAbsoluteThreshold = 1; // These seem reasonable values.
	private static int lastRelativeThreshold = 1;
	private static int lastSafetyThreshold = 95;
	private static int LastNofSComponents = 20; // Seems more than enough.
	private static boolean lastUseILP = true;
	private static XEventClassifier lastClassifier = null;
	private static List<String> lastActivities = null;
	private static ActivityAlphabet lastAlphabet = null;
	private static ActivityLog lastLog = null;
	private static ActivityMatrix lastMatrix = null;
	private static ActivitySets lastActivitySets = null;
	private static ActivitySets lastAllActivitySets = null;
	private static ActivityMatrixCollection lastMatrixCollection = null;

	/**
	 * Creates default parameter settings.
	 */
	public DiscoverPetriNetParameters() {
		setMerge(lastMerge);
		setReduce(lastReduce);
		setReduceAll(lastReduceAll);
		setReduceRestricted(lastReduceRestricted);
		setVetoNoise(lastVetoNoise);
		setRelativeThreshold(lastRelativeThreshold);
		setAbsoluteThreshold(lastAbsoluteThreshold);
		setSafetyThreshold(lastSafetyThreshold);
		setNofSComponents(LastNofSComponents);
		setUseILP(lastUseILP);
		setClassifier(lastClassifier, false);
		setActivities(lastActivities, false);
		setAlphabet(lastAlphabet, false);
		setLog(lastLog, false);
		setMatrix(lastMatrix, false);
		setActivitySets(lastActivitySets, false);
		setAllActivitySets(lastAllActivitySets, false);
		setMatrixCollection(lastMatrixCollection, false);
	}

	/*
	 * Getters and setters
	 */

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.lastMerge = merge;
		this.merge = merge;
	}

	public int getRelativeThreshold() {
		return relativeThreshold;
	}

	public void setRelativeThreshold(int relativeThreshold) {
		this.lastRelativeThreshold = relativeThreshold;
		this.relativeThreshold = relativeThreshold;
	}

	public int getAbsoluteThreshold() {
		return absoluteThreshold;
	}

	public void setAbsoluteThreshold(int absoluteThreshold) {
		this.lastAbsoluteThreshold = absoluteThreshold;
		this.absoluteThreshold = absoluteThreshold;
	}

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		this.lastReduce = reduce;
		this.reduce = reduce;
	}

	public boolean isVetoNoise() {
		return vetoNoise;
	}

	public void setVetoNoise(boolean majority) {
		this.lastVetoNoise = majority;
		this.vetoNoise = majority;
	}

	public int getNofSComponents() {
		return nofSComponents;
	}

	public void setNofSComponents(int nofSComponents) {
		this.LastNofSComponents = nofSComponents;
		this.nofSComponents = nofSComponents;
	}

	public int getSafetyThreshold() {
		return safetyThreshold;
	}

	public void setSafetyThreshold(int safetyThreshold) {
		this.lastSafetyThreshold = safetyThreshold;
		this.safetyThreshold = safetyThreshold;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		setClassifier(classifier, true);
	}

	private void setClassifier(XEventClassifier classifier, boolean propagate) {
		if (this.classifier == null || !this.classifier.equals(classifier)) {
			this.lastClassifier = classifier;
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
			this.lastActivities = (activities == null ? null : new ArrayList<String>(activities));
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
			this.lastAlphabet = (alphabet == null ? null : new ActivityAlphabet(alphabet));
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
			this.lastLog = (log == null ? null : new ActivityLog(log));
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
			this.lastActivitySets = (activitySets == null ? null : new ActivitySets(activitySets));
			this.activitySets = (activitySets == null ? null : new ActivitySets(activitySets));
			if (propagate) {
				setMatrixCollection(null);
			}
		}
	}

	public static ActivitySets getAllActivitySets() {
		return lastAllActivitySets;
	}

	public void setAllActivitySets(List<ActivitySet> allActivitySets) {
		setAllActivitySets(allActivitySets, true);
	}
	
	public void setAllActivitySets(List<ActivitySet> allActivitySets, boolean propagate) {
		if (this.allActivitySets == null || !this.allActivitySets.equals(allActivitySets)) {
			this.lastAllActivitySets = (allActivitySets == null ? null : new ActivitySets(allActivitySets));
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
			this.lastMatrixCollection = (matrixCollection == null ? null : new ActivityMatrixCollection(matrixCollection));
			this.matrixCollection = (matrixCollection == null ? null : new ActivityMatrixCollection(matrixCollection));
		}
	}

	public boolean isUseILP() {
		return useILP;
	}

	public void setUseILP(boolean useILP) {
		this.useILP = useILP;
	}

	public boolean isReduceAll() {
		return reduceAll;
	}

	public void setReduceAll(boolean reduceAll) {
		this.lastReduceAll = reduceAll;
		this.reduceAll = reduceAll;
	}

	public boolean isReduceRestricted() {
		return reduceRestricted;
	}

	public void setReduceRestricted(boolean reduceRestricted) {
		this.lastReduceRestricted = reduceRestricted;
		this.reduceRestricted = reduceRestricted;
	}
}
