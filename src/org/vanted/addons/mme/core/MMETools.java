package org.vanted.addons.mme.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.vanted.addons.mme.graphs.BaseGraph;

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
public class MMETools {

	private static MMETools instance;

	private MMETools() {
	}

	public static synchronized MMETools getInstance() {
		if (MMETools.instance == null) {
			MMETools.instance = new MMETools();
		}
		return MMETools.instance;
	}

	public HashSet<String> findNotes(Graph graph) {
		HashSet<String> res = new HashSet<>();
		List<Node> nodes = graph.getNodes();
		SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(graph);
		SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(graph);

		for (Node node : nodes) {
			if (isSpecies(node)) {
				XMLNode notes = sbmlSpeciesHelper.getNotes(node);
				findNote(notes, res);
			} else if (isReaction(node)) {
				XMLNode notes = sbmlReactionHelper.getNotes(node);
				findNote(notes, res);
			}
		}
		return res;
	}

	private void findNote(XMLNode xmlNode, HashSet<String> resultList) {
		if (xmlNode != null) {
			if (xmlNode.isText()) {
				if (xmlNode.getCharacters().indexOf(":") != -1) {
					String note = xmlNode.getCharacters().split(Pattern.quote(":"))[0].trim();
					resultList.add(note);
				}
			} else {
				int numChildren = xmlNode.getNumChildren();
				for (int i = 0; i < numChildren; i++) {
					XMLNode child = xmlNode.getChild(i);
					findNote(child, resultList);
				}
			}
		}
	}

	/**
	 * This method iterates over a set of nodes, extracting SBML notes that are then
	 * going to be stored as attributes in {@link GsmmSession.nodeAttributeMap} -
	 * This is done for species as well as for reactions.
	 * 
	 * This method exactly finds SBML notes of the form "[noteName] : [value]". The
	 * [value] is then stored as attribute with the name [attributeName] for the
	 * respective node.
	 * 
	 * The method iterates over the original nodes in the BaseGraph and then for any
	 * node assigns the found attribute to all of the corresponding working copies
	 * of that node.
	 * 
	 * @param nodes
	 * @param noteName
	 * @param attributeName
	 */
	public boolean readNotes(String noteName, String attributeName) {
		BaseGraph baseGraph = MMEController.getInstance().getCurrentSession().getBaseGraph();
		List<Node> nodes = baseGraph.getOriginalGraph().getNodes();

		if (!baseGraph.getProcessedNotes().contains(noteName)) {

			SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(
					MMEController.getInstance().getCurrentSession().getBaseGraph().getGraph());
			SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(
					MMEController.getInstance().getCurrentSession().getBaseGraph().getGraph());

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
				baseGraph.getProcessedNotes().add(noteName);
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
						for (Node workingNode : MMEController.getInstance().getCurrentSession().getBaseGraph().getWorkingNodes(node)) {
							MMEController.getInstance().getCurrentSession().addNodeAttribute(workingNode, attributeName, note);
						}
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
	 * @param node The node to be checked
	 * @return whether the given node is a reaction node
	 */
	public boolean isReaction(Node node) {

		return isRole(node, SBML_Constants.ROLE_REACTION);

	}

	/**
	 * Check whether a certain node is a species node.
	 * 
	 * @param node The node to be checked
	 * @return whether the given node is a species node
	 */
	public boolean isSpecies(Node node) {

		return isRole(node, SBML_Constants.ROLE_SPECIES);

	}

	/**
	 * Check whether a certain node has a given role.
	 * 
	 * @param node The node to be checked
	 * @param role The role for which the role affiliation is to be checked
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