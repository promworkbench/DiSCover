package org.processmining.discover.plugins;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.ReduceClusteredSimpleSilentTransitionsAlgorithm;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class ReduceClusteredSimpleSilentTransitionsPlugin extends ReduceClusteredSimpleSilentTransitionsAlgorithm {

	@Plugin( //
			name = "Reduce clusters of simple silent transitions", //
			parameterLabels = { "Accepting Petri net" }, //
			returnLabels = { "Reduced Accepting Petri net" }, //
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
			variantLabel = "Reduce clusters of simple silent transition", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet run(PluginContext context, AcceptingPetriNet apn) {
		return apply(context, apn);
	}
}
