package org.processmining.discover.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.discover.models.ActivitySet;
import org.processmining.discover.models.ActivitySets;
import org.processmining.discover.models.ConcurrentActivityPairs;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.framework.util.ui.widgets.ProMList;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

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
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30, 30, 30, 30 } };
		setLayout(new TableLayout(size));

		if (parameters.getAllActivitySets() == null) {
			ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(parameters.getMatrix(), parameters.getAlphabet(), parameters);
			ActivitySets activitySets = new ActivitySets(pairs, parameters.getAlphabet(), parameters.getMode());
			parameters.setAllActivitySets(new ActivitySets(activitySets));
			parameters.setActivitySets(new ActivitySets(activitySets));
		}
		
		listPanel = getMainComponent(parameters);
		add(listPanel, "0, 0");

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
		final NiceSlider scomSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Limit on number of components (0 if no limit)", 0, 99, parameters.getNofSComponents(), Orientation.HORIZONTAL);
		scomSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = scomSlider.getSlider().getValue();
				parameters.setNofSComponents(value);
			}
		});
		scomSlider.setPreferredSize(new Dimension(100, 30));
		add(scomSlider, "0, 5");

		final JCheckBox activityBestBox = SlickerFactory.instance().createCheckBox("Generate a largest component for every activity",
				parameters.getMode() == ActivitySets.MODE_ACT_BST);
		final JCheckBox activityFirstBox = SlickerFactory.instance().createCheckBox("Generate a component for every activity",
				parameters.getMode() == ActivitySets.MODE_ACT_FRST);

		activityFirstBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (activityFirstBox.isSelected()) {
					parameters.setMode(ActivitySets.MODE_ACT_FRST);
					activityBestBox.setSelected(false);
				} else if (!activityBestBox.isSelected()) {
					parameters.setMode(ActivitySets.MODE_ALL);
				}
			}

		});
		activityFirstBox.setOpaque(false);
		add(activityFirstBox, "0, 1");

		activityBestBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (activityBestBox.isSelected()) {
					parameters.setMode(ActivitySets.MODE_ACT_BST);
					activityFirstBox.setSelected(false);
				} else if (!activityFirstBox.isSelected()) {
					parameters.setMode(ActivitySets.MODE_ALL);
				}
			}
		});
		activityBestBox.setOpaque(false);
		add(activityBestBox, "0, 2");

		// Check box for merge
		final JCheckBox ilpBox = SlickerFactory.instance().createCheckBox("Select components that cover all activities",
				parameters.isUseILP2());
		final JCheckBox mergeBox = SlickerFactory.instance().createCheckBox("Select components that cover all activity sets",
				parameters.isUseILP());
		ilpBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseILP2(ilpBox.isSelected());
				parameters.setUseILP(false);
				if (ilpBox.isSelected()) {
					mergeBox.setSelected(false);
				}
			}

		});
		ilpBox.setOpaque(false);
		add(ilpBox, "0, 3");

		// Check box for merge
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseILP(mergeBox.isSelected());
				parameters.setUseILP2(false);
				if (mergeBox.isSelected()) {
					ilpBox.setSelected(false);
				}
			}

		});
		mergeBox.setOpaque(false);
		add(mergeBox, "0, 4");

	}

	private ProMList<ActivitySet> getMainComponent(DiscoverPetriNetParameters parameters) {
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
		ProMList<ActivitySet> list = new ProMList<ActivitySet>("Select components", listModel);
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
			parameters.setActivitySets(new ActivitySets(listPanel.getSelectedValuesList()));
		}
		
	}
	
}
