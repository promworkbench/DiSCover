package org.processmining.discover.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.discover.parameters.DiscoverPetriNetParameters;

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


		final JLabel providersLabel = new JLabel("Select conversion settings");
		providersLabel.setOpaque(false);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		add(providersLabel, "0, 0");

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
		add(mergeBox, "0, 1");
		
		// Check box for reduce
		final JCheckBox reduceBox = SlickerFactory.instance().createCheckBox("Reduce Petri net",
				parameters.isReduce());
		reduceBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduce(reduceBox.isSelected());
			}

		});
		reduceBox.setOpaque(false);
		add(reduceBox, "0, 2");
		
		// Check box for reduce
		final JCheckBox reduceSilentBox = SlickerFactory.instance().createCheckBox("Reduce simple silent transitions (precision may suffer)",
				parameters.isReduceSilent());
		reduceSilentBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setReduceSilent(reduceSilentBox.isSelected());
			}

		});
		reduceSilentBox.setOpaque(false);
		add(reduceSilentBox, "0, 3");
		
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
