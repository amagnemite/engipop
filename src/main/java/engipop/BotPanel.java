package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Position;

import engipop.ButtonListManager.States;
import engipop.Engipop.Classes;
import engipop.Engipop.ItemSlot;
import engipop.ItemParser.ItemData;
import engipop.Node.TFBotNode;
import engipop.Node.WaveSpawnNode;

//todo: add some sort of sanity checking for itemattributes
@SuppressWarnings("serial")
public class BotPanel extends EngiPanel implements PropertyChangeListener { //class to make the panel for bot creation/editing
	private final static int ATTRMAX = 20; //apparently the game stops parsing attributes after the 20th
	private final static String ADDNEWATTR = "Add new ItemAttributes";
	static ItemParser parser; //make sure all botpanels have same list
	
	String[] tags = {"bot_giant", "bot_squad_member"}; //potentially move bot_giant
	
	MainWindow mainWindow;
	EngiPanel attrPanel = new EngiPanel();
	WherePanel teleWherePanel = new WherePanel();
	
	//DefaultComboBoxModel<String> classModel;
	DefaultComboBoxModel<String> iconModel = new DefaultComboBoxModel<String>();
	DefaultTableModel tagModel = new DefaultTableModel(0, 1);
	DefaultTableModel teleModel = new DefaultTableModel(0, 1);
	DefaultComboBoxModel<String> primaryModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> secModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> meleeModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> buildingModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> hat1Model = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> hat2Model = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> hat3Model = new DefaultComboBoxModel<String>();
	
	JTextField nameField = new JTextField(30); //max bot name is ~32
	JTextField templateField = new JTextField(30);
	JComboBox<Classes> classBox = new JComboBox<Classes>(Classes.values());
	JComboBox<String> iconBox = new JComboBox<String>(iconModel);
	JTable tagTable = new JTable(tagModel);
	JTable teleTable = new JTable(teleModel);
	JList<String> botAttributeList;
	
	List<JComboBox<String>> itemLists = new ArrayList<JComboBox<String>>();
	
	JComboBox<String> primaryList = new JComboBox<String>(primaryModel);
	JComboBox<String> secList = new JComboBox<String>(secModel);
	JComboBox<String> meleeList = new JComboBox<String>(meleeModel);
	JComboBox<String> buildingList = new JComboBox<String>(buildingModel);
	JComboBox<String> hat1List = new JComboBox<String>(hat1Model);
	JComboBox<String> hat2List = new JComboBox<String>(hat2Model);
	JComboBox<String> hat3List = new JComboBox<String>(hat3Model);
	
	JLabel buildingLabel = new JLabel("Sapper: ");
	JLabel teleportLabel = new JLabel("TeleportWhere");
	
	ButtonGroup wepGroup = new ButtonGroup();
	ButtonGroup skillGroup = new ButtonGroup();
	
	JRadioButton noneBut = new JRadioButton(TFBotNode.NOSKILL);
	JRadioButton easyBut = new JRadioButton(TFBotNode.EASY);
	JRadioButton normalBut = new JRadioButton(TFBotNode.NORMAL);
	JRadioButton hardBut = new JRadioButton(TFBotNode.HARD);
	JRadioButton expBut = new JRadioButton(TFBotNode.EXPERT);
	
	JRadioButton anyBut = new JRadioButton(TFBotNode.ANY);
	JRadioButton priBut = new JRadioButton(TFBotNode.PRIMARYONLY);
	JRadioButton secBut = new JRadioButton(TFBotNode.SECONDARYONLY);
	JRadioButton melBut = new JRadioButton(TFBotNode.MELEEONLY);
	
	DefaultComboBoxModel<String> attributesSlotsModel = new DefaultComboBoxModel<String>();
	JComboBox<String> attributesSlotsBox = new JComboBox<String>(attributesSlotsModel);
	
	DefaultTableModel itemAttributeTableModel = new DefaultTableModel(0, 2);
	JTable itemAttributeTable = new JTable(itemAttributeTableModel) { //lock first row from editing
		public boolean isCellEditable(int row, int column) {
			if(row == 0 && column == 0) {
				return false;
			}
			return true;
		}
	};
	
	JButton addAttributeToListButton = new JButton("Add attribute");
	JButton removeAttributeFromList = new JButton("Remove attribute");
	
	JComboBox<String> itemAttributesListBox;
	List<Map<String, Object>> attributeMapsArray = new ArrayList<Map<String, Object>>(TFBotNode.ITEMCOUNT); //contains all item attribute maps
	Map<String, Object> currentAttributeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	Map<String, Object> currentCharAttributeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	
	//optional keyvals
	JLabel healthLabel = new JLabel("Health:");
	JSpinner healthSpinner = new JSpinner();
	JLabel scaleLabel = new JLabel("Scale:");
	JSpinner scaleSpinner = new JSpinner();
	JLabel autoJumpMinLabel = new JLabel("AutoJumpMin:");
	JSpinner autoJumpMinSpinner = new JSpinner();
	JLabel autoJumpMaxLabel = new JLabel("AutoJumpMax:");
	JSpinner autoJumpMaxSpinner = new JSpinner();
	JLabel maxVisionLabel = new JLabel("MaxVisionRange:");
	JSpinner maxVisionSpinner = new JSpinner();
	
	public BotPanel(MainWindow mainWindow, PopulationPanel popPanel) {
		//window to send feedback to, mainwindow to get item updates, secondarywindow to get map updates
		JTextField cellEditor = new JTextField();
		JButton addTagRow = new JButton("+");
		JButton removeTagRow = new JButton("-");
		
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		setBackground(new Color(192, 192, 192));
		attrPanel.setOpaque(false);
		
		this.mainWindow = mainWindow;
		popPanel.addPropertyChangeListener(this);
		mainWindow.addPropertyChangeListener(this);
		
		itemAttributesListBox = new JComboBox<String>(new ItemAttributes().getItemAttributes());
		attrPanel.setVisible(false);
		removeTagRow.setEnabled(false);
		
		itemLists.add(primaryList);
		itemLists.add(secList);
		itemLists.add(meleeList);
		itemLists.add(hat1List);
		itemLists.add(hat2List);
		itemLists.add(hat3List);
		itemLists.add(buildingList);
		
		initAttributePanel();
		
		botAttributeList = new JList<String>(TFBotNode.getAttributesList().toArray(new String[TFBotNode.getAttributesList().size()]));
		
		iconBox.setPrototypeDisplayValue("heavyweapons_healonkill_giant");
		//templateField.setPrototypeDisplayValue("Giant Rapid Fire Demo Chief (T_TFBot_Giant_Demo_Spammer_Reload_Chief)");
		//tagList.setPrototypeCellValue("bot_squad_member");
		botAttributeList.setPrototypeCellValue("BecomeSpectatorOnDeath");
		
		tagModel.addRow(new String[] {""});
		tagTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cellEditor));
		
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
					buildingList.setVisible(true);
					buildingLabel.setVisible(true);
					
					teleportLabel.setVisible(false);
					teleWherePanel.setVisible(false);
				}
				else if(classBox.getSelectedItem() == Classes.Engineer) {
					teleportLabel.setVisible(true);
					teleWherePanel.setVisible(true);
					
					buildingList.setVisible(false);
					buildingLabel.setVisible(false);
				}
				else {
					buildingList.setVisible(false);
					buildingLabel.setVisible(false);
					
					teleportLabel.setVisible(false);
					teleWherePanel.setVisible(false);
				}
			}
		});
		tagTable.getSelectionModel().addListSelectionListener(event -> {
			if(tagTable.getSelectedRowCount() == 1) {
				removeTagRow.setEnabled(true);
			}
			else {
				removeTagRow.setEnabled(false);
			}
		});
		addTagRow.addActionListener(event -> {
			tagModel.addRow(new String[] {""});
		});
		removeTagRow.addActionListener(event -> {
			tagModel.removeRow(tagTable.getSelectedRow());
		});
		
		classBox.setSelectedIndex(0);
		tagTable.setTableHeader(null);
		
		iconBox.setEditable(true);
		primaryList.setEditable(true);
		secList.setEditable(true);
		meleeList.setEditable(true);
		buildingList.setEditable(true);
		hat1List.setEditable(true);
		hat2List.setEditable(true);
		hat3List.setEditable(true);
		templateField.setEditable(true);
		
		buildingList.setVisible(false);
	
		//skill level radio buttons
		JPanel skillPanel = new JPanel();
		JPanel wepPanel = new JPanel();
		skillPanel.setOpaque(false);
		wepPanel.setOpaque(false);
		
		skillPanel.add(noneBut);
		noneBut.setActionCommand(TFBotNode.NOSKILL);
		skillGroup.add(noneBut);
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
		JLabel botAttributesLabel = new JLabel("Attributes: ");
		JLabel botPrimary = new JLabel("Primary weapon: ");
		JLabel botSecondary = new JLabel("Secondary weapon: ");
		JLabel botMelee = new JLabel("Melee weapon: ");
		JLabel botHat = new JLabel("Hats: ");
		JLabel templateLabel = new JLabel("Template: ");
		JLabel itemAttributesLabel = new JLabel("ItemAttributes: ");
	
		JScrollPane tagListPane = new JScrollPane(tagTable);
		JScrollPane attributesListPane = new JScrollPane(botAttributeList);
		JPanel tagButtonPanel = new JPanel();
		
		teleportLabel.setVisible(false);
		teleWherePanel.setVisible(false);
		
		//prevents dumb resizing stuff with the attr panel
		//may fix later
		nameField.setMinimumSize(nameField.getPreferredSize());
		templateField.setMinimumSize(templateField.getPreferredSize());
		classBox.setMinimumSize(classBox.getPreferredSize());
		iconBox.setMinimumSize(iconBox.getPreferredSize()); //double check this once custom input happens
		//tagListPane.setMinimumSize(tagList.getPreferredScrollableViewportSize());
		tagListPane.setMinimumSize(new Dimension(200, 100));
		tagListPane.setPreferredSize(new Dimension(200, 100));
		attributesListPane.setMinimumSize(new Dimension(botAttributeList.getPreferredScrollableViewportSize().width + 20, 
				botAttributeList.getPreferredScrollableViewportSize().height));
		attributesListPane.setMaximumSize(new Dimension(375, 250));
		//375, 250
		//attributesListPane.setPreferredSize(attributesListPane.getMinimumSize());
		
		addTagRow.setToolTipText("Add a row to the tag list");
		removeTagRow.setToolTipText("Remove the currently selected tag");
		tagButtonPanel.add(addTagRow);
		tagButtonPanel.add(removeTagRow);
		
		addGB(botClass, 0, 0);
		addGB(classBox, 1, 0);
		addGB(botIcon, 2, 0);
		addGB(iconBox, 3, 0); //fix size so it doesn't change for long lists
		
		addGB(botName, 0, 1);
		addGB(nameField, 1, 1); 
		addGB(templateLabel, 2, 1);
		addGB(templateField, 3, 1);
		
		addGB(botSkill, 0, 3);
		addGB(skillPanel, 1, 3);
		addGB(botRestrict, 2, 3);
		addGB(wepPanel, 3, 3);
		addGB(tagButtonPanel, 1, 5);
		addGB(botTags, 0, 4);
		addGB(tagListPane, 1, 4);
		
		addGB(botAttributesLabel, 2, 4);
		addGB(attributesListPane, 3, 4);
		
		addGB(botPrimary, 0, 8);
		addGB(primaryList, 1, 8);
		addGB(botSecondary, 0, 9);
		addGB(secList, 1, 9);
		addGB(botMelee, 0, 10);
		addGB(meleeList, 1, 10);
		
		addGB(buildingLabel, 0, 11);
		addGB(buildingList, 1, 11);
		
		addGB(botHat, 0, 12);
		addGB(hat1List, 1, 12);		
		addGB(hat2List, 1, 13);		
		addGB(hat3List, 1, 14);
		addGB(teleportLabel, 0, 15);
		
		addGB(itemAttributesLabel, 2, 7);
		addGB(attributesSlotsBox, 3, 7);
		
		gbConstraints.gridwidth = 2;
		addGB(teleWherePanel, 1, 15);
		
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.gridheight = 8;
		addGB(attrPanel, 2, 8);
		
		gbConstraints = new GridBagConstraints();
		gbConstraints.anchor = GridBagConstraints.WEST;
		addGB(healthLabel, 0, 16);
		addGB(healthSpinner, 1, 16);
		addGB(scaleLabel, 2, 16);
		addGB(scaleSpinner, 3, 16);
		addGB(autoJumpMinLabel, 0, 17);
		addGB(autoJumpMinSpinner, 1, 17);
		addGB(autoJumpMaxLabel, 2, 17);
		addGB(autoJumpMaxSpinner, 3, 17);
		addGB(maxVisionLabel, 0, 18);
		addGB(maxVisionSpinner, 1, 18);
		
		healthLabel.setVisible(false);
		healthSpinner.setVisible(false);
		scaleLabel.setVisible(false);
		scaleSpinner.setVisible(false);
		autoJumpMinLabel.setVisible(false);
		autoJumpMinSpinner.setVisible(false);
		autoJumpMaxLabel.setVisible(false);
		autoJumpMaxSpinner.setVisible(false);
		maxVisionLabel.setVisible(false);
		maxVisionSpinner.setVisible(false);
	}
	
	private void initAttributePanel() {
		attrPanel.setLayout(gbLayout);
		attrPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		attributesSlotsModel.addElement(ItemSlot.NONE.toString());
		attributesSlotsModel.addElement(ItemSlot.CHARACTER.toString());
		attributesSlotsModel.addElement(ADDNEWATTR);
		
		JTextField cellField = new JTextField();
		DefaultCellEditor cellEditor = new DefaultCellEditor(cellField);
		itemAttributeTableModel.setColumnIdentifiers(new Object[] {"Attribute name", "Value"});
		itemAttributeTable.putClientProperty("terminateEditOnFocusLost", true);
		itemAttributeTable.getColumnModel().getColumn(1).setCellEditor(cellEditor);
		//so adding identifiers causes new column objs
		
		//consider adding custom coloring
		
		itemAttributesListBox.setEditable(true);

		JScrollPane attrListPane = new JScrollPane(itemAttributeTable);
		//itemAttributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		itemAttributesListBox.setPrototypeDisplayValue("CHARACTER");
		attrListPane.setPreferredSize(new Dimension(300, 200)); //this is arbitary but the preferred is giant
		//itemAttributeTable.getPreferredScrollableViewportSize()
		
		attrPanel.addGB(addAttributeToListButton, 0, 1);
		attrPanel.addGB(removeAttributeFromList, 1, 1);
		
		attrPanel.gbConstraints.gridwidth = 2;
		attrPanel.gbConstraints.fill = GridBagConstraints.BOTH;
		attrPanel.addGB(itemAttributesListBox, 0, 0);
		
		attrPanel.gbConstraints.fill = GridBagConstraints.NONE;
		
		//attrPanel.gbConstraints.gridwidth = 2;
		//attrPanel.gbConstraints.gridheight = 6;
		attrPanel.addGB(attrListPane, 0, 2);
		
		itemAttributeTable.getSelectionModel().addListSelectionListener(event -> {
			if(itemAttributeTable.getSelectedRowCount() == 1 && itemAttributeTable.getSelectedRow() != 0) {
				removeAttributeFromList.setEnabled(true);
			}
			else {
				removeAttributeFromList.setEnabled(false);
			}
		});
		cellEditor.addCellEditorListener(new CellEditorListener() {
			public void editingCanceled(ChangeEvent c) {
			}
			
			public void editingStopped(ChangeEvent c) {
				if(!attributesSlotsModel.getSelectedItem().equals(ItemSlot.CHARACTER.toString())) {
					if(itemAttributeTable.getSelectedRow() == 0 && itemAttributeTable.getSelectedColumn() == 1) {
						int index = attributesSlotsBox.getSelectedIndex();
						currentAttributeMap.put(TFBotNode.ITEMNAME, (String) cellEditor.getCellEditorValue());
						//this will trigger an attributes slots action
						attributesSlotsModel.insertElementAt((String) cellEditor.getCellEditorValue(), index);
						attributesSlotsModel.removeElementAt(index + 1);
						return;
					}
				}
				
				if(((String) cellEditor.getCellEditorValue()).isBlank()) {
					currentAttributeMap.remove((String) itemAttributesListBox.getSelectedItem());
					mainWindow.setFeedback("No value to add to attribute");
				}
				else { //put attr - value pair in map
					currentAttributeMap.put((String) itemAttributesListBox.getSelectedItem(), cellEditor.getCellEditorValue());
					mainWindow.setFeedback("Attribute value added");
				}
			}
		});
		
		addAttributeToListButton.addActionListener(event -> {
			if(!currentAttributeMap.containsKey((String) itemAttributesListBox.getSelectedItem())) {
				itemAttributeTableModel.addRow(new String[] {(String) itemAttributesListBox.getSelectedItem(), ""});
				currentAttributeMap.put((String) itemAttributesListBox.getSelectedItem(), null);
			}
			else {
				mainWindow.setFeedback("Attribute already in list");
			}
			
			if(itemAttributeTable.getRowCount() - 1 == ATTRMAX) {
				addAttributeToListButton.setEnabled(false);
			}
		});
		
		//remove a specific attribute from the list
		removeAttributeFromList.addActionListener(event -> { 
			currentAttributeMap.remove(itemAttributeTable.getValueAt(itemAttributeTable.getSelectedRow(), 0));
			//doesn't do anything if attr wasn't added to map
			itemAttributeTableModel.removeRow(itemAttributeTable.getSelectedRow());
			
			if(!addAttributeToListButton.isEnabled()) { //reenable if we were previously at max
				addAttributeToListButton.setEnabled(true);
			}
		});
		attributesSlotsBox.addActionListener(event -> {
			String slot = (String) attributesSlotsBox.getSelectedItem();
			if(attributesSlotsBox.getSelectedIndex() != -1) {
				if(slot == ItemSlot.NONE.toString()) {
					attrPanel.setVisible(false);
				}
				else if(slot.equals(ADDNEWATTR)) {
					if(attributesSlotsBox.getItemCount() < TFBotNode.ITEMCOUNT) {
						attributesSlotsModel.insertElementAt(ADDNEWATTR, attributesSlotsBox.getItemCount());
					}
					attrPanel.setVisible(true);
					loadMap(slot);
				}
				else {
					attrPanel.setVisible(true);
					loadMap(slot);
				}
			}
		});
	}
	public void updatePanel(TFBotNode tf) {	
		classBox.setSelectedItem(tf.getValue(TFBotNode.CLASSNAME));
		if(tf.containsKey(TFBotNode.CLASSNAME)) {
			setIconBox(iconModel, (Classes) tf.getValue(TFBotNode.CLASSNAME)); //values of classname are classes from enum
		}
		iconBox.setSelectedItem(tf.getValue(TFBotNode.CLASSICON));
		nameField.setText((String) tf.getValue(TFBotNode.NAME));
		if(tf.containsKey(TFBotNode.SKILL)) {
			switch ((String) tf.getValue(TFBotNode.SKILL)) {
				case TFBotNode.NOSKILL:
					skillGroup.setSelected(noneBut.getModel(), true);
					break;
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
		else {
			skillGroup.setSelected(noneBut.getModel(), true);
		}
		if(tf.containsKey(TFBotNode.WEAPONRESTRICT)) {
			switch ((String) tf.getValue(TFBotNode.WEAPONRESTRICT)) {
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
		
		tagTable.clearSelection();
		if(tf.containsKey(TFBotNode.TAGS)) {
			List<Object> tags = new ArrayList<Object>();
			tags.addAll(tf.getListValue(TFBotNode.TAGS));
			
			//select all the tags that are already in the tagmodel
			for(int i = 0; i < tagModel.getRowCount(); i++) {
				if(tags.contains(tagModel.getValueAt(i, 0))) {
					tagTable.changeSelection(i, 1, true, false);
					tags.remove(tagModel.getValueAt(i, 0));
				}
			}
			
			//then add new tags that weren't added
			for(Object newTag : tags) {
				tagModel.addRow(new String[] {(String) newTag});
				tagTable.changeSelection(tagModel.getRowCount() - 1, 0, true, false);
			}
		}
		
		botAttributeList.clearSelection();
		if(tf.containsKey(TFBotNode.ATTRIBUTES)) {
			List<Object> attr = tf.getListValue(TFBotNode.ATTRIBUTES);
			int[] indices = new int[attr.size()]; //max possible taglist
			
			for(int i = 0; i < attr.size(); i++) { //get indices so they can be selected
				indices[i] = botAttributeList.getNextMatch((String) attr.get(i), 0, Position.Bias.Forward);
				//System.out.println(indices[i]);
			}
			botAttributeList.setSelectedIndices(indices);
		}
		
		templateField.setText((String) tf.getValue(TFBotNode.TEMPLATE));
		
		if(!tf.isItemsSorted() && tf.containsKey(TFBotNode.ITEM) && (Classes) tf.getValue(TFBotNode.CLASSNAME) != Classes.None) {
			String[] oldBotItems = (String[]) tf.getValue(TFBotNode.ITEM);
			String newBotItems[] = new String[TFBotNode.ITEMCOUNT];
			List<ItemData> classList = parser.getClassList((Classes) tf.getValue(TFBotNode.CLASSNAME));
			List<String> asList = Arrays.asList(oldBotItems);
			
			for(ItemData item : classList) {
				String itemName = item.toString();
				
				if(asList.contains(itemName.toLowerCase())) {
					if(item.getSlot() == ItemSlot.COSMETIC1) {
						if(newBotItems[ItemSlot.COSMETIC1.getSlot()] == null) {
							newBotItems[ItemSlot.COSMETIC1.getSlot()] = itemName;
						}
						else if(newBotItems[ItemSlot.COSMETIC2.getSlot()] == null) {
							newBotItems[ItemSlot.COSMETIC2.getSlot()] = itemName;
						}
						else {
							newBotItems[ItemSlot.COSMETIC3.getSlot()] = itemName;
						}
					}
					else {
						newBotItems[item.getSlot().getSlot()] = itemName;
					}
					oldBotItems[asList.indexOf(itemName.toLowerCase())] = null;
				}
				if(asList.isEmpty()) {
					break;
				}
			}
			
			tf.putKey(TFBotNode.ITEM, newBotItems);
			tf.setItemsSorted(true);
		}
		
		if(tf.isItemsSorted() && tf.containsKey(TFBotNode.ITEM)) {
			String[] items = (String[]) tf.getValue(TFBotNode.ITEM);
			
			primaryList.setSelectedItem(items[ItemSlot.PRIMARY.getSlot()]);
			secList.setSelectedItem(items[ItemSlot.SECONDARY.getSlot()]);
			meleeList.setSelectedItem(items[ItemSlot.MELEE.getSlot()]);
			buildingList.setSelectedItem(items[ItemSlot.BUILDING.getSlot()]);
			hat1List.setSelectedItem(items[ItemSlot.COSMETIC1.getSlot()]);
			hat2List.setSelectedItem(items[ItemSlot.COSMETIC2.getSlot()]);
			hat3List.setSelectedItem(items[ItemSlot.COSMETIC3.getSlot()]);
		}

		if(tf.containsKey(TFBotNode.ITEMATTRIBUTES)) {
			List<Object> list = tf.getListValue(TFBotNode.ITEMATTRIBUTES);
			attributeMapsArray = new ArrayList<Map<String, Object>>(TFBotNode.ITEMCOUNT);
			attributesSlotsBox.removeAllItems();
			attributesSlotsBox.addItem(ItemSlot.NONE.toString());
			attributesSlotsBox.addItem(ItemSlot.CHARACTER.toString());
			
			for(Object entry : list) {
				Map<String, Object> map = (Map<String, Object>) entry;
				Map<String, Object> mapCopy = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
				mapCopy.putAll(map);
				
				if(map.containsKey(TFBotNode.ITEMNAME)) {
					attributesSlotsBox.addItem((String) map.get(TFBotNode.ITEMNAME));
				}
				
				attributeMapsArray.add(mapCopy);
			}
			
			if(attributesSlotsBox.getItemCount() < TFBotNode.ITEMCOUNT) {
				attributesSlotsModel.insertElementAt(ADDNEWATTR, attributesSlotsBox.getItemCount());
			}
			
			attributesSlotsBox.setSelectedIndex(0);
		}
		else {
			attributeMapsArray = new ArrayList<Map<String, Object>>(TFBotNode.ITEMCOUNT);
			attributesSlotsBox.removeAllItems();
			attributesSlotsBox.addItem(ItemSlot.NONE.toString());
			attributesSlotsBox.addItem(ItemSlot.CHARACTER.toString());
			
			if(attributesSlotsBox.getItemCount() < TFBotNode.ITEMCOUNT) {
				attributesSlotsModel.insertElementAt(ADDNEWATTR, attributesSlotsBox.getItemCount());
			}
		}
		
		if(tf.containsKey(TFBotNode.CHARACTERATTRIBUTES)) {
			currentCharAttributeMap = (Map<String, Object>) tf.getValue(TFBotNode.CHARACTERATTRIBUTES);
		}
		else {
			currentCharAttributeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		}
		attributesSlotsBox.setSelectedItem(ItemSlot.NONE);
		
		teleWherePanel.clearSelection();
		if(tf.containsKey(TFBotNode.TELEPORTWHERE)) {
			teleWherePanel.updateWhere(tf.getListValue(TFBotNode.TELEPORTWHERE));
		}
	}
	
	public void updateNode(TFBotNode tf) { //put values into node
		List<String> tags = new ArrayList<String>(4);
		List<String> botAttr = new ArrayList<String>(4);
		
		tf.putKey(TFBotNode.CLASSNAME, classBox.getSelectedItem());
		tf.putKey(TFBotNode.CLASSICON, iconBox.getSelectedItem()); //string
		tf.putKey(TFBotNode.NAME, nameField.getText());
		//allow the default values, filter them out in treeparse while we're parsing
		tf.putKey(TFBotNode.SKILL, skillGroup.getSelection().getActionCommand());
		tf.putKey(TFBotNode.WEAPONRESTRICT, wepGroup.getSelection().getActionCommand());
		
		tf.putKey(TFBotNode.TEMPLATE, templateField.getText());
		
		for(int row : tagTable.getSelectedRows()) {
			tags.add((String) tagTable.getValueAt(row, 0));
		}
		tf.putKey(TFBotNode.TAGS, tags);
		
		botAttr.addAll(botAttributeList.getSelectedValuesList());
		tf.putKey(TFBotNode.ATTRIBUTES, botAttr);
		
		//this sucks
		String[] array = new String[TFBotNode.ITEMCOUNT];
		
		if(primaryList.getSelectedItem() != null && !((String) primaryList.getSelectedItem()).isBlank()) {
			array[ItemSlot.PRIMARY.getSlot()] = (String) primaryList.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.PRIMARY.getSlot()] = null;
		}
		
		if(secList.getSelectedItem() != null && !((String) secList.getSelectedItem()).isBlank()) {
			array[ItemSlot.SECONDARY.getSlot()] = (String) secList.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.SECONDARY.getSlot()] = null;
		}
		
		if(meleeList.getSelectedItem() != null && !((String) meleeList.getSelectedItem()).isBlank()) {
			array[ItemSlot.MELEE.getSlot()] = (String) meleeList.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.MELEE.getSlot()] = null;
		}
		
		if(classBox.getSelectedItem() == Classes.Spy) {
			//TODO: need to clear out buildings for not spy classes probably
			if(buildingList.getSelectedItem() != null && !((String) buildingList.getSelectedItem()).isBlank()) {
				array[ItemSlot.BUILDING.getSlot()] = (String) buildingList.getSelectedItem();
			}
			else { //if null or is blank
				array[ItemSlot.BUILDING.getSlot()] = null;
			}
		}
		else {
			array[ItemSlot.BUILDING.getSlot()] = null;
		}
		
		if(hat1List.getSelectedItem() != null && !((String) hat1List.getSelectedItem()).isBlank()) {
			array[ItemSlot.COSMETIC1.getSlot()] = (String) hat1List.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.COSMETIC1.getSlot()] = null;
		}
		
		if(hat2List.getSelectedItem() != null && !((String) hat2List.getSelectedItem()).isBlank()) {
			array[ItemSlot.COSMETIC2.getSlot()] = (String) hat2List.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.COSMETIC2.getSlot()] = null;
		}
		
		if(hat3List.getSelectedItem() != null && !((String) hat3List.getSelectedItem()).isBlank()) {
			array[ItemSlot.COSMETIC3.getSlot()] = (String) hat3List.getSelectedItem();
		}
		else { //if null or is blank
			array[ItemSlot.COSMETIC3.getSlot()] = null;
		}
		
		//item attributes are added separatelY
		if(Arrays.asList(array).isEmpty()) {
			tf.removeKey(TFBotNode.ITEM);
		}
		else {
			tf.putKey(TFBotNode.ITEM, array);
		}
		
		//this likely also needs some validation
		tf.putKey(TFBotNode.CHARACTERATTRIBUTES, currentCharAttributeMap);
		
		Iterator<Map <String, Object>> iterator = attributeMapsArray.iterator();
		while(iterator.hasNext()) {
			Map<String, Object> map = iterator.next();
			if(map.get(TFBotNode.ITEMNAME) == null || map.get(TFBotNode.ITEMNAME).equals(ADDNEWATTR)) {
				iterator.remove();
			}
		}
		
		//TODO: this may not be necessary?
		int emptyItemAttributes = 0;
		for(Map<String, Object> attributeMap : attributeMapsArray) {
			if(attributeMap.isEmpty()) {
				emptyItemAttributes++;
			}
		}
		if(emptyItemAttributes == 0) {
			tf.putKey(TFBotNode.ITEMATTRIBUTES, attributeMapsArray);
		}
		
		tf.putKey(TFBotNode.TELEPORTWHERE, teleWherePanel.updateNode());
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
			List<ItemData> lists = parser.getClassList(index);
			
			for(ItemData item : lists) {
				String itemName = item.toString();
				
				switch(item.getSlot()) {
					case PRIMARY:
						primaryModel.addElement(itemName);
						break;
					case SECONDARY:
						secModel.addElement(itemName);
						break;
					case MELEE:
						meleeModel.addElement(itemName);
						break;
					case BUILDING:
						buildingModel.addElement(itemName);
						break;
					case COSMETIC1:
						hat1Model.addElement(itemName);
						hat2Model.addElement(itemName);
						hat3Model.addElement(itemName);
						break;
					default:
						break;
						
				}
			}
			
			//set model(get the appropriate slot from lists, convert to a new string array of size inner list)
			/*
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
			} */
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
	}
	
	//get info changes from secondarywindow
	//TODO: this receives tfbot template events
	public void propertyChange(PropertyChangeEvent evt) {
		switch(evt.getPropertyName()) {
			case PopulationPanel.TAGS:
				updateTagList((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
				//tagList.setFixedCellWidth(-1);
				break;
			/*
			case WaveSpawnNode.TFBOT:
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
			*/	
			default:
				break;
		}
	}
	
	public static void setItemParser(ItemParser parser) {
		BotPanel.parser = parser;
	}
		
	private void checkListSize() {
		if(itemAttributeTableModel.getRowCount() == ATTRMAX) { //only allow as many attributes that can be fit
			addAttributeToListButton.setEnabled(false);
			removeAttributeFromList.setEnabled(true);
		}
		else { 
			addAttributeToListButton.setEnabled(true);
			removeAttributeFromList.setEnabled(false);
		}
	}
	
	//load the appropriate map for the item slot
	private void loadMap(String item) {
		if(item.equals(ItemSlot.CHARACTER.toString())) {
			currentAttributeMap = currentCharAttributeMap;
		}
		else {
			currentAttributeMap = null;
			for(Map<String, Object> entry : attributeMapsArray) {
				if(item.equals(entry.get(TFBotNode.ITEMNAME))) {
					currentAttributeMap = entry;
					break;
				}
			}
			
			if(currentAttributeMap == null) {
				Map<String, Object> newMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
				newMap.put(TFBotNode.ITEMNAME, item);
				attributeMapsArray.add(newMap);
				currentAttributeMap = newMap;
			}
		}
		
		itemAttributeTableModel.setRowCount(0);
		
		if(!item.equals(ItemSlot.CHARACTER.toString())) {
			itemAttributeTableModel.addRow(new Object[] {TFBotNode.ITEMNAME, item});
		}
		
		currentAttributeMap.forEach((k, v) -> {
			if(v != null && !v.equals(item)) {
				itemAttributeTableModel.addRow(new String[] {k, v.toString()});
			}
		});
		checkListSize(); //force check in case list index never changes (stays at unselected)
	}
}
