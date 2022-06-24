package org.processmining.discover.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TheInputOutputInfo {

	private Set<Set<Integer>>[] inputs;
	private Set<Set<Integer>>[] outputs;
	private Set<Integer> activities;

	@SuppressWarnings("unchecked")
	public TheInputOutputInfo(int size, int[][] edges) {
		inputs = new Set[size];
		outputs = new Set[size];
		activities = new HashSet<Integer>();

		for (int i = 0; i < size; i++) {
			inputs[i] = new HashSet<Set<Integer>>();
			outputs[i] = new HashSet<Set<Integer>>();
		}

		for (int fromIdx = 0; fromIdx < size; fromIdx++) {
			Set<Integer> output = new HashSet<Integer>();
			for (int toIdx = 0; toIdx < size; toIdx++) {
				if (edges[fromIdx][toIdx] > 0) {
					output.add(toIdx);
					activities.add(fromIdx);
					activities.add(toIdx);
				}
			}
			if (!output.isEmpty()) {
				outputs[fromIdx].add(output);
			}
		}

		System.out.println("[TheInputOutputInfo] acticities: " + activities);
		
		for (int toIdx = 0; toIdx < size; toIdx++) {
			Set<Integer> input = new HashSet<Integer>();
			for (int fromIdx = 0; fromIdx < size; fromIdx++) {
				if (edges[fromIdx][toIdx] > 0) {
					input.add(fromIdx);
				}
			}
			if (!input.isEmpty()) {
				inputs[toIdx].add(input);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public TheInputOutputInfo(Set<TheInputOutputInfo> infos) {
		int size = infos.iterator().next().inputs.length;
		inputs = new Set[size];
		outputs = new Set[size];

		for (int i = 0; i < size; i++) {
			inputs[i] = new HashSet<Set<Integer>>();
			outputs[i] = new HashSet<Set<Integer>>();
		}

		for (TheInputOutputInfo info : infos) {
			for (int i = 0; i < size; i++) {
				inputs[i].addAll(info.inputs[i]);
				outputs[i].addAll(info.outputs[i]);
			}
		}
	}

	public void add(Petrinet net, Transition startTransition, Transition endTransition, Transition transitions[],
			Map<Integer, Integer> inProxy, Map<Integer, Integer> outProxy) {
		Map<Integer, Place> inputPlaces = new HashMap<Integer, Place>();
		Map<Integer, Place> outputPlaces = new HashMap<Integer, Place>();

		// Add places.
		for (int idx : activities) {
			if (idx == 0) {
				continue;
			}
			for (Set<Integer> in : inputs[idx]) {
				if (outProxy.containsKey(idx)) {
					if (!inputPlaces.containsKey(outProxy.get(idx))) {
						inputPlaces.put(outProxy.get(idx), net.addPlace("i" + outProxy.get(idx) + ":" + in));
					}
					net.addArc(inputPlaces.get(outProxy.get(idx)), transitions[idx]);
				} else {
					inputPlaces.put(idx, net.addPlace("i" + idx + ":" + in));
					net.addArc(inputPlaces.get(idx), transitions[idx]);
				}
			}
			for (Set<Integer> out : outputs[idx]) {
				if (inProxy.containsKey(idx)) {
					if (!outputPlaces.containsKey(inProxy.get(idx))) {
						outputPlaces.put(inProxy.get(idx), net.addPlace("o" + inProxy.get(idx) + ":" + out));
					}
					net.addArc(transitions[idx], outputPlaces.get(inProxy.get(idx)));
				} else {
					System.out.println("[TheInputOutputInfo] add output place " + "o" + idx + ":" + out);
					outputPlaces.put(idx, net.addPlace("o" + idx + ":" + out));
					net.addArc(transitions[idx], outputPlaces.get(idx));
				}
			}
		}

		// Add invisible transitions
		for (int fromIdx : activities) {
			if (inProxy.containsKey(fromIdx)) {
				continue;
			}
			for (Set<Integer> out : outputs[fromIdx]) {
				for (int toIdx : activities) {
					if (outProxy.containsKey(toIdx)) {
						continue;
					}
					if (out.contains(toIdx)) {
						for (Set<Integer> in : inputs[toIdx]) {
							if (in.contains(fromIdx)) {
								if (fromIdx == 0) {
									net.addArc(startTransition, inputPlaces.get(toIdx));
								}
								if (toIdx == 0) {
									net.addArc(outputPlaces.get(fromIdx), endTransition);
								}
								if (fromIdx != 0 && toIdx != 0) {
									Transition transition = net.addTransition("t" + in + ":" + out);
									transition.setInvisible(true);
									net.addArc(outputPlaces.get(fromIdx), transition);
									net.addArc(transition, inputPlaces.get(toIdx));
								}
							}
						}
					}
				}
			}
		}
	}

	public String toString(TheLog log) {
		String s = "In:\n";
		for (int i = 0; i < inputs.length; i++) {
			s += "- " + log.getActivity(i);
			Set<Set<String>> nameSets = new HashSet<Set<String>>();
			for (Set<Integer> inputSet : inputs[i]) {
				Set<String> nameSet = new HashSet<String>();
				for (Integer idx : inputSet) {
					nameSet.add(log.getActivity(idx));
				}
				nameSets.add(nameSet);
			}
			s += ": " + nameSets + "\n";
		}
		s += "Out:\n";
		for (int i = 0; i < inputs.length; i++) {
			s += "- " + log.getActivity(i);
			Set<Set<String>> nameSets = new HashSet<Set<String>>();
			for (Set<Integer> outputSet : outputs[i]) {
				Set<String> nameSet = new HashSet<String>();
				for (Integer idx : outputSet) {
					nameSet.add(log.getActivity(idx));
				}
				nameSets.add(nameSet);
			}
			s += ": " + nameSets + "\n";
		}
		return s;
	}
}
