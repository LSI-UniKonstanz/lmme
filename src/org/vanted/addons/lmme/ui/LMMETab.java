package org.vanted.addons.lmme.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.FolderPanel;
import org.GuiRow;
import org.SystemInfo;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.vanted.addons.lmme.core.LMMEConstants;
import org.vanted.addons.lmme.core.LMMEController;
import org.vanted.addons.lmme.core.LMMESession;
import org.vanted.addons.lmme.core.LMMETools;
import org.vanted.addons.lmme.decomposition.MMDecompositionAlgorithm;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.OverviewGraph;
import org.vanted.addons.lmme.layout.ForceDirectedMMLayout;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class LMMETab extends InspectorTab {

	// instance variables begin

	private JPanel mainPanel;

	private JLabel lblSessionInfoBaseGraph;
	private JLabel lblSessionInfoMetabolites;
	private JLabel lblSessionInfoReactions;
	private JLabel lblSessionInfoSubsystems;

	private JPanel panelSelectionInformation;

	private JPanel panelSelectedSubsystemInfo;
	private JLabel lblNameOfSelectedSubsystem;
	private JLabel lblNumberMetabolitesOfSelectedSubsystem;
	private JLabel lblNumberReactionsOfSelectedSubsystem;

	private JPanel panelSelectedEdgeInfo;
	private JScrollPane scrollPaneSelectedEdgeInfo;
	private JPanel interfaceListPanel;
	private JLabel lblNameSubsystem1OfSelectedEdge;
	private JLabel lblNameSubsystem2OfSelectedEdge;

	private JPanel panelNoSelection;

	private JLabel lblSubsystemInfoSubsystems;
	private JLabel lblSubsystemInfoMetabolites;
	private JLabel lblSubsystemInfoReactions;

	private TableLayout monitorLayout;
	private JPanel monitorInnerPanel;
	private JScrollPane monitorScrollPane;

	private JCheckBox ckbMapToEdgeThickness;
	private JCheckBox ckbDrawEdges;
	private JCheckBox ckbAddTransporterSubS;

	private JComboBox<String> cbDecompMethod;
	private JComboBox<String> cbOverviewLayout;
	private JComboBox<String> cbSubsystemLayout;

	private JCheckBox ckbClearSubsView;
	private JCheckBox ckbUseColorMapping;

	private final int defaultSplitDeg = 15;
	private JSlider sliderSplitDeg;
	private JLabel labelSliderCorrespSpeciesNumber;

	private boolean editedCloneList;
	private JLabel lblEditedCloneList;
	ArrayList<JCheckBox> clonableSpeciesCheckBoxes = new ArrayList<>();
	ArrayList<JCheckBox> clonableSpeciesCheckBoxesSubmitted = new ArrayList<>();
	HashMap<JCheckBox, Node> checkbox2nodeMap = new HashMap<>();

	// instance variables end

	public LMMETab() {
		this.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } }));
		mainPanel = new JPanel();
		JScrollPane mainScrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(mainScrollPane, "0,0");

		mainPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
				{ 5.0, TableLayoutConstants.MINIMUM, 5.0, TableLayoutConstants.MINIMUM, 5.0,
						TableLayoutConstants.MINIMUM, 5.0, TableLayoutConstants.MINIMUM, 5.0,
						TableLayoutConstants.MINIMUM, 5.0, TableLayoutConstants.MINIMUM, 5.0,
						TableLayoutConstants.MINIMUM, 5.0, TableLayoutConstants.MINIMUM, 5.0,
						TableLayoutConstants.MINIMUM, 5.0, TableLayoutConstants.MINIMUM } }));
		mainPanel.setBackground(Color.WHITE);

		int rowCount = 1;

		JButton btnSetModel = new JButton("Set model");
		btnSetModel.setToolTipText(
				"Sets the model from the currently active view as the base graph for the decomposition.");
		mainPanel.add(btnSetModel, "0," + rowCount);
		rowCount += 2;
		btnSetModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LMMEController.getInstance().setModelAction();
			}
		});

		mainPanel.add(createSessionInformationComponent(), "0," + rowCount);
		rowCount += 2;

		mainPanel.add(createSettingsComponent(), "0," + rowCount);
		rowCount += 2;

		JButton btnShowOverviewGraph = new JButton("Show Overview Graph");
		btnShowOverviewGraph
				.setToolTipText("Runs the decomposition on the base graph and shows the resulting overview graph.");
		mainPanel.add(btnShowOverviewGraph, "0," + rowCount);
		rowCount += 2;
		btnShowOverviewGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LMMEController.getInstance().showOverviewGraphAction();
			}
		});

		this.ckbDrawEdges = new JCheckBox("Show edges in overview graph");
		this.ckbDrawEdges
				.setToolTipText("<html>If deselected, only the subsystem nodes will be drawn without edges</html>");
		this.ckbDrawEdges.setBackground(Color.WHITE);
		mainPanel.add(this.ckbDrawEdges, "0," + rowCount);
		rowCount += 2;
		this.ckbDrawEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (LMMEController.getInstance().getCurrentSession().isOverviewGraphConstructed()) {
					LMMEController.getInstance().getCurrentSession().getOverviewGraph().updateEdgeThickness();
				}
			}
		});
		this.ckbDrawEdges.setSelected(true);
		this.ckbDrawEdges.setRolloverEnabled(false);

		this.ckbMapToEdgeThickness = new JCheckBox("Map number of interfaces to edge thickness");
		this.ckbMapToEdgeThickness.setToolTipText(
				"<html>The edge thickness in the overview graph will be proportional to<br>the number of interface metabolites between the respective subsystems</html>");
		this.ckbMapToEdgeThickness.setBackground(Color.WHITE);
		mainPanel.add(this.ckbMapToEdgeThickness, "0," + rowCount);
		rowCount += 2;
		this.ckbMapToEdgeThickness.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (LMMEController.getInstance().getCurrentSession().isOverviewGraphConstructed()) {
					LMMEController.getInstance().getCurrentSession().getOverviewGraph().updateEdgeThickness();
				}
			}
		});
		this.ckbMapToEdgeThickness.setSelected(false);

		mainPanel.add(instantiateORA(), "0," + rowCount);
		rowCount += 2;

		mainPanel.add(createSubsystemsViewComponent(), "0," + rowCount);
		rowCount += 2;

		JButton btnShowSubsystems = new JButton("Show selected subsystems");
		btnShowSubsystems.setToolTipText("Shows the expanded versions of the currently selected subsystems "
				+ "from the left window in the right window.");
		mainPanel.add(btnShowSubsystems, "0," + rowCount);
		rowCount += 2;
		btnShowSubsystems.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LMMEController.getInstance().showSubsystemGraphsAction();
			}
		});

		JButton btnTransformToSbgn = new JButton("Transform to SBGN");
		btnTransformToSbgn.setToolTipText("Transforms the subsystem view to SBGN representation.");
		mainPanel.add(btnTransformToSbgn, "0," + rowCount);
		rowCount += 2;
		btnTransformToSbgn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LMMEController.getInstance().transformToSbgnAction();
			}
		});

		LMMEController.getInstance().setTab(this);
	}

	private JButton instantiateORA() {
		String defaultTextForLoadFile = "No file selected.";
		JTextField tfPathToDifferentiallyExpressedFile = new JTextField(defaultTextForLoadFile);
		tfPathToDifferentiallyExpressedFile.setEditable(false);
		JTextField tfPathToReferenceFile = new JTextField(defaultTextForLoadFile);
		tfPathToReferenceFile.setEditable(false);

		JFrame frameORA = new JFrame("Over-Representation Analysis (ORA)");
		JPanel panelORA = new JPanel();
		panelORA.setBackground(Color.WHITE);
		JButton buttonExecuteORA = new JButton("Execute");
		frameORA.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
				{ TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM, 35.0 } }));
		panelORA.setLayout(new TableLayout(new double[][] {
				{ 10.0, TableLayoutConstants.PREFERRED, 10.0, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM },
				{ 30.0, 30.0 } }));
		panelORA.add(new JLabel("<html><b>Differentially Expressed Metabolites</b></html>"), "1,0");
		panelORA.add(new JLabel("<html><b>Reference Metabolites</b></html>"), "1,1");
		JButton btnLoadDiff = new JButton("Load");
		JButton btnLoadRef = new JButton("Load");
		
		btnLoadDiff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int res = fc.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					tfPathToDifferentiallyExpressedFile.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		btnLoadRef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int res = fc.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					tfPathToReferenceFile.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		panelORA.add(tfPathToDifferentiallyExpressedFile, "3,0");
		panelORA.add(tfPathToReferenceFile, "3,1");
		panelORA.add(btnLoadDiff, "4,0");
		panelORA.add(btnLoadRef, "4,1");

		JPanel fillPanel = new JPanel();
		fillPanel.setBackground(Color.WHITE);

		JLabel lblORADescription = new JLabel(
				"<html><p>The Over-Representation Analysis (ORA) compares the amount of differentially expressed metabolites within a subsystem to the expected amount resulting from the amount of differentially expressed metabolites within the reference set of metabolites.</p>"
						+ "<p></p>"
						+ "<p><b>Differentially Expressed Metabolites:</b> Please upload a file that contains the IDs of the differentially expressed metabolites</p>"
						+ "<p><b>Reference Metabolites:</b> Please upload a file that contains the IDs of the total set of metabolites that have been measured (including the differentially expressed ones). If no file is selected, the total set of metabolites of the model is used as default.</p></html>");
		
		JPanel panelORADescription = new JPanel();
		panelORADescription.setLayout(new TableLayout(new double[][] {{TableLayoutConstants.FILL}, {TableLayoutConstants.MINIMUM}}));
		panelORADescription.add(FolderPanel.getBorderedComponent(lblORADescription, 5, 5, 5, 5), "0,0");
		panelORADescription.setBackground(Color.WHITE);
		frameORA.add(panelORADescription, "0,0");
		frameORA.add(panelORA, "0,1");
		frameORA.add(fillPanel, "0,2");
		frameORA.add(buttonExecuteORA, "0,3");
		frameORA.setSize(600, 300);
		frameORA.setLocationRelativeTo(null);

		buttonExecuteORA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tfPathToDifferentiallyExpressedFile.getText().equals(defaultTextForLoadFile)) {
					JOptionPane.showMessageDialog(null,
							"There is no file selected for the differentially expressed metabolites.");
				} else {
					try {
						if (tfPathToReferenceFile.getText().equals(defaultTextForLoadFile)) {
							LMMEController.getInstance().oraAction(tfPathToDifferentiallyExpressedFile.getText(), null);
						} else {
							LMMEController.getInstance().oraAction(tfPathToDifferentiallyExpressedFile.getText(),
									tfPathToReferenceFile.getText());
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					frameORA.setVisible(false);
				}

			}
		});

		JButton btnORA = new JButton("Over-Representation Analysis");

		btnORA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfPathToDifferentiallyExpressedFile.setText(defaultTextForLoadFile);
				tfPathToReferenceFile.setText(defaultTextForLoadFile);
				frameORA.revalidate();
				frameORA.setVisible(true);
			}
		});

		return btnORA;
	}

	/**
	 * Creates and returns the JComponent for the Session Information.
	 * 
	 * @return
	 */
	private JComponent createSessionInformationComponent() {
		FolderPanel fpSessionInfo = new FolderPanel("Session Information", false, true, false, null);

		JPanel sessionInfoPanel = new JPanel();

		sessionInfoPanel.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM },
						{ TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
								TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM } }));

		this.lblSessionInfoBaseGraph = new JLabel("");
		this.lblSessionInfoMetabolites = new JLabel("");
		this.lblSessionInfoReactions = new JLabel("");
		this.lblSessionInfoSubsystems = new JLabel("");

		sessionInfoPanel.add(new JLabel("<html><u>General</u></html>"), "0,0");

		sessionInfoPanel.add(new JLabel("Base Graph: "), "0,1");
		sessionInfoPanel.add(this.lblSessionInfoBaseGraph, "1,1");
		sessionInfoPanel.add(new JLabel("Metabolites: "), "0,2");
		sessionInfoPanel.add(this.lblSessionInfoMetabolites, "1,2");
		sessionInfoPanel.add(new JLabel("Reactions: "), "0,3");
		sessionInfoPanel.add(this.lblSessionInfoReactions, "1,3");
		sessionInfoPanel.add(new JLabel("Subsystems: "), "0,4");
		sessionInfoPanel.add(this.lblSessionInfoSubsystems, "1,4");
		sessionInfoPanel.setBackground(Color.WHITE);

		JPanel monitorOuterPanel = new JPanel();
		monitorOuterPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 60 } }));
		this.monitorInnerPanel = new JPanel();
		this.monitorLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 0 } });
		monitorInnerPanel.setLayout(monitorLayout);
		this.monitorScrollPane = new JScrollPane(monitorInnerPanel);
		this.monitorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.monitorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		monitorOuterPanel.add(this.monitorScrollPane, "0,0");

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new TableLayout(
				new double[][] { { TableLayoutConstants.MINIMUM, 10.0, 1.0, 10.0, TableLayoutConstants.FILL },
						{ TableLayoutConstants.MINIMUM } }));

		infoPanel.add(sessionInfoPanel, "0,0");
		JPanel separatorPanel = new JPanel();
		separatorPanel.setBackground(Color.BLACK);
		infoPanel.add(separatorPanel, "2,0");

		instantiateSelectionInformationPanel();
		infoPanel.add(this.panelSelectionInformation, "4,0");
		infoPanel.setBackground(Color.WHITE);
		addOneElementSpanning(FolderPanel.getBorderedComponent(infoPanel, 2, 0, 0, 0), fpSessionInfo);
//		fpSessionInfo.addGuiComponentRow(FolderPanel.getBorderedComponent(infoPanel, 2, 0, 0, 0), null, true);

		return fpSessionInfo;
	}

	/**
	 * Instantiates the selection information panel and all necessary instance
	 * variables needed later on.
	 */
	private void instantiateSelectionInformationPanel() {

		this.panelSelectionInformation = new JPanel();
		this.panelSelectionInformation.setBackground(Color.WHITE);

		this.panelSelectionInformation.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { TableLayoutConstants.MINIMUM } }));

		this.panelSelectedSubsystemInfo = new JPanel();
		this.panelSelectedSubsystemInfo.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.MINIMUM }, { TableLayoutConstants.MINIMUM,
						TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM } }));

		this.lblNameOfSelectedSubsystem = new JLabel("");
		this.lblNumberMetabolitesOfSelectedSubsystem = new JLabel("");
		this.lblNumberReactionsOfSelectedSubsystem = new JLabel("");

		this.panelSelectedSubsystemInfo.add(new JLabel("<html><u>Selected&nbsp;Subsystem</u></html>"), "0,0");
		this.panelSelectedSubsystemInfo.add(this.lblNameOfSelectedSubsystem, "0,1");
		this.panelSelectedSubsystemInfo.add(this.lblNumberMetabolitesOfSelectedSubsystem, "0,2");
		this.panelSelectedSubsystemInfo.add(this.lblNumberReactionsOfSelectedSubsystem, "0,3");
		this.panelSelectedSubsystemInfo.setBackground(Color.WHITE);

		this.panelSelectedEdgeInfo = new JPanel();
		this.panelSelectedEdgeInfo.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.PREFERRED },
				{ TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
						TableLayoutConstants.MINIMUM, 3.0, TableLayoutConstants.MINIMUM, 50.0 } }));
		this.panelSelectedEdgeInfo.setBackground(Color.WHITE);

//		JPanel labelPanelSelectedEdgeInfo = new JPanel();
//		labelPanelSelectedEdgeInfo.setLayout(new TableLayout(new double[][] {
//			{TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM}, {TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM}
//		}));

		this.lblNameSubsystem1OfSelectedEdge = new JLabel("");
		this.lblNameSubsystem2OfSelectedEdge = new JLabel("");

		this.panelSelectedEdgeInfo.add(new JLabel("<html><u>Selected&nbsp;Edge</u></html>"), "0,0");
		this.panelSelectedEdgeInfo.add(new JLabel("Involved subsystems:"), "0,1");
//		labelPanelSelectedEdgeInfo.add(new JLabel("Subsystem 1: "), "0,1");
		this.panelSelectedEdgeInfo.add(this.lblNameSubsystem1OfSelectedEdge, "0,2");
//		labelPanelSelectedEdgeInfo.add(new JLabel("Subsystem 2: "), "0,2");
		this.panelSelectedEdgeInfo.add(this.lblNameSubsystem2OfSelectedEdge, "0,3");
//		labelPanelSelectedEdgeInfo.setBackground(Color.WHITE);
		this.panelSelectedEdgeInfo.add(new JLabel("Corresponding Interfaces:"), "0,5");

		this.interfaceListPanel = new JPanel();
		this.scrollPaneSelectedEdgeInfo = new JScrollPane(this.interfaceListPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//		this.panelSelectedEdgeInfo.add(labelPanelSelectedEdgeInfo, "0,0");
		this.panelSelectedEdgeInfo.add(this.scrollPaneSelectedEdgeInfo, "0,6");

		this.panelNoSelection = new JPanel();
		this.panelNoSelection.setLayout(new TableLayout(
				new double[][] { { TableLayoutConstants.PREFERRED }, { TableLayoutConstants.MINIMUM } }));
		this.panelNoSelection.add(
				new JLabel("<html>Select a single edge or a single<br> node in the overview graph.</html>"), "0,0");
		this.panelNoSelection.setBackground(Color.WHITE);

		this.panelSelectionInformation.add(this.panelNoSelection, "0,0");

	}

	/**
	 * Creates and returns the JComponent for the Settings.
	 * 
	 * @return
	 */
	private JComponent createSettingsComponent() {
		FolderPanel fpSettings = new FolderPanel("Settings", false, true, false, null);

		JLabel labelDecompMethod = new JLabel("Decomposition Method:");
		Set<String> decompositionAlgorithms = LMMEController.getInstance().getDecompositionAlgorithmsMap().keySet();
		String[] cbListDecompMethod = new String[decompositionAlgorithms.size()];
		int i = 0;
		for (String str : decompositionAlgorithms) {
			cbListDecompMethod[i++] = str;
		}

		this.cbDecompMethod = new JComboBox<>(cbListDecompMethod);
		JPanel decompMethodSelect = combine(labelDecompMethod, cbDecompMethod, Color.WHITE, false, true);
		fpSettings.addGuiComponentRow(FolderPanel.getBorderedComponent(decompMethodSelect, 0, 0, 5, 0), null, true);

		JLabel dummyLabel = new JLabel("");
		GuiRow cloningRow = new GuiRow(dummyLabel, null);
		cloningRow.span = true;
		fpSettings.addGuiComponentRow(cloningRow, true);

		GuiRow specificDecompSettingsRow = new GuiRow(new JLabel(""), null);
		specificDecompSettingsRow.span = true;
		fpSettings.addGuiComponentRow(specificDecompSettingsRow, true);

		JComponent compCloning = createCloningComponent();

		cbDecompMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String decompositionMethod = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				MMDecompositionAlgorithm decompositionAlgorithm = LMMEController.getInstance()
						.getDecompositionAlgorithmsMap().get(decompositionMethod);

				specificDecompSettingsRow.left = decompositionAlgorithm.getFolderPanel();
				if (decompositionAlgorithm.requiresCloning()) {
					cloningRow.left = compCloning;
				} else {
					cloningRow.left = dummyLabel;
				}
				// compCloning.setVisible(decompositionAlgorithm.requiresCloning());
				fpSettings.layoutRows();
			}
		});
		cbDecompMethod.setSelectedIndex(0);

		this.ckbAddTransporterSubS = new JCheckBox("Add Transporter Subsystem");
		this.ckbAddTransporterSubS
				.setToolTipText("Constructs another subsystem that consists of all transport reactions.");
		this.ckbAddTransporterSubS.setBackground(Color.WHITE);
		fpSettings.addGuiComponentRow(FolderPanel.getBorderedComponent(ckbAddTransporterSubS, 0, 0, 0, 0), null, true);

// Snippet might be useful in the future
//		ckbAddDefaultSubS.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent e) {
//				ckbSplitDefaultSubS.setEnabled(ckbAddDefaultSubS.isSelected());
//				tfSplitDefaultSubSThreshold.setEditable(ckbAddDefaultSubS.isSelected());
//			}
//		});

		JLabel labelOverviewLayoutAlgo = new JLabel("Layout Method:");
		Set<String> overviewLayouts = LMMEController.getInstance().getOverviewLayoutsMap().keySet();
		String[] cbListOverviewLayouts = new String[overviewLayouts.size()];
		i = 0;
		for (String str : overviewLayouts) {
			cbListOverviewLayouts[i++] = str;
		}

		this.cbOverviewLayout = new JComboBox<>(cbListOverviewLayouts);
		JPanel overviewLayoutSelect = combine(labelOverviewLayoutAlgo, this.cbOverviewLayout, Color.WHITE, false, true);
		fpSettings.addGuiComponentRow(FolderPanel.getBorderedComponent(overviewLayoutSelect, 5, 0, 0, 0), null, true);
		this.cbOverviewLayout.setSelectedItem(ForceDirectedMMLayout.name());

		return fpSettings;
	}

	private JComponent createCloningComponent() {

		FolderPanel fp = new FolderPanel("Cloning Species", false, true, false, null);

		JFrame clonableSpeciesFrame = new JFrame("Clonable Species Selection");
		JPanel clonableSpeciesPanel = new JPanel();
		JButton submitClonableSpecies = new JButton("Submit");
		clonableSpeciesFrame.setLayout(
				new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 300, TableLayoutConstants.FILL } }));
		JScrollPane clonableSpeciesScrollPane = new JScrollPane(clonableSpeciesPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		clonableSpeciesFrame.add(clonableSpeciesScrollPane, "0,0");
		clonableSpeciesFrame.add(submitClonableSpecies, "0,1");
		clonableSpeciesFrame.setSize(300, 375);

		JLabel labelSliderSplitDeg = new JLabel("Degree Threshold: ");
		JLabel labelSliderSplitDegCurr = new JLabel();
		JLabel labelSliderCorrespSpecies = new JLabel("Corresponding species: ");
		this.labelSliderCorrespSpeciesNumber = new JLabel();

		this.lblEditedCloneList = new JLabel(" (edited)");
		lblEditedCloneList.setVisible(false);

		this.sliderSplitDeg = new JSlider();
		if (SystemInfo.isMac()) {
			sliderSplitDeg.setPaintTrack(false);
		}
		sliderSplitDeg.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		sliderSplitDeg.setMinimum(2);
		sliderSplitDeg.setMaximum(30);
		sliderSplitDeg.setToolTipText("The minimum degree at which metabolites may be considered for cloning.");
		// sliderSplitDeg.setMinorTickSpacing(1);
		sliderSplitDeg.setPaintLabels(true);
		sliderSplitDeg.setPaintTicks(true);
		Hashtable<Integer, JComponent> sliderLabels = new Hashtable<>();
		for (int i = 5; i < sliderSplitDeg.getMaximum(); i += 5) {
			sliderLabels.put(Integer.valueOf(i), new JLabel(Integer.toString(i)));
		}
		// sliderLabels.put(Integer.valueOf(sliderSplitDeg.getMinimum()), new
		// JLabel(Integer.toString(sliderSplitDeg.getMinimum())));
		// sliderLabels.put(Integer.valueOf(sliderSplitDeg.getMaximum()), new
		// JLabel(Integer.toString(sliderSplitDeg.getMaximum())));

		sliderSplitDeg.setLabelTable(sliderLabels);
		sliderSplitDeg.setBackground(Color.WHITE);

		sliderSplitDeg.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int newValue = ((JSlider) e.getSource()).getValue();
				labelSliderSplitDegCurr.setText(String.valueOf(newValue));
				if (LMMEController.getInstance().getCurrentSession().getBaseGraph() != null) {
					labelSliderCorrespSpeciesNumber.setText(String.valueOf(LMMEController.getInstance()
							.getCurrentSession().getBaseGraph().getNumberOfSpeciesWithDegreeAtLeast(newValue)));
				}

				setEditedCloneList(false);
			}
		});

		sliderSplitDeg.setValue(this.defaultSplitDeg);

		JComponent splitDegSetting = TableLayout
				.getDoubleRow(combine(combine(labelSliderSplitDeg, labelSliderSplitDegCurr, Color.WHITE, false, false),
						combine(labelSliderCorrespSpecies, labelSliderCorrespSpeciesNumber, Color.WHITE, false, false),
						Color.WHITE, true, false), sliderSplitDeg, Color.WHITE);

		fp.addGuiComponentRow(splitDegSetting, null, true);

		JButton editCloneListButton = new JButton("Edit List");

		editCloneListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (editedCloneList) {
					clonableSpeciesFrame.setVisible(true);
				} else if (LMMEController.getInstance().getCurrentSession().getBaseGraph() != null) {
					// We use getOriginalSpeciesWithDeg... here to ensure the possibility to perform
					// another selection, even if there has already been done a decomposition based
					// on a former selection.
					ArrayList<Node> speciesAbove = LMMEController.getInstance().getCurrentSession().getBaseGraph()
							.getOriginalSpeciesWithDegreeAtLeast(sliderSplitDeg.getValue());
					// ArrayList<String> speciesAboveLabels = new ArrayList<>();
					// for (Node node : speciesAbove) {
					// speciesAboveLabels.add(AttributeHelper.getLabel(node, ""));
					// }
					clonableSpeciesPanel.removeAll();
					double[] rows = new double[speciesAbove.size()];
					for (int i = 0; i < rows.length; i++) {
						rows[i] = TableLayoutConstants.PREFERRED;
					}
					clonableSpeciesPanel
							.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, rows }));
					for (int i = 0; i < speciesAbove.size(); i++) {
						String speciesStr = AttributeHelper.getLabel(speciesAbove.get(i), "");
						JCheckBox speciesCheckBox = new JCheckBox(speciesStr, true);
						clonableSpeciesCheckBoxes.add(speciesCheckBox);
						clonableSpeciesPanel.add(speciesCheckBox, "0," + i);
						checkbox2nodeMap.put(speciesCheckBox, speciesAbove.get(i));
					}
					clonableSpeciesFrame.setVisible(true);
				}
			}
		});

		submitClonableSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clonableSpeciesFrame.setVisible(false);
				setEditedCloneList(true);
			}
		});

		JButton btnShowHistogram = new JButton("Show Degree Distribution");
		
		btnShowHistogram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (LMMEController.getInstance().getCurrentSession().isModelSet()) {
					HistogramDataset dataset = new HistogramDataset();
					
					ArrayList<Node> originalSpecies = LMMEController.getInstance().getCurrentSession().getBaseGraph().getOriginalSpeciesNodes();
					double[] degrees = new double[originalSpecies.size()];
					int i = 0;
					for (Node speciesNode : originalSpecies) {
						degrees[i] = (double) speciesNode.getDegree();
						i++;
					}
					dataset.addSeries("Data", degrees, 100, 0.0, 100.0);
					JFreeChart chart = ChartFactory.createHistogram("Degree Distribution", "Degree", null, dataset, PlotOrientation.VERTICAL, false, false, false);
					chart.getXYPlot().getRangeAxis().setRange(0.0, 100.0);
					ChartPanel chartPanel = new ChartPanel(chart);
					JFrame chartFrame = new JFrame();
					chartFrame.add(chartPanel);
					chartFrame.setSize(800, 500);
					chartFrame.setLocationRelativeTo(null);
					chartFrame.setVisible(true);
				}
			}
		});
		
		fp.addGuiComponentRow(combine(btnShowHistogram,combine(editCloneListButton, lblEditedCloneList, Color.WHITE, false, true), Color.WHITE, false, true), null, true);

		return fp;
	}

	private JComponent createSubsystemsViewComponent() {
		FolderPanel fp = new FolderPanel("Subsystem View", false, true, false, null);

		JPanel subsystemInfoPanel = new JPanel();

		subsystemInfoPanel.setLayout(new TableLayout(new double[][] {
				{ TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM },
				{ TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM } }));

		this.lblSubsystemInfoSubsystems = new JLabel("");
		this.lblSubsystemInfoMetabolites = new JLabel("");
		this.lblSubsystemInfoReactions = new JLabel("");

		subsystemInfoPanel.add(new JLabel("Shown Subsystems:"), "0,0");
		subsystemInfoPanel.add(this.lblSubsystemInfoSubsystems, "1,0");
		subsystemInfoPanel.add(new JLabel("Metabolites:"), "0,1");
		subsystemInfoPanel.add(this.lblSubsystemInfoMetabolites, "1,1");
		subsystemInfoPanel.add(new JLabel("Reactions:"), "0,2");
		subsystemInfoPanel.add(this.lblSubsystemInfoReactions, "1,2");
		subsystemInfoPanel.setBackground(Color.WHITE);

		fp.addGuiComponentRow(FolderPanel.getBorderedComponent(subsystemInfoPanel, 0, 0, 2, 0), null, true);

		JLabel labelSubsystemLayoutAlgo = new JLabel("Layout Method:");
		Set<String> subsystemLayouts = LMMEController.getInstance().getSubsystemLayoutsMap().keySet();
		String[] cbListSubsystemLayouts = new String[subsystemLayouts.size()];
		int i = 0;
		for (String str : subsystemLayouts) {
			cbListSubsystemLayouts[i++] = str;
		}

		this.cbSubsystemLayout = new JComboBox<>(cbListSubsystemLayouts);
		JPanel subsystemLayoutSelect = combine(labelSubsystemLayoutAlgo, this.cbSubsystemLayout, Color.WHITE, false,
				true);
		fp.addGuiComponentRow(FolderPanel.getBorderedComponent(subsystemLayoutSelect, 5, 0, 5, 0), null, true);
		this.cbSubsystemLayout.setSelectedItem(ForceDirectedMMLayout.name());

		this.ckbClearSubsView = new JCheckBox("Clear subsystem view before");
		ckbClearSubsView.setSelected(true);
		ckbClearSubsView.setBackground(Color.WHITE);
		fp.addGuiComponentRow(ckbClearSubsView, null, true);

		this.ckbUseColorMapping = new JCheckBox("Use Colors for Subsystems");
		ckbUseColorMapping.setSelected(true);
		ckbUseColorMapping.setBackground(Color.WHITE);
		fp.addGuiComponentRow(FolderPanel.getBorderedComponent(ckbUseColorMapping, 5, 0, 0, 0), null, true);

		return fp;
	}

	private void setSliderMax(int max) {

		this.sliderSplitDeg.setMaximum(max);

		int stepSize;

		if (max <= 50) {
			stepSize = 5;
		} else if (max <= 100) {
			stepSize = 10;
		} else if (max <= 150) {
			stepSize = 15;
		} else {
			stepSize = max / 50;
			stepSize *= 5;
		}

		Hashtable<Integer, JComponent> sliderLabels = new Hashtable<>();
		for (int i = stepSize; i < sliderSplitDeg.getMaximum(); i += stepSize) {
			sliderLabels.put(Integer.valueOf(i), new JLabel(Integer.toString(i)));
		}
		// sliderLabels.put(Integer.valueOf(this.sliderSplitDeg.getMinimum()),
		// new JLabel(Integer.toString(this.sliderSplitDeg.getMinimum())));
		// sliderLabels.put(Integer.valueOf(this.sliderSplitDeg.getMaximum()),
		// new JLabel(Integer.toString(this.sliderSplitDeg.getMaximum())));
		sliderSplitDeg.setPaintTicks(true);
		sliderSplitDeg.setLabelTable(sliderLabels);
	}

	/**
	 * Updates the selection info at the top of the tab. The subsystems names will
	 * be shown as labels, while the interface names will be shown as scrollable
	 * list.
	 * 
	 * @param subsystem1     The name of the first subsystem
	 * @param subsystem2     The name of the secons subsystem
	 * @param interfaceNames The interface names to be shown in a scrollable list
	 */
	public void showSelectedEdgeInfo(String subsystem1, String subsystem2, ArrayList<String> interfaceNames) {
		this.panelSelectionInformation.removeAll();

		String name1ToShow = subsystem1.length() > 25 ? subsystem1.substring(0, 24) + "..." : subsystem1;
		String name2ToShow = subsystem2.length() > 25 ? subsystem2.substring(0, 24) + "..." : subsystem2;

		this.lblNameSubsystem1OfSelectedEdge.setText("- " + name1ToShow);
		this.lblNameSubsystem2OfSelectedEdge.setText("- " + name2ToShow);

		this.interfaceListPanel.removeAll();
		double[] rows = new double[interfaceNames.size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = TableLayoutConstants.MINIMUM;
		}
		this.interfaceListPanel.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, rows }));

		for (int i = 0; i < interfaceNames.size(); i++) {
//			String labelToShow = interfaceNames.get(i).length() > 25 ? interfaceNames.get(i).substring(0, 24) + "..." : interfaceNames.get(i);
			JLabel lbl = new JLabel(interfaceNames.get(i));
			interfaceListPanel.add(lbl, "0," + i);
		}

		this.panelSelectionInformation.add(this.panelSelectedEdgeInfo, "0,0");
		this.panelSelectionInformation.revalidate();
		this.panelSelectionInformation.repaint();
	}

	/**
	 * Resets the selection info. needs to be called when no or more than one
	 * entities are selected.
	 */
	public void resetSelectionInfo() {
		this.panelSelectionInformation.removeAll();
		this.panelSelectionInformation.add(this.panelNoSelection, "0,0");
		this.panelSelectionInformation.revalidate();
		this.panelSelectionInformation.repaint();
	}

	/**
	 * Updates the selection info at the top of the tab. all information will be
	 * shown as labels.
	 * 
	 * @param subsystemName
	 * @param numberOfMetabolites
	 * @param numberOfReactions
	 */
	public void showSelectedSubsystemInfo(String subsystemName, int numberOfMetabolites, int numberOfReactions) {
		this.panelSelectionInformation.removeAll();

		String nameToShow = subsystemName.length() > 25 ? subsystemName.substring(0, 24) + "..." : subsystemName;
		this.lblNameOfSelectedSubsystem.setText(nameToShow);
		this.lblNumberMetabolitesOfSelectedSubsystem.setText(Integer.toString(numberOfMetabolites) + " Metabolites");
		this.lblNumberReactionsOfSelectedSubsystem.setText(Integer.toString(numberOfReactions) + " Reactions");

		this.panelSelectionInformation.add(this.panelSelectedSubsystemInfo, "0,0");
		this.panelSelectionInformation.revalidate();
		this.panelSelectionInformation.repaint();
	}

	/**
	 * Sets the corresponding labels in the subsystem information panel.
	 * 
	 * @param numberOfSubsystems
	 * @param numberOfMetabolites
	 * @param numberOfReactions
	 */
	public void setSubsystemInfo(int numberOfSubsystems, int numberOfMetabolites, int numberOfReactions) {
		this.lblSubsystemInfoSubsystems.setText(Integer.toString(numberOfSubsystems));
		this.lblSubsystemInfoMetabolites.setText(Integer.toString(numberOfMetabolites));
		this.lblSubsystemInfoReactions.setText(Integer.toString(numberOfReactions));
	}

	/**
	 * Resets the corresponding labels in the subsystem information panel.
	 */
	public void resetSubsystemInfo() {
		this.lblSubsystemInfoSubsystems.setText("");
		this.lblSubsystemInfoMetabolites.setText("");
		this.lblSubsystemInfoReactions.setText("");
	}

	/**
	 * Sets the corresponding labels in the session information panel.
	 * 
	 * @param name
	 * @param numberOfMetabolites
	 * @param numberOfReactions
	 */
	public void setBaseGraphInfo(String name, int numberOfMetabolites, int numberOfReactions) {
		String nameToShow = name.length() > 25 ? name.substring(0, 24) + "..." : name;
		this.lblSessionInfoBaseGraph.setText(nameToShow);
		this.lblSessionInfoMetabolites.setText(Integer.toString(numberOfMetabolites));
		this.lblSessionInfoReactions.setText(Integer.toString(numberOfReactions));
	}

	/**
	 * Resets the corresponding labels in the session information panel.
	 */
	public void resetBaseGraphInfo() {
		this.lblSessionInfoBaseGraph.setText("");
		this.lblSessionInfoMetabolites.setText("");
		this.lblSessionInfoReactions.setText("");
	}

	/**
	 * Sets the corresponding label in the session information panel.
	 * 
	 * @param numberOfSubsystems
	 */
	public void setLblNumberOfSubsystems(int numberOfSubsystems) {
		this.lblSessionInfoSubsystems.setText(Integer.toString(numberOfSubsystems));
	}

	/**
	 * Resets the corresponding label in the session information panel.
	 */
	public void resetLblNumberOfSubsystems() {
		this.lblSessionInfoSubsystems.setText("");
	}

	/**
	 * Returns the currently selected decomposition method.
	 * 
	 * @return
	 */
	public String getDecompositionMethod() {
		return ((String) this.cbDecompMethod.getSelectedItem());
	}

	/**
	 * Returns the currently selected overview layout method.
	 * 
	 * @return
	 */
	public String getOverviewLayoutMethod() {
		return ((String) this.cbOverviewLayout.getSelectedItem());
	}

	/**
	 * Returns the currently selected subsystem layout method.
	 * 
	 * @return
	 */
	public String getSubsystemLayoutMethod() {
		return ((String) this.cbSubsystemLayout.getSelectedItem());
	}

	public boolean getMapToEdgeThickness() {
		return this.ckbMapToEdgeThickness.isSelected();
	}

	public boolean getDrawEdges() {
		return this.ckbDrawEdges.isSelected();
	}

	/**
	 * @return the ckbAddTransporterSubS
	 */
	public boolean getAddTransporterSubS() {
		return this.ckbAddTransporterSubS.isSelected();
	}

	/**
	 * Returns whether the clear subsystem view checkbox is currently selected.
	 * 
	 * @return
	 */
	public boolean getClearSubsystemView() {
		return this.ckbClearSubsView.isSelected();
	}

	/**
	 * @return the ckbUseColorMapping
	 */
	public boolean getCkbUseColorMapping() {
		return this.ckbUseColorMapping.isSelected();
	}

	public int getClonableSpeciesThreshold() {
		return this.sliderSplitDeg.getValue();
	}

	public boolean isEditedCloneList() {
		return editedCloneList;
	}

	private void setEditedCloneList(boolean editedCloneList) {
		this.editedCloneList = editedCloneList;
		this.lblEditedCloneList.setVisible(editedCloneList);
		if (editedCloneList) {
			clonableSpeciesCheckBoxesSubmitted = (ArrayList<JCheckBox>) clonableSpeciesCheckBoxes.clone();
		} else {
			clonableSpeciesCheckBoxesSubmitted.clear();
			clonableSpeciesCheckBoxes.clear();
		}
	}

	/**
	 * 
	 * @return the species to be cloned. Species in the returned list are from the
	 *         originalGraph
	 */
	public ArrayList<Node> getClonableSpecies() {
		ArrayList<Node> res = new ArrayList<>();
		if (editedCloneList) {
			for (JCheckBox cb : clonableSpeciesCheckBoxesSubmitted) {
				if (cb.isSelected()) {
					res.add(checkbox2nodeMap.get(cb));
				}
			}
		} else {
			return LMMEController.getInstance().getCurrentSession().getBaseGraph()
					.getOriginalSpeciesWithDegreeAtLeast(sliderSplitDeg.getValue());
		}
		return res;
	}

	/**
	 * This method grabs the current values for any GUI element that represents or
	 * relies on external information and accordingly updates the appearance of
	 * these elements.
	 */
	public void updateGUI() {
		// TODO implement: metabolites/reactions/slider dimensions/number above/...
		LMMESession session = LMMEController.getInstance().getCurrentSession();
		if (session.isModelSet()) {
			BaseGraph baseGraph = session.getBaseGraph();
			this.labelSliderCorrespSpeciesNumber.setText(
					String.valueOf(baseGraph.getNumberOfSpeciesWithDegreeAtLeast(this.getClonableSpeciesThreshold())));
			this.setSliderMax(baseGraph.getMaximumDegree());

			String decompositionMethod = this.getDecompositionMethod();
			MMDecompositionAlgorithm decompositionAlgorithm = LMMEController.getInstance()
					.getDecompositionAlgorithmsMap().get(decompositionMethod);
			decompositionAlgorithm.updateFolderPanel();
		} else {
			resetBaseGraphInfo();
			this.labelSliderCorrespSpeciesNumber.setText("");
			this.setSliderMax(30);
		}
		if (session.isOverviewGraphConstructed()) {
			OverviewGraph overviewGraph = session.getOverviewGraph();
			setLblNumberOfSubsystems(overviewGraph.getDecomposition().getSubsystems().size());
		} else {
			resetLblNumberOfSubsystems();
		}
		// TODO SubsystemView Management query, what is shown?!
	}

	/**
	 * This method allows to create a log message in the session information panel.
	 * 
	 * @param msg The message to be logged.
	 */
	public void logMsg(String msg) {
		this.monitorLayout.insertRow(0, TableLayoutConstants.PREFERRED);
		this.monitorInnerPanel.add(new JLabel(msg), "0,0");
		SwingUtilities.invokeLater(() -> {
			JScrollBar scrollBar = this.monitorScrollPane.getVerticalScrollBar();
			scrollBar.setValue(scrollBar.getMaximum());
		});
		this.monitorInnerPanel.updateUI();
	}

	/**
	 * This method adds the given element as a new row into the given FolderPanel in
	 * a way such that it spans the whole row
	 * 
	 * @param el    The element to be added.
	 * @param panel The FolderPanel into which the element should be inserted.
	 */
	private static void addOneElementSpanning(JComponent el, FolderPanel panel) {
		GuiRow gr = new GuiRow(el, null);
		gr.span = true;
		panel.addGuiComponentRow(gr, true);
	}

	/**
	 * This method is used to form a group of two JComponents.
	 * 
	 * @param left   The left component
	 * @param right  The right component
	 * @param color  The background color of the new component
	 * @param fill   States whether the components should be aligned in a way that
	 *               uses the entire horizontally available space.
	 * @param spaced States whether there should be a small space separating the
	 *               combined components.
	 * @return The new component that results from combining the left and the right
	 *         component.
	 */
	public static JPanel combine(JComponent left, JComponent right, Color color, boolean fill, boolean spaced) {
		JPanel combinedPanel = new JPanel();
		combinedPanel
				.setLayout(
						new TableLayout(
								new double[][] {
										{ fill == true ? TableLayoutConstants.FILL : TableLayoutConstants.PREFERRED,
												spaced == true ? 5.0 : 0.0,
												fill == true ? TableLayoutConstants.FILL
														: TableLayoutConstants.PREFERRED },
										{ TableLayoutConstants.PREFERRED } }));
		combinedPanel.add(left, "0,0");
		combinedPanel.add(right, "2,0");
		combinedPanel.setBackground(color);
		return combinedPanel;
	}

	private static JPanel combine3(JComponent left, JComponent mid, JComponent right, Color color, boolean spaced) {
		JPanel combinedPanel = new JPanel();
		combinedPanel
				.setLayout(new TableLayout(new double[][] {
						{ TableLayoutConstants.FILL, spaced == true ? 5.0 : 0.0, TableLayoutConstants.FILL,
								spaced == true ? 5.0 : 0.0, TableLayoutConstants.FILL },
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

		return "LMME";

	}

	@Override
	public boolean visibleForView(View v) {

		return true;

	}

}
