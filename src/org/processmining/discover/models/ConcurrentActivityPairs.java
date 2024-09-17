package org.processmining.discover.models;

import java.util.HashSet;
import java.util.Set;

import org.ujmp.core.Matrix;

public class ConcurrentActivityPairs {

	private ActivityPair pairs[];
	private int size;
	
	public ConcurrentActivityPairs(ActivityMatrix matrix, ActivityAlphabet alphabet) {
		Set<ActivityPair> pairs = new HashSet<ActivityPair>();
		for (int fromIdx = 1; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 1; toIdx < fromIdx; toIdx++) {
				if (matrix.get(fromIdx, toIdx) > 0 && matrix.get(toIdx, fromIdx) > 0) {
					if (haveSamePredecessor(matrix, alphabet, fromIdx, toIdx) && haveSameSuccessor(matrix, alphabet, fromIdx, toIdx)) {
						pairs.add(new ActivityPair(fromIdx, toIdx));
					}
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
	
	private boolean haveSamePredecessor(ActivityMatrix matrix, ActivityAlphabet alphabet, int fromIdx, int toIdx) {
		for (int idx = 1; idx < alphabet.size(); idx++) {
			if (idx == fromIdx || idx == toIdx) {
				continue;
			}
			if (matrix.get(idx, fromIdx) > 0 && matrix.get(idx, toIdx) > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean haveSameSuccessor(ActivityMatrix matrix, ActivityAlphabet alphabet, int fromIdx, int toIdx) {
		for (int idx = 1; idx < alphabet.size(); idx++) {
			if (idx == fromIdx || idx == toIdx) {
				continue;
			}
			if (matrix.get(fromIdx, idx) > 0 && matrix.get(toIdx, idx) > 0) {
				return true;
			}
		}
		return false;
	}
}
