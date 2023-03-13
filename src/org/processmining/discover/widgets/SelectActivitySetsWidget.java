package org.processmining.discover.widgets;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.discover.models.ActivitySet;
import org.processmining.discover.models.ActivitySets;
import org.processmining.discover.models.ConcurrentActivityPairs;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.framework.util.ui.widgets.ProMList;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SelectActivitySetsWidget extends JPanel implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7042751826371507733L;
	
	private DiscoverPetriNetParameters parameters;
	private ProMList<ActivitySet> listPanel = null;

	public SelectActivitySetsWidget(DiscoverPetriNetParameters parameters) {
		this.parameters = parameters;
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (parameters.getAllActivitySets() == null) {
			ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(parameters.getMatrix(), parameters.getAlphabet());
			ActivitySets activitySets = new ActivitySets(pairs);
			parameters.setAllActivitySets(new ArrayList<ActivitySet>());
			parameters.setActivitySets(new ArrayList<ActivitySet>());
			for (int i = 0; i < activitySets.size(); i++) {
				parameters.getAllActivitySets().add(activitySets.get(i));
				parameters.getActivitySets().add(activitySets.get(i));
			}
		}
		
		listPanel = getMainComponent(parameters);
		add(listPanel, "0, 0");

	}

	private ProMList<ActivitySet> getMainComponent(DiscoverPetriNetParameters parameters) {
		ActivitySet.alphabet = parameters.getAlphabet();
		DefaultListModel<ActivitySet> listModel = new DefaultListModel<ActivitySet>();
		for (int i = 0; i < parameters.getAllActivitySets().size(); i++) {
			listModel.addElement(parameters.getAllActivitySets().get(i));
		}
		int selectedIndices[] = new int[parameters.getActivitySets().size()];
		int j = 0;
		for (int i = 0; i < parameters.getAllActivitySets().size(); i++) {
			if (parameters.getActivitySets().contains(listModel.get(i))) {
				selectedIndices[j++] = i;
			}
		}
		ProMList<ActivitySet> list = new ProMList<ActivitySet>("Select components by selecting groups of activities to ignore", listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (parameters.getAllActivitySets().size() == j) {
			/*
			 * Faster way to select all items in the list.
			 */
			list.getList().setSelectionInterval(0, parameters.getAllActivitySets().size() - 1);
		} else {
			/*
			 * Slower way to select some items in the list.
			 */
			list.setSelectedIndices(selectedIndices);
		}
		list.addListSelectionListener(this);

		parameters.setActivitySets(list.getSelectedValuesList());
		
		list.setPreferredSize(new Dimension(100,100));
		return list;
	}

	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		if (!parameters.getActivitySets().equals(listPanel.getSelectedValuesList())) {
			parameters.setActivitySets(listPanel.getSelectedValuesList());
		}
		
	}
	
}
