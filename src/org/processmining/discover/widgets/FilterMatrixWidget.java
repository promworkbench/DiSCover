package org.processmining.discover.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.deckfour.xes.model.XLog;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityLog;
import org.processmining.discover.models.ActivityMatrix;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class FilterMatrixWidget extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1016124096702672631L;

	int maxValue = 0;

	JComponent mainPanel = null;
	JComponent matrixComponent = null;

	ActivityMatrix matrix = null;

	private class LocalRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8334837478919515692L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			double s = 0.0;
			try {
				int d = Integer.valueOf((String) table.getModel().getValueAt(row, col));
				if (d != matrix.get(row, col)) {
					matrix.set(row, col, d);
				}
				int d2 = Integer.valueOf((String) table.getModel().getValueAt(col, row));
				if (d > 0 && d2 > 0) {
					s = ((double) -Math.min(d, d2)) / maxValue;
				} else if (d > 0) {
					s = ((double) d) / maxValue;
				} else {
					s = 0.0;
				}
			} catch (NumberFormatException e) {
				// Treat any non-number as zero.
				matrix.set(row, col, 0);
			}

			if (s > 1.0) {
				s = 1.0;
			} else if (s < -1.0) {
				s = -1.0;
			}
			comp.setBackground(getColorValue(s, col == row));
			if (s > 0.1 || col == row) {
				comp.setForeground(Color.WHITE);
			} else {
				comp.setForeground(Color.BLACK);
			}

			return (comp);
		}

		private Color getColorValue(double d, boolean isDiagonal) {
			/* int color */

			int r = 255, g = 255, b = 255;
			if (isDiagonal) {
				r = 128;
				g = 128;
				b = 128;
			}
			if (d > 0.0) {
				g = (int) ((1.0 - d) * 191.0);
				r = g;
			}
			if (d < 0.0) {
				g = (int) ((d + 1.0) * 191.0);
				b = g;
			}

			return new Color(r, g, b);
		}
	}

	public FilterMatrixWidget(XLog eventLog, DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		mainPanel = getMainComponent(eventLog, parameters);
		add(mainPanel, "0, 0");

		//		SlickerButton resetButton = new SlickerButton("Reset");
		//		resetButton.addActionListener(new ActionListener() {
		//
		//			public void actionPerformed(ActionEvent arg0) {
		//				if (mainPanel != null) {
		//					remove(mainPanel);
		//				}
		//				mainPanel = getMainComponent(eventLog, parameters);
		//				add(mainPanel, "0, 0");
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

	private JPanel getMainComponent(XLog eventLog, DiscoverPetriNetParameters parameters) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30, 30 } };
		panel.setLayout(new TableLayout(size));
		//		if (parameters.getMatrix() == null) {
		parameters.setAlphabet(new ActivityAlphabet(parameters.getActivities()));
		parameters.setLog(new ActivityLog(eventLog, parameters.getClassifier(), parameters.getAlphabet()));
		parameters.setMatrix(new ActivityMatrix(parameters.getLog(), parameters.getAlphabet()));
		//		}
		filter(parameters);
		addMatrixWidget(panel, parameters);

		final NiceSlider absSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Absolute threshold (0 if no noise)", 0, 20, parameters.getAbsoluteThreshold(), Orientation.HORIZONTAL);
		absSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				//				System.out.println("[FilterMatrixWidget] start change");
				int value = absSlider.getSlider().getValue();
				parameters.setAbsoluteThreshold(value);
				filter(parameters);
				addMatrixWidget(panel, parameters);
				//				System.out.println("[FilterMatrixWidget] end change");
			}
		});
		absSlider.setPreferredSize(new Dimension(100, 30));
		panel.add(absSlider, "0, 1");

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
		final NiceSlider relSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Relative threshold (0 if no noise)", 0, 99, parameters.getRelativeThreshold(), Orientation.HORIZONTAL);
		relSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = relSlider.getSlider().getValue();
				parameters.setRelativeThreshold(value);
				filter(parameters);
				addMatrixWidget(panel, parameters);
			}
		});
		relSlider.setPreferredSize(new Dimension(100, 30));
		panel.add(relSlider, "0, 2");

		final NiceSlider safSlider = SlickerFactory.instance().createNiceIntegerSlider("Safety threshold", 0, 99,
				parameters.getSafetyThreshold(), Orientation.HORIZONTAL);
		safSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = safSlider.getSlider().getValue();
				parameters.setSafetyThreshold(value);
				filter(parameters);
				addMatrixWidget(panel, parameters);
			}
		});
		safSlider.setPreferredSize(new Dimension(100, 30));
		panel.add(safSlider, "0, 3");
		return panel;
	}

	private void filter(DiscoverPetriNetParameters parameters) {
		matrix = new ActivityMatrix(parameters.getMatrix());
		matrix.restore();
		matrix.filterAbsolute(parameters.getAbsoluteThreshold());
		matrix.filterRelative(parameters.getRelativeThreshold(), parameters.getSafetyThreshold());
		parameters.setMatrix(matrix);
	}

	private void addMatrixWidget(JPanel panel, DiscoverPetriNetParameters parameters) {
		if (matrixComponent != null) {
			//			System.out.println("[FilterMatrixWdiget] removing matrix component");
			panel.remove(matrixComponent);
			//			System.out.println("[FilterMatrixWdiget] removed matrix component");
		}
		final JTable table = new JTable();
		ActivityAlphabet alphabet = parameters.getAlphabet();
		matrix = parameters.getMatrix();
		int n = alphabet.size();
		String[] columnNames = new String[n];
		String[][] rows = new String[n][n];
		maxValue = 0;
		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				if (r == 0) {
					columnNames[c] = alphabet.get(c);
				}

				int v = matrix.get(r, c);
				maxValue = Math.max(Math.abs(v), maxValue);
				rows[r][c] = String.valueOf(v);
			}
		}

		TableModel model = new DefaultTableModel(rows, columnNames);
		table.setModel(model);
		table.setDefaultRenderer(Object.class, new LocalRenderer());
		table.setOpaque(false);
		table.setEnabled(true);

		//		System.out.println("[FilterMatrixWdiget] setting matrix component");
		matrixComponent = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		matrixComponent.setOpaque(false);
		matrixComponent.setPreferredSize(new Dimension(100, 100));
		//		System.out.println("[FilterMatrixWdiget] adding matrix component");
		panel.add(matrixComponent, "0, 0");
		//		System.out.println("[FilterMatrixWdiget] added matrix component");
		panel.validate();
		panel.repaint();
	}

}
