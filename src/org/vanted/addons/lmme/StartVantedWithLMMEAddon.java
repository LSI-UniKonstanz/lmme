/*******************************************************************************
 * LMME is a VANTED Add-on for the exploration of large metabolic models.
 * Copyright (C) 2020 Chair for Life Science Informatics, University of Konstanz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.vanted.addons.lmme;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * This class is required by VANTED in order to start the LMME Add-on.
 *
 * @author Tobias Czauderna
 * @author Michael Aichem
 */
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
