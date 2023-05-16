package org.processmining.discover.plugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.ExcavatePetriNetAlgorithm;
import org.processmining.discover.parameters.ExcavatePetriNetParameters;
import org.processmining.discover.widgets.ExcavatePetriNetWidget;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class ExcavatePetriNetPlugin extends ExcavatePetriNetAlgorithm {

	@Plugin( //
			name = "Xcavate Petri net (provided)", //
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
			name = "Xcavate Petri net (default)", //
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
		absValues.add(0);
		parameters.setAbsValues(absValues);
		List<Integer> relValues = new ArrayList<Integer>();
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
		parameters.setSimplicityFactor(0.5);
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "Xcavate Petri net (user)", //
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
	public AcceptingPetriNet runUser(UIPluginContext context, XLog log) {
		ExcavatePetriNetParameters parameters = new ExcavatePetriNetParameters();
		ExcavatePetriNetWidget widget = new ExcavatePetriNetWidget(parameters);
		InteractionResult result = context.showWizard("Configure Xcavation", true, true, widget);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, log , parameters);
	}
	
}
