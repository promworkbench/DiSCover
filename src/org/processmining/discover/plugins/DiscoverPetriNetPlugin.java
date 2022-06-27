package org.processmining.discover.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.DiscoverPetriNetAlgorithm;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.discover.widgets.DiscoverPetriNetWidget;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class DiscoverPetriNetPlugin extends DiscoverPetriNetAlgorithm {

	@Plugin( //
			name = "DiSCover Petri net (default parameter values)", //
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
			variantLabel = "DiSCover Petri net (default parameter values)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runNoMerge(PluginContext context, XLog log) {
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (provided parameter values)", //
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
			variantLabel = "DiSCover Petri net (provided parameter values)", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet runNoMerge(PluginContext context, XLog log, DiscoverPetriNetParameters parameters) {
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (User-selected parameter values)", //
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
			variantLabel = "DiSCover Petri net (User-selected parameter values)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runMerge(UIPluginContext context, XLog log) {
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		parameters.setMerge(true);
		
		DiscoverPetriNetWidget widget = new DiscoverPetriNetWidget(parameters);
		InteractionResult result = context.showWizard("Configure DiSCovery", true, true, widget);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		return apply(context, log, parameters);
	}

}
