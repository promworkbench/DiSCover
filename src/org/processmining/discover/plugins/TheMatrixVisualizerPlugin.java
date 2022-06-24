package org.processmining.discover.plugins;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.discover.algorithms.TheCreateMatrixCollectionAlgorithm;
import org.processmining.discover.algorithms.TheSeparateAllConcurrentActivitiesAlgorithm;
import org.processmining.discover.models.TheActivitySets;
import org.processmining.discover.models.TheConcurrencyInfo;
import org.processmining.discover.models.TheInputOutputInfo;
import org.processmining.discover.models.TheLog;
import org.processmining.discover.models.TheMatrix;
import org.processmining.discover.models.TheMatrixCollection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class TheMatrixVisualizerPlugin {

	@Plugin(name = "Visualize Direclty-Follows Graph", returnLabels = {
			"Visualized Directly-Follows Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runMatrix(PluginContext context, TheMatrix matrix) {
		return matrix.getComponent();
	}

	@Plugin(name = "Visualize Direclty-Follows Graph", returnLabels = {
			"Visualized Directly-Follows Graph" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runLog(PluginContext context, XLog eventLog) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		TheLog log = new TheLog(eventLog, classifier);
		TheMatrix matrix = new TheMatrix(log);

		return matrix.getComponent();
	}

	@Plugin(name = "Visualize Synchronized Direclty-Follows Graphs", returnLabels = {
			"Visualized Synchronized Directly-Follows Graphs" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runLogCollection(PluginContext context, XLog eventLog) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		if (!eventLog.getClassifiers().isEmpty()) {
			classifier = eventLog.getClassifiers().get(0);
		}
		TheLog log = new TheLog(eventLog, classifier);
		TheMatrix matrix = new TheMatrix(log);

		TheConcurrencyInfo info = matrix.getConcurrencyInfo();
		TheActivitySets sets = TheSeparateAllConcurrentActivitiesAlgorithm.apply(info);
		TheMatrixCollection matrices = TheCreateMatrixCollectionAlgorithm.apply(log, matrix, sets);
		
		Set<TheInputOutputInfo> infos = new HashSet<TheInputOutputInfo>();
		for (TheMatrix subMatrix : matrices.getMatrices()) {
			infos.add(subMatrix.getInputOutputInfo());
		}
		TheInputOutputInfo mergedInfo = new TheInputOutputInfo(infos);
		
		System.out.println("[TheMatrixVisualizerPlugin]\n" + mergedInfo.toString(log));
		
		return matrices.getComponent();
	}
}
