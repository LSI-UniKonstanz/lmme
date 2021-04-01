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
package org.vanted.addons.lmme_dm;

import org.graffiti.plugin.inspector.InspectorTab;
import org.vanted.addons.lmme_dm.ui.LMMETab;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * This class is required by VANTED in order to recognize the LMME Add-on.
 * <p>
 * LMME allows to decompose a large metabolic network into smaller subsystems.
 * The decomposition is not only performed in the background but the system
 * produces an overview graph that consists of individual subsystems. A
 * subsystem formally contains a subgraph of the original graph. The user can
 * choose several different approaches to decompose the initial network, e.g.
 * based on notes in the underlying SBML file (either direct subsystem notes -
 * or KEGG reaction id notes that can then be resolved to individual pathways
 * via KEGG API requests). There are also structural decomposition methods that
 * do not expect any notes or annotations within the underlying SBML file.
 * Finally, there is an overview graph and several subsystem graphs. The
 * subsystem graphs may then be visualised on demand by selecting them in the
 * overview graph.
 * 
 * @author Tobias Czauderna
 * @author Michael Aichem
 */
public class LMMEAddon extends AddonAdapter {
	
	@Override
	protected void initializeAddon() {
		
		this.tabs = new InspectorTab[] { new LMMETab() };
		
	}
	
}
