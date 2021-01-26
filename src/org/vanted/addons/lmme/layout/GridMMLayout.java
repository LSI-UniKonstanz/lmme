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

import org.graffiti.graph.Graph;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.graphs.OverviewGraph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;

/**
 * A grid-layout for the {@link OverviewGraph}.
 * <p>
 * The vertices are placed on a grid (integer coordinates) to improve
 * readability and reduce clutter.
 *
 * @author Michael Aichem
 */
public class GridMMLayout implements MMOverviewLayout {
	
	@Override
	public void layOutAsOverview(Graph graph) {
		
		double distance = LMMEController.getInstance().getCurrentSession().getOverviewGraph().getSubsystemNodeSize() * 2;
		GridLayouterAlgorithm.layoutOnGrid(graph.getNodes(), 0.5, distance, distance);
		
	}
	
	@Override
	public String getName() {
		return "Grid";
	}
	
}
