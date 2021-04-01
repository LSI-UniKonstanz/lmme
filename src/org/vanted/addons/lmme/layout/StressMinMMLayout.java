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
package org.vanted.addons.lmme.layout;

import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.vanted.addons.lmme.graphs.OverviewGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;
import org.vanted.addons.lmme.ui.LMMEViewManagement;
import org.vanted.indexednodes.IndexedComponent;
import org.vanted.indexednodes.IndexedGraphOperations;
import org.vanted.indexednodes.IndexedNodeSet;
import org.vanted.plugins.layout.stressminimization.StressMinimizationImplementation;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;

/**
 * A stress minimization layout for the {@link OverviewGraph} as well as a
 * {@link SubsystemGraph}.
 * <p>
 * Uses the implementation that is available in Vanted and was developed by the student groups 2.1 and 2.2 in the software project course at university of
 * Konstanz during summer term 2019.
 * Based on the paper:
 * Emden R. Gansner, Yehuda Koren and Stephen North. Graph Drawing by Stress Majorization. AT&T Labs Research, Florham Park, NJ 07932, 2005.
 * 
 * @author Michael Aichem
 */
public class StressMinMMLayout implements MMOverviewLayout, MMSubsystemLayout {
	
	@Override
	public void layOutAsSubsystems(Graph graph) {
		layOut(graph);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ZoomFitChangeComponent.zoomRegion(false, LMMEViewManagement.getInstance().getSubsystemFrame().getView());
				ZoomFitChangeComponent.zoomOut();
			}
		});
	}
	
	@Override
	public void layOutAsOverview(Graph graph) {
		layOut(graph);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ZoomFitChangeComponent.zoomRegion(false, LMMEViewManagement.getInstance().getOverviewFrame().getView());
				ZoomFitChangeComponent.zoomOut();
			}
		});
	}
	
	private void layOut(Graph graph) {
		IndexedNodeSet workNodes = IndexedNodeSet.setOfAllIn(graph.getNodes());
		List<IndexedComponent> components = IndexedGraphOperations.getComponents(workNodes);
		HashMap<Node, Vector2d> nodes2NewPositions = new HashMap<>();
		RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
		rla.attach(graph, new Selection(graph.getGraphElements()));
//		rla.attach(graph, new Selection());
		rla.execute();
		
		for (IndexedComponent component : components) {
			StressMinimizationImplementation impl = new StressMinimizationImplementation(
					component.nodes,
					null,
					false,
					component.nodes.size(),
					2,
					0.001,
					0,
					0,
					350,
					3);
			impl.calculateLayout();
			nodes2NewPositions.putAll(impl.getLayoutSupplier().get());
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodes2NewPositions, "Stress Minimization");
//		
		GravistoService.getInstance().runAlgorithm(
				new CenterLayouterAlgorithm(),
				graph,
				new Selection(graph.getGraphElements()),
				null);
		ConnectedComponentLayout.layoutConnectedComponents(graph);
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	public static String name() {
		return "Stress-Minimization";
	}
	
}
