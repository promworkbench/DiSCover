package org.processmining.discover.parameters;

public class TheDiscoverPetriNetParameters {

	private boolean merge;

	public TheDiscoverPetriNetParameters() {
		setMerge(true);
	}
	
	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}
}
