package org.processmining.discover.models;

import java.util.BitSet;

public class ActivitySet extends BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3438285338528189401L;
	
	/*
	 * The alphabet used for this activity log.
	 */
	private ActivityAlphabet alphabet;

	private final String prefix;
	
	public ActivitySet(String prefix, ActivityAlphabet alphabet) {
		super();
		this.alphabet = new ActivityAlphabet(alphabet);
		this.prefix = prefix;
	}

	public ActivitySet(ActivitySet set) {
		or(set);
		this.alphabet = new ActivityAlphabet(set.alphabet);
		this.prefix = set.prefix;
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
		if (prefix != null) {
			s.append(prefix);
			s.append(" {");
		} else {
			s.append("{");			
		}
		boolean first = true;
		int j = 0;
		for (int i = 0; i < length(); i++) {
			if (get(i)) {
				j = i;
			}
		}
		
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
