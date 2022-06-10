package org.processmining.discover.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.discover.parameters.DiscoverPetriNetFromCountMatrixParameters;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class CountMatrix {

	/*
	 * Maps an activity to its index.
	 */
	private Map<String, Integer> indices;
	/*
	 * Maps an index onto its activity.
	 */
	private String[] classes;
	/*
	 * For i and j < class.length: - dfCounts[i][j] counts the number of times
	 * classes[i] is directly followed by classes[j]. -
	 * dfCounts[class.length][i] counts the number of times classes[i] started a
	 * trace. - dfCounts[i][class.length] counts the number of times classes[i]
	 * ended a trace.
	 */
	private int[][] dfCounts;
	private int maxCount;

	private XLog log;
	private XEventClassifier classifier;

	public CountMatrix(XLog log, XEventClassifier classifier) {
		this.log = log;
		this.classifier = classifier;
	}

	public void clean(DiscoverPetriNetFromCountMatrixParameters parameters) {
		maxCount = dfCounts[0][0];
		for (int i = 0; i < classes.length + 1; i++) {
			for (int j = 0; j < classes.length + 1; j++) {
				if (dfCounts[i][j] < 0) {
					dfCounts[i][j] = -dfCounts[i][j];
				}
				maxCount = Math.max(maxCount, dfCounts[i][j]);
			}
		}
		for (int i = 0; i < classes.length + 1; i++) {
			for (int j = 0; j < classes.length + 1; j++) {
				if (dfCounts[i][j] > 0) {
					int maxi = 0;
					int maxj = 0;
					for (int k = 0; k < classes.length; k++) {
						if (Math.abs(dfCounts[i][k]) > maxi) {
							maxi = Math.abs(dfCounts[i][k]);
						}
						if (Math.abs(dfCounts[k][j]) > maxj) {
							maxj = Math.abs(dfCounts[k][j]);
						}
					}
					if ((dfCounts[i][j] <= parameters.getAbsoluteThreshold())
							|| (dfCounts[i][j] < maxi && parameters.getRelativeThreshold() * maxi
									* dfCounts[i][j] < (maxi + dfCounts[i][j]) * (maxi + dfCounts[i][j]))
							|| (dfCounts[i][j] < maxj && parameters.getRelativeThreshold() * maxj
									* dfCounts[i][j] < (maxj + dfCounts[i][j]) * (maxj + dfCounts[i][j]))) {
						/*
						 * Count is considered irrelevant. Negate it, as this
						 * still allows us to use Math.abs(count).
						 */
						dfCounts[i][j] = -dfCounts[i][j];
					}
				}
			}
		}
	}

	public int getMaxCount() {
		return maxCount;
	}
	
	public void discoverFromEventLog(DiscoverPetriNetFromCountMatrixParameters parameters) {
		discoverFromEventLog(new HashSet<Integer>(), parameters);
	}

	public void discoverFromEventLog(Set<Integer> ignore, DiscoverPetriNetFromCountMatrixParameters parameters) {
		int newIndex = 0;
		/*
		 * Determine the number of event classes (entries).
		 */
		Set<String> entries = new HashSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				entries.add(classifier.getClassIdentity(event));
			}
		}
		/*
		 * Initialize fields.
		 */
		indices = new HashMap<String, Integer>();
		dfCounts = new int[entries.size() + 1][entries.size() + 1];
		classes = new String[entries.size()];
		/*
		 * Fill the fields.
		 */
		for (XTrace trace : log) {
			int fromIndex = -1;
			for (XEvent event : trace) {
				String eventClass = classifier.getClassIdentity(event);
				if (!indices.containsKey(eventClass)) {
					indices.put(eventClass, newIndex);
					classes[newIndex++] = eventClass;
				}
				int toIndex = indices.get(eventClass);
				if (!ignore.contains(toIndex)) {
					if (fromIndex == -1) {
						/*
						 * classes[toIndex] starts the trace.
						 */
						dfCounts[classes.length][toIndex]++;
					} else {
						/*
						 * classes[fromIndex] is directly followed by
						 * classes[toIndex].
						 */
						dfCounts[fromIndex][toIndex]++;
					}
					fromIndex = toIndex;
				}
			}
			if (fromIndex != -1) {
				/*
				 * classes[fromIndex] ends the trace.
				 */
				dfCounts[fromIndex][classes.length]++;
			}
		}
		clean(parameters);
		//		System.out.println("[CountMatrix] " + ignore + "\n" + this.toString());
	}

	public String toString() {
		String s = "----\n";
		String t = "";
		for (int toIndex = 0; toIndex < classes.length; toIndex++) {
			t += "\t" + dfCounts[classes.length][toIndex];
		}
		s += t + "\n----\n";
		for (int fromIndex = 0; fromIndex < classes.length; fromIndex++) {
			t = "";
			for (int toIndex = 0; toIndex < classes.length; toIndex++) {
				t += "\t" + dfCounts[fromIndex][toIndex];
			}
			s += t + "\n";
		}
		s += "----\n";
		t = "";
		for (int fromIndex = 0; fromIndex < classes.length; fromIndex++) {
			t += "\t" + dfCounts[fromIndex][classes.length];
		}
		s += t + "\n----\n";
		t = "";
		for (int index = 0; index < classes.length; index++) {
			t += classes[index] + "\n";
		}
		s += t + "----\n";
		return s;
	}

	private boolean isBoth(int fromIndex, int toIndex) {
		return dfCounts[fromIndex][toIndex] > 0 && dfCounts[toIndex][fromIndex] > 0;
	}

	private boolean isAbsolute(int fromIndex, int toIndex) {
		return dfCounts[fromIndex][toIndex] > 0;
	}

	private Set<Set<Integer>> getSolutions(DiscoverPetriNetFromCountMatrixParameters parameters) {
		Set<Set<Integer>> solutions = new HashSet<Set<Integer>>();
		Set<Integer> candidateSolution = new HashSet<Integer>(indices.values());
		searchSolutions(candidateSolution, new HashSet<Set<Integer>>(), solutions, parameters);
		return solutions;
	}

	private void searchSolutions(Set<Integer> candidate, Set<Set<Integer>> candidatesDone, Set<Set<Integer>> solutions,
			DiscoverPetriNetFromCountMatrixParameters parameters) {
		if (candidatesDone.contains(candidate)) {
			//			System.out.println("[CountMatrix] Skipping candidate " + candidate);
			return;
		}
		candidatesDone.add(candidate);
		for (int fromIndex : candidate) {
			for (int toIndex : candidate) {
				if (toIndex < fromIndex) {
					if (isBoth(fromIndex, toIndex)) {
						//						System.out.println("[CountMatrix] " + candidate + " Both " + fromIndex + " " + toIndex);
						Set<Integer> firstCandidateSolution = new HashSet<Integer>(candidate);
						firstCandidateSolution.remove(fromIndex);
						searchSolutions(firstCandidateSolution, candidatesDone, solutions, parameters);
						Set<Integer> secondCandidateSolution = new HashSet<Integer>(candidate);
						secondCandidateSolution.remove(toIndex);
						searchSolutions(secondCandidateSolution, candidatesDone, solutions, parameters);
						return;
					}
				}
			}
		}
		//		System.out.println("[CountMatrix] Found candidate " + candidate);
		for (Set<Integer> solution : solutions) {
			if (solution.containsAll(candidate)) {
				//				System.out.println("[CountMatrix] Candidate is not maximal " + candidate);
				return;
			}
		}
		Set<Set<Integer>> subSolutions = new HashSet<Set<Integer>>();
		for (Set<Integer> solution : solutions) {
			if (candidate.containsAll(solution)) {
				subSolutions.add(solution);
			}
		}
		//		System.out.println("[CountMatrix] Removing subsolutions " + subSolutions);
		solutions.removeAll(subSolutions);
		//		System.out.println("[CountMatrix] Adding solution " + candidate);
		solutions.add(candidate);
	}

	public AcceptingPetriNet convert(DiscoverPetriNetFromCountMatrixParameters parameters) {
		Petrinet net = PetrinetFactory.newPetrinet("Petri net DiSCovered");

		System.out.println("[CountMatrix]\n" + toString());
		Transition tStart = net.addTransition("\u25BA");
		tStart.setInvisible(true);
		Place pStart = net.addPlace("i");
		net.addArc(pStart, tStart);
		Marking mi = new Marking();
		mi.add(pStart);

		Transition tEnd = net.addTransition("\u25A0");
		tEnd.setInvisible(true);
		Place pEnd = net.addPlace("o");
		net.addArc(tEnd, pEnd);
		Marking mf = new Marking();
		mf.add(pEnd);
		Set<Marking> mfs = new HashSet<Marking>();
		mfs.add(mf);

		Transition[] tClass = new Transition[classes.length];
		if (parameters.isMerge()) {
			for (String eventClass : indices.keySet()) {
				int index = indices.get(eventClass);
				tClass[index] = net.addTransition(eventClass);
			}
		}
		
		Set<Set<Integer>> solutions = getSolutions(parameters);
		System.out.println("[CountMatrix] " + solutions);
		for (Set<Integer> solution : solutions) {
			if (!parameters.isMerge()) {
				for (int index : solution) {
					tClass[index] = net.addTransition(classes[index]);
				}
			}
			CountMatrix subMatrix = new CountMatrix(log, classifier);
			Set<Integer> ignore = new HashSet<>(indices.values());
			ignore.removeAll(solution);
			subMatrix.discoverFromEventLog(ignore, parameters);
			System.out.println("[CountMatrix] " + solution + "\n" + subMatrix.toString());
			Place[] piClass = new Place[indices.size()];
			Place[] poClass = new Place[indices.size()];
			for (int index : solution) {
				piClass[index] = net.addPlace("pi_" + solution + "@" + index);
				net.addArc(piClass[index], tClass[index]);
				poClass[index] = net.addPlace("po_" + solution + "@" + index);
				net.addArc(tClass[index], poClass[index]);
			}
			Set<Integer> starters = new TreeSet<Integer>();
			Set<Integer> enders = new TreeSet<Integer>();
			for (int index : solution) {
				if (dfCounts[classes.length][index] > 0) {
					starters.add(index);
				}
				if (dfCounts[index][classes.length] > 0) {
					enders.add(index);
				}
			}
			Place pi = net.addPlace("pi_s->" + solution);
			net.addArc(tStart, pi);
			for (int index : starters) {
				Transition t = net.addTransition("t_" + solution + "@s->" + index);
				t.setInvisible(true);
				net.addArc(pi, t);
				net.addArc(t, piClass[index]);
			}
			Place po = net.addPlace("po_" + solution + "->e");
			net.addArc(po, tEnd);
			for (int index : enders) {
				Transition t = net.addTransition("t_" + solution + "@" + index + "->e");
				t.setInvisible(true);
				net.addArc(poClass[index], t);
				net.addArc(t, po);
			}
			for (int fromIndex : solution) {
				for (int toIndex : solution) {
					if (subMatrix.isAbsolute(fromIndex, toIndex)) {
						Transition t = net.addTransition("t_" + solution + "@" + fromIndex + "->" + toIndex);
						t.setInvisible(true);
						net.addArc(poClass[fromIndex], t);
						net.addArc(t, piClass[toIndex]);
					}
				}
			}
			/*
			 * Remove semi-connected places.
			 */
			if (net.getInEdges(pi).isEmpty() || net.getOutEdges(pi).isEmpty()) {
				net.removePlace(pi);
			}
			if (net.getInEdges(po).isEmpty() || net.getOutEdges(po).isEmpty()) {
				net.removePlace(po);
			}
			for (int index : solution) {
				if (net.getInEdges(piClass[index]).isEmpty() || net.getOutEdges(piClass[index]).isEmpty()) {
					net.removePlace(piClass[index]);
				}
				if (net.getInEdges(poClass[index]).isEmpty() || net.getOutEdges(poClass[index]).isEmpty()) {
					net.removePlace(poClass[index]);
				}
			}
		}

		/*
		 * Replace by a sequence of a place and an invisible transition by an edge, to simplify the net.
		 */
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = new HashSet<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>(net.getEdges());
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
			if (!net.getEdges().contains(edge)) {
				continue;
			}
			if (edge.getSource() instanceof Transition) {
				Transition t = (Transition) edge.getSource();
				if (!t.isInvisible()) {
					continue;
				}
				Place p = (Place) edge.getTarget();
				if (net.getInEdges(t).size() == 1 && net.getOutEdges(t).size() == 1 && net.getInEdges(p).size() == 1
						&& net.getOutEdges(p).size() == 1 && net.getInEdges(t).iterator().next() != net.getOutEdges(p).iterator().next()) {
					net.addArc((Place) net.getInEdges(t).iterator().next().getSource(),
							(Transition) net.getOutEdges(p).iterator().next().getTarget());
					net.removeEdge(net.getInEdges(t).iterator().next());
					net.removeEdge(net.getOutEdges(p).iterator().next());
					net.removeEdge(edge);
					net.removeTransition(t);
					net.removePlace(p);
				}
			} else {
				Transition t = (Transition) edge.getTarget();
				if (!t.isInvisible()) {
					continue;
				}
				Place p = (Place) edge.getSource();
				if (net.getInEdges(t).size() == 1 && net.getOutEdges(t).size() == 1 && net.getInEdges(p).size() == 1
						&& net.getOutEdges(p).size() == 1 && net.getInEdges(p).iterator().next() != net.getOutEdges(t).iterator().next()) {
					net.addArc((Transition) net.getInEdges(p).iterator().next().getSource(),
							(Place) net.getOutEdges(t).iterator().next().getTarget());
					net.removeEdge(net.getInEdges(p).iterator().next());
					net.removeEdge(net.getOutEdges(t).iterator().next());
					net.removeEdge(edge);
					net.removeTransition(t);
					net.removePlace(p);
				}
			}
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(net, mi, mfs);
	}

}
