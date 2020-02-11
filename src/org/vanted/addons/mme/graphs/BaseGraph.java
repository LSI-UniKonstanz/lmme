package org.vanted.addons.mme.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.core.MMEController;
import org.vanted.addons.mme.core.MMETools;

public class BaseGraph {

	private Graph originalGraph;
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
	 * This constructor also computes the lists of species and reactions as well as
	 * the information regarding the degree distribution of the species that are
	 * contained in the base graph.
	 * 
	 * @param graph The graph object to be set as base graph
	 */
	public BaseGraph(Graph graph) {

		this.originalGraph = graph;

		for (Node node : originalGraph.getNodes()) {
			if (MMETools.getInstance().isSpecies(node)) {
				originalSpeciesNodes.add(node);
			} else if (MMETools.getInstance().isReaction(node)) {
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

		HashSet<String> availableNotesHS = MMETools.getInstance().findNotes(this.originalGraph);
		this.availableNotes = new String[availableNotesHS.size()];
		int index = 0;
		for (String str : availableNotesHS) {
			this.availableNotes[index++] = str;
		}

		MMEController.getInstance().getTab().setBaseGraphInfo(this.getOriginalGraph().getName(),
				this.getNumberOfSpecies(), this.getNumberOfReactions());

	}

	/**
	 * This method clones the species from the given list in the workingGraph.
	 * Cloning in this case means to replace the node by a copy of itself for every
	 * edge it is incident to. In addition, these edges are also copied such that
	 * the resulting copied nodes all have a degree of 1.
	 * 
	 * @param clonableSpecies A list of species from the originalGraph that are to
	 *                        be cloned in the working copy.
	 * @param degreeThreshold
	 */
	public void cloneSpecies(List<Node> clonableSpecies) {
		for (Node nodeToClone : clonableSpecies) {
			Node workingNode = original2workingNodes.get(nodeToClone).get(0);
			original2workingNodes.get(nodeToClone).clear();
			for (Edge edge : workingNode.getEdges()) {
				Node newNode = workingGraph.addNodeCopy(workingNode);
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
		MMEController.getInstance().getCurrentSession().getBaseGraph().updateLists();
	}

	public Graph getGraph() {
		return workingGraph;
	}

	/**
	 * @return the originalGraph
	 */
	public Graph getOriginalGraph() {
		return originalGraph;
	}

	public Node getOriginalNode(Node workingNode) {
		return working2originalNodes.get(workingNode);
	}

	public ArrayList<Node> getWorkingNodes(Node originalNode) {
		return original2workingNodes.get(originalNode);
	}

	/**
	 * @return the availableNotes
	 */
	public String[] getAvailableNotes() {
		return availableNotes;
	}

	/**
	 * @return the notes
	 */
	public ArrayList<String> getProcessedNotes() {
		return processedNotes;
	}

	public int getNumberOfSpeciesWithDegreeAtLeast(int degree) {
		return degreeSpecies[degree];
	}

	public ArrayList<Node> getOriginalSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getOriginalGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (MMETools.getInstance().isSpecies(node))) {
				res.add(node);
			}
		}
		return res;
	}

	public ArrayList<Node> getSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (MMETools.getInstance().isSpecies(node))) {
				res.add(node);
			}
		}
		return res;
	}

	public int getMaximumDegree() {
		return degreeSpecies.length - 1;
	}

	// TODO: Distinguish between before/after cloning?
	public int getNumberOfSpecies() {
		return speciesNodes.size();
	}

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

	public void updateLists() {
		this.speciesNodes.clear();
		this.reactionNodes.clear();
		for (Node node : this.getGraph().getNodes()) {
			if (MMETools.getInstance().isSpecies(node)) {
				speciesNodes.add(node);
			} else if (MMETools.getInstance().isReaction(node)) {
				reactionNodes.add(node);
			}
		}
	}

}
