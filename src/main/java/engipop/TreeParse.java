package engipop;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import engipop.EngiPanel.Classes;
import engipop.EngiPanel.ItemSlot;
import engipop.Node.*;
import engipop.Node.TFBotNode.*;

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
	
	private String checkWave(WaveNode wave, int waveNum, boolean lastWave) {
		String error = "Wave " + waveNum + " has an empty ";
		String stopCheck = "";

		//all waves need to have a start
		
		stopCheck = ((RelayNode) wave.getValue(WaveNode.STARTWAVEOUTPUT)).containsKey(RelayNode.TARGET) ? "" : error + "StartWaveOutput";
		if(stopCheck.isEmpty() && !lastWave) { //all waves need done except last wave
			stopCheck = ((RelayNode) wave.getValue(WaveNode.DONEOUTPUT)).containsKey(RelayNode.TARGET) ? "" : error + "DoneOutput";
			
		}
		if(stopCheck.isEmpty() && wave.containsKey(WaveNode.INITWAVEOUTPUT)) { 
			stopCheck = ((RelayNode) wave.getValue(WaveNode.INITWAVEOUTPUT)).containsKey(RelayNode.TARGET) ? "" : error + "InitWaveOutput";
		}
		
		if(stopCheck.isEmpty()) {
			ListIterator<Node> iterator = wave.getChildren().listIterator();
			while(stopCheck.isEmpty() && iterator.hasNext()) {
				stopCheck = checkWaveSpawn((WaveSpawnNode) iterator.next(), waveNum);
			}
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
		
		//if wavespawn has a spawner that isn't a tank and it doesn't have a where 
		if(ws.hasChildren() && ws.getSpawnerType() != SpawnerType.TANK) {
			if(!ws.getMap().containsKey(WaveSpawnNode.WHERE)) {
				stopCheck = errorString + "Where";
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
		EngiPanel.Classes botClass = (EngiPanel.Classes) bot.getValue(TFBotNode.CLASSNAME);
		
		if(bot.containsKey(TFBotNode.ITEM)) {
			List<Object> itemList = bot.getListValue(TFBotNode.ITEM);
			
			itemList.remove(botClass.primary());
			itemList.remove(botClass.secondary());
			itemList.remove(botClass.melee());
		}
		/*
		if(itemList.get(ItemSlot.PRIMARY.getSlot()).equals(botClass.primary())) {
			itemList.add(ItemSlot.PRIMARY.getSlot(), null);
		}
		if(itemList.get(ItemSlot.SECONDARY.getSlot()).equals(botClass.secondary())) {
			itemList.add(ItemSlot.SECONDARY.getSlot(), null);
		}
		if(itemList.get(ItemSlot.MELEE.getSlot()).equals(botClass.melee())) {
			itemList.add(ItemSlot.MELEE.getSlot(), null);
		}
		*/
		//might need to strip building here
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
				checkBot((TFBotNode) node);
			}
		}
		
		return stopCheck;
	}
	
	public void parseTree(File file, PopNode root) {
		int waveCount = root.getChildren().size();
		FileWriter fw;
		PrintWriter pw;
		indentCount = 0;
		
		try { //make these work so parse doesn't happen if error
			fw = new FileWriter(file);
			pw = new PrintWriter(fw, true);
			
			pw.println("//made with engipop");
			pw.println("WaveSchedule");
			pw.println("{");
			
			//indentCount++;
			printPopulation(pw, root);
			
			//pain
			for(int i = 0; i < waveCount; i++) {
				printWave(pw, (WaveNode) root.getChildren().get(i));
			}
			//indentCount--;
			pw.println("}");
			pw.close();
			fw.close();
			
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
					indentPrintln(pw, entry.getKey() + " " + subentry);
				}
			}
		}
	}

	private void printPopulation(PrintWriter pw, PopNode root) { //population settings
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(root.getMap()); //do this to prevent case issues
		
		if(root.containsKey(PopNode.STARTINGCURRENCY)) {
			indentPrintln(pw, "StartingCurrency " + root.getValue(PopNode.STARTINGCURRENCY));
			mapCopy.remove(PopNode.STARTINGCURRENCY);
		}
		
		if(root.containsKey(PopNode.RESPAWNWAVETIME)) {
			if((int) root.getValue(PopNode.RESPAWNWAVETIME) != 10) {
				indentPrintln(pw, "RespawnWaveTime " + root.getValue(PopNode.RESPAWNWAVETIME));
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
			if((int) root.getValue(PopNode.BUSTERDAMAGE) != EngiPanel.BUSTERDEFAULTDMG) {
				indentPrintln(pw, "AddSentryBusterWhenDamageDealtExceeds " + root.getValue(PopNode.BUSTERDAMAGE));
			}
			mapCopy.remove(PopNode.BUSTERDAMAGE);
		}
		
		if(root.containsKey(PopNode.BUSTERKILLS)) {
			if((int) root.getValue(PopNode.BUSTERKILLS) != EngiPanel.BUSTERDEFAULTKILLS) {
				indentPrintln(pw, "AddSentryBusterWhenKillCountExceeds " + root.getValue(PopNode.BUSTERKILLS));
			}
			mapCopy.remove(PopNode.BUSTERKILLS);
		}
		
		if(!(boolean) root.getValue(PopNode.BOTSATKINSPAWN)) {
			indentPrintln(pw, "CanBotsAttackWhileInSpawnRoom no");
		} //double check if a nonexistent bots attack allows spawn attacks
		mapCopy.remove(PopNode.BOTSATKINSPAWN);
		
		if(root.containsKey(PopNode.MISSION)) {
			
		}
		
		if(root.containsKey(PopNode.TEMPLATE)) {
			
		}
		
		printGenericMap(pw, mapCopy);
		
		System.out.println("printed pop");
	}
	
	private void printWave(PrintWriter pw, WaveNode node) { //a wave
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		indentPrintln(pw, "Wave");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(node.containsKey(WaveNode.STARTWAVEOUTPUT)) {
			printRelay(pw, "StartWaveOutput", (RelayNode) node.getValue(WaveNode.STARTWAVEOUTPUT));
			mapCopy.remove(WaveNode.STARTWAVEOUTPUT);
		}
		
		if(node.containsKey(WaveNode.DONEOUTPUT)) {
			printRelay(pw, "DoneOutput", (RelayNode) node.getValue(WaveNode.DONEOUTPUT));
			mapCopy.remove(WaveNode.DONEOUTPUT);
		} //since tree was already validated upstream, only final wave should pass this case
		
		if(node.containsKey(WaveNode.INITWAVEOUTPUT)) {
			printRelay(pw, "InitWaveOutput", (RelayNode) node.getValue(WaveNode.INITWAVEOUTPUT));
			mapCopy.remove(WaveNode.INITWAVEOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		for(int i = 0; i < node.getChildren().size(); i++) {
			printWaveSpawn(pw, (WaveSpawnNode) node.getChildren().get(i));
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed wave");
	}
	
	private void printRelay(PrintWriter pw, String name, RelayNode node) {
		indentPrintln(pw, name);
		indentPrintln(pw, "{");
		indentCount++;
		
		//this will lead to some very unhinged formatting for vscripts but that's a later problem
		node.getMap().forEach((k, v) -> indentPrintln(pw, k + " " + "\"" + v.get(0) + "\""));
		
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
			indentPrintln(pw, "Name \"" + node.getValue(WaveSpawnNode.NAME) + "\"");
			mapCopy.remove(WaveSpawnNode.NAME);
		}
		if(node.containsKey(WaveSpawnNode.WHERE)) { //somewhat redundant check 
			if(spawner != null && spawner.getClass() != TankNode.class) { //no wheres for tanks
				for(Object where : mapCopy.remove(WaveSpawnNode.WHERE)) {
					indentPrintln(pw, "Where " + where);
				}
			} //way this is written now is spawnerless wavespawns don't get wheres
		} //which may or may not be irrelevant
		
		//spinners don't allow empty values
		if(node.containsKey(WaveSpawnNode.TOTALCOUNT)) {
			if((int) node.getValue(WaveSpawnNode.TOTALCOUNT) > 0) {
				indentPrintln(pw, "TotalCount " + node.getValue(WaveSpawnNode.TOTALCOUNT));
			}
			mapCopy.remove(WaveSpawnNode.TOTALCOUNT);
		}
		if(node.containsKey(WaveSpawnNode.MAXACTIVE)) {
			if((int) node.getValue(WaveSpawnNode.MAXACTIVE) > 0) {
				indentPrintln(pw, "MaxActive " + node.getValue(WaveSpawnNode.MAXACTIVE));
			}
			mapCopy.remove(WaveSpawnNode.MAXACTIVE);
		}
		if(node.containsKey(WaveSpawnNode.SPAWNCOUNT)) {
			if((int) node.getValue(WaveSpawnNode.SPAWNCOUNT) > 1) {
				indentPrintln(pw, "SpawnCount " + node.getValue(WaveSpawnNode.SPAWNCOUNT));
			}
			mapCopy.remove(WaveSpawnNode.SPAWNCOUNT);
		}
		//if((double) node.getValueSingular(WaveSpawnNode.WAITBEFORESTARTING) > 0.0) {
		if(node.containsKey(WaveSpawnNode.WAITBEFORESTARTING)) {
			indentPrintln(pw, "WaitBeforeStarting " + node.getValue(WaveSpawnNode.WAITBEFORESTARTING));
			mapCopy.remove(WaveSpawnNode.WAITBEFORESTARTING);
		}
		//if((double) node.getValueSingular(WaveSpawnNode.WAITBETWEENSPAWNS) > 0.0) {
		if(node.containsKey(WaveSpawnNode.WAITBETWEENSPAWNS)) {
			if(node.getBetweenDeaths()) { //boolean keys should also always be true or false
				indentPrintln(pw, "WaitBetweenSpawnsAfterDeath " + node.getValue(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH));
				mapCopy.remove(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH);
			}
			else {
				indentPrintln(pw, "WaitBetweenSpawns " + node.getValue(WaveSpawnNode.WAITBETWEENSPAWNS));
				mapCopy.remove(WaveSpawnNode.WAITBETWEENSPAWNS);
			}
		}
		
		if(node.containsKey(WaveSpawnNode.TOTALCURRENCY)) {
			if((int) node.getValue(WaveSpawnNode.TOTALCURRENCY) > 0) {
				indentPrintln(pw, "TotalCurrency " + node.getValue(WaveSpawnNode.TOTALCURRENCY));
			}
			mapCopy.remove(WaveSpawnNode.TOTALCURRENCY);
		}
		if(node.containsKey(WaveSpawnNode.WAITFORALLSPAWNED)) {
			indentPrintln(pw, "WaitForAllSpawned \"" + node.getValue(WaveSpawnNode.WAITFORALLSPAWNED) + "\"");
			mapCopy.remove(WaveSpawnNode.WAITFORALLSPAWNED);
		}
		if(node.containsKey(WaveSpawnNode.WAITFORALLDEAD)) {
			indentPrintln(pw, "WaitForAllDead \"" + node.getValue(WaveSpawnNode.WAITFORALLDEAD) + "\"");
			mapCopy.remove(WaveSpawnNode.WAITFORALLDEAD);
		}
		
		if(node.containsKey(WaveSpawnNode.SUPPORT)) {
			if(node.getSupportLimited()) {
				indentPrintln(pw, "Support Limited");
			}
			else {
				indentPrintln(pw, "Support true");
			}
			mapCopy.remove(WaveSpawnNode.SUPPORT);
		}
		
		if(node.containsKey(WaveSpawnNode.STARTWAVEOUTPUT)) {
			printRelay(pw, "StartWaveOutput", (RelayNode) node.getValue(WaveSpawnNode.STARTWAVEOUTPUT));
			mapCopy.remove(WaveSpawnNode.STARTWAVEOUTPUT);
		}
		
		if(mapCopy.containsKey(WaveSpawnNode.FIRSTSPAWNOUTPUT)) {
			printRelay(pw, "FirstSpawnOutput", (RelayNode) node.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT));
			mapCopy.remove(WaveSpawnNode.FIRSTSPAWNOUTPUT);
		}	
		if(mapCopy.containsKey(WaveSpawnNode.LASTSPAWNOUTPUT)) {
			printRelay(pw, "LastSpawnOutput", (RelayNode) node.getValue(WaveSpawnNode.LASTSPAWNOUTPUT));
			mapCopy.remove(WaveSpawnNode.LASTSPAWNOUTPUT);
		}
		if(mapCopy.containsKey(WaveSpawnNode.DONEOUTPUT)) {
			printRelay(pw, "DoneOutput", (RelayNode) node.getValue(WaveSpawnNode.DONEOUTPUT));
			mapCopy.remove(WaveSpawnNode.DONEOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		System.out.println("printed wavespawn");
		
		if(spawner != null) {
			if(spawner.getClass() == TFBotNode.class) {
				printTFBot(pw, (TFBotNode) spawner);
			}
			else if(spawner.getClass() == TankNode.class) {
				printTank(pw, (TankNode) spawner);
			}
			else if(spawner.getClass() == SquadNode.class) {
				printSquad(pw, (SquadNode) spawner);
			}
			else if(spawner.getClass() == RandomChoiceNode.class) {
				printRandom(pw, (RandomChoiceNode) spawner);
			}
		}	
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private void printTFBot(PrintWriter pw, TFBotNode node) {
		indentPrintln(pw, "TFBot");
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		//these are hardcoded to force a specific print order
		//otherwise would need some conversion to a sortedmap + a comparator of some sort
		
		if(node.containsKey(TFBotNode.CLASSNAME)) {
			if(node.getValue(TFBotNode.CLASSNAME) != Classes.None) {
				indentPrintln(pw, "Class " + node.getValue(TFBotNode.CLASSNAME));
			}
			mapCopy.remove(TFBotNode.CLASSNAME);
		}
		
		if(node.containsKey(TFBotNode.CLASSICON)) { //todo: probably should check for empty icon strings
			String value = (String) node.getValue(TFBotNode.CLASSICON);
			if(!(value.equalsIgnoreCase(node.getValue(TFBotNode.CLASSNAME).toString()))) {
				//if classicon is not equal to the string version of its classname
				if(!value.equals("heavy")) { //dumb workaround to heavyweapons != heavy
					indentPrintln(pw, "ClassIcon " + value);
				}	
			}
			mapCopy.remove(TFBotNode.CLASSICON);
		}
		
		if(node.containsKey(TFBotNode.NAME)) { 
			indentPrintln(pw, "Name \"" + node.getValue(TFBotNode.NAME) + "\"");
			mapCopy.remove(TFBotNode.NAME);
		}
		
		if(node.containsKey(TFBotNode.SKILL)) {
			String skill = (String) node.getValue(TFBotNode.SKILL);
			if(!skill.equals(TFBotNode.NOSKILL) && !skill.equals(TFBotNode.EASY)) {
				indentPrintln(pw, "Skill " + skill);
			}
			mapCopy.remove(TFBotNode.SKILL);
		}	
		
		if(node.containsKey(TFBotNode.WEAPONRESTRICT)) {
			if(!node.getValue(TFBotNode.WEAPONRESTRICT).equals(TFBotNode.ANY)) {
				indentPrintln(pw, "WeaponRestrictions " + node.getValue(TFBotNode.WEAPONRESTRICT));
			}
			mapCopy.remove(TFBotNode.WEAPONRESTRICT);
		}	
		
		if(node.containsKey(TFBotNode.ITEM)) {
			for(Object item : (List<Object>) mapCopy.remove(TFBotNode.ITEM)) {
				indentPrintln(pw, "Item \"" + item + "\"");
			}
		}
		
		if(node.containsKey(TFBotNode.TAGS)) {
			for(Object tag : (List<Object>) mapCopy.remove(TFBotNode.TAGS)) {
				indentPrintln(pw, "Tag " + tag);
			}
		}
		
		//this trainwreck needs rewritten
		if(node.containsKey(TFBotNode.ITEMATTRIBUTES)) {
			List<Object> mapList = mapCopy.remove(TFBotNode.ITEMATTRIBUTES);
			
			for(Object submap : mapList ) {
				printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotNode.CLASSNAME), 
						mapList.indexOf((Map<String, List<Object>>) submap), node, (Map<String, List<Object>>) submap);
			}
		}
		
		//TODO: eventchangeattributes
		
		printGenericMap(pw, mapCopy);
		
		/*
		if(node.containsKey(TFBotNode.ITEMATTRIBUTES)) {
			Iterator<String> itemIterator = ((Map<String, Object[]>) mapCopy.remove(TFBotNode.ITEMATTRIBUTES)[0]).keySet().iterator();
			
			while(itemIterator.hasNext()) {
				EngiPanel.ItemSlot slot = itemIterator.next();
				Map<String, String> attrNode = node.getItemAttributeList().get(slot);
				switch(slot) {
					case PRIMARY:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.PRIMARY, node, attrNode);
						break;
					case SECONDARY:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.SECONDARY, node, attrNode);
						break;
					case MELEE:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.MELEE, node, attrNode);
						break;
					case BUILDING:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.BUILDING, node, attrNode);
						break;	
					case CHARACTER:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.CHARACTER, node, attrNode);
						break;
					case HAT1:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.HAT1, node, attrNode);
						break;
					case HAT2:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.HAT2, node, attrNode);
						break;
					case HAT3:
						printAttr(pw, (EngiPanel.Classes) node.getValueSingular(TFBotNode.CLASSNAME), TFBotNode.HAT3, node, attrNode);
						break;			
					case NONE: //shouldn't have this in actual node
					default:
						break;				
				}
			}
		} */
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tfbot");
	}
	
	private void printAttr(PrintWriter pw, EngiPanel.Classes tfClass, int slot, TFBotNode botNode, Map<String, List<Object>> attrNode) {
		Object itemName = null;
		
		if(slot == ItemSlot.CHARACTER.getSlot()) { //different subtree name
			indentPrintln(pw, "CharacterAttributes");
		}
		else {
			indentPrintln(pw, "ItemAttributes");
			itemName = attrNode.get(TFBotNode.ITEMNAME).get(0);
		}
		indentPrintln(pw, "{");
		indentCount++;
		
		if(itemName != null) {
			indentPrintln(pw, "ItemName \"" + itemName + "\"");
		}
		
		/*
		if(slot != ItemSlot.CHARACTER.getSlot()) {
			if(botNode.getValueSingular(slot) == null || ((String) botNode.getValueSingular(slot)).isEmpty()) { //if the slot was stripped out by the precheck
				switch (slot) {
					case ItemSlot.PRIMARY.getSlot():
						indentPrintln(pw, "ItemName \"" + tfClass.primary() + "\"");
						break;
					case SECONDARY:
						indentPrintln(pw, "ItemName \"" + tfClass.secondary() + "\"");
						break;
					case MELEE:
						indentPrintln(pw, "ItemName \"" + tfClass.melee() + "\"");
						break;
					case BUILDING:
						indentPrintln(pw, "ItemName \"" + tfClass.building() + "\"");
						break;
					default:
						break;
				}
			}
			else {
				indentPrintln(pw, "ItemName \"" + botNode.getValueSingular(slot) + "\"");
			}
		} */
		
		//fix this
		attrNode.forEach((k, v) -> {
			if(!k.equals(PETALTNAME) && 
					!(((String) k).equalsIgnoreCase((String) attrNode.get(TFBotNode.ITEMNAME).get(0))) ) {
				indentPrintln(pw, "\"" + k + "\"" + " " + v.get(0));
			}
			else { //super super long 
				indentPrintln(pw, "\"counts as assister is some kind of pet this update is going to be awesome\" " + v.get(0));
			}
		}
			
		);
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
			indentPrintln(pw, "Name " + node.getValue(TankNode.NAME));
			mapCopy.remove(TankNode.NAME);
		}
		
		if(node.containsKey(TankNode.HEALTH)) {
			indentPrintln(pw, "Health " + node.getValue(TankNode.HEALTH));
			mapCopy.remove(TankNode.HEALTH);
		}
		if((boolean) node.containsKey(TankNode.SKIN)) {
			indentPrintln(pw, "Skin 1");
		}
		mapCopy.remove(TankNode.SKIN);
		
		if(node.containsKey(TankNode.STARTINGPATHTRACKNODE)) {
			indentPrintln(pw, "StatingPathTrackNode \"" + node.getValue(TankNode.STARTINGPATHTRACKNODE) + "\"");
			mapCopy.remove(TankNode.STARTINGPATHTRACKNODE);
		}
		
		if(node.containsKey(TankNode.ONKILLEDOUTPUT)) {
			printRelay(pw, "OnKilledOutput", (RelayNode) node.getValue(TankNode.ONKILLEDOUTPUT));
			mapCopy.remove(TankNode.ONKILLEDOUTPUT);
		}
		
		if(node.containsKey(TankNode.ONBOMBDROPPEDOUTPUT)) {
			printRelay(pw, "OnBombOutput", (RelayNode) node.getValue(TankNode.ONBOMBDROPPEDOUTPUT));
			mapCopy.remove(TankNode.ONBOMBDROPPEDOUTPUT);
		}
		
		printGenericMap(pw, mapCopy);
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tank");
	}
	
	private void printSquad(PrintWriter pw, SquadNode node) {
		indentPrintln(pw, "Squad");
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		for(Node n : node.getChildren()) {
			printTFBot(pw, (TFBotNode) n);
		}
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed squad");
	}
	
	private void printRandom(PrintWriter pw, RandomChoiceNode node) {
		indentPrintln(pw, "RandomChoice");
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<String, List<Object>> mapCopy = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		mapCopy.putAll(node.getMap());
		
		for(Node n : node.getChildren()) {
			printTFBot(pw, (TFBotNode) n);
		}
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed random");
	}
	
	private void indentPrint(PrintWriter pw, String text) {
		pw.print(indent.substring(0, indentCount + 1) + text);
		if(pw.checkError()) {
			System.out.println(text);
			pw.println("//an error occured!");
		}
	}
	
	private void indentPrintln(PrintWriter pw, String text) {
		pw.println(indent.substring(0, indentCount + 1) + text);
		if(pw.checkError()) {
			System.out.println(text);
			pw.println("//an error occured!");
		}
	}
}
