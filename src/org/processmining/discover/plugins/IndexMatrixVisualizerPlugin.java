package org.processmining.discover.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.discover.models.IndexLog;
import org.processmining.discover.models.IndexMatrix;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class IndexMatrixVisualizerPlugin {

	@Plugin(name = "Visualize Direclty-Follows Graph", returnLabels = {
			"Visualized Directly-Follows Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(PluginContext context, IndexMatrix matrix) {
		return matrix.getComponent();
	}

	@Plugin(name = "Visualize Direclty-Follows Graph", returnLabels = {
			"Visualized Directly-Follows Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(PluginContext context, XLog log) { 
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!log.getClassifiers().isEmpty()) {
			classifier = log.getClassifiers().get(0);
		}
		IndexMatrix matrix = new IndexMatrix(new IndexLog(log, classifier));
		return matrix.getComponent();
	}
}
