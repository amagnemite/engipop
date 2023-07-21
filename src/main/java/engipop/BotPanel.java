package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Position;

import engipop.ButtonListManager.States;
import engipop.Node.TFBotNode;

//todo: add some sort of sanity checking for itemattributes
@SuppressWarnings("serial")
public class BotPanel extends EngiPanel implements PropertyChangeListener { //class to make the panel for bot creation/editing
	private final static int ATTRMAX = 20; //apparently the game stops parsing attributes after the 20th
	
	String[] tags = {"bot_giant", "bot_squad_member"}; //potentially move bot_giant
	
	EngiWindow containingWindow;
	EngiPanel attrPanel = new EngiPanel();
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel = new DefaultComboBoxModel<String>();
	//DefaultListModel<String> tagModel = new DefaultListModel<String>();
	DefaultTableModel tagModel = new DefaultTableModel(0, 1);
	DefaultComboBoxModel<String> templateModel = new DefaultComboBoxModel<String>();
	//ComboBoxModel<String> templateModel; //double check if access to model is actually needed
	
	JTextField nameField = new JTextField(30); //max bot name is ~32
	JTextField tagField = new JTextField(5);
	JComboBox<Classes> classBox = new JComboBox<Classes>(Classes.values());
	JComboBox<String> iconBox = new JComboBox<String>(iconModel);
	JComboBox<String> templateBox = new JComboBox<String>(templateModel); 
	JTable tagList = new JTable(tagModel);
	JList<String> botAttributeList;
	
	List<JComboBox<String>> itemLists = new ArrayList<JComboBox<String>>();
	
	JComboBox<String> primaryList = new JComboBox<String>();
	JComboBox<String> secList = new JComboBox<String>();
	JComboBox<String> meleeList = new JComboBox<String>();
	JComboBox<String> buildingList = new JComboBox<String>();
	JComboBox<String> hat1List = new JComboBox<String>();
	JComboBox<String> hat2List = new JComboBox<String>();
	JComboBox<String> hat3List = new JComboBox<String>();
	
	JComboBox<String> itemAttributesListBox;
	
	static ItemParser parser; //make sure all botpanels have same list
	
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
	
	DefaultComboBoxModel<ItemSlot> attrComboModel = new DefaultComboBoxModel<ItemSlot>();
	JComboBox<ItemSlot> attributesBox = new JComboBox<ItemSlot>(attrComboModel);
	
	DefaultListModel<String> itemAttrListModel = new DefaultListModel<String>();
	JList<String> itemAttrList = new JList<String>(itemAttrListModel); //fix list box
	JTextField attrValueField = new JTextField(11);
	JButton addAttributeToListButton = new JButton("Add attribute");
	JButton updateAttributeValueButton = new JButton("Update attribute value");
	JButton removeAttributeFromList = new JButton("Remove attribute");
	
	JComboBox<String> itemAttributeBox; //make this a jtree
	Object[] attributeMapsArray = new Object[TFBotNode.ITEMCOUNT]; //contains all item attribute maps
	Map<String, String> currentAttributeMap = new HashMap<String, String>();
	Map<String, String> currentCharAttributeMap = new HashMap<String, String>();
	
	ButtonListManager attrBLManager = new ButtonListManager(addAttributeToListButton,
			updateAttributeValueButton, removeAttributeFromList);
	
	public BotPanel(EngiWindow containingWindow, MainWindow mainWindow, SecondaryWindow secondaryWindow) {
		//window to send feedback to, mainwindow to get item updates, secondarywindow to get map updates
		JTextField cellEditor = new JTextField();
		
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		
		this.containingWindow = containingWindow;
		secondaryWindow.addPropertyChangeListener(this);
		mainWindow.addPropertyChangeListener(this);
		
		itemAttributeBox = new JComboBox<String>(new ItemAttributes().getItemAttributes());
		attrPanel.setVisible(false);
		
		initItemLists();
		initAttributeRadio();
		initAttributePanel();
		
		botAttributeList = new JList<String>(setAttributesList());
		
		iconBox.setPrototypeDisplayValue("heavyweapons_healonkill_giant");
		templateBox.setPrototypeDisplayValue("Giant Rapid Fire Demo Chief (T_TFBot_Giant_Demo_Spammer_Reload_Chief)");
		//tagList.setPrototypeCellValue("bot_squad_member");
		botAttributeList.setPrototypeCellValue("BecomeSpectatorOnDeath");
		
		//setIconBox(iconBox, iconModel, "Scout");
		
		tagModel.addRow(new String[] {"+"});
		tagList.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cellEditor));
		
		classBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				Classes str = (Classes) classBox.getSelectedItem();
				if(str != null) {
					setIconBox(iconModel, str);
				}
				if(parser != null) { //prevent loading in cases where items_game.txt is unknown
					setClassItems((Classes) classBox.getSelectedItem());
				}		
							
				if(classBox.getSelectedItem() == Classes.Spy) {
					buildingState(true);
				}
				else {
					buildingState(false);
				}
			}
		});
		hat1List.addItemListener(event -> { //p sure this isn't correct either
			if(hat1List.getSelectedItem() == null || ((String) hat1List.getSelectedItem()).isEmpty()) {
				attrComboModel.removeElement(ItemSlot.COSMETIC1);
				attributesBox.setSelectedIndex(0);
			}
			else {
				if(attrComboModel.getIndexOf(ItemSlot.COSMETIC1) != ItemSlot.COSMETIC1.getSlot() + 1) { //if not already in list
					attrComboModel.insertElementAt(ItemSlot.COSMETIC1, ItemSlot.COSMETIC1.getSlot() + 1);
				}
			}
		});
		
		cellEditor.addKeyListener(new KeyListener() { //todo: cell editor
			public void keyTyped(KeyEvent k) {
			}

			public void keyReleased(KeyEvent k) {
			}

			public void keyPressed(KeyEvent k) {
				int currentRow = tagList.getEditingRow();
				
				if(currentRow == tagList.getRowCount() - 1) { //editing the last row, add a new one
					tagModel.addRow(new String[] {"+"});
				}
				else {
					if(cellEditor.getText().isBlank() && k.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						tagModel.removeRow(currentRow);
						
						//if(tagList.getRowCount() == 0) {
						//	tagModel.addRow(new String[] {"+"});
						//}
					}
				}
			}
		});
		
		classBox.setSelectedIndex(0);
		tagList.setTableHeader(null);
		
		iconBox.setEditable(true);
		primaryList.setEditable(true);
		secList.setEditable(true);
		meleeList.setEditable(true);
		buildingList.setEditable(true);
		hat1List.setEditable(true);
		hat2List.setEditable(true);
		hat3List.setEditable(true);
		templateBox.setEditable(true);
		
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
		JLabel templateLabel = new JLabel("Template: ");
		JLabel attributesLabel = new JLabel("Attributes: "); //rename this
	
		JScrollPane tagListPane = new JScrollPane(tagList);
		JScrollPane attributesListPane = new JScrollPane(botAttributeList);
		
		//prevents dumb resizing stuff with the attr panel
		//may fix later
		nameField.setMinimumSize(nameField.getPreferredSize());
		classBox.setMinimumSize(classBox.getPreferredSize());
		iconBox.setMinimumSize(iconBox.getPreferredSize()); //double check this once custom input happens
		//tagListPane.setMinimumSize(tagList.getPreferredScrollableViewportSize());
		tagListPane.setMinimumSize(new Dimension(200, 100));
		attributesListPane.setMinimumSize(botAttributeList.getPreferredScrollableViewportSize());
		
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
		
		addGB(attributesLabel, 2, 7);
		addGB(attributesBox, 3, 7);
		
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridwidth = 2;
		gbConstraints.gridheight = 8;
		addGB(attrPanel, 2, 8);
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
		if(state) {
			attrComboModel.insertElementAt(ItemSlot.BUILDING, ItemSlot.BUILDING.getSlot() + 1);
		}
		else {
			attrComboModel.removeElement(ItemSlot.BUILDING);
		}
	}
	
	public void updatePanel(TFBotNode tf) {
		int[] indices = new int[tagList.getModel().getRowCount()]; //max possible taglist
		
		classBox.setSelectedItem(tf.getValueSingular(TFBotNode.CLASSNAME));
		//if(tf.containsKey(TFBotNode.CLASSNAME)) {
		//	setIconBox(iconModel, (Classes) tf.getValueSingular(TFBotNode.CLASSNAME)); //values of classname are classes from enum
		//}
		iconBox.setSelectedItem(tf.getValueSingular(TFBotNode.CLASSICON));
		nameField.setText((String) tf.getValueSingular(TFBotNode.NAME));
		if(tf.containsKey(TFBotNode.SKILL)) { //TODO: sort out skill
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
		}
		if(tf.containsKey(TFBotNode.WEAPONRESTRICT)) {
			switch ((String) tf.getValueSingular(TFBotNode.WEAPONRESTRICT)) {
				//case TFBotNode.ANY:
				default:
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
		}
		else {
			wepGroup.setSelected(anyBut.getModel(), true);
		}
		
		tagList.clearSelection();
		if(tf.containsKey(TFBotNode.TAGS)) {
			List<String> tags = new ArrayList<String>();
			tags.addAll((List<String>) tf.getValueSingular(TFBotNode.TAGS));
			//List<String> tags = (List<String>) tf.getValueSingular(TFBotNode.TAGS);
			
			//select all the tags that are already in the tagmodel
			for(int i = 0; i < tagModel.getRowCount(); i++) {
				if(tags.contains(tagModel.getValueAt(i, 0))) {
					tagList.changeSelection(i, 1, true, false);
					tags.remove(tagModel.getValueAt(i, 0));
				}
			}
			
			//then add new tags that weren't added
			for(String newTag : tags) {
				tagModel.addRow(new String[] {newTag});
				tagList.changeSelection(tagModel.getRowCount() - 1, 0, true, false);
			}
		}
		
		if(tf.containsKey(TFBotNode.ATTRIBUTES)) {
			List<String> attr = (List<String>) tf.getValueSingular(TFBotNode.ATTRIBUTES);
			for(int i = 0; i < attr.size(); i++) { //get indices so they can be selected
				indices[i] = botAttributeList.getNextMatch(attr.get(i), 0, Position.Bias.Forward);
				//System.out.println(indices[i]);
			}
			botAttributeList.setSelectedIndices(indices);
		}
		else {
			botAttributeList.clearSelection();
		}
		
		templateBox.setSelectedItem((String) tf.getValueSingular(TFBotNode.TEMPLATE));
		
		//sort items here if bot isn't sorted already
		//skip if we don't have a class to compare to
		if(tf.getValueSingular(TFBotNode.CLASSNAME) != Classes.None && !tf.isItemsSorted() && tf.containsKey(TFBotNode.ITEM)) {
			Classes cclass = (Classes) tf.getValueSingular(TFBotNode.CLASSNAME);
			List<String> newItemsList = new ArrayList<String>(TFBotNode.ITEMCOUNT);
			 
			List<String> itemsList = (List<String>) tf.getValueSingular(TFBotNode.ITEM);
			
			//this for is somewhat unclear
			for(int slot = ItemSlot.PRIMARY.getSlot(); slot < ItemSlot.BUILDING.getSlot(); slot++) {
				try {
					List<String> sublist = parser.checkIfItemInSlot(itemsList, cclass, slot);
					//check what happens if itemslist is empty
					
					if(!sublist.isEmpty()) { //if not empty, something matched the list
						if(slot == ItemSlot.COSMETIC1.getSlot()) {
							switch(sublist.size()) { 
								case 3: //flow down
									newItemsList.add(ItemSlot.COSMETIC3.getSlot(), sublist.get(2));
								case 2:
									newItemsList.add(ItemSlot.COSMETIC2.getSlot(), sublist.get(1));
								case 1:
									newItemsList.add(ItemSlot.COSMETIC1.getSlot(), sublist.get(0));
									break;
								default:
									break;
									//4 hats or something silly
							}
						}
					}
					else if(slot != ItemSlot.COSMETIC2.getSlot() && slot != ItemSlot.COSMETIC1.getSlot()) { //handle cosmetics all in one go
						itemsList.removeAll(sublist);
						newItemsList.add(slot, sublist.get(0));
						//should not have multiple primary/secondary/melees/buildings, so should be 1 length array
					}
				}
				catch (IndexOutOfBoundsException i) {
					//thrown if not spy hits building check
					//since we're done then, do nothing
				}
			}
			tf.setItemsSorted(true);
			tf.putKey(TFBotNode.ITEM, newItemsList);
		}
		
		if(tf.containsKey(TFBotNode.ITEM)) {
			List<String> array = (List<String>) tf.getValueSingular(TFBotNode.ITEM);
			
			primaryList.setSelectedItem(array.get(ItemSlot.PRIMARY.getSlot()));
			secList.setSelectedItem(array.get(ItemSlot.SECONDARY.getSlot()));
			meleeList.setSelectedItem(array.get(ItemSlot.MELEE.getSlot()));
			if(tf.getValueSingular(TFBotNode.CLASSNAME) == Classes.Spy) {
				buildingList.setSelectedItem(array.get(ItemSlot.BUILDING.getSlot()));
			}
			hat1List.setSelectedItem(array.get(ItemSlot.COSMETIC1.getSlot()));
			hat2List.setSelectedItem(array.get(ItemSlot.COSMETIC2.getSlot()));
			hat3List.setSelectedItem(array.get(ItemSlot.COSMETIC3.getSlot()));
			
			//setHatVisibility((String) hat1List.getSelectedItem(), hat1AttrBut);
			//setHatVisibility((String) hat2List.getSelectedItem(), hat2AttrBut);
			//setHatVisibility((String) hat3List.getSelectedItem(), hat3AttrBut);
		}
		if(tf.containsKey(TFBotNode.ITEMATTRIBUTES)) {
			attributeMapsArray = tf.getValueArray(TFBotNode.ITEMATTRIBUTES);
			/*
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
			} */
		}
		else {
			attributeMapsArray = new Object[TFBotNode.ITEMCOUNT];
		}
		if(tf.containsKey(TFBotNode.CHARACTERATTRIBUTES)) {
			currentCharAttributeMap = (Map<String, String>) tf.getValueSingular(TFBotNode.CHARACTERATTRIBUTES);
		}
		else {
			currentCharAttributeMap = new HashMap<String, String>();
		}
		attributesBox.setSelectedItem(ItemSlot.NONE);
	}
	
	public void updateNode(TFBotNode tf) { //put values into node
		List<String> tags = new ArrayList<String>(4);
		
		tf.putKey(TFBotNode.CLASSNAME, classBox.getSelectedItem());
		tf.putKey(TFBotNode.CLASSICON, iconBox.getSelectedItem()); //string
		tf.putKey(TFBotNode.NAME, nameField.getText());
		tf.putKey(TFBotNode.SKILL, skillGroup.getSelection().getActionCommand());
		if(wepGroup.getSelection().getActionCommand() != TFBotNode.ANY) {
			tf.putKey(TFBotNode.WEAPONRESTRICT, wepGroup.getSelection().getActionCommand());
		}
		
		tf.putKey(TFBotNode.TEMPLATE, templateBox.getSelectedItem());
		
		for(int row : tagList.getSelectedRows()) {
			tags.add((String) tagList.getValueAt(row, 0));
		}
		tf.putKey(TFBotNode.TAGS, tags);
		
		//TODO: make this a list
		String[] array = new String[TFBotNode.ITEMCOUNT];
		
		array[ItemSlot.PRIMARY.getSlot()] = (String) primaryList.getSelectedItem();
		array[ItemSlot.SECONDARY.getSlot()] = (String) secList.getSelectedItem();
		array[ItemSlot.MELEE.getSlot()] = (String) meleeList.getSelectedItem();
		if(classBox.getSelectedItem() == Classes.Spy) {
			array[ItemSlot.BUILDING.getSlot()] = (String) buildingList.getSelectedItem();
		}
		array[ItemSlot.COSMETIC1.getSlot()] = (String) hat1List.getSelectedItem();
		array[ItemSlot.COSMETIC2.getSlot()] = (String) hat2List.getSelectedItem();
		array[ItemSlot.COSMETIC3.getSlot()] = (String) hat3List.getSelectedItem();
		//item attributes are added separately
		
		tf.putKey(TFBotNode.ITEM, array);
		tf.putKey(TFBotNode.CHARACTERATTRIBUTES, currentCharAttributeMap);
		tf.putKey(TFBotNode.ITEMATTRIBUTES, attributeMapsArray);
		/*	
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
		 */
	}
	
	void setIconBox(DefaultComboBoxModel<String> model, Classes str) { //set icon list depending on class
		//todo: load icons from folder
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
			primaryList.setModel(getNewModel(lists, ItemSlot.PRIMARY.getSlot()));
			secList.setModel(getNewModel(lists, ItemSlot.SECONDARY.getSlot()));
			meleeList.setModel(getNewModel(lists, ItemSlot.MELEE.getSlot()));
			
			hat1List.setModel(getNewModel(lists, ItemSlot.COSMETIC1.getSlot() - 1));
			hat2List.setModel(getNewModel(lists, ItemSlot.COSMETIC1.getSlot() - 1));
			hat3List.setModel(getNewModel(lists, ItemSlot.COSMETIC1.getSlot() - 1));
			
			if(index == Classes.Spy && buildingList.getItemAt(0) == null) {
				//only need to load once since there's only one possible building list
				buildingList.setModel(getNewModel(lists, ItemSlot.BUILDING.getSlot() + 1));
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
		tagModel.setRowCount(0);
		
		for(String s : tags) { //add the constant tags
			tagModel.addRow(new String[]{s});
		}
		for(String s : list) { //then add the variable ones
			tagModel.addRow(new String[]{s});
		}
		//tagList.setModel(tagModel);
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
	//TODO: remove this or move where appropriate for templatewindow
	public void updateTemplateModel(Map<String, String> templateMap) {
		templateModel.removeAllElements();
		
		for(String templateName: templateMap.keySet()) {
			templateModel.addElement(templateName);
		}

		templateBox.setSelectedIndex(-1); //default to no template
	}
	
	//get info changes from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		switch(evt.getPropertyName()) {
			case SecondaryWindow.TAGS:
				updateTagList((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
				//tagList.setFixedCellWidth(-1);
				break;
			case MainWindow.ITEMPARSE:			
				BotPanel.parser = (ItemParser) evt.getNewValue();
				//may want to make this update once total instead of every botpanel
				//setClassItems(Classes.Scout); //since force updating, just default to first class
				break;
			case MainWindow.TFBOT:
				if(evt.getOldValue() == null) {
					templateModel.addElement((String) evt.getNewValue());
				}
				else if(evt.getNewValue() == null) {
					templateModel.removeElement((String) evt.getOldValue());
				}
				else {
					int index = templateModel.getIndexOf((String) evt.getOldValue());
					templateModel.removeElementAt(index);
					templateModel.insertElementAt((String) evt.getNewValue(), index);
				}
				//templateBox.setSelectedIndex(-1);
				break;
			case MainWindow.BOTTEMPLATEMAP: 
				updateTemplateModel((Map<String, String>) evt.getNewValue());
				break;
			default:
				break;
		}
	}
	
	//add radio button to proper panels and the group, then init the listeners
	private void initAttributeRadio() {
		attrComboModel.addElement(ItemSlot.NONE);
		attrComboModel.addElement(ItemSlot.CHARACTER);
		attrComboModel.addElement(ItemSlot.PRIMARY);
		attrComboModel.addElement(ItemSlot.SECONDARY);
		attrComboModel.addElement(ItemSlot.MELEE);
		
		//consider adding custom coloring
		
		attributesBox.addActionListener(event -> {
			ItemSlot slot = (ItemSlot) attributesBox.getSelectedItem();
			if(slot == ItemSlot.NONE) {
				attrPanel.setVisible(false);
			}
			else {
				attrPanel.setVisible(true);
				loadMap(slot);
			}
		});
	}
	
	private void initAttributePanel() {
		attrPanel.setLayout(gbLayout);
		attrPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		itemAttributeBox.setEditable(true);
		
		attrValueField.setMinimumSize(attrValueField.getPreferredSize());

		JScrollPane attrListPane = new JScrollPane(itemAttrList);
		itemAttrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		itemAttributeBox.setPrototypeDisplayValue("CHARACTER");
		attrListPane.setMinimumSize(itemAttrList.getPreferredScrollableViewportSize());
		
		attrPanel.addGB(addAttributeToListButton, 2, 0);
		attrPanel.addGB(removeAttributeFromList, 3, 0);
		
		attrPanel.addGB(attrValueField, 0, 1);
		
		attrPanel.gbConstraints.gridwidth = 2;
		attrPanel.gbConstraints.fill = GridBagConstraints.BOTH;
		attrPanel.addGB(itemAttributeBox, 0, 0);
		
		attrPanel.gbConstraints.fill = GridBagConstraints.NONE;
		attrPanel.addGB(updateAttributeValueButton, 1, 1);
		
		
		
		//attrPanel.gbConstraints.gridwidth = 2;
		//attrPanel.gbConstraints.gridheight = 6;
		attrPanel.addGB(attrListPane, 0, 2);
		
		attrBLManager.changeButtonState(States.EMPTY);
		
		itemAttributeBox.addItemListener(event -> {
			itemAttrList.clearSelection();
		}); //if user selects a new attribute from dropdown, automatically clear selection to save a click
		
		itemAttrList.addListSelectionListener(event -> { //fetches the value and updates button state
			if(itemAttrList.getSelectedIndex() != -1) {
				attrValueField.setText(currentAttributeMap.get(itemAttrList.getSelectedValue()));
				attrBLManager.changeButtonState(States.SELECTED);
			}
			else {
				//checkListSize();
			}
		});
		
		addAttributeToListButton.addActionListener(event -> {
			if(!itemAttrListModel.contains((String) itemAttributeBox.getSelectedItem())) {
				itemAttrListModel.addElement((String) itemAttributeBox.getSelectedItem());
			} //prevent duplicate entries in the visual list
			else {
				containingWindow.updateFeedback("Attribute already in list");
			}
			//currentAttributeMap.put((String) itemAttributeBox.getSelectedItem(), null);
			itemAttrList.setSelectedValue((String) itemAttributeBox.getSelectedItem(), false);
			//explicitly show it is selected here, also selects if it was already entered
			
			attrBLManager.changeButtonState(States.SELECTED);
		});
		
		updateAttributeValueButton.addActionListener(event -> {
			if(attrValueField.getText().isEmpty()) {
				containingWindow.updateFeedback("No value to add to attribute");
			}
			else { //put attr - value pair in map
				currentAttributeMap.put((String) itemAttributeBox.getSelectedItem(), attrValueField.getText());
				itemAttrListModel.setElementAt((String) itemAttributeBox.getSelectedItem() + " " + attrValueField.getText(), 
						itemAttrList.getSelectedIndex());
				
				containingWindow.updateFeedback("Attribute value added");
			}
		});
		
		//remove a specific attribute from the list
		removeAttributeFromList.addActionListener(event -> { 
			currentAttributeMap.remove((String) itemAttributeBox.getSelectedItem());
			//doesn't do anything if attr wasn't added to map
			itemAttrListModel.removeElementAt(itemAttrList.getSelectedIndex());	
			if(itemAttrListModel.isEmpty()) {
				attrBLManager.changeButtonState(States.EMPTY);
				//don't allow adding empty itemattributes
			}
			else {
				attrBLManager.changeButtonState(States.NOSELECTION);
			}
		});
	}
		
	private void checkListSize() {
		if(itemAttrListModel.getSize() == ATTRMAX) { //only allow as many attributes that can be fit
			attrBLManager.changeButtonState(States.REMOVEONLY);
		}
		/*
		else if(userAttrListModel.getSize() == 0) { //if nothing in list
			attrBLManager.changeButtonState(States.EMPTY);
		} */
		else { 
			attrBLManager.changeButtonState(States.NOSELECTION);
		}
	}
	
	//load the appropriate map for the item slot
	private void loadMap(ItemSlot index) {
		if(index == ItemSlot.CHARACTER) {
			//if(currentCharAttributeMap == null) {
			//	currentCharAttributeMap = new HashMap<String, String>();
			//}
			currentAttributeMap = currentCharAttributeMap;
		}
		else {
			//if(attributeMaps[index.getSlot()] == null) {
			//	attributeMaps[index.getSlot()] = new HashMap<String, String>();
			//}
			currentAttributeMap = (Map<String, String>) attributeMapsArray[index.getSlot()];
		}
		
		attrValueField.setText(null);
		itemAttrListModel.clear();
		currentAttributeMap.forEach((k, v) -> itemAttrListModel.addElement(k + " " + v));
		checkListSize(); //force check in case list index never changes (stays at unselected)
	}
	
	//load the map into model
	/*
	private void updateComponents(boolean loadList) {
		attrValueField.setText(null);
		if(loadList) { //if there is an existing list
			userAttrListModel.clear(); 
			currentAttributeMap.forEach((k, v) -> userAttrListModel.addElement(k + " " + v));
			checkListSize(); //force check in case list index never changes (stays at unselected)
		}
		else {
			currentAttributeMap = new HashMap<String, String>();
			userAttrListModel.clear(); //do it after updating addedAttributes so checklistsize can't false positive
		}
	} */
}
