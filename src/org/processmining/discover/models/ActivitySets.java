package org.processmining.discover.models;

import java.util.HashSet;
import java.util.Set;

public class ActivitySets {

	private ActivitySet sets[];
	private int size;
	
	public ActivitySets(ConcurrentActivityPairs pairs) {
		Set<ActivitySet> sets = new HashSet<ActivitySet>();
		apply(pairs, 0, new ActivitySet(), sets);
		size = sets.size();
		System.out.println("[ActivitySets] " + size + " solutions.");
		this.sets = new ActivitySet[size];
		size = 0;
		for (ActivitySet set : sets) {
			this.sets[size++] = set;
		}
	}
	
	private void apply(ConcurrentActivityPairs pairs, int idx, ActivitySet candidateSet, Set<ActivitySet> sets) {
		if (idx == pairs.size()) {
			for (ActivitySet set : sets) {
				if (candidateSet.containsAll(set)) {
					return;
				}
			}
			Set<ActivitySet> coveredSets = new HashSet<ActivitySet>();
			for (ActivitySet set : sets) {
				if (set.containsAll(candidateSet)) {
					coveredSets.add(set);
				}
			}
			sets.removeAll(coveredSets);	
			ActivitySet set = new ActivitySet();
			set.addAll(candidateSet);
			sets.add(set);
			System.out.println("[ActivitySets] " + sets.size() + " solutions found so far.");
			return;
		}
		ActivityPair pair = pairs.get(idx);
		if (candidateSet.contains(pair.getFirst()) || candidateSet.contains(pair.getSecond())) {
			apply(pairs, idx + 1, candidateSet, sets);
			return;
		}
		candidateSet.add(pair.getFirst());
		apply(pairs, idx + 1, candidateSet, sets);
		candidateSet.remove(pair.getFirst());
		candidateSet.add(pair.getSecond());
		apply(pairs, idx + 1, candidateSet, sets);
		candidateSet.remove(pair.getSecond());
	}
	
	public int size() {
		return size;
	}
	
	public ActivitySet get(int idx) {
		return sets[idx];
	}
}
