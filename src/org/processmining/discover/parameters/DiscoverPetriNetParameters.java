package org.processmining.discover.parameters;

public class DiscoverPetriNetParameters {

	private boolean merge;
	private boolean reduce;
	private int relativeThreshold;
	private int absoluteThreshold;
	
	public DiscoverPetriNetParameters() {
		setMerge(true);
		setReduce(true);
		setRelativeThreshold(0);
		setAbsoluteThreshold(0);
	}
	
	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public int getRelativeThreshold() {
		return relativeThreshold;
	}

	public void setRelativeThreshold(int relativeThreshold) {
		this.relativeThreshold = relativeThreshold;
	}

	public int getAbsoluteThreshold() {
		return absoluteThreshold;
	}

	public void setAbsoluteThreshold(int absoluteThreshold) {
		this.absoluteThreshold = absoluteThreshold;
	}

	public boolean isReduce() {
		return reduce;
	}

	public void setReduce(boolean reduce) {
		this.reduce = reduce;
	}
}
