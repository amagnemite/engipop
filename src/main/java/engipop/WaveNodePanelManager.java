package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import engipop.ButtonListManager.States;
import engipop.EngiWindow.NoDeselectionModel;
import engipop.Node.PopNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;

//also manages lists for wave/wavespawns
public class WaveNodePanelManager extends NodePanelManager implements PropertyChangeListener {
	WavePanel wavePanel;
	WaveSpawnPanel wsPanel;
	
	private PopNode popNode = new PopNode();
	WaveNode currentWaveNode = new WaveNode();
	
	JButton addWave = new JButton("Add wave");
	JButton updateWave = new JButton("Update wave");
	JButton removeWave = new JButton("Remove wave");
	
	JButton addWaveSpawn = new JButton("Add wavespawn");
	JButton updateWaveSpawn = new JButton("Update wavespawn");
	JButton removeWaveSpawn = new JButton("Remove wavespawn");
	
	DefaultListModel<String> waveListModel = new DefaultListModel<String>();
	DefaultListModel<String> waveSpawnListModel = new DefaultListModel<String>();
	
	JList<String> waveList = new JList<String>(waveListModel);
	JList<String> waveSpawnList = new JList<String>(waveSpawnListModel);
	JScrollPane waveListScroll = new JScrollPane(waveList);
	JScrollPane waveSpawnListScroll = new JScrollPane(waveSpawnList);
	
	ButtonListManager waveBLManager = new ButtonListManager(addWave, updateWave, removeWave);
	ButtonListManager waveSpawnBLManager = new ButtonListManager(addWaveSpawn, updateWaveSpawn, removeWaveSpawn);
	
	JLabel currentWaveLabel = new JLabel("");
	JLabel currentWSLabel = new JLabel("");
	
	public WaveNodePanelManager(MainWindow window, WavePanel wavePanel, WaveSpawnPanel wsPanel, BotPanel botPanel, TankPanel tankPanel,
			SecondaryWindow secondaryWindow) {
		super(window, botPanel, tankPanel);
		initWaveListeners();
		
		this.wavePanel = wavePanel;
		this.wsPanel = wsPanel;
		popNode = window.getPopNode();
		
		secondaryWindow.addPropertyChangeListener("POPNODE", this);
		
		waveList.setSelectionModel(new NoDeselectionModel());
		waveSpawnList.setSelectionModel(new NoDeselectionModel());
		
		waveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		waveSpawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		addWave.setToolTipText("Creates an empty wave");
		addWaveSpawn.setToolTipText("Creates an empty wavespawn");
		
		currentWaveNode.connectNodes(popNode);
		currentWSNode.connectNodes(currentWaveNode);
		currentBotNode.connectNodes(currentWSNode);
		
		getWaveList();
		getWaveSpawnList();
		
		waveList.setSelectedIndex(0); //on init, have the 1st wave selected
		waveSpawnList.setSelectedIndex(0); //same here
		
		//waveList.setvis
		waveListScroll.setMinimumSize(waveList.getPreferredScrollableViewportSize());
		waveSpawnListScroll.setMinimumSize(waveSpawnList.getPreferredScrollableViewportSize());
	}
	
	public JPanel makeListPanel() {
		EngiPanel panel = new EngiPanel();
		
		panel.setLayout(panel.gbLayout);
		panel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.gbConstraints.insets = new Insets(5, 0, 5, 5);
		
		//addGB(currentWaveLabel, 0, 0);
		panel.addGB(addWave, 0, 1);
		panel.addGB(updateWave, 0, 2);
		panel.addGB(removeWave, 0, 3);
		
		//addGB(currentWSLabel, 0, 4);
		panel.addGB(addWaveSpawn, 0, 5);
		panel.addGB(updateWaveSpawn, 0, 6);
		panel.addGB(removeWaveSpawn, 0, 7);
		
		panel.addGB(addSpawner, 0, 8);
		panel.addGB(updateSpawner, 0, 9);
		panel.addGB(removeSpawner, 0, 10);
		
		panel.addGB(addSquadRandomBot, 0, 11);
		panel.addGB(updateSquadRandomBot, 0, 12);
		panel.addGB(removeSquadRandomBot, 0, 13);
		
		panel.gbConstraints.gridheight = 3;
		panel.addGB(waveListScroll, 1, 1);
		panel.addGB(waveSpawnListScroll, 1, 5);
		panel.addGB(squadRandomListScroll, 1, 8);
				
		return panel;
	}
	
	//can't override 
	protected void initWaveListeners() {
		waveList.addListSelectionListener(event -> { //when a wave is selected from list
			int waveIndex = waveList.getSelectedIndex();
			window.feedback.setText(" ");
			
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
					waveSpawnBLManager.changeButtonState(States.EMPTY);
					
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
				window.feedback.setText(" ");
				
				currentWaveNode = new WaveNode();
				currentWaveNode.connectNodes(popNode);
				//currentWaveLabel.setText("Editing wave " + Integer.toString(popNode.getChildren().size()));
				
				//getWaveList();
				waveListModel.addElement("Wave");
				
				waveList.setSelectedIndex(waveListModel.getSize() - 1); //this also clears the ws list
				
				//does all the create a new wavespawn stuff
				addWaveSpawn.doClick();
			} 
		});
		
		removeWave.addActionListener(new ActionListener () { //remove the selected wave 
			public void actionPerformed(ActionEvent a) {
				List<Node> list = popNode.getChildren();
				window.feedback.setText(" ");
				
				//if(waveList.getSelectedIndex() != -1) { //if there's nothing selected, fallback to removing the last node 
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
				//} 
				//else {
				//	list.remove(list.size() - 1);
				//}					
			}
		});
		
		updateWave.addActionListener(event -> {
			window.feedback.setText("Wave updated");
			wavePanel.updateNode(currentWaveNode);
			//don't update list since no real name
		});
		
		waveSpawnList.addListSelectionListener(new ListSelectionListener() { //when wavespawn is selected from list
			public void valueChanged(ListSelectionEvent l) {
				int waveSpawnIndex = waveSpawnList.getSelectedIndex();
				window.feedback.setText(" ");
				
				noneBut.setSelected(true);
				//TODO: update this since only time there can be nothing selected is if the list is empty
				
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
					currentWSLabel.setText("No wavespawn selected");
				}
			}		
		});
		
		addWaveSpawn.addActionListener(new ActionListener() { //add button is clicked
			public void actionPerformed(ActionEvent a) {
				window.feedback.setText(" ");
				//System.out.println(currentWSNode);
				
				currentWSNode = new WaveSpawnNode();
				currentWSNode.connectNodes(currentWaveNode);
				
				waveSpawnListModel.addElement("Wavespawn");
				waveSpawnList.setSelectedIndex(waveSpawnListModel.getSize() - 1);
				
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
				window.feedback.setText(" ");
				
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
				window.feedback.setText(" ");
				wsPanel.updateNode(currentWSNode);
				
				if(currentWSNode.getValueSingular(WaveSpawnNode.NAME) != null) {
					waveSpawnListModel.set(waveSpawnList.getSelectedIndex(), (String) currentWSNode.getValueSingular(WaveSpawnNode.NAME));
				}
				else { //fallback if name was added but then removed
					waveSpawnListModel.set(waveSpawnList.getSelectedIndex(), "Wavespawn");
				}
				//getWaveSpawnList();
			}
		});
	}
	
	private void getWaveList() { //since waves don't have real names, just approx by naming them wave 1, wave 2, etc
		int length = popNode.getChildren().size();
		
		waveListModel.clear();
		
		for(int i = 0; i < length; i++) {
			//waveListModel.addElement("Wave " + Integer.toString(i + 1));
			waveListModel.addElement("Wave");
		}
		waveList.setSelectedIndex(0);
	}
	
	private void getWaveSpawnList() { //similar to the above, just gets actual names 
		int length = currentWaveNode.getChildren().size();
		
		waveSpawnListModel.clear();

		for(int i = 0; i < length; i++) {
			WaveSpawnNode t = (WaveSpawnNode) currentWaveNode.getChildren().get(i);
			if(t.containsKey(WaveSpawnNode.NAME) && !((String) t.getValueSingular(WaveSpawnNode.NAME)).isEmpty()) {
				waveSpawnListModel.addElement((String) t.getValueSingular(WaveSpawnNode.NAME)); //this is awful
			}
			else {
				waveSpawnListModel.addElement(Integer.toString(i));
			}
		}
		waveSpawnListScroll.revalidate();
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
	
	/*
	public PopNode getPopNode() {
		return this.popNode;
	} */
	
	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = (PopNode) evt.getNewValue();
		getWaveList();
	}
}
