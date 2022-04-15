package engipop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.Position;

import engipop.Tree.TFBotNode;
//import engipop.window;

public class BotPanel extends JPanel { //class to make the panel for bot creation/editing

	static final String[] CLASSES = {"Scout", "Soldier", "Pyro",
			"Demoman", "HeavyWeapons", "Engineer",
			"Medic", "Sniper", "Spy"};
	//this is copied from window, fix later?
	
	String[] tags = {"bot_giant", "bot_squad_member", "bot_gatebot"};
	String[] attr = {"todo: big attr list goes here"};
	
	String[] primaries = {"todo: figure out how to store wep lists"};
	String[] secs = {"todo: figure out how to store wep lists"};
	String[] melees = {"todo: figure out how to store wep lists"};
	
	String[] itemAttr = {"todo: big item attr list goes here"};
	String[] charAttr = {"todo: big char attr list goes here"};
	
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gb = new GridBagConstraints();
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel;
	
	JTextField nameField = new JTextField(30);
	JComboBox<String> classBox;
	JComboBox<String> iconBox;
	JList<String> tagList;
	JList<String> attrList;
	JList<String> primaryList;
	JList<String> secList;
	JList<String> meleeList;
	JList<String> primAttrList;
	JList<String> secAttrList;
	JList<String> meleeAttrList;
	JList<String> charAttrList;
	
	ButtonGroup wepGroup;
	ButtonGroup skillGroup;
	
	JRadioButton easyBut;
	JRadioButton normalBut;
	JRadioButton hardBut;
	JRadioButton expBut;
	
	JRadioButton anyBut;
	JRadioButton priBut;
	JRadioButton secBut;
	JRadioButton melBut;
	
	public BotPanel() {
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		
		classBox = new JComboBox<String>(CLASSES);
		iconBox = new JComboBox<String>();
		
		iconModel = (DefaultComboBoxModel<String>) iconBox.getModel();
		iconBox.setPrototypeDisplayValue("heavyweapons_healonkill_giant");
		
		setIconBox(iconBox, iconModel, "Scout");
		
		classBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				String str = (String) classBox.getSelectedItem();
				setIconBox(iconBox, iconModel, str);
				
				//currentBotNode.setClassName((String) classBox.getSelectedItem());
				
				//setPrimaryList(primaryList, index);
				//setSecondaryList(secList, index);
				//setMeleeList(meleeList, index);
			}
		});
		
		tagList = new JList<String>(tags);
		//allow custom entry and or load additional tags based on map
		
		attrList = new JList<String>(attr);
		
		primaryList = new JList<String>(primaries);
		
		secList = new JList<String>(secs);
		
		meleeList = new JList<String>(melees);
		
		primAttrList = new JList<String>(itemAttr);
		
		secAttrList = new JList<String>(itemAttr);
		
		meleeAttrList = new JList<String>(itemAttr);
		
		charAttrList = new JList<String>(charAttr);

		//skill level radio buttons
		JPanel skillPanel = new JPanel();
		skillGroup = new ButtonGroup();
		skillPanel.add(easyBut = new JRadioButton("Easy"));
		easyBut.setActionCommand("Easy");
		skillGroup.add(easyBut);
		skillPanel.add(normalBut = new JRadioButton("Normal"));
		normalBut.setActionCommand("Normal");
		skillGroup.add(normalBut);
		skillPanel.add(hardBut = new JRadioButton("Hard"));
		hardBut.setActionCommand("Hard");
		skillGroup.add(hardBut);
		skillPanel.add(expBut = new JRadioButton("Expert"));
		expBut.setActionCommand("Expert");
		skillGroup.add(expBut);
		skillGroup.setSelected(easyBut.getModel(), true);
		
		//wep restrict radio buttons
		JPanel wepPanel = new JPanel();
		wepGroup = new ButtonGroup();
		//JRadioButton wepRadio;
		wepPanel.add(anyBut = new JRadioButton("Any"));
		anyBut.setActionCommand("Any");
		wepGroup.add(anyBut);
		wepPanel.add(priBut = new JRadioButton("PrimaryOnly"));
		priBut.setActionCommand("PrimaryOnly");
		wepGroup.add(priBut);		
		wepPanel.add(secBut = new JRadioButton("SecondaryOnly"));
		secBut.setActionCommand("SecondaryOnly");
		wepGroup.add(secBut);		
		wepPanel.add(melBut = new JRadioButton("MeleeOnly"));
		melBut.setActionCommand("MeleeOnly");
		wepGroup.add(melBut);
		wepGroup.setSelected(anyBut.getModel(), true);
		

		
		JLabel botClass = new JLabel("Class: ");
		JLabel botName = new JLabel("Name: ");
		JLabel botIcon = new JLabel("Icon: ");
		JLabel botSkill = new JLabel("Skill: ");
		JLabel botRestrict = new JLabel("WeaponRestrictions: ");
		JLabel botTags = new JLabel("Tags: ");
		JLabel botAttributes = new JLabel("Attributes: ");
		JLabel botPrimary = new JLabel("Primary weapon: ");
		JLabel botPriAttr = new JLabel("ItemAttributes: ");
		JLabel botSecondary = new JLabel("Secondary weapon: ");
		JLabel botSecAttr = new JLabel("ItemAttributes: ");
		JLabel botMelee = new JLabel("Melee weapon: ");
		JLabel botMelAttr = new JLabel("ItemAttributes: ");
		JLabel botCharAttr = new JLabel("CharacterAttributes: ");
		
		addGB(botClass, 0, 0);
		addGB(new JScrollPane(classBox), 1, 0); //fix size
		addGB(botIcon, 2, 0);
		addGB(new JScrollPane(iconBox), 3, 0); //fix size so it doesn't change for long lists
		
		addGB(botName, 0, 1);
		
		addGB(botSkill, 0, 3);
		addGB(skillPanel, 1, 3);
		addGB(botRestrict, 0, 4);
		addGB(wepPanel, 1, 4);
		addGB(botTags, 0, 5);
		addGB(tagList, 1, 5);
		addGB(botAttributes, 0, 6);
		addGB(attrList, 1, 6);
		addGB(botPrimary, 0, 7);
		addGB(primaryList, 1, 7);
		addGB(botPriAttr, 2, 7);
		addGB(primAttrList, 3, 7);
		addGB(botSecondary, 0, 8);
		addGB(secList, 1, 8);
		addGB(botSecAttr, 2, 8);
		addGB(secAttrList, 3, 8);		
		addGB(botMelee, 0, 9);
		addGB(meleeList, 1, 9);
		addGB(botMelAttr, 2, 9);
		addGB(meleeAttrList, 3, 9);	
		addGB(botCharAttr, 0, 10);
		addGB(charAttrList, 1, 10);
		
		addGB(nameField, 1, 1); 
	}
	
	public void updatePanel(TFBotNode tf) {
		int[] indices = new int[tagList.getModel().getSize()];
		
		classBox.setSelectedItem(tf.getClassName());
		setIconBox(iconBox, iconModel, tf.getClassName());
		iconBox.setSelectedItem(tf.getIcon());
		nameField.setText(tf.getName());
		switch (tf.getSkill()) {
			case "Easy":
				skillGroup.setSelected(easyBut.getModel(), true);
				break;
			case "Normal":
				skillGroup.setSelected(normalBut.getModel(), true);
				break;
			case "Hard":
				skillGroup.setSelected(hardBut.getModel(), true);
				break;
			case "Expert":
				skillGroup.setSelected(expBut.getModel(), true);
				break;
		}
		switch (tf.getWepRestrict()) {
			case "Any":
				wepGroup.setSelected(anyBut.getModel(), true);
				break;
			case "PrimaryOnly":
				wepGroup.setSelected(priBut.getModel(), true);
				break;
			case "SecondaryOnly":
				wepGroup.setSelected(secBut.getModel(), true);
				break;
			case "MeleeOnly":
				wepGroup.setSelected(melBut.getModel(), true);
				break;
		}
		if(tf.getTags().size() > 0) {
			for(int i = 0; i < tf.getTags().size(); i++) { //get indices so they can be selected
				indices[i] = (tagList.getNextMatch(tf.getTags().get(i), 0, Position.Bias.Forward));
			}
			tagList.setSelectedIndices(indices);
		}	
	}
	
	public void clearPanel() { //clear panel
		classBox.setSelectedItem("Scout");
		setIconBox(iconBox, iconModel, "Scout");
		nameField.setText("");
		skillGroup.setSelected(easyBut.getModel(), true);
		wepGroup.setSelected(anyBut.getModel(), true);
		tagList.clearSelection();
	}
	
	public void updateNode(TFBotNode tf) {
		tf.setClassName((String) classBox.getSelectedItem());
		tf.setIcon((String) iconBox.getSelectedItem());
		tf.setName(nameField.getText());
		tf.setSkill(skillGroup.getSelection().getActionCommand());
		//awful, but should only have one selection
		tf.setWepRestrict(wepGroup.getSelection().getActionCommand());
		if(!tagList.getSelectedValuesList().isEmpty()) { //probably some other funny conversion bugs, double check
			tf.setTags((ArrayList<String>) tagList.getSelectedValuesList());
		}
	}
	
	private void addGB(Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		add(comp, gb);
	}	
	
	void setIconBox(JComboBox<String> jb, DefaultComboBoxModel<String> model, String str) { //set icon list depending on class
		//todo: load icons from 
		model.removeAllElements();
		
		switch (str) {
			case "Scout":
				String[] scout = {"scout", "scout_bat", "scout_bonk",
						"scout_fan", "scout_giant_fast", "scout_jumping",
						"scout_shortstop", "scout_stun", "scout_stun_armored"};				
				for(String item : scout) {
					model.addElement(item);
				}
				break;
			case "Soldier":
				String[] soldier = {"soldier", "soldier_backup", "soldier_barrage",
						"soldier_blackbox", "soldier_buff", "soldier_burstfire",
						"soldier_conch", "soldier_crit", "soldier_libertylauncher",
						"soldier_major_crits", "soldier_sergeant_crits", "soldier_spammer"};
				for(String item : soldier) {
					model.addElement(item);
				}
				break;
			case "Pyro":
				String[] pyro = {"pyro", "pyro_flare"};
				for(String item : pyro) {
					model.addElement(item);
				}
				break;
			case "Demoman":
				String[] demo = {"demo", "demo_bomber", "demo_burst",
						"demoknight", "demoknight_samurai"};
				for(String item : demo) {
					model.addElement(item);
				}
				break;
			case "HeavyWeapons":
				String[] heavy = {"heavy", "heavy_champ", "heavy_chief",
						"heavy_deflector", "heavy_deflector_healonkill", "heavy_deflector_push",
						"heavy_gru", "heavy_heater", "heavy_mittens", "heavy_shotgun",
						"heavy_steelfist", "heavy_urgent"};
				for(String item : heavy) {
					model.addElement(item);
				}
				break;
			case "Engineer":
				String[] engie = {"engineer"};
				for(String item : engie) {
					model.addElement(item);
				}
				break;
			case "Medic":
				String[] medic = {"medic", "medic_uber"};
				for(String item : medic) {
					model.addElement(item);
				}
				break;
			case "Sniper":
				String[] sniper = {"sniper", "sniper_bow", "sniper_multi",
						"sniper_jarate", "sniper_sydneysleeper"};
				for(String item : sniper) {
					model.addElement(item);
				}
				break;
			case "Spy":
				String[] spy = {"spy"};
				for(String item : spy) {
					model.addElement(item);
				}
				break;
		}
		jb.setModel(model);
	}
}
