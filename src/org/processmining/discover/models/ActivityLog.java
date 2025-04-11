package org.processmining.discover.models;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityLog {

	/*
	 * The alphabet used for this activity log.
	 */
	private ActivityAlphabet alphabet;

	/*
	 * The sequence of activities. An occurrence of the index corresponding to the
	 * artificial start-end activity denotes the end of this trace and the beginning
	 * of the next trace (if any).
	 */
	private int[] activities;
	/*
	 * Whether the activity is part of a trace classified positive. If so, the DF
	 * relation from its predecessor to it should not be treated as noise.
	 */
	private boolean[] positive;

	/*
	 * The size of the activity log (length of activities array).
	 */
	private int size;

	private final String ISPOSKEY = "pdc:isPos";

	/**
	 * Creates an activity log from the given event log using the given classifier
	 * and the given alphabet (should correspond to the event log).
	 * 
	 * @param log        The event log
	 * @param classifier The given classifier
	 * @param alphabet   The given alphabet
	 */
	public ActivityLog(XLog log, XEventClassifier classifier, ActivityAlphabet alphabet) {
		this.alphabet = alphabet;
		// Allocate room for the artificial start-end activities.
		size = 1 + log.size();
		// Allocate room for the regular activities.
		for (XTrace trace : log) {
			boolean isPos = false;
			boolean isNeg = false;
			if (trace.getAttributes().containsKey(ISPOSKEY)) {
				XAttribute isPosAttribute = trace.getAttributes().get(ISPOSKEY);
				if (isPosAttribute instanceof XAttributeBoolean) {
					isPos = ((XAttributeBoolean) isPosAttribute).getValue();
					isNeg = !isPos;
				}
			}
			if (!isNeg) {
				for (XEvent event : trace) {
					String activity = classifier.getClassIdentity(event);
					if (alphabet.contains(activity)) {
						size++;
					}
				}
			} else {
				/*
				 * Trace is classified as negative. As a result, we will just skip this trace.
				 */
				size--;
			}
		}
		activities = new int[size];
		positive = new boolean[size];
		size = 0;
		// Copy all necessary indices into the activity log.
		positive[size] = false;
		activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
		for (XTrace trace : log) {
			boolean isPos = false;
			boolean isNeg = false;
			if (trace.getAttributes().containsKey(ISPOSKEY)) {
				XAttribute isPosAttribute = trace.getAttributes().get(ISPOSKEY);
				if (isPosAttribute instanceof XAttributeBoolean) {
					isPos = ((XAttributeBoolean) isPosAttribute).getValue();
					isNeg = !isPos;
				}
			}
			if (!isNeg) {
				for (XEvent event : trace) {
					String activity = classifier.getClassIdentity(event);
					if (alphabet.contains(activity)) {
						positive[size] = isPos;
						activities[size++] = alphabet.get(activity);
					}
				}
				positive[size] = isPos;
				activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
			}
		}
	}

	public ActivityLog(ActivityLog log) {
		alphabet = log.alphabet;
		size = log.size;
		activities = new int[size];
		positive = new boolean[size];
		for (int i = 0; i < size; i++) {
			activities[i] = log.activities[i];
			positive[i] = log.positive[i];
		}
	}

	public boolean filter(ActivityMatrix matrix, ActivitySet ignoreSet) {
		boolean didFilter = false;
		int startIdx = 0; // First start/end of trace
		int endIdx = startIdx + 1; // Next activity
		while (endIdx < size) {
			// Look for next start/end of trace.
			while (activities[endIdx] != 0) {
				endIdx++;
			}
			// Do not touch positive traces.
			if (!positive[endIdx]) {
				// Determine whether this trace may stay.
				boolean mayStay = true;
				int lastIdx = startIdx;
				for (int idx = startIdx + 1; idx < endIdx; idx++) {
					if (activities[idx] < 0) {
						continue;
					}
					if (ignoreSet.contains(activities[idx])) {
						continue;
					}
					if (matrix.get(activities[lastIdx], activities[idx]) <= 0) {
						System.out.println("[ActivityLog] Dropping edge " + alphabet.get(activities[lastIdx]) + "->" + alphabet.get(activities[idx]) + ": " + matrix.get(activities[lastIdx], activities[idx]));
					}
					mayStay = mayStay && (matrix.get(activities[lastIdx], activities[idx]) > 0);
					lastIdx = idx;
				}
				if (!mayStay) {
					for (int idx = startIdx; idx < endIdx; idx++) {
						if (activities[idx] > 0) {
							activities[idx] = -activities[idx];
							didFilter = true;
						}
					}
				}
			}
			startIdx = endIdx;
			endIdx = startIdx + 1;
		}
		return didFilter;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof ActivityLog) {
			ActivityLog log = (ActivityLog) o;
			if (size != log.size) {
				return false;
			}
			if (!alphabet.equals(log.alphabet)) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (positive[i] != log.positive[i]) {
					return false;
				}
				if (activities[i] != log.activities[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the activity index at the given index of the activity log.
	 * 
	 * @param idx The given index in the activity log
	 * @return The activity index at the given index
	 */
	public int get(int idx) {
		return activities[idx];
	}

	public boolean isPos(int idx) {
		return positive[idx];
	}

	/**
	 * Returns the size (number of indices) of the activity log.
	 * 
	 * @return The size of the activity log.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the alphabet used to create the activity log.
	 * 
	 * @return the alphabet.
	 */
	public ActivityAlphabet getAlphabet() {
		return alphabet;
	}
}
