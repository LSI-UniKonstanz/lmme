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
package org.vanted.addons.lmme.layout;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme.core.LMMEConstants;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.core.LMMETools;
import org.vanted.addons.lmme.graphs.SubsystemGraph;
import org.vanted.addons.lmme.ui.LMMESubsystemViewManagement;

/**
 * A concentric circles layout for a {@link SubsystemGraph}.
 * <p>
 * All the reaction nodes are placed on an imaginary circle. The same holds for
 * the metabolite nodes, and both circles are arranged concentrically. Again,
 * nodes on both circles are then re-ordered according to a heuristic in order
 * to minimise their crossings.
 *
 * @author Michael Aichem
 */
public class ConcentricCirclesMMLayout implements MMSubsystemLayout {
	
	private MMLayoutTools layoutTools;
	private LMMETools tools;
	
	public ConcentricCirclesMMLayout() {
		layoutTools = MMLayoutTools.getInstance();
		tools = LMMETools.getInstance();
	}
	
	@Override
	public void layOutAsSubsystems(Graph graph) {
		
		ArrayList<Node> species = new ArrayList<>();
		ArrayList<Node> reactions = new ArrayList<>();
		ArrayList<Node> subsystems = new ArrayList<>();
		for (Node node : graph.getNodes()) {
			if (tools.isSpecies(node)) {
				species.add(node);
			} else if (tools.isReaction(node)) {
				reactions.add(node);
			} else if (LMMEController.getInstance().getCurrentSession().getNodeAttribute(node, LMMEConstants.NODETYPE_ATTRIBUTE_NAME)
					.equals(LMMEConstants.NODETYPE_SUBSYSTEM)) {
				subsystems.add(node);
			}
		}
		
		if (!subsystems.isEmpty()) {
			layoutTools.threeLayerCrossingMin(reactions, species, subsystems);
		} else {
			layoutTools.twoLayerCrossingMin(species, reactions);
		}
		
		int nodeSize = LMMESubsystemViewManagement.getInstance().getNodeSize();
		int subsystemNodeSize = LMMESubsystemViewManagement.getInstance().getSubsystemNodeSize();
		
		double circumferenceReactions = (Math.sqrt(2) * nodeSize + 10) * reactions.size();
		double circumferenceSpecies = (nodeSize + 10) * species.size();
		double circumferenceSubsystems = (Math.sqrt(2) * subsystemNodeSize + 10) * subsystems.size();
		
		double circumference = (double) Math.max(Math.max(circumferenceReactions, circumferenceSpecies / 1.5), circumferenceSubsystems / 2.0);
		
		int radReactions = (int) Math.round(circumference / (2 * Math.PI));
		int radSpecies = (int) Math.round(radReactions < 400 ? radReactions + 200 : radReactions * 1.5);
		int radSubsystems = (int) Math.round(radReactions < 400 ? radReactions + 400 : radReactions * 2.0);
		
		int center = radSubsystems + 100;
		for (int i = 0; i < reactions.size(); i++) {
			
			int xPos = (int) Math
					.round(center + radReactions * Math.cos((((double) i) / ((double) reactions.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + radReactions * Math.sin((((double) i) / ((double) reactions.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(reactions.get(i), xPos, yPos);
		}
		for (int i = 0; i < species.size(); i++) {
			
			int xPos = (int) Math
					.round(center + radSpecies * Math.cos((((double) i) / ((double) species.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + radSpecies * Math.sin((((double) i) / ((double) species.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(species.get(i), xPos, yPos);
		}
		for (int i = 0; i < subsystems.size(); i++) {
			
			int xPos = (int) Math
					.round(center + radSubsystems * Math.cos((((double) i) / ((double) subsystems.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + radSubsystems * Math.sin((((double) i) / ((double) subsystems.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(subsystems.get(i), xPos, yPos);
		}
	}
	
	@Override
	public String getName() {
		return "Concentric Circles";
	}
	
}
