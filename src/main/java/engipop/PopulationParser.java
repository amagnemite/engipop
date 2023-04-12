package engipop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import engipop.Tree.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	//ideally we only have 1 instance of this per panel/whatever
	//that parses any population put into it
	private final static String WAVESPAWNSUFFIX = "WAVESPAWN";
	private final static String TFBOTSUFFIX = "TFBOT";
	
	private EngiWindow window;
	public Map<String, Map<String, String>> templateMap = new HashMap<String, Map<String, String>>(4); //parameterize this
	//key: file name + WAVESPAWN or TFBOT suffix, value: map
	//submaps key: template name, value: bot/ws name
	
	public PopulationParser(MainWindow window) {
		this.window = window;
	}
	
	//parses entire population
	public void parsePopulation(File file) {
		VDFNode root = null;
		String[] includes;
		PopNode popNode;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch (IOException i) {
			window.updateFeedback(file.getName() + " was not found");
			return;
		}
		
		//once again, need error checking here
		includes = (String[]) root.get(root.firstKey());
		root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants
		
		popNode = new PopNode(root);
		
		
		//parse templates
		//parse missions
	}
	
	//parses one template pop at a time
	public void parseTemplates(ItemParser itemparser, File file) {
		String filename = file.getName().substring(0, file.getName().length() - 4);
		VDFNode root = null;
		VDFNode templates = null;
		boolean hasTemplates = false;
		Map<String, String> botList = new HashMap<String, String>(8);
		Map<String, String> wsList = new HashMap<String, String>(4);
	
		//this is kinda awful, probably should do something better
		//excludes name and template
		Set<String> wsKeys = new HashSet<String>(Arrays.asList("Where", "TotalCount", "MaxActive", "SpawnCount",
				"WaitBeforeStarting", "WaitBetweenSpawns", "WaitBetweenSpawnsAfterDeath", "TotalCurrency", 
				"WaitForAllSpawned", "WaitForAllDead", "Support", "StartWaveOutput", "FirstSpawnOutput",
				"LastSpawnOutput", "DoneOutput"));
		Set<String> botKeys = new HashSet<String>(Arrays.asList("Class", "ClassIcon", "Health",
				"Scale", "TeleportWhere", "AutoJumpMin", "AutoJumpMax", "Skill", "WeaponRestrictions",
				"BehaviorModifiers", "MaxVisionRange", "Item", "Attributes", "ItemAttributes",
				"CharacterAttributes", "EventChangeAttributes"));
		Set<String> overlappingKeys = new HashSet<String>(Arrays.asList("Template", "Name"));
				
		LinkedList<Entry<String, Object[]>> templateQueue = new LinkedList<Entry<String, Object[]>>();
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch (IOException i) {
			window.updateFeedback(file.getName() + " was not found");
			return;
		}
		Iterator<String> iterator = root.keySet().iterator();
		
		while(iterator.hasNext() && !hasTemplates) {
			String key = iterator.next();
			
			if(key.toUpperCase().equals("TEMPLATES")) {
				hasTemplates = true;
				templates = root.getSubNode(key);
			}
		}
		
		if(hasTemplates) { //no templates to process
			for(Entry<String, Object[]> entry : templates.entrySet()) {
				VDFNode node = root.getSubNode(entry.getKey());		
				Set<String> subset = new HashSet<String>(node.keySet()); //copies
				String name = null;
				
				//for now assume people capitalize their keyvals
				if(subset.retainAll(botKeys)) { //keep all keys that are also in botkeys
					if(templateMap.get(filename + TFBOTSUFFIX) == null) {
						templateMap.put(filename + TFBOTSUFFIX, botList);
					}
					if(node.containsKey("Name")) {
						name = node.getString("Name");
					}
					
					botList.put(entry.getKey(), name);
				}
				else if(subset.retainAll(wsKeys)) {
					if(templateMap.get(filename + WAVESPAWNSUFFIX) == null) {
						templateMap.put(filename + WAVESPAWNSUFFIX, wsList);
					}
					if(node.containsKey("Name")) {
						name = node.getString("Name");
					}
					
					wsList.put(entry.getKey(), name);
				}
				else if(subset.retainAll(overlappingKeys)) { //contains template, name or both, handle later
					templateQueue.add(entry);
				}
				else { //consists either of uncommon keys or rafmod keys, deal with later
					
					//wsTemplateParent.putTemplate(filename, wsTemplateParent);
					//need conversion here
				}
				
			}
			
		}
		else {
			window.updateFeedback("No templates to process");
		}
	}
}
