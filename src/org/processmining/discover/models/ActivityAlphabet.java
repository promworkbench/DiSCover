package org.processmining.discover.models;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityAlphabet {

	public static final String START = "\u25BA";
	public static final String END = "\u25A0";
	public static final String STARTEND = START + "/" + END;

	private Map<String, Integer> activity2Idx;
	private String[] idx2Activity;
	private int size;

	public ActivityAlphabet(XLog log, XEventClassifier classifier) {
		activity2Idx = new HashMap<String, Integer>();
		activity2Idx.put(STARTEND, 0);
		size = 1;
		for (XTrace trace : log) {
			for (XEvent event: trace) {
				String activity = classifier.getClassIdentity(event);
				if (!activity2Idx.containsKey(activity)) {
					activity2Idx.put(activity,  size++);
				}
			}
		}
		idx2Activity = new String[size];
		for (String activity : activity2Idx.keySet()) {
			idx2Activity[activity2Idx.get(activity)] = activity;
		}
	}
	
	public String get(int idx) {
		return idx2Activity[idx];
	}
	
	public int get(String activity) {
		return activity2Idx.get(activity);
	}
	
	public int size() {
		return size;
	}
}
