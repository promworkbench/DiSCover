package org.processmining.discover.algorithms;

import org.processmining.discover.models.CountMatrix;
import org.processmining.discover.parameters.DiscoverCountMatrixFromEventLogParameters;
import org.processmining.framework.plugin.PluginContext;

public class DiscoverCountMatrixFromEventLogAlgorithm {

	public CountMatrix apply(PluginContext context, DiscoverCountMatrixFromEventLogParameters parameters) {
		CountMatrix matrix = new CountMatrix(parameters.getLog(), parameters.getClassifier());
		matrix.discoverFromEventLog(parameters);
		return matrix;
	}
}
