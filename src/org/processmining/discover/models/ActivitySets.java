package org.processmining.discover.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

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

	/**
	 * Discovers the minimal ignore sets from a process tree and the alphabet.
	 * @param tree The process tree
	 * @param alphabet The alphabet
	 */
	public ActivitySets(ProcessTree tree, ActivityAlphabet alphabet) {
		List<ActivitySet> sets = new ArrayList<ActivitySet>();
		ActivitySet set = new ActivitySet();
		set.add(0);
		sets.add(set);
		apply(tree, tree.getRoot(), alphabet, sets);
		/*
		 * We now have the sets that take for any AND node a single branch.
		 * We now need to take the complement of those as the remainder will
		 * ignore these sets. As a result, all others branches will be ignored.
		 */
		this.sets = new ArrayList<ActivitySet>(size);
		size = 0;
		for (ActivitySet set2 : sets) {
			ActivitySet ignoreSet = new ActivitySet();
			for (int idx = 0; idx < alphabet.size(); idx++) {
				if (!set2.contains(idx)) {
					ignoreSet.add(idx);
				}
			}
			this.sets.add(size++, ignoreSet);
		}
	}
	
	public ActivitySets(Collection<ActivitySet> activitySets) {
		this.size = activitySets.size();
		this.sets = new ArrayList<ActivitySet>(size);
		for (ActivitySet activities : activitySets) {
			this.sets.add(activities);
		}
	}
	
	private void apply(ProcessTree tree, Node node, ActivityAlphabet alphabet, List<ActivitySet> sets) {
		System.out.println("[ActivitySets] sets: " + sets);
		if (node.isLeaf()) {
			/*
			 * Leaf node. Extend all possible sets with the activivty, if possible.
			 */
			String name = node.getName();
			System.out.println("[ActivitySets] Found activity name " + name);
			if (alphabet.contains(name)) {
				int idx = alphabet.get(name);
				System.out.println("[ActivitySets] has index " + idx);
				for (ActivitySet set : sets) {
					set.add(idx);
				}
			}
		} else if (tree.getType(node) == ProcessTree.Type.AND) {
			/*
			 * AND node. Compute a new set for every possible branch.
			 */
			Block block = (Block) node;
			List<ActivitySet> copySets = new ArrayList<ActivitySet>();
			for (ActivitySet set : sets) {
				ActivitySet copySet = new ActivitySet();
				copySet.addAll(set);
				copySets.add(copySet);
			}
			sets.clear();
			for (Node child : block.getChildren()) {
				List<ActivitySet> newSets = new ArrayList<ActivitySet>();
				for (ActivitySet copySet : copySets) {
					ActivitySet newSet = new ActivitySet();
					newSet.addAll(copySet);
					newSets.add(newSet);
				}
				apply(tree, child, alphabet, newSets);
				sets.addAll(newSets);
			}
		} else {
			/*
			 * Other node. Gather everything into this set.
			 */
			Block block = (Block) node;
			for (Node child : block.getChildren()) {
				apply(tree, child, alphabet, sets);
			}
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
	
	public String toString() {
		return sets.toString();
	}
}
