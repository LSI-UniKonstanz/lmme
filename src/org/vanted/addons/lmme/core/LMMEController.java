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
package org.vanted.addons.lmme.core;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.session.Session;
import org.graffiti.util.InstanceLoader;
import org.vanted.addons.lmme.analysis.OverRepresentationAnalysis;
import org.vanted.addons.lmme.decomposition.CompartmentMMDecomposition;
import org.vanted.addons.lmme.decomposition.DiseaseMapPathwayDecomposition;
import org.vanted.addons.lmme.decomposition.KeggMMDecomposition;
import org.vanted.addons.lmme.decomposition.MMDecomposition;
import org.vanted.addons.lmme.decomposition.MMDecompositionAlgorithm;
import org.vanted.addons.lmme.decomposition.PredefinedMMDecomposition;
import org.vanted.addons.lmme.decomposition.SchusterMMDecomposition;
import org.vanted.addons.lmme.graphs.BaseGraph;
import org.vanted.addons.lmme.graphs.OverviewGraph;
import org.vanted.addons.lmme.graphs.SubsystemGraph;
import org.vanted.addons.lmme.layout.CircularMMLayout;
import org.vanted.addons.lmme.layout.ConcentricCirclesMMLayout;
import org.vanted.addons.lmme.layout.ForceDirectedMMLayout;
import org.vanted.addons.lmme.layout.GridMMLayout;
import org.vanted.addons.lmme.layout.MMOverviewLayout;
import org.vanted.addons.lmme.layout.MMSubsystemLayout;
import org.vanted.addons.lmme.layout.ParallelLinesMMLayout;
import org.vanted.addons.lmme.layout.StressMinMMLayout;
import org.vanted.addons.lmme.ui.LMMESubsystemViewManagement;
import org.vanted.addons.lmme.ui.LMMETab;
import org.vanted.addons.lmme.ui.LMMEViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.MergeNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * This class controls and coordinates the actions of LMME.
 * <p>
 * It implements the action listeners for the main button functions in the tab and maintains the
 * available layout and decomposition algorithms.
 * 
 * @author Michael Aichem
 * @author Tobias Czauderna
 */
public class LMMEController {
	
	private static LMMEController instance;
	
	private LMMESession currentSession;
	
	private LMMETab tab;
	
	/**
	 * A {@code HashMap} that maps the name as {@code String} to the actual decomposition method object as {@code MMDecompositionAlgorithm}.
	 */
	private HashMap<String, MMDecompositionAlgorithm> decompositionAlgorithmsMap = new HashMap<>();
	
	/**
	 * A {@code HashMap} that maps the name as {@code String} to the actual overview layout method object as {@code MMOverviewLayout}.
	 */
	private HashMap<String, MMOverviewLayout> overviewLayoutsMap = new HashMap<>();
	
	/**
	 * A {@code HashMap} that maps the name as {@code String} to the actual subsystem graph layout method object as {@code MMSubsystemLayout}.
	 */
	private HashMap<String, MMSubsystemLayout> subsystemLayoutsMap = new HashMap<>();
	
	/**
	 * The constructor of the controller.
	 * <p>
	 * During execution, all objects for the decomposition and layout methods are created and put in the respective {@code HashMap}s.
	 */
	private LMMEController() {
		
		currentSession = new LMMESession();
		
		PredefinedMMDecomposition predefDecomp = new PredefinedMMDecomposition();
		KeggMMDecomposition keggDecomp = new KeggMMDecomposition();
		SchusterMMDecomposition schusterDecomp = new SchusterMMDecomposition();
		CompartmentMMDecomposition compartmentDecomp = new CompartmentMMDecomposition();
		DiseaseMapPathwayDecomposition diseaseMapPathwayDecomp = new DiseaseMapPathwayDecomposition();
//		GirvanMMDecomposition girvanDecomp = new GirvanMMDecomposition();
		
//		decompositionAlgorithmsMap.put(predefDecomp.getName(), predefDecomp);
//		decompositionAlgorithmsMap.put(keggDecomp.getName(), keggDecomp);
		decompositionAlgorithmsMap.put(schusterDecomp.getName(), schusterDecomp);
		decompositionAlgorithmsMap.put(compartmentDecomp.getName(), compartmentDecomp);
		decompositionAlgorithmsMap.put(diseaseMapPathwayDecomp.getName(), diseaseMapPathwayDecomp);
//		decompositionAlgorithmsMap.put(girvanDecomp.getName(), girvanDecomp);
		
		StressMinMMLayout stressMinLayout = new StressMinMMLayout();
		ForceDirectedMMLayout forceLayout = new ForceDirectedMMLayout();
		ConcentricCirclesMMLayout concentricCircLayout = new ConcentricCirclesMMLayout();
		ParallelLinesMMLayout parallelLinesLayout = new ParallelLinesMMLayout();
		CircularMMLayout circularLayout = new CircularMMLayout();
		GridMMLayout gridLayout = new GridMMLayout();
		
		overviewLayoutsMap.put(stressMinLayout.getName(), stressMinLayout);
		overviewLayoutsMap.put(forceLayout.getName(), forceLayout);
		overviewLayoutsMap.put(circularLayout.getName(), circularLayout);
		overviewLayoutsMap.put(gridLayout.getName(), gridLayout);
		
		subsystemLayoutsMap.put(stressMinLayout.getName(), stressMinLayout);
		subsystemLayoutsMap.put(forceLayout.getName(), forceLayout);
		subsystemLayoutsMap.put(concentricCircLayout.getName(), concentricCircLayout);
		subsystemLayoutsMap.put(parallelLinesLayout.getName(), parallelLinesLayout);
		
	}
	
	public static synchronized LMMEController getInstance() {
		if (LMMEController.instance == null) {
			LMMEController.instance = new LMMEController();
		}
		return LMMEController.instance;
	}
	
	public HashMap<String, MMDecompositionAlgorithm> getDecompositionAlgorithmsMap() {
		return decompositionAlgorithmsMap;
	}
	
	public HashMap<String, MMOverviewLayout> getOverviewLayoutsMap() {
		return overviewLayoutsMap;
	}
	
	public HashMap<String, MMSubsystemLayout> getSubsystemLayoutsMap() {
		return subsystemLayoutsMap;
	}
	
	public LMMESession getCurrentSession() {
		return currentSession;
	}
	
	public void setCurrentSession(LMMESession currentSession) {
		this.currentSession = currentSession;
	}
	
	public LMMETab getTab() {
		return tab;
	}
	
	public void setTab(LMMETab tab) {
		this.tab = tab;
	}
	
	/**
	 * Implements the action for the 'Set Model' button in the Add-On tab.
	 * <p>
	 * The currently selected SBML model is set as {@link BaseGraph} of the current session.
	 */
	public void setModelAction() {
		if (this.currentSession.isModelSet()) {
			// 0=Yes, 1=No, -1=window closed
			int option = JOptionPane.showConfirmDialog(null,
					"<html>The base graph has already been set. Re-setting it will start a new session <br>"
							+ "and delete the data from the current session. Do you want to continue?</html>",
					"Warning: Model already set", JOptionPane.YES_NO_OPTION);
			if (option == 0) {
				LMMEViewManagement.getInstance().closeFrames();
				resetSession();
			} else {
				return;
			}
		}
		if (MainFrame.getInstance().getActiveEditorSession() != null) {
			Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
			SBMLSpeciesHelper helper = new SBMLSpeciesHelper(graph);
			if (helper.getSpeciesNodes().isEmpty()) {
				JOptionPane.showMessageDialog(null, "The currently active graph is no SBML model.");
				return;
			}
			this.currentSession.setBaseGraph(new BaseGraph(graph));
			LMMESubsystemViewManagement.getInstance().resetLists();
		} else {
			JOptionPane.showMessageDialog(null, "There is no active model.");
			return;
		}
	}
	
	public void aggregateModelsAction() {
//		aggregateModels();
		setModelAction();
	}
	
	private void aggregateModels() {
		
		Graph aggregatedGraph = new AdjListGraph();
		aggregatedGraph.setName("Aggregated Model");
		Set<Session> session = new HashSet<Session>(MainFrame.getSessions());
		HashSet<String> allUniqueNames = new HashSet<String>();
		for (Session s : session) {
			Graph tempGraph = new AdjListGraph();
			tempGraph.addGraph(s.getGraph());
			// get name without '.xml' suffix.
			String tempName = s.getGraph().getName().substring(0, s.getGraph().getName().length() - 4);
			for (Node reactionNode : tempGraph.getNodes()) {
				if (LMMETools.getInstance().isReaction(reactionNode)) {
					AttributeHelper.setAttribute(reactionNode, LMMEConstants.ATTRIBUTE_PATH, LMMEConstants.DISEASE_MAP_PATHWAY_ATRIBUTE, tempName);
				}
			}
			
			ArrayList<String> allNames = new ArrayList<String>();
			HashSet<String> uniqueNames = new HashSet<String>();
			
			for (Node speciesNode : tempGraph.getNodes()) {
				if (LMMETools.getInstance().isSpecies(speciesNode)) {
					if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_COMPARTMENT_NAME)) {
						String compName = (String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML,
								SBML_Constants.SPECIES_COMPARTMENT_NAME, "", "");
						allNames.add(compName);
						uniqueNames.add(compName);
						allUniqueNames.add(compName);
					}
				}
			}
			
//			System.out.println(tempName + ":");
			for (String compName : uniqueNames) {
				int occurences = 0;
				for (int i = 0; i < allNames.size(); i++) {
					if (allNames.get(i).equals(compName)) {
						occurences += 1;
					}
				}
//				System.out.println("\t" + occurences + " occurences of " + compName);
			}
			
			aggregatedGraph.addGraph(tempGraph);
			s.getGraph().setModified(false);
			MainFrame.getInstance().getSessionManager().closeSession(s);
		}
//		System.out.println("All compartments:");
		for (String compName : allUniqueNames) {
//			System.out.println("\t" + compName);
		}
		
		HashMap<String, ArrayList<Node>> label2NodeListMap = new HashMap<String, ArrayList<Node>>();
		for (Node speciesNode : aggregatedGraph.getNodes()) {
			String speciesName = AttributeHelper.getLabel(speciesNode, "");
			
			// only merge species, and ignore those with generic names such as 's234'
			if (LMMETools.getInstance().isSpecies(speciesNode) && !speciesName.split(Pattern.quote("__"))[0].matches("s\\d+")) {
				if (!label2NodeListMap.containsKey(speciesName)) {
					label2NodeListMap.put(speciesName, new ArrayList<Node>());
				}
				label2NodeListMap.get(speciesName).add(speciesNode);
			}
		}
		for (ArrayList<Node> nodesToMerge : label2NodeListMap.values()) {
			if (nodesToMerge.size() > 1) {
				Node newNode = MergeNodes.mergeNode(aggregatedGraph, nodesToMerge, NodeTools.getCenter(nodesToMerge), false);
				aggregatedGraph.deleteAll(nodesToMerge);
			}
		}
		
		// get rid of unused cluster attributes.
		for (Node node : aggregatedGraph.getNodes()) {
			if (AttributeHelper.hasAttribute(node, "cluster", "cluster")) {
				AttributeHelper.deleteAttribute(node, "cluster", "cluster");
			}
		}
		
		MainFrame.getInstance().showGraph(aggregatedGraph, null);
//		setModelAction();
		
	}
	
	/**
	 * Implements the action for the 'Show Overview Graph' button in the Add-On tab.
	 * <p>
	 * Using the {@link BaseGraph} of the {@link #currentSession}, the selected decomposition is performed and the overview graph is created and drawn according
	 * to the selected layout method.
	 */
	public void showOverviewGraphAction() {
		if (this.currentSession.isModelSet()) {
			if (this.currentSession.isOverviewGraphConstructed()) {
				// 0=Yes, 1=No, -1=window closed
				int option = JOptionPane.showConfirmDialog(null,
						"<html>The overview graph has already been constructed. Constructing a new one will start a new session <br>"
								+ "where the base graph is kept while the remaining data from the current session is deleted. Do you want to continue?</html>",
						"Warning: Overview graph already constructed", JOptionPane.YES_NO_OPTION);
				if ((option == 1) || (option == -1)) {
					return;
				}
				LMMEViewManagement.getInstance().closeFrames();
				partiallyResetSession();
			}
			
			Thread decompositionThread = new Thread(new Runnable() {
				public void run() {
					MMDecomposition decomposition = decompositionAlgorithmsMap.get(tab.getDecompositionMethod())
							.run(tab.getAddTransporterSubS());
					currentSession.setOverviewGraph(new OverviewGraph(decomposition, tab.getShowInterfaces()));
					MainFrame.showMessage("Calculating Layout ...", MessageType.PERMANENT_INFO);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							LMMEViewManagement.getInstance()
									.showAsOverviewGraph(currentSession.getOverviewGraph().getGraph());
							overviewLayoutsMap.get(tab.getOverviewLayoutMethod()).layOutAsOverview(
									LMMEViewManagement.getInstance().getOverviewFrame().getView().getGraph());
							LMMESubsystemViewManagement.getInstance().resetLists();
							tab.setLblNumberOfSubsystems(decomposition.getSubsystems().size());
							tab.updateOptions();
						}
					});
				}
			});
			decompositionThread.setName("Decomposition");
//			keggRequestThread.setPriority(Thread.MIN_PRIORITY);
			decompositionThread.start();
		} else {
			JOptionPane.showMessageDialog(null, "No base graph was set.");
			return;
		}
		
	}
	
	/**
	 * Implements the action for the 'Show Selected Subsystems' button in the Add-On tab.
	 * <p>
	 * The selected subsystem nodes are read and the {@link SubsystemGraph} is constructed and laid out according to the selected layout method.
	 */
	public void showSubsystemGraphsAction() {
		if (this.currentSession.getOverviewGraph() != null) {
			ArrayList<SubsystemGraph> selectedSubsystems = this.currentSession.getOverviewGraph().getSelectedSubsystems();
			if (!selectedSubsystems.isEmpty()) {
				LMMESubsystemViewManagement.getInstance().showSubsystems(selectedSubsystems,
						this.tab.getClearSubsystemView(), this.tab.getCkbUseColorMapping());
				this.subsystemLayoutsMap.get(this.tab.getSubsystemLayoutMethod())
						.layOutAsSubsystems(LMMEViewManagement.getInstance().getSubsystemFrame().getView().getGraph());
			} else {
				JOptionPane.showMessageDialog(null, "There are no subsystems selected in the overview graph.");
				return;
			}
		} else {
			JOptionPane.showMessageDialog(null, "There was no overview graph constructed so far.");
		}
		
	}
	
	/**
	 * Implements the action for the 'Over-Representation Analysis' button in the Add-On tab.
	 * <p>
	 * The Over-Representation Analysis is performed and the results are highlighted in the overview graph.
	 * 
	 * @param pathToDifferentiallyExpressedFile
	 *           the path to the differentially expressed metabolites file in the local file system
	 * @param pathToReferenceFile
	 *           the path to the reference metabolites file in the local file system, may be null
	 * @throws IOException
	 */
	public void oraAction(String pathToDifferentiallyExpressedFile, String pathToReferenceFile) throws IOException {
		
		if (this.currentSession.isOverviewGraphConstructed()) {
			OverRepresentationAnalysis ora = new OverRepresentationAnalysis(pathToDifferentiallyExpressedFile,
					pathToReferenceFile);
			HashSet<SubsystemGraph> significantSubsystems = ora.getSignificantSubsystems();
			LMMESubsystemViewManagement.getInstance().resetOverviewGraphColoring();
			OverviewGraph og = getCurrentSession().getOverviewGraph();
			for (SubsystemGraph subsystem : significantSubsystems) {
				AttributeHelper.setFillColor(og.getNodeOfSubsystem(subsystem), Color.RED);
			}
		} else {
			JOptionPane.showMessageDialog(null, "There was no overview graph constructed so far.");
		}
	}
	
	/**
	 * Implements the action for the 'Translate to SBGN' button in the Add-On tab.
	 * <p>
	 * Translates the representation of the {@link SubsystemGraph} to SBGN if the SBGN-ED add-on is available.
	 */
	public void transformToSbgnAction() {
		
		GraffitiInternalFrame gif = LMMEViewManagement.getInstance().getSubsystemFrame();
		
		if (gif != null) {
			try {
				Class<?> SBMLTranslationMode = Class.forName("org.sbgned.translation.SBMLTranslationMode", true,
						InstanceLoader.getCurrentLoader());
				Object[] enumConstants = SBMLTranslationMode.getEnumConstants();
				Class<?> SBMLTranslation = Class.forName("org.sbgned.translation.SBMLTranslation", true,
						InstanceLoader.getCurrentLoader());
				Constructor<?> constructor = SBMLTranslation.getDeclaredConstructor(SBMLTranslationMode);
				// enumConstants[0] INTERACTIVE, enumConstants[1] NONINTERACTIVE
				Object instance = constructor.newInstance(enumConstants[1]);
				
				MainFrame.getInstance().setActiveSession(gif.getSession(), gif.getView());
				
				// runs as algorithm to get the current graph
				GravistoService.getInstance().runAlgorithm((Algorithm) instance, null);
				GraphHelper.issueCompleteRedrawForActiveView();
				
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(null,
						"Could not find SBGN-ED Add-on. Please make sure that it is installed before using this function.");
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "There are currently no subsystems shown in the subsystems view (right side).");
		}
		
	}
	
	/**
	 * Resets the whole session.
	 */
	private void resetSession() {
		currentSession = new LMMESession();
		this.tab.updateGUI();
	}
	
	/**
	 * Partially resets the session.
	 * <p>
	 * The session is reseted, except that the selected model is kept and a new, clean {@link BaseGraph} is constructed from the latter.
	 */
	public void partiallyResetSession() {
		Graph originalGraph = currentSession.getBaseGraph().getOriginalGraph();
		currentSession = new LMMESession(new BaseGraph(originalGraph));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tab.updateGUI();
			}
		});
	}
}
