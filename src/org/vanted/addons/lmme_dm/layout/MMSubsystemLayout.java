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

import org.graffiti.graph.Graph;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;

/**
 * Interface for a layout method for the {@link SubsystemGraph}.
 *
 * @author Michael Aichem
 */
public interface MMSubsystemLayout {
	
	/**
	 * Lays out the given graph in the subsystems view.
	 * 
	 * @param graph
	 *           the graph to be laid out.
	 */
	public void layOutAsSubsystems(Graph graph);
	
	/**
	 * Gets the name of the layout algorithm.
	 * 
	 * @return the name of the layout algorithm
	 */
	public String getName();
	
}
