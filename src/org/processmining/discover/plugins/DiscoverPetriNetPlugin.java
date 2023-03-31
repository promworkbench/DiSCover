package org.processmining.discover.plugins;

import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.DiscoverPetriNetAlgorithm;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.discover.widgets.DiscoverPetriNetWidget;
import org.processmining.discover.widgets.FilterMatrixCollectionWidget;
import org.processmining.discover.widgets.FilterMatrixWidget;
import org.processmining.discover.widgets.SelectActivitiesWidget;
import org.processmining.discover.widgets.SelectActivitySetsWidget;
import org.processmining.discover.widgets.SelectClassifierWidget;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.processtree.ProcessTree;

public class DiscoverPetriNetPlugin extends DiscoverPetriNetAlgorithm {

	@Plugin( //
			name = "DiSCover Petri net (process tree)", //
			parameterLabels = { "Event log", "Process tree" }, //
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
			variantLabel = "DiSCover Petri net (process tree)", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet runProcessTree(UIPluginContext context, XLog log, ProcessTree tree) {
		// Get (last) parameter settings.
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		int step = 0;

		while (true) {
			JPanel widget;
			switch (step) {
				case 0 :
					widget = new SelectClassifierWidget(log, parameters);
					break;
				case 1 :
					widget = new SelectActivitiesWidget(log, parameters);
					break;
				case 2 :
					widget = new FilterMatrixWidget(log, parameters);
					break;
				case 3 :
					widget = parameters.getMatrix().getComponent();
					break;
				case 4 :
					widget = new SelectActivitySetsWidget(parameters);
					break;
				case 5 :
					widget = new FilterMatrixCollectionWidget(parameters);
					break;
				case 6 :
					widget = parameters.getMatrixCollection().getComponent();
					break;
				default :
					widget = new DiscoverPetriNetWidget(parameters);
					break;
			}
			InteractionResult result = context.showWizard("Configure DiSCovery", step == 0, step == 7, widget);
			switch (result) {
				case NEXT :
					if (step == 0 && parameters.getClassifier() == null) {
						// Ignore, need classifier to proceed.
					} else if (step == 1 && parameters.getActivities().isEmpty()) {
						// Ignore, need a non-empty alphabet.
					} else {
						step++;
						if (step == 3 && !parameters.isShowGraph()) {
							step++;
						}
					}
					break;
				case PREV :
					step--;
					if (step == 3 && !parameters.isShowGraph()) {
						step--;
					}
					break;
				case FINISHED :
					return apply(context, log, tree, parameters);
				default :
					context.getFutureResult(0).cancel(true);
					return null;
			}
		}
	}

	@Plugin( //
			name = "DiSCover Petri net (last)", //
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
			variantLabel = "DiSCover Petri net (last)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runLast(PluginContext context, XLog log) {
		// Get last parameter settings.
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		// Discover accepting Petri net.
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (provided)", //
			parameterLabels = { "Event log", "Parameter values" }, //
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
			variantLabel = "DiSCover Petri net (provided)", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet runProvided(PluginContext context, XLog log, DiscoverPetriNetParameters parameters) {
		// Discover accepting Petri net.
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (user)", //
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
			variantLabel = "DiSCover Petri net (user)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runUser(UIPluginContext context, XLog log) {
		return runProcessTree(context, log, null);
	}

	@Plugin( //
			name = "DiSCover Petri net (auto)", //
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
			variantLabel = "DiSCover Petri net (auto)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runAuto(PluginContext context, XLog log) {
		
		int pivotAbs = 1;
		int pivotRel = 3;
		/*
		 * Try to discover a net with as few silent transitions as possible.
		 */
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		parameters.setAbsoluteThreshold(pivotAbs);
		parameters.setRelativeThreshold(pivotRel);
		parameters.setAbsoluteThreshold2(pivotAbs);
		parameters.setRelativeThreshold2(pivotRel);
		AcceptingPetriNet bestApn = apply(context, log, parameters);
		int bestCount = countArcs(bestApn);
		if (bestCount < 2) {
			// No way to get a better result.
			System.out.println("[DiscoverPetriNetPlugin] Found best net with thresholds " + pivotAbs + " and " + pivotRel + ".");
			return bestApn;
		}
		/*
		 * Discover alternative nets by changing the thresholds.
		 * Use the thresholds itself as a penalty to promote low thresholds.
		 */
		int bestAbs = 0;
		int bestRel = 0;
		for (int abs = 0; abs < 5; abs++) {
			for (int rel = 0; rel < 100; rel++) {
				if (abs == pivotAbs && rel == pivotRel) {
					// Done this one before.
					continue;
				}
				if ((abs + rel) >= (pivotAbs + pivotRel) && Math.abs(pivotAbs - abs) + Math.abs(pivotRel - rel) >= bestCount + Math.abs(pivotAbs - bestAbs) + Math.abs(pivotRel - bestRel)) {
					// Cannot be better.
					rel = 100;
					continue;
				}
				parameters = new DiscoverPetriNetParameters();
				parameters.setAbsoluteThreshold(abs);
				parameters.setRelativeThreshold(rel);
				parameters.setAbsoluteThreshold2(abs);
				parameters.setRelativeThreshold2(rel);
				AcceptingPetriNet apn = apply(context, log, parameters);
				int count = countArcs(apn);
				System.out.println("[DiscoverPetriNetPlugin] Found net with thresholds " + abs + " and " + rel + ", score is " + count);
				if (count + Math.abs(pivotAbs - abs) + Math.abs(pivotRel - rel) < bestCount + Math.abs(pivotAbs - bestAbs) + Math.abs(pivotRel - bestRel)) {
					bestCount = count;
					bestApn = apn;
					bestAbs = abs;
					bestRel = rel;
				}
			}
		}
		System.out.println("[DiscoverPetriNetPlugin] Found best net with thresholds " + bestAbs + " and " + bestRel + ".");
		return bestApn;
	}

	private int countArcs(AcceptingPetriNet apn) {
		return apn.getNet().getEdges().size();
	}

	private int countSilent(AcceptingPetriNet apn) {
		int cnt = 0;
		for (Transition transition : apn.getNet().getTransitions()) {
			if (transition.isInvisible()) {
				cnt++;
			}
		}
		return cnt;
	}
}
