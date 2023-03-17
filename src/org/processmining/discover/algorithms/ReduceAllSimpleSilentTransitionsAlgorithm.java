package org.processmining.discover.algorithms;

import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ReduceAllSimpleSilentTransitionsAlgorithm extends ReduceAbstractSimpleSilentTransitionsAlgorithm {

	boolean isOK(Transition transition, Map<PetrinetNode, Set<PetrinetNode>> preset,
			Map<PetrinetNode, Set<PetrinetNode>> postset) {
		return true;
	}
}
