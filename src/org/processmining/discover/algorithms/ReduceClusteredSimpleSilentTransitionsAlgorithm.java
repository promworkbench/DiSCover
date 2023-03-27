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

public class ReduceClusteredSimpleSilentTransitionsAlgorithm {

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
				simpleSilentTransitions.add(transition);
			}
		}
		Collections.sort(simpleSilentTransitions, new Comparator<Transition>() {

			public int compare(Transition arg0, Transition arg1) {
				// TODO Auto-generated method stub
				return arg0.toString().compareTo(arg1.toString());
			}

		});
		
		Set<Transition> transitionsToRemove = new HashSet<Transition>();
		for (Transition transition1 : simpleSilentTransitions) {
			if (transitionsToRemove.contains(transition1)) {
				continue;
			}
			for (Transition transition2 : simpleSilentTransitions) {
				if (transition1 == transition2) {
					continue;
				}
				if (transitionsToRemove.contains(transition2)) {
					continue;
				}
				Set<PetrinetNode> nodes = new HashSet<PetrinetNode>(preset.get(transition2));
				nodes.retainAll(preset.get(transition1));
				if (!nodes.isEmpty()) {
					// Transitions share same input place. Merge output places.
					Place place1 = (Place) postset.get(transition1).iterator().next();
					Place place2 = (Place) postset.get(transition2).iterator().next();
					if (place1 == place2) {
						transitionsToRemove.add(transition2);
						continue;
					}
					System.out.println("[ReduceSimpleSilentTransitionsClusterAlgorithm] Reducing " + place2 + " to " + place1);
					for (PetrinetNode node : preset.get(place2)) {
						apn.getNet().addArc((Transition) node, place1);
						preset.get(place1).add(node);
						postset.get(node).add(place1);
						postset.get(node).remove(place2);
					}
					for (PetrinetNode node : postset.get(place2)) {
						apn.getNet().addArc(place1, (Transition) node);
						postset.get(place1).add(node);
						preset.get(node).add(place1);
						preset.get(node).remove(place2);
					}
					apn.getNet().removePlace(place2);
					preset.remove(place2);
					postset.remove(place2);
					transitionsToRemove.add(transition2);
					continue;
				}
				nodes = new HashSet<PetrinetNode>(postset.get(transition2));
				nodes.retainAll(postset.get(transition1));
				if (!nodes.isEmpty()) {
					// Transitions share same output place. Merge input places.
					Place place1 = (Place) preset.get(transition1).iterator().next();
					Place place2 = (Place) preset.get(transition2).iterator().next();
					if (place1 == place2) {
						transitionsToRemove.add(transition2);
						continue;
					}
					System.out.println("[ReduceSimpleSilentTransitionsClusterAlgorithm] Reducing " + place2 + " to " + place1);
					for (PetrinetNode node : preset.get(place2)) {
						apn.getNet().addArc((Transition) node, place1);
						preset.get(place1).add(node);
						postset.get(node).add(place1);
						postset.get(node).remove(place2);
					}
					for (PetrinetNode node : postset.get(place2)) {
						apn.getNet().addArc(place1, (Transition) node);
						postset.get(place1).add(node);
						preset.get(node).add(place1);
						preset.get(node).remove(place2);
					}
					apn.getNet().removePlace(place2);
					preset.remove(place2);
					postset.remove(place2);
					transitionsToRemove.add(transition2);
					continue;
				}
			}
		}
		
		for (Transition transition : transitionsToRemove) {
			apn.getNet().removeTransition(transition);
		}
	}
}
