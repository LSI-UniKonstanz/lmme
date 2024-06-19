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
 * A parallel lines layout method for a {@link SubsystemGraph}.
 * <p>
 * This method produces a layout of the graph that consists of two parallel
 * lines. The upper line consists of the species whereas the lower line is made
 * up of the reactions. Moreover, the barycenter heuristic for crossing
 * minimisation is applied.
 * 
 * @author Michael Aichem
 */
public class ParallelLinesMMLayout implements MMSubsystemLayout {
	
	private MMLayoutTools layoutTools;
	private LMMETools tools;
	
	public ParallelLinesMMLayout() {
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
		
		int ySpanSpecies = (nodeSize + 10) * species.size();
		int ySpanReactions = (nodeSize + 10) * reactions.size();
		int ySpanSubsystems = (subsystemNodeSize + 10) * subsystems.size();
		
		int ySpan = Math.max(Math.max(ySpanSpecies, ySpanReactions), ySpanSubsystems);
		
		int yStep = ySpan / reactions.size();
		int yPos = yStep / 2;
		for (int i = 0; i < reactions.size(); i++) {
			AttributeHelper.setPosition(reactions.get(i), 100, yPos);
			yPos += yStep;
		}
		
		yStep = ySpan / species.size();
		yPos = yStep / 2;
		for (int i = 0; i < species.size(); i++) {
			AttributeHelper.setPosition(species.get(i), 500, yPos);
			yPos += yStep;
		}
		
		if (!subsystems.isEmpty()) {
			yStep = ySpan / subsystems.size();
			yPos = yStep / 2;
			for (int i = 0; i < subsystems.size(); i++) {
				AttributeHelper.setPosition(subsystems.get(i), 1000, yPos);
				yPos += yStep;
			}
		}
		
	}
	
	@Override
	public String getName() {
		return "Parallel Lines";
	}
	
}
