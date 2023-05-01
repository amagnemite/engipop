package engipop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import engipop.Node.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	//ideally we only have 1 instance of this per panel/whatever
	//that parses any population put into it
	private final static String WAVESPAWNSUFFIX = "WAVESPAWN";
	private final static String TFBOTSUFFIX = "TFBOT";
	
	private EngiWindow window;
	private SettingsWindow setWindow;
	public Map<String, String> botTemplateMap = new HashMap<String, String>();
	public Map<String, String> wsTemplateMap = new HashMap<String, String>();
	//key: bot/ws name + template name, value: file name
	
	public PopulationParser(EngiWindow window, SettingsWindow setWindow) {
		this.window = window;
		this.setWindow = setWindow;
	}
	
	//parses entire population
	public PopNode parsePopulation(File file) {
		VDFNode root = null;
		Object[] includes;
		//Set<Object> defaultTemplates = new HashSet<Object>("robot_standard.pop", "robot_giant.pop", "robot_gatebot.pop");
		//PopNode popNode;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch (IOException i) {
			window.updateFeedback(file.getName() + " was not found");
			return null;
		}
		
		//once again, need error checking here
		includes = root.get(root.firstKey());
		root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants
		
		for(Object includedPop : includes) {
			
			parseTemplates(new File(setWindow.getScriptPathString() + "\\population\\" + (String) includedPop));
		}
		
		//parse templates
		//parse missions
		return new PopNode(root);
	}
	
	//parses one template pop at a time
	public void parseTemplates(File file) {
		String filename = file.getName().substring(0, file.getName().length() - 4);
		VDFNode root = null;
		VDFNode templates = null;
		boolean hasTemplates = false;
	
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
		root = root.getSubNode(root.lastKey());
		
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
				VDFNode node = templates.getSubNode(entry.getKey());		
				Set<String> subset = new HashSet<String>(node.keySet()); //copies
				String name = null;
				
				//for now assume people capitalize their keyvals
				if(subset.retainAll(botKeys)) { //keep all keys that are also in botkeys
					if(node.containsKey("Name")) {
						name = node.getString("Name");
						
					}
					else {
						name = node.getString("Class");
					}
					name = name + "(" + entry.getKey() + ")";
					
					botTemplateMap.put(name, filename);
				}
				else if(subset.retainAll(wsKeys)) {
					if(node.containsKey("Name")) {
						name = node.getString("Name");
					}
					name = name + "(" + entry.getKey() + ")";
					
					wsTemplateMap.put(name, filename);
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
