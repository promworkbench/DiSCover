package org.processmining.discover.models;

import java.util.BitSet;

public class ActivitySet extends BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3438285338528189401L;

	public ActivitySet() {
		super();
	}
	
	public void add(int i) {
		set(i);
	}
	
	public void remove(int i) {
		clear(i);
	}
	
	public boolean contains(int i) {
		return get(i);
	}
	
	public boolean containsAll(ActivitySet set) {
		for (int i = 0; i < set.length(); i++) {
			if (set.get(i) && !get(i)) {
				return false;
			}
		}
		return true;
	}
	
	public void addAll(ActivitySet set) {
		or(set);
	}

}
