package org.vanted.addons.lmme;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * 
 * @author Tobias Czauderna
 *
 */
@SuppressWarnings("nls")
public class StartVantedWithLMMEAddon {
	
	public static void main(String[] args) {
		
		System.out.println("Starting VANTED with " + getAddonName() + " Add-on");
		Main.startVanted(args, getAddonNameXML());
		
	}
	
	public static String getAddonName() {
		
		return "LMME";
		
	}
	
	public static String getAddonNameXML() {
		
		return "lmme.xml";
		
	}
	
}
