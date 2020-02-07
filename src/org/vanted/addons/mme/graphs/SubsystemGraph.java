package org.vanted.addons.mme.graphs;

import java.util.ArrayList;
import java.util.HashSet;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

public class SubsystemGraph {

	// private Graph graph;

	private String name;

	/**
	 * This list holds the species nodes that have been added to this subsystem. The
	 * list entries are references to the respective species nodes in the base graph
	 * ({@link BaseGraph.graph}).
	 */
	private HashSet<Node> speciesNodes;

	/**
	 * This list holds the reactions nodes that have been added to this subsystem.
	 * The list entries are references to the respective reaction nodes in the base
	 * graph ({@link BaseGraph.graph}).
	 */
	private HashSet<Node> reactionNodes;
	
	/**
	 * This list holds the edges that have been added to this subsystem.
	 * The list entries are references to the respective edges in the base
	 * graph ({@link BaseGraph.graph}).
	 */
	private HashSet<Edge> edges;
	
	
	/**
	 * Creates a new subsystem graph
	 * @param name
	 * @param speciesNodes
	 * @param reactionNodes
	 */
	public SubsystemGraph(String name, HashSet<Node> speciesNodes, HashSet<Node> reactionNodes, HashSet<Edge> edges) {
		this.name = name;
		this.speciesNodes = speciesNodes;
		this.reactionNodes = reactionNodes;
		this.edges = edges;
	}

	// public Graph getGraph() {
	// return graph;
	// }

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public HashSet<Node> getSpeciesNodes() {
		return speciesNodes;
	}

	public HashSet<Node> getReactionNodes() {
		return reactionNodes;
	}
	
	/**
	 * @return the edges
	 */
	public HashSet<Edge> getEdges() {
		return edges;
	}

	public int getNumberOfSpecies() {
		return speciesNodes.size();
	}

	public int getNumberOfReactions() {
		return reactionNodes.size();
	}
	
	public void addSpecies(Node speciesNode) {
		this.speciesNodes.add(speciesNode);
	}
	
	public void addReaction(Node reactionNode) {
		this.reactionNodes.add(reactionNode);
	}
	
	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

}
