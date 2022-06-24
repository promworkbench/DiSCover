package org.processmining.discover.algorithms;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.discover.models.TheActivitySets;
import org.processmining.discover.models.TheConcurrencyInfo;
import org.processmining.discover.models.TheLog;
import org.processmining.discover.models.TheMatrix;
import org.processmining.discover.models.TheMatrixCollection;
import org.processmining.discover.parameters.TheDiscoverPetriNetParameters;

public class TheDiscoverPetriNetAlgorithm {

	public static AcceptingPetriNet apply(XLog eventLog, TheDiscoverPetriNetParameters parameters) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		TheLog log = new TheLog(eventLog, classifier);
		TheMatrix matrix = new TheMatrix(log);

		TheConcurrencyInfo info = matrix.getConcurrencyInfo();
		TheActivitySets sets = TheSeparateAllConcurrentActivitiesAlgorithm.apply(info);
		TheMatrixCollection matrices = TheCreateMatrixCollectionAlgorithm.apply(log, matrix, sets);
		
		return matrices.createNet(log, parameters);
	}
}
