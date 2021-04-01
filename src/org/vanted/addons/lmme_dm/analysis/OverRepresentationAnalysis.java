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
package org.vanted.addons.lmme_dm.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.graffiti.graph.Node;
import org.vanted.addons.lmme_dm.core.LMMEController;
import org.vanted.addons.lmme_dm.graphs.BaseGraph;
import org.vanted.addons.lmme_dm.graphs.SubsystemGraph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;

/**
 * This class implements an over-representation analysis (ORA).
 * <p>
 * A list of differentially expressed metabolites is checked against a reference set of
 * metabolites. The latter may be the set of all metabolites present in the
 * model, if not specified otherwise by the user. To correct for multiple
 * testing, the false discovery rate (FDR) is controlled according to the method
 * by Benjamini and Hochberg.
 * 
 * @author Michael Aichem
 */
public class OverRepresentationAnalysis {
	
	private HashSet<Node> differentiallyExpressedMetaboliteNodes;
	private HashSet<Node> referenceMetaboliteNodes;
	
	private double significanceLevel = 0.05;
	
	private HashMap<SubsystemGraph, Double> pValueMap;
	private HashSet<SubsystemGraph> significantSubsystems;
	
	/**
	 * Constructs an instance of an over-representation analysis.
	 * <p>
	 * During construction, the lists
	 * {@link #differentiallyExpressedMetaboliteNodes} and
	 * {@link #referenceMetaboliteNodes} are initialised
	 * based on the files contents.
	 * The second parameter may be null. Then, the set of all metabolites present in the underlying model will serve as reference set.
	 * 
	 * @param pathToDifferentiallyExpressedFile
	 *           the path to the differentially expressed metabolites file in the local file system
	 * @param pathToReferenceFile
	 *           the path to the reference metabolites file in the local file system, may be null
	 * @throws IOException
	 */
	public OverRepresentationAnalysis(String pathToDifferentiallyExpressedFile, String pathToReferenceFile)
			throws IOException {
		
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		SBMLSpeciesHelper speciesHelper = new SBMLSpeciesHelper(baseGraph.getOriginalGraph());
		
		differentiallyExpressedMetaboliteNodes = new HashSet<Node>();
		referenceMetaboliteNodes = new HashSet<Node>();
		
		FileReader differentiallyFileReader = new FileReader(pathToDifferentiallyExpressedFile);
		BufferedReader differentiallyBufferedReader = new BufferedReader(differentiallyFileReader);
		
		HashSet<String> differentiallyExpressedMetaboliteStrings = new HashSet<String>();
		
		String line = differentiallyBufferedReader.readLine();
		while (line != null) {
			differentiallyExpressedMetaboliteStrings.add(line);
			line = differentiallyBufferedReader.readLine();
		}
		
		HashSet<String> referenceMetaboliteStrings = new HashSet<String>();
		if (pathToReferenceFile != null) {
			FileReader referenceFileReader = new FileReader(pathToReferenceFile);
			BufferedReader referenceBufferedReader = new BufferedReader(referenceFileReader);
			line = referenceBufferedReader.readLine();
			while (line != null) {
				referenceMetaboliteStrings.add(line);
				line = referenceBufferedReader.readLine();
			}
		}
		
		for (Node speciesNode : baseGraph.getOriginalSpeciesNodes()) {
			if (differentiallyExpressedMetaboliteStrings.contains(speciesHelper.getID(speciesNode))) {
				differentiallyExpressedMetaboliteNodes.add(speciesNode);
				referenceMetaboliteNodes.add(speciesNode);
			}
			if (referenceMetaboliteStrings.isEmpty()
					|| referenceMetaboliteStrings.contains(speciesHelper.getID(speciesNode))) {
				referenceMetaboliteNodes.add(speciesNode);
			}
		}
	}
	
	/**
	 * Calculates the p-values for the subsystems of the current decomposition
	 * assuming a hypergeometric distribution (Fishers Exact Test, one-tailed).
	 * <p>
	 * During this, the list {@link #pValueMap} is
	 * initialised.
	 */
	private void calculatePValues() {
		
		pValueMap = new HashMap<SubsystemGraph, Double>();
		
		ArrayList<SubsystemGraph> subsystems = LMMEController.getInstance().getCurrentSession().getOverviewGraph()
				.getDecomposition().getSubsystems();
		BaseGraph baseGraph = LMMEController.getInstance().getCurrentSession().getBaseGraph();
		
		int referenceNumber = referenceMetaboliteNodes.size();
		int differentiallyExpressedNumber = differentiallyExpressedMetaboliteNodes.size();
		
		HypergeometricDistribution hgdist;
		
		int differentiallyExpressedInSubsystem;
		int referenceInSubsystem;
		HashSet<Node> differentialTemp = new HashSet<Node>();
		HashSet<Node> referenceTemp = new HashSet<Node>();
		
		for (SubsystemGraph subsystem : subsystems) {
			differentialTemp.clear();
			referenceTemp.clear();
			for (Node subsystemMetabolite : subsystem.getSpeciesNodes()) {
				
				Node originalMetaboliteNode = baseGraph.getOriginalNode(subsystemMetabolite);
				
				if (differentiallyExpressedMetaboliteNodes.contains(originalMetaboliteNode)) {
					differentialTemp.add(originalMetaboliteNode);
					referenceTemp.add(originalMetaboliteNode);
				} else if (referenceMetaboliteNodes.contains(originalMetaboliteNode)) {
					referenceTemp.add(originalMetaboliteNode);
				}
				
			}
			differentiallyExpressedInSubsystem = differentialTemp.size();
			referenceInSubsystem = referenceTemp.size();
			hgdist = new HypergeometricDistribution(referenceNumber, differentiallyExpressedNumber, referenceInSubsystem);
			
			double cumulativeProbability = hgdist.upperCumulativeProbability(differentiallyExpressedInSubsystem);
			
//			System.out.println("Subsystem " + subsystem.getName() + " has diff: " + differentiallyExpressedInSubsystem
//					+ " and ref: " + referenceInSubsystem + " and p= " + cumulativeProbability);
//			System.out.println(subsystem.getName() + ";" + cumulativeProbability);
			
			pValueMap.put(subsystem, Double.valueOf(cumulativeProbability));
		}
		
	}
	
	/**
	 * Calculates the critical values for the FDR correction procedure introduced by
	 * Benjamini and Hochberg and decides which subsystems are significantly
	 * differentially expressed.
	 * <p>
	 * During this, the list {@link #significantSubsystems} is initialised.
	 * Citation: Benjamini, Y., Hochberg, Y. (1995). Controlling the false
	 * discovery rate: a practical and powerful approach to multiple testing.
	 * Journal of the Royal statistical society: series B (Methodological), 57(1),
	 * 289-300.
	 */
	private void doFDRCorrection() {
		
		this.significantSubsystems = new HashSet<SubsystemGraph>();
		
		double counter = 1.0;
		double numberOfSubsystems = (double) pValueMap.keySet().size();
		boolean conditionStillSatisfied = true;
		
		while (conditionStillSatisfied) {
			SubsystemGraph currentMinSubsystem = pValueMap.keySet().iterator().next();
			double currentMinP = pValueMap.get(currentMinSubsystem);
			
			for (SubsystemGraph subsystem : pValueMap.keySet()) {
				if (pValueMap.get(subsystem) < currentMinP) {
					currentMinSubsystem = subsystem;
					currentMinP = pValueMap.get(subsystem);
				}
			}
			
			if (currentMinP <= ((counter / numberOfSubsystems) * significanceLevel)) {
				significantSubsystems.add(currentMinSubsystem);
				counter += 1.0;
				pValueMap.remove(currentMinSubsystem);
			} else {
				conditionStillSatisfied = false;
			}
			
		}
	}
	
	/**
	 * Performs the entire ORA and the significantly differentially expressed subsystems are returned.
	 * 
	 * @return a {@code HashSet} that contains the subsystems that have been found
	 *         significantly differentially expressed
	 */
	public HashSet<SubsystemGraph> getSignificantSubsystems() {
		calculatePValues();
		doFDRCorrection();
		return this.significantSubsystems;
	}
	
}
