package engipop;
import java.util.*;
import java.util.Map.Entry;

import engipop.EngiPanel.Classes;
import engipop.EngiPanel.ItemSlot;
import net.platinumdigitalgroup.jvdf.VDFBindField;
import net.platinumdigitalgroup.jvdf.VDFNode;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
public class Tree {
	
    private Node root;

    public Tree(Node PopNode) {
        root = PopNode;
        
    }
    
    public Node getRoot() {
    	return this.root;
    }
    /*
    public static boolean checkIfNullOrEmpty(Object obj) {
		boolean check = false;
		
		if(obj == null) {
			check
		}
		
	} */
    
    public enum SpawnerType {
    	TFBOT, TANK, SQUAD, RANDOMCHOICE
    }

    public static class Node {
        private Node parent;
        private List<Node> children = new ArrayList<Node>();
        //children refers to waveschedule - wave - wavespawn - spawner connections
        //side connections like relays are connected elsewhere
        
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
        
        public void setParent(Node parent) {
        	this.parent = parent;
        }
 
    }
    
    public static class PopNode extends Node { //
    	private int mapIndex = -1;
    	private int startingCurrency = 400; //default 0
    	private int respawnWaveTime = 6; //default 10
    	private boolean fixedRespawnWaveTime;
    	private boolean EventPopfile; //this is a string, convert this later
    	private int busterDmg = 3000; //default 3k
    	private int busterKills = 15; //default 15
    	private boolean botsAtkInSpawn; //another string
    	private boolean advanced;
    	private Map<String, WaveSpawnNode> wsTemplateMap;
    	//= new HashMap<String, WaveSpawnNode>();
    	private Map<String, TFBotNode> botTemplateMap;
    	//= new HashMap<String, TFBotNode>();
        
        public PopNode() {
        	
        }
        
        public void setCurrency(int c) {
        	this.startingCurrency = c;
        }
        
        public int getCurrency() {
        	return this.startingCurrency;
        }
        
        public void setWaveTime(int w) {
        	this.respawnWaveTime = w;
        }
        
        public int getWaveTime() {
        	return this.respawnWaveTime;
        }
        
        public void setFixedWaveTime(boolean w) {
        	this.fixedRespawnWaveTime = w;
        }
        
        public boolean getFixedWaveTime() {
        	return this.fixedRespawnWaveTime;
        }
        
        public void setEventPop(boolean e) {
        	this.EventPopfile = e;
        }
        
        public boolean getEventPop() {
        	return this.EventPopfile;
        }
        
        public void setBusterDmg(int d) {
        	this.busterDmg = d;
        }
        
        public int getBusterDmg() {
        	return this.busterDmg;
        }
        
        public void setBusterKills(int k) {
        	this.busterKills = k;
        }
        
        public int getBusterKills() {
        	return this.busterKills;
        }
        
        public void setAtkInSpawn(boolean a) {
        	this.botsAtkInSpawn = a;
        }
        
        public boolean getAtkInSpawn() {
        	return this.botsAtkInSpawn;
        }
        
        public void setAdvanced(boolean a) {
        	this.advanced = a;
        }
        
        public boolean getAdvanced() {
        	return this.advanced;
        }
        
        public void setMapIndex(int i) {
        	this.mapIndex = i;
        }
        
        public int getMapIndex() {
        	return this.mapIndex;
        }
        
        public void setWSTemplateMap(Map<String, WaveSpawnNode> map) {
        	this.wsTemplateMap = map;
        }
        
        public Map<String, WaveSpawnNode> getWSTemplateMap() {
        	return this.wsTemplateMap;
        }
        
        public void setBotTemplateMap(Map<String, TFBotNode> map) {
        	this.botTemplateMap = map;
        }
        
        public Map<String, TFBotNode> getBotTemplateMap() {
        	return this.botTemplateMap;
        }
    }
    
    //node for template info
    public static class TemplateNode extends Node {
    	private Map<String, Node> templateMap = new TreeMap<String, Node>();
    	
    	public void putTemplate(String name, Node template) {
    		templateMap.put(name, template);
    	}
    	
    	public Map<String, Node> getMap() {
    		return this.templateMap;
    	}
    }
    
    public static class TemplateInfoNode extends Node {
    	private String nodename;
    	
    	public TemplateInfoNode(String nodename) {
    		this.nodename = nodename;
    	}
    }
    
    public static class MissionNode extends Node {
    	
    }
    
    public static class WaveNode extends Node { 
    	//todo: sound support
    	//description?
    	//waitwhendone and checkpoint are both vestigal
    	private RelayNode start = new RelayNode();
    	private RelayNode done = new RelayNode();
    	private RelayNode init;
    	
    	public void setStart(RelayNode s) {
    		this.start = s;
    	}
    	
    	public RelayNode getStart() {
    		return this.start;
    	}
    	
    	public void setDone(RelayNode s) {
    		this.done = s;
    	}
    	
    	public RelayNode getDone() {
    		return this.done;
    	}
    	
    	public void setInit(RelayNode s) {
    		this.init = s;
    	}
    	
    	public RelayNode getInit() {
    		return this.init;
    	}
    }
    
    public static class RelayNode extends Node { //something experimental
    	Map<String, String> map = new HashMap<String, String>();
    	
    	public RelayNode() { //for now, assume all actions are trigger
    		map.put("Target", null); //mostly for nice formatting
			map.put("Action", "trigger");
		}
    	
    	public void setTarget(String relay) {
    		map.put("Target", relay);
    	}
    	
    	public String getTarget() {
    		return map.get("Target");
    	}
    	
    	public Map<String, String> getMap() {
    		return map;
    	}
    	
    	public boolean isTargetEmptyOrNull() {
    		boolean check = false;
    		
    		if(map.get("Target") == null) {
    			check = true;
    		}
    		else {
    			check = map.get("Target").isEmpty() ? true : false;
    		}
    		return check;
    	}
    }
    
    public static class StartWaveOutputNode extends RelayNode { 
    	
    }
    
    public static class DoneOutputNode extends RelayNode {
   
    }
    
    public static class InitWaveOutputNode extends RelayNode {
    	   
    }
    
    public static class FirstWaveOutputNode extends RelayNode {
 	   
    }
    
    public static class LastWaveOutputNode extends RelayNode {
  	   
    }
    
    public static class WaveSpawnNode extends Node { //node for wavespawns
    	
    	public enum WaveSpawnKeys {
    		WHERE, TOTALCOUNT, MAXACTIVE,
    		SPAWNCOUNT, WAITBEFORESTARTING,
    		WAITBETWEENSPAWNS, WAITBETWEENDEATHS,
    		TOTALCURRENCY, NAME, WAITFORALLDEAD,
    		WAITFORALLSPAWNED, SUPPORT, SUPPORTLIMITED,
    		WAVESTARTOUTPUT, FIRSTSPAWNOUTPUT, LASTSPAWNOUTPUT,
    		DONEOUTPUT, TEMPLATE
    	}
    	
    	//where, name, waitforalldead, waitforallspawned, template = string
    	//totalcount, maxactive, spawncount, totalcurrency = int
    	//waitbeforestarting, waitbetweenspawns = double
    	//waitbetweendeaths, support, supportlimited = boolean
    	
    	private Map<WaveSpawnKeys, Object> keyVals = new HashMap<WaveSpawnKeys, Object>();
    	
    	/*
    	private String where = "spawnbot"; //todo: add support for the weird locations no one uses
    	private int totalCount = 1; //default 0
    	private int maxActive = 1; //default 999
    	private int spawnCount = 1; //default 1
    	private double waitBeforeStarting;
    	private double waitBetweenSpawns;
    	private boolean waitBetweenDeaths; //betweenspawns and betweenspawnsafterdeath are mutually exclusive
    	//todo: add wavespawn sounds/outputs as optional feature
    	private int totalCurrency; //default -1
    	private String name;
    	private String waitForAllDead; 
    	private String waitForAllSpawned;
    	private boolean support;
    	private boolean supportLimited;
    	//private boolean randomSpawn; //default 0, if true allows bot spawns to be randomized
    	//prob should be disabled for single spawn maps
    	private RelayNode start;
    	private RelayNode first;
    	private RelayNode last;
    	private RelayNode done; */
    	
    	public WaveSpawnNode() {
    		keyVals.put(WaveSpawnKeys.WHERE, "spawnbot");
    		keyVals.put(WaveSpawnKeys.TOTALCOUNT, 1);
    		keyVals.put(WaveSpawnKeys.MAXACTIVE, 1);
    		keyVals.put(WaveSpawnKeys.SPAWNCOUNT, 1);
    		keyVals.put(WaveSpawnKeys.WAITBEFORESTARTING, 0.0);
    		keyVals.put(WaveSpawnKeys.WAITBETWEENSPAWNS, 0.0);
    		keyVals.put(WaveSpawnKeys.WAITBETWEENDEATHS, false);
    		keyVals.put(WaveSpawnKeys.TOTALCURRENCY, 0);
    		keyVals.put(WaveSpawnKeys.SUPPORT, false);
    		keyVals.put(WaveSpawnKeys.SUPPORTLIMITED, false);
    	}
    	
    	public WaveSpawnNode(VDFNode node) {
    		for(Entry<String, Object[]> entry : node.entrySet()) {
    			if(!entry.getKey().equals("WaveStartOutput") && !entry.getKey().equals("FirstSpawnOutput") &&
    				!entry.getKey().equals("LastSpawnOutput") && !entry.getKey().equals("DoneOutput")) {
    			//if not complex keyvalue type
    				keyVals.put(WaveSpawnKeys.valueOf(entry.getKey()), entry.getValue());
    				
    				
    				
    			}
    		}
    	}
    	
    	//put key into map assuming it's not null or empty, otherwise remove it
    	public void putKey(WaveSpawnKeys key, Object value) {
    		if(value != null) {
    			if((value.getClass() == String.class && !value.equals("")) || 
    					value.getClass() != String.class) {
    				//if it is a string that is not empty or it is not a string
    				keyVals.put(key, value);
    			}
    			else { //remove key does nothing if key doesn't exist
    				keyVals.remove(key);
    			}
    		}
    		else {
    			keyVals.remove(key);
    		}
    	}
    	
    	//if you only need one value
    	public Object getValue(WaveSpawnKeys key) {
    		return keyVals.get(key);
    	}
    	
    	//entire map
    	public Map<WaveSpawnKeys, Object> getMap() {
    		return this.keyVals;
    	}
    	
    	/*
    	public void setName(String name) {
    		this.name = name;
    	}
    	
    	public String getName() {
    		return this.name;
    	}
 	
    	public void setWhere(String where) {
    		this.where = where;
    	}
    	
    	public String getWhere() {
    		return this.where;
    	}
    	
    	public void setTotalCount(int total) {
    		this.totalCount = total;
    	}
    	
    	public int getTotalCount() {
    		return this.totalCount;
    	}
    	
    	public void setMaxActive(int max) {
    		this.maxActive = max;
    	}
    	
    	public int getMaxActive() {
    		return this.maxActive;
    	}
    	
    	public void setSpawnCount(int count) {
    		this.spawnCount = count;
    	}
    	
    	public int getSpawnCount() {
    		return this.spawnCount;
    	}
    	
    	public void setBeforeStarting(double before) {
    		this.waitBeforeStarting = before;
    	}
    	
    	public double getBeforeStarting() {
    		return this.waitBeforeStarting;
    	}
    	
    	public void setBetweenSpawns(double spawn) {
    		this.waitBetweenSpawns = spawn;
    	}
    	
    	public double getBetweenSpawns() {
    		return this.waitBetweenSpawns;
    	}
    	
    	public void setBetweenDeaths(boolean death) {
    		this.waitBetweenDeaths = death;
    	}
    	
    	public boolean getBetweenDeaths() {
    		return this.waitBetweenDeaths;
    	}
    	
    	public void setCurrency(int curr) {
    		this.totalCurrency = curr;
    	}
    	
    	public int getCurrency() {
    		return this.totalCurrency;
    	}
    	
    	public void setWaitDead(String dead) {
    		this.waitForAllDead = dead;
    	}
    	
    	public String getWaitDead() {
    		return this.waitForAllDead;
    	}
    	
    	public void setWaitSpawned(String spawn) {
    		this.waitForAllSpawned = spawn;
    	}
    	
    	public String getWaitSpawned() {
    		return this.waitForAllSpawned;
    	}
    	
    	public void setSupport(boolean s) {
    		this.support = s;
    	}
    	
    	public boolean getSupport() {
    		return this.support;
    	}
    	
    	public void setSupportLimited(boolean s) {
    		this.supportLimited = s;
    	}
    	
    	public boolean getSupportLimited() {
    		return this.supportLimited;
    	}
    	
    	public void setStart(RelayNode s) {
    		this.start = s;
    	}
    	
    	public RelayNode getStart() {
    		return this.start;
    	}
    	
    	public void setFirst(RelayNode s) {
    		this.first = s;
    	}
    	
    	public RelayNode getFirst() {
    		return this.first;
    	}
    	
    	public void setLast(RelayNode s) {
    		this.last = s;
    	}
    	
    	public RelayNode getLast() {
    		return this.last;
    	}
    	
    	public void setDone(RelayNode s) {
    		this.done = s;
    	}
    	
    	public RelayNode getDone() {
    		return this.done;
    	} */
    	
    	//copy input node to calling
    	public void copyWaveSpawn(WaveSpawnNode copyFrom) {
    		this.setParent(copyFrom.getParent()); //double check parent doesn't interfere with anything
    		this.setChildren(copyFrom.getChildren());
    		this.getMap().putAll(copyFrom.getMap());
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
    	private int health = EngiPanel.tankDefaultHealth;
    	//private double speed = Values.tankDefaultHealth;
    	private String name = "tankboss";
    	private boolean skin = false;
    	private String startingPathTrackNode; //default ""
    	private RelayNode onKilled;
    	private RelayNode onBomb;
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public void setHealth(int h) {
    		this.health = h;
    	}
    	
    	public int getHealth() {
    		return this.health;
    	}
    	
    	public void setSkin(boolean s) {
    		this.skin = s;
    	}
    	
    	public boolean getSkin() {
    		return this.skin;
    	}
    	
    	public void setStartingPath(String s) {
    		this.startingPathTrackNode = s;
    	}
    	
    	public String getStartingPath() {
    		return this.startingPathTrackNode;
    	}
    	
    	public void setOnKilled(RelayNode r) {
    		this.onKilled = r;
    	}
    	
    	public RelayNode getOnKilled() {
    		return this.onKilled;
    	}
    	
    	public void setOnBomb(RelayNode r) {
    		this.onBomb = r;
    	}
    	
    	public RelayNode getOnBomb() {
    		return this.onBomb;
    	}
    	
    	
    	
    }
    
    public static class TFBotNode extends Node { //node for tfbot spawners
    	
    	public enum TFBotKeys {
    		CLASSNAME ("Class"), //class 
    		CLASSICON ("ClassIcon"), //string
    		NAME ("Name"),
    		SKILL ("Skill"), //skill
    		WEAPONRESTRICT ("WeaponRestriction"), //weaponrestriction
    		TAGS ("Tag"), //string
    		ATTRIBUTES ("Attribute"), 
    		PRIMARY ("ItemPrimary"), 
    		SECONDARY ("ItemSecondary"),
    		MELEE ("ItemMelee"), 
    		BUILDING ("ItemBuilding"), 
    		HAT1 ("ItemCosmetic1"), 
    		HAT2 ("ItemCosmetic2"),
    		HAT3 ("ItemCosmetic3"),
    		ACTION ("Action"),
    		ITEM ("Item"),
    		ITEMATTRIBUTES ("ItemAttributes"),
    		ITEMATTRIBUTESPRIMARY ("ItemAttributesPrimary"),
    		ITEMATTRIBUTESSECONDARY ("ItemAttributesSecondary"),
    		ITEMATTRIBUTESMELEE ("ItemAttributesMelee"),
    		ITEMATTRIBUTESHAT1 ("ItemAttributesCosmetic1"),
    		ITEMATTRIBUTESHAT2 ("ItemAttributesCosmetic2"),
    		ITEMATTRIBUTESHAT3 ("ItemAttributesCosmetic3"),
    		ITEMATTRIBUTESACTION ("ItemAttributesAction"),
    		CHARACTERATTRIBUTES ("CharacterAttributes"),
    		TEMPLATE ("Template"), 
    		HEALTH ("Health"), 
    		SCALE ("Scale"), 
    		AUTOJUMPMIN ("AutoJumpMin"),
    		AUTOJUMPMAX ("AutoJumpMax"), 
    		BEHAVIORMODIFIERS ("BehaviorModifiers"),
    		MAXVISIONRANGE ("MaxVisionRange"),
    		TELEPORTWHERE ("TeleportWhere"),
    		EVENTCHANGEATTRIBUTES ("EventChangeAttributes");
    		
    		private String keyName;
    		
    		TFBotKeys(String keyName) {
    			this.keyName = keyName;
    		}
    		
    		@Override
    		public String toString() {
    			return keyName;
    		}
    	}
    	//classname - classes
    	//classicon - string
    	//name - string
    	//skill - string
    	//template - string
    	//weaponrestrict - string
    	//primary, secondary, melee, building, hat1, hat2, hat3 - string
    	
    	public static final int ITEMCOUNT = 7;
    	
    	public enum Skill {
    		EASY ("Easy"),
    		NORMAL ("Normal"),
    		HARD ("Hard"),
    		EXPERT ("Expert");
    		
    		private String name;
    		
    		Skill(String name) {
    			this.name = name;
    		}
    		
    		@Override
    		public String toString() {
    			return name;
    		}
    	}
    	
    	public enum WeaponRestriction {
    		ANY ("Any"),
    		PRIMARYONLY ("PrimaryOnly"),
    		SECONDARYONLY ("SecondaryOnly"),
    		MELEEONLY ("MeleeOnly");
    		
    		private String name;
    		
    		WeaponRestriction(String name) {
    			this.name = name;
    		}
    		
    		@Override
    		public String toString() {
    			return name;
    		}
    	}
    	
    	//todo: add support for the niche use vars 
    	
    	private Map<String, Object[]> keyVals = new HashMap<String, Object[]>(8);
    	//private List<String> tags;
    	//private List<String> attributes;
    	//private String[] items;
    	private boolean isItemsSorted;
    	//private Map<ItemSlot, HashMap<String, String>> itemAttributeList;
    	
    	public TFBotNode() { //defaults
    		keyVals.put(TFBotKeys.CLASSNAME.name(), new Object[] {Classes.Scout});
    		keyVals.put(TFBotKeys.CLASSICON.name(), new Object[] {"scout"});
    		keyVals.put(TFBotKeys.SKILL.name(), new Object[] {Skill.EASY});
    		keyVals.put(TFBotKeys.WEAPONRESTRICT.name(), new Object[] {WeaponRestriction.ANY});
    		isItemsSorted = true;
    	}
    	 
    	//alternate complex constructor for read in tfbots
    	public TFBotNode(VDFNode parsedNode) {   		
    		isItemsSorted = false;
    		
    		for(Entry<String, Object[]> entry : parsedNode.entrySet()) {
    			VDFNode subNode;
    			
    			//need to convert array of vdfnodes to array of hashmaps
    			if(entry.getValue()[0].getClass() == VDFNode.class) {
    				Object[] nodeArray = new Object[entry.getValue().length];
    				
    				for(int i = 0; i < nodeArray.length; i++) {
    					Map<String, Object[]> subMap = new HashMap<String, Object[]>(4);
    					subMap.putAll((VDFNode) entry.getValue()[i]);
    					nodeArray[i] = subMap;
    				}
    				
    				keyVals.put(entry.getKey().toUpperCase(), nodeArray);
    			}
    			else if(entry.getValue().length > 1) {
    				//converts an array to a list to an array containing a singular list
    				List<Object> list = Arrays.asList(entry.getValue());
    				keyVals.put(entry.getKey(), new Object[] {list});
    			}
    			else {
    				keyVals.put(entry.getKey().toUpperCase(), entry.getValue());
    			}
    		}
    	}
    	
    	//put key into map assuming it's not null or empty, otherwise remove it
    	public void putKey(String key, Object value) {
    		if(value != null) {
    			if((value.getClass() == String.class && !value.equals("")) || 
    					value.getClass() != String.class) {
    				//if it is a string that is not empty or it is not a string
    				keyVals.put(key, new Object[] {value});
    			}
    			else { //remove key does nothing if key doesn't exist
    				keyVals.remove(key);
    			}
    		}
    		else {
    			keyVals.remove(key);
    		}
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
    	
    	public Object getValueSingular(TFBotKeys key) {
    		//if(keyVals.get(key).length == 1) {
    		//	return keyVals.get(key);
    		//}
    		
    		return keyVals.get(key)[0];
    	}
    	
    	public Object[] getValueArray(TFBotKeys key) {
    		return keyVals.get(key);
    	}
    	
    	public boolean isItemsSorted() {
    		return isItemsSorted;
    	}
    	
    	public void setItemsSorted(boolean isItemsSorted) {
    		this.isItemsSorted = isItemsSorted;
    	}
    	
    	//reconsider this
    	public Map<String, Object[]> getMap() {
    		return this.keyVals;
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
    	
    }
    
    public static class RandomChoiceNode extends Node {
    	
    }
    
}