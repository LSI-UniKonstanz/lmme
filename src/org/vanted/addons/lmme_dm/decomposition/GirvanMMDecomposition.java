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
package org.vanted.addons.lmme_dm.decomposition;

import java.util.ArrayList;
import java.util.HashSet;

import org.FolderPanel;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;

/**
 * This class is an implementation of the method that has
 * been proposed by Girvan et al.
 * <p>
 * It was published in the following paper:
 * Girvan, M., Newman, M. E. (2002). Community structure in social and
 * biological networks. Proceedings of the national academy of sciences, 99(12),
 * 7821-7826.
 *
 * @author Michael Aichem
 */
public class GirvanMMDecomposition extends MMDecompositionAlgorithm {
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		return null;
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean requiresCloning() {
		return true;
	}
	
	@Override
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName(), false, true, false, null);
		
		return fp;
	}
	
	@Override
	public void updateFolderPanel() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getName() {
		return "Girvan et al.";
	}
	
	@Override
	public boolean requiresTransporterSubsystem() {
		return false;
	}
	
}
