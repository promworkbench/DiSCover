package org.processmining.discover.parameters;

public class DiscoverPetriNetParameters {

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
	 * Parameter settings selected last by the user.
	 */
	private static boolean lastMerge = true;
	private static boolean lastReduce = true;
	private static boolean lastVetoNoise = false;
	private static int lastAbsoluteThreshold = 0;
	private static int lastRelativeThreshold = 0;
	private static int LastNofSComponents = 20; // Seems more than enough.
	/**
	 * Creates default parameter settings.
	 */
	public DiscoverPetriNetParameters() {
		setMerge(lastMerge);
		setReduce(lastReduce);
		setVetoNoise(lastVetoNoise);
		setRelativeThreshold(lastRelativeThreshold);
		setAbsoluteThreshold(lastAbsoluteThreshold);
		setNofSComponents(LastNofSComponents);
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
}
