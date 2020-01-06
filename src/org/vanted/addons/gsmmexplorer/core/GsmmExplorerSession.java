package org.vanted.addons.gsmmexplorer.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.addons.gsmmexplorer.decomposition.GsmmDecomposition;
import org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm;
import org.vanted.addons.gsmmexplorer.graphs.BaseGraph;
import org.vanted.addons.gsmmexplorer.graphs.OverviewGraph;
import org.vanted.addons.gsmmexplorer.graphs.SubsystemGraph;
import org.vanted.addons.gsmmexplorer.layout.GsmmOverviewLayout;
import org.vanted.addons.gsmmexplorer.layout.GsmmSubsystemLayout;

/**
 * This class represents an exploration session in the GSMM exploration addon.
 * It contains all settings and generated variables that might be created during
 * a session and that need to be cleared before starting another session.
 * 
 * @author Michael Aichem
 *
 */
public class GsmmExplorerSession {

	private BaseGraph baseGraph;
	private OverviewGraph overviewGraph;

//	private HashMap<Node, HashMap<String, String>> nodeAttributeMap = new HashMap<>();

	// private GsmmDecompositionAlgorithm decompositionAlgorithm;
	//
	// private GsmmOverviewLayout overviewLayout;
	// private GsmmSubsystemLayout subsystemLayout;

	public GsmmExplorerSession() {

	}

	public GsmmExplorerSession(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
	}

	public BaseGraph getBaseGraph() {
		return baseGraph;
	}

	public void setBaseGraph(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
		GsmmExplorerController.getInstance().getTab().updateGUI();
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
		AttributeHelper.setAttribute(node, GsmmExplorerConstants.ATTRIBUTE_PATH, attributeName, attributeValue);
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
		return (String) AttributeHelper.getAttributeValue(node, GsmmExplorerConstants.ATTRIBUTE_PATH, attributeName, "", "");
	}

	public boolean isModelSet() {
		return this.baseGraph != null;
	}

	public boolean isOverviewGraphConstructed() {
		return this.overviewGraph != null;
	}

}
