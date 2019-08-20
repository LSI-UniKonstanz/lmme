package org.vanted.addons.wholecell;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.AttributeHelper;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionModel;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.SplitNodeForSingleMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterWithMinimumCrossingsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

@SuppressWarnings("nls")

/**
 * This is the main class of the Wholecell exploration Add-On. This Add-On
 * allows to decompose a huge metabolic network into smaller modules. The
 * decomposition is not only performed in the background but the system produces
 * an overview network that consists of individual subsystems. A subsystem
 * formally contains a (most often) connected subgraph of the original graph.
 * The user can choose several different approaches to decompose the initial
 * network, e.g. based on annotations in the underlying SBML file (either direct
 * subsystem annotations - or KEGG reaction id annotations that can then be
 * resolved to individual pathways via KEGG API requests). Afterwards there are
 * heuristic approaches that decompose the remaining, yet unclassified reactions
 * in the network such that finally there is an overview graph and several
 * subsystem graphs. The subsystem graphs may then be visualised on demand by
 * selecting it in the overview graph.
 * 
 * @author Michael Aichem, Tobias Czauderna
 * 
 */
public class WholecellTools {

	/**
	 * Stating whether the initialisation tasks (grabbing SBML model data,
	 * constructing initial network) have been run so far.
	 */
	private static boolean initialised = false;

	/**
	 * Stating whether the initialisation tasks (grabbing SBML model data,
	 * constructing initial network) have been run so far.
	 * 
	 * @return a boolean stating whether the initialisation tasks have been run
	 */
	public static boolean isInitialised() {
		return initialised;
	}

	/**
	 * Reference to the UI tab in the system in order to manipulate the displayed
	 * content.
	 */
	private static WholecellTab myTab;

	/**
	 * Sets the corresponding UI tab.
	 * 
	 * @param myTab
	 *            The tab to be set
	 */
	public static void setMyTab(WholecellTab myTab) {
		WholecellTools.myTab = myTab;
	}

	/**
	 * This is a list of species that may be cloned as they are not at all specific
	 * to a certain model. But this may cause problems due to their hard-coded
	 * nature.
	 */
	private static ArrayList<String> clonableSpecies = new ArrayList<>();

	/**
	 * Returns the list of clonable species.
	 * 
	 * @return the list of clonable species
	 */
	public static ArrayList<String> getClonableSpecies() {
		return clonableSpecies;
	}

	/**
	 * The degree threshold. Any (clonable) species with degree greater or equal to
	 * this threshold will be replaced by clones in the respective step.
	 */
	private static int clonableSpeciesThreshold = 8;

	/**
	 * Sets the clonable species threshold.
	 * 
	 * @param clonableSpeciesThreshold
	 *            the threshold to be set for clonable species
	 */
	public static void setClonableSpeciesThreshold(int clonableSpeciesThreshold) {
		WholecellTools.clonableSpeciesThreshold = clonableSpeciesThreshold;
	}

	/**
	 * ?
	 */
	private static String extracellularCompartmentID = "e";

	/**
	 * When we use KEGG annotations to derive subsystems, there are reactions that
	 * have assigned more than one KEGG pathway. Thus, we need to decide which ones
	 * to keep. One criterion while doing so, is to require a subsystem to consist
	 * out of a minimum number of reactions to be viable.
	 */
	private static int minimumReactionsPerKeggSubsystem = 5;

	/**
	 * These are the global and overview pathway maps from KEGG. They are not
	 * considered to be subsystems in our system, as they do not refer to actual
	 * functional subunits. Thus we ignore them in the query result.
	 */
	private static List<String> inegligibleKEGGPathways = new ArrayList<>(
			Arrays.asList(new String[] { "Metabolic pathways", "Biosynthesis of secondary metabolites",
					"Microbial metabolism in diverse environments", "Biosynthesis of antibiotics", "Carbon metabolism",
					"2-Oxocarboxylic acid metabolism", "Fatty acid metabolism", "Degradation of aromatic compounds",
					"Biosynthesis of amino acids" }));

	/**
	 * The initial huge graph to be decomposed / explored.
	 */
	private static Graph initialGraph;

	/**
	 * Shortcut to the species from the inital graph.
	 */
	private static ArrayList<Node> speciesFromInitialGraph = new ArrayList<>();

	/**
	 * Shortcut to the reactions from the inital graph.
	 */
	private static ArrayList<Node> reactionsFromInitialGraph = new ArrayList<>();

	/**
	 * Shortcut to those reactions from the inital graph that have a KEGG subsystem
	 * annotation.
	 */
	private static ArrayList<Node> reactionsWithKeggSubsystem = new ArrayList<>();

	/**
	 * Shortcut to the subsystem nodes in the overview graph.
	 */
	private static ArrayList<Node> subsystemNodes = new ArrayList<>();

	/**
	 * A HashMap that maps a degree to the number of species in the initial graph
	 * that have this degree.
	 */
	private static int[] degreeSpecies;

	/**
	 * Shortcut to those species in the original graph that have been identified to
	 * be interfaces between two (or more) subsystems. Besides the subsystem nodes,
	 * they will also be part of the overview graph.
	 */
	private static ArrayList<Node> interfaceMetabolites = new ArrayList<>();

	/**
	 * Shortcut to the interface nodes in the overview graph.
	 */
	private static ArrayList<Node> interfaceMetabolitesInOverviewGraph = new ArrayList<>();

	/**
	 * Shortcut to the subsystem nodes in the overview graph - stored as map to
	 * ensure accessibility also via the name of a particular subsystem.
	 */
	private static Map<String, Node> subsystemNameMap = new HashMap<>();

	/**
	 * This Map is used to map a subsystem node to its actual subsystem subgraph of
	 * the original graph.
	 */
	private static Map<Node, Graph> subsystemGraphMap = new HashMap<>();

	/**
	 * A HashSet that contains the names of all yet classified subsystems.
	 */
	private static HashSet<String> subsystems = new HashSet<>();

	/**
	 * The overview graph that contains all detected subsystems and their relations.
	 */
	private static Graph overviewGraph;

	/**
	 * The rest service for the KEGG requests.
	 */
	private static RestService restService = new RestService("http://rest.kegg.jp/get/");

	/**
	 * The tag used in the SBML file to store the KEGG (reation) id.
	 */
	private static String keggTag = "KEGG";

	/**
	 * The tag used in the SBML file to store the SUBSYSTEM anme.
	 */
	private static String subsAnnTag = "SUBSSTEM";

	/**
	 * The character(sequence) that is used to separate KEGG IDs in the SBML
	 * annotations.
	 */
	private static String keggSep;

	/**
	 * The character(sequence) that is used to separate SUBSYSTEM names in the SBML
	 * annotations.
	 */
	private static String annotSep;

	/**
	 * This method initialises the whole procedure by reading the information from
	 * the SBML model that underlies the network from the currently active editor
	 * session. Moreover, it sets up some basic attributes for later usage,
	 * including {@link initialGraph}, {@link speciesFromInitialGraph} and
	 * {@link reactionsFromInitialGraph}.
	 */
	public static void initialise() {
		grabGraph();
		readNotes();
		initialised = true;
		myTab.logMsg("Finished initialisation tasks.");
	}

	/**
	 * This method executes the decomposition process. During this, it queries the
	 * UI tab to get the necessary parameters that might have been set by the user.
	 * 
	 * @return An ActionListener that is put on the execution button in the
	 *         extension tab.
	 */
	public static ActionListener execute() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				keggTag = myTab.getKeggTag();
				System.out.println("KEGG tag: " + keggTag);
				subsAnnTag = myTab.getAnnotTag();
				keggSep = myTab.getKeggSep();
				annotSep = myTab.getAnnotSep();

				reset();
				myTab.logMsg("Reset all involved values.");

				if (!myTab.isEditedCloneList()) {
					clonableSpecies = getSpeciesAboveDegThreshold(myTab.getClonableSpeciesThreshold());
				}
				cloneSpecies();

				String decompMethod = myTab.getDecompMethod();
				switch (decompMethod) {
				case WholecellConstants.DECOMP_KEGG:
					(new KeggRequestUtils()).request();
					decompKeggAnn();
					break;
				case WholecellConstants.DECOMP_SUBSANN:
					decompSubsAnn();
					break;
				default:
					break;
				}

				addTransporterSubsystem();
				addDefaultSubsystem();

				String layoutAlgorithmOverviewGraph = myTab.getLayoutAlgorithmOverviewGraph();
				// boolean useSbgnForOverviewGraph = myTab.isUseSbgnForOverviewGraph();
				(new OverviewGraphUtils()).run(layoutAlgorithmOverviewGraph);

			}
		};
	}

	/**
	 * This method opens the subgraph that corresponds to the selected subsystem.
	 * 
	 * @return An ActionListener that can be put on the respective button in the
	 *         extension tab.
	 */
	public static ActionListener openSubsystemViews() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {

				SelectionModel selectionModel = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
				showSubsystemGraphOf(selectionModel.getActiveSelection().getNodes());

			}
		};
	}

	/**
	 * This method resets all of the values that are going to be changed during the
	 * execution to defaults to ensure that repeated executions all have the same
	 * preconditions.
	 */
	private static void reset() {
		// TODO implement method.
	}

	/**
	 * Returns the number of species in the original graph that have a degree of at
	 * least {@link degree}
	 * 
	 * @param degree
	 *            The lower bound for the degree that we want the number of species
	 *            from
	 * @return The number of species in the original graph that have a degree of at
	 *         least {@link degree}
	 */
	public static int numberOfSpeciesWithDegreeAtLeast(int degree) {
		if (!(initialGraph == null)) {
			if (degreeSpecies == null) {
				degreeSpecies = new int[speciesFromInitialGraph.size()];
				for (int i = 0; i < degreeSpecies.length; i++) {
					degreeSpecies[i] = 0;
				}
				for (Node speciesNode : speciesFromInitialGraph) {
					degreeSpecies[speciesNode.getDegree()]++;
				}
				for (int i = degreeSpecies.length - 2; i >= 0; i--) {
					degreeSpecies[i] += degreeSpecies[i + 1];
				}
			}
			return degreeSpecies[degree];
		}
		return -1;
	}

	/**
	 * Returns all species as a list of Strings that have a degree of at least
	 * {@link degree}
	 * 
	 * @param degree
	 *            The lower bound for the degree that we want the species from
	 * @return All species as a list of Strings that have a degree of at least
	 *         {@link degree}
	 */
	public static ArrayList<String> getSpeciesAboveDegThreshold(int degree) {
		ArrayList<String> res = new ArrayList<>();
		if (!(initialGraph == null)) {
			for (Node speciesNode : speciesFromInitialGraph) {
				if (speciesNode.getDegree() >= degree) {
					res.add(AttributeHelper.getLabel(speciesNode, ""));
				}
			}
		}
		return res;
	}

	/**
	 * this method grabs the graph from the current active editor session and stores
	 * it into {@link initialGraph}.
	 * 
	 * Moreover, the nodes are traversed in order to instantiate
	 * {@link speciesFromInitialGraph} and {@link reactionsFromInitialGraph}.
	 */
	private static void grabGraph() {

		if (MainFrame.getInstance().getActiveEditorSession() == null) {
			return;
		}

		initialGraph = MainFrame.getInstance().getActiveEditorSession().getGraph();

		for (Node node : initialGraph.getNodes()) {
			if (isReaction(node)) {
				reactionsFromInitialGraph.add(node);
			} else if (isSpecies(node)) {
				speciesFromInitialGraph.add(node);
			}
		}
		myTab.setLabelGeneralInfoMet(String.valueOf(speciesFromInitialGraph.size()));
		myTab.setLabelGeneralInfoReac(String.valueOf(reactionsFromInitialGraph.size()));
		myTab.setLabelSliderCorrespSpeciesNumber(
				String.valueOf(numberOfSpeciesWithDegreeAtLeast(myTab.getClonableSpeciesThreshold())));

	}

	/**
	 * Reads the notes in the SBML file that underlies the graph from the current
	 * editor session. Aftwerwards, we have the SUBSYSTEM note stored in the node
	 * attribute "SUBSYSTEM_ANNOTATED" and the KEGG ids of the reactions in the node
	 * attribute "kegg_rid"
	 */
	private static void readNotes() {

		SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(initialGraph);
		SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(initialGraph);

		for (Node reactionNode : reactionsFromInitialGraph) {
			if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.REACTION_NOTES)) {
				XMLNode notes = sbmlReactionHelper.getNotes(reactionNode);
				int numChildren = notes.getNumChildren();
				for (int k = 0; k < numChildren; k++) {
					XMLNode child = notes.getChild(k);
					int numGrandChildren = child.getNumChildren();
					for (int l = 0; l < numGrandChildren; l++) {
						XMLNode grandChild = child.getChild(l);
						// In this case, the actual notes are on the second level of the SBML hierarchy.
						if (grandChild.isText()) {
							readNote(reactionNode, grandChild, "GENE_ASSOCIATION", "gene_association");
							readNote(reactionNode, grandChild, keggTag, "kegg_rid");
							readNote(reactionNode, grandChild, "PROTEIN_ASSOCIATION", "protein_association");
							readNote(reactionNode, grandChild, "PROTEIN_CLASS", "protein_class");
							readNote(reactionNode, grandChild, "SOURCE", "source_model");
							readNote(reactionNode, grandChild, subsAnnTag, WholecellConstants.SUBSYSTEM + "_ANNOTATED");

							// In this case, the actual notes are on the third level of the SBML hierarchy.
						} else {
							int numGrandGrandChildren = grandChild.getNumChildren();
							for (int m = 0; m < numGrandGrandChildren; m++) {
								XMLNode grandGrandChild = grandChild.getChild(m);
								if (grandGrandChild.isText()) {
									readNote(reactionNode, grandGrandChild, "GENE_ASSOCIATION", "gene_association");
									readNote(reactionNode, grandGrandChild, keggTag, "kegg_rid");
									readNote(reactionNode, grandGrandChild, "PROTEIN_ASSOCIATION",
											"protein_association");
									readNote(reactionNode, grandGrandChild, "PROTEIN_CLASS", "protein_class");
									readNote(reactionNode, grandGrandChild, "SOURCE", "source_model");
									readNote(reactionNode, grandGrandChild, subsAnnTag,
											WholecellConstants.SUBSYSTEM + "_ANNOTATED");
								}
							}
						}
					}
				}
			}
		}
		for (Node speciesNode : speciesFromInitialGraph) {
			if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NOTES)) {
				XMLNode notes = sbmlSpeciesHelper.getNotes(speciesNode);
				int numChildren = notes.getNumChildren();
				for (int k = 0; k < numChildren; k++) {
					XMLNode child = notes.getChild(k);
					int numGrandChildren = child.getNumChildren();
					for (int l = 0; l < numGrandChildren; l++) {
						XMLNode grandChild = child.getChild(l);
						int numGrandGrandChildren = grandChild.getNumChildren();
						for (int m = 0; m < numGrandGrandChildren; m++) {
							XMLNode grandGrandChild = grandChild.getChild(m);
							readNote(speciesNode, grandGrandChild, "ABBREVIATION", "abbreviation");
							readNote(speciesNode, grandGrandChild, "KEGG", "keggcid");
							readNote(speciesNode, grandGrandChild, "KEGG Compound", "keggcid");
							readNote(speciesNode, grandGrandChild, "FORMULA", "formula_model");
							readNote(speciesNode, grandGrandChild, "SOURCE", "source_model");
						}
					}
				}
			}
		}

	}

	/**
	 * Reads the information contained in a given xml node and stores this as an
	 * attribute of a given node.
	 * 
	 * @param node
	 *            the node for which the note is to be stored as an attribute
	 * @param xmlNode
	 *            the xml node containing the information that is to be stored
	 * @param noteName
	 *            the expected name of the note contained in the xml node
	 * @param noteAttributeName
	 *            the name of the node attribute that is going to be created for the
	 *            node
	 */
	private static void readNote(Node node, XMLNode xmlNode, String noteName, String noteAttributeName) {
		if (xmlNode.isText() && xmlNode.getCharacters().trim().startsWith(noteName + ":")) {
			String note = xmlNode.getCharacters().trim();
			note = note.replace(noteName + ": ", "");
			if (!note.equals("null")) {
				AttributeHelper.setAttribute(node, WholecellConstants.NOTES, noteAttributeName, note);
			}
		}
	}

	/**
	 * This method clones the species specified in the {@link clonableSpecies} list.
	 */
	private static void cloneSpecies() {
		Selection selection = new Selection("speciesToBeCloned");
		for (Node node : speciesFromInitialGraph) {
			String label = AttributeHelper.getLabel(node, "");
			if (clonableSpecies.contains(label)) {
				selection.add(node);
			}
		}
		SelectionModel selectionModel = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
		selectionModel.setActiveSelection(selection);
		selectionModel.selectionChanged();

		// split species using split nodes algorithm
		Algorithm splitNodeForSingleMappingData = new SplitNodeForSingleMappingData();
		splitNodeForSingleMappingData.attach(initialGraph, selection);
		splitNodeForSingleMappingData.setParameters(new Parameter[] {
				new ObjectListParameter("Split nodes with a degree over the specified threshold", "", "",
						new ArrayList<String>()),
				new IntegerParameter(Integer.valueOf(clonableSpeciesThreshold), "", ""),
				new BooleanParameter(false, "", ""), new BooleanParameter(true, "", "") });
		splitNodeForSingleMappingData.execute();
	}

	/**
	 * This inner class contains the methods that are required for the decomposotion
	 * based on KEGG identifiers. This includes the querying of the KEGG REST API.
	 */
	private static class KeggRequestUtils {

		/**
		 * For each reaction, we send an HTTP request to KEGG, containing the attribute
		 * "kegg_rid", querying the pathways that belong to the reaction. These are then
		 * stored in the node attributes "SUBSYSTEM_KEGG_k" of the reaction nodes,
		 * whereas k>=0.
		 */
		private void request() {
			System.out.println("request");
			// Grab reactions with viable KEGG reaction id.
			ArrayList<Node> reactionsWithKeggRid = new ArrayList<>();
			for (Node reactionNode : reactionsFromInitialGraph) {
				String kegg_rid = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
						"kegg_rid", "", "");
				if ((kegg_rid.length() > 0) && !(kegg_rid.equals("null"))) {
					reactionsWithKeggRid.add(reactionNode);
				}
			}

			ArrayList<Node> reactionPackage;
			String[] res;
			int packageStart = 0;
			while (reactionsWithKeggRid.size() - packageStart >= 10) {
				reactionPackage = new ArrayList<>();
				for (int i = 0; i < 10; i++) {
					reactionPackage.add(reactionsWithKeggRid.get(packageStart + i));
				}
				res = requestPackage(reactionPackage);
				if (res.length == 10) {
					processPackageResults(reactionPackage, res);
				} else {
					requestAndProcessPackageSeparately(reactionPackage);
				}
				packageStart += 10;
				if ((packageStart % 100) == 0) {
					myTab.logMsg("100 reactions requested from KEGG");
				}
			}
			reactionPackage = new ArrayList<>();
			for (int i = packageStart; i < reactionsWithKeggRid.size(); i++) {
				reactionPackage.add(reactionsWithKeggRid.get(i));
			}
			if (!reactionPackage.isEmpty()) {
				res = requestPackage(reactionPackage);
				if (res.length == reactionPackage.size()) {
					processPackageResults(reactionPackage, res);
				} else {
					requestAndProcessPackageSeparately(reactionPackage);
				}
			}
		}

		/**
		 * Sending HTTP requests to KEGG for a whole package of up to 10 reactions.
		 * 
		 * @param reactionPackage
		 *            The package of reaction nodes to be requested
		 * @return a list of request result Strings
		 */
		private String[] requestPackage(ArrayList<Node> reactionPackage) {
			String urlPostFix = "";
			for (Node reactionNode : reactionPackage) {
				String kegg_rid = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
						"kegg_rid", "", "");
				// This forces the calling method to use requestAndProcessPackageSeparately
				// instead as the latter can
				// handle multiple reaction ids.
				if (kegg_rid.contains(keggSep)) {
					return new String[0];
				}
				urlPostFix += "+rn:" + kegg_rid;
			}
			urlPostFix = urlPostFix.substring(1);
			String response = (String) restService.makeRequest(urlPostFix, MediaType.TEXT_PLAIN_TYPE, String.class);
			response = response.substring(0, response.length() - 3);
			return response.split(WholecellConstants.KEGGSEP);
		}

		/**
		 * Store the results from the KEGG requests as node attributes of the respective
		 * reaction nodes. The individual pathways are then stored as node attributes
		 * "SUBSYSTEM_KEGG_k" where k>=0. The method thus therefore expects two lists: a
		 * list of reactions and a list of request result Strings, that match position
		 * by position. This method assumes both lists to have the same length.
		 * 
		 * @param reactionNodes
		 *            The nodes that have been requested
		 * @param requestResults
		 *            The results from the request
		 */
		private void processPackageResults(ArrayList<Node> reactionNodes, String[] requestResults) {
			for (int i = 0; i < reactionNodes.size(); i++) {
				Node reactionNode = reactionNodes.get(i);
				String response = requestResults[i];
				processSingleNode(reactionNode, response);
			}
		}

		/**
		 * This method also expects a list of reaction nodes but in contrast to
		 * {@link requestPackage} it requests AND processes (stores the results as
		 * attributes) the nodes one by one. This is necessary if one of the reactions
		 * did not yield request results during a previous call of
		 * {@link requestPackage}.
		 * 
		 * @param reactionPackage
		 *            A list of reaction nodes that will be requested and directly
		 */
		private void requestAndProcessPackageSeparately(ArrayList<Node> reactionPackage) {
			for (Node reactionNode : reactionPackage) {
				String kegg_rid = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
						"kegg_rid", "", "");
				ArrayList<String> severalIDs = new ArrayList<>();
				if (kegg_rid.contains(keggSep)) {
					String[] splitted = kegg_rid.split(keggSep);
					for (int i = 0; i < splitted.length; i++) {
						severalIDs.add(splitted[i].trim());
					}
				} else {
					severalIDs.add(kegg_rid);
				}

				for (String singleKeggRid : severalIDs) {
					String response = (String) restService.makeRequest("rn:" + singleKeggRid, MediaType.TEXT_PLAIN_TYPE,
							String.class);
					processSingleNode(reactionNode, response);
				}
			}
		}

		/**
		 * This method processes a single node, meaning that the given request result is
		 * nterpreted in the context of the given reaction node and the respective
		 * pathway information from the request result. The pathways are then stored as
		 * node attributes "SUBSYSTEM_KEGG_k" where k>=0.
		 * 
		 * @param reactionNode
		 * @param requestResult
		 */
		private void processSingleNode(Node reactionNode, String requestResult) {
			int subsystemIndex = 0;
			while (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_KEGG_" + subsystemIndex)) {
				subsystemIndex += 1;
			}
			if (requestResult != null) {
				String[] arrLines = requestResult.split("\n");

				int lineIndex = 0;
				while (lineIndex < arrLines.length) {
					if (arrLines[lineIndex].startsWith("PATHWAY"))
						do {
							String line = arrLines[lineIndex];
							line = line.substring(line.indexOf("rn") + 2);
							line = line.substring(line.indexOf(" ") + 2);
							if (!inegligibleKEGGPathways.contains(line)) {
								AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES,
										WholecellConstants.SUBSYSTEM + "_KEGG_" + subsystemIndex++, line);
							}
							lineIndex++;
						} while ((lineIndex < arrLines.length) && (arrLines[lineIndex].startsWith(" ")));
					else {
						lineIndex++;
					}
				}

			}
			if (subsystemIndex > 0) {
				reactionsWithKeggSubsystem.add(reactionNode);
			}
		}

	}

	/**
	 * Deriving the initial subsystems from the requested KEGG pathway information.
	 * This means that for those reactions that have set the attribute
	 * "SUBSYSTEM_KEGG_k" fore some k, we set the attribute "SUBSYSTEM_DEFINITE" to
	 * one of these values.
	 * 
	 * As we currently want a reaction to be only part of exactly one module, this
	 * method also applies some heuristics that choose a pathway for the each
	 * reaction in the case that the KEGG request retrieved more than one viable
	 * pathways for that particular reaction.
	 */
	private static void decompKeggAnn() {

		Map<Node, String> node2finalSubsystem = new KeggDecompUtils().run();

		for (Node reactionNode : node2finalSubsystem.keySet()) {
			AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_DEFINITE", node2finalSubsystem.get(reactionNode));
		}

	}

	/**
	 * This class provides some utilities that are used for the
	 * {@link decompKeggAnn} method.
	 *
	 */
	private static class KeggDecompUtils {

		private Map<Node, ArrayList<String>> node2possibleSubsystems = new HashMap<>();
		private Map<Node, String> node2finalSubsystem = new HashMap<>();
		private Map<String, Integer> subsystem2number = new HashMap<>();

		/**
		 * This method runs the overall KEGG decomposition.
		 * 
		 * @return a mapping that maps reaction nodes to a String representing their
		 *         assigned subsystem.
		 */
		public Map<Node, String> run() {

			for (Node reactionNode : reactionsWithKeggSubsystem) {
				ArrayList<String> subsystems = new ArrayList<>();
				int subsystemIndex = 0;
				while (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES,
						WholecellConstants.SUBSYSTEM + "_KEGG_" + subsystemIndex)) {
					subsystems.add((String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
							WholecellConstants.SUBSYSTEM + "_KEGG_" + subsystemIndex, "", ""));
					subsystemIndex++;
				}
				node2possibleSubsystems.put(reactionNode, subsystems);
			}

			while (!node2possibleSubsystems.isEmpty()) {
				updateSubsystemNumber();
				removeTooSmallSubsystems();
				removeNodesWithoutSubsystem();
				extractNodesFromHugestSubsystem();
			}

			System.out.println("Finished Kegg stuff.");

			return node2finalSubsystem;

		}

		/**
		 * This method counts for every subsystem the possible occurences over all
		 * reactions what directly relates to the number of reactions they would be made
		 * up of
		 */
		private void updateSubsystemNumber() {
			subsystem2number.clear();
			for (Node reactionNode : node2possibleSubsystems.keySet()) {
				for (String subsystem : node2possibleSubsystems.get(reactionNode)) {
					Integer numberOf = Integer.valueOf(1);
					if (subsystem2number.containsKey(subsystem)) {
						numberOf = Integer.valueOf(subsystem2number.get(subsystem).intValue() + 1);
					}
					subsystem2number.put(subsystem, numberOf);
				}
			}
		}

		/**
		 * This method removes subsystems that have too few occurences, meaning that
		 * they would be made up of too few reactions.
		 */
		private void removeTooSmallSubsystems() {
			ArrayList<String> subsystemsToRemove = new ArrayList<>();
			for (String smallSubsystem : subsystem2number.keySet()) {
				if (subsystem2number.get(smallSubsystem).intValue() < minimumReactionsPerKeggSubsystem) {
					subsystemsToRemove.add(smallSubsystem);
					for (ArrayList<String> list : node2possibleSubsystems.values()) {
						list.remove(smallSubsystem);
					}
				}
			}
			for (String subsystem : subsystemsToRemove) {
				subsystem2number.remove(subsystem);
			}
		}

		/**
		 * This method removes reactions that don't have possible subsystems assigned
		 * (this could happen e.g. during the {@link removeTooSmallSubsystems} method.
		 */
		private void removeNodesWithoutSubsystem() {

			ArrayList<Node> nodesToRemove = new ArrayList<>();
			for (Node reactionNode : node2possibleSubsystems.keySet()) {
				if (node2possibleSubsystems.get(reactionNode).isEmpty()) {
					nodesToRemove.add(reactionNode);
				}
			}
			for (Node nodeToRemove : nodesToRemove) {
				node2possibleSubsystems.remove(nodeToRemove);
			}

		}

		/**
		 * This emthod then takes the currently hugest subsystem that has not been
		 * definitely assigned and assigns it to the corresponding reaction nodes.
		 */
		private void extractNodesFromHugestSubsystem() {

			if (subsystem2number.isEmpty()) {
				return;
			}

			int currentMax = 0;
			String hugestSubsystem = "";
			for (String subsystem : subsystem2number.keySet()) {
				if (subsystem2number.get(subsystem).intValue() > currentMax) {
					currentMax = subsystem2number.get(subsystem).intValue();
					hugestSubsystem = subsystem;
				}
			}

			ArrayList<Node> nodesToRemove = new ArrayList<>();
			for (Node reactionNode : node2possibleSubsystems.keySet()) {
				if (node2possibleSubsystems.get(reactionNode).contains(hugestSubsystem)) {
					nodesToRemove.add(reactionNode);
					node2finalSubsystem.put(reactionNode, hugestSubsystem);
				}
			}
			myTab.logMsg("Added " + nodesToRemove.size() + " nodes to subsystem " + hugestSubsystem);

			for (Node nodeToRemove : nodesToRemove) {
				node2possibleSubsystems.remove(nodeToRemove);
			}

		}

	}

	/**
	 * Deriving the initial subsystems from the "SUBSYSTEM" annotation of the
	 * SBML-file. This means that for those reactions that have set the attribute
	 * "SUBSYSTEM_ANNOTATED", we set the attribute "SUBSYSTEM_DEFINITE" to the same
	 * value.
	 */
	private static void decompSubsAnn() {

		HashMap<String, Integer> subsystemCount = new HashMap<>();

		for (Node reactionNode : reactionsFromInitialGraph) {
			if (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_ANNOTATED")) {
				String subsystem = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
						WholecellConstants.SUBSYSTEM + "_ANNOTATED", "", "");
				AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES,
						WholecellConstants.SUBSYSTEM + "_DEFINITE", subsystem);
				if (subsystemCount.containsKey(subsystem)) {
					subsystemCount.put(subsystem, Integer.valueOf(subsystemCount.get(subsystem).intValue() + 1));
				} else {
					subsystemCount.put(subsystem, Integer.valueOf(1));
				}
			}
		}

		for (String subsystem : subsystemCount.keySet()) {
			myTab.logMsg("Added " + subsystemCount.get(subsystem) + " nodes to subsystem " + subsystem + ".");
		}

	}

	/**
	 * This method extends the derived decomposition by another subsystem that
	 * contains transporter reactions. For this purpose, for each reaction we check
	 * whether it contains exactly one reactant and exactly one product and these
	 * have different compartments. The resulting subsystem is called
	 * {@link WholecellConstants.TRANSPORTER_SUBSYSTEM}.
	 */
	private static void addTransporterSubsystem() {
		int numOfTransporters = 0;
		for (Node reactionNode : reactionsFromInitialGraph) {
			if (!AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_DEFINITE")) {
				Set<Node> neighbors = reactionNode.getNeighbors();
				Object[] neighborArray = neighbors.toArray();
				if (neighborArray.length == 2) {
					Node neighbor0 = (Node) neighborArray[0];
					Node neighbor1 = (Node) neighborArray[1];
					if (AttributeHelper.hasAttribute(neighbor0, SBML_Constants.SBML, SBML_Constants.COMPARTMENT)
							&& AttributeHelper.hasAttribute(neighbor1, SBML_Constants.SBML,
									SBML_Constants.COMPARTMENT)) {
						String comp0 = (String) AttributeHelper.getAttributeValue(neighbor0, SBML_Constants.SBML,
								SBML_Constants.COMPARTMENT, "", "");
						String comp1 = (String) AttributeHelper.getAttributeValue(neighbor1, SBML_Constants.SBML,
								SBML_Constants.COMPARTMENT, "", "");
						if (comp0 != comp1) {
							AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES,
									WholecellConstants.SUBSYSTEM + "_DEFINITE",
									WholecellConstants.TRANSPORTER_SUBSYSTEM);
							numOfTransporters++;
						}
					}
				}
			}
		}
		myTab.logMsg(
				"Added " + numOfTransporters + " nodes to subsystem " + WholecellConstants.TRANSPORTER_SUBSYSTEM + ".");
	}

	/**
	 * This method finally completes the subsystem decomposition by assigning any
	 * unassigned reaction to the subsystem
	 * {@link WholecellConstants.DEFAULT_SUBSYSTEM}.
	 */
	private static void addDefaultSubsystem() {
		int numOfDefaults = 0;
		for (Node reactionNode : reactionsFromInitialGraph) {
			if (!AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_DEFINITE")) {
				AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES,
						WholecellConstants.SUBSYSTEM + "_DEFINITE", WholecellConstants.DEFAULT_SUBSYSTEM);
				numOfDefaults++;
			}
		}
		myTab.logMsg("Added " + numOfDefaults + " nodes to subsystem " + WholecellConstants.DEFAULT_SUBSYSTEM + ".");
	}

	/**
	 * This helper class contains all utilities to finally constuct and show the
	 * overview graph.
	 * 
	 * @author Michael
	 *
	 */
	private static class OverviewGraphUtils {

		private HashMap<String, HashMap<String, ArrayList<String>>> subsystemRelationships;

		/**
		 * This method runs the overall overview graph construction and presentation.
		 * 
		 * @param layoutAlgorithm
		 *            A string refering to the layout algorithm that shall be used to
		 *            layout the constructed overview graph.
		 */
		public void run(String layoutAlgorithm) {
			// determineInterfaceMetabolites();
			determineSubsystemRelationships();
			createOverviewGraph();
			createSubsystemGraphs();
			showOverviewGraph(layoutAlgorithm);
		}

		/**
		 * This method determines which subsystems are related to each other by
		 * detection of metabolites between them.
		 */
		private void determineSubsystemRelationships() {

			subsystemRelationships = new HashMap<>();

			// Grab subsystems
			for (Node reactionNode : reactionsFromInitialGraph) {
				String subsystemName = getSubsystem(reactionNode);
				subsystems.add(subsystemName);
			}

			// Initialise HashMap(s)
			for (String subsystem1 : subsystems) {
				HashMap<String, ArrayList<String>> hmap = new HashMap<>();
				for (String subsystem2 : subsystems) {
					if (subsystem2 != subsystem1) {
						ArrayList<String> strList = new ArrayList<>();
						hmap.put(subsystem2, strList);
					}
				}
				subsystemRelationships.put(subsystem1, hmap);
			}

			for (Node speciesNode : speciesFromInitialGraph) {
				Set<Node> inneighbors = speciesNode.getInNeighbors();
				Set<Node> outneighbors = speciesNode.getOutNeighbors();
				HashSet<String> inSystems = new HashSet<>();
				HashSet<String> outSystems = new HashSet<>();
				for (Node reactionNode : inneighbors) {
					inSystems.add(getSubsystem(reactionNode));
				}
				for (Node reactionNode : outneighbors) {
					outSystems.add(getSubsystem(reactionNode));
				}
				for (String inSystem : inSystems) {
					for (String outSystem : outSystems) {
						if (inSystem != outSystem) {
							subsystemRelationships.get(inSystem).get(outSystem)
									.add(AttributeHelper.getLabel(speciesNode, ""));
						}
					}
				}
			}
		}

		/**
		 * This method creates the overview graph by constructing it according to the
		 * classified subsystems and the determined interface metabolites. For each of
		 * the subsystems, a new subsystem node is created and finally the interface
		 * metabolite nodes and the subsystem nodes are joined together via
		 * corresponding edges.
		 * 
		 * During this step, {@link subsystemNodes} and {@link subsystemNameMap} are
		 * instantiated.
		 */
		private void createOverviewGraph() {

			overviewGraph = new AdjListGraph();

			for (String subsystem1 : subsystemRelationships.keySet()) {
				for (String subsystem2 : subsystemRelationships.get(subsystem1).keySet()) {
					if (!subsystemRelationships.get(subsystem1).get(subsystem2).isEmpty()) {
						Node subsystemNode1 = getOrCreateSubsystemNode(subsystem1);
						Node subsystemNode2 = getOrCreateSubsystemNode(subsystem2);
						overviewGraph.addEdge(subsystemNode1, subsystemNode2, true,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
					}
				}
			}

			myTab.setLabelGeneralInfoSubs(String.valueOf(subsystemNameMap.keySet().size()));

		}

		/**
		 * This method checks whether there is already a node in the overview graph with
		 * the specified label. If so, the respective node is returned. Otherwise, a new
		 * node is created and then returned.
		 * 
		 * @param subsystem
		 *            the label of the node that is to be returned or created,
		 *            respectively.
		 * @return the node with the specified label.
		 */
		private Node getOrCreateSubsystemNode(String subsystem) {

			Node subsystemNode = null;
			for (Node node : overviewGraph.getNodes()) {
				if (AttributeHelper.getLabel(node, "").equals(subsystem)) {
					subsystemNode = node;
					break;
				}
			}

			if (subsystemNode == null) {
				Random random = new Random();
				subsystemNode = overviewGraph.addNode(
						AttributeHelper.getDefaultGraphicsAttributeForNode(random.nextInt(1000), random.nextInt(1000)));
				AttributeHelper.setLabel(subsystemNode, subsystem);
				AttributeHelper.setSize(subsystemNode, 100, 100);

				// Storing the created subsystem node to the subsystemNodeMap.
				subsystemNameMap.put(subsystem, subsystemNode);
				subsystemNodes.add(subsystemNode);
			}

			return subsystemNode;

		}

		/**
		 * This method constructs the subsystem graphs depending on the subsystems
		 * contained in the {@link subsystemNameMap} map that has been set before.
		 * Moreover, the subsystem graphs are then put in the {@link subsystemGraphMap}
		 * map.
		 */
		private void createSubsystemGraphs() {

			HashSet<Node> nodes;
			HashSet<Edge> edges;
			Graph subsystemGraph;

			for (String subsystem : subsystemNameMap.keySet()) {
				nodes = new HashSet<>();
				edges = new HashSet<>();

				for (Node reactionNode : reactionsFromInitialGraph) {
					if (subsystem.equals(getSubsystem(reactionNode))) {
						nodes.add(reactionNode);
						edges.addAll(reactionNode.getEdges());
						nodes.addAll(reactionNode.getNeighbors());
					}
				}

				subsystemGraph = new AdjListGraph((CollectionAttribute) initialGraph.getAttributes().copy());
				Map<Node, Node> nodes2newNodes = new HashMap<>();
				for (Node node : nodes) {
					Node newNode = subsystemGraph.addNodeCopy(node);
					nodes2newNodes.put(node, newNode);
				}
				for (Edge edge : edges) {
					Node sourceNode = nodes2newNodes.get(edge.getSource());
					Node targetNode = nodes2newNodes.get(edge.getTarget());
					subsystemGraph.addEdgeCopy(edge, sourceNode, targetNode);
				}
				subsystemGraph.setName(subsystem);
				subsystemGraphMap.put(subsystemNameMap.get(subsystem), subsystemGraph);
			}

		}

		/**
		 * This method finally creates a new viewer that shows the previously
		 * constructed overview graph.
		 */
		private void showOverviewGraph(String layoutAlgorithm) {

			MainFrame.getInstance().showGraph(overviewGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
			switch (layoutAlgorithm) {
			case WholecellConstants.LAYOUT_FORCEDIR:
				layOutForceDir();
				break;
			case WholecellConstants.LAYOUT_CIRCULAR:
				double circumference = 120.0 * subsystems.size();
				layOutCircular(Math.round(circumference / (2.0 * Math.PI)));
				break;
			default:
				break;
			}

		}

		/**
		 * Helper method that returns the subsystem String of a given node.
		 * 
		 * @param node
		 *            The node that we want the subsystem from.
		 * @return The subsystem String that has been assigned to this node.
		 */
		private String getSubsystem(Node node) {
			return (String) AttributeHelper.getAttributeValue(node, WholecellConstants.NOTES,
					WholecellConstants.SUBSYSTEM + "_DEFINITE", "", "");
		}

	}

	/**
	 * Shows the subsystem graph for all subsystem nodes contained in the given
	 * list. Nodes that may be contained in that list but are no subsystem nodes
	 * themselves are simply ignored.
	 * 
	 * @param node
	 *            The node for which the subsystem graph is to be shown.
	 */
	private static void showSubsystemGraphOf(Collection<Node> subsystemNodes) {

		// if more than one node selected?! ...

		for (Node node : subsystemNodes) {
			String label = AttributeHelper.getLabel(node, "");
			if (subsystemNameMap.containsKey(label)) {
				Graph subsystemGraph = subsystemGraphMap.get(subsystemNameMap.get(label));
				MainFrame.getInstance().showGraph(subsystemGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
				String layoutAlgo = myTab.getLayoutAlgorithmSubsystemGraph();
				switch (layoutAlgo) {
				case WholecellConstants.LAYOUT_FORCEDIR:
					layOutForceDir();
					break;
				case WholecellConstants.LAYOUT_CONCENTRIC_CIRC:
					layOutConcentricCirc();
					break;
				case WholecellConstants.LAYOUT_PARALLEL_LINES:
					layOutParallelLines();
					break;
				}

			}
		}

	}

	/**
	 * This method applies a force-directed layout to the currently active editor
	 * session.
	 */
	private static void layOutForceDir() {

		Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();

		ThreadSafeOptions threadSafeOptions = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
		threadSafeOptions.doFinishMoveToTop = true;
		threadSafeOptions.doFinishRemoveOverlapp = true;
		threadSafeOptions.doRemoveAllBends = true;
		threadSafeOptions.temperature_max_move = 300;
		// default value for temp_alpha is 0.998, not really clear if the value 0.98
		// improves the layout or not
		// threadSafeOptions.temp_alpha = 0.98;
		threadSafeOptions.setDval(myOp.DvalIndexSliderZeroLength, 100);
		threadSafeOptions.setDval(myOp.DvalIndexSliderHorForce, 90000);
		threadSafeOptions.setDval(myOp.DvalIndexSliderVertForce, 90000);
		Selection selection = new Selection(graph.getGraphElements());
		MyNonInteractiveSpringEmb nonInteractiveSpringEmbedder = new MyNonInteractiveSpringEmb(graph, selection,
				threadSafeOptions);
		// run without background task
		// nonInteractiveSpringEmbedder.run();
		// run in background task
		BackgroundTaskHelper.issueSimpleTask("LayoutModel", "", nonInteractiveSpringEmbedder, null,
				nonInteractiveSpringEmbedder);

	}

	/**
	 * This method applies a circular layout to the currently active editor session.
	 */
	private static void layOutCircular(double radius) {
		CircleLayouterWithMinimumCrossingsAlgorithm cl = new CircleLayouterWithMinimumCrossingsAlgorithm(radius);
		Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		Selection selection = new Selection(graph.getGraphElements());
		cl.attach(graph, selection);
		cl.execute();
	}

	/**
	 * This method produces a layout of the graph that consists of two concentric
	 * circles. The outer circle consists of the species whereas the inner circle is
	 * made up of the reactions. Moreover, the barycenter heuristic for crossing
	 * minimisation is applied.
	 */
	private static void layOutConcentricCirc() {

		ArrayList<Node> species = new ArrayList<>();
		ArrayList<Node> reactions = new ArrayList<>();
		Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		for (Node node : graph.getNodes()) {
			if (isSpecies(node)) {
				species.add(node);
			} else if (isReaction(node)) {
				reactions.add(node);
			}
		}

		crossingMin(species, reactions);

		double circumference = Math.max(30 * species.size(), 30 * reactions.size());
		int minRad = (int) Math.round(circumference / (2 * Math.PI));
		int maxRad = 2 * minRad;
		int center = maxRad + 100;
		for (int i = 0; i < reactions.size(); i++) {

			int xPos = (int) Math
					.round(center + minRad * Math.cos((((double) i) / ((double) reactions.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + minRad * Math.sin((((double) i) / ((double) reactions.size())) * 2 * Math.PI));
			;
			AttributeHelper.setPosition(reactions.get(i), xPos, yPos);
		}
		for (int i = 0; i < species.size(); i++) {

			int xPos = (int) Math
					.round(center + maxRad * Math.cos((((double) i) / ((double) species.size())) * 2 * Math.PI));
			int yPos = (int) Math
					.round(center + maxRad * Math.sin((((double) i) / ((double) species.size())) * 2 * Math.PI));
			;
			AttributeHelper.setPosition(species.get(i), xPos, yPos);
		}
	}

	/**
	 * This method produces a layout of the graph that consists of two parallel
	 * lines. The upper line consists of the species whereas the lower line is made
	 * up of the reactions. Moreover, the barycenter heuristic for crossing
	 * minimisation is applied.
	 */
	private static void layOutParallelLines() {

		ArrayList<Node> species = new ArrayList<>();
		ArrayList<Node> reactions = new ArrayList<>();
		Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		for (Node node : graph.getNodes()) {
			if (isSpecies(node)) {
				species.add(node);
			} else if (isReaction(node)) {
				reactions.add(node);
			}
		}

		crossingMin(species, reactions);

		int xSpan = Math.max(60 * species.size(), 60 * reactions.size());

		int xPos = 0;
		int xStep = xSpan / species.size();
		for (int i = 0; i < species.size(); i++) {
			AttributeHelper.setPosition(species.get(i), xPos, 100);
			xPos += xStep;
		}

		xPos = 0;
		xStep = xSpan / reactions.size();
		for (int i = 0; i < reactions.size(); i++) {
			AttributeHelper.setPosition(reactions.get(i), xPos, 700);
			xPos += xStep;
		}

	}

	/**
	 * This method applies the barycenter heuristic for two layer crossing
	 * minimisation, choosing the best result out of five.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 */
	private static void crossingMin(ArrayList<Node> layer1, ArrayList<Node> layer2) {
		ArrayList<Node> currentMinL1;
		ArrayList<Node> currentMinL2;
		ArrayList<Node> workCopyL1 = (ArrayList<Node>) layer1.clone();
		ArrayList<Node> workCopyL2 = (ArrayList<Node>) layer2.clone();
		int currentMin = numberOfCrossings(workCopyL1, workCopyL2);
		currentMinL1 = (ArrayList<Node>) workCopyL1.clone();
		currentMinL2 = (ArrayList<Node>) workCopyL2.clone();
		for (int i = 0; i < 5; i++) {
			Collections.shuffle(workCopyL1);
			Collections.shuffle(workCopyL2);
			crossingMin(workCopyL1, workCopyL2, -1);
			int noc = numberOfCrossings(workCopyL1, workCopyL2);
			System.out.println("NoC: " + noc);
			if (noc < currentMin) {
				currentMin = noc;
				currentMinL1 = (ArrayList<Node>) workCopyL1.clone();
				currentMinL2 = (ArrayList<Node>) workCopyL2.clone();
			}
		}
		System.out.println("Found min: " + currentMin);
		for (int i = 0; i < workCopyL1.size(); i++) {
			layer1.set(i, workCopyL1.get(i));
		}
		for (int i = 0; i < workCopyL2.size(); i++) {
			layer2.set(i, workCopyL2.get(i));
		}
	}

	/**
	 * This is a helper function for the crossing minimisation. It calculates the
	 * barycenters of the nodes and sorts the layers according to their barycenters.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 * @param numberOfCrossings
	 *            The current best result.
	 */
	private static void crossingMin(ArrayList<Node> layer1, ArrayList<Node> layer2, int numberOfCrossings) {
		Map<Node, Double> node2barycenter = new HashMap<>();
		for (Node node : layer1) {
			node2barycenter.put(node, Double.valueOf(getBarycenter(node, layer2)));
		}
		// BubbleSort according to barycenter.
		for (int i = 0; i < layer1.size() - 1; i++) {
			int m = i;
			for (int j = i + 1; j < layer1.size(); j++) {
				if (node2barycenter.get(layer1.get(j)).doubleValue() < node2barycenter.get(layer1.get(m))
						.doubleValue()) {
					m = j;
				}
			}
			Collections.swap(layer1, i, m);
		}
		int newNumberOfCrossings = numberOfCrossings(layer1, layer2);
		if (numberOfCrossings == -1 || newNumberOfCrossings < numberOfCrossings) {
			crossingMin(layer2, layer1, newNumberOfCrossings);
		}
	}

	/**
	 * This method counts the number of crossings that occur between the two layers.
	 * 
	 * @param layer1
	 *            First layer.
	 * @param layer2
	 *            Second layer.
	 * @return The number of crossings between the two layers.
	 */
	private static int numberOfCrossings(ArrayList<Node> layer1, ArrayList<Node> layer2) {
		int res = 0;
		for (int i = 0; i < layer1.size(); i++) {
			for (int j = i + 1; j < layer1.size(); j++) {
				for (Node n1 : layer1.get(i).getNeighbors()) {
					for (Node n2 : layer1.get(j).getNeighbors()) {
						int k = layer2.indexOf(n1);
						int l = layer2.indexOf(n2);
						if (k > l) {
							res++;
						}
					}
				}
			}
		}
		return res;
	}

	/**
	 * This method computes for the given node the barycenter of the index positions
	 * of all its neighbors in the given neighbor array.
	 * 
	 * @param node
	 *            The node for which the barycenter is to be computed.
	 * @param neighborList
	 *            The list in which the neighbors are contained.
	 * @return The barycenter of the index positions of the nodes neighbors.
	 */
	private static double getBarycenter(Node node, ArrayList<Node> neighborList) {
		double res = 0.0;
		for (Node neighbor : node.getNeighbors()) {
			res += ((double) neighborList.indexOf(neighbor));
		}
		res /= ((double) node.getNeighbors().size());
		return res;
	}

	/**
	 * Check whether a certain node is a reaction node.
	 * 
	 * @param node
	 *            The node to be checked
	 * @return whether the given node is a reaction node
	 */
	private static boolean isReaction(Node node) {

		return isRole(node, SBML_Constants.ROLE_REACTION);

	}

	/**
	 * Check whether a certain node is a species node.
	 * 
	 * @param node
	 *            The node to be checked
	 * @return whether the given node is a species node
	 */
	private static boolean isSpecies(Node node) {

		return isRole(node, SBML_Constants.ROLE_SPECIES);

	}

	/**
	 * Check whether a certain node has a given role.
	 * 
	 * @param node
	 *            The node to be checked
	 * @param role
	 *            The role for which the role affiliation is to be checked
	 * @return whether the given node has the specified role
	 */
	private static boolean isRole(Node node, String role) {

		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML, SBML_Constants.SBML_ROLE)) {
			String sbmlRole = (String) AttributeHelper.getAttributeValue(node, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, "", "");
			if (sbmlRole.equals(role))
				return true;
		}
		return false;

	}

}