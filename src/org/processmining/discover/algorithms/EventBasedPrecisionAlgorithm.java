package org.processmining.discover.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.EventBasedPrecision;
import org.processmining.discover.parameters.EventBasedPrecisionParameters;

public class EventBasedPrecisionAlgorithm {

	/*
	 * Mapping from #hist(e) to enL(e)
	 */
	private Map<List<String>, Set<String>> enL;
	/*
	 * Mapping from #hist(e) to enM(e)
	 */
	private Map<List<String>, Set<String>> enM;
	/*
	 * The resulting precision.
	 */
	private EventBasedPrecision precision;

	public EventBasedPrecisionAlgorithm() {
		/*
		 * Initialize.
		 */
		enL = new HashMap<List<String>, Set<String>>();
		enM = new HashMap<List<String>, Set<String>>();
		precision = new EventBasedPrecision();
	}

	/*
	 * Get the event-based precision given the alignments and the net (apn).
	 * ssumption is that the net was used to create the alignments (the transitions
	 * in the alignments should be transitions from this net).
	 */
	public EventBasedPrecision apply(PluginContext context, PNRepResult alignments, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		/*
		 * First, construct enL en enM.
		 */
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment, apn, parameters);
		}
		/*
		 * Second, compute precision based on constructed enL en enM.
		 */
		for (SyncReplayResult alignment : alignments) {
			apply(context, alignment, parameters);
		}
		/*
		 * Return precision.
		 */
		return precision;
	}

	private void add(List<String> hist, Set<String> enabled) {
		if (!enM.containsKey(hist)) {
			enL.put(new ArrayList<String>(hist), new HashSet<String>());
			enM.put(new ArrayList<String>(hist), new HashSet<String>());
		}
		enM.get(hist).addAll(enabled);
	}

	/*
	 * Extend enL en enM with the given alignment.
	 */
	private void apply(PluginContext context, SyncReplayResult alignment, AcceptingPetriNet apn,
			EventBasedPrecisionParameters parameters) throws IllegalTransitionException {
		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			preset.get(edge.getTarget()).add(edge.getSource());
		}
		/*
		 * Current state in the net.
		 */
		Marking state = apn.getInitialMarking();
		/*
		 * Current history in alignment. Initially empty.
		 */
		List<String> hist = new ArrayList<String>();
		add(hist, getEnabled(apn, state, preset));

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			/*
			 * Get current step type an dnode instance.
			 */
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);

			switch (stepType) {
			case MREAL:
				// Fall-through
			case LMGOOD: {
				Transition transition = (Transition) nodeInstance;
				/*
				 * From the current history, the (repaired) trace can do the activity associated
				 * with this transition.
				 */
				enL.get(hist).add(transition.getLabel());
				/*
				 * Update state by executing transition.
				 */
				parameters.getSemantics().setCurrentState(state);
				parameters.getSemantics().executeExecutableTransition(transition);
				state = new Marking(parameters.getSemantics().getCurrentState());
				/*
				 * Update history.
				 */
				hist.add(transition.getLabel());
				add(hist, getEnabled(apn, state, preset));
				break;
			}
			case MINVI: {
				Transition transition = (Transition) nodeInstance;
				/*
				 * Update state by executing transition.
				 */
				parameters.getSemantics().setCurrentState(state);
				parameters.getSemantics().executeExecutableTransition(transition);
				state = new Marking(parameters.getSemantics().getCurrentState());
				break;
			}
			case L: {
				break;
			}
			default:
			}
		}
	}

	/*
	 * Get the enabled activities. Exploits the structure of the net. 
	 */
	private Set<String> getEnabled(AcceptingPetriNet apn, Marking marking,
			Map<PetrinetNode, Set<PetrinetNode>> preset) {
//		System.out.println("[DicoverPetriNetAlgorithm] Checking marking " + marking);		
		Set<String> enabled = new HashSet<String>();
		for (Transition transition : apn.getNet().getTransitions()) {
			if (transition.isInvisible()) {
				continue;
			}
//			System.out.println("[DicoverPetriNetAlgorithm] Checking transition " + transition.getLabel());		
			boolean allMarked = true;
			for (PetrinetNode node : preset.get(transition)) {
				if (!allMarked) {
					continue;
				}
				Place place = (Place) node;
				if (marking.occurrences(place) > 0) {
					// Place contains a token.
				} else {
					// Place does not contain a token. Check input places of input routing
					// transitions
					allMarked = false;
					for (PetrinetNode node2 : preset.get(place)) {
						Transition transition2 = (Transition) node2;
//						System.out.println("[DicoverPetriNetAlgorithm] Checking input transition " + transition2.getLabel());		
						if (transition2.isInvisible() && !transition2.getLabel().equals(ActivityAlphabet.START)
								&& !transition2.getLabel().equals(ActivityAlphabet.END)) {
							PetrinetNode node3 = preset.get(transition2).iterator().next();
							Place place3 = (Place) node3;
//							System.out.println("[DicoverPetriNetAlgorithm] Checking input place " + place3.getLabel());		
							if (marking.occurrences(place3) > 0) {
								// Found a marked input place of a routing transition.
								allMarked = true;
							}
						}
					}
				}
			}
			if (allMarked) {
				enabled.add(transition.getLabel());
			}
		}
//		System.out.println("[DicoverPetriNetAlgorithm] Enabled " + enabled);		
		return enabled;
	}

	/*
	 * Given enL and enM, extend the precision with the given alignment.
	 */
	private void apply(PluginContext context, SyncReplayResult alignment, EventBasedPrecisionParameters parameters) {
		/*
		 * History is initally empty.
		 */
		List<String> hist = new ArrayList<String>();
		/*
		 * Alignment corresponds to this many traces in event log.
		 */
		int n = alignment.getTraceIndex().size();

		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			/*
			 * Get step type an dnode instance.
			 */
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);

			/*
			 * Check whether we're at some event, and whether it makes sense.
			 */
			if ((stepType == StepTypes.L || stepType == StepTypes.LMGOOD) && enM.get(hist).size() > 0) {
				/*
				 * Add as amny events to the precision as there were traces corresponding to
				 * this alignment.
				 */
				precision.addNofEvents(n);
				/*
				 * Extend the precision in a similar way. By definition of the precision.
				 */
				double eventPrecision = n * (((double) enL.get(hist).size()) / enM.get(hist).size());
				precision.addSumPrecision(eventPrecision);
				/*
				 * Output mismatches, that is, if precision drops. Could be useful diagnostic
				 * information.
				 */
				if (parameters.isShowInfo() && !enL.get(hist).equals(enM.get(hist))) {
					precision.addInfo("History = " + hist + ", enL = " + enL.get(hist) + ", enM = " + enM.get(hist));
					precision.addInfo(
							"Number of Events = " + n + ", Accumulated Precision for Events = " + eventPrecision);
				}
			}

			/*
			 * Update the history.
			 */
			switch (stepType) {
			case MREAL:
				// Fall-through
			case LMGOOD: {
				Transition transition = (Transition) nodeInstance;
				hist.add(transition.getLabel());
				break;
			}
			case MINVI: {
				break;
			}
			case L: {
				break;
			}
			default:
			}
		}
	}
}
