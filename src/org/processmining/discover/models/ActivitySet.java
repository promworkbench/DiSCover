package org.processmining.discover.models;

import java.util.BitSet;

public class ActivitySet extends BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3438285338528189401L;
	
	public static ActivityAlphabet alphabet = null;

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
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("{");
		boolean first = true;
		for (int i = 0; i < length(); i++) {
			if (get(i)) {
				if (first) {
					first = false;
				} else {
					s.append(", ");
				}
				s.append(alphabet.get(i));
			}
		}
		s.append("}");
		return s.toString();
	}

}
