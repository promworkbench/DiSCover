package org.processmining.discover.widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityMatrix;

public class ActivityMatrixWidget {

	int maxValue = 0;
	
	private class LocalRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8334837478919515692L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			if (col > 0) {

				int i = Integer.valueOf((String) table.getModel().getValueAt(row, col));
				double s = i / maxValue;

				comp.setBackground(getColorValue(s));
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

	public JComponent apply(ActivityMatrix matrix, ActivityAlphabet alphabet) {
		final JTable table = new JTable();
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
		table.setEnabled(false);
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		scrollPane.setOpaque(false);
		return scrollPane;

	}
}
