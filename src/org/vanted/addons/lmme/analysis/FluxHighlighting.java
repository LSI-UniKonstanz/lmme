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
package org.vanted.addons.lmme.analysis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme.core.LMMEConstants;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.OverviewGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;
import org.vanted.addons.lmme.ui.LMMESubsystemViewManagement;
import org.vanted.addons.lmme.ui.LMMEViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * This class implements a series of highlighting options for flux data.
 * <p>
 * Given a path to a file containing a mapping of reaction id's to flux values,
 * the values can be mapped to colors or sizes in either the overview graph or
 * the subsystem graph. Within the overview graph, either the absolute or the
 * relative amount of non-zero fluxes can be used for the mapping.
 * 
 * @author Michael Aichem
 */
public class FluxHighlighting {
	
	private double minValue;
	private double maxValue;
	HashMap<Node, Double> nodeHighlightMap;
	HashMap<SubsystemGraph, Integer> nodeCount;
	int maxCount;
	
	public FluxHighlighting(String filePath) {
		
		minValue = Double.MAX_VALUE;
		maxValue = Double.MIN_VALUE;
		nodeHighlightMap = new HashMap<>();
		nodeCount = new HashMap<>();
		maxCount = 0;
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			
			String nameValuePair = br.readLine();
			while (nameValuePair != null) {
				String[] splitStr = nameValuePair.split(Pattern.quote(","));
				if (splitStr.length > 1) {
					double currentValue = Double.parseDouble(splitStr[splitStr.length - 1]);
					if (currentValue > maxValue) {
						maxValue = currentValue;
					}
					if (currentValue < minValue) {
						minValue = currentValue;
					}
					String reactionID = splitStr[0];
					for (int i = 1; i < splitStr.length - 1; i++) {
						reactionID += ",";
						reactionID += splitStr[i];
					}
					for (Node reactionNode : baseGraph.getReactionNodes()) {
						if (AttributeHelper
								.getAttributeValue(reactionNode, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", "")
								.equals(reactionID)) {
							if (currentValue != 0) {
								nodeHighlightMap.put(reactionNode, currentValue);
							}
							LMMEController.getInstance().getCurrentSession().addNodeAttribute(reactionNode, LMMEConstants.FLUX_ATTRIBUTE,
									Double.toString(currentValue));
						}
					}
				}
				nameValuePair = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void countOccurences() {
		OverviewGraph og = LMMEController.getInstance().getCurrentSession().getOverviewGraph();
		for (SubsystemGraph subsystem : og.getDecomposition().getSubsystems()) {
			int count = 0;
			for (Node n1 : subsystem.getReactionNodes()) {
				for (Node n2 : nodeHighlightMap.keySet()) {
					if (AttributeHelper.getAttributeValue(n1, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", "")
							.equals(AttributeHelper.getAttributeValue(n2, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", ""))) {
						count += 1;
					}
				}
			}
			if (count > maxCount) {
				maxCount = count;
			}
			nodeCount.put(subsystem, Integer.valueOf(count));
		}
	}
	
	public void highlightColorInOverview(boolean useAbsoluteCount) {
		
		OverviewGraph og = LMMEController.getInstance().getCurrentSession().getOverviewGraph();
		countOccurences();
		
		for (SubsystemGraph subsystem : og.getDecomposition().getSubsystems()) {
			AttributeHelper.setFillColor(og.getNodeOfSubsystem(subsystem), Color.WHITE);
			if (nodeCount.get(subsystem).intValue() > 0) {
				int frac;
				if (useAbsoluteCount) {
					frac = (int) Math.round((1.0 - ((double) nodeCount.get(subsystem)) / ((double) maxCount)) * 200.0);
				} else {
					frac = (int) Math.round((1.0 - ((double) nodeCount.get(subsystem)) / ((double) subsystem.getReactionNodes().size())) * 200.0);
				}
				Color c = new Color(frac, frac, 255);
				AttributeHelper.setFillColor(og.getNodeOfSubsystem(subsystem), c);
			}
		}
	}
	
	public void highlightColorInSubsystemView() {
		
		Graph csg = LMMEViewManagement.getInstance().getSubsystemFrame().getView().getGraph();
		for (Node n1 : csg.getNodes()) {
			AttributeHelper.setFillColor(n1, Color.WHITE);
			for (Node n2 : nodeHighlightMap.keySet()) {
				if (AttributeHelper.getAttributeValue(n1, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", "")
						.equals(AttributeHelper.getAttributeValue(n2, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", ""))) {
					double currentVal = nodeHighlightMap.get(n2).doubleValue();
					if (currentVal < 0) {
						int frac = (int) Math.round((1.0 - currentVal / minValue) * 200.0);
						Color c = new Color(frac, frac, 255);
						AttributeHelper.setFillColor(n1, c);
					} else {
						int frac = (int) Math.round((1.0 - currentVal / maxValue) * 200.0);
						Color c = new Color(255, frac, frac);
						AttributeHelper.setFillColor(n1, c);
					}
				}
			}
		}
		
	}
	
	public void highlightSizeInOverview(boolean useAbsoluteCount) {
		
		OverviewGraph og = LMMEController.getInstance().getCurrentSession().getOverviewGraph();
		countOccurences();
		
		for (SubsystemGraph subsystem : og.getDecomposition().getSubsystems()) {
			AttributeHelper.setSize(og.getNodeOfSubsystem(subsystem), og.getNodeSizeSubsystem(), og.getNodeSizeSubsystem());
			if (nodeCount.get(subsystem).intValue() > 0) {
				double frac;
				if (useAbsoluteCount) {
					frac = ((double) nodeCount.get(subsystem)) / ((double) maxCount);
				} else {
					frac = ((double) nodeCount.get(subsystem)) / ((double) subsystem.getReactionNodes().size());
				}
				int size = (int) Math.round(((double) og.getNodeSizeSubsystem()) * (0.3 * frac + 1.2));
				AttributeHelper.setSize(og.getNodeOfSubsystem(subsystem), size, size);
			}
		}
	}
	
	public void highlightSizeInSubsystemView() {
		
		Graph csg = LMMEViewManagement.getInstance().getSubsystemFrame().getView().getGraph();
		int nodeSize = LMMESubsystemViewManagement.getInstance().getNodeSize();
		for (Node n1 : csg.getNodes()) {
			AttributeHelper.setSize(n1, nodeSize, nodeSize);
			for (Node n2 : nodeHighlightMap.keySet()) {
				if (AttributeHelper.getAttributeValue(n1, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", "")
						.equals(AttributeHelper.getAttributeValue(n2, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", ""))) {
					double currentVal = nodeHighlightMap.get(n2).doubleValue();
					if (currentVal < 0) {
						double frac = currentVal / minValue;
						int size = (int) Math.round(((double) nodeSize) * (0.75 - 0.5 * frac));
						AttributeHelper.setSize(n1, size, size);
					} else {
						double frac = currentVal / maxValue;
						int size = (int) Math.round(((double) nodeSize) * (1.0 * frac + 1.25));
						AttributeHelper.setSize(n1, size, size);
					}
				}
			}
		}
		
	}
	
}
