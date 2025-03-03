package engipop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import engipop.Engipop.Classes;
import engipop.Node.*;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParseException;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class PopulationParser { //parse .pop
	private MainWindow mainWindow;
	private SettingsWindow setWindow;
	
	public PopulationParser(MainWindow mainWindow, SettingsWindow setWindow) {
		this.mainWindow = mainWindow;
		this.setWindow = setWindow;
	}
	
	//parses entire population
	public PopNode parsePopulation(File file, String type) {
		VDFNode root = null;
		Object[] includes;
		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			//if(root == null) {
			//	window.updateFeedback(file.getName() + " is not a population file");
			//	return null;
			//}
			
			//only two possible keyvals at root, include which is always first and waveschedule
		}
		catch(IOException i) {
			mainWindow.setFeedback(file.getName() + " was not found");
			return null;
		}
		catch(VDFParseException v) { //TODO: edit vdfparser for more specifics
			//also need to do something about missing values
			System.out.println(v.getMessage());
			mainWindow.setFeedback("Failed to parse popfile, mismatched number of brackets?");
			return null;
		}
		
		if(root.size() > 2) {
			mainWindow.setFeedback("Population file has too many root keys");
			return null;
		}
		
		if(root.containsKey("#base")) {
			includes = root.get(root.firstKey());
			
			for(Object includedPop : includes) {
				parseTemplates((String) includedPop, type);
			}
		}
		
		root = root.getSubNode(root.lastKey()); //waveschedule is standard name, but can be named whatever user wants		
		return new PopNode(root);
	}
	
	//parses one template pop at a time
	public void parseTemplates(String filename, String location) {
		Set<String> defaultTemplates = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		defaultTemplates.addAll(Arrays.asList("robot_standard.pop", "robot_giant.pop", "robot_gatebot.pop"));
		File file = null;
		
		if(defaultTemplates.contains(filename)) {
			return;
		}
		else {
			file = Engipop.getPopulationPath().resolve(filename).toFile();
			if(!file.exists()) {
				file = Engipop.getDownloadPopulationPath().resolve(filename).toFile();
			}
		}
		
		if(file == null) {
			mainWindow.setFeedback("Can't find " + filename);
			return;
		}
		
		if(!Engipop.getImportedTemplatePops().containsKey(file.getName()) && 
				!Engipop.getIncludedTemplatePops().containsKey(file.getName())) {
			parseTemplates(file, location);
		}
	}
	
	public void parseTemplates(String name, URL url, String location) {
		InputStream stream;
		byte[] bytes = null;
		
		try {
			stream = url.openStream();
			bytes = stream.readAllBytes();
			stream.close();
		}
		catch (IOException e) {
			return;
		}
		parseTemplates(name, new String(bytes, StandardCharsets.US_ASCII), location);
	}
	
	public void parseTemplates(File file, String location) {
		String string = null;
		try {
			string = ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII);
		}
		catch (IOException e) {
			mainWindow.setFeedback(file.getName() + " was not found");
			return;
		}
		parseTemplates(file.getName(), string, location);
	}
	
	private void parseTemplates(String filename, String data, String location) {
		VDFNode root = null;
		Object[] includes;
		
		root = new VDFParser().parse(data);
		//only two possible keyvals at root, include which is always first and waveschedule
		root = root.getSubNode(root.lastKey());
		
		if(root.containsKey("#base")) {
			includes = root.get(root.firstKey());
			
			for(Object includedPop : includes) {
				parseTemplates((String) includedPop, location);
			}
		}
		
		if(root.containsKey("Templates")) {
			if(location == PopulationPanel.IMPORTED) {
				Engipop.getImportedTemplatePops().put(filename, new PopNode(root));
			}
			else if(location == PopulationPanel.INCLUDED) {
				Engipop.getIncludedTemplatePops().put(filename, new PopNode(root));
			}
		}
		else {
			mainWindow.setFeedback("No templates to process");
		}
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
