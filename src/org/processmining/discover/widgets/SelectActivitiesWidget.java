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
	private ProMList<String> list;
	
	public SelectActivitiesWidget(XLog log, DiscoverPetriNetParameters parameters) {
		this.parameters = parameters;
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		ActivityAlphabet alphabet = new ActivityAlphabet(log, parameters.getClassifier());
		
		if (parameters.getActivities() == null || !matches(alphabet, parameters.getActivities())) {
			List<String> activities = new ArrayList<String>();
			for (int i = 1; i < alphabet.size(); i++) {
				activities.add(alphabet.get(i));
			}
			parameters.setActivities(activities);
		}
		for (int i = 1; i < alphabet.size(); i++) {
			listModel.addElement(alphabet.get(i));
		}
		int selectedIndices[] = new int[parameters.getActivities().size()];
		int j = 0;
		for (int i = 1; i < alphabet.size(); i++) {
			if (parameters.getActivities().contains(listModel.get(i - 1))) {
				selectedIndices[j++] = i - 1;
			}
		}
		list = new ProMList<String>("Alphabet", listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (alphabet.size() - 1 == j) {
			/*
			 * Faster way to select all items in the list.
			 */
			list.getList().setSelectionInterval(0, alphabet.size() - 2);
		} else {
			/*
			 * Slower way to select some items in the list.
			 */
			list.setSelectedIndices(selectedIndices);
		}
		list.addListSelectionListener(this);

		parameters.setActivities(list.getSelectedValuesList());
		
		list.setPreferredSize(new Dimension(100,100));
		add(list, "0, 0");

	}

	private boolean matches(ActivityAlphabet alphabet, List<String> activities) {
		for (String activity : activities) {
			if (!alphabet.contains(activity)) {
				return false;
			}
		}
		return true;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if (!parameters.getActivities().equals(list.getSelectedValuesList())) {
			parameters.setActivities(list.getSelectedValuesList());
			parameters.setMatrix(null);
		}
	}
}
