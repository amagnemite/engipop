package engipop;

import java.io.*;

import engipop.Tree.*;

public class TreeParse { //it is time to parse
	private static int indentCount  = 0;
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
			pw.print(indent.substring(0, indentCount + 1) + "StartingCurrency ");
			pw.println(node.getCurrency());
		}
		
		if(node.getWaveTime() != 10) {
			pw.print(indent.substring(0, indentCount + 1) + "RespawnWaveTime ");
			pw.println(node.getWaveTime());
		}
		
		if(node.getEventPop()) { //double check these escapes
			pw.println(indent.substring(0, indentCount + 1) + "EventPopfile Halloween");
		}
		
		if(node.getFixedWaveTime()) {
			pw.println(indent.substring(0, indentCount + 1) + "FixedRespawnWaveTime");
		}
		
		if(node.getBusterDmg() != 3000) {
			pw.print(indent.substring(0, indentCount + 1) + "AddSentryBusterWhenDamageDealtExceeds ");
			pw.println(node.getBusterDmg());
		}
		
		if(node.getBusterKills() != 15) {
			pw.print(indent.substring(0, indentCount + 1) + "AddSentryBusterWhenKillCountExceeds ");
			pw.println(node.getBusterKills());
		}
		
		if(!node.getAtkInSpawn()) {
			pw.println(indent.substring(0, indentCount + 1) + "CanBotsAttackWhileInSpawnRoom no");
		} //double check if a nonexistent bots attack allows spawn attacks
		
		if(node.getAdvanced()) {
			pw.println(indent.substring(0, indentCount + 1) + "Advanced");
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
		indentPrintln(pw, "WaveSpawn");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!node.getName().equals("")) {
			indentPrintln(pw, "Name \"" + node.getName() + "\"");
		}
		if(node.getWhere() != null) { //fix this once map support
			indentPrintln(pw, "Where " + node.getWhere());
		} 
		else {
			indentPrintln(pw, "Where spawnbot");
		}
		if(node.getTotalCount() > 0) {
			indentPrint(pw, "TotalCount ");
			pw.println(node.getTotalCount());
		}
		if(node.getMaxActive() < 999) { //very silly check since maxactive can't go above 22, should probably remove
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
		if(!node.getWaitSpawned().equals("")) {
			indentPrintln(pw, "WaitForAllSpawned \"" + node.getWaitSpawned() + "\"");
		}
		if(!node.getWaitDead().equals("")) {
			indentPrintln(pw, "WaitForAllDead \"" + node.getWaitDead() + "\"");
		}
		
		printTFBot(pw, (TFBotNode) node.getChildren().get(0)); //obviously fix this depending on subtree type
		indentCount--;
		indentPrintln(pw, "}");
		System.out.println("printed wavespawn");
	}
	
	private static void printTFBot(PrintWriter pw, TFBotNode node) {
		indentPrintln(pw, "TFBot");
		indentPrintln(pw, "{");
		indentCount++;
		
		if(!node.getClassName().equals("")) {
			indentPrintln(pw, "Class " + node.getClassName());
		}
		if(!node.getName().equals("")) {
			indentPrintln(pw, "Name \"" + node.getName() + "\"");
		}
		if(!node.getIcon().equals("") && node.getIcon().equalsIgnoreCase(node.getClassName())) {
			//ignore default icons essentially
			//change if case is ignored upstream
			indentPrintln(pw, "ClassIcon" + node.getIcon());
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
	
	private static void indentPrint(PrintWriter pw, String text) {
		pw.print(indent.substring(0, indentCount + 1) + text);
	}
	
	private static void indentPrintln(PrintWriter pw, String text) {
		pw.println(indent.substring(0, indentCount + 1) + text);
	}
}
