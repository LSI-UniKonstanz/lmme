package org.vanted.addons.gsmmexplorer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * 
 * @author Tobias Czauderna
 *
 */
@SuppressWarnings("nls")
public class StartVantedWithGsmmExplorerAddon {
	
	public static void main(String[] args) {
		
		System.out.println("Starting VANTED with " + getAddonName() + " Add-on");
		Main.startVanted(args, getAddonNameXML());
		
	}
	
	public static String getAddonName() {
		
		return "GSMM Explorer";
		
	}
	
	public static String getAddonNameXML() {
		
		return "gsmm-explorer.xml";
		
	}
	
}
