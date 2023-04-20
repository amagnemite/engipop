package engipop;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import engipop.EngiPanel.Classes;
import net.platinumdigitalgroup.jvdf.VDFNode;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
@SuppressWarnings("unchecked")
public class Node {
	private Node parent;
	private List<Node> children = new ArrayList<Node>();
	//protected Map<String, Object[]> keyVals = new HashMap<String, Object[]>(8);
	protected Map<String, Object[]> keyVals = new TreeMap<String, Object[]>(String.CASE_INSENSITIVE_ORDER);
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
		else if(value.getClass() == List.class && ((List<String>) value).isEmpty()) {
			keyVals.remove(key);
			return;
		}
		//else if(value.getClass() != String.class || 
		//	(value.getClass() == String.class && !value.equals(""))) {
			//if it is a string that is not empty or it is not a string
			
		//}
		keyVals.put(key, new Object[] {value});
	}
	
	//array value ver of above
	public void putKey(String key, Object[] value) {
		if(value.length > 0) {
			keyVals.put(key, value);
		}
		else {
			keyVals.remove(key);
		}
	}
	
	public Object getValueSingular(String key) {
		//if(keyVals.get(key).length == 1) {
		//	return keyVals.get(key);
		//}
		if(!keyVals.containsKey(key)) {
			return null;
		}
		else {
			return keyVals.get(key)[0];
		}
	}
	
	public Object[] getValueArray(String key) {
		return keyVals.get(key);
	}
	
	public boolean containsKey(String key) {
		return keyVals.containsKey(key);
	}
	
	public void removeKey(String key) {
		keyVals.remove(key);
	}
	
	public void printKeyVals() {
		for(Entry<String, Object[]> entry : keyVals.entrySet()) {
			System.out.print(entry.getKey());
			if(entry.getValue().length == 1) {
				System.out.println(" " + entry.getValue()[0]);
			}
			else {
				System.out.println(" " + entry.getValue());
			}
		}
	}
	
	//reconsider this
	public Map<String, Object[]> getMap() {
		return this.keyVals;
	}
	
	//converts child vdfnodes into case insensitive treemaps
	public void copyVDFNode(Map<String, Object[]> map) {
		Set<String> stringSet = new HashSet<String>(
				Arrays.asList(WaveSpawnNode.NAME, WaveSpawnNode.WAITFORALLDEAD, WaveSpawnNode.WAITFORALLSPAWNED));
		
		for(Entry<String, Object[]> entry : map.entrySet()) {
			if(entry.getValue()[0].getClass() == VDFNode.class) {
				//List<Map<String, Object[]>> nodeArray = new ArrayList<Map<String, Object[]>>(entry.getValue().length);
				Object[] nodeArray = new Object[entry.getValue().length];
				
				for(int i = 0; i < entry.getValue().length; i++) {
					Map<String, Object[]> subMap = new TreeMap<String, Object[]>(String.CASE_INSENSITIVE_ORDER);
					subMap.putAll((VDFNode) entry.getValue()[i]);
					copyVDFNode(subMap);
					//nodeArray.add(subMap);
					nodeArray[i] = subMap;
				}
				entry.setValue(nodeArray);
				//entry.setValue(new Object[] {nodeArray});
			}
			else if(!stringSet.contains(entry.getKey())) {
				final String Digits = "(\\p{Digit}+)";
				//decimal digit one or more times

				final String fpRegex =
					("-?(" + //- 0 or 1 times
					"("+Digits+"(\\.)?("+Digits+"?))|" + //digits 1 or more times, . 0 or 1 times, digits 0 or 1 times
					"(\\.("+Digits+")))"); //. , digits

				if (Pattern.matches(fpRegex, myString)) {
					Double.valueOf(myString); // Will not throw NumberFormatException
				}	
				else {
				// Perform suitable alternative action
				}	
			}
		}	
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
    	
    	private int mapIndex = -1;
    	private Map<String, Node> wsTemplateMap;
    	private Map<String, Node> botTemplateMap;

        public PopNode() {
        	this.putKey(STARTINGCURRENCY, 400);
        	this.putKey(RESPAWNWAVETIME, 6);
        	this.putKey(BUSTERDAMAGE, 3000);
        	this.putKey(BUSTERKILLS, 15);
        	this.putKey(BOTSATKINSPAWN, false);
        	this.putKey(FIXEDRESPAWNWAVETIME, false);
        	this.putKey(EVENTPOPFILE, false);
        	//this.putKey(ADVANCED, false);
        }
        
		public PopNode(Map<String, Object[]> map) { //constructor for read in nodes  	
        	this.copyVDFNode(map);
        	keyVals.putAll(map);
        	
        	if(keyVals.containsKey("Wave")) {
        		for(Object wave : keyVals.get("Wave")) {
        			WaveNode waveNode = new WaveNode((Map<String, Object[]>) wave);
					waveNode.connectNodes(this);
        		}
        		keyVals.remove("Wave");
        	}
        	
        	if(((String) keyVals.get(EVENTPOPFILE)[0]).equalsIgnoreCase("Halloween")) {
        		keyVals.put(EVENTPOPFILE, new Object[] {true});
        	}
        	else {
        		keyVals.put(EVENTPOPFILE, new Object[] {false});
        	}
        	
        	String atk = (String) keyVals.get(BOTSATKINSPAWN)[0];
        	if(atk != null && (atk.equalsIgnoreCase("no") || atk.equalsIgnoreCase("false"))) {
        		keyVals.put(BOTSATKINSPAWN, new Object[] {false});
        	}
        	else {
        		keyVals.put(BOTSATKINSPAWN, new Object[] {true});
        	}
        	
        	if(keyVals.containsKey(FIXEDRESPAWNWAVETIME)) {
        		keyVals.put(FIXEDRESPAWNWAVETIME, new Object[] {true});
        	}
        	else {
        		keyVals.put(FIXEDRESPAWNWAVETIME, new Object[] {false});
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
    	public static final String WHERE = "Where"; //string
    	public static final String OBJECTIVE = "Objective"; //string
    	public static final String INITIALCOOLDOWN = "InitialCooldown"; //float
    	public static final String COOLDOWNTIME = "CooldownTime"; //float
    	public static final String BEGINATWAVE = "BeginAtWave"; //int
    	public static final String RUNFORTHISMANYWAVES = "RunForThisManyWaves"; //int
    	public static final String DESIREDCOUNT = "DesiredCount"; //int
    	//spawner
    }
    
    public static class WaveNode extends Node { 
    	//todo: sound support
    	//description?
    	//waitwhendone and checkpoint are both vestigal
    	public static final String STARTWAVEOUTPUT = "StartWaveOutput";
    	public static final String DONEOUTPUT = "DoneOutput";
    	public static final String INITWAVEOUTPUT = "InitWaveOutput";
    	
    	public WaveNode() {
    		this.putKey(STARTWAVEOUTPUT, new RelayNode());
    		this.putKey(DONEOUTPUT, new RelayNode());
    	}
    	
        public WaveNode(Map<String, Object[]> map) { //readin node, key case should already be converted
        	keyVals.putAll(map);
        	
        	if(keyVals.containsKey("WaveSpawn")) {
        		for(Object wavespawn : keyVals.get("WaveSpawn")) {
        			WaveSpawnNode waveSpawnNode = new WaveSpawnNode((Map<String, Object[]>) wavespawn);
					waveSpawnNode.connectNodes(this);
        		}
        		keyVals.remove("WaveSpawn");
        	}
        	
        	if(keyVals.containsKey(STARTWAVEOUTPUT)) {
        		RelayNode start = new RelayNode((Map<String, Object[]>) keyVals.get(STARTWAVEOUTPUT)[0]);
        		keyVals.put(STARTWAVEOUTPUT, new Object[] {start});
        	}
        	
        	if(keyVals.containsKey(DONEOUTPUT)) {
        		RelayNode done = new RelayNode((Map<String, Object[]>) keyVals.get(DONEOUTPUT)[0]);
        		keyVals.put(DONEOUTPUT, new Object[] {done});
        	}
        	
        	if(keyVals.containsKey(INITWAVEOUTPUT)) {
        		RelayNode init = new RelayNode((Map<String, Object[]>) keyVals.get(INITWAVEOUTPUT)[0]);
        		keyVals.put(INITWAVEOUTPUT, new Object[] {init});
        	}
        }
    }
    
    public static class RelayNode extends Node { //something experimental
    	public static final String TARGET = "Target";
    	public static final String ACTION = "Action";
    	public static final String PARAM = "Param";
    	public static final String DELAY = "Delay";
    	
    	public RelayNode() { //for now, assume all actions are trigger
    		//this.putKey(TARGET, null);
			this.putKey(ACTION, "trigger");
		}
    	
    	public RelayNode(Map<String, Object[]> map) {
    		for(Entry<String, Object[]> entry : map.entrySet()) {
    			if(entry.getKey().equalsIgnoreCase(TARGET)) {
    				this.putKey(TARGET, entry.getValue());
    			}
    			else if(entry.getKey().equalsIgnoreCase(ACTION)) {
    				this.putKey(ACTION, entry.getValue());
    			}
    			else if(entry.getKey().equalsIgnoreCase(PARAM)) { 
    				this.putKey(PARAM, entry.getValue());
    			}
    			else { //delay
    				this.putKey(DELAY, entry.getValue());
    			}
    		}
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
    	
    	private boolean waitBetweenDeaths; //betweenspawns and betweenspawnsafterdeath are mutually exclusive
    	private boolean supportLimited;
    	
    	public WaveSpawnNode() {
    		this.putKey(WHERE, "spawnbot");
    		this.putKey(TOTALCOUNT, 1);
    		this.putKey(MAXACTIVE, 1);
    		this.putKey(SPAWNCOUNT, 1);
    		this.putKey(WAITBEFORESTARTING, 0.0);
    		this.putKey(WAITBETWEENSPAWNS, 0.0);
    		this.putKey(TOTALCURRENCY, 0);
    		this.putKey(SUPPORT, false);
    	}
    	
    	public WaveSpawnNode(Map<String, Object[]> map) {
    		keyVals.putAll(map);
    		
    		if(keyVals.containsKey(WAITBETWEENSPAWNSAFTERDEATH)) {
    			waitBetweenDeaths = true;
    		}
    		
    		//need to check this 
    		if(keyVals.containsKey(SUPPORT)) {
    			Object supportVal = keyVals.get(SUPPORT)[0];
    			if(supportVal.getClass() == String.class) {
    				if(((String) supportVal).equalsIgnoreCase("LIMITED")) {
    					supportLimited = true;
    				}
    			}
    		}
    		
    		if(keyVals.containsKey(STARTWAVEOUTPUT)) {
    			this.putKey(STARTWAVEOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(STARTWAVEOUTPUT)));
    		}
    		if(keyVals.containsKey(FIRSTSPAWNOUTPUT)) {
    			this.putKey(FIRSTSPAWNOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(FIRSTSPAWNOUTPUT)));
    		}
    		if(keyVals.containsKey(LASTSPAWNOUTPUT)) {
    			this.putKey(LASTSPAWNOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(LASTSPAWNOUTPUT)));
    		}
    		if(keyVals.containsKey(DONEOUTPUT)) {
    			this.putKey(DONEOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(DONEOUTPUT)));
    		}
    		
    		if(keyVals.containsKey(TFBOT)) {
    			TFBotNode node = new TFBotNode((Map<String, Object[]>) keyVals.get(TFBOT)[0]);
    			node.connectNodes(this);
    			keyVals.remove(TFBOT);
    		}
    		else if(keyVals.containsKey(TANK)) {
    			TankNode node = new TankNode((Map<String, Object[]>) keyVals.get(TANK)[0]);
    			node.connectNodes(this);
    			keyVals.remove(TANK);
    		}
    		else if(keyVals.containsKey(SQUAD)) {
    			SquadNode node = new SquadNode((Map<String, Object[]>) keyVals.get(SQUAD)[0]);
    			node.connectNodes(this);
    			keyVals.remove(SQUAD);
    		}
    		else if(keyVals.containsKey(RANDOMCHOICE)) {
    			RandomChoiceNode node = new RandomChoiceNode((Map<String, Object[]>) keyVals.get(RANDOMCHOICE)[0]);
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
    }
    
    public static class TankNode extends Node {
    	public static final String HEALTH = "Health"; //int
    	public static final String SPEED = "Speed"; //float
    	public static final String NAME = "Name"; //string
    	public static final String SKIN = "Skin"; //flag
    	public static final String STARTINGPATHTRACKNODE = "StartingPathTrackNode"; //default ""
    	public static final String ONKILLEDOUTPUT = "OnKilledOutput";
    	public static final String ONBOMBDROPPEDOUTPUT = "OnBombDroppedOutput";
    	
    	public TankNode() {
    		this.putKey(HEALTH, EngiPanel.tankDefaultHealth);
    		this.putKey(NAME, "tankboss");
    		this.putKey(SKIN, false);
    	}
    	
    	public TankNode(Map<String, Object[]> map) {
    		keyVals.putAll(map);
    		
    		if(keyVals.containsKey(ONKILLEDOUTPUT)) {
    			this.putKey(ONKILLEDOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(ONKILLEDOUTPUT)));
    		}
    		
    		if(keyVals.containsKey(ONBOMBDROPPEDOUTPUT)) {
    			this.putKey(ONBOMBDROPPEDOUTPUT, 
    				new RelayNode((Map<String, Object[]>) this.getValueSingular(ONBOMBDROPPEDOUTPUT)));
    		}
    	}
    }
    
    public static class TFBotNode extends Node { //node for tfbot spawners
    	
    	public static final String CLASSNAME = "Class"; //class 
    	public static final String CLASSICON = "ClassIcon"; //string
    	public static final String NAME = "Name"; //string
    	public static final String SKILL = "Skill"; //skill
    	public static final String WEAPONRESTRICT = "WeaponRestriction"; //weaponrestriction
    	public static final String TAGS = "Tag"; //List<String>
    	public static final String ATTRIBUTES = "Attribute";  //List<String>
    	public static final String ITEM = "Item"; //String[]
    	public static final String ITEMATTRIBUTES = "ItemAttributes"; //List<HashMap<String, String>>
		//when read from VDF, is TreeMap[]
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
    	
    	public static final int ITEMCOUNT = 7;
    	
    	public static final String EASY = "Easy";
    	public static final String NORMAL = "Normal";
    	public static final String HARD = "Hard";
    	public static final String EXPERT = "Expert";
    	
    	public static final String ANY = "Any";
    	public static final String PRIMARYONLY = "PrimaryOnly";
    	public static final String SECONDARYONLY = "SecondaryOnly";
    	public static final String MELEEONLY = "MeleeOnly";
    	
    	//private List<String> tags;
    	//private List<String> attributes;
    	//private String[] items;
    	private boolean isItemsSorted;
    	//private Map<ItemSlot, HashMap<String, String>> itemAttributeList;
    	
    	public TFBotNode() { //defaults
    		this.putKey(CLASSNAME, Classes.Scout);
    		this.putKey(CLASSICON, "scout");
    		this.putKey(SKILL, EASY);
    		this.putKey(WEAPONRESTRICT, ANY);
    		isItemsSorted = true;
    	}
    	 
    	//alternate constructor for read in tfbots
    	public TFBotNode(Map<String, Object[]> map) {
    		keyVals.putAll(map);
    		
    		for(Entry<String, Object[]> entry : keyVals.entrySet()) {
    			if(entry.getValue().length > 1) {
    				//converts an array to a list to an array containing a singular list
    				List<Object> list = new ArrayList<Object>(Arrays.asList(entry.getValue()));
    				entry.setValue(new Object[] {list});
    			}
    		}
    	}
    	
    	public boolean isItemsSorted() {
    		return isItemsSorted;
    	}
    	
    	public void setItemsSorted(boolean isItemsSorted) {
    		this.isItemsSorted = isItemsSorted;
    	}
    	
    	/*
    	public void setTags(List<String> list) {
    		this.tags = list;
    	}
    	
    	public List<String> getTags() {
    		return this.tags;
    	}
    	
    	public void setAttributes(List<String> list) {
    		this.attributes = list;
    	}
    	
    	public List<String> getAttributes() {
    		return this.attributes;
    	}
    	
    	//updates the entire itemattribute map
    	public void setItemAttributeList(HashMap<EngiPanel.ItemSlot, HashMap<String, String>> list) {
    		this.itemAttributeList = list;
    	}
    	
    	//gets the entire itemattribute map
    	public Map<EngiPanel.ItemSlot, HashMap<String, String>> getItemAttributeList() {
    		return this.itemAttributeList;
    	}
    	
    	public void setItemArray(String[] items) {
    		this.items = items;
    	}
    	
    	public String[] getItemArray() {
    		return this.items;
    	}
    	*/
    }
    
    //these two are mostly convenience 
    public static class SquadNode extends Node {
    	public SquadNode() {
    		
    	}
    	
    	public SquadNode(Map<String, Object[]> map) {
    		boolean match = false;
    		String currentKey = "";
    		
    		keyVals.putAll(map);
    		
    		//may want to use generic spawners here
    		for(String key : keyVals.keySet()) {
    			if(key.equalsIgnoreCase(WaveSpawnNode.TFBOT) && !key.equals(WaveSpawnNode.TFBOT)) {
    				match = true;
    				currentKey = key;
    			}
    		}
    		
    		if(match) {
    			keyVals.put(WaveSpawnNode.TFBOT, keyVals.remove(currentKey));
    		}
    	}
    }
    
    public static class RandomChoiceNode extends Node {
    	public RandomChoiceNode() {
    		
    	}
    	
    	public RandomChoiceNode(Map<String, Object[]> map) { 
    		boolean match = false;
    		String currentKey = "";
    		
    		keyVals.putAll(map);
    		
    		for(String key : keyVals.keySet()) {
    			if(key.equalsIgnoreCase(WaveSpawnNode.TFBOT) && !key.equals(WaveSpawnNode.TFBOT)) {
    				match = true;
    				currentKey = key;
    			}
    		}
    		
    		if(match) {
    			keyVals.put(WaveSpawnNode.TFBOT, keyVals.remove(currentKey));
    		}
    	}
    }
    
}