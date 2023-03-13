package org.processmining.discover.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityAlphabet {

	/*
	 * Label for the artificial start activity.
	 */
	public static final String START = "\u25BA";
	/*
	 * Label for the artificial end activity.
	 */
	public static final String END = "\u25A0";
	/*
	 * Label used for both.
	 */
	public static final String STARTEND = START + "/" + END;

	/*
	 * Maps every activity onto its index. The start and end activity are mapped
	 * onto index 0.
	 */
	private Map<String, Integer> activity2Idx;
	/*
	 * Maps every index onto its activity.
	 */
	private String[] idx2Activity;
	/*
	 * The number of activities (where the artificial start and end activity
	 * count as a single start-end activity).
	 */
	private int size;

	/**
	 * Creates the alphabet from an event log given the classifier to use.
	 * 
	 * @param log The event log
	 * @param classifier The classifier to use
	 */
	public ActivityAlphabet(XLog log, XEventClassifier classifier) {
		this(getActivities(log, classifier));
	}

	public ActivityAlphabet(List<String> activities) {
		List<String> sortedActivities = new ArrayList<String>(activities);
		Collections.sort(sortedActivities);
		
		activity2Idx = new HashMap<String, Integer>();
		idx2Activity = new String[activities.size() + 1];
		activity2Idx.put(STARTEND, 0);
		idx2Activity[0] = STARTEND;
		size = 1;
		for (String activity : sortedActivities) {
			activity2Idx.put(activity, size);
			idx2Activity[size] = activity;
			size++;
		}
	}
	
	private static List<String> getActivities(XLog log, XEventClassifier classifier) {
		Set<String> activities = new HashSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				activities.add(classifier.getClassIdentity(event));
			}
		}
		return new ArrayList<String>(activities);
	}
	
	/**
	 * Returns the activity at the given index.
	 * 
	 * @param idx The given index.
	 * @return The activity at the given index.
	 */
	public String get(int idx) {
		return idx2Activity[idx];
	}

	/**
	 * Returns the index of the given activity.
	 * 
	 * @param activity The given activity.
	 * @return The index of the given activity.
	 */
	public int get(String activity) {
		return activity2Idx.get(activity);
	}
	
	/**
	 * Returns whether the set contains the given activity.
	 * 
	 * @param activity The given activity.
	 * @return Whether the set contains the given activity.
	 */
	public boolean contains(String activity) {
		return activity2Idx.containsKey(activity);
	}

	/**
	 * Returns the size of the alphabet (where the artificial start and end activity
	 * count as one single start-end activity).
	 * @return The size of the alphabet.
	 */
	public int size() {
		return size;
	}
}
