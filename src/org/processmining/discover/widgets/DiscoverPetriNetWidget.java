package org.processmining.discover.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
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

	public DiscoverPetriNetWidget(final DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { 30, 30, 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		final NiceSlider absSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Absolute threshold (0 if no noise)", 0, 20, parameters.getAbsoluteThreshold(), Orientation.HORIZONTAL);
		absSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = absSlider.getSlider().getValue();
				parameters.setAbsoluteThreshold(value);
			}
		});
		absSlider.setPreferredSize(new Dimension(100, 30));
		add(absSlider, "0, 0");

		final NiceSlider relSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Relative threshold (0 if no noise)", 0, 100, parameters.getRelativeThreshold(), Orientation.HORIZONTAL);
		relSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = relSlider.getSlider().getValue();
				parameters.setRelativeThreshold(value);
			}
		});
		relSlider.setPreferredSize(new Dimension(100, 30));
		add(relSlider, "0, 1");

		final JCheckBox mergeBox = SlickerFactory.instance().createCheckBox("Merge state machines on visible transitons",
				parameters.isMerge());
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setMerge(mergeBox.isSelected());
			}

		});
		mergeBox.setOpaque(false);
		add(mergeBox, "0, 2");
		
		final JCheckBox reduceBox = SlickerFactory.instance().createCheckBox("Reduce Petri net",
				parameters.isReduce());
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduce(reduceBox.isSelected());
			}

		});
		reduceBox.setOpaque(false);
		add(reduceBox, "0, 3");
		
		revalidate();
		repaint();
	}

}
