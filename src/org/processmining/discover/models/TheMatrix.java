package org.processmining.discover.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class TheMatrix {

	private String[] activities;
	private int[][] edgeCounts;
	private int[] nodeCounts;

	/**
	 * Creates an index matrix from the given index log.
	 * 
	 * @param log
	 *            The given index log.
	 */
	public TheMatrix(TheLog log) {
		this(log, new HashSet<Integer>());
	}

	public TheMatrix(TheLog log, Set<Integer> filteredOut) {
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
	public String getNodeLabel(int idx) {
		return getNodeLabel(activities[idx], nodeCounts[idx]);
	}

	/*
	 * Returns a label for the given node.
	 */
	public String getNodeLabel(String activity, int nodeCount) {
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

		DotNode startNode = dotGraph.addNode(getNodeLabel(TheLog.START, nodeCounts[0]));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(getNodeLabel(TheLog.END, nodeCounts[0]));
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
	
	public TheConcurrencyInfo getConcurrencyInfo() {
		TheConcurrencyInfo concurrencyInfo = new TheConcurrencyInfo();
		for (int fromIdx = 0; fromIdx < activities.length; fromIdx++) {
			for (int toIdx = 0; toIdx < fromIdx; toIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0 && edgeCounts[toIdx][fromIdx] > 0 && //
						edgeCounts[fromIdx][toIdx] + edgeCounts[toIdx][fromIdx] <= //
						Math.min(nodeCounts[fromIdx], nodeCounts[toIdx])) {
					Set<Integer> activities = new HashSet<Integer>();
					activities.add(fromIdx);
					activities.add(toIdx);
					concurrencyInfo.put(activities,
							(int) ((4000L * edgeCounts[fromIdx][toIdx] * edgeCounts[toIdx][fromIdx])
									/ (nodeCounts[fromIdx] * nodeCounts[toIdx])));
				}
			}
		}
		return concurrencyInfo;
	}
	
	public int getNofTraces() {
		return nodeCounts[0];
	}
	
	public TheInputOutputInfo getInputOutputInfo() {
		return new TheInputOutputInfo(activities.length, edgeCounts);
	}
	
	public void add(Petrinet net, Transition startTransition, Transition endTransition) {
		Transition transitions[] = new Transition[activities.length];
		
		// Add visible transitions
		for (int idx = 1; idx < nodeCounts.length; idx++) {
			transitions[idx] = nodeCounts[idx] > 0 ? net.addTransition(activities[idx]) : null;
		}
		
		add(net, startTransition, endTransition, transitions);
	}

	public void add(Petrinet net, Transition startTransition, Transition endTransition, Transition transitions[]) {
		TheInputOutputInfo info = new TheInputOutputInfo(transitions.length, edgeCounts);

		// Find proxy places
		Map<Set<Integer>, Integer> inRepresentative = new HashMap<Set<Integer>, Integer>();
		Map<Integer, Integer> inProxy = new HashMap<Integer, Integer>();
		for (int fromIdx = 0; fromIdx < transitions.length; fromIdx++) {
			Set<Integer> toIndices = new HashSet<Integer>();
			for (int toIdx = 0; toIdx < transitions.length; toIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0) {
					toIndices.add(toIdx);
				}
			}
			if (toIndices.isEmpty()) {
				continue;
			}
			if (inRepresentative.containsKey(toIndices)) {
				inProxy.put(fromIdx, inRepresentative.get(toIndices));
			} else {
				inRepresentative.put(toIndices, fromIdx);
			}
		}

		Map<Set<Integer>, Integer> outRepresentative = new HashMap<Set<Integer>, Integer>();
		Map<Integer, Integer> outProxy = new HashMap<Integer, Integer>();
		for (int toIdx = 0; toIdx < transitions.length; toIdx++) {
			Set<Integer> fromIndices = new HashSet<Integer>();
			for (int fromIdx = 0; fromIdx < transitions.length; fromIdx++) {
				if (edgeCounts[fromIdx][toIdx] > 0) {
					fromIndices.add(fromIdx);
				}
			}
			if (fromIndices.isEmpty()) {
				continue;
			}
			if (outRepresentative.containsKey(fromIndices)) {
				outProxy.put(toIdx, outRepresentative.get(fromIndices));
			} else {
				outRepresentative.put(fromIndices, toIdx);
			}
		}

		// Glue them together.
		info.add(net, startTransition, endTransition, transitions, inProxy, outProxy);
	}
}
