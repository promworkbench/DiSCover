package org.processmining.discover.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
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
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.models.LogSkeleton;
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
		 * Get the first classifier. If the event log has no classifier, use the default
		 * classifier.
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
			if (parameters.isFilterLog()) {
				if (parameters.getLog().filter(parameters.getMatrix(),
						new ActivitySet("All except ", parameters.getAlphabet()))) {
					parameters.setMatrix(new ActivityMatrix(parameters.getLog(), parameters.getAlphabet()));
				}
			}
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
						parameters.getAlphabet(), parameters);
				System.out.println("[DiscoverPetriNetAlgorithm] Creating concurrent pairs took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				time = System.currentTimeMillis();

				/*
				 * Create sets of activities from these pairs. Every set covers at least one
				 * activity for every pair. These sets will be minimal in the sense that
				 * removing an activity from it will result in some pairs being uncovered.
				 * 
				 * This may take some time.
				 */
				parameters.setActivitySets(new ActivitySets(pairs, parameters.getAlphabet(), parameters.getMode()));
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
		 * For every activity set, filter these activities out of the activity log and
		 * discover a directly-follows matrix for it.
		 */
		if (parameters.getMatrixCollection() == null) {
			parameters.setMatrixCollection(new ActivityMatrixCollection(parameters.getLog(), parameters.getAlphabet(),
					parameters.getActivitySets(), parameters.getMatrix(), parameters));
			System.out.println("[DiscoverPetriNetAlgorithm] Creating secondary matrices took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();

			parameters.getMatrixCollection().filterAbsolute(parameters.getAbsoluteThreshold2());
			parameters.getMatrixCollection().filterRelative(parameters.getRelativeThreshold2(),
					parameters.getSafetyThreshold2());
			if (parameters.isFilterLog()) {
				parameters.getMatrixCollection().filter(parameters.getLog(), parameters.getActivitySets(),
						parameters.getMatrix());
			}
			System.out.println("[DiscoverPetriNetAlgorithm] Filtering secondary matrices took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Filter all created matrices.
		 */
		// for (int idx = 0; idx < matrices.size(); idx++) {
		// matrices.get(idx).filterAbsolute(parameters.getAbsoluteThreshold());
		// matrices.get(idx).filterRelative(parameters.getRelativeThreshold(),
		// parameters.getSafetyThreshold());
		// }
		// System.out.println("[DiscoverPetriNetAlgorithm] Filtering secondary matrices
		// took " + (System.currentTimeMillis() - time) + " milliseconds.");
		// time = System.currentTimeMillis();

		/*
		 * If selected, use majority vote for whether to consider some edge as noise.
		 */
		if (parameters.isVetoNoise()) {
			parameters.getMatrixCollection().vetoNoise(parameters.getAlphabet());
			System.out.println("[DiscoverPetriNetAlgorithm] Vetoing noise took " + (System.currentTimeMillis() - time)
					+ " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Discover an accepting Petri net from the matrices. Every matrix corresponds
		 * to a state machine WF-net. These WF-nets are merged on the visible
		 * transitions, that is, on the transitions that represent activities.
		 */
		AcceptingPetriNet apn = createNet(parameters);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating accepting Petri net took "
				+ (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		enhanceNet(context, apn, eventLog, parameters);

		/*
		 * If selected by the user, reduce the accepting Petri net as much as possible.
		 * This may take considerable time.
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
			// System.out.println("[DiscoverPetriNetAlgorithm] Reducing same contexts...");
			// reduceSameContext(apn);
			// System.out.println("[DiscoverPetriNetAlgorithm] Reducing using rules...");
			// apn = redAlgorithm.apply(context, apn, redParameters);
			System.out.println("[DiscoverPetriNetAlgorithm] Reduced the net.");
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing accepting Petri net took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}
		time = System.currentTimeMillis();

		if (parameters.isReduceAll()) {
			ReduceAbstractSimpleSilentTransitionsAlgorithm redAlgorithm = new ReduceAllSimpleSilentTransitionsAlgorithm();
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing all simple silent transitions");
			apn = redAlgorithm.apply(context, apn);
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing all simple silent transitions took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
		} else if (parameters.isReduceRestricted()) {
			ReduceClusteredSimpleSilentTransitionsAlgorithm redAlgorithm = new ReduceClusteredSimpleSilentTransitionsAlgorithm();
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing clustered simple silent transitions");
			apn = redAlgorithm.apply(context, apn);
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing clustered simple silent transitions took "
					+ (System.currentTimeMillis() - time) + " milliseconds.");
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
		// Map<Pair<ActivitySet, ActivitySet>, Transition> silentTransitions = new
		// HashMap<Pair<ActivitySet, ActivitySet>, Transition>();
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
			 * Find the tau-clusters for this matrix. First, initialize the tau-clusters.
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
			 * Second, merge tau-clusters if they have an object (must be place) in common.
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
				// silentTransitions.clear();
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
					// Pair<ActivitySet, ActivitySet> pair = new Pair<ActivitySet,
					// ActivitySet>(nextActivities.get(nodeIdx), previousActivities.get(nextIdx));
					// Transition transition = silentTransitions.get(pair);
					// if (transition == null) {
					Transition transition = net.addTransition("(" + parameters.getAlphabet().get(nodeIdx) + ","
							+ parameters.getAlphabet().get(nextIdx) + ")");
					transition.setInvisible(true);
					// silentTransitions.put(pair, transition);
					// }
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
			Set<PetrinetNode> prePlaces = preset.get(transition);
			if (prePlaces == null || prePlaces.isEmpty()) {
				continue;
			}
			for (PetrinetNode node : postset.get(prePlaces.iterator().next())) {
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
			Set<PetrinetNode> preTransitions = preset.get(place);
			if (preTransitions == null || preTransitions.isEmpty()) {
				continue;
			}
			for (PetrinetNode node : postset.get(preTransitions.iterator().next())) {
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
			if (!transition.isInvisible() || preset.get(transition) == null || preset.get(transition).size() != 1
					|| postset.get(transition) == null || postset.get(transition).size() != 1) {
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

	private void enhanceNet(PluginContext context, AcceptingPetriNet apn, XLog eventLog,
			DiscoverPetriNetParameters parameters) {
		if (!parameters.isEnhanceWithLS()) {
			return;
		}
		if (apn.getNet().getTransitions().size() > 100) {
			return;
		}
		try {
			LogSkeleton lsLog = context.tryToFindOrConstructFirstNamedObject(LogSkeleton.class,
					"Build Log Skeleton from Event Log", null, null, eventLog);

			XLog apnLog = generateLog(apn);
			LogSkeleton lsModel = context.tryToFindOrConstructFirstNamedObject(LogSkeleton.class,
					"Build Log Skeleton from Event Log", null, null, apnLog);

			Map<String, Transition> transitionMap = new HashMap<String, Transition>();
			for (Transition transition : apn.getNet().getTransitions()) {
				if (!transition.isInvisible() || transition.getLabel().equals(ActivityAlphabet.START)
						|| transition.getLabel().equals(ActivityAlphabet.END)) {
					transitionMap.put(transition.getLabel(), transition);
				}
			}

			for (String activity : lsLog.getActivities()) {
				if (apn.getNet().getTransitions().size() > 100) {
					return;
				}
				if (lsLog.getMin(activity) == 1 && lsLog.getMax(activity) == 1) {
					if (!(lsModel.getMin(activity) == 1 && lsModel.getMax(activity) == 1)) {
						Place p = apn.getNet().addPlace("p11_" + activity);
						apn.getNet().addArc(transitionMap.get(ActivityAlphabet.START), p);
						apn.getNet().addArc(p, transitionMap.get(activity));
						Place q = apn.getNet().addPlace("q11_" + activity);
						apn.getNet().addArc(transitionMap.get(activity), q);
						apn.getNet().addArc(q, transitionMap.get(ActivityAlphabet.END));
					}
				}
			}
			for (String activity : lsLog.getActivities()) {
				if (apn.getNet().getTransitions().size() > 100) {
					return;
				}
				if (lsLog.getMin(activity) == 0 && lsLog.getMax(activity) == 1) {
					if (!(lsModel.getMin(activity) == 0 && lsModel.getMax(activity) == 1)) {
						Place p = apn.getNet().addPlace("p01_" + activity);
						apn.getNet().addArc(transitionMap.get(ActivityAlphabet.START), p);
						apn.getNet().addArc(p, transitionMap.get(activity));
						Place q = apn.getNet().addPlace("q01_" + activity);
						apn.getNet().addArc(transitionMap.get(activity), q);
						apn.getNet().addArc(q, transitionMap.get(ActivityAlphabet.END));
						Transition t = apn.getNet().addTransition("t01_" + activity);
						t.setInvisible(true);
						apn.getNet().addArc(p, t);
						apn.getNet().addArc(t, q);
					}
				}
			}
			for (String source : lsLog.getActivities()) {
				for (String target : lsLog.getActivities()) {
					if (apn.getNet().getTransitions().size() > 100) {
						return;
					}
					if (source == target) {
						continue;
					}
					if (lsLog.hasNonRedundantResponse(source, target, lsLog.getActivities())) {
						if (!lsModel.hasNonRedundantResponse(source, target, lsModel.getActivities())) {
							Place p = apn.getNet().addPlace("pr_" + source + "_" + target);
							apn.getNet().addArc(transitionMap.get(ActivityAlphabet.START), p);
							apn.getNet().addArc(p, transitionMap.get(ActivityAlphabet.END));
							Place q = apn.getNet().addPlace("qr_" + source + "_" + target);
							apn.getNet().addArc(transitionMap.get(source), p);
							apn.getNet().addArc(p, transitionMap.get(source));
							apn.getNet().addArc(transitionMap.get(target), p);
							apn.getNet().addArc(q, transitionMap.get(target));
							Transition t = apn.getNet().addTransition("tr_" + source + "_" + target);
							t.setInvisible(true);
							apn.getNet().addArc(t, q);
							apn.getNet().addArc(p, t);
						}
					}
				}
			}
			for (String source : lsLog.getActivities()) {
				for (String target : lsLog.getActivities()) {
					if (apn.getNet().getTransitions().size() > 100) {
						return;
					}
					if (source == target) {
						continue;
					}
					if (lsLog.hasNonRedundantPrecedence(source, target, lsLog.getActivities())) {
						if (!lsModel.hasNonRedundantPrecedence(source, target, lsModel.getActivities())) {
							Place p = apn.getNet().addPlace("pp_" + source + "_" + target);
							apn.getNet().addArc(transitionMap.get(ActivityAlphabet.START), p);
							apn.getNet().addArc(p, transitionMap.get(ActivityAlphabet.END));
							Place q = apn.getNet().addPlace("qp_" + source + "_" + target);
							apn.getNet().addArc(transitionMap.get(source), p);
							apn.getNet().addArc(p, transitionMap.get(source));
							apn.getNet().addArc(transitionMap.get(target), q);
							apn.getNet().addArc(p, transitionMap.get(target));
							Transition t = apn.getNet().addTransition("tp_" + source + "_" + target);
							t.setInvisible(true);
							apn.getNet().addArc(t, p);
							apn.getNet().addArc(q, t);
						}
					}
				}
			}
			for (String source : lsLog.getActivities()) {
				for (String target : lsLog.getActivities()) {
					if (apn.getNet().getTransitions().size() > 100) {
						return;
					}
					if (source == target) {
						continue;
					}
					if (lsLog.hasNonRedundantNotResponse(source, target, lsLog.getActivities())) {
						if (!lsModel.hasNonRedundantNotResponse(source, target, lsModel.getActivities())) {
							Place p = apn.getNet().addPlace("pn_" + source + "_" + target);
							apn.getNet().addArc(transitionMap.get(ActivityAlphabet.START), p);
							Place q = apn.getNet().addPlace("qn_" + source + "_" + target);
							apn.getNet().addArc(q, transitionMap.get(ActivityAlphabet.END));
							apn.getNet().addArc(transitionMap.get(source), p);
							apn.getNet().addArc(p, transitionMap.get(source));
							apn.getNet().addArc(transitionMap.get(target), q);
							apn.getNet().addArc(q, transitionMap.get(target));
							Transition t = apn.getNet().addTransition("tn_" + source + "_" + target);
							t.setInvisible(true);
							apn.getNet().addArc(t, q);
							apn.getNet().addArc(p, t);
						}
					}
				}
			}

		} catch (ConnectionCannotBeObtained e) {
			// Ignore: No enhancements possible.
		}
	}

	private XLog generateLog(AcceptingPetriNet apn) {
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
		XLog log = XFactoryRegistry.instance().currentDefault().createLog();
		log.getClassifiers().add(new XEventNameClassifier());
		for (int i = 0; i < 1000; i++) {
			System.out.println("[DiscoverPetriNetAlgorithm] Generating trace " + i);
			XTrace trace = XFactoryRegistry.instance().currentDefault().createTrace();
			boolean incomplete = true;
			while (incomplete) {
				Marking marking = new Marking(apn.getInitialMarking());
//				System.out.println("[DiscoverPetriNetAlgorithm] Marking " + marking);
				while (!apn.getFinalMarkings().contains(marking) && trace.size() < 1000) {
					List<Transition> enabled = getEnabledTransitions(apn, marking, preset, postset);
					Transition transition = enabled.get(new Random().nextInt(enabled.size()));
//					System.out.println("[DiscoverPetriNetAlgorithm] Firing transition " + transition.getLabel());
					marking = fireTransition(transition, marking, preset, postset);
//					System.out.println("[DiscoverPetriNetAlgorithm] Marking " + marking);
					if (!transition.isInvisible()) {
						XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
						XConceptExtension.instance().assignName(event, transition.getLabel());
						trace.add(event);
					}
				}
				if (trace.size() < 1000) {
					incomplete = false;
				} else {
					System.out.println("[DiscoverPetriNetAlgorithm] Regenerating trace " + i);
					trace.clear();
				}
			}
			log.add(trace);
		}
		return log;
	}

	private List<Transition> getEnabledTransitions(AcceptingPetriNet apn, Marking marking,
			Map<PetrinetNode, Set<PetrinetNode>> preset, Map<PetrinetNode, Set<PetrinetNode>> postset) {
		List<Transition> enabled = new ArrayList<Transition>();
		for (Transition transition : apn.getNet().getTransitions()) {
			if (!transition.isInvisible() || transition.getLabel().equals(ActivityAlphabet.START)
					|| transition.getLabel().equals(ActivityAlphabet.END)) {
				boolean isEnabled = true;
				for (PetrinetNode node : preset.get(transition)) {
					Place place = (Place) node;
					isEnabled = isEnabled && hasToken(place, marking, preset, postset);
				}
				if (isEnabled) {
					enabled.add(transition);
				}
			}
		}
		return enabled;
	}

	private boolean hasToken(Place place, Marking marking, Map<PetrinetNode, Set<PetrinetNode>> preset,
			Map<PetrinetNode, Set<PetrinetNode>> postset) {
		if (marking.contains(place)) {
			return true;
		}
		for (PetrinetNode node : preset.get(place)) {
			Transition transition = (Transition) node;
			if (!transition.isInvisible()) {
				continue;
			}
			if (transition.getLabel().equals(ActivityAlphabet.START)) {
				continue;
			}
			if (transition.getLabel().equals(ActivityAlphabet.END)) {
				continue;
			}
			if (marking.contains(preset.get(transition).iterator().next())) {
				return true;
			}
		}
		return false;
	}

	private Marking fireTransition(Transition transition, Marking marking, Map<PetrinetNode, Set<PetrinetNode>> preset,
			Map<PetrinetNode, Set<PetrinetNode>> postset) {
		Marking newMarking = new Marking(marking);
		for (PetrinetNode node : preset.get(transition)) {
			Place place = (Place) node;
			removeToken(place, newMarking, preset);
		}
		for (PetrinetNode node : postset.get(transition)) {
			Place place = (Place) node;
			newMarking.add(place);
		}
		return newMarking;
	}

	private void removeToken(Place place, Marking marking, Map<PetrinetNode, Set<PetrinetNode>> preset) {
		if (marking.contains(place)) {
			marking.remove(place);
			return;
		}
		for (PetrinetNode node : preset.get(place)) {
			Transition transition = (Transition) node;
			if (!transition.isInvisible()) {
				continue;
			}
			if (transition.getLabel().equals(ActivityAlphabet.START)) {
				continue;
			}
			if (transition.getLabel().equals(ActivityAlphabet.END)) {
				continue;
			}
			if (marking.contains(preset.get(transition).iterator().next())) {
				marking.remove(preset.get(transition).iterator().next());
				return;
			}
		}
	}
}
