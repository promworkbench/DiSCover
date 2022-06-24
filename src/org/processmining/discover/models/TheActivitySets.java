package org.processmining.discover.models;

import java.util.HashSet;
import java.util.Set;

public class TheActivitySets {

	private Set<Set<Integer>> sets;

	public TheActivitySets() {
		sets = new HashSet<Set<Integer>>();
	}
	
	public void add(Set<Integer> candidate) {
		for (Set<Integer> set : sets) {
			if (candidate.containsAll(set)) {
				// Candidate is already covered.
				return;
			}
		}
		// Candidate is new set.
		// Remove sets covered by this candidate.
		Set<Set<Integer>> coveredSets = new HashSet<Set<Integer>>();
		for (Set<Integer> set : sets) {
			if (set.containsAll(candidate)) {
				coveredSets.add(set);
			}
		}
		sets.removeAll(coveredSets);
		sets.add(candidate);
	}
	
	public Set<Set<Integer>> getSets() {
		return sets;
	}
	
	public String toString() {
		return sets.toString();
	}
}
