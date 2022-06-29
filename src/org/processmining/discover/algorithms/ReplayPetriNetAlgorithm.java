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

	private Map<PetrinetNode, Set<PetrinetNode>> preset;
	private Map<PetrinetNode, Set<PetrinetNode>> postset;
	private Map<String, Transition> activities;

	public XLog apply(XLog log, AcceptingPetriNet apn) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!log.getClassifiers().isEmpty()) {
			classifier = log.getClassifiers().get(0);
		}

		preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		activities = new HashMap<String, Transition>();
		
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {

			if (!postset.containsKey(edge.getSource())) {
				postset.put(edge.getSource(), new HashSet<PetrinetNode>());
			}
			postset.get(edge.getSource()).add(edge.getTarget());
			if (!preset.containsKey(edge.getTarget())) {
				preset.put(edge.getTarget(), new HashSet<PetrinetNode>());
			}
			preset.get(edge.getTarget()).add(edge.getSource());
		}

		for (Transition transition : apn.getNet().getTransitions()) {
			if (!transition.isInvisible() || transition.getLabel().equals(ActivityAlphabet.START)
					|| transition.getLabel().equals(ActivityAlphabet.END)) {
				activities.put(transition.getLabel(), transition);
			}
		}

		Marking initialMarking = apn.getInitialMarking();
		Set<Marking> finalMarkings = apn.getFinalMarkings();

		for (XTrace trace : log) {
			Marking marking = replay(trace, classifier, apn.getNet(), initialMarking);
			XAttributeBoolean isPosAttribute = XFactoryRegistry.instance().currentDefault()
					.createAttributeBoolean("pdc:isPos", marking != null && finalMarkings.contains(marking), null);
			trace.getAttributes().put("pdc:isPos", isPosAttribute);
		}

		return log;
	}

	private Marking replay(XTrace trace, XEventClassifier classifier, Petrinet net, Marking initialMarking) {
		Marking marking = initialMarking;
		if (activities.containsKey(ActivityAlphabet.START)) {
			marking = fire(net, marking, activities.get(ActivityAlphabet.START));
		}
		for (XEvent event : trace) {
			if (marking != null) {
				marking = fire(net, marking, activities.get(classifier.getClassIdentity(event)));
			}
		}
		if (marking != null && activities.containsKey(ActivityAlphabet.END)) {
			marking = fire(net, marking, activities.get(ActivityAlphabet.END));
		}
		return marking;
	}

	private Marking fire(Petrinet net, Marking marking, Transition transition) {
		Marking newMarking = new Marking(marking);
		for (PetrinetNode place : preset.get(transition)) {
			boolean ok = false;
			if (newMarking.contains(place)) {
				ok = true;
				newMarking.remove(place);
			} else {
				for (PetrinetNode invisibleTransition : preset.get(place)) {
					if (ok) {
						continue;
					}
					if (!((Transition) invisibleTransition).isInvisible()) {
						continue;
					}
					if (preset.get(invisibleTransition).size() != 1 || postset.get(invisibleTransition).size() != 1) {
						continue;
					}
					if (newMarking.contains(preset.get(invisibleTransition).iterator().next())) {
						ok = true;
						newMarking.remove(preset.get(invisibleTransition).iterator().next());
					}
				}
			}
			if (!ok) {
				return null;
			}
		}
		for (PetrinetNode place : postset.get(transition)) {
			newMarking.add((Place) place);
		}
		return newMarking;
	}
}
