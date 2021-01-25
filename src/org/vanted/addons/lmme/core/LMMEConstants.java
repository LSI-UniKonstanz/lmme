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
package org.vanted.addons.lmme.core;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class stores the constants that are used in the remaining code.
 * 
 * @author Tobias Czauderna
 * @author Michael Aichem
 */
public class LMMEConstants {
	
	public static final String ATTRIBUTE_PATH = "lmme";
	public static final String SUBSYSTEM = "SUBSYSTEM";
	public static final String KEGGSEP = "///";
	
	public static final String LAYOUT_FORCEDIR = "Force-Directed Layout";
	public static final String LAYOUT_CIRCULAR = "Circular Layout";
	public static final String LAYOUT_CONCENTRIC_CIRC = "Concentric Circles Layout";
	public static final String LAYOUT_PARALLEL_LINES = "Parallel Lines Layout";
	
	public static final String TRANSPORTER_SUBSYSTEM = "Transport Reactions (algorithmically derived)";
	public static final String DEFAULT_SUBSYSTEM = "Default Subsystem (algorithmically derived)";
	
	public static final String DISEASE_MAP_PATHWAY_ATRIBUTE = "dmPathway";
	
	/**
	 * These are the global and overview pathway maps from KEGG.
	 * <p>
	 * They are not considered to be subsystems in our system, as they do not refer to actual
	 * functional subunits. Thus we ignore them in the query result.
	 */
	public static final ArrayList<String> INEGLIGIBLE_KEGG_PATHWAYS = new ArrayList<>(
			Arrays.asList(new String[] { "Metabolic pathways", "Biosynthesis of secondary metabolites",
					"Microbial metabolism in diverse environments", "Biosynthesis of antibiotics", "Carbon metabolism",
					"2-Oxocarboxylic acid metabolism", "Fatty acid metabolism", "Degradation of aromatic compounds",
					"Biosynthesis of amino acids" }));
	
}
