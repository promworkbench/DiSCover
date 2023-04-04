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
	public AcceptingPetriNet run(PluginContext context, XLog log,ExcavatePetriNetParameters xParameters) {
		return apply(context, log, xParameters);		
	}
	
	@Plugin( //
			name = "Xcavate Petri net maximizing fitness, precision and simplicity", //
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
		return apply(context, log, parameters);
	}
	
	@Plugin( //
			name = "Xcavate Petri net maximizing fitness and precision", //
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
	public AcceptingPetriNet runFP(PluginContext context, XLog log) {
		ExcavatePetriNetParameters parameters = new ExcavatePetriNetParameters();
		parameters.setSimplicityFactor(0.0);
		return apply(context, log, parameters);
	}

}
