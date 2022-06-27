package org.processmining.discover.models;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityLog {

	private ActivityAlphabet alphabet;
	private int[] activities;
	private int size;

	public ActivityLog(XLog log, XEventClassifier classifier, ActivityAlphabet alphabet) {
		this.alphabet = alphabet;
		size = 1 + log.size();
		for (XTrace trace : log) {
			size += trace.size();
		}
		activities = new int[size];
		size = 0;
		activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				activities[size++] = alphabet.get(classifier.getClassIdentity(event));
			}
			activities[size++] = alphabet.get(ActivityAlphabet.STARTEND);
		}
	}
	
	public int get(int idx) {
		return activities[idx];
	}
	
	public int size() {
		return size;
	}
	
	public ActivityAlphabet getAlphabet() {
		return alphabet;
	}
}
