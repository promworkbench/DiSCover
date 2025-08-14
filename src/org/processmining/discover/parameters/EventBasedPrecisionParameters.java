package org.processmining.discover.parameters;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;

public class EventBasedPrecisionParameters {

	private PetrinetSemantics semantics;
	private boolean showInfo;

	public EventBasedPrecisionParameters(AcceptingPetriNet apn) {
		semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(apn.getNet().getTransitions(), apn.getInitialMarking());
		showInfo = true;
	}
	
	public PetrinetSemantics getSemantics() {
		return semantics;
	}

	public void setSemantics(PetrinetSemantics semantics) {
		this.semantics = semantics;
	}

	public boolean isShowInfo() {
		return showInfo;
	}

	public void setShowInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}
}
