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
package org.vanted.addons.lmme_dm.core;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.xml.XMLNode;
import org.vanted.addons.lmme_dm.graphs.BaseGraph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * This class provides several useful tools that may be used in the remaining code.
 * 
 * @author Michael Aichem
 */
public class LMMETools {
	
	private static LMMETools instance;
	
	private LMMETools() {
		
	}
	
	public static synchronized LMMETools getInstance() {
		if (LMMETools.instance == null) {
			LMMETools.instance = new LMMETools();
		}
		return LMMETools.instance;
	}
	
	/**
	 * Searches for available notes within the SBML file of a specified graph.
	 * 
	 * @param graph
	 * @return the found note names as {@code HashSet} of {@code String}s
	 */
	public HashSet<String> findNotes(Graph graph) {
		HashSet<String> res = new HashSet<>();
		List<Node> nodes = graph.getNodes();
		SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(graph);
		SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(graph);
		
		// Problem with copied notes of aggregated disease map graph!
//		for (Node node : nodes) {
//			if (isSpecies(node)) {
//				XMLNode notes = sbmlSpeciesHelper.getNotes(node);
//				findNote(notes, res);
//			} else if (isReaction(node)) {
//				XMLNode notes = sbmlReactionHelper.getNotes(node);
//				findNote(notes, res);
//			}
//		}
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
	 * going to be stored as attributes.
	 * <p>
	 * The notes are stored as attributes using VANTED's internal attributing mechanism.
	 * This is done for species as well as for reactions.
	 * This method exactly finds SBML notes of the form "[noteName] : [value]". The
	 * [value] is then stored as attribute with the name [attributeName] for the
	 * respective node.
	 * The method iterates over the original nodes in the BaseGraph and then for any
	 * node assigns the found attribute to all of the corresponding working copies
	 * of that node.
	 * 
	 * @param noteName
	 *           the name of the note to be read
	 * @param attributeName
	 *           the name of the newly created attribute
	 * @return whether a note with the specified name has been read
	 */
	public boolean readNotes(String noteName, String attributeName) {
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		List<Node> nodes = baseGraph.getOriginalGraph().getNodes();
		
		if (!baseGraph.getProcessedNotes().contains(noteName)) {
			
			SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(
					LMMEController.getInstance().getCurrentSession().getBaseGraph().getGraph());
			SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(
					LMMEController.getInstance().getCurrentSession().getBaseGraph().getGraph());
			
			boolean foundNotes = false;
			
			// Problem with copied notes of aggregated disease map graph!
//			for (Node node : nodes) {
//				if (isSpecies(node)) {
//					XMLNode notes = sbmlSpeciesHelper.getNotes(node);
//					foundNotes |= readNote(node, notes, noteName, attributeName);
//				} else if (isReaction(node)) {
//					XMLNode notes = sbmlReactionHelper.getNotes(node);
//					foundNotes |= readNote(node, notes, noteName, attributeName);
//				}
//			}
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
						for (Node workingNode : LMMEController.getInstance().getCurrentSession().getBaseGraph()
								.getWorkingNodes(node)) {
							LMMEController.getInstance().getCurrentSession().addNodeAttribute(workingNode,
									attributeName, note);
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
	 * Checks whether a given node is a reaction node.
	 * 
	 * @param node
	 *           the node to be checked
	 * @return whether the given node is a reaction node
	 */
	public boolean isReaction(Node node) {
		
		return isRole(node, SBML_Constants.ROLE_REACTION);
		
	}
	
	/**
	 * Checks whether a given node is a species node.
	 * 
	 * @param node
	 *           the node to be checked
	 * @return whether the given node is a species node
	 */
	public boolean isSpecies(Node node) {
		
		return isRole(node, SBML_Constants.ROLE_SPECIES);
		
	}
	
	/**
	 * Checks whether a given node has a given role.
	 * 
	 * @param node
	 *           the node to be checked
	 * @param role
	 *           the role for which the role affiliation is to be checked
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