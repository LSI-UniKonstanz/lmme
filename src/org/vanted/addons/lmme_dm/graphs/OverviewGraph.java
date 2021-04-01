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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.decomposition.MMDecomposition;
import org.vanted.addons.lmme_dm.ui.LMMETab;
import org.vanted.addons.lmme_dm.ui.LMMEViewManagement;

/**
 * Maintains the generated overview graph, which models the relationships between the individual subsystems.
 *
 * @author Michael Aichem
 */
public class OverviewGraph {
	
	private final int nodeSizeSubsystem = 100;
	private final int nodeSizeInterface = 50;
	
	private Graph graph;
	
	private MMDecomposition decomposition;
	
	private boolean containsInterfaceNodes;
	
	private HashMap<SubsystemGraph, HashMap<SubsystemGraph, ArrayList<Node>>> interfaceMap;
	
	// private ArrayList<String> interfaces = new ArrayList<>();
	
	/**
	 * A map that maps a node in the overview graph to its corresponding subsystem
	 * graph.
	 */
	private HashMap<Node, SubsystemGraph> nodeToSubsystemMap;
	
	/**
	 * A map that maps a subsystem to its corresponding node in the overview graph.
	 */
	private HashMap<SubsystemGraph, Node> subsystemToNodeMap;
	
	/**
	 * A map that maps an edge to the interfaces between the corresponding subsystems, justifying the edge.
	 */
	private HashMap<Edge, ArrayList<Node>> edgeToInterfacesMap;
	
	/**
	 * A map that maps an interface node to its corresponding node in the overview graph.
	 */
	private HashMap<Node, Node> interfaceToNewNodeMap;
	
	/**
	 * Constructor for the overview graph.
	 * <p>
	 * During execution, the decomposition is processed to create an overview graph. This is done by creating one node per subsystem and successively determining
	 * interfaces between the given subsystems and adding respective edges connecting the subsystems.
	 * 
	 * @param decomposition
	 *           the underlying decomposition
	 */
	public OverviewGraph(MMDecomposition decomposition, boolean showInterfaces) {
		this.nodeToSubsystemMap = new HashMap<>();
		this.subsystemToNodeMap = new HashMap<>();
		this.edgeToInterfacesMap = new HashMap<>();
		this.interfaceToNewNodeMap = new HashMap<>();
		this.containsInterfaceNodes = showInterfaces;
		Random random = new Random();
		
		this.decomposition = decomposition;
		determineInterfaces();
		
		this.graph = new AdjListGraph();
		
		ArrayList<SubsystemGraph> subsystems = this.decomposition.getSubsystems();
		
		for (SubsystemGraph subsystem : subsystems) {
			
			Node subsystemNode = graph.addNode(
					AttributeHelper.getDefaultGraphicsAttributeForNode(random.nextInt(1000), random.nextInt(1000)));
			AttributeHelper.setLabel(subsystemNode, subsystem.getName());
			AttributeHelper.setSize(subsystemNode, nodeSizeSubsystem, nodeSizeSubsystem);
			
			subsystemToNodeMap.put(subsystem, subsystemNode);
			nodeToSubsystemMap.put(subsystemNode, subsystem);
		}
		
		for (int i = 0; i < this.decomposition.getSubsystems().size(); i++) {
			for (int j = i + 1; j < this.decomposition.getSubsystems().size(); j++) {
				SubsystemGraph subsystem1 = this.decomposition.getSubsystems().get(i);
				SubsystemGraph subsystem2 = this.decomposition.getSubsystems().get(j);
				int totalInterfaces = getInterfaceNodes(subsystem1, subsystem2).size()
						+ getInterfaceNodes(subsystem2, subsystem1).size();
				if (totalInterfaces > 0) {
					Node sourceNode = subsystemToNodeMap.get(subsystem1);
					Node targetNode = subsystemToNodeMap.get(subsystem2);
					
					HashSet<Node> interfaceSet = new HashSet<Node>();
					interfaceSet.addAll(getInterfaceNodes(subsystem1, subsystem2));
					interfaceSet.addAll(getInterfaceNodes(subsystem2, subsystem1));
					
					if (showInterfaces) {
						for (Node interfaceNode : interfaceSet) {
							if (!interfaceToNewNodeMap.containsKey(interfaceNode)) {
								Node newInterfaceNode = graph.addNodeCopy(interfaceNode);
								AttributeHelper.setSize(newInterfaceNode, nodeSizeInterface, nodeSizeInterface);
								interfaceToNewNodeMap.put(interfaceNode, newInterfaceNode);
							}
							Node interfaceNodeOG = interfaceToNewNodeMap.get(interfaceNode);
							if (!interfaceNodeOG.getNeighbors().contains(sourceNode)) {
								graph.addEdge(sourceNode, interfaceNodeOG, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false));
							}
							if (!interfaceNodeOG.getNeighbors().contains(targetNode)) {
								graph.addEdge(targetNode, interfaceNodeOG, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false));
							}
						}
					} else {
						ArrayList<Node> interfaces = new ArrayList<Node>();
						interfaces.addAll(interfaceSet);
						Edge addedEdge = graph.addEdge(sourceNode, targetNode, false,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false));
						edgeToInterfacesMap.put(addedEdge, interfaces);
					}
					
				}
			}
		}
		updateEdgeThickness();
		colorInterfaces();
	}
	
	/**
	 * @return the nodeSize
	 */
	public int getSubsystemNodeSize() {
		return nodeSizeSubsystem;
	}
	
	/**
	 * @return the nodeSize
	 */
	public int getInterfaceNodeSize() {
		return nodeSizeInterface;
	}
	
	/**
	 * Gets the {@link SubsystemGraph} of the given node within the overview graph.
	 * 
	 * @param node
	 *           the node within the overview graph
	 * @return the {@link SubsystemGraph} of the given node
	 */
	public SubsystemGraph getSubsystemGraphOfNode(Node node) {
		return this.nodeToSubsystemMap.get(node);
	}
	
	/**
	 * Gets the node within the overview graph of the specified subsystem.
	 * 
	 * @param subsystem
	 *           the subsystem
	 * @return the corresponding node within the overview graph
	 */
	public Node getNodeOfSubsystem(SubsystemGraph subsystem) {
		return this.subsystemToNodeMap.get(subsystem);
	}
	
	/**
	 * Gets the list of interface metabolites that lie between the two specified subsystems.
	 * 
	 * @param subsystem1
	 * @param subsystem2
	 * @return the list of interface metabolites between the subsystems
	 */
	public ArrayList<Node> getInterfaceNodes(SubsystemGraph subsystem1, SubsystemGraph subsystem2) {
		return this.interfaceMap.get(subsystem1).get(subsystem2);
	}
	
	/**
	 * Gets a list of subsystems whose nodes have been selected by the user in the overview graph representation.
	 * 
	 * @return a list of subsystems whose nodes have been selected by the user
	 */
	public ArrayList<SubsystemGraph> getSelectedSubsystems() {
		EditorSession editorSession = null;
		for (EditorSession es : MainFrame.getInstance().getEditorSessions()) {
			if (es.getGraph() == this.graph) {
				editorSession = es;
				break;
			}
		}
		Collection<Node> selectedNodes = editorSession.getSelectionModel().getActiveSelection().getNodes();
		
		ArrayList<SubsystemGraph> selectedSubsystems = new ArrayList<>();
		
		for (Node node : selectedNodes) {
			if (this.nodeToSubsystemMap.containsKey(node)) {
				selectedSubsystems.add(this.nodeToSubsystemMap.get(node));
			}
		}
		
		return selectedSubsystems;
	}
	
	/**
	 * Registers a selection listener for the selection information in the panel.
	 */
	public void registerSelectionListener() {
		
		EditorSession editorSession = null;
		for (EditorSession es : MainFrame.getInstance().getEditorSessions()) {
			if (es.getGraph() == this.graph) {
				editorSession = es;
				break;
			}
		}
		editorSession.getSelectionModel().addSelectionListener(new SelectionListener() {
			
			public void selectionListChanged(SelectionEvent e) {
				// Do nothing.
			}
			
			public void selectionChanged(SelectionEvent e) {
				Collection<Edge> edges = e.getSelection().getEdges();
				Collection<Node> nodes = e.getSelection().getNodes();
				LMMETab tab = LMMEController.getInstance().getTab();
				
				if ((edges.size() == 1) && (nodes.size() == 0) && !containsInterfaceNodes) {
					Edge edge = edges.iterator().next();
					ArrayList<String> names = new ArrayList<String>();
					for (Node interfaceNode : edgeToInterfacesMap.get(edge)) {
						names.add(AttributeHelper.getLabel(interfaceNode, ""));
					}
					tab.showSelectedEdgeInfo(nodeToSubsystemMap.get(edge.getSource()).getName(),
							nodeToSubsystemMap.get(edge.getTarget()).getName(), names);
				} else if ((nodes.size() == 1) && (edges.size() == 0)) {
					Node node = nodes.iterator().next();
					if (nodeToSubsystemMap.containsKey(node)) {
						SubsystemGraph subsystem = nodeToSubsystemMap.get(node);
						tab.showSelectedSubsystemInfo(subsystem.getName(), subsystem.getNumberOfSpecies(),
								subsystem.getNumberOfReactions());
					}
				} else {
					tab.resetSelectionInfo();
				}
			}
		});
		
	}
	
	/**
	 * Determines the interfaces in the overview graph.
	 * <p>
	 * Interfaces in this case refers to species that act as connection between subsystems. In
	 * this implementation, a species s is considered to be an interface between
	 * subsystems S1 and S2 if and only if s has at least one in-neighbor from S1
	 * and at least one out-neighbor from S2. During this method,
	 * {@link interfaceMap} is set.
	 */
	private void determineInterfaces() {
		ArrayList<SubsystemGraph> subsystems = this.decomposition.getSubsystems();
		
		// Initialise HashMap(s)
		interfaceMap = new HashMap<>();
		for (SubsystemGraph subsystem1 : subsystems) {
			HashMap<SubsystemGraph, ArrayList<Node>> hmap = new HashMap<>();
			for (SubsystemGraph subsystem2 : subsystems) {
				if (subsystem2 != subsystem1) {
					ArrayList<Node> nodeList = new ArrayList<>();
					hmap.put(subsystem2, nodeList);
				}
			}
			interfaceMap.put(subsystem1, hmap);
		}
		
		for (Node speciesNode : LMMEController.getInstance().getCurrentSession().getBaseGraph().getSpeciesNodes()) {
			
			Set<Node> neighbours = speciesNode.getNeighbors();
			
			HashSet<SubsystemGraph> containingSystems = new HashSet<>();
			for (Node reactionNode : neighbours) {
				if (LMMETools.getInstance().isReaction(reactionNode)) {
					containingSystems.addAll(this.decomposition.getSubsystemsForReaction(reactionNode));
				}
			}
			
			// currently, interfaces correspond to undirected relationships between subsystems.
			// may be changed in the future.
			for (SubsystemGraph inSystem : containingSystems) {
				for (SubsystemGraph outSystem : containingSystems) {
					if (inSystem != outSystem) {
						interfaceMap.get(inSystem).get(outSystem).add(speciesNode);
					}
				}
			}
		}
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public MMDecomposition getDecomposition() {
		return decomposition;
	}
	
	/**
	 * Updates the edge thicknesses in the overview graph according to the user
	 * settings in the tab.
	 */
	public void updateEdgeThickness() {
		for (Edge edge : graph.getEdges()) {
			if (LMMEController.getInstance().getTab().getDrawEdges()) {
				if (LMMEController.getInstance().getTab().getMapToEdgeThickness() && !containsInterfaceNodes) {
					int totalInterfaces = edgeToInterfacesMap.get(edge).size();
					AttributeHelper.setFrameThickNess(edge, totalInterfaces > 20 ? 20.0 : (double) totalInterfaces);
				} else {
					AttributeHelper.setFrameThickNess(edge, 1.0);
				}
			} else {
				AttributeHelper.setFrameThickNess(edge, -1.0);
			}
		}
		
		if (LMMEViewManagement.getInstance().getOverviewFrame() != null) {
			DefaultEditPanel.issueCompleteRedrawForView(LMMEViewManagement.getInstance().getOverviewFrame().getView(),
					LMMEViewManagement.getInstance().getOverviewFrame().getView().getGraph());
		}
		
	}
	
	public void colorInterfaces() {
		int maxDeg = -1;
		for (Node interfaceNode : this.interfaceToNewNodeMap.values()) {
			if (interfaceNode.getDegree() > maxDeg) {
				maxDeg = interfaceNode.getDegree();
			}
		}
		for (Node interfaceNode : this.interfaceToNewNodeMap.values()) {
			Color c = Color.WHITE;
			if (LMMEController.getInstance().getTab().getColorInterfaces()) {
				
				int frac = 255 - ((interfaceNode.getDegree() * 255) / maxDeg);
				c = new Color(255, frac, frac);
			}
			AttributeHelper.setFillColor(interfaceNode, c);
		}
	}
	
}
