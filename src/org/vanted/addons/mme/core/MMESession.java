package org.vanted.addons.mme.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.decomposition.MMDecomposition;
import org.vanted.addons.mme.decomposition.MMDecompositionAlgorithm;
import org.vanted.addons.mme.graphs.BaseGraph;
import org.vanted.addons.mme.graphs.OverviewGraph;
import org.vanted.addons.mme.graphs.SubsystemGraph;
import org.vanted.addons.mme.layout.MMOverviewLayout;
import org.vanted.addons.mme.layout.MMSubsystemLayout;

/**
 * This class represents an exploration session in the GSMM exploration addon.
 * It contains all settings and generated variables that might be created during
 * a session and that need to be cleared before starting another session.
 * 
 * @author Michael Aichem
 *
 */
public class MMESession {

	private BaseGraph baseGraph;
	private OverviewGraph overviewGraph;

//	private HashMap<Node, HashMap<String, String>> nodeAttributeMap = new HashMap<>();

	// private GsmmDecompositionAlgorithm decompositionAlgorithm;
	//
	// private GsmmOverviewLayout overviewLayout;
	// private GsmmSubsystemLayout subsystemLayout;

	public MMESession() {
		
	}

	public MMESession(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
	}

	public BaseGraph getBaseGraph() {
		return baseGraph;
	}

	public void setBaseGraph(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
		MMEController.getInstance().getTab().updateGUI();
	}

	public OverviewGraph getOverviewGraph() {
		return overviewGraph;
	}

	public void setOverviewGraph(OverviewGraph overviewGraph) {
		this.overviewGraph = overviewGraph;
	}

	/**
	 * This method is used to associate an attribute to a node.
	 * 
	 * @param node
	 * @param attributeName
	 * @param attributeValue
	 */
	public void addNodeAttribute(Node node, String attributeName, String attributeValue) {
		AttributeHelper.setAttribute(node, MMEConstants.ATTRIBUTE_PATH, attributeName, attributeValue);
	}

	/**
	 * This method can be used to retrieve an attribute that has been associated to
	 * a node.
	 * 
	 * @param node
	 * @param attributeName
	 * @return the respective attribute or an empty String otherwise
	 */
	public String getNodeAttribute(Node node, String attributeName) {
		return (String) AttributeHelper.getAttributeValue(node, MMEConstants.ATTRIBUTE_PATH, attributeName, "", "");
	}

	public boolean isModelSet() {
		return this.baseGraph != null;
	}

	public boolean isOverviewGraphConstructed() {
		return this.overviewGraph != null;
	}

}
