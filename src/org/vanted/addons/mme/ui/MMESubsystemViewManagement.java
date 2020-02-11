/**
 * 
 */
package org.vanted.addons.mme.ui;

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
import org.vanted.addons.mme.core.MMEController;
import org.vanted.addons.mme.core.MMESession;
import org.vanted.addons.mme.graphs.BaseGraph;
import org.vanted.addons.mme.graphs.SubsystemGraph;

/**
 * This class manages the subsystem view during the exploration phase. Among
 * others, it is responsible for the resonable combination of selected subsystem
 * graphs to a whole and the addition of further subsystems to an existing
 * drawing.
 * 
 * @author Michael Aichem
 *
 */
public class MMESubsystemViewManagement {

	private static MMESubsystemViewManagement instance;

	private ArrayList<SubsystemGraph> currentSubSystems;

	private Color[] colors;

	private Color defaultColor;

	private HashMap<SubsystemGraph, Color> colorMap;

	private MMESubsystemViewManagement() {
		this.currentSubSystems = new ArrayList<>();
		colors = new Color[] { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED,
				Color.YELLOW };
		defaultColor = Color.GRAY;
		colorMap = new HashMap<>();
	}

	public static synchronized MMESubsystemViewManagement getInstance() {
		if (MMESubsystemViewManagement.instance == null) {
			MMESubsystemViewManagement.instance = new MMESubsystemViewManagement();
		}
		return MMESubsystemViewManagement.instance;
	}

	public void showSubsystems(ArrayList<SubsystemGraph> subsystems, boolean clearView, boolean useColor) {
		// if (! useColor) {
		// colorMap = new HashMap<>();
		// for (SubsystemGraph subsystem : currentSubSystems) {
		// // restore white color!
		// }
		// }
		if (clearView) {
			replaceSubsystems(subsystems, useColor);
		} else {
			addSubsystems(subsystems, useColor);
		}

		int species = 0;
		int reactions = 0;
		
		HashSet<Node> speciesHashSet = new HashSet<Node>();
		HashSet<Node> reactionsHashSet = new HashSet<Node>();
		
		for (SubsystemGraph subsystem : currentSubSystems) {
			speciesHashSet.addAll(subsystem.getSpeciesNodes());
			reactionsHashSet.addAll(subsystem.getReactionNodes());
		}
		MMEController.getInstance().getTab().setSubsystemInfo(currentSubSystems.size(), speciesHashSet.size(), reactionsHashSet.size());
	}

	/**
	 * This method adds the subsystems in the given list to the current list.
	 * Elements that have already existed in the current list will not be added
	 * again.
	 * 
	 * @param subsystemsToAdd
	 */
	public void addSubsystems(ArrayList<SubsystemGraph> subsystemsToAdd, boolean useColor) {
		int nextColorIndex = this.currentSubSystems.size();
		for (SubsystemGraph subsystem : subsystemsToAdd) {
			if (!currentSubSystems.contains(subsystem)) {
				currentSubSystems.add(subsystem);
				if (useColor) {
					if (nextColorIndex <= colors.length - 1) {
						colorMap.put(subsystem, colors[nextColorIndex]);
					} else {
						colorMap.put(subsystem, this.defaultColor);
					}
				}
				nextColorIndex++;
			}
		}
		// restore/extend colormapping
		updateView(useColor);
	}

	/**
	 * This method replaces the current subsystems by the ones contained in the
	 * given list.
	 * 
	 * @param subsystems
	 */
	public void replaceSubsystems(ArrayList<SubsystemGraph> subsystems, boolean useColor) {
		for (SubsystemGraph subsystem : currentSubSystems) {
			AttributeHelper.setFillColor(MMEController.getInstance().getCurrentSession().getOverviewGraph()
					.getNodeOfSubsystem(subsystem), Color.WHITE);
		}
		currentSubSystems.clear();
		int nextColorIndex = 0;
		for (SubsystemGraph subsystem : subsystems) {
			currentSubSystems.add(subsystem);
			if (useColor) {
				if (nextColorIndex <= colors.length - 1) {
					colorMap.put(subsystem, colors[nextColorIndex]);
				} else {
					colorMap.put(subsystem, this.defaultColor);
				}
			}
			nextColorIndex++;
		}
		updateView(useColor);
	}

	/**
	 * This method updates the view according to the list of subsystems that have to
	 * be shown.
	 */
	private void updateView(boolean useColor) {

		BaseGraph baseGraph = MMEController.getInstance().getCurrentSession().getBaseGraph();

		Graph consolidatedSubsystemGraph = new AdjListGraph(
				(CollectionAttribute) baseGraph.getOriginalGraph().getAttributes().copy());

		HashMap<Node, Node> nodes2newNodes = new HashMap<>();
		HashSet<Edge> addedEdges = new HashSet<>();
		HashSet<Node> processedInterfaces = new HashSet<>();

		for (SubsystemGraph subsystem : currentSubSystems) {
			if (useColor) {
				AttributeHelper.setFillColor(MMEController.getInstance().getCurrentSession().getOverviewGraph()
						.getNodeOfSubsystem(subsystem), colorMap.get(subsystem));
			}
			// access and use color when creating nodes.
			for (Node speciesNode : subsystem.getSpeciesNodes()) {
				if (!nodes2newNodes.keySet().contains(speciesNode)) {
					Node newNode = consolidatedSubsystemGraph.addNodeCopy(speciesNode);
					nodes2newNodes.put(speciesNode, newNode);
					if (useColor) {
						AttributeHelper.setFillColor(newNode, colorMap.get(subsystem));
					}
				}
			}
			for (Node reactionNode : subsystem.getReactionNodes()) {
				if (!nodes2newNodes.keySet().contains(reactionNode)) {
					Node newNode = consolidatedSubsystemGraph.addNodeCopy(reactionNode);
					nodes2newNodes.put(reactionNode, newNode);
					if (useColor) {
						AttributeHelper.setFillColor(newNode, colorMap.get(subsystem));
					}
				}
			}
			for (Edge edge : subsystem.getEdges()) {
				Node sourceNode = nodes2newNodes.get(edge.getSource());
				Node targetNode = nodes2newNodes.get(edge.getTarget());
				consolidatedSubsystemGraph.addEdgeCopy(edge, sourceNode, targetNode);
				addedEdges.add(edge);
			}
		}

		for (SubsystemGraph sourceSystem : currentSubSystems) {
			for (SubsystemGraph targetSystem : currentSubSystems) {
				if (sourceSystem != targetSystem) {

					ArrayList<Node> interfaces = MMEController.getInstance().getCurrentSession()
							.getOverviewGraph().getInterfaceNodes(sourceSystem, targetSystem);
					for (Node interfaceNode : interfaces) {
						if (!processedInterfaces.contains(interfaceNode)) {
							processedInterfaces.add(interfaceNode);
							if (!nodes2newNodes.keySet().contains(interfaceNode)) {
								Node newNode = consolidatedSubsystemGraph.addNodeCopy(interfaceNode);
								nodes2newNodes.put(interfaceNode, newNode);
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

		MMEViewManagement.getInstance().showAsSubsystemGraph(consolidatedSubsystemGraph);
	}

	public void resetLists() {
		this.colorMap.clear();
		this.currentSubSystems.clear();
	}

	// TODO maintain list of currently shown subsystems, create color mapping

	// TODO create artificial graph made out of node copies of the selected
	// subsystems nodes

	// TODO also add interfaces respectively, they should exist in both subsystems.

}
