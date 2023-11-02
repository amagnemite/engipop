package engipop;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import engipop.EngiPanel.Classes;
import engipop.Node.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParseException;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	private EngiPanel containingPanel;
	private SettingsWindow setWindow;
	
	public PopulationParser(EngiPanel containingPanel, SettingsWindow setWindow) {
		this.containingPanel = containingPanel;
		this.setWindow = setWindow;
	}
	
	//parses entire population
	public PopNode parsePopulation(File file, Map<String, List<TemplateData>> templateMap) {
		VDFNode root = null;
		Object[] includes;
		Set<String> defaultTemplates = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		defaultTemplates.addAll(Arrays.asList("robot_standard.pop", "robot_giant.pop", "robot_gatebot.pop"));
		//PopNode popNode;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			//if(root == null) {
			//	window.updateFeedback(file.getName() + " is not a population file");
			//	return null;
			//}
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch(IOException i) {
			containingPanel.updateFeedback(file.getName() + " was not found");
			return null;
		}
		catch(VDFParseException v) { //TODO: edit vdfparser for more specifics
			//also need to do something about missing values
			containingPanel.updateFeedback("Failed to parse popfile, mismatched number of brackets?");
			return null;
		}
		
		if(root.size() > 2) {
			containingPanel.updateFeedback("Population file has too many root keys");
			return null;
		}
		
		if(root.containsKey("#base")) {
			includes = root.get(root.firstKey());
			
			for(Object includedPop : includes) {
				if(defaultTemplates.contains(includedPop)) {
					URL popURL = MainWindow.class.getResource("/" + (String) includedPop);
					//Entry<String, List<TemplateData>> entry = parseTemplates(new File(popURL.getFile()));
					parseTemplates(new File(popURL.getFile()), templateMap);
					
					//templateMap.put(entry.getKey(), entry.getValue());
				}
				else {
					//Entry<String, List<TemplateData>> entry = parseTemplates(
					parseTemplates(new File(setWindow.getTFPathString() + "\\population\\" + (String) includedPop), templateMap);
					
					//templateMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants		
		return new PopNode(root);
	}
	
	//parses one template pop at a time
	//public Entry<String, List<TemplateData>> parseTemplates(File file, Map<String, List<TemplateData>>) {
	public void parseTemplates(File file, Map<String, List<TemplateData>> templateMap) {
		String filename = file.getName().substring(0, file.getName().length() - 4);
		VDFNode root = null;
		VDFNode templates = null;
		
		List<TemplateData> templateList = new ArrayList<TemplateData>();
	
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
			containingPanel.updateFeedback(file.getName() + " was not found");
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
				String cclass = null;
				
				if(node.containsKey("Name")) {
					name = node.getString("Name");
				}
				if(node.containsKey("Class")) {
					cclass = node.getString("Class");
				}
				
				if(subset.removeAll(botKeys)) { //keep all keys that are also in botkeys
					templateList.add(new TemplateData(entry.getKey(), cclass, name, WaveSpawnNode.TFBOT));
				}
				else if(subset.removeAll(wsKeys)) {
					templateList.add(new TemplateData(entry.getKey(), name, WaveNode.WAVESPAWN));
				}
				else if(subset.removeAll(overlappingKeys)) { //contains template, name or both, handle later
					templateQueue.add(entry);
				}
				else { //consists either of uncommon keys or rafmod keys, deal with later
					//note also rafmod allows tank templates
					templateList.add(new TemplateData(entry.getKey(), name, "OTHER"));
				}
			}
		}
		else {
			containingPanel.updateFeedback("No templates to process");
		}
		
		templateMap.put(filename, templateList);
		//return new AbstractMap.SimpleEntry<String, List<TemplateData>>(filename, templateList);
	}
	
	public static class TemplateData {
		private int classSlot = -1;
		private String templateName;
		private String blockName = null; //refers to botname, ws name or tank name
		private String type = null; //this should probably be an enum of some sort
		
		public TemplateData(String templateName, Classes cclass, String botName, String type) {
			this.templateName = templateName;
			classSlot = cclass.getSlot();
			this.blockName = botName;
			this.type = type;
		}
		
		public TemplateData(String templateName, String classString, String botName, String type) {
			this.templateName = templateName;
			classSlot = Classes.toClass(classString).getSlot();
			this.blockName = botName;
			this.type = type;
		}
		
		public TemplateData(String templateName, String blockName, String type) {
			this.templateName = templateName;
			this.blockName = blockName;
			this.type = type;
		}

		public int getClassSlot() {
			return this.classSlot;
		}
		
		public String getTemplateName() {
			return this.templateName;
		}
		
		public String getBlockName() {
			return this.blockName;
		}
		
		public String getType() {
			return this.type;
		}
		
		public String toString() {
			String name = "";
			
			if(blockName != null) {
				name = blockName + " ";
			}
			name = name + "(" + templateName + ")";
			
			return name;
		}
	}
}
