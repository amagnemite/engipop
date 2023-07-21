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
		else if(value == Collections.emptyList()) {
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
		if(!Arrays.asList(value).isEmpty()) {
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
	
	//converts child vdfnodes into <String, Object> maps
	public void copyVDFNode(Map<String, Object[]> map) {
		Set<String> stringSet = new HashSet<String>(
				Arrays.asList(WaveSpawnNode.NAME, WaveSpawnNode.WAITFORALLDEAD, WaveSpawnNode.WAITFORALLSPAWNED));
		final String Digits = "(\\p{Digit}+)"; //decimal digit one or more times
		final String fpRegex =
				("-?(" + //- 0 or 1 times
				"("+Digits+"(\\.)?("+Digits+"?))|" + //digits 1 or more times, . 0 or 1 times, digits 0 or 1 times
				"(\\.("+Digits+")))"); //. , digits
		/*
		Map<String, Object> newMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		
		for(Entry<String, Object[]> entry : map.entrySet()) {
			List<Object> nodeArray = new ArrayList<Object>();
			
			for(int i = 0; i < entry.getValue().length; i++) {
				if(entry.getValue()[i] instanceof Map) {
					nodeArray.add(copyVDFNode((VDFNode) entry.getValue()[i]));
				}
				else if(!stringSet.contains(entry.getKey())) { //convert string numbers into ints/doubles
					String str = (String) entry.getValue()[i];
					if(str.contains(".") && Pattern.matches(fpRegex, str)) {
						nodeArray.add(Double.valueOf(str));
					}	
					else if(Pattern.matches(Digits, str)) {
						nodeArray.add(Integer.valueOf(str)); //this may also catch certain booleans/flags
					}
				}
			}
			if(entry.getValue().length == 1) {
				newMap.put(entry.getKey(), entry.getValue()[0]);
			}
			else {
				newMap.put(entry.getKey(), nodeArray);
			}
		} */
		for(Entry<String, Object[]> entry : map.entrySet()) {
            Object[] nodeArray = new Object[entry.getValue().length];
            
            for(int i = 0; i < entry.getValue().length; i++) {
                if(entry.getValue()[i].getClass() == VDFNode.class) {
                   // Map<String, Object[]> subMap = new TreeMap<String, Object[]>(String.CASE_INSENSITIVE_ORDER);
                    //subMap.putAll((VDFNode) entry.getValue()[i]);
                    copyVDFNode((VDFNode) entry.getValue()[i]);
                    nodeArray[i] = (entry.getValue()[i]);
                }
                else if(!stringSet.contains(entry.getKey())) { //convert string numbers into ints/doubles
                    String str = (String) entry.getValue()[i];
                    if(str.contains(".") && Pattern.matches(fpRegex, str)) {
                        nodeArray[i] = Double.valueOf(str);
                    }    
                    else if(Pattern.matches(Digits, str)) {
                        nodeArray[i] = (Integer.valueOf(str)); //this may also catch certain booleans/flags
                    }
                }
            }
            if(nodeArray[0] != null) { //may need to double check this
                entry.setValue(nodeArray);
            }
        }   
		
		//return newMap; 
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
    	public static final String TEMPLATE = "Template";
    	
    	private int mapIndex = -1;
    	private Map<String, Node> wsTemplateMap = new HashMap<String, Node>();
    	private Map<String, Node> botTemplateMap = new HashMap<String, Node>();

        public PopNode() {
        	this.putKey(STARTINGCURRENCY, 400);
        	this.putKey(RESPAWNWAVETIME, 6);
        	this.putKey(BUSTERDAMAGE, EngiPanel.BUSTERDEFAULTDMG);
        	this.putKey(BUSTERKILLS, EngiPanel.BUSTERDEFAULTKILLS);
        	this.putKey(BOTSATKINSPAWN, false);
        	this.putKey(FIXEDRESPAWNWAVETIME, false);
        	this.putKey(EVENTPOPFILE, false);
        	this.putKey(MISSION, new ArrayList<Node>());
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
        	
        	//may need to make sure this isn't a not string
        	if(keyVals.containsKey(EVENTPOPFILE) && 
        			((String) keyVals.get(EVENTPOPFILE)[0]).equals("Halloween")) {
        		keyVals.put(EVENTPOPFILE, new Object[] {true});
        	}
        	else {
        		keyVals.put(EVENTPOPFILE, new Object[] {false});
        	}
        	
        	if(keyVals.containsKey(BOTSATKINSPAWN)) {
        		String atk = (String) keyVals.get(BOTSATKINSPAWN)[0];
        		if((atk.equalsIgnoreCase("no") || atk.equalsIgnoreCase("false"))) {
        			keyVals.put(BOTSATKINSPAWN, new Object[] {false});
        		}
        	}
        	else {
        		keyVals.put(BOTSATKINSPAWN, new Object[] {true});
        	}
        	
        	if(keyVals.containsKey(FIXEDRESPAWNWAVETIME)) { //presence of flag is true
        		keyVals.put(FIXEDRESPAWNWAVETIME, new Object[] {true});
        	}
        	else {
        		keyVals.put(FIXEDRESPAWNWAVETIME, new Object[] {false});
        	}
        	
        	if(!keyVals.containsKey(BUSTERDAMAGE)) {
        		keyVals.put(BUSTERDAMAGE, new Object[] {EngiPanel.BUSTERDEFAULTDMG});
        	}
        	if(!keyVals.containsKey(BUSTERKILLS)) {
        		keyVals.put(BUSTERKILLS, new Object[] {EngiPanel.BUSTERDEFAULTKILLS});
        	}
        	
        	if(keyVals.containsKey(MISSION)) {
        		List<Node> array = new ArrayList<Node>();
        		for(Object mission : keyVals.get(MISSION)) {
        			array.add(new MissionNode((Map<String, Object[]>) mission));
        		}
        		keyVals.put(MISSION, new Object[] {array});
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
    	
    	public static final String DESTROYSENTRIES = "DestroySentries";
    	public static final String SNIPER = "Sniper";
    	public static final String SPY = "Spy";
    	public static final String ENGINEER = "Engineer";
    	//spawner
    	
    	public MissionNode() {
    		this.putKey(OBJECTIVE, DESTROYSENTRIES);
    		this.putKey(INITIALCOOLDOWN, 0);
    		this.putKey(COOLDOWNTIME, 0);
    		this.putKey(BEGINATWAVE, 1);
    		this.putKey(RUNFORTHISMANYWAVES, 1);
    		this.putKey(DESIREDCOUNT, 1);
    	}
    	
    	public MissionNode(Map<String, Object[]> map) {
    		keyVals.putAll(map);
    	}
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
    			if(entry.getKey().equals(TARGET)) {
    				this.putKey(TARGET, entry.getValue());
    			}
    			else if(entry.getKey().equals(ACTION)) {
    				this.putKey(ACTION, entry.getValue());
    			}
    			else if(entry.getKey().equals(PARAM)) { 
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
    	//TODO: fix support
    	
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
    		
    		if(!keyVals.containsKey(TOTALCOUNT)) {
    			this.putKey(TOTALCOUNT, 0);
    		}
    		if(!keyVals.containsKey(MAXACTIVE)) {
    			this.putKey(MAXACTIVE, 0);
    		}
    		if(!keyVals.containsKey(SPAWNCOUNT)) {
    			this.putKey(SPAWNCOUNT, 0);
    		}
    		if(!keyVals.containsKey(WAITBEFORESTARTING)) {
    			this.putKey(WAITBEFORESTARTING, 0.0);
    		}
    		if(!keyVals.containsKey(WAITBETWEENSPAWNS)) {
    			this.putKey(WAITBETWEENSPAWNS, 0.0);
    		}
    		if(!keyVals.containsKey(TOTALCURRENCY)) {
    			this.putKey(TOTALCURRENCY, 0);
    		}
    		
    		//need to check this 
    		if(keyVals.containsKey(SUPPORT)) {
    			Object supportVal = keyVals.get(SUPPORT)[0];
    			if(supportVal.getClass() == String.class) {
    				if(((String) supportVal).equalsIgnoreCase("LIMITED")) {
    					supportLimited = true;
    				}
    			}
    			keyVals.put(SUPPORT, new Object[] {true});
    		}
    		else {
    			keyVals.put(SUPPORT, new Object[] {false});
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
    	public static final String SKIN = "Skin"; //flag int
    	public static final String STARTINGPATHTRACKNODE = "StartingPathTrackNode"; //default ""
    	public static final String ONKILLEDOUTPUT = "OnKilledOutput";
    	public static final String ONBOMBDROPPEDOUTPUT = "OnBombDroppedOutput";
    	
    	public TankNode() {
    		this.putKey(HEALTH, EngiPanel.TANKDEFAULTHEALTH);
    		this.putKey(NAME, "tankboss");
    		this.putKey(SKIN, false);
    	}
    	
    	public TankNode(Map<String, Object[]> map) {
    		keyVals.putAll(map);
    		
    		//may need to check this
    		if(keyVals.containsKey(SKIN)) {
    			this.putKey(SKIN, true);
    		}
    		else {	
    			this.putKey(SKIN, false);
    		}
    		
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
    	
    	//private List<String> tags;
    	//private List<String> attributes;
    	//private String[] items;
    	private boolean isItemsSorted;
    	//private Map<ItemSlot, HashMap<String, String>> itemAttributeList;
    	
    	//consider allowing template only 
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
    		
    		if(keyVals.containsKey(CLASSNAME)) {
    			this.putKey(CLASSNAME, Classes.toClass((String) this.getValueSingular(CLASSNAME)));
    		}
    		else {
    			this.putKey(CLASSNAME, Classes.None);
    		}
    		
    		if(keyVals.containsKey(TAGS)) {
    			List<Object> list = new ArrayList<Object>(Arrays.asList(keyVals.get(TAGS)));
    			this.putKey(TAGS, list);
    		}
    		
    		if(keyVals.containsKey(ATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(Arrays.asList(keyVals.get(ATTRIBUTES)));
    			this.putKey(ATTRIBUTES, list);
    		}
    		
    		if(keyVals.containsKey(ITEM)) {
    			List<Object> list = new ArrayList<Object>(ITEMCOUNT);
    			list.addAll(Arrays.asList(keyVals.get(ITEM)));
    			this.putKey(ITEM, list);
    		}
    		
    		if(keyVals.containsKey(ITEMATTRIBUTES)) {
    			List<Object> list = new ArrayList<Object>(ITEMCOUNT);
    			list.addAll(Arrays.asList(keyVals.get(ITEMATTRIBUTES))); //list of map<string, object[]>
    			this.putKey(ITEMATTRIBUTES, list);
    		}
    		
    		if(!keyVals.containsKey(SKILL)) {
    			this.putKey(SKILL, NOSKILL);
    		}
    		
    		if(!keyVals.containsKey(WEAPONRESTRICT)) {
    			this.putKey(WEAPONRESTRICT, ANY);
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
    		keyVals.putAll(map);
    		
    		//may want to use generic spawners here
    		if(map.containsKey(WaveSpawnNode.TFBOT)) {
	    		for(Object key : map.get(WaveSpawnNode.TFBOT)) {
	    			TFBotNode node = new TFBotNode((Map<String, Object[]>) key);
	    			node.connectNodes(this);
	    		}
	    		keyVals.remove(WaveSpawnNode.TFBOT);
    		}
    	}
    }
    
    public static class RandomChoiceNode extends Node {
    	public RandomChoiceNode() {
    		
    	}
    	
    	public RandomChoiceNode(Map<String, Object[]> map) { 
    		keyVals.putAll(map);
    		
    		//may want to use generic spawners here
    		if(map.containsKey(WaveSpawnNode.TFBOT)) {
    			for(Object key : map.get(WaveSpawnNode.TFBOT)) {
        			TFBotNode node = new TFBotNode((Map<String, Object[]>) key);
        			node.connectNodes(this);
        		}
        		keyVals.remove(WaveSpawnNode.TFBOT);
    		}
    	}
    }
    
}