package org.vanted.addons.mme.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.FolderPanel;
import org.GuiRow;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.core.MMEController;
import org.vanted.addons.mme.graphs.BaseGraph;
import org.vanted.addons.mme.graphs.SubsystemGraph;
import org.vanted.addons.mme.ui.MMETab;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class CompartmentMMDecomposition extends MMDecompositionAlgorithm {

	private FolderPanel fp;

	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {

		HashSet<String> compartments = new HashSet<String>();

		BaseGraph baseGraph = MMEController.getInstance().getCurrentSession().getBaseGraph();

		for (Node speciesNode : baseGraph.getSpeciesNodes()) {
			if (!alreadyClassifiedNodes.contains(speciesNode)) {
				if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.COMPARTMENT)) {
					compartments.add((String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML,
							SBML_Constants.COMPARTMENT, "", ""));
				}
			}
		}

		ArrayList<SubsystemGraph> subsystems = new ArrayList<SubsystemGraph>();

		for (String compartment : compartments) {
			SubsystemGraph subsystem = new SubsystemGraph("Compartment " + compartment, new HashSet<>(),
					new HashSet<>(), new HashSet<>());
			for (Node speciesNode : baseGraph.getSpeciesNodes()) {
				if (compartment.equals((String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML,
						SBML_Constants.COMPARTMENT, "", ""))) {
					if (!alreadyClassifiedNodes.contains(speciesNode)) {
						subsystem.addSpecies(speciesNode);
						Collection<Edge> edgeSet = speciesNode.getEdges();
						for (Edge edge : edgeSet) {
							Node neighbor;
							if (edge.getSource() == speciesNode) {
								neighbor = edge.getTarget();
							} else {
								neighbor = edge.getSource();
							}
							if (!alreadyClassifiedNodes.contains(neighbor)) {
								subsystem.addReaction(neighbor);
								subsystem.addEdge(edge);
							}
						}
					}
				}
			}
			subsystems.add(subsystem);
		}
		return subsystems;
	}

	@Override
	public boolean requiresCloning() {
		return true;
	}

	@Override
	public FolderPanel getFolderPanel() {

		if (this.fp != null) {
			updateFolderPanel();
		} else {
			fp = new FolderPanel(getName() + " Settings", false, true, false, null);
			GuiRow row = new GuiRow(new JLabel("No settings."), null);
			fp.addGuiComponentRow(row, true);
		}
		return fp;

	}

	@Override
	public void updateFolderPanel() {
		// Do nothing.
	}

	@Override
	public String getName() {
		return "Compartment Decomposition";
	}

}
