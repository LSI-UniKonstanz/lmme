package org.vanted.addons.gsmmexplorer.layout;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerTools;

public class ConcentricCirclesGsmmLayout implements GsmmSubsystemLayout {

	private LayoutTools layoutTools;
	private GsmmExplorerTools tools;
	
	public ConcentricCirclesGsmmLayout() {
		layoutTools = LayoutTools.getInstance();
		tools = GsmmExplorerTools.getInstance();
	}
	
	@Override
	public void layOut(Graph graph) {
		
		ArrayList<Node> species = new ArrayList<>();
		ArrayList<Node> reactions = new ArrayList<>();
		for (Node node : graph.getNodes()) {
			if (tools.isSpecies(node)) {
				species.add(node);
			} else
				if (tools.isReaction(node)) {
					reactions.add(node);
				}
		}
		
		layoutTools.crossingMin(species, reactions);
		
		double circumference = Math.max(30 * species.size(), 30 * reactions.size());
		int minRad = (int) Math.round(circumference / (2 * Math.PI));
		int maxRad = 2 * minRad;
		int center = maxRad + 100;
		for (int i = 0; i < reactions.size(); i++) {
			
			int xPos = (int) Math
					.round(center + minRad * Math.cos((((double) i) / ((double) reactions.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + minRad * Math.sin((((double) i) / ((double) reactions.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(reactions.get(i), xPos, yPos);
		}
		for (int i = 0; i < species.size(); i++) {
			
			int xPos = (int) Math
					.round(center + maxRad * Math.cos((((double) i) / ((double) species.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + maxRad * Math.sin((((double) i) / ((double) species.size())) * 2 * Math.PI));;
			AttributeHelper.setPosition(species.get(i), xPos, yPos);
		}
		
	}

	/**
	 * 
	 */
	public String getName() {
		return "Concentric Circles";
	}

}
