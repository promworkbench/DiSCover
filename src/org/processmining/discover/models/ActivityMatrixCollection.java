package org.processmining.discover.models;

import javax.swing.JComponent;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class ActivityMatrixCollection {

	/**
	 * Array containing all matrices.
	 */
	private ActivityMatrix matrices[];

	/**
	 * Number of matrices.
	 */
	private int size;

	/**
	 * Discovers the matrices for the given activity log using the given
	 * alphabet and the given sets of activities to ignore. For every set of
	 * activities to ignore, a matrix will be discovered.
	 * 
	 * @param log
	 *            The activity log
	 * @param alphabet
	 *            The given alphabet
	 * @param ignoreSets
	 *            THe given activity sets to ignore
	 */
	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets ignoreSets) {
		size = ignoreSets.size();
		matrices = new ActivityMatrix[size];
		for (int idx = 0; idx < size; idx++) {
			matrices[idx] = new ActivityMatrix(log, alphabet, ignoreSets.get(idx));
		}
	}

	/**
	 * Return the number of matrices.
	 * @return
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the matrix at the given index.
	 * 
	 * @param idx The given index
	 * @return The matrix at the given index
	 */
	public ActivityMatrix get(int idx) {
		return matrices[idx];
	}

	/**
	 * Returns a JComponent(using Dot) containing the directly-follows graphs of all matrices side-by-side.
	 * The artificial start and end activities will be shared by all graphs.
	 * 
	 * @return The JComponent containing all directly-follows graphs.
	 */
	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		// Add the nodes for the artificial start and end activities.
		DotNode startNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.START, matrices[0].get(0)));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.END, matrices[0].get(0)));
		endNode.setOption("shape", "none");
		
		// Add all other nodes and edges.
		for (ActivityMatrix matrix : matrices) {
			matrix.addNodesAndEdges(dotGraph, startNode, endNode);
		}
		
		return new DotPanel(dotGraph);
	}
}
