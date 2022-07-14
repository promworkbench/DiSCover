package org.processmining.discover.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
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

public class DiscoverPetriNetAlgorithm {

	public AcceptingPetriNet apply(PluginContext context, XLog eventLog, DiscoverPetriNetParameters parameters) {
		/*
		 * Get the first classifier. If the event log has no classifier, use the
		 * default classifier.
		 */
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}

		long time = System.currentTimeMillis();
		long time2 = time;
		/*
		 * Create the alphabet (set of activities) for the event log.
		 */
		ActivityAlphabet alphabet = new ActivityAlphabet(eventLog, classifier);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating alphabet took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();
		
		/*
		 * Convert the event log to an activity log using the alphabet.
		 */
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating activity log took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * Discover a directly-follows matrix from the activity log.
		 */
		ActivityMatrix matrix = new ActivityMatrix(log, alphabet);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating primary matrix took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * Filter the directly-follows matrix.
		 */
		matrix.filterAbsolute(parameters.getAbsoluteThreshold());
		matrix.filterRelative(parameters.getRelativeThreshold());
		System.out.println("[DiscoverPetriNetAlgorithm] Filtering primary matrix took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * Discover pairs of concurrent activities.
		 */
		ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(matrix, alphabet);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating concurrent pairs took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * Create sets of activities from these pairs. Every set covers at least
		 * one activity for every pair. These sets will be minimal in the sense
		 * that removing an activity from it will result in some pairs being
		 * uncovered.
		 * 
		 * This may take some time.
		 */
		ActivitySets separated = new ActivitySets(pairs);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating non-concurrent sets took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * For every activity set, filter these activities out of the activity
		 * log and discover a directly-follows matrix for it.
		 */
		ActivityMatrixCollection matrices = new ActivityMatrixCollection(log, alphabet, separated);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating secondary matrices took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();

		/*
		 * Filter all created matrices.
		 */
		for (int idx = 0; idx < matrices.size(); idx++) {
			matrices.get(idx).filterAbsolute(parameters.getAbsoluteThreshold());
			matrices.get(idx).filterRelative(parameters.getRelativeThreshold());
		}
		System.out.println("[DiscoverPetriNetAlgorithm] Filtering secondary matrices took " + (System.currentTimeMillis() - time) + " milliseconds.");
		time = System.currentTimeMillis();
		
		/*
		 * If selected, use majority vote for whether to consider some edge as noise.
		 */
		if (parameters.isVetoNoise()) {
			matrices.vetoNoise(alphabet);
			System.out.println("[DiscoverPetriNetAlgorithm] Vetoing noise took " + (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Discover an accepting Petri net from the matrices. Every matrix
		 * corresponds to a state machine WF-net. These WF-nets are merged on
		 * the visible transitions, that is, on the transitions that represent
		 * activities.
		 */
		AcceptingPetriNet apn = createNet(matrices, alphabet, parameters);
		System.out.println("[DiscoverPetriNetAlgorithm] Creating accepting Petri net took " + (System.currentTimeMillis() - time) + " milliseconds.");
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
			System.out.println("[DiscoverPetriNetAlgorithm] Reduced the net.");
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing accepting Petri net took " + (System.currentTimeMillis() - time) + " milliseconds.");
			time = System.currentTimeMillis();
		}

		/*
		 * Return the discovered accepting Petri net.
		 */
		System.out.println("[DiscoverPetriNetAlgorithm] Discovering accepting Petri net took " + (System.currentTimeMillis() - time2) + " milliseconds.");
		return apn;
	}

	private AcceptingPetriNet createNet(ActivityMatrixCollection matrices, ActivityAlphabet alphabet,
			DiscoverPetriNetParameters parameters) {
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
		// Add visible shared transitions.
		if (parameters.isMerge()) {
			for (int nodeIdx = 1; nodeIdx < alphabet.size(); nodeIdx++) {
				transitions.put(nodeIdx, net.addTransition(alphabet.get(nodeIdx)));
			}
		}

		for (int idx = 0; idx < matrices.size(); idx++) {
			ActivityMatrix subMatrix = matrices.get(idx);
			if (!parameters.isMerge()) {
				// Add visible non-shared transitions.
				for (int nodeIdx = 1; nodeIdx < alphabet.size(); nodeIdx++) {
					if (subMatrix.get(nodeIdx) > 0) {
						transitions.put(nodeIdx, net.addTransition(alphabet.get(nodeIdx)));
					}
				}
			}
			// Add places
			Map<ActivitySet, Place> nextPlaces = new HashMap<ActivitySet, Place>();
			Map<Integer, ActivitySet> nextActivities = subMatrix.getNextActivities();
			for (ActivitySet next : new HashSet<ActivitySet>(nextActivities.values())) {
				nextPlaces.put(next, net.addPlace(next.toString()));
			}
			Map<ActivitySet, Place> previousPlaces = new HashMap<ActivitySet, Place>();
			Map<Integer, ActivitySet> previousActivities = subMatrix.getPreviousActivities();
			for (ActivitySet previous : new HashSet<ActivitySet>(previousActivities.values())) {
				previousPlaces.put(previous, net.addPlace(previous.toString()));
			}
			// Connect visible transitions to places
			for (int nodeIdx = 0; nodeIdx < alphabet.size(); nodeIdx++) {
				if (subMatrix.get(nodeIdx) == 0) {
					continue;
				}
				net.addArc(nodeIdx == 0 ? startTransition : transitions.get(nodeIdx),
						nextPlaces.get(nextActivities.get(nodeIdx)));
				net.addArc(previousPlaces.get(previousActivities.get(nodeIdx)),
						nodeIdx == 0 ? endTransition : transitions.get(nodeIdx));
			}
			// Add invisible transitions and connect them.
			for (int nodeIdx = 0; nodeIdx < alphabet.size(); nodeIdx++) {
				if (subMatrix.get(nodeIdx) == 0) {
					continue;
				}
				for (int nextIdx : nextActivities.get(nodeIdx)) {
					if (subMatrix.get(nextIdx) == 0) {
						continue;
					}
					Transition transition = net
							.addTransition("(" + alphabet.get(nodeIdx) + "," + alphabet.get(nextIdx) + ")");
					transition.setInvisible(true);
					net.addArc(nextPlaces.get(nextActivities.get(nodeIdx)), transition);
					net.addArc(transition, previousPlaces.get(previousActivities.get(nextIdx)));
				}
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
			if(transitionsRemoved.contains(transition)) {
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
}
