package org.processmining.discover.models;

import java.util.HashSet;
import java.util.Set;

public class ConcurrentActivityPairs {

	private ActivityPair pairs[];
	private int size;
	
	public ConcurrentActivityPairs(ActivityMatrix matrix, ActivityAlphabet alphabet) {
		Set<ActivityPair> pairs = new HashSet<ActivityPair>();
		for (int fromIdx = 1; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 1; toIdx < fromIdx; toIdx++) {
				if (matrix.get(fromIdx, toIdx) > 0 && matrix.get(toIdx, fromIdx) > 0) {
					pairs.add(new ActivityPair(fromIdx, toIdx));
				}
			}
		}
		size = pairs.size();
		this.pairs = new ActivityPair[size];
		size = 0;
		for (ActivityPair pair : pairs) {
			this.pairs[size++] = pair;
		}
	}
	
	public ActivityPair get(int idx) {
		return pairs[idx];
	}
	
	public int size() {
		return size;
	}
}
