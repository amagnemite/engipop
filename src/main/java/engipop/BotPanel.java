package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

import engipop.ButtonListManager.States;
import engipop.Node.TFBotNode;

//todo: add some sort of sanity checking for itemattributes
@SuppressWarnings("serial")
public class BotPanel extends EngiPanel implements PropertyChangeListener { //class to make the panel for bot creation/editing
	
	String[] tags = {"bot_giant", "bot_squad_member"}; //potentially move bot_giant
	
	EngiWindow window;
	AttributesPanel attrPanel;
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel = new DefaultComboBoxModel<String>();
	DefaultListModel<String> tagModel = new DefaultListModel<String>();
	DefaultComboBoxModel<String> templateModel = new DefaultComboBoxModel<String>();
	//ComboBoxModel<String> templateModel; //double check if access to model is actually needed
	
	JTextField nameField = new JTextField(30); //max bot name is ~32
	JTextField templateField = new JTextField(15);
	JComboBox<Classes> classBox = new JComboBox<Classes>(Classes.values());
	JComboBox<String> iconBox = new JComboBox<String>(iconModel);
	JComboBox<String> templateBox = new JComboBox<String>(templateModel);
	JList<String> tagList = new JList<String>(tags);
	JList<String> attributesList;
	
	List<JComboBox<String>> itemLists = new ArrayList<JComboBox<String>>();
	
	JComboBox<String> primaryList = new JComboBox<String>();
	JComboBox<String> secList = new JComboBox<String>();
	JComboBox<String> meleeList = new JComboBox<String>();
	JComboBox<String> buildingList = new JComboBox<String>();
	JComboBox<String> hat1List = new JComboBox<String>();
	JComboBox<String> hat2List = new JComboBox<String>();
	JComboBox<String> hat3List = new JComboBox<String>();
	
	JComboBox<String> itemAttributesListBox;
	
	ItemParser parser; //make sure all botpanels have same list
	
	JLabel botBuilding = new JLabel("Sapper: ");
	
	ButtonGroup wepGroup = new ButtonGroup();
	ButtonGroup skillGroup = new ButtonGroup();
	
	JRadioButton easyBut = new JRadioButton(TFBotNode.EASY);
	JRadioButton normalBut = new JRadioButton(TFBotNode.NORMAL);
	JRadioButton hardBut = new JRadioButton(TFBotNode.HARD);
	JRadioButton expBut = new JRadioButton(TFBotNode.EXPERT);
	
	JRadioButton anyBut = new JRadioButton(TFBotNode.ANY);
	JRadioButton priBut = new JRadioButton(TFBotNode.PRIMARYONLY);
	JRadioButton secBut = new JRadioButton(TFBotNode.SECONDARYONLY);
	JRadioButton melBut = new JRadioButton(TFBotNode.MELEEONLY);
	
	//radio buttons to select active attribute editing
	JPanel attrButPanel = new JPanel();
	JPanel attrButPanel2 = new JPanel();
	ButtonGroup attrGroup = new ButtonGroup();
	JRadioButton noAttrBut = new JRadioButton("None");
	JRadioButton charAttrBut = new JRadioButton("CharacterAttributes");
	JRadioButton priAttrBut = new JRadioButton("Primary ItemAttributes");
	JRadioButton secAttrBut = new JRadioButton("Secondary ItemAttributes");
	JRadioButton melAttrBut = new JRadioButton("Melee ItemAttributes");
	JRadioButton buiAttrBut = new JRadioButton("Sapper ItemAttributes");
	JRadioButton hat1AttrBut = new JRadioButton("Cosmetic 1 ItemAttributes"); //consider updating these dynamically
	JRadioButton hat2AttrBut = new JRadioButton("Cosmetic 2 ItemAttributes");
	JRadioButton hat3AttrBut = new JRadioButton("Cosmetic 3 ItemAttributes");
	
	public BotPanel(EngiWindow window, MainWindow mainWindow, SecondaryWindow secondaryWindow) {
		//window to send feedback to, mainwindow to get item updates, secondarywindow to get map updates
		
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		
		//this.parser = parser;
		
		this.window = window;
		secondaryWindow.addPropertyChangeListener(SecondaryWindow.TAGS, this);
		mainWindow.addPropertyChangeListener(this);
		
		attrPanel = new AttributesPanel(ItemAttributes.getItemAttributes());
		attrPanel.setVisible(false);
		
		initItemLists();
		initAttributeRadio();
		
		attributesList = new JList<String>(setAttributesList());
		
		iconBox.setPrototypeDisplayValue("heavyweapons_healonkill_giant");
		templateBox.setPrototypeDisplayValue("T_TFBot_Heavyweapons");
		tagList.setPrototypeCellValue("bot_squad_member");
		
		//setIconBox(iconBox, iconModel, "Scout");
		
		classBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				Classes str = (Classes) classBox.getSelectedItem();
				setIconBox(iconBox, iconModel, str);
				if(parser != null) { //prevent loading in cases where items_game.txt is unknown
					setClassItems((Classes) classBox.getSelectedItem());
				}		
							
				if(classBox.getSelectedIndex() == 8) {
					buildingState(true);
				}
				else {
					buildingState(false);
				}
			}
		});
		
		classBox.setSelectedIndex(0);
		
		primaryList.setEditable(true);
		secList.setEditable(true);
		meleeList.setEditable(true);
		buildingList.setEditable(true);
		hat1List.setEditable(true);
		hat2List.setEditable(true);
		hat3List.setEditable(true);
		buildingList.setVisible(false);
		templateBox.setEditable(true);
	
		//skill level radio buttons
		JPanel skillPanel = new JPanel();
		
		skillPanel.add(easyBut);
		easyBut.setActionCommand(TFBotNode.EASY);
		skillGroup.add(easyBut);
		skillPanel.add(normalBut);
		normalBut.setActionCommand(TFBotNode.NORMAL);
		skillGroup.add(normalBut);
		skillPanel.add(hardBut);
		hardBut.setActionCommand(TFBotNode.HARD);
		skillGroup.add(hardBut);
		skillPanel.add(expBut);
		expBut.setActionCommand(TFBotNode.EXPERT);
		skillGroup.add(expBut);
		skillGroup.setSelected(easyBut.getModel(), true);
		
		//wep restrict radio buttons
		JPanel wepPanel = new JPanel();
		wepPanel.add(anyBut);
		anyBut.setActionCommand(TFBotNode.ANY);
		wepGroup.add(anyBut);
		wepPanel.add(priBut);
		priBut.setActionCommand(TFBotNode.PRIMARYONLY);
		wepGroup.add(priBut);
		wepPanel.add(secBut);
		secBut.setActionCommand(TFBotNode.SECONDARYONLY);
		wepGroup.add(secBut);		
		wepPanel.add(melBut);
		melBut.setActionCommand(TFBotNode.MELEEONLY);
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
		JLabel botSecondary = new JLabel("Secondary weapon: ");
		JLabel botMelee = new JLabel("Melee weapon: ");
		JLabel botHat = new JLabel("Hats: ");
		JLabel templateLabel = new JLabel("Template: ");
	
		//shrinks down the attribute radio button panels so they fit better
		((FlowLayout) attrButPanel2.getLayout()).setVgap(0);
		((FlowLayout) attrButPanel.getLayout()).setVgap(0);
		attrButPanel.setPreferredSize(new Dimension(529, 20));
		attrButPanel2.setPreferredSize(new Dimension(557, 20));
		
		JScrollPane tagListPane = new JScrollPane(tagList);
		JScrollPane attributesListPane = new JScrollPane(attributesList);
		
		//prevents dumb resizing stuff with the attr panel
		//may fix later
		nameField.setMinimumSize(nameField.getPreferredSize());
		classBox.setMinimumSize(classBox.getPreferredSize());
		iconBox.setMinimumSize(iconBox.getPreferredSize()); //double check this once custom input happens
		tagListPane.setMinimumSize(tagList.getPreferredScrollableViewportSize());
		attributesListPane.setMinimumSize(attributesList.getPreferredScrollableViewportSize());
		
		addGB(botClass, 0, 0);
		addGB(classBox, 1, 0);
		addGB(botIcon, 2, 0);
		addGB(iconBox, 3, 0); //fix size so it doesn't change for long lists
		
		addGB(botName, 0, 1);
		addGB(nameField, 1, 1); 
		addGB(templateLabel, 2, 1);
		addGB(templateBox, 3, 1);
		
		addGB(botSkill, 0, 3);
		addGB(skillPanel, 1, 3);
		addGB(botRestrict, 2, 3);
		addGB(wepPanel, 3, 3);
		addGB(botTags, 0, 5);
		addGB(tagListPane, 1, 5);
		
		addGB(botAttributes, 2, 5);
		addGB(attributesListPane, 3, 5);
		
		addGB(botPrimary, 0, 8);
		addGB(primaryList, 1, 8);
		addGB(botSecondary, 0, 9);
		addGB(secList, 1, 9);
		addGB(botMelee, 0, 10);
		addGB(meleeList, 1, 10);
		
		addGB(botBuilding, 0, 11);
		addGB(buildingList, 1, 11);
		
		addGB(botHat, 0, 12);
		addGB(hat1List, 1, 12);		
		addGB(hat2List, 1, 13);		
		addGB(hat3List, 1, 14);
		
		gbConstraints.anchor = GridBagConstraints.EAST;
		addGB(buiAttrBut, 2, 9);
		
		gbConstraints.anchor = GridBagConstraints.CENTER;
		gbConstraints.gridwidth = 2;
		addGB(attrButPanel, 2, 7);
		addGB(attrButPanel2, 2, 8);
		
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridheight = 8;
		addGB(attrPanel, 2, 10);
	}
	
	private void initItemLists() { //add boxes to the list of lists
		itemLists.add(primaryList);
		itemLists.add(secList);
		itemLists.add(meleeList);
		itemLists.add(hat1List);
		itemLists.add(hat2List);
		itemLists.add(hat3List);
		itemLists.add(buildingList);
	}
	
	private void buildingState(boolean state) { //change building related visibility
		buildingList.setVisible(state);
		botBuilding.setVisible(state);
		buiAttrBut.setVisible(state);
	}
	
	public void updatePanel(TFBotNode tf) {
		int[] indices = new int[tagList.getModel().getSize()]; //max possible taglist
		
		classBox.setSelectedItem(tf.getValueSingular(TFBotNode.CLASSNAME));
		setIconBox(iconBox, iconModel, (Classes) tf.getValueSingular(TFBotNode.CLASSNAME)); //values of classname are classes from enum
		iconBox.setSelectedItem(tf.getValueSingular(TFBotNode.CLASSICON));
		nameField.setText((String) tf.getValueSingular(TFBotNode.NAME));
		switch ((String) tf.getValueSingular(TFBotNode.SKILL)) {
			case TFBotNode.EASY:
				skillGroup.setSelected(easyBut.getModel(), true);
				break;
			case TFBotNode.NORMAL:
				skillGroup.setSelected(normalBut.getModel(), true);
				break;
			case TFBotNode.HARD:
				skillGroup.setSelected(hardBut.getModel(), true);
				break;
			case TFBotNode.EXPERT:
				skillGroup.setSelected(expBut.getModel(), true);
				break;
		}
		switch ((String) tf.getValueSingular(TFBotNode.WEAPONRESTRICT)) {
			case TFBotNode.ANY:
				wepGroup.setSelected(anyBut.getModel(), true);
				break;
			case TFBotNode.PRIMARYONLY:
				wepGroup.setSelected(priBut.getModel(), true);
				break;
			case TFBotNode.SECONDARYONLY:
				wepGroup.setSelected(secBut.getModel(), true);
				break;
			case TFBotNode.MELEEONLY:
				wepGroup.setSelected(melBut.getModel(), true);
				break;
		}
		List<String> tags = (List<String>) tf.getValueSingular(TFBotNode.TAGS);
		if(tags != null) {
			for(int i = 0; i < tags.size(); i++) { //get indices so they can be selected
				indices[i] = tagList.getNextMatch(tags.get(i), 0, Position.Bias.Forward);
			}
			tagList.setSelectedIndices(indices);
		}
		else {
			tagList.clearSelection();
		}
		
		//sort items here if bot isn't sorted already
		//skip if we don't have a class to compare to
		if(tf.getValueSingular(TFBotNode.CLASSNAME) != Classes.None && !tf.isItemsSorted()) {
			Classes cclass = (Classes) tf.getValueSingular(TFBotNode.CLASSNAME);
			String[] itemslots = new String[TFBotNode.ITEMCOUNT];
			
			List<String> itemsList = new ArrayList<String>(Arrays.asList((String[]) tf.getValueArray(TFBotNode.ITEM)));
			
			//this for is somewhat unclear
			for(int slot = ItemSlot.PRIMARY.getSlot(); slot < ItemSlot.BUILDING.getSlot(); slot++) {
				try {
					List<String> sublist = parser.checkIfItemInSlot(itemsList, cclass, slot);
					//check what happens if itemslist is empty
					
					if(!sublist.isEmpty()) { //if not empty, something matched the list
						if(slot == ItemSlot.HAT1.getSlot()) {
							switch(sublist.size()) { 
								case 3: //flow down
									itemslots[ItemSlot.HAT3.getSlot()] = sublist.get(2);
								case 2:
									itemslots[ItemSlot.HAT2.getSlot()] = sublist.get(1);
								case 1:
									itemslots[ItemSlot.HAT1.getSlot()] = sublist.get(0);
									break;
								default:
									break;
									//4 hats or something silly
							}
						}
					}
					else if(slot != ItemSlot.HAT2.getSlot() && slot != ItemSlot.HAT1.getSlot()) { //handle cosmetics all in one go
						itemsList.removeAll(sublist);
						itemslots[slot] = sublist.get(0);
						//should not have multiple primary/secondary/melees/buildings, so should be 1 length array
					}
				}
				catch (IndexOutOfBoundsException i) {
					//thrown if not spy hits building check
					//since we're done then, do nothing
				}
			}
			tf.setItemsSorted(true);
			tf.putKey(TFBotNode.ITEM, itemslots);
		}
		
		if(tf.getValueArray(TFBotNode.ITEM) != null) {
			String[] array = (String[]) tf.getValueArray(TFBotNode.ITEM);
			
			primaryList.setSelectedItem(array[EngiPanel.PRIMARY]);
			secList.setSelectedItem(array[EngiPanel.SECONDARY]);
			meleeList.setSelectedItem(array[EngiPanel.MELEE]);
			if(tf.getValueSingular(TFBotNode.CLASSNAME) == Classes.Spy) {
				buildingList.setSelectedItem(array[EngiPanel.BUILDING]);
			}
			hat1List.setSelectedItem(array[EngiPanel.COSMETIC]);
			hat2List.setSelectedItem(array[EngiPanel.COSMETIC2]);
			hat3List.setSelectedItem(array[EngiPanel.COSMETIC3]);
			
			setHatVisibility((String) hat1List.getSelectedItem(), hat1AttrBut);
			setHatVisibility((String) hat2List.getSelectedItem(), hat2AttrBut);
			setHatVisibility((String) hat3List.getSelectedItem(), hat3AttrBut);
		}
	}
	
	public void updateNode(TFBotNode tf) { //put values into node
		tf.putKey(TFBotNode.CLASSNAME, classBox.getSelectedItem());
		tf.putKey(TFBotNode.CLASSICON, iconBox.getSelectedItem()); //string
		tf.putKey(TFBotNode.NAME, nameField.getText());
		tf.putKey(TFBotNode.SKILL, skillGroup.getSelection().getActionCommand());
		tf.putKey(TFBotNode.WEAPONRESTRICT, wepGroup.getSelection().getActionCommand());
		tf.putKey(TFBotNode.TEMPLATE, templateBox.getSelectedItem());
		tf.putKey(TFBotNode.TAGS, tagList.getSelectedValuesList());
		
		String[] array = new String[TFBotNode.ITEMCOUNT];
		
		array[EngiPanel.PRIMARY] = (String) primaryList.getSelectedItem();
		array[EngiPanel.SECONDARY] = (String) secList.getSelectedItem();
		array[EngiPanel.MELEE] = (String) meleeList.getSelectedItem();
		if(classBox.getSelectedItem() == Classes.Spy) {
			array[EngiPanel.BUILDING] = (String) buildingList.getSelectedItem();
		}
		array[EngiPanel.COSMETIC] = (String) hat1List.getSelectedItem();
		array[EngiPanel.COSMETIC2] = (String) hat2List.getSelectedItem();
		array[EngiPanel.COSMETIC3] = (String) hat3List.getSelectedItem();
		//item attributes are added separately
		
		tf.putKey(TFBotNode.ITEM, array);
		
		setHatVisibility((String) hat1List.getSelectedItem(), hat1AttrBut);
		setHatVisibility((String) hat2List.getSelectedItem(), hat2AttrBut);
		setHatVisibility((String) hat3List.getSelectedItem(), hat3AttrBut);
	}
	
	//set visibility based on contents of string
	private void setHatVisibility(String value, JRadioButton button) {
		if(value == null || value.isEmpty()) {
			button.setVisible(false);
		}
		else {
			button.setVisible(true);
		}
	}
	
	void setIconBox(JComboBox<String> jb, DefaultComboBoxModel<String> model, Classes str) { //set icon list depending on class
		//todo: load icons from 
		model.removeAllElements();
		
		switch (str) {
			case Scout:
				String[] scout = {"scout", "scout_bat", "scout_bonk",
						"scout_fan", "scout_giant_fast", "scout_jumping",
						"scout_shortstop", "scout_stun", "scout_stun_armored"};				
				for(String item : scout) {
					model.addElement(item);
				}
				break;
			case Soldier:
				String[] soldier = {"soldier", "soldier_backup", "soldier_barrage",
						"soldier_blackbox", "soldier_buff", "soldier_burstfire",
						"soldier_conch", "soldier_crit", "soldier_libertylauncher",
						"soldier_major_crits", "soldier_sergeant_crits", "soldier_spammer"};
				for(String item : soldier) {
					model.addElement(item);
				}
				break;
			case Pyro:
				String[] pyro = {"pyro", "pyro_flare"};
				for(String item : pyro) {
					model.addElement(item);
				}
				break;
			case Demoman:
				String[] demo = {"demo", "demo_bomber", "demo_burst",
						"demoknight", "demoknight_samurai"};
				for(String item : demo) {
					model.addElement(item);
				}
				break;
			case Heavyweapons:
				String[] heavy = {"heavy", "heavy_champ", "heavy_chief",
						"heavy_deflector", "heavy_deflector_healonkill", "heavy_deflector_push",
						"heavy_gru", "heavy_heater", "heavy_mittens", "heavy_shotgun",
						"heavy_steelfist", "heavy_urgent"};
				for(String item : heavy) {
					model.addElement(item);
				}
				break;
			case Engineer:
				String[] engie = {"engineer"};
				for(String item : engie) {
					model.addElement(item);
				}
				break;
			case Medic:
				String[] medic = {"medic", "medic_uber"};
				for(String item : medic) {
					model.addElement(item);
				}
				break;
			case Sniper:
				String[] sniper = {"sniper", "sniper_bow", "sniper_multi",
						"sniper_jarate", "sniper_sydneysleeper"};
				for(String item : sniper) {
					model.addElement(item);
				}
				break;
			case Spy:
				String[] spy = {"spy"};
				for(String item : spy) {
					model.addElement(item);
				}
				break;
			case None:
			default:
				break;
		}
	}
	
	//update item lists
	public void setItemParser(ItemParser parser) {
		this.parser = parser;
		setClassItems(Classes.Scout); //since force updating, just default to first class
	}
	
	public void setClassItems(Classes index) { //take class and get items from the parser
		primaryList.removeAllItems();
		secList.removeAllItems();
		meleeList.removeAllItems();
		hat1List.removeAllItems();
		hat2List.removeAllItems();
		hat3List.removeAllItems();
		
		if(index != Classes.None) {
			List<List<String>> lists = parser.getClassList(index);
			
			//set model(get the appropriate slot from lists, convert to a new string array of size inner list)
			primaryList.setModel(getNewModel(lists, EngiPanel.PRIMARY));
			secList.setModel(getNewModel(lists, EngiPanel.SECONDARY));
			meleeList.setModel(getNewModel(lists, EngiPanel.MELEE));
			
			hat1List.setModel(getNewModel(lists, EngiPanel.COSMETIC));
			hat2List.setModel(getNewModel(lists, EngiPanel.COSMETIC));
			hat3List.setModel(getNewModel(lists, EngiPanel.COSMETIC));
			
			if(index == Classes.Spy && buildingList.getItemAt(0) == null) {
				//only need to load once since there's only one possible building list
				buildingList.setModel(getNewModel(lists, EngiPanel.BUILDING));
				buildingList.setSelectedIndex(-1);
			}
		}
	
		//default to no selection since these aren't mandatory
		primaryList.setSelectedIndex(-1);
		secList.setSelectedIndex(-1);
		meleeList.setSelectedIndex(-1);
		hat1List.setSelectedIndex(-1);
		hat2List.setSelectedIndex(-1);
		hat3List.setSelectedIndex(-1);
	}
	
	//make new defaultcomboboxmodel from the sublist converted to string[] of sublist's size
	private DefaultComboBoxModel<String> getNewModel(List<List<String>> lists, int slot) {
		List<String> sublist = lists.get(slot);
		
		return new DefaultComboBoxModel<String>(sublist.toArray(new String[sublist.size()]));
	}
	
	public void updateTagList(List<String> list) { //add custom tags to list
		tagModel.clear();
		
		for(String s : tags) { //add the constant tags
			tagModel.addElement(s);
		}
		for(String s : list) { //then add the variable ones
			tagModel.addElement(s);
		}
		tagList.setModel(tagModel);
	}
	
	//no reason to hold this in memory since it gets put in attributeslist and is done with
	public String[] setAttributesList() {
		String[] attributes = {
				"RemoveOnDeath", "Aggressive",
				"SuppressFire", "DisableDodge",
				"BecomeSpectatorOnDeath",
				"RetainBuildings", "SpawnWithFullCharge",
				"AlwaysCrit", "IgnoreEnemies",
				"HoldFireUntilFullReload",
				"AlwaysFireWeapon", "MiniBoss",
				"UseBossHealthBar", "IgnoreFlag",
				"AutoJump", "AirChargeOnly",
				"VaccinatorBullets", "VaccinatorBlast",
				"VaccinatorFire", "BulletImmune",
				"BlastImmune", "FireImmune",
				"Parachute", "ProjectileShield"
		};
		//teleporttohint is not included here, manually add to mission
		
		return attributes;
	}
	
	//copy templatepanel's bot template model data to botpanel's template model
	//i hate models
	public void updateTemplateModel(DefaultComboBoxModel<String> model) {
		templateModel.removeAllElements();
		
		for(int i = 0; i < model.getSize(); i++) {
			templateModel.addElement(model.getElementAt(i));
		}

		templateBox.setSelectedIndex(-1); //default to no template
	}
	
	//add radio button to proper panels and the group, then init the listeners
	private void initAttributeRadio() {
		attrButPanel.add(noAttrBut);
		attrButPanel.add(hat1AttrBut);
		attrButPanel.add(hat2AttrBut);
		attrButPanel.add(hat3AttrBut);
		attrButPanel2.add(charAttrBut);
		attrButPanel2.add(priAttrBut);
		attrButPanel2.add(secAttrBut);
		attrButPanel2.add(melAttrBut);
		
		attrGroup.add(noAttrBut);
		attrGroup.add(charAttrBut);
		attrGroup.add(priAttrBut);
		attrGroup.add(secAttrBut);
		attrGroup.add(melAttrBut);
		attrGroup.add(buiAttrBut);
		attrGroup.add(hat1AttrBut);
		attrGroup.add(hat2AttrBut);
		attrGroup.add(hat3AttrBut);
		attrGroup.setSelected(noAttrBut.getModel(), true);
		
		noAttrBut.setActionCommand(ItemSlot.NONE.toString());
		priAttrBut.setActionCommand(ItemSlot.PRIMARY.toString());
		secAttrBut.setActionCommand(ItemSlot.SECONDARY.toString());
		melAttrBut.setActionCommand(ItemSlot.MELEE.toString());
		buiAttrBut.setActionCommand(ItemSlot.BUILDING.toString());
		charAttrBut.setActionCommand(ItemSlot.CHARACTER.toString());
		hat1AttrBut.setActionCommand(ItemSlot.HAT1.toString());
		hat2AttrBut.setActionCommand(ItemSlot.HAT2.toString());
		hat3AttrBut.setActionCommand(ItemSlot.HAT3.toString());
		
		setAttrButRed();
		
		//don't allow adding attributes with no hats set
		hat1AttrBut.setVisible(false);
		hat2AttrBut.setVisible(false);
		hat3AttrBut.setVisible(false);
		
		noAttrBut.addItemListener(event -> {
			if(event.getStateChange() == ItemEvent.SELECTED) {
				attrPanel.setVisible(false);
			}
			else {
				attrPanel.setVisible(true);
			}
		});
		priAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.PRIMARY);
		});
		secAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.SECONDARY);
		});
		melAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.MELEE);
		});
		melAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.MELEE);
		});
		buiAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.BUILDING);
		});
		charAttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.CHARACTER);
		});
		hat1AttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.HAT1);
		});
		hat2AttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.HAT2);
		});
		hat3AttrBut.addActionListener(event -> {
			attrPanel.loadMap(ItemSlot.HAT3);
		});
	}
	
	//colors all the attr radio buttons red
	public void setAttrButRed() {
		charAttrBut.setForeground(Color.RED);
		priAttrBut.setForeground(Color.RED);
		secAttrBut.setForeground(Color.RED);
		melAttrBut.setForeground(Color.RED);
		buiAttrBut.setForeground(Color.RED);
		hat1AttrBut.setForeground(Color.RED);
		hat2AttrBut.setForeground(Color.RED);
		hat3AttrBut.setForeground(Color.RED);
	}
	
	public void setAttrButGreen(ItemSlot k) {
		switch (k) {
			case PRIMARY:
				priAttrBut.setForeground(Color.GREEN);
				break;
			case SECONDARY:
				secAttrBut.setForeground(Color.GREEN);
				break;
			case MELEE:
				melAttrBut.setForeground(Color.GREEN);
				break;
			case BUILDING:
				buiAttrBut.setForeground(Color.GREEN);
				break;
			case CHARACTER:
				charAttrBut.setForeground(Color.GREEN);
				break;
			case HAT1:
				hat1AttrBut.setForeground(Color.GREEN);
				break;
			case HAT2:
				hat2AttrBut.setForeground(Color.GREEN);
				break;
			case HAT3:	
				hat3AttrBut.setForeground(Color.GREEN);
				break;
			case NONE: //none is never colored
			default:
				break;
		}
	}
	
	public AttributesPanel getAttributesPanel() {
		return this.attrPanel;
	}
	
	//get info changes from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(SecondaryWindow.TAGS)) {
			updateTagList((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
			tagList.setFixedCellWidth(-1);
		}
		else if(evt.getPropertyName().equals(MainWindow.ITEMPARSE)) {
			setItemParser((ItemParser) evt.getNewValue());
		}
		else if(evt.getPropertyName().equals(MainWindow.BOTTEMPLATELISTFIXED)) {
			if(evt.getNewValue() instanceof List<?>) {
				addPermanentTemplates((List<String>) evt.getNewValue());
			}
			
		}
	}
	
	private void addPermanentTemplates(List<String> list) {
		templateBox.setModel(new DefaultComboBoxModel<String>(list.toArray(new String[list.size()])));
		templateBox.setSelectedIndex(-1);
		templateBox.revalidate();
	}
	
	//class for the attribute panel
	public class AttributesPanel extends EngiPanel {
		final int ATTRMAX = 20;
		
		DefaultListModel<String> userAttrListModel = new DefaultListModel<String>();
		JList<String> attrSelectedList = new JList<String>(userAttrListModel); //fix list box
		JButton addAttributeToListButton = new JButton("Add attribute");
		JButton updateAttributeValueButton = new JButton("Update attribute value");
		JButton removeAttributeFromList = new JButton("Remove attribute");
		JTextField attrValueField = new JTextField(11);
		
		JComboBox<String> itemAttributeBox;
		//ItemAttributeNode currentAttributeNode;
		HashMap<String, String> addedAttributes = new HashMap<String, String>();
		TFBotNode tfbotnode;
		//Map<ItemSlot, ItemAttributeNode> nodeMap = new HashMap<ItemSlot, ItemAttributeNode>();
		
		//manage these two via outer frames
		JButton addAttrToBot = new JButton("Add current ItemAttributes type to bot");
		JButton removeAttrFromBot = new JButton("Remove current ItemAttribute type from bot");
		
		ButtonListManager attrBLManager = new ButtonListManager(addAttributeToListButton,
				updateAttributeValueButton, removeAttributeFromList);
		
		public AttributesPanel(String[] itemAttributes) {
			setLayout(gbLayout);
			gbConstraints.anchor = GridBagConstraints.WEST;
			
			itemAttributeBox = new JComboBox<String>(itemAttributes);
			itemAttributeBox.setEditable(true);
			initAttributeComponents();
			
			JScrollPane attrListPane = new JScrollPane(attrSelectedList);
			
			attrSelectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			itemAttributeBox.setMinimumSize(itemAttributeBox.getPreferredSize());
			attrListPane.setMinimumSize(attrSelectedList.getPreferredScrollableViewportSize());
			
			addGB(addAttributeToListButton, 0, 1);
			addGB(removeAttributeFromList, 1, 1);
			addGB(updateAttributeValueButton, 2, 1);
			addGB(attrValueField, 3, 1);
			
			gbConstraints.gridwidth = 2;
			addGB(addAttrToBot, 2, 5);
			addGB(removeAttrFromBot, 2, 6);
			addGB(itemAttributeBox, 0, 0);
			
			//gb.gridwidth = 2;
			gbConstraints.gridheight = 6;
			addGB(attrListPane, 0, 2);	
			
			setAttrToBotButtonsStates(false, false, States.DISABLE);
			//force no add until there's something
		}

		//init listeners
		private void initAttributeComponents() {
			itemAttributeBox.addItemListener(event -> {
				attrSelectedList.clearSelection();
			}); //if user selects a new attribute from dropdown, automatically clear selection to save a click
			
			attrSelectedList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) { //fetches the value and updates button state
					if(attrSelectedList.getSelectedIndex() != -1) {
						attrValueField.setText(addedAttributes.get(attrSelectedList.getSelectedValue()));
						attrBLManager.changeButtonState(ButtonListManager.States.SELECTED);
					}
					else {
						checkListSize();
					}
				}
			});
			
			addAttributeToListButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!userAttrListModel.contains((String) itemAttributeBox.getSelectedItem())) {
						userAttrListModel.addElement((String) itemAttributeBox.getSelectedItem());
					} //prevent duplicate entries in the visual list
					else {
						window.updateFeedback("Attribute already in list");
					}
					//addedAttributes.put((String) itemAttributeBox.getSelectedItem(), null);
					attrSelectedList.setSelectedValue((String) itemAttributeBox.getSelectedItem(), false);
					//explicitly show it is selected here, also selects if it was already entered
					
					attrBLManager.changeButtonState(States.SELECTED);
				}
			});
			
			updateAttributeValueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(attrValueField.getText().isEmpty()) {
						window.updateFeedback("No value to add to attribute");
					}
					else { //put attr - value pair in map
						addedAttributes.put((String) itemAttributeBox.getSelectedItem(), attrValueField.getText());
						window.updateFeedback("Attribute value added");
						addAttrToBot.setEnabled(true); //hit minimum valid attribute list at this point
					}
				}
			});
			
			//remove a specific attribute from the list
			removeAttributeFromList.addActionListener(event -> { 
				addedAttributes.remove((String) itemAttributeBox.getSelectedItem());
				//doesn't do anything if attr wasn't added to map
				userAttrListModel.removeElementAt(attrSelectedList.getSelectedIndex());	
				if(userAttrListModel.isEmpty()) {
					setAttrToBotButtonsStates(false, false, ButtonListManager.States.EMPTY);
					//don't allow adding empty itemattributes
				}
				else {
					attrBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
				}
			});
			
			//add the attribute map to the tfbot node
			addAttrToBot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					setAttrButGreen(selectedSlot);
					//get selected radio button and convert command back to itemslot
					//also update radio color
					
					List<HashMap<String, String>> list;
					
					if(tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES) == null) { //init map if there isn't one
						list = new ArrayList<HashMap<String, String>>(TFBotNode.ITEMCOUNT);
						tfbotnode.putKey(TFBotNode.ITEMATTRIBUTES, list);
					}
					else {
						list = (List<HashMap<String, String>>) tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES);
					}
					
					if(addedAttributes.isEmpty() || addedAttributes.containsValue(null) 
							|| addedAttributes.containsValue("")) {
						window.updateFeedback("Failed to add ItemAttributes list, an attribute is missing a value");
					}
					else {
						list.set(selectedSlot.getSlot(), addedAttributes);
						window.updateFeedback("ItemAttributes list added to TFBot");
						removeAttrFromBot.setEnabled(true);
					}
					//possibly warn of overwrite
				}
			});
			
			removeAttrFromBot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					//get selected radio button and convert command back to itemslot
					((List<HashMap<String, String>>) tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES)).remove(selectedSlot.getSlot());
					removeAttrFromBot.setEnabled(false);
					//update slot color
					window.updateFeedback("ItemAttributes list removed from TFBot");
					updateComponents(false); //refresh components/destroy the node
				}
			});
		}
		
		private void checkListSize() {
			
			if(userAttrListModel.getSize() == ATTRMAX) { //only allow as many attributes that can be fit
				setAttrToBotButtonsStates(true, false, States.REMOVEONLY);
			} 
			else if (tfbotnode == null || tfbotnode.getParent() == null) { //even if no visible ws there is invis temp ws
				setAttrToBotButtonsStates(false, false, States.DISABLE);
			} //different from above since you want to be able to add if parent and empty
			else if(userAttrListModel.getSize() == 0) { //if nothing in list
				setAttrToBotButtonsStates(false, false, States.EMPTY);
			} 
			else { 
				setAttrToBotButtonsStates(true, false, States.EMPTY);
			}
			
			if(tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES) != null && 
					((List<HashMap<String, String>>) tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES)).contains(addedAttributes)) {
				removeAttrFromBot.setEnabled(true); //enable remove only if already added to bot
			}
		}
		
		public void setAttrToBotButtonsStates(boolean botState, boolean canRemove, ButtonListManager.States state) { //awful name
			addAttrToBot.setEnabled(botState);
			removeAttrFromBot.setEnabled(canRemove);
			
			attrBLManager.changeButtonState(state);
		}
		
		//take node and load the appropriate map for the item slot
		private void loadMap(ItemSlot index) {
			
			if(tfbotnode != null) {
				List<HashMap<String, String>> list = (List<HashMap<String, String>>) tfbotnode.getValueSingular(TFBotNode.ITEMATTRIBUTES);
				if(list != null && !list.isEmpty()) { //if map is not empty/null
					if(list.get(index.getSlot()) != null) { //set addedattributes map to map defined at corresponding index in list
						addedAttributes = list.get(index.getSlot());
						updateComponents(true);
					}	
					else {
						updateComponents(false);
					}
				}
				else {
					updateComponents(false);
				}
			}	
			else { //otherwise just clear components
				updateComponents(false);
			}
			
		}
		
		//load the map into model
		private void updateComponents(boolean loadList) {
			attrValueField.setText(null);
			if(loadList) { //if there is an existing list
				userAttrListModel.clear(); 
				addedAttributes.forEach((k, v) -> userAttrListModel.addElement(k + " " + v));
				checkListSize(); //force check in case list index never changes (stays at unselected)
			}
			else {
				addedAttributes = new HashMap<String, String>();
				userAttrListModel.clear(); //do it after updating addedAttributes so checklistsize can't false positive
			}
			
		}
		
		//gets node from window and updates the radio colors 
		public void updateItemAttrInfo(TFBotNode node) {
			this.tfbotnode = node;
			List<HashMap<String, String>> list = (List<HashMap<String, String>>) node.getValueSingular(TFBotNode.ITEMATTRIBUTES);
			
			updateComponents(false);
			setAttrButRed();
			if(node.getParent() != null) {
				if(list != null) { //update radio colors based on what attrs it has				
					list.forEach(k -> {
						//setAttrButGreen(list.indexOf(k));
					});
				}
			}
			noAttrBut.setSelected(true);
			if(node.getValueSingular(TFBotNode.ITEMATTRIBUTES) == null) { //if no attribute list to add to
				setAttrToBotButtonsStates(false, false, ButtonListManager.States.EMPTY);
			}
			else {
				setAttrToBotButtonsStates(true, false, ButtonListManager.States.EMPTY);
			}
		}
	}
}
