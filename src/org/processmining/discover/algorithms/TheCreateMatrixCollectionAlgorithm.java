package org.processmining.discover.algorithms;

import java.util.Set;

import org.processmining.discover.models.TheActivitySets;
import org.processmining.discover.models.TheLog;
import org.processmining.discover.models.TheMatrix;
import org.processmining.discover.models.TheMatrixCollection;

public class TheCreateMatrixCollectionAlgorithm {

	public static TheMatrixCollection apply(TheLog log, TheMatrix matrix, TheActivitySets sets) {
		TheMatrixCollection matrices = new TheMatrixCollection(matrix);
		for (Set<Integer> set : sets.getSets()) {
			matrices.add(new TheMatrix(log, set));
		}
		return matrices;
	}
}
