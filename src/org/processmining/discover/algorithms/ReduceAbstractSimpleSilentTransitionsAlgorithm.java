package org.processmining.discover.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public abstract class ReduceAbstractSimpleSilentTransitionsAlgorithm {

	public AcceptingPetriNet apply(PluginContext context, AcceptingPetriNet apn) {
		/*
		 * First, clone the entire APN.
		 */
		Map<DirectedGraphElement, DirectedGraphElement> cloneMap = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		Petrinet net = PetrinetFactory.clonePetrinet(apn.getNet(), cloneMap);
		Marking initialMarking = new Marking();
		for (Place p : apn.getInitialMarking()) {
			initialMarking.add((Place) cloneMap.get(p));
		}
		Set<Marking> finalMarkings = new HashSet<Marking>();
		for (Marking finalMarking : apn.getFinalMarkings()) {
			Marking marking = new Marking();
			for (Place p : finalMarking) {
				marking.add((Place) cloneMap.get(p));
			}
			finalMarkings.add(marking);
		}
		AcceptingPetriNet clonedApn = AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking,
				finalMarkings);
		/*
		 * Second, apply the reductions on the clone.
		 */
		applyInPlace(context, clonedApn);
		/*
		 * Return the clone;
		 */
		return clonedApn;
	}

	public void applyInPlace(PluginContext context, AcceptingPetriNet apn) {
//		int i = apn.getNet().getTransitions().size();
//		if (i <= 100) {
//			return;
//		}
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
		List<Transition> simpleSilentTransitions = new ArrayList<Transition>();
		for (Transition transition : apn.getNet().getTransitions()) {
			if (transition.isInvisible() && preset.get(transition).size() == 1 && postset.get(transition).size() == 1) {
				Place prePlace = (Place) preset.get(transition).iterator().next();
				if (apn.getInitialMarking().contains(prePlace)) {
					continue;
				}
				Place postPlace = (Place) postset.get(transition).iterator().next();
				boolean ok = true;
				for (Marking finalMarking: apn.getFinalMarkings()) {
					if (finalMarking.contains(postPlace)) {
						ok = false;
					}
				}
				if (!ok) {
					continue;
				}
				if (isOK(transition, preset, postset)) {
					simpleSilentTransitions.add(transition);
				}
			}
		}
		Collections.sort(simpleSilentTransitions, new Comparator<Transition>() {

			public int compare(Transition arg0, Transition arg1) {
				// TODO Auto-generated method stub
				return arg0.toString().compareTo(arg1.toString());
			}

		});
		Map<Place, Place> reduced = new HashMap<Place, Place>();
		for (Place place : apn.getNet().getPlaces()) {
			reduced.put(place, place);
		}
		for (Transition transition : simpleSilentTransitions) {
			System.out.println("[ReduceSimpleSilentTransitionsAlgorithm Reducing " + transition);
			Place prePlace = reduced.get(preset.get(transition).iterator().next());
			Place postPlace = reduced.get(postset.get(transition).iterator().next());
			if (prePlace.equals(postPlace)) {
				for (Place p : reduced.keySet()) {
					preset.get(p).remove(transition);
					postset.get(p).remove(transition);
				}
				apn.getNet().removeTransition(transition);
				continue;
			}
			Place place = apn.getNet().addPlace(prePlace.getLabel() + "+" + postPlace.getLabel());
			System.out.println("[ReduceSimpleSilentTransitionsAlgorithm Added " + place);
			for (PetrinetNode node : preset.get(reduced.get(prePlace))) {
				apn.getNet().addArc((Transition) node, place);
			}
			for (PetrinetNode node : preset.get(reduced.get(postPlace))) {
				if (!transition.equals(node)) {
					apn.getNet().addArc((Transition) node, place);
				}
			}
			for (PetrinetNode node : postset.get(reduced.get(prePlace))) {
				if (!transition.equals(node)) {
					apn.getNet().addArc(place, (Transition) node);
				}
			}
			for (PetrinetNode node : postset.get(reduced.get(postPlace))) {
				apn.getNet().addArc(place, (Transition) node);
			}
			Set<PetrinetNode> nodes = new HashSet<PetrinetNode>(preset.get(reduced.get(prePlace)));
			nodes.addAll(preset.get(reduced.get(postPlace)));
			nodes.remove(transition);
			preset.put(place, nodes);
			System.out.println("[ReduceSimpleSilentTransitions] Preset of " + place + " set to " + nodes);
			nodes = new HashSet<PetrinetNode>(postset.get(reduced.get(prePlace)));
			nodes.addAll(postset.get(reduced.get(postPlace)));
			nodes.remove(transition);
			postset.put(place, nodes);
			System.out.println("[ReduceSimpleSilentTransitions] Postset of " + place + " set to " + nodes);

			for (Place p : reduced.keySet()) {
				if (reduced.get(p).equals(prePlace) || reduced.get(p).equals(postPlace)) {
					reduced.put(p, place);
				}
			}
			reduced.put(prePlace, place);
			reduced.put(postPlace, place);
			reduced.put(place, place);

			apn.getNet().removeTransition(transition);
			apn.getNet().removePlace(prePlace);
			apn.getNet().removePlace(postPlace);
			
			if (apn.getInitialMarking().remove(prePlace)) {
				apn.getInitialMarking().add(place);
			}
			Set<Marking> finalMarkings = new HashSet<Marking>(apn.getFinalMarkings());
			for (Marking finalMarking : finalMarkings) {
				apn.getFinalMarkings().remove(finalMarking);
				if (finalMarking.remove(postPlace)) {					
					finalMarking.add(place);
				}
				apn.getFinalMarkings().add(finalMarking);
			}
//			i--;
//			if (i <= 100) {
//				return;
//			}
		}
	}
	
	abstract boolean isOK(Transition transition, Map<PetrinetNode, Set<PetrinetNode>> preset, Map<PetrinetNode, Set<PetrinetNode>> postset);
}
