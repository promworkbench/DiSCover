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
	 * The absolute threshold to use.
	 */
	private int relativeThreshold;
	
	/**
	 * The relative threshold to use.
	 */
	private int absoluteThreshold;
	
	/**
	 * Parameter settings selected last by the user.
	 */
	private static boolean lastMerge = true;
	private static boolean lastReduce = true;
	private static int lastAbsoluteThreshold = 0;
	private static int lastRelativeThreshold = 0;
	/**
	 * Creates default parameter settings.
	 */
	public DiscoverPetriNetParameters() {
		setMerge(lastMerge);
		setReduce(lastReduce);
		setRelativeThreshold(lastRelativeThreshold);
		setAbsoluteThreshold(lastAbsoluteThreshold);
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
}