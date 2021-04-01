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
package org.vanted.addons.lmme_dm.ui;

import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.vanted.addons.lmme_dm.core.LMMEController;

/**
 * Manages the horizontally splitted views.
 * 
 * @author Michael Aichem
 * @author Tobias Czauderna
 */
public class LMMEViewManagement {
	
	private static LMMEViewManagement instance;
	
	private GraffitiInternalFrame overviewFrame;
	private GraffitiInternalFrame subsystemFrame;
	
	private LMMEViewManagement() {
		
	}
	
	public static synchronized LMMEViewManagement getInstance() {
		if (LMMEViewManagement.instance == null) {
			LMMEViewManagement.instance = new LMMEViewManagement();
		}
		return LMMEViewManagement.instance;
	}
	
	/**
	 * Updates the frame sizes to ensure both frames take exactly half of the horizontal space that is available.
	 */
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
	
	/**
	 * Shows the given graph in the overview graph view (left side).
	 * 
	 * @param graph
	 *           the graph to be shown
	 */
	public void showAsOverviewGraph(Graph graph) {
		ensureClosed(overviewFrame);
		ensureClosed(subsystemFrame);
		overviewFrame = show(graph);
		overviewFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				ensureClosed(subsystemFrame);
				LMMEController.getInstance().partiallyResetSession();
			}
		});
		reArrangeFrames();
		LMMEController.getInstance().getCurrentSession().getOverviewGraph().registerSelectionListener();
	}
	
	/**
	 * Shows the given graph in the subsystems view (right side).
	 * 
	 * @param graph
	 *           the graph to be shown
	 */
	public void showAsSubsystemGraph(Graph graph) {
		ensureClosed(subsystemFrame);
		subsystemFrame = show(graph);
		subsystemFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				LMMEController.getInstance().getTab().resetSubsystemInfo();
				LMMESubsystemViewManagement.getInstance().resetOverviewGraphColoring();
//				MMESubsystemViewManagement.getInstance().resetLists();
			}
		});
		reArrangeFrames();
	}
	
	/**
	 * Makes sure that the given frame has been closed.
	 * 
	 * @param frame
	 *           the frame to be closed
	 */
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
	
	/**
	 * Creates a {@code GraffitiInternalFrame} showing the given graph.
	 * 
	 * @param graph
	 *           the graph to be shown
	 * @return a {@code GraffitiInternalFrame} showing the given graph
	 */
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
	
	/**
	 * Makes sure that the overview graph view is the active session of VANTED.
	 */
	public void ensureOverviewActive() {
		MainFrame.getInstance().setActiveSession(overviewFrame.getSession(), overviewFrame.getView());
	}
	
	/**
	 * Makes sure that the subsystem view is the active session of VANTED.
	 */
	public void ensureSubsystemViewActive() {
		MainFrame.getInstance().setActiveSession(subsystemFrame.getSession(), subsystemFrame.getView());
	}
	
	/**
	 * Closes both frames.
	 */
	public void closeFrames() {
		ensureClosed(overviewFrame);
		ensureClosed(subsystemFrame);
	}
	
}
