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
package org.vanted.addons.lmme_dm.layout;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JPanel;

import org.AttributeHelper;
import org.JMButton;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.vanted.addons.lmme_dm.core.LMMEConstants;
import org.vanted.addons.lmme_dm.ui.LMMESubsystemViewManagement;
import org.vanted.addons.lmme_dm.ui.LMMEViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;

/**
 * A layout for the consolidated subsystem graph that is built upon the Minerva coordinates.
 * The subsystems are arranged circular, adopting their predefined layout. Any remaining node that had no individual coordinates (as it had more than one, or
 * was merged or was cloned) is placed according to a force-directed layout.
 * 
 * @author Michael Aichem
 */
public class MinervaDMLayout implements MMSubsystemLayout {
	
	@Override
	public void layOutAsSubsystems(Graph graph) {
		
		HashSet<Node> nodesWithCoordinates = new HashSet<>();
		HashSet<Node> nodesWithoutCoordinates = new HashSet<>();
		
		int isClone = 0;
		int wasMerged = 0;
		int noCoordinates = 0;
		int moreThanOneCoordinates = 0;
		int onlyOneCoordinates = 0;
		
		for (Node node : graph.getNodes()) {
			if (AttributeHelper.hasAttribute(node, LMMEConstants.ATTRIBUTE_PATH, "isClone")) {
				nodesWithoutCoordinates.add(node);
				isClone++;
			} else if (AttributeHelper.hasAttribute(node, "lmme_dm", "wasMerged")) {
				nodesWithoutCoordinates.add(node);
				wasMerged++;
			} else if (AttributeHelper.hasAttribute(node, "minerva", "minerva_x")) {
				if (AttributeHelper.hasAttribute(node, "minerva", "minerva_x2")) {
					nodesWithoutCoordinates.add(node);
					moreThanOneCoordinates++;
				} else {
					nodesWithCoordinates.add(node);
					onlyOneCoordinates++;
				}
			} else {
				nodesWithoutCoordinates.add(node);
				noCoordinates++;
			}
		}
		System.out.println("isClone: " + isClone);
		System.out.println("wasMerged: " + wasMerged);
		System.out.println("noCoordinates: " + noCoordinates);
		System.out.println("moreThanOneCoordinates: " + moreThanOneCoordinates);
		System.out.println("onlyOneCoordinates: " + onlyOneCoordinates);
		
		HashMap<String, HashSet<Node>> subsystemMap = new HashMap<>();
		HashSet<Node> nodesToRemove = new HashSet<>();
		for (Node node : nodesWithCoordinates) {
			String subsystemName = LMMESubsystemViewManagement.getInstance().getSubsystemName(node);
			if (subsystemName == null) {
				nodesToRemove.add(node);
				nodesWithoutCoordinates.add(node);
			} else {
				if (!subsystemMap.containsKey(subsystemName)) {
					subsystemMap.put(subsystemName, new HashSet<>());
				}
				subsystemMap.get(subsystemName).add(node);
			}
		}
		nodesWithCoordinates.removeAll(nodesToRemove);
		
		HashMap<String, Vector2d> subsystem2NewCenterMap = new HashMap<>();
		HashMap<String, Double> subsystem2RadiusMap = new HashMap<>();
		
		for (String subsystem : subsystemMap.keySet()) {
			double minX = 0.0;
			double minY = 0.0;
			double maxX = 0.0;
			double maxY = 0.0;
			HashSet<Node> nodes = subsystemMap.get(subsystem);
			boolean initialised = false;
			for (Node node : nodes) {
				AttributeHelper.setPosition(node,
						Double.parseDouble((String) AttributeHelper.getAttributeValue(node, "minerva", "minerva_x", 0, "")),
						Double.parseDouble((String) AttributeHelper.getAttributeValue(node, "minerva", "minerva_y", 0, "")));
				if (!initialised) {
					minX = AttributeHelper.getPositionX(node);
					maxX = AttributeHelper.getPositionX(node);
					minY = AttributeHelper.getPositionY(node);
					maxY = AttributeHelper.getPositionY(node);
					initialised = true;
				} else {
					if (AttributeHelper.getPositionX(node) < minX) {
						minX = AttributeHelper.getPositionX(node);
					}
					if (AttributeHelper.getPositionX(node) > maxX) {
						maxX = AttributeHelper.getPositionX(node);
					}
					if (AttributeHelper.getPositionY(node) < minY) {
						minY = AttributeHelper.getPositionY(node);
					}
					if (AttributeHelper.getPositionY(node) > maxY) {
						maxY = AttributeHelper.getPositionY(node);
					}
				}
			}
			double centerX = (maxX + minX) / 2;
			double centerY = (maxY + minY) / 2;
			
			// centering nodes around (0,0)
			for (Node node : nodes) {
				AttributeHelper.setPosition(node,
						AttributeHelper.getPositionX(node) - centerX,
						AttributeHelper.getPositionY(node) - centerY);
			}
			
			double radius = 0.0;
			Vector2d center = new Vector2d(0.0, 0.0);
			for (Node node : nodes) {
				double distance = center.distance(
						AttributeHelper.getPositionX(node),
						AttributeHelper.getPositionY(node));
				if (distance > radius) {
					radius = distance;
				}
			}
			subsystem2RadiusMap.put(subsystem, radius);
		}
		
		boolean overlapping = true;
		double radius = 900.0;
		int numberOfSubsystems = subsystemMap.keySet().size();
		
		while (overlapping) {
			radius += 100.0;
			double center = radius + 100.0;
			int i = 0;
			for (String subsystem : subsystemMap.keySet()) {
				subsystem2NewCenterMap.put(subsystem, new Vector2d(
						center + radius * Math.cos((((double) i) / ((double) numberOfSubsystems)) * 2 * Math.PI),
						center + radius * Math.sin((((double) i) / ((double) numberOfSubsystems)) * 2 * Math.PI)));
				i++;
			}
			boolean overlappingTemp = false;
			for (String subsystem1 : subsystemMap.keySet()) {
				for (String subsystem2 : subsystemMap.keySet()) {
					if (!subsystem1.equals(subsystem2)) {
						double centerDistance = subsystem2NewCenterMap.get(subsystem1).distance(subsystem2NewCenterMap.get(subsystem2));
						double sumOfRadii = subsystem2RadiusMap.get(subsystem1) + subsystem2RadiusMap.get(subsystem2);
						if (centerDistance < sumOfRadii) {
							overlappingTemp = true;
						}
					}
				}
			}
			overlapping = overlappingTemp;
		}
		
		for (String subsystem : subsystemMap.keySet()) {
			HashSet<Node> nodes = subsystemMap.get(subsystem);
			double x, y;
			for (Node node : nodes) {
				x = Double.parseDouble((String) AttributeHelper.getAttributeValue(node, "minerva", "minerva_x", 0, ""));
				y = Double.parseDouble((String) AttributeHelper.getAttributeValue(node, "minerva", "minerva_y", 0, ""));
				x += subsystem2NewCenterMap.get(subsystem).x;
				y += subsystem2NewCenterMap.get(subsystem).y;
				// increase space between nodes to reduce node overlap.
				x *= 2.0;
				y *= 2.0;
				AttributeHelper.setPosition(node, x, y);
			}
		}
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
		ZoomFitChangeComponent.zoomRegion(false, LMMEViewManagement.getInstance().getSubsystemFrame().getView());
		ZoomFitChangeComponent.zoomOut();
//			}
//		});
		
		EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		Selection selection = session.getSelectionModel().getActiveSelection();
		selection.clear();
		selection.addAll(nodesWithoutCoordinates);
		session.getSelectionModel().selectionChanged();
		
		final PatternSpringembedder pse = new PatternSpringembedder();
		JPanel pluginContent = new JPanel();
		ThreadSafeOptions optionsForPlugin = new ThreadSafeOptions();
		
		if (pse.setControlInterface(optionsForPlugin, pluginContent)) {
			for (int i = 0; i < pluginContent.getComponents().length; i++) {
				if ((pluginContent.getComponents()[i] instanceof JMButton)
						&& ((JMButton) pluginContent.getComponents()[i]).getText().equalsIgnoreCase("Layout Network")) {
					((JMButton) pluginContent.getComponents()[i]).doClick();
				}
			}
		}
		
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	public static String name() {
		return "Minerva Layout";
	}
	
}
