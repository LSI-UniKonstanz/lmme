package org.vanted.addons.mme.core;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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
import org.vanted.addons.mme.analysis.OverRepresentationAnalysis;
import org.vanted.addons.mme.decomposition.CompartmentMMDecomposition;
import org.vanted.addons.mme.decomposition.GirvanMMDecomposition;
import org.vanted.addons.mme.decomposition.MMDecomposition;
import org.vanted.addons.mme.decomposition.MMDecompositionAlgorithm;
import org.vanted.addons.mme.decomposition.KeggMMDecomposition;
import org.vanted.addons.mme.decomposition.PredefinedMMDecomposition;
import org.vanted.addons.mme.decomposition.SchusterMMDecomposition;
import org.vanted.addons.mme.graphs.BaseGraph;
import org.vanted.addons.mme.graphs.OverviewGraph;
import org.vanted.addons.mme.graphs.SubsystemGraph;
import org.vanted.addons.mme.layout.CircularMMLayout;
import org.vanted.addons.mme.layout.ConcentricCirclesMMLayout;
import org.vanted.addons.mme.layout.ForceDirectedMMLayout;
import org.vanted.addons.mme.layout.MMOverviewLayout;
import org.vanted.addons.mme.layout.MMSubsystemLayout;
import org.vanted.addons.mme.layout.ParallelLinesMMLayout;
import org.vanted.addons.mme.ui.MMETab;
import org.vanted.addons.mme.ui.MMEViewManagement;
import org.vanted.addons.mme.ui.MMESubsystemViewManagement;

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
public class MMEController {

	private static MMEController instance;

	private MMESession currentSession;

	private MMETab tab;

	private HashMap<String, MMDecompositionAlgorithm> decompositionAlgorithmsMap = new HashMap<>();
	private HashMap<String, MMOverviewLayout> overviewLayoutsMap = new HashMap<>();
	private HashMap<String, MMSubsystemLayout> subsystemLayoutsMap = new HashMap<>();

	private MMEController() {

		currentSession = new MMESession();

		PredefinedMMDecomposition predefDecomp = new PredefinedMMDecomposition();
		KeggMMDecomposition keggDecomp = new KeggMMDecomposition();
		SchusterMMDecomposition schusterDecomp = new SchusterMMDecomposition();
		CompartmentMMDecomposition compartmentDecomp = new CompartmentMMDecomposition();
//		GirvanMMDecomposition girvanDecomp = new GirvanMMDecomposition();

		decompositionAlgorithmsMap.put(predefDecomp.getName(), predefDecomp);
		decompositionAlgorithmsMap.put(keggDecomp.getName(), keggDecomp);
		decompositionAlgorithmsMap.put(schusterDecomp.getName(), schusterDecomp);
		decompositionAlgorithmsMap.put(compartmentDecomp.getName(), compartmentDecomp);
//		decompositionAlgorithmsMap.put(girvanDecomp.getName(), girvanDecomp);

		ForceDirectedMMLayout forceLayout = new ForceDirectedMMLayout();
		ConcentricCirclesMMLayout concentricCircLayout = new ConcentricCirclesMMLayout();
		ParallelLinesMMLayout parallelLinesLayout = new ParallelLinesMMLayout();
		CircularMMLayout circularLayout = new CircularMMLayout();

		overviewLayoutsMap.put(forceLayout.getName(), forceLayout);
		overviewLayoutsMap.put(circularLayout.getName(), circularLayout);

		subsystemLayoutsMap.put(forceLayout.getName(), forceLayout);
		subsystemLayoutsMap.put(concentricCircLayout.getName(), concentricCircLayout);
		subsystemLayoutsMap.put(parallelLinesLayout.getName(), parallelLinesLayout);

	}

	public static synchronized MMEController getInstance() {
		if (MMEController.instance == null) {
			MMEController.instance = new MMEController();
		}
		return MMEController.instance;
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

	public MMESession getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(MMESession currentSession) {
		this.currentSession = currentSession;
	}

	public MMETab getTab() {
		return tab;
	}

	public void setTab(MMETab tab) {
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
				MMEViewManagement.getInstance().closeFrames();
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
			MMESubsystemViewManagement.getInstance().resetLists();
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
								+ "where the base graph is kept while the remaining data from the current session is deleted. Do you want to continue?</html>",
						"Warning: Overview graph already constructed", JOptionPane.YES_NO_OPTION);
				if ((option == 1) || (option == -1)) {
					return;
				}
				MMEViewManagement.getInstance().closeFrames();
				partiallyResetSession();
			}

			Thread decompositionThread = new Thread(new Runnable() {
				public void run() {
					MMDecomposition decomposition = decompositionAlgorithmsMap.get(tab.getDecompositionMethod())
							.run(tab.getAddTransporterSubS());
					currentSession.setOverviewGraph(new OverviewGraph(decomposition));
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							MMEViewManagement.getInstance()
									.showAsOverviewGraph(currentSession.getOverviewGraph().getGraph());
							overviewLayoutsMap.get(tab.getOverviewLayoutMethod()).layOutAsOverview(
									MMEViewManagement.getInstance().getOverviewFrame().getView().getGraph());
							MMESubsystemViewManagement.getInstance().resetLists();
							tab.setLblNumberOfSubsystems(decomposition.getSubsystems().size());
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

	public void showSubsystemGraphsAction() {
		ArrayList<SubsystemGraph> selectedSubsystems = this.currentSession.getOverviewGraph().getSelectedSubsystems();
		if (!selectedSubsystems.isEmpty()) {
			MMESubsystemViewManagement.getInstance().showSubsystems(selectedSubsystems,
					this.tab.getClearSubsystemView(), this.tab.getCkbUseColorMapping());
			this.subsystemLayoutsMap.get(this.tab.getSubsystemLayoutMethod())
					.layOutAsSubsystems(MMEViewManagement.getInstance().getSubsystemFrame().getView().getGraph());
		} else {
			JOptionPane.showMessageDialog(null, "There are no subsystems selected in the overview graph.");
			return;
		}

	}

	public void oraAction(String pathToDifferentiallyExpressedFile, String pathToReferenceFile) throws IOException {

		if (this.currentSession.isOverviewGraphConstructed()) {
			OverRepresentationAnalysis ora = new OverRepresentationAnalysis(pathToDifferentiallyExpressedFile,
					pathToReferenceFile);
			HashSet<SubsystemGraph> significantSubsystems = ora.getSignificantSubsystems();
			MMESubsystemViewManagement.getInstance().resetOverviewGraphColoring();
			OverviewGraph og = getCurrentSession().getOverviewGraph();
			for (SubsystemGraph subsystem : significantSubsystems) {
				AttributeHelper.setFillColor(og.getNodeOfSubsystem(subsystem), Color.RED);
			}
		} else {
			JOptionPane.showMessageDialog(null, "There was no overview graph constructed so far.");
		}
	}

	/**
	 * This method translates the model to SBGN if the SBGN-ED addon is available.
	 * TODO: move translation to background thread, runs currently in main thread
	 * TODO: provide proper error message to user if method is not available because
	 * SBGN-ED addon is not available OR deactivate button TODO: currently the
	 * original graph is translated to SBGN, maybe translate the copy?
	 * 
	 */
	public void transformToSbgnAction() {

		try {
			Class<?> SBMLTranslationMode = Class.forName("org.sbgned.translation.SBMLTranslationMode", true,
					InstanceLoader.getCurrentLoader());
			Object[] enumConstants = SBMLTranslationMode.getEnumConstants();
			Class<?> SBMLTranslation = Class.forName("org.sbgned.translation.SBMLTranslation", true,
					InstanceLoader.getCurrentLoader());
			Constructor<?> constructor = SBMLTranslation.getDeclaredConstructor(SBMLTranslationMode);
			// enumConstants[0] INTERACTIVE, enumConstants[1] NONINTERACTIVE
			Object instance = constructor.newInstance(enumConstants[1]);

			GraffitiInternalFrame gif = MMEViewManagement.getInstance().getSubsystemFrame();
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

	}

	private void resetSession() {
		currentSession = new MMESession();
		this.tab.updateGUI();
	}

	public void partiallyResetSession() {
		Graph originalGraph = currentSession.getBaseGraph().getOriginalGraph();
		currentSession = new MMESession(new BaseGraph(originalGraph));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tab.updateGUI();
			}
		});
	}
}
