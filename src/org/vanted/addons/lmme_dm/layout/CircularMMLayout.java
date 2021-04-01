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
import org.vanted.addons.lmme_dm.graphs.OverviewGraph;

/**
 * A circular layout for the {@link OverviewGraph}.
 * <p>
 * All nodes are placed on a circle. A heuristic approach is applied to reduce
 * edge crossings by changing the order of the nodes on the circle.
 * 
 * @author Michael Aichem
 */
public class CircularMMLayout implements MMOverviewLayout {
	
	private MMLayoutTools layoutTools;
	
	public CircularMMLayout() {
		layoutTools = MMLayoutTools.getInstance();
	}
	
	@Override
	public void layOutAsOverview(Graph graph) {
		
		ArrayList<Node> layer1 = new ArrayList<>();
		ArrayList<Node> layer2 = new ArrayList<>();
		
		int evenOdd = 0;
		for (Node node : graph.getNodes()) {
			if ((evenOdd % 2) == 0) {
				layer1.add(node);
			} else {
				layer2.add(node);
			}
			evenOdd++;
		}
		
		layoutTools.crossingMin(layer1, layer2);
		
		// ArrayList<Node> allNodes = new ArrayList<>();
		for (int i = layer2.size() - 1; i >= 0; i--) {
			layer1.add(layer2.get(i));
		}
		
		double circumference = 150 * graph.getNodes().size();
		int radius = (int) Math.round(circumference / (2 * Math.PI));
		int center = radius + 100;
		
		for (int i = 0; i < layer1.size(); i++) {
			
			int xPos = (int) Math
					.round(center + radius * Math.cos((((double) i) / ((double) layer1.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + radius * Math.sin((((double) i) / ((double) layer1.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(layer1.get(i), xPos, yPos);
		}
	}
	
	@Override
	public String getName() {
		return "Circular";
	}
	
}
