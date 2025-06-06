package org.processmining.discover.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.discover.models.ActivityAlphabet;
import org.processmining.discover.models.ActivityLog;
import org.processmining.discover.models.ActivityMatrix;
import org.processmining.discover.models.ActivityMatrixCollection;
import org.processmining.discover.models.ActivitySets;
import org.processmining.discover.models.ConcurrentActivityPairs;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class ActivityMatrixVisualizerPlugin {

	@Plugin(name = "Visualize root graph", returnLabels = {
			"Visualized Directly-Follows Activity Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runMatrix(PluginContext context, ActivityMatrix matrix) {
		return matrix.getComponent();
	}

	@Plugin(name = "Visualize root graph", returnLabels = {
			"Visualized Directly-Follows Activity Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runLog(PluginContext context, XLog eventLog) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		ActivityAlphabet alphabet = new ActivityAlphabet(eventLog, classifier);
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);
		ActivityMatrix matrix = new ActivityMatrix(log, alphabet);

		return matrix.getComponent();
	}

	@Plugin(name = "Visualize combined component graphs", returnLabels = {
			"Visualized irectly-Follows Activity Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runLogCollection(PluginContext context, XLog eventLog) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		ActivityAlphabet alphabet = new ActivityAlphabet(eventLog, classifier);
		ActivityLog log = new ActivityLog(eventLog, classifier, alphabet);
		ActivityMatrix matrix = new ActivityMatrix(log, alphabet);

		ConcurrentActivityPairs pairs = new ConcurrentActivityPairs(matrix, alphabet, new DiscoverPetriNetParameters());
		ActivitySets separated = new ActivitySets(pairs, alphabet);
		ActivityMatrixCollection matrices = new ActivityMatrixCollection(log, alphabet, separated, matrix);

		return matrices.getComponent();
	}
}
