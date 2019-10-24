package org.vanted.addons.wholecell;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.FolderPanel;
import org.GuiRow;
import org.SystemInfo;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class WholecellTab extends InspectorTab {
	
	private JFrame clonableSpeciesFrame;
	private JPanel clonableSpeciesPanel;
	private JButton submitClonableSpecies;
	private JLabel editedList;
	private boolean editedCloneList = false;
	
	public boolean isEditedCloneList() {
		return editedCloneList;
	}
	
	ArrayList<JCheckBox> clonableSpeciesCheckBoxes = new ArrayList<>();
	
	private static final long serialVersionUID = 1L;
	
	private TableLayout monitorLayout;
	private JPanel monitorInnerPanel;
	
	private JButton initButton;
	
	private GuiRow specificDecompSettingsRow = new GuiRow(new JLabel(""), null);
	
	private String initDecompMethod;
	
	public String getInitDecompMethod() {
		return initDecompMethod;
	}
	
	private JTextField keggTag;
	private JTextField keggSep;
	private JTextField annotTag;
	private JTextField annotSep;
	
	public String getKeggSep() {
		return keggSep.getText().trim();
	}
	
	public String getAnnotSep() {
		return annotSep.getText().trim();
	}
	
	public String getKeggTag() {
		return keggTag.getText().trim();
	}
	
	public String getAnnotTag() {
		return annotTag.getText().trim();
	}
	
	private String decompMethod;
	private String layoutAlgorithmOverviewGraph;
	private boolean useSbgnForOverviewGraph;
	private String layoutAlgorithmSubsystemGraph;
	private boolean useSbgnForSubsystemGraph;
	
	public String getDecompMethod() {
		return decompMethod;
	}
	
	public String getLayoutAlgorithmOverviewGraph() {
		return layoutAlgorithmOverviewGraph;
	}
	
	public boolean isUseSbgnForOverviewGraph() {
		return useSbgnForOverviewGraph;
	}
	
	public String getLayoutAlgorithmSubsystemGraph() {
		return layoutAlgorithmSubsystemGraph;
	}
	
	public boolean isUseSbgnForSubsystemGraph() {
		return useSbgnForSubsystemGraph;
	}
	
	private int clonableSpeciesThreshold = 8;
	
	public int getClonableSpeciesThreshold() {
		return clonableSpeciesThreshold;
	}
	
	private JLabel labelGeneralInfoMet;
	private JLabel labelGeneralInfoReac;
	private JLabel labelGeneralInfoSubs;
	private JLabel labelSubsystemInfoName;
	private JLabel labelSubsystemInfoMet;
	private JLabel labelSubsystemInfoReac;
	private JLabel labelSliderCorrespSpeciesNumber;
	
	public void setLabelSliderCorrespSpeciesNumber(String labelSliderCorrespSpeciesNumber) {
		this.labelSliderCorrespSpeciesNumber.setText(labelSliderCorrespSpeciesNumber);
	}
	
	public void setLabelGeneralInfoMet(String labelGeneralInfoMet) {
		this.labelGeneralInfoMet.setText(labelGeneralInfoMet);
	}
	
	public void setLabelGeneralInfoReac(String labelGeneralInfoReac) {
		this.labelGeneralInfoReac.setText(labelGeneralInfoReac);
	}
	
	public void setLabelGeneralInfoSubs(String labelGeneralInfoSubs) {
		this.labelGeneralInfoSubs.setText(labelGeneralInfoSubs);
	}
	
	public void setLabelSubsystemInfoName(String labelSubsystemInfoName) {
		this.labelSubsystemInfoName.setText(labelSubsystemInfoName);
	}
	
	public void setLabelSubsystemInfoMet(String labelSubsystemInfoMet) {
		this.labelSubsystemInfoMet.setText(labelSubsystemInfoMet);
	}
	
	public void setLabelSubsystemInfoReac(String labelSubsystemInfoReac) {
		this.labelSubsystemInfoReac.setText(labelSubsystemInfoReac);
	}
	
	public WholecellTab() {
		
		clonableSpeciesFrame = new JFrame("Clonable Species Selection");
		clonableSpeciesPanel = new JPanel();
		submitClonableSpecies = new JButton("Submit");
		clonableSpeciesFrame.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 300, TableLayoutConstants.FILL } }));
		JScrollPane clonableSpeciesScrollPane = new JScrollPane(clonableSpeciesPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		clonableSpeciesFrame.add(clonableSpeciesScrollPane, "0,0");
		clonableSpeciesFrame.add(submitClonableSpecies, "0,1");
		clonableSpeciesFrame.setSize(300, 375);
		
		JPanel mainPanel = new JPanel();
		
		mainPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
				{ 10, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10,
						TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10,
						TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10 } }));
		
		JButton buttonReadData = new JButton("Show Model Information");
		JButton runDecomp = new JButton("Create Overview Graph");
		runDecomp.addActionListener(WholecellTools.execute());
		mainPanel.add(combine(buttonReadData, runDecomp, Color.WHITE, true), "0,1");
		
		buttonReadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WholecellTools.initialise();
			}
		});
		
		FolderPanel information = new FolderPanel("Information", false, true, false, null);
		FolderPanel cloningSpecies = new FolderPanel("Cloning Species", false, true, false, null);
		FolderPanel settings = new FolderPanel("Settings", false, true, false, null);
		FolderPanel subsViews = new FolderPanel("Open Subsystem View", false, true, false, null);
		
		// --- Folderpanel information BEGIN
		
		JPanel infoPanel = new JPanel();
		
		infoPanel.setLayout(new TableLayout(new double[][] {
				{ TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED },
				{ TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED } }));
		
		this.labelGeneralInfoMet = new JLabel("");
		this.labelGeneralInfoReac = new JLabel("");
		this.labelGeneralInfoSubs = new JLabel("");
		
		infoPanel.add(new JLabel("Metabolites: "), "0,0");
		infoPanel.add(labelGeneralInfoMet, "1,0");
		infoPanel.add(new JLabel("Reactions: "), "0,1");
		infoPanel.add(labelGeneralInfoReac, "1,1");
		infoPanel.add(new JLabel("Subsystems: "), "0,2");
		infoPanel.add(labelGeneralInfoSubs, "1,2");
		infoPanel.setBackground(Color.WHITE);
		
		JPanel monitorOuterPanel = new JPanel();
		monitorOuterPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 60 } }));
		this.monitorInnerPanel = new JPanel();
		this.monitorLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 0 } });
		monitorInnerPanel.setLayout(monitorLayout);
		JScrollPane monitorScrollPane = new JScrollPane(monitorInnerPanel);
		monitorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		monitorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		monitorOuterPanel.add(monitorScrollPane, "0,0");
		
		information.addGuiComponentRow(FolderPanel.getBorderedComponent(infoPanel, 0, 0, 5, 0), null, true);
		addOneElementSpanning(monitorOuterPanel, information);
		
		mainPanel.add(information, "0,3");
		
		// --- Folderpanel information END
		
		// --- Folderpanel cloningSpecies BEGIN
		
		JLabel labelSliderSplitDeg = new JLabel("Degree Threshold: ");
		JLabel labelSliderSplitDegCurr = new JLabel();
		JLabel labelSliderCorrespSpecies = new JLabel("Corresponding species: ");
		this.labelSliderCorrespSpeciesNumber = new JLabel();
		
		JSlider sliderSplitDeg = new JSlider();
		if (SystemInfo.isMac()) {
			sliderSplitDeg.setPaintTrack(false);
		}
		sliderSplitDeg.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		sliderSplitDeg.setMinimum(0);
		sliderSplitDeg.setMaximum(30);
		sliderSplitDeg.setToolTipText("The minimum degree at which metabolites may be considered for cloning.");
		sliderSplitDeg.setMinorTickSpacing(1);
		sliderSplitDeg.setMajorTickSpacing(5);
		sliderSplitDeg.setPaintLabels(true);
		sliderSplitDeg.setPaintTicks(true);
		sliderSplitDeg.setLabelTable(sliderSplitDeg.createStandardLabels(5));
		sliderSplitDeg.setBackground(Color.WHITE);
		
		sliderSplitDeg.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int newValue = ((JSlider) e.getSource()).getValue();
				clonableSpeciesThreshold = newValue;
				labelSliderSplitDegCurr.setText(String.valueOf(clonableSpeciesThreshold));
				labelSliderCorrespSpeciesNumber
						.setText(String.valueOf(WholecellTools.numberOfSpeciesWithDegreeAtLeast(newValue)));
				if (editedList != null) {
					editedList.setVisible(false);
				}
				editedCloneList = false;
			}
		});
		
		sliderSplitDeg.setValue(clonableSpeciesThreshold);
		JComponent splitDegSetting = TableLayout
				.getDoubleRow(combine(combine(labelSliderSplitDeg, labelSliderSplitDegCurr, Color.WHITE, false),
						combine(labelSliderCorrespSpecies, labelSliderCorrespSpeciesNumber, Color.WHITE, false),
						Color.WHITE, true), sliderSplitDeg, Color.WHITE);
		
		cloningSpecies.addGuiComponentRow(splitDegSetting, null, true);
		
		JButton editCloneListButton = new JButton("Edit List");
		
		editCloneListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (WholecellTools.isInitialised()) {
					ArrayList<String> speciesAbove = WholecellTools
							.getSpeciesAboveDegThreshold(clonableSpeciesThreshold);
					Collections.sort(speciesAbove);
					clonableSpeciesPanel.removeAll();
					double[] rows = new double[speciesAbove.size()];
					for (int i = 0; i < rows.length; i++) {
						rows[i] = TableLayoutConstants.PREFERRED;
					}
					clonableSpeciesPanel
							.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, rows }));
					clonableSpeciesCheckBoxes.clear();
					for (int i = 0; i < speciesAbove.size(); i++) {
						String speciesStr = speciesAbove.get(i);
						JCheckBox speciesCheckBox = new JCheckBox(speciesStr, true);
						clonableSpeciesCheckBoxes.add(speciesCheckBox);
						clonableSpeciesPanel.add(speciesCheckBox, "0," + i);
					}
					clonableSpeciesFrame.setVisible(true);
				}
			}
		});
		
		submitClonableSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> clonableSpecies = WholecellTools.getClonableSpecies();
				clonableSpecies.clear();
				for (JCheckBox checkBox : clonableSpeciesCheckBoxes) {
					if (checkBox.isSelected()) {
						clonableSpecies.add(checkBox.getText());
					}
				}
				clonableSpeciesFrame.setVisible(false);
				editedList.setVisible(true);
				editedCloneList = true;
			}
		});
		
		this.editedList = new JLabel(" (edited)");
		this.editedList.setVisible(false);
		cloningSpecies.addGuiComponentRow(combine(editCloneListButton, editedList, Color.WHITE, false), null, true);
		
		mainPanel.add(cloningSpecies, "0,5");
		
		// --- Folderpanel cloningSpecies END
		
		// --- Folderpanel settings BEGIN
		
		JLabel labelDecompMethod = new JLabel("Decomposition Method:");
		String[] cbListDecompMethod = { WholecellConstants.DECOMP_KEGG, WholecellConstants.DECOMP_SUBSANN };
		JComboBox<String> cbDecompMethod = new JComboBox<>(cbListDecompMethod);
		JPanel decompMethodSelect = combine(labelDecompMethod, cbDecompMethod, Color.WHITE, true);
		settings.addGuiComponentRow(FolderPanel.getBorderedComponent(decompMethodSelect, 0, 0, 5, 0), null, true);
		FolderPanel keggAnnSettings = new FolderPanel("Settings for KEGG annotation based method", false, true, false,
				null);
		
		JLabel lblKeggTag = new JLabel("SBML Tag:");
		this.keggTag = new JTextField(10);
		keggTag.setText("KEGG");
		JPanel keggTagField = combine(lblKeggTag, keggTag, Color.WHITE, false);
		keggAnnSettings.addGuiComponentRow(keggTagField, null, true);
		JLabel lblKeggSep = new JLabel("ID Separator:");
		this.keggSep = new JTextField(5);
		keggSep.setText(",");
		JPanel keggSepField = combine(lblKeggSep, keggSep, Color.WHITE, false);
		keggAnnSettings.addGuiComponentRow(keggSepField, null, true);
		
		FolderPanel subsAnnSettings = new FolderPanel("Settings for SUBSYSTEM annotation based method", false, true,
				false, null);
		JLabel lblSubsAnnTag = new JLabel("SBML Tag:");
		this.annotTag = new JTextField(10);
		annotTag.setText("SUBSYSTEM");
		JPanel subsAnnTagField = combine(lblSubsAnnTag, annotTag, Color.WHITE, false);
		subsAnnSettings.addGuiComponentRow(subsAnnTagField, null, true);
		JLabel lblAnnotSep = new JLabel("ID Separator:");
		this.annotSep = new JTextField(5);
		annotSep.setText(",");
		JPanel annotSepField = combine(lblAnnotSep, annotSep, Color.WHITE, false);
		subsAnnSettings.addGuiComponentRow(annotSepField, null, true);
		
		cbDecompMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newItem = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				
				switch (newItem) {
					case WholecellConstants.DECOMP_KEGG:
						specificDecompSettingsRow.left = keggAnnSettings;
						settings.layoutRows();
						decompMethod = WholecellConstants.DECOMP_KEGG;
						break;
					case WholecellConstants.DECOMP_SUBSANN:
						specificDecompSettingsRow.left = subsAnnSettings;
						settings.layoutRows();
						decompMethod = WholecellConstants.DECOMP_SUBSANN;
						break;
					default:
						break;
				}
			}
		});
		specificDecompSettingsRow.span = true;
		settings.addGuiComponentRow(specificDecompSettingsRow, true);
		cbDecompMethod.setSelectedItem(WholecellConstants.DECOMP_KEGG);
		
		JLabel labelLayoutAlgo = new JLabel("Layout Algorithm: ");
		String[] cbListLayoutAlgo = { WholecellConstants.LAYOUT_FORCEDIR, WholecellConstants.LAYOUT_CIRCULAR };
		JComboBox<String> cbLayoutAlgo = new JComboBox<>(cbListLayoutAlgo);
		JPanel layoutAlgoSelect = combine(labelLayoutAlgo, cbLayoutAlgo, Color.WHITE, true);
		settings.addGuiComponentRow(FolderPanel.getBorderedComponent(layoutAlgoSelect, 5, 0, 0, 0), null, true);
		cbLayoutAlgo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newItem = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				layoutAlgorithmOverviewGraph = newItem;
			}
		});
		cbLayoutAlgo.setSelectedItem(WholecellConstants.LAYOUT_FORCEDIR);
		
		mainPanel.add(settings, "0,7");
		
		// --- Folderpanel settings END
		
		// --- Folderpanel subsViews BEGIN
		
		JPanel subsystemInfoPanel = new JPanel();
		subsystemInfoPanel.setLayout(new TableLayout(new double[][] {
				{ TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED },
				{ TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED } }));
		
		this.labelSubsystemInfoName = new JLabel("");
		this.labelSubsystemInfoMet = new JLabel("");
		this.labelSubsystemInfoReac = new JLabel("");
		
		subsViews.addGuiComponentRow(new JLabel("<html><u>Information about selected subsystem:</u></html>"), null,
				true);
		
		subsystemInfoPanel.add(new JLabel("Name: "), "0,0");
		subsystemInfoPanel.add(labelSubsystemInfoName, "1,0");
		subsystemInfoPanel.add(new JLabel("Metabolites: "), "0,1");
		subsystemInfoPanel.add(labelSubsystemInfoMet, "1,1");
		subsystemInfoPanel.add(new JLabel("Reactions: "), "0,2");
		subsystemInfoPanel.add(labelSubsystemInfoReac, "1,2");
		//
		subsystemInfoPanel.setBackground(Color.WHITE);
		
		subsViews.addGuiComponentRow(FolderPanel.getBorderedComponent(subsystemInfoPanel, 0, 0, 5, 0), null, true);
		
		JLabel labelSubsLayoutAlgo = new JLabel("Layout Algorithm: ");
		String[] cbListSubsLayoutAlgo = { WholecellConstants.LAYOUT_FORCEDIR, WholecellConstants.LAYOUT_CONCENTRIC_CIRC,
				WholecellConstants.LAYOUT_PARALLEL_LINES };
		JComboBox<String> cbSubsLayoutAlgo = new JComboBox<>(cbListSubsLayoutAlgo);
		JPanel subsLayoutAlgoSelect = combine(labelSubsLayoutAlgo, cbSubsLayoutAlgo, Color.WHITE, true);
		subsViews.addGuiComponentRow(subsLayoutAlgoSelect, null, true);
		cbSubsLayoutAlgo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newItem = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				layoutAlgorithmSubsystemGraph = newItem;
			}
		});
		cbSubsLayoutAlgo.setSelectedItem(WholecellConstants.LAYOUT_FORCEDIR);
		
		JCheckBox sbgnCheckBoxSubs = new JCheckBox("Use SBGN Representation");
		sbgnCheckBoxSubs.setBackground(Color.WHITE);
		subsViews.addGuiComponentRow(sbgnCheckBoxSubs, null, true);
		sbgnCheckBoxSubs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean newVal = ((JCheckBox) e.getSource()).isSelected();
				useSbgnForSubsystemGraph = newVal;
			}
		});
		
		JButton buttonShowSubsystems = new JButton("Show Subsystem View");
		buttonShowSubsystems.setToolTipText("Opens a new view for the currently selected subsystem, "
				+ "showing the metabolites and reactions that it contains.");
		buttonShowSubsystems.addActionListener(WholecellTools.openSubsystemViews());
		addOneElementSpanning(buttonShowSubsystems, subsViews);
		
		mainPanel.add(subsViews, "0,9");
		
		JButton buttonToSBGN = new JButton("Translate model to SBGN");
		buttonToSBGN.addActionListener(WholecellTools.translateToSBGN());
		mainPanel.add(buttonToSBGN, "0,11");
		
		// --- Folderpanel subsViews END
		
		JScrollPane mainScrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } }));
		
		add(mainScrollPane, "0,0");
		
		// initButton.addActionListener(WholecellTools.initialise());
		
		WholecellTools.setMyTab(this);
		
		// logMsg("A very long message for debugging purposes!");
		
	}
	
	/**
	 * This emthod allows to create a log message in the respective field.
	 * 
	 * @param msg
	 *           The message to be logged.
	 */
	public void logMsg(String msg) {
		this.monitorLayout.insertRow(0, TableLayoutConstants.PREFERRED);
		this.monitorInnerPanel.add(new JLabel(msg), "0,0");
	}
	
	/**
	 * This emthod adds the given element as a new row into the given FolderPanel in
	 * a way such that it spans the whole row
	 * 
	 * @param el
	 *           The element to be added.
	 * @param panel
	 *           The FolderPanel into which the element should be inserted.
	 */
	private static void addOneElementSpanning(JComponent el, FolderPanel panel) {
		GuiRow gr = new GuiRow(el, null);
		gr.span = true;
		panel.addGuiComponentRow(gr, true);
	}
	
	/**
	 * This method is used to form a group of two JComponents.
	 * 
	 * @param left
	 *           The left component
	 * @param right
	 *           The right component
	 * @param color
	 *           The background color of the new component
	 * @param fill
	 *           States whether the components should be aligned in a way that uses
	 *           the entire horizontally available space.
	 * @return The new component that results from combining the left and the right
	 *         component.
	 */
	private static JPanel combine(JComponent left, JComponent right, Color color, boolean fill) {
		JPanel combinedPanel = new JPanel();
		combinedPanel
				.setLayout(
						new TableLayout(
								new double[][] {
										{ fill == true ? TableLayoutConstants.FILL : TableLayoutConstants.PREFERRED,
												fill == true ? TableLayoutConstants.FILL
														: TableLayoutConstants.PREFERRED },
										{ TableLayoutConstants.PREFERRED } }));
		combinedPanel.add(left, "0,0");
		combinedPanel.add(right, "1,0");
		combinedPanel.setBackground(color);
		return combinedPanel;
	}
	
	private static JPanel combine3(JComponent left, JComponent mid, JComponent right, Color color) {
		JPanel combinedPanel = new JPanel();
		combinedPanel.setLayout(new TableLayout(
				new double[][] { { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
						{ TableLayoutConstants.PREFERRED } }));
		combinedPanel.add(left, "0,0");
		combinedPanel.add(mid, "1,0");
		combinedPanel.add(right, "2,0");
		combinedPanel.setBackground(color);
		return combinedPanel;
	}
	
	@Override
	public String getName() {
		
		return getTitle();
		
	}
	
	@Override
	public String getTitle() {
		
		return "Whole-cell model";
		
	}
	
	@Override
	public boolean visibleForView(View v) {
		
		return true;
		
	}
	
}
