package org.processmining.discover.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinetclassicalreductor.algorithms.ReduceUsingMurataRulesAlgorithm;
import org.processmining.acceptingpetrinetclassicalreductor.parameters.ReduceUsingMurataRulesParameters;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.DiscoverCountMatrixFromEventLogAlgorithm;
import org.processmining.discover.algorithms.DiscoverPetriNetFromCountMatrixAlgorithm;
import org.processmining.discover.models.CountMatrix;
import org.processmining.discover.parameters.DiscoverCountMatrixFromEventLogParameters;
import org.processmining.discover.parameters.DiscoverPetriNetFromCountMatrixParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

@Plugin( //
		name = "DiSCover Petri net", //
		parameterLabels = { "Event log" }, //
		returnLabels = { "DiSCovered Accepting Petri net" }, //
		returnTypes = { AcceptingPetriNet.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "http://www.win.tue.nl/~hverbeek/", //
		help = "" //
) //
public class DiscoverPetriNetFromEventLogPlugin {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (Fullyautomatix)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet run(PluginContext context, XLog log) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!log.getClassifiers().isEmpty()) {
			classifier = log.getClassifiers().get(0);
		}
		DiscoverCountMatrixFromEventLogParameters matrixParameters = new DiscoverCountMatrixFromEventLogParameters();
		matrixParameters.setLog(log);
		matrixParameters.setClassifier(classifier);
		DiscoverCountMatrixFromEventLogAlgorithm matrixAlgorithm = new DiscoverCountMatrixFromEventLogAlgorithm();
		CountMatrix matrix = matrixAlgorithm.apply(context, matrixParameters);

		DiscoverPetriNetFromCountMatrixParameters netParameters = new DiscoverPetriNetFromCountMatrixParameters();
		DiscoverPetriNetFromCountMatrixAlgorithm netAlgorithm = new DiscoverPetriNetFromCountMatrixAlgorithm();
		ReduceUsingMurataRulesAlgorithm redAlgorithm = new ReduceUsingMurataRulesAlgorithm();
		ReduceUsingMurataRulesParameters redParameters = new ReduceUsingMurataRulesParameters();
		AcceptingPetriNet bestApn = null;
		AcceptingPetriNet firstApn = null;
		int bestEdges = 0;
		netParameters.setMatrix(matrix);
		int maxCount = matrix.getMaxCount();
		for (int absThreshold = 0; absThreshold < 5; absThreshold++) {
			int penalty = 2*absThreshold;
			for (int relThreshold = 2*maxCount; relThreshold > 0; relThreshold /= 2) {
				penalty += 2;
				netParameters.setAbsoluteThreshold(absThreshold);
				netParameters.setRelativeThreshold(relThreshold);
				matrix.clean(netParameters);
				AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
				apn = redAlgorithm.apply(context, apn, redParameters);
				if (firstApn == null) {
					firstApn = apn;
				}
				Set<Transition> transitions = new HashSet<Transition>(apn.getNet().getTransitions());
				Set<Transition> inputTransitions = new HashSet<Transition>();
				Set<Transition> outputTransitions = new HashSet<Transition>();
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
					if (transitions.contains(edge.getSource())) {
						outputTransitions.add((Transition) edge.getSource());
					}
					if (transitions.contains(edge.getTarget())) {
						inputTransitions.add((Transition) edge.getTarget());
					}
				}
				if (inputTransitions.size() == transitions.size() && outputTransitions.size() == transitions.size()) {
					int edges = apn.getNet().getEdges().size() + penalty;
					System.out.println("[DiscoverPetriNetFromEventLogPlugin] " + absThreshold + ", " + relThreshold
							+ ": " + edges);
					if (bestApn == null || edges < bestEdges) {
						bestApn = apn;
						bestEdges = edges;
					}
				}
			}
		}
		System.out.println("[DiscoverPetriNetFromEventLogPlugin] " + bestEdges);
		return bestApn != null ? bestApn : firstApn;
	}
}
