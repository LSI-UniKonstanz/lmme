package org.vanted.addons.gsmmexplorer.core;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.DefaultStyledDocument;
import javax.wsdl.OperationType;

import org.AttributeHelper;
import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.util.InstanceLoader;
import org.sbml.jsbml.util.SBMLtools;
import org.vanted.addons.gsmmexplorer.decomposition.GirvanGsmmDecomposition;
import org.vanted.addons.gsmmexplorer.decomposition.GsmmDecomposition;
import org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm;
import org.vanted.addons.gsmmexplorer.decomposition.KeggGsmmDecomposition;
import org.vanted.addons.gsmmexplorer.decomposition.PredefinedGsmmDecomposition;
import org.vanted.addons.gsmmexplorer.decomposition.SchusterGsmmDecomposition;
import org.vanted.addons.gsmmexplorer.graphs.BaseGraph;
import org.vanted.addons.gsmmexplorer.graphs.OverviewGraph;
import org.vanted.addons.gsmmexplorer.graphs.SubsystemGraph;
import org.vanted.addons.gsmmexplorer.layout.CircularGsmmLayout;
import org.vanted.addons.gsmmexplorer.layout.ConcentricCirclesGsmmLayout;
import org.vanted.addons.gsmmexplorer.layout.ForceDirectedGsmmLayout;
import org.vanted.addons.gsmmexplorer.layout.GsmmOverviewLayout;
import org.vanted.addons.gsmmexplorer.layout.GsmmSubsystemLayout;
import org.vanted.addons.gsmmexplorer.layout.ParallelLinesGsmmLayout;
import org.vanted.addons.gsmmexplorer.ui.GsmmExplorerTab;
import org.vanted.addons.gsmmexplorer.ui.GsmmExplorerViewManagement;
import org.vanted.addons.gsmmexplorer.ui.SubsystemViewManagement;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * This class controls the actions of the GSMM exploration addon.
 * 
 * @author Michael Aichem
 *
 */
public class GsmmExplorerController {

	private static GsmmExplorerController instance;

	private GsmmExplorerSession currentSession;

	private GsmmExplorerTab tab;

	private HashMap<String, GsmmDecompositionAlgorithm> decompositionAlgorithmsMap = new HashMap<>();
	private HashMap<String, GsmmOverviewLayout> overviewLayoutsMap = new HashMap<>();
	private HashMap<String, GsmmSubsystemLayout> subsystemLayoutsMap = new HashMap<>();

	private GsmmExplorerController() {

		currentSession = new GsmmExplorerSession();

		PredefinedGsmmDecomposition predefDecomp = new PredefinedGsmmDecomposition();
		KeggGsmmDecomposition keggDecomp = new KeggGsmmDecomposition();
		SchusterGsmmDecomposition schusterDecomp = new SchusterGsmmDecomposition();
		GirvanGsmmDecomposition girvanDecomp = new GirvanGsmmDecomposition();

		decompositionAlgorithmsMap.put(predefDecomp.getName(), predefDecomp);
		decompositionAlgorithmsMap.put(keggDecomp.getName(), keggDecomp);
		decompositionAlgorithmsMap.put(schusterDecomp.getName(), schusterDecomp);
//		decompositionAlgorithmsMap.put(girvanDecomp.getName(), girvanDecomp);

		ForceDirectedGsmmLayout forceLayout = new ForceDirectedGsmmLayout();
		ConcentricCirclesGsmmLayout concentricCircLayout = new ConcentricCirclesGsmmLayout();
		ParallelLinesGsmmLayout parallelLinesLayout = new ParallelLinesGsmmLayout();
		CircularGsmmLayout circularLayout = new CircularGsmmLayout();

		overviewLayoutsMap.put(forceLayout.getName(), forceLayout);
		overviewLayoutsMap.put(circularLayout.getName(), circularLayout);

		subsystemLayoutsMap.put(forceLayout.getName(), forceLayout);
		subsystemLayoutsMap.put(concentricCircLayout.getName(), concentricCircLayout);
		subsystemLayoutsMap.put(parallelLinesLayout.getName(), parallelLinesLayout);

	}

	public static synchronized GsmmExplorerController getInstance() {
		if (GsmmExplorerController.instance == null) {
			GsmmExplorerController.instance = new GsmmExplorerController();
		}
		return GsmmExplorerController.instance;
	}

	public HashMap<String, GsmmDecompositionAlgorithm> getDecompositionAlgorithmsMap() {
		return decompositionAlgorithmsMap;
	}

	public HashMap<String, GsmmOverviewLayout> getOverviewLayoutsMap() {
		return overviewLayoutsMap;
	}

	public HashMap<String, GsmmSubsystemLayout> getSubsystemLayoutsMap() {
		return subsystemLayoutsMap;
	}

	public GsmmExplorerSession getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(GsmmExplorerSession currentSession) {
		this.currentSession = currentSession;
	}

	public GsmmExplorerTab getTab() {
		return tab;
	}

	public void setTab(GsmmExplorerTab tab) {
		this.tab = tab;
	}

	public void setModelAction() {
		if (this.currentSession.isModelSet()) {
			// 0=Yes, 1=No, -1=window closed
			int option = JOptionPane.showConfirmDialog(null,
					"<html>The base graph has already been set. Re-setting it will start a new session <br>"
							+ "and delete the data from the current session. Do you want to continue?</html>",
					"Warning: Model already set", JOptionPane.YES_NO_OPTION);
			if (option == 0) {
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
			SubsystemViewManagement.getInstance().resetLists();
		} else {
			JOptionPane.showMessageDialog(null, "There is no active model.");
			return;
		}
	}

	public void showOverviewGraphAction() {
		if (this.currentSession.isModelSet()) {
			if (this.currentSession.isOverviewGraphConstructed()) {
				// 0=Yes, 1=No, -1=window closed
				int option = JOptionPane.showConfirmDialog(null,
						"<html>The overview graph has already been constructed. Constructing a new one will start a new session <br>"
								+ "where the base graph is kept wile the remaining data from the current session is deleted. Do you want to continue?</html>",
						"Warning: Overview graph already constructed", JOptionPane.YES_NO_OPTION);
				if ((option == 1) || (option == -1)) {
					return;
				}
				partiallyResetSession();
			}
			GsmmDecomposition decomposition = this.decompositionAlgorithmsMap.get(this.tab.getDecompositionMethod())
					.run(this.tab.getAddTransporterSubS(), this.tab.getAddDefaultSubS(), this.tab.getSplitDefaultSubS(),
							this.tab.getSplitDefaultSubSThreshold());
			this.currentSession.setOverviewGraph(new OverviewGraph(decomposition));
			GsmmExplorerViewManagement.getInstance()
					.showAsOverviewGraph(this.currentSession.getOverviewGraph().getGraph());
			this.overviewLayoutsMap.get(this.tab.getOverviewLayoutMethod())
					.layOut(GsmmExplorerViewManagement.getInstance().getOverviewFrame().getView().getGraph());
			SubsystemViewManagement.getInstance().resetLists();
			this.tab.setLblNumberOfSubsystems(decomposition.getSubsystems().size());
		} else {
			JOptionPane.showMessageDialog(null, "No base graph was set.");
			return;
		}

	}

	public void showSubsystemGraphsAction() {
		ArrayList<SubsystemGraph> selectedSubsystems = this.currentSession.getOverviewGraph().getSelectedSubsystems();
		SubsystemViewManagement.getInstance().showSubsystems(selectedSubsystems, this.tab.getClearSubsystemView(),
				this.tab.getCkbUseColorMapping());

		this.subsystemLayoutsMap.get(this.tab.getSubsystemLayoutMethod())
				.layOut(GsmmExplorerViewManagement.getInstance().getSubsystemFrame().getView().getGraph());
	}

	/**
	 * This method translates the model to SBGN if the SBGN-ED addon is available.
	 * TODO: move translation to background thread, runs currently in main thread
	 * TODO: provide proper error message to user if method is not available because SBGN-ED addon is not available OR deactivate button
	 * TODO: currently the original graph is translated to SBGN, maybe translate the copy?
	 * 
	 * @return An ActionListener that is put on the translate to SBGN button in the extension tab.
	 */
	public void transformToSbgnAction() {

		try {
			Class<?> SBMLTranslationMode = Class.forName("org.sbgned.translation.SBMLTranslationMode", true, InstanceLoader.getCurrentLoader());
			Object[] enumConstants = SBMLTranslationMode.getEnumConstants();
			Class<?> SBMLTranslation = Class.forName("org.sbgned.translation.SBMLTranslation", true, InstanceLoader.getCurrentLoader());
			Constructor<?> constructor = SBMLTranslation.getDeclaredConstructor(SBMLTranslationMode);
			// enumConstants[0] INTERACTIVE, enumConstants[1] NONINTERACTIVE
			Object instance = constructor.newInstance(enumConstants[1]);
			
			GraffitiInternalFrame gif = GsmmExplorerViewManagement.getInstance().getSubsystemFrame();
			MainFrame.getInstance().setActiveSession(gif.getSession(), gif.getView());
			
			// runs as algorithm to get the current graph
			GravistoService.getInstance().runAlgorithm((Algorithm) instance, null);
			GraphHelper.issueCompleteRedrawForActiveView();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
	}

	private void resetSession() {
		currentSession = new GsmmExplorerSession();
	}

	private void partiallyResetSession() {
		Graph originalGraph = currentSession.getBaseGraph().getOriginalGraph();
		currentSession = new GsmmExplorerSession(new BaseGraph(originalGraph));
	}
}
