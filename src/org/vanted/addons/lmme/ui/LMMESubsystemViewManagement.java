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
package org.vanted.addons.lmme.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;

/**
 * Manages the subsystem view during the exploration phase.
 * <p>
 * Among others, it is responsible for the resonable combination of selected subsystem graphs to a
 * whole and the addition of further subsystems to an existing drawing.
 * 
 * @author Michael Aichem
 */
public class LMMESubsystemViewManagement {
	
	private static LMMESubsystemViewManagement instance;
	
	private ArrayList<SubsystemGraph> currentSubsystems;
	
	private Color[] colors;
	
	private Color defaultColor;
	
	private HashMap<SubsystemGraph, Color> colorMap;
	
	/**
	 * The size of a node in the resulting drawing of the consolidated subsystem graph.
	 */
	private final int nodeSize = 50;
	
	private LMMESubsystemViewManagement() {
		this.currentSubsystems = new ArrayList<>();
		colors = new Color[] {
				new Color(126, 88, 57),
				new Color(95, 64, 255),
				new Color(145, 220, 72),
				new Color(189, 83, 214),
				new Color(108, 197, 111),
				new Color(208, 75, 158),
				new Color(207, 197, 81),
				new Color(93, 44, 110),
				new Color(114, 209, 190),
				new Color(213, 78, 41),
				new Color(104, 117, 195),
				new Color(203, 139, 67),
				new Color(142, 181, 218),
				new Color(211, 83, 98),
				new Color(74, 105, 51),
				new Color(202, 145, 180),
				new Color(55, 45, 61),
				new Color(199, 194, 158),
				new Color(118, 44, 51),
				new Color(82, 119, 124)
		};
		defaultColor = Color.GRAY;
		colorMap = new HashMap<>();
	}
	
	public static synchronized LMMESubsystemViewManagement getInstance() {
		if (LMMESubsystemViewManagement.instance == null) {
			LMMESubsystemViewManagement.instance = new LMMESubsystemViewManagement();
		}
		return LMMESubsystemViewManagement.instance;
	}
	
	/**
	 * Draws the given subsystems in the subsystems view according to some given options.
	 * <p>
	 * The consolidated subsystem graph is constructed from the given subsystems.
	 * 
	 * @param subsystems
	 *           the list of {@link SubsystemGraph}s to be combined into a consolidated subsystem graph
	 * @param clearView
	 *           whether the subsystems view has to be reset, only showing the selected subsystems. If set to {@code false}, the given subsystems will be added
	 *           to the existing drawing.
	 * @param useColor
	 *           whether a color mapping shall be used between the overview graph and the subsystems view
	 */
	public void showSubsystems(ArrayList<SubsystemGraph> subsystems, boolean clearView, boolean useColor) {
		
		if ((LMMEViewManagement.getInstance().getSubsystemFrame() == null)
				|| (LMMEViewManagement.getInstance().getSubsystemFrame().isClosed() == true)) {
			clearView = true;
		}
		if (clearView) {
			resetLists();
			int nextColorIndex = 0;
			for (SubsystemGraph subsystem : subsystems) {
				currentSubsystems.add(subsystem);
				if (nextColorIndex <= colors.length - 1) {
					colorMap.put(subsystem, colors[nextColorIndex]);
				} else {
					colorMap.put(subsystem, this.defaultColor);
				}
				nextColorIndex++;
			}
		} else {
			int nextColorIndex = this.currentSubsystems.size();
			for (SubsystemGraph subsystem : subsystems) {
				if (!currentSubsystems.contains(subsystem)) {
					currentSubsystems.add(subsystem);
					if (nextColorIndex <= colors.length - 1) {
						colorMap.put(subsystem, colors[nextColorIndex]);
					} else {
						colorMap.put(subsystem, this.defaultColor);
					}
					nextColorIndex++;
				}
			}
		}
		
		updateView(useColor);
		
		for (SubsystemGraph subsystem : currentSubsystems) {
			if (useColor) {
				AttributeHelper.setFillColor(LMMEController.getInstance().getCurrentSession().getOverviewGraph()
						.getNodeOfSubsystem(subsystem), colorMap.get(subsystem));
			}
		}
		
		HashSet<Node> speciesHashSet = new HashSet<Node>();
		HashSet<Node> reactionsHashSet = new HashSet<Node>();
		
		for (SubsystemGraph subsystem : currentSubsystems) {
			speciesHashSet.addAll(subsystem.getSpeciesNodes());
			reactionsHashSet.addAll(subsystem.getReactionNodes());
		}
		LMMEController.getInstance().getTab().setSubsystemInfo(currentSubsystems.size(), speciesHashSet.size(),
				reactionsHashSet.size());
	}
	
	/**
	 * Internally handles the update of the drawing in the subsystems view.
	 * 
	 * @param useColor
	 *           whether a color mapping shall be used between the overview graph and the subsystems view
	 */
	private void updateView(boolean useColor) {
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		
		Graph consolidatedSubsystemGraph = new AdjListGraph(
				(CollectionAttribute) baseGraph.getOriginalGraph().getAttributes().copy());
		
		HashMap<Node, Node> nodes2newNodes = new HashMap<>();
		HashSet<Edge> addedEdges = new HashSet<>();
		HashSet<Node> processedInterfaces = new HashSet<>();
		
		resetOverviewGraphColoring();
		
		for (SubsystemGraph subsystem : currentSubsystems) {
			for (Node speciesNode : subsystem.getSpeciesNodes()) {
				if (!nodes2newNodes.keySet().contains(speciesNode)) {
					Node newNode = consolidatedSubsystemGraph.addNodeCopy(speciesNode);
					AttributeHelper.setSize(newNode, nodeSize, nodeSize);
					nodes2newNodes.put(speciesNode, newNode);
					if (useColor) {
						AttributeHelper.setFillColor(newNode, colorMap.get(subsystem));
					}
				}
			}
			for (Node reactionNode : subsystem.getReactionNodes()) {
				if (!nodes2newNodes.keySet().contains(reactionNode)) {
					Node newNode = consolidatedSubsystemGraph.addNodeCopy(reactionNode);
					AttributeHelper.setSize(newNode, nodeSize, nodeSize);
					nodes2newNodes.put(reactionNode, newNode);
					if (useColor) {
						AttributeHelper.setFillColor(newNode, colorMap.get(subsystem));
					}
				}
			}
			for (Edge edge : subsystem.getEdges()) {
				if (!addedEdges.contains(edge)) {
					Node sourceNode = nodes2newNodes.get(edge.getSource());
					Node targetNode = nodes2newNodes.get(edge.getTarget());
					consolidatedSubsystemGraph.addEdgeCopy(edge, sourceNode, targetNode);
					addedEdges.add(edge);
				}
			}
		}
		
		for (SubsystemGraph sourceSystem : currentSubsystems) {
			for (SubsystemGraph targetSystem : currentSubsystems) {
				if (sourceSystem != targetSystem) {
					
					ArrayList<Node> interfaces = LMMEController.getInstance().getCurrentSession().getOverviewGraph()
							.getInterfaceNodes(sourceSystem, targetSystem);
					for (Node interfaceNode : interfaces) {
						if (!processedInterfaces.contains(interfaceNode)) {
							processedInterfaces.add(interfaceNode);
							if (!nodes2newNodes.keySet().contains(interfaceNode)) {
								Node newNode = consolidatedSubsystemGraph.addNodeCopy(interfaceNode);
								AttributeHelper.setSize(newNode, nodeSize, nodeSize);
								nodes2newNodes.put(interfaceNode, newNode);
							} else {
								AttributeHelper.setFillColor(nodes2newNodes.get(interfaceNode), Color.WHITE);
							}
							for (Edge inEdge : interfaceNode.getAllInEdges()) {
								if (nodes2newNodes.keySet().contains(inEdge.getSource())
										&& !addedEdges.contains(inEdge)) {
									addedEdges.add(inEdge);
									consolidatedSubsystemGraph.addEdgeCopy(inEdge,
											nodes2newNodes.get(inEdge.getSource()), nodes2newNodes.get(interfaceNode));
								}
							}
							for (Edge outEdge : interfaceNode.getAllOutEdges()) {
								if (nodes2newNodes.keySet().contains(outEdge.getTarget())
										&& !addedEdges.contains(outEdge)) {
									addedEdges.add(outEdge);
									consolidatedSubsystemGraph.addEdgeCopy(outEdge, nodes2newNodes.get(interfaceNode),
											nodes2newNodes.get(outEdge.getTarget()));
								}
							}
						}
					}
					
				}
			}
		}
		LMMEViewManagement.getInstance().showAsSubsystemGraph(consolidatedSubsystemGraph);
	}
	
	/**
	 * Resets the list of subsystems to be shown as well as the color mapping.
	 */
	public void resetLists() {
		resetOverviewGraphColoring();
		this.colorMap.clear();
		this.currentSubsystems.clear();
	}
	
	/**
	 * Resets the colors of the subsystem nodes in the overview graph.
	 */
	public void resetOverviewGraphColoring() {
		if (LMMEController.getInstance().getCurrentSession().isOverviewGraphConstructed()) {
			for (SubsystemGraph subsystem : LMMEController.getInstance().getCurrentSession().getOverviewGraph()
					.getDecomposition().getSubsystems()) {
				AttributeHelper.setFillColor(LMMEController.getInstance().getCurrentSession().getOverviewGraph()
						.getNodeOfSubsystem(subsystem), Color.WHITE);
			}
		}
	}
	
}
