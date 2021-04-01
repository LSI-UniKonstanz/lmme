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

import java.util.HashSet;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.decomposition.MMDecomposition;

/**
 * Maintains a subsystem graph, which is a subgraph of the {@link BaseGraph} and
 * has been found during an {@link MMDecomposition}.
 *
 * @author Michael Aichem
 */
public class SubsystemGraph {
	
	private String name;
	
	/**
	 * This list holds the species nodes that have been added to this subsystem. The
	 * list entries are references to the respective species nodes in the base graph
	 * {@link BaseGraph#workingGraph}.
	 */
	private HashSet<Node> speciesNodes;
	
	/**
	 * This list holds the reactions nodes that have been added to this subsystem.
	 * The list entries are references to the respective reaction nodes in the base
	 * graph {@link BaseGraph#workingGraph} (working graph).
	 */
	private HashSet<Node> reactionNodes;
	
	/**
	 * This list holds the edges that have been added to this subsystem. The list
	 * entries are references to the respective edges in the base graph
	 * {@link BaseGraph#workingGraph} (working graph).
	 */
	private HashSet<Edge> edges;
	
	/**
	 * Creates a new subsystem graph.
	 * 
	 * @param name
	 *           the name for the subsystem graph to be created
	 * @param speciesNodes
	 *           the species nodes that are assigned to the new subsystem graph
	 * @param reactionNodes
	 *           the reaction nodes that are assigned to the new subsystem graph
	 * @param edges
	 *           the edges that are assigned to the new subsystem graph
	 */
	public SubsystemGraph(String name, HashSet<Node> speciesNodes, HashSet<Node> reactionNodes, HashSet<Edge> edges) {
		this.name = name;
		this.speciesNodes = speciesNodes;
		this.reactionNodes = reactionNodes;
		this.edges = edges;
	}
	
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
