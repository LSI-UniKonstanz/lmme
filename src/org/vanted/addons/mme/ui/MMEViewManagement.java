package org.vanted.addons.mme.ui;

import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.vanted.addons.mme.graphs.OverviewGraph;

/**
 * This class manages the horizontally splitted views.
 * 
 * @author Michael Aichem
 *
 */
public class MMEViewManagement {
	
	private static MMEViewManagement instance;

	private GraffitiInternalFrame overviewFrame;
	private GraffitiInternalFrame subsystemFrame;
	
	private MMEViewManagement() {
		
	}
	
	public static synchronized MMEViewManagement getInstance() {
		if (MMEViewManagement.instance == null) {
			MMEViewManagement.instance = new MMEViewManagement();
		}
		return MMEViewManagement.instance;
	}
	
	private void reArrangeFrames() {
		
		int width = MainFrame.getInstance().getDesktop().getWidth();
		int height = MainFrame.getInstance().getDesktop().getHeight();
		
		int halfwidth = (int) Math.round(width / 2.0);
		
		if (overviewFrame != null) {
			overviewFrame.setBounds(0, 0, halfwidth, height);
		}
		
		if (subsystemFrame != null) {
			subsystemFrame.setBounds(width - halfwidth, 0, width - halfwidth, height);
		}
	}
	
	public void showAsOverviewGraph(Graph graph) {
		ensureClosed(overviewFrame);
		ensureClosed(subsystemFrame);
		overviewFrame = show(graph);
		reArrangeFrames();
	}
	
	public void showAsSubsystemGraph(Graph graph) {
		ensureClosed(subsystemFrame);
		subsystemFrame = show(graph);
		reArrangeFrames();
	}
	
	private void ensureClosed(GraffitiInternalFrame frame) {
		if (frame != null) {
			if (!frame.isClosed()) {
				try {
					frame.getView().getGraph().setModified(false);
					frame.setClosed(true);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private GraffitiInternalFrame show(Graph graph) {
		GraffitiInternalFrame res = null;
		MainFrame.getInstance().showGraph(graph, null, LoadSetting.VIEW_CHOOSER_NEVER);
		JInternalFrame[] internalFrames = MainFrame.getInstance().getDesktop().getAllFrames();
		for (JInternalFrame frame : internalFrames) {
			if (frame instanceof GraffitiInternalFrame) {
				GraffitiInternalFrame graffitiFrame = (GraffitiInternalFrame) frame;
				try {
					graffitiFrame.setMaximum(false);
					graffitiFrame.setIcon(false);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
				if (graffitiFrame.getView().getGraph() == graph) {
					res = graffitiFrame;
				}
			}
		}
		return res;
	}
	
	/**
	 * @return the overviewFrame
	 */
	public GraffitiInternalFrame getOverviewFrame() {
		return overviewFrame;
	}
	
	/**
	 * @return the subsystemFrame
	 */
	public GraffitiInternalFrame getSubsystemFrame() {
		return subsystemFrame;
	}

}
