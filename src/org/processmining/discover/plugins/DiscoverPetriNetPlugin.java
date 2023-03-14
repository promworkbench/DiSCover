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
				case 0:
					widget = new SelectClassifierWidget(log, parameters);
					break;
				case 1:
					widget = new SelectActivitiesWidget(log, parameters);
					break;
				case 2:
					widget = new FilterMatrixWidget(log, parameters);
					break;
				case 3:
					widget = new SelectActivitySetsWidget(parameters);
					break;
				case 4:
					widget = new FilterMatrixCollectionWidget(parameters);
					break;
				default: 
					widget = new DiscoverPetriNetWidget(parameters);
					break;
			}
			InteractionResult result = context.showWizard("Configure DiSCovery", step == 0, step == 5, widget);
			switch (result) {
				case NEXT:
					if (step == 0 && parameters.getClassifier() == null) {
						// Ignore, need classifier to proceed.
					} else if (step == 1 && parameters.getActivities().isEmpty()) {
						// Ignore, need a non-empty alphabet.
					} else {
						step++;
					}
					break;
				case PREV:
					step--;
					break;
				case FINISHED:
					return apply(context, log, tree, parameters);
				default:
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

}
