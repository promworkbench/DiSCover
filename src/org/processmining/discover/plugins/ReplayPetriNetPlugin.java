package org.processmining.discover.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.ReplayPetriNetAlgorithm;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class ReplayPetriNetPlugin extends ReplayPetriNetAlgorithm {

	@Plugin( //
			name = "Classify DiSCovered Petri net", //
			parameterLabels = { "Event log", "Accepting Petri net" }, //
			returnLabels = { "Classified event log" }, //
			returnTypes = { XLog.class }, //
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
			variantLabel = "Classify DiSCovered Petri net", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog runDefault(PluginContext context, XLog log, AcceptingPetriNet apn) {
		return apply(log, apn);
	}
}
