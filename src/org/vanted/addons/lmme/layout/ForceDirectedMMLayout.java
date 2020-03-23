package org.vanted.addons.lmme.layout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.JMButton;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.vanted.addons.lmme.ui.LMMEViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class ForceDirectedMMLayout implements MMOverviewLayout, MMSubsystemLayout {

	
	/**
	 * 
	 */
	public void layOutAsSubsystems(Graph graph) {
		
		LMMEViewManagement.getInstance().ensureSubsystemViewActive();
		layOut(graph);
		
	}

	/**
	 * 
	 */
	public void layOutAsOverview(Graph graph) {
		
		LMMEViewManagement.getInstance().ensureOverviewActive();
		layOut(graph);
		
	}
	
	
	
	/**
	 * 
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
				if ((pluginContent.getComponents()[i] instanceof JMButton) && ((JMButton) pluginContent.getComponents()[i]).getText().equalsIgnoreCase("Layout Network")) {
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

	/**
	 * 
	 */
	public String getName() {
		return name();
	}
	
	public static String name() {
		return "Force-Directed";
	}

}
