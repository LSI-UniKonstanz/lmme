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
package org.vanted.addons.lmme.decomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.graffiti.graph.Node;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;

/**
 * This class represents the decomposition of a model. It maintains the list of
 * derived subsystems and mappings between species/reactions and the subsystems.
 *
 * @author Michael Aichem
 */
public class MMDecomposition {

	/**
	 * The list of reactions that have been classified to belong to any subsystem.
	 * The list entries are references to the respective reaction nodes in the base
	 * graph ({@link BaseGraph.graph}).
	 */
	// private HashSet<Node> classifiedReactions;

	private HashMap<Node, ArrayList<SubsystemGraph>> speciesSubsystemsMap;

	private HashMap<Node, ArrayList<SubsystemGraph>> reactionSubsystemsMap;

	private ArrayList<SubsystemGraph> subsystems;

	public MMDecomposition(ArrayList<SubsystemGraph> subsystems) {

		this.subsystems = new ArrayList<>();
		speciesSubsystemsMap = new HashMap<>();
		reactionSubsystemsMap = new HashMap<>();

		for (SubsystemGraph subsystem : subsystems) {
			this.addSubsystem(subsystem);
		}
	}

	public ArrayList<SubsystemGraph> getSubsystems() {
		return subsystems;
	}

	public boolean hasReactionBeenClassified(Node reactionNode) {
		return reactionSubsystemsMap.containsKey(reactionNode);
	}

	/**
	 * 
	 * @param subsystem
	 */
	public void addSubsystem(SubsystemGraph subsystem) {

		this.subsystems.add(subsystem);

		for (Node speciesNode : subsystem.getSpeciesNodes()) {
			if (speciesSubsystemsMap.containsKey(speciesNode)) {
				speciesSubsystemsMap.get(speciesNode).add(subsystem);
			} else {
				ArrayList<SubsystemGraph> arrayList = new ArrayList<>();
				arrayList.add(subsystem);
				speciesSubsystemsMap.put(speciesNode, arrayList);
			}
		}

		for (Node reactionNode : subsystem.getReactionNodes()) {
			if (reactionSubsystemsMap.containsKey(reactionNode)) {
				reactionSubsystemsMap.get(reactionNode).add(subsystem);
			} else {
				ArrayList<SubsystemGraph> arrayList = new ArrayList<>();
				arrayList.add(subsystem);
				reactionSubsystemsMap.put(reactionNode, arrayList);
			}
		}
	}

	/**
	 * Returns an arraylist that contains the subsystems that this species belongs
	 * to, or null if it does not belong to any.
	 * 
	 * @param speciesNode
	 * @return
	 */
	public ArrayList<SubsystemGraph> getSubsystemsForSpecies(Node speciesNode) {
		return this.speciesSubsystemsMap.get(speciesNode);
	}

	/**
	 * Returns an arraylist that contains the subsystems that this reaction belongs
	 * to, or null if it does not belong to any.
	 * 
	 * @param reactionNode
	 * @return
	 */
	public ArrayList<SubsystemGraph> getSubsystemsForReaction(Node reactionNode) {
		return this.reactionSubsystemsMap.get(reactionNode);
	}

}
