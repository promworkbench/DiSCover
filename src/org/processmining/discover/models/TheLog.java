package org.processmining.discover.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class TheLog {

	private Map<String, Integer> activityMap;
	private String[] activities;

	private int[] events;

	public static final String START = "\u25BA";
	public static final String END = "\u25A0";

	/**
	 * Create an index log from a XES log given a classifier.
	 * 
	 * @param log
	 *            The XES log.
	 * @param classifier
	 *            The classifier.
	 */
	public TheLog(XLog log, XEventClassifier classifier) {
		int nofEvents = 1 + log.size(); // One START and as many ENDs as there are traces.

		/*
		 * Create mapping from names to indices.
		 */
		activityMap = new HashMap<String, Integer>();
		activityMap.put(END, 0);
		int nofNames = 1;
		for (XTrace trace : log) {
			nofEvents += trace.size();
			for (XEvent event : trace) {
				String name = classifier.getClassIdentity(event);
				if (!activityMap.containsKey(name)) {
					activityMap.put(name, nofNames++);
				}
			}
		}

		/*
		 * Create mapping from indices to names. Index 0 is mapped onto END, but
		 * also serves for START.
		 */
		activities = new String[nofNames];
		for (String name : activityMap.keySet()) {
			activities[activityMap.get(name)] = name;
		}

		/*
		 * Create the array with the events.
		 */
		events = new int[nofEvents];
		events[0] = 0; // START event
		int idx = 1;
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				String name = classifier.getClassIdentity(event);
				events[idx++] = activityMap.get(name);
			}
			events[idx++] = 0; // END event
		}
		
		for (int i = 0; i < activities.length; i++) {
			System.out.println("[TheLog] activity["+ i + "]: "+ activities[i]);
		}
	}

	/**
	 * Create an index log filtered out on the provided indices. The filtered log
	 * will use the same indices as the original log.
	 * 
	 * @param log
	 *            The index log to filter.
	 * @param indices
	 *            The indices to filter out.
	 */
	public TheLog(TheLog log, Set<Integer> indices) {
		activityMap = log.activityMap;
		activities = log.activities;
		int nofEvents = 0;
		for (int idx = 0; idx < events.length; idx++) {
			if (!indices.contains(events[idx])) {
				nofEvents++;
			}
		}
		events = new int[nofEvents];
		nofEvents = 0;
		for (int idx = 0; idx < events.length; idx++) {
			if (!indices.contains(events[idx])) {
				events[nofEvents++] = events[idx];
			}
		}
	}

	/**
	 * Returns the number of events.
	 * 
	 * @return The number of events.
	 */
	public int getNofEvents() {
		return events.length;
	}

	/**
	 * Returns the number of activities.
	 * START and END count as one activity.
	 * 
	 * @return The number of activities.
	 */
	public int getNofActivities() {
		return activities.length;
	}

	/**
	 * Returns the event (index) at the given index.
	 * 
	 * @param idx The given position.
	 * @return The event at the given position.
	 */
	public int get(int idx) {
		return events[idx];
	}
	
	/**
	 * Returns activity for the given index.
	 * 
	 * @param idx The given idnex.
	 * @return The activity for the given index.
	 */
	public String getActivity(int idx) {
		return activities[idx];
	}
}
