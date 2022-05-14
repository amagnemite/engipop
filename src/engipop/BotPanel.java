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

import engipop.Tree.ItemAttributeNode;
import engipop.Tree.TFBotNode;
import engipop.Tree.TFBotNode.*;
//import engipop.window;

//todo: add some sort of sanity checking for itemattributes
public class BotPanel extends EngiPanel { //class to make the panel for bot creation/editing
	
	String[] tags = {"bot_giant", "bot_squad_member"}; //potentially move bot_giant
	String[] attr = {"todo: big attr list goes here"};
	
	window window; 
	JPanel attributePanel = new JPanel();
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel;
	DefaultListModel<String> tagModel = new DefaultListModel<String>();
	
	JTextField nameField = new JTextField(30); //max bot name is ~32
	JComboBox<Classes> classBox = new JComboBox<Classes>(Classes.values());
	JComboBox<String> iconBox = new JComboBox<String>();
	JList<String> tagList;
	JList<String> attrList;
	
	//DefaultComboBoxModel<String> primaryModel = new DefaultComboBoxModel<String>();
	
	List<JComboBox<String>> itemLists = new ArrayList<JComboBox<String>>();
	
	JComboBox<String> primaryList = new JComboBox<String>();
	JComboBox<String> secList = new JComboBox<String>();
	JComboBox<String> meleeList = new JComboBox<String>();
	JComboBox<String> buildingList = new JComboBox<String>();
	JComboBox<String> hat1List = new JComboBox<String>();
	JComboBox<String> hat2List = new JComboBox<String>();
	JComboBox<String> hat3List = new JComboBox<String>();
	
	JComboBox<String> primaryAttrList;
	JList<String> secAttrList;
	JList<String> meleeAttrList;
	JList<String> charAttrList;
	
	ItemParser parser;
	String[] itemAttributes;
	
	JLabel botBuilding = new JLabel("Sapper: ");
	
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
	
	AttributesPanel attrPanel;
	
	public BotPanel(window window) {
		GridBagLayout gbLayout = new GridBagLayout();
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		
		//this.parser = parser;
		ItemAttributes ia = new ItemAttributes();
		this.window = window;
		/*itemAttributes = ;
		
		primaryAttrList = new JComboBox<String>(itemAttributes);
		primaryAttrList.setEditable(true); */
		
		attrPanel = new AttributesPanel(ia.getItemAttributes());
		attrPanel.setVisible(false);
		
		initItemLists();
		initAttributeRadio();
		
		iconModel = (DefaultComboBoxModel<String>) iconBox.getModel();
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
		
		tagList = new JList<String>(tags);
		attrList = new JList<String>(attr);
		
		//primaryList = new JComboBox<String>(primaries);
		//secList = new JComboBox<String>(secs);
		//meleeList = new JComboBox<String>(melees);
		
		
		
		//secAttrList = new JList<String>(itemAttr);
		//meleeAttrList = new JList<String>(itemAttr);
		//charAttrList = new JList<String>(charAttr);

		//skill level radio buttons
		JPanel skillPanel = new JPanel();
		skillGroup = new ButtonGroup();
		skillPanel.add(easyBut = new JRadioButton(TFBotNode.EASY));
		easyBut.setActionCommand(TFBotNode.EASY);
		skillGroup.add(easyBut);
		skillPanel.add(normalBut = new JRadioButton(TFBotNode.NORMAL));
		normalBut.setActionCommand(TFBotNode.NORMAL);
		skillGroup.add(normalBut);
		skillPanel.add(hardBut = new JRadioButton(TFBotNode.HARD));
		hardBut.setActionCommand(TFBotNode.HARD);
		skillGroup.add(hardBut);
		skillPanel.add(expBut = new JRadioButton(TFBotNode.EXPERT));
		expBut.setActionCommand(TFBotNode.EXPERT);
		skillGroup.add(expBut);
		skillGroup.setSelected(easyBut.getModel(), true);
		
		//wep restrict radio buttons
		JPanel wepPanel = new JPanel();
		wepGroup = new ButtonGroup();
		//JRadioButton wepRadio;
		wepPanel.add(anyBut = new JRadioButton(TFBotNode.ANY));
		anyBut.setActionCommand(TFBotNode.ANY);
		wepGroup.add(anyBut);
		wepPanel.add(priBut = new JRadioButton(TFBotNode.PRIMARYONLY));
		priBut.setActionCommand(TFBotNode.PRIMARYONLY);
		wepGroup.add(priBut);
		wepPanel.add(secBut = new JRadioButton(TFBotNode.SECONDARYONLY));
		secBut.setActionCommand(TFBotNode.SECONDARYONLY);
		wepGroup.add(secBut);		
		wepPanel.add(melBut = new JRadioButton(TFBotNode.MELEEONLY));
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
		JLabel primaryAttributeLabel = new JLabel("ItemAttributes: ");
		JLabel botSecondary = new JLabel("Secondary weapon: ");
		JLabel botSecAttr = new JLabel("ItemAttributes: ");
		JLabel botMelee = new JLabel("Melee weapon: ");
		JLabel botMelAttr = new JLabel("ItemAttributes: ");
		JLabel botCharAttr = new JLabel("CharacterAttributes: ");
		JLabel botHat = new JLabel("Hats: ");
	
		//shrinks down the attribute radio button panels so they fit in nice
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
		
	//	addGB(botCharAttr, 0, 7);
	//	addGB(charAttrList, 1, 7);
		
		
		addGB(botPrimary, 0, 8);
		addGB(primaryList, 1, 8);
		addGB(botSecondary, 0, 9);
		addGB(secList, 1, 9);
		//addGB(botSecAttr, 2, 9);
		//addGB(secAttrList, 3, 9);		
		addGB(botMelee, 0, 10);
		addGB(meleeList, 1, 10);
		//addGB(botMelAttr, 2, 10);
		//addGB(meleeAttrList, 3, 10);	
		
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
				indices[i] = (tagList.getNextMatch(tags.get(i), 0, Position.Bias.Forward));
			}
			tagList.setSelectedIndices(indices);
		}
		primaryList.setSelectedItem((String) tf.getValue(TFBotKeys.PRIMARY));
		secList.setSelectedItem((String) tf.getValue(TFBotKeys.SECONDARY));
		meleeList.setSelectedItem((String) tf.getValue(TFBotKeys.MELEE));
		hat1List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT1));
		hat2List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT2));
		hat3List.setSelectedItem((String) tf.getValue(TFBotKeys.HAT3));
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
		tf.putKey(TFBotKeys.HAT1, hat1List.getSelectedItem());
		tf.putKey(TFBotKeys.HAT2, hat2List.getSelectedItem());
		tf.putKey(TFBotKeys.HAT3, hat3List.getSelectedItem());
		//item attributes are added separately
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
		//jb.setModel(model);
	}
	
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
		
		/*
		for(int i = 0; i < lists.size(); i++) {
			if(i < ItemParser.cosmetic) {
				itemLists.get(i).setModel(new DefaultComboBoxModel<String>(lists.get(i)
						.toArray(new String[lists.get(i).size()])));
			}
			else if(i == ItemParser.cosmetic) {
				itemLists.get(i).setModel(new DefaultComboBoxModel<String>(lists.get(i)
						.toArray(new String[lists.get(i).size()])));
				itemLists.get(i + 1).setModel(new DefaultComboBoxModel<String>(lists.get(i)
						.toArray(new String[lists.get(i).size()])));
				itemLists.get(i + 2).setModel(new DefaultComboBoxModel<String>(lists.get(i)
						.toArray(new String[lists.get(i).size()])));
			}
			else {
				itemLists.get(i).setModel(new DefaultComboBoxModel<String>(lists.get(i)
						.toArray(new String[lists.get(i).size()])));
			}
			
		} */
		
		//set model(get the appropriate slot from lists, convert to a new string array of size inner list)
		primaryList.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.primary)
				.toArray(new String[lists.get(ItemParser.primary).size()])));
		secList.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.secondary)
				.toArray(new String[lists.get(ItemParser.secondary).size()])));
		meleeList.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.melee)
				.toArray(new String[lists.get(ItemParser.melee).size()])));
		
		hat1List.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.cosmetic)
				.toArray(new String[lists.get(ItemParser.cosmetic).size()])));
		hat2List.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.cosmetic)
				.toArray(new String[lists.get(ItemParser.cosmetic).size()])));
		hat3List.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.cosmetic)
				.toArray(new String[lists.get(ItemParser.cosmetic).size()])));
		
		if(index == 8 && buildingList.getItemAt(0) == null) {
			//only need to load once since there's only one possible building list
			buildingList.setModel(new DefaultComboBoxModel<String>(lists.get(ItemParser.building)
					.toArray(new String[lists.get(ItemParser.building).size()])));
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
		//attrButPanel.add(buiAttrBut);
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
	public void setAttrNone() {
		noAttrBut.setSelected(true);
		//setAttrButRed();
	}
	
	//class for the attribute panel
	public class AttributesPanel extends EngiPanel {
		final int ATTRMAX = 2;
		
		DefaultListModel<String> userAttrListModel = new DefaultListModel<String>();
		JList<String> attrSelectedList = new JList<String>(userAttrListModel);
		JButton addAttributeToListButton = new JButton("Add attribute");
		JButton updateAttributeValueButton = new JButton("Update attribute value");
		JButton removeAttributeButton = new JButton("Remove attribute");
		JTextField attrValueField = new JTextField(11);
		
		JComboBox<String> itemAttributeBox;
		//ItemAttributeNode currentAttributeNode;
		Map<String, String> addedAttributes = new HashMap<String, String>();
		TFBotNode node;
		Map<ItemSlot, ItemAttributeNode> nodeMap = new HashMap<ItemSlot, ItemAttributeNode>();
		
		//manage these two via outer frames
		JButton addAttrToBot = new JButton("Add current ItemAttributes type to bot");
		JButton removeAttrFromBot = new JButton("Remove current ItemAttribute type from bot");
		
		ButtonListManager attrBLManager = new ButtonListManager(attrSelectedList, addAttributeToListButton,
				updateAttributeValueButton, removeAttributeButton);
		
		public AttributesPanel(String[] itemAttributes) {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.WEST;
			
			itemAttributeBox = new JComboBox<String>(itemAttributes);
			itemAttributeBox.setEditable(true);
			initAttributeComponents();
			
			attrSelectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			addGB(addAttributeToListButton, 0, 1);
			addGB(removeAttributeButton, 1, 1);
			addGB(updateAttributeValueButton, 2, 1);
			addGB(attrValueField, 3, 1);
			
			addGB(addAttrToBot, 3, 5);
			addGB(removeAttrFromBot, 3, 6);
			
			gb.gridwidth = 2;
			addGB(new JScrollPane(itemAttributeBox), 0, 0);
			
			gb.gridwidth = 2;
			gb.gridheight = 6;
			addGB(new JScrollPane(attrSelectedList), 0, 2);	
			
			attrBLManager.changeButtonState(ButtonListManager.States.DISABLE);
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
					
					attrBLManager.changeButtonState(ButtonListManager.States.SELECTED);
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
						//maybe update list here
					}
				}
			});
			
			//remove a specific attribute from the list
			removeAttributeButton.addActionListener(event -> { 
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
					if(node.getItemAttributeList() == null) { //init map if there isn't one
						node.setItemAttributeList(new HashMap<EngiPanel.ItemSlot, ItemAttributeNode>());
					}
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					setAttrButGreen(selectedSlot);
					//get selected radio button and convert command back to itemslot
					//also update radio color
					
					node.getItemAttributeList().put(selectedSlot, new ItemAttributeNode(addedAttributes));
					window.updateFeedback("ItemAttributes list added to TFBot");
					removeAttrFromBot.setEnabled(true);
					//add type and the node to tfbot's itemattributes map
					//possibly warn of overwrite
				}
			});
			
			removeAttrFromBot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ItemSlot selectedSlot = ItemSlot.valueOf(attrGroup.getSelection().getActionCommand());
					//get selected radio button and convert command back to itemslot
					node.getItemAttributeList().remove(selectedSlot);
					removeAttrFromBot.setEnabled(false);
					//update slot color
					window.updateFeedback("ItemAttributes list removed from TFBot");
					//possibly catch error if removed with no existing list
					updateComponents(false); //refresh components/destroy the node
				}
			});
		}
		
		private void checkListSize() {
			if(userAttrListModel.getSize() == ATTRMAX) {
				attrBLManager.changeButtonState(ButtonListManager.States.REMOVEONLY);
			}
			else {
				attrBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
			}
			//only allow as many attributes that can be fit
		}
		
		public void setAttrToBotButtonsStates(boolean botState, boolean canRemove, ButtonListManager.States state) { //awful name
			addAttrToBot.setEnabled(botState);
			removeAttrFromBot.setEnabled(canRemove);
			
			attrBLManager.changeButtonState(state);
		}
		
		//take node and load the appropriate map for the item slot
		private void loadMap(ItemSlot index) {
			//node = window.getCurrentBotNode();
			
			if(node != null) {
				Map<ItemSlot, ItemAttributeNode> nodeMap = node.getItemAttributeList();
				//node.getParent() != null && 
				if(nodeMap != null && !nodeMap.isEmpty()) { 
					//if map is not empty/null
					if(nodeMap.containsKey(index)) { //set addedattributes map to map defined at corresponding key in nodemap
						addedAttributes = nodeMap.get(index).getMap();
						updateComponents(true);
					}	
					else {
						updateComponents(false);
					}
				}
			}	
			else { //otherwise just clear components
				updateComponents(false);
			}
		}
		
		//gets node from window and updates the radio colors 
		public void updateItemAttrInfo(TFBotNode node) {
			this.node = node;
			
			updateComponents(false);
			setAttrButRed();
			if(node.getParent() != null) {
				if(node.getItemAttributeList() != null) { //update radio colors based on what attrs it has
					node.getItemAttributeList().keySet().forEach(k -> {
						setAttrButGreen(k);
					});
				}
			}
			else {
				attrBLManager.changeButtonState(ButtonListManager.States.DISABLE);
			}
		}
		
		//load the map into model
		private void updateComponents(boolean loadList) {
			userAttrListModel.clear();
			attrValueField.setText(null);
			if(loadList) {
				addedAttributes.forEach((k, v) -> userAttrListModel.addElement(k + " " + v));
				checkListSize();
			}
			else {
				addedAttributes = new HashMap<String, String>();
			}
		}
	}
}
