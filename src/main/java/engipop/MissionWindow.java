package engipop;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import engipop.ButtonListManager.States;
import engipop.EngiWindow.NoDeselectionModel;
import engipop.Node.*;

@SuppressWarnings("serial")
public class MissionWindow extends EngiWindow implements PropertyChangeListener{
	
	EngiPanel missionPanel = new EngiPanel();
	BotPanel botPanel;
	TankPanel tankPanel;
	NodePanelManager spawnerListManager;
	JPanel listPanel;
	//JPanel spawnerPanel; for now only do bots
	
	//PopNode popNode;
	List<MissionNode> missionArray = new ArrayList<MissionNode>();
	MissionNode currentMissionNode = new MissionNode();
	TFBotNode currentBotNode = new TFBotNode();
	//TankNode currentTankNode = new TankNode();
	//SquadNode currentSquadNode = new SquadNode();
	//RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
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
		setSize(1100, 600);
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		int iMin = 0, botMax = 22;
		double dMin = 0.0;
		
		secondaryWindow.addPropertyChangeListener("POPNODE", this);
		for(Object item :  mainWindow.getPopNode().getListValue(PopNode.MISSION)) {
			missionArray.add((MissionNode) item);
		}
		
		SpinnerNumberModel initialCooldownModel = new SpinnerNumberModel(dMin, dMin, null, 1.0);
		SpinnerNumberModel cooldownModel = new SpinnerNumberModel(dMin, dMin, null, 1.0);
		SpinnerNumberModel beginModel = new SpinnerNumberModel(1, iMin, null, 1);
		SpinnerNumberModel runModel = new SpinnerNumberModel(1, iMin, null, 1);
		SpinnerNumberModel desiredModel = new SpinnerNumberModel(1, iMin, null, botMax);
		
		JScrollPane missionScroll = new JScrollPane(missionList);
		JLabel whereLabel = new JLabel("Where:");
		JLabel objectiveLabel = new JLabel("Objective:");
		JLabel initialCooldownLabel = new JLabel("InitialCooldown:");
		JLabel cooldownLabel = new JLabel("CooldownTime:");
		JLabel beginLabel = new JLabel("BeginAtWave:");
		JLabel runLabel = new JLabel("RunForThisManyWaves:");
		JLabel desiredLabel = new JLabel("DesiredCount:");
		feedback = new JLabel();
		
		botPanel = new BotPanel(this, mainWindow, secondaryWindow);
		tankPanel = new TankPanel(secondaryWindow);
		spawnerListManager = new NodePanelManager(this, botPanel, tankPanel);
		JPanel listPanel = spawnerListManager.getListPanel();
		//JPanel spawnerPanel = spawnerListManager.getSpawnerPanel();
		
		initialCooldownSpinner.setModel(initialCooldownModel);
		cooldownSpinner.setModel(cooldownModel);
		beginSpinner.setModel(beginModel);
		runSpinner.setModel(runModel);
		desiredSpinner.setModel(desiredModel);
		
		missionList.setSelectionModel(new NoDeselectionModel());
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tankPanel.setVisible(false);
		whereBox.setEditable(true);
		missionScroll.setMinimumSize(missionScroll.getPreferredSize());
		
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
		buttonPanel.addGB(missionScroll, 1, 0);
		
		this.addGB(feedback, 0, 0);
		gbConstraints.gridwidth = 2;
		gbConstraints.gridheight = 2;
		this.addGB(missionPanel, 0, 1);
		//this.addGB(spawnerPanel, 0, 2);
		this.addGB(botPanel, 0, 3);
		//this.addGB(tankPanel, 0, 3);
		
		gbConstraints.gridwidth = 1;
		addGB(buttonPanel, 2, 1);
		addGB(listPanel, 2, 3);
		
		initListeners();
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void initListeners() {
		missionList.addListSelectionListener(event -> {
			int index = missionList.getSelectedIndex();
			
			//spawnerListManager.setSelectedButton(SpawnerType.NONE);
			
			if(index != -1) { 
				//spawnerListManager.checkSpawner(missionArray.get(index));
				currentMissionNode = missionArray.get(index);
				currentBotNode = (TFBotNode) missionArray.get(index).getChildren().get(0);
			}
			else { //only happens when no nodes
				missionBLManager.changeButtonState(States.EMPTY);
				spawnerListManager.setButtonState(States.DISABLE);
				botPanel.setVisible(false);
				missionPanel.setVisible(false);
			}
		});
		
		addMission.addActionListener(event -> {
			currentMissionNode = new MissionNode();
			currentBotNode = new TFBotNode();
			currentBotNode.connectNodes(currentMissionNode);
			missionArray.add(currentMissionNode);
			updatePanel(currentMissionNode);
			
			missionListModel.addElement(MissionNode.DESTROYSENTRIES);
			missionList.setSelectedIndex(missionListModel.getSize() - 1);
			
			if((missionListModel.getSize()) == 1) {
				missionBLManager.changeButtonState(States.SELECTED);
				spawnerListManager.setButtonState(States.FILLEDSLOT);
				botPanel.setVisible(true);
				missionPanel.setVisible(true);
			}
		});
		updateMission.addActionListener(event -> {
			updateNode(currentMissionNode);
			
			String name = (String) currentMissionNode.getValue(MissionNode.OBJECTIVE) + " " +
					currentMissionNode.getValue(MissionNode.BEGINATWAVE).toString();
			missionListModel.set(missionList.getSelectedIndex(), name);
		});
		removeMission.addActionListener(event -> {
			if(missionList.getSelectedIndex() != 1) {
				missionArray.remove(missionList.getSelectedIndex());
				missionListModel.remove(missionList.getSelectedIndex());
				
				missionList.setSelectedIndex(missionArray.size() - 1);
			}
		});
	}
	
	private void updatePanel(MissionNode mission) {
		whereBox.setSelectedItem(mission.getValue(MissionNode.WHERE));
		objectiveBox.setSelectedItem(mission.getValue(MissionNode.OBJECTIVE));
		initialCooldownSpinner.setValue(mission.getValue(MissionNode.INITIALCOOLDOWN));
		cooldownSpinner.setValue(mission.getValue(MissionNode.COOLDOWNTIME));
		beginSpinner.setValue(mission.getValue(MissionNode.BEGINATWAVE));
		runSpinner.setValue(mission.getValue(MissionNode.RUNFORTHISMANYWAVES));
		desiredSpinner.setValue(mission.getValue(MissionNode.DESIREDCOUNT));
		
		spawnerListManager.loadBot(false, currentBotNode);
	}
	
	private void updateNode(MissionNode mission) {
		mission.putKey(MissionNode.WHERE, whereBox.getSelectedItem());
		mission.putKey(MissionNode.OBJECTIVE, objectiveBox.getSelectedItem());
		mission.putKey(MissionNode.INITIALCOOLDOWN, initialCooldownSpinner.getValue());
		mission.putKey(MissionNode.COOLDOWNTIME, cooldownSpinner.getValue());
		mission.putKey(MissionNode.BEGINATWAVE, beginSpinner.getValue());
		mission.putKey(MissionNode.RUNFORTHISMANYWAVES, runSpinner.getValue());
		mission.putKey(MissionNode.DESIREDCOUNT, desiredSpinner.getValue());
		
		botPanel.updateNode(currentBotNode);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		PopNode popNode = (PopNode) evt.getNewValue();
		if(!popNode.containsKey(PopNode.MISSION)) {
			return;
		}
		
		for(Object node : popNode.getListValue(PopNode.MISSION)) {
			missionArray.add((MissionNode) node);
		}
		//force a list reload
	}
}
