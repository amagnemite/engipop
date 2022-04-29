package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;

//convenience class for reused methods / static variables
public class EngiPanel extends JPanel {
	GridBagConstraints gb;
	
	public static final String[] CLASSES = {"Scout", "Soldier", "Pyro",
			"Demoman", "Heavyweapons", "Engineer",
			"Medic", "Sniper", "Spy"};
	
	public static int tankDefaultHealth = 50000;
	public static double tankDefaultSpeed = 75.0;
	public static String tankDefaultName = "tankboss";
	
	//method to position on grid bag layout
	public void addGB(Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		add(comp, gb);
	}	
}
