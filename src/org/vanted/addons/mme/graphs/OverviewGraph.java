package org.vanted.addons.mme.graphs;

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
import org.graffiti.selection.SelectionModel;
import org.graffiti.session.EditorSession;
import org.vanted.addons.mme.core.MMEController;
import org.vanted.addons.mme.decomposition.MMDecomposition;
import org.vanted.addons.mme.ui.MMETab;
import org.vanted.addons.mme.ui.MMEViewManagement;

public class OverviewGraph {

	private final int nodeSize = 100;

	private Graph graph;

	private MMDecomposition decomposition;

	// private HashMap<Edge, ArrayList<Node>> interfaceMap = new HashMap<>();

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
	 * A map that maps en edge to the interfaces between the corresponding
	 * subsystems.
	 */
	private HashMap<Edge, ArrayList<Node>> edgeToInterfacesMap;

	public OverviewGraph(Graph graph) {
		// this.graph = graph;
		// determineInterfaces();
	}

	public OverviewGraph(MMDecomposition decomposition) {
		this.nodeToSubsystemMap = new HashMap<>();
		this.subsystemToNodeMap = new HashMap<>();
		this.edgeToInterfacesMap = new HashMap<>();

		this.decomposition = decomposition;
		determineInterfaces();

		this.graph = new AdjListGraph();

		ArrayList<SubsystemGraph> subsystems = this.decomposition.getSubsystems();

		for (SubsystemGraph subsystem : subsystems) {
			Random random = new Random();

			Node subsystemNode = graph.addNode(
					AttributeHelper.getDefaultGraphicsAttributeForNode(random.nextInt(1000), random.nextInt(1000)));
			AttributeHelper.setLabel(subsystemNode, subsystem.getName());
			AttributeHelper.setSize(subsystemNode, nodeSize, nodeSize);

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
					Edge addedEdge = graph.addEdge(sourceNode, targetNode, false,
							AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false));

					ArrayList<Node> interfaces = new ArrayList<Node>();
					interfaces.addAll(getInterfaceNodes(subsystem1, subsystem2));
					interfaces.addAll(getInterfaceNodes(subsystem2, subsystem1));
					edgeToInterfacesMap.put(addedEdge, interfaces);
				}
			}
		}
		updateEdgeThickness();

		// TODO: Create graph! Perhaps using determineInterfaces() to achieve it!

		// TODO Create interface maps
		// TODO Construct nodeToSubsystem map during construction of graph.
	}

	/**
	 * @return the nodeSize
	 */
	public int getNodeSize() {
		return nodeSize;
	}

	public SubsystemGraph getSubsystemGraphOfNode(Node node) {
		return this.nodeToSubsystemMap.get(node);
	}

	public Node getNodeOfSubsystem(SubsystemGraph subsystem) {
		return this.subsystemToNodeMap.get(subsystem);
	}

	public ArrayList<Node> getInterfaceNodes(SubsystemGraph subsystem1, SubsystemGraph subsystem2) {
		return this.interfaceMap.get(subsystem1).get(subsystem2);
	}

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
			selectedSubsystems.add(this.nodeToSubsystemMap.get(node));
		}

		return selectedSubsystems;
	}

	/**
	 * This registers a selection listener for the selection information in the
	 * panel.
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
				MMETab tab = MMEController.getInstance().getTab();
				
				if ((edges.size() == 1) && (nodes.size() == 0)) {
					Edge edge = edges.iterator().next();
					ArrayList<String> names = new ArrayList<String>();
					for (Node interfaceNode : edgeToInterfacesMap.get(edge)) {
						names.add(AttributeHelper.getLabel(interfaceNode, ""));
					}
					tab.showSelectedEdgeInfo(nodeToSubsystemMap.get(edge.getSource()).getName(), nodeToSubsystemMap.get(edge.getTarget()).getName(), names);
				} else if ((nodes.size() == 1) && (edges.size() == 0)) {
					Node node = nodes.iterator().next();
					SubsystemGraph subsystem = nodeToSubsystemMap.get(node);
					tab.showSelectedSubsystemInfo(subsystem.getName(), subsystem.getNumberOfSpecies(), subsystem.getNumberOfReactions());
				} else {
					tab.resetSelectionInfo();
				}
			}
		});
		
		
		
	}

	/**
	 * This method determines the interfaces in the overview graph. Interfaces in
	 * this case refers to species that act as connection between subsystems. In
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

		for (Node speciesNode : MMEController.getInstance().getCurrentSession().getBaseGraph().getSpeciesNodes()) {
			Collection<Node> inneighbors = speciesNode.getAllInNeighbors();
			Collection<Node> outneighbors = speciesNode.getAllOutNeighbors();
			HashSet<SubsystemGraph> inSystems = new HashSet<>();
			HashSet<SubsystemGraph> outSystems = new HashSet<>();
			for (Node reactionNode : inneighbors) {
				inSystems.addAll(this.decomposition.getSubsystemsForReaction(reactionNode));
			}
			for (Node reactionNode : outneighbors) {
				outSystems.addAll(this.decomposition.getSubsystemsForReaction(reactionNode));
			}
			for (SubsystemGraph inSystem : inSystems) {
				for (SubsystemGraph outSystem : outSystems) {
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

	// public HashMap<SubsystemGraph, HashMap<SubsystemGraph, ArrayList<Node>>>
	// getInterfaceMap() {
	// return interfaceMap;
	// }

	// public ArrayList<String> getInterfaces() {
	// return interfaces;
	// }

	public MMDecomposition getDecomposition() {
		return decomposition;
	}

	/**
	 * Updates the edge thicknesses in the overview graph according to the user
	 * settings in the tab.
	 */
	public void updateEdgeThickness() {
		for (Edge edge : graph.getEdges()) {
			if (MMEController.getInstance().getTab().getDrawEdges()) {
				if (MMEController.getInstance().getTab().getMapToEdgeThickness()) {
					int totalInterfaces = edgeToInterfacesMap.get(edge).size();
					AttributeHelper.setFrameThickNess(edge, totalInterfaces > 20 ? 20.0 : (double) totalInterfaces);
				} else {
					AttributeHelper.setFrameThickNess(edge, 1.0);
				}
			} else {
				AttributeHelper.setFrameThickNess(edge, -1.0);
			}
		}

		if (MMEViewManagement.getInstance().getOverviewFrame() != null) {
			DefaultEditPanel.issueCompleteRedrawForView(MMEViewManagement.getInstance().getOverviewFrame().getView(),
					MMEViewManagement.getInstance().getOverviewFrame().getView().getGraph());
		}

	}

}
