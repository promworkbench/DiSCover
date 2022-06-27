package org.processmining.discover.models;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class ActivityMatrix {

	private ActivityAlphabet alphabet;
	private int[][] edgeCounts;
	private int[] nodeCounts;

	public ActivityMatrix(ActivityLog log, ActivityAlphabet alphabet) {
		this(log, alphabet, new ActivitySet());
	}

	public ActivityMatrix(ActivityLog log, ActivityAlphabet alphabet, ActivitySet filteredOut) {
		this.alphabet = alphabet;
		edgeCounts = new int[alphabet.size()][alphabet.size()];
		nodeCounts = new int[alphabet.size()];
		
		int lastIdx = log.get(0);
		for (int idx = 1; idx < log.size(); idx++) {
			if (!filteredOut.contains(log.get(idx))) {
				nodeCounts[log.get(idx)]++;
				edgeCounts[lastIdx][log.get(idx)]++;
				lastIdx = log.get(idx);
			}
		}
	}

	public int get(int fromIdx, int toIdx) {
		return edgeCounts[fromIdx][toIdx];
	}
	
	public int get(int idx) {
		return nodeCounts[idx];
	}
	
	public String getNodeLabel(int idx) {
		return getNodeLabel(alphabet.get(idx), nodeCounts[idx]);
	}

	public String getNodeLabel(String activity, int nodeCount) {
		return "<<table align=\"center\"" + " border=\"1\"" + " cellborder=\"0\"" + " cellpadding=\"2\""
				+ " columns=\"*\"" + " style=\"rounded\">" + "<tr><td><font point-size=\"24\"><b>" + activity
				+ "</b></font></td></tr>" + "<hr/>" + "<tr><td>" + nodeCount + "</td></tr>" + "</table>>";
	}
	
	public void addNodesAndEdges(Dot dotGraph, DotNode startNode, DotNode endNode) {
		Map<Integer, DotNode> map = new HashMap<Integer, DotNode>();
		for (int fromIdx = 1; fromIdx < alphabet.size(); fromIdx++) {
			if (nodeCounts[fromIdx] > 0) {
				DotNode dotNode = dotGraph.addNode(getNodeLabel(fromIdx));
				dotNode.setOption("shape", "none");
				map.put(fromIdx, dotNode);
			}
		}

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

	public JComponent getComponent() {
		Dot dotGraph = new Dot();

		DotNode startNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.START, nodeCounts[0]));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(getNodeLabel(ActivityAlphabet.END, nodeCounts[0]));
		endNode.setOption("shape", "none");
		addNodesAndEdges(dotGraph, startNode, endNode);
		return new DotPanel(dotGraph);
	}

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
	
	public void restore() {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				edgeCounts[fromIdx][toIdx] = Math.abs(edgeCounts[fromIdx][toIdx]);
			}
		}
	}
	
	public void filterAbsolute(int threshold) {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (edgeCounts[fromIdx][toIdx] <= threshold) {
					edgeCounts[fromIdx][toIdx] = -Math.abs(edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}
	
	public void filterRelative(int threshold) {
		int fromMax[] = new int[alphabet.size()];
		int toMax[] = new int[alphabet.size()];

		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				fromMax[fromIdx] = Math.max(fromMax[fromIdx], edgeCounts[fromIdx][toIdx]);
				toMax[toIdx] = Math.max(toMax[toIdx], edgeCounts[fromIdx][toIdx]);
			}
		}
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			if (nodeCounts[fromIdx] == 0) {
				continue;
			}
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				if (nodeCounts[toIdx] == 0) {
					continue;
				}
				if (edgeCounts[fromIdx][toIdx]*100 <= Math.min(fromMax[fromIdx], toMax[toIdx])*threshold) {
					edgeCounts[fromIdx][toIdx] = -Math.abs(edgeCounts[fromIdx][toIdx]);
				}
			}
		}
	}

}
