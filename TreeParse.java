package engipop;

import java.io.*;

import engipop.Tree.*;

public class TreeParse { //it is time to parse
	private static int indentCount = 0;
	private static String indent = "																				";
	//20 indents
	
	public TreeParse() {
		
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
		pw.println(indent.substring(0, indentCount + 1) + "Wave");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		
		//todo: whenever relays are implemented, change these
		pw.println(indent.substring(0, indentCount + 1) + "StartWaveOutput");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		pw.println(indent.substring(0, indentCount + 1) + "Target wave_start_relay");
		pw.println(indent.substring(0, indentCount + 1) + "Action Trigger");
		indentCount--;
		pw.println(indent.substring(0, indentCount + 1) + "}");
		
		pw.println(indent.substring(0, indentCount + 1) + "DoneOutput");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		pw.println(indent.substring(0, indentCount + 1) + "Target wave_finished_relay");
		pw.println(indent.substring(0, indentCount + 1) + "Action Trigger");
		indentCount--;
		pw.println(indent.substring(0, indentCount + 1) + "}");
		
		for(int i = 0; i < node.getChildren().size(); i++) {
			printWaveSpawn(pw, (WaveSpawnNode) node.getChildren().get(i));
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed wave");
	}
	
	private static void printWaveSpawn(PrintWriter pw, WaveSpawnNode node) { //a wavespawn	
		Node spawner = node.getSpawner();
		
		indentPrintln(pw, "WaveSpawn");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!node.getName().equals("")) {
			indentPrintln(pw, "Name \"" + node.getName() + "\"");
		}
		if(node.getWhere() != null && !node.getWhere().equals("")){ //fix this once map support
			indentPrintln(pw, "Where " + node.getWhere());
		} 
		else if(spawner.getClass() != TankNode.class){ //no wheres for tanks
			indentPrintln(pw, "Where spawnbot");
		}
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
		
		System.out.println("printed wavespawn");
		
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
		
		indentCount--;
		indentPrintln(pw, "}");
	}
	
	private static void printTFBot(PrintWriter pw, TFBotNode node) {
		indentPrintln(pw, "TFBot");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!node.getClassName().equals("")) {
			indentPrintln(pw, "Class " + node.getClassName());
		}
		if(node.getName()!= null && !node.getName().equals("")) {
			indentPrintln(pw, "Name \"" + node.getName() + "\"");
		}
		if(!node.getIcon().equals("") && !node.getIcon().equalsIgnoreCase(node.getClassName())) {
			//ignore default icons essentially
			//change if case is ignored upstream
			if(!node.getIcon().equals("heavy")) { //dumb workaround to heavyweapons != heavy
				indentPrintln(pw, "ClassIcon " + node.getIcon());
			}	
		}
		if(!node.getSkill().equals("Easy")) {
			indentPrintln(pw, "Skill " + node.getSkill());
		}
		if(!node.getWepRestrict().equals("Any")) {
			indentPrintln(pw, "WeaponRestrictions " + node.getWepRestrict());
		}
		if(node.getTags().size() > 0) {
			for(String tag : node.getTags()) {
				indentPrintln(pw, "Tag " + tag);
			}
		}
		
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed tfbot");
	}
	
	private static void printTank(PrintWriter pw, TankNode node) {
		//for now, print boss relay here
		pw.println(indent.substring(0, indentCount + 1) + "FirstSpawnOutput");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		pw.println(indent.substring(0, indentCount + 1) + "Target boss_spawn_relay");
		pw.println(indent.substring(0, indentCount + 1) + "Action Trigger");
		indentCount--;
		pw.println(indent.substring(0, indentCount + 1) + "}");
		
		indentPrintln(pw, "Tank");
		indentPrintln(pw, "{");
		indentCount++;
		
		indentPrintln(pw, "Name " + node.getName());
		if(node.getHealth() != Values.tankDefaultHealth) {
			indentPrint(pw, "Health ");
			pw.println(node.getHealth());
		}
		if(node.getSkin()) {
			indentPrintln(pw, "Skin 1");
		}
		if(node.getStartingPath() != null && !node.getStartingPath().equals("")) {
			indentPrintln(pw, "StatingPathTrackNode \"" + node.getStartingPath() + "\"");
		}
		
		pw.println(indent.substring(0, indentCount + 1) + "OnKilledOutput");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		pw.println(indent.substring(0, indentCount + 1) + "Target boss_dead_relay");
		pw.println(indent.substring(0, indentCount + 1) + "Action Trigger");
		indentCount--;
		pw.println(indent.substring(0, indentCount + 1) + "}");
		
		pw.println(indent.substring(0, indentCount + 1) + "OnBombDroppedOutput");
		pw.println(indent.substring(0, indentCount + 1) + "{");
		indentCount++;
		pw.println(indent.substring(0, indentCount + 1) + "boss_deploy_relay");
		pw.println(indent.substring(0, indentCount + 1) + "Action Trigger");
		indentCount--;
		pw.println(indent.substring(0, indentCount + 1) + "}");
		
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
