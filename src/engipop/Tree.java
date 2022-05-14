package engipop;
import java.util.*;

import engipop.EngiPanel.Classes;

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
        	this.advanced = a ;
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
    }  
    
    public static class TemplateNode extends Node {
    	//templates can be wavespawns or tfbots
    	//may be able to use a generic node instead
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
    	private boolean randomSpawn; //default 0, if true allows bot spawns to be randomized
    	//prob should be disabled for single spawn maps
    	private RelayNode start;
    	private RelayNode first;
    	private RelayNode last;
    	private RelayNode done;
    	
    	public WaveSpawnNode() {
    	}
    	
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
    	}
    	
    	public Node getSpawner() {
    		return this.getChildren().get(0);
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
    		CLASSNAME, CLASSICON, NAME,
    		SKILL, WEAPONRESTRICT, TAGS,
    		ATTRIBUTES, PRIMARY, SECONDARY,
    		MELEE, BUILDING, HAT1, HAT2,
    		HAT3, ITEMATTRIBUTES, CHARACTER
    	}
    	//classname - classes
    	//classicon - string
    	//name - string
    	//skill - string
    	//weaponrestrict - string
    	//attributes - arraylist<string>
    	//primary, secondary, melee, building, hat1, hat2, hat3 - string
    	//character is always null, mostly just for printing purposes
    	
    	public static final String EASY = "Easy";
    	public static final String NORMAL = "Normal";
    	public static final String HARD = "Hard";
    	public static final String EXPERT = "Expert";
    	
    	public static final String ANY = "Any";
    	public static final String PRIMARYONLY = "PrimaryOnly";
    	public static final String SECONDARYONLY = "SecondaryOnly";
    	public static final String MELEEONLY = "MeleeOnly";
    	//private ArrayList<String> attr = new ArrayList<String>();

    	//todo: add support for the niche use vars 
    	
    	private List<String> tags = new ArrayList<String>();
    	private Map<EngiPanel.ItemSlot, ItemAttributeNode> itemAttributeList;
    	private Map<TFBotKeys, Object> keyVals = new HashMap<TFBotKeys, Object>();
    	
    	public TFBotNode() { //defaults
    		keyVals.put(TFBotKeys.CLASSNAME, Classes.Scout);
    		keyVals.put(TFBotKeys.CLASSICON, "scout");
    		keyVals.put(TFBotKeys.SKILL, EASY);
    		keyVals.put(TFBotKeys.WEAPONRESTRICT, ANY);
    	}
    	
    	public void putKey(TFBotKeys key, Object value) {
    		keyVals.put(key, value);
    	}
    	
    	public Object getValue(TFBotKeys key) {
    		return keyVals.get(key);
    	}
    	/*
    	public Map<TFBotKeys, Object> getMap() {
    		return this.keyVals;
    	} */
    	
    	public Set<TFBotKeys> getKeySet() {
    		return keyVals.keySet();
    	} 
    	
    	public void setTags(List<String> list) {
    		this.tags = list;
    	}
    	
    	public List<String> getTags() {
    		return this.tags;
    	}
    	
    	//updates the entire map
    	public void setItemAttributeList(HashMap<EngiPanel.ItemSlot, ItemAttributeNode> list) {
    		itemAttributeList = list;
    	}
    	
    	//gets the entire map
    	public Map<EngiPanel.ItemSlot, ItemAttributeNode> getItemAttributeList() {
    		return this.itemAttributeList;
    	}
    }
    
    //only contains list of attributes, type is handled by tfbot node
    public static class ItemAttributeNode extends Node {
    	private Map<String, String> attributeMap = new HashMap<String, String>();
    	
    	public ItemAttributeNode(Map<String, String> list) {
    		this.attributeMap = list;
    	}

    	public void setMap(Map <String, String> map) {
    		attributeMap = map;
    	}
    	
    	public Map <String, String> getMap() {
    		return this.attributeMap;
    	}
    }
    
    //these two are mostly convenience 
    public static class SquadNode extends Node {
    	
    }
    
    public static class RandomChoiceNode extends Node {
    	
    }
    
}