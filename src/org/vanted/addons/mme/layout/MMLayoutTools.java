/**
 * 
 */
package org.vanted.addons.mme.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.graffiti.graph.Node;
import org.vanted.addons.mme.core.MMETools;

/**
 * @author Michael Aichem
 *
 */
public class MMLayoutTools {

	private static MMLayoutTools instance;

	private MMLayoutTools() {
	}

	public static synchronized MMLayoutTools getInstance() {
		if (MMLayoutTools.instance == null) {
			MMLayoutTools.instance = new MMLayoutTools();
		}
		return MMLayoutTools.instance;
	}

	/**
	 * This method applies the barycenter heuristic for two layer crossing
	 * minimisation, choosing the best result out of five.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 */
	public void crossingMin(ArrayList<Node> layer1, ArrayList<Node> layer2) {
		ArrayList<Node> currentMinL1;
		ArrayList<Node> currentMinL2;
		ArrayList<Node> workCopyL1 = (ArrayList<Node>) layer1.clone();
		ArrayList<Node> workCopyL2 = (ArrayList<Node>) layer2.clone();
		int currentMin = numberOfCrossings(workCopyL1, workCopyL2);
		currentMinL1 = (ArrayList<Node>) workCopyL1.clone();
		currentMinL2 = (ArrayList<Node>) workCopyL2.clone();
		for (int i = 0; i < 5; i++) {
			Collections.shuffle(workCopyL1);
			Collections.shuffle(workCopyL2);
			crossingMin(workCopyL1, workCopyL2, -1);
			int noc = numberOfCrossings(workCopyL1, workCopyL2);
			if (noc < currentMin) {
				currentMin = noc;
				currentMinL1 = (ArrayList<Node>) workCopyL1.clone();
				currentMinL2 = (ArrayList<Node>) workCopyL2.clone();
			}
		}
		for (int i = 0; i < layer1.size(); i++) {
			layer1.set(i, currentMinL1.get(i));
		}
		for (int i = 0; i < layer2.size(); i++) {
			layer2.set(i, currentMinL2.get(i));
		}
	}

	/**
	 * This is a helper function for the crossing minimisation. It calculates the
	 * barycenters of the nodes and sorts the layers according to their barycenters.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 * @param numberOfCrossings
	 *            The current best result.
	 */
	private void crossingMin(ArrayList<Node> layer1, ArrayList<Node> layer2, int numberOfCrossings) {
		HashMap<Node, Double> node2barycenter = new HashMap<>();
		for (Node node : layer1) {
			node2barycenter.put(node, Double.valueOf(getBarycenter(node, layer2)));
		}
		// BubbleSort according to barycenter.
		for (int i = 0; i < layer1.size() - 1; i++) {
			int m = i;
			for (int j = i + 1; j < layer1.size(); j++) {
				if (node2barycenter.get(layer1.get(j)).doubleValue() < node2barycenter.get(layer1.get(m))
						.doubleValue()) {
					m = j;
				}
			}
			Collections.swap(layer1, i, m);
		}
		int newNumberOfCrossings = numberOfCrossings(layer1, layer2);
		if (numberOfCrossings == -1 || newNumberOfCrossings < numberOfCrossings) {
			crossingMin(layer2, layer1, newNumberOfCrossings);
		}
	}

	/**
	 * This method counts the number of crossings that occur between the two layers.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 * @return The number of crossings between the two layers.
	 */
	private int numberOfCrossings(ArrayList<Node> layer1, ArrayList<Node> layer2) {
		int res = 0;
		for (int i = 0; i < layer1.size(); i++) {
			for (int j = i + 1; j < layer1.size(); j++) {
				for (Node n1 : layer1.get(i).getNeighbors()) {
					for (Node n2 : layer1.get(j).getNeighbors()) {
						int k = layer2.indexOf(n1);
						int l = layer2.indexOf(n2);
						if (k > l) {
							res++;
						}
					}
				}
			}
		}
		return res;
	}

	/**
	 * This method computes for the given node the barycenter of the index positions
	 * of all its neighbors in the given neighbor array.
	 * 
	 * @param node
	 *            The node for which the barycenter is to be computed.
	 * @param neighborList
	 *            The list in which the neighbors are contained.
	 * @return The barycenter of the index positions of the nodes neighbors.
	 */
	private double getBarycenter(Node node, ArrayList<Node> neighborList) {
		double res = 0.0;
		for (Node neighbor : node.getNeighbors()) {
			res += ((double) neighborList.indexOf(neighbor));
		}
		res /= ((double) node.getNeighbors().size());
		return res;
	}

}
