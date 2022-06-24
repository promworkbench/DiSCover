package org.processmining.discover.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TheConcurrencyInfo {

	private Map<Set<Integer>, Integer> map;

	public TheConcurrencyInfo() {
		map = new HashMap<Set<Integer>, Integer>();
	}
	
	public void put(Set<Integer> activities, int score) {
		map.put(activities,  score);
	}
	
	public int get(Set<Integer> activities) {
		return map.get(activities);
	}
	
	public Set<Set<Integer>> getConcurrentSets() {
		return map.keySet();
	}
	
	public String toString() {
		return map.toString();
	}
}
