package org.vanted.addons.wholecell;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

@SuppressWarnings("nls")
public class WholecellTab extends InspectorTab {
	
	private static final long serialVersionUID = 1L;
	private static JButton jReadNotes;
	private static JButton jRequestAdditionalSubsystemsFromKEGG;
	private static JButton jSetSpeciesSubsystems;
	private static JButton jCreateOverviewGraph;
	private static JButton jCloneSpecies;
	private static JButton jSeparateCompartments;
	private static JButton jLayoutModelForceDirected;
	private static JButton jLayoutModelUnconnectedSubgraphs;
	private static JButton jFilterNonTransporters;
	private static JButton jCreatePathwayViews;
	
	public WholecellTab() {
		
		double[][] size = { { 2, TableLayoutConstants.FILL, 2 }, // columns
				{ 2, TableLayoutConstants.PREFERRED, // rows
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						12, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						2, TableLayoutConstants.PREFERRED,
						// 2, TableLayoutConstants.PREFERRED,
						2 } };
		
		setLayout(new TableLayout(size));
		int rowIndex = 1;
		
		jReadNotes = createButton("Read Notes from SBML", WholecellTools.readNotes());
		// setReadNotes();
		add(jReadNotes, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jRequestAdditionalSubsystemsFromKEGG = createButton("Request Additional Subsystems from KEGG", WholecellTools.requestAdditionalSubsystemsFromKEGG());
		setRequestAdditionalSubsystemsFromKEGG();
		add(jRequestAdditionalSubsystemsFromKEGG, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jSetSpeciesSubsystems = createButton("Set Subsystems for Species", WholecellTools.setSpeciesSubsystems());
		setSetSpeciesSubsystems();
		add(jSetSpeciesSubsystems, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jCreateOverviewGraph = createButton("Create Overview Graph", WholecellTools.createOverviewGraph());
		setCreateOverviewGraph();
		add(jCreateOverviewGraph, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jCloneSpecies = createButton("Clone Species", WholecellTools.cloneSpecies());
		setCloneSpecies();
		add(jCloneSpecies, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jSeparateCompartments = createButton("Separate Compartments", WholecellTools.separateCompartments());
		setSeparateCompartments();
		add(jSeparateCompartments, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jLayoutModelForceDirected = createButton("Layout Model (force directed)", WholecellTools.layoutModelForceDirected());
		setLayoutModelForceDirected();
		add(jLayoutModelForceDirected, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jLayoutModelUnconnectedSubgraphs = createButton("Layout Model (unconnected subgraphs)", WholecellTools.layoutModelUnconnectedSubgraphs());
		setLayoutModelUnconnectedSubgraphs();
		add(jLayoutModelUnconnectedSubgraphs, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jFilterNonTransporters = createButton("Filter Non-Transporters", WholecellTools.filterNonTransporters());
		setFilterNonTransporters();
		add(jFilterNonTransporters, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		jCreatePathwayViews = createButton("Create Pathway Views", WholecellTools.createPathwayViews());
		setCreatePathwayViews();
		add(jCreatePathwayViews, new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		add(createButton("Copy Selection to Clipboard", WholecellTools.copySelectionToClipboard()), new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		add(createButton("Paste Selection from Clipboard", WholecellTools.pasteSelectionFromClipboard()), new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		add(createButton("Paste Selection from Clipboard to all Sessions", WholecellTools.pasteSelectionFromClipboardToAllSessions()),
				new String("1," + rowIndex));
		rowIndex = rowIndex + 2;
		
		// add(createButton("Test", ), new String("1," + rowIndex));
		// rowIndex = rowIndex + 2;
		
	}
	
	public static void setReadNotes() {
		
		jReadNotes.setEnabled(!jReadNotes.isEnabled());
		
	}
	
	public static void setRequestAdditionalSubsystemsFromKEGG() {
		
		jRequestAdditionalSubsystemsFromKEGG.setEnabled(!jRequestAdditionalSubsystemsFromKEGG.isEnabled());
		
	}
	
	public static void setSetSpeciesSubsystems() {
		
		jSetSpeciesSubsystems.setEnabled(!jSetSpeciesSubsystems.isEnabled());
		
	}
	
	public static void setCreateOverviewGraph() {
		
		jCreateOverviewGraph.setEnabled(!jCreateOverviewGraph.isEnabled());
		
	}
	
	public static void setCloneSpecies() {
		
		jCloneSpecies.setEnabled(!jCloneSpecies.isEnabled());
		
	}
	
	public static void setSeparateCompartments() {
		
		jSeparateCompartments.setEnabled(!jSeparateCompartments.isEnabled());
		
	}
	
	public static void setLayoutModelForceDirected() {
		
		jLayoutModelForceDirected.setEnabled(!jLayoutModelForceDirected.isEnabled());
		
	}
	
	public static void setLayoutModelUnconnectedSubgraphs() {
		
		jLayoutModelUnconnectedSubgraphs.setEnabled(!jLayoutModelUnconnectedSubgraphs.isEnabled());
		
	}
	
	public static void setFilterNonTransporters() {
		
		jFilterNonTransporters.setEnabled(!jFilterNonTransporters.isEnabled());
		
	}
	
	public static void setCreatePathwayViews() {
		
		jCreatePathwayViews.setEnabled(!jCreatePathwayViews.isEnabled());
		
	}
	
	private static JButton createButton(String text, ActionListener actionListener) {
		
		JButton jButton = new JButton(text);
		jButton.putClientProperty("JButton.buttonType", "square");
		jButton.setOpaque(true);
		jButton.addActionListener(actionListener);
		return jButton;
		
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
