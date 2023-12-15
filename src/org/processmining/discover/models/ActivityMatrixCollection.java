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

	private ActivityAlphabet alphabet;

	/**
	 * Number of matrices.
	 */
	private int size;

	/**
	 * Discovers the matrices for the given activity log using the given alphabet
	 * and the given sets of activities to ignore. For every set of activities to
	 * ignore, a matrix will be discovered.
	 * 
	 * @param log        The activity log
	 * @param alphabet   The given alphabet
	 * @param ignoreSets THe given activity sets to ignore
	 * @param rootMatrix The matrix discovered earlier or the entire log
	 */
	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets ignoreSets,
			ActivityMatrix rootMatrix) {
		this(log, alphabet, ignoreSets, rootMatrix, new DiscoverPetriNetParameters());
	}

	public ActivityMatrixCollection(ActivityLog log, ActivityAlphabet alphabet, ActivitySets ignoreSets,
			ActivityMatrix rootMatrix, DiscoverPetriNetParameters parameters) {
		this.size = ignoreSets.size();
		this.alphabet = new ActivityAlphabet(alphabet);
		this.matrices = new ActivityMatrix[size];
		for (int idx = 0; idx < size; idx++) {
			this.matrices[idx] = new ActivityMatrix(log, alphabet, ignoreSets.get(idx), rootMatrix);
		}
//		if (parameters.getNofSComponents() > 0) {
		reduce(parameters);
//		}
	}

	public ActivityMatrixCollection(ActivityMatrixCollection matrices) {
		this.size = matrices.size;
		this.alphabet = new ActivityAlphabet(matrices.alphabet);
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
			if (!alphabet.equals(matrices.alphabet)) {
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
	 * @param idx The given index
	 * @return The matrix at the given index
	 */
	public ActivityMatrix get(int idx) {
		return matrices[idx];
	}

	/**
	 * Returns a JComponent(using Dot) containing the directly-follows graphs of all
	 * matrices side-by-side. The artificial start and end activities will be shared
	 * by all graphs.
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
	 * @param alphabet The alphabet of activities.
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

		if (parameters.isUseILP2()) {
			LPEngine engine = LPEngineFactory.createLPEngine(EngineType.LPSOLVE, 0, 0);
			Map<Integer, Double> objective = new HashMap<Integer, Double>();
			int variables[] = new int[matrices.length];
			for (int i = 0; i < matrices.length; i++) {
				variables[i] = engine.addVariable(new HashMap<Integer, Double>(), LPEngine.VariableType.INTEGER);
				objective.put(variables[i], 1.0);
			}
			engine.setObjective(objective, ObjectiveTargetType.MIN);

			for (int a = 0; a < parameters.getAlphabet().size(); a++) {
				Map<Integer, Double> constraint = new HashMap<Integer, Double>();
				for (int i = 0; i < matrices.length; i++) {
					constraint.put(variables[i], matrices[i].get(a) > 0 ? 1.0 : 0.0);
				}
				engine.addConstraint(constraint, Operator.GREATER_EQUAL, 1.0);
			}
			Map<Integer, Double> solution = engine.solve();
			for (int i = 0; i < matrices.length; i++) {
				if (solution.containsKey(variables[i]) && solution.get(variables[i]) > 0.0) {
					selected.add(matrices[i]);
				}
			}
			if (!selected.isEmpty()) {
				matrices = new ActivityMatrix[selected.size()];
				int idx = 0;
				for (ActivityMatrix matrix : selected) {
					matrices[idx++] = matrix;
				}
				System.out.println(
						"[ActivityMatrixCollection] Reduced from " + size + " to " + selected.size() + " matrices.");
				size = selected.size();
			}
		}

		if (parameters.isUseILP()) {
			Set<ActivitySet> nextActivities = new HashSet<ActivitySet>();
			Set<ActivitySet> previousActivities = new HashSet<ActivitySet>();
			for (ActivityMatrix matrix : matrices) {
				nextActivities.addAll(matrix.getNextActivities().values());
				previousActivities.addAll(matrix.getPreviousActivities().values());
			}
			// System.out.println("[ActivityMatrixCollection] Reduced from " + size + " to "
			// + selected.size() + " matrices.");
			// size = selected.size();
			// matrices = new ActivityMatrix[size];
			// int idx = 0;
			// for (ActivityMatrix matrix : selected) {
			// matrices[idx++] = matrix;
			// }

			/*
			 * Create an ILP to get a minimal set of matrices that cover all next and
			 * previous sets.
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

			if (!selected.isEmpty()) {
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
		}

		/*
		 * Limit the number of matrices to the provided limit.
		 */
		int limit = parameters.getNofSComponents();
		if (limit > 0) {
			selected.clear();
			if (limit > 0 && size > 0) {
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

	/*
	 * Filter the matrices on the given (absolute) threshold.
	 */
	public void filterAbsolute(int threshold) {
		/*
		 * First, restore all matrices, as the provided threshold may be lower than the
		 * one used to create these matrices.
		 */
		for (int i = 0; i < size; i++) {
			matrices[i].restore();
		}
		/*
		 * Second, apply the threshold on the restored matrices.
		 */
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				/*
				 * Initially, every cell needs to be changed.
				 */
				boolean change = true;
				for (int i = 0; i < size; i++) {
					if (matrices[i].get(fromIdx, toIdx) > 0 && matrices[i].get(fromIdx, toIdx) > threshold) {
						/*
						 * Found a matrix where the value of this cell exceeds the threshold. This cell
						 * should not be changed.
						 */
						change = false;
					}
				}
				if (change) {
					/*
					 * All matrices have a value for this cell below (or equal to) the provided
					 * threshold. Filter these cells out as being noise.
					 */
					for (int i = 0; i < size; i++) {
						matrices[i].set(fromIdx, toIdx, -Math.abs(matrices[i].get(fromIdx, toIdx)));
					}
				}
			}
		}
	}

	public void filterRelative(int relativeThreshold, int safetyThreshold) {
		if (relativeThreshold == 0) {
			return;
		}

		// Arrays to hold the maximal values for any row (from) and any column (to).
		int fromMax[][] = new int[matrices.length][alphabet.size()];
		int toMax[][] = new int[matrices.length][alphabet.size()];

		// Initialize these arrays.
		for (int m = 0; m < matrices.length; m++) {
			for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
				for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
					fromMax[m][fromIdx] = Math.max(fromMax[m][fromIdx], Math.abs(matrices[m].get(fromIdx, toIdx)));
					toMax[m][toIdx] = Math.max(toMax[m][toIdx], Math.abs(matrices[m].get(fromIdx, toIdx)));
				}
			}
		}

		boolean change[][] = new boolean[alphabet.size()][alphabet.size()];
		for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
			for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
				change[fromIdx][toIdx] = true;
			}
		}

		for (int m = 0; m < matrices.length; m++) {
			for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
				if (matrices[m].get(fromIdx) == 0) {
					continue;
				}
				for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
					if (matrices[m].get(toIdx) == 0) {
						continue;
					}
					if (!change[fromIdx][toIdx]) {
						continue;
					}
					if (100 * matrices[m].get(fromIdx, toIdx) >= safetyThreshold
							* Math.min(fromMax[m][fromIdx], toMax[m][toIdx])) {
						change[fromIdx][toIdx] = false;
					}
					if (matrices[m].get(fromIdx, toIdx) * 100 > Math.max(fromMax[m][fromIdx], toMax[m][toIdx])
							* relativeThreshold) {
						change[fromIdx][toIdx] = false;
					}
				}
			}
		}

		for (int m = 0; m < matrices.length; m++) {
			for (int fromIdx = 0; fromIdx < alphabet.size(); fromIdx++) {
				for (int toIdx = 0; toIdx < alphabet.size(); toIdx++) {
					if (change[fromIdx][toIdx]) {
						matrices[m].set(fromIdx, toIdx, -Math.abs(matrices[m].get(fromIdx, toIdx)));
					}
				}
			}
		}

	}

}
