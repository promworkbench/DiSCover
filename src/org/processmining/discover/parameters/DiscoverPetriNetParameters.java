package org.processmining.discover.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityMatrix;
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
	 * Whether to require unanimity for noise. 
	 * If set, noise requires all matrices agree on it.
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
	 * The maximal number of S-components to take into account (use 0 for no limit).
	 */
	private int nofSComponents;

	/**
	 * The safety threshold to use.
	 */
	private int safetyThreshold;

	private XEventClassifier classifier;
	
	private List<String> activities;
	
	private ActivityMatrix matrix;
	
	/**
	 * Parameter settings selected last by the user.
	 */
	private static boolean lastMerge = true;
	private static boolean lastReduce = true;
	private static boolean lastVetoNoise = false;
	private static int lastAbsoluteThreshold = 1; // These seem reasonable values.
	private static int lastRelativeThreshold = 1;
	private static int lastSafetyThreshold = 95;
	private static int LastNofSComponents = 20; // Seems more than enough.
	private static XEventClassifier lastClassifier = null;
	private static List<String> lastActivities = null;
	private static ActivityMatrix lastMatrix = null;
	/**
	 * Creates default parameter settings.
	 */
	public DiscoverPetriNetParameters() {
		setMerge(lastMerge);
		setReduce(lastReduce);
		setVetoNoise(lastVetoNoise);
		setRelativeThreshold(lastRelativeThreshold);
		setAbsoluteThreshold(lastAbsoluteThreshold);
		setSafetyThreshold(lastSafetyThreshold);
		setNofSComponents(LastNofSComponents);
		setClassifier(lastClassifier);
		setActivities(lastActivities);
		setMatrix(lastMatrix);
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
		this.lastClassifier = classifier;
		this.classifier = classifier;
	}

	public List<String> getActivities() {
		return activities;
	}

	public void setActivities(List<String> activities) {
		this.lastActivities = (activities == null ? null : new ArrayList<String>(activities));
		this.activities = (activities ==  null ? null : new ArrayList<String>(activities));
	}
	
	public ActivityAlphabet getAlphabet() {
		if (this.activities == null) {
			return null;
		}
		return new ActivityAlphabet(this.activities);
	}

	public ActivityMatrix getMatrix() {
		return matrix;
	}

	public void setMatrix(ActivityMatrix matrix) {
		this.matrix = matrix;
	}
}
