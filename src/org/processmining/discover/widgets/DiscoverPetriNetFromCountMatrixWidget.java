package org.processmining.discover.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.discover.models.CountMatrix;
import org.processmining.discover.parameters.DiscoverPetriNetFromCountMatrixParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DiscoverPetriNetFromCountMatrixWidget extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7870247562755823620L;

	public DiscoverPetriNetFromCountMatrixWidget(CountMatrix matrix,
			final DiscoverPetriNetFromCountMatrixParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { 30, 30, 30, TableLayoutConstants.FILL } };
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

		List<Integer> thresholds = new ArrayList<Integer>();
		for (int m = matrix.getMaxRelThreshold(); m > 0; m /= 2) {
			thresholds.add(m);
		}
		thresholds.add(0);
		final NiceSlider relSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Noise level (0 means no noise)", 0, thresholds.size() - 1, 0, Orientation.HORIZONTAL);
		relSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = relSlider.getSlider().getValue();
				parameters.setRelativeThreshold(thresholds.get(value));
				System.out.println("[DiscoverPetriNetFromCountMatrixWidget] Noise threshold set to " + thresholds.get(value));
			}
		});
		relSlider.setPreferredSize(new Dimension(100, 30));
		add(relSlider, "0, 1");

		final JCheckBox mergeBox = SlickerFactory.instance().createCheckBox("Merge transitions systems",
				parameters.isMerge());
		mergeBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setMerge(mergeBox.isSelected());
			}

		});
		mergeBox.setOpaque(false);
		add(mergeBox, "0, 2");
		
		revalidate();
		repaint();
	}

}
