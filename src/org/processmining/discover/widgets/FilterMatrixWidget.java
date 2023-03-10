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

import org.deckfour.xes.classification.XEventClassifier;
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

			if (col > 0) {

				double s = 0.0;
				try {
					int d = Integer.valueOf((String) table.getModel().getValueAt(row, col));
					matrix.set(row, col - 1, d);
					s = ((double) d) / maxValue;
				} catch (NumberFormatException e) {
					// Treat any non-number as zero.
					matrix.set(row, col - 1, 0);
				}

				if (s > 1.0) {
					s = 1.0;
				} else if (s < -1.0) {
					s = -1.0;
				}
				comp.setBackground(getColorValue(s));
				if (s > 0.0) {
					comp.setForeground(Color.WHITE);
				} else {
					comp.setForeground(Color.BLACK);
				}
			} else {
				comp.setBackground(Color.WHITE);
			}

			return (comp);
		}

		private Color getColorValue(double d) {
			/* int color */

			int r = 255, g = 255, b = 255;
			if (d > 0.0) {
				g = (int) ((1.0 - d) * 127.0);
				r = g;
			}
			if (d < 0.0) {
				g = (int) ((d + 1.0) * 127.0);
				b = g;
			}

			return new Color(r, g, b);
		}
	}

	public FilterMatrixWidget(XLog eventLog, DiscoverPetriNetParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30, 30 } };
		setLayout(new TableLayout(size));
		//		if (parameters.getMatrix() == null) {
		XEventClassifier classifier = parameters.getClassifier();
		ActivityAlphabet alphabet = parameters.getAlphabet();
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);
		matrix = new ActivityMatrix(log, alphabet);
		parameters.setMatrix(matrix);
		//		}
		filter(parameters);
		addMatrixWidget(parameters);

		final NiceSlider absSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Absolute threshold (0 if no noise)", 0, 20, parameters.getAbsoluteThreshold(), Orientation.HORIZONTAL);
		absSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				//				System.out.println("[FilterMatrixWidget] start change");
				int value = absSlider.getSlider().getValue();
				parameters.setAbsoluteThreshold(value);
				filter(parameters);
				addMatrixWidget(parameters);
				//				System.out.println("[FilterMatrixWidget] end change");
			}
		});
		absSlider.setPreferredSize(new Dimension(100, 30));
		add(absSlider, "0, 1");

		// Slider for the relative threshold. Ranges from 0 to 99 (percent).
		final NiceSlider relSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Relative threshold (0 if no noise)", 0, 99, parameters.getRelativeThreshold(), Orientation.HORIZONTAL);
		relSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = relSlider.getSlider().getValue();
				parameters.setRelativeThreshold(value);
				filter(parameters);
				addMatrixWidget(parameters);
			}
		});
		relSlider.setPreferredSize(new Dimension(100, 30));
		add(relSlider, "0, 2");

		final NiceSlider safSlider = SlickerFactory.instance().createNiceIntegerSlider("Safety threshold", 0, 99,
				parameters.getSafetyThreshold(), Orientation.HORIZONTAL);
		safSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = safSlider.getSlider().getValue();
				parameters.setSafetyThreshold(value);
				filter(parameters);
				addMatrixWidget(parameters);
			}
		});
		safSlider.setPreferredSize(new Dimension(100, 30));
		add(safSlider, "0, 3");
	}

	private void filter(DiscoverPetriNetParameters parameters) {
		parameters.getMatrix().restore();
		parameters.getMatrix().filterAbsolute(parameters.getAbsoluteThreshold());
		parameters.getMatrix().filterRelative(parameters.getRelativeThreshold(), parameters.getSafetyThreshold());
	}

	private void addMatrixWidget(DiscoverPetriNetParameters parameters) {
		if (matrixComponent != null) {
			//			System.out.println("[FilterMatrixWdiget] removing matrix component");
			remove(matrixComponent);
			//			System.out.println("[FilterMatrixWdiget] removed matrix component");
		}
		final JTable table = new JTable();
		ActivityAlphabet alphabet = parameters.getAlphabet();
		matrix = parameters.getMatrix();
		int n = alphabet.size();
		String[] columnNames = new String[n + 1];
		String[][] rows = new String[n][n + 1];
		for (int r = 0; r < n; r++) {
			rows[r][0] = alphabet.get(r);

			for (int c = 0; c < n; c++) {
				if (r == 0) {
					columnNames[c + 1] = alphabet.get(c);
				}

				int v = matrix.get(r, c);
				maxValue = Math.max(Math.abs(v), maxValue);
				rows[r][c + 1] = String.valueOf(v);
			}
		}
		columnNames[0] = "Matrix";

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
		add(matrixComponent, "0, 0");
		//		System.out.println("[FilterMatrixWdiget] added matrix component");
		revalidate();
		repaint();
	}
}
