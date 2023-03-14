package engipop;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import engipop.ButtonListManager.States;
import engipop.Tree.*;
import engipop.Tree.TFBotNode.TFBotKeys;
import engipop.Tree.WaveSpawnNode.WaveSpawnKeys;

//main class
//also contains listpanel, which manages the other panels
@SuppressWarnings("serial")
public class MainWindow extends EngiWindow {
	public static final String ITEMPARSE = "itemparse";
	public static final String BOTTEMPLATELISTFIXED = "bottemplatelistfixed";
	public static final String WSTEMPLATELIST = "wstemplatelist";
	
	//todo: update botpanel
	JMenuItem popSet = new JMenuItem("Open population settings");
	JMenuItem settings = new JMenuItem("Open Engipop settings");
	JMenuItem templateSet = new JMenuItem("Template settings");
	
	BotPanel botPanel;
	WaveSpawnPanel wsPanel;
	TankPanel tankPanel;
	WavePanel wavePanel;
	ListPanel listPanel;
	
	JPanel spawnerPanel;
	
	PopNode popNode = new PopNode(); //minimum working pop
	
	WaveNode currentWaveNode = new WaveNode();
	WaveSpawnNode currentWSNode = new WaveSpawnNode();
	TFBotNode currentBotNode = new TFBotNode();
	TankNode currentTankNode = new TankNode();
	SquadNode currentSquadNode = new SquadNode();
	RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	JLabel spawnerInfo = new JLabel("Current spawner: none");
	
	Tree tree = new Tree(popNode);
	ItemParser itemParser;
	private PopulationParser popParse = new PopulationParser(this);
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	//todo: add a vertical scrollbar
	public MainWindow() {
		super("Engipop main");
	
		setSize(1400, 1000);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		feedback = new JLabel(" ");
		
		//may want to reconsider this
		SecondaryWindow w2 = new SecondaryWindow(popNode, this);
		TemplateWindow tempWindow = new TemplateWindow(this, w2);
		
		popSet.addActionListener(event -> {
			w2.updatePopPanel();
			w2.setVisible(true);
		});
		templateSet.addActionListener(event -> {
			if(!tempWindow.isVisible()) {
				tempWindow.setVisible(true);
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		JMenu options = new JMenu("Options");
		
		options.add(settings);
		options.add(popSet);
		menuBar.add(options);
		menuBar.add(templateSet);
		setJMenuBar(menuBar);		

		currentWSNode.putKey(WaveSpawnKeys.NAME, "default");
		currentWaveNode.connectNodes(popNode);
		currentWSNode.connectNodes(currentWaveNode);
		
		botPanel = new BotPanel(this, this, w2);
		wsPanel = new WaveSpawnPanel(w2);
		wavePanel = new WavePanel(w2);
		tankPanel = new TankPanel(w2);
		
		listPanel = new ListPanel();
		
		tankPanel.setVisible(false);
		
		spawnerInfo.setPreferredSize(new Dimension(179, 14));
		
		/*
		JPanel filler = new JPanel();
		filler.setSize(new Dimension(100, 200));
		filler.setMinimumSize(filler.getPreferredSize()); */
		
		//addGB(filler, 0, 0);
		addGB(feedback, 0, 1);
		
		gbConstraints.gridwidth = 2;
		addGB(wavePanel, 0, 2);
		addGB(wsPanel, 0, 3);
	
		gbConstraints.gridwidth = 1;
		addGB(spawnerPanel, 0, 4);
		gbConstraints.anchor = GridBagConstraints.WEST;
		addGB(spawnerInfo, 1, 4);
		
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbConstraints.gridwidth = 2;
		addGB(tankPanel, 0, 5);
		
		gbConstraints.gridheight = 2;
		gbConstraints.weighty = 1;
		addGB(botPanel, 0, 5);
		
		gbConstraints.weighty = 0;
		gbConstraints.gridwidth = 1;
		gbConstraints.gridheight = 3;
		addGB(listPanel, 2, 3);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		w2.requestFocus();
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    //
		}
		
		MainWindow w = new MainWindow();
		SettingsWindow setW = new SettingsWindow(w);
		
		setW.initConfig(w);
		
		w.listen(setW); //could change this
		
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
	

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
	
    public void updatePropertyListeners() {
    	support.firePropertyChange(ITEMPARSE, null, itemParser);
    	support.firePropertyChange(BOTTEMPLATELISTFIXED, null, popParse.getBotTemplateList());
    }
	
	public PopNode getPopNode() {
		return this.popNode;
	}
	
	//listeners that interact with other windows
	void listen(SettingsWindow sw) {
		settings.addActionListener(event -> {
			if(!sw.isVisible()) {
				sw.updateWindow();
				sw.setVisible(true);
			}
		});
	}
	
	//take file, parse it, let botpanels know
	public void parseItems(File itemsTxt) { 
		itemParser = new ItemParser(itemsTxt, this);
		updatePropertyListeners();
		
		try {
			popParse.parseTemplates(itemParser, Paths.get((this.getClass().getResource("/robot_standard.pop")).toURI()).toFile());
		} catch (URISyntaxException e) {
		}
		
		
		PopulationParser popParse = new PopulationParser(this);
		popParse.parseTemplates(itemParser, new File());
	}
	
	//subclass for list panel
	public class ListPanel extends EngiPanel {
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
		
		JButton addSpawner = new JButton(addBotMsg);
		JButton updateSpawner = new JButton(updateBotMsg);
		JButton removeSpawner = new JButton(removeBotMsg);
		
		//essentially functions the same as the above 3, just manages squad/random's child bots
		JButton addSquadRandomBot = new JButton(addBotMsg);
		JButton updateSquadRandomBot = new JButton(updateBotMsg);
		JButton removeSquadRandomBot = new JButton(removeBotMsg);
		
		JButton addWave = new JButton("Create empty wave");
		JButton updateWave = new JButton("Update wave");
		JButton removeWave = new JButton("Remove wave");
		
		JButton addWaveSpawn = new JButton("Create empty wavespawn");
		JButton updateWaveSpawn = new JButton("Update wavespawn");
		JButton removeWaveSpawn = new JButton("Remove wavespawn");
		
		DefaultListModel<String> waveListModel = new DefaultListModel<String>();
		DefaultListModel<String> waveSpawnListModel = new DefaultListModel<String>();
		DefaultListModel<String> squadRandomListModel = new DefaultListModel<String>();
		
		JList<String> waveList = new JList<String>(waveListModel);
		JList<String> waveSpawnList = new JList<String>(waveSpawnListModel);
		JList<String> squadRandomList = new JList<String>(squadRandomListModel);
		
		ButtonListManager waveBLManager = new ButtonListManager(waveList, addWave, updateWave, removeWave);
		ButtonListManager waveSpawnBLManager = new ButtonListManager(waveSpawnList, addWaveSpawn, updateWaveSpawn, removeWaveSpawn);
		ButtonListManager spawnerBLManager = new ButtonListManager(squadRandomList, addSpawner, updateSpawner, removeSpawner);
		ButtonListManager squadRandomBLManager = new ButtonListManager(squadRandomList, addSquadRandomBot, updateSquadRandomBot, removeSquadRandomBot);
		//todo: change the list if it gets used for anything
		
		JLabel currentWaveLabel = new JLabel("");
		JLabel currentWSLabel = new JLabel("");
		
		JRadioButton tfbotBut;
		JRadioButton tankBut;
		JRadioButton squadBut;
		JRadioButton randomBut;
		JRadioButton noneBut = new JRadioButton("none");
		//virtual button to ensure the buttongroup state always changes, so can't go from say tfbot to tfbot, which wouldn't cause a state change
		
		JButton createPop = new JButton("Create popfile"); //may consider putting this in window	
		
		public ListPanel() {
			setLayout(gbLayout);
			gb.anchor = GridBagConstraints.NORTHWEST;
			gb.insets = new Insets(5, 0, 5, 5);
			
			waveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			waveSpawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			squadRandomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			//listAddWaveSpawn.setPreferredSize(new Dimension(159, 22));
			//+2 for padding or something
		
			getWaveList();
			getWaveSpawnList();
			
			waveBLManager.changeButtonState(States.SELECTED);
			waveSpawnBLManager.changeButtonState(States.SELECTED);
			spawnerBLManager.changeButtonState(States.EMPTY);
			squadRandomBLManager.changeButtonState(States.DISABLE);
			
			addSquadRandomBot.setVisible(false);
			updateSquadRandomBot.setVisible(false);
			removeSquadRandomBot.setVisible(false);
			
			initListeners();
			initSpawnerSelector();
			
			waveList.setSelectedIndex(0); //on init, have the 1st wave selected
			waveSpawnList.setSelectedIndex(0); //same here
			
			addWave.setToolTipText("Creates an empty wave");
			addWaveSpawn.setToolTipText("Creates an empty wavespawn");
			
			//addGB(currentWaveLabel, 0, 0);
			addGB(addWave, 0, 1);
			addGB(updateWave, 0, 2);
			addGB(removeWave, 0, 3);
			
			//addGB(currentWSLabel, 0, 4);
			addGB(addWaveSpawn, 0, 5);
			addGB(updateWaveSpawn, 0, 6);
			addGB(removeWaveSpawn, 0, 7);
			
			addGB(addSpawner, 0, 8);
			addGB(updateSpawner, 0, 9);
			addGB(removeSpawner, 0, 10);
			
			addGB(addSquadRandomBot, 0, 11);
			addGB(updateSquadRandomBot, 0, 12);
			addGB(removeSquadRandomBot, 0, 13);
			
			addGB(createPop, 0, 14);
			
			gb.gridheight = 3;
			addGB(waveList, 1, 1);
			addGB(squadRandomList, 1, 8);
			
			//gb.gridheight = 3;
			addGB(waveSpawnList, 1, 5);		
		}
		
		private void initListeners() { //inits all the button/list listeners
			waveList.addListSelectionListener(event -> { //when a wave is selected from list
				int waveIndex = waveList.getSelectedIndex();
				feedback.setText(" ");
				
				noneBut.setSelected(true);
				
				if(waveIndex != -1) { //prevents listener fits
					currentWaveNode = (WaveNode) popNode.getChildren().get(waveIndex);
					//currentWaveLabel.setText("Editing wave " + Integer.toString(waveIndex + 1));
					wavePanel.updatePanel(currentWaveNode);
					waveBLManager.changeButtonState(States.SELECTED);	
					
					if(currentWaveNode.getChildren().size() > 0) {
						getWaveSpawnList();
						waveSpawnList.setSelectedIndex(0);
					}
					else { //should only happen if user removes all wavespawns
						waveSpawnListModel.clear();
						waveSpawnList.setSelectedIndex(-1);
						//waveSpawnBLManager.changeButtonState(States.EMPTY);
						
						//tfbotBut.setSelected(true);
						//spawnerInfo.setText(noSpawner);
					}
				}
				else { //remember that refreshing the list also causes index to be -1
					//think about this since once something is selected -> selection won't be changed until something else is
					//currentWaveLabel.setText("No wave selected");
					waveBLManager.changeButtonState(States.EMPTY);
					waveSpawnBLManager.changeButtonState(States.DISABLE);
					spawnerBLManager.changeButtonState(States.DISABLE);
				}
			});
			
			addWave.addActionListener(new ActionListener () { //adds a new wave to end of list 
				public void actionPerformed(ActionEvent a) {
					feedback.setText(" ");
					
					currentWaveNode = new WaveNode();
					currentWaveNode.connectNodes(popNode);
					//currentWaveLabel.setText("Editing wave " + Integer.toString(popNode.getChildren().size()));
					
					//getWaveList();
					waveListModel.addElement("Wave");
					
					waveList.setSelectedIndex(waveListModel.getSize() - 1);
					
					//does all the create a new wavespawn stuff
					addWaveSpawn.doClick();
					/*
					//may not be necessary, but makes sure existing subnodes can't get linked
					currentWSNode = new WaveSpawnNode();
					currentWSNode.connectNodes(currentWaveNode);
					wsPanel.updatePanel(currentWSNode);
					getWaveSpawnList();
					waveSpawnList.setSelectedIndex(0);
					
					//loadBot(true);
					tfbotBut.setSelected(true);
					spawnerInfo.setText(noSpawner); */
				} 
			});
			
			removeWave.addActionListener(new ActionListener () { //remove the selected wave 
				public void actionPerformed(ActionEvent a) {
					List<Node> list = popNode.getChildren();
					feedback.setText(" ");
					
					if(waveList.getSelectedIndex() != -1) { //if there's nothing selected, fallback to removing the last node 
						list.remove(waveList.getSelectedIndex()); 
						waveListModel.remove(waveList.getSelectedIndex());
						
						//getWaveList();
						
						if(list.size() == 0) {
							//currentWaveNode = new WaveNode();
							currentWaveLabel.setText("No waves");
							currentWSLabel.setText("No wavespawns"); //not the best place
							resetWaveState();
						}
						else { //set current wave and its subnodes
							waveList.setSelectedIndex(list.size() - 1);
						}
					} 
					//else {
					//	list.remove(list.size() - 1);
					//}					
				}
			});
			
			updateWave.addActionListener(event -> {
				feedback.setText("Wave updated");
				wavePanel.updateNode(currentWaveNode);
				//don't update list since no real name
			});
			
			waveSpawnList.addListSelectionListener(new ListSelectionListener() { //when wavespawn is selected from list
				public void valueChanged(ListSelectionEvent l) {
					int waveSpawnIndex = waveSpawnList.getSelectedIndex();
					feedback.setText(" ");
					
					noneBut.setSelected(true);
					
					//prevent fits if index is reset
					if(waveSpawnIndex != -1) {
						currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(waveSpawnIndex);
						//currentWSLabel.setText("Editing wavespawn " + waveSpawnListModel.get(waveSpawnIndex));
						
						waveSpawnBLManager.changeButtonState(States.SELECTED);
						wsPanel.updatePanel(currentWSNode);
						
						if(currentWSNode.hasChildren()) {
							checkSpawner(currentWSNode.getSpawner());
						}
						else {
							//loadBot(true);
							tfbotBut.setSelected(true);
						}
					}
					else { //disable updating when there is not a subwave explicitly selected
						waveSpawnBLManager.changeButtonState(States.EMPTY);
						spawnerBLManager.changeButtonState(States.DISABLE);
						//currentWSLabel.setText("No wavespawn selected");
					}
				}		
			});
			
			addWaveSpawn.addActionListener(new ActionListener() { //add/update button is clicked
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
					
					waveSpawnListModel.addElement("Wavespawn");
					
					//removeWaveSpawn.setEnabled(true); //might put a check for this
					
					//getWaveSpawnList();
					//currentWSLabel.setText("Editing wavespawn " + waveSpawnListModel.lastElement());
					wsPanel.updatePanel(currentWSNode);
					spawnerInfo.setText(noSpawner);

					//loadBot(true);
					tfbotBut.setSelected(true);
				}
			});
			
			removeWaveSpawn.addActionListener(new ActionListener() { //remove button clicked
				public void actionPerformed(ActionEvent a) {
					List<Node> list = currentWaveNode.getChildren();
					feedback.setText(" ");
					
					if(waveSpawnList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node
						//list.remove(list.size() - 1);
					} 
					else {
						list.remove(waveSpawnList.getSelectedIndex());
						waveSpawnListModel.remove(waveSpawnList.getSelectedIndex());
						//botPanel.updatePanel(new TFBotNode()); //update panel so no dead references
						resetSpawnerState();
						//getWaveSpawnList();
						
						if(list.size() == 0) { //if no wavespawns again
							resetWaveSpawnState(States.EMPTY);
							//currentWSNode = new WaveSpawnNode();
							currentWSLabel.setText("No wavespawns");
						}
						else {
							waveSpawnList.setSelectedIndex(list.size() - 1);
						}	
					}
				}
			});
			
			updateWaveSpawn.addActionListener(new ActionListener() { //update wavespawn button clicked
				public void actionPerformed(ActionEvent a) {
					feedback.setText(" ");
					wsPanel.updateNode(currentWSNode);
					
					if(currentWSNode.getValue(WaveSpawnKeys.NAME) != null) {
						waveSpawnListModel.set(waveSpawnList.getSelectedIndex(), (String) currentWSNode.getValue(WaveSpawnKeys.NAME));
					}
					else { //fallback if name was added but then removed
						waveSpawnListModel.set(waveSpawnList.getSelectedIndex(), "Wavespawn");
					}
					//getWaveSpawnList();
				}
			});
			
			addSpawner.addActionListener(new ActionListener() { //adds spawner when clicked
				public void actionPerformed(ActionEvent a) { //will need new contexts later
					switch (addSpawner.getText()) {
						case (addBotMsg):
							botPanel.updateNode(currentBotNode);
							currentBotNode.connectNodes(currentWSNode);
							spawnerBLManager.changeButtonState(ButtonListManager.States.SELECTED);
							botPanel.getAttributesPanel().updateItemAttrInfo(currentBotNode); //other place where you can enter bot added state
							spawnerInfo.setText(botSpawner);				
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
							spawnerBLManager.changeButtonState(States.SELECTED);
							spawnerInfo.setText(tankSpawner);
							if(currentWSNode.getParent() == null) {
								feedback.setText("Tank successfully created, but the wavespawn it is linked to is currently unadded");
							}
							else {
								feedback.setText("Tank successfully created");
							}
							break;
						case (addSquadMsg):
							currentSquadNode.connectNodes(currentWSNode);
							spawnerBLManager.changeButtonState(States.SELECTED);
							squadRandomBLManager.changeButtonState(States.EMPTY); //enable adding bot subnodes
							
							spawnerInfo.setText(squadSpawner);
							feedback.setText("Squad successfully created");
							//loadBot(true);
							
							break;
						case (addRandomMsg):
							currentRCNode.connectNodes(currentWSNode);
							spawnerBLManager.changeButtonState(States.SELECTED);
							squadRandomBLManager.changeButtonState(States.EMPTY);
							
							spawnerInfo.setText(randomSpawner);
							feedback.setText("RandomChoice successfully created");
						
							break;
					}
				}
			});
			
			removeSpawner.addActionListener(new ActionListener() { //remove current spawner from wavespawn
				public void actionPerformed(ActionEvent a) {
					currentWSNode.getChildren().clear();
					resetSpawnerState();
				}
			});
			
			updateSpawner.addActionListener(new ActionListener() { //update current spawner
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
					if(squadBut.isSelected()) { //double check for the old spawner times
						currentBotNode.connectNodes(currentSquadNode);
					}
					else {
						currentBotNode.connectNodes(currentRCNode);
					}
					//getSquadRandomList();
					
					setSquadRandomListElement(currentBotNode);
					
					loadBot(true);
				}
			});
			
			updateSquadRandomBot.addActionListener(event -> { //squad/rc specific button to update bots
				botPanel.updateNode(currentBotNode);
				feedback.setText("Bot successfully updated");
				
				//similar logic to setsquadrandomlistelement but uses set instead of addelement to update
				if(currentBotNode.getValue(TFBotKeys.NAME) != null) {
					squadRandomListModel.set(squadRandomList.getSelectedIndex(), (String) currentBotNode.getValue(TFBotKeys.NAME));
				}
				else {
					squadRandomListModel.set(squadRandomList.getSelectedIndex(), currentBotNode.getValue(TFBotKeys.CLASSNAME).toString());
				}
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
						squadRandomListModel.remove(squadRandomList.getSelectedIndex());
						
						//else {
						//	list.remove(list.size() - 1); 
						//}				
						//getSquadRandomList();
						
						if(list.size() == 0) { //if no wavespawns again
							squadRandomBLManager.changeButtonState(States.EMPTY);
							loadBot(true);
						}
						else { 
							//squadRandomList.setSelectedIndex(list.size() - 1);
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
			
			createPop.addActionListener(event -> { //potentially move this
				String error = TreeParse.treeCheck(tree);
				if(error.isEmpty()) {
					getFile();
				}
				else {
					feedback.setText(error);
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
			spawnerPanel = new JPanel();
			
			ButtonGroup spawnerGroup = new ButtonGroup();
			
			spawnerPanel.add(tfbotBut = new JRadioButton("TFBot"));
			spawnerPanel.add(tankBut = new JRadioButton("Tank"));
			spawnerPanel.add(squadBut = new JRadioButton("Squad"));
			spawnerPanel.add(randomBut = new JRadioButton("RandomChoice"));	
			
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
		
		//reset panels to fresh state whenever a node/its parents are removed
		private void resetWaveState() {
			wavePanel.updatePanel(new WaveNode());
			waveListModel.clear();
			waveBLManager.changeButtonState(States.EMPTY);
			resetWaveSpawnState(States.DISABLE);
		}
		
		private void resetWaveSpawnState(States state) {
			wsPanel.updatePanel(new WaveSpawnNode());
			waveSpawnListModel.clear();	
			waveSpawnBLManager.changeButtonState(state);
			resetSpawnerState();
		}
		
		private void resetSpawnerState() {
			spawnerInfo.setText(noSpawner);
			//loadBot(true);
			tfbotBut.setSelected(true);
			spawnerBLManager.changeButtonState(States.DISABLE);
		}
		
		private void getFile() { //get filename/place to save pop at
			JFileChooser c = new JFileChooser();
			c.showSaveDialog(this);
			//if(result == JFileChooser.CANCEL_OPTION) return;
			try { //double check
				File file = c.getSelectedFile();
				if(file.exists()) { //confirm overwrite
					int op = JOptionPane.showConfirmDialog(this, "Overwrite this file?");
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
		
		private void getWaveList() { //since waves don't have real names, just approx by naming them wave 1, wave 2, etc
			int length = popNode.getChildren().size();
			
			waveListModel.clear();
			
			for(int i = 0; i < length; i++) {
				//waveListModel.addElement("Wave " + Integer.toString(i + 1));
				waveListModel.addElement("Wave");
			}
		}
		
		private void getWaveSpawnList() { //similar to the above, just gets actual names 
			int length = currentWaveNode.getChildren().size();
			
			waveSpawnListModel.clear();

			for(int i = 0; i < length; i++) {
				WaveSpawnNode t = (WaveSpawnNode) currentWaveNode.getChildren().get(i);
				if(t.getValue(WaveSpawnKeys.NAME) != null && !t.getValue(WaveSpawnKeys.NAME).equals("")) {
					waveSpawnListModel.addElement((String) t.getValue(WaveSpawnKeys.NAME)); //this is extremely awful
				}
				else {
					waveSpawnListModel.addElement(Integer.toString(i));
				}
			}
		}
		
		//refresh entire squadrandom list
		private void getSquadRandomList() { //could be either a squadnode or randomchoicenode
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
		}
		
		//refresh singular element on squadrandom list
		private void setSquadRandomListElement(TFBotNode node) {
			if(node.getValue(TFBotKeys.NAME) != null) {
				squadRandomListModel.addElement((String) node.getValue(TFBotKeys.NAME)); //this sucks
			}
			else {
				squadRandomListModel.addElement(node.getValue(TFBotKeys.CLASSNAME).toString());
				//classname is classname obj
			}
		}
	}
}
