package org.processmining.discover.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.TheDiscoverPetriNetAlgorithm;
import org.processmining.discover.parameters.TheDiscoverPetriNetParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class TheDiscoverPetriNetPlugin extends TheDiscoverPetriNetAlgorithm {

	@Plugin( //
			name = "DiSCover Petri net (Do not merge)", //
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
			variantLabel = "DiSCover Petri net (Do not merge)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runDoNotMerge(PluginContext context, XLog log) {
		TheDiscoverPetriNetParameters parameters = new TheDiscoverPetriNetParameters();
		parameters.setMerge(false);
		
		return apply(log, parameters);
	}
	
	@Plugin( //
			name = "DiSCover Petri net (Do merge)", //
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
			variantLabel = "DiSCover Petri net (Do merge)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runDoMerge(PluginContext context, XLog log) {
		TheDiscoverPetriNetParameters parameters = new TheDiscoverPetriNetParameters();
		parameters.setMerge(true);
		
		return apply(log, parameters);
	}
	
}
