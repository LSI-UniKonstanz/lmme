package org.vanted.addons.lmme.layout;

import org.graffiti.graph.Graph;
import org.vanted.addons.lmme.core.LMMEController;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;

public class GridMMLayout implements MMOverviewLayout {

	@Override
	public void layOutAsOverview(Graph graph) {

		double distance = LMMEController.getInstance().getCurrentSession().getOverviewGraph().getNodeSize() * 2;
		GridLayouterAlgorithm.layoutOnGrid(graph.getNodes(), 0.5, distance, distance);

	}

	@Override
	public String getName() {
		return "Grid";
	}

}
