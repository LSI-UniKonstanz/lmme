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
import java.util.Collections;
import java.util.HashMap;

import org.graffiti.graph.Node;

/**
 * A collection of tools that may be used by the included layout algorithms.
 * 
 * @author Michael Aichem
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
	 * Applies the barycenter heuristic for two layer crossing
	 * minimisation, choosing the best result out of five.
	 * 
	 * @param layer1
	 *           the first layer
	 * @param layer2
	 *           the second layer
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
	 * A helper function for the crossing minimisation. It calculates the
	 * barycenters of the nodes and sorts the layers according to their barycenters.
	 * 
	 * @param layer1
	 *           first layer
	 * @param layer2
	 *           second layer
	 * @param numberOfCrossings
	 *           the current best result
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
	 * Counts the number of crossings that occur between two layers.
	 * 
	 * @param layer1
	 *           first layer
	 * @param layer2
	 *           second layer
	 * @return the number of crossings between the two layers
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
	 * Computes for the given node the barycenter of the index positions
	 * of all its neighbors in the given neighbor array.
	 * 
	 * @param node
	 *           the node for which the barycenter is to be computed
	 * @param neighborList
	 *           the list of neighbors
	 * @return the barycenter of the index positions of the node's neighbors
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
