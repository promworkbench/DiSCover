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
		
		/*
		 * Transitions scheduled to be removed. These can only be removed afterwards, as here
		 * we are still iterating over all transitions.
		 */
		Set<Transition> transitionsToRemove = new HashSet<Transition>();
		for (Transition transitionToKeep : simpleSilentTransitions) {
			if (transitionsToRemove.contains(transitionToKeep)) {
				// Scheduled to remove, ignore it.
				continue;
			}
			for (Transition transitionToRemove : simpleSilentTransitions) {
				if (transitionToKeep == transitionToRemove) {
					// Do not compare a transition with itself :-).
					continue;
				}
				if (transitionsToRemove.contains(transitionToRemove)) {
					// Scheduled to remove, ignore it.
					continue;
				}
				/*
				 * Places to merge. All arcs will be copied from the place to remove to the place to keep,
				 * and then the place to remove will be removed.
				 */
				Place placeToKeep = null;
				Place placeToRemove = null;
				if (preset.get(transitionToRemove).iterator().next() == preset.get(transitionToKeep).iterator().next()) {
					// Transitions share same input place. Select output places to merge.
					placeToKeep = (Place) postset.get(transitionToKeep).iterator().next();
					placeToRemove = (Place) postset.get(transitionToRemove).iterator().next();
				} else if (postset.get(transitionToRemove).iterator().next() == postset.get(transitionToKeep).iterator().next()) {
					// Transitions share same output place. Select input places to merge.
					placeToKeep = (Place) preset.get(transitionToKeep).iterator().next();
					placeToRemove = (Place) preset.get(transitionToRemove).iterator().next();
				}
				if (placeToKeep != null && placeToRemove != null) {
					System.out.println("[ReduceSimpleSilentTransitionsClusterAlgorithm] Reducing " + placeToRemove + " to " + placeToKeep);
					if (placeToKeep == placeToRemove) {
						// Transitions also share same input place. Schedule to remove the transition to remove.
						transitionsToRemove.add(transitionToRemove);
						continue;
					}
					// Copy all the inputs of the place to remove to the place to keep.
					for (PetrinetNode node : preset.get(placeToRemove)) {
						apn.getNet().addArc((Transition) node, placeToKeep);
						// Adjust preset and postset accordingly.
						preset.get(placeToKeep).add(node);
						postset.get(node).add(placeToKeep);
						// Anticipate the fact that the place to remove will be removed.
						postset.get(node).remove(placeToRemove);
					}
					// Copy all the outputs of the place to remove to the place to keep.
					for (PetrinetNode node : postset.get(placeToRemove)) {
						apn.getNet().addArc(placeToKeep, (Transition) node);
						// Adjust preset and postset accordingly.
						postset.get(placeToKeep).add(node);
						preset.get(node).add(placeToKeep);
						// Anticipate the fact that the place to remove will be removed.
						preset.get(node).remove(placeToRemove);
					}
					// Now everything has been copied, remove the place to remove.
					apn.getNet().removePlace(placeToRemove);
					// Adjust preset and postset accordingly.
					preset.remove(placeToRemove);
					postset.remove(placeToRemove);
					// Schedule to remove the transition to remove.
					transitionsToRemove.add(transitionToRemove);
				}
			}
		}
		// Now remove all transitions scheduled for removal.
		for (Transition transition : transitionsToRemove) {
			apn.getNet().removeTransition(transition);
		}
	}
}
