/*******************************************************************************
 * LMME is a VANTED Add-on for the exploration of large metabolic models.
 * Copyright (C) 2020 Chair for Life Science Informatics, University of Konstanz
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.vanted.addons.lmme_dm.core;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.graphs.BaseGraph;
import org.vanted.addons.lmme_dm.graphs.OverviewGraph;

/**
 * This class represents a decomposition session in the LMME addon.
 * <p>
 * It contains all settings and generated variables that might be created during
 * a decomposition session and that need to be cleared before performing another
 * decomposition.
 * 
 * @author Michael Aichem
 */
public class LMMESession {
	
	private BaseGraph baseGraph;
	private OverviewGraph overviewGraph;
	
	public LMMESession() {
		
	}
	
	public LMMESession(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
	}
	
	public BaseGraph getBaseGraph() {
		return baseGraph;
	}
	
	public void setBaseGraph(BaseGraph baseGraph) {
		this.baseGraph = baseGraph;
		LMMEController.getInstance().getTab().updateGUI();
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
	 *           the node to put the attribute on
	 * @param attributeName
	 *           a name for the new attribute
	 * @param attributeValue
	 *           the value for the new attribute
	 */
	public void addNodeAttribute(Node node, String attributeName, String attributeValue) {
		AttributeHelper.setAttribute(node, LMMEConstants.ATTRIBUTE_PATH, attributeName, attributeValue);
	}
	
	/**
	 * This method can be used to retrieve an attribute that has been associated to
	 * a node.
	 * 
	 * @param node
	 *           the node from which the attribute is to be retrieved
	 * @param attributeName
	 *           the name of the attribute
	 * @return the respective attribute or an empty String otherwise
	 */
	public String getNodeAttribute(Node node, String attributeName) {
		return (String) AttributeHelper.getAttributeValue(node, LMMEConstants.ATTRIBUTE_PATH, attributeName, "", "");
	}
	
	public boolean isModelSet() {
		return this.baseGraph != null;
	}
	
	public boolean isOverviewGraphConstructed() {
		return this.overviewGraph != null;
	}
	
}
