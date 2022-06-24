package org.processmining.discover.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.discover.parameters.TheDiscoverPetriNetParameters;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class TheMatrixCollection {

	private TheMatrix matrix;
	private Set<TheMatrix> matrices;

	public TheMatrixCollection(TheMatrix matrix) {
		this.matrix = matrix;
		matrices = new HashSet<TheMatrix>();
	}

	public void add(TheMatrix matrix) {
		matrices.add(matrix);
	}

	public Set<TheMatrix> getMatrices() {
		return matrices;
	}

	public AcceptingPetriNet createNet(TheLog log, TheDiscoverPetriNetParameters parameters) {
		Petrinet net = PetrinetFactory.newPetrinet("Petri net DiSCovered");
		Map<Integer, Transition> transitions = new HashMap<Integer, Transition>();
		Map<Pair<Integer, Set<Integer>>, Place> inputPlaces = new HashMap<Pair<Integer, Set<Integer>>, Place>();
		Map<Pair<Integer, Set<Integer>>, Place> outputPlaces = new HashMap<Pair<Integer, Set<Integer>>, Place>();

		Transition startTransition = net.addTransition(log.START);
		startTransition.setInvisible(true);
		Transition endTransition = net.addTransition(log.END);
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

		if (parameters.isMerge()) {
			Transition visibleTransitions[] = new Transition[log.getNofActivities()];

			// Add visible transitions
			for (int idx = 1; idx < log.getNofActivities(); idx++) {
				visibleTransitions[idx] = net.addTransition(log.getActivity(idx));
			}

			for (TheMatrix matrix : matrices) {
				matrix.add(net, startTransition,
						endTransition, visibleTransitions);
			}
		} else {
			for (TheMatrix matrix : matrices) {
				matrix.add(net, startTransition,
						endTransition);
			}
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarkings);
	}

	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		DotNode startNode = dotGraph.addNode(matrix.getNodeLabel(TheLog.START, matrix.getNofTraces()));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(matrix.getNodeLabel(TheLog.END, matrix.getNofTraces()));
		endNode.setOption("shape", "none");
		for (TheMatrix matrix : matrices) {
			matrix.addNodesAndEdges(dotGraph, startNode, endNode);
		}
		return new DotPanel(dotGraph);
	}
}
