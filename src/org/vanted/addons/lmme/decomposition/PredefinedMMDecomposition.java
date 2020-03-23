package org.vanted.addons.lmme.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.FolderPanel;
import org.GuiRow;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.core.LMMESession;
import org.vanted.addons.lmme.core.LMMETools;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;
import org.vanted.addons.lmme.ui.LMMETab;

public class PredefinedMMDecomposition extends MMDecompositionAlgorithm {

	private JComboBox<String> cbTag;
	private GuiRow tagRow;
	private FolderPanel fp;

	private final String ATTRIBUTE_NAME = "predefinedSubsystem";

	/**
	 * 
	 * 
	 * @return
	 */
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {

		LMMETools.getInstance().readNotes(this.getSelectedTag(), this.ATTRIBUTE_NAME);

		return determineSubsystemsFromReactionAttributes(this.ATTRIBUTE_NAME, false, "", alreadyClassifiedNodes);

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

		if (this.fp != null) {
			updateFolderPanel();
		} else {
			fp = new FolderPanel(getName() + " Settings", false, true, false, null);

			this.cbTag = createComboBox();
			JPanel tagLine = LMMETab.combine(new JLabel("SBML Tag:"), this.cbTag, Color.WHITE, false, true);
			this.tagRow = new GuiRow(tagLine, null);
			fp.addGuiComponentRow(this.tagRow, true);
		}
		return fp;
	}

	/**
	 * 
	 */
	public void updateFolderPanel() {
		this.cbTag = createComboBox();
		JPanel tagLine = LMMETab.combine(new JLabel("SBML Tag:"), this.cbTag, Color.WHITE, false, true);
		this.tagRow.left = tagLine;
		this.fp.layoutRows();
	}

	private JComboBox<String> createComboBox() {
		JComboBox<String> cb;
		if (LMMEController.getInstance().getCurrentSession().isModelSet()) {
			cb = new JComboBox<String>(
					LMMEController.getInstance().getCurrentSession().getBaseGraph().getAvailableNotes());
		} else {
			cb = new JComboBox<String>();
		}
		for (int i = 0; i < cb.getItemCount(); i++) {
			if (cb.getItemAt(i).equalsIgnoreCase("SUBSYSTEM")) {
				cb.setSelectedIndex(i);
			}
		}
		return cb;
	}

	private String getSelectedTag() {
		return (String) this.cbTag.getSelectedItem();
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getName() {
		return "Predefined Annotation";
	}
	
	public boolean requiresTransporterSubsystem() {
		return false;
	}

}
