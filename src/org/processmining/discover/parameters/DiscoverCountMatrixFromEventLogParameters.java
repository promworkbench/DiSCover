package org.processmining.discover.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;

public class DiscoverCountMatrixFromEventLogParameters extends DiscoverPetriNetFromCountMatrixParameters {

	private XLog log;
	
	private XEventClassifier classifier;

	public XLog getLog() {
		return log;
	}

	public void setLog(XLog log) {
		this.log = log;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}
}
