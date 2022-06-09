package org.processmining.discover.algorithms;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.discover.parameters.DiscoverPetriNetFromCountMatrixParameters;
import org.processmining.framework.plugin.PluginContext;

public class DiscoverPetriNetFromCountMatrixAlgorithm {

	public AcceptingPetriNet apply(PluginContext context, DiscoverPetriNetFromCountMatrixParameters parameters) {
		
		return parameters.getMatrix().convert(parameters);
	}
}
