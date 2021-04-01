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

import javax.swing.JLabel;

import org.FolderPanel;
import org.GuiRow;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEConstants;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;

/**
 * A decomposition method, specifically designed to build the decomposition from the pathways that have been merged.
 * 
 * @author Michael Aichem
 */
public class DiseaseMapPathwayDecomposition extends MMDecompositionAlgorithm {
	
	private FolderPanel fp;
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		
		return determineSubsystemsFromReactionAttributes(LMMEConstants.DISEASE_MAP_PATHWAY_ATRIBUTE, false, "", alreadyClassifiedNodes);
		
	}
	
	@Override
	public boolean requiresCloning() {
		
		return true;
		
	}
	
	@Override
	public boolean requiresTransporterSubsystem() {
		
		return false;
		
	}
	
	@Override
	public FolderPanel getFolderPanel() {
		
		if (this.fp != null) {
			updateFolderPanel();
		} else {
			fp = new FolderPanel(getName() + " Settings", false, true, false, null);
			GuiRow row = new GuiRow(new JLabel("No settings."), null);
			fp.addGuiComponentRow(row, true);
		}
		return fp;
		
	}
	
	@Override
	public void updateFolderPanel() {
		
		// Do nothing.
		
	}
	
	@Override
	public String getName() {
		
		return "Disease Map Pathway Decomposition";
		
	}
	
}
