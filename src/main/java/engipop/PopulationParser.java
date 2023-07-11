package engipop;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import engipop.Node.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	private EngiWindow window;
	private SettingsWindow setWindow;
	
	public PopulationParser(EngiWindow window, SettingsWindow setWindow) {
		this.window = window;
		this.setWindow = setWindow;
	}
	
	//parses entire population
	public PopNode parsePopulation(File file, Map<String, String> botTemplateStringMap, Map<String, String> wsTemplateStringMap) {
		VDFNode root = null;
		Object[] includes;
		Set<String> defaultTemplates = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		defaultTemplates.addAll(Arrays.asList("robot_standard.pop", "robot_giant.pop", "robot_gatebot.pop"));
		//PopNode popNode;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			if(root == null) {
				window.updateFeedback(file.getName() + " is not a population file");
				return null;
			}
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch (IOException i) {
			window.updateFeedback(file.getName() + " was not found");
			return null;
		}
		
		if(root.size() > 2) {
			window.updateFeedback("Population file has too many root keys");
			return null;
		}
		
		if(root.containsKey("#include") || root.containsKey("#base")) {
			includes = root.get(root.firstKey());
			
			for(Object includedPop : includes) {
				if(defaultTemplates.contains(includedPop)) {
					URL popURL = MainWindow.class.getResource("/" + (String) includedPop);
					parseTemplates(new File(popURL.getFile()), botTemplateStringMap, wsTemplateStringMap);
				}
				else {
					parseTemplates(new File(setWindow.getScriptPathString() + "\\population\\" + (String) includedPop), 
						botTemplateStringMap, wsTemplateStringMap);
				}
			}
		}
		
		root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants
		return new PopNode(root);
	}
	
	//parses one template pop at a time
	public void parseTemplates(File file, Map<String, String> botTemplateStringMap, Map<String, String> wsTemplateStringMap) {
		String filename = file.getName().substring(0, file.getName().length() - 4);
		VDFNode root = null;
		VDFNode templates = null;
	
		//this is kinda awful, probably should do something better
		//excludes name and template
		Set<String> wsKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> botKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> overlappingKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		wsKeys.addAll(Arrays.asList("Where", "TotalCount", "MaxActive", "SpawnCount",
				"WaitBeforeStarting", "WaitBetweenSpawns", "WaitBetweenSpawnsAfterDeath", "TotalCurrency", 
				"WaitForAllSpawned", "WaitForAllDead", "Support", "StartWaveOutput", "FirstSpawnOutput",
				"LastSpawnOutput", "DoneOutput"));
		botKeys.addAll(Arrays.asList("Class", "ClassIcon", "Health",
				"Scale", "TeleportWhere", "AutoJumpMin", "AutoJumpMax", "Skill", "WeaponRestrictions",
				"BehaviorModifiers", "MaxVisionRange", "Item", "Attributes", "ItemAttributes",
				"CharacterAttributes", "EventChangeAttributes"));
		overlappingKeys.addAll(Arrays.asList("Template", "Name"));
				
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
		
		if(root.containsKey("Templates")) { //templates to process
			templates = root.getSubNode("Templates");
			
			for(Entry<String, Object[]> entry : templates.entrySet()) {
				VDFNode node = templates.getSubNode(entry.getKey());		
				Set<String> subset = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER); //copies
				subset.addAll(node.keySet());
				String name = null;
				
				if(subset.retainAll(botKeys)) { //keep all keys that are also in botkeys
					if(node.containsKey("Name")) {
						name = node.getString("Name");
					}
					else {
						name = node.getString("Class");
					}
					name = name != null ? name + " (" + entry.getKey() + ")" : "(" + entry.getKey() + ")";
					
					botTemplateStringMap.put(name, filename);
				}
				else if(subset.retainAll(wsKeys)) {
					if(node.containsKey("Name")) {
						name = node.getString("Name");
					}
					name = name != null ? name + " (" + entry.getKey() + ")" : "(" + entry.getKey() + ")";
					
					wsTemplateStringMap.put(name, filename);
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
