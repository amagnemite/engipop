package engipop;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import engipop.EngiPanel.Classes;
import engipop.PopulationParser.TemplateData;
import net.platinumdigitalgroup.jvdf.VDFNode;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
@SuppressWarnings("unchecked")
public class Node implements Serializable {
	private static final long serialVersionUID = 1029176597406135628L;
	private transient Node parent;
	private List<Node> children = new ArrayList<Node>();
	protected Map<String, List<Object>> keyVals = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
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
				Arrays.asList(WaveSpawnNode.NAME, WaveSpawnNode.WAITFORALLDEAD, WaveSpawnNode.WAITFORALLSPAWNED));
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
                else if(!stringSet.contains(entry.getKey())) { //convert string numbers into ints/doubles
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
		private static final long serialVersionUID = 8298901116813898839L;
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
    	
    	private transient int mapIndex = -1;
    	private Map<String, Node> wsTemplateMap = new HashMap<String, Node>();
    	private Map<String, Node> botTemplateMap = new HashMap<String, Node>();

        public PopNode() {
        	putKey(STARTINGCURRENCY, 400);
        	putKey(RESPAWNWAVETIME, 6);
        	putKey(BUSTERDAMAGE, EngiPanel.BUSTERDEFAULTDMG);
        	putKey(BUSTERKILLS, EngiPanel.BUSTERDEFAULTKILLS);
        	putKey(BOTSATKINSPAWN, false);
        	putKey(FIXEDRESPAWNWAVETIME, false);
        	putKey(EVENTPOPFILE, false);
        	putKey(MISSION, new ArrayList<Node>());
        	//putKey(ADVANCED, false);
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
        		putKey(BUSTERDAMAGE, EngiPanel.BUSTERDEFAULTDMG);
        	}
        	if(!keyVals.containsKey(BUSTERKILLS)) {
        		putKey(BUSTERKILLS, EngiPanel.BUSTERDEFAULTKILLS);
        	}
        	
        	if(keyVals.containsKey(MISSION)) {
        		List<Node> array = new ArrayList<Node>();
        		for(Object mission : keyVals.get(MISSION)) {
        			array.add(new MissionNode((Map<String, List<Object>>) mission));
        		}
        		putKey(MISSION, array);
        	}
        	
        	if(keyVals.containsKey(TEMPLATE)) {
        		List<Object> list = keyVals.get(TEMPLATE);
        		
        		for(Entry<String, Object> node : ((Map<String, Object>) list.get(0)).entrySet()) {
        			//i love casting i love casting
        			List<Object> sublist = (List<Object>) node.getValue();
        			Map<String, List<Object>> keyvals = (Map<String, List<Object>>) sublist.get(0);
        			Set<String> subset = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        			subset.addAll(TFBotNode.getNodeKeyList());
        			subset.remove(TFBotNode.NAME);
        			subset.remove(TFBotNode.TEMPLATE);
        			
        			if(subset.removeAll(keyvals.keySet())) {
        				botTemplateMap.put(node.getKey(), new TFBotNode(keyvals));
        			}
        			else {
        				wsTemplateMap.put(node.getKey(), new WaveSpawnNode(keyvals));
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
        	return this.wsTemplateMap;
        }
        
        public void setBotTemplateMap(Map<String, Node> map) {
        	this.botTemplateMap = map;
        }
        
        public Map<String, Node> getBotTemplateMap() {
        	return this.botTemplateMap;
        }
    
        public List<String> printNode() {
        	List<String> list = new ArrayList<String>(Arrays.asList(STARTINGCURRENCY, RESPAWNWAVETIME, FIXEDRESPAWNWAVETIME,
        			BUSTERDAMAGE, BUSTERKILLS, BOTSATKINSPAWN, EVENTPOPFILE, TEMPLATE, MISSION));
        	
    		return list;
        }
    }
    
    /*
    //node for template info
    public static class TemplateNode extends Node {
    	private Map<String, Node> templateMap = new TreeMap<String, Node>();
    	
    	public void putTemplate(String name, Node template) {
    		templateMap.put(name, template);
    	}
    	
    	public Map<String, Node> getMap() {
    		return this.templateMap;
    	}
    } */
    
    public static class MissionNode extends Node {
		private static final long serialVersionUID = 1757828183752920816L;
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
    			TFBotNode botNode = new TFBotNode((Map<String, List<Object>>) this.getValue("TFBOT"));
    			botNode.connectNodes(this);
        		this.removeKey("TFBOT");
    		}
    	}
    	
    	public List<String> printNode() {
    		List<String> keyValList = new ArrayList<String>(Arrays.asList(WHERE, OBJECTIVE, INITIALCOOLDOWN, COOLDOWNTIME, BEGINATWAVE,
    				RUNFORTHISMANYWAVES, DESIREDCOUNT));
    		
    		return keyValList;
    	}
    }
    
    public static class WaveNode extends Node { 
		private static final long serialVersionUID = 5277478750013077900L;
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
        	
        	if(this.containsKey("WaveSpawn")) {
        		for(Object wavespawn : keyVals.get("WaveSpawn")) {
        			WaveSpawnNode waveSpawnNode = new WaveSpawnNode((Map<String, List<Object>>) wavespawn);
					waveSpawnNode.connectNodes(this);
        		}
        		keyVals.remove("WaveSpawn");
        	}
        	
        	if(this.containsKey(STARTWAVEOUTPUT)) {
        		RelayNode start = new RelayNode((Map<String, List<Object>>) this.getValue(STARTWAVEOUTPUT));
        		putKey(STARTWAVEOUTPUT, start);
        	}
        	
        	if(keyVals.containsKey(DONEOUTPUT)) {
        		RelayNode done = new RelayNode((Map<String, List<Object>>) this.getValue(DONEOUTPUT));
        		putKey(DONEOUTPUT, done);
        	}
        	
        	if(keyVals.containsKey(INITWAVEOUTPUT)) {
        		RelayNode init = new RelayNode((Map<String, List<Object>>) this.getValue(INITWAVEOUTPUT));
        		putKey(INITWAVEOUTPUT, init);
        	}
        }
        
        public List<String> getNode() {
        	return new ArrayList<String>(Arrays.asList(STARTWAVEOUTPUT, DONEOUTPUT, INITWAVEOUTPUT));
        }
    }
    
    public static class RelayNode extends Node { //something experimental
		private static final long serialVersionUID = 6997941017069474364L;
		public static final String TARGET = "Target";
    	public static final String ACTION = "Action";
    	public static final String PARAM = "Param";
    	public static final String DELAY = "Delay";
    	
    	public RelayNode() { //for now, assume all actions are trigger
    		//putKey(TARGET, null);
			putKey(ACTION, "trigger");
		}
    	
    	public RelayNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    	}
    	
    	public List<String> getNodeKeyList() {
        	return new ArrayList<String>(Arrays.asList(TARGET, ACTION, PARAM, DELAY));
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
    
    public static class WaveSpawnNode extends Node { //node for wavespawns
		private static final long serialVersionUID = 8227518791283123088L;
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
    	
    	public static final String TFBOT = "TFBot";
    	public static final String TANK = "Tank";
    	public static final String SQUAD = "Squad";
    	public static final String RANDOMCHOICE = "RandomChoice";
    	//TODO: fix support
    	
    	private transient boolean waitBetweenDeaths; //betweenspawns and betweenspawnsafterdeath are mutually exclusive
    	private transient boolean supportLimited;
    	
    	public WaveSpawnNode() {
    		putKey(WHERE, "spawnbot");
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
    		if(!keyVals.containsKey(WAITBETWEENSPAWNS)) {
    			putKey(WAITBETWEENSPAWNS, 0.0);
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
    	
    	public Node getSpawner() {
    		return this.getChildren().get(0);
    	}
    	
    	//precheck spawner type
    	//this assumes the child is valid
    	public SpawnerType getSpawnerType() {
    		Node node = getSpawner();
    		SpawnerType type = null;
    		
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
    	
    	public static List<String> getNodeKeyList() {
    		return new ArrayList<String>(Arrays.asList(NAME, WHERE, TOTALCOUNT, MAXACTIVE, SPAWNCOUNT, TOTALCURRENCY, WAITBEFORESTARTING,
    				WAITBETWEENSPAWNS, WAITBETWEENSPAWNSAFTERDEATH, WAITFORALLSPAWNED, WAITFORALLDEAD, SUPPORT, STARTWAVEOUTPUT,
    					FIRSTSPAWNOUTPUT, LASTSPAWNOUTPUT, DONEOUTPUT, TFBOT, TANK, SQUAD, RANDOMCHOICE));
    	}
    }
    
    public static class TankNode extends Node {
		private static final long serialVersionUID = -4923846338890204356L;
		public static final String HEALTH = "Health"; //int
    	public static final String SPEED = "Speed"; //float
    	public static final String NAME = "Name"; //string
    	public static final String SKIN = "Skin"; //flag int
    	public static final String STARTINGPATHTRACKNODE = "StartingPathTrackNode"; //default ""
    	public static final String ONKILLEDOUTPUT = "OnKilledOutput";
    	public static final String ONBOMBDROPPEDOUTPUT = "OnBombDroppedOutput";
    	
    	public TankNode() {
    		putKey(HEALTH, EngiPanel.TANKDEFAULTHEALTH);
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
		private static final long serialVersionUID = -4048312032891579094L;
		public static final String CLASSNAME = "Class"; //class 
    	public static final String CLASSICON = "ClassIcon"; //string
    	public static final String NAME = "Name"; //string
    	public static final String SKILL = "Skill"; //skill
    	public static final String WEAPONRESTRICT = "WeaponRestriction"; //weaponrestriction
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
    	
    	private boolean isItemsSorted;
    	
    	//consider allowing template only 
    	public TFBotNode() { //defaults
    		putKey(CLASSNAME, Classes.Scout);
    		putKey(CLASSICON, "scout");
    		putKey(SKILL, EASY);
    		putKey(WEAPONRESTRICT, ANY);
    		//putKey(ITEMATTRIBUTES, new ArrayList<Map<String, String>>(Arrays.asList(null, null, null, null, null, null, null)));
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
    		
    		if(keyVals.containsKey(ATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(keyVals.get(ATTRIBUTES));
    			putKey(ATTRIBUTES, list);
    		}
    		
    		if(keyVals.containsKey(ITEM)) {
    			List<Object> list = new ArrayList<Object>(ITEMCOUNT);
    			list.addAll(keyVals.get(ITEM));
    			putKey(ITEM, list);
    		}
    		
    		//this is sorted in botpanel, maybe shouldn't be
    		//post copyvdfnode = arraylist of map<string, arraylist<object>>s
    		//so need to copy into an arraylist of map<string, string>
    		if(keyVals.containsKey(ITEMATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(ITEMCOUNT);
    			for(Object value : keyVals.get(ITEMATTRIBUTES)) {
    				Map<String, List<Object>> attrMap = (Map<String, List<Object>>) value;
    				Map<String, String> newMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    				
    				for(Entry<String, List<Object>> entry : attrMap.entrySet()) {
    					//also post int/double conversion so need to reconvert
    					newMap.put(entry.getKey(), entry.getValue().get(0).toString());	
    				}
    				list.add(newMap);
    			}
    			putKey(ITEMATTRIBUTES, list);
    		}
    		
    		if(keyVals.containsKey(CHARACTERATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(1);
				Map<String, String> newMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
				
				for(Entry<String, List<Object>> entry : ((Map <String, List<Object>>) this.getValue(CHARACTERATTRIBUTES)).entrySet()) {
					//also post int/double conversion so need to reconvert
					newMap.put(entry.getKey(), entry.getValue().get(0).toString());	
				}
				list.add(newMap);
    			putKey(CHARACTERATTRIBUTES, list);
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
    }
    
    //these two are mostly convenience 
    public static class SquadNode extends Node {
		private static final long serialVersionUID = -5640678276809303546L;

		public SquadNode() {
    		
    	}
    	
    	public SquadNode(Map<String, List<Object>> map) {
    		keyVals.putAll(map);
    		
    		//may want to use generic spawners here
    		if(map.containsKey(WaveSpawnNode.TFBOT)) {
	    		for(Object key : map.get(WaveSpawnNode.TFBOT)) {
	    			TFBotNode node = new TFBotNode((Map<String, List<Object>>) key);
	    			node.connectNodes(this);
	    		}
	    		keyVals.remove(WaveSpawnNode.TFBOT);
    		}
    	}
    }
    
    public static class RandomChoiceNode extends Node {
		private static final long serialVersionUID = -7656129117713922281L;

		public RandomChoiceNode() {
    		
    	}
    	
    	public RandomChoiceNode(Map<String, List<Object>> map) { 
    		keyVals.putAll(map);
    		
    		//may want to use generic spawners here
    		if(map.containsKey(WaveSpawnNode.TFBOT)) {
    			for(Object key : map.get(WaveSpawnNode.TFBOT)) {
        			TFBotNode node = new TFBotNode((Map<String, List<Object>>) key);
        			node.connectNodes(this);
        		}
        		keyVals.remove(WaveSpawnNode.TFBOT);
    		}
    	}
    }
    
}