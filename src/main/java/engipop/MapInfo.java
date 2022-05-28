package engipop;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapInfo { //parse map info txt, load it into gui
	static URL txt;
	
	List<String> waveRelays;
	List<String> wsRelays;
	List<String> tankRelays;
	
	List<String> botSpawns;
	List<String> busterSpawns;
	List<String> sniperSpawns;
	List<String> spySpawns;
	List<String> tankSpawns;
	
	List<String> tags;
	boolean hasEngiHints;
	
	public MapInfo() {
		txt = getClass().getResource("/mapinfo.txt");
	}

	public List<String> getMapNames() { //load map names from first slot of each line in txt file
		List<String> maps = new ArrayList<String>();
		
		try {
			Reader ir = new InputStreamReader(txt.openStream());
			BufferedReader in = new BufferedReader(ir);
			
			String line;
			String token;
			int pos = 0;
			
			while((line = in.readLine()) != null) {
				pos = line.indexOf(","); //first comma occurs after mapname
				token = line.substring(0, pos);
				maps.add(token);
			}
			in.close();
		}
		catch (Exception e) {
			//print error here
		}
		return maps;
	}
	
	public void getMapData(int mapIndex) { //take map name, get its data
		waveRelays = new ArrayList<String>();
		wsRelays = new ArrayList<String>();
		tankRelays = new ArrayList<String>();
		botSpawns = new ArrayList<String>();
		busterSpawns = new ArrayList<String>();
		sniperSpawns = new ArrayList<String>();
		spySpawns = new ArrayList<String>();
		tankSpawns = new ArrayList<String>();
		tags = new ArrayList<String>();
		
		List<List<String>> listlist = new ArrayList<List<String>>();
		listlist.add(waveRelays);
		listlist.add(wsRelays);
		listlist.add(tankRelays);
		listlist.add(botSpawns);
		listlist.add(busterSpawns);
		listlist.add(sniperSpawns);
		listlist.add(spySpawns);
		listlist.add(tankSpawns);
		listlist.add(tags);
		
		try {
			Reader ir = new InputStreamReader(txt.openStream());
			BufferedReader in = new BufferedReader(ir);
			
			String line;
			int index;
			
			for(int i = 0; i < mapIndex; i++) {
				in.readLine();
			}
			line = in.readLine();
			in.close();
			
			index = line.indexOf(",");
			
			for(List<String> list : listlist) {
				index++; //now pointing at space
				if(line.charAt(index + 1) == ',') { //if list has no entries
					list.add("");
				}
				else {
					index += 2; //skip over the space and bracket
					index = readList(line, list, index);
				}
				index++; //now at comma
			}
			//index++; //first char of bool
			
			if(line.endsWith("true")) { //currently assuming end is always true or false
				hasEngiHints = true;
			}
			else {
				hasEngiHints = false;
			}
			
		}
		catch (Exception e) {
			//print somewhere file has gone missing
		}
	}
	
	private int readList(String line, List<String> list, int i) { //reads bracketed stuff into list
		int start = i;
		
		while(line.charAt(i) != ']') {
			if(line.charAt(i) == ',') {
				list.add(line.substring(start, i));
				i += 2; //skip over comma and space
				start = i;
			}
			else {
				i++;
			}
		}
		list.add(line.substring(start, i));
		
		return i; //when this returns it'll be pointing at the last bracket
	}
	
	public List<String> getWaveRelay() {
		return this.waveRelays;
	}
	
	public List<String> getWSRelay() {
		return this.wsRelays;
	}
	
	public List<String> getBotSpawns() {
		return this.botSpawns;
	}
	
	public List<String> getTankSpawns() {
		return this.tankSpawns;
	}
	
	public List<String> getTankRelays() {
		return this.tankRelays;
	}
	
	public List<String> getTags() {
		return this.tags;
	}
}
