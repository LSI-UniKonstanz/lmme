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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.graphs.BaseGraph;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;
import org.vanted.addons.lmme_dm.ui.LMMETab;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * This method is an implementation of the method that has been proposed by Schuster et al.
 * <p>
 * It was published in the following paper:
 * S. Schuster, T. Pfeiffer, F. Moldenhauer, I. Koch, T. Dandekar, Exploring the
 * pathway structure of metabolism: decomposition into subnetworks and
 * application to Mycoplasma pneumoniae, Bioinformatics, Volume 18, Issue 2,
 * February 2002, Pages 351–361.
 *
 * @author Michael Aichem
 */
public class SchusterMMDecomposition extends MMDecompositionAlgorithm {
	
	private int defaultThreshold = 8;
	
	private JTextField tfThreshold;
	
	private final String ATTRIBUTE_NAME_SUBSYSTEM = "SchusterSubsystem";
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		
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
				if (LMMETools.getInstance().isReaction(originalNode)
						&& !alreadyClassifiedNodes.contains(originalNode)) {
					LMMEController.getInstance().getCurrentSession().addNodeAttribute(originalNode,
							this.ATTRIBUTE_NAME_SUBSYSTEM, "Algorithmically derived Subsystem " + count);
				}
			}
			count++;
		}
		
		return determineSubsystemsFromReactionAttributes(this.ATTRIBUTE_NAME_SUBSYSTEM, true, ";",
				alreadyClassifiedNodes);
	}
	
	@Override
	public boolean requiresCloning() {
		return true;
	}
	
	@Override
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName() + " Settings", false, true, false, null);
		
		JLabel lblThreshold = new JLabel("Metabolite degree threshold: ");
		this.tfThreshold = new JTextField(5);
		this.tfThreshold.setText(Integer.toString(this.defaultThreshold));
		
		JPanel thresholdLine = LMMETab.combine(lblThreshold, this.tfThreshold, Color.WHITE, false, true);
		fp.addGuiComponentRow(thresholdLine, null, true);
		
		return fp;
	}
	
	@Override
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
	
	@Override
	public String getName() {
		return "Schuster et al.";
	}
	
	@Override
	public boolean requiresTransporterSubsystem() {
		return false;
	}
	
}
