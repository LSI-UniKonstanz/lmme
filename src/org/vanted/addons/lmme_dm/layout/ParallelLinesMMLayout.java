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
package org.vanted.addons.lmme_dm.layout;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;

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
		for (Node node : graph.getNodes()) {
			if (tools.isSpecies(node)) {
				species.add(node);
			} else if (tools.isReaction(node)) {
				reactions.add(node);
			}
		}
		
		layoutTools.crossingMin(species, reactions);
		
		int xSpan = Math.max(60 * species.size(), 60 * reactions.size());
		
		int xPos = 0;
		int xStep = xSpan / species.size();
		for (int i = 0; i < species.size(); i++) {
			AttributeHelper.setPosition(species.get(i), xPos, 100);
			xPos += xStep;
		}
		
		xPos = 0;
		xStep = xSpan / reactions.size();
		for (int i = 0; i < reactions.size(); i++) {
			AttributeHelper.setPosition(reactions.get(i), xPos, 700);
			xPos += xStep;
		}
		
	}
	
	@Override
	public String getName() {
		return "Bipartite Graph";
	}
	
}
