package engipop;

import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

import engipop.ButtonListManager.States;
import engipop.EngiWindow.NoDeselectionModel;
import engipop.Node.*;

@SuppressWarnings("serial")
public class MissionWindow extends EngiWindow implements PropertyChangeListener{
	
	//EngiPanel missionPanel = new EngiPanel();
	BotPanel botPanel;
	TankPanel tankPanel;
	NodePanelManager spawnerListManager;
	JPanel listPanel;
	JPanel spawnerPanel;
	
	//PopNode popNode;
	List<Node> missionArray;
	MissionNode currentMissionNode = new MissionNode();
	TFBotNode currentBotNode = new TFBotNode();
	TankNode currentTankNode = new TankNode();
	SquadNode currentSquadNode = new SquadNode();
	RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	JButton addMission = new JButton("Add mission");
	JButton updateMission = new JButton("Update mission");
	JButton removeMission = new JButton("Remove mission");
	
	ButtonListManager missionBLManager = new ButtonListManager(addMission,
			updateMission, removeMission);
	
	DefaultListModel<String> missionListModel = new DefaultListModel<String>();
	DefaultComboBoxModel<String> whereModel = new DefaultComboBoxModel<String>();

	JList<String> missionList = new JList<String>(missionListModel);
	JComboBox<String> whereBox = new JComboBox<String>(whereModel);
	JComboBox<String> objectiveBox = new JComboBox<String>(new String[] {"DestroySentries", "Sniper", "Spy", "Engineer"});
	JSpinner initialCooldownSpinner = new JSpinner();
	JSpinner cooldownSpinner = new JSpinner();
	JSpinner beginSpinner = new JSpinner();
	JSpinner runSpinner = new JSpinner();
	JSpinner desiredSpinner = new JSpinner();
	
	
	public MissionWindow(MainWindow mainWindow, SecondaryWindow secondaryWindow) {
		super("Mission editor");
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		int iMin = 0, botMax = 22;
		double dMin = 0.0;
		
		secondaryWindow.addPropertyChangeListener("POPNODE", this);
		missionArray = (List<Node>) mainWindow.getPopNode().getValueSingular(PopNode.MISSION);
		
		SpinnerNumberModel initialCooldownModel = new SpinnerNumberModel(dMin, dMin, null, 1.0);
		SpinnerNumberModel cooldownModel = new SpinnerNumberModel(dMin, dMin, null, 1.0);
		SpinnerNumberModel beginModel = new SpinnerNumberModel(iMin, iMin, null, 1);
		SpinnerNumberModel runModel = new SpinnerNumberModel(iMin, 1, null, 1);
		SpinnerNumberModel desiredModel = new SpinnerNumberModel(iMin, iMin, null, botMax);
		
		JScrollPane missionPane = new JScrollPane(missionList);
		JLabel whereLabel = new JLabel("Where:");
		JLabel objectiveLabel = new JLabel("Objective:");
		JLabel initialCooldownLabel = new JLabel("InitialCooldown:");
		JLabel cooldownLabel = new JLabel("CooldownTime:");
		JLabel beginLabel = new JLabel("BeginAtWave:");
		JLabel runLabel = new JLabel("RunForThisManyWaves:");
		JLabel desiredLabel = new JLabel("DesiredCount:");
		
		JPanel listPanel = spawnerListManager.getListPanel();
		JPanel spawnerPanel = spawnerListManager.getSpawnerPanel();
		botPanel = new BotPanel(this, mainWindow, secondaryWindow);
		tankPanel = new TankPanel(secondaryWindow);
		spawnerListManager = new NodePanelManager(this, botPanel, tankPanel);
		
		initialCooldownSpinner.setModel(initialCooldownModel);
		cooldownSpinner.setModel(cooldownModel);
		beginSpinner.setModel(beginModel);
		runSpinner.setModel(runModel);
		desiredSpinner.setModel(desiredModel);
		
		missionList.setSelectionModel(new NoDeselectionModel());
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tankPanel.setVisible(false);
		whereBox.setEditable(true);
		
		EngiPanel missionPanel = new EngiPanel();
		missionPanel.setLayout(missionPanel.gbLayout);
		EngiPanel buttonPanel = new EngiPanel();
		buttonPanel.setLayout(buttonPanel.gbLayout);
		
		missionPanel.addGB(objectiveLabel, 0, 0);
		missionPanel.addGB(objectiveBox, 1, 0);
		missionPanel.addGB(whereLabel, 2, 0);
		missionPanel.addGB(whereBox, 3, 0);
		missionPanel.addGB(initialCooldownLabel, 0, 1);
		missionPanel.addGB(initialCooldownSpinner, 1, 1);
		missionPanel.addGB(cooldownLabel, 2, 1);
		missionPanel.addGB(cooldownSpinner, 3, 1);
		missionPanel.addGB(beginLabel, 0, 2);
		missionPanel.addGB(beginSpinner, 1, 2);
		missionPanel.addGB(runLabel, 2, 2);
		missionPanel.addGB(runSpinner, 3, 2);
		missionPanel.addGB(desiredLabel, 0, 3);
		missionPanel.addGB(desiredSpinner, 1, 3);
		
		buttonPanel.addGB(addMission, 0, 0);
		buttonPanel.addGB(updateMission, 0, 1);
		buttonPanel.addGB(removeMission, 0, 2);
		buttonPanel.gbConstraints.gridheight = 3;
		buttonPanel.addGB(missionPane, 1, 0);
		
		gbConstraints.gridwidth = 2;
		this.addGB(missionPanel, 0, 1);
		this.addGB(spawnerPanel, 0, 2);
		this.addGB(botPanel, 0, 3);
		this.addGB(tankPanel, 0, 3);
		
		gbConstraints.gridwidth = 1;
		gbConstraints.gridheight = 2;
		addGB(buttonPanel, 2, 1);
		addGB(listPanel, 2, 4);
	}
	
	private void initListeners() {
		missionList.addListSelectionListener(event -> {
			int index = missionList.getSelectedIndex();
			
			spawnerListManager.setSelectedButton(SpawnerType.NONE);
			
			if(index != -1) { //only happens when no nodes
				spawnerListManager.checkSpawner(missionArray.get(index));
			}
			else {
				missionBLManager.changeButtonState(States.EMPTY);
				spawnerListManager.setButtonState(States.DISABLE);
			}
		});
		
		addMission.addActionListener(event -> {
			currentMissionNode = new MissionNode();
			currentBotNode = new TFBotNode();
			missionArray.add(currentMissionNode);
			updatePanel(currentMissionNode);
			
			missionListModel.addElement(MissionNode.DESTROYSENTRIES);
		});
		updateMission.addActionListener(event -> {
			updateNode(currentMissionNode);
			
			String name = (String) currentMissionNode.getValueSingular(MissionNode.OBJECTIVE) + " " +
					currentMissionNode.getValueSingular(MissionNode.BEGINATWAVE).toString();
			missionListModel.set(missionList.getSelectedIndex(), name);
		});
		removeMission.addActionListener(event -> {
			if(missionList.getSelectedIndex() != 1) {
				missionArray.remove(missionList.getSelectedIndex());
				missionListModel.remove(missionList.getSelectedIndex());
				
				if(missionArray.size() == 0) {
					//disable spawner stuff
					missionBLManager.changeButtonState(States.EMPTY);
				}
				else {
					missionList.setSelectedIndex(missionArray.size() - 1);
				}
			}
		});
	}
	
	private void updatePanel(MissionNode mission) {
		
		botPanel.updatePanel(currentBotNode);
	}
	
	private void updateNode(MissionNode mission) {
		
		
	}

	public void propertyChange(PropertyChangeEvent evt) {
		PopNode popNode = (PopNode) evt.getNewValue();
		missionArray = (List<Node>) popNode.getValueSingular(PopNode.MISSION);
		//force a list reload
	}
}
