package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;

import engipop.ButtonListManager.States;
import engipop.Tree.TFBotNode;
import engipop.Tree.TFBotNode.*;
//import engipop.window;

//todo: add some sort of sanity checking for itemattributes
public class BotPanel extends EngiPanel { //class to make the panel for bot creation/editing
	
	String[] tags = {"bot_giant", "bot_squad_member"}; //potentially move bot_giant
	String[] attr = {"todo: big attr list goes here"};
	
	window window;
	AttributesPanel attrPanel;
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel = new DefaultComboBoxModel<String>();
	DefaultListModel<String> tagModel = new DefaultListModel<String>();
	
	JTextField nameField = new JTextField(30); //max bot name is ~32
	JComboBox<Classes> classBox = new JComboBox<Classes>(Classes.values());
	JComboBox<String> iconBox = new JComboBox<String>(iconModel);
	JList<String> tagList = new JList<String>(tags);
	JList<String> attrList = new JList<String>(attr);
	
	List<JComboBox<String>> itemLists = new ArrayList<JComboBox<String>>();
	
	JComboBox<String> primaryList = new JComboBox<String>();
	JComboBox<String> secList = new JComboBox<String>();
	JComboBox<String> meleeList = new JComboBox<String>();
	JComboBox<String> buildingList = new JComboBox<String>();
	JComboBox<String> hat1List = new JComboBox<String>();
	JComboBox<String> hat2List = new JComboBox<String>();
	JComboBox<String> hat3List = new JComboBox<String>();
	
	JComboBox<String> itemAttributesListBox;
	
	ItemParser parser;
	
	JLabel botBuilding = new JLabel("Sapper: ");
	
	ButtonGroup wepGroup = new ButtonGroup();;
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
	
	public BotPanel(window window) {
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		
		//this.parser = parser;
		
		this.window = window;
		
		attrPanel = new AttributesPanel(ItemAttributes.getItemAttributes());
		attrPanel.setVisible(false);
		
		initItemLists();
		initAttributeRadio();
		
		iconBox.setPrototypeDisplayValue("heavyweapons_healonkill_giant");
		
		//setIconBox(iconBox, iconModel, "Scout");
		
		classBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				Classes str = (Classes) classBox.getSelectedItem();
				setIconBox(iconBox, iconModel, str);
				if(parser != null) { //prevent loading in cases where items_game.txt is unknown
					setClassItems(classBox.getSelectedIndex());
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
	
		//shrinks down the attribute radio button panels so they fit beter
		((FlowLayout) attrButPanel2.getLayout()).setVgap(0);
		((FlowLayout) attrButPanel.getLayout()).setVgap(0);
		attrButPanel.setPreferredSize(new Dimension(529, 20));
		attrButPanel2.setPreferredSize(new Dimension(557, 20));
		
		addGB(botClass, 0, 0);
		addGB(new JScrollPane(classBox), 1, 0); //fix size
		addGB(botIcon, 2, 0);
		addGB(new JScrollPane(iconBox), 3, 0); //fix size so it doesn't change for long lists
		
		addGB(botName, 0, 1);
		addGB(nameField, 1, 1); 
		
		addGB(botSkill, 0, 3);
		addGB(skillPanel, 1, 3);
		addGB(botRestrict, 2, 3);
		addGB(wepPanel, 3, 3);
		addGB(botTags, 0, 5);
		addGB(new JScrollPane(tagList), 1, 5);
		
		addGB(botAttributes, 2, 5);
		addGB(attrList, 3, 5);
		
		
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
		
		addGB(buiAttrBut, 2, 9);
		
		gb.gridwidth = 2;
		addGB(attrButPanel, 2, 7);
		addGB(attrButPanel2, 2, 8);
		
		gb.gridheight = 8;
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
		
		classBox.setSelectedItem(tf.getValue(TFBotKeys.CLASSNAME));
		setIconBox(iconBox, iconModel, (Classes) tf.getValue(TFBotKeys.CLASSNAME));
		//values of classname are classes from enum
		iconBox.setSelectedItem(tf.getValue(TFBotKeys.CLASSICON));
		nameField.setText((String) tf.getValue(TFBotKeys.NAME));
		switch ((String) tf.getValue(TFBotKeys.SKILL)) {
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
		switch ((String) tf.getValue(TFBotKeys.WEAPONRESTRICT)) {
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
		List<String> tags = tf.getTags();
		if(tags.size() > 0) {
			for(int i = 0; i < tags.size(); i++) { //get indices so they can be selected
				indices[i] = tagList.getNextMatch(tags.get(i), 0, Position.Bias.Forward);
			}
			tagList.setSelectedIndices(indices);
		}
		else {
			tagList.clearSelection();
		}
		primaryList.setSelectedItem((String) tf.getValue(TFBotKeys.PRIMARY));
		secList.setSelectedItem((String) tf.getValue(TFBotKeys.SECONDARY));
		meleeList.setSelectedItem((String) tf.getValue(TFBotKeys.MELEE));
		if(tf.getValue(TFBotKeys.CLASSNAME) == EngiPanel.Classes.Spy) {
			buildingList.setSelectedItem((String) tf.getValue(TFBotKeys.BUILDING));
		}
		hat1List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT1));
		hat2List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT2));
		hat3List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT3));
		
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT1), hat1AttrBut);
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT2), hat2AttrBut);
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT3), hat3AttrBut);
	}
	
	public void updateNode(TFBotNode tf) { //put values into node
		tf.putKey(TFBotKeys.CLASSNAME, classBox.getSelectedItem());
		tf.putKey(TFBotKeys.CLASSICON, iconBox.getSelectedItem()); //string
		tf.putKey(TFBotKeys.NAME, nameField.getText());
		tf.putKey(TFBotKeys.SKILL, skillGroup.getSelection().getActionCommand());
		tf.putKey(TFBotKeys.WEAPONRESTRICT, wepGroup.getSelection().getActionCommand());
		if(!tagList.getSelectedValuesList().isEmpty()) { //probably some other funny conversion bugs, double check
			tf.setTags(tagList.getSelectedValuesList());
		}
		tf.putKey(TFBotKeys.PRIMARY, primaryList.getSelectedItem()); //strings
		tf.putKey(TFBotKeys.SECONDARY, secList.getSelectedItem());
		tf.putKey(TFBotKeys.MELEE, meleeList.getSelectedItem());
		if(classBox.getSelectedItem() == EngiPanel.Classes.Spy) {
			tf.putKey(TFBotKeys.BUILDING, buildingList.getSelectedItem());
		}
		tf.putKey(TFBotKeys.HAT1, hat1List.getSelectedItem());
		tf.putKey(TFBotKeys.HAT2, hat2List.getSelectedItem());
		tf.putKey(TFBotKeys.HAT3, hat3List.getSelectedItem());
		//item attributes are added separately
		
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT1), hat1AttrBut);
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT2), hat2AttrBut);
		setHatVisibility((String) tf.getValue(TFBotKeys.HAT3), hat3AttrBut);
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
		}
	}
	
	//update item lists
	public void getItemParser(ItemParser parser) {
		this.parser = parser;
		setClassItems(0); //since force updating, just default to first class
	}
	
	public void setClassItems(int index) { //take class and get items from the parser
		primaryList.removeAllItems();
		secList.removeAllItems();
		meleeList.removeAllItems();
		hat1List.removeAllItems();
		hat2List.removeAllItems();
		hat3List.removeAllItems();
			
		List<List<String>> lists = parser.updateModels(index);
		
		//set model(get the appropriate slot from lists, convert to a new string array of size inner list)
		primaryList.setModel(getNewModel(lists, ItemParser.primary));
		secList.setModel(getNewModel(lists, ItemParser.secondary));
		meleeList.setModel(getNewModel(lists, ItemParser.melee));
		
		hat1List.setModel(getNewModel(lists, ItemParser.cosmetic));
		hat2List.setModel(getNewModel(lists, ItemParser.cosmetic));
		hat3List.setModel(getNewModel(lists, ItemParser.cosmetic));
		
		if(index == 8 && buildingList.getItemAt(0) == null) {
			//only need to load once since there's only one possible building list
			buildingList.setModel(getNewModel(lists, ItemParser.building));
			buildingList.setSelectedIndex(-1);
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
	
	//externally changing between values
	//public void setAttrNone() {
	//	noAttrBut.setSelected(true);
	//	//setAttrButRed();
	//}
	
	//class for the attribute panel
	@SuppressWarnings("serial")
	public class AttributesPanel extends EngiPanel {
		final int ATTRMAX = 2;
		
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
		
		ButtonListManager attrBLManager = new ButtonListManager(attrSelectedList, addAttributeToListButton,
				updateAttributeValueButton, removeAttributeFromList);
		
		public AttributesPanel(String[] itemAttributes) {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.WEST;
			
			itemAttributeBox = new JComboBox<String>(itemAttributes);
			itemAttributeBox.setEditable(true);
			initAttributeComponents();
			
			attrSelectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			addGB(addAttributeToListButton, 0, 1);
			addGB(removeAttributeFromList, 1, 1);
			addGB(updateAttributeValueButton, 2, 1);
			addGB(attrValueField, 3, 1);
			
			addGB(addAttrToBot, 3, 5);
			addGB(removeAttrFromBot, 3, 6);
			
			gb.gridwidth = 2;
			addGB(new JScrollPane(itemAttributeBox), 0, 0);
			
			gb.gridwidth = 2;
			gb.gridheight = 6;
			addGB(new JScrollPane(attrSelectedList), 0, 2);	
			
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
					if(tfbotnode.getItemAttributeList() == null) { //init map if there isn't one
						tfbotnode.setItemAttributeList(new HashMap<EngiPanel.ItemSlot, HashMap<String, String>>());
					}
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					setAttrButGreen(selectedSlot);
					//get selected radio button and convert command back to itemslot
					//also update radio color
					
					if(addedAttributes.isEmpty() || addedAttributes.containsValue(null) 
							|| addedAttributes.containsValue("")) {
						window.updateFeedback("Failed to add ItemAttributes list, an attribute is missing a value");
					}
					else {
						//currentAttributeNode = new ItemAttributeNode(addedAttributes);
						tfbotnode.getItemAttributeList().put(selectedSlot, addedAttributes);
						window.updateFeedback("ItemAttributes list added to TFBot");
						removeAttrFromBot.setEnabled(true);
					}
					//add type and the node to tfbot's itemattributes map
					//possibly warn of overwrite
				}
			});
			
			removeAttrFromBot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					//get selected radio button and convert command back to itemslot
					tfbotnode.getItemAttributeList().remove(selectedSlot);
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
			
			if(tfbotnode.getItemAttributeList() != null && tfbotnode.getItemAttributeList().containsValue(addedAttributes)) {
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
			//node = window.getCurrentBotNode();
			
			if(tfbotnode != null) {
				Map<ItemSlot, HashMap<String, String>> nodeMap = tfbotnode.getItemAttributeList();
				//node.getParent() != null && 
				if(nodeMap != null && !nodeMap.isEmpty()) { //if map is not empty/null
					if(nodeMap.containsKey(index)) { //set addedattributes map to map defined at corresponding key in nodemap
						//currentAttributeNode = nodeMap.get(index);
						addedAttributes = nodeMap.get(index);
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
			
			updateComponents(false);
			setAttrButRed();
			if(node.getParent() != null) {
				if(node.getItemAttributeList() != null) { //update radio colors based on what attrs it has
					node.getItemAttributeList().keySet().forEach(k -> {
						setAttrButGreen(k);
					});
				}
			}
			noAttrBut.setSelected(true);
			if(node.getItemAttributeList() == null) { //if no attribute list to add to
				setAttrToBotButtonsStates(false, false, ButtonListManager.States.EMPTY);
			}
			else {
				setAttrToBotButtonsStates(true, false, ButtonListManager.States.EMPTY);
			}
		}
	}
}
