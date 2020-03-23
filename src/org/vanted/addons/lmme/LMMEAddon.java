package org.vanted.addons.lmme;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.inspector.InspectorTab;
import org.vanted.addons.lmme.ui.LMMETab;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

/**
 * TODO: re-write! This is the main class of the LMME (Large Metabolic Model
 * Explorer) Add-On. This Add-On allows to decompose a huge metabolic network
 * into smaller modules. The decomposition is not only performed in the
 * background but the system produces an overview network that consists of
 * individual subsystems. A subsystem formally contains a (most often) connected
 * subgraph of the original graph. The user can choose several different
 * approaches to decompose the initial network, e.g. based on annotations in the
 * underlying SBML file (either direct subsystem annotations - or KEGG reaction
 * id annotations that can then be resolved to individual pathways via KEGG API
 * requests). Afterwards there are heuristic approaches that decompose the
 * remaining, yet unclassified reactions in the network such that finally there
 * is an overview graph and several subsystem graphs. The subsystem graphs may
 * then be visualised on demand by selecting it in the overview graph.
 * 
 * @author Tobias Czauderna
 */
public class LMMEAddon extends AddonAdapter {

	@Override
	protected void initializeAddon() {

		this.tabs = new InspectorTab[] { new LMMETab() };

	}

}
