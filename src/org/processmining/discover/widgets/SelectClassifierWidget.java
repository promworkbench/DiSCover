package org.processmining.discover.widgets;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.log.dialogs.ClassifierPanel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SelectClassifierWidget extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7976542514991505085L;

	public SelectClassifierWidget(XLog log, DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		
		if (parameters.getClassifier() == null || !log.getClassifiers().contains(parameters.getClassifier())) {
			if (log.getClassifiers().isEmpty()) {
				parameters.setClassifier(new XEventNameClassifier());
			} else {
				parameters.setClassifier(log.getClassifiers().get(0));
			}
		}
		add(new ClassifierPanel(log.getClassifiers(), parameters), "0, 0");

	}
}
