package org.processmining.discover.plugins;

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

import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.discover.algorithms.DiscoverPetriNetAlgorithm;
import org.processmining.discover.parameters.DiscoverPetriNetParameters;
import org.processmining.discover.widgets.DiscoverPetriNetWidget;
import org.processmining.discover.widgets.FilterMatrixCollectionWidget;
import org.processmining.discover.widgets.FilterMatrixWidget;
import org.processmining.discover.widgets.SelectActivitiesWidget;
import org.processmining.discover.widgets.SelectActivitySetsWidget;
import org.processmining.discover.widgets.SelectClassifierWidget;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
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
import org.processmining.processtree.ProcessTree;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class DiscoverPetriNetPlugin extends DiscoverPetriNetAlgorithm {

	@Plugin( //
			name = "DiSCover Petri net (process tree)", //
			parameterLabels = { "Event log", "Process tree" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
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
			variantLabel = "DiSCover Petri net (process tree)", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet runProcessTree(UIPluginContext context, XLog log, ProcessTree tree) {
		// Get (last) parameter settings.
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		int step = 0;

		while (true) {
			JPanel widget;
			switch (step) {
				case 0 :
					widget = new SelectClassifierWidget(log, parameters);
					break;
				case 1 :
					widget = new SelectActivitiesWidget(log, parameters);
					break;
				case 2 :
					widget = new FilterMatrixWidget(log, parameters);
					break;
				case 3 :
					widget = parameters.getMatrix().getComponent();
					break;
				case 4 :
					widget = new SelectActivitySetsWidget(parameters);
					break;
				case 5 :
					widget = new FilterMatrixCollectionWidget(parameters);
					break;
				case 6 :
					widget = parameters.getMatrixCollection().getComponent();
					break;
				default :
					widget = new DiscoverPetriNetWidget(parameters);
					break;
			}
			InteractionResult result = context.showWizard("Configure DiSCovery", step == 0, step == 7, widget);
			switch (result) {
				case NEXT :
					if (step == 0 && parameters.getClassifier() == null) {
						// Ignore, need classifier to proceed.
					} else if (step == 1 && parameters.getActivities().isEmpty()) {
						// Ignore, need a non-empty alphabet.
					} else {
						step++;
						if (step == 3 && !parameters.isShowGraph()) {
							step++;
						}
					}
					break;
				case PREV :
					step--;
					if (step == 3 && !parameters.isShowGraph()) {
						step--;
					}
					break;
				case FINISHED :
					return apply(context, log, tree, parameters);
				default :
					context.getFutureResult(0).cancel(true);
					return null;
			}
		}
	}

	@Plugin( //
			name = "DiSCover Petri net (last)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
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
			variantLabel = "DiSCover Petri net (last)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runLast(PluginContext context, XLog log) {
		// Get last parameter settings.
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		// Discover accepting Petri net.
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (provided)", //
			parameterLabels = { "Event log", "Parameter values" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
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
			variantLabel = "DiSCover Petri net (provided)", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public AcceptingPetriNet runProvided(PluginContext context, XLog log, DiscoverPetriNetParameters parameters) {
		// Discover accepting Petri net.
		return apply(context, log, parameters);
	}

	@Plugin( //
			name = "DiSCover Petri net (user)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
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
			variantLabel = "DiSCover Petri net (user)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runUser(UIPluginContext context, XLog log) {
		return runProcessTree(context, log, null);
	}

	@Plugin( //
			name = "DiSCover Petri net (auto)", //
			parameterLabels = { "Event log" }, //
			returnLabels = { "DiSCovered Accepting Petri net" }, //
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
			variantLabel = "DiSCover Petri net (auto)", //
			requiredParameterLabels = { 0 } //
	) //
	public AcceptingPetriNet runAuto(PluginContext context, XLog log) {

		/*
		 * Try to discover a net with as few silent transitions as possible.
		 */
		DiscoverPetriNetParameters parameters = new DiscoverPetriNetParameters();
		AcceptingPetriNet bestApn = null;
		double bestScore = 0.0;
		/*
		 * Discover alternative nets by changing the thresholds. Use the
		 * thresholds itself as a penalty to promote low thresholds.
		 */
		int bestAbs = 0;
		int bestRel = 0;
		int maxAbs = 6;
		int maxRel = 100;
		for (int abs = 0; abs < maxAbs; abs++) {
			for (int rel = 0; rel < maxRel; rel += (rel < 10 ? 1 : rel < 30 ? 2 : 5)) {
				parameters = new DiscoverPetriNetParameters();
				parameters.setAbsoluteThreshold(abs);
				parameters.setRelativeThreshold(rel);
				parameters.setAbsoluteThreshold2(abs);
				parameters.setRelativeThreshold2(rel);
				AcceptingPetriNet apn = apply(context, log, parameters);

				if (apn.getNet().getTransitions().size() > 100) {
					continue;
				}

				double time = System.currentTimeMillis();
				double simplicity = getSimplicity(apn);
				System.out.println("[DiscoverPetriNetPlugin] Computing simplicity took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");

				if (getScore(1.0, 1.0, simplicity) < bestScore) {
					/*
					 * Even a perfect fitness and precision will not result in a new best score.
					 */
					continue;
				}
				
				time = System.currentTimeMillis();
				PNRepResult replay = getReplay(apn, log);
				System.out.println("[DiscoverPetriNetPlugin] Replaying log on discovered net took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				time = System.currentTimeMillis();
				double fitness = getFitness(replay, log);
				System.out.println("[DiscoverPetriNetPlugin] Computing fitness took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");
				time = System.currentTimeMillis();
				double precision = getPrecision(replay, apn);
				System.out.println("[DiscoverPetriNetPlugin] Computing precision took "
						+ (System.currentTimeMillis() - time) + " milliseconds.");

				double score = getScore(fitness, precision, simplicity);
				System.out.println("[DiscoverPetriNetPlugin] Found net with thresholds " + abs + " and " + rel
						+ ", score " + score + " (f=" + fitness + ", p=" + precision + ", s=" + simplicity + ")");
				if (score > bestScore) {
					bestScore = score;
					bestApn = apn;
					bestAbs = abs;
					bestRel = rel;
				}
				if (getScore(fitness, 1.0, 1.0) < bestScore) {
					/*
					 * As fitness is unlikely to go up when increasing the relative
					 * threshold, we will most likely not get a new best score by increasing
					 * this threshold.
					 */
					rel = maxRel;
				}
			}
		}
		System.out.println("[DiscoverPetriNetPlugin] Found best net with thresholds " + bestAbs + " and " + bestRel
				+ ", score " + bestScore);
		return bestApn;
	}

	private double getScore(double fitness, double precision, double simplicity) {
		double fitPrec = 2 * fitness * precision / (fitness + precision);
		return 2 * fitPrec * simplicity / (fitPrec + simplicity);
	}
	
	private double getFitness(PNRepResult replay, XLog log) {
		int fitting = 0;
		for (SyncReplayResult traceReplay : replay) {
			if (traceReplay.getInfo().containsKey(PNRepResult.RAWFITNESSCOST)
					&& traceReplay.getInfo().get(PNRepResult.RAWFITNESSCOST) == 0) {
				fitting += traceReplay.getTraceIndex().size();
			}
		}
		return (1.0 * fitting) / log.size();
	}

	private double getPrecision(PNRepResult replay, AcceptingPetriNet apn) {
		EventBasedPrecisionParameters pars = new EventBasedPrecisionParameters(apn);
		EventBasedPrecisionAlgorithm alg = new EventBasedPrecisionAlgorithm();
		try {
			return alg.apply(null, replay, apn, pars).getPrecision();
		} catch (IllegalTransitionException e) {
			// TODO Auto-generated catch block
			return 0.0;
		}
	}

	private double getSimplicity(AcceptingPetriNet apn) {
		return (apn.getNet().getPlaces().size() + apn.getNet().getTransitions().size() + 1.0)
				/ apn.getNet().getEdges().size();
	}

	private PNRepResult getReplay(AcceptingPetriNet apn, XLog log) {
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
				costModelMove, costSyncMove, costLogMove);
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
			Map<Transition, Integer> costSyncMove, Map<XEventClass, Integer> costLogMove) {

		int nThreads = 6;
		int costUpperBound = Integer.MAX_VALUE;
		// timeout per trace in milliseconds
		int timeoutMilliseconds = 10 * 1000;

		int maximumNumberOfStates = Integer.MAX_VALUE;
		ReplayerParameters parameters;

		//		parameters = new ReplayerParameters.Dijkstra(false, false, nThreads, Debug.DOT, timeoutMilliseconds,
		//				maximumNumberOfStates, costUpperBound, false, 2, true);

		//Current: 
		//		parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false, Debug.NONE, timeoutMilliseconds,
		//				maximumNumberOfStates, costUpperBound, false, false, 0, 3);

		//		//BPM2018: 
		//		parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false, Debug.DOT,
		//						timeoutMilliseconds, maximumNumberOfStates, costUpperBound, false, false);
		//Traditional
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
				case DUPLICATE :
					assert false; // cannot happen in this setting
					throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
				case FAILED :
					// internal error in the construction of synchronous product or other error.
					throw new RuntimeException("Error in alignment computations");
				case SUCCESS :
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
						// in some marking, there were too many tokens in a place, blocking the addition of more tokens. Current upper limit is 128
					}
					if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
						// in some marking, the cost function went through the upper limit of 2^24
					}
					if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
						// in some marking, the heuristic function went through the upper limit of 2^24
					}
					if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED
							|| (exitCode & Utils.SOLVERTIMEOUTREACHED) == Utils.SOLVERTIMEOUTREACHED) {
						// alignment failed with a timeout (caused in the solver if SOLVERTIMEOUTREACHED is set)
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
