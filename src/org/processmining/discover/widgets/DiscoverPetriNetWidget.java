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

	/**
	 * Creates a widget that can be used by the user to change the given parameter settings.
	 * 
	 * @param parameters The given parameter settings
	 */
	public DiscoverPetriNetWidget(final DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { 30, 30, 30, 30, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		// Slider for the absolute threshold. Ranges from 0 to 20.
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

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
		final NiceSlider relSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Relative threshold (0 if no noise)", 0, 99, parameters.getRelativeThreshold(), Orientation.HORIZONTAL);
		relSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = relSlider.getSlider().getValue();
				parameters.setRelativeThreshold(value);
			}
		});
		relSlider.setPreferredSize(new Dimension(100, 30));
		add(relSlider, "0, 1");

		// Check box for merge
		final JCheckBox mergeBox = SlickerFactory.instance().createCheckBox("Merge activities",
				parameters.isMerge());
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setMerge(mergeBox.isSelected());
			}

		});
		mergeBox.setOpaque(false);
		add(mergeBox, "0, 2");
		
		// Check box for reduce
		final JCheckBox reduceBox = SlickerFactory.instance().createCheckBox("Reduce Petri net",
				parameters.isReduce());
		reduceBox.addActionListener(new ActionListener() {

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