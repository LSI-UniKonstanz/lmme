package org.vanted.addons.wholecell;

import org.graffiti.plugin.inspector.InspectorTab;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

public class WholecellAddon extends AddonAdapter {
	
	@Override
	protected void initializeAddon() {
		
		this.tabs = new InspectorTab[] { new WholecellTab() };
		
	}
	
}
