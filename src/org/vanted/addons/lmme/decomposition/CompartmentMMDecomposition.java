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
package org.vanted.addons.lmme.decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.FolderPanel;
import org.GuiRow;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * This method determines a decomposition of the model based on the compartment
 * information available in the model.
 *
 * @author Michael Aichem
 */
public class CompartmentMMDecomposition extends MMDecompositionAlgorithm {
	
	private FolderPanel fp;
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		
		HashSet<String> compartments = new HashSet<String>();
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		
		for (Node speciesNode : baseGraph.getSpeciesNodes()) {
			if (!alreadyClassifiedNodes.contains(speciesNode)) {
				if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_COMPARTMENT_NAME)) {
					String compName = (String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_COMPARTMENT_NAME, "", "");
					compartments.add(compName);
				}
			}
		}
		
		ArrayList<SubsystemGraph> subsystems = new ArrayList<SubsystemGraph>();
		
		for (String compartment : compartments) {
			SubsystemGraph subsystem = new SubsystemGraph(compartment, new HashSet<>(), new HashSet<>(), new HashSet<>());
			for (Node speciesNode : baseGraph.getSpeciesNodes()) {
				if (compartment
						.equals((String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_COMPARTMENT_NAME, "", ""))) {
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
							alreadyClassifiedNodes.add(neighbor);
							for (Edge incidentEdge : neighbor.getEdges()) {
								subsystem.addEdge(incidentEdge);
								if (incidentEdge.getSource() == neighbor) {
									subsystem.addSpecies(incidentEdge.getTarget());
								} else {
									subsystem.addSpecies(incidentEdge.getSource());
								}
							}
							subsystem.addEdge(edge);
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
	
	@Override
	public boolean requiresTransporterSubsystem() {
		return false;
	}
	
}
