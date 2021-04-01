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
package org.vanted.addons.lmme_dm.decomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.AttributeHelper;
import org.FolderPanel;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEConstants;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMESession;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.graphs.BaseGraph;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;
import org.vanted.addons.lmme_dm.ui.LMMETab;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * Abstract class representing a decomposition method.
 * <p>
 * This class needs to be extended whenever a new decomposition method is to be implemented.
 * 
 * @author Michael Aichem
 */
public abstract class MMDecompositionAlgorithm {
	
	/**
	 * This method contains the specific procedure of the decomposition.
	 * <p>
	 * It will be placed within the broader procedure in the {@link #run(boolean)} method.
	 * 
	 * @param alreadyClassifiedNodes
	 *           a list of nodes that have been classified already
	 * @return a list of {@link SubsystemGraph}s constituting a decomposition
	 */
	protected abstract ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes);
	
	/**
	 * The overall method that creates a decomposition.
	 * <p>
	 * Within this method, cloning and specific decomposition are performed, and, if appropriate, the transporter and
	 * default subsystems are computed and added.
	 * 
	 * @param addTransporterSubsystem
	 *           whether to add the transporter subsystem
	 * @return an {@link MMDecomposition} object representing the constructed decomposition
	 */
	public MMDecomposition run(boolean addTransporterSubsystem) {
		
		LMMESession currentSession = LMMEController.getInstance().getCurrentSession();
		LMMETab tab = LMMEController.getInstance().getTab();
		
		if (this.requiresCloning()) {
			LMMEController.getInstance().getCurrentSession().getBaseGraph().cloneSpecies(tab.getClonableSpecies());
		}
		
		HashSet<Node> transporters;
		SubsystemGraph transporterSubsystem = null;
		
		if (requiresTransporterSubsystem() || addTransporterSubsystem) {
			transporterSubsystem = this.determineTransporterSubsystem();
			transporters = (HashSet<Node>) transporterSubsystem.getReactionNodes().clone();
		} else {
			transporters = new HashSet<>();
		}
		
		ArrayList<SubsystemGraph> specificSubsystems = runSpecific(transporters);
		
		if (transporterSubsystem != null) {
			specificSubsystems.add(transporterSubsystem);
		}
		
		MMDecomposition decomposition = new MMDecomposition(specificSubsystems);
		
		SubsystemGraph defaultSubsystem = this.determineDefaultSubsystem(decomposition);
		
		if (defaultSubsystem != null) {
			decomposition.addSubsystem(defaultSubsystem);
		}
		
		return decomposition;
	}
	
	/**
	 * Determines the transporter subsystem.
	 * 
	 * @return a {@link SubsystemGraph} representing the transporter subsystem
	 */
	private SubsystemGraph determineTransporterSubsystem() {
		HashSet<Node> speciesNodes = new HashSet<>();
		HashSet<Node> reactionNodes = new HashSet<>();
		HashSet<Edge> edges = new HashSet<>();
		
		for (Node reactionNode : LMMEController.getInstance().getCurrentSession().getBaseGraph().getReactionNodes()) {
			
			HashSet<String> compartments = new HashSet<String>();
			Collection<Edge> edgeSet = reactionNode.getEdges();
			HashSet<Node> neighbors = new HashSet<Node>();
			
			for (Edge edge : edgeSet) {
				Node neighbor;
				if (edge.getSource() == reactionNode) {
					neighbor = edge.getTarget();
				} else {
					neighbor = edge.getSource();
				}
				if (AttributeHelper.hasAttribute(neighbor, SBML_Constants.SBML, SBML_Constants.COMPARTMENT)) {
					compartments.add((String) AttributeHelper.getAttributeValue(neighbor, SBML_Constants.SBML,
							SBML_Constants.COMPARTMENT, "", ""));
				}
				neighbors.add(neighbor);
			}
			
			if (compartments.size() > 1) {
				speciesNodes.addAll(neighbors);
				reactionNodes.add(reactionNode);
				edges.addAll(edgeSet);
			}
		}
		
		return new SubsystemGraph(LMMEConstants.TRANSPORTER_SUBSYSTEM, speciesNodes, reactionNodes, edges);
	}
	
	/**
	 * Determines the default subsystem.
	 * 
	 * @param decomposition
	 *           the current decomposition
	 * @return a {@link SubsystemGraph} representing the default subsystem
	 */
	private SubsystemGraph determineDefaultSubsystem(MMDecomposition decomposition) {
		HashSet<Node> speciesNodes = new HashSet<>();
		HashSet<Node> reactionNodes = new HashSet<>();
		HashSet<Edge> edges = new HashSet<>();
		
		for (Node reactionNode : LMMEController.getInstance().getCurrentSession().getBaseGraph().getReactionNodes()) {
			if (!decomposition.hasReactionBeenClassified(reactionNode)) {
				for (Edge inEdge : reactionNode.getDirectedInEdges()) {
					edges.add(inEdge);
					speciesNodes.add(inEdge.getSource());
				}
				for (Edge outEdge : reactionNode.getDirectedOutEdges()) {
					edges.add(outEdge);
					speciesNodes.add(outEdge.getTarget());
				}
				reactionNodes.add(reactionNode);
			}
		}
		if (reactionNodes.isEmpty()) {
			return null;
		} else {
			return new SubsystemGraph(LMMEConstants.DEFAULT_SUBSYSTEM, speciesNodes, reactionNodes, edges);
		}
	}
	
	/**
	 * Legacy version of a method to split the default subsystem in its conencted components.
	 * <p>
	 * Would need a review before usage.
	 * 
	 * @deprecated
	 * @param decomposition
	 * @param defaultSubsystem
	 * @param threshold
	 * @return
	 */
	private ArrayList<SubsystemGraph> splitDefaultSubsystem(MMDecomposition decomposition,
			SubsystemGraph defaultSubsystem, int threshold) {
		
		ArrayList<SubsystemGraph> subsystems = new ArrayList<>();
		
		Graph workingCopy = new AdjListGraph();
		HashMap<Node, Node> original2CopiedNodes = new HashMap<>();
		HashMap<Node, Node> copied2OriginalNodes = new HashMap<>();
		// HashMap<Edge, Edge> original2CopiedEdges = new HashMap<>();
		HashMap<Edge, Edge> copied2OriginalEdges = new HashMap<>();
		
		for (Node speciesNode : defaultSubsystem.getSpeciesNodes()) {
			Node newNode = workingCopy.addNodeCopy(speciesNode);
			original2CopiedNodes.put(speciesNode, newNode);
			copied2OriginalNodes.put(newNode, speciesNode);
		}
		for (Node reactionNode : defaultSubsystem.getReactionNodes()) {
			Node newNode = workingCopy.addNodeCopy(reactionNode);
			original2CopiedNodes.put(reactionNode, newNode);
			copied2OriginalNodes.put(newNode, reactionNode);
		}
		
		for (Edge edge : defaultSubsystem.getEdges()) {
			Node sourceNode = original2CopiedNodes.get(edge.getSource());
			Node tragetNode = original2CopiedNodes.get(edge.getTarget());
			Edge newEdge = workingCopy.addEdgeCopy(edge, sourceNode, tragetNode);
			copied2OriginalEdges.put(newEdge, edge);
		}
		
		Set<Set<Node>> connComps = GraphHelper.getConnectedComponents(workingCopy.getNodes());
		
		int i = 1;
		for (Set<Node> connComp : connComps) {
			if (connComp.size() >= threshold) {
				HashSet<Node> speciesNodes = new HashSet<>();
				HashSet<Node> reactionNodes = new HashSet<>();
				HashSet<Edge> edges = new HashSet<>();
				for (Node node : connComp) {
					Node originalNode = copied2OriginalNodes.get(node);
					if (LMMETools.getInstance().isSpecies(originalNode)) {
						speciesNodes.add(originalNode);
					} else if (LMMETools.getInstance().isReaction(originalNode)) {
						reactionNodes.add(originalNode);
					}
					for (Edge edge : node.getEdges()) {
						Edge originalEdge = copied2OriginalEdges.get(edge);
						if (defaultSubsystem.getEdges().contains(originalEdge)) {
							edges.add(originalEdge);
						}
					}
				}
				subsystems.add(new SubsystemGraph(LMMEConstants.DEFAULT_SUBSYSTEM + " " + i, speciesNodes,
						reactionNodes, edges));
				i += 1;
			}
		}
		
		return subsystems;
	}
	
	/**
	 * Creates subsystems from the reaction attributes.
	 * <p>
	 * The specified {@code attributeName} is used to distribute the reactions to subsystems. Every unique value of the attribute constitutes a subsystem which
	 * gets assigned all corresponding reactions.
	 * 
	 * @param attributeName
	 *           the name of the attribute serving as subsystem indicator
	 * @param considerSeparator
	 *           whether to consider a separating character within this attribute
	 * @param separator
	 *           the separating character
	 * @param alreadyClassifiedNodes
	 *           a list of nodes that have been classified already
	 * @return a list of {@link SubsystemGraph}s constituting a decomposition
	 */
	protected ArrayList<SubsystemGraph> determineSubsystemsFromReactionAttributes(String attributeName,
			boolean considerSeparator, String separator, HashSet<Node> alreadyClassifiedNodes) {
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		LMMESession currentSession = LMMEController.getInstance().getCurrentSession();
		
		HashSet<String> allSubsystemNames = new HashSet<>();
		
		for (Node reactionNode : baseGraph.getReactionNodes()) {
			if (!alreadyClassifiedNodes.contains(reactionNode)) {
				String subsystemName = currentSession.getNodeAttribute(reactionNode, attributeName);
				// if (subsystemName != "") {
				if (subsystemName.length() > 0) {
					if (considerSeparator) {
						String[] subsystemNames = subsystemName.split(Pattern.quote(separator));
						allSubsystemNames.addAll(Arrays.asList(subsystemNames));
					} else {
						allSubsystemNames.add(subsystemName);
					}
				}
			}
		}
		
		HashMap<String, SubsystemGraph> subsystemMap = new HashMap<>();
		
		for (String subsystemName : allSubsystemNames) {
			subsystemMap.put(subsystemName,
					new SubsystemGraph(subsystemName, new HashSet<>(), new HashSet<>(), new HashSet<>()));
		}
		
		for (Node reactionNode : baseGraph.getReactionNodes()) {
			if (!alreadyClassifiedNodes.contains(reactionNode)) {
				String subsystemName = currentSession.getNodeAttribute(reactionNode, attributeName);
				// if (subsystemName != "") {
				if (subsystemName.length() > 0) {
					String[] subsystemNames;
					if (considerSeparator) {
						subsystemNames = subsystemName.split(Pattern.quote(separator));
					} else {
						subsystemNames = new String[1];
						subsystemNames[0] = subsystemName;
					}
					for (String currentSubsystemName : subsystemNames) {
						subsystemMap.get(currentSubsystemName).addReaction(reactionNode);
						for (Edge incidentEdge : reactionNode.getEdges()) {
							subsystemMap.get(currentSubsystemName).addEdge(incidentEdge);
							if (incidentEdge.getSource() == reactionNode) {
								subsystemMap.get(currentSubsystemName).addSpecies(incidentEdge.getTarget());
							} else {
								subsystemMap.get(currentSubsystemName).addSpecies(incidentEdge.getSource());
							}
						}
					}
				}
			}
		}
		ArrayList<SubsystemGraph> res = new ArrayList<>();
		for (SubsystemGraph subsystem : subsystemMap.values()) {
			res.add(subsystem);
		}
		return res;
	}
	
	/**
	 * Returns whether this method expects a cloning step prior to the specific procedure.
	 * 
	 * @return whether this method expects a cloning step prior to the specific procedure
	 */
	public abstract boolean requiresCloning();
	
	/**
	 * Returns whether this method requires the transporter subsystem to be determined.
	 * 
	 * @return whether this method requires the transporter subsystem to be determined
	 */
	public abstract boolean requiresTransporterSubsystem();
	
	/**
	 * Returns the {@code FolderPanel} of this method.
	 * <p>
	 * The {@code FolderPanel} contains all settings relevant to this method and will be placed in the add-on tab.
	 * 
	 * @return the {@code FolderPanel} of this method
	 */
	public abstract FolderPanel getFolderPanel();
	
	/**
	 * Updates the {@code FolderPanel}.
	 */
	public abstract void updateFolderPanel();
	
	public abstract String getName();
}
