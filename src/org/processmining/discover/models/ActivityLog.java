package org.processmining.discover.models;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityLog {

	/*
	 * The alphabet used for this activity log.
	 */
	private ActivityAlphabet alphabet;

	/*
	 * The sequence of activities. An occurrence of the index corresponding to
	 * the artificial start-end activity denotes the end of this trace and the
	 * beginning of the next trace (if any).
	 */
	private int[] activities;

	/*
	 * The size of the activity log (length of activities array).
	 */
	private int size;

	/**
	 * Creates an activity log from the given event log using the given
	 * classifier and the given alphabet (should correspond to the event log).
	 * 
	 * @param log
	 *            The event log
	 * @param classifier
	 *            The given classifier
	 * @param alphabet
	 *            The given alphabet
	 */
	public ActivityLog(XLog log, XEventClassifier classifier, ActivityAlphabet alphabet) {
		this.alphabet = alphabet;
		// Allocate room for the artificial start-end activities.
		size = 1 + log.size();
		// Allocate room for the regular activities.
		for (XTrace trace : log) {
			size += trace.size();
		}
		activities = new int[size];
		size = 0;
		// Copy all necessary indices into the activity log.
		activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				activities[size++] = alphabet.get(classifier.getClassIdentity(event));
			}
			activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
		}
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
