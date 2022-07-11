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
	protected GridBagConstraints gb = new GridBagConstraints();
	
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
	}
	
	//character item slots
	public enum ItemSlot {
		 NONE (-1),
		 PRIMARY (0),
		 SECONDARY (1),
		 MELEE (2),
		 BUILDING (3),
		 CHARACTER (4),
		 HAT1 (5),
		 HAT2 (6),
		 HAT3 (7);
		 
		 private final int slot;
		 ItemSlot(int slot) {
			 this.slot = slot;
		 }
		 public int getSlot() { return slot; }
	}
	
	/*
	//simplified version of the above
	public enum ItemSlotAlternate {
		//NONE (-1),
		PRIMARY (0),
		SECONDARY (1),
		MELEE (2),
		COSMETIC (3),
		COSMETIC2 (4),
		COSMETIC3 (5),
		BUILDING (6);
		
		private final int slot;
		ItemSlotAlternate(int slot) {
			this.slot = slot;
		}
		public int getSlot() { return slot; }
	} */
	
	public static final int PRIMARY = 0;
	public static final int SECONDARY = 1;
	public static final int MELEE = 2;
	public static final int COSMETIC = 3;
	public static final int COSMETIC2 = 4;
	public static final int COSMETIC3 = 5;
	public static final int BUILDING = 6;
	
	public static int tankDefaultHealth = 50000;
	public static double tankDefaultSpeed = 75.0;
	public static String tankDefaultName = "tankboss";
	public static int maxActive = 22;
	
	//method to position on grid bag layout
	protected void addGB(Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		add(comp, gb);
	}
	
	//set visibility of a component and its paired label
	protected void setComponentAndLabelVisible(JLabel label, JComponent box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
}
