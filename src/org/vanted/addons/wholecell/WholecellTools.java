package org.vanted.addons.wholecell;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.graffiti.editor.actions.ClipboardService;
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
import org.graffiti.session.EditorSession;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.SplitNodeForSingleMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components.ConnectedComponentLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

@SuppressWarnings("nls")
public class WholecellTools {
	
	static List<String> clonableSpecies = new ArrayList<>(Arrays.asList(new String[] { "ACP", "ADP", "AMP", "ATP", "CMP", "CO2", "CTP", "DTDP", "FAD", "FADH2",
			"FMN", "FMNH2", "GDP", "GTP", "H+", "H+[e]", "H+[p]", "H2CO3", "H2O", "H2O[e]", "H2O[p]", "H2O2", "Na+", "Na+[p]", "NAD", "NADH", "NADP", "NADPH",
			"NH3", "O2", "Phosphate", "Phosphate[e]", "Phosphate[p]", "PPi", "PRPP", "S-Adenosyl-homocysteine", "S-Adenosyl-L-methionine", "Tetrahydrofolate",
			"UDP", "UMP", "UTP" }));
	
	private static int clonableSpeciesThreshold = 8;
	
	static String extracellularCompartmentID = "e";
	
	static List<String> inegligibleKEGGPathways = new ArrayList<>(
			Arrays.asList(new String[] { "Metabolic pathways", "Biosynthesis of secondary metabolites", "Microbial metabolism in diverse environments",
					"Biosynthesis of antibiotics", "Carbon metabolism", "2-Oxocarboxylic acid metabolism", "Fatty acid metabolism",
					"Degradation of aromatic compounds", "Biosynthesis of amino acids" }));
	
	/*
	 * Read the notes of reactions and species (XML hierarchy) and add them as attributes to the nodes
	 */
	public static ActionListener readNotes() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				
				if (MainFrame.getInstance().getActiveEditorSession() == null)
					return;
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				Set<String> subsystems = new HashSet<>();
				
				for (Node node : graph.getNodes()) {
					// read notes of reactions
					SBMLReactionHelper sbmlReactionHelper = new SBMLReactionHelper(graph);
					// if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML, SBML_Constants.REACTION_NOTES)) {
					if (isReaction(node)) {
						XMLNode notes = sbmlReactionHelper.getNotes(node);
						int numChildren = notes.getNumChildren();
						for (int k = 0; k < numChildren; k++) {
							XMLNode child = notes.getChild(k);
							int numGrandChildren = child.getNumChildren();
							for (int l = 0; l < numGrandChildren; l++) {
								XMLNode grandChild = child.getChild(l);
								readNote(node, grandChild, "GENE_ASSOCIATION", "gene_association");
								readNote(node, grandChild, "KEGG_RID", "kegg_rid");
								readNote(node, grandChild, "PROTEIN_ASSOCIATION", "protein_association");
								readNote(node, grandChild, "PROTEIN_CLASS", "protein_class");
								readNote(node, grandChild, "SOURCE", "source_model");
								String subsystem = readNote(node, grandChild, "SUBSYSTEM", WholecellConstants.SUBSYSTEM + "0");
								if (subsystem != null)
									subsystems.add(subsystem);
							}
						}
					}
					// read notes of species
					SBMLSpeciesHelper sbmlSpeciesHelper = new SBMLSpeciesHelper(graph);
					// if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML, SBML_Constants.SPECIES_NOTES)) {
					if (isSpecies(node)) {
						XMLNode notes = sbmlSpeciesHelper.getNotes(node);
						int numChildren = notes.getNumChildren();
						for (int k = 0; k < numChildren; k++) {
							XMLNode child = notes.getChild(k);
							int numGrandChildren = child.getNumChildren();
							for (int l = 0; l < numGrandChildren; l++) {
								XMLNode grandChild = child.getChild(l);
								int numGrandGrandChildren = grandChild.getNumChildren();
								for (int m = 0; m < numGrandGrandChildren; m++) {
									XMLNode grandGrandChild = grandChild.getChild(m);
									readNote(node, grandGrandChild, "ABBREVIATION", "abbreviation");
									readNote(node, grandGrandChild, "KEGGCID", "keggcid");
									readNote(node, grandGrandChild, "FORMULA", "formula_model");
									readNote(node, grandGrandChild, "SOURCE", "source_model");
								}
							}
						}
					}
				}
				
				String[] knownSubsystemsSorted = subsystems.toArray(new String[subsystems.size()]);
				Arrays.sort(knownSubsystemsSorted);
				for (int k = 0; k < knownSubsystemsSorted.length; k++)
					System.out.println(knownSubsystemsSorted[k]);
				
				WholecellTab.setReadNotes();
				WholecellTab.setRequestAdditionalSubsystemsFromKEGG();
				
			}
			
		};
		
	}
	
	static String readNote(Node node, XMLNode xmlNode, String noteName, String noteAttributeName) {
		
		if (xmlNode.isText() && xmlNode.getCharacters().contains(noteName)) {
			// String note = xmlNode.getCharacters().replace(noteName + ": ", "");
			String note = xmlNode.getCharacters().trim();
			note = note.replace(noteName + ": ", "");
			AttributeHelper.setAttribute(node, WholecellConstants.NOTES, noteAttributeName, note);
			return note;
		}
		return null;
		
	}
	
	public static ActionListener requestAdditionalSubsystemsFromKEGG() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				// Map<String, String> gene_id_to_pathway_ids = new HashMap<>();
				// Map<String, String> pathway_id_to_pathway_name = new HashMap<>();
				// List<String> lines = null;
				// try {
				// lines = new TextFile("C:/Home/Projekte/Wholecell/visualisation/gene_id_to_pathway_id.txt");
				// for (String line : lines) {
				// String[] arrLine = line.split("\t");
				// if (gene_id_to_pathway_ids.containsKey(arrLine[0])) {
				// String pathway_ids = gene_id_to_pathway_ids.get(arrLine[0]) + "\t" + arrLine[1];
				// gene_id_to_pathway_ids.put(arrLine[0], pathway_ids);
				// } else
				// gene_id_to_pathway_ids.put(arrLine[0], arrLine[1]);
				// }
				// } catch (IOException ioException) {
				// ioException.printStackTrace();
				// }
				// try {
				// lines = new TextFile("C:/Home/Projekte/Wholecell/visualisation/pathway_id_to_pathway_name.txt");
				// for (String line : lines) {
				// String[] arrLine = line.split("\t");
				// pathway_id_to_pathway_name.put(arrLine[0], arrLine[1]);
				// }
				// } catch (IOException ioException) {
				// ioException.printStackTrace();
				// }
				
				RestService restService = new RestService("http://rest.kegg.jp/get/");
				int counter = 1;
				for (Node reactionNode : graph.getNodes())
					if (isReaction(reactionNode)) {
						String sbml_id = (String) AttributeHelper.getAttributeValue(reactionNode, SBML_Constants.SBML, SBML_Constants.REACTION_ID, "", "");
						String subsystem = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + "0",
								"", "");
						// mark UNKNOWN or SPONTANEOUS
						if (subsystem.equals("UNKNOWN") || subsystem.equals("SPONTANEOUS"))
							AttributeHelper.setFillColor(reactionNode, Color.RED);
						String kegg_rid = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES, "kegg_rid", "", "");
						// String gene_associations = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES, "gene_association", "", "");
						Set<String> subsystems = new HashSet<>();
						String text = counter + "\tsbml id\t" + sbml_id + "\tsub-system\t" + subsystem;
						boolean isAnnotationConfirmed = false;
						// if (!gene_associations.contains("(") && !gene_associations.contains(")")) {
						// gene_associations = gene_associations.replace(" and ", " ");
						// gene_associations = gene_associations.replace(" or ", " ");
						// String[] arrGene_association = gene_associations.split(" ");
						// for (String gene_association : arrGene_association) {
						// text = text + "\tgene\t" + gene_association;
						// String pathway_ids = gene_id_to_pathway_ids.get(gene_association);
						// if (pathway_ids != null) {
						// String[] arrPathway_id = pathway_ids.split("\t");
						// for (String pathway_id : arrPathway_id) {
						// String pathway_name = pathway_id_to_pathway_name.get(pathway_id);
						// if (pathway_name != null && !subsystem.equals(pathway_name)) {
						// subsystems.add(pathway_name);
						// text = text + "\tpathway\t" + pathway_name;
						// }
						// if (pathway_name != null && subsystem.equals(pathway_name))
						// isAnnotationConfirmed = true;
						// }
						// }
						// }
						// }
						if (kegg_rid.length() > 0) {
							text = text + "\tkegg rid\t" + kegg_rid;
							String response = (String) restService.makeRequest("rn:" + kegg_rid, MediaType.TEXT_PLAIN_TYPE, String.class);
							if (response != null) {
								String[] arrLines = response.split("\n");
								int lineIndex = 0;
								while (lineIndex < arrLines.length) {
									if (arrLines[lineIndex].startsWith("PATHWAY"))
										do {
											String line = arrLines[lineIndex];
											line = line.substring(line.indexOf("rn") + 2);
											line = line.substring(line.indexOf(" ") + 2);
											if (!subsystem.equals(line) && !inegligibleKEGGPathways.contains(line)) {
												subsystems.add(line);
												text = text + "\tpathway\t" + line;
											}
											if (subsystem.equals(line))
												isAnnotationConfirmed = true;
											lineIndex++;
										} while (arrLines[lineIndex].startsWith(" "));
									else
										lineIndex++;
								}
								text = text + "\tFINISHED";
							} else
								text = text + "\tERROR";
						}
						// add new sub-systems
						int subsystemIndex = 0;
						if (subsystem.length() > 0)
							subsystemIndex = 1;
						for (String newSubsystem : subsystems) {
							AttributeHelper.setAttribute(reactionNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex, newSubsystem);
							subsystemIndex++;
						}
						// mark unconfirmed annotations
						if (!isAnnotationConfirmed && !subsystem.equals("UNKNOWN") && !subsystem.equals("SPONTANEOUS"))
							AttributeHelper.setFillColor(reactionNode, Color.YELLOW);
						System.out.println(text);
						counter++;
						try {
							Thread.sleep(100);
						} catch (InterruptedException interruptedException) {
							interruptedException.printStackTrace();
						}
					}
				
				WholecellTab.setRequestAdditionalSubsystemsFromKEGG();
				WholecellTab.setSetSpeciesSubsystems();
				
			}
			
		};
		
	}
	
	/*
	 * Set sub-systems for species, by default only the reactions have set sub-systems
	 */
	public static ActionListener setSpeciesSubsystems() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				// for all species
				for (Node speciesNode : graph.getNodes())
					if (isSpecies(speciesNode)) {
						String label = AttributeHelper.getLabel(speciesNode, "");
						if (!clonableSpecies.contains(label)) {
							
							// get sub-systems for all reactions with this species and
							// count number of reactions within a sub-system for this species
							Map<String, Integer> subsystems = new HashMap<>();
							for (Node reactionNode : speciesNode.getNeighbors()) {
								int subsystemIndex = 0;
								// while (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex)) {
								String subsystem = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
										WholecellConstants.SUBSYSTEM + subsystemIndex, "", "");
								Integer numberOf = Integer.valueOf(1);
								if (subsystems.containsKey(subsystem))
									numberOf = Integer.valueOf(subsystems.get(subsystem).intValue() + 1);
								subsystems.put(subsystem, numberOf);
								// subsystemIndex++;
								// }
							}
							
							// remove all sub-systems with more than one reaction for this species, this species would be part of this sub-system
							// only keep sub-systems with one reaction for this species, this species would be an interface between the sub-systems
							List<String> subsystemsToBeRemoved = new ArrayList<>();
							for (String subsystem : subsystems.keySet())
								if (subsystems.get(subsystem).intValue() > 1)
									subsystemsToBeRemoved.add(subsystem);
							for (String subsystem : subsystemsToBeRemoved)
								subsystems.remove(subsystem);
							
							// set sub-systems for species if more than one
							if (subsystems.size() > 1) {
								int subsystemIndex = 0;
								for (String subsystem : subsystems.keySet())
									AttributeHelper.setAttribute(speciesNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex++, subsystem);
							}
							
						}
					}
				
				WholecellTab.setSetSpeciesSubsystems();
				WholecellTab.setCreateOverviewGraph();
				
			}
			
		};
		
	}
	
	public static ActionListener createOverviewGraph() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				Graph newGraph = new AdjListGraph(); // overview graph
				
				// for all species
				for (Node speciesNode : graph.getNodes())
					if (isSpecies(speciesNode) &&
							AttributeHelper.hasAttribute(speciesNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + "0")) {
						// add species to overview graph
						Node newSpeciesNode = newGraph.addNodeCopy(speciesNode);
						AttributeHelper.setSize(newSpeciesNode, 50, 50);
						// for all sub-systems
						int subsystemIndex = 0;
						while (AttributeHelper.hasAttribute(speciesNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex)) {
							String subsystem = (String) AttributeHelper.getAttributeValue(speciesNode, WholecellConstants.NOTES,
									WholecellConstants.SUBSYSTEM + subsystemIndex, "", "");
							Node subsystemNode = null;
							// check whether overview graph already contains a node for this sub-system ...
							for (Node node : newGraph.getNodes())
								if (!isSpecies(node)) {
									String label = AttributeHelper.getLabel(node, "");
									if (label.equals(subsystem)) {
										subsystemNode = node;
										break;
									}
								}
							// ... if not add node to the overview graph
							if (subsystemNode == null) {
								Random random = new Random();
								subsystemNode = newGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(random.nextInt(1000), random.nextInt(1000)));
								AttributeHelper.setLabel(subsystemNode, subsystem);
								AttributeHelper.setSize(subsystemNode, 100, 100);
							}
							// add edge to the overview graph connecting species and node for sub-system
							newGraph.addEdge(newSpeciesNode, subsystemNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
							subsystemIndex++;
						}
					}
				// show overview graph
				MainFrame.getInstance().showGraph(newGraph, null);
				
				WholecellTab.setCreateOverviewGraph();
				WholecellTab.setCloneSpecies();
				
			}
			
		};
		
	}
	
	/*
	 * Clone species based on a static definition of species which can be cloned
	 */
	public static ActionListener cloneSpecies() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				// select species which can be cloned
				Selection selection = new Selection("speciesToBeCloned");
				for (Node node : graph.getNodes()) {
					String label = AttributeHelper.getLabel(node, "");
					if (clonableSpecies.contains(label) && isSpecies(node))
						selection.add(node);
				}
				SelectionModel selectionModel = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
				selectionModel.setActiveSelection(selection);
				selectionModel.selectionChanged();
				
				// split species using split nodes algorithm
				Algorithm splitNodeForSingleMappingData = new SplitNodeForSingleMappingData();
				splitNodeForSingleMappingData.attach(graph, selection);
				splitNodeForSingleMappingData.setParameters(getCloneSpeciesParameters());
				splitNodeForSingleMappingData.execute();
				
				WholecellTab.setCloneSpecies();
				WholecellTab.setSeparateCompartments();
				
			}
			
		};
		
	}
	
	static Parameter[] getCloneSpeciesParameters() {
		
		return new Parameter[] {
				new ObjectListParameter("Split nodes with a degree over the specified threshold", "", "", new ArrayList<String>()),
				new IntegerParameter(Integer.valueOf(clonableSpeciesThreshold), "", ""),
				new BooleanParameter(false, "", ""),
				new BooleanParameter(true, "", "") };
		
	}
	
	/*
	 * Separate Compartments
	 */
	public static ActionListener separateCompartments() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				List<Node> nodes = new ArrayList<>();
				nodes.addAll(graph.getNodes());
				for (Node reactionNode : nodes)
					if (isReaction(reactionNode)) {
						Set<String> compartments = new HashSet<>();
						Set<Node> neighbors = reactionNode.getNeighbors();
						for (Node neighbor : neighbors)
							if (AttributeHelper.hasAttribute(neighbor, SBML_Constants.SBML, SBML_Constants.COMPARTMENT)) {
								String compartment = (String) AttributeHelper.getAttributeValue(neighbor, SBML_Constants.SBML, SBML_Constants.COMPARTMENT, "", "");
								if (compartment.length() > 0)
									compartments.add(compartment);
							}
						if (compartments.size() > 1)
							for (Node neighbor : neighbors)
								if (neighbor.getDegree() > 1) {
									Node newNode = graph.addNodeCopy(neighbor);
									for (Edge edge : reactionNode.getEdges()) {
										if (edge.getSource().equals(neighbor)) {
											edge.setSource(newNode);
											break;
										}
										if (edge.getTarget().equals(neighbor)) {
											edge.setTarget(newNode);
											break;
										}
									}
								}
					}
				
				WholecellTab.setSeparateCompartments();
				WholecellTab.setLayoutModelForceDirected();
				
			}
			
		};
		
	}
	
	/*
	 * Lay out the model using force directed layout algorithm
	 */
	public static ActionListener layoutModelForceDirected() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				ThreadSafeOptions threadSafeOptions = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
				threadSafeOptions.doFinishMoveToTop = true;
				threadSafeOptions.doFinishRemoveOverlapp = true;
				threadSafeOptions.doRemoveAllBends = true;
				threadSafeOptions.temperature_max_move = 300;
				// default value for temp_alpha is 0.998, not really clear if the value 0.98 improves the layout or not
				// threadSafeOptions.temp_alpha = 0.98;
				threadSafeOptions.setDval(myOp.DvalIndexSliderZeroLength, 100);
				threadSafeOptions.setDval(myOp.DvalIndexSliderHorForce, 90000);
				threadSafeOptions.setDval(myOp.DvalIndexSliderVertForce, 90000);
				Selection selection = new Selection(graph.getGraphElements());
				MyNonInteractiveSpringEmb nonInteractiveSpringEmbedder = new MyNonInteractiveSpringEmb(graph, selection, threadSafeOptions);
				// run without background task
				// nonInteractiveSpringEmbedder.run();
				// run in background task
				BackgroundTaskHelper.issueSimpleTask("LayoutModel", "", nonInteractiveSpringEmbedder, null, nonInteractiveSpringEmbedder);
				
				WholecellTab.setLayoutModelForceDirected();
				WholecellTab.setLayoutModelUnconnectedSubgraphs();
				
			}
			
		};
		
	}
	
	/*
	 * Lay out the model using unconnected subgraphs on grid layout algorithm
	 */
	public static ActionListener layoutModelUnconnectedSubgraphs() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				ConnectedComponentLayout connectedComponentLayout = new ConnectedComponentLayout();
				connectedComponentLayout.attach(graph, null);
				connectedComponentLayout.execute();
				
				WholecellTab.setLayoutModelUnconnectedSubgraphs();
				WholecellTab.setFilterNonTransporters();
				
			}
			
		};
		
	}
	
	public static ActionListener filterNonTransporters() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				Selection selection = new Selection("filterNonTransporters");
				
				// for all reactions
				for (Node reactionNode : graph.getNodes())
					if (isReaction(reactionNode) && reactionNode.getNeighbors().size() > 1) {
						Set<String> compartments = new HashSet<>();
						// check whether species are located in two or more compartments ...
						for (Node neighbor : reactionNode.getNeighbors())
							if (AttributeHelper.hasAttribute(neighbor, SBML_Constants.SBML, SBML_Constants.COMPARTMENT)) {
								String compartment = (String) AttributeHelper.getAttributeValue(neighbor, SBML_Constants.SBML, SBML_Constants.COMPARTMENT, "", "");
								if (compartment.length() > 0)
									compartments.add(compartment);
							}
						// ... if not add reaction and all species of this reaction to selection
						if (compartments.size() == 1) {
							selection.add(reactionNode);
							selection.addAll(reactionNode.getEdges());
							selection.addAll(reactionNode.getNeighbors());
						}
					}
				
				SelectionModel selectionModel = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
				selectionModel.setActiveSelection(selection);
				selectionModel.selectionChanged();
				
				WholecellTab.setFilterNonTransporters();
				WholecellTab.setCreatePathwayViews();
				
			}
			
		};
		
	}
	
	public static ActionListener createPathwayViews() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				
				Set<String> subsystems = new HashSet<>();
				
				for (Node reactionNode : graph.getNodes())
					if (isReaction(reactionNode)) {
						int subsystemIndex = 0;
						// int subsystemIndex = 1;
						while (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex)) {
							String subsystem = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
									WholecellConstants.SUBSYSTEM + subsystemIndex, "", "");
							if (!subsystems.contains(subsystem))
								subsystems.add(subsystem);
							subsystemIndex++;
						}
					}
				
				SelectionModel selectionModel = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
				for (String currentSubsystem : subsystems) {
					// for (String currentSubsystem : subsystems2) {
					Selection selection = new Selection(currentSubsystem);
					for (Node reactionNode : graph.getNodes())
						if (isReaction(reactionNode)) {
							// for all sub-systems
							int subsystemIndex = 0;
							// int subsystemIndex = 1;
							while (AttributeHelper.hasAttribute(reactionNode, WholecellConstants.NOTES, WholecellConstants.SUBSYSTEM + subsystemIndex)) {
								String subsystem = (String) AttributeHelper.getAttributeValue(reactionNode, WholecellConstants.NOTES,
										WholecellConstants.SUBSYSTEM + subsystemIndex, "", "");
								if (subsystem.equals(currentSubsystem)) {
									selection.add(reactionNode);
									// extend selection to adjacent species
									selection.addAll(reactionNode.getEdges());
									selection.addAll(reactionNode.getNeighbors());
								}
								subsystemIndex++;
							}
						}
					selectionModel.setActiveSelection(selection);
					selectionModel.selectionChanged();
					
					Graph pathwayGraph = new AdjListGraph((CollectionAttribute) graph.getAttributes().copy());
					Map<Node, Node> nodes2newNodes = new HashMap<>();
					for (Node node : selectionModel.getActiveSelection().getNodes()) {
						Node newNode = pathwayGraph.addNodeCopy(node);
						nodes2newNodes.put(node, newNode);
					}
					for (Edge edge : selectionModel.getActiveSelection().getEdges()) {
						Node sourceNode = nodes2newNodes.get(edge.getSource());
						Node targetNode = nodes2newNodes.get(edge.getTarget());
						pathwayGraph.addEdgeCopy(edge, sourceNode, targetNode);
					}
					pathwayGraph.setName(currentSubsystem);
					MainFrame.getInstance().showGraph(pathwayGraph, null, LoadSetting.VIEW_CHOOSER_NEVER);
					
				}
				
				WholecellTab.setCreatePathwayViews();
				
			}
			
		};
		
	}
	
	public static ActionListener copySelectionToClipboard() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String labels = "";
				Selection selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
				for (Node node : selection.getNodes())
					labels = labels + AttributeHelper.getLabel(node, "<no label>") + "\n";
				ClipboardService.writeToClipboardAsText(labels);
				
			}
			
		};
		
	}
	
	public static ActionListener pasteSelectionFromClipboard() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				EditorSession editorSession = MainFrame.getInstance().getActiveEditorSession();
				String[] labels = getLabels();
				if (labels == null)
					return;
				selectNodes(editorSession, labels, "fromClipboard");
				
			}
			
		};
		
	}
	
	public static ActionListener pasteSelectionFromClipboardToAllSessions() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Set<EditorSession> editorSessions = MainFrame.getEditorSessions();
				String[] labels = getLabels();
				if (labels == null)
					return;
				int sessionIndex = 0;
				for (EditorSession editorSession : editorSessions) {
					selectNodes(editorSession, labels, "fromClipboard" + sessionIndex++);
					printFoundSpecies(editorSession.getGraph(), labels);
				}
				
			}
			
		};
		
	}
	
	static String[] getLabels() {
		
		String clipboardText = ClipboardService.readFromClipboardAsText();
		if (clipboardText == null)
			return null;
		String[] labels = clipboardText.split("\n");
		Arrays.sort(labels);
		return labels;
		
	}
	
	static void selectNodes(EditorSession editorSession, String[] labels, String selectionName) {
		
		Graph graph = editorSession.getGraph();
		Selection selection = new Selection(selectionName);
		for (Node node : graph.getNodes()) {
			String nodeLabel = AttributeHelper.getLabel(node, "<no label>");
			for (String label : labels)
				if (label.equals(nodeLabel)) {
					selection.add(node);
					break;
				}
		}
		SelectionModel selectionModel = editorSession.getSelectionModel();
		selectionModel.setActiveSelection(selection);
		selectionModel.selectionChanged();
		
	}
	
	static void printFoundSpecies(Graph graph, String[] labels) {
		
		String text = graph.getName().replace(".gml", "");
		for (String label : labels) {
			text = text + ";";
			for (Node node : graph.getNodes()) {
				String nodeLabel = AttributeHelper.getLabel(node, "<no label>");
				if (label.equals(nodeLabel)) {
					text = text + label;
					break;
				}
			}
		}
		System.out.println(text);
		
	}
	
	static boolean isReaction(Node node) {
		
		return isRole(node, SBML_Constants.ROLE_REACTION);
		
	}
	
	static boolean isSpecies(Node node) {
		
		return isRole(node, SBML_Constants.ROLE_SPECIES);
		
	}
	
	private static boolean isRole(Node node, String role) {
		
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML, SBML_Constants.SBML_ROLE)) {
			String sbmlRole = (String) AttributeHelper.getAttributeValue(node, SBML_Constants.SBML, SBML_Constants.SBML_ROLE, "", "");
			if (sbmlRole.equals(role))
				return true;
		}
		return false;
		
	}
	
}
