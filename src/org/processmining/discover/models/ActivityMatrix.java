package org.processmining.discover.models;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class ActivityMatrix {

	/**
	 * The alphabet used to discover this matrix.
	 */
	private ActivityAlphabet alphabet;

	/**
	 * The edge counts in the activity log: How often is one activity
	 * directly-followed by another? If edgeCounts[a][b] = 10, then in the
	 * activity log a is directly followed 10 times by b.
	 */
	private int[][] edgeCounts;

	/**
	 * The node counts in the activity log: How often does an activity occur?
	 */
	private int[] nodeCounts;

	/**
	 * Discovers an activity matrix for the given event log using the given
	 * alphabet.
	 * 
	 * @param log
	 *            The event log
	 * @param alphabet
	 *            The given alphabet
	 */
	public ActivityMatrix(ActivityLog log, ActivityAlphabet alphabet) {
		this(log, alphabet, new ActivitySet());
	}

	/**
	 * Discovers an activity matrix for the given event log using the given
	 * alphabet and using a set of activities to ignore.
	 * 
	 * @param log
	 *            The event log
	 * @param alphabet
	 *            The given alphabet
	 * @param fitleredOut
	 *            The activities to ignore.
	 */
	public ActivityMatrix(ActivityLog log, ActivityAlphabet alphabet, ActivitySet ignoreSet) {

		// Register the alphabet.
		this.alphabet = alphabet;

		// Initialize the arrays.
		edgeCounts = new int[alphabet.size()][alphabet.size()];
		nodeCounts = new int[alphabet.size()];

		// Get the index of the previous activity that was not ignored.
		int lastIdx = log.get(0);
		// Do the counting.
		for (int idx = 1; idx < log.size(); idx++) {
			if (!ignoreSet.contains(log.get(idx))) {
				// Not ignored. Count.
				nodeCounts[log.get(idx)]++;
				edgeCounts[lastIdx][log.get(idx)]++;
				lastIdx = log.get(idx);
			}
		}
	}

	/**
	 * Returns how often the first index was directly followed by the second
	 * index in the activity log.
	 * 
	 * @param fromIdx
	 *            The first index
	 * @param toIdx
	 *            The second index
	 * @return How often the first index was directly followed by the second
	 *         index in the activity log
	 */
	public int get(int fromIdx, int toIdx) {
		return edgeCounts[fromIdx][toIdx];
	}

	public void set(int fromIdx, int toIdx) {
		edgeCounts[fromIdx][toIdx] = Math.abs(edgeCounts[fromIdx][toIdx]);
	}

	/**
	 * Returns how often the index occurred in the activity log.
	 * 
	 * @param idx
	 *            The index
	 * @return How often the index occurred in the activity log
	 */
	public int get(int idx) {
		return nodeCounts[idx];
	}

	/**
	 * Returns the node label for the given index.
	 * 
	 * @param idx
	 *            The index
	 * @return The node label for the given index
	 */
	public String getNodeLabel(int idx) {
		return getNodeLabel(alphabet.get(idx), nodeCounts[idx]);
	}

	/**
	 * Returns the node label for the given activity and the given count.
	 * 
	 * @param activity
	 *            The activity
	 * @param nodeCount
	 *            The count
	 * @return The node label for the given activity and the given count
	 */
	public String getNodeLabel(String activity, int nodeCount) {
		return "<<table align=\"center\"" + " border=\"1\"" + " cellborder=\"0\"" + " cellpadding=\"2\""
				+ " columns=\"*\"" + " style=\"rounded\">" + "<tr><td><font point-size=\"24\"><b>" + activity
				+ "</b></font></td></tr>" + "<hr/>" + "<tr><td>" + nodeCount + "</td></tr>" + "</table>>";
	}

	/**
	 * Adds all non-artificial nodes and edges to the given Dot graph, resulting
	 * in a directly-follows graph.
	 * 
	 * @param dotGraph
	 *            The Dot graph
	 * @param startNode
	 *            The node for the artificial start activity.
	 * @param endNode
	 *            The node for the artificial end activity
	 */
	public void addNodesAndEdges(Dot dotGraph, DotNode startNode, DotNode endNode) {
		Map<Integer, DotNode> map = new HashMap<Integer, DotNode>();
		// Add all nodes
		for (int fromIdx = 1; fromIdx < alphabet.size(); fromIdx++) {
			if (nodeCounts[fromIdx] > 0) {
				DotNode dotNode = dotGraph.addNode(getNodeLabel(fromIdx));
				dotNode.setOption("shape", "none");
				map.put(fromIdx, dotNode);
			}
		}
		// Add all edges
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0) {
					DotEdge dotEdge = dotGraph.addEdge(fromIdx == 0 ? startNode : map.get(fromIdx),
							toIdx == 0 ? endNode : map.get(toIdx));
					dotEdge.setLabel("" + edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

	/**
	 * Returns a JCOmponent containing the directly-follows graph (using Dot).
	 * 
	 * @return A JCOmponent containing the directly-follows graph
	 */
	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		DotNode startNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.START, nodeCounts[0]));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.END, nodeCounts[0]));
		endNode.setOption("shape", "none");
		addNodesAndEdges(dotGraph, startNode, endNode);
		return new DotPanel(dotGraph);
	}

	/**
	 * Returns a mapping that maps every activity onto the set of activities
	 * that can directly-follow that activity.
	 * 
	 * @return A mapping that maps every activity onto the set of activities
	 *         that can directly-follow that activity
	 */
	public Map<Integer, ActivitySet> getNextActivities() {
		Map<Integer, ActivitySet> nextActivities = new HashMap<Integer, ActivitySet>();
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			if (nodeCounts[fromIdx] == 0) {
				continue;
			}
			ActivitySet activities = new ActivitySet();
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (nodeCounts[toIdx] == 0) {
					continue;
				}
				if (edgeCounts[fromIdx][toIdx] > 0) {
					activities.add(toIdx);
				}
			}
			nextActivities.put(fromIdx, activities);
		}
		return nextActivities;
	}

	/**
	 * Returns a mapping that maps every activity onto the set of activities
	 * that can directly-precede that activity.
	 * 
	 * @return A mapping that maps every activity onto the set of activities
	 *         that can directly-precede that activity
	 */
	public Map<Integer, ActivitySet> getPreviousActivities() {
		Map<Integer, ActivitySet> previousActivities = new HashMap<Integer, ActivitySet>();
		for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
			if (nodeCounts[toIdx] == 0) {
				continue;
			}
			ActivitySet activities = new ActivitySet();
			for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
				if (nodeCounts[fromIdx] == 0) {
					continue;
				}
				if (edgeCounts[fromIdx][toIdx] > 0) {
					activities.add(fromIdx);
				}
			}
			previousActivities.put(toIdx, activities);
		}
		return previousActivities;
	}

	/**
	 * Restores the matrix from any filtering.
	 */
	public void restore() {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				edgeCounts[fromIdx][toIdx] = Math.abs(edgeCounts[fromIdx][toIdx]);
			}
		}
	}

	/**
	 * Filters the matrix on the given absolute threshold. Any count not
	 * exceeding this threshold will be filtered out.
	 * 
	 * @param threshold
	 *            The given absolute threshold
	 */
	public void filterAbsolute(int threshold) {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (edgeCounts[fromIdx][toIdx] <= 0) {
					continue;
				}
				if (edgeCounts[fromIdx][toIdx] <= threshold) {
					// Does not exceed threshold, filter out.
					edgeCounts[fromIdx][toIdx] = -Math.abs(edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

	/**
	 * Filters the matrix on the given relative threshold.
	 * 
	 * @param relativeThreshold
	 *            The given relative threshold
	 */
	public void filterRelative(int relativeThreshold, int safetyThreshold) {
		if (relativeThreshold == 0) {
			return;
		}
		
		// Arrays to hold the maximal values for any row (from) and any column (to).
		int fromMax[] = new int[alphabet.size()];
		int toMax[] = new int[alphabet.size()];

		// Initialize these arrays.
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				fromMax[fromIdx] = Math.max(fromMax[fromIdx], Math.abs(edgeCounts[fromIdx][toIdx]));
				toMax[toIdx] = Math.max(toMax[toIdx], Math.abs(edgeCounts[fromIdx][toIdx]));
			}
		}

		// Any count that does not exceed at least threshold percent of the lowest maximal value (row or column) is filtered out.
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			if (nodeCounts[fromIdx] == 0) {
				continue;
			}
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (nodeCounts[toIdx] == 0) {
					continue;
				}
				if (edgeCounts[fromIdx][toIdx] <= 0) {
					continue;
				}
				if (100 * edgeCounts[fromIdx][toIdx] >= safetyThreshold * Math.min(fromMax[fromIdx], toMax[toIdx])) {
					continue;
				}
//				if (((edgeCounts[fromIdx][toIdx] < fromMax[fromIdx] && 1000 * fromMax[fromIdx]
//								* edgeCounts[fromIdx][toIdx] < threshold * (fromMax[fromIdx] + edgeCounts[fromIdx][toIdx]) * (fromMax[fromIdx] + edgeCounts[fromIdx][toIdx]))
//						|| (edgeCounts[fromIdx][toIdx] < toMax[toIdx] && 1000 * toMax[toIdx]
//								* edgeCounts[fromIdx][toIdx] < threshold * (toMax[toIdx] + edgeCounts[fromIdx][toIdx]) * (toMax[toIdx] + edgeCounts[fromIdx][toIdx])))) {
				if (edgeCounts[fromIdx][toIdx] * 100 <= Math.max(fromMax[fromIdx], toMax[toIdx]) * relativeThreshold) {
					// Does not exceed threshold percent, filter out.
					edgeCounts[fromIdx][toIdx] = -Math.abs(edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

}
