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
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class DiscoverPetriNetAlgorithm {

	public AcceptingPetriNet apply(PluginContext context, XLog eventLog, DiscoverPetriNetParameters parameters) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		ActivityAlphabet alphabet = new ActivityAlphabet(eventLog, classifier);
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);
		ActivityMatrix matrix = new ActivityMatrix(log, alphabet);

		matrix.filterAbsolute(parameters.getAbsoluteThreshold());
		matrix.filterRelative(parameters.getRelativeThreshold());

		ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(matrix, alphabet);
		ActivitySets separated = new ActivitySets(pairs);
		ActivityMatrixCollection matrices = new ActivityMatrixCollection(log, alphabet, separated);

		for (int idx = 0; idx < matrices.size(); idx++) {
			matrices.get(idx).filterAbsolute(parameters.getAbsoluteThreshold());
			matrices.get(idx).filterRelative(parameters.getRelativeThreshold());
		}

		Petrinet net = PetrinetFactory.newPetrinet("Petri net DiSCovered");
		Map<Pair<Integer, Set<Integer>>, Place> inputPlaces = new HashMap<Pair<Integer, Set<Integer>>, Place>();
		Map<Pair<Integer, Set<Integer>>, Place> outputPlaces = new HashMap<Pair<Integer, Set<Integer>>, Place>();

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

		AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarkings);

		if (parameters.isReduce()) {
			ReduceUsingMurataRulesAlgorithm redAlgorithm = new ReduceUsingMurataRulesAlgorithm();
			ReduceUsingMurataRulesParameters redParameters = new ReduceUsingMurataRulesParameters();
			apn = redAlgorithm.apply(context, apn, redParameters);
		}

		return apn;
	}
}
