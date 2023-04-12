package engipop;
import java.util.*;
import java.util.Map.Entry;

import engipop.EngiPanel.Classes;
import engipop.EngiPanel.ItemSlot;
import engipop.Tree.WaveNode;
import net.platinumdigitalgroup.jvdf.VDFBindField;
import net.platinumdigitalgroup.jvdf.VDFNode;

//class to mimic popfile structure via a tree so it can be later parsed into a popfile
@SuppressWarnings("unchecked")
public class Tree {
	
    private Node root;

    public Tree(Node PopNode) {
        root = PopNode;
        
    }
    
    public Node getRoot() {
    	return this.root;
    }
    public enum SpawnerType {
    	NONE, TFBOT, TANK, SQUAD, RANDOMCHOICE
    }

    public static class Node {
        private Node parent;
        private List<Node> children = new ArrayList<Node>();
        protected Map<String, Object[]> keyVals = new HashMap<String, Object[]>(8);
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
    	
    	//put key into map assuming it's not null or empty, otherwise remove it
    	public void putKey(String key, Object value) {
    		if(value != null) {
    			if(value.getClass() == Boolean.class) { //prevent checkboxes from erroring
    				keyVals.put(key, new Object[] {value});
    			}
    			else if(value.getClass() != String.class || 
    				(value.getClass() == String.class && !value.equals(""))) {
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
    	
    	//reconsider this
    	public Map<String, Object[]> getMap() {
    		return this.keyVals;
    	}
    	
    	//convert inconsistent cases to standardized
    	public boolean convertCase(Map<String, Object[]> map, List<String> keys) {
    		boolean updated = false;
       		Iterator<String> iterator = map.keySet().iterator();
       		List<Entry<String, Object[]>> newEntries = new ArrayList<Entry<String, Object[]>>();
    		
    		while(iterator.hasNext()) { 
    			String next = iterator.next();
    			int k = keys.indexOf(next.toUpperCase());
				if(k != -1 && !keys.get(k).equals(next)) { //if already in pascal case, skip
					newEntries.add(new AbstractMap.SimpleEntry<String, Object[]>(keys.get(k), map.get(next)));
					iterator.remove();
				}
    		}
    		for(Entry<String, Object[]> entry : newEntries) {
    			map.put(entry.getKey(), entry.getValue());
    		}
    		
    		if(newEntries.size() > 0) {
    			updated = true;
    		}
    		
    		return updated;
    	}
    	
    	public void convertVDFNodeToHash(Map<String, Object[]> map) {
    		for(Entry<String, Object[]> entry : map.entrySet()) {
    			if(entry.getValue()[0].getClass() == VDFNode.class) {
    				List<Map<String, Object[]>> nodeArray = new ArrayList<Map<String, Object[]>>(entry.getValue().length);
    				
    				for(int i = 0; i < entry.getValue().length; i++) {
    					Map<String, Object[]> subMap = new HashMap<String, Object[]>(8);
    					subMap.putAll((VDFNode) entry.getValue()[i]);
    					convertVDFNodeToHash(subMap);
    					nodeArray.add(subMap);
    				}
    				entry.setValue(new Object[] {nodeArray});
    			}
    		}
    	}
    }
    
    public static class PopNode extends Node { //
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
        	this.putKey(ADVANCED, false);
        }
        
		public PopNode(Map<String, Object[]> map) { //constructor for read in nodes
        	List<String> popKeys = new ArrayList<String>(Arrays.asList(STARTINGCURRENCY.toUpperCase(), RESPAWNWAVETIME.toUpperCase(),
        			FIXEDRESPAWNWAVETIME.toUpperCase(), EVENTPOPFILE.toUpperCase(), BUSTERDAMAGE.toUpperCase(), 
        				BUSTERKILLS.toUpperCase(), BOTSATKINSPAWN.toUpperCase(), ADVANCED.toUpperCase()));
        	List<String> waveKeys = new ArrayList<String>(Arrays.asList(WaveNode.STARTWAVEOUTPUT.toUpperCase(), 
        			WaveNode.DONEOUTPUT.toUpperCase(), WaveNode.INITWAVEOUTPUT.toUpperCase()));
        	
        	keyVals.putAll(map);
        	
        	convertCase(keyVals, popKeys);
        	
    		Iterator<Entry<String, Object[]>> iterator2 = keyVals.entrySet().iterator();
    		while(iterator2.hasNext()) {
    			Entry<String, Object[]> entry = iterator2.next();
    			
    			if(entry.getValue()[0].getClass() == VDFNode.class) {
    				boolean updated = convertCase((Map<String, Object[]>) entry.getValue()[0], waveKeys);
    				
    				if(updated) {
    					WaveNode waveNode = new WaveNode(entry.getKey(), (Map<String, Object[]>) entry.getValue()[0]);
    					waveNode.connectNodes(this);
    					iterator2.remove();
    				}
    			}
    		}
        }
        
        /*
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
        } */
        
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
    	
    	private String name;
    	
    	public WaveNode() {
    		this.putKey(STARTWAVEOUTPUT, new RelayNode());
    		this.putKey(DONEOUTPUT, new RelayNode());
    	}
    	
        public WaveNode(String name, Map<String, Object[]> map) { //readin node, key case should already be converted
        	List<String> wsKeys = new ArrayList<String>(Arrays.asList(WaveSpawnNode.WHERE.toUpperCase(), WaveSpawnNode.TOTALCOUNT.toUpperCase(),
        			WaveSpawnNode.MAXACTIVE.toUpperCase(), WaveSpawnNode.SPAWNCOUNT.toUpperCase(), WaveSpawnNode.WAITBEFORESTARTING.toUpperCase(),
        			WaveSpawnNode.WAITBETWEENSPAWNS.toUpperCase(), WaveSpawnNode.TOTALCURRENCY.toUpperCase(), 
        			WaveSpawnNode.NAME.toUpperCase(), WaveSpawnNode.WAITFORALLDEAD.toUpperCase(), WaveSpawnNode.WAITFORALLSPAWNED.toUpperCase(),
        			WaveSpawnNode.SUPPORT.toUpperCase(), WaveSpawnNode.STARTWAVEOUTPUT.toUpperCase(), WaveSpawnNode.FIRSTSPAWNOUTPUT.toUpperCase(),
        			WaveSpawnNode.LASTSPAWNOUTPUT.toUpperCase(), WaveSpawnNode.DONEOUTPUT.toUpperCase(), WaveSpawnNode.TFBOT.toUpperCase(),
        			WaveSpawnNode.TANK.toUpperCase(), WaveSpawnNode.SQUAD.toUpperCase(), WaveSpawnNode.RANDOMCHOICE.toUpperCase()));
        	
        	this.setName(name); //this is the internal pop block name
        	keyVals.putAll(map);
        	
    		Iterator<Entry<String, Object[]>> iterator = keyVals.entrySet().iterator();
    		while(iterator.hasNext()) {
    			Entry<String, Object[]> entry = iterator.next();
    			
    			if(entry.getValue()[0].getClass() == VDFNode.class) {
    				boolean updated = convertCase((Map<String, Object[]>) entry.getValue()[0], wsKeys);
    				
    				if(updated) {
    					WaveSpawnNode wsNode = new WaveSpawnNode((Map<String, Object[]>) entry.getValue()[0]);
    					wsNode.connectNodes(this);
    					iterator.remove();
    				}
    			}
    		}
        }

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
    	
    	/*
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
    	*/
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
	
    	//where, name, waitforalldead, waitforallspawned, template = string
    	//totalcount, maxactive, spawncount, totalcurrency = int
    	//waitbeforestarting, waitbetweenspawns = double
    	//waitbetweendeaths, support, supportlimited = boolean
  
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
    		//keys are converted by wave
    		keyVals.putAll(map);
    		
    		if(keyVals.get(WAITBETWEENSPAWNSAFTERDEATH) != null) {
    			waitBetweenDeaths = true;
    		}
    		
    		//need to check this 
    		if(keyVals.get(SUPPORT) != null) {
    			Object supportVal = keyVals.get(SUPPORT)[0];
    			if(supportVal.getClass() == String.class) {
    				if(((String) supportVal).equalsIgnoreCase("LIMITED")) {
    					supportLimited = true;
    				}
    			}
    		}
    		
    		if(keyVals.get(STARTWAVEOUTPUT) != null) {
    			this.putKey(STARTWAVEOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(STARTWAVEOUTPUT)));
    		}
    		if(keyVals.get(FIRSTSPAWNOUTPUT) != null) {
    			this.putKey(FIRSTSPAWNOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(FIRSTSPAWNOUTPUT)));
    		}
    		if(keyVals.get(LASTSPAWNOUTPUT) != null) {
    			this.putKey(LASTSPAWNOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(LASTSPAWNOUTPUT)));
    		}
    		if(keyVals.get(DONEOUTPUT) != null) {
    			this.putKey(DONEOUTPUT, new RelayNode((Map<String, Object[]>) this.getValueSingular(DONEOUTPUT)));
    		}
    		
    		if(keyVals.get(TFBOT) != null) {
    			TFBotNode botNode = new TFBotNode((Map<String, Object[]>) keyVals.get(TFBOT)[0]);
    			botNode.setParent(this);
    			keyVals.remove(TFBOT);
    		}
    		else if(keyVals.get(TANK) != null) {
    			
    		}
    		else if(keyVals.get(SQUAD) != null) {
    			
    		}
    		else if(keyVals.get(RANDOMCHOICE) != null) {
    			
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
    		List<String> keys = new ArrayList<String>(Arrays.asList(HEALTH.toUpperCase(), SPEED.toUpperCase(), NAME.toUpperCase(), 
    				SKIN.toUpperCase(), STARTINGPATHTRACKNODE.toUpperCase(), ONKILLEDOUTPUT.toUpperCase(), 
    				ONBOMBDROPPEDOUTPUT.toUpperCase()));
    		
    		keyVals.putAll(map);
    		convertCase(keyVals, keys);
    	}
    	
    	/*
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
    	*/
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
    	
    	//private List<String> tags;
    	//private List<String> attributes;
    	//private String[] items;
    	private boolean isItemsSorted;
    	//private Map<ItemSlot, HashMap<String, String>> itemAttributeList;
    	
    	public TFBotNode() { //defaults
    		this.putKey(CLASSNAME, Classes.Scout);
    		this.putKey(CLASSICON, "scout");
    		this.putKey(SKILL, Skill.EASY);
    		this.putKey(WEAPONRESTRICT, WeaponRestriction.ANY);
    		isItemsSorted = true;
    	}
    	 
    	//alternate constructor for read in tfbots
    	public TFBotNode(Map<String, Object[]> map) {
    		List<String> keys = new ArrayList<String>(Arrays.asList(CLASSNAME.toUpperCase(), CLASSICON.toUpperCase(), NAME.toUpperCase(), 
    				SKILL.toUpperCase(), WEAPONRESTRICT.toUpperCase(), TAGS.toUpperCase(), ATTRIBUTES.toUpperCase(), ITEM.toUpperCase(), 
    				ITEMATTRIBUTES.toUpperCase(), CHARACTERATTRIBUTES.toUpperCase(), TEMPLATE.toUpperCase(), HEALTH.toUpperCase(), 
    				SCALE.toUpperCase(), AUTOJUMPMIN.toUpperCase(), AUTOJUMPMAX.toUpperCase(), BEHAVIORMODIFIERS.toUpperCase(), 
    				MAXVISIONRANGE.toUpperCase(), TELEPORTWHERE.toUpperCase(), EVENTCHANGEATTRIBUTES.toUpperCase()));
    		
    		keyVals.putAll(map);
    		convertCase(keyVals, keys);
    		
    		for(Entry<String, Object[]> entry : keyVals.entrySet()) {
    			if(entry.getValue().length > 1) {
    				//converts an array to a list to an array containing a singular list
    				List<Object> list = new ArrayList<Object>(Arrays.asList(entry.getValue()));
    				entry.setValue(new Object[] {list});
    			}
    			
    			if(entry.getKey().equals(TFBotNode.WEAPONRESTRICT)) {
					//need to sanity check here
					entry.setValue(new Object[] {(WeaponRestriction.valueOf(((String) entry.getValue()[0]).toUpperCase()))});
				}
				else if(entry.getKey().equals(TFBotNode.SKILL)) {
					entry.setValue(new Object[] {(Skill.valueOf(((String) entry.getValue()[0]).toUpperCase()))});
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