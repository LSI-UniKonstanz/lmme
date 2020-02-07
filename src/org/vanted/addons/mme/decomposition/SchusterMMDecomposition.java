package org.vanted.addons.mme.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.FolderPanel;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.core.MMEController;
import org.vanted.addons.mme.core.MMETools;
import org.vanted.addons.mme.graphs.BaseGraph;
import org.vanted.addons.mme.graphs.SubsystemGraph;
import org.vanted.addons.mme.ui.MMETab;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class SchusterMMDecomposition extends MMDecompositionAlgorithm {

	private int defaultThreshold = 8;

	private JTextField tfThreshold;

	private final String ATTRIBUTE_NAME_SUBSYSTEM = "SchusterSubsystem";

	@Override
	protected ArrayList<SubsystemGraph> runSpecific() {

		BaseGraph baseGraph = MMEController.getInstance().getCurrentSession().getBaseGraph();

		Graph workingCopy = new AdjListGraph();
		HashMap<Node, Node> original2CopiedNodes = new HashMap<>();
		HashMap<Node, Node> copied2OriginalNodes = new HashMap<>();
		// HashMap<Edge, Edge> original2CopiedEdges = new HashMap<>();
		HashMap<Edge, Edge> copied2OriginalEdges = new HashMap<>();

		for (Node speciesNode : baseGraph.getSpeciesNodes()) {
			Node newNode = workingCopy.addNodeCopy(speciesNode);
			original2CopiedNodes.put(speciesNode, newNode);
			copied2OriginalNodes.put(newNode, speciesNode);
		}
		for (Node reactionNode : baseGraph.getReactionNodes()) {
			Node newNode = workingCopy.addNodeCopy(reactionNode);
			original2CopiedNodes.put(reactionNode, newNode);
			copied2OriginalNodes.put(newNode, reactionNode);
		}

		for (Edge edge : baseGraph.getGraph().getEdges()) {
			Node sourceNode = original2CopiedNodes.get(edge.getSource());
			Node tragetNode = original2CopiedNodes.get(edge.getTarget());
			Edge newEdge = workingCopy.addEdgeCopy(edge, sourceNode, tragetNode);
			copied2OriginalEdges.put(newEdge, edge);
		}

		int threshold = readThreshold();

		// ArrayList<Node> possibleInterfaces = new ArrayList<>();

		for (Node speciesNode : baseGraph.getSpeciesNodes()) {
			if (speciesNode.getDegree() >= threshold) {
				// possibleInterfaces.add(speciesNode);
				workingCopy.deleteNode(original2CopiedNodes.get(speciesNode));
			}
		}

		Set<Set<Node>> connComps = GraphHelper.getConnectedComponents(workingCopy.getNodes());

		int count = 1;
		for (Set<Node> nodes : connComps) {
			for (Node node : nodes) {
				Node originalNode = copied2OriginalNodes.get(node);
				if (MMETools.getInstance().isReaction(originalNode)) {
					MMEController.getInstance().getCurrentSession().addNodeAttribute(originalNode,
							this.ATTRIBUTE_NAME_SUBSYSTEM, "Algorithmically derived Subsystem " + count);
				}
			}
			count++;
		}

		return determineSubsystemsFromReactionAttributes(this.ATTRIBUTE_NAME_SUBSYSTEM, true, ";");
	}

	/**
	 * 
	 */
	public boolean requiresCloning() {
		return true;
	}

	/**
	 * 
	 */
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName() + " Settings", false, true, false, null);

		JLabel lblThreshold = new JLabel("Metabolite degree threshold: ");
		this.tfThreshold = new JTextField(5);
		this.tfThreshold.setText(Integer.toString(this.defaultThreshold));

		JPanel thresholdLine = MMETab.combine(lblThreshold, this.tfThreshold, Color.WHITE, false, true);
		fp.addGuiComponentRow(thresholdLine, null, true);

		return fp;
	}

	/**
	 * 
	 */
	public void updateFolderPanel() {
		// no need to do sth.
	}

	private int readThreshold() {
		int res;
		try {
			res = Integer.parseInt(this.tfThreshold.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "The metabolite degree threshold could not be read. "
					+ "It has therefore been set to " + this.defaultThreshold + ".");
			res = this.defaultThreshold;
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm#
	 * getName()
	 */
	@Override
	public String getName() {
		return "Schuster et al.";
	}

}
