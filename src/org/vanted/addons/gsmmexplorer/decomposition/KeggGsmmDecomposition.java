package org.vanted.addons.gsmmexplorer.decomposition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.ws.rs.core.MediaType;

import org.BackgroundTaskStatusProvider;
import org.FolderPanel;
import org.SystemInfo;
import org.codehaus.stax2.evt.NotationDeclaration2;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerConstants;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerController;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerSession;
import org.vanted.addons.gsmmexplorer.core.GsmmExplorerTools;
import org.vanted.addons.gsmmexplorer.graphs.SubsystemGraph;
import org.vanted.addons.gsmmexplorer.ui.GsmmExplorerTab;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;
import info.clearthought.layout.TableLayout;

public class KeggGsmmDecomposition extends GsmmDecompositionAlgorithm { //implements Runnable, BackgroundTaskStatusProvider {

	private JTextField tag;
	private JTextField separator;
	private JLabel minimumNumberValue;
	private JSlider minimumNumberSlider;

	private final String ATTRIBUTE_NAME_KEGG_ID = "KeggID";

	private final String ATTRIBUTE_NAME_FINAL_SUBSYSTEM = "FinalSubsystem";

	/**
	 * The rest service for the KEGG requests.
	 */
	private RestService restService = new RestService("http://rest.kegg.jp/get/");

	private HashMap<Node, ArrayList<String>> node2possibleSubsystems;
	private HashMap<String, Integer> subsystem2number;
	
	private int packageStart;

	/**
	 * 
	 */
	public KeggGsmmDecomposition() {
		this.node2possibleSubsystems = new HashMap<>();
		this.subsystem2number = new HashMap<>();
	}

	@Override
	protected ArrayList<SubsystemGraph> runSpecific() {
		
		GsmmExplorerTools.getInstance().readNotes(this.tag.getText().trim(), this.ATTRIBUTE_NAME_KEGG_ID);
		
		request();
		
//		BackgroundTaskHelper.issueSimpleTask("Request from Kegg", "", this, null, this);

		while (!this.node2possibleSubsystems.isEmpty()) {
			updateSubsystemNumber();
			removeTooSmallSubsystems();
			removeNodesWithoutSubsystem();
			extractNodesFromHugestSubsystem();
		}

		return determineSubsystemsFromReactionAttributes(ATTRIBUTE_NAME_FINAL_SUBSYSTEM,
				this.separator.getText().trim());
	}

	/**
	 * For each reaction that has a KEGG ID, an HTTP request to KEGG is sent,
	 * querying the pathways that belong to the reaction. These are then stored in
	 * the the HashMap {@link node2possibleSubsystems}.
	 */
	private void request() {

		GsmmExplorerSession currentSession = GsmmExplorerController.getInstance().getCurrentSession();

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
			MainFrame.showMessage("Requested " + this.packageStart + " reactions from Kegg so far.", MessageType.PERMANENT_INFO);
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
	 *            The package of reaction nodes to be requested
	 * @return a list of request result Strings
	 */
	private String[] requestPackage(ArrayList<Node> reactionPackage) {
		String urlPostFix = "";
		for (Node reactionNode : reactionPackage) {
			String keggId = GsmmExplorerController.getInstance().getCurrentSession().getNodeAttribute(reactionNode,
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
			return new String[0];
		}
		response = response.substring(0, response.length() - 3);
		return response.split(Pattern.quote(GsmmExplorerConstants.KEGGSEP));
	}

	/**
	 * This method stores the results from the KEGG requests in the map
	 * {@link node2possibleSubsystems}. The method thus therefore expects two lists:
	 * a list of reactions and a list of request result Strings, that match position
	 * by position. This method assumes both lists to have the same length.
	 * 
	 * @param reactionNodes
	 *            The nodes that have been requested
	 * @param requestResults
	 *            The results from the request
	 */
	private void processPackageResults(ArrayList<Node> reactionNodes, String[] requestResults) {
		for (int i = 0; i < reactionNodes.size(); i++) {
			Node reactionNode = reactionNodes.get(i);
			String response = requestResults[i];
			processSingleNode(reactionNode, response);
		}
	}

	/**
	 * This method also expects a list of reaction nodes but in contrast to
	 * {@link requestPackage} it requests AND processes (stores the results in the
	 * map {@link node2possibleSubsystems}) the nodes one by one. This is necessary
	 * if one of the reactions did not yield request results during a previous call
	 * of {@link requestPackage} such that list sizes do not match or if a reaction
	 * has had assigned more than one KEGG id.
	 * 
	 * @param reactionPackage
	 *            A list of reaction nodes that will be requested and directly
	 *            processed.
	 */
	private void requestAndProcessPackageSeparately(ArrayList<Node> reactionPackage) {
		for (Node reactionNode : reactionPackage) {
			String keggId = GsmmExplorerController.getInstance().getCurrentSession().getNodeAttribute(reactionNode,
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
	 * This method processes a single node, meaning that the given request result is
	 * nterpreted in the context of the given reaction node and the respective
	 * pathway information from the request result. The pathways are then storedin
	 * the map {@link node2possibleSubsystems}.
	 * 
	 * @param reactionNode
	 * @param requestResult
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
						if (!GsmmExplorerConstants.INEGLIGIBLE_KEGG_PATHWAYS.contains(line)) {
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
		}
	}

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
				GsmmExplorerController.getInstance().getCurrentSession().addNodeAttribute(reactionNode,
						ATTRIBUTE_NAME_FINAL_SUBSYSTEM, hugestSubsystem);
			}
		}
		GsmmExplorerController.getInstance().getTab()
				.logMsg("Added " + nodesToRemove.size() + " nodes to subsystem " + hugestSubsystem);

		for (Node nodeToRemove : nodesToRemove) {
			node2possibleSubsystems.remove(nodeToRemove);
		}

	}

	/**
	 * 
	 */
	public boolean requiresCloning() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm#
	 * getFolderPanel()
	 */
	@Override
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName() + " Settings", false, true, false, null);

		JLabel lblTag = new JLabel("SBML Tag:");
		this.tag = new JTextField(10);
		this.tag.setText("KEGG");
		JPanel tagLine = GsmmExplorerTab.combine(lblTag, this.tag, Color.WHITE, false, true);
		fp.addGuiComponentRow(tagLine, null, true);

		JLabel lblSeparator = new JLabel("Separator:");
		this.separator = new JTextField(5);
		this.separator.setText(",");
		JPanel separatorLine = GsmmExplorerTab.combine(lblSeparator, this.separator, Color.WHITE, false, true);
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
				GsmmExplorerTab.combine(lblMinimumNumber, this.minimumNumberValue, Color.WHITE, false, true),
				this.minimumNumberSlider, Color.WHITE);

		fp.addGuiComponentRow(minimumNumberComponent, null, true);

		return fp;
	}

	/**
	 * 
	 */
	public String getName() {
		return "KEGG Annotation";
	}

//	/**
//	 * 
//	 */
//	public int getCurrentStatusValue() {
//		// TODO Auto-generated method stub
//		return -1;
//	}
//
//	/**
//	 * 
//	 */
//	public void setCurrentStatusValue(int value) {
//	}
//
//	/**
//	 * 
//	 */
//	public double getCurrentStatusValueFine() {
//		return -1.0;
//	}
//
//	/**
//	 * 
//	 */
//	public String getCurrentStatusMessage1() {
//		return "Requested " + this.packageStart + " reactions from Kegg so far.";
//	}
//
//	/**
//	 * 
//	 */
//	public String getCurrentStatusMessage2() {
//		return "";
//	}
//
//	/**
//	 * 
//	 */
//	public void pleaseStop() {
//	}
//
//	/**
//	 * 
//	 */
//	public boolean pluginWaitsForUser() {
//		return false;
//	}
//
//	/**
//	 * 
//	 */
//	public void pleaseContinueRun() {
//	}
//
//	/**
//	 * 
//	 */
//	public void run() {
//		request();
//	}

}
