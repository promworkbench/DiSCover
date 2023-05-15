package org.processmining.discover.models;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
		this(log, alphabet, new ActivitySet("Not", alphabet), null);
	}

	/**
	 * Discovers an activity matrix for the given event log using the given
	 * alphabet and using a set of activities to ignore.
	 * 
	 * @param log
	 *            The event log
	 * @param alphabet
	 *            The given alphabet
	 * @param ignoreSet
	 *            The activities to ignore
	 * @param rootMatrix
	 *            The matrix discovered earlier or the entire log
	 */
	public ActivityMatrix(ActivityLog log, ActivityAlphabet alphabet, ActivitySet ignoreSet,
			ActivityMatrix rootMatrix) {

		// Register the alphabet.
		this.alphabet = alphabet;

		// Initialize the arrays.
		edgeCounts = new int[alphabet.size()][alphabet.size()];
		nodeCounts = new int[alphabet.size()];

		// Get the index of the previous activity that was not ignored (and the one before that).
		int lastIdx = 0;
		// Do the counting.
		boolean noise = false;
		for (int idx = 1; idx < log.size(); idx++) {
			if (log.get(idx - 1) == 0) {
				// Starts a new trace. 
				noise = containsNoise(log, idx, rootMatrix);
			}
			if (noise) {
				/*
				 * Some DF pair was filtered out in the root matrix. As a result, we assume
				 * that this trace contains some noise. Leave it out completely.
				 */
				continue;
			}
//			if (rootMatrix != null && rootMatrix.get(log.get(idx - 1), log.get(idx)) < 0) {
//				/*
//				 * This DF pair was filtered out of the root matrix. As a
//				 * result, we should not add a DF pair in this matrix.
//				 */
//				lastIdx = -1;
//			}
			if (!ignoreSet.contains(log.get(idx))) {
				// Not ignored. Count.
				nodeCounts[log.get(idx)]++;
//				if (lastIdx >= 0) {
					edgeCounts[log.get(lastIdx)][log.get(idx)]++;
//				}
				lastIdx = idx;
			}
		}
	}

	public ActivityMatrix(ActivityMatrix matrix) {
		this.alphabet = matrix.alphabet;
		edgeCounts = new int[alphabet.size()][alphabet.size()];
		nodeCounts = new int[alphabet.size()];
		for (int i = 0; i < alphabet.size(); i++) {
			nodeCounts[i] = matrix.nodeCounts[i];
			for (int j = 0; j < alphabet.size(); j++) {
				edgeCounts[i][j] = matrix.edgeCounts[i][j];
			}
		}
	}

	/*
	 * Return whether the current trace contains a DF pair that is filtered out in the root matrix.
	 */
	private boolean containsNoise(ActivityLog log, int idx, ActivityMatrix rootMatrix) {
		if (rootMatrix == null) {
			// No root matrix. Hence no such DF pair.
			return false;
		}
		if (rootMatrix.get(log.get(idx - 1), log.get(idx)) < 0) {
			// Found such a DF pair at the current index.
			return true;
		}
		if (log.get(idx) == 0) {
			// End of trace. No such DF pair found for this trace.
			return false;
		}
		// Try next index.
		return containsNoise(log, idx + 1, rootMatrix);
	}
	
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof ActivityMatrix) {
			ActivityMatrix matrix = (ActivityMatrix) o;
			if (!alphabet.equals(matrix.alphabet)) {
				return false;
			}
			for (int i = 0; i < alphabet.size(); i++) {
				if (nodeCounts[i] != matrix.nodeCounts[i]) {
					return false;
				}
				for (int j = 0; j < alphabet.size(); j++) {
					if (edgeCounts[i][j] != matrix.edgeCounts[i][j]) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
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

	public void set(int fromIdx, int toIdx, int value) {
		if (value != edgeCounts[fromIdx][toIdx]) {
//			nodeCounts[fromIdx] += (value - edgeCounts[fromIdx][toIdx]);
			edgeCounts[fromIdx][toIdx] = value;
		}
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
				if (nodeCounts[fromIdx] > 0 && nodeCounts[toIdx] > 0 && edgeCounts[fromIdx][toIdx] > 0) {
					DotEdge dotEdge = dotGraph.addEdge(fromIdx == 0 ? startNode : map.get(fromIdx),
							toIdx == 0 ? endNode : map.get(toIdx));
					dotEdge.setLabel("" + edgeCounts[fromIdx][toIdx]);
					if (fromIdx != 0 && toIdx != 0 && edgeCounts[toIdx][fromIdx] > 0) {
						dotEdge.setOption("color", "red");
						map.get(fromIdx).setOption("color", "red");
					} else {
						dotEdge.setOption("color", "blue");
					}
				}
			}
		}
	}

	/**
	 * Returns a JCOmponent containing the directly-follows graph (using Dot).
	 * 
	 * @return A JCOmponent containing the directly-follows graph
	 */
	public JPanel getComponent() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double size[][] = { { TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL } };
		panel.setLayout(new TableLayout(size));

		final JLabel providersLabel = new JLabel("Check root graph");
		providersLabel.setOpaque(false);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		panel.add(providersLabel, "0, 0");
		Dot dotGraph = new Dot();

		DotNode startNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.START, nodeCounts[0]));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.END, nodeCounts[0]));
		endNode.setOption("shape", "none");
		addNodesAndEdges(dotGraph, startNode, endNode);
		panel.add(new DotPanel(dotGraph), "0, 1");
		return panel;
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
			ActivitySet activities = new ActivitySet("To", alphabet);
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
			ActivitySet activities = new ActivitySet("From", alphabet);
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
				if (edgeCounts[fromIdx][toIdx] < 0) {
					set(fromIdx, toIdx, -edgeCounts[fromIdx][toIdx]);
				}
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
					set(fromIdx, toIdx, -edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

	/**
	 * Filters the matrix on the given relative threshold and given safety
	 * threshold.
	 * 
	 * @param relativeThreshold
	 *            The given relative threshold
	 * @param safetyThreshold
	 *            The given safety threshold
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
					set(fromIdx, toIdx, -edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

}
