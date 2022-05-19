package engipop;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import engipop.Tree.*;
import engipop.Tree.TFBotNode.TFBotKeys;

public class window {
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
	
	JFrame frame = new JFrame("Engipop main");
	GridBagConstraints constraints = new GridBagConstraints();
	GridBagLayout frameGB = new GridBagLayout();
	
	static URL iconURL = window.class.getResource("/icon.png");
	static ImageIcon icon = new ImageIcon(iconURL);
	//same icon across all windows
	
	JMenuBar menuBar = new JMenuBar();
	JMenu options = new JMenu("Options");
	JMenuItem popSet = new JMenuItem("Open population settings");
	JMenuItem settings = new JMenuItem("Open Engipop settings");
	
	BotPanel botPanel;
	WaveSpawnPanel wsPanel;
	TankPanel tankPanel;
	WavePanel wavePanel;
	
	JPanel spawnerPanel;
	JPanel listPanel = new JPanel();
	
	JButton addBot = new JButton(addBotMsg);
	JButton removeBot = new JButton(removeBotMsg);
	JButton updateBot = new JButton(updateBotMsg);
	JRadioButton tfbotBut;
	JRadioButton tankBut;
	JRadioButton squadBut;
	JRadioButton randomBut;
	JButton addSquadRandom;
	JButton removeSquadRandom;
	
	ButtonListManager waveBLManager;
	ButtonListManager waveSpawnBLManager;
	ButtonListManager squadRandomBLManager;
	
	DefaultListModel<String> waveListModel = new DefaultListModel<String>();
	DefaultListModel<String> waveSpawnListModel = new DefaultListModel<String>();
	DefaultListModel<String> squadRandomListModel = new DefaultListModel<String>();
	
	PopNode popNode = new PopNode(); //minimum working pop
	
	WaveNode currentWaveNode = new WaveNode();
	WaveSpawnNode currentWSNode = new WaveSpawnNode();
	TFBotNode currentBotNode = new TFBotNode();
	TankNode currentTankNode = new TankNode();
	SquadNode currentSquadNode = new SquadNode();
	RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	JLabel feedback = new JLabel(" ");
	JLabel spawnerInfo = new JLabel(noSpawner);
	
	MapInfo info = new MapInfo();
	
	Tree tree = new Tree(popNode);
	ItemParser itemParser;
	
	public window() {
		frame.setIconImage(icon.getImage());
		
		frame.setLayout(frameGB);
		frame.setSize(1300, 1000);
		
		options.add(settings);
		options.add(popSet);
		menuBar.add(options);
		frame.setJMenuBar(menuBar);
		
		constraints.anchor = GridBagConstraints.WEST;
		
		botPanel = new BotPanel(this);
		wsPanel = new WaveSpawnPanel();
		wavePanel = new WavePanel();
		tankPanel = new TankPanel();
		
		currentWSNode.setName("default");
		
		currentWaveNode.connectNodes(popNode);
		currentWSNode.connectNodes(currentWaveNode);
		
		makeListPanel();
		
		spawnerSelector();
		spawnerInfo.setPreferredSize(new Dimension(179, 14));

		constraints.insets = new Insets(5, 0, 0, 0);
		
		addGB(frame, constraints, feedback, 0, 1);
		constraints.anchor = GridBagConstraints.NORTHWEST;
		
		addGB(frame, constraints, spawnerPanel, 0, 4);
		addGB(frame, constraints, spawnerInfo, 1, 4);
		
		constraints.gridwidth = 2;
		
		addGB(frame, constraints, wsPanel, 0, 3);
		addGB(frame, constraints, botPanel, 0, 5);
		addGB(frame, constraints, tankPanel, 0, 5);
		tankPanel.setVisible(false);
		
		constraints.gridwidth = 3;
		addGB(frame, constraints, wavePanel, 0, 2);
		
		constraints.gridwidth = 1;
		constraints.gridheight = 3;
		addGB(frame, constraints, listPanel, 2, 3);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    //
		}
		
		window w = new window();
		SecondaryWindow w2 = new SecondaryWindow(w.getPopNode(), w);
		SettingsWindow setW = new SettingsWindow(w);
		
		w.initConfig(w.getFrame(), setW);
		
		w.listen(w2, setW); //could change this
		
	}
	
	JFrame getFrame() {
		return this.frame;
	}
	
	public PopNode getPopNode() {
		return this.popNode;
	}
	
	public TFBotNode getCurrentBotNode() {
		return this.currentBotNode;
	}
	
	//listeners that interact with other windows
	void listen(SecondaryWindow w, SettingsWindow sw) {
		popSet.addActionListener(event -> {
			w.updatePopPanel();
			w.setVisible(true);
		});
		settings.addActionListener(event -> {
			if(!sw.isVisible()) {
				sw.setVisible(true);
			}
		});
	}
	
	void updateWaveSpawn(int index) { //refreshes the list, updates the current node, updates the panel
		getWaveSpawnList();
		currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(index);
		wsPanel.updatePanel(currentWSNode);
	}
	
	void loadBot(boolean newNode, Node node) { //if true, generate a new tfbot otherwise load the inputted node
		if(tankPanel.isVisible()) {
			tankPanel.setVisible(false);
			botPanel.setVisible(true);
		}
		if(tfbotBut.isSelected()) { //don't clear if in squadrandom
			squadRandomListModel.clear();
			addSquadRandom.setVisible(false);
			removeSquadRandom.setVisible(false);
		}
		
		if(newNode) { //generate new tfbot
			currentBotNode = new TFBotNode();
			addBot.setText(addBotMsg);
			squadRandomBLManager.changeButtonState(ButtonListManager.States.EMPTY);
			botPanel.getAttributesPanel().setAttrToBotButtonsStates(false, false, ButtonListManager.States.DISABLE);
		}
		else { //load inputted tfbot
			if(tfbotBut.isSelected()) {
				squadRandomBLManager.changeButtonState(ButtonListManager.States.SELECTED);
			}
			else { //let squadrandom add new bots
				squadRandomBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
			}
			currentBotNode = (TFBotNode) node;
			updateBot.setText(updateBotMsg);
			removeBot.setText(removeBotMsg);
			
			botPanel.getAttributesPanel().setAttrToBotButtonsStates(true, false, ButtonListManager.States.NOSELECTION);		
		}
		botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode);
		botPanel.setAttrNone(); //default to not showing any specific attribute
		botPanel.updatePanel(currentBotNode);
	}
	
	void loadBot(boolean newNode) { //if you're making a fresh node for ws don't need to specify linking 
		loadBot(newNode, null);
	}
	
	void loadTank(boolean newTank) { //load tank info or create a new tank node and set panel visibility
		botPanel.setVisible(false);
		tankPanel.setVisible(true);
		squadRandomListModel.clear();
		addSquadRandom.setVisible(false);
		removeSquadRandom.setVisible(false);
		
		if(newTank) {
			currentTankNode = new TankNode();
			addBot.setText(addTankMsg);
			squadRandomBLManager.changeButtonState(ButtonListManager.States.EMPTY);
		}
		else {
			currentTankNode = (TankNode) currentWSNode.getSpawner();
			squadRandomBLManager.changeButtonState(ButtonListManager.States.SELECTED);
			updateBot.setText(updateTankMsg);
			removeBot.setText(removeTankMsg);
		}
		tankPanel.updatePanel(currentTankNode);
	}
	
	void loadSquad(boolean newSquad) { //creates a new squad node if true and loads existing if false
		if(tankPanel.isVisible()) {
			tankPanel.setVisible(false);
			botPanel.setVisible(true);
		}
		addSquadRandom.setVisible(true);
		addSquadRandom.setText(addSquadMsg);
		removeSquadRandom.setVisible(true);
		removeSquadRandom.setText(removeSquadMsg);
		
		if(newSquad) {
			currentSquadNode = new SquadNode();
			addSquadRandom.setEnabled(true);
			removeSquadRandom.setEnabled(false);
			loadBot(true);
		}
		else {
			currentSquadNode = (SquadNode) currentWSNode.getSpawner();
			addSquadRandom.setEnabled(false);
			removeSquadRandom.setEnabled(true);
			if(currentSquadNode.hasChildren()) {
				loadBot(false, currentSquadNode.getChildren().get(0));
			}
		}
		getSquadRandomList();
	}
	
	void loadRandom(boolean newRandom) {
		if(tankPanel.isVisible()) {
			tankPanel.setVisible(false);
			botPanel.setVisible(true);
		}
		addSquadRandom.setVisible(true);
		addSquadRandom.setText(addRandomMsg);
		removeSquadRandom.setVisible(true);
		removeSquadRandom.setText(removeRandomMsg);
		
		if(newRandom) {
			currentRCNode = new RandomChoiceNode();
			addSquadRandom.setEnabled(true);
			removeSquadRandom.setEnabled(false);
			loadBot(true);
		}
		else {
			currentRCNode = (RandomChoiceNode) currentWSNode.getSpawner();
			addSquadRandom.setEnabled(false);
			removeSquadRandom.setEnabled(true);
			if(currentRCNode.hasChildren()) {
				loadBot(false, currentRCNode.getChildren().get(0));
			}		
		}
		getSquadRandomList();
	}
	
	void checkSpawner(Node node) { //check what the wavespawn's spawner is
		if(node.getClass() == TFBotNode.class) {
			loadBot(false, node);
			tfbotBut.setSelected(true);
			spawnerInfo.setText(botSpawner);
		}
		else if(node.getClass() == TankNode.class) {
			loadTank(false);
			tankBut.setSelected(true);
			spawnerInfo.setText(tankSpawner);
		}
		else if(node.getClass() == SquadNode.class) {
			loadSquad(false);
			squadBut.setSelected(true);
			spawnerInfo.setText(squadSpawner);
		}
		else if(node.getClass() == RandomChoiceNode.class) {
			loadRandom(false);
			randomBut.setSelected(true);
			spawnerInfo.setText(randomSpawner);
		}
	}
	
	void spawnerSelector() {
		spawnerPanel = new JPanel();
		
		ButtonGroup spawnerGroup = new ButtonGroup();
		
		spawnerPanel.add(tfbotBut = new JRadioButton("TFBot"));
		spawnerPanel.add(tankBut = new JRadioButton("Tank"));
		spawnerPanel.add(squadBut = new JRadioButton("Squad"));
		spawnerPanel.add(randomBut = new JRadioButton("RandomChoice"));	
		
		spawnerGroup.add(tfbotBut);
		spawnerGroup.add(tankBut);
		spawnerGroup.add(squadBut);
		spawnerGroup.add(randomBut);
		spawnerGroup.setSelected(tfbotBut.getModel(), true);
		//todo: add mob and sentrygun here
		
		tfbotBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent c) {
				try { //indexoutofbounds if no spawner
					Node node = currentWSNode.getSpawner();
					
					if(node.getClass() == TFBotNode.class) {
						//if currentWSNode has a tfbot, show it
						loadBot(false, node);
					}
					else {
						//hide tank panel
						if(tankPanel.isVisible()) {
							tankPanel.setVisible(false);
							botPanel.setVisible(true);
						}
						
						//if squadrandom, leave buttons visible so it can be removed
						if(node.getClass() == SquadNode.class || node.getClass() == RandomChoiceNode.class) {
							addSquadRandom.setVisible(true);
							removeSquadRandom.setVisible(true);
						}
						else { //otherwise hide the buttons and enable removebot still
							addSquadRandom.setVisible(false);
							removeSquadRandom.setVisible(false);
							
							removeBot.setEnabled(true);
						}
						squadRandomListModel.clear();
						
						addBot.setEnabled(false);
						updateBot.setEnabled(false);
					}
					
				}
				catch (IndexOutOfBoundsException e) {
					loadBot(true);
				}
			}
		});
		tankBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				addSquadRandom.setVisible(false);
				
				try { //if currentwsnode doesn't have anything linked
					Node node = currentWSNode.getChildren().get(0);
					
					//currentBotNode != null &&
					if(node.getClass() == TankNode.class) {
						//if currentWSNode has a tank, show it
						loadTank(false);
					}
					else { 
						botPanel.setVisible(false);
						tankPanel.setVisible(true);
						
						//if squadrandom, leave buttons visible so it can be removed
						if(node.getClass() == SquadNode.class || node.getClass() == RandomChoiceNode.class) {
							addSquadRandom.setVisible(true);
							removeSquadRandom.setVisible(true);
						}
						else { //otherwise hide the buttons and enable removebot still
							addSquadRandom.setVisible(false);
							removeSquadRandom.setVisible(false);
							
							removeBot.setEnabled(true);
						}
						squadRandomListModel.clear();
						
						addBot.setEnabled(false);
						updateBot.setEnabled(false);
					}
				}
				catch (IndexOutOfBoundsException e) {
					loadTank(true);
				}			
			}
		});
		squadBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				try {
					Node node = currentWSNode.getSpawner();
					
					if(node.getClass() == SquadNode.class) {
						loadSquad(false);
					}
					else { //as of now only randomchoice and tank/tfbot down here
						if(tankPanel.isVisible()) {
							tankPanel.setVisible(false);
							botPanel.setVisible(true);
						}
						
						if(node.getClass() == TFBotNode.class || node.getClass() == TankNode.class) {
							removeBot.setEnabled(true);
							removeSquadRandom.setEnabled(false);
						}
						else {
							removeBot.setEnabled(false);
							removeSquadRandom.setEnabled(true);
						}
						squadRandomListModel.clear(); 
						
						//getSquadRandomList();
						addSquadRandom.setVisible(true);
						addSquadRandom.setEnabled(false);
						removeSquadRandom.setVisible(true);
						
						
						addBot.setEnabled(false); //for now, disable overwriting nodes
						updateBot.setEnabled(false);
					}
				}
				catch (IndexOutOfBoundsException e) {
					loadSquad(true);
				}		
			}
		});
		randomBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				try {
					Node node = currentWSNode.getSpawner();
					
					if(node.getClass() == RandomChoiceNode.class) {
						loadRandom(false);
					}
					else {
						if(tankPanel.isVisible()) {
							tankPanel.setVisible(false);
							botPanel.setVisible(true);
						}
						
						if(node.getClass() == TFBotNode.class || node.getClass() == TankNode.class) {
							removeBot.setEnabled(true);
							removeSquadRandom.setEnabled(false);
						}
						else {
							removeBot.setEnabled(false);
							removeSquadRandom.setEnabled(true);
						}
						squadRandomListModel.clear(); 
						
						//getSquadRandomList();
						addSquadRandom.setVisible(true);
						//addSquadRandom.setEnabled(false);
						removeSquadRandom.setVisible(true);
						
						addBot.setEnabled(false); //for now, disable overwriting nodes
						updateBot.setEnabled(false);
					}
				}
				catch (IndexOutOfBoundsException e) {
					loadRandom(true);
				}	
			}
		});
	}
	
	void makeListPanel() { //panel for all the lists and various buttons to edit them
		listPanel.setLayout(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.anchor = GridBagConstraints.NORTHWEST;
		gb.insets = new Insets(5, 0, 5, 5);
		
		String addWaveMsg = "Add wave";
		String addWSMsg = "Add wavespawn"; //to population?
		
		//String updateSquadMsg = "Update squad";
		
		JList<String> listWaveList = new JList<String>(waveListModel);
		listWaveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JLabel listCurrentWave = new JLabel("Editing wave 1");
		JButton addWave = new JButton(addWaveMsg);
		JButton removeWave = new JButton("Remove wave");
		JButton updateWave = new JButton("Update wave");
		waveBLManager = new ButtonListManager(listWaveList, addWave, updateWave, removeWave);
		
		JLabel listCurrentWSLabel = new JLabel("Editing " + currentWSNode.getName());
		
		JList<String> waveSpawnList = new JList<String>(waveSpawnListModel);
		waveSpawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		JButton listAddWaveSpawn = new JButton(addWSMsg);
		//listAddWaveSpawn.setPreferredSize(new Dimension(159, 22));
		//+2 for padding or something
		
		JButton listRemoveWaveSpawn = new JButton("Remove wavespawn");
		//listRemoveWaveSpawn.setEnabled(false);
		
		JButton listUpdateWaveSpawn = new JButton("Update wavespawn");
		//listUpdateWaveSpawn.setEnabled(false)
		
		waveSpawnBLManager = new ButtonListManager(waveSpawnList, listAddWaveSpawn, listUpdateWaveSpawn, listRemoveWaveSpawn);
		
		JList<String> squadRandomList = new JList<String>(squadRandomListModel);
		squadRandomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addSquadRandom = new JButton(addSquadMsg);
		removeSquadRandom = new JButton(removeSquadMsg);
		
		squadRandomBLManager = new ButtonListManager(squadRandomList, addBot, updateBot, removeBot);
		
		JButton createPop = new JButton("Create popfile");
		
		getWaveList();
		getWaveSpawnList();
		
		listWaveList.setSelectedIndex(0); //on init, have the 1st wave selected
		waveSpawnList.setSelectedIndex(0); //same here
		waveBLManager.changeButtonState(ButtonListManager.States.SELECTED);
		waveSpawnBLManager.changeButtonState(ButtonListManager.States.SELECTED);
		
		listWaveList.addListSelectionListener(new ListSelectionListener() { //when a wave is selected from list
			 public void valueChanged(ListSelectionEvent l) {
				int waveIndex = listWaveList.getSelectedIndex();
				feedback.setText(" ");
				
				if(waveIndex != -1) { //prevents listener fits
					currentWaveNode = (WaveNode) popNode.getChildren().get(waveIndex); //populate subwave list, subwave panel with first subwave, first tfbot 
					listCurrentWave.setText("Editing wave " + Integer.toString(waveIndex + 1));
					//addWave.setText(addWaveMsg);
					wavePanel.updatePanel(currentWaveNode);
					waveBLManager.changeButtonState(ButtonListManager.States.SELECTED);	
					
					if(currentWaveNode.getChildren().size() > 0) {
						getWaveSpawnList();
						waveSpawnList.setSelectedIndex(0);
						//listAddWaveSpawn.setText(addWSMsg);
					}
					else { //if user decided to add the wave before making a wavespawn 
						waveSpawnBLManager.changeButtonState(ButtonListManager.States.EMPTY);
						waveSpawnListModel.clear();
						//listRemoveWaveSpawn.setEnabled(false);
						loadBot(true);
						tfbotBut.setSelected(true);
						spawnerInfo.setText(noSpawner);
					}
				}
				else { //remember that refreshing the list also causes index to be -1
					waveBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
				}
			} 
		});
		
		addWave.addActionListener(new ActionListener () { //adds a new wave to end of list 
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				
				/*
				if(addWave.getText().equals(addWaveMsg)) {
					currentWaveNode.connectNodes(popNode);
					listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size())); //indexed from 1 
				}
				else { //createWaveMsg
					addWave.setText(addWaveMsg);
					listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size() + 1));
				} */
				currentWaveNode = new WaveNode();
				currentWaveNode.connectNodes(popNode);
				listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size()));
				
				getWaveList();
				
				//may not be necessary, but makes sure existing subnodes can't get linked
				currentWSNode = new WaveSpawnNode();
				waveSpawnListModel.clear();
				wsPanel.updatePanel(currentWSNode);
				currentWSNode.connectNodes(currentWaveNode);
				
				loadBot(true);
				tfbotBut.setSelected(true);
				spawnerInfo.setText(noSpawner);
				
				//removeWave.setEnabled(true);
			} 
		});
		
		removeWave.addActionListener(new ActionListener () { //remove the selected wave 
			public void actionPerformed(ActionEvent a) {
				List<Node> list = popNode.getChildren();
				feedback.setText(" ");
				
				if(listWaveList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node 
					list.remove(list.size() - 1); 
				} 
				else {
					list.remove(listWaveList.getSelectedIndex()); 
				}				
				getWaveList();
				
				if(list.size() == 0) { //double check subnode states here
					currentWaveNode = new WaveNode();
					listCurrentWave.setText("Editing wave 1");
					//removeWave.setEnabled(false);
					waveSpawnListModel.clear();
					//listRemoveWaveSpawn.setEnabled(false);
					waveBLManager.changeButtonState(ButtonListManager.States.EMPTY);
					waveSpawnBLManager.changeButtonState(ButtonListManager.States.EMPTY);
				}
				else { //set current wave and its subnodes
					listWaveList.setSelectedIndex(list.size() - 1);
				}
			}
		});
		
		updateWave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				feedback.setText("Wave updated");
				wavePanel.updateNode(currentWaveNode);
				//don't update list since no name
			}
		});
		
		waveSpawnList.addListSelectionListener(new ListSelectionListener() { //when wavespawn is selected from list
			public void valueChanged(ListSelectionEvent l) {
				int waveSpawnIndex = waveSpawnList.getSelectedIndex();
				feedback.setText(" ");
				
				//prevent fits if index is reset
				if(waveSpawnIndex != -1) {
					currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(waveSpawnIndex);
					//System.out.println(currentWSNode.getChildren());
					listCurrentWSLabel.setText("Editing wavespawn " + waveSpawnListModel.get(waveSpawnIndex));
					
					listAddWaveSpawn.setText(addWSMsg);
					waveSpawnBLManager.changeButtonState(ButtonListManager.States.SELECTED);
					wsPanel.updatePanel(currentWSNode);
					
					if(currentWSNode.hasChildren()) {
						checkSpawner(currentWSNode.getSpawner());
					}
					else {
						loadBot(true);
						tfbotBut.setSelected(true);
					}
				}
				else { //disable updating when there is not a subwave explicitly selected
					waveSpawnBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
				}
			}
		});
		
		listAddWaveSpawn.addActionListener(new ActionListener() { //add/update button is clicked
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				//System.out.println(currentWSNode);
				
				/*
				if(listAddWaveSpawn.getText().equals(addWSMsg)) { //if we're adding a wavespawn
					wsPanel.updateNode(currentWSNode);
					currentWSNode.connectNodes(currentWaveNode);
					
					if(currentWaveNode.getParent() == null) {
						feedback.setText("WaveSpawn was successfully linked to the current wave, but the wave itself is unlinked");
					}
				}
				else { //creating a new one
					listAddWaveSpawn.setText(addWSMsg);
				} */
				currentWSNode = new WaveSpawnNode();
				currentWSNode.connectNodes(currentWaveNode);
				
				listRemoveWaveSpawn.setEnabled(true); //might put a check for this
				
				getWaveSpawnList();
				listCurrentWSLabel.setText("Editing wavespawn " + waveSpawnListModel.lastElement());
				wsPanel.updatePanel(currentWSNode);
				spawnerInfo.setText(noSpawner);

				loadBot(true);
				tfbotBut.setSelected(true);
			}
		});
		
		listRemoveWaveSpawn.addActionListener(new ActionListener() { //remove button clicked
			public void actionPerformed(ActionEvent a) {
				List<Node> list = currentWaveNode.getChildren();
				feedback.setText(" ");
				
				if(waveSpawnList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node 
					list.remove(list.size() - 1); 
				} 
				else {
					list.remove(waveSpawnList.getSelectedIndex()); 
				}				
				getWaveSpawnList();
				
				if(list.size() == 0) { //if no wavespawns again
					waveSpawnBLManager.changeButtonState(ButtonListManager.States.EMPTY);
					currentWSNode = new WaveSpawnNode();
					listCurrentWSLabel.setText("Editing new wavespawn");
				}
				else {
					waveSpawnList.setSelectedIndex(list.size() - 1);
				}	
			}
		});
		
		listUpdateWaveSpawn.addActionListener(new ActionListener() { //update wavespawn button clicked
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				wsPanel.updateNode(currentWSNode);
				getWaveSpawnList();
			}
		});
		
		addBot.addActionListener(new ActionListener() { //adds bot when clicked
			public void actionPerformed(ActionEvent a) { //will need new contexts later
				switch (addBot.getText()) {
					case (addBotMsg):
						botPanel.updateNode(currentBotNode);
						if(squadBut.isSelected()) {
							currentBotNode.connectNodes(currentSquadNode);
							getSquadRandomList();
							loadBot(true);
						}
						else if(randomBut.isSelected()) {
							currentBotNode.connectNodes(currentRCNode);
							getSquadRandomList();
							loadBot(true);
						}
						else {
							currentBotNode.connectNodes(currentWSNode);
							squadRandomBLManager.changeButtonState(ButtonListManager.States.SELECTED);
							botPanel.getAttributesPanel().setAttrToBotButtonsStates(true, false, ButtonListManager.States.EMPTY);
							botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode); //other place where you can enter bot added state
							updateBot.setText(updateBotMsg);
							removeBot.setText(removeBotMsg);
							spawnerInfo.setText(botSpawner);				
						}
						//currentBotNode = new TFBotNode();
						
						if(currentWSNode.getParent() == null) { //may need to change here
							feedback.setText("Bot successfully created, but the wavespawn it is linked to is currently unadded");
						}
						else {
							feedback.setText("Bot successfully created");
						}
						break;
					case (addTankMsg):
						tankPanel.updateNode(currentTankNode);
						currentTankNode.connectNodes(currentWSNode);
						squadRandomBLManager.changeButtonState(ButtonListManager.States.SELECTED);
						updateBot.setText(updateTankMsg);
						removeBot.setText(removeTankMsg);
						spawnerInfo.setText(tankSpawner);
						if(currentWSNode.getParent() == null) {
							feedback.setText("Tank successfully created, but the wavespawn it is linked to is currently unadded");
						}
						else {
							feedback.setText("Tank successfully created");
						}
						break;
				}
			}
		});
		
		removeBot.addActionListener(new ActionListener() { //remove current spawner from wavespawn
			public void actionPerformed(ActionEvent a) {
				List<Node> list;
				
				if(tfbotBut.isSelected() || tankBut.isSelected()) {
					//both simple spawners
					currentWSNode.getChildren().clear();
					spawnerInfo.setText(noSpawner);
					loadBot(true);
					tfbotBut.setSelected(true);
				}
				else if(squadBut.isSelected() || randomBut.isSelected()) { //same as above list removal logic
					if(squadBut.isSelected()) {
						list = currentSquadNode.getChildren();
					}
					else {
						list = currentRCNode.getChildren();
					}
					
					if(squadRandomList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node 
						list.remove(list.size() - 1); 
					} 
					else {
						list.remove(squadRandomList.getSelectedIndex()); 
					}				
					getSquadRandomList();
					
					if(list.size() == 0) { //if no wavespawns again
						squadRandomBLManager.changeButtonState(ButtonListManager.States.EMPTY);
						loadBot(true);
					}
					else {
						//squadRandomList.setSelectedIndex(list.size() - 1);
					}	
				}
			}
		});
		
		updateBot.addActionListener(new ActionListener() { //update current spawner
			public void actionPerformed(ActionEvent a) {
				if(tankBut.isSelected()) {
					tankPanel.updateNode(currentTankNode);
					feedback.setText("Tank successfully updated");
				}
				else {
					botPanel.updateNode(currentBotNode);
					feedback.setText("Bot successfully updated");
					
					getSquadRandomList();
				}	
			}
		});
		
		addSquadRandom.addActionListener(new ActionListener() { //specific button for adding squadrandom to ws, similiar logic to add bot
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				if(squadBut.isSelected()) {
					currentSquadNode.connectNodes(currentWSNode);
					feedback.setText("Squad added");
					spawnerInfo.setText(squadSpawner);
				}
				else {
					currentRCNode.connectNodes(currentWSNode);
					feedback.setText("Randomchoice added");
					spawnerInfo.setText(randomSpawner);
				}
				addSquadRandom.setEnabled(false);
				removeSquadRandom.setEnabled(true);
			}
		});
		
		removeSquadRandom.addActionListener(new ActionListener() { //same as remove tfbot/tank
			public void actionPerformed(ActionEvent a) {
				currentWSNode.getChildren().clear();
				squadRandomListModel.clear();
				removeSquadRandom.setEnabled(false);
				spawnerInfo.setText(noSpawner);
				loadBot(true);
				tfbotBut.setSelected(true);
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
					squadRandomBLManager.changeButtonState(ButtonListManager.States.NOSELECTION);
				}
				//System.out.println(currentBotNode);
			}
		});
		
		createPop.addActionListener(event -> {
			String error = TreeParse.treeCheck(tree, this);
			if(error.isEmpty()) {
				getFile();
			}
			else {
				feedback.setText(error);
			}	 
		});
		
		//todo: template list
		
		addGB(listPanel, gb, listCurrentWave, 0, 0);
		addGB(listPanel, gb, addWave, 0, 1);
		addGB(listPanel, gb, updateWave, 0, 2);
		addGB(listPanel, gb, removeWave, 0, 3);
		
		addGB(listPanel, gb, listCurrentWSLabel, 0, 4);
		addGB(listPanel, gb, listAddWaveSpawn, 0, 5);
		addGB(listPanel, gb, listUpdateWaveSpawn, 0, 6);
		addGB(listPanel, gb, listRemoveWaveSpawn, 0, 7);
		
		addGB(listPanel, gb, addSquadRandom, 0, 8);
		addSquadRandom.setVisible(false);
		addGB(listPanel, gb, removeSquadRandom, 0, 9);
		removeSquadRandom.setVisible(false);
		
		addGB(listPanel, gb, addBot, 0, 10);
		addGB(listPanel, gb, updateBot, 0, 11);
		addGB(listPanel, gb, removeBot, 0, 12);
		updateBot.setEnabled(false);
		removeBot.setEnabled(false);
		
		addGB(listPanel, gb, createPop, 0, 13);
		
		gb.gridheight = 2;
		addGB(listPanel, gb, listWaveList, 1, 1);
		addGB(listPanel, gb, squadRandomList, 1, 8);
		
		gb.gridheight = 3;
		addGB(listPanel, gb, waveSpawnList, 1, 5);		
	} 
	
	void getFile() { //get filename/place to save pop at
		JFileChooser c = new JFileChooser();
		c.showSaveDialog(frame);
		//if(result == JFileChooser.CANCEL_OPTION) return;
		try { //double check
			File file = c.getSelectedFile();
			if(file.exists()) { //confirm overwrite
				int op = JOptionPane.showConfirmDialog(frame, "Overwrite this file?");
				if (op == JOptionPane.YES_OPTION) {
					TreeParse.parseTree(file, tree);
					feedback.setText("Popfile successfully generated!");
				} //kinda jank
			}
			else { //if it doesn't exist, no overwrite check needed
				TreeParse.parseTree(file, tree);
				feedback.setText("Popfile successfully generated!");
			}
		}
		catch(Exception e) {
			
		}
	}
	
	void getWaveList() { //since waves don't have real names, just approx by naming them wave 1, wave 2, etc
		int length = popNode.getChildren().size();
		
		waveListModel.clear();
		
		for(int i = 0; i < length; i++) {
			waveListModel.addElement("Wave " + Integer.toString(i + 1));
		}
	}
	
	void getWaveSpawnList() { //similar to the above, just gets actual names 
		int length = currentWaveNode.getChildren().size();
		
		waveSpawnListModel.clear();

		for(int i = 0; i < length; i++) {
			WaveSpawnNode t = (WaveSpawnNode) currentWaveNode.getChildren().get(i);
			if(t.getName() != null && !t.getName().equals("")) {
				waveSpawnListModel.addElement(t.getName()); //this is extremely awful
			}
			else {
				waveSpawnListModel.addElement(Integer.toString(i));
			}
		}
	}
	
	void getSquadRandomList() { //take input here since it could be either a squadnode or randomchoicenode
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
			if(!((String) t.getValue(TFBotKeys.NAME)).isEmpty()) {
				squadRandomListModel.addElement((String) t.getValue(TFBotKeys.NAME)); //this is extremely awful
			}
			else {
				squadRandomListModel.addElement(t.getValue(TFBotKeys.CLASSNAME).toString());
				//classname is classname obj
			}		
		}
	}
	
	public void loadMap(int mapIndex) { //takes mapindex and passes map info to appropriate locations
		info.getMapData(mapIndex);
		
		wavePanel.setRelay(info.getWaveRelay());
		wsPanel.setRelay(info.getWSRelay());
		wsPanel.setWhere(info.getBotSpawns());
		botPanel.updateTagList(info.getTags());
		tankPanel.setMapInfo(info.getTankSpawns(), info.getTankRelays());
	}
	 
	public void addGB(Container cont, GridBagConstraints gb, Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		cont.add(comp, gb);
	}
	
	public void updateFeedback(String string) {
		feedback.setText(string);
	}
	
	//on load, look for engiconfig and make one if there isn't one
	void initConfig(JFrame windowFrame, SettingsWindow sw) {
		File cfg = new File("engiconfig.cfg");
		try {
			if(cfg.createNewFile() || sw.getItemsTxtPath() == null) { //if no cfg existed or cfg existed but has no path set
				int op = JOptionPane.showConfirmDialog(windowFrame, "items_game.txt path is currently unset. Set it?");
				
				if(op == JOptionPane.YES_OPTION) {
					File itemsTxt = getItemsTxtPath(); //try to get item path, then parse
					if(itemsTxt != null) {
						sw.setItemsTxtPath(itemsTxt);
						sw.writeToConfig();
						parseItems(itemsTxt);
					}
				}
			}
			else {	
				parseItems(new File(sw.getItemsTxtPath()));
			}
		}
		catch (IOException io) {
			updateFeedback("engiconfig.cfg was not found or unable to be written to");
		}
	}
	
	//take file, parse it, let botpanel know
	void parseItems(File itemsTxt) { 
		itemParser = new ItemParser(itemsTxt, this);
		botPanel.getItemParser(itemParser);
	}
	
	File getItemsTxtPath() { //get file object of items_game.txt
		JFileChooser c;
		File file = null; //prob shouldn't do this but no defaults for linux/osx
		boolean selectingFile = true;
		
		if(System.getProperty("os.name").contains("Windows")) { //for windows, default to standard items_game path
			file = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Team Fortress 2\\tf\\scripts\\items\\items_game.txt");
			c = new JFileChooser(file);
		}
		else { //make linux and rare osx people suffer
			//file = new File();
			c = new JFileChooser();
		}
		
		while(selectingFile) {
			c.showOpenDialog(frame);
			file = c.getSelectedFile();
			if(!file.getName().equals("items_game.txt")) {
				int op = JOptionPane.showConfirmDialog(frame, "File selected is not items_game.txt. Select a new file?");
				if (op != JOptionPane.YES_OPTION) { //if cancelled or no, leave
					selectingFile = false;
					file = null; //clear out
				} //otherwise it just opens the dialogue again
			}
			else { //if it turns out it's a different file named items_game.txt, the parser will handle it
				selectingFile = false;
			}
		}
		return file;
	}
}
