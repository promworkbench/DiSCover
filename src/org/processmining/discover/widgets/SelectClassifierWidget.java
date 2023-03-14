package org.processmining.discover.widgets;

import javax.swing.JComponent;
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

	private JComponent classifierPanel = null;
	
	public SelectClassifierWidget(XLog log, DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		
		if (parameters.getClassifier() == null || !log.getClassifiers().contains(parameters.getClassifier())) {
			/*
			 * Either the classifier has not been set before, or the last classifier set does not exist in the current log.
			 * Reset the classifier.
			 */
			reset(log, parameters);
		}
		classifierPanel = new ClassifierPanel(log.getClassifiers(), parameters);
		add(classifierPanel, "0, 0");

//		SlickerButton resetButton = new SlickerButton("Reset");
//		resetButton.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				if (classifierPanel != null) {
//					remove(classifierPanel);
//				}
//				reset(log, parameters);
//				classifierPanel = new ClassifierPanel(log.getClassifiers(), parameters);
//				add(classifierPanel, "0, 0");
//				validate();
//				repaint();
//				/*
//				 * Get rid of animation that results from pressing the button.
//				 */
//				resetButton.setEnabled(false);
//				resetButton.setEnabled(true);
//			}
//			
//		});
//		add(resetButton, "0, 1");
	}
	
	private void reset(XLog log, DiscoverPetriNetParameters parameters) {
		if (log.getClassifiers().isEmpty()) {
			parameters.setClassifier(new XEventNameClassifier());
		} else {
			parameters.setClassifier(log.getClassifiers().get(0));
		}
	}
}
