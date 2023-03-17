package org.processmining.discover.algorithms;

import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ReduceRestrictedSimpleSilentTransitionsAlgorithm extends ReduceAbstractSimpleSilentTransitionsAlgorithm {

	boolean isOK(Transition transition, Map<PetrinetNode, Set<PetrinetNode>> preset,
			Map<PetrinetNode, Set<PetrinetNode>> postset) {
		boolean ok = false;
		for (PetrinetNode siblingNode : postset.get(preset.get(transition).iterator().next())) {
			if (!siblingNode.equals(transition) && ((Transition) siblingNode).isInvisible()
					&& preset.get(siblingNode).size() == 1) {
				ok = true;
			}
		}
		if (!ok) {
			return false;
		}
		ok = false;
		for (PetrinetNode siblingNode : preset.get(postset.get(transition).iterator().next())) {
			if (!siblingNode.equals(transition) && ((Transition) siblingNode).isInvisible()
					&& postset.get(siblingNode).size() == 1) {
				ok = true;
			}
		}
		return ok;
	}
}
