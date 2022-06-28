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

		/*
		 * Create the alphabet (set of activities) for the event log.
		 */
		ActivityAlphabet alphabet = new ActivityAlphabet(eventLog, classifier);

		/*
		 * Convert the event log to an activity log using the alphabet.
		 */
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);

		/*
		 * Discover a directly-follows matrix from the activity log.
		 */
		ActivityMatrix matrix = new ActivityMatrix(log, alphabet);

		/*
		 * Filter the directly-follows matrix.
		 */
		matrix.filterAbsolute(parameters.getAbsoluteThreshold());
		matrix.filterRelative(parameters.getRelativeThreshold());

		/*
		 * Discover pairs of concurrent activities.
		 */
		ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(matrix, alphabet);

		/*
		 * Create sets of activities from these pairs. Every set covers at least
		 * one activity for every pair. These sets will be minimal in the sense
		 * that removing an activity from it will result in some pairs being
		 * uncovered.
		 * 
		 * This may take some time.
		 */
		ActivitySets separated = new ActivitySets(pairs);

		/*
		 * For every activity set, filter these activities out of the activity
		 * log and discover a directly-follows matrix for it.
		 */
		ActivityMatrixCollection matrices = new ActivityMatrixCollection(log, alphabet, separated);

		/*
		 * Filter all created matrices.
		 */
		for (int idx = 0; idx < matrices.size(); idx++) {
			matrices.get(idx).filterAbsolute(parameters.getAbsoluteThreshold());
			matrices.get(idx).filterRelative(parameters.getRelativeThreshold());
		}

		/*
		 * Discover an accepting Petri net from the matrices. Every matrix
		 * corresponds to a state machine WF-net. These WF-nets are merged on
		 * the visible transitions, that is, on the transitions that represent
		 * activities.
		 */
		AcceptingPetriNet apn = createNet(matrices, alphabet, parameters);

		/*
		 * If selected by the user, reduce the accepting Petri net as much as possible.
		 * This may take considerable time.
		 */
		if (parameters.isReduce()) {
			System.out.println("[DiscoverPetriNetAlgorithm] Reducing the net, please be patient...");
			ReduceUsingMurataRulesAlgorithm redAlgorithm = new ReduceUsingMurataRulesAlgorithm();
			ReduceUsingMurataRulesParameters redParameters = new ReduceUsingMurataRulesParameters();
			apn = redAlgorithm.apply(context, apn, redParameters);
			reduceNet(apn);
			apn = redAlgorithm.apply(context, apn, redParameters);
			System.out.println("[DiscoverPetriNetAlgorithm] Reduced the net.");
		}

		/*
		 * Return the discovered accepting Petri net.
		 */
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

	private void reduceNet(AcceptingPetriNet apn) {
		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		Map<PetrinetNode, Set<PetrinetNode>> postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			if (!preset.containsKey(edge.getTarget())) {
				preset.put(edge.getTarget(), new HashSet<PetrinetNode>());
			}
			preset.get(edge.getTarget()).add(edge.getSource());
			if (!postset.containsKey(edge.getSource())) {
				postset.put(edge.getSource(), new HashSet<PetrinetNode>());
			}
			postset.get(edge.getSource()).add(edge.getTarget());
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
			for (PetrinetNode node : postset.get(place)) {
				apn.getNet().addArc((Place) preset.get(transition).iterator().next(), (Transition) node);
			}
			apn.getNet().removeTransition(transition);
			apn.getNet().removePlace(place);
		}
	}
}
