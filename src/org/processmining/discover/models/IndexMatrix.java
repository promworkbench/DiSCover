package org.processmining.discover.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class IndexMatrix {

	private String[] activities;
	private int[][] edgeCounts;
	private int[] nodeCounts;

	/**
	 * Creates an index matrix from the given index log.
	 * 
	 * @param log
	 *            The given index log.
	 */
	public IndexMatrix(IndexLog log) {
		this(log, new HashSet<Integer>());
	}

	public IndexMatrix(IndexLog log, Set<Integer> filteredOut) {
		activities = new String[log.getNofActivities()];
		for (int idx = 0; idx < log.getNofActivities(); idx++) {
			activities[idx] = log.getActivity(idx);
		}
		nodeCounts = new int[log.getNofActivities()];
		edgeCounts = new int[log.getNofActivities()][log.getNofActivities()];
		for (int fromIdx = 0; fromIdx < log.getNofActivities(); fromIdx++) {
			nodeCounts[fromIdx] = 0;
			for (int toIdx = 0; toIdx < log.getNofActivities(); toIdx++) {
				edgeCounts[fromIdx][toIdx] = 0;
			}
		}
		int lastIdx = log.get(0);
		for (int idx = 1; idx < log.getNofEvents(); idx++) {
			if (!filteredOut.contains(log.get(idx))) {
				nodeCounts[log.get(idx)]++;
				edgeCounts[lastIdx][log.get(idx)]++;
				lastIdx = log.get(idx);
			}
		}
	}

	/*
	 * Returns a label for the given node.
	 */
	private String getNodeLabel(int idx) {
		return getNodeLabel(activities[idx], nodeCounts[idx]);
	}

	/*
	 * Returns a label for the given node.
	 */
	private String getNodeLabel(String activity, int nodeCount) {
		return "<<table align=\"center\"" + " border=\"1\"" + " cellborder=\"0\"" + " cellpadding=\"2\""
				+ " columns=\"*\"" + " style=\"rounded\">" + "<tr><td><font point-size=\"24\"><b>" + activity
				+ "</b></font></td></tr>" + "<hr/>" + "<tr><td>" + nodeCount + "</td></tr>" + "</table>>";
	}

	/**
	 * Creates a Dot component visualizing the directly-follows graph for this
	 * index matrix.
	 * 
	 * @return The Dot component.
	 */
	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		int nofTraces = 0;
		for (int toIdx = 0; toIdx < activities.length; toIdx++) {
			nofTraces += edgeCounts[0][toIdx];
		}
		DotNode startNode = dotGraph.addNode(getNodeLabel(IndexLog.START, nofTraces));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(getNodeLabel(IndexLog.END, nofTraces));
		endNode.setOption("shape", "none");
		addNodesAndEdges(dotGraph, startNode, endNode);
		return new DotPanel(dotGraph);
	}

	public void addNodesAndEdges(Dot dotGraph, DotNode startNode, DotNode endNode) {
		Map<Integer, DotNode> map = new HashMap<Integer, DotNode>();
		for (int fromIdx = 1; fromIdx < activities.length; fromIdx++) {
			if (nodeCounts[fromIdx] > 0) {
				DotNode dotNode = dotGraph.addNode(getNodeLabel(fromIdx));
				dotNode.setOption("shape", "none");
				map.put(fromIdx, dotNode);
			}
		}

		for (int fromIdx = 0; fromIdx < activities.length; fromIdx++) {
			for (int toIdx = 0; toIdx < activities.length; toIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0) {
					DotEdge dotEdge = dotGraph.addEdge(fromIdx == 0 ? startNode : map.get(fromIdx),
							toIdx == 0 ? endNode : map.get(toIdx));
					dotEdge.setLabel("" + edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}
	
	public Map<Set<Integer>, Integer> getWeightedConcurrentActivities() {
		Map<Set<Integer>, Integer> concurrentActivities = new HashMap<Set<Integer>, Integer>();
		for (int fromIdx = 0; fromIdx < activities.length; fromIdx++) {
			for (int toIdx = 0; toIdx < fromIdx; toIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0 && edgeCounts[toIdx][fromIdx] > 0 && //
						edgeCounts[fromIdx][toIdx] + edgeCounts[toIdx][fromIdx] <= //
						Math.min(nodeCounts[fromIdx], nodeCounts[toIdx])) {
					Set<Integer> activities = new HashSet<Integer>();
					activities.add(fromIdx);
					activities.add(toIdx);
					concurrentActivities.put(activities,
							(int) ((4000L * edgeCounts[fromIdx][toIdx] * edgeCounts[toIdx][fromIdx])
									/ (nodeCounts[fromIdx] * nodeCounts[toIdx])));
				}
			}
		}
		return concurrentActivities;
	}
}
