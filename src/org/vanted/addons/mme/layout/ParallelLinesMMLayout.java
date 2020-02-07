package org.vanted.addons.mme.layout;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.core.MMETools;

/**
 * This method produces a layout of the graph that consists of two parallel
 * lines. The upper line consists of the species whereas the lower line is made
 * up of the reactions. Moreover, the barycenter heuristic for crossing
 * minimisation is applied.
 * 
 * @author Michael Aichem
 */
public class ParallelLinesMMLayout implements MMSubsystemLayout {
	
	private MMLayoutTools layoutTools;
	private MMETools tools;
	
	public ParallelLinesMMLayout() {
		layoutTools = MMLayoutTools.getInstance();
		tools = MMETools.getInstance();
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

	/* (non-Javadoc)
	 * @see org.vanted.addons.gsmmexplorer.layout.GsmmSubsystemLayout#getName()
	 */
	@Override
	public String getName() {
		return "Parallel Lines";
	}

}
