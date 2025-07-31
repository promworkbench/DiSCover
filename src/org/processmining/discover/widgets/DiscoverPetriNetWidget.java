package org.processmining.discover.widgets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.discover.parameters.DiscoverPetriNetParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DiscoverPetriNetWidget extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7870247562755823620L;

	/**
	 * Creates a widget that can be used by the user to change the given parameter settings.
	 * 
	 * @param parameters The given parameter settings
	 */
	public DiscoverPetriNetWidget(final DiscoverPetriNetParameters parameters) {
		double size[][] = { { 30, TableLayoutConstants.FILL }, { 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));


		final JLabel providersLabel = new JLabel("Select conversion settings");
		providersLabel.setOpaque(false);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		add(providersLabel, "0, 0, 1, 0");

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
//		final NiceSlider scomSlider = SlickerFactory.instance().createNiceIntegerSlider(
//				"Limit on number of components (0 if no limit)", 0, 99, parameters.getNofSComponents(), Orientation.HORIZONTAL);
//		scomSlider.addChangeListener(new ChangeListener() {
//
//			public void stateChanged(ChangeEvent e) {
//				int value = scomSlider.getSlider().getValue();
//				parameters.setNofSComponents(value);
//			}
//		});
//		scomSlider.setPreferredSize(new Dimension(100, 30));
//		add(scomSlider, "0, 0");

		// Check box for merge
		final JCheckBox mergeBox = SlickerFactory.instance().createCheckBox("Merge activities",
				parameters.isMerge());
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setMerge(mergeBox.isSelected());
			}

		});
		mergeBox.setOpaque(false);
		add(mergeBox, "0, 1, 1, 1");
		
		// Check box for reduce
		final JCheckBox reduceBox = SlickerFactory.instance().createCheckBox("Reduce Petri net (maximal precise)",
				parameters.isReduce());
		// Check box for reduce
		final JCheckBox reduceAllBox = SlickerFactory.instance().createCheckBox("Additionally reduce all simple silent transitions (least precise)",
				parameters.isReduceAll());
		// Check box for reduce
		final JCheckBox reduceRestrictedBox = SlickerFactory.instance().createCheckBox("Additionally reduce clustered simple silent transitions (less precise)",
				parameters.isReduceRestricted());
		
		reduceBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduce(reduceBox.isSelected());
				reduceAllBox.setSelected(false);
				reduceAllBox.setVisible(reduceBox.isSelected());
				reduceRestrictedBox.setSelected(false);
				reduceRestrictedBox.setVisible(reduceBox.isSelected());
			}

		});
		reduceBox.setOpaque(false);
		add(reduceBox, "0, 2, 1, 2");

		reduceAllBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduceAll(reduceAllBox.isSelected());
				parameters.setReduceRestricted(false);
				if (reduceRestrictedBox.isSelected()) {
					reduceRestrictedBox.setSelected(false);
				}
			}

		});
		reduceAllBox.setOpaque(false);
		reduceAllBox.setVisible(reduceBox.isSelected());
		add(reduceAllBox, "1, 4");
		
		reduceRestrictedBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduceRestricted(reduceRestrictedBox.isSelected());
				parameters.setReduceAll(false);
				if (reduceAllBox.isSelected()) {
					reduceAllBox.setSelected(false);
				}
			}

		});
		reduceRestrictedBox.setOpaque(false);
		reduceRestrictedBox.setVisible(reduceBox.isSelected());
		add(reduceRestrictedBox, "1, 3");

		final JCheckBox addOccurrencePlacesBox = SlickerFactory.instance().createCheckBox("Add valid occurrence places",
				parameters.isAddOccurrencePlaces());
		addOccurrencePlacesBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setAddOccurrencePlaces(addOccurrencePlacesBox.isSelected());
			}

		});
		addOccurrencePlacesBox.setOpaque(false);
		add(addOccurrencePlacesBox, "0, 5, 1, 5");

		// Slider for the number of traces to play out.
		final NiceSlider nofTracesSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Number of traces per thread to play out", 0, 1000, parameters.getNofTraces(), Orientation.HORIZONTAL);
		nofTracesSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = nofTracesSlider.getSlider().getValue();
				parameters.setNofTraces(value);
			}
		});
		nofTracesSlider.setPreferredSize(new Dimension(100, 30));
		nofTracesSlider.setVisible(parameters.isAddEquivalencePlaces());
		add(nofTracesSlider, "1, 8");

		// Slider for the maximal length of a trace during play out.
		final NiceSlider maxTraceLengthSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Maximal length of trace during play out", 0, 1000, parameters.getMaxTraceLength(), Orientation.HORIZONTAL);
		maxTraceLengthSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = maxTraceLengthSlider.getSlider().getValue();
				parameters.setMaxTraceLength(value);
			}
		});
		maxTraceLengthSlider.setPreferredSize(new Dimension(100, 30));
		maxTraceLengthSlider.setVisible(parameters.isAddEquivalencePlaces());
		add(maxTraceLengthSlider, "1, 9");

		// Slider for the maximal length of a trace during play out.
		final NiceSlider nofThreadsSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Number of threads to use for play out", 0, 100, parameters.getNofThreads(), Orientation.HORIZONTAL);
		nofThreadsSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = nofThreadsSlider.getSlider().getValue();
				parameters.setNofThreads(value);
			}
		});
		nofThreadsSlider.setPreferredSize(new Dimension(100, 30));
		nofThreadsSlider.setVisible(parameters.isAddEquivalencePlaces());
		add(nofThreadsSlider, "1, 7");

		final JCheckBox addBinaryPlacesBox = SlickerFactory.instance().createCheckBox("Add valid equivalence places",
				parameters.isAddEquivalencePlaces());
		addBinaryPlacesBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				nofTracesSlider.setVisible(addBinaryPlacesBox.isSelected());
				maxTraceLengthSlider.setVisible(addBinaryPlacesBox.isSelected());
				nofThreadsSlider.setVisible(addBinaryPlacesBox.isSelected());
				parameters.setAddEquivalencePlaces(addBinaryPlacesBox.isSelected());
			}

		});
		addBinaryPlacesBox.setOpaque(false);
		add(addBinaryPlacesBox, "0, 6, 1, 6");


		final NiceSlider nofRoutingTransitionsSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Target number of routing transitions", 1, 1000, Math.abs(parameters.getMaxNofRoutingTransitions()), Orientation.HORIZONTAL);
		nofRoutingTransitionsSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = nofRoutingTransitionsSlider.getSlider().getValue();
				parameters.setMaxNofRoutingTransitions(value);
			}
		});
		nofRoutingTransitionsSlider.setPreferredSize(new Dimension(100, 30));
		nofRoutingTransitionsSlider.setVisible(parameters.getMaxNofRoutingTransitions() > 0);
		add(nofRoutingTransitionsSlider, "1, 11");

		final JCheckBox nofRoutingTransitionsBox = SlickerFactory.instance().createCheckBox("Reduce routing transitions",
				parameters.getMaxNofRoutingTransitions() > 0);
		nofRoutingTransitionsBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				nofRoutingTransitionsSlider.setVisible(nofRoutingTransitionsBox.isSelected());
				parameters.setMaxNofRoutingTransitions(-parameters.getMaxNofRoutingTransitions());
			}

		});
		nofRoutingTransitionsBox.setOpaque(false);
		add(nofRoutingTransitionsBox, "0, 10, 1, 10");

		// Check box for majority
//		final JCheckBox majorityBox = SlickerFactory.instance().createCheckBox("Use veto for noise",
//				parameters.isVetoNoise());
//		majorityBox.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				parameters.setVetoNoise(majorityBox.isSelected());
//			}
//
//		});
//		majorityBox.setOpaque(false);
//		add(majorityBox, "0, 6");
		
		revalidate();
		repaint();
	}

}
