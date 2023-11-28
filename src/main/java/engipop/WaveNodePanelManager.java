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
	
	
	public WaveNodePanelManager(MainWindow window, WavePanel wavePanel, WaveSpawnPanel wsPanel, BotPanel botPanel, TankPanel tankPanel,
			PopulationPanel secondaryWindow) {
		super(window.getMainPanel(), botPanel, tankPanel);
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
		
		waveList.setPrototypeCellValue("Wave 10");
		waveSpawnList.setPrototypeCellValue("wave06abcde");
		
		addWave.setToolTipText("Creates an empty wave");
		addWaveSpawn.setToolTipText("Creates an empty wavespawn");
		
		currentWaveNode.connectNodes(popNode);
		currentWSNode.connectNodes(currentWaveNode);
		currentBotNode.connectNodes(currentWSNode);
		
		getWaveList();
		
		waveListScroll.setMinimumSize(new Dimension(waveList.getPreferredScrollableViewportSize().width + 20, 
				waveList.getPreferredScrollableViewportSize().height));
		//i don't even know
		waveListScroll.setMaximumSize(new Dimension(waveList.getPreferredScrollableViewportSize().width + 25, 
				waveList.getPreferredScrollableViewportSize().height));
		waveSpawnListScroll.setMinimumSize(new Dimension(waveSpawnList.getPreferredScrollableViewportSize().width + 20, 
				waveSpawnList.getPreferredScrollableViewportSize().height));
		
		listPanel.gbConstraints = new GridBagConstraints(); //dump the one from NodePanelManager
		listPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		listPanel.gbConstraints.insets = new Insets(5, 0, 5, 5);
		
		listPanel.addGB(addWave, 1, 1);
		listPanel.addGB(updateWave, 1, 2);
		listPanel.addGB(removeWave, 1, 3);
		
		listPanel.addGB(addWaveSpawn, 1, 5);
		listPanel.addGB(updateWaveSpawn, 1, 6);
		listPanel.addGB(removeWaveSpawn, 1, 7);
		
		listPanel.addGB(addSpawner, 1, 8);
		listPanel.addGB(updateSpawner, 1, 9);
		listPanel.addGB(removeSpawner, 1, 10);
		
		listPanel.addGB(addSquadRandomBot, 1, 11);
		listPanel.addGB(updateSquadRandomBot, 1, 12);
		listPanel.addGB(removeSquadRandomBot, 1, 13);
		
		listPanel.gbConstraints.gridheight = 3;
		listPanel.addGB(waveListScroll, 0, 1);
		listPanel.addGB(waveSpawnListScroll, 0, 5);
		listPanel.addGB(squadRandomListScroll, 0, 8);
	}
	
	//can't override nodepanel's
	protected void initWaveListeners() {
		waveList.addListSelectionListener(event -> { //when a wave is selected from list
			int waveIndex = waveList.getSelectedIndex();
			containingPanel.feedback.setText(" ");
			
			noneBut.setSelected(true);
			
			if(waveIndex != -1) { //prevents listener fits
				currentWaveNode = (WaveNode) popNode.getChildren().get(waveIndex);
				wavePanel.updatePanel(currentWaveNode);
				waveBLManager.changeButtonState(States.SELECTED);
				wavePanel.setVisible(true);
				
				getWaveSpawnList();
				
				if(currentWaveNode.getChildren().size() > 0) {
					waveSpawnList.setSelectedIndex(-1);
					waveSpawnList.setSelectedIndex(0);
				}
				else { //should only happen if user removes all wavespawns
					//waveSpawnBLManager.changeButtonState(States.EMPTY);
					waveSpawnList.setSelectedIndex(-1);
				}
			}
			else { 
				//think about this since once something is selected -> selection won't be changed until something else is
				waveBLManager.changeButtonState(States.EMPTY);
			}
		});
		
		addWave.addActionListener(new ActionListener () { //adds a new wave to end of list 
			public void actionPerformed(ActionEvent a) {
				containingPanel.feedback.setText(" ");
				
				currentWaveNode = new WaveNode();
				currentWaveNode.connectNodes(popNode);
				
				//remember that refreshing the list also causes index to be -1
				waveListModel.addElement("Wave"); //TODO: add counter
				
				waveList.setSelectedIndex(waveListModel.getSize() - 1); //this also clears the ws list
			} 
		});
		
		removeWave.addActionListener(new ActionListener () { //remove the selected wave 
			public void actionPerformed(ActionEvent a) {
				List<Node> list = popNode.getChildren();
				containingPanel.feedback.setText(" ");
				
				list.remove(waveList.getSelectedIndex()); 
				waveListModel.remove(waveList.getSelectedIndex());	
	
				if(list.size() == 0) {
					resetWaveState();
					wavePanel.setVisible(false);
				}
				else {
					waveList.setSelectedIndex(list.size() - 1);
				}
			}
		});
		
		updateWave.addActionListener(event -> {
			containingPanel.feedback.setText("Wave updated");
			wavePanel.updateNode(currentWaveNode);
			//don't update list since no real name
		});
		
		waveSpawnList.addListSelectionListener(event -> { //when wavespawn is selected from list
			int waveSpawnIndex = waveSpawnList.getSelectedIndex();
			containingPanel.feedback.setText(" ");
			
			noneBut.setSelected(true);
			//TODO: update this since only time there can be nothing selected is if the list is empty
			
			//prevent fits if index is reset
			if(waveSpawnIndex != -1) {
				currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(waveSpawnIndex);
				//currentWSLabel.setText("Editing wavespawn " + waveSpawnListModel.get(waveSpawnIndex));
				wsPanel.setVisible(true);
				
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
				//resetWaveSpawnState(States.EMPTY);
				//resetSpawnerState();
			}
		});
		
		addWaveSpawn.addActionListener(new ActionListener() { //add button is clicked
			public void actionPerformed(ActionEvent a) {
				containingPanel.feedback.setText(" ");
				//System.out.println(currentWSNode);
				
				currentWSNode = new WaveSpawnNode();
				currentWSNode.connectNodes(currentWaveNode);
				
				waveSpawnListModel.addElement("Wavespawn");
				waveSpawnList.setSelectedIndex(waveSpawnListModel.size() - 1);
				
				//getWaveSpawnList();
				//wsPanel.updatePanel(currentWSNode);
				//spawnerInfo.setText(noSpawner);

				//loadBot(true);
				//tfbotBut.setSelected(true);
			}
		});
		
		removeWaveSpawn.addActionListener(new ActionListener() { //remove button clicked
			public void actionPerformed(ActionEvent a) {
				List<Node> list = currentWaveNode.getChildren();
				containingPanel.feedback.setText(" ");
				
				list.remove(waveSpawnList.getSelectedIndex());
				waveSpawnListModel.remove(waveSpawnList.getSelectedIndex());
				//botPanel.updatePanel(new TFBotNode()); //update panel so no dead references
				resetSpawnerState();
				//getWaveSpawnList();
				
				if(list.size() == 0) { //if no wavespawns again
					resetWaveSpawnState(States.EMPTY);
				} 
				else {
					waveSpawnList.setSelectedIndex(list.size() - 1);
				}
			}
		});
		
		updateWaveSpawn.addActionListener(new ActionListener() { //update wavespawn button clicked
			public void actionPerformed(ActionEvent a) {
				containingPanel.feedback.setText(" ");
				wsPanel.updateNode(currentWSNode);
				
				if(currentWSNode.getValue(WaveSpawnNode.NAME) != null) {
					waveSpawnListModel.set(waveSpawnList.getSelectedIndex(), (String) currentWSNode.getValue(WaveSpawnNode.NAME));
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
				waveSpawnListModel.addElement((String) t.getValue(WaveSpawnNode.NAME));
			}
			else {
				waveSpawnListModel.addElement(Integer.toString(i));
			}
		}
		//waveSpawnList.setSelectedIndex(0);
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
		wsPanel.setVisible(false);
		currentWSNode = new WaveSpawnNode();
		wsPanel.updatePanel(currentWSNode);
		waveSpawnListModel.clear();
		waveSpawnBLManager.changeButtonState(state);
		resetSpawnerState();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = (PopNode) evt.getNewValue();
		getWaveList();
		containingPanel.revalidate();
	}
}
