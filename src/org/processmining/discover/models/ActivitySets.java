package org.processmining.discover.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivitySets {

	/**
	 * Array containing all minimal ignore sets.
	 */
	private ArrayList<ActivitySet> sets;

	/**
	 * Number of minimal ignore sets.
	 */
	private int size;

	/**
	 * Discovers the minimal ignore sets from a set of concurrent pairs. An
	 * ignore set should contain at least one activity from every pair. An
	 * ignore set is minimal if removing any activity results in some pair not
	 * being covered anymore.
	 * 
	 * @param pairs
	 *            Set or concurrent pairs
	 */
	public ActivitySets(ConcurrentActivityPairs pairs) {
		Set<ActivitySet> sets = new HashSet<ActivitySet>();
		List<Set<ActivitySet>> seen = new ArrayList<Set<ActivitySet>>(pairs.size());
		for (int idx = 0; idx < pairs.size(); idx++) {
			seen.add(idx, new HashSet<ActivitySet>());
		}
		apply(pairs, 0, new ActivitySet(), sets, seen);
		size = sets.size();
		System.out.println("[ActivitySets] " + size + " solutions.");
		this.sets = new ArrayList<ActivitySet>(size);
		size = 0;
		for (ActivitySet set : sets) {
			this.sets.add(size++, set);
		}
	}

	private void apply(ConcurrentActivityPairs pairs, int idx, ActivitySet candidateSet, Set<ActivitySet> ignoreSets, List<Set<ActivitySet>> seen) {
		if (idx == pairs.size()) {
			// All pairs are now covered.
			for (ActivitySet set : ignoreSets) {
				if (candidateSet.containsAll(set)) {
					// A smaller set is already present. Skip this canddiate.
					return;
				}
			}
			// Check whether there are any larger sets.
			Set<ActivitySet> largerSets = new HashSet<ActivitySet>();
			for (ActivitySet set : ignoreSets) {
				if (set.containsAll(candidateSet)) {
					largerSets.add(set);
				}
			}
			// Remove all the larger sets.
			ignoreSets.removeAll(largerSets);
			// Now add the candidate as a new set.
			ActivitySet set = new ActivitySet();
			set.addAll(candidateSet);
			ignoreSets.add(set);
			System.out.println("[ActivitySets] " + ignoreSets.size() + " solutions found so far.");
			return;
		}
		if (seen.get(idx).contains(candidateSet)) {
			System.out.println("[ActivitySets] Already seen set " + candidateSet + " at index " + idx);
			return;
		}
		ActivitySet candidateSetCopy = new ActivitySet();
		candidateSetCopy.addAll(candidateSet);
		seen.get(idx).add(candidateSetCopy);
		// Cover the next pair.
		ActivityPair pair = pairs.get(idx);
		if (candidateSet.contains(pair.getFirst()) || candidateSet.contains(pair.getSecond())) {
			// Pair is already covered. Continue.
			apply(pairs, idx + 1, candidateSet, ignoreSets, seen);
			return;
		}
		// First, try the first activity.
		candidateSet.add(pair.getFirst());
		apply(pairs, idx + 1, candidateSet, ignoreSets, seen);
		candidateSet.remove(pair.getFirst());
		// Second try, the second activity.
		candidateSet.add(pair.getSecond());
		apply(pairs, idx + 1, candidateSet, ignoreSets, seen);
		candidateSet.remove(pair.getSecond());
	}

	/**
	 * Returns the number of minimal ignore sets.
	 * 
	 * @return The number of minimal ignore sets
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the ignore set at the given index.
	 * 
	 * @param idx The given index
	 * @return The ignore set at the given index
	 */
	public ActivitySet get(int idx) {
		return sets.get(idx);
	}
}
