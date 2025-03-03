package engipop;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import engipop.Engipop.Classes;
import engipop.Engipop.ItemSlot;
import engipop.Node.*;

public class TreeParse { //it is time to parse
	private int indentCount = 0;
	private static String indent = "																				";
	//20 indents
	
	public static final String PETALTNAME = "assister in Pyrovision";
	//counts as assister is some kind of pet this update is going to be awesome
	
	public TreeParse() {
	}
	
	//sanity check pop, make sure no nulls and general silliness can get printed
	public String treeCheck(PopNode root) {
		//int waveCount = tree.getRoot().getChildren().size();
		String stopCheck = "";
		ListIterator<Node> iterator = root.getChildren().listIterator();
		int waveNum = 1;
		
		//if(popNode.getChildren().size() == 0) {
		//	stopCheck = "Popfile generation failed, no waves to generate";
		//}
		
		//i hate it here
		while(stopCheck.isEmpty() && iterator.hasNext()) {
			boolean lastWave = waveNum < root.getChildren().size() ? false : true;
			//if it's less than size, then not lastwave otherwise it is
			stopCheck = checkWave((WaveNode) iterator.next(), waveNum, lastWave);
			waveNum++;
		}
		
		//error returned is the lowest offense (something related to spawner, then wavespawn, then wave)
		return stopCheck; 
	}
	
	private String checkRelay(Node parent, String key) {
		String stopCheck = "";
		
		if(parent.containsKey(key) ) {
			RelayNode relay = (RelayNode) parent.getValue(key);
			
			if(!relay.containsKey(RelayNode.TARGET)) {
				stopCheck = key + " has no target";
			}
			else if(!relay.containsKey(RelayNode.ACTION)) {
				stopCheck = key + " has no action";
			}
		}
		
		return stopCheck;
	}
	
	private String checkWave(WaveNode wave, int waveNum, boolean lastWave) {
		String error = "Wave " + waveNum + "'s ";
		String stopCheck = "";
		
		stopCheck = checkRelay(wave, WaveNode.STARTWAVEOUTPUT);
		stopCheck = checkRelay(wave, WaveNode.DONEOUTPUT);
		stopCheck = checkRelay(wave, WaveNode.INITWAVEOUTPUT);
			
		if(stopCheck.isEmpty()) {
			ListIterator<Node> iterator = wave.getChildren().listIterator();
			while(stopCheck.isEmpty() && iterator.hasNext()) {
				stopCheck = checkWaveSpawn((WaveSpawnNode) iterator.next(), waveNum);
			}
		}
		else {
			stopCheck = error + stopCheck;
		}
		
		return stopCheck;
	}
	
	//check ws relays here
	private String checkWaveSpawn(WaveSpawnNode ws, int waveNum) {
		String stopCheck = ""; //may not even get into the checks in here
		String errorString = "Wavespawn " + (String) ws.getValue(WaveSpawnNode.NAME) + " in wave " + waveNum 
				+ " has an empty ";
		//name can be potentially empty here
		//warning no where
		//warning no counts
		//waitforall names
		
		//this gets set to the last offending relay
		if(ws.containsKey(WaveSpawnNode.STARTWAVEOUTPUT)) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnNode.STARTWAVEOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "StartWaveOutput";
		}
		if(ws.containsKey(WaveSpawnNode.FIRSTSPAWNOUTPUT)) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "FirstSpawnOutput";
		}
		if(ws.containsKey(WaveSpawnNode.LASTSPAWNOUTPUT)) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnNode.LASTSPAWNOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "LastSpawnOutput";
		}
		if(ws.containsKey(WaveSpawnNode.DONEOUTPUT)) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnNode.DONEOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "DoneOutput";
		}
		
		if(ws.containsKey(WaveSpawnNode.TEMPLATE)) {
			String template = (String) ws.getValue(WaveSpawnNode.TEMPLATE);
			
			for(Entry<String, PopNode> entry : Engipop.getImportedTemplatePops().entrySet()) {
				if(!template.contains(entry.getKey()) && entry.getValue().getWSTemplateMap().containsKey(template)) {
					Engipop.includeTemplate(entry.getKey());
				}
			}
		}
		
		//if wavespawn has a spawner that isn't a tank and it doesn't have a where 
		if(ws.hasChildren() && ws.getSpawnerType() != SpawnerType.TANK) {
			if(!ws.getMap().containsKey(WaveSpawnNode.WHERE)) {
				stopCheck = "Wavespawn " + (String) ws.getValue(WaveSpawnNode.NAME) + " in wave " + waveNum 
						+ " does not have a Where selected";
			}
		}
		
		if(stopCheck.isEmpty() && ws.hasChildren()) {
			if(ws.getSpawner().getClass() == TFBotNode.class) {
				checkBot((TFBotNode) ws.getSpawner());
			}
			else if(ws.getSpawner().getClass() == TankNode.class) {
				stopCheck = checkTank((TankNode) ws.getSpawner(), waveNum, (String) ws.getValue(WaveSpawnNode.NAME));
			}
			else if(ws.getSpawner().getClass() == SquadNode.class || ws.getSpawner().getClass() == RandomChoiceNode.class) {
				checkSquadRandom(ws.getSpawner());
			}
		}
		
		return stopCheck;
	}
	
	//unlike wave/spawn, for now just strip out stock weapons here so they don't get unnecessarily printed
	private void checkBot(TFBotNode bot) {
		Classes botClass = (Classes) bot.getValue(TFBotNode.CLASSNAME);
		
		if(bot.containsKey(TFBotNode.ITEM)) {
			String[] itemList = (String[]) bot.getValue(TFBotNode.ITEM);
			
			int slot = ItemSlot.PRIMARY.getSlot();
			if(itemList[slot] != null && itemList[slot].equals(botClass.primary())) {
				itemList[slot] = null;
			}
			
			slot = ItemSlot.SECONDARY.getSlot();
			if(itemList[slot] != null && itemList[slot].equals(botClass.secondary())) {
				itemList[slot] = null;
			}
			
			slot = ItemSlot.MELEE.getSlot();
			if(itemList[slot] != null && itemList[slot].equals(botClass.melee())) {
				itemList[slot] = null;
			}
			
			slot = ItemSlot.BUILDING.getSlot();
			if(itemList[slot] != null && itemList[slot].equals(botClass.building())) {
				itemList[slot] = null;
			}
		}
		
		if(bot.containsKey(TFBotNode.TEMPLATE)) {
			String template = (String) bot.getValue(TFBotNode.TEMPLATE);
			List<String> newIncludes = new ArrayList<String>();
			
			for(Entry<String, PopNode> entry : Engipop.getImportedTemplatePops().entrySet()) {
				if(!template.contains(entry.getKey()) && entry.getValue().getBotTemplateMap().containsKey(template)) {
					if(!newIncludes.contains(entry.getKey())) {
						newIncludes.add(entry.getKey());
					}
				}
			}
			
			for(String pop : newIncludes) { //need to do this way since can't add to map while iterating through it
				Engipop.includeTemplate(pop);
			}
		}
	} 
	
	private String checkTank(TankNode tank, int waveNum, String wsName) {
		String stopCheck = "";
		String errorString = "Tank in wavespawn " + wsName + " in wave " + waveNum + " has an empty ";
		
		if(tank.containsKey(TankNode.ONKILLEDOUTPUT)) {
			stopCheck = ((RelayNode) tank.getValue(TankNode.ONKILLEDOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "OnKilledOutput";
		}
		if(tank.containsKey(TankNode.ONBOMBDROPPEDOUTPUT)) {
			stopCheck = ((RelayNode) tank.getValue(TankNode.ONBOMBDROPPEDOUTPUT)).containsKey(RelayNode.TARGET)
					? "" : errorString + "OnKilledOutput";
		}
		
		return stopCheck;
	}
	
	private String checkSquadRandom(Node squadRandom) { //generic here since don't know whether squad or random
		String stopCheck = "";
		
		if(!squadRandom.hasChildren()) {
			//possibly error print here
		}
		else {
			for(Node node : squadRandom.getChildren()) {
				if(node.getClass() == TFBotNode.class) {
					checkBot((TFBotNode) node);
				}
				else if(node.getClass() == TankNode.class) {
					//checkTank((TankNode) node);
				}
				else if(node.getClass() == SquadRCNode.class) {
					checkSquadRandom(node);
				}
			}
		}
		return stopCheck;
	}
	
	public void parseTree(File originalFile, PopNode root) {
		File tempFile;
		File backupFile = new File(originalFile.getAbsolutePath() + ".bak");
		int waveCount = root.getChildren().size();
		FileWriter fw;
		PrintWriter pw;
		indentCount = 0;
		
		try { //make these work so parse doesn't happen if error
			tempFile = File.createTempFile(originalFile.getName(), ".tmp", originalFile.getParentFile());
			tempFile.deleteOnExit();
			fw = new FileWriter(tempFile);
			pw = new PrintWriter(fw, true);
			
			pw.println("//made with engipop");
			
			for(String include : Engipop.getIncludedTemplatePops().keySet()) {
				pw.println("#base " + include);
			}
			
			pw.println("");
			pw.println("WaveSchedule");
			pw.println("{");
			
			//indentCount++;
			printPopulation(pw, root);
			indentPrintln(pw, "");
			
			//pain
			for(int i = 0; i < waveCount; i++) {
				printWave(pw, (WaveNode) root.getChildren().get(i));
			}
			//indentCount--;
			pw.println("}");
			pw.close();
			fw.close();
			
			if(backupFile.exists()) {
				backupFile.delete();
			}
			originalFile.renameTo(backupFile);
			tempFile.renameTo(originalFile);
		}
		catch (FileNotFoundException e){
			
		}
		catch (IOException i) {
			
		}
	}
	
	private void printGenericMap(PrintWriter pw, Map<String, List<Object>> map) {
		for(Entry<String, List<Object>> entry : map.entrySet()) {
			for(Object subentry : entry.getValue()) {
				if(subentry instanceof Map) {
					indentPrintln(pw, entry.getKey());
					indentPrintln(pw, "{");
					indentCount++;
					
					printGenericMap(pw, (Map<String, List<Object>>) subentry);
					
					indentCount--;
					indentPrintln(pw, "}");
				}
				else {
					/*
					String string = "";
					
					if(entry.getKey().contains(" ")) { //basically an itemattributes check
						string = "\"" + entry.getKey() + "\" ";
					}
					else {
						string = entry.getKey() + " ";
					}
					
					
					if(subentry.getClass() == String.class) {
						if(((String) subentry).contains(" ") || ((String) subentry).contains(",") || 
							((String) subentry).contains("/")) {
							string = string + "\"" + subentry + "\"";
						}
						else {
							string = string + subentry;
						}
						
						indentPrintln(pw, entry.getKey(), subentry);
					}
					else {
						indentPrintln(pw, string + subentry);
					}
					*/
					indentPrintln(pw, entry.getKey(), subentry);
				}
			}
		}
	}

	private void printPopulation(PrintWriter pw, PopNode root) { //population settings
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(root.getMap()); //do this to prevent case issues
		
		if(root.containsKey(PopNode.STARTINGCURRENCY)) {
			indentPrintln(pw, PopNode.STARTINGCURRENCY, root.getValue(PopNode.STARTINGCURRENCY));
			mapCopy.remove(PopNode.STARTINGCURRENCY);
		}
		
		if(root.containsKey(PopNode.RESPAWNWAVETIME)) {
			if((int) root.getValue(PopNode.RESPAWNWAVETIME) != 10 && (int) root.getValue(PopNode.RESPAWNWAVETIME) != 0) {
				indentPrintln(pw, PopNode.RESPAWNWAVETIME, root.getValue(PopNode.RESPAWNWAVETIME));
			}
			mapCopy.remove(PopNode.RESPAWNWAVETIME);
		}
		
		if((boolean) root.getValue(PopNode.EVENTPOPFILE)) { //double check these escapes
			indentPrintln(pw, "EventPopfile Halloween");
		}
		mapCopy.remove(PopNode.EVENTPOPFILE);
		
		if((boolean) root.getValue(PopNode.FIXEDRESPAWNWAVETIME)) {
			indentPrintln(pw, "FixedRespawnWaveTime 1");
		}
		mapCopy.remove(PopNode.FIXEDRESPAWNWAVETIME);
		
		if(root.containsKey(PopNode.BUSTERDAMAGE)) {
			if((int) root.getValue(PopNode.BUSTERDAMAGE) != Engipop.BUSTERDEFAULTDMG) {
				indentPrintln(pw, PopNode.BUSTERDAMAGE, root.getValue(PopNode.BUSTERDAMAGE));
			}
			mapCopy.remove(PopNode.BUSTERDAMAGE);
		}
		
		if(root.containsKey(PopNode.BUSTERKILLS)) {
			if((int) root.getValue(PopNode.BUSTERKILLS) != Engipop.BUSTERDEFAULTKILLS) {
				indentPrintln(pw, PopNode.BUSTERKILLS, root.getValue(PopNode.BUSTERKILLS));
			}
			mapCopy.remove(PopNode.BUSTERKILLS);
		}
		
		if(!(boolean) root.getValue(PopNode.BOTSATKINSPAWN)) {
			indentPrintln(pw, "CanBotsAttackWhileInSpawnRoom no");
		} //double check if a nonexistent bots attack allows spawn attacks
		mapCopy.remove(PopNode.BOTSATKINSPAWN);
		
		if(root.containsKey(PopNode.MISSION)) {
			for(Object obj : root.getListValue(PopNode.MISSION)) {
				MissionNode mission = (MissionNode) obj;
				printMission(pw, mission);
			}
		}
		mapCopy.remove(PopNode.MISSION);
		
		if(root.getBotTemplateMap().size() > 0 || root.getWSTemplateMap().size() > 0) {
			indentPrintln(pw, PopNode.TEMPLATE);
			indentPrintln(pw, "{");
			indentCount++;
			
			Iterator<Entry<String, Node>> iterator = root.getBotTemplateMap().entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, Node> entry = iterator.next();
				printTFBot(pw, (TFBotNode) entry.getValue(), entry.getKey());
				if(iterator.hasNext()) {
					indentPrintln(pw, "");
				}
			}

			for(Entry<String, Node> entry : root.getWSTemplateMap().entrySet()) {
				indentPrintln(pw, entry.getKey());
				indentPrintln(pw, "{");
				indentCount++;
				
				printWaveSpawn(pw, (WaveSpawnNode) entry);
				
				indentCount--;
				indentPrintln(pw, "}");
			}
			
			indentCount--;
			indentPrintln(pw, "}");
		}
	
		printGenericMap(pw, mapCopy);
	}
	
	private void printWave(PrintWriter pw, WaveNode node) { //a wave
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		indentPrintln(pw, "Wave");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(node.containsKey(WaveNode.STARTWAVEOUTPUT)) {
			printRelay(pw, WaveNode.STARTWAVEOUTPUT, (RelayNode) node.getValue(WaveNode.STARTWAVEOUTPUT));
			mapCopy.remove(WaveNode.STARTWAVEOUTPUT);
		}
		
		if(node.containsKey(WaveNode.DONEOUTPUT)) {
			printRelay(pw, WaveNode.DONEOUTPUT, (RelayNode) node.getValue(WaveNode.DONEOUTPUT));
			mapCopy.remove(WaveNode.DONEOUTPUT);
		} //since tree was already validated upstream, only final wave should pass this case
		
		if(node.containsKey(WaveNode.INITWAVEOUTPUT)) {
			printRelay(pw, WaveNode.INITWAVEOUTPUT, (RelayNode) node.getValue(WaveNode.INITWAVEOUTPUT));
			mapCopy.remove(WaveNode.INITWAVEOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		for(int i = 0; i < node.getChildren().size(); i++) {
			printWaveSpawn(pw, (WaveSpawnNode) node.getChildren().get(i));
		}
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printRelay(PrintWriter pw, String name, RelayNode node) {
		if(!node.containsKey(RelayNode.TARGET)) {
			return;
		}
		
		indentPrintln(pw, name);
		indentPrintln(pw, "{");
		indentCount++;
		
		indentPrintln(pw, RelayNode.TARGET, node.getValue(RelayNode.TARGET));
		indentPrintln(pw, RelayNode.ACTION, node.getValue(RelayNode.ACTION));
		
		if(node.containsKey(RelayNode.PARAM)) {
			indentPrintln(pw, RelayNode.PARAM, node.getValue(RelayNode.PARAM));
		}
		
		if(node.containsKey(RelayNode.DELAY)) {
			indentPrintln(pw, RelayNode.DELAY, node.getValue(RelayNode.DELAY));
		}
		
		//this will lead to some very unhinged formatting for vscripts but that's a later problem
		//node.getMap().forEach((k, v) -> indentPrintln(pw, k + " " + "\"" + v.get(0) + "\""));
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printMission(PrintWriter pw, MissionNode node) {
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		indentPrintln(pw, "Mission");
		indentPrintln(pw, "{");
		indentCount++;
		
		for(String key : MissionNode.getNodeKeyList()) {
			if(node.containsKey(key)) {
			
				if(key.equals(MissionNode.WHERE)) {
					if(node.getSpawner() != null) { // spawner.getClass() != TankNode.class) { //no wheres for tanks
						for(Object where : mapCopy.remove(WaveSpawnNode.WHERE)) {
							indentPrintln(pw, MissionNode.WHERE, where);
						}
					}
				}
				else {
					indentPrintln(pw, key, node.getValue(key));
				}
				mapCopy.remove(key);
			}
		}
		printGenericMap(pw, mapCopy);
		printTFBot(pw, (TFBotNode) node.getSpawner(), WaveSpawnNode.TFBOT);
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printWaveSpawn(PrintWriter pw, WaveSpawnNode node) { //a wavespawn
		Node spawner = node.hasChildren() ? node.getSpawner() : null;
		//since spawnerless wavespawns are valid
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		indentPrintln(pw, "WaveSpawn");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(node.containsKey(WaveSpawnNode.NAME)) {
			indentPrintln(pw, WaveSpawnNode.NAME, node.getValue(WaveSpawnNode.NAME));
			mapCopy.remove(WaveSpawnNode.NAME);
		}
		if(node.containsKey(WaveSpawnNode.WHERE)) { //somewhat redundant check 
			if(spawner != null && spawner.getClass() != TankNode.class) { //no wheres for tanks
				for(Object where : mapCopy.remove(WaveSpawnNode.WHERE)) {
					indentPrintln(pw, WaveSpawnNode.WHERE, where);
				}
			} //way this is written now is spawnerless wavespawns don't get wheres
		} //which may or may not be irrelevant
		
		//spinners don't allow empty values
		if(node.containsKey(WaveSpawnNode.TOTALCOUNT)) {
			if((int) node.getValue(WaveSpawnNode.TOTALCOUNT) > 0) {
				indentPrintln(pw, WaveSpawnNode.TOTALCOUNT, node.getValue(WaveSpawnNode.TOTALCOUNT));
			}
			mapCopy.remove(WaveSpawnNode.TOTALCOUNT);
		}
		if(node.containsKey(WaveSpawnNode.MAXACTIVE)) {
			if((int) node.getValue(WaveSpawnNode.MAXACTIVE) > 0) {
				indentPrintln(pw, WaveSpawnNode.MAXACTIVE, node.getValue(WaveSpawnNode.MAXACTIVE));
			}
			mapCopy.remove(WaveSpawnNode.MAXACTIVE);
		}
		if(node.containsKey(WaveSpawnNode.SPAWNCOUNT)) {
			if((int) node.getValue(WaveSpawnNode.SPAWNCOUNT) > 0) {
				indentPrintln(pw, WaveSpawnNode.SPAWNCOUNT, node.getValue(WaveSpawnNode.SPAWNCOUNT));
			}
			mapCopy.remove(WaveSpawnNode.SPAWNCOUNT);
		}
		//TODO: truncate decimals as needed
		if(node.containsKey(WaveSpawnNode.WAITBEFORESTARTING)) {
			if((double) node.getValue(WaveSpawnNode.WAITBEFORESTARTING) > 0) {
				indentPrintln(pw, WaveSpawnNode.WAITBEFORESTARTING, node.getValue(WaveSpawnNode.WAITBEFORESTARTING));
			}
			mapCopy.remove(WaveSpawnNode.WAITBEFORESTARTING);
		}
		
		if(node.getBetweenDeaths()) { //boolean keys should also always be true or false
			if(node.containsKey(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH)) {
				indentPrintln(pw, WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH, node.getValue(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH));
			}
			mapCopy.remove(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH);
		}
		else {
			if(node.containsKey(WaveSpawnNode.WAITBETWEENSPAWNS) && (double) node.getValue(WaveSpawnNode.WAITBETWEENSPAWNS) > 0) {
				indentPrintln(pw, WaveSpawnNode.WAITBETWEENSPAWNS, node.getValue(WaveSpawnNode.WAITBETWEENSPAWNS));
			}
			mapCopy.remove(WaveSpawnNode.WAITBETWEENSPAWNS);
		}
		
		if(node.containsKey(WaveSpawnNode.TOTALCURRENCY)) {
			if((int) node.getValue(WaveSpawnNode.TOTALCURRENCY) > 0) {
				indentPrintln(pw, WaveSpawnNode.TOTALCURRENCY, node.getValue(WaveSpawnNode.TOTALCURRENCY));
			}
			mapCopy.remove(WaveSpawnNode.TOTALCURRENCY);
		}
		if(node.containsKey(WaveSpawnNode.WAITFORALLSPAWNED)) {
			indentPrintln(pw, WaveSpawnNode.WAITFORALLSPAWNED, node.getValue(WaveSpawnNode.WAITFORALLSPAWNED));
			mapCopy.remove(WaveSpawnNode.WAITFORALLSPAWNED);
		}
		if(node.containsKey(WaveSpawnNode.WAITFORALLDEAD)) {
			indentPrintln(pw, WaveSpawnNode.WAITFORALLDEAD, node.getValue(WaveSpawnNode.WAITFORALLDEAD));
			mapCopy.remove(WaveSpawnNode.WAITFORALLDEAD);
		}
		
		if((Boolean) node.getValue(WaveSpawnNode.SUPPORT)) {
			if(node.getSupportLimited()) {
				indentPrintln(pw, "Support Limited");
			}
			else {
				indentPrintln(pw, "Support true");
			}
		}
		mapCopy.remove(WaveSpawnNode.SUPPORT);
		
		if(node.containsKey(WaveSpawnNode.STARTWAVEOUTPUT)) {
			printRelay(pw, WaveSpawnNode.STARTWAVEOUTPUT, (RelayNode) node.getValue(WaveSpawnNode.STARTWAVEOUTPUT));
			mapCopy.remove(WaveSpawnNode.STARTWAVEOUTPUT);
		}
		
		if(mapCopy.containsKey(WaveSpawnNode.FIRSTSPAWNOUTPUT)) {
			printRelay(pw, WaveSpawnNode.FIRSTSPAWNOUTPUT, (RelayNode) node.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT));
			mapCopy.remove(WaveSpawnNode.FIRSTSPAWNOUTPUT);
		}	
		if(mapCopy.containsKey(WaveSpawnNode.LASTSPAWNOUTPUT)) {
			printRelay(pw, WaveSpawnNode.LASTSPAWNOUTPUT, (RelayNode) node.getValue(WaveSpawnNode.LASTSPAWNOUTPUT));
			mapCopy.remove(WaveSpawnNode.LASTSPAWNOUTPUT);
		}
		if(mapCopy.containsKey(WaveSpawnNode.DONEOUTPUT)) {
			printRelay(pw, WaveSpawnNode.DONEOUTPUT, (RelayNode) node.getValue(WaveSpawnNode.DONEOUTPUT));
			mapCopy.remove(WaveSpawnNode.DONEOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		if(spawner != null) {
			if(spawner.getClass() == TFBotNode.class) {
				printTFBot(pw, (TFBotNode) spawner, WaveSpawnNode.TFBOT);
			}
			else if(spawner.getClass() == TankNode.class) {
				printTank(pw, (TankNode) spawner);
			}
			else if(spawner.getClass() == SquadNode.class) {
				printSquadRC(pw, (SquadNode) spawner, WaveSpawnNode.SQUAD);
			}
			else if(spawner.getClass() == RandomChoiceNode.class) {
				printSquadRC(pw, (RandomChoiceNode) spawner, WaveSpawnNode.RANDOMCHOICE);
			}
		}	
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printTFBot(PrintWriter pw, TFBotNode node, String header) {
		indentPrintln(pw, header);
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		//these are hardcoded to force a specific print order
		//otherwise would need some conversion to a sortedmap + a comparator of some sort
		
		if(node.containsKey(TFBotNode.CLASSNAME)) {
			if(node.getValue(TFBotNode.CLASSNAME) != Classes.None) {
				indentPrintln(pw, TFBotNode.CLASSNAME, node.getValue(TFBotNode.CLASSNAME));
			}
			mapCopy.remove(TFBotNode.CLASSNAME);
		}
		
		if(node.containsKey(TFBotNode.CLASSICON)) { //todo: probably should check for empty icon strings
			String value = (String) node.getValue(TFBotNode.CLASSICON);
			if(!(value.equalsIgnoreCase(node.getValue(TFBotNode.CLASSNAME).toString()))) {
				//if classicon is not equal to the string version of its classname
				if(!value.equals("heavy") || !value.equals("demo")) { //dumb workaround to heavyweapons != heavy
					indentPrintln(pw, TFBotNode.CLASSICON, value);
				}	
			}
			mapCopy.remove(TFBotNode.CLASSICON);
		}
		
		if(node.containsKey(TFBotNode.NAME)) { 
			indentPrintln(pw, TFBotNode.NAME, node.getValue(TFBotNode.NAME));
			mapCopy.remove(TFBotNode.NAME);
		}
		
		if(node.containsKey(TFBotNode.SKILL)) {
			String skill = (String) node.getValue(TFBotNode.SKILL);
			//if(!skill.equals(TFBotNode.NOSKILL) && !skill.equals(TFBotNode.EASY)) {
			if(!skill.equals(TFBotNode.NOSKILL)) {
				indentPrintln(pw, TFBotNode.SKILL, skill);
			}
			mapCopy.remove(TFBotNode.SKILL);
		}
		
		if(node.containsKey(TFBotNode.WEAPONRESTRICT)) {
			if(!node.getValue(TFBotNode.WEAPONRESTRICT).equals(TFBotNode.ANY)) {
				indentPrintln(pw, TFBotNode.WEAPONRESTRICT, node.getValue(TFBotNode.WEAPONRESTRICT));
			}
			mapCopy.remove(TFBotNode.WEAPONRESTRICT);
		}	
		
		if(node.containsKey(TFBotNode.ITEM)) {
			for(String item : (String[]) node.getValue(TFBotNode.ITEM)) {
				if(item != null) {
					indentPrintln(pw, TFBotNode.ITEM, item);
				}
			}
			mapCopy.remove(TFBotNode.ITEM);
		}
		
		if(node.containsKey(TFBotNode.TAGS)) {
			for(Object tag : (List<Object>) mapCopy.remove(TFBotNode.TAGS)) {
				indentPrintln(pw, TFBotNode.TAGS, tag);
			}
		}
		
		if(node.containsKey(TFBotNode.CHARACTERATTRIBUTES)) {
			Map<String, Object> map = (Map<String, Object>) mapCopy.remove(TFBotNode.CHARACTERATTRIBUTES).get(0);
			
			printAttr(pw, TFBotNode.CHARACTERATTRIBUTES, map);
		}
		
		//this trainwreck needs rewritten
		if(node.containsKey(TFBotNode.ITEMATTRIBUTES)) {
			List<Object> mapList = mapCopy.remove(TFBotNode.ITEMATTRIBUTES);
			
			for(Object submap : mapList) {
				if(!((Map<String, Object>) submap).isEmpty()) {
					printAttr(pw, TFBotNode.ITEMATTRIBUTES, (Map<String, Object>) submap);
				}
			}
		}
		
		//TODO: eventchangeattributes
		
		printGenericMap(pw, mapCopy);
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printAttr(PrintWriter pw, String type, Map<String, Object> attrMap) {
		Object itemName = null;
		
		if(attrMap.isEmpty()) {
			return;
		}
		
		if(type == TFBotNode.CHARACTERATTRIBUTES) { //different subtree name
			indentPrintln(pw, TFBotNode.CHARACTERATTRIBUTES);
		}
		else {
			indentPrintln(pw, TFBotNode.ITEMATTRIBUTES);
			itemName = attrMap.remove(TFBotNode.ITEMNAME);
		}
		indentPrintln(pw, "{");
		indentCount++;
		
		if(itemName != null) {
			indentPrintln(pw, TFBotNode.ITEMNAME, itemName);
		}
		
		//fix this
		attrMap.forEach((k, v) -> {
			if(!k.equalsIgnoreCase(PETALTNAME)) {
				indentPrintln(pw, k, v);
			}
			else { //super super long 
				indentPrintln(pw, "counts as assister is some kind of pet this update is going to be awesome", v);
			}
		});
		//todo: paint conversions
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printTank(PrintWriter pw, TankNode node) {
		indentPrintln(pw, "Tank");
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		if(node.containsKey(TankNode.NAME)) {
			indentPrintln(pw, TankNode.NAME, node.getValue(TankNode.NAME));
			mapCopy.remove(TankNode.NAME);
		}
		
		if(node.containsKey(TankNode.HEALTH)) {
			indentPrintln(pw, TankNode.HEALTH, node.getValue(TankNode.HEALTH));
			mapCopy.remove(TankNode.HEALTH);
		}
		if((boolean) node.getValue(TankNode.SKIN)) {
			indentPrintln(pw, "Skin 1");
		}
		mapCopy.remove(TankNode.SKIN);
		
		if(node.containsKey(TankNode.SPEED) && (double) node.getValue(TankNode.SPEED) != Engipop.TANKDEFAULTSPEED) {
			indentPrintln(pw, TankNode.SPEED, node.getValue(TankNode.SPEED));
		}
		mapCopy.remove(TankNode.SPEED);
		
		if(node.containsKey(TankNode.STARTINGPATHTRACKNODE)) {
			indentPrintln(pw, TankNode.STARTINGPATHTRACKNODE, node.getValue(TankNode.STARTINGPATHTRACKNODE));
			mapCopy.remove(TankNode.STARTINGPATHTRACKNODE);
		}
		
		if(node.containsKey(TankNode.ONKILLEDOUTPUT)) {
			printRelay(pw, TankNode.ONKILLEDOUTPUT, (RelayNode) node.getValue(TankNode.ONKILLEDOUTPUT));
			mapCopy.remove(TankNode.ONKILLEDOUTPUT);
		}
		
		if(node.containsKey(TankNode.ONBOMBDROPPEDOUTPUT)) {
			printRelay(pw, TankNode.ONBOMBDROPPEDOUTPUT, (RelayNode) node.getValue(TankNode.ONBOMBDROPPEDOUTPUT));
			mapCopy.remove(TankNode.ONBOMBDROPPEDOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printSquadRC(PrintWriter pw, Node node, String type) {
		indentPrintln(pw, type);
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		for(Node spawner : node.getChildren()) {
			if(spawner.getClass() == TFBotNode.class) {
				printTFBot(pw, (TFBotNode) spawner, WaveSpawnNode.TFBOT);
			}
			else if(spawner.getClass() == TankNode.class) {
				printTank(pw, (TankNode) spawner);
			}
			else if(spawner.getClass() == SquadNode.class) {
				printSquadRC(pw, (SquadNode) spawner, WaveSpawnNode.SQUAD);
			}
			else if(spawner.getClass() == RandomChoiceNode.class) {
				printSquadRC(pw, (RandomChoiceNode) spawner, WaveSpawnNode.RANDOMCHOICE);
			}
		}
		printGenericMap(pw, mapCopy);
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void indentPrintln(PrintWriter pw, String text) {
		pw.println(indent.substring(0, indentCount + 1) + text);
		if(pw.checkError()) {
			System.out.println(text);
			pw.println("//an error occured!");
		}
	}
	
	private void indentPrintln(PrintWriter pw, String key, Object value) {
		//text.replace("\\", "/");
		String text = key.contains(" ") ? "\"" + key + "\" " : key + " ";
		
		if(value == null) {
			return;
		}
		
		if(value.getClass() == Double.class) { 
			if((Double) value % 1 != 0) {
				pw.println(indent.substring(0, indentCount + 1) + String.format(text + "%f", value));
			}
			else { //don't give whole numbers decimals
				pw.println(indent.substring(0, indentCount + 1) + text + ((Double) value).intValue());
			}
		}
		else if(value.getClass() == String.class && (((String) value).contains(" ") || ((String) value).contains(",") || 
				((String) value).contains("/"))) {
			pw.println(indent.substring(0, indentCount + 1) + text + "\"" + value + "\"");
		}
		else {
			pw.println(indent.substring(0, indentCount + 1) + text + value);
		}
		
		if(pw.checkError()) {
			//System.out.println(text);
			pw.println("//an error occured!");
		}
	}
}
