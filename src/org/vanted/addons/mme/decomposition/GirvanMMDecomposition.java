package org.vanted.addons.mme.decomposition;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JLabel;

import org.FolderPanel;
import org.graffiti.graph.Node;
import org.vanted.addons.mme.graphs.SubsystemGraph;

public class GirvanMMDecomposition extends MMDecompositionAlgorithm {

	/**
	 * 
	 */
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		return null;
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	public boolean requiresCloning() {
		return true;
	}

	/**
	 * 
	 */
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName(), false, true, false, null);
		
		return fp;
	}
	
	/**
	 * 
	 */
	public void updateFolderPanel() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public String getName() {
		return "Girvan et al.";
	}

}
