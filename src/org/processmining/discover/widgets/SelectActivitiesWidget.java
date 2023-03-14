package org.processmining.discover.widgets;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XLog;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.framework.util.ui.widgets.ProMList;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SelectActivitiesWidget extends JPanel implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7976742514293349986L;
	
	private DiscoverPetriNetParameters parameters;
	private ProMList<String> listPanel = null;
	
	public SelectActivitiesWidget(XLog log, DiscoverPetriNetParameters parameters) {
		this.parameters = parameters;
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		parameters.setAlphabet(new ActivityAlphabet(log, parameters.getClassifier()));

		if (parameters.getActivities() == null || !matches(parameters.getAlphabet(), parameters.getActivities())) {
			/*
			 * Either the activities have not been set before, or the some of the last selected 
			 * activities do not match the current classifier.
			 * Reset the activities.
			 */
			reset(log, parameters);
		}
		listPanel = getMainComponent(log, parameters);
		add(listPanel, "0, 0");

//		SlickerButton resetButton = new SlickerButton("Reset");
//		resetButton.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				if (listPanel != null) {
//					remove(listPanel);
//				}
//				reset(log, parameters, alphabet);
//				listPanel = getMainComponent(log, parameters, alphabet);
//				add(listPanel, "0, 0");
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

	private ProMList<String> getMainComponent(XLog log, DiscoverPetriNetParameters parameters) {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (int i = 1; i < parameters.getAlphabet().size(); i++) {
			listModel.addElement(parameters.getAlphabet().get(i));
		}
		int selectedIndices[] = new int[parameters.getActivities().size()];
		int j = 0;
		for (int i = 1; i < parameters.getAlphabet().size(); i++) {
			if (parameters.getActivities().contains(listModel.get(i - 1))) {
				selectedIndices[j++] = i - 1;
			}
		}
		ProMList<String> list = new ProMList<String>("Select activities", listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (parameters.getAlphabet().size() - 1 == j) {
			/*
			 * Faster way to select all items in the list.
			 */
			list.getList().setSelectionInterval(0, parameters.getAlphabet().size() - 2);
		} else {
			/*
			 * Slower way to select some items in the list.
			 */
			list.setSelectedIndices(selectedIndices);
		}
		list.addListSelectionListener(this);

		parameters.setActivities(list.getSelectedValuesList());
		
		list.setPreferredSize(new Dimension(100,100));
		return list;
	}
	
	private boolean matches(ActivityAlphabet alphabet, List<String> activities) {
		for (String activity : activities) {
			if (!alphabet.contains(activity)) {
				/*
				 * The selected activity is not valid anymore.
				 */
				return false;
			}
		}
		return true;
	}
	
	private void reset(XLog log, DiscoverPetriNetParameters parameters) {
		List<String> activities = new ArrayList<String>();
		for (int i = 1; i < parameters.getAlphabet().size(); i++) {
			activities.add(parameters.getAlphabet().get(i));
		}
		if (listPanel == null || !activities.equals(listPanel.getSelectedValuesList())) {
			parameters.setActivities(activities);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if (!parameters.getActivities().equals(listPanel.getSelectedValuesList())) {
			parameters.setActivities(listPanel.getSelectedValuesList());
		}
	}
}
