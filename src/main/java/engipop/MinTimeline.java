package engipop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import engipop.Node.WaveSpawnNode;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

public class MinTimeline {
	private static Double TANK = -0.1337;
	private static Double BOSS = -0.2401;
	
	public void parsePopulation(File file) {
		VDFNode root = null;
		int waveCount = 0;
		int wsCount = 1;
		Map<String, Double> lengthMap = new TreeMap<String, Double>(String.CASE_INSENSITIVE_ORDER);
		String fileName = file.getPath();
		
		//fileName.replace(".pop", ".txt");
		int index = fileName.indexOf(".pop");
		fileName = fileName.substring(0, index);
		fileName = fileName + ".txt";
 		
		try {
			root = new VDFParser().parse(ItemParser.readFile(file.toPath(), StandardCharsets.US_ASCII));
			if(root == null) {
				//window.updateFeedback(file.getName() + " is not a population file");
				return;
			}
		}
		catch (IOException i) {
			//window.updateFeedback(file.getName() + " was not found");
			return;
		}
		
		root = root.getSubNode(root.lastKey()); //includes irrelevant here
		waveCount = root.get("wave").length; //TODO: static
		
		for(int i = 0; i < waveCount; i++) {
			VDFNode waveNode = root.getSubNode("wave", i);
			int wavespawnCount = waveNode.get("wavespawn").length;
			List<Double> threadsLength = new ArrayList<Double>();
			double longest = 0;
			
			for(int j = 0; j < wavespawnCount; j++) {
				VDFNode wavespawnNode = waveNode.getSubNode("wavespawn", j);
				double wsTime = 0;
				String dependency = null;
				
				if(wavespawnNode.containsKey(WaveSpawnNode.SUPPORT)) { 
					continue; //can ignore wavespawns with support since they aren't necessary
				}
				
				if(wavespawnNode.containsKey(WaveSpawnNode.TANK)) {
					if(wavespawnNode.containsKey(WaveSpawnNode.NAME)) {
						lengthMap.put(wavespawnNode.getString(WaveSpawnNode.NAME), TANK);
					}
					else { //filler name if no name defined
						lengthMap.put(Integer.toString(wsCount), TANK);
						wsCount++;
					}
					System.out.println("tank");
					continue; //todo: do some time conversion for tanks
				}
				
				//likely some sort of relay or logic only subwave, ignore
				if(!wavespawnNode.containsKey(WaveSpawnNode.SPAWNCOUNT) || wavespawnNode.getInt(WaveSpawnNode.SPAWNCOUNT) == 0) {
					continue;
				}
				
				//functionally the same since we assume instakills
				if(wavespawnNode.containsKey(WaveSpawnNode.WAITBETWEENSPAWNS)) {
					wsTime = wavespawnNode.getFloat(WaveSpawnNode.WAITBETWEENSPAWNS);
				}
				else if(wavespawnNode.containsKey(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH)) {
					wsTime = wavespawnNode.getFloat(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH);
				}
				
				//treated as floats to get a float result, even if they're ints
				wsTime = wsTime * (wavespawnNode.getFloat(WaveSpawnNode.TOTALCOUNT) / wavespawnNode.getFloat(WaveSpawnNode.SPAWNCOUNT));
				
				if(wavespawnNode.containsKey(WaveSpawnNode.WAITFORALLSPAWNED)) {
					dependency = wavespawnNode.getString(WaveSpawnNode.WAITFORALLSPAWNED);
				}
				else if(wavespawnNode.containsKey(WaveSpawnNode.WAITFORALLDEAD)) {
					dependency = wavespawnNode.getString(WaveSpawnNode.WAITFORALLDEAD);
				}
				
				if(dependency != null) {
					Double dependencyLength = lengthMap.get(dependency);
					
					if(dependencyLength.equals(null)) { //missing subwave
						
					}
					
					if(wavespawnNode.containsKey(WaveSpawnNode.WAITBEFORESTARTING)) {
						double waitBefore = wavespawnNode.getFloat(WaveSpawnNode.WAITBEFORESTARTING);
						
						//if the dependency is longer than the waitbeforestarting, then we can ignore the latter
						if(dependencyLength > waitBefore) {
							wsTime = wsTime + dependencyLength;
						}
						else {
							wsTime = wsTime + dependencyLength + waitBefore;
						}
					}
					else {
						wsTime = wsTime + dependencyLength;
					}
					threadsLength.remove(dependencyLength);
				}
				else {
					if(wavespawnNode.containsKey(WaveSpawnNode.WAITBEFORESTARTING)) {
						wsTime = wsTime + wavespawnNode.getFloat(WaveSpawnNode.WAITBEFORESTARTING);
					}
				}
				
				if(wavespawnNode.containsKey(WaveSpawnNode.NAME)) {
					lengthMap.put(wavespawnNode.getString(WaveSpawnNode.NAME), wsTime);
				}
				else { //filler name if no name defined
					lengthMap.put(Integer.toString(wsCount), wsTime);
					wsCount++;
				}
				threadsLength.add(wsTime);
				System.out.println("wavespawn" + j);
			} //end of ws
			for(double val : threadsLength) {
				if(val > longest) {
					longest = val;
				}
			}
			lengthMap.put("WAVE" + (i + 1), longest);
			System.out.println("wave" + i);
		} //end of wave
		System.out.println("finished map");
		
		printTime(fileName, lengthMap);
	}
	
	private void printTime(String filePath, Map<String, Double> map) {
		FileWriter fw;
		PrintWriter pw;
		
		try {
			fw = new FileWriter(filePath);
			pw = new PrintWriter(fw, true);
			
			for(Entry<String, Double> entry : map.entrySet()) {
				pw.println(entry.getKey() + ": " + entry.getValue() + "s");
			}
			System.out.println("finished print");
			
			pw.close();
			fw.close();
		}
		catch (FileNotFoundException e){
			
		}
		catch (IOException i) {
			
		}
	}
}
