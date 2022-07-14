package org.processmining.discover.models;

import java.util.HashSet;
import java.util.Set;

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
		reduce();
	}

	/**
	 * Return the number of matrices.
	 * 
	 * @return
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the matrix at the given index.
	 * 
	 * @param idx
	 *            The given index
	 * @return The matrix at the given index
	 */
	public ActivityMatrix get(int idx) {
		return matrices[idx];
	}

	/**
	 * Returns a JComponent(using Dot) containing the directly-follows graphs of
	 * all matrices side-by-side. The artificial start and end activities will
	 * be shared by all graphs.
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

	/**
	 * Have all matrices veto any noise. If any matrix says it is not noise, the matrices will follow this.
	 * 
	 * @param alphabet
	 *            The alphabet of activities.
	 */
	public void vetoNoise(ActivityAlphabet alphabet) {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				int notNoise = 0;
				for (ActivityMatrix matrix : matrices) {
					if (matrix.get(fromIdx, toIdx) > 0) {
						notNoise++;
					}
				}
				if (notNoise > 0) {
					// One says not noise, have all agree.
					for (ActivityMatrix matrix : matrices) {
						matrix.set(fromIdx, toIdx);
					}
				}
			}
		}
	}
	
	private void reduce() {
		Set<ActivitySet> nextActivities = new HashSet<ActivitySet>();
		Set<ActivitySet> previousActivities = new HashSet<ActivitySet>();
		Set<ActivityMatrix> selected = new HashSet<ActivityMatrix>();
		for (ActivityMatrix matrix : matrices) {
			if (nextActivities.addAll(matrix.getNextActivities().values())) {
				selected.add(matrix);
			}
			if (previousActivities.addAll(matrix.getPreviousActivities().values())) {
				selected.add(matrix);
			}
		}
		System.out.println("[ActivityMatrixCollection] Reduced from " + size + " to " + selected.size() + " matrices.");
		size = selected.size();
		matrices = new ActivityMatrix[size];
		int idx = 0;
		for (ActivityMatrix matrix : selected) {
			matrices[idx++] = matrix;
		}
	}
}
