package engipop;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

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
		listPanel.removeAll(); //wavenode has a different layout than node, so need to set up here
		
		this.wavePanel = wavePanel;
		this.wsPanel = wsPanel;
		popNode = window.getPopNode();
		
		secondaryWindow.addPropertyChangeListener("POPNODE", this);
		
		waveList.setSelectionModel(new NoDeselectionModel());
		waveSpawnList.setSelectionModel(new NoDeselectionModel());
		
		waveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		waveSpawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//waveList.setPrototypeCellValue("Wave 10");
		waveSpawnList.setPrototypeCellValue("Wavespawn");
		
		addWave.setToolTipText("Creates an empty wave");
		addWaveSpawn.setToolTipText("Creates an empty wavespawn");
		
		currentWaveNode.connectNodes(popNode);
		currentWSNode.connectNodes(currentWaveNode);
		currentBotNode.connectNodes(currentWSNode);
		
		getWaveList();
		getWaveSpawnList();
		
		waveList.setSelectedIndex(0); //on init, have the 1st wave selected
		waveSpawnList.setSelectedIndex(0); //same here
		
		waveListScroll.setMinimumSize(new Dimension(waveList.getPreferredScrollableViewportSize().width + 20,waveList.getPreferredScrollableViewportSize().height));
		//i don't even know
		waveSpawnListScroll.setMinimumSize(waveSpawnList.getPreferredScrollableViewportSize());
		
		listPanel.gbConstraints = new GridBagConstraints(); //dump the one from NodePanelManager
		listPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		listPanel.gbConstraints.insets = new Insets(5, 0, 5, 5);
		
		//addGB(currentWaveLabel, 0, 0);
		listPanel.addGB(addWave, 0, 1);
		listPanel.addGB(updateWave, 0, 2);
		listPanel.addGB(removeWave, 0, 3);
		
		//addGB(currentWSLabel, 0, 4);
		listPanel.addGB(addWaveSpawn, 0, 5);
		listPanel.addGB(updateWaveSpawn, 0, 6);
		listPanel.addGB(removeWaveSpawn, 0, 7);
		
		listPanel.addGB(addSpawner, 0, 8);
		listPanel.addGB(updateSpawner, 0, 9);
		listPanel.addGB(removeSpawner, 0, 10);
		
		listPanel.addGB(addSquadRandomBot, 0, 11);
		listPanel.addGB(updateSquadRandomBot, 0, 12);
		listPanel.addGB(removeSquadRandomBot, 0, 13);
		
		listPanel.gbConstraints.gridheight = 3;
		listPanel.addGB(waveListScroll, 1, 1);
		listPanel.addGB(waveSpawnListScroll, 1, 5);
		listPanel.addGB(squadRandomListScroll, 1, 8);
	}
	
	//can't override nodepanel's
	protected void initWaveListeners() {
		waveList.addListSelectionListener(event -> { //when a wave is selected from list
			int waveIndex = waveList.getSelectedIndex();
			containingWindow.feedback.setText(" ");
			
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
					waveSpawnList.setSelectedIndex(-1);
					waveSpawnListModel.clear();
					//tfbotBut.setSelected(true);
					//spawnerInfo.setText(noSpawner);
				}
			}
			else { //remember that refreshing the list also causes index to be -1
				//think about this since once something is selected -> selection won't be changed until something else is
				waveBLManager.changeButtonState(States.EMPTY);
				waveSpawnBLManager.changeButtonState(States.DISABLE);
				spawnerBLManager.changeButtonState(States.DISABLE);
			}
		});
		
		addWave.addActionListener(new ActionListener () { //adds a new wave to end of list 
			public void actionPerformed(ActionEvent a) {
				containingWindow.feedback.setText(" ");
				
				currentWaveNode = new WaveNode();
				currentWaveNode.connectNodes(popNode);
				
				waveListModel.addElement("Wave"); //TODO: add counter
				
				waveList.setSelectedIndex(waveListModel.getSize() - 1); //this also clears the ws list
				
				//does all the create a new wavespawn stuff
				//addWaveSpawn.doClick();
			} 
		});
		
		removeWave.addActionListener(new ActionListener () { //remove the selected wave 
			public void actionPerformed(ActionEvent a) {
				List<Node> list = popNode.getChildren();
				containingWindow.feedback.setText(" ");
				
				list.remove(waveList.getSelectedIndex()); 
				waveListModel.remove(waveList.getSelectedIndex());
				
				if(list.size() == 0) {
					resetWaveState();
				}
				else { //set current wave and its subnodes
					waveList.setSelectedIndex(list.size() - 1);
				}				
			}
		});
		
		updateWave.addActionListener(event -> {
			containingWindow.feedback.setText("Wave updated");
			wavePanel.updateNode(currentWaveNode);
			//don't update list since no real name
		});
		
		waveSpawnList.addListSelectionListener(event -> { //when wavespawn is selected from list
			int waveSpawnIndex = waveSpawnList.getSelectedIndex();
			containingWindow.feedback.setText(" ");
			
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
		});
		
		addWaveSpawn.addActionListener(new ActionListener() { //add button is clicked
			public void actionPerformed(ActionEvent a) {
				containingWindow.feedback.setText(" ");
				//System.out.println(currentWSNode);
				
				currentWSNode = new WaveSpawnNode();
				currentWSNode.connectNodes(currentWaveNode);
				
				waveSpawnListModel.addElement("Wavespawn");
				waveSpawnList.setSelectedIndex(waveSpawnListModel.getSize() - 1);
				
				//getWaveSpawnList();
				wsPanel.updatePanel(currentWSNode);
				spawnerInfo.setText(noSpawner);

				//loadBot(true);
				tfbotBut.setSelected(true);
				wsPanel.setVisible(true);
			}
		});
		
		removeWaveSpawn.addActionListener(new ActionListener() { //remove button clicked
			public void actionPerformed(ActionEvent a) {
				List<Node> list = currentWaveNode.getChildren();
				containingWindow.feedback.setText(" ");
				
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
						wsPanel.setVisible(false);
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
				containingWindow.feedback.setText(" ");
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
			waveListModel.addElement("Wave " + Integer.toString(i + 1));
			//waveListModel.addElement("Wave");
		}
		waveList.setSelectedIndex(0);
	}
	
	private void getWaveSpawnList() { //similar to the above, just gets actual names 
		int length = currentWaveNode.getChildren().size();
		
		waveSpawnListModel.clear();

		for(int i = 0; i < length; i++) {
			WaveSpawnNode t = (WaveSpawnNode) currentWaveNode.getChildren().get(i);
			//if(t.containsKey(WaveSpawnNode.NAME) && !((String) t.getValueSingular(WaveSpawnNode.NAME)).isEmpty()) {
			if(t.containsKey(WaveSpawnNode.NAME)) {
				waveSpawnListModel.addElement((String) t.getValueSingular(WaveSpawnNode.NAME));
			}
			else {
				waveSpawnListModel.addElement(Integer.toString(i));
			}
		}
		waveSpawnList.setSelectedIndex(0);
		//waveSpawnListScroll.revalidate();
	}
	
	//reset panels to fresh state whenever a node/its parents are removed
	private void resetWaveState() {
		wavePanel.updatePanel(new WaveNode());
		waveListModel.clear();
		waveBLManager.changeButtonState(States.EMPTY);
		resetWaveSpawnState(States.DISABLE);
	}
	
	private void resetWaveSpawnState(States state) {
		currentWSNode = new WaveSpawnNode();
		wsPanel.updatePanel(currentWSNode);
		waveSpawnListModel.clear();	
		waveSpawnBLManager.changeButtonState(state);
		resetSpawnerState();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = (PopNode) evt.getNewValue();
		getWaveList();
		containingWindow.revalidate();
	}
}
