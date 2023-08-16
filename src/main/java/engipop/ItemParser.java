package engipop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import engipop.EngiPanel.Classes;
import engipop.EngiPanel.ItemSlot;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

//todo: possibly add more filtering options
public class ItemParser { //parse item schema, get weapons and hats
	List<ItemData> scoutItems = new ArrayList<ItemData>(550);
	List<ItemData> soldierItems = new ArrayList<ItemData>(530);
	List<ItemData> pyroItems = new ArrayList<ItemData>(560);
	List<ItemData> demomanItems = new ArrayList<ItemData>(510);
	List<ItemData> heavyItems = new ArrayList<ItemData>(530);
	List<ItemData> engineerItems = new ArrayList<ItemData>(535);
	List<ItemData> medicItems = new ArrayList<ItemData>(490);
	List<ItemData> sniperItems = new ArrayList<ItemData>(500);
	List<ItemData> spyItems = new ArrayList<ItemData>(470);
	
	public ItemParser() {
	}
    
	public void parse(File file, EngiWindow window) {
		String schema = "";
		VDFNode item;
		VDFNode allPrefabs;
		//Path path = FileSystems.getDefault().getPath("C:\\", "Program Files (x86)", "Steam", "steamapps", "common", "Team Fortress 2", "tf", "scripts", "items", "items_game.txt");
		Path path = file.toPath();
		
		//initLists();
		
		try {
			schema = readFile(path, StandardCharsets.US_ASCII);
		}
		catch (IOException i) {
			window.updateFeedback("items_game.txt was moved or not found");
		}
		item = new VDFParser().parse(schema);
		if(!item.containsKey("items_game")) {
			//wrong items_game.txt
			window.updateFeedback("The file does not contain TF2 item definitions");
		}
		else {
			allPrefabs = item.getSubNode("items_game").getSubNode("prefabs");
			item = item.getSubNode("items_game").getSubNode("items");
			parsePrefab(item, allPrefabs);
		}
	}
	
	public static String readFile(Path path, Charset encoding) throws IOException {		
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}
	
	public void parsePrefab(VDFNode item, VDFNode allPrefabs) { //time to suffer		
		for(String key : item.keySet()) {
			VDFNode node = item.getSubNode(key);
			String prefabString = node.containsKey("prefab") ? prefabString = node.getString("prefab") : null;
			
			if(key.equals("default")) {
				continue;
			}
			
			if(prefabString == null) {
				if(node.containsKey("item_slot")) {
					if(node.getString("name").equals("The B.A.S.E. Jumper")) {
						getClasses(new ItemData("The B.A.S.E. Jumper", ItemSlot.PRIMARY, key), "demoman");
						getClasses(new ItemData("The B.A.S.E. Jumper", ItemSlot.SECONDARY, key), "soldier");
					}
					else {
						switch(node.getString("item_slot")) {
							case "primary":
							case "secondary":
							case "melee":
							case "building":
								if(!node.containsKey("craft_class") || !node.getString("craft_class").equals("craft_token")) {
									getClasses(new ItemData(node.getString("name"), ItemSlot.valueOf(node.getString("item_slot").toUpperCase()), key), 
											node.getSubNode("used_by_classes"));
								}
								break;
							case "pda2":
								getClasses(new ItemData(node.getString("name"), ItemSlot.PRIMARY, key), "spy");
								break;
							default:
								break;
						}
					}
				}
				continue; //whether wep or not, skip after
			}
			
			if(prefabString.contains("misc") || prefabString.contains("hat") || prefabString.contains("grenades")) {			
				if(!node.containsKey("equip_region")) {
					//hat with no specific equip region
					if(node.containsKey("used_by_classes")) {
						getClasses(new ItemData(node.getString("name"), ItemSlot.COSMETIC1, key), node.getSubNode("used_by_classes"));
					}
					else {
						//"score_reward_hat"
						//System.out.println("no usedby: " +  node.getString("name"));
					}
				}
				else if(node.get("equip_region")[0] instanceof VDFNode) {
					//hat with multiple equip regions
					getClasses(new ItemData(node.getString("name"), ItemSlot.COSMETIC1, key), node.getSubNode("used_by_classes"));
				}
				else if(!node.getString("equip_region").equals("medal")) { //for now filter out all medals
					getClasses(new ItemData(node.getString("name"), ItemSlot.COSMETIC1, key), node.getSubNode("used_by_classes"));
				}
			}
			else {
				String equip = "";
				VDFNode wepPrefab = new VDFNode(String.CASE_INSENSITIVE_ORDER);
				VDFNode classNode = null;
				boolean isWep = prefabString.contains("weapon_") && !prefabString.contains("case") ? true : false;
				//this should probably be rewritten
				if(!isWep) {
					isWep = prefabString.contains("valve") && node.containsKey("craft_class")
							&& node.getString("craft_class").equals("weapon") ? true : false;
				}	
				if(!isWep) {
					continue;
				}
				
				//if prefab contains weapon and is not a case or has a prefab of valve + is a weapon
				if(node.containsKey("item_slot") && !key.equals("205")) { //items that define equip/class in itself and not in a prefab
					equip = node.getString("item_slot");
					classNode = node.getSubNode("used_by_classes");
				}
				else {
					prefabString = getPrefabString(allPrefabs, prefabString);
					wepPrefab = allPrefabs.getSubNode(prefabString);
					
					if(node.getString("prefab").equals("weapon_sword") || node.getString("prefab").equals("weapon_lunchbox")) {
						classNode = node.getSubNode("used_by_classes");
					} //swords and lunchboxes have their classes defined in outer prefab but equip in inner
					
					else if(!wepPrefab.containsKey("item_slot")) { //if its prefab doesn't have an item slot named
						if(wepPrefab.containsKey("prefab")) { //prefab of a prefab
							if(wepPrefab.getString("prefab").equals("weapon_sword") || 
									wepPrefab.getString("prefab").equals("weapon_lunchbox")) {
								classNode = wepPrefab.getSubNode("used_by_classes");
							} //swords and lunchboxes have their classes defined in outer prefab but equip in inner
							wepPrefab = allPrefabs.getSubNode(wepPrefab.getString("prefab"));
						}
					}
					else { //single prefab
						if(prefabString.equals("weapon_shotgun_multiclass") || 
								prefabString.equals("weapon_trenchgun")) {
							//prevent shotguns in wrong primary slots
							equip = "shotgun";
							//if(node.get("name") != null) {
								addShotguns(node.getString("name"), key);
						}
						else { //normal rules
							equip = wepPrefab.getString("item_slot");
							classNode = wepPrefab.getSubNode("used_by_classes");
						}
					}
				}
				
				switch(equip) {
					case "primary":
					case "secondary":
					case "melee":
						if(node.get("name") != null) { //if item is non base item, so real name is defined
							getClasses(new ItemData(node.getString("name"), ItemSlot.valueOf(equip.toUpperCase()), key), classNode);
						}
						else { //otherwise just get its prefab name
							getClasses(new ItemData(wepPrefab.getString("item_name"), ItemSlot.valueOf(equip.toUpperCase()), key), classNode);
						}
						break;
					case "building":
						getClasses(new ItemData(node.getString("name"), ItemSlot.BUILDING, key), "spy");
						break;
					case "pda2":
						getClasses(new ItemData(node.getString("name"), ItemSlot.PRIMARY, key), "spy");
						break;
					default:
						break;
				}
			}
		}
		//dumb thing here
		getClasses(new ItemData("Upgradeable TF_WEAPON_ROCKETLAUNCHER", ItemSlot.PRIMARY, "205"), "soldier");
	}
	
	private String getPrefabString(VDFNode allPrefabs, String prefabString) { //get the weapon prefab
		int first = 0;
		int last = 0;
		
		first = prefabString.indexOf("weapon_");
		last = first;
		
		while(last < prefabString.length() && prefabString.charAt(last) != ' ') {
			last++;
		}
		if(first != 0 && prefabString.charAt(first - 1) == '_') { //if it's a paintkit with a prefix
			while(first > 0 && prefabString.charAt(first) != ' ') {
				first--;
			} //recursively grab its usuable prefab
			prefabString = getPrefabString(allPrefabs, allPrefabs.getSubNode(prefabString.substring(first, last)).getString("prefab"));
		}
		else {
			prefabString = prefabString.substring(first, last);
		}
		return prefabString;
	}
	
	//manually adding shotguns 
	private void addShotguns(String name, String key) {
		getClasses(new ItemData(name, ItemSlot.SECONDARY, key), "soldier");
		getClasses(new ItemData(name, ItemSlot.SECONDARY, key), "pyro");
		getClasses(new ItemData(name, ItemSlot.SECONDARY, key), "heavy");
		getClasses(new ItemData(name, ItemSlot.PRIMARY, key), "engineer");
	}
	
	private void getClasses(ItemData data, String aclass) {
		VDFNode node = new VDFNode(String.CASE_INSENSITIVE_ORDER);
		node.put(aclass, 1);
		
		getClasses(data, node);
	}
	
	//add the item to appropriate class
	private void getClasses(ItemData data, VDFNode classes) {
		
		if(classes.containsKey("scout")) {
			scoutItems.add(data);
		}
		if(classes.containsKey("soldier")) {
			soldierItems.add(data);
		}
		if(classes.containsKey("pyro")) {
			pyroItems.add(data);
		}
		if(classes.containsKey("demoman")) {
			demomanItems.add(data);
		}
		if(classes.containsKey("heavy")) {
			heavyItems.add(data);
		}
		if(classes.containsKey("engineer")) {		
			engineerItems.add(data);
		}
		if(classes.containsKey("medic")) {
			medicItems.add(data);
		}
		if(classes.containsKey("sniper")) {
			sniperItems.add(data);
		}
		if(classes.containsKey("spy")) {
			spyItems.add(data);
		}
	}
	
	//get class items based on input class
	public List<ItemData> getClassList(Classes selected) {
		List<ItemData> classList = new ArrayList<ItemData>();
		
		switch (selected) {
			case Scout:
				classList = scoutItems;
				break;
			case Soldier:
				classList = soldierItems;
				break;
			case Pyro:
				classList = pyroItems;
				break;
			case Demoman:
				classList = demomanItems;
				break;
			case Heavyweapons:
				classList = heavyItems;
				break;
			case Engineer:
				classList = engineerItems;
				break;
			case Medic:
				classList = medicItems;
				break;
			case Sniper:
				classList = sniperItems;
				break;	
			case Spy:
				classList = spyItems;
				break;
			case None:
			default:
				break;
		}
		return classList;
	}
	
	//returns set containing only items that are in the slot list
	public List<ItemData> checkIfItemInSlot(List<Object> items, EngiPanel.Classes selected, int slot) throws IndexOutOfBoundsException {
		List<ItemData> list = new ArrayList<ItemData>(getClassList(selected));
		//Set<String> subset = new HashSet<String>(list);
		//Set<String> itemSet = new HashSet<String>(items);
		
		list.retainAll(items);
		
		//subset.retainAll(itemSet);
		
		return list;
	}
	
	public static class ItemData {
		private String name;
		private ItemSlot slot;
		private int index;
		
		public ItemData(String name, ItemSlot slot, String indexString) {
			this.name = name;
			this.slot = slot;
			index = Integer.parseInt(indexString);
		}
		
		public ItemSlot getSlot() {
			return this.slot;
		}
		public String toString() {
			return this.name;
		}
	}
}
