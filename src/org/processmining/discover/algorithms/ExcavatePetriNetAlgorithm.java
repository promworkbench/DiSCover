package org.processmining.discover.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.discover.parameters.ExcavatePetriNetParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.inputs.CheckerInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.precision.algorithms.EventBasedPrecisionAlgorithm;
import org.processmining.precision.parameters.EventBasedPrecisionParameters;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class ExcavatePetriNetAlgorithm extends DiscoverPetriNetAlgorithm {

	private final String ISPOSKEY = "pdc:isPos";

	public AcceptingPetriNet apply(PluginContext context, XLog log, ExcavatePetriNetParameters xParameters) {

		UIPluginContext uiContext = null;
		if (context instanceof UIPluginContext) {
			uiContext = (UIPluginContext) context;
			uiContext.getProgress().setMinimum(0);
			uiContext.getProgress().setMaximum(xParameters.getAbsValues().size() * xParameters.getRelValues().size());
			uiContext.getProgress().setValue(0);
		}
		/*
		 * Try to discover a net with as few silent transitions as possible.
		 */
		DiscoverPetriNetParameters parameters = null;
		AcceptingPetriNet bestApn = null;
		AcceptingPetriNet simplestApn = null;

		double bestScore = -1.0;
		double simplestScore = -1.0;
		/*
		 * Discover alternative nets by changing the thresholds. Use the thresholds
		 * itself as a penalty to promote low thresholds.
		 */
		int bestAbs = 0;
		int bestRel = 0;
		int simplestAbs = 0;
		int simplestRel = 0;
		int i = 0;
		boolean foundWFnet = false;
		Set<Integer> seenSizes = new HashSet<Integer>();
		for (int abs : xParameters.getAbsValues()) {

			/*
			 * If all else fails, use the (non-filtered) log as the filtered log.
			 */
			XLog filteredLog = log;
			XLogInfo info = XLogInfoFactory.createLogInfo(log, xParameters.getClassifier());
			CheckerInput input = null;
			CheckerConfiguration configuration = null;
			/*
			 * Collect all positive and negative traces.
			 */
			Set<XTrace> positiveTraces = new HashSet<XTrace>();
			Set<XTrace> negativeTraces = new HashSet<XTrace>();
			for (XTrace trace : filteredLog) {
				if (trace.getAttributes().containsKey(ISPOSKEY)) {
					XAttribute isPosAttribute = trace.getAttributes().get(ISPOSKEY);
					if (isPosAttribute instanceof XAttributeBoolean) {
						if (((XAttributeBoolean) isPosAttribute).getValue()) {
							positiveTraces.add(trace);
						} else {
							negativeTraces.add(trace);
						}
					}
				}
			}
			if (!positiveTraces.isEmpty() || !negativeTraces.isEmpty()) {
				filteredLog = (XLog) log.clone();
			}
			/*
			 * Filter the negative traces out.
			 */
			filteredLog.removeAll(negativeTraces);

			/*
			 * if smaller than 100, abs will be the target value for et, rt, and pt.
			 * A value of 21 indicates "do not use". 
			 */
			int et = abs;
			int rt = abs;
			int pt = abs;
			int nt = 21;

			if (abs > 100) {
				/*
				 * The value of abs exceeds 100.
				 * It should know be considered as equal to 100 + 22*et + 22*22*rt + 22*22*22*pt + 22*22*22*22*nt.
				 */
				abs = abs - 100;
				et = abs % 22;
				if (abs > 21) {
					abs = abs / 22;
				}
				rt = abs % 22;
				if (abs > 21) {
					abs = abs / 22;
				}
				pt = abs % 22;
				if (abs > 21) {
					abs = abs / 22;
				}
				nt = abs % 22;
			}
			System.out.println("[ExcavatePetriNetAglorithm] Usign log skeleton thresholds " + et + ", " + rt + ", " + pt
					+ " and " + nt + ".");

			if (abs > 20) {
				System.out.println("[ExcavatePetriNetAglorithm] Capped log skeleton thresholds to 20.");
				abs = 20;
			}
			if (abs > 0) {
				try {
					/*
					 * Filter the log using log skeletons.
					 * 
					 * First, filter on equivalence.
					 */
					if (et < 21) {
						LogSkeleton ls = context.tryToFindOrConstructFirstNamedObject(LogSkeleton.class,
								"Build Log Skeleton from Event Log", null, null, filteredLog);
						ls.setEquivalenceThreshold(100 - et);
						input = new CheckerInput(ls, log);
						configuration = new CheckerConfiguration(input);
						filteredLog = context.tryToFindOrConstructFirstNamedObject(XLog.class,
								"Filter Event Log on Log Skeleton", null, null, ls, filteredLog, configuration);
						/*
						 * Add classifiers if needed.
						 */
						if (filteredLog.getClassifiers().isEmpty()) {
							filteredLog.getClassifiers().addAll(log.getClassifiers());
						}
					}
					/*
					 * Second, filter on response and precedence.
					 */
					if (rt < 21 || pt < 21) {
						LogSkeleton ls = context.tryToFindOrConstructFirstNamedObject(LogSkeleton.class,
								"Build Log Skeleton from Event Log", null, null, filteredLog);
						if (rt < 21) {
							ls.setResponseThreshold(100 - rt);
						}
						if (pt < 21) {
							ls.setPrecedenceThreshold(100 - pt);
						}
						input = new CheckerInput(ls, log);
						configuration = new CheckerConfiguration(input);
						filteredLog = context.tryToFindOrConstructFirstNamedObject(XLog.class,
								"Filter Event Log on Log Skeleton", null, null, ls, filteredLog, configuration);
						/*
						 * Add classifiers if needed.
						 */
						if (filteredLog.getClassifiers().isEmpty()) {
							filteredLog.getClassifiers().addAll(log.getClassifiers());
						}
					}
					if (nt < 21) {
						LogSkeleton ls = context.tryToFindOrConstructFirstNamedObject(LogSkeleton.class,
								"Build Log Skeleton from Event Log", null, null, filteredLog);
						ls.setNotCoExistenceThreshold(100 - nt);
						input = new CheckerInput(ls, log);
						configuration = new CheckerConfiguration(input);
						filteredLog = context.tryToFindOrConstructFirstNamedObject(XLog.class,
								"Filter Event Log on Log Skeleton", null, null, ls, filteredLog, configuration);
						/*
						 * Add classifiers if needed.
						 */
						if (filteredLog.getClassifiers().isEmpty()) {
							filteredLog.getClassifiers().addAll(log.getClassifiers());
						}
					}
					/*
					 * Add positive traces if they were filtered out.
					 */
					for (XTrace trace : positiveTraces) {
						if (!filteredLog.contains(trace)) {
							filteredLog.add(trace);
						}
					}
				} catch (Exception e) {
					System.err.println("[ExcavatePetriNetAglorithm] Failed to filter using a log skeleton: " + e);
				}
				XLogInfo filteredInfo = XLogInfoFactory.createLogInfo(filteredLog, xParameters.getClassifier());
				if (seenSizes.contains(filteredLog.size())) {
					/*
					 * A filtered log with the same number of traces was already seen.
					 */
					System.out.println("[ExcavatePetriNetAlgorithm] Discarded threshold " + abs
							+ " because filtered log already seen.");
					if (uiContext != null) {
						i += xParameters.getRelValues().size();
						uiContext.getProgress().setValue(i);
					}
					continue;
				}
				if (filteredInfo.getEventClasses().size() < info.getEventClasses().size()) {
					/*
					 * Lost some activities due to the filtering. Do not consider this filtered log.
					 */
					System.out.println("[ExcavatePetriNetAlgorithm] Discarded threshold " + abs
							+ " because filtered log does not contain all activities.");
					if (uiContext != null) {
						i += xParameters.getRelValues().size();
						uiContext.getProgress().setValue(i);
					}
					continue;
				}
			}
			seenSizes.add(filteredLog.size());

			for (int rel : xParameters.getRelValues()) {
				if (uiContext != null) {
					uiContext.getProgress().setValue(i++);
				}

				parameters = new DiscoverPetriNetParameters();
				parameters.setClassifier(xParameters.getClassifier());
				// parameters.setAbsoluteThreshold(abs);
				parameters.setRelativeThreshold(rel);
				// parameters.setAbsoluteThreshold2(0);
				parameters.setRelativeThreshold2(0);
				AcceptingPetriNet apn = apply(context, filteredLog, parameters);

				double time = System.currentTimeMillis();

				boolean isWFnet = false;
				if (xParameters.isPreferWFnet()) {
					time = System.currentTimeMillis();
					isWFnet = isWFNet(context, apn);
					System.out.println("[ExcavatePetriNetAlgorithm] Analyzing WF net on discovered net took "
							+ (System.currentTimeMillis() - time) + " milliseconds.");
				}
				if (foundWFnet && !isWFnet) {
					System.out.println("[ExcavatePetriNetAlgorithm] Discarded thresholds " + abs + " and " + rel
							+ " because result is not a WF net.");
					continue;
				}
				if (isWFnet && !foundWFnet) {
					foundWFnet = true;
					bestApn = null;
					bestScore = -1.0;
					simplestApn = null;
					simplestScore = -1.0;
				}

				double simplicity = getSimplicity(apn, xParameters);
				double size = getSize(apn, xParameters);
				System.out.println("[ExcavatePetriNetAlgorithm] Computing simplicity took "
						+ (System.currentTimeMillis() - time) + " milliseconds: " + simplicity + ", " + size + ".");

				double simpleScore = getScore(1.0, 1.0, simplicity, size, xParameters);

				if (simpleScore > simplestScore) {
					if (uiContext != null) {
						uiContext.log("Discovered net at spot (" + abs + ", " + rel + ") with new best simple score "
								+ simpleScore);
					}
					simplestApn = apn;
					simplestAbs = abs;
					simplestRel = rel;
					simplestScore = simpleScore;
				}

				if (simpleScore < bestScore) {
					/*
					 * Even a perfect fitness and precision will not result in a new best score.
					 */
					System.out.println("[ExcavatePetriNetAlgorithm] Discarded thresholds " + abs + " and " + rel
							+ " due to insufficient simplicity.");
					continue;
				}

				if (apn.getNet().getTransitions().size() > xParameters.getMaxNofTransitions()) {
					System.out.println("[DiscoverPetriNetPlugin] Discarded thresholds " + abs + " and " + rel
							+ " for replay due to too many transitions.");
					continue;
				}

				time = System.currentTimeMillis();
				PNRepResult replay = getReplay(apn, log, xParameters);
				System.out.println("[ExcavatePetriNetAlgorithm] Replaying log on discovered net took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				time = System.currentTimeMillis();
				double fitness = getFitness(replay, log, xParameters);
				System.out.println("[ExcavatePetriNetAlgorithm] Computing fitness took "
						+ (System.currentTimeMillis() - time) + " milliseconds: " + fitness + ".");

				if (getScore(fitness, 1.0, simplicity, size, xParameters) < bestScore) {
					/*
					 * Even a perfect precision will not result in a new best score.
					 */
					System.out.println("[ExcavatePetriNetAlgorithm] Discarded thresholds " + abs + " and " + rel
							+ " due to insufficient fitness.");
					continue;
				}

				time = System.currentTimeMillis();
				double precision = getPrecision(replay, apn, xParameters);
				System.out.println("[ExcavatePetriNetAlgorithm] Computing precision took "
						+ (System.currentTimeMillis() - time) + " milliseconds: " + precision + ".");

				double score = getScore(fitness, precision, simplicity, size, xParameters);
				System.out.println("[ExcavatePetriNetAlgorithm] Found net with thresholds " + abs + " and " + rel
						+ ", score " + score + " (f=" + fitness + ", p=" + precision + ", s=" + simplicity + ", n="
						+ size + ")");
				if (score > bestScore) {
					if (uiContext != null) {
						uiContext.log("Discovered net at spot (" + abs + ", " + rel + ") with new best score " + score);
					}
					bestScore = score;
					bestApn = apn;
					bestAbs = abs;
					bestRel = rel;
				}
			}
		}
		if (uiContext != null) {
			uiContext.getProgress().setValue(uiContext.getProgress().getMaximum());
		}
		if (bestApn == null) {
			/*
			 * No net found that is small enough to compute alignments. Take the simplest
			 * one found instead.
			 */
			System.out.println("[ExcavatePetriNetAlgorithm] Found smallest net with thresholds " + simplestAbs + " and "
					+ simplestRel + ", score " + simplestScore);
			return simplestApn;
		}
		System.out.println("[ExcavatePetriNetAlgorithm] Found best net with thresholds " + bestAbs + " and " + bestRel
				+ ", score " + bestScore);
		return bestApn;
	}

	private boolean isWFNet(PluginContext context, AcceptingPetriNet apn) {

		Map<PetrinetNode, Set<PetrinetNode>> preset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		Map<PetrinetNode, Set<PetrinetNode>> postset = new HashMap<PetrinetNode, Set<PetrinetNode>>();
		for (PetrinetNode node : apn.getNet().getNodes()) {
			preset.put(node, new HashSet<PetrinetNode>());
			postset.put(node, new HashSet<PetrinetNode>());
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : apn.getNet().getEdges()) {
			postset.get(edge.getSource()).add(edge.getTarget());
			preset.get(edge.getTarget()).add(edge.getSource());
		}

		Place in = null;
		Place out = null;

		for (Place place : apn.getNet().getPlaces()) {
			if (preset.get(place).isEmpty()) {
				if (in != null) {
					// Multiple source places
					return false;
				}
				in = place;
			}
			if (postset.get(place).isEmpty()) {
				if (out != null) {
					// Multiple sink places
					return false;
				}
				out = place;
			}
		}
		if (in == null || out == null) {
			// No source place or no sink place
			return false;
		}

		for (Transition transition : apn.getNet().getTransitions()) {
			if (preset.get(transition).isEmpty() || postset.get(transition).isEmpty()) {
				// Transition with either no input places or no output places
				return false;
			}
		}

		Set<PetrinetNode> frontier = new HashSet<PetrinetNode>();
		Set<PetrinetNode> covered = new HashSet<PetrinetNode>();

		frontier.add(in);
		while (!frontier.isEmpty()) {
			PetrinetNode node = frontier.iterator().next();
			frontier.remove(node);
			for (PetrinetNode postNode : postset.get(node)) {
				if (!covered.contains(postNode)) {
					frontier.add(postNode);
				}
			}
			covered.add(node);
		}
		if (!covered.equals(apn.getNet().getNodes())) {
			// Some not not reachable fron source node
			return false;
		}

		frontier.clear();
		covered.clear();

		frontier.add(out);
		while (!frontier.isEmpty()) {
			PetrinetNode node = frontier.iterator().next();
			frontier.remove(node);
			for (PetrinetNode preNode : preset.get(node)) {
				if (!covered.contains(preNode)) {
					frontier.add(preNode);
				}
			}
			covered.add(node);
		}
		if (!covered.equals(apn.getNet().getNodes())) {
			// Sink node not reachable from some node
			return false;
		}

		return true;
	}

	// private boolean isWFNetWoflan(PluginContext context, AcceptingPetriNet apn) {
	//
	// try {
	// WoflanAssumptions assumptions = new WoflanAssumptions();
	// // We sure have an S cover.
	// assumptions.add(WoflanState.SCOVER);
	// /*
	// * Prevent Woflan from constructing a coverability
	// * graph. We only want to know whether the ne tis a WF
	// * net.
	// */
	// assumptions.add(WoflanState.BOUNDED);
	// assumptions.add(WoflanState.NOTDEAD);
	// assumptions.add(WoflanState.LIVE);
	// WoflanDiagnosis diagnosis = (new
	// Woflan()).diagnose(context.createChildContext("Woflan"),
	// apn.getNet(), assumptions);
	// if (diagnosis.isSound()) {
	// return true;
	// }
	// } catch (Exception e) {
	// System.out.println("[ExcavatePetriNetAlgorithm] Could not check WF net due to
	// " + e);
	// }
	// return false;
	// }

	private double getScore(double fitness, double precision, double simplicity, double size,
			ExcavatePetriNetParameters xParameters) {
		double numerator = xParameters.getFitnessFactor() * fitness;
		double denominator = xParameters.getFitnessFactor();
		numerator += xParameters.getPrecisionFactor() * precision;
		denominator += xParameters.getPrecisionFactor();
		numerator += xParameters.getSimplicityFactor() * simplicity;
		denominator += xParameters.getSimplicityFactor();
		numerator += xParameters.getSizeFactor() * size;
		denominator += xParameters.getSizeFactor();
		return denominator == 0.0 ? 0.0 : numerator / denominator;
	}

	private double getScorePow(double fitness, double precision, double simplicity, double size) {
		double fitPrec = 2 * fitness * precision / (fitness + precision);
		double SimSize = 2 * simplicity * size / (simplicity + size);
		return 2 * fitPrec * SimSize / (fitPrec + SimSize);
	}

	private double getFitnessPow(PNRepResult replay, XLog log, ExcavatePetriNetParameters xParameters) {
		return Math.pow(getFitness(replay, log, xParameters), xParameters.getFitnessFactor());
	}

	private double getFitness(PNRepResult replay, XLog log, ExcavatePetriNetParameters xParameters) {
		if (xParameters.getFitnessFactor() == 0.0) {
			return -1.0;
		}
		double mlf = (double) replay.getInfo().get(PNRepResult.MOVELOGFITNESS);
		double mmf = (double) replay.getInfo().get(PNRepResult.MOVEMODELFITNESS);
		return mlf + mmf == 0.0 ? 0.0 : 2 * mlf * mmf / (mlf + mmf);
	}

	private double getPrecisionPow(PNRepResult replay, AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		return Math.pow(getPrecision(replay, apn, xParameters), xParameters.getPrecisionFactor());
	}

	private double getPrecision(PNRepResult replay, AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		if (xParameters.getPrecisionFactor() == 0.0) {
			return -1.0;
		}
		EventBasedPrecisionParameters pars = new EventBasedPrecisionParameters(apn);
		pars.setShowInfo(true);
		EventBasedPrecisionAlgorithm alg = new EventBasedPrecisionAlgorithm();
		try {
			// EventBasedPrecision precision = alg.apply(null, replay, apn, pars);
			// System.out.println("[ExcavatePetriNetALgorithm]\n" +
			// precision.toHTMLString(false));
			return alg.apply(null, replay, apn, pars).getPrecision();
		} catch (IllegalTransitionException e) {
			// TODO Auto-generated catch block
			return 0.0;
		}
	}

	private double getSimplicityPow(AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		return Math.pow(getSimplicity(apn, xParameters), xParameters.getSimplicityFactor());
	}

	private double getSimplicity(AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		if (xParameters.getSimplicityFactor() == 0.0) {
			return -1.0;
		}
		int nodeCount = apn.getNet().getPlaces().size() + apn.getNet().getTransitions().size();
		int edgeCount = apn.getNet().getEdges().size() + 1;
		double minCount = 1.0 * Math.min(nodeCount, edgeCount);
		double maxCount = 1.0 * Math.max(nodeCount, edgeCount);
		return minCount / maxCount;
	}

	private double getSizePow(AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		return Math.pow(getSize(apn, xParameters), xParameters.getSizeFactor());
	}

	private double getSize(AcceptingPetriNet apn, ExcavatePetriNetParameters xParameters) {
		if (xParameters.getSizeFactor() == 0.0) {
			return -1.0;
		}
		double cnt = 0;
		for (Transition transition : apn.getNet().getTransitions()) {
			if (!transition.isInvisible()) {
				cnt++;
			}
		}
		return (cnt / apn.getNet().getTransitions().size());
	}

	private PNRepResult getReplay(AcceptingPetriNet apn, XLog log, ExcavatePetriNetParameters xParameters) {
		if (xParameters.getFitnessFactor() == 0.0 && xParameters.getPrecisionFactor() == 0.0) {
			return null;
		}
		// matching using A (typical for xes files)
		XEventClassifier eventClassifier = new XEventNameClassifier();
		Petrinet net = apn.getNet();

		Iterator<Transition> it = net.getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = it.next();
			if (!t.isInvisible() && t.getLabel().endsWith("+complete")) {
				// matching using A+Complete (typical for mxml files)
				eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
				break;
			}
		}

		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		TransEvClassMapping mapping = constructMappingBasedOnLabelEquality(net, log, dummyEvClass, eventClassifier);
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses classes = summary.getEventClasses();

		Map<Transition, Integer> costModelMove = new HashMap<>();
		Map<Transition, Integer> costSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costLogMove = new HashMap<>();
		for (Transition t : net.getTransitions()) {
			costSyncMove.put(t, 0);
			costModelMove.put(t, t.isInvisible() ? 0 : 2);
		}
		for (XEventClass c : summary.getEventClasses().getClasses()) {
			costLogMove.put(c, 5);
		}
		costLogMove.put(dummyEvClass, 5);

		System.out.println(String.format("Log size: %d events, %d traces, %d classes", summary.getNumberOfEvents(),
				summary.getNumberOfTraces(), (summary.getEventClasses().size() + 1)));
		System.out.println(String.format("Model size: %d transitions, %d places", net.getTransitions().size(),
				net.getPlaces().size()));

		return doReplay(log, net, apn.getInitialMarking(), apn.getFinalMarkings().iterator().next(), classes, mapping,
				costModelMove, costSyncMove, costLogMove, xParameters);
	}

	private TransEvClassMapping constructMappingBasedOnLabelEquality(PetrinetGraph net, XLog log,
			XEventClass dummyEvClass, XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			boolean mapped = false;
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();

				if (t.getLabel().equals(id)) {
					mapping.put(t, evClass);
					mapped = true;
					break;
				}
			}

			if (!mapped && !t.isInvisible()) {
				mapping.put(t, dummyEvClass);
			}

		}

		return mapping;
	}

	private PNRepResult doReplay(XLog log, Petrinet net, Marking initialMarking, Marking finalMarking,
			XEventClasses classes, TransEvClassMapping mapping, Map<Transition, Integer> costModelMove,
			Map<Transition, Integer> costSyncMove, Map<XEventClass, Integer> costLogMove,
			ExcavatePetriNetParameters xParameters) {

		int nThreads = xParameters.getNofThreads();
		int costUpperBound = Integer.MAX_VALUE;
		// timeout per trace in milliseconds
		int timeoutMilliseconds = 10 * 1000;

		int maximumNumberOfStates = Integer.MAX_VALUE;
		ReplayerParameters parameters;

		// parameters = new ReplayerParameters.Dijkstra(false, false, nThreads,
		// Debug.DOT, timeoutMilliseconds,
		// maximumNumberOfStates, costUpperBound, false, 2, true);

		// Current:
		// parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false,
		// Debug.NONE, timeoutMilliseconds,
		// maximumNumberOfStates, costUpperBound, false, false, 0, 3);

		// //BPM2018:
		// parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false,
		// Debug.DOT,
		// timeoutMilliseconds, maximumNumberOfStates, costUpperBound, false, false);
		// Traditional
		parameters = new ReplayerParameters.AStar(true, true, true, nThreads, true, Debug.NONE, timeoutMilliseconds,
				maximumNumberOfStates, costUpperBound, false);

		Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classes, costModelMove,
				costLogMove, costSyncMove, mapping, false);

		// preprocessing time to be added to the statistics if necessary
		long preProcessTimeNanoseconds = 0;

		ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);

		@SuppressWarnings("unchecked")
		Future<TraceReplayTask>[] futures = new Future[log.size()];

		for (int i = 0; i < log.size(); i++) {
			// Setup the trace replay task
			TraceReplayTask task = new TraceReplayTask(replayer, parameters, log.get(i), i, timeoutMilliseconds,
					parameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			// submit for execution
			futures[i] = service.submit(task);
		}
		// initiate shutdown and wait for termination of all submitted tasks.
		service.shutdown();

		// obtain the results one by one.
		Collection<SyncReplayResult> results = new ArrayList<SyncReplayResult>();

		for (int i = 0; i < log.size(); i++) {

			TraceReplayTask result;
			try {
				result = futures[i].get();
			} catch (Exception e) {
				// execution os the service has terminated.
				throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			}
			switch (result.getResult()) {
			case DUPLICATE:
				assert false; // cannot happen in this setting
				throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
			case FAILED:
				// internal error in the construction of synchronous product or other error.
				throw new RuntimeException("Error in alignment computations");
			case SUCCESS:
				// process succcesful execution of the replayer
				SyncReplayResult replayResult = result.getSuccesfulResult();
				int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();
				if ((exitCode & Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
					// Optimal alignment found.
					results.add(replayResult);

				} else if ((exitCode & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
					// failure in the alignment. Error code shows more details.
				}
				if ((exitCode & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
					// in some marking, there were too many tokens in a place, blocking the addition
					// of more tokens. Current upper limit is 128
				}
				if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
					// in some marking, the cost function went through the upper limit of 2^24
				}
				if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
					// in some marking, the heuristic function went through the upper limit of 2^24
				}
				if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED
						|| (exitCode & Utils.SOLVERTIMEOUTREACHED) == Utils.SOLVERTIMEOUTREACHED) {
					// alignment failed with a timeout (caused in the solver if SOLVERTIMEOUTREACHED
					// is set)
				}
				if ((exitCode & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
					// alignment failed due to reacing too many states.
				}
				if ((exitCode & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
					// no optimal alignment found with cost less or equal to the given limit.
				}
				if ((exitCode & Utils.CANCELLED) == Utils.CANCELLED) {
					// user-cancelled.
				}
				if ((exitCode & Utils.FINALMARKINGUNREACHABLE) == Utils.FINALMARKINGUNREACHABLE) {
					// user-cancelled.
					System.err.println("final marking unreachable.");
				}

				break;
			}
		}
		return new PNRepResultImpl(results);
	}

	private CostBasedCompleteParam createReplayParameters(Collection<XEventClass> activities,
			XEventClass invisibleActivity, AcceptingPetriNet net) {
		CostBasedCompleteParam parameters = new CostBasedCompleteParam(activities, invisibleActivity,
				net.getNet().getTransitions(), 1, 1);
		parameters.setInitialMarking(net.getInitialMarking());
		Set<Marking> finalMarkings = net.getFinalMarkings();
		if (finalMarkings.isEmpty()) {
			finalMarkings = new HashSet<Marking>();
			finalMarkings.add(new Marking());
		}
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
		return parameters;
	}
}
