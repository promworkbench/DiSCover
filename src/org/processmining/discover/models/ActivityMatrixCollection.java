package org.processmining.discover.models;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.lpengines.factories.LPEngineFactory;
import org.processmining.lpengines.interfaces.LPEngine;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;
import org.processmining.lpengines.interfaces.LPEngine.ObjectiveTargetType;
import org.processmining.lpengines.interfaces.LPEngine.Operator;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class ActivityMatrixCollection {

	/**
	 * Array containing all matrices.
	 */
	private ActivityMatrix matrices[];

	/**
	 * Number of matrices.
	 */
	private int size;

	/**
	 * Discovers the matrices for the given activity log using the given
	 * alphabet and the given sets of activities to ignore. For every set of
	 * activities to ignore, a matrix will be discovered.
	 * 
	 * @param log
	 *            The activity log
	 * @param alphabet
	 *            The given alphabet
	 * @param ignoreSets
	 *            THe given activity sets to ignore
	 * @param rootMatrix
	 *            The matrix discovered earlier or the entire log
	 */
	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets ignoreSets,
			ActivityMatrix rootMatrix) {
		this(log, alphabet, ignoreSets, rootMatrix, new DiscoverPetriNetParameters());
	}

	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets ignoreSets,
			ActivityMatrix rootMatrix, DiscoverPetriNetParameters parameters) {
		size = ignoreSets.size();
		matrices = new ActivityMatrix[size];
		for (int idx = 0; idx < size; idx++) {
			matrices[idx] = new ActivityMatrix(log, alphabet, ignoreSets.get(idx), rootMatrix);
		}
		if (parameters.getNofSComponents() > 0) {
			reduce(parameters);
		}
	}

	public ActivityMatrixCollection(ActivityMatrixCollection matrices) {
		size = matrices.size;
		this.matrices = new ActivityMatrix[size];
		for (int i = 0; i < size; i++) {
			this.matrices[i] = new ActivityMatrix(matrices.get(i));
		}
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof ActivityMatrixCollection) {
			ActivityMatrixCollection matrices = (ActivityMatrixCollection) o;
			if (size != matrices.size) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (!this.matrices[i].equals(matrices.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Return the number of matrices.
	 * 
	 * @return The number of matrices
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the matrix at the given index.
	 * 
	 * @param idx
	 *            The given index
	 * @return The matrix at the given index
	 */
	public ActivityMatrix get(int idx) {
		return matrices[idx];
	}

	/**
	 * Returns a JComponent(using Dot) containing the directly-follows graphs of
	 * all matrices side-by-side. The artificial start and end activities will
	 * be shared by all graphs.
	 * 
	 * @return The JComponent containing all directly-follows graphs.
	 */
	public JPanel getComponent() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double size[][] = { { TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL } };
		panel.setLayout(new TableLayout(size));

		final JLabel providersLabel = new JLabel("Check combined component graphs");
		providersLabel.setOpaque(false);
		providersLabel.setFont(providersLabel.getFont().deriveFont(13f));
		providersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		providersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		providersLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		panel.add(providersLabel, "0, 0");
		Dot dotGraph = new Dot();

		// Add the nodes for the artificial start and end activities.
		DotNode startNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.START, matrices[0].get(0)));
		startNode.setOption("shape", "none");
		DotNode endNode = dotGraph.addNode(matrices[0].getNodeLabel(ActivityAlphabet.END, matrices[0].get(0)));
		endNode.setOption("shape", "none");

		// Add all other nodes and edges.
		for (ActivityMatrix matrix : matrices) {
			matrix.addNodesAndEdges(dotGraph, startNode, endNode);
		}

		panel.add(new DotPanel(dotGraph), "0, 1");
		return panel;
	}

	/**
	 * Have all matrices veto any noise. If any matrix says it is not noise, the
	 * matrices will follow this.
	 * 
	 * @param alphabet
	 *            The alphabet of activities.
	 */
	public void vetoNoise(ActivityAlphabet alphabet) {
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				int notNoise = 0;
				for (ActivityMatrix matrix : matrices) {
					if (matrix.get(fromIdx, toIdx) > 0) {
						notNoise++;
					}
				}
				if (notNoise > 0) {
					// One says not noise, have all agree.
					for (ActivityMatrix matrix : matrices) {
						matrix.set(fromIdx, toIdx);
					}
				}
			}
		}
	}

	private void reduce(DiscoverPetriNetParameters parameters) {
		Set<ActivityMatrix> selected = new HashSet<ActivityMatrix>();

		if (parameters.isUseILP()) {
			Set<ActivitySet> nextActivities = new HashSet<ActivitySet>();
			Set<ActivitySet> previousActivities = new HashSet<ActivitySet>();
			for (ActivityMatrix matrix : matrices) {
				nextActivities.addAll(matrix.getNextActivities().values());
				previousActivities.addAll(matrix.getPreviousActivities().values());
			}
			//		System.out.println("[ActivityMatrixCollection] Reduced from " + size + " to " + selected.size() + " matrices.");
			//		size = selected.size();
			//		matrices = new ActivityMatrix[size];
			//		int idx = 0;
			//		for (ActivityMatrix matrix : selected) {
			//			matrices[idx++] = matrix;
			//		}

			/*
			 * Create an ILP to get a minimal set of matrices that cover all
			 * next and previous sets.
			 */
			LPEngine engine = LPEngineFactory.createLPEngine(EngineType.LPSOLVE, 0, 0);
			Map<Integer, Double> objective = new HashMap<Integer, Double>();
			int variables[] = new int[matrices.length];
			for (int i = 0; i < matrices.length; i++) {
				variables[i] = engine.addVariable(new HashMap<Integer, Double>(), LPEngine.VariableType.INTEGER);
				objective.put(variables[i], 1.0);
			}
			engine.setObjective(objective, ObjectiveTargetType.MIN);

			for (ActivitySet set : nextActivities) {
				Map<Integer, Double> constraint = new HashMap<Integer, Double>();
				for (int i = 0; i < matrices.length; i++) {
					constraint.put(variables[i], matrices[i].getNextActivities().containsValue(set) ? 1.0 : 0.0);
				}
				engine.addConstraint(constraint, Operator.GREATER_EQUAL, 1.0);
			}

			for (ActivitySet set : previousActivities) {
				Map<Integer, Double> constraint = new HashMap<Integer, Double>();
				for (int i = 0; i < matrices.length; i++) {
					constraint.put(variables[i], matrices[i].getPreviousActivities().containsValue(set) ? 1.0 : 0.0);
				}
				engine.addConstraint(constraint, Operator.GREATER_EQUAL, 1.0);
			}
			// Solve the ILP
			Map<Integer, Double> solution = engine.solve();
			// Select the matrices for the minimal set.
			for (int i = 0; i < matrices.length; i++) {
				if (solution.containsKey(variables[i]) && solution.get(variables[i]) > 0.0) {
					selected.add(matrices[i]);
				}
			}
			// Set the matrices.
			matrices = new ActivityMatrix[selected.size()];
			int idx = 0;
			for (ActivityMatrix matrix : selected) {
				matrices[idx++] = matrix;
			}
			System.out.println(
					"[ActivityMatrixCollection] Reduced from " + size + " to " + selected.size() + " matrices.");
			size = selected.size();
		}

		/*
		 * Limit the number of matrices to the provided limit.
		 */
		selected.clear();
		int limit = parameters.getNofSComponents();
		if (limit > 0) {
			for (int i = 0; i < limit; i++) {
				selected.add(matrices[(i * size) / limit]);
			}
		}
		// Set the matrices.
		matrices = new ActivityMatrix[selected.size()];
		int idx = 0;
		for (ActivityMatrix matrix : selected) {
			matrices[idx++] = matrix;
		}
		System.out.println("[ActivityMatrixCollection] Limited to " + selected.size() + " matrices.");
		size = selected.size();
	}
}
