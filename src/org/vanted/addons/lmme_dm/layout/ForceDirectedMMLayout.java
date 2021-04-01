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

import javax.swing.JPanel;

import org.JMButton;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.vanted.addons.lmme_dm.graphs.OverviewGraph;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;
import org.vanted.addons.lmme_dm.ui.LMMEViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;

/**
 * A force-directed layout for the {@link OverviewGraph} as well as a
 * {@link SubsystemGraph}.
 * <p>
 * Uses the implementation of the Spring Embedder, that is available in Vanted.
 * The main idea is to use a physical analogy to create a visually appealing
 * drawing of a graph. Physical forces are assumed in two ways: attractive force
 * between adjacent vertices and repulsive forces between any pair of vertices.
 * Node movement according to these forces is then computed iteratively until a
 * force equilibrium is reached. See the publication below for more details.
 * Eades, P. (1984). A heuristic for graph drawing. Congressus numerantium, 42,
 * 149-160.
 *
 * @author Michael Aichem
 */
public class ForceDirectedMMLayout implements MMOverviewLayout, MMSubsystemLayout {
	
	@Override
	public void layOutAsSubsystems(Graph graph) {
		
		LMMEViewManagement.getInstance().ensureSubsystemViewActive();
		layOut(graph);
		
	}
	
	@Override
	public void layOutAsOverview(Graph graph) {
		
		LMMEViewManagement.getInstance().ensureOverviewActive();
		layOut(graph);
		
	}
	
	/**
	 * Performs the actual layout.
	 * <p>
	 * Uses the {@code PatternSpringembedder} available in VANTED.
	 * 
	 * @param graph
	 *           the graph to be laid out
	 */
	public void layOut(Graph graph) {
		
//		ThreadSafeOptions threadSafeOptions = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
//		threadSafeOptions.doFinishMoveToTop = true;
//		threadSafeOptions.doFinishRemoveOverlapp = true;
//		threadSafeOptions.doRemoveAllBends = true;
//		threadSafeOptions.temperature_max_move = 300;
//		// default value for temp_alpha is 0.998, not really clear if the value 0.98
//		// improves the layout or not
//		// threadSafeOptions.temp_alpha = 0.98;
//		threadSafeOptions.setDval(myOp.DvalIndexSliderZeroLength, 100);
//		threadSafeOptions.setDval(myOp.DvalIndexSliderHorForce, 90000);
//		threadSafeOptions.setDval(myOp.DvalIndexSliderVertForce, 90000);
//		Selection selection = new Selection(graph.getGraphElements());
		
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
		
		ConnectedComponentLayout.layoutConnectedComponents(graph);
		
//		Thread newBackgroundThread = new Thread(new Runnable() {
//			public void run() {
//				threadSafeOptions.setGraphInstance(graph);
//				threadSafeOptions.setSelection(selection);
//				pse.executeThreadSafe(threadSafeOptions);
//			}
//		}) {
//		};
//		if (MMEViewManagement.getInstance().getSubsystemFrame() != null && MMEViewManagement.getInstance().getSubsystemFrame().getView() instanceof GraffitiView)
//			((GraffitiView) MMEViewManagement.getInstance().getSubsystemFrame().getView()).setDrawMode(DrawMode.FAST);
//
//		newBackgroundThread.setName("SpringEmbedderLayout");
//		newBackgroundThread.setPriority(Thread.MIN_PRIORITY);
//		newBackgroundThread.start();
		
//		MyNonInteractiveSpringEmb nonInteractiveSpringEmbedder = new MyNonInteractiveSpringEmb(graph, selection,
//				threadSafeOptions);
//		
//		// run without background task
//		// nonInteractiveSpringEmbedder.run();
//		// run in background task
//		
//		BackgroundTaskHelper bth = new BackgroundTaskHelper(nonInteractiveSpringEmbedder, nonInteractiveSpringEmbedder, "Force Directed Layout", "Force Directed Layout",
//				true, false);
//		bth.startWork(this);
//		try {
//            Thread.sleep(20000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//		BackgroundTaskHelper.issueSimpleTask("LayoutModel", "", nonInteractiveSpringEmbedder, null,
//				nonInteractiveSpringEmbedder);
		
	}
	
	@Override
	public String getName() {
		return "Force-Directed";
	}
	
}
