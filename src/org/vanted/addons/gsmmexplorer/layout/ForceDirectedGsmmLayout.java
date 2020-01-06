package org.vanted.addons.gsmmexplorer.layout;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class ForceDirectedGsmmLayout implements GsmmOverviewLayout, GsmmSubsystemLayout {

	/**
	 * 
	 */
	public void layOut(Graph graph) {
		
		ThreadSafeOptions threadSafeOptions = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
		threadSafeOptions.doFinishMoveToTop = true;
		threadSafeOptions.doFinishRemoveOverlapp = true;
		threadSafeOptions.doRemoveAllBends = true;
		threadSafeOptions.temperature_max_move = 300;
		// default value for temp_alpha is 0.998, not really clear if the value 0.98
		// improves the layout or not
		// threadSafeOptions.temp_alpha = 0.98;
		threadSafeOptions.setDval(myOp.DvalIndexSliderZeroLength, 100);
		threadSafeOptions.setDval(myOp.DvalIndexSliderHorForce, 90000);
		threadSafeOptions.setDval(myOp.DvalIndexSliderVertForce, 90000);
		Selection selection = new Selection(graph.getGraphElements());
		MyNonInteractiveSpringEmb nonInteractiveSpringEmbedder = new MyNonInteractiveSpringEmb(graph, selection,
				threadSafeOptions);
		// run without background task
		// nonInteractiveSpringEmbedder.run();
		// run in background task
		BackgroundTaskHelper.issueSimpleTask("LayoutModel", "", nonInteractiveSpringEmbedder, null,
				nonInteractiveSpringEmbedder);
//		ConnectedComponentLayout ccl = new ConnectedComponentLayout();
//		ccl.attach(graph, new Selection(graph.getGraphElements()));
//		ccl.setParameters(new Parameter[] {});
//		ccl.execute();
		
		
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
