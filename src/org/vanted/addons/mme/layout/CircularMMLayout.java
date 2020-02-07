/**
 * 
 */
package org.vanted.addons.mme.layout;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

/**
 * @author Michael Aichem
 *
 */
public class CircularMMLayout implements MMOverviewLayout {

	private MMLayoutTools layoutTools;

	public CircularMMLayout() {
		layoutTools = MMLayoutTools.getInstance();
	}

	/**
	 * 
	 */
	public void layOut(Graph graph) {

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

		ArrayList<Node> allNodes = new ArrayList<>();
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
					.round(center + radius * Math.sin((((double) i) / ((double) layer1.size())) * 2 * Math.PI));
			;
			AttributeHelper.setPosition(layer1.get(i), xPos, yPos);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanted.addons.gsmmexplorer.layout.GsmmOverviewLayout#getName()
	 */
	@Override
	public String getName() {
		return "Circular";
	}

}
