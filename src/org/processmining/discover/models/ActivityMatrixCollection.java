package org.processmining.discover.models;

import javax.swing.JComponent;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class ActivityMatrixCollection {

	private ActivityMatrix matrices[];
	private int size;
	
	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets sets) {
		size = sets.size();
		matrices = new ActivityMatrix[size];
		for (int idx = 0; idx < size; idx++) {
			matrices[idx] = new ActivityMatrix(log, alphabet, sets.get(idx));
		}
	}
	
	public int size() {
		return size;
	}

	public ActivityMatrix get(int idx) {
		return matrices[idx];	
	}
	
	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		DotNode startNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.START, matrices[0].get(0)));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.END, matrices[0].get(0)));
		endNode.setOption("shape", "none");
		for (ActivityMatrix matrix : matrices) {
			matrix.addNodesAndEdges(dotGraph, startNode, endNode);
		}
		return new DotPanel(dotGraph);
	}
}
