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
		// Get last parameter settings.
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		
		// Create widget to allow the user to change the settings, and show it.
		DiscoverPetriNetWidget widget = new DiscoverPetriNetWidget(parameters);
		InteractionResult result = context.showWizard("Configure DiSCovery", true, true, widget);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// Discover accepting Petri net.
		return apply(context, log, parameters);
	}

}
