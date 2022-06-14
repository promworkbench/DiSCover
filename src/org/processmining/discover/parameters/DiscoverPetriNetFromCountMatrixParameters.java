package org.processmining.discover.parameters;

import org.processmining.discover.models.CountMatrix;

public class DiscoverPetriNetFromCountMatrixParameters {

	private CountMatrix matrix;
	
	private int absoluteThreshold;
	
	private int relativeThreshold;
	
	private int maxNofSolutions;
	
	private boolean merge;

	public DiscoverPetriNetFromCountMatrixParameters() {
		setAbsoluteThreshold(0); // 4, 0
		setRelativeThreshold(1000); // 16, 1000
		setMerge(true);
		setMaxNofSolutions(Integer.MAX_VALUE);
	}
	
	public int getAbsoluteThreshold() {
		return absoluteThreshold;
	}

	public void setAbsoluteThreshold(int absoluteThreshold) {
		this.absoluteThreshold = absoluteThreshold;
	}

	public int getRelativeThreshold() {
		return relativeThreshold;
	}

	public void setRelativeThreshold(int relativeThreshold) {
		this.relativeThreshold = relativeThreshold;
	}

	public CountMatrix getMatrix() {
		return matrix;
	}

	public void setMatrix(CountMatrix matrix) {
		this.matrix = matrix;
	}
	
	public boolean isMerge() {
		return merge;
	}
	
	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public int getMaxNofSolutions() {
		return maxNofSolutions;
	}

	public void setMaxNofSolutions(int maxNofSolutions) {
		this.maxNofSolutions = maxNofSolutions;
	}
}
