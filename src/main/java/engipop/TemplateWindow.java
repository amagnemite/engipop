package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import engipop.ButtonListManager.States;
import engipop.Tree.Node;
import engipop.Tree.PopNode;
import engipop.Tree.RandomChoiceNode;
import engipop.Tree.SquadNode;
import engipop.Tree.TFBotNode;
import engipop.Tree.TankNode;
import engipop.Tree.WaveNode;
import engipop.Tree.WaveSpawnNode;

//window to edit bot and ws templates
//similar structure to main window
@SuppressWarnings("serial")
public class TemplateWindow extends EngiWindow {
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	WaveSpawnTemplatePanel wsTemplatePanel = new WaveSpawnTemplatePanel();
	TFBotTemplatePanel botTemplatePanel = new TFBotTemplatePanel();
	TankPanel tankPanel;
	JPanel spawnerPanel = new JPanel();
	ListPanel listPanel = new ListPanel();
	
	PopNode popNode;
	WaveSpawnNode currentWSNode = new WaveSpawnNode();
	TFBotNode currentBotNode = new TFBotNode();
	TankNode currentTankNode = new TankNode();
	SquadNode currentSquadNode = new SquadNode();
	RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	//TemplateNode templateParent = new TemplateNode(); //placeholder node, doesn't contain anything
	int generated = 1; //attempt to make sure names are unique
	
	public TemplateWindow(MainWindow w, SecondaryWindow w2) {
		super("Template editor");
		setSize(1300, 800);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		feedback = new JLabel(" ");
		
		this.popNode = w.getPopNode();
		wsPanel = new WaveSpawnPanel(w2);
		botPanel = new BotPanel(this, w, w2);
		tankPanel = new TankPanel(w2);
		
		tankPanel.setVisible(false);
		
		JRadioButton botModeButton = new JRadioButton("Bot");
		JRadioButton wsModeButton = new JRadioButton("WaveSpawn");
		
		JPanel modePanel = new JPanel();
		ButtonGroup modeGroup = new ButtonGroup();
		JLabel modeLabel = new JLabel("Template type: ");
		
		modePanel.add(modeLabel);
		modePanel.add(botModeButton);
		modePanel.add(wsModeButton);
		
		modeGroup.add(botModeButton);
		modeGroup.add(wsModeButton);
		
		//make template mode state clear
		botModeButton.addItemListener(event -> {
			if(botModeButton.isSelected()) {
				botTemplatePanel.setVisible(true);
			}
			else {
				botTemplatePanel.setVisible(false);
				botTemplatePanel.setListIndex(-1);
			}
		});
		wsModeButton.addItemListener(event -> {
			if(wsModeButton.isSelected()) {
				wsPanel.setVisible(true);
				wsTemplatePanel.setVisible(true);
				spawnerPanel.setVisible(true);
				listPanel.setVisible(true);
			}
			else {
				wsPanel.setVisible(false);
				wsTemplatePanel.setVisible(false);
				spawnerPanel.setVisible(false);
				listPanel.setVisible(false);
				listPanel.setSelectedButton(TFBOT);
				wsTemplatePanel.setListIndex(-1);
			}
		});
		
		//force a load/hide ws cycle
		modeGroup.setSelected(wsModeButton.getModel(), true);
		modeGroup.setSelected(botModeButton.getModel(), true);
		
		addGB(feedback, 0, 0);
		addGB(modePanel, 0, 1);
		addGB(wsPanel, 0, 2);
		addGB(spawnerPanel, 0, 3);
		addGB(botPanel, 0, 4);
		addGB(tankPanel, 0, 4);
		
		addGB(wsTemplatePanel, 1, 2);
		addGB(botTemplatePanel, 1, 4);
		addGB(listPanel, 1, 4);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	//check if map contains the entered template name already
	protected boolean checkTemplateName(String newName, int type) {
		boolean overwrite = false;
		
		Set<String> keyset;
		
		if(type == TFBOT) {
			keyset = popNode.getBotTemplateMap().keySet();
		}
		else { //ws
			keyset = popNode.getWSTemplateMap().keySet();
		}
		
		if(keyset.contains(newName)) { //todo: fix 
			int op = JOptionPane.showConfirmDialog(this, "Overwrite the existing template?");
			if(op == JOptionPane.YES_OPTION) {
				overwrite = true;
			}
		}
		return overwrite;
	}
	
	//generate a template name if user doesn't pick one
	protected String generateTemplateName(Node node) {
		String header = "T_";
		String type;
		String body = "";
		String full;
		
		Set<String> keyset;
		
		if(node.getClass() == WaveSpawnNode.class) { //returns format of T_WaveSpawn_[wavespawn name]_[spawner]
			type = "WaveSpawn_";
			WaveSpawnNode wsNode = (WaveSpawnNode) node; //redundant but code clarity
			
			if(wsNode.getMap().containsKey(WaveSpawnNode.NAME)) { //this currently doesn't get used
				body = (String) wsNode.getValueSingular(WaveSpawnNode.NAME);
			}
			
			if(wsNode.hasChildren()) { //same here
				body = body + "_" + wsNode.getSpawnerType();
			}
			keyset = popNode.getWSTemplateMap().keySet();
		}
		else { //returns format of T_TFBot_[bot class]_[bot name]
			type = "TFBot_";
			TFBotNode botNode = (TFBotNode) node;
			
			body = botNode.getValueSingular(TFBotNode.CLASSNAME).toString();
			
			if((String) botNode.getValueSingular(TFBotNode.NAME) != null) {
				body = body + "_" + (String) botNode.getValueSingular(TFBotNode.NAME) + "_";
			}
			keyset = popNode.getBotTemplateMap().keySet();
		}
		full = header + type + body;
		
		if(keyset.contains(full)) {
			boolean invalidName = true;
			
			while (invalidName) { //this sucks
				String suffix = Integer.toString(generated);
				
				if(keyset.contains(full + suffix)) {
					generated++;
				}
				else {
					full = full + suffix;
					invalidName = false;
				}
			}
		}
		return full;
	}
	
	//panel to manage ws templates
	public class WaveSpawnTemplatePanel extends EngiPanel {		
		DefaultListModel<String> wsTemplateListModel = new DefaultListModel<String>();
		JList<String> wsTemplateList = new JList<String>(wsTemplateListModel);
		
		//consider using shorter strings
		JButton addToWSList = new JButton("Create empty wavespawn template");
		JButton updateWSTemplate = new JButton("Update wavespawn template");
		JButton removeWSTemplate = new JButton("Remove wavespawn template");
		
		ButtonListManager wavespawnBLManager = new ButtonListManager(wsTemplateList, addToWSList,
				updateWSTemplate, removeWSTemplate);
		
		JLabel wsTemplateNameLabel = new JLabel("Wavespawn template name: ");
		JTextField wsTemplateNameField = new JTextField("", 20);
		
		//String currentWSTemplateName;
		
		public WaveSpawnTemplatePanel() {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.NORTHWEST;
			gb.insets = new Insets(0, 0, 5, 5);
			
			wsTemplateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			wavespawnBLManager.changeButtonState(States.EMPTY);
			
			updateWSTemplate.setToolTipText("The currently loaded spawner will also be included in the template");
			
			wsTemplateNameField.setMinimumSize(wsTemplateNameField.getPreferredSize());
			
			initWSTempListeners();
			
			addGB(wsTemplateNameLabel, 0, 0);
			addGB(wsTemplateNameField, 1, 0);
			addGB(addToWSList, 0 , 1);
			addGB(updateWSTemplate, 0 , 2);
			addGB(removeWSTemplate, 0 , 3);
			
			gb.gridheight = 3;
			addGB(wsTemplateList, 1, 1);
		}
		
		public void setListIndex(int index) {
			wsTemplateList.setSelectedIndex(index);
		}
		
		//ws and bot counterparts do essentially the same thing
		private void initWSTempListeners() {
			wsTemplateList.addListSelectionListener(event -> {
				int index = wsTemplateList.getSelectedIndex();
				
				listPanel.setSelectedButton(NONE);
				
				if(index != -1) {
					currentWSNode = popNode.getWSTemplateMap().get(wsTemplateList.getSelectedValue());
					//currentWSTemplateName = wsTemplateList.getSelectedValue(); 
					wsPanel.updatePanel(currentWSNode);
					wsTemplateNameField.setText(wsTemplateList.getSelectedValue());
					
					wavespawnBLManager.changeButtonState(States.SELECTED);
					
					if(currentWSNode.hasChildren()) {
						listPanel.checkSpawner(currentWSNode.getSpawner());
						listPanel.setButtonState(States.SELECTED);
						}
					else {
						listPanel.setSelectedButton(TFBOT);
						listPanel.setButtonState(States.EMPTY);
					}
				}
				else {
					wsTemplateNameField.setText("");
					listPanel.setButtonState(States.DISABLE);
					wavespawnBLManager.changeButtonState(States.EMPTY);
				}
			});
			
			//ignore panel, create fresh node
			addToWSList.addActionListener(event -> {
				feedback.setText("");
				currentWSNode = new WaveSpawnNode();
				//newTemplate.copyWaveSpawn(currentWSNode);
				
				String name = JOptionPane.showInputDialog("Enter a template name");
				
				if(popNode.getWSTemplateMap() == null) { //init ws template map if it doesn't exist
					popNode.setWSTemplateMap(new TreeMap<String, WaveSpawnNode>()); //forces no nulls and sorts
				}
				
				if(name != null && !name.isEmpty()) {
					if(popNode.getWSTemplateMap().containsKey(name)) { //overwrite check
						if(checkTemplateName(wsTemplateNameField.getText(), WAVESPAWN)) { 
							popNode.getWSTemplateMap().put(name, currentWSNode);
						} //does nothing if user selects no
					}
					else { //otherwise just shove it in
						popNode.getWSTemplateMap().put(name, currentWSNode);
					}
				}
				else { //generate a name if one isn't entered
					popNode.getWSTemplateMap().put(generateTemplateName(currentWSNode), currentWSNode);
				}
				wsTemplateList.setSelectedIndex(updateWSTemplateList());
			});
			
			//update currently selected template
			updateWSTemplate.addActionListener(event -> {
				boolean overwrite = false;
				String newName = wsTemplateNameField.getText();
				String oldName = wsTemplateList.getSelectedValue();
				
				wsPanel.updateNode(currentWSNode);
				
				//need to update and be able to remove nodes	
				
				if(!newName.isEmpty()) {
					if(!newName.equals(oldName)) { //user updated template name
						if(popNode.getWSTemplateMap().containsKey(newName)) { //named template with a name already in map
							if(checkTemplateName(newName, WAVESPAWN)) { 
								overwrite = true;
							}
						}
						else { //new name not already in map
							overwrite = true;
						}
						
						if(overwrite) {
							popNode.getWSTemplateMap().remove(oldName); //make sure old name isn't floating around
							oldName = newName;
							popNode.getWSTemplateMap().put(oldName, currentWSNode); //overwrites preexisting old if first case
							updateWSTemplateList();
						}
					}
					else { //otherwise just shove in
						feedback.setText("Template successfully updated");
						popNode.getWSTemplateMap().put(oldName, currentWSNode);
					}
				}
				else {
					feedback.setText("Failed to update template; name must be nonempty");
				}
			});
			
			removeWSTemplate.addActionListener(event -> {
				popNode.getWSTemplateMap().remove(wsTemplateList.getSelectedValue());
				wsTemplateListModel.removeElement(wsTemplateList.getSelectedValue()); 
				updateWSTemplateList(); //selection index should reset here
				wavespawnBLManager.changeButtonState(States.NOSELECTION);
				//check if no more templates
			});
		}
		
		//returns last index
		private int updateWSTemplateList() {
			wsTemplateListModel.clear();
			
			popNode.getWSTemplateMap().forEach((k, v) -> {
				wsTemplateListModel.addElement(k);
			});
			
			return wsTemplateListModel.size() - 1;
		}
	}
	
	//panel to manage bot templates
	public class TFBotTemplatePanel extends EngiPanel {
		DefaultListModel<String> botListModel = new DefaultListModel<String>();
		JList<String> botTemplateList = new JList<String>(botListModel);
		
		JButton addToBotList = new JButton("Save bot as template");
		JButton updateBotTemplate = new JButton("Update bot template");
		JButton removeBotTemplate = new JButton("Remove bot template");
		
		ButtonListManager botBLManager = new ButtonListManager(botTemplateList, addToBotList,
				updateBotTemplate, removeBotTemplate);
		
		JLabel botTemplateNameLabel = new JLabel("Bot template name: ");
		JTextField botTemplateNameField = new JTextField("", 15);
		
		String currentBotTemplateName;
		
		public TFBotTemplatePanel() {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.NORTHWEST;
			gb.insets = new Insets(0, 0, 5, 5);
			
			botTemplateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			botBLManager.changeButtonState(States.EMPTY);
			
			botTemplateNameField.setMinimumSize(botTemplateNameField.getPreferredSize());
			initBotTempListeners();
			
			addGB(botTemplateNameLabel, 0, 0);
			addGB(botTemplateNameField, 1, 0);
			addGB(addToBotList, 0 , 1);
			addGB(updateBotTemplate, 0 , 2);
			addGB(removeBotTemplate, 0 , 3);
			
			gb.gridheight = 3;
			addGB(botTemplateList, 1, 1); 
		}
		
		public void setListIndex(int index) {
			botTemplateList.setSelectedIndex(index);
		}
		
		//init all the listeners
		private void initBotTempListeners() {
			botTemplateList.addListSelectionListener(event -> {
				int index = botTemplateList.getSelectedIndex();
				
				if(index != -1) {
					currentBotNode = popNode.getBotTemplateMap().get(botTemplateList.getSelectedValue());
					currentBotTemplateName = botTemplateList.getSelectedValue(); 
					botPanel.updatePanel(currentBotNode);
					botTemplateNameField.setText(currentBotTemplateName);
					
					botBLManager.changeButtonState(States.SELECTED);
					botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode); //check for no odd side effects
				}
				else { //if deselected manually or via some list reset
					botTemplateNameField.setText("");
					botBLManager.changeButtonState(States.EMPTY);
				}
			});
			
			//make sure template nodes aren't the same obj as their origin node
			addToBotList.addActionListener(event -> {
				TFBotNode newTemplate = new TFBotNode();
				botPanel.updateNode(newTemplate);
				//newTemplate.setParent(templateParent); //fake parent to appease attributepanel
				//if(currentBotNode.getItemAttributeList() != null) { //copy attributes over
					//newTemplate.setItemAttributeList(new HashMap<ItemSlot, HashMap<String, String>>());
					//newTemplate.getItemAttributeList().putAll(currentBotNode.getItemAttributeList());
				//}
				
				String name = JOptionPane.showInputDialog("Enter a template name");
				
				if(popNode.getBotTemplateMap() == null) { //init bot template map if it doesn't exist
					popNode.setBotTemplateMap(new TreeMap<String, TFBotNode>()); //forces no nulls and sorts
				}
				
				if(!name.isEmpty()) {	
					if(popNode.getBotTemplateMap().containsKey(name)) { //overwrite check
						if(checkTemplateName(botTemplateNameField.getText(), TFBOT)) { //possibly unnecessary check
							popNode.getBotTemplateMap().put(name, newTemplate);
						} //does nothing if overwrite is false
					}
					else { //otherwise just shove it in
						popNode.getBotTemplateMap().put(name, newTemplate);
					}
				}
				else {
					popNode.getBotTemplateMap().put(generateTemplateName(newTemplate), newTemplate);
				}
				updateBotTemplateList();
				botPanel.updateTemplateModel(botListModel);
				//botBLManager.changeButtonState(States.SELECTED);
			});
			
			//update currently selected template
			updateBotTemplate.addActionListener(event -> {
				botPanel.updateNode(currentBotNode);
				boolean overwrite = false;
				
				String name = botTemplateNameField.getText();
				
				if(!name.isEmpty()) {
					if(!name.equals(currentBotTemplateName)) { //user updated template name
						if(popNode.getBotTemplateMap().containsKey(name)) { //named template with a name already in map
							if(checkTemplateName(name, TFBOT)) { 
								overwrite = true;
							} //otherwise do nothing
						}
						else { //new name not already in map
							overwrite = true;
						}
						
						if(overwrite) {
							popNode.getBotTemplateMap().remove(currentBotTemplateName); //make sure old name isn't floating around
							currentBotTemplateName = name;
							popNode.getBotTemplateMap().put(currentBotTemplateName, currentBotNode); //overwrites preexisting old if first case
							updateBotTemplateList();
						}
					}
					else { //otherwise just shove in
						feedback.setText("Template successfully updated");
						popNode.getBotTemplateMap().put(currentBotTemplateName, currentBotNode);
					}
				}
				else {
					feedback.setText("Failed to update template; name must be nonempty");
				}
			});
			
			removeBotTemplate.addActionListener(event -> {
				popNode.getBotTemplateMap().remove(botTemplateList.getSelectedValue());
				botListModel.removeElement(botTemplateList.getSelectedValue()); 
				updateBotTemplateList(); //selection index should reset here
				botPanel.updateTemplateModel(botListModel);
			});
		}
		
		//clear bot list, update it
		private void updateBotTemplateList() {
			botListModel.clear();
			
			popNode.getBotTemplateMap().forEach((k, v) -> {
				botListModel.addElement(k);
			});
			
			botTemplateList.setSelectedIndex(-1);
		}
	}
	
	//similar to the listpanel in mainwindow, but more specifically tailored to template
	//todo: do something about this being very similar to listpanel so there's less redundant code
	public class ListPanel extends EngiPanel {
		static final String noSpawner = "Current spawner type: none";
		static final String botSpawner = "Current spawner type: TFBot";
		static final String tankSpawner = "Current spawner type: tank";
		static final String squadSpawner = "Current spawner type: squad";
		static final String randomSpawner = "Current spawner type: randomchoice";	
		
		static final String updateBotMsg = "Update bot";
		static final String addBotMsg = "Add bot";
		static final String removeBotMsg = "Remove bot";
		
		static final String updateTankMsg = "Update tank";
		static final String addTankMsg = "Add tank";
		static final String removeTankMsg = "Remove tank";
		
		static final String addSquadMsg = "Add squad";
		static final String removeSquadMsg = "Remove squad";
		static final String addRandomMsg = "Add randomchoice";
		static final String removeRandomMsg = "Remove randomchoice";
		
		JLabel spawnerInfo = new JLabel("Current spawner: none");
		
		JRadioButton tfbotBut = new JRadioButton("TFBot");
		JRadioButton tankBut = new JRadioButton("Tank");
		JRadioButton squadBut = new JRadioButton("Squad");
		JRadioButton randomBut = new JRadioButton("RandomChoice");
		JRadioButton noneBut = new JRadioButton("none");
		
		JButton addSpawner = new JButton(addBotMsg);
		JButton updateSpawner = new JButton(updateBotMsg);
		JButton removeSpawner = new JButton(removeBotMsg);
		
		JButton addSquadRandomBot = new JButton(addBotMsg);
		JButton updateSquadRandomBot = new JButton(updateBotMsg);
		JButton removeSquadRandomBot = new JButton(removeBotMsg);
		
		DefaultListModel<String> squadRandomListModel = new DefaultListModel<String>();
		JList<String> squadRandomList = new JList<String>(squadRandomListModel);
		
		ButtonListManager spawnerBLManager = new ButtonListManager(squadRandomList, addSpawner, updateSpawner, removeSpawner);
		ButtonListManager squadRandomBLManager = new ButtonListManager(squadRandomList, addSquadRandomBot, updateSquadRandomBot, removeSquadRandomBot);
		
		public ListPanel() {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.NORTHWEST;
			gb.insets = new Insets(5, 0, 5, 5);
			
			squadRandomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			spawnerBLManager.changeButtonState(States.DISABLE);
			squadRandomBLManager.changeButtonState(States.DISABLE);
			
			addSpawner.setToolTipText("The spawner will be directly added to the wavespawn template; no template update is necessary");
			
			addSquadRandomBot.setVisible(false);
			updateSquadRandomBot.setVisible(false);
			removeSquadRandomBot.setVisible(false);
			
			initListeners();
			initSpawnerSelector();
			
			addGB(addSpawner, 0, 0);
			addGB(updateSpawner, 0, 1);
			addGB(removeSpawner, 0, 2);
			
			addGB(addSquadRandomBot, 0, 3);
			addGB(updateSquadRandomBot, 0, 4);
			addGB(removeSquadRandomBot, 0, 5);
			
			gb.gridheight = 3;
			addGB(squadRandomList, 1, 3);
		}
		
		public void setButtonState(States state) {
			spawnerBLManager.changeButtonState(state);
		}
		
		private void initListeners() { //inits all the button/list listeners
			addSpawner.addActionListener(new ActionListener() { //adds spawner when clicked
				public void actionPerformed(ActionEvent a) { //will need new contexts later
					switch (addSpawner.getText()) { //
						case (addBotMsg):
							botPanel.updateNode(currentBotNode);
							currentBotNode.connectNodes(currentWSNode);
							botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode); //other place where you can enter bot added state
							spawnerInfo.setText(botSpawner);
							feedback.setText("TFBot sucessfully added to wavespawn template");
							break;
						case (addTankMsg):
							tankPanel.updateNode(currentTankNode);
							currentTankNode.connectNodes(currentWSNode);
							spawnerInfo.setText(tankSpawner);
							feedback.setText("Tank successfully added to wavespawn template");
							break;
						case (addSquadMsg):
							currentSquadNode.connectNodes(currentWSNode);
							squadRandomBLManager.changeButtonState(States.EMPTY); //enable adding bot subnodes
							spawnerInfo.setText(squadSpawner);
							feedback.setText("Squad successfully added to wavespawn template");
							
							break;
						case (addRandomMsg):
							currentRCNode.connectNodes(currentWSNode);
							squadRandomBLManager.changeButtonState(States.EMPTY);
							
							spawnerInfo.setText(randomSpawner);
							feedback.setText("RandomChoice successfully added to wavespawn template");
						
							break;
					}
					spawnerBLManager.changeButtonState(States.SELECTED);
				}
			});
			
			removeSpawner.addActionListener(event -> { //remove current spawner from wavespawn
				currentWSNode.getChildren().clear();
				spawnerInfo.setText(noSpawner);
				tfbotBut.setSelected(true);
				spawnerBLManager.changeButtonState(States.EMPTY);
				feedback.setText("Spawner removed from template");
			});
			
			updateSpawner.addActionListener(new ActionListener() { //update current spawner
				//this also allows updating squad/rc, which doesn't do anything
				public void actionPerformed(ActionEvent a) {
					if(tankBut.isSelected()) {
						tankPanel.updateNode(currentTankNode);
						feedback.setText("Tank successfully updated");
					}
					else {
						botPanel.updateNode(currentBotNode);
						feedback.setText("Bot successfully updated");
					}	
				}
			});
			
			addSquadRandomBot.addActionListener(new ActionListener() { //squad/rc specific button for adding bots to them
				public void actionPerformed(ActionEvent a) {
					feedback.setText(" ");
					
					botPanel.updateNode(currentBotNode);
					if(squadBut.isSelected()) { //double check for the old spawner types
						currentBotNode.connectNodes(currentSquadNode);
					}
					else {
						currentBotNode.connectNodes(currentRCNode);
					}
					getSquadRandomList();
					loadBot(true);	
				}
			});
			
			updateSquadRandomBot.addActionListener(event -> { //squad/rc specific button to update bots
				botPanel.updateNode(currentBotNode);
				feedback.setText("Bot successfully updated");
				
				getSquadRandomList();
			});
			
			removeSquadRandomBot.addActionListener(new ActionListener() { //squad/rc button to remove bots from them
				public void actionPerformed(ActionEvent a) {
					if(squadRandomList.getSelectedIndex() != -1) {
						List<Node> list;
						
						if(squadBut.isSelected()) {
							list = currentSquadNode.getChildren();
						}
						else {
							list = currentRCNode.getChildren();
						}
						
						list.remove(squadRandomList.getSelectedIndex()); 
							
						getSquadRandomList();
						
						if(list.size() == 0) { //if no wavespawns again
							squadRandomBLManager.changeButtonState(States.EMPTY);
							loadBot(true);
						}
						else { 
							squadRandomBLManager.changeButtonState(States.NOSELECTION);
						}
					}
				}
			});
			
			squadRandomList.addListSelectionListener(new ListSelectionListener() { //list of squad/random's bots
				public void valueChanged(ListSelectionEvent l) {
					int squadRandomIndex = squadRandomList.getSelectedIndex();
					feedback.setText(" ");
					
					//prevent fits if index is reset
					if(squadRandomIndex != -1) {
						if(squadBut.isSelected()) {
							currentBotNode = (TFBotNode) currentSquadNode.getChildren().get(squadRandomIndex);
						}
						else {
							currentBotNode = (TFBotNode) currentRCNode.getChildren().get(squadRandomIndex);
						}
						loadBot(false, currentBotNode);
						squadRandomBLManager.changeButtonState(ButtonListManager.States.SELECTED);
					}
					else { //create a new bot 
						loadBot(true);
						squadRandomBLManager.changeButtonState(ButtonListManager.States.EMPTY);
					}
				}
			});
		}
		
		//select a spawner radiobutton
		public void setSelectedButton(int type) {
			switch(type) {
				case NONE:
					noneBut.setSelected(true);
					break;
				case TFBOT:
					tfbotBut.setSelected(true);
					break;
				case TANK:
					tankBut.setSelected(true);
					break;
				case SQUAD:
					squadBut.setSelected(true);
					break;
				case RANDOMCHOICE:
					randomBut.setSelected(true);
					break;
				default:
					break;
			}
		}
		
		public void checkSpawner(Node node) { //check what the wavespawn's spawner is
			if(node.getClass() == TFBotNode.class) {
				tfbotBut.setSelected(true);
				spawnerInfo.setText(botSpawner);
			}
			else if(node.getClass() == TankNode.class) {
				tankBut.setSelected(true);
				spawnerInfo.setText(tankSpawner);
			}
			else if(node.getClass() == SquadNode.class) {
				squadBut.setSelected(true);
				spawnerInfo.setText(squadSpawner);
			}
			else if(node.getClass() == RandomChoiceNode.class) {
				randomBut.setSelected(true);
				spawnerInfo.setText(randomSpawner);
			}
		}
		
		void loadBot(boolean newNode, Node node) { //if true, generate a new tfbot otherwise load the inputted node
			if(tfbotBut.isSelected()) {
				addSpawner.setText(addBotMsg);
				updateSpawner.setText(updateBotMsg);
				removeSpawner.setText(removeBotMsg);	
			}
			
			if(newNode) { //generate new tfbot
				currentBotNode = new TFBotNode();
				if(tfbotBut.isSelected()) {
					spawnerBLManager.changeButtonState(States.EMPTY);
				}
				botPanel.getAttributesPanel().setAttrToBotButtonsStates(false, false, States.DISABLE);//disable item attr editing if new node
			}
			else { //load inputted tfbot
				currentBotNode = (TFBotNode) node;
				if(tfbotBut.isSelected()) {
					spawnerBLManager.changeButtonState(States.SELECTED);
				}
				botPanel.getAttributesPanel().setAttrToBotButtonsStates(true, false, States.NOSELECTION);
			}
			botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode);
			botPanel.updatePanel(currentBotNode);
		}
		
		void loadBot(boolean newNode) { //if you're making a fresh node for ws don't need to specify linking 
			loadBot(newNode, null);
		}
		
		void loadTank(boolean newTank) { //load tank info or create a new tank node and set panel visibility
			addSpawner.setText(addTankMsg);
			updateSpawner.setText(updateTankMsg);
			removeSpawner.setText(removeTankMsg);
			
			if(newTank) {
				currentTankNode = new TankNode();
				spawnerBLManager.changeButtonState(ButtonListManager.States.EMPTY);
			}
			else {
				currentTankNode = (TankNode) currentWSNode.getSpawner();
				spawnerBLManager.changeButtonState(ButtonListManager.States.SELECTED);
			}
			tankPanel.updatePanel(currentTankNode);
		}
		
		void loadSquad(boolean newSquad) { //creates a new squad node if true and loads existing if false
			addSpawner.setText(addSquadMsg);
			//updateSpawner.setText(updateBotMsg);
			removeSpawner.setText(removeSquadMsg);
			
			if(newSquad) {
				currentSquadNode = new SquadNode();
				spawnerBLManager.changeButtonState(States.EMPTY);
				squadRandomBLManager.changeButtonState(States.DISABLE);
				loadBot(true);
			}
			else {
				currentSquadNode = (SquadNode) currentWSNode.getSpawner();
				spawnerBLManager.changeButtonState(States.SELECTED);
				if(currentSquadNode.hasChildren()) {
					loadBot(false, currentSquadNode.getChildren().get(0));
					getSquadRandomList();
					squadRandomBLManager.changeButtonState(States.NOSELECTION);
				}
				else { //only allow children removal if there are children to remove
					squadRandomBLManager.changeButtonState(States.EMPTY);
				}
			}
		}
		
		void loadRandom(boolean newRandom) { //same as above, just with rc
			addSpawner.setText(addRandomMsg);
			removeSpawner.setText(removeRandomMsg);
			
			if(newRandom) {
				currentRCNode = new RandomChoiceNode();
				spawnerBLManager.changeButtonState(States.EMPTY);
				squadRandomBLManager.changeButtonState(States.DISABLE);
				loadBot(true);
			}
			else {
				currentRCNode = (RandomChoiceNode) currentWSNode.getSpawner();
				spawnerBLManager.changeButtonState(States.SELECTED);
				if(currentRCNode.hasChildren()) {
					loadBot(false, currentRCNode.getChildren().get(0));
					getSquadRandomList();
					squadRandomBLManager.changeButtonState(States.NOSELECTION);
				}
				else {
					loadBot(true);
					squadRandomBLManager.changeButtonState(States.EMPTY);
				}
			}	
		}
		
		void initSpawnerSelector() { //inits the spawner related radio buttons		
			ButtonGroup spawnerGroup = new ButtonGroup();
			
			//spawnerPanel.add(noneBut); //unlike main, no spawner is a valid state
			spawnerPanel.add(tfbotBut);
			spawnerPanel.add(tankBut);
			spawnerPanel.add(squadBut);
			spawnerPanel.add(randomBut);	
			
			spawnerGroup.add(noneBut);
			spawnerGroup.add(tfbotBut);
			spawnerGroup.add(tankBut);
			spawnerGroup.add(squadBut);
			spawnerGroup.add(randomBut);
			spawnerGroup.setSelected(tfbotBut.getModel(), true);
			//todo: add mob and sentrygun here
			
			//radio buttons control visibility, button states only if selected button mismatches linked spawner
			
			tfbotBut.addItemListener(event -> {
				if(event.getStateChange() == ItemEvent.SELECTED) {
					try { //indexoutofbounds if no spawner
						Node node = currentWSNode.getSpawner();
						
						if(node.getClass() == TFBotNode.class) {
							//if currentWSNode has a tfbot, show it
							loadBot(false, node);
						}
						else {	
							spawnerBLManager.changeButtonState(ButtonListManager.States.REMOVEONLY);
						}
						
					}
					catch (IndexOutOfBoundsException e) {
						loadBot(true);
					}
				}
			});
			tankBut.addItemListener(event -> {
				if(event.getStateChange() == ItemEvent.SELECTED) {
					botPanel.setVisible(false);
					tankPanel.setVisible(true);
					
					try { //if currentwsnode doesn't have anything linked
						Node node = currentWSNode.getSpawner();
						
						//currentBotNode != null &&
						if(node.getClass() == TankNode.class) {
							//if currentWSNode has a tank, show it
							loadTank(false);
						}
						else {
							spawnerBLManager.changeButtonState(ButtonListManager.States.REMOVEONLY);
						}
					}
					catch (IndexOutOfBoundsException e) {
						loadTank(true);
					}
				}
				else {
					tankPanel.setVisible(false);
					botPanel.setVisible(true);
				}			
			});
			squadBut.addItemListener(event -> {
				if(event.getStateChange() == ItemEvent.SELECTED) { //only set srbot buttons visible in squad or random mode
					addSquadRandomBot.setVisible(true);
					updateSquadRandomBot.setVisible(true);
					removeSquadRandomBot.setVisible(true);
					
					updateSpawner.setVisible(false);
					
					try {
						Node node = currentWSNode.getSpawner();
						
						if(node.getClass() == SquadNode.class) {
							loadSquad(false);
						}
						else {
							spawnerBLManager.changeButtonState(States.REMOVEONLY); //allow squad node removal only, disable subnode tfbot editing
							squadRandomBLManager.changeButtonState(States.DISABLE);
						}
					}
					catch (IndexOutOfBoundsException e) {
						loadSquad(true);
					}	
				}
				else {
					addSquadRandomBot.setVisible(false);
					updateSquadRandomBot.setVisible(false);
					removeSquadRandomBot.setVisible(false);
					
					updateSpawner.setVisible(true);
					squadRandomListModel.clear();
				}					
			});
			randomBut.addItemListener(event -> { //functionally the same as squad, just with rc
				if(event.getStateChange() == ItemEvent.SELECTED) {
					addSquadRandomBot.setVisible(true);
					updateSquadRandomBot.setVisible(true);
					removeSquadRandomBot.setVisible(true);
					
					updateSpawner.setVisible(false);
					
					try {
						Node node = currentWSNode.getSpawner();
						
						if(node.getClass() == RandomChoiceNode.class) {
							loadRandom(false);
						}
						else {
							spawnerBLManager.changeButtonState(States.REMOVEONLY); //allow squad node removal only, disable subnode tfbot editing
							squadRandomBLManager.changeButtonState(States.DISABLE);
						}
					}
					catch (IndexOutOfBoundsException e) {
						loadRandom(true);
					}
				}
				else {
					addSquadRandomBot.setVisible(false);
					updateSquadRandomBot.setVisible(false);
					removeSquadRandomBot.setVisible(false);
					
					updateSpawner.setVisible(true);
					squadRandomListModel.clear();
				}
			});
		}

		void getSquadRandomList() { //could be either a squadnode or randomchoicenode
			int length;
			Node node = new Node();
			
			if(squadBut.isSelected()) {
				node = currentSquadNode;
			}
			else if(randomBut.isSelected()){
				node = currentRCNode;
			}
			length = node.getChildren().size(); //maybe have check if node is somehow null
			
			squadRandomListModel.clear();
			
			for(int i = 0; i < length; i++) {
				TFBotNode t = (TFBotNode) node.getChildren().get(i);
				if(t.getValueSingular(TFBotNode.NAME) != null) {
					squadRandomListModel.addElement((String) t.getValueSingular(TFBotNode.NAME)); //this is extremely awful
				}
				else {
					squadRandomListModel.addElement(t.getValueSingular(TFBotNode.CLASSNAME).toString());
					//classname is classname obj
				}		
			}
		}
		
	}
}
