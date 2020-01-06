package org.vanted.addons.gsmmexplorer.decomposition;

import java.util.ArrayList;

import javax.swing.JLabel;

import org.FolderPanel;
import org.vanted.addons.gsmmexplorer.graphs.SubsystemGraph;

public class GirvanGsmmDecomposition extends GsmmDecompositionAlgorithm {

	@Override
	protected ArrayList<SubsystemGraph> runSpecific() {
		return null;
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm#requiresCloning()
	 */
	@Override
	public boolean requiresCloning() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm#getFolderPanel()
	 */
	@Override
	public FolderPanel getFolderPanel() {
		FolderPanel fp = new FolderPanel(getName(), false, true, false, null);
		fp.addGuiComponentRow(new JLabel("hi"),  null,  true);
		return fp;
	}

	/* (non-Javadoc)
	 * @see org.vanted.addons.gsmmexplorer.decomposition.GsmmDecompositionAlgorithm#getName()
	 */
	@Override
	public String getName() {
		return "Girvan et al.";
	}

}
