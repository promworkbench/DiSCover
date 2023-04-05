package org.processmining.discover.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.ExcavatePetriNetAlgorithm;
import org.processmining.discover.parameters.ExcavatePetriNetParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class ExcavatePetriNetPlugin extends ExcavatePetriNetAlgorithm {

	@Plugin( //
			name = "Xcavate Petri net", //
			parameterLabels = { "Event log", "Parameters" }, //
			returnLabels = { "Xcavated Accepting Petri net" }, //
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
			variantLabel = "Xcavate Petri net", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet run(PluginContext context, XLog log,ExcavatePetriNetParameters xParameters) {
		return apply(context, log, xParameters);		
	}
	
	@Plugin( //
			name = "Xcavate Petri net using default settings", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "Xcavated Accepting Petri net" }, //
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
			variantLabel = "Xcavate Petri net", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet run(PluginContext context, XLog log) {
		ExcavatePetriNetParameters parameters = new ExcavatePetriNetParameters();
		parameters.getAbsValues().clear();
		parameters.getAbsValues().add(0);
		parameters.getRelValues().clear();
		parameters.getRelValues().add(0);
		return apply(context, log, parameters);
	}

}
