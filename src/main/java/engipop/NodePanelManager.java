package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import engipop.ButtonListManager.States;
import engipop.EngiWindow.NoDeselectionModel;
import engipop.Node.PopNode;
import engipop.Node.RandomChoiceNode;
import engipop.Node.SpawnerType;
import engipop.Node.SquadNode;
import engipop.Node.TFBotNode;
import engipop.Node.TankNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;
import engipop.WaveBarPanel.BotType;

//buttons to interface with backend / select panel visibility
//split across two panels
public class NodePanelManager {
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
	
	static final String noSpawner = "Current spawner type: none";
	static final String botSpawner = "Current spawner type: TFBot";
	static final String tankSpawner = "Current spawner type: tank";
	static final String squadSpawner = "Current spawner type: squad";
	static final String randomSpawner = "Current spawner type: randomchoice";
	
	protected PopNode popNode;
	protected WaveNode currentWaveNode = new WaveNode();
	protected WaveSpawnNode currentWSNode = new WaveSpawnNode();
	protected TFBotNode currentBotNode = new TFBotNode();
	protected TankNode currentTankNode = new TankNode();
	protected SquadNode currentSquadNode = new SquadNode();
	protected RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	MainWindow mainWindow;
	BotPanel botPanel;
	TankPanel tankPanel;
	WaveBarPanel wavebar;
	EngiPanel listPanel = new EngiPanel();
	EngiPanel spawnerPanel = new EngiPanel();
	EngiPanel botTankPanel = new EngiPanel();
	
	JButton addSpawner = new JButton(addBotMsg);
	JButton updateSpawner = new JButton(updateBotMsg);
	JButton removeSpawner = new JButton(removeBotMsg);
	
	//essentially functions the same as the above 3, just manages squad/random's child bots
	JButton addSquadRandomBot = new JButton(addBotMsg);
	JButton updateSquadRandomBot = new JButton(updateBotMsg);
	JButton removeSquadRandomBot = new JButton(removeBotMsg);
	
	DefaultListModel<String> squadRandomListModel = new DefaultListModel<String>();
	JList<String> squadRandomList = new JList<String>(squadRandomListModel);
	JScrollPane squadRandomListScroll = new JScrollPane(squadRandomList);
	
	ButtonListManager spawnerBLManager = new ButtonListManager(addSpawner, updateSpawner, removeSpawner);
	ButtonListManager squadRandomBLManager = new ButtonListManager(addSquadRandomBot, updateSquadRandomBot, removeSquadRandomBot);
	
	JLabel spawnerInfo = new JLabel(noSpawner);
	
	JRadioButton tfbotBut = new JRadioButton("TFBot");
	JRadioButton tankBut = new JRadioButton("Tank");
	JRadioButton squadBut = new JRadioButton("Squad");
	JRadioButton randomBut = new JRadioButton("RandomChoice");
	JRadioButton noneBut = new JRadioButton("none");
	//hidden button to ensure the buttongroup state always changes, so can't go from say tfbot to tfbot, which wouldn't cause a state change
	
	//TODO: may want a better way of handling classes that use nodepanelmanager but don't need wavebar support
	public NodePanelManager(MainWindow mainWindow, PopulationPanel popPanel) {
		this(mainWindow, popPanel, null);
	}
	
	public NodePanelManager(MainWindow mainWindow, PopulationPanel popPanel, WaveBarPanel wavebar) {
		popNode = Engipop.getPopNode();
		this.mainWindow = mainWindow;
		botPanel = new BotPanel(mainWindow, popPanel);
		tankPanel = new TankPanel(popPanel);
		this.wavebar = wavebar;
		spawnerPanel.setBackground(botPanel.getBackground());
		listPanel.setBackground(new Color(240, 129, 73));
		
		spawnerInfo.setForeground(Color.CYAN);
		spawnerInfo.setFont(spawnerInfo.getFont().deriveFont(16f));
		
		squadRandomList.setSelectionModel(new NoDeselectionModel());
		squadRandomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		squadRandomList.setPrototypeCellValue("Giant Demoman");
		squadRandomListScroll.setMinimumSize(new Dimension(squadRandomList.getPreferredScrollableViewportSize().width, 
				squadRandomList.getPreferredScrollableViewportSize().height));
		//squadRandomListScroll.setMinimumSize(squadRandomList.getPreferredScrollableViewportSize());
		//System.out.println(squadRandomList.getPreferredScrollableViewportSize());
		
		//listAddWaveSpawn.setPreferredSize(new Dimension(159, 22));
		//+2 for padding or something
		
		squadRandomBLManager.changeButtonState(States.DISABLE);
		
		addSquadRandomBot.setVisible(false);
		updateSquadRandomBot.setVisible(false);
		removeSquadRandomBot.setVisible(false);
		squadRandomListScroll.setVisible(false);
		
		initListeners();
		
		listPanel.setLayout(listPanel.gbLayout);
		listPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		listPanel.gbConstraints.insets = new Insets(5, 0, 5, 5);
		
		botTankPanel.setLayout(botTankPanel.gbLayout);
		botTankPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		botTankPanel.gbConstraints.insets = new Insets(5, 0, 5, 5);
		
		tankPanel.setVisible(false);
		
		listPanel.addGB(addSpawner, 0, 0);
		listPanel.addGB(updateSpawner, 0, 1);
		listPanel.addGB(removeSpawner, 0, 2);
		
		listPanel.addGB(addSquadRandomBot, 0, 3);
		listPanel.addGB(updateSquadRandomBot, 0, 4);
		listPanel.addGB(removeSquadRandomBot, 0, 5);
		
		listPanel.gbConstraints.gridheight = 3;
		listPanel.addGB(squadRandomListScroll, 1, 0);
		
		botTankPanel.addGB(botPanel, 0, 0);
		botTankPanel.addGB(tankPanel, 0, 0);
		
		spawnerPanel.add(tfbotBut);
		spawnerPanel.add(tankBut);
		spawnerPanel.add(squadBut);
		spawnerPanel.add(randomBut);
		spawnerPanel.add(spawnerInfo);
	}
	
	//will need new contexts later
	protected void initListeners() { //inits all the button/list listeners
		addSpawner.addActionListener(event -> {
			mainWindow.setFeedback(" ");
			spawnerBLManager.changeButtonState(States.FILLEDSLOT);
			botTankPanel.getDisabledPanel().setEnabled(true);
			spawnerPanel.getDisabledPanel().setEnabled(true);
			
			switch (addSpawner.getText()) {
				//TODO: get the current wsnode from whatever parent window
				case (addBotMsg):
					//botPanel.updateNode(currentBotNode);
					currentBotNode = new TFBotNode();
					currentBotNode.connectNodes(currentWSNode);
					spawnerInfo.setText(botSpawner);
					//window.feedback.setText("Bot successfully created");
					if(wavebar != null) {
						int count = (Integer) currentWSNode.getValue(WaveSpawnNode.TOTALCOUNT);
						wavebar.modifyIcon(currentBotNode, count, BotType.COMMON, true); //by default new tfbots are just scouts
					}
					break;
				case (addTankMsg):
					//tankPanel.updateNode(currentTankNode);
					currentTankNode = new TankNode();
					currentTankNode.connectNodes(currentWSNode);
					spawnerInfo.setText(tankSpawner);
					
					if(wavebar != null) {
						int count = (Integer) currentWSNode.getValue(WaveSpawnNode.TOTALCOUNT);
						wavebar.addIcon("tank", false, count, BotType.GIANT); //may need to check support tanks as well
					}
					//window.feedback.setText("Tank successfully created");
					break;
				case (addSquadMsg): //for squadrc handle their wavebars when bots actually get added
					currentSquadNode = new SquadNode();
					currentSquadNode.connectNodes(currentWSNode);
					squadRandomBLManager.changeButtonState(States.EMPTY); //enable adding bot subnodes
					
					spawnerInfo.setText(squadSpawner);
					//window.feedback.setText("Squad successfully created");
					//loadBot(true);
					
					break;
				case (addRandomMsg):
					currentRCNode = new RandomChoiceNode();
					currentRCNode.connectNodes(currentWSNode);
					squadRandomBLManager.changeButtonState(States.EMPTY);
					
					spawnerInfo.setText(randomSpawner);
					//window.feedback.setText("RandomChoice successfully created");
				
					break;
			}
		});
		
		updateSpawner.addActionListener(event -> { //update current spawner
			if(tankBut.isSelected()) {
				tankPanel.updateNode(currentTankNode);
				mainWindow.setFeedback("Tank successfully updated");
			}
			else {
				botPanel.updateNode(currentBotNode);
				mainWindow.setFeedback("Bot successfully updated");
				
				if(wavebar != null) {
					wavebar.rebuildWavebar((WaveNode) currentWSNode.getParent());
				}
			}
		});
		
		removeSpawner.addActionListener(event -> { //remove current spawner from wavespawn
			if(wavebar != null) {
				int count = (Integer) currentWSNode.getValue(WaveSpawnNode.TOTALCOUNT);
				
				if(tfbotBut.isSelected()) {
					wavebar.modifyIcon(currentBotNode, count, null, false);
				}
				else if(tankBut.isSelected()) {
					boolean support = (boolean) currentWSNode.getValue(WaveSpawnNode.SUPPORT);
					BotType type = support == true ? BotType.COMMON : BotType.GIANT;
					wavebar.removeIcon("tank", false, count, type);
				}
				else if(squadBut.isSelected()) {
					int spawnCount = (int) currentWSNode.getValue(WaveSpawnNode.SPAWNCOUNT);
					int batches = count / spawnCount;
					
					for(Node node : currentSquadNode.getChildren()) {
						TFBotNode bot = (TFBotNode) node;
						
						wavebar.modifyIcon(bot, batches, null, false);
					}
				}
			}
			currentWSNode.getChildren().clear();
			spawnerInfo.setText(noSpawner);
			if(randomBut.isSelected() && wavebar != null) {
				//rebuild rc due to nature of rc
				wavebar.rebuildWavebar((WaveNode) currentWSNode.getParent());
			}
			if(randomBut.isSelected() || squadBut.isSelected()) {
				squadRandomListModel.clear();
				squadRandomBLManager.changeButtonState(States.DISABLE);
			}
			
			spawnerBLManager.changeButtonState(States.EMPTY);
			botTankPanel.getDisabledPanel().setEnabled(false);
			//spawnerPanel.getDisabledPanel().setEnabled(false);
		});
		
		addSquadRandomBot.addActionListener(event -> { //squad/rc specific button for adding bots to them
			mainWindow.setFeedback(" ");
			
			//botPanel.updateNode(currentBotNode);
			currentBotNode = new TFBotNode();
			
			if(squadBut.isSelected()) { //double check for the old spawner times
				currentBotNode.connectNodes(currentSquadNode);
			}
			else {
				currentBotNode.connectNodes(currentRCNode);
			}
			//getSquadRandomList();
			
			setSquadRandomListElement(currentBotNode);
			squadRandomList.setSelectedIndex(squadRandomListModel.size() - 1);
			
			//loadBot(true);
			if(wavebar != null) {
				int count = (Integer) currentWSNode.getValue(WaveSpawnNode.TOTALCOUNT);
				int spawnCount = (int) currentWSNode.getValue(WaveSpawnNode.SPAWNCOUNT);
				int batches = count / spawnCount;
				
				wavebar.modifyIcon(currentBotNode, batches, BotType.COMMON, true); //by default new tfbots are just scouts
			}
		});
		
		updateSquadRandomBot.addActionListener(event -> { //squad/rc specific button to update bots
			botPanel.updateNode(currentBotNode);
			mainWindow.setFeedback("Bot successfully updated");
			
			//similar logic to setsquadrandomlistelement but uses set instead of addelement to update
			if(currentBotNode.containsKey(TFBotNode.NAME)) {
				squadRandomListModel.set(squadRandomList.getSelectedIndex(), (String) currentBotNode.getValue(TFBotNode.NAME));
			}
			else if(currentBotNode.containsKey(TFBotNode.TEMPLATE)) {
				squadRandomListModel.set(squadRandomList.getSelectedIndex(), (String) currentBotNode.getValue(TFBotNode.TEMPLATE));
			}
			else {
				squadRandomListModel.set(squadRandomList.getSelectedIndex(), currentBotNode.getValue(TFBotNode.CLASSNAME).toString());
			}
			
			if(wavebar != null) {
				wavebar.rebuildWavebar((WaveNode) currentWSNode.getParent());
			}
		});
			
		removeSquadRandomBot.addActionListener(event -> { //squad/rc button to remove bots from them
			if(squadRandomList.getSelectedIndex() != -1) {
				List<Node> list;
				
				if(squadBut.isSelected()) {
					list = currentSquadNode.getChildren();
				}
				else {
					list = currentRCNode.getChildren();
				}
				
				list.remove(squadRandomList.getSelectedIndex()); 
				squadRandomListModel.remove(squadRandomList.getSelectedIndex());
				
				if(wavebar != null) {
					int count = (Integer) currentWSNode.getValue(WaveSpawnNode.TOTALCOUNT);
					int spawnCount = (int) currentWSNode.getValue(WaveSpawnNode.SPAWNCOUNT);
					int batches = count / spawnCount;
					
					wavebar.modifyIcon(currentBotNode, batches, null, false); //by default new tfbots are just scouts
				}
				//getSquadRandomList();
				
				if(list.size() == 0) { //if no wavespawns again
					squadRandomBLManager.changeButtonState(States.EMPTY);
					loadBot(true);
				}
				else { 
					squadRandomList.setSelectedIndex(list.size() - 1);
				}
			}
		});
		
		squadRandomList.addListSelectionListener(event -> { //list of squad/random's bots
			int squadRandomIndex = squadRandomList.getSelectedIndex();
			mainWindow.setFeedback(" ");
			
			//prevent fits if index is reset
			if(squadRandomIndex != -1) {
				Node currentParent = squadBut.isSelected() ? currentSquadNode : currentRCNode;
				if(currentParent.getChildren().get(squadRandomIndex).getClass() == TFBotNode.class) {
					currentBotNode = (TFBotNode) currentParent.getChildren().get(squadRandomIndex);
					loadBot(false, currentBotNode);
				}
				else {
					mainWindow.setFeedback("Possible nested randomchoice/squad! Unable to load");
				}
				
				squadRandomBLManager.changeButtonState(States.SELECTED);
			}
			else { //create a new bot 
				loadBot(true);
				squadRandomBLManager.changeButtonState(States.EMPTY);
			}
		});
		
		ButtonGroup spawnerGroup = new ButtonGroup();
		
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
				//try { //indexoutofbounds if no spawner
					Node node = currentWSNode.getSpawner();
					
					if(node == null) {
						loadBot(true);
					}
					else if(node.getClass() == TFBotNode.class) {
						//if currentWSNode has a tfbot, show it
						loadBot(false, node);
					}
					else {	
						spawnerBLManager.changeButtonState(States.REMOVEONLY);
					}
					
				//}
				//catch (IndexOutOfBoundsException e) {
				//	loadBot(true);
				//}
			}
		});
		tankBut.addItemListener(event -> {
			if(event.getStateChange() == ItemEvent.SELECTED) {
				tankPanel.setVisible(true);
				botPanel.setVisible(false);

				try { //if currentwsnode doesn't have anything linked
					Node node = currentWSNode.getSpawner();
					
					if(node == null) {
						loadTank(true);
					}
					else if(node.getClass() == TankNode.class) {
						//if currentWSNode has a tank, show it
						loadTank(false);
					}
					else {
						spawnerBLManager.changeButtonState(States.REMOVEONLY);
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
				squadRandomListScroll.setVisible(true);
				
				try {
					Node node = currentWSNode.getSpawner();
					
					if(node == null) {
						loadSquad(true);
					}
					else if(node.getClass() == SquadNode.class) {
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
				squadRandomListScroll.setVisible(false);
			}					
		});
		randomBut.addItemListener(event -> { //functionally the same as squad, just with rc
			if(event.getStateChange() == ItemEvent.SELECTED) {
				addSquadRandomBot.setVisible(true);
				updateSquadRandomBot.setVisible(true);
				removeSquadRandomBot.setVisible(true);
				
				updateSpawner.setVisible(false);
				squadRandomListScroll.setVisible(true);
				
				try {
					Node node = currentWSNode.getSpawner();
					
					if(node == null) {
						loadRandom(true);
					}
					else if(node.getClass() == RandomChoiceNode.class) {
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
				squadRandomListScroll.setVisible(false);
			}
		});
	}
	
	void checkSpawner(Node node) { //check what the wavespawn's spawner is
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
	
	void loadBot(boolean newNode, Node node) { //if true, generate a new tfbot, otherwise load the input node
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
		}
		else { //load input tfbot
			currentBotNode = (TFBotNode) node;
			if(tfbotBut.isSelected()) {
				spawnerBLManager.changeButtonState(States.FILLEDSLOT);
			}
		}
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
			spawnerBLManager.changeButtonState(States.EMPTY);
		}
		else {
			currentTankNode = (TankNode) currentWSNode.getSpawner();
			spawnerBLManager.changeButtonState(States.FILLEDSLOT);
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
			spawnerBLManager.changeButtonState(States.FILLEDSLOT);
			if(currentSquadNode.hasChildren()) {
				if(currentSquadNode.getChildren().get(0).getClass() == TFBotNode.class) {
					//indexoutbounds?
					loadBot(false, currentSquadNode.getChildren().get(0));
					getSquadRandomList();
				}
				else {
					mainWindow.setFeedback("Possible nested randomchoice/squad! Unable to load");
				}
				squadRandomBLManager.changeButtonState(States.SELECTED);
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
			spawnerBLManager.changeButtonState(States.FILLEDSLOT);
			if(currentRCNode.hasChildren()) {
				if(currentRCNode.getChildren().get(0).getClass() == TFBotNode.class) {
					loadBot(false, currentRCNode.getChildren().get(0));
					getSquadRandomList();
				}
				else {
					mainWindow.setFeedback("Possible nested randomchoice/squad! Unable to load");
				}
				squadRandomBLManager.changeButtonState(States.NOSELECTION);
			}
			else {
				loadBot(true);
				squadRandomBLManager.changeButtonState(States.EMPTY);
			}
		}	
	}
	
	protected void resetSpawnerState() {
		spawnerInfo.setText(noSpawner);
		tfbotBut.setSelected(true);
		spawnerBLManager.changeButtonState(States.DISABLE);
		botTankPanel.getDisabledPanel().setEnabled(false);
		spawnerPanel.getDisabledPanel().setEnabled(false);
	}		
	
	//refresh entire squadrandom list
	protected void getSquadRandomList() { //could be either a squadnode or randomchoicenode
		Node node = new Node();
		
		if(squadBut.isSelected()) {
			node = currentSquadNode;
		}
		else if(randomBut.isSelected()){
			node = currentRCNode;
		}
		squadRandomListModel.clear();
		
		for(Node botNode : node.getChildren()) {
			setSquadRandomListElement((TFBotNode) botNode);
		}
		squadRandomList.setSelectedIndex(0);
	}
	
	//refresh singular element on squadrandom list
	protected void setSquadRandomListElement(TFBotNode node) {
		if(node.containsKey(TFBotNode.NAME)) {
			squadRandomListModel.addElement((String) node.getValue(TFBotNode.NAME)); //this sucks
		}
		else if(node.containsKey(TFBotNode.TEMPLATE)) {
			squadRandomListModel.addElement((String) node.getValue(TFBotNode.TEMPLATE));
		}
		else if(node.containsKey(TFBotNode.CLASSNAME)) {
			squadRandomListModel.addElement(node.getValue(TFBotNode.CLASSNAME).toString());
		}
		else {
			squadRandomListModel.addElement("Non TFBot spawner");
		}
	}
	
	//various panel state updates
	public void setSelectedButton(SpawnerType type) {
		switch(type) {
			case NONE:
				tfbotBut.setSelected(true); //default to tfbot if no spawner
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
	
	public void setButtonState(States state) {
		spawnerBLManager.changeButtonState(state);
	}
	
	public void setWSNode(WaveSpawnNode currentWSNode) {
		this.currentWSNode = currentWSNode;
	}
	
	public JPanel getListPanel() {
		return this.listPanel;
	}
	
	public EngiPanel getSpawnerPanel() {
		return this.spawnerPanel;
	}
	
	public EngiPanel getBotTankPanel() {
		return botTankPanel;
	}
	
	public BotPanel getBotPanel() {
		return botPanel;
	}
}
