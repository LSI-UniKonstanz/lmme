package org.vanted.addons.lmme.core;

import java.util.ArrayList;
import java.util.Arrays;

public class LMMEConstants {

	public static final String ATTRIBUTE_PATH = "lmme";
	public static final String SUBSYSTEM = "SUBSYSTEM";
	public static final String KEGGSEP = "///";

//	public static final String DECOMP_KEGG = "Decomposition by KEGG Annotation";
//	public static final String DECOMP_PREDEFINED = "Predefined Decomposition";
//
//	public static final String DECOMP_SCHUSTER = "Decomposition by Schuster et al.";
//	public static final String DECOMP_GIRVAN = "Decomposition by Girvan et al.";

	public static final String LAYOUT_FORCEDIR = "Force-Directed Layout";
	public static final String LAYOUT_CIRCULAR = "Circular Layout";
	public static final String LAYOUT_CONCENTRIC_CIRC = "Concentric Circles Layout";
	public static final String LAYOUT_PARALLEL_LINES = "Parallel Lines Layout";

	public static final String TRANSPORTER_SUBSYSTEM = "Transport Reactions (algorithmically derived)";
	public static final String DEFAULT_SUBSYSTEM = "Default Subsystem (algorithmically derived)";

	/**
	 * These are the global and overview pathway maps from KEGG. They are not
	 * considered to be subsystems in our system, as they do not refer to actual
	 * functional subunits. Thus we ignore them in the query result.
	 */
	public static final ArrayList<String> INEGLIGIBLE_KEGG_PATHWAYS = new ArrayList<>(
			Arrays.asList(new String[] { "Metabolic pathways", "Biosynthesis of secondary metabolites",
					"Microbial metabolism in diverse environments", "Biosynthesis of antibiotics", "Carbon metabolism",
					"2-Oxocarboxylic acid metabolism", "Fatty acid metabolism", "Degradation of aromatic compounds",
					"Biosynthesis of amino acids" }));

}
