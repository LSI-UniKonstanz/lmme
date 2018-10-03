package org.vanted.addons.wholecell;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

@SuppressWarnings("nls")
public class StartVantedWithWholecellAddon {
	
	public static void main(String[] args) {
		
		System.out.println("Starting VANTED with " + getAddonName() + " Add-on");
		Main.startVanted(args, getAddonNameXML());
		
	}
	
	public static String getAddonName() {
		
		return "Whole-cell";
		
	}
	
	public static String getAddonNameXML() {
		
		return "wholecell.xml";
		
	}
	
}
