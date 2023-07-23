package engipop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import engipop.EngiPanel.ItemSlot;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

//todo: possibly add more filtering options
public class ItemParser { //parse item schema, get weapons and hats
	List<String> scoutPrimary = new ArrayList<String>();
	List<String> scoutSecondary = new ArrayList<String>();
	List<String> scoutMelee = new ArrayList<String>();
	List<String> scoutCosmetics = new ArrayList<String>();
	List<List<String>> scoutItems = new ArrayList<List<String>>();
	
	List<String> soldierPrimary = new ArrayList<String>();
	List<String> soldierSecondary = new ArrayList<String>();
	List<String> soldierMelee = new ArrayList<String>();
	List<String> soldierCosmetics = new ArrayList<String>();
	List<List<String>> soldierItems = new ArrayList<List<String>>();
	
	List<String> pyroPrimary = new ArrayList<String>();
	List<String> pyroSecondary = new ArrayList<String>();
	List<String> pyroMelee = new ArrayList<String>();
	List<String> pyroCosmetics = new ArrayList<String>();
	List<List<String>> pyroItems = new ArrayList<List<String>>();
	
	List<String> demomanPrimary = new ArrayList<String>();
	List<String> demomanSecondary = new ArrayList<String>();
	List<String> demomanMelee = new ArrayList<String>();
	List<String> demomanCosmetics = new ArrayList<String>();
	List<List<String>> demomanItems = new ArrayList<List<String>>();
	
	List<String> heavyPrimary = new ArrayList<String>();
	List<String> heavySecondary = new ArrayList<String>();
	List<String> heavyMelee = new ArrayList<String>();
	List<String> heavyCosmetics = new ArrayList<String>();
	List<List<String>> heavyItems = new ArrayList<List<String>>();
	
	List<String> engineerPrimary = new ArrayList<String>();
	List<String> engineerSecondary = new ArrayList<String>();
	List<String> engineerMelee = new ArrayList<String>();
	List<String> engineerCosmetics = new ArrayList<String>();
	List<List<String>> engineerItems = new ArrayList<List<String>>();
	
	List<String> medicPrimary = new ArrayList<String>();
	List<String> medicSecondary = new ArrayList<String>();
	List<String> medicMelee = new ArrayList<String>();
	List<String> medicCosmetics = new ArrayList<String>();
	List<List<String>> medicItems = new ArrayList<List<String>>();
	
	List<String> sniperPrimary = new ArrayList<String>();
	List<String> sniperSecondary = new ArrayList<String>();
	List<String> sniperMelee = new ArrayList<String>();
	List<String> sniperCosmetics = new ArrayList<String>();
	List<List<String>> sniperItems = new ArrayList<List<String>>();

	List<String> spyPDA = new ArrayList<String>();
	List<String> spySecondary = new ArrayList<String>();
	List<String> spyMelee = new ArrayList<String>();
	List<String> spyBuilding = new ArrayList<String>();
	List<String> spyCosmetics = new ArrayList<String>();
	List<List<String>> spyItems = new ArrayList<List<String>>();
	
	public ItemParser() {
	}
    
	public void parse(File file, MainWindow window) {
		String schema = "";
		VDFNode item;
		VDFNode allPrefabs;
		//Path path = FileSystems.getDefault().getPath("C:\\", "Program Files (x86)", "Steam", "steamapps", "common", "Team Fortress 2", "tf", "scripts", "items", "items_game.txt");
		Path path = file.toPath();
		
		initLists();
		
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
	
	private void initLists() { //mostly to declutter constructor
		scoutItems.add(scoutPrimary);
		scoutItems.add(scoutSecondary);
		scoutItems.add(scoutMelee);
		scoutItems.add(scoutCosmetics);
		
		soldierItems.add(soldierPrimary);
		soldierItems.add(soldierSecondary);
		soldierItems.add(soldierMelee);
		soldierItems.add(soldierCosmetics);
		
		pyroItems.add(pyroPrimary);
		pyroItems.add(pyroSecondary);
		pyroItems.add(pyroMelee);
		pyroItems.add(pyroCosmetics);
		
		demomanItems.add(demomanPrimary);
		demomanItems.add(demomanSecondary);
		demomanItems.add(demomanMelee);
		demomanItems.add(demomanCosmetics);
		
		heavyItems.add(heavyPrimary);
		heavyItems.add(heavySecondary);
		heavyItems.add(heavyMelee);
		heavyItems.add(heavyCosmetics);
		
		engineerItems.add(engineerPrimary);
		engineerItems.add(engineerSecondary);
		engineerItems.add(engineerMelee);
		engineerItems.add(engineerCosmetics);
		
		medicItems.add(medicPrimary);
		medicItems.add(medicSecondary);
		medicItems.add(medicMelee);
		medicItems.add(medicCosmetics);
		
		sniperItems.add(sniperPrimary);
		sniperItems.add(sniperSecondary);
		sniperItems.add(sniperMelee);
		sniperItems.add(sniperCosmetics);
		
		spyItems.add(spyPDA);
		spyItems.add(spySecondary);
		spyItems.add(spyMelee);
		spyItems.add(spyCosmetics);
		spyItems.add(spyBuilding);
	}
	
	public static String readFile(Path path, Charset encoding) throws IOException {
		//if(!path.endsWith(".pop")) { //if not a popfile
		//	return null;
		//}
		
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}
	
	void parsePrefab(VDFNode item, VDFNode allPrefabs) { //time to suffer
		VDFNode node;
		for(String key : item.keySet()) {
			node = item.getSubNode(key);
			//System.out.println(key);
			
			try { //hats and miscs are weird
				if(node.getString("prefab").contains("misc") || node.getString("prefab").contains("hat")) {			
					try {
						if(!node.getString("equip_region").equals("medal")) { //for now filter out all medals
							getClasses("cosmetic", node.getString("name"), node.getSubNode("used_by_classes"));
						}
					}
					catch (Exception e) {
						getClasses("cosmetic", node.getString("name"), node.getSubNode("used_by_classes"));
						
						//if it ends up here it's either
						//a: item that relies on prefab for its equip_region or
						//b: item with multiple equip_regions
						//so not a medal either way
					}	
				}
				else {
					try {
						String prefabString = node.getString("prefab"); //null ptrs here if no prefab
						String equip = "";
						VDFNode wepPrefab = new VDFNode(String.CASE_INSENSITIVE_ORDER);
						VDFNode classNode = null;
						
						if((prefabString.contains("weapon_") && !prefabString.contains("case")) || 
								(prefabString.contains("valve") && node.getString("craft_class").equals("weapon"))) { 
							//if prefab contains weapon and is not a case or has a prefab of valve + is a weapon
							
							if(node.containsKey("item_slot")) { //items that define equip/class in itself and not in a prefab
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
											addShotguns(node.getString("name"));
									}
									else { //normal rules
										equip = wepPrefab.getString("item_slot");
										classNode = wepPrefab.getSubNode("used_by_classes");
									}
								}
							}
							
							switch (equip) {
								case "primary":
								case "secondary":
								case "melee":
									if(node.get("name") != null) { //if item is non base item, so real name is defined
										getClasses(equip, node.getString("name"), classNode);
									}
									else { //otherwise just get its prefab name
										getClasses(equip, wepPrefab.getString("item_name"), classNode);
									}
									break;
								case "building":
									spyBuilding.add(node.getString("name")); //all sappers are buildings
									break;
								case "pda2":
									spyPDA.add(node.getString("name")); //pda2
									break;
								default:
									break; //only thing that falls down here is engie pda and the shotguns
							}
						}
					}
					catch (NullPointerException f) {
						//System.out.println("no prefab " + node.getString("name"));
					}
				}
			}
			catch (NullPointerException e) {
				
			}
		}
		//for some dumb reason these two are the only cosmetics not defined as a hat or cosmetic
		pyroCosmetics.add("The Burning Bongos");
		demomanCosmetics.add("Six Pack Abs");
		//dumb mistake here
		//soldierPrimary.add("Upgradeable TF_WEAPON_ROCKETLAUNCHER");
	}
	
	private String getPrefabString(VDFNode allPrefabs, String fabs) { //get the weapon prefab
		int first = 0;
		int last = 0;
		
		first = fabs.indexOf("weapon_");
		last = first;
		
		while(last < fabs.length() && fabs.charAt(last) != ' ') {
			last++;
		}
		if(first != 0 && fabs.charAt(first - 1) == '_') { //if it's a paintkit with a prefix
			while(first > 0 && fabs.charAt(first) != ' ') {
				first--;
			} //recursively grab its usuable prefab
			fabs = getPrefabString(allPrefabs, allPrefabs.getSubNode(fabs.substring(first, last)).getString("prefab"));
		}
		else {
			fabs = fabs.substring(first, last);
		}
		return fabs;
	}
	
	//manually adding shotguns 
	private void addShotguns(String name) {
		soldierSecondary.add(name);
		pyroSecondary.add(name);
		heavySecondary.add(name);
		engineerPrimary.add(name);
	}
	
	//add the item to appropriate class + slot
	private void getClasses(String slot, String name, VDFNode classes) {
		int type = 0;
		
		switch (slot) {
			case "primary":
				type = ItemSlot.PRIMARY.getSlot();
				break;
			case "secondary":
				type = ItemSlot.SECONDARY.getSlot();
				break;
			case "melee":
				type = ItemSlot.MELEE.getSlot();
				break;
			case "cosmetic":
				type = ItemSlot.COSMETIC1.getSlot() - 1;
				break;
		}
		
		if(classes.containsKey("scout")) {
			scoutItems.get(type).add(name);
		}
		if(classes.containsKey("soldier")) {
			soldierItems.get(type).add(name);
		}
		if(classes.containsKey("pyro")) {
			pyroItems.get(type).add(name);
		}
		if(classes.containsKey("demoman")) {
			demomanItems.get(type).add(name);
		}
		if(classes.containsKey("heavy")) {
			heavyItems.get(type).add(name);
		}
		if(classes.containsKey("engineer")) {		
			engineerItems.get(type).add(name);
		}
		if(classes.containsKey("medic")) {
			medicItems.get(type).add(name);
		}
		if(classes.containsKey("sniper")) {
			sniperItems.get(type).add(name);
		}
		if(classes.containsKey("spy")) {
			spyItems.get(type).add(name);
		}
	}
	
	//get class items based on input class
	public List<List<String>> getClassList(EngiPanel.Classes selected) {
		List<List<String>> classList = new ArrayList<List<String>>();
		
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
	public List<String> checkIfItemInSlot(List<Object> items, EngiPanel.Classes selected, int slot) throws IndexOutOfBoundsException {
		List<String> list = getClassList(selected).get(slot);
		//Set<String> subset = new HashSet<String>(list);
		//Set<String> itemSet = new HashSet<String>(items);
		
		list.retainAll(items);
		
		//subset.retainAll(itemSet);
		
		return list;
	}
}
