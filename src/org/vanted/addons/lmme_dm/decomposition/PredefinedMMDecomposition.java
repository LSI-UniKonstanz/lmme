/*******************************************************************************
 * LMME is a VANTED Add-on for the exploration of large metabolic models.
 * Copyright (C) 2020 Chair for Life Science Informatics, University of Konstanz
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.vanted.addons.lmme_dm.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.FolderPanel;
import org.GuiRow;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;
import org.vanted.addons.lmme_dm.ui.LMMETab;

/**
 * This method is based on an existing decomposition of the model, which is
 * contained in the underlying SBML file of the model in the form of reaction
 * notes.
 * <p>
 * Reactions in this case are required to have assigned the name of the
 * subsystem that they belong to.
 *
 * @author Michael Aichem
 */
public class PredefinedMMDecomposition extends MMDecompositionAlgorithm {
	
	private JComboBox<String> cbTag;
	private GuiRow tagRow;
	private FolderPanel fp;
	
	private HashMap<String, String> notesShort2longForm = new HashMap<String, String>();
	private HashMap<String, String> notesLong2ShortForm = new HashMap<String, String>();
	
	private final String ATTRIBUTE_NAME = "predefinedSubsystem";
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		
		LMMETools.getInstance().readNotes(this.getSelectedTag(), this.ATTRIBUTE_NAME);
		
		return determineSubsystemsFromReactionAttributes(this.ATTRIBUTE_NAME, false, "", alreadyClassifiedNodes);
		
	}
	
	@Override
	public boolean requiresCloning() {
		return true;
	}
	
	@Override
	public FolderPanel getFolderPanel() {
		
		if (this.fp != null) {
			updateFolderPanel();
		} else {
			fp = new FolderPanel(getName() + " Settings", false, true, false, null);
			
			this.cbTag = createComboBox();
			JPanel tagLine = LMMETab.combine(new JLabel("SBML Note:"), this.cbTag, Color.WHITE, false, true);
			this.tagRow = new GuiRow(tagLine, null);
			fp.addGuiComponentRow(this.tagRow, true);
		}
		return fp;
	}
	
	@Override
	public void updateFolderPanel() {
		if (this.cbTag == null) {
			this.cbTag = createComboBox();
		} else {
			String currentVal = getSelectedTag();
			this.cbTag = createComboBox(currentVal);
		}
		JPanel tagLine = LMMETab.combine(new JLabel("SBML Note:"), this.cbTag, Color.WHITE, false, true);
		this.tagRow.left = tagLine;
		this.fp.layoutRows();
	}
	
	private JComboBox<String> createComboBox() {
		JComboBox<String> cb;
		if (LMMEController.getInstance().getCurrentSession().isModelSet()) {
			String[] availableNotes = LMMEController.getInstance().getCurrentSession().getBaseGraph().getAvailableNotes();
			notesLong2ShortForm.clear();
			notesShort2longForm.clear();
			String[] cbReadyNotes = new String[availableNotes.length];
			for (int i = 0; i < availableNotes.length; i++) {
				String str = availableNotes[i];
				if (str.length() <= 30) {
					notesLong2ShortForm.put(str, str);
					notesShort2longForm.put(str, str);
					cbReadyNotes[i] = str;
				} else {
					String strShortened = str.substring(0, 30) + "...";
					notesLong2ShortForm.put(str, strShortened);
					notesShort2longForm.put(strShortened, str);
					cbReadyNotes[i] = strShortened;
				}
			}
			cb = new JComboBox<String>(cbReadyNotes);
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
	
	private JComboBox<String> createComboBox(String initiallySelectedValue) {
		JComboBox<String> cb = createComboBox();
		for (int i = 0; i < cb.getItemCount(); i++) {
			if (cb.getItemAt(i).equalsIgnoreCase(initiallySelectedValue)) {
				cb.setSelectedIndex(i);
			}
		}
		return cb;
	}
	
	private String getSelectedTag() {
		if (this.cbTag == null) {
			return "";
		} else {
			return notesShort2longForm.get((String) this.cbTag.getSelectedItem());
		}
	}
	
	@Override
	public String getName() {
		return "Predefined Decomposition";
	}
	
	@Override
	public boolean requiresTransporterSubsystem() {
		return false;
	}
	
}
