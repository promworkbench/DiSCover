package org.processmining.discover.plugins;

import java.util.ArrayList;
import java.util.List;

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
	public AcceptingPetriNet run(PluginContext context, XLog log) {
		ExcavatePetriNetParameters parameters = new ExcavatePetriNetParameters();
		List<Integer> absValues = new ArrayList<Integer>();
		for (int i = 0; i < 6; i++) {
			absValues.add(i);
		}
		parameters.setAbsValues(absValues);
		List<Integer> relValues = new ArrayList<Integer>();
		relValues = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			relValues.add(i);
		}
		for (int i = 10; i < 30; i += 2) {
			relValues.add(i);
		}
		for (int i = 30; i < 100; i += 5) {
			relValues.add(i);
		}
		parameters.setRelValues(relValues);
		parameters.setFitnessFactor(1.0);
		parameters.setPrecisionFactor(1.0);
		parameters.setSimplicityFactor(1.0);
		parameters.setNofThreads(6);
		return apply(context, log, parameters);
	}
}
