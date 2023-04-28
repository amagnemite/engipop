package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//convenience class for reused methods / static variables
public class EngiPanel extends JPanel {
	protected GridBagLayout gbLayout = new GridBagLayout();
	protected GridBagConstraints gbConstraints = new GridBagConstraints();
	
	public enum Classes { //class names + their default weps
		None ("n/a", "n/a", "n/a"),
		Scout ("TF_WEAPON_SCATTERGUN", "TF_WEAPON_PISTOL_SCOUT", "TF_WEAPON_BAT"), 
		Soldier ("TF_WEAPON_ROCKETLAUNCHER", "TF_WEAPON_SHOTGUN_SOLDIER", "TF_WEAPON_SHOVEL"),
		Pyro ("TF_WEAPON_FLAMETHROWER", "TF_WEAPON_SHOTGUN_PYRO", "TF_WEAPON_FIREAXE"),
		Demoman ("TF_WEAPON_GRENADELAUNCHER", "TF_WEAPON_PIPEBOMBLAUNCHER", "TF_WEAPON_BOTTLE"),
		Heavyweapons ("TF_WEAPON_MINIGUN", "TF_WEAPON_SHOTGUN_HWG", "TF_WEAPON_FISTS"),
		Engineer ("TF_WEAPON_SHOTGUN_PRIMARY", "TF_WEAPON_PISTOL", "TF_WEAPON_WRENCH"),
		Medic ("TF_WEAPON_SYRINGEGUN_MEDIC", "TF_WEAPON_MEDIGUN", "TF_WEAPON_BONESAW"),
		Sniper ("TF_WEAPON_SNIPERRIFLE", "TF_WEAPON_SMG", "TF_WEAPON_CLUB"),
		Spy ("TF_WEAPON_INVIS", "TF_WEAPON_REVOLVER", "TF_WEAPON_KNIFE");
		
		private final String primary;
		private final String secondary;
		private final String melee;
		Classes(String primary, String secondary, String melee) {
			this.primary = primary;
			this.secondary = secondary;
			this.melee = melee;
		}
		public String primary() { return primary; }
		public String secondary() { return secondary; }
		public String melee() { return melee; }
		public String building() { return "TF_WEAPON_BUILDER_SPY"; } //dumb spy hardcode
		
		public static Classes toClass(String str) { //dumb
			Classes c;
			
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
	
	//method to position on grid bag layout
	protected void addGB(Component comp, int x, int y) {
		gbConstraints.gridx = x;
		gbConstraints.gridy = y;
		add(comp, gbConstraints);
	}
	
	//set visibility of a component and its paired label
	protected void setComponentAndLabelVisible(JLabel label, JComponent box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
}
