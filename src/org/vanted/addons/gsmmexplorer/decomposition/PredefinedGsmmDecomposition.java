package org.vanted.addons.gsmmexplorer.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.FolderPanel;
import org.graffiti.graph.Node;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerController;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerSession;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerTools;
import org.vanted.addons.gsmmexplorer.graphs.BaseGraph;
import org.vanted.addons.gsmmexplorer.graphs.SubsystemGraph;
import org.vanted.addons.gsmmexplorer.ui.GsmmExplorerTab;

public class PredefinedGsmmDecomposition extends GsmmDecompositionAlgorithm {

	private JTextField tag;
	private JTextField separator;

	private final String ATTRIBUTE_NAME = "predefinedSubsystem";

	/**
	 * 
	 * 
	 * @return
	 */
	protected ArrayList<SubsystemGraph> runSpecific() {
		
		GsmmExplorerTools.getInstance().readNotes(this.tag.getText().trim(), this.ATTRIBUTE_NAME);

		return determineSubsystemsFromReactionAttributes(this.ATTRIBUTE_NAME, this.separator.getText());

		// initialise subsystem with empty lists, then
		// successively add species and reactions.

	}

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean requiresCloning() {
		return true;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName() + " Settings", false, true, false, null);

		JLabel lblTag = new JLabel("SBML Tag:");
		this.tag = new JTextField(10);
		this.tag.setText("SUBSYSTEM");
		JPanel tagLine = GsmmExplorerTab.combine(lblTag, this.tag, Color.WHITE, false, true);
		fp.addGuiComponentRow(tagLine, null, true);

		JLabel lblSeparator = new JLabel("Separator:");
		this.separator = new JTextField(5);
		this.separator.setText(",");
		JPanel separatorLine = GsmmExplorerTab.combine(lblSeparator, this.separator, Color.WHITE, false, true);
		fp.addGuiComponentRow(FolderPanel.getBorderedComponent(separatorLine, 5, 0, 0, 0), null, true);

		return fp;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getName() {
		return "Predefined Annotation";
	}

}
