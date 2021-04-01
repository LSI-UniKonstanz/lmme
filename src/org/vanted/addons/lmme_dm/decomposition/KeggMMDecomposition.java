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
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.ws.rs.core.MediaType;

import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEConstants;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.core.LMMESession;
import org.vanted.addons.lmme_dm.core.LMMETools;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;
import org.vanted.addons.lmme_dm.ui.LMMETab;

import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;
import info.clearthought.layout.TableLayout;

/**
 * This method uses the KEGG IDs, that have been assigned to the reactions, and
 * the information from KEGG, which reactions belong to which pathways, to
 * finally determine a set of KEGG pathways that are very likely to be present
 * in the model at hand.
 *
 * @author Michael Aichem
 * @author Tobias Czauderna
 */
public class KeggMMDecomposition extends MMDecompositionAlgorithm implements NeedsSwingThread {
	
	private JComboBox<String> cbTag;
	private GuiRow tagRow;
	private FolderPanel fp;
	
	private JTextField separator;
	private JLabel minimumNumberValue;
	private JSlider minimumNumberSlider;
	
	private HashMap<String, String> notesShort2longForm = new HashMap<String, String>();
	private HashMap<String, String> notesLong2ShortForm = new HashMap<String, String>();
	
	private final String ATTRIBUTE_NAME_KEGG_ID = "KeggID";
	
	private final String ATTRIBUTE_NAME_FINAL_SUBSYSTEM = "FinalSubsystem";
	
	/**
	 * The rest service for the KEGG requests.
	 */
	private RestService restService = new RestService("http://rest.kegg.jp/get/");
	
	private HashMap<Node, ArrayList<String>> node2possibleSubsystems;
	private HashMap<String, Integer> subsystem2number;
	
	private int packageStart;
	
	public KeggMMDecomposition() {
		this.node2possibleSubsystems = new HashMap<>();
		this.subsystem2number = new HashMap<>();
	}
	
	@Override
	protected ArrayList<SubsystemGraph> runSpecific(HashSet<Node> alreadyClassifiedNodes) {
		
		LMMETools.getInstance().readNotes(this.getSelectedTag(), this.ATTRIBUTE_NAME_KEGG_ID);
		
		request();
		
		while (!this.node2possibleSubsystems.isEmpty()) {
			updateSubsystemNumber();
			removeTooSmallSubsystems();
			removeNodesWithoutSubsystem();
			extractNodesFromHugestSubsystem();
		}
		
		return determineSubsystemsFromReactionAttributes(ATTRIBUTE_NAME_FINAL_SUBSYSTEM, false, "",
				alreadyClassifiedNodes);
	}
	
	/**
	 * For each reaction that has a KEGG ID, an HTTP request to KEGG is sent,
	 * querying the pathways that belong to the reaction.
	 * <p>
	 * These are then stored in the the HashMap {@link node2possibleSubsystems}.
	 */
	private void request() {
		
		LMMESession currentSession = LMMEController.getInstance().getCurrentSession();
		
		// Grab reactions with viable KEGG reaction id.
		ArrayList<Node> reactionsWithKeggId = new ArrayList<>();
		for (Node reactionNode : currentSession.getBaseGraph().getReactionNodes()) {
			String keggId = currentSession.getNodeAttribute(reactionNode, ATTRIBUTE_NAME_KEGG_ID);
			if ((keggId.length() > 0) && !(keggId.equals("null"))) {
				reactionsWithKeggId.add(reactionNode);
			}
		}
		
		ArrayList<Node> reactionPackage;
		String[] res;
		this.packageStart = 0;
		while (reactionsWithKeggId.size() - this.packageStart >= 10) {
			reactionPackage = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				reactionPackage.add(reactionsWithKeggId.get(this.packageStart + i));
			}
			res = requestPackage(reactionPackage);
			if (res.length == 10) {
				processPackageResults(reactionPackage, res);
			} else {
				requestAndProcessPackageSeparately(reactionPackage);
			}
			this.packageStart += 10;
			MainFrame.showMessage("So far " + packageStart + " of " + reactionsWithKeggId.size()
					+ " reactions have been queried from Kegg.", MessageType.PERMANENT_INFO);
		}
		reactionPackage = new ArrayList<>();
		for (int i = this.packageStart; i < reactionsWithKeggId.size(); i++) {
			reactionPackage.add(reactionsWithKeggId.get(i));
		}
		if (!reactionPackage.isEmpty()) {
			res = requestPackage(reactionPackage);
			if (res.length == reactionPackage.size()) {
				processPackageResults(reactionPackage, res);
			} else {
				requestAndProcessPackageSeparately(reactionPackage);
			}
		}
	}
	
	/**
	 * This method sends HTTP requests to KEGG for a whole package of up to 10
	 * reactions.
	 * 
	 * @param reactionPackage
	 *           The package of reaction nodes to be requested
	 * @return a list of request result Strings
	 */
	private String[] requestPackage(ArrayList<Node> reactionPackage) {
		String urlPostFix = "";
		for (Node reactionNode : reactionPackage) {
			String keggId = LMMEController.getInstance().getCurrentSession().getNodeAttribute(reactionNode,
					ATTRIBUTE_NAME_KEGG_ID);
			/*
			 * This forces the calling method to use requestAndProcessPackageSeparately
			 * instead as the latter can handle multiple reaction ids.
			 */
			if (keggId.contains(this.separator.getText().trim())) {
				return new String[0];
			}
			urlPostFix += "+rn:" + keggId;
		}
		urlPostFix = urlPostFix.substring(1);
		String response = (String) this.restService.makeRequest(urlPostFix, MediaType.TEXT_PLAIN_TYPE, String.class);
		if ((response == null)) {
			// TODO: proper evaluation of response once RestService returns a map instead of just a string
			ErrorMsg.addErrorMessage("KEGG Decomposition failed: could not retrieve pathway information for reactions from KEGG database.");
			return new String[0];
		}
		response = response.substring(0, response.length() - 3);
		return response.split(Pattern.quote(LMMEConstants.KEGGSEP));
	}
	
	/**
	 * This method stores the results from the KEGG requests in the map
	 * {@link node2possibleSubsystems}.
	 * <p>
	 * The method thus therefore expects two lists:
	 * <ul>
	 * <li>a list of reactions and
	 * <li>a list of request result Strings,
	 * </ul>
	 * that match position by position.
	 * <p>
	 * This method assumes both lists to have the same length.
	 * 
	 * @param reactionNodes
	 *           The nodes that have been requested
	 * @param requestResults
	 *           The results from the request
	 */
	private void processPackageResults(ArrayList<Node> reactionNodes, String[] requestResults) {
		for (int i = 0; i < reactionNodes.size(); i++) {
			Node reactionNode = reactionNodes.get(i);
			String response = requestResults[i];
			processSingleNode(reactionNode, response);
		}
	}
	
	/**
	 * This method separately requests reaction by reaction.
	 * <p>
	 * This method also expects a list of reaction nodes but in contrast to
	 * {@link requestPackage} it requests AND processes (stores the results in the
	 * map {@link node2possibleSubsystems}) the nodes one by one. This is necessary
	 * if one of the reactions did not yield request results during a previous call
	 * of {@link requestPackage} such that list sizes do not match or if a reaction
	 * has had assigned more than one KEGG id.
	 * 
	 * @param reactionPackage
	 *           A list of reaction nodes that will be requested and
	 *           directly processed.
	 */
	private void requestAndProcessPackageSeparately(ArrayList<Node> reactionPackage) {
		for (Node reactionNode : reactionPackage) {
			String keggId = LMMEController.getInstance().getCurrentSession().getNodeAttribute(reactionNode,
					ATTRIBUTE_NAME_KEGG_ID);
			ArrayList<String> severalIDs = new ArrayList<>();
			if (keggId.contains(this.separator.getText().trim())) {
				String[] splitted = keggId.split(Pattern.quote(this.separator.getText().trim()));
				for (int i = 0; i < splitted.length; i++) {
					severalIDs.add(splitted[i].trim());
				}
			} else {
				severalIDs.add(keggId);
			}
			
			for (String singleKeggRid : severalIDs) {
				String response = (String) this.restService.makeRequest("rn:" + singleKeggRid,
						MediaType.TEXT_PLAIN_TYPE, String.class);
				processSingleNode(reactionNode, response);
			}
		}
	}
	
	/**
	 * This method processes a single node.
	 * <p>
	 * This means that the given request result is
	 * interpreted in the context of the given reaction node and the respective
	 * pathway information from the request result. The pathways are then storedin
	 * the map {@link node2possibleSubsystems}.
	 * 
	 * @param reactionNode
	 *           the node that has been queried
	 * @param requestResult
	 *           the result of the KEGG query
	 */
	private void processSingleNode(Node reactionNode, String requestResult) {
		if (requestResult != null) {
			String[] arrLines = requestResult.split("\n");
			int lineIndex = 0;
			while (lineIndex < arrLines.length) {
				if (arrLines[lineIndex].startsWith("PATHWAY"))
					do {
						String line = arrLines[lineIndex];
						line = line.substring(line.indexOf("rn") + 2);
						line = line.substring(line.indexOf(" ") + 2);
						if (!LMMEConstants.INEGLIGIBLE_KEGG_PATHWAYS.contains(line)) {
							if (!node2possibleSubsystems.containsKey(reactionNode)) {
								node2possibleSubsystems.put(reactionNode, new ArrayList<>());
							}
							node2possibleSubsystems.get(reactionNode).add(line);
						}
						lineIndex++;
					} while ((lineIndex < arrLines.length) && (arrLines[lineIndex].startsWith(" ")));
				else {
					lineIndex++;
				}
			}
		} else {
			// TODO: proper evaluation of response once RestService returns a map instead of just a string
			ErrorMsg.addErrorMessage("KEGG Decomposition failed: could not retrieve pathway information for reactions "
					+ "from KEGG database.");
		}
	}
	
	/**
	 * Updates the numbers associated to subsystems, stating how many possible reactions may belong to a subsystem.
	 */
	private void updateSubsystemNumber() {
		subsystem2number.clear();
		for (Node reactionNode : node2possibleSubsystems.keySet()) {
			for (String subsystem : node2possibleSubsystems.get(reactionNode)) {
				Integer number = Integer.valueOf(1);
				if (subsystem2number.containsKey(subsystem)) {
					number = Integer.valueOf(subsystem2number.get(subsystem).intValue() + 1);
				}
				subsystem2number.put(subsystem, number);
			}
		}
	}
	
	/**
	 * Removes those candidate subsystems that may have assigned a lesser number of reactions than specified by the user.
	 */
	private void removeTooSmallSubsystems() {
		ArrayList<String> subsystemsToRemove = new ArrayList<>();
		for (String smallSubsystem : subsystem2number.keySet()) {
			if (subsystem2number.get(smallSubsystem).intValue() < this.minimumNumberSlider.getValue()) {
				subsystemsToRemove.add(smallSubsystem);
				for (ArrayList<String> list : node2possibleSubsystems.values()) {
					list.remove(smallSubsystem);
				}
			}
		}
		for (String subsystem : subsystemsToRemove) {
			subsystem2number.remove(subsystem);
		}
	}
	
	/**
	 * Removes reactions that do not have candidate pathways anymore.
	 */
	private void removeNodesWithoutSubsystem() {
		
		ArrayList<Node> nodesToRemove = new ArrayList<>();
		for (Node reactionNode : node2possibleSubsystems.keySet()) {
			if (node2possibleSubsystems.get(reactionNode).isEmpty()) {
				nodesToRemove.add(reactionNode);
			}
		}
		for (Node nodeToRemove : nodesToRemove) {
			node2possibleSubsystems.remove(nodeToRemove);
		}
		
	}
	
	/**
	 * Processes the not-yet-confirmed subsystem that would currently have the most reactions assigned to.
	 */
	private void extractNodesFromHugestSubsystem() {
		
		if (subsystem2number.isEmpty()) {
			return;
		}
		
		int currentMax = 0;
		String hugestSubsystem = "";
		for (String subsystem : subsystem2number.keySet()) {
			if (subsystem2number.get(subsystem).intValue() > currentMax) {
				currentMax = subsystem2number.get(subsystem).intValue();
				hugestSubsystem = subsystem;
			}
		}
		
		ArrayList<Node> nodesToRemove = new ArrayList<>();
		for (Node reactionNode : node2possibleSubsystems.keySet()) {
			if (node2possibleSubsystems.get(reactionNode).contains(hugestSubsystem)) {
				nodesToRemove.add(reactionNode);
				LMMEController.getInstance().getCurrentSession().addNodeAttribute(reactionNode,
						ATTRIBUTE_NAME_FINAL_SUBSYSTEM, hugestSubsystem);
			}
		}
		LMMEController.getInstance().getTab()
				.logMsg("Added " + nodesToRemove.size() + " nodes to subsystem " + hugestSubsystem);
		
		for (Node nodeToRemove : nodesToRemove) {
			node2possibleSubsystems.remove(nodeToRemove);
		}
		
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
			this.fp = new FolderPanel(getName() + " Settings", false, true, false, null);
			
			this.cbTag = createComboBox();
			JPanel tagLine = LMMETab.combine(new JLabel("SBML Note:"), this.cbTag, Color.WHITE, false, true);
			this.tagRow = new GuiRow(tagLine, null);
			fp.addGuiComponentRow(this.tagRow, true);
			
			JLabel lblSeparator = new JLabel("Separator:");
			this.separator = new JTextField(5);
			this.separator.setText(",");
			JPanel separatorLine = LMMETab.combine(lblSeparator, this.separator, Color.WHITE, false, true);
			fp.addGuiComponentRow(FolderPanel.getBorderedComponent(separatorLine, 5, 0, 0, 0), null, true);
			
			JLabel lblMinimumNumber = new JLabel("Minimum reactions per pathway:");
			this.minimumNumberValue = new JLabel();
			
			this.minimumNumberSlider = new JSlider();
			if (SystemInfo.isMac()) {
				this.minimumNumberSlider.setPaintTrack(false);
			}
			this.minimumNumberSlider.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
			this.minimumNumberSlider.setMinimum(1);
			this.minimumNumberSlider.setMaximum(20);
			this.minimumNumberSlider.setToolTipText(
					"The minimum number of reactions that must belong to a KEGG pathway in order to consider this pathway "
							+ "as present.");
			this.minimumNumberSlider.setPaintLabels(true);
			this.minimumNumberSlider.setPaintTicks(true);
			Hashtable<Integer, JComponent> sliderLabels = new Hashtable<>();
			for (int i = 5; i <= this.minimumNumberSlider.getMaximum(); i += 5) {
				sliderLabels.put(Integer.valueOf(i), new JLabel(Integer.toString(i)));
			}
			this.minimumNumberSlider.setLabelTable(sliderLabels);
			this.minimumNumberSlider.setBackground(Color.WHITE);
			this.minimumNumberSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int newValue = ((JSlider) e.getSource()).getValue();
					minimumNumberValue.setText(String.valueOf(newValue));
				}
			});
			this.minimumNumberSlider.setValue(5);
			
			JComponent minimumNumberComponent = TableLayout.getDoubleRow(
					LMMETab.combine(lblMinimumNumber, this.minimumNumberValue, Color.WHITE, false, true),
					this.minimumNumberSlider, Color.WHITE);
			
			fp.addGuiComponentRow(minimumNumberComponent, null, true);
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
			if (cb.getItemAt(i).equalsIgnoreCase("KEGG")) {
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
		return "KEGG Decomposition";
	}
	
	@Override
	public boolean requiresTransporterSubsystem() {
		return false;
	}
	
}
