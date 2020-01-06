package org.vanted.addons.gsmmexplorer.graphs;

import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerController;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerTools;

public class BaseGraph {

	private Graph originalGraph;
	private Graph workingGraph;

	private ArrayList<Node> speciesNodes = new ArrayList<>();
	private ArrayList<Node> reactionNodes = new ArrayList<>();
	private ArrayList<Node> originalSpeciesNodes = new ArrayList<>();
	private ArrayList<Node> originalReactionNodes = new ArrayList<>();
	
	private HashMap<Node, Node> working2originalNodes = new HashMap<>();
	private HashMap<Node, Node> original2workingNodes = new HashMap<>();

	private int[] degreeSpecies;

	private ArrayList<String> notes = new ArrayList<>();

	/**
	 * This constructor also computes the lists of species and reactions as well as
	 * the information regarding the degree distribution of the species that are
	 * contained in the base graph.
	 * 
	 * @param graph
	 *            The graph object to be set as base graph
	 */
	public BaseGraph(Graph graph) {

		this.originalGraph = graph;

		for (Node node : originalGraph.getNodes()) {
			if (GsmmExplorerTools.getInstance().isSpecies(node)) {
				originalSpeciesNodes.add(node);
			} else if (GsmmExplorerTools.getInstance().isReaction(node)) {
				originalReactionNodes.add(node);
			}
		}
		
		this.workingGraph = new AdjListGraph();
		
		for (Node node : originalGraph.getNodes()) {
			Node newNode = workingGraph.addNodeCopy(node);
			original2workingNodes.put(node, newNode);
			working2originalNodes.put(newNode, node);
		}
		for (Edge edge : originalGraph.getEdges()) {
			Node source = original2workingNodes.get(edge.getSource());
			Node target = original2workingNodes.get(edge.getTarget());
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

		GsmmExplorerController.getInstance().getTab().setBaseGraphInfo(this.getOriginalGraph().getName(),
				this.getNumberOfSpecies(), this.getNumberOfReactions());

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
	
	public Node getWorkingNode(Node originalNode) {
		return original2workingNodes.get(originalNode);
	}

	/**
	 * @return the notes
	 */
	public ArrayList<String> getNotes() {
		return notes;
	}

	public int getNumberOfSpeciesWithDegreeAtLeast(int degree) {
		return degreeSpecies[degree];
	}

	public ArrayList<Node> getOriginalSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getOriginalGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (GsmmExplorerTools.getInstance().isSpecies(node))) {
				res.add(node);
			}
		}
		return res;
	}
	
	public ArrayList<Node> getSpeciesWithDegreeAtLeast(int degree) {
		ArrayList<Node> res = new ArrayList<>();
		for (Node node : getGraph().getNodes()) {
			if ((node.getDegree() >= degree) && (GsmmExplorerTools.getInstance().isSpecies(node))) {
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
		return speciesNodes;
	}

	public ArrayList<Node> getOriginalReactionNodes() {
		return reactionNodes;
	}

	public void updateLists() {
		this.speciesNodes.clear();
		this.reactionNodes.clear();
		for (Node node : this.getGraph().getNodes()) {
			if (GsmmExplorerTools.getInstance().isSpecies(node)) {
				speciesNodes.add(node);
			} else if (GsmmExplorerTools.getInstance().isReaction(node)) {
				reactionNodes.add(node);
			}
		}
	}

}
