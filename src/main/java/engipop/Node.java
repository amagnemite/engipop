package engipop;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import engipop.Engipop.Classes;
import net.platinumdigitalgroup.jvdf.VDFNode;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
@SuppressWarnings("unchecked")
public class Node {
	private transient Node parent;
	private List<Node> children = new ArrayList<Node>();
	Map<String, List<Object>> keyVals = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
	//children refers to waveschedule - wave - wavespawn - spawner connections
	//side connections like relays are connected elsewhere
	
    public enum SpawnerType {
    	NONE, TFBOT, TANK, SQUAD, RANDOMCHOICE
    }
    
    public void connectNodes(Node parent) { //parents the calling node and adds calling node to parent's list
    	this.parent = parent; 
    	parent.children.add(this);
    }
    
    public List<Node> getChildren() {
    	return this.children;
    }
    
    public void setChildren(List<Node> children) {
    	this.children = children;
    }
    
 	public boolean hasChildren() {
		boolean hasChild = false;
		
		if(this.getChildren().size() > 0) {
			hasChild = true;
		}
		return hasChild;
	}
    
    public Node getParent() {
    	return this.parent;
    }
    
    //remove this?
    public void setParent(Node parent) {
    	this.parent = parent;
    }
	
	//put key into map assuming it's not null or empty, otherwise remove it
	public void putKey(String key, Object value) {
		if(value == null || value.equals("")) { //remove key does nothing if key doesn't exist
			keyVals.remove(key);
			return;
		}
		else if(value == Collections.emptyList()) { //may need to remove this
			keyVals.remove(key);
			return;
		}
		//else if(value.getClass() != String.class || 
		//	(value.getClass() == String.class && !value.equals(""))) {
			//if it is a string that is not empty or it is not a string
			
		//}
		if(value instanceof List) {
			keyVals.put(key, new ArrayList<Object>((List<Object>) value));
		}
		else {
			keyVals.put(key, new ArrayList<Object>(Arrays.asList(value)));
		}
	}
	
	//gets values in the list object
	public Object getValue(String key, int pos) {
		//if(keyVals.get(key).length == 1) {
		//	return keyVals.get(key);
		//}
		if(!keyVals.containsKey(key)) {
			return null;
		}
		else {
			return keyVals.get(key).get(pos);
		}
	}
	
	public Object getValue(String key) {
		return getValue(key, 0);
	}
	
	//gets the actual list object that contains all the sub objects
	public List<Object> getListValue(String key) {
		return keyVals.get(key);
	}
	
	public boolean containsKey(String key) {
		return keyVals.containsKey(key);
	}
	
	public void removeKey(String key) {
		keyVals.remove(key);
	}
	
	public void printKeyVals() {
		for(Entry<String, List<Object>> entry : keyVals.entrySet()) {
			System.out.print(entry.getKey());
			if(entry.getValue().size() == 1) {
				System.out.println(" " + entry.getValue().get(0));
			}
			else {
				System.out.println(" " + entry.getValue());
			}
		}
	}
	
	//reconsider this
	public Map<String, List<Object>> getMap() {
		return this.keyVals;
	}
	
	//converts vdfnodes (<string, object[]>) into treemaps <string, list<object>>
	public Map<String, List<Object>> copyVDFNode(Map<String, Object[]> map) {
		Set<String> stringSet = new HashSet<String>(
				Arrays.asList(WaveSpawnNode.NAME.toLowerCase(), WaveSpawnNode.WAITFORALLDEAD.toLowerCase(), 
						WaveSpawnNode.WAITFORALLSPAWNED.toLowerCase()));
		final String Digits = "(\\p{Digit}+)"; //decimal digit one or more times
		final String fpRegex =
				("-?(" + //- 0 or 1 times
				"("+Digits+"(\\.)?("+Digits+"?))|" + //digits 1 or more times, . 0 or 1 times, digits 0 or 1 times
				"(\\.("+Digits+")))"); //. , digits
		
		Map<String, List<Object>> newMap = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
		for(Entry<String, Object[]> entry : map.entrySet()) {
            List<Object> array = new ArrayList<Object>(entry.getValue().length);
            
            //for(int i = 0; i < entry.getValue().length; i++) {
            for(Object arrayEntry : entry.getValue()) {
                if(arrayEntry instanceof Map) {
                	//Map<String, List<Object>> subMap = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
                    //subMap.putAll((VDFNode) entry.getValue()[i]);
                    array.add(copyVDFNode((VDFNode) arrayEntry));
                }
                else if(!stringSet.contains(entry.getKey().toLowerCase())) { //convert string numbers into ints/doubles
                    String str = (String) arrayEntry;
                    if(str.contains(".") && Pattern.matches(fpRegex, str)) {
                        array.add(Double.valueOf(str));
                    }    
                    else if(Pattern.matches(Digits, str)) {
                    	try {
                    		array.add(Integer.valueOf(str)); //this may also catch certain booleans/flags
                    	}
                        catch(NumberFormatException n) {
                        	//if people decide to be silly and use stupid large numbers, just keep it as string
                        	//this will cause issues with the mandatory spinners though
                        	array.add(arrayEntry);
                        }
                    }
                    else {
                    	array.add(arrayEntry);
                    }
                }
                else {
                	array.add(arrayEntry);
                }
            }
            newMap.put(entry.getKey(), array);
        }   
		return newMap; 
	}
    
    public static class PopNode extends Node {
		public static final String STARTINGCURRENCY = "StartingCurrency"; //int
    	public static final String RESPAWNWAVETIME = "RespawnWaveTime"; //int
    	public static final String FIXEDRESPAWNWAVETIME = "FixedRespawnWaveTime"; //boolean
    	public static final String EVENTPOPFILE = "EventPopfile"; //boolean / this is a string, convert this later
    	public static final String BUSTERDAMAGE = "AddSentryBusterWhenDamageDealtExceeds"; //int
    	public static final String BUSTERKILLS = "AddSentryBusterWhenKillCountExceeds"; //int
    	public static final String BOTSATKINSPAWN = "CanBotsAttackWhileInSpawnRoom"; //boolean / string
    	public static final String ADVANCED = "Advanced"; //boolean
    	public static final String MISSION = "Mission";
    	public static final String TEMPLATE = "Templates";
    	
    	private int mapIndex = -1;
    	private Map<String, Node> wsTemplateMap = new TreeMap<String, Node>(String.CASE_INSENSITIVE_ORDER);
    	private Map<String, Node> botTemplateMap = new TreeMap<String, Node>(String.CASE_INSENSITIVE_ORDER);
    	private List<String> usedTemplates = new ArrayList<String>();

        public PopNode() {
        	putKey(STARTINGCURRENCY, 400);
        	putKey(RESPAWNWAVETIME, 6);
        	putKey(BUSTERDAMAGE, Engipop.BUSTERDEFAULTDMG);
        	putKey(BUSTERKILLS, Engipop.BUSTERDEFAULTKILLS);
        	putKey(BOTSATKINSPAWN, false);
        	putKey(FIXEDRESPAWNWAVETIME, false);
        	putKey(EVENTPOPFILE, false);
        	putKey(MISSION, new ArrayList<Node>());
        	//putKey(ADVANCED, false);
        	
        	WaveNode wave = new WaveNode();
        	WaveSpawnNode ws = new WaveSpawnNode();
        	TFBotNode bot = new TFBotNode();
        	
        	wave.connectNodes(this);
    		ws.connectNodes(wave);
    		bot.connectNodes(ws);
    		//TODO: this scout isn't added to the wavebar 
        }
        
        //only constructor to use Object[] as opposed to List<Object>> since hasn't been processed yet
		public PopNode(Map<String, Object[]> map) { //constructor for read in nodes  	
			keyVals = this.copyVDFNode(map);
        	
        	if(this.containsKey("Wave")) {
        		for(Object wave : keyVals.get("Wave")) {
        			WaveNode waveNode = new WaveNode((Map<String, List<Object>>) wave);
					waveNode.connectNodes(this);
        		}
        		this.removeKey("Wave");
        	}
        	
        	if(!this.containsKey(STARTINGCURRENCY)) {
        		putKey(STARTINGCURRENCY, 0);
        	}
        	
        	//may need to make sure this isn't a not string
        	if(this.containsKey(EVENTPOPFILE) && 
        			((String) this.getValue(EVENTPOPFILE)).equals("Halloween")) {
        		putKey(EVENTPOPFILE, true);
        	}
        	else {
        		putKey(EVENTPOPFILE, false);
        	}
        	
        	if(this.containsKey(BOTSATKINSPAWN)) {
        		String atk = (String) this.getValue(BOTSATKINSPAWN);
        		if((atk.equalsIgnoreCase("no") || atk.equalsIgnoreCase("false"))) {
        			putKey(BOTSATKINSPAWN, false);
        		}
        		else {
        			putKey(BOTSATKINSPAWN, true);
        		}
        	}
        	else { //double check this
        		putKey(BOTSATKINSPAWN, true);
        	}
        	
        	if(!this.containsKey(RESPAWNWAVETIME)) {
        		putKey(RESPAWNWAVETIME, 0);
        	}
        	
        	if(this.containsKey(FIXEDRESPAWNWAVETIME)) { //presence of flag is true
        		putKey(FIXEDRESPAWNWAVETIME, true);
        	}
        	else {
        		putKey(FIXEDRESPAWNWAVETIME, false);
        	}
        	
        	if(!keyVals.containsKey(BUSTERDAMAGE)) {
        		putKey(BUSTERDAMAGE, Engipop.BUSTERDEFAULTDMG);
        	}
        	if(!keyVals.containsKey(BUSTERKILLS)) {
        		putKey(BUSTERKILLS, Engipop.BUSTERDEFAULTKILLS);
        	}
        	
        	if(keyVals.containsKey(MISSION)) {
        		List<Node> array = new ArrayList<Node>();
        		for(Object mission : keyVals.get(MISSION)) {
        			array.add(new MissionNode((Map<String, List<Object>>) mission));
        		}
        		putKey(MISSION, array);
        	}
        	else {
        		putKey(MISSION, new ArrayList<Node>());
        	}
        	
        	if(keyVals.containsKey(TEMPLATE)) {
        		List<Object> list = keyVals.get(TEMPLATE);
        		
        		for(Entry<String, Object> node : ((Map<String, Object>) list.get(0)).entrySet()) {
        			//i love casting i love casting
        			List<Object> sublist = (List<Object>) node.getValue();
        			Map<String, List<Object>> templateKeyVals = (Map<String, List<Object>>) sublist.get(0);
        			Set<String> botKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        			Set<String> wsKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        			
        			botKeys.addAll(TFBotNode.getNodeKeyList());
        			botKeys.remove(TFBotNode.NAME);
        			botKeys.remove(TFBotNode.TEMPLATE);
        			wsKeys.addAll(WaveSpawnNode.getNodeKeyList());
        			wsKeys.remove(WaveSpawnNode.NAME);
        			wsKeys.remove(WaveSpawnNode.TEMPLATE);
        			
        			if(botKeys.removeAll(templateKeyVals.keySet())) {
        				botTemplateMap.put(node.getKey(), new TFBotNode(templateKeyVals));
        			}
        			else if(wsKeys.removeAll(templateKeyVals.keySet())){
        				wsTemplateMap.put(node.getKey(), new WaveSpawnNode(templateKeyVals));
        			}
        			else {
        				//not bot/ws templates, ignore for now
        			}
        		}
        		keyVals.remove(TEMPLATE);
        	}
        }
        
        public void setMapIndex(int i) {
        	this.mapIndex = i;
        }
        
        public int getMapIndex() {
        	return this.mapIndex;
        }
        
        public void setWSTemplateMap(Map<String, Node> map) {
        	this.wsTemplateMap = map;
        }
        
        public Map<String, Node> getWSTemplateMap() {
        	return wsTemplateMap;
        }
        
        public void setBotTemplateMap(Map<String, Node> map) {
        	this.botTemplateMap = map;
        }
        
        public Map<String, Node> getBotTemplateMap() {
        	return botTemplateMap;
        }
        
        public void addTemplate(String template) {
        	usedTemplates.add(template);
        }
        
        public List<String> getUsedTemplates() {
        	return usedTemplates;
        }
    
        public static List<String> getNodeKeyList() {
        	List<String> list = new ArrayList<String>(Arrays.asList(STARTINGCURRENCY, RESPAWNWAVETIME, FIXEDRESPAWNWAVETIME,
        			BUSTERDAMAGE, BUSTERKILLS, BOTSATKINSPAWN, EVENTPOPFILE, TEMPLATE, MISSION));
        	
    		return list;
        }
    }
    
    public static abstract class NodeWithSpawner extends Node {
    	public Node getSpawner() {
    		if(getChildren().isEmpty()) {
    			return null;
    		}
    		return getChildren().get(0);
    	}
    	
    	//precheck spawner type
    	//this assumes the child is valid
    	public SpawnerType getSpawnerType() {
    		Node node = getSpawner();
    		SpawnerType type = null;
    		
    		if(node == null) {
    			return null;
    		}
    		
    		if(node.getClass() == TFBotNode.class) {
    			type = SpawnerType.TFBOT;
    		}
    		else if(node.getClass() == TankNode.class) {
    			type = SpawnerType.TANK;
    		}
    		else if(node.getClass() == SquadNode.class) {
    			type = SpawnerType.SQUAD;
    		}
    		else if(node.getClass() == RandomChoiceNode.class) {
    			type = SpawnerType.RANDOMCHOICE;
    		}
    		return type;
    	}
    }
    
    public static class MissionNode extends NodeWithSpawner {
		public static final String WHERE = "Where"; //string
    	public static final String OBJECTIVE = "Objective"; //string
    	public static final String INITIALCOOLDOWN = "InitialCooldown"; //float
    	public static final String COOLDOWNTIME = "CooldownTime"; //float
    	public static final String BEGINATWAVE = "BeginAtWave"; //int
    	public static final String RUNFORTHISMANYWAVES = "RunForThisManyWaves"; //int
    	public static final String DESIREDCOUNT = "DesiredCount"; //int
    	
    	public static final String DESTROYSENTRIES = "DestroySentries";
    	public static final String SNIPER = "Sniper";
    	public static final String SPY = "Spy";
    	public static final String ENGINEER = "Engineer";
    	//spawner
    	
    	//tfbot node is currently in the children list, may just move to keyval
    	
    	public MissionNode() {
    		putKey(OBJECTIVE, DESTROYSENTRIES);
    		putKey(INITIALCOOLDOWN, 0);
    		putKey(COOLDOWNTIME, 0);
    		putKey(BEGINATWAVE, 1);
    		putKey(RUNFORTHISMANYWAVES, 1);
    		putKey(DESIREDCOUNT, 1);
    		putKey(WHERE, new ArrayList<Object>(2));
    	}
    	
    	public MissionNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		if(map.containsKey("TFBOT")) {
    			TFBotNode botNode = new TFBotNode((Map<String, List<Object>>) getValue("TFBOT"));
    			botNode.connectNodes(this);
        		this.removeKey("TFBOT");
    		}
    		else {
    			TFBotNode botNode = new TFBotNode();
    			botNode.connectNodes(this);
    		}
    	}
    	
    	public static List<String> getNodeKeyList() {
    		List<String> keyValList = new ArrayList<String>(Arrays.asList(OBJECTIVE, WHERE, INITIALCOOLDOWN, COOLDOWNTIME, BEGINATWAVE,
    				RUNFORTHISMANYWAVES, DESIREDCOUNT));
    		
    		return keyValList;
    	}
    }
    
    public static class WaveNode extends Node { 
		//todo: sound support
    	//description?
    	//waitwhendone and checkpoint are both vestigal
    	public static final String STARTWAVEOUTPUT = "StartWaveOutput";
    	public static final String DONEOUTPUT = "DoneOutput";
    	public static final String INITWAVEOUTPUT = "InitWaveOutput";
    	public static final String WAVESPAWN = "WaveSpawn";
    	
    	public WaveNode() {
    		putKey(STARTWAVEOUTPUT, new RelayNode());
    		putKey(DONEOUTPUT, new RelayNode());
    	}
    	
        public WaveNode(Map<String, List<Object>> map) { //readin node, key case should already be converted
        	keyVals.putAll(map);
        	
        	if(containsKey("WaveSpawn")) {
        		for(Object wavespawn : keyVals.get("WaveSpawn")) {
        			WaveSpawnNode waveSpawnNode = new WaveSpawnNode((Map<String, List<Object>>) wavespawn);
					waveSpawnNode.connectNodes(this);
        		}
        		keyVals.remove("WaveSpawn");
        	}
        	
        	if(containsKey(STARTWAVEOUTPUT)) {
        		RelayNode start = new RelayNode((Map<String, List<Object>>) getValue(STARTWAVEOUTPUT));
        		putKey(STARTWAVEOUTPUT, start);
        	}
        	
        	if(keyVals.containsKey(DONEOUTPUT)) {
        		RelayNode done = new RelayNode((Map<String, List<Object>>) getValue(DONEOUTPUT));
        		putKey(DONEOUTPUT, done);
        	}
        	
        	if(keyVals.containsKey(INITWAVEOUTPUT)) {
        		RelayNode init = new RelayNode((Map<String, List<Object>>) getValue(INITWAVEOUTPUT));
        		putKey(INITWAVEOUTPUT, init);
        	}
        }
        
        public static List<String> getNodeKeyList() {
        	return new ArrayList<String>(Arrays.asList(STARTWAVEOUTPUT, DONEOUTPUT, INITWAVEOUTPUT));
        }
    }
    
    public static class RelayNode extends Node { //something experimental
		public static final String TARGET = "Target";
    	public static final String ACTION = "Action";
    	public static final String PARAM = "Param";
    	public static final String DELAY = "Delay";
    	
    	public RelayNode() { //for now, assume all actions are trigger
		}
    	
    	public RelayNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    	}
    	
    	public void updateNode(String target, String action, String param, String delay) {
    		putKey(TARGET, target);
    		putKey(ACTION, action);
    		
    		if(param != null && !param.isBlank()) {
    			putKey(PARAM, param);
    		}
    		else {
    			removeKey(PARAM);
    		}
    		
    		if(delay != null && !delay.isBlank()) {
    			putKey(DELAY, delay);
    		}
    		else {
    			removeKey(DELAY);
    		}
    	}
    	
    	public void updateNode(String target, String action) {
    		updateNode(target, action, null, null);
    	}
    	
    	public static List<String> getNodeKeyList() {
        	return new ArrayList<String>(Arrays.asList(TARGET, ACTION, PARAM, DELAY));
        }
    	
    	/*
    	public boolean isTargetEmptyOrNull() {
    		boolean check = false;
    		
    		if(this.getValueSingular("Target") == null) {
    			check = true;
    		}
    		else {
    			check = ((String) this.getValueSingular("Target")).isEmpty() ? true : false;
    		}
    		return check;
    	}
    	*/
    }
    
    public static class WaveSpawnNode extends NodeWithSpawner { //node for wavespawns
		public static final String WHERE = "Where"; //string
    	public static final String TOTALCOUNT = "TotalCount"; //int
    	public static final String MAXACTIVE = "MaxActive"; //int
    	public static final String SPAWNCOUNT = "SpawnCount"; //int
    	public static final String WAITBEFORESTARTING = "WaitBeforeStarting"; //float
    	public static final String WAITBETWEENSPAWNS = "WaitBetweenSpawns"; //float
    	public static final String WAITBETWEENSPAWNSAFTERDEATH = "WaitBetweenSpawnsAfterDeath";
    	//todo: add wavespawn sounds/outputs as optional feature
    	public static final String TOTALCURRENCY = "TotalCurrency"; //int
    	public static final String NAME = "Name"; //string
    	public static final String WAITFORALLDEAD = "WaitForAllDead"; //string
    	public static final String WAITFORALLSPAWNED = "WaitForAllSpawned"; //string
    	public static final String SUPPORT = "Support"; //boolean/flag
    	//private boolean randomSpawn; //default 0, if true allows bot spawns to be randomized
    	//prob should be disabled for single spawn maps
    	public static final String STARTWAVEOUTPUT = "StartWaveOutput"; //relaynode / map
    	public static final String FIRSTSPAWNOUTPUT = "FirstSpawnOutput";
    	public static final String LASTSPAWNOUTPUT = "LastSpawnOutput";
    	public static final String DONEOUTPUT = "DoneOutput";
    	public static final String TEMPLATE = "Template";
    	
    	public static final String TFBOT = "TFBot";
    	public static final String TANK = "Tank";
    	public static final String SQUAD = "Squad";
    	public static final String RANDOMCHOICE = "RandomChoice";
    	//TODO: fix support
    	
    	private boolean waitBetweenDeaths; //betweenspawns and betweenspawnsafterdeath are mutually exclusive
    	private boolean supportLimited;
    	
    	public WaveSpawnNode() {
    		//putKey(WHERE, "spawnbot");
    		putKey(TOTALCOUNT, 1);
    		putKey(MAXACTIVE, 1);
    		putKey(SPAWNCOUNT, 1);
    		putKey(WAITBEFORESTARTING, 0.0);
    		putKey(WAITBETWEENSPAWNS, 0.0);
    		putKey(TOTALCURRENCY, 0);
    		putKey(SUPPORT, false);
    	}
    	
    	public WaveSpawnNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		/*
    		if(keyVals.containsKey(NAME) && getValue(NAME).getClass() == Integer.class) {
    			putKey(NAME, getValue(NAME).toString());
    		}
    		
    		if(keyVals.containsKey(WAITFORALLDEAD) && getValue(WAITFORALLDEAD).getClass() == Integer.class) {
    			putKey(WAITFORALLDEAD, getValue(WAITFORALLDEAD).toString());
    		}
    		
    		if(keyVals.containsKey(WAITFORALLSPAWNED) && getValue(WAITFORALLSPAWNED).getClass() == Integer.class) {
    			putKey(WAITFORALLSPAWNED, getValue(WAITFORALLSPAWNED).toString());
    		}
    		*/
    		
    		if(keyVals.containsKey(WAITBETWEENSPAWNSAFTERDEATH)) {
    			waitBetweenDeaths = true;
    		}
    		
    		if(!keyVals.containsKey(TOTALCOUNT)) {
    			putKey(TOTALCOUNT, 0);
    		}
    		if(!keyVals.containsKey(MAXACTIVE)) {
    			putKey(MAXACTIVE, 0);
    		}
    		if(!keyVals.containsKey(SPAWNCOUNT)) {
    			putKey(SPAWNCOUNT, 0);
    		}
    		if(!keyVals.containsKey(WAITBEFORESTARTING)) {
    			putKey(WAITBEFORESTARTING, 0.0);
    		}
    		else {
    			if(getValue(WAITBEFORESTARTING).getClass() == Integer.class) {
    				putKey(WAITBEFORESTARTING, (Integer) getValue(WAITBEFORESTARTING) * 1.0);
    			}
    		}
    		
    		if(!keyVals.containsKey(WAITBETWEENSPAWNS)) {
    			putKey(WAITBETWEENSPAWNS, 0.0);
    		}
    		else {
    			if(getValue(WAITBETWEENSPAWNS).getClass() == Integer.class) {
    				putKey(WAITBETWEENSPAWNS, (Integer) getValue(WAITBETWEENSPAWNS) * 1.0);
    			}
    		}
    		
    		if(!keyVals.containsKey(TOTALCURRENCY)) {
    			putKey(TOTALCURRENCY, 0);
    		}
    		
    		//need to check this 
    		if(keyVals.containsKey(SUPPORT)) {
    			Object supportVal = getValue(SUPPORT);
    			if(supportVal.getClass() == String.class) {
    				if(((String) supportVal).equalsIgnoreCase("LIMITED")) {
    					supportLimited = true;
    				}
    			}
    			putKey(SUPPORT, true);
    		}
    		else {
    			putKey(SUPPORT, false);
    		}
    		
    		if(keyVals.containsKey(STARTWAVEOUTPUT)) {
    			putKey(STARTWAVEOUTPUT, new RelayNode((Map<String, List<Object>>) getValue(STARTWAVEOUTPUT)));
    		}
    		if(keyVals.containsKey(FIRSTSPAWNOUTPUT)) {
    			putKey(FIRSTSPAWNOUTPUT, new RelayNode((Map<String, List<Object>>) getValue(FIRSTSPAWNOUTPUT)));
    		}
    		if(keyVals.containsKey(LASTSPAWNOUTPUT)) {
    			putKey(LASTSPAWNOUTPUT, new RelayNode((Map<String, List<Object>>) getValue(LASTSPAWNOUTPUT)));
    		}
    		if(keyVals.containsKey(DONEOUTPUT)) {
    			putKey(DONEOUTPUT, new RelayNode((Map<String, List<Object>>) getValue(DONEOUTPUT)));
    		}
    		
    		if(keyVals.containsKey(TFBOT)) {
    			TFBotNode node = new TFBotNode((Map<String, List<Object>>) getValue(TFBOT));
    			node.connectNodes(this);
    			keyVals.remove(TFBOT);
    		}
    		else if(keyVals.containsKey(TANK)) {
    			TankNode node = new TankNode((Map<String, List<Object>>) getValue(TANK));
    			node.connectNodes(this);
    			keyVals.remove(TANK);
    		}
    		else if(keyVals.containsKey(SQUAD)) {
    			SquadNode node = new SquadNode((Map<String, List<Object>>) getValue(SQUAD));
    			node.connectNodes(this);
    			keyVals.remove(SQUAD);
    		}
    		else if(keyVals.containsKey(RANDOMCHOICE)) {
    			RandomChoiceNode node = new RandomChoiceNode((Map<String, List<Object>>) getValue(RANDOMCHOICE));
    			node.connectNodes(this);
    			keyVals.remove(RANDOMCHOICE);
    		}
    	}
 
    	public void setBetweenDeaths(boolean death) {
    		this.waitBetweenDeaths = death;
    	}
    	
    	public boolean getBetweenDeaths() {
    		return this.waitBetweenDeaths;
    	}
    	
    	public void setSupportLimited(boolean s) {
    		this.supportLimited = s;
    	}
    	
    	public boolean getSupportLimited() {
    		return this.supportLimited;
    	}
    	
    	//copy input node to calling
    	public void copyWaveSpawn(WaveSpawnNode copyFrom) {
    		this.setParent(copyFrom.getParent()); //double check parent doesn't interfere with anything
    		this.setChildren(copyFrom.getChildren());
    		//this.getMap().putAll(copyFrom.getMap());
    	}
    	
    	public static List<String> getNodeKeyList() {
    		return new ArrayList<String>(Arrays.asList(NAME, WHERE, TOTALCOUNT, MAXACTIVE, SPAWNCOUNT, TOTALCURRENCY, WAITBEFORESTARTING,
    				WAITBETWEENSPAWNS, WAITBETWEENSPAWNSAFTERDEATH, WAITFORALLSPAWNED, WAITFORALLDEAD, SUPPORT, STARTWAVEOUTPUT,
    					FIRSTSPAWNOUTPUT, LASTSPAWNOUTPUT, DONEOUTPUT, TFBOT, TANK, SQUAD, RANDOMCHOICE, TEMPLATE));
    	}
    }
    
    public static class TankNode extends Node {
		public static final String HEALTH = "Health"; //int
    	public static final String SPEED = "Speed"; //float
    	public static final String NAME = "Name"; //string
    	public static final String SKIN = "Skin"; //flag int
    	public static final String STARTINGPATHTRACKNODE = "StartingPathTrackNode"; //default ""
    	public static final String ONKILLEDOUTPUT = "OnKilledOutput";
    	public static final String ONBOMBDROPPEDOUTPUT = "OnBombDroppedOutput";
    	
    	public TankNode() {
    		putKey(HEALTH, Engipop.TANKDEFAULTHEALTH);
    		putKey(NAME, "tankboss");
    		putKey(SKIN, false);
    	}
    	
    	public TankNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		//may need to check this
    		if(keyVals.containsKey(SKIN)) {
    			putKey(SKIN, true);
    		}
    		else {	
    			putKey(SKIN, false);
    		}
    		
    		if(keyVals.containsKey(SPEED) && getValue(SPEED).getClass() == Integer.class) {
    			putKey(SPEED, (Integer) getValue(SPEED) * 1.0);
    		}
    		
    		if(keyVals.containsKey(ONKILLEDOUTPUT)) {
    			putKey(ONKILLEDOUTPUT, new RelayNode((Map<String, List<Object>>) getValue(ONKILLEDOUTPUT)));
    		}
    		
    		if(keyVals.containsKey(ONBOMBDROPPEDOUTPUT)) {
    			putKey(ONBOMBDROPPEDOUTPUT, 
    				new RelayNode((Map<String, List<Object>>) getValue(ONBOMBDROPPEDOUTPUT)));
    		}
    	}
    }
    
    public static class TFBotNode extends Node { //node for tfbot spawners
		public static final String CLASSNAME = "Class"; //class 
    	public static final String CLASSICON = "ClassIcon"; //string
    	public static final String NAME = "Name"; //string
    	public static final String SKILL = "Skill"; //skill
    	public static final String WEAPONRESTRICT = "WeaponRestrictions"; //weaponrestriction
    	public static final String TAGS = "Tag"; //List<String>
    	public static final String ATTRIBUTES = "Attributes";  //List<String>
    	public static final String ITEM = "Item"; //List<Object>
    	public static final String ITEMATTRIBUTES = "ItemAttributes"; //List<Map<String, String>>
    	public static final String CHARACTERATTRIBUTES = "CharacterAttributes";
    	public static final String TEMPLATE = "Template"; //string
    	public static final String HEALTH = "Health"; //int
    	public static final String SCALE = "Scale"; //float
    	public static final String AUTOJUMPMIN = "AutoJumpMin"; //float
    	public static final String AUTOJUMPMAX = "AutoJumpMax"; //float
    	public static final String BEHAVIORMODIFIERS = "BehaviorModifiers"; //string
    	public static final String MAXVISIONRANGE = "MaxVisionRange"; //int
    	public static final String TELEPORTWHERE = "TeleportWhere"; //string
    	public static final String EVENTCHANGEATTRIBUTES = "EventChangeAttributes";
    	public static final String ITEMNAME = "ItemName";
    	
    	public static final int ITEMCOUNT = 7;
    	
    	public static final String NOSKILL = "N/A";
    	public static final String EASY = "Easy";
    	public static final String NORMAL = "Normal";
    	public static final String HARD = "Hard";
    	public static final String EXPERT = "Expert";
    	
    	public static final String ANY = "Any";
    	public static final String PRIMARYONLY = "PrimaryOnly";
    	public static final String SECONDARYONLY = "SecondaryOnly";
    	public static final String MELEEONLY = "MeleeOnly";
    	
    	public static final String REMOVEONDEATH = "RemoveOnDeath";
    	public static final String AGGRESIVE = "Aggressive";
    	public static final String SUPPRESSFIRE = "SuppressFire";
    	public static final String DISABLEDODGE = "DisableDodge";
    	public static final String BECOMESPECTATORONDEATH = "BecomeSpectatorOnDeath";
    	public static final String RETAINBUILDINGS = "RetainBuildings";
    	public static final String SPAWNWITHFULLCHARGE = "SpawnWithFullCharge";
    	public static final String ALWAYSCRIT = "AlwaysCrit";
    	public static final String IGNOREENEMIES = "IgnoreEnemies";
    	public static final String HOLDFIREUNTILFULLRELOAD = "HoldFireUntilFullReload";
    	public static final String ALWAYSFIREWEAPON = "AlwaysFireWeapon";
    	public static final String MINIBOSS = "MiniBoss";
    	public static final String USEBOSSHEALTHBAR = "UseBossHealthBar";
    	public static final String IGNOREFLAG = "IgnoreFlag";
    	public static final String AUTOJUMP = "AutoJump";
    	public static final String AIRCHARGEONLY = "AirChargeOnly";
    	public static final String VACCINATORBULLETS = "VaccinatorBullets";
    	public static final String VACCINATORBLAST = "VaccinatorBlast";
    	public static final String VACCINATORFIRE = "VaccinatorFire";
    	public static final String BULLETIMMUNE = "BulletImmune";
    	public static final String BLASTIMMUNE = "BlastImmune";
    	public static final String FIREIMMUNE = "FireImmune";
    	public static final String PARACHUTE = "Parachute";
    	public static final String PROJECTILESHIELD = "ProjectileShield";
    	public static final String TELEPORTTOHINT = "TeleportToHint";
    	
    	private boolean isItemsSorted;
    	
    	//consider allowing template only 
    	public TFBotNode() { //defaults
    		putKey(CLASSNAME, Classes.Scout);
    		putKey(CLASSICON, "scout");
    		putKey(SKILL, EASY);
    		putKey(WEAPONRESTRICT, ANY);
    		putKey(ITEM, new String[ITEMCOUNT]);
    		putKey(ITEMATTRIBUTES, new ArrayList<Map<String, Object>>());
    		putKey(CHARACTERATTRIBUTES, new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));
    		isItemsSorted = true;
    	}
    	 
    	//alternate constructor for read in tfbots
    	public TFBotNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		if(keyVals.containsKey(CLASSNAME)) {
    			putKey(CLASSNAME, Classes.toClass((String) keyVals.get(CLASSNAME).get(0)));
    		}
    		else {
    			putKey(CLASSNAME, Classes.None);
    		}
    		
    		if(keyVals.containsKey(TAGS)) {
    			List<Object> list = new ArrayList<Object>(keyVals.get(TAGS));
    			putKey(TAGS, list);
    		}
    		
    		//this is awful, do something about it
    		//TODO: probably store everything as lowercase and just link the visual/printing to actual
    		if(keyVals.containsKey(ATTRIBUTES)) {
    			List<String> newList = new ArrayList<String>();
    			
    			Iterator<Object> iterator = keyVals.get(ATTRIBUTES).iterator();
    			List<String> lowerAttrList = getAttributesLowercaseList();
    			List<String> attrList = getAttributesList();
    			
    			while(iterator.hasNext()) {
    				String attr = (String) iterator.next();
    				//boolean containsSearchStr = lowerAttrList.stream().anyMatch(attr::equalsIgnoreCase);
    				
    				if(lowerAttrList.contains(attr.toLowerCase())) {
    					int index = lowerAttrList.indexOf(attr.toLowerCase());
    					newList.add(attrList.get(index));
    				}
    			}
    			putKey(ATTRIBUTES, newList);
    		}
    		
    		if(keyVals.containsKey(ITEM)) {
    			isItemsSorted = false;
    			String[] items = new String[ITEMCOUNT];
    			
    			for(int i = 0; i < keyVals.get(ITEM).size(); i++) {
    				//lowercase for later sorting
    				items[i] = ((String) keyVals.get(ITEM).get(i)).toLowerCase();
    			}
    			
    			putKey(ITEM, items);
    		}
    		else {
    			putKey(ITEM, new String[ITEMCOUNT]);
    		}
    		
    		//this is sorted in botpanel, maybe shouldn't be
    		//post copyvdfnode = arraylist of map<string, arraylist<object>>s
    		//so need to copy into an arraylist of map<string, string>
    		if(keyVals.containsKey(ITEMATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(ITEMCOUNT);
    			for(Object value : keyVals.get(ITEMATTRIBUTES)) { //for each map in itemattributes
    				Map<String, List<Object>> attrMap = (Map<String, List<Object>>) value;
    				Map<String, Object> newMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    				
    				for(Entry<String, List<Object>> entry : attrMap.entrySet()) {
    					newMap.put(entry.getKey(), entry.getValue().get(0));	
    				}
    				list.add(newMap);
    			}
    			putKey(ITEMATTRIBUTES, list);
    		}
    		else {
    			putKey(ITEMATTRIBUTES, new ArrayList<Map<String, Object>>());
    		}
    		
    		if(keyVals.containsKey(CHARACTERATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(1);
				Map<String, String> newMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
				
				for(Entry<String, List<Object>> entry : ((Map <String, List<Object>>) getValue(CHARACTERATTRIBUTES)).entrySet()) {
					//also post int/double conversion so need to reconvert
					newMap.put(entry.getKey(), entry.getValue().get(0).toString());	
				}
				list.add(newMap);
    			putKey(CHARACTERATTRIBUTES, list);
    		}
    		else {
    			putKey(CHARACTERATTRIBUTES, new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER));
    		}
    		
    		if(!keyVals.containsKey(SKILL)) {
    			putKey(SKILL, NOSKILL);
    		}
    		
    		if(!keyVals.containsKey(WEAPONRESTRICT)) {
    			putKey(WEAPONRESTRICT, ANY);
    		}
    	}
    	
    	public boolean isItemsSorted() {
    		return isItemsSorted;
    	}
    	
    	public void setItemsSorted(boolean isItemsSorted) {
    		this.isItemsSorted = isItemsSorted;
    	}
    	
    	public static List<String> getNodeKeyList() {
    		return new ArrayList<String>(Arrays.asList(NAME, CLASSNAME, CLASSICON, TEMPLATE, SKILL, WEAPONRESTRICT, HEALTH, SCALE, ATTRIBUTES, TAGS,
    				CHARACTERATTRIBUTES, ITEM, ITEMATTRIBUTES, AUTOJUMPMIN, AUTOJUMPMAX, BEHAVIORMODIFIERS, MAXVISIONRANGE, TELEPORTWHERE,
    					EVENTCHANGEATTRIBUTES));
    	}
    	
    	public static List<String> getAttributesList() {
    		return new ArrayList<String>(Arrays.asList(REMOVEONDEATH, AGGRESIVE, SUPPRESSFIRE, 
    			DISABLEDODGE, BECOMESPECTATORONDEATH, RETAINBUILDINGS, SPAWNWITHFULLCHARGE, 
    				ALWAYSCRIT, IGNOREENEMIES, HOLDFIREUNTILFULLRELOAD, ALWAYSFIREWEAPON,
					MINIBOSS, USEBOSSHEALTHBAR, IGNOREFLAG, AUTOJUMP, 
					AIRCHARGEONLY, VACCINATORBULLETS, VACCINATORBLAST, VACCINATORFIRE, 
						BULLETIMMUNE, BLASTIMMUNE, FIREIMMUNE, PARACHUTE, 
							PROJECTILESHIELD, TELEPORTTOHINT));
    	}
    	
    	public static List<String> getAttributesLowercaseList() {
    		return new ArrayList<String>(Arrays.asList(REMOVEONDEATH.toLowerCase(), AGGRESIVE.toLowerCase(), SUPPRESSFIRE.toLowerCase(), 
    			DISABLEDODGE.toLowerCase(), BECOMESPECTATORONDEATH.toLowerCase(), RETAINBUILDINGS.toLowerCase(), SPAWNWITHFULLCHARGE.toLowerCase(), 
    				ALWAYSCRIT.toLowerCase(), IGNOREENEMIES.toLowerCase(), HOLDFIREUNTILFULLRELOAD.toLowerCase(), ALWAYSFIREWEAPON.toLowerCase(),
					MINIBOSS.toLowerCase(), USEBOSSHEALTHBAR.toLowerCase(), IGNOREFLAG.toLowerCase(), AUTOJUMP.toLowerCase(), 
					AIRCHARGEONLY.toLowerCase(), VACCINATORBULLETS.toLowerCase(), VACCINATORBLAST.toLowerCase(), VACCINATORFIRE.toLowerCase(), 
						BULLETIMMUNE.toLowerCase(), BLASTIMMUNE.toLowerCase(), FIREIMMUNE.toLowerCase(), PARACHUTE.toLowerCase(), 
							PROJECTILESHIELD.toLowerCase(), TELEPORTTOHINT.toLowerCase()));
    	}
    }
    
    public static abstract class SquadRCNode extends Node {
    	public SquadRCNode() {
    	}
    	
    	public SquadRCNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		if(map.containsKey(WaveSpawnNode.SQUAD)) {
	    		for(Object bot : map.get(WaveSpawnNode.SQUAD)) {
	    			SquadNode node = new SquadNode((Map<String, List<Object>>) bot);
	    			node.connectNodes(this);
	    		}
	    		keyVals.remove(WaveSpawnNode.SQUAD);
    		}
    		if(map.containsKey(WaveSpawnNode.RANDOMCHOICE)) {
	    		for(Object bot : map.get(WaveSpawnNode.RANDOMCHOICE)) {
	    			RandomChoiceNode node = new RandomChoiceNode((Map<String, List<Object>>) bot);
	    			node.connectNodes(this);
	    		}
	    		keyVals.remove(WaveSpawnNode.RANDOMCHOICE);
    		}
    		if(map.containsKey(WaveSpawnNode.TFBOT)) {
	    		for(Object bot : map.get(WaveSpawnNode.TFBOT)) {
	    			TFBotNode node = new TFBotNode((Map<String, List<Object>>) bot);
	    			node.connectNodes(this);
	    		}
	    		keyVals.remove(WaveSpawnNode.TFBOT);
    		}
    	}
    	
    	public boolean hasNestedSquadRC() {
    		for(Node subnode : getChildren()) {
    			if(subnode instanceof SquadRCNode) {
    				return true;
    			}
    		}
    		return false;
    	}
    }
    
    //these two are mostly convenience 
    public static class SquadNode extends SquadRCNode {
    	public SquadNode() {
    	}
    	
    	public SquadNode(Map<String, List<Object>> map) {
    		super(map);
    	}
    }
    
    public static class RandomChoiceNode extends SquadRCNode {
    	public RandomChoiceNode() {
    	}
    	
    	public RandomChoiceNode(Map<String, List<Object>> map) {
    		super(map);
    	}
    }
}