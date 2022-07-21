package org.processmining.discover.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class ReplayPetriNetAlgorithm {

	/*
	 * Preset and postset of every node.
	 */
	private Map<PetrinetNode, Set<PetrinetNode>> preset;
	private Map<PetrinetNode, Set<PetrinetNode>> postset;

	/*
	 * Maps an activity label to its transition, including the artificial start
	 * and end activities.
	 */
	private Map<String, Transition> activities;

	private class ReplayResult{
		int missing = 0;
		int consumed = 0;
		int remaining = 0;
		int produced = 0;
		
		boolean hasPerfectFitness() {
			return missing == 0 && remaining == 0;
		}
		
		double getFitness() {
			return ((0.5*missing)/consumed) + ((0.5*remaining)/produced);
		}
	};
	
	/**
	 * Replay every trace of the log on the given net. If the replay of a trace
	 * is successful, the "pdc:isPos" boolean attribute with the value true is
	 * added to the trace, otherwise the "pdc:isPos" boolean attribute with the
	 * value false is added.
	 * 
	 * @param log
	 * @param apn
	 * @return
	 */
	public XLog apply(XLog log, AcceptingPetriNet apn) {
		/*
		 * Get the classidier.
		 */
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!log.getClassifiers().isEmpty()) {
			classifier = log.getClassifiers().get(0);
		}

		/*
		 * Initialize and build the preset, postset, and mapping.
		 */
		preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		activities = new HashMap<String, Transition>();

		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
			postset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			postset.get(edge.getSource()).add(edge.getTarget());
			preset.get(edge.getTarget()).add(edge.getSource());
		}

		for (Transition transition : apn.getNet().getTransitions()) {
			if (!transition.isInvisible() || transition.getLabel().equals(ActivityAlphabet.START)
					|| transition.getLabel().equals(ActivityAlphabet.END)) {
				activities.put(transition.getLabel(), transition);
			}
		}

		/*
		 * Replay every trace on the net. If the replay fails, the null marking
		 * will be returned. For a successful replay, we still need to check
		 * that after the replay the reached marking is a final marking.
		 */
		Marking initialMarking = apn.getInitialMarking();
		Set<Marking> finalMarkings = apn.getFinalMarkings();

		for (XTrace trace : log) {
			ReplayResult result = new ReplayResult();
			// Replay the trace.
			Marking marking = replay(trace, classifier, apn.getNet(), initialMarking, result);
			// Clean up, that is, go to the best-fitting final marking.
			marking = cleanUp(marking, finalMarkings, result);
			XAttributeBoolean isPosAttribute = XFactoryRegistry.instance().currentDefault()
					.createAttributeBoolean("pdc:isPos", result.hasPerfectFitness(), null);
			trace.getAttributes().put("pdc:isPos", isPosAttribute);
			XAttributeContinuous isFitnessAttribute = XFactoryRegistry.instance().currentDefault()
					.createAttributeContinuous("pdc:fitness", result.getFitness(), null);
			trace.getAttributes().put("pdc:isPos", isPosAttribute);
			trace.getAttributes().put("pdc:fitness", isFitnessAttribute);
		}

		return log;
	}

	/**
	 * Replays the given trace on the given net using the given classifier and
	 * the given initial marking. Returns the marking that results after the
	 * replay, or null if the replay has failed.
	 * 
	 * @param trace
	 *            The given trace
	 * @param classifier
	 *            The given classifier
	 * @param net
	 *            The given net
	 * @param initialMarking
	 *            The given initial marking
	 * @param result
	 *            The replay result
	 * @return The marking that results from the replay, or null if the replays
	 *         has failed
	 */
	private Marking replay(XTrace trace, XEventClassifier classifier, Petrinet net, Marking initialMarking, ReplayResult result) {
		Marking marking = initialMarking;
		// If present, first try to fire the artificial start activity.
		if (activities.containsKey(ActivityAlphabet.START)) {
			marking = fire(net, marking, activities.get(ActivityAlphabet.START), result);
		}
		// Then try to fire all activities in the trace.
		for (XEvent event : trace) {
			marking = fire(net, marking, activities.get(classifier.getClassIdentity(event)), result);
		}
		// Finally, if present, try to fire the artificial end activity.
		if (activities.containsKey(ActivityAlphabet.END)) {
			marking = fire(net, marking, activities.get(ActivityAlphabet.END), result);
		}
		// Return the resulting marking.
		return marking;
	}

	/**
	 * Fires the given transition in the given net at the given marking. Returns
	 * the marking that results of this firing, or null if the transition could
	 * not be fired.
	 * 
	 * @param net
	 *            The given net
	 * @param marking
	 *            The given marking
	 * @param transition
	 *            The given transition
	 * @return THe marking that results from firing the transition, or null if
	 *         it could not be fired.
	 */
	private Marking fire(Petrinet net, Marking marking, Transition transition, ReplayResult result) {
		Marking newMarking = new Marking(marking);
		// Check all places in the preset of the transition.
		for (PetrinetNode place : preset.get(transition)) {
			// Variable ok indicates that the place is marked, or can be marked after firing an invisible transition first.
			if (newMarking.contains(place)) {
				// Place is marked. Remove the token.
				result.consumed++;
				newMarking.remove(place);
			} else {
				// Place is not marked. Look for invisible transitions that can feed a token into the place. 
				boolean found = false;
				for (PetrinetNode invisibleTransition : preset.get(place)) {
					if (found) {
						continue;
					}
					if (!((Transition) invisibleTransition).isInvisible()) {
						// Transition is not invisible.
						continue;
					}
					if (preset.get(invisibleTransition).size() != 1 || postset.get(invisibleTransition).size() != 1) {
						// Transition is invisible, but has multiple inputs or multiple outputs. 
						// Must be artificial start or end transition, which should not be taken into account here.
						continue;
					}
					if (newMarking.contains(preset.get(invisibleTransition).iterator().next())) {
						// Found an invisible transition of which the input place is marked.
						// Remove this token (assume the invisible transition fires first).
						found = true;
						newMarking.remove(preset.get(invisibleTransition).iterator().next());
						result.consumed++;
					}
				}
				if (!found) {
					result.missing++;
					result.consumed++;
				}
			}
		}
		// All input places were marked. Not add tokens to all output places.
		for (PetrinetNode place : postset.get(transition)) {
			newMarking.add((Place) place);
			result.produced++;
		}
		// Return the new marking.
		return newMarking;
	}
	
	Marking cleanUp(Marking marking, Set<Marking> finalMarkings, ReplayResult result) {
		Marking bestFinalMarking = null;
		Marking bestMissingMarking = null;
		Marking bestRemainingMarking = null;
		for (Marking finalMarking : finalMarkings) {
			Marking missingMarking = new Marking(finalMarking);
			missingMarking.removeAll(marking);
			Marking remainingMarking = new Marking(marking);
			remainingMarking.removeAll(finalMarking);
			if (bestFinalMarking == null) {
				bestFinalMarking = finalMarking;
				bestMissingMarking = missingMarking;
				bestRemainingMarking = remainingMarking;
			} else if (missingMarking.size() + remainingMarking.size() < bestMissingMarking.size() + bestRemainingMarking.size()) {
				bestFinalMarking = finalMarking;
				bestMissingMarking = missingMarking;
				bestRemainingMarking = remainingMarking;
			}
		}
		result.missing += bestMissingMarking.size();
		result.remaining += bestRemainingMarking.size();
		return bestFinalMarking;
	}
}
