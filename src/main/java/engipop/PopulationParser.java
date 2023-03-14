package engipop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import engipop.Tree.Node;
import engipop.Tree.TFBotNode;
import engipop.Tree.TFBotNode.TFBotKeys;
import engipop.Tree.TemplateInfoNode;
import engipop.Tree.TemplateNode;
import engipop.Tree.WaveSpawnNode;
import net.platinumdigitalgroup.jvdf.VDFBinder;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	//ideally we only have 1 instance of this per panel/whatever
	//that parses any population put into it
	private MainWindow window;
	private Node templateTreeRoot = new Node();
	private List<String> botTemplateList = new ArrayList<String>();
	private List<String> wsTemplateList = new ArrayList<String>();
	private Map<Integer, String> botLengthPopMap = new HashMap<Integer, String>();
	private Map<Integer, String> wsLengthPopMap = new HashMap<Integer, String>();
	
	public PopulationParser(MainWindow window) {
		this.window = window;
	}
	
	public void processPop(File file) {
		//read in pop
		//make note of #includes, will need it later
		
		//parse templates
		//parse missions
		
		//parse rest
		
		
	}
	
	public VDFNode readPop(File file) {
		VDFNode root = null;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch (IOException i) {
			window.updateFeedback(file.getName() + " was moved or not found");
		}
		
		return root;
	}
	
	public void parseTemplates(ItemParser itemparser, File file) {
		String filename = file.getName();
		VDFNode templateRoot = null;
		TemplateInfoNode popTemplateRoot = new TemplateInfoNode(filename);
		TemplateNode botTemplateParent = new TemplateNode();
		TemplateNode wsTemplateParent = new TemplateNode();
		
		//this is kinda awful, probably should do something better
		//excludes name and template
		Set<String> botKeys =  new HashSet<String>(Arrays.asList("Class", "ClassIcon", "Health",
				"Scale", "TeleportWhere", "AutoJumpMin", "AutoJumpMax", "Skill", "WeaponRestrictions",
				"BehaviorModifiers", "MaxVisionRange", "Item", "Attributes", "ItemAttributes",
				"CharacterAttributes", "EventChangeAttributes"));
		Set<String> overlappingKeys = new HashSet<String>(Arrays.asList("Template", "Name"));
				
		LinkedList<Entry<String, Object[]>> templateQueue = new LinkedList<Entry<String, Object[]>>();
		
		templateRoot = readPop(file);
		if(templateRoot != null) { //no root or waveschedule
			templateRoot = templateRoot.getSubNode("Templates");
			if(templateRoot != null) { //no templates to process
				for(Entry<String, Object[]> entry : templateRoot.entrySet()) {
					VDFNode node = templateRoot.getSubNode(entry.getKey());		
					Set<String> subset = new HashSet<String>(node.keySet()) ;
					Set<String> overlapSet = new HashSet<String>(node.keySet());
					//ensures these are copies
					 
					//for now assume people capitalize their keyvals
					if(subset.retainAll(botKeys)) { //keep all keys that are also in botkeys
						//contains tfbot keys	
						botTemplateParent.putTemplate(entry.getKey(), new TFBotNode(node, itemparser));
						botTemplateList.add(entry.getKey());
						//this is dumb
					}
					else if(overlapSet.retainAll(overlappingKeys)) { //contains template, name or both, handle later
						templateQueue.add(entry);
					}
					else { //otherwise ws
						//wsTemplateParent.putTemplate(filename, wsTemplateParent);
						//need conversion here
					}
					
				}
				
				//connect nodes only if they exist
				if(!botTemplateParent.getMap().isEmpty()) {
					botTemplateParent.connectNodes(popTemplateRoot);
					botLengthPopMap.put(botTemplateList.size() - 1, filename); //last index with related templates, inclusive
				}
				if(!wsTemplateParent.getMap().isEmpty()) {
					wsTemplateParent.connectNodes(popTemplateRoot);
				}
				if(popTemplateRoot.hasChildren()) {
					popTemplateRoot.connectNodes(templateTreeRoot);
				}
			}
		}
	}
	
	public List<String> getBotTemplateList() {
		return this.botTemplateList;
	}
	
	public List<String> getWaveSpawnTemplateList() {
		return this.wsTemplateList;
	}
}
