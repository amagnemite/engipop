package engipop;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import engipop.Tree.*;
import engipop.Tree.TFBotNode.*;
import engipop.Tree.WaveSpawnNode.WaveSpawnKeys;

public class TreeParse { //it is time to parse
	private static int indentCount = 0;
	private static String indent = "																				";
	//20 indents
	
	public static String petAltName = "assister in Pyrovision";
	//counts as assister is some kind of pet this update is going to be awesome
	
	public TreeParse() {
		
	}
	
	//sanity check pop, make sure no nulls and general silliness can get printed
	public static String treeCheck(Tree tree) {
		//int waveCount = tree.getRoot().getChildren().size();
		PopNode popNode = (PopNode) tree.getRoot();
		String stopCheck = "";
		ListIterator<Node> iterator = popNode.getChildren().listIterator();
		int waveNum = 1;
		
		if(popNode.getChildren().size() == 0) {
			stopCheck = "Popfile generation failed, no waves to generate";
		}
		
		//i hate it here
		while(stopCheck.isEmpty() && iterator.hasNext()) {
			boolean lastWave = waveNum < popNode.getChildren().size() ? false : true;
			//if it's less than size, then not lastwave otherwise it is
			stopCheck = checkWave((WaveNode) iterator.next(), waveNum, lastWave);
			waveNum++;
		}
		
		//error returned is the lowest offense (something related to spawner, then wavespawn, then wave)
		return stopCheck; 
	} 
	
	private static String checkWave(WaveNode wave, int waveNum, boolean lastWave) {
		String error = "Wave " + waveNum + " has an empty ";
		String stopCheck = "";

		//all waves need to have a start
		stopCheck = wave.getStart().isTargetEmptyOrNull() ? error + "StartWaveOutput" : "";
		if(stopCheck.isEmpty() && !lastWave) { //all waves need done except last wave
			stopCheck = wave.getDone().isTargetEmptyOrNull() ? error + "DoneOutput" : "";
			
		}
		if(stopCheck.isEmpty() && wave.getInit() != null) { 
			stopCheck = wave.getInit().isTargetEmptyOrNull() ? error + "InitWaveOutput" : "";
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
	private static String checkWaveSpawn(WaveSpawnNode ws, int waveNum) {
		String stopCheck = ""; //may not even get into the checks in here
		String errorString = "Wavespawn " + (String) ws.getValue(WaveSpawnKeys.NAME) + " in wave " + waveNum 
				+ " has an empty ";
		//name can be potentially empty here
		//warning no where
		//warning no counts
		//waitforall names
		
		//this gets set to the last offending relay
		if(ws.getValue(WaveSpawnKeys.WAVESTARTOUTPUT) != null) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnKeys.WAVESTARTOUTPUT)).getTarget().isEmpty()
					? errorString + "StartWaveOutput" : "";
		}
		if(ws.getValue(WaveSpawnKeys.FIRSTSPAWNOUTPUT) != null) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnKeys.FIRSTSPAWNOUTPUT)).getTarget().isEmpty()
					? errorString + "FirstSpawnOutput" : "";
		}
		if(ws.getValue(WaveSpawnKeys.LASTSPAWNOUTPUT) != null) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnKeys.LASTSPAWNOUTPUT)).getTarget().isEmpty()
					? errorString + "LastSpawnOutput" : "";
		}
		if(ws.getValue(WaveSpawnKeys.DONEOUTPUT) != null) {
			stopCheck = ((RelayNode) ws.getValue(WaveSpawnKeys.DONEOUTPUT)).getTarget().isEmpty()
					? errorString + "DoneOutput" : "";
		}
		
		//if wavespawn has a spawner that isn't a tank and it doesn't have a where 
		if(ws.hasChildren() && ws.getSpawnerType() != SpawnerType.TANK) {
			if(!ws.getMap().containsKey(WaveSpawnKeys.WHERE)) {
				stopCheck = errorString + "Where";
			}
		}
		
		if(stopCheck.isEmpty() && ws.hasChildren()) {
			if(ws.getSpawner().getClass() == TFBotNode.class) {
				checkBot((TFBotNode) ws.getSpawner());
			}
			else if(ws.getSpawner().getClass() == TankNode.class) {
				stopCheck = checkTank((TankNode) ws.getSpawner(), waveNum, (String) ws.getValue(WaveSpawnKeys.NAME));
			}
			else if(ws.getSpawner().getClass() == SquadNode.class || ws.getSpawner().getClass() == RandomChoiceNode.class) {
				checkSquadRandom(ws.getSpawner());
			}
		}
		
		return stopCheck;
	}
	
	//unlike wave/spawn, for now just strip out stock weapons here so they don't get unnecessarily printed
	private static void checkBot(TFBotNode bot) {
		EngiPanel.Classes botClass = (EngiPanel.Classes) bot.getValue(TFBotKeys.CLASSNAME);
		
		if(bot.getValue(TFBotKeys.PRIMARY) != null && bot.getValue(TFBotKeys.PRIMARY).equals(botClass.primary())) {
			bot.putKey(TFBotKeys.PRIMARY, "");
		}
		if(bot.getValue(TFBotKeys.SECONDARY) != null && bot.getValue(TFBotKeys.SECONDARY).equals(botClass.secondary())) {
			bot.putKey(TFBotKeys.SECONDARY, "");
		}
		if(bot.getValue(TFBotKeys.MELEE) != null && bot.getValue(TFBotKeys.MELEE).equals(botClass.melee())) {
			bot.putKey(TFBotKeys.MELEE, "");
		}
	} 
	
	private static String checkTank(TankNode tank, int waveNum, String wsName) {
		String stopCheck = "";
		String errorString = "Tank in wavespawn " + wsName + " in wave " + waveNum + " has an empty ";
		
		if(tank.getOnKilled() != null) {
			stopCheck = tank.getOnKilled().getTarget().isEmpty() ? errorString + "OnKilledOutput" : "";
		}
		if(tank.getOnBomb() != null) {
			stopCheck = tank.getOnBomb().getTarget().isEmpty() ? errorString + "OnBombDroppedOutput" : "";
		}
		
		return stopCheck;
	}
	
	private static String checkSquadRandom(Node squadRandom) { //generic here since don't know whether squad or random
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
	
	public static void parseTree(File file, Tree tree) {
		int waveCount = tree.getRoot().getChildren().size();
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
			printPopulation(pw, tree);
			
			//pain
			for(int i = 0; i < waveCount; i++) {
				printWave(pw, (WaveNode) tree.getRoot().getChildren().get(i));
			}
			indentCount--;
			pw.println("}");
			pw.close();
			
		}
		catch (FileNotFoundException e){
			
		}
		catch (IOException i) {
			
		}
	
	}
	
	private static void printPopulation(PrintWriter pw, Tree tree) { //population settings
		PopNode node = (PopNode) tree.getRoot();
			
		if(node.getCurrency() > 0) {
			indentPrint(pw, "StartingCurrency ");
			pw.println(node.getCurrency());
		}
		
		if(node.getWaveTime() != 10) {
			indentPrint(pw, "RespawnWaveTime ");
			pw.println(node.getWaveTime());
		}
		
		if(node.getEventPop()) { //double check these escapes
			indentPrintln(pw, "EventPopfile Halloween");
		}
		
		if(node.getFixedWaveTime()) {
			indentPrintln(pw, "FixedRespawnWaveTime 1");
		}
		
		if(node.getBusterDmg() != 3000) {
			indentPrint(pw, "AddSentryBusterWhenDamageDealtExceeds ");
			pw.println(node.getBusterDmg());
		}
		
		if(node.getBusterKills() != 15) {
			indentPrint(pw, "AddSentryBusterWhenKillCountExceeds ");
			pw.println(node.getBusterKills());
		}
		
		if(!node.getAtkInSpawn()) {
			indentPrintln(pw, "CanBotsAttackWhileInSpawnRoom no");
		} //double check if a nonexistent bots attack allows spawn attacks
		
		if(node.getAdvanced()) {
			indentPrintln(pw, "Advanced 1");
		}
		System.out.println("printed pop");
	}
	
	private static void printWave(PrintWriter pw, WaveNode node) { //a wave
		indentPrintln(pw, "Wave");
		indentPrintln(pw, "{");
		indentCount++;
		
		printRelay(pw, "StartWaveOutput", node.getStart());
		
		if(!node.getDone().isTargetEmptyOrNull()) {
			printRelay(pw, "DoneOutput", node.getDone());
		} //since tree was already validated upstream, only final wave should pass this case
		
		if(node.getInit() != null) {
			printRelay(pw, "InitWaveOutput", node.getInit());
		} 
		
		for(int i = 0; i < node.getChildren().size(); i++) {
			printWaveSpawn(pw, (WaveSpawnNode) node.getChildren().get(i));
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed wave");
	}
	
	private static void printRelay(PrintWriter pw, String name, RelayNode node) {
		indentPrintln(pw, name);
		indentPrintln(pw, "{");
		indentCount++;
		
		node.getMap().forEach((k, v) -> indentPrintln(pw, k + " " + "\"" + v + "\""));
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private static void printWaveSpawn(PrintWriter pw, WaveSpawnNode node) { //a wavespawn
		Node spawner = node.hasChildren() ? node.getSpawner() : null;
		//since spawnerless wavespawns are valid
		
		Map<WaveSpawnKeys, Object> map = node.getMap();
		
		indentPrintln(pw, "WaveSpawn");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(map.containsKey(WaveSpawnKeys.NAME)) {
			indentPrintln(pw, "Name \"" + map.get(WaveSpawnKeys.NAME) + "\"");
		}
		if(map.containsKey(WaveSpawnKeys.WHERE)) { //somewhat redundant check 
			if(spawner != null && spawner.getClass() != TankNode.class) { //no wheres for tanks
				indentPrintln(pw, "Where " + map.get(WaveSpawnKeys.WHERE));
			} //way this is written now is spawnerless wavespawns don't get wheres
		} //which may or may not be irrelevant
		//spinners don't allow empty values
		if((int) map.get(WaveSpawnKeys.TOTALCOUNT) > 0) {
			indentPrint(pw, "TotalCount ");
			pw.println(map.get(WaveSpawnKeys.TOTALCOUNT));
		}
		if((int) map.get(WaveSpawnKeys.MAXACTIVE) > 0) {
			indentPrint(pw, "MaxActive ");
			pw.println(map.get(WaveSpawnKeys.MAXACTIVE));
		}
		if((int) map.get(WaveSpawnKeys.SPAWNCOUNT) > 1) {
			indentPrint(pw, "SpawnCount ");
			pw.println(map.get(WaveSpawnKeys.SPAWNCOUNT));
		}
		if((double) map.get(WaveSpawnKeys.WAITBEFORESTARTING) > 0.0) {
			indentPrint(pw, "WaitBeforeStarting ");
			pw.println(map.get(WaveSpawnKeys.WAITBEFORESTARTING));
		}
		if((double) map.get(WaveSpawnKeys.WAITBETWEENSPAWNS) > 0.0) {
			if((boolean) map.get(WaveSpawnKeys.WAITBETWEENDEATHS)) { //boolean keys should also always be true or false
				indentPrint(pw, "WaitBetweenSpawnsAfterDeath ");
			}
			else {
				indentPrint(pw, "WaitBetweenSpawns ");
			}
			pw.println(map.get(WaveSpawnKeys.WAITBETWEENSPAWNS));
		}
		if((int) map.get(WaveSpawnKeys.TOTALCURRENCY) > 0) {
			indentPrint(pw, "TotalCurrency ");
			pw.println(map.get(WaveSpawnKeys.TOTALCURRENCY));
		}
		if(map.containsKey(WaveSpawnKeys.WAITFORALLSPAWNED)) {
			indentPrintln(pw, "WaitForAllSpawned \"" + map.get(WaveSpawnKeys.WAITFORALLSPAWNED) + "\"");
		}
		if(map.containsKey(WaveSpawnKeys.WAITFORALLDEAD)) {
			indentPrintln(pw, "WaitForAllDead \"" + map.get(WaveSpawnKeys.WAITFORALLDEAD) + "\"");
		}
		if((boolean) map.get(WaveSpawnKeys.SUPPORT)) {
			if((boolean) map.get(WaveSpawnKeys.SUPPORTLIMITED)) {
				indentPrintln(pw, "Support limited");
			}
			else {
				indentPrintln(pw, "Support");
			}
		}
		
		if(map.containsKey(WaveSpawnKeys.WAVESTARTOUTPUT)) {
			printRelay(pw, "StartWaveOutput", (RelayNode) map.get(WaveSpawnKeys.WAVESTARTOUTPUT));
		}
		
		if(map.containsKey(WaveSpawnKeys.FIRSTSPAWNOUTPUT)) {
			printRelay(pw, "FirstSpawnOutput", (RelayNode) map.get(WaveSpawnKeys.FIRSTSPAWNOUTPUT));
		}	
		if(map.containsKey(WaveSpawnKeys.LASTSPAWNOUTPUT)) {
			printRelay(pw, "LastSpawnOutput", (RelayNode) map.get(WaveSpawnKeys.LASTSPAWNOUTPUT));
		}
		if(map.containsKey(WaveSpawnKeys.DONEOUTPUT)) {
			printRelay(pw, "DoneOutput", (RelayNode) map.get(WaveSpawnKeys.DONEOUTPUT));
		}
		
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
	
	private static void printTFBot(PrintWriter pw, TFBotNode node) {
		indentPrintln(pw, "TFBot");
		indentPrintln(pw, "{");
		indentCount++;
		
		Map<TFBotKeys, Object> map = node.getMap();
		
		//these are hardcoded to force a specific print order
		//otherwise would need some conversion to a sortedmap + a comparator of some sort
		
		//if(map.containsKey(TFBotKeys.CLASSNAME)) { //always has a classname
		indentPrintln(pw, "Class " + map.get(TFBotKeys.CLASSNAME));
		//}
		
		if(map.containsKey(TFBotKeys.CLASSICON)) { //todo: probably should check for empty icon strings
			String value = (String) map.get(TFBotKeys.CLASSICON);
			if(!(value.equalsIgnoreCase(node.getValue(TFBotKeys.CLASSNAME).toString()))) {
				//if classicon is not equal to the string version of its classname
				if(!value.equals("heavy")) { //dumb workaround to heavyweapons != heavy
					indentPrintln(pw, "ClassIcon " + value);
				}	
			}
		}
		
		if(map.containsKey(TFBotKeys.NAME)) { 
			indentPrintln(pw, "Name \"" + map.get(TFBotKeys.NAME) + "\"");
		}
		
		//these two are always contained
		if(!map.get(TFBotKeys.SKILL).equals(TFBotNode.EASY)) {
			indentPrintln(pw, "Skill " + map.get(TFBotKeys.SKILL));
		}	
		
		if(!map.get(TFBotKeys.WEAPONRESTRICT).equals(TFBotNode.ANY)) {
			indentPrintln(pw, "WeaponRestrictions " + map.get(TFBotKeys.WEAPONRESTRICT));
		}	
		
		//past here more optional stuff
		if(map.containsKey(TFBotKeys.PRIMARY)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.PRIMARY) + "\"");
		}
		
		if(map.containsKey(TFBotKeys.SECONDARY)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.SECONDARY) + "\"");
		}
		
		if(map.containsKey(TFBotKeys.MELEE)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.MELEE) + "\"");
		}
		
		if(map.containsKey(TFBotKeys.HAT1)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.HAT1) + "\"");
		}
		
		if(map.containsKey(TFBotKeys.HAT2)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.HAT2) + "\"");
		}
		
		if(map.containsKey(TFBotKeys.HAT3)) {
			indentPrintln(pw, "Item \"" + map.get(TFBotKeys.HAT3) + "\"");
		}
		
		if(node.getTags().size() > 0) { //ez
			for(String tag : node.getTags()) {
				indentPrintln(pw, "Tag " + tag);
			}
		}
		
		if(node.getItemAttributeList() != null && !node.getItemAttributeList().isEmpty()) {
			Iterator<EngiPanel.ItemSlot> itemIterator = node.getItemAttributeList().keySet().iterator();
			
			while(itemIterator.hasNext()) {
				EngiPanel.ItemSlot slot = itemIterator.next();
				Map<String, String> attrNode = node.getItemAttributeList().get(slot);
				switch(slot) {
					case PRIMARY:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.PRIMARY, node, attrNode);
						break;
					case SECONDARY:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.SECONDARY, node, attrNode);
						break;
					case MELEE:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.MELEE, node, attrNode);
						break;
					case BUILDING:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.BUILDING, node, attrNode);
						break;	
					case CHARACTER:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.CHARACTER, node, attrNode);
						break;
					case HAT1:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.HAT1, node, attrNode);
						break;
					case HAT2:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.HAT2, node, attrNode);
						break;
					case HAT3:
						printAttr(pw, (EngiPanel.Classes) node.getValue(TFBotKeys.CLASSNAME), TFBotKeys.HAT3, node, attrNode);
						break;			
					case NONE: //shouldn't have this in actual node
					default:
						break;				
				}
			}
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tfbot");
	}
	
	private static void printAttr(PrintWriter pw, EngiPanel.Classes tfClass, TFBotKeys slot, TFBotNode botNode, Map<String, String> attrNode) {
		if(slot.equals(TFBotKeys.CHARACTER)) { //different subtree name
			indentPrintln(pw, "CharacterAttributes");
		}
		else {
			indentPrintln(pw, "ItemAttributes");
		}
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!slot.equals(TFBotKeys.CHARACTER)) {
			if(botNode.getValue(slot) == null || ((String) botNode.getValue(slot)).isEmpty()) { //if the slot was stripped out by the precheck
				switch (slot) {
					case PRIMARY:
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
				indentPrintln(pw, "ItemName \"" + botNode.getValue(slot) + "\"");
			}
		}
		
		attrNode.forEach((k, v) -> {
			if(!v.equals(petAltName)) {
				indentPrintln(pw, "\"" + k + "\"" + " " + v);
			}
			else { //super super long 
				indentPrintln(pw, "\"" + k + "\"" + " " + 
					"counts as assister is some kind of pet this update is going to be awesome");
			}
		}
			
		);
		//todo: paint conversions
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private static void printTank(PrintWriter pw, TankNode node) {
		indentPrintln(pw, "Tank");
		indentPrintln(pw, "{");
		indentCount++;
		
		indentPrintln(pw, "Name " + node.getName());
		if(node.getHealth() != EngiPanel.tankDefaultHealth) {
			indentPrint(pw, "Health ");
			pw.println(node.getHealth());
		}
		if(node.getSkin()) {
			indentPrintln(pw, "Skin 1");
		}
		if(node.getStartingPath() != null && !node.getStartingPath().equals("")) {
			indentPrintln(pw, "StatingPathTrackNode \"" + node.getStartingPath() + "\"");
		}
		
		if(node.getOnKilled() != null) {
			printRelay(pw, "OnKilledOutput", node.getOnKilled());
		}
		
		if(node.getOnBomb() != null) {
			printRelay(pw, "OnBombOutput", node.getOnBomb());
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tank");
	}
	
	private static void printSquad(PrintWriter pw, SquadNode node) {
		indentPrintln(pw, "Squad");
		indentPrintln(pw, "{");
		indentCount++;
		
		for(Node n : node.getChildren()) {
			printTFBot(pw, (TFBotNode) n);
		}
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed squad");
	}
	
	private static void printRandom(PrintWriter pw, RandomChoiceNode node) {
		indentPrintln(pw, "RandomChoice");
		indentPrintln(pw, "{");
		indentCount++;
		
		for(Node n : node.getChildren()) {
			printTFBot(pw, (TFBotNode) n);
		}
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed random");
	}
	
	private static void indentPrint(PrintWriter pw, String text) {
		pw.print(indent.substring(0, indentCount + 1) + text);
	}
	
	private static void indentPrintln(PrintWriter pw, String text) {
		pw.println(indent.substring(0, indentCount + 1) + text);
	}
}
