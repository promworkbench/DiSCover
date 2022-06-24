package org.processmining.discover.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.processmining.discover.models.TheActivitySets;
import org.processmining.discover.models.TheConcurrencyInfo;

public class TheSeparateAllConcurrentActivitiesAlgorithm {

	public static TheActivitySets apply(TheConcurrencyInfo info) {
		Set<Set<Integer>> sets = new HashSet<Set<Integer>>(info.getConcurrentSets());
		TheActivitySets solutions = new TheActivitySets();
		Set<Integer> candidate = new HashSet<Integer>();

//		System.out.println("[TheSeparateAllConcurrentActivitiesAlgorithm] concurrency info: " + sets);

		apply(sets, candidate, solutions);

//		System.out.println("[TheSeparateAllConcurrentActivitiesAlgorithm] solutions: " + solutions);

		return solutions;
	}

	private static void apply(Set<Set<Integer>> sets, Set<Integer> candidate, TheActivitySets solutions) {
		if (sets.isEmpty()) {
			// Add candidate as solution.
			solutions.add(new HashSet<Integer>(candidate));
			return;
		}
		Set<Set<Integer>> copySets = new HashSet<Set<Integer>>(sets);
		for (Set<Integer> set : copySets) {
			sets.remove(set);
			for (int idx : set) {
				if (candidate.contains(idx)) {
					apply(sets, candidate, solutions);
				} else {
					candidate.add(idx);
					apply(sets, candidate, solutions);
					candidate.remove(idx);
				}

			}
			sets.add(set);
		}
	}
}
