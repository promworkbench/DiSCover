package org.processmining.discover.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinetclassicalreductor.algorithms.ReduceUsingMurataRulesAlgorithm;
import org.processmining.acceptingpetrinetclassicalreductor.parameters.ReduceUsingMurataRulesParameters;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityLog;
import org.processmining.discover.models.ActivityMatrix;
import org.processmining.discover.models.ActivityMatrixCollection;
import org.processmining.discover.models.ActivitySet;
import org.processmining.discover.models.ActivitySets;
import org.processmining.discover.models.ConcurrentActivityPairs;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.ProcessTree;

public class DiscoverPetriNetAlgorithm {

	public AcceptingPetriNet apply(PluginContext context, XLog eventLog, DiscoverPetriNetParameters parameters) {
		return apply(context, eventLog, null, parameters);
	}

	public AcceptingPetriNet apply(PluginContext context, XLog eventLog, ProcessTree tree,
			DiscoverPetriNetParameters parameters) {
		/*
		 * Get the first classifier. If the event log has no classifier, use the
		 * default classifier.
		 */
		if (parameters.getClassifier() == null) {
			if (eventLog.getClassifiers().isEmpty()) {
				parameters.setClassifier(new XEventNameClassifier());
			} else {
				parameters.setClassifier(eventLog.getClassifiers().get(0));
			}
		}

		long time = System.currentTimeMillis();
		long time2 = time;
		/*
		 * Create the alphabet (set of activities) for the event log.
		 */
		if (parameters.getAlphabet() == null) {
			parameters.setAlphabet(new ActivityAlphabet(eventLog, parameters.getClassifier()));
			System.out.println("[DiscoverPetriNetAlgorithm] Creating alphabet took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		if (parameters.getLog() == null) {
			/*
			 * Convert the event log to an activity log using the alphabet.
			 */
			parameters.setLog(new ActivityLog(eventLog, parameters.getClassifier(), parameters.getAlphabet()));
			System.out.println("[DiscoverPetriNetAlgorithm] Creating activity log took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		if (parameters.getMatrix() == null) {
			/*
			 * Discover a directly-follows matrix from the activity log.
			 */
			parameters.setMatrix(new ActivityMatrix(parameters.getLog(), parameters.getAlphabet()));
			System.out.println("[DiscoverPetriNetAlgorithm] Creating primary matrix took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();

			/*
			 * Filter the directly-follows matrix.
			 */
			parameters.getMatrix().filterAbsolute(parameters.getAbsoluteThreshold());
			parameters.getMatrix().filterRelative(parameters.getRelativeThreshold(), parameters.getSafetyThreshold());
			System.out.println("[DiscoverPetriNetAlgorithm] Filtering primary matrix took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		if (parameters.getActivitySets() == null) {
			if (tree == null) {
				/*
				 * Discover pairs of concurrent activities.
				 */
				ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(parameters.getMatrix(),
						parameters.getAlphabet());
				System.out.println("[DiscoverPetriNetAlgorithm] Creating concurrent pairs took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				time = System.currentTimeMillis();

				/*
				 * Create sets of activities from these pairs. Every set covers
				 * at least one activity for every pair. These sets will be
				 * minimal in the sense that removing an activity from it will
				 * result in some pairs being uncovered.
				 * 
				 * This may take some time.
				 */
				parameters.setActivitySets(new ActivitySets(pairs, parameters.getAlphabet()));
				System.out.println("[DiscoverPetriNetAlgorithm] Creating non-concurrent sets took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				System.out.println(
						"[DiscoverPetriNetAlgorithm] Created non-concurrent sets: " + parameters.getActivitySets());
				time = System.currentTimeMillis();
			} else {
				/*
				 * Create sets of activities using the provided process tree.
				 */
				parameters.setActivitySets(new ActivitySets(tree, parameters.getAlphabet()));
				System.out.println("[DiscoverPetriNetAlgorithm] Creating non-concurrent sets from process tree took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				System.out.println(
						"[DiscoverPetriNetAlgorithm] Created non-concurrent sets: " + parameters.getActivitySets());
				time = System.currentTimeMillis();
			}
		}

		/*
		 * For every activity set, filter these activities out of the activity
		 * log and discover a directly-follows matrix for it.
		 */
		if (parameters.getMatrixCollection() == null) {
			parameters.setMatrixCollection(new ActivityMatrixCollection(parameters.getLog(), parameters.getAlphabet(),
					parameters.getActivitySets(), parameters.getMatrix(), parameters));
			System.out.println("[DiscoverPetriNetAlgorithm] Creating secondary matrices took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Filter all created matrices.
		 */
		//		for (int idx = 0; idx < matrices.size(); idx++) {
		//			matrices.get(idx).filterAbsolute(parameters.getAbsoluteThreshold());
		//			matrices.get(idx).filterRelative(parameters.getRelativeThreshold(), parameters.getSafetyThreshold());
		//		}
		//		System.out.println("[DiscoverPetriNetAlgorithm] Filtering secondary matrices took " + (System.currentTimeMillis() - time) + " milliseconds.");
		//		time = System.currentTimeMillis();

		/*
		 * If selected, use majority vote for whether to consider some edge as
		 * noise.
		 */
		if (parameters.isVetoNoise()) {
			parameters.getMatrixCollection().vetoNoise(parameters.getAlphabet());
			System.out.println("[DiscoverPetriNetAlgorithm] Vetoing noise took " + (System.currentTimeMillis() - time)
					+ " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Discover an accepting Petri net from the matrices. Every matrix
		 * corresponds to a state machine WF-net. These WF-nets are merged on
		 * the visible transitions, that is, on the transitions that represent
		 * activities.
		 */
		AcceptingPetriNet apn = createNet(parameters);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating accepting Petri net took "
				+ (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * If selected by the user, reduce the accepting Petri net as much as
		 * possible. This may take considerable time.
		 */
		if (parameters.isReduce()) {
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing the net, please be patient...");
			ReduceUsingMurataRulesAlgorithm redAlgorithm = new ReduceUsingMurataRulesAlgorithm();
			ReduceUsingMurataRulesParameters redParameters = new ReduceUsingMurataRulesParameters();
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing duplicates");
			reduceDuplicates(apn);
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing using rules...");
			apn = redAlgorithm.apply(context, apn, redParameters);
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing invisibles");
			reduceNet(apn);
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing using rules...");
			apn = redAlgorithm.apply(context, apn, redParameters);
			//			System.out.println("[DiscoverPetriNetAlgorithm] Reducing same contexts...");
			//			reduceSameContext(apn);
			//			System.out.println("[DiscoverPetriNetAlgorithm] Reducing using rules...");
			//			apn = redAlgorithm.apply(context, apn, redParameters);
			System.out.println("[DiscoverPetriNetAlgorithm] Reduced the net.");
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing accepting Petri net took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Return the discovered accepting Petri net.
		 */
		System.out.println("[DiscoverPetriNetAlgorithm] Discovering accepting Petri net took "
				+ (System.currentTimeMillis() - time2) + " milliseconds.");
		return apn;
	}

	private AcceptingPetriNet createNet(DiscoverPetriNetParameters parameters) {
		Petrinet net = PetrinetFactory.newPetrinet("Petri net DiSCovered");

		// Add shared start and end
		Transition startTransition = net.addTransition(ActivityAlphabet.START);
		startTransition.setInvisible(true);
		Transition endTransition = net.addTransition(ActivityAlphabet.END);
		endTransition.setInvisible(true);

		Place startPlace = net.addPlace("i");
		net.addArc(startPlace, startTransition);
		Place endPlace = net.addPlace("o");
		net.addArc(endTransition, endPlace);

		Marking initialMarking = new Marking();
		initialMarking.add(startPlace);
		Set<Marking> finalMarkings = new HashSet<Marking>();
		Marking finalMarking = new Marking();
		finalMarking.add(endPlace);
		finalMarkings.add(finalMarking);

		Map<Integer, Transition> transitions = new HashMap<Integer, Transition>();
		//		Map<Pair<ActivitySet, ActivitySet>, Transition> silentTransitions = new HashMap<Pair<ActivitySet, ActivitySet>, Transition>();
		// Add visible shared transitions.
		if (parameters.isMerge()) {
			for (int nodeIdx = 1; nodeIdx < parameters.getAlphabet().size(); nodeIdx++) {
				transitions.put(nodeIdx, net.addTransition(parameters.getAlphabet().get(nodeIdx)));
			}
		}

		/*
		 * Set of tau-clusters that have already been produced.
		 */
		Set<Set<String>> clustersProduced = new HashSet<Set<String>>();

		for (int idx = 0; idx < parameters.getMatrixCollection().size(); idx++) {
			ActivityMatrix subMatrix = parameters.getMatrixCollection().get(idx);
			Map<Integer, ActivitySet> nextActivities = subMatrix.getNextActivities();
			Map<Integer, ActivitySet> previousActivities = subMatrix.getPreviousActivities();

			/*
			 * Find the tau-clusters for this matrix. First, initialize the
			 * tau-clusters.
			 */
			Map<String, Set<String>> clusters = new HashMap<String, Set<String>>();
			for (int nodeIdx = 0; nodeIdx < parameters.getAlphabet().size(); nodeIdx++) {
				if (subMatrix.get(nodeIdx) == 0) {
					continue;
				}
				for (int nextIdx = 0; nextIdx < nextActivities.get(nodeIdx).length(); nextIdx++) {
					if (!nextActivities.get(nodeIdx).get(nextIdx)) {
						continue;
					}
					if (subMatrix.get(nextIdx) == 0) {
						continue;
					}
					Set<String> cluster = new HashSet<String>();
					cluster.add("(" + parameters.getAlphabet().get(nodeIdx) + ","
							+ parameters.getAlphabet().get(nextIdx) + ")");
					cluster.add("n" + nextActivities.get(nodeIdx).toString());
					cluster.add("p" + previousActivities.get(nextIdx).toString());
					clusters.put("(" + parameters.getAlphabet().get(nodeIdx) + ","
							+ parameters.getAlphabet().get(nextIdx) + ")", cluster);
					clusters.put("n" + nextActivities.get(nodeIdx).toString(), cluster);
					clusters.put("p" + previousActivities.get(nextIdx).toString(), cluster);
				}
			}
			/*
			 * Second, merge tau-clusters if they have an object (must be place)
			 * in common.
			 */
			boolean change = true;
			while (change) {
				change = false;
				for (String clusterKey : clusters.keySet()) {
					for (String otherClusterKey : clusters.keySet()) {
						if (clusterKey.equals(otherClusterKey)) {
							continue;
						}
						if (clusters.get(clusterKey).equals(clusters.get(otherClusterKey))) {
							continue;
						}
						Set<String> otherCluster = new HashSet<String>(clusters.get(otherClusterKey));
						otherCluster.retainAll(clusters.get(clusterKey));
						if (!otherCluster.isEmpty()) {
							change = true;
							clusters.get(clusterKey).addAll(clusters.get(otherClusterKey));
							clusters.get(otherClusterKey).addAll(clusters.get(clusterKey));
						}
					}
				}
			}

			if (!parameters.isMerge()) {
				// Add visible non-shared transitions.
				for (int nodeIdx = 1; nodeIdx < parameters.getAlphabet().size(); nodeIdx++) {
					if (subMatrix.get(nodeIdx) > 0) {
						transitions.put(nodeIdx, net.addTransition(parameters.getAlphabet().get(nodeIdx)));
					}
				}
				//				silentTransitions.clear();
			}
			// Add places
			Map<ActivitySet, Place> nextPlaces = new HashMap<ActivitySet, Place>();
			Map<ActivitySet, Place> previousPlaces = new HashMap<ActivitySet, Place>();
			for (ActivitySet next : new HashSet<ActivitySet>(nextActivities.values())) {
				if (!clustersProduced.contains(clusters.get("n" + next.toString()))) {
					/*
					 * Not produced yet: add place.
					 */
					nextPlaces.put(next, net.addPlace(next.toString()));
				}
			}
			for (ActivitySet previous : new HashSet<ActivitySet>(previousActivities.values())) {
				if (!clustersProduced.contains(clusters.get("p" + previous.toString()))) {
					/*
					 * Not produced yet: add place.
					 */
					previousPlaces.put(previous, net.addPlace(previous.toString()));
				}
			}
			// Connect visible transitions to places
			for (int nodeIdx = 0; nodeIdx < parameters.getAlphabet().size(); nodeIdx++) {
				if (subMatrix.get(nodeIdx) == 0) {
					continue;
				}
				if (nextPlaces.containsKey(nextActivities.get(nodeIdx))) {
					net.addArc(nodeIdx == 0 ? startTransition : transitions.get(nodeIdx),
							nextPlaces.get(nextActivities.get(nodeIdx)));
				}
				if (previousPlaces.containsKey(previousActivities.get(nodeIdx))) {
					net.addArc(previousPlaces.get(previousActivities.get(nodeIdx)),
							nodeIdx == 0 ? endTransition : transitions.get(nodeIdx));
				}
			}
			// Add invisible transitions and connect them.
			for (int nodeIdx = 0; nodeIdx < parameters.getAlphabet().size(); nodeIdx++) {
				if (subMatrix.get(nodeIdx) == 0) {
					continue;
				}
				for (int nextIdx = 0; nextIdx < nextActivities.get(nodeIdx).length(); nextIdx++) {
					if (!nextActivities.get(nodeIdx).get(nextIdx)) {
						continue;
					}
					if (subMatrix.get(nextIdx) == 0) {
						continue;
					}
					if (clustersProduced.contains(clusters.get("(" + parameters.getAlphabet().get(nodeIdx) + ","
							+ parameters.getAlphabet().get(nextIdx) + ")"))) {
						/*
						 * Already produced. Skip here..
						 */
						continue;
					}
					//					Pair<ActivitySet, ActivitySet> pair = new Pair<ActivitySet, ActivitySet>(nextActivities.get(nodeIdx), previousActivities.get(nextIdx));
					//					Transition transition = silentTransitions.get(pair);
					//					if (transition == null) {
					Transition transition = net.addTransition("(" + parameters.getAlphabet().get(nodeIdx) + ","
							+ parameters.getAlphabet().get(nextIdx) + ")");
					transition.setInvisible(true);
					//						silentTransitions.put(pair, transition);
					//					}
					net.addArc(nextPlaces.get(nextActivities.get(nodeIdx)), transition);
					net.addArc(transition, previousPlaces.get(previousActivities.get(nextIdx)));
				}
			}
			if (parameters.isMerge()) {
				/*
				 * Register all clusters that have been produced by now.
				 */
				clustersProduced.addAll(clusters.values());
			}
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarkings);
	}

	private void reduceDuplicates(AcceptingPetriNet apn) {
		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		Map<PetrinetNode, Set<PetrinetNode>> postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
			postset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			postset.get(edge.getSource()).add(edge.getTarget());
			preset.get(edge.getTarget()).add(edge.getSource());
		}
		Set<Transition> transitions = new HashSet<Transition>(apn.getNet().getTransitions());
		Set<Transition> transitionsRemoved = new HashSet<Transition>();
		for (Transition transition : transitions) {
			if (!transition.isInvisible()) {
				continue;
			}
			if (transitionsRemoved.contains(transition)) {
				continue;
			}
			for (PetrinetNode node : postset.get(preset.get(transition).iterator().next())) {
				Transition otherTransition = (Transition) node;
				if (transition == otherTransition) {
					continue;
				}
				if (!otherTransition.isInvisible()) {
					continue;
				}
				if (!preset.get(transition).equals(preset.get(otherTransition))) {
					continue;
				}
				if (!postset.get(transition).equals(postset.get(otherTransition))) {
					continue;
				}
				apn.getNet().removeTransition(otherTransition);
				transitionsRemoved.add(otherTransition);
			}
		}
		Set<Place> places = new HashSet<Place>(apn.getNet().getPlaces());
		Set<Place> placesRemoved = new HashSet<Place>();
		for (Place place : places) {
			if (placesRemoved.contains(place)) {
				continue;
			}
			if (preset.get(place).isEmpty()) {
				continue;
			}
			for (PetrinetNode node : postset.get(preset.get(place).iterator().next())) {
				Place otherPlace = (Place) node;
				if (place == otherPlace) {
					continue;
				}
				if (!preset.get(place).equals(preset.get(otherPlace))) {
					continue;
				}
				if (!postset.get(place).equals(postset.get(otherPlace))) {
					continue;
				}
				apn.getNet().removePlace(otherPlace);
				placesRemoved.add(otherPlace);
			}
		}
	}

	private void reduceNet(AcceptingPetriNet apn) {
		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		Map<PetrinetNode, Set<PetrinetNode>> postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
			postset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			postset.get(edge.getSource()).add(edge.getTarget());
			preset.get(edge.getTarget()).add(edge.getSource());
		}
		Set<Transition> transitions = new HashSet<Transition>(apn.getNet().getTransitions());
		for (Transition transition : transitions) {
			if (!transition.isInvisible() || preset.get(transition).size() != 1
					|| postset.get(transition).size() != 1) {
				continue;
			}
			Place place = (Place) postset.get(transition).iterator().next();
			if (preset.get(place).size() != 1 && postset.get(place).size() != 0) {
				continue;
			}
			if (apn.getInitialMarking().contains(place)) {
				continue;
			}
			boolean isFinal = false;
			for (Marking finalMarking : apn.getFinalMarkings()) {
				if (finalMarking.contains(place)) {
					isFinal = true;
				}
			}
			if (isFinal) {
				continue;
			}
			for (PetrinetNode node : postset.get(place)) {
				apn.getNet().addArc((Place) preset.get(transition).iterator().next(), (Transition) node);
			}
			apn.getNet().removeTransition(transition);
			apn.getNet().removePlace(place);
		}

	}

	private void reduceSameContext(AcceptingPetriNet apn) {
		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		Map<PetrinetNode, Set<PetrinetNode>> postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
			postset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			postset.get(edge.getSource()).add(edge.getTarget());
			preset.get(edge.getTarget()).add(edge.getSource());
		}
		Set<Transition> transitions = new HashSet<Transition>(apn.getNet().getTransitions());
		Map<List<Set<PetrinetNode>>, Transition> seen = new HashMap<List<Set<PetrinetNode>>, Transition>();
		for (Transition transition : transitions) {
			if (!transition.isInvisible() || preset.get(transition).size() != 1
					|| postset.get(transition).size() != 1) {
				continue;
			}
			Place prePlace = (Place) preset.get(transition).iterator().next();
			if (preset.get(prePlace).size() != 1) {
				continue;
			}
			Place postPlace = (Place) postset.get(transition).iterator().next();
			if (postset.get(postPlace).size() != 1) {
				continue;
			}

			Set<PetrinetNode> postPrePlace = new HashSet<PetrinetNode>();
			for (PetrinetNode node : postset.get(prePlace)) {
				if (!((Transition) node).isInvisible()) {
					postPrePlace.add(node);
				}
			}
			Set<PetrinetNode> prePostPlace = new HashSet<PetrinetNode>();
			for (PetrinetNode node : preset.get(postPlace)) {
				if (!((Transition) node).isInvisible()) {
					prePostPlace.add(node);
				}
			}
			if (postPrePlace.size() == 0 && prePostPlace.size() == 0) {
				continue;
			}

			List<Set<PetrinetNode>> context = new ArrayList<Set<PetrinetNode>>();
			context.add(preset.get(prePlace));
			context.add(postset.get(postPlace));
			context.add(prePostPlace);
			context.add(postPrePlace);
			if (seen.containsKey(context)) {
				apn.getNet().removeArc(prePlace, transition);
				apn.getNet().removeArc(transition, postPlace);
				apn.getNet().removeTransition(transition);
				apn.getNet().addArc(prePlace, seen.get(context));
				apn.getNet().addArc(seen.get(context), postPlace);
			} else {
				seen.put(context, transition);
			}
		}
	}
}
