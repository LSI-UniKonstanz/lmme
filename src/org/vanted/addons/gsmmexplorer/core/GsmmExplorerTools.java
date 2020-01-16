package org.vanted.addons.gsmmexplorer.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionModel;
import org.sbml.jsbml.xml.XMLNode;
import org.vanted.addons.gsmmexplorer.graphs.BaseGraph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.SplitNodeForSingleMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

/**
 * 
 * @author Michael Aichem
 */
public class GsmmExplorerTools {

	private static GsmmExplorerTools instance;

	private GsmmExplorerTools() {
	}

	public static synchronized GsmmExplorerTools getInstance() {
		if (GsmmExplorerTools.instance == null) {
			GsmmExplorerTools.instance = new GsmmExplorerTools();
		}
		return GsmmExplorerTools.instance;
	}

	public void cloneSpecies(List<Node> clonableSpecies, int degreeThreshold) {
		for (Node nodeToCopy : clonableSpecies) {
			if (nodeToCopy.getDegree() >= degreeThreshold) {
				for (Edge edge : nodeToCopy.getEdges()) {
					Node newNode = nodeToCopy.getGraph().addNodeCopy(nodeToCopy);
					Node source, target;
					if (edge.getSource() == nodeToCopy) {
						source = newNode;
					} else {
						source = edge.getSource();
					}
					if (edge.getTarget() == nodeToCopy) {
						target = newNode;
					} else {
						target = edge.getTarget();
					}
					nodeToCopy.getGraph().addEdgeCopy(edge, source, target);
				}
				nodeToCopy.getGraph().deleteNode(nodeToCopy);
			}
		}

		GsmmExplorerController.getInstance().getCurrentSession().getBaseGraph().updateLists();
	}

	/**
	 * This method iterates over a set of nodes, extracting SBML notes that are then
	 * going to be stored as attributes in {@link GsmmSession.nodeAttributeMap} -
	 * Should be called before cloning!
	 * 
	 * @param nodes
	 * @param noteName
	 * @param attributeName
	 */
	public boolean readNotes(String noteName, String attributeName) {
		BaseGraph baseGraph = GsmmExplorerController.getInstance().getCurrentSession().getBaseGraph();
		List<Node> nodes = baseGraph.getOriginalGraph().getNodes();

		if (!baseGraph.getNotes().contains(noteName)) {

			SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(
					GsmmExplorerController.getInstance().getCurrentSession().getBaseGraph().getGraph());
			SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(
					GsmmExplorerController.getInstance().getCurrentSession().getBaseGraph().getGraph());

			boolean foundNotes = false;

			for (Node node : nodes) {
				if (isSpecies(node)) {
					XMLNode notes = sbmlSpeciesHelper.getNotes(node);
					foundNotes |= readNote(node, notes, noteName, attributeName);
				} else if (isReaction(node)) {
					XMLNode notes = sbmlReactionHelper.getNotes(node);
					foundNotes |= readNote(node, notes, noteName, attributeName);
				}
			}
			if (foundNotes) {
				baseGraph.getNotes().add(noteName);
			} else {
				JOptionPane.showMessageDialog(null, "<html> Could not find a note with the tag <b>" + noteName
						+ "</b> in the SBML file. <br> The decomposition will thus not depend on these notes.</html>");
			}
			return foundNotes;
		} else {
			return true;
		}
	}

	private boolean readNote(Node node, XMLNode xmlNode, String noteName, String attributeName) {
		if (xmlNode == null) {
			return false;
		} else {
			if (xmlNode.isText()) {
				if (xmlNode.getCharacters().trim().startsWith(noteName + ":")) {
					String note = xmlNode.getCharacters().trim();
					note = note.replace(noteName + ": ", "");
					if (!note.equals("null")) {
						GsmmExplorerController.getInstance().getCurrentSession().addNodeAttribute(GsmmExplorerController
								.getInstance().getCurrentSession().getBaseGraph().getWorkingNode(node), attributeName,
								note);
					}
					return true;
				} else {
					return false;
				}
			} else {
				int numChildren = xmlNode.getNumChildren();
				boolean foundNotes = false;
				for (int i = 0; i < numChildren; i++) {
					XMLNode child = xmlNode.getChild(i);
					foundNotes |= readNote(node, child, noteName, attributeName);
				}
				return foundNotes;
			}
		}
	}

	/**
	 * Check whether a certain node is a reaction node.
	 * 
	 * @param node
	 *            The node to be checked
	 * @return whether the given node is a reaction node
	 */
	public boolean isReaction(Node node) {

		return isRole(node, SBML_Constants.ROLE_REACTION);

	}

	/**
	 * Check whether a certain node is a species node.
	 * 
	 * @param node
	 *            The node to be checked
	 * @return whether the given node is a species node
	 */
	public boolean isSpecies(Node node) {

		return isRole(node, SBML_Constants.ROLE_SPECIES);

	}

	/**
	 * Check whether a certain node has a given role.
	 * 
	 * @param node
	 *            The node to be checked
	 * @param role
	 *            The role for which the role affiliation is to be checked
	 * @return whether the given node has the specified role
	 */
	public boolean isRole(Node node, String role) {

		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML, SBML_Constants.SBML_ROLE)) {
			String sbmlRole = (String) AttributeHelper.getAttributeValue(node, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, "", "");
			if (sbmlRole.equals(role))
				return true;
		}
		return false;
	}

}