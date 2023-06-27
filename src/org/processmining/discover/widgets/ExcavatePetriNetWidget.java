package org.processmining.discover.widgets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.discover.parameters.ExcavatePetriNetParameters;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.dialogs.ClassifierPanel;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class ExcavatePetriNetWidget extends JPanel implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5844516981777798249L;

	private ExcavatePetriNetParameters parameters;
	private ProMList<Integer> absList = null;
	private ProMList<Integer> relList = null;
	
	public ExcavatePetriNetWidget(final ExcavatePetriNetParameters parameters) {
		this.parameters = parameters;
		double size[][] = { { 150, 150, 20, TableLayoutConstants.FILL }, { 30, 30, 30, 30, TableLayoutConstants.FILL, 30, 30, 30, 30 } };
		setLayout(new TableLayout(size));

		final JLabel label1 = new JLabel("Select relative weights for metrics");
		label1.setOpaque(false);
		label1.setFont(label1.getFont().deriveFont(13f));
		label1.setAlignmentX(Component.CENTER_ALIGNMENT);
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setHorizontalTextPosition(SwingConstants.CENTER);

		add(label1, "3, 0");

		final JLabel label2 = new JLabel("Select additional settings");
		label2.setOpaque(false);
		label2.setFont(label2.getFont().deriveFont(13f));
		label2.setAlignmentX(Component.CENTER_ALIGNMENT);
		label2.setHorizontalAlignment(SwingConstants.CENTER);
		label2.setHorizontalTextPosition(SwingConstants.CENTER);

		JPanel classifierPanel = new ClassifierPanel(parameters.getLog().getClassifiers(), parameters);
		add(classifierPanel, "3, 4");

		add(label2, "3, 5");
		
		final JLabel label3 = new JLabel("Select thresholds to try");
		label3.setOpaque(false);
		label3.setFont(label3.getFont().deriveFont(13f));
		label3.setAlignmentX(Component.CENTER_ALIGNMENT);
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		label3.setHorizontalTextPosition(SwingConstants.CENTER);

		add(label3, "0, 0, 1, 0");
		
		final JCheckBox wfBox = SlickerFactory.instance().createCheckBox("Prefer WF net",
				parameters.isPreferWFnet());
		wfBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setPreferWFnet(wfBox.isSelected());
			}

		});
		wfBox.setOpaque(false);
		add(wfBox, "3, 8");
		
		final NiceSlider fitSlider = SlickerFactory.instance().createNiceIntegerSlider("Fitness", 0, 100,
				(int) (100*parameters.getFitnessFactor()), Orientation.HORIZONTAL);
		fitSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = fitSlider.getSlider().getValue();
				parameters.setFitnessFactor(value / 100.0);
			}
		});
		fitSlider.setPreferredSize(new Dimension(100, 30));
		add(fitSlider, "3, 1");
		
		final NiceSlider precSlider = SlickerFactory.instance().createNiceIntegerSlider("Precision", 0, 100,
				(int) (100*parameters.getPrecisionFactor()), Orientation.HORIZONTAL);
		precSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = precSlider.getSlider().getValue();
				parameters.setPrecisionFactor(value / 100.0);
			}
		});
		precSlider.setPreferredSize(new Dimension(100, 30));
		add(precSlider, "3, 2");
		
		final NiceSlider simpSlider = SlickerFactory.instance().createNiceIntegerSlider("Simplicity", 0, 100,
				(int) (100*parameters.getSimplicityFactor()), Orientation.HORIZONTAL);
		simpSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = simpSlider.getSlider().getValue();
				parameters.setSimplicityFactor(value / 100.0);
				parameters.setSizeFactor(value / 100.0);
			}
		});
		simpSlider.setPreferredSize(new Dimension(100, 30));
		add(simpSlider, "3, 3");
		
		final NiceSlider threadSlider = SlickerFactory.instance().createNiceIntegerSlider("Number of threads to use for replay", 0, 64,
				parameters.getNofThreads(), Orientation.HORIZONTAL);
		threadSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = threadSlider.getSlider().getValue();
				parameters.setNofThreads(value);
			}
		});
		threadSlider.setPreferredSize(new Dimension(100, 30));
		add(threadSlider, "3, 6");
		
		final NiceSlider tranSlider = SlickerFactory.instance().createNiceIntegerSlider("Maximal number of transitions", 0, 100,
				parameters.getMaxNofTransitions(), Orientation.HORIZONTAL);
		tranSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = tranSlider.getSlider().getValue();
				parameters.setMaxNofTransitions(value);
			}
		});
		tranSlider.setPreferredSize(new Dimension(100, 30));
		add(tranSlider, "3, 7");
		
		DefaultListModel<Integer> absListModel = new DefaultListModel<Integer>();
		for (int i = 0; i < 21; i++) {
			absListModel.addElement(i);
		}
		int selectedIndices[] = new int[parameters.getAbsValues().size()];
		int j = 0;
		for (int i = 0; i < 21; i++) {
			if (parameters.getAbsValues().contains(i)) {
				selectedIndices[j++] = i;
			}
		}
		absList = new ProMList<Integer>("Log skeleton", absListModel);
		absList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (21 == j) {
			/*
			 * Faster way to select all items in the list.
			 */
			absList.getList().setSelectionInterval(0, 20);
		} else {
			/*
			 * Slower way to select some items in the list.
			 */
			absList.setSelectedIndices(selectedIndices);
		}
		absList.addListSelectionListener(this);

		parameters.setAbsValues(absList.getSelectedValuesList());
		
		absList.setPreferredSize(new Dimension(100,100));
		add(absList, "0, 1, 0, 8");

		DefaultListModel<Integer> relListModel = new DefaultListModel<Integer>();
		for (int i = 0; i < 100; i++) {
			relListModel.addElement(i);
		}
		selectedIndices = new int[parameters.getRelValues().size()];
		j = 0;
		for (int i = 0; i < 100; i++) {
			if (parameters.getRelValues().contains(i)) {
				selectedIndices[j++] = i;
			}
		}
		relList = new ProMList<Integer>("Relative", relListModel);
		relList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (100 == j) {
			/*
			 * Faster way to select all items in the list.
			 */
			relList.getList().setSelectionInterval(0, 99);
		} else {
			/*
			 * Slower way to select some items in the list.
			 */
			relList.setSelectedIndices(selectedIndices);
		}
		relList.addListSelectionListener(this);

		parameters.setRelValues(relList.getSelectedValuesList());
		
		relList.setPreferredSize(new Dimension(100,100));
		add(relList, "1, 1, 1, 8");

	}

	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if (!parameters.getAbsValues().equals(absList.getSelectedValuesList())) {
			parameters.setAbsValues(absList.getSelectedValuesList());
		}
		if (!parameters.getRelValues().equals(relList.getSelectedValuesList())) {
			parameters.setRelValues(relList.getSelectedValuesList());
		}
		
	}
}
