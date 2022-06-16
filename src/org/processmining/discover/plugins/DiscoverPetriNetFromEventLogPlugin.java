package org.processmining.discover.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinetclassicalreductor.algorithms.ReduceUsingMurataRulesAlgorithm;
import org.processmining.acceptingpetrinetclassicalreductor.parameters.ReduceUsingMurataRulesParameters;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.DiscoverCountMatrixFromEventLogAlgorithm;
import org.processmining.discover.algorithms.DiscoverPetriNetFromCountMatrixAlgorithm;
import org.processmining.discover.models.CountMatrix;
import org.processmining.discover.parameters.DiscoverCountMatrixFromEventLogParameters;
import org.processmining.discover.parameters.DiscoverPetriNetFromCountMatrixParameters;
import org.processmining.discover.widgets.DiscoverPetriNetFromCountMatrixWidget;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class DiscoverPetriNetFromEventLogPlugin {

	@Plugin( //
			name = "DiSCover Petri net (Auto noise)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
			returnTypes = { AcceptingPetriNet.class }, //
			userAccessible = true, //
			url = "http://www.win.tue.nl/~hverbeek/", //
			help = "" //
	) //
	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (Auto noise)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runAuto(PluginContext context, XLog log) {
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
		int bestScore = 0;
		netParameters.setMatrix(matrix);
		int maxRelThreshold = matrix.getMaxRelThreshold();
		for (int absThreshold = 0; absThreshold < 1; absThreshold++) {
			int penalty = 2 * absThreshold;
			for (int relThreshold = maxRelThreshold; relThreshold > 0; relThreshold /= 2) {
				penalty += 2;
				netParameters.setAbsoluteThreshold(absThreshold);
				netParameters.setRelativeThreshold(relThreshold);
				matrix.clean(netParameters);
				AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
				if (apn == null) {
					continue;
				}
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
				int score = apn.getNet().getEdges().size() + penalty
						+ 100 * (2 * transitions.size() - inputTransitions.size() - outputTransitions.size());
				System.out.println(
						"[DiscoverPetriNetFromEventLogPlugin] " + absThreshold + ", " + relThreshold + ": " + score);
				if (bestApn == null || score < bestScore) {
					bestApn = apn;
					bestScore = score;
				}
			}
		}
		System.out.println("[DiscoverPetriNetFromEventLogPlugin] " + bestScore);
		return bestApn != null ? bestApn : firstApn;
	}

	@Plugin( //
			name = "DiSCover Petri net (No noise)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
			returnTypes = { AcceptingPetriNet.class }, //
			userAccessible = true, //
			url = "http://www.win.tue.nl/~hverbeek/", //
			help = "" //
	) //
	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (No noise)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runNoNoise(PluginContext context, XLog log) {
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

		netParameters.setAbsoluteThreshold(0);
		netParameters.setRelativeThreshold(matrix.getMaxRelThreshold());
		netParameters.setMatrix(matrix);
		matrix.clean(netParameters);
		AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
		apn = redAlgorithm.apply(context, apn, redParameters);
		return apn;
	}

	@Plugin( //
			name = "DiSCover Petri net (Specific noise)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
			returnTypes = { AcceptingPetriNet.class }, //
			userAccessible = true, //
			url = "http://www.win.tue.nl/~hverbeek/", //
			help = "" //
	) //
	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (Specific noise)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runSpecificNoise(PluginContext context, XLog log) {
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

		netParameters.setAbsoluteThreshold(2);
		netParameters.setRelativeThreshold(29);
		netParameters.setMatrix(matrix);
		matrix.clean(netParameters);
		AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
		apn = redAlgorithm.apply(context, apn, redParameters);
		return apn;
	}

	@Plugin( //
			name = "DiSCover Petri net (Given noise)", //
			parameterLabels = { "Event log", "Absolute threshold", "Relative threshold" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
			returnTypes = { AcceptingPetriNet.class }, //
			userAccessible = true, //
			url = "http://www.win.tue.nl/~hverbeek/", //
			help = "" //
	) //
	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (Given noise)", //
			requiredParameterLabels = { 0, 1, 2 } //
	) //
	public AcceptingPetriNet runGivenNoise(PluginContext context, XLog log, int absThreshold, int relThreshold) {
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

		netParameters.setAbsoluteThreshold(absThreshold);
		netParameters.setRelativeThreshold(relThreshold);
		netParameters.setMatrix(matrix);
		matrix.clean(netParameters);
		AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
		apn = redAlgorithm.apply(context, apn, redParameters);
		return apn;
	}

	@Plugin( //
			name = "DiSCover Petri net (User selects noise)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
			returnTypes = { AcceptingPetriNet.class }, //
			userAccessible = true, //
			url = "http://www.win.tue.nl/~hverbeek/", //
			help = "" //
	) //
	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "DiSCover Petri net (User selects noise)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runUserSelectedNoise(UIPluginContext context, XLog log) {
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

		netParameters.setAbsoluteThreshold(0);
		netParameters.setRelativeThreshold(matrix.getMaxRelThreshold());
		netParameters.setMatrix(matrix);
		
		DiscoverPetriNetFromCountMatrixWidget widget = new DiscoverPetriNetFromCountMatrixWidget(matrix, netParameters);
		InteractionResult result = context.showWizard("Configure DiSCovery", true, true, widget);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		matrix.clean(netParameters);
		AcceptingPetriNet apn = netAlgorithm.apply(context, netParameters);
		apn = redAlgorithm.apply(context, apn, redParameters);
		return apn;
	}
}
