package org.processmining.discover.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityMatrix;
import org.processmining.discover.models.ActivityMatrixCollection;
import org.processmining.discover.models.ActivitySets;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class FilterMatrixCollectionWidget extends JPanel {

	JComponent matrixPanel = null;
	int selectedMatrix = 0;

	private int maxValue = 0;

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
				if (d > 0) {
					s = ((double) d) / maxValue;
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

	public FilterMatrixCollectionWidget(DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		add(getMainComponent(parameters), "0, 0");
		
		
	}

	private JPanel getMainComponent(DiscoverPetriNetParameters parameters) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double size[][] = { { TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL, 30 } };
		panel.setLayout(new TableLayout(size));

		final JLabel providersLabel = new JLabel("Filter component matrices");
		providersLabel.setOpaque(false);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		panel.add(providersLabel, "0, 0");

		parameters.setMatrixCollection(new ActivityMatrixCollection(parameters.getLog(), parameters.getAlphabet(),
				new ActivitySets(parameters.getActivitySets()), parameters.getMatrix(), parameters));
		selectedMatrix = 0;
		addMatrixWidget(panel, parameters);

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
		final NiceSlider scomSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Select component", 0, parameters.getMatrixCollection().size() - 1, selectedMatrix, Orientation.HORIZONTAL);
		scomSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				selectedMatrix = scomSlider.getSlider().getValue();
				if (matrixPanel != null) {
					remove(matrixPanel);
				}
				addMatrixWidget(panel, parameters);
			}
		});
		scomSlider.setPreferredSize(new Dimension(100, 30));
		panel.add(scomSlider, "0, 2");

		return panel;
	}

	private void addMatrixWidget(JPanel panel, DiscoverPetriNetParameters parameters) {
		if (matrixPanel != null) {
			//			System.out.println("[FilterMatrixWdiget] removing matrix component");
			panel.remove(matrixPanel);
			//			System.out.println("[FilterMatrixWdiget] removed matrix component");
		}
		final JTable table = new JTable();
		ActivityAlphabet alphabet = parameters.getAlphabet();
		matrix = parameters.getMatrixCollection().get(selectedMatrix);
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
		matrixPanel = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		matrixPanel.setOpaque(false);
		matrixPanel.setPreferredSize(new Dimension(100, 100));
		//		System.out.println("[FilterMatrixWdiget] adding matrix component");
		panel.add(matrixPanel, "0, 1");
		//		System.out.println("[FilterMatrixWdiget] added matrix component");
		panel.validate();
		panel.repaint();
	}

}
