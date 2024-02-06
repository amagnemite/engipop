package engipop;

import java.nio.file.Path;
import java.nio.file.Paths;

import engipop.Node.PopNode;

//important stuff
public class Engipop {
	
	private static Path TFPATH = null;
	private static PopNode POPNODE = null;
	
	public enum Classes { //class names + their default weps
		None ("n/a", "n/a", "n/a", 0),
		Scout ("TF_WEAPON_SCATTERGUN", "TF_WEAPON_PISTOL_SCOUT", "TF_WEAPON_BAT", 1), 
		Soldier ("TF_WEAPON_ROCKETLAUNCHER", "TF_WEAPON_SHOTGUN_SOLDIER", "TF_WEAPON_SHOVEL", 2),
		Pyro ("TF_WEAPON_FLAMETHROWER", "TF_WEAPON_SHOTGUN_PYRO", "TF_WEAPON_FIREAXE", 3),
		Demoman ("TF_WEAPON_GRENADELAUNCHER", "TF_WEAPON_PIPEBOMBLAUNCHER", "TF_WEAPON_BOTTLE", 4),
		Heavyweapons ("TF_WEAPON_MINIGUN", "TF_WEAPON_SHOTGUN_HWG", "TF_WEAPON_FISTS", 5),
		Engineer ("TF_WEAPON_SHOTGUN_PRIMARY", "TF_WEAPON_PISTOL", "TF_WEAPON_WRENCH", 6),
		Medic ("TF_WEAPON_SYRINGEGUN_MEDIC", "TF_WEAPON_MEDIGUN", "TF_WEAPON_BONESAW", 7),
		Sniper ("TF_WEAPON_SNIPERRIFLE", "TF_WEAPON_SMG", "TF_WEAPON_CLUB", 8),
		Spy ("TF_WEAPON_INVIS", "TF_WEAPON_REVOLVER", "TF_WEAPON_KNIFE", 9);
		
		private final String primary;
		private final String secondary;
		private final String melee;
		private final int slot;
		Classes(String primary, String secondary, String melee, int slot) {
			this.primary = primary;
			this.secondary = secondary;
			this.melee = melee;
			this.slot = slot;
		}
		public String primary() { return primary; }
		public String secondary() { return secondary; }
		public String melee() { return melee; }
		public String building() { return "TF_WEAPON_BUILDER_SPY"; } //dumb spy hardcode
		public int getSlot() { return slot; }
		
		public static Classes toClass(String str) { //dumb
			Classes c;
			
			if(str == null) {
				return Classes.None;
			}
			
			switch(str.toLowerCase()) {
				case "scout":
					c = Classes.Scout;
					break;
				case "soldier":
					c = Classes.Soldier;
					break;
				case "pyro":
					c = Classes.Pyro;
					break;
				case "demoman":
					c = Classes.Demoman;
					break;
				case "heavyweapons":
					c = Classes.Heavyweapons;
					break;
				case "engineer":
					c = Classes.Engineer;
					break;
				case "medic":
					c = Classes.Medic;
					break;
				case "sniper":
					c = Classes.Sniper;
					break;
				case "spy":
					c = Classes.Spy;
					break;
				case "none":
				default:
					c = Classes.None;
					break;
			}
			
			//dumb check since heavy is also valid
			if(str.toLowerCase().contains("heavy")) {
				return Classes.Heavyweapons;
			}
			
			return c;
		}
	}
	
	//character item slots
	public enum ItemSlot {
		NONE (-1),
		PRIMARY (0),
		SECONDARY (1),
		MELEE (2),
		BUILDING (3),
		COSMETIC1 (4),
		COSMETIC2 (5),
		COSMETIC3 (6),
		CHARACTER (7);
		//may need to add actionitem
		 
		private final int slot;
		ItemSlot(int slot) {
			this.slot = slot;
		}
		public int getSlot() { return slot; }
	}

	public static final int TANKDEFAULTHEALTH = 50000;
	public static final double TANKDEFAULTSPEED = 75.0;
	public static final String TANKDEFAULTNAME = "tankboss";
	public static final int MAXACTIVE = 22;
	public static final int BUSTERDEFAULTDMG = 3000;
	public static final int BUSTERDEFAULTKILLS = 15;
	
	public static void setPopNode(PopNode popnode) {
		POPNODE = popnode;
	}
	
	public static PopNode getPopNode() {
		if(POPNODE == null) {
			POPNODE = new PopNode();
		}
		return POPNODE;
	}
	
	public static void setTFPath(Path tfpath) {
		TFPATH = tfpath;
	}
	
	public static Path getTFPath() {
		return TFPATH;
	}
	
	public static Path getScriptPath() {
		return TFPATH.resolve("scripts");
	}

	public static Path getItemSchemaPath() {
		return Paths.get(TFPATH.toString(), "items", "items_game.txt");
	}
	
	public static Path getIconPath() {
		return Paths.get(TFPATH.toString(), "materials", "hud");
	}
	
	public static Path getDownloadIconPath() {
		return Paths.get(TFPATH.toString(), "download", "materials", "hud");
	}
}
