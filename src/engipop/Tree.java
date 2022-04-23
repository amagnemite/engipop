package engipop;
import java.awt.Window;
import java.util.*;

import org.w3c.dom.Node;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
public class Tree {
	
    private Node root;

    public Tree(Node PopNode) {
        root = PopNode;
        
    }
    
    public Node getRoot() {
    	return this.root;
    }

    public static class Node {
        private Node parent;
        private List<Node> children = new ArrayList<Node>();
        
        public void connectNodes(Node parent) { //parents the calling node and adds calling node to parent's list
        	this.parent = parent; 
        	parent.children.add(this);
        }
        
        public List<Node> getChildren() {
        	return this.children;
        }
        
     	public boolean hasChildren() {
    		boolean hasSpawner = false;
    		
    		if(this.getChildren().size() > 0) {
    			hasSpawner = true;
    		}
    		return hasSpawner;
    	}
        
        public Node getParent() {
        	return this.parent;
        }
 
    }
    
    public static class PopNode extends Node { //
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
    	//initwaveoutput?
    }
    
    public static class StartWaveOutputNode extends Node {
    	private String target = "wave_start_relay";
    	private String action = "trigger";
    }
    
    public static class DoneOutputNode extends Node {
    	private String target = "wave_finished_relay";
    	private String action = "trigger";
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
    	private String support; //don't forget support limited
    	private boolean randomSpawn; //default 0, if true allows bot spawns to be randomized
    	//prob should be disabled for single spawn maps
    	
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
    	
    	public Node getSpawner() {
    		return this.getChildren().get(0);
    	}
    }
    
    public static class TankNode extends Node {
    	private int health = Values.tankDefaultHealth;
    	//private double speed = Values.tankDefaultHealth;
    	private String name = "tankboss";
    	private boolean skin = false;
    	private String startingPathTrackNode; //default ""
    	//add relays
    	
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
    }
    
    public static class TFBotNode extends Node { //node for tfbot spawners
    	private String className = window.CLASSES[0];
    	private String name;
    	private String icon = "scout";
    	private String skill = "Easy"; //default easy
    	private String wepRestrict = "Any";
    	private ArrayList<String> tags = new ArrayList<String>();
    	private ArrayList<String> attr = new ArrayList<String>();
    	private String primary;
    	private ArrayList<String> priAttr = new ArrayList<String>();
    	private String secondary;
    	private ArrayList<String> secAttr = new ArrayList<String>();
    	private String melee;
    	private ArrayList<String> melAttr = new ArrayList<String>();  	
    	private ArrayList<String> charAttr = new ArrayList<String>();  	
    	//todo: add support for the niche use vars
    	
    	public TFBotNode() {
    	}
    	
    	public void setClassName(String className) {
    		this.className = className;
    	}
    	
    	public String getClassName() {
    		return this.className;
    	}
    	
    	public void setName(String name) {
    		this.name = name;
    	}
    	
    	public String getName() {
    		return this.name;
    	}   	
    	
    	
    	public void setIcon(String icon) {
    		this.icon = icon;
    	}
    	
    	public String getIcon() {
    		return this.icon;
    	}
    	
    	public void setSkill(String skill) {
    		this.skill = skill;
    	}
    	
    	public String getSkill() {
    		return this.skill;
    	}
    	
       	public void setWepRestrict(String wep) {
    		this.wepRestrict = wep;
    	}
    	
    	public String getWepRestrict() {
    		return this.wepRestrict;
    	}
    	
    	public void setTags(ArrayList<String> list) {
    		this.tags = list;
    	}
    	
    	public ArrayList<String> getTags() {
    		return this.tags;
    	}
    	
    	public Map<String, Object> makeMap() {
    		Map<String, Object> map = new HashMap<String, Object>();
    		
    		map.put("Class", this.className);
    		map.put("Name", this.name);
    		map.put("ClassIcon", this.icon);
    		map.put("Skill", this.skill);
    		map.put("WeaponRestrictions", this.skill);
    		map.put("Tag", this.tags);
    		
    		return map;
    	}
    }
    
    public static class SquadNode extends Node {
    	
    }
    
    public static class RandomChoiceNode extends Node {
    	
    }
    
}