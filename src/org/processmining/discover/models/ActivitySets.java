package org.processmining.discover.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.lpengines.factories.LPEngineFactory;
import org.processmining.lpengines.interfaces.LPEngine;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;
import org.processmining.lpengines.interfaces.LPEngine.ObjectiveTargetType;
import org.processmining.lpengines.interfaces.LPEngine.Operator;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class ActivitySets extends ArrayList<ActivitySet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1802894563746035348L;

	/*
	 * Generate all possible maximal activity sets.
	 */
	public static final int MODE_ALL = 0;
	/*
	 * Generate a largest maximal activity set for every activity.
	 */
	public static final int MODE_ACT_BST = 1;
	/*
	 * Generate a maximal activity set for every activity.
	 */
	public static final int MODE_ACT_FRST = 2;

	/**
	 * Discovers the minimal ignore sets from a set of concurrent pairs. An ignore
	 * set should contain at least one activity from every pair. An ignore set is
	 * minimal if removing any activity results in some pair not being covered
	 * anymore.
	 * 
	 * @param pairs    Set or concurrent pairs
	 * @param alphabet The alphabet
	 */
	public ActivitySets(ConcurrentActivityPairs pairs, ActivityAlphabet alphabet) {
		this(pairs, alphabet, MODE_ALL);
	}
	
	public ActivitySets(ConcurrentActivityPairs pairs, ActivityAlphabet alphabet, int mode) {
		Set<ActivitySet> sets = new HashSet<ActivitySet>();
		switch (mode) {
		case MODE_ACT_BST: {
			applyILP(pairs, alphabet, sets);
			break;
		}
		case MODE_ACT_FRST: {
			apply(pairs, alphabet, sets);
			break;
		}
		default: {
			List<Set<ActivitySet>> seen = new ArrayList<Set<ActivitySet>>(pairs.size());
			for (int idx = 0; idx < pairs.size(); idx++) {
				seen.add(idx, new HashSet<ActivitySet>());
			}
			apply(pairs, 0, new ActivitySet("All except", alphabet), sets, seen, false);
		}
		}
		System.out.println("[ActivitySets] " + sets.size() + " solutions.");
		for (ActivitySet set : sets) {
			add(set);
		}
	}

	/**
	 * Discovers the minimal ignore sets from a process tree and the alphabet.
	 * 
	 * @param tree     The process tree
	 * @param alphabet The alphabet
	 */
	public ActivitySets(ProcessTree tree, ActivityAlphabet alphabet) {
		List<ActivitySet> sets = new ArrayList<ActivitySet>();
		ActivitySet set = new ActivitySet("", alphabet);
		set.add(0);
		sets.add(set);
		apply(tree, tree.getRoot(), alphabet, sets);
		/*
		 * We now have the sets that take for any AND node a single branch. We now need
		 * to take the complement of those as the remainder will ignore these sets. As a
		 * result, all others branches will be ignored.
		 */
		for (ActivitySet set2 : sets) {
			ActivitySet ignoreSet = new ActivitySet("All except", alphabet);
			for (int idx = 0; idx < alphabet.size(); idx++) {
				if (!set2.contains(idx)) {
					ignoreSet.add(idx);
				}
			}
			add(ignoreSet);
		}
	}

	public ActivitySets(List<ActivitySet> activitySets) {
		for (int i = 0; i < activitySets.size(); i++) {
			add(activitySets.get(i));
		}
	}

	private void apply(ProcessTree tree, Node node, ActivityAlphabet alphabet, List<ActivitySet> sets) {
//		System.out.println("[ActivitySets] sets: " + sets);
		if (node.isLeaf()) {
			/*
			 * Leaf node. Extend all possible sets with the activivty, if possible.
			 */
			String name = node.getName();
//			System.out.println("[ActivitySets] Found activity name " + name);
			if (alphabet.contains(name)) {
				int idx = alphabet.get(name);
//				System.out.println("[ActivitySets] has index " + idx);
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
				ActivitySet copySet = new ActivitySet(set);
				copySets.add(copySet);
			}
			sets.clear();
			for (Node child : block.getChildren()) {
				List<ActivitySet> newSets = new ArrayList<ActivitySet>();
				for (ActivitySet copySet : copySets) {
					ActivitySet newSet = new ActivitySet(copySet);
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

	private void apply(ConcurrentActivityPairs pairs, ActivityAlphabet alphabet, Set<ActivitySet> ignoreSets) {
		for (int a = 0; a < alphabet.size(); a++) {
//			System.out.println("[ActivitySets] Adding an ignore set for " + alphabet.get(a));
			ActivitySet candidateSet = new ActivitySet("All except", alphabet);
			List<Set<ActivitySet>> seen = new ArrayList<Set<ActivitySet>>(pairs.size());
			/* 
			 * Add all activities that are concurrent with a.
			 * This ensures that a will not be added to the ignore set.
			 */
			for (int p = 0; p < pairs.size(); p++) {
				if (pairs.get(p).getFirst() == a) {
					candidateSet.add(pairs.get(p).getSecond());
				} else if (pairs.get(p).getSecond() == a) {
					candidateSet.add(pairs.get(p).getFirst());
				}
				seen.add(p, new HashSet<ActivitySet>());
			}
			apply(pairs, 0, candidateSet, ignoreSets, seen, true);
//			System.out.println("[ActivitySets] Ignore sets found " + ignoreSets);
		}
	}

	/*
	 * Recursive method, may take very long if there happen to be many pairs of
	 * concurrent activities, May result in many subsets.
	 */
	private boolean apply(ConcurrentActivityPairs pairs, int idx, ActivitySet candidateSet, Set<ActivitySet> ignoreSets,
			List<Set<ActivitySet>> seen, boolean stopAtFirst) {
		if (idx == pairs.size()) {
			// All pairs are now covered.
			for (ActivitySet set : ignoreSets) {
				if (candidateSet.containsAll(set)) {
					// A smaller set is already present. Skip this canddiate.
					return false;
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
			ActivitySet set = new ActivitySet(candidateSet);
			ignoreSets.add(set);
//			System.out.println("[ActivitySets] " + ignoreSets.size() + " solutions found so far.");
			return true;
		}
//		if (seen.get(idx).contains(candidateSet)) {
//			System.out.println("[ActivitySets] Already seen set " + candidateSet + " at index " + idx);
//			return;
//		}
		Set<ActivitySet> toBeRemoved = new HashSet<ActivitySet>();
		for (ActivitySet seenSet : seen.get(idx)) {
			if (candidateSet.containsAll(seenSet)) {
				/*
				 * The partial candidate set is a superset of some partial set seen before. As a
				 * result, the partial candidate set cannot evolve into a better candidate set
				 * than that seen set can.
				 */
				return false;
			} else if (seenSet.containsAll(candidateSet)) {
				/*
				 * The candidate set is a proper subset of set seen earlier. Keeping this set
				 * seen earlier makes no sense when adding this candidate set, so schedule it
				 * for removal.
				 */
				toBeRemoved.add(seenSet);
			}
		}
		// Remove all the proper super sets of the candidate set.
		seen.get(idx).removeAll(toBeRemoved);
		// Add a copy of the candidate set.
		ActivitySet candidateSetCopy = new ActivitySet(candidateSet);
		seen.get(idx).add(candidateSetCopy);
		// Cover the next pair.
		ActivityPair pair = pairs.get(idx);
		if (candidateSet.contains(pair.getFirst()) || candidateSet.contains(pair.getSecond())) {
			// Pair is already covered. Continue.
			return apply(pairs, idx + 1, candidateSet, ignoreSets, seen, stopAtFirst);
		}
		// First, try the first activity.
		candidateSet.add(pair.getFirst());
		boolean found = apply(pairs, idx + 1, candidateSet, ignoreSets, seen, stopAtFirst);
		candidateSet.remove(pair.getFirst());
		if (!stopAtFirst || !found) {
			// Second try, the second activity.
			candidateSet.add(pair.getSecond());
			found = apply(pairs, idx + 1, candidateSet, ignoreSets, seen, stopAtFirst);
			candidateSet.remove(pair.getSecond());
		}
		return found;
	}

	/*
	 * Non-recursive, ILP-based method. Solves for every activity an ILP finding a
	 * maximal non-concurrent subset of activities. Results in at most as many
	 * subsets as there are activities.
	 */
	private void applyILP(ConcurrentActivityPairs pairs, ActivityAlphabet alphabet, Set<ActivitySet> ignoreSets) {
		ActivitySet covered = new ActivitySet("Covered", alphabet);
		for (int idx = 0; idx < alphabet.size(); idx++) {
			if (covered.contains(idx)) {
				/*
				 * This activity has already been covered.
				 */
				continue;
			}
			/*
			 * Get a maximal non-concurrent activity set that includes alphabet.get(idx).
			 */

			/*
			 * Create an ILP to solve this which maximizes the number of activities.
			 */
			LPEngine engine = LPEngineFactory.createLPEngine(EngineType.LPSOLVE, 0, 0);
			Map<Integer, Double> objective = new HashMap<Integer, Double>();
			int variables[] = new int[alphabet.size()];
			for (int i = 0; i < alphabet.size(); i++) {
				variables[i] = engine.addVariable(new HashMap<Integer, Double>(), LPEngine.VariableType.INTEGER);
				objective.put(variables[i], 1.0);
			}
			engine.setObjective(objective, ObjectiveTargetType.MAX);

			/*
			 * The current activity should have weight 1.0.
			 */
			Map<Integer, Double> constraint = new HashMap<Integer, Double>();
			for (int i = 0; i < alphabet.size(); i++) {
				constraint.put(variables[i], i == idx ? 1.0 : 0.0);
			}
			engine.addConstraint(constraint, Operator.GREATER_EQUAL, 1.0);

			/*
			 * Every activity should have weight at least 0.0 and at most 1.0.
			 */
			for (int p = 0; p < alphabet.size(); p++) {
				constraint = new HashMap<Integer, Double>();
				for (int i = 0; i < alphabet.size(); i++) {
					constraint.put(variables[i], i == p ? 1.0 : 0.0);
				}
				engine.addConstraint(constraint, Operator.GREATER_EQUAL, 0.0);
				engine.addConstraint(constraint, Operator.LESS_EQUAL, 1.0);
			}

			/*
			 * The sum of concurrent pairs of activities should be at most 1.0.
			 */
			for (int p = 0; p < pairs.size(); p++) {
				constraint = new HashMap<Integer, Double>();
				for (int i = 0; i < alphabet.size(); i++) {
					constraint.put(variables[i],
							i == pairs.get(p).getFirst() || i == pairs.get(p).getSecond() ? 1.0 : 0.0);
				}
				engine.addConstraint(constraint, Operator.LESS_EQUAL, 1.0);
			}

			/*
			 * Solve it.
			 */
			Map<Integer, Double> solution = engine.solve();

			/*
			 * Now get the set of activities *not included* in this maximal set (historic
			 * reasons).
			 */
			ActivitySet selected = new ActivitySet("All except", alphabet);
			for (int i = 0; i < alphabet.size(); i++) {
				if (!solution.containsKey(variables[i]) || solution.get(variables[i]) == 0.0) {
					selected.add(i);
				} else {
					covered.add(i);
				}
			}

//			System.out.println("[ActivitySets] Solution for " + alphabet.get(idx) + " is " + selected);

			/*
			 * If a new set, add it.
			 */
			if (!ignoreSets.contains(selected)) {
				ignoreSets.add(selected);
			}
		}
	}
}
