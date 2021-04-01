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
package org.vanted.addons.lmme_dm.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEConstants;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMETools;

/**
 * Maintains the graph that is represented by the underlying model.
 * <p>
 * Within this class, there is a strict separation of the {@code originalGraph} and the {@code workingGraph} and their corresponding species and reactions.
 * <p>
 * Within the working graph, the cloning is performed, while the {@code originalGraph} remains the same as in the beginning. In most cases, the right way is to
 * access the {@code workingGraph}. The only reason for the {@code originalGraph} to be also stored is that this is the only one that has the SBML file
 * associated. It needs therefore only to be accessed when the SBML file is to be read - or when a session is to be reseted and a new working copy is to be
 * produced.
 *
 * @author Michael Aichem
 */
public class BaseGraph {
	
	/**
	 * The original graph that has associated the SBML file.
	 */
	private Graph originalGraph;
	
	/**
	 * The working copy of the {@link #originalGraph}.
	 */
	private Graph workingGraph;
	
	private ArrayList<Node> speciesNodes = new ArrayList<>();
	private ArrayList<Node> reactionNodes = new ArrayList<>();
	private ArrayList<Node> originalSpeciesNodes = new ArrayList<>();
	private ArrayList<Node> originalReactionNodes = new ArrayList<>();
	
	private HashMap<Node, Node> working2originalNodes = new HashMap<>();
	private HashMap<Node, ArrayList<Node>> original2workingNodes = new HashMap<>();
	
	private int[] degreeSpecies;
	
	private ArrayList<String> processedNotes = new ArrayList<>();
	private String[] availableNotes;
	
	/**
	 * Constructs a {@code BaseGraph}.
	 * <p>
	 * This constructor also computes the lists of species and reactions as well as
	 * the information regarding the degree distribution of the species that are
	 * contained in the base graph.
	 * 
	 * @param graph
	 *           The graph object of the model that is to be set as base graph
	 */
	public BaseGraph(Graph graph) {
		
		this.originalGraph = graph;
		
		for (Node node : originalGraph.getNodes()) {
			if (LMMETools.getInstance().isSpecies(node)) {
				originalSpeciesNodes.add(node);
			} else if (LMMETools.getInstance().isReaction(node)) {
				originalReactionNodes.add(node);
			}
		}
		
		this.workingGraph = new AdjListGraph();
		
		for (Node node : originalGraph.getNodes()) {
			Node newNode = workingGraph.addNodeCopy(node);
			ArrayList<Node> workingNodesList = new ArrayList<>();
			workingNodesList.add(newNode);
			original2workingNodes.put(node, workingNodesList);
			working2originalNodes.put(newNode, node);
		}
		for (Edge edge : originalGraph.getEdges()) {
			Node source = original2workingNodes.get(edge.getSource()).get(0);
			Node target = original2workingNodes.get(edge.getTarget()).get(0);
			workingGraph.addEdgeCopy(edge, source, target);
		}
		
		updateLists();
		
		HashMap<Integer, Integer> degreeCount = new HashMap<>();
		for (Node speciesNode : speciesNodes) {
			Integer deg = Integer.valueOf(speciesNode.getDegree());
			if (degreeCount.containsKey(deg)) {
				degreeCount.put(deg, Integer.valueOf(degreeCount.get(deg).intValue() + 1));
			} else {
				degreeCount.put(deg, Integer.valueOf(1));
			}
		}
		
		int max = 0;
		for (Integer deg : degreeCount.keySet()) {
			if (deg.intValue() > max) {
				max = deg.intValue();
			}
		}
		
		degreeSpecies = new int[max + 1];
		degreeSpecies[max] = degreeCount.get(Integer.valueOf(max)).intValue();
		// TODO -1 ok?
		for (int i = max - 1; i >= 0; i--) {
			if (degreeCount.containsKey(Integer.valueOf(i))) {
				degreeSpecies[i] = degreeSpecies[i + 1] + degreeCount.get(Integer.valueOf(i)).intValue();
			} else {
				degreeSpecies[i] = degreeSpecies[i + 1];
			}
		}
		
		HashSet<String> availableNotesHS = LMMETools.getInstance().findNotes(this.originalGraph);
		this.availableNotes = new String[availableNotesHS.size()];
		int index = 0;
		for (String str : availableNotesHS) {
			this.availableNotes[index++] = str;
		}
		
		LMMEController.getInstance().getTab().setBaseGraphInfo(this.getOriginalGraph().getName(),
				this.getNumberOfSpecies(), this.getNumberOfReactions());
		
	}
	
	/**
	 * Clones the species from the given list in the {@link #workingGraph}.
	 * <p>
	 * Cloning in this case means to replace the node by a copy of itself for every
	 * edge it is incident to. In addition, these edges are also copied such that
	 * the resulting copied nodes all have a degree of 1.
	 * 
	 * @param clonableSpecies
	 *           A list of species from the originalGraph that are to
	 *           be cloned in the working copy.
	 */
	public void cloneSpecies(List<Node> clonableSpecies) {
		for (Node nodeToClone : clonableSpecies) {
			Node workingNode = original2workingNodes.get(nodeToClone).get(0);
			original2workingNodes.get(nodeToClone).clear();
			for (Edge edge : workingNode.getEdges()) {
				Node newNode = workingGraph.addNodeCopy(workingNode);
				AttributeHelper.setAttribute(newNode, LMMEConstants.ATTRIBUTE_PATH, "isClone", true);
				original2workingNodes.get(nodeToClone).add(newNode);
				working2originalNodes.put(newNode, nodeToClone);
				Node source, target;
				if (edge.getSource() == workingNode) {
					source = newNode;
				} else {
					source = edge.getSource();
				}
				if (edge.getTarget() == workingNode) {
					target = newNode;
				} else {
					target = edge.getTarget();
				}
				workingGraph.addEdgeCopy(edge, source, target);
			}
			workingGraph.deleteNode(workingNode);
			working2originalNodes.remove(workingNode);
		}
		LMMEController.getInstance().getCurrentSession().getBaseGraph().updateLists();
	}
	
	public Graph getGraph() {
		return workingGraph;
	}
	
	public Graph getOriginalGraph() {
		return originalGraph;
	}
	
	/**
	 * Gets the node in the {@link #originalGraph} that corresponds to the given node within the {@link #workingGraph}.
	 * 
	 * @param workingNode
	 *           the requested node within the {@link #workingGraph}
	 * @return the node in the {@link #originalGraph} that corresponds to the given node
	 */
	public Node getOriginalNode(Node workingNode) {
		return working2originalNodes.get(workingNode);
	}
	
	/**
	 * Gets the nodes in the {@link #workingGraph} that correspond to the given node within the {@link #originalGraph}.
	 * 
	 * @param originalNode
	 *           the requested node within the {@link #originalGraph}
	 * @return the nodes in the {@link #workingGraph} that correspond to the given node
	 */
	public ArrayList<Node> getWorkingNodes(Node originalNode) {
		return original2workingNodes.get(originalNode);
	}
	
	/**
	 * Returns the names of the notes that are available in the SBML file underlying this graph.
	 * 
	 * @return the availableNotes
	 */
	public String[] getAvailableNotes() {
		return availableNotes;
	}
	
	/**
	 * Returns the names of the notes from the SBML file underlying this graph, that have yet been processed.
	 * 
	 * @return the notes that have yet been processed
	 */
	public ArrayList<String> getProcessedNotes() {
		return processedNotes;
	}
	
	/**
	 * Returns the number of species in the {@link #originalGraph} having at least the specified degree.
	 * 
	 * @param degree
	 *           the degree threshold
	 * @return the number of species having at least the specified degree
	 */
	public int getNumberOfSpeciesWithDegreeAtLeast(int degree) {
		if (degreeSpecies.length >= degree + 1)
			return degreeSpecies[degree];
		else
			return 0;
	}
	
	/**
	 * Gets a list of species from the original graph that have at least the specified degree within the original graph.
	 * 
	 * @param degree
	 *           the degree threshold
	 * @return list of species from the original graph that have at least the specified degree
	 */
	public ArrayList<Node> getOriginalSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getOriginalGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (LMMETools.getInstance().isSpecies(node))) {
				res.add(node);
			}
		}
		return res;
	}
	
	/**
	 * Gets a list of species from the working graph that have at least the specified degree within the working graph.
	 * 
	 * @param degree
	 *           the degree threshold
	 * @return list of species from the working graph that have at least the specified degree
	 */
	public ArrayList<Node> getSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (LMMETools.getInstance().isSpecies(node))) {
				res.add(node);
			}
		}
		return res;
	}
	
	/**
	 * Gets the maximum degree occurring in the original graph.
	 * 
	 * @return the maximum degree occurring in the original graph
	 */
	public int getMaximumDegree() {
		return degreeSpecies.length - 1;
	}
	
	/**
	 * Gets the number of species in the working graph.
	 * 
	 * @return the number of species in the working graph
	 */
	public int getNumberOfSpecies() {
		return speciesNodes.size();
	}
	
	/**
	 * Gets the number of reactions in the working graph.
	 * 
	 * @return the number of reactions in the working graph
	 */
	public int getNumberOfReactions() {
		return reactionNodes.size();
	}
	
	public ArrayList<Node> getSpeciesNodes() {
		return speciesNodes;
	}
	
	public ArrayList<Node> getReactionNodes() {
		return reactionNodes;
	}
	
	public ArrayList<Node> getOriginalSpeciesNodes() {
		return originalSpeciesNodes;
	}
	
	public ArrayList<Node> getOriginalReactionNodes() {
		return originalReactionNodes;
	}
	
	/**
	 * Updates the species and reaction lists.
	 * <p>
	 * Needs to be called whenever a species or reaction has been deleted or added.
	 */
	public void updateLists() {
		this.speciesNodes.clear();
		this.reactionNodes.clear();
		for (Node node : this.getGraph().getNodes()) {
			if (LMMETools.getInstance().isSpecies(node)) {
				speciesNodes.add(node);
			} else if (LMMETools.getInstance().isReaction(node)) {
				reactionNodes.add(node);
			}
		}
	}
	
}
