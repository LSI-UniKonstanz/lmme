/**
 * 
 */
package org.vanted.addons.lmme.decomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.graffiti.graph.Node;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;

/**
 * @author Michael Aichem
 *
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

	// public void setSubsystems(ArrayList<SubsystemGraph> subsystems) {
	// this.subsystems = subsystems;
	// }

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

	// TDOD add subsystems etc.

	// TODO possibility to freeze?

}
