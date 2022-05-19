package engipop;

import java.io.*;
import java.util.*;

import engipop.Tree.*;
import engipop.Tree.TFBotNode.*;

public class TreeParse { //it is time to parse
	private static int indentCount = 0;
	private static String indent = "																				";
	//20 indents
	
	public static String petAltName = "assister in Pyrovision";
	//counts as assister is some kind of pet this update is going to be awesome
	
	public TreeParse() {
		
	}
	
	//sanity check pop, make sure no nulls and general silliness can get printed
	public static String treeCheck(Tree tree, window window) {
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
			stopCheck = checkWave((WaveNode) iterator.next(), waveNum, lastWave, window);
			waveNum++;
		}
		
		//error returned is the lowest offense (something related to spawner, then wavespawn, then wave)
		return stopCheck; 
	} 
	
	private static String checkWave(WaveNode wave, int waveNum, boolean lastWave, window window) {
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
				stopCheck = checkWaveSpawn((WaveSpawnNode) iterator.next(), waveNum, window);
			}
		}
		
		return stopCheck;
	}
	
	//check ws relays here
	private static String checkWaveSpawn(WaveSpawnNode ws, int waveNum, window window) {
		String stopCheck = ""; //may not even get into the checks in here
		String errorString = "Wavespawn " + ws.getName() + " in wave " + waveNum + " has an empty ";
		//name can be potentially empty here
		//warning no where
		//warning no counts
		//waitforall names
		
		//this gets set to the last offending relay
		if(ws.getStart() != null) {
			stopCheck = ws.getFirst().getTarget().isEmpty() ? errorString + "StartWaveOutput" : "";
		}
		if(ws.getFirst() != null) {
			stopCheck = ws.getFirst().getTarget().isEmpty() ? errorString + "FirstSpawnOutput" : "";
		}
		if(ws.getLast() != null) {
			stopCheck = ws.getFirst().getTarget().isEmpty() ? errorString + "LastSpawnOutput" : "";
		}
		if(ws.getDone() != null) {
			stopCheck = ws.getFirst().getTarget().isEmpty() ? errorString + "DoneOutput" : "";
		}
		
		if(stopCheck.isEmpty() && ws.hasChildren()) {
			if(ws.getSpawner().getClass() == TFBotNode.class) {
				checkBot((TFBotNode) ws.getSpawner());
			}
			else if(ws.getSpawner().getClass() == TankNode.class) {
				stopCheck = checkTank((TankNode) ws.getSpawner(), waveNum, ws.getName());
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
			indentPrintln(pw, "FixedRespawnWaveTime");
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
			indentPrintln(pw, "Advanced");
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
		
		indentPrintln(pw, "WaveSpawn");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!node.getName().isEmpty()) {
			indentPrintln(pw, "Name \"" + node.getName() + "\"");
		}
		if(node.getWhere() != null && !node.getWhere().isEmpty()){ //fix this once map support
			indentPrintln(pw, "Where " + node.getWhere());
		} 
		else if(spawner != null && spawner.getClass() != TankNode.class){ //no wheres for tanks
			indentPrintln(pw, "Where spawnbot");
		} // catch upstream
		if(node.getTotalCount() > 0) {
			indentPrint(pw, "TotalCount ");
			pw.println(node.getTotalCount());
		}
		if(node.getMaxActive() > 0) {
			indentPrint(pw, "MaxActive ");
			pw.println(node.getMaxActive());
		}
		if(node.getSpawnCount() > 1) {
			indentPrint(pw, "SpawnCount ");
			pw.println(node.getSpawnCount());
		}
		if(node.getBeforeStarting() > 0.0) {
			indentPrint(pw, "WaitBeforeStarting ");
			pw.println(node.getBeforeStarting());
		}
		if(node.getBetweenSpawns() > 0.0) {
			if(node.getBetweenDeaths()) {
				indentPrint(pw, "WaitBetweenSpawnsAfterDeath ");
			}
			else {
				indentPrint(pw, "WaitBetweenSpawns ");
			}
			pw.println(node.getBetweenSpawns());
		}
		if(node.getCurrency() > 0) {
			indentPrint(pw, "TotalCurrency ");
			pw.println(node.getCurrency());
		}
		if(node.getWaitSpawned() != null && !node.getWaitSpawned().equals("")) {
			indentPrintln(pw, "WaitForAllSpawned \"" + node.getWaitSpawned() + "\"");
		}
		if(node.getWaitDead() != null && !node.getWaitDead().equals("")) {
			indentPrintln(pw, "WaitForAllDead \"" + node.getWaitDead() + "\"");
		}
		if(node.getSupport()) {
			if(node.getSupportLimited()) {
				indentPrintln(pw, "Support limited");
			}
			else {
				indentPrintln(pw, "Support");
			}
		}
		
		if(node.getStart() != null) {
			printRelay(pw, "StartWaveOutput", node.getStart());
		}
		
		if(node.getFirst() != null) {
			printRelay(pw, "FirstSpawnOutput", node.getFirst());
		}	
		if(node.getLast() != null) {
			printRelay(pw, "LastSpawnOutput", node.getLast());
		}
		if(node.getDone() != null) {
			printRelay(pw, "DoneOutput", node.getDone());
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
		
		Iterator<TFBotKeys> iterator = node.getKeySet().iterator();
		while(iterator.hasNext()) {
			TFBotKeys k = iterator.next();
			Object value = node.getValue(k);
			switch (k) {
				case CLASSNAME:
					indentPrintln(pw, "Class " + value);
					break;
				case CLASSICON:
					if(!((String) value).equalsIgnoreCase(node.getValue(TFBotKeys.CLASSNAME).toString())) {
						//if classicon is not equal to the string version of its classname
						if(!value.equals("heavy")) { //dumb workaround to heavyweapons != heavy
							indentPrintln(pw, "ClassIcon " + value);
						}	
					}
					break;
				case NAME: 
					if(value != null && !((String) value).isEmpty()) {
						indentPrintln(pw, "Name \"" + value + "\"");
					}
					break;
				case SKILL:
					if(!value.equals(TFBotNode.EASY)) {
						indentPrintln(pw, "Skill " + value);
					}
					break;
				case WEAPONRESTRICT:
					if(!value.equals(TFBotNode.ANY)) {
						indentPrintln(pw, "WeaponRestrictions " + value);
					}
					break;
				case PRIMARY:
				case SECONDARY:
				case MELEE:
				case BUILDING:
				case HAT1:
				case HAT2:
				case HAT3: //these all fall under the same key of item
					if(value != null && !((String) value).isEmpty()) {
						indentPrintln(pw, "Item \"" + value + "\"");
					}
					break;
				default:
					break;
			}
		}
		if(node.getItemAttributeList() != null && !node.getItemAttributeList().isEmpty()) {
			Iterator<EngiPanel.ItemSlot> itemIterator = node.getItemAttributeList().keySet().iterator();
			
			while(itemIterator.hasNext()) {
				EngiPanel.ItemSlot slot = itemIterator.next();
				ItemAttributeNode attrNode = node.getItemAttributeList().get(slot);
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
		
		if(node.getTags().size() > 0) { //ez
			for(String tag : node.getTags()) {
				indentPrintln(pw, "Tag " + tag);
			}
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tfbot");
	}
	
	private static void printAttr(PrintWriter pw, EngiPanel.Classes tfClass, TFBotKeys slot, TFBotNode botNode, ItemAttributeNode attrNode) {
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
		
		
		attrNode.getMap().forEach((k, v) -> 
			indentPrintln(pw, "\"" + k + "\"" + " " + v)
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
