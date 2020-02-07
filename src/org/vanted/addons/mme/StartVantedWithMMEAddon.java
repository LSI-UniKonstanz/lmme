package org.vanted.addons.mme;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * 
 * @author Tobias Czauderna
 *
 */
@SuppressWarnings("nls")
public class StartVantedWithMMEAddon {
	
	public static void main(String[] args) {
		
		System.out.println("Starting VANTED with " + getAddonName() + " Add-on");
		Main.startVanted(args, getAddonNameXML());
		
	}
	
	public static String getAddonName() {
		
		return "Metabolic Model Explorer";
		
	}
	
	public static String getAddonNameXML() {
		
		return "mme.xml";
		
	}
	
}
