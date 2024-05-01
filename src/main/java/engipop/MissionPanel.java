package engipop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import engipop.ButtonListManager.States;
import engipop.EngiWindow.NoDeselectionModel;
import engipop.Node.*;

@SuppressWarnings("serial")
public class MissionPanel extends EngiPanel implements PropertyChangeListener{
	
	MainWindow mainWindow;
	EngiPanel missionComponentPanel = new EngiPanel();
	BotPanel botPanel;
	EngiPanel botTankPanel;
	//TankPanel tankPanel;
	NodePanelManager spawnerListManager;
	//JPanel listPanel;
	WherePanel wherePanel = new WherePanel();
	//JPanel spawnerPanel; for now only do bots
	
	List<Object> missionArray = new ArrayList<Object>();
	MissionNode currentMissionNode = new MissionNode();
	TFBotNode currentBotNode = new TFBotNode();
	//TankNode currentTankNode = new TankNode();
	//SquadNode currentSquadNode = new SquadNode();
	//RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	JButton addMission = new JButton("Add mission");
	JButton removeMission = new JButton("Remove mission");
	
	ButtonListManager missionBLManager = new ButtonListManager(addMission, removeMission);
	
	DefaultListModel<String> missionListModel = new DefaultListModel<String>();

	JList<String> missionList = new JList<String>(missionListModel);
	JComboBox<String> objectiveBox = new JComboBox<String>(new String[] {"DestroySentries", "Sniper", "Spy", "Engineer"});
	JSpinner initialCooldownSpinner = new JSpinner();
	JSpinner cooldownSpinner = new JSpinner();
	JSpinner beginSpinner = new JSpinner();
	JSpinner runSpinner = new JSpinner();
	JSpinner desiredSpinner = new JSpinner();
	
	private boolean isNodeResetting = false;
	
	public MissionPanel(MainWindow mainWindow, PopulationPanel popPanel, WaveBarPanel wavebar) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		missionComponentPanel.setLayout(missionComponentPanel.gbLayout);
		missionComponentPanel.gbConstraints.anchor = GridBagConstraints.LINE_START;
		missionComponentPanel.gbConstraints.insets = new Insets(0, 0, 0, 5);
		//this.setBackground(new Color(208, 169, 107));
		//componentPanel.setBackground(new Color(208, 169, 107));
		
		int iMin = 0, botMax = 22;
		double dMin = 0.0;
		
		popPanel.addPropertyChangeListener("POPNODE", this);
		missionArray = Engipop.getPopNode().getListValue(PopNode.MISSION);
		
		SpinnerNumberModel initialCooldownModel = new SpinnerNumberModel(10.0, dMin, null, 1.0);
		SpinnerNumberModel cooldownModel = new SpinnerNumberModel(10.0, dMin, null, 1.0);
		SpinnerNumberModel beginModel = new SpinnerNumberModel(1, iMin, null, 1);
		SpinnerNumberModel runModel = new SpinnerNumberModel(1, iMin, null, 1);
		SpinnerNumberModel desiredModel = new SpinnerNumberModel(0, iMin, botMax, 1); //TODO: remove 0 desired count from printing
		
		JScrollPane missionScroll = new JScrollPane(missionList);
		JLabel whereLabel = new JLabel("Where:");
		JLabel objectiveLabel = new JLabel("Objective:");
		JLabel initialCooldownLabel = new JLabel("InitialCooldown:");
		JLabel cooldownLabel = new JLabel("CooldownTime:");
		JLabel beginLabel = new JLabel("BeginAtWave:");
		JLabel runLabel = new JLabel("RunForThisManyWaves:");
		JLabel desiredLabel = new JLabel("DesiredCount:");
		
		this.mainWindow = mainWindow;
		//tankPanel = new TankPanel(popPanel);
		spawnerListManager = new NodePanelManager(mainWindow, popPanel);
		//listPanel = spawnerListManager.getListPanel();
		botPanel = spawnerListManager.getBotPanel();
		botTankPanel = spawnerListManager.getBotTankPanel();
		//spawnerPanel = spawnerListManager.getSpawnerPanel();
		
		initialCooldownSpinner.setModel(initialCooldownModel);
		cooldownSpinner.setModel(cooldownModel);
		beginSpinner.setModel(beginModel);
		runSpinner.setModel(runModel);
		desiredSpinner.setModel(desiredModel);
		
		//tankPanel.setVisible(false);
		missionList.setSelectionModel(new NoDeselectionModel());
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.setPrototypeCellValue("DestroySentries ");
		objectiveBox.setPrototypeDisplayValue("DestroySentries ");
		missionScroll.setMinimumSize(missionList.getPreferredScrollableViewportSize());
		//missionScroll.setPreferredSize(missionList.getPreferredScrollableViewportSize());
		
		EngiPanel buttonPanel = new EngiPanel();
		buttonPanel.setLayout(buttonPanel.gbLayout);
		buttonPanel.setBackground(new Color(240, 129, 73));
		buttonPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		missionComponentPanel.gbConstraints.ipady = 1;
		missionComponentPanel.addGB(objectiveBox, 1, 0);
		
		missionComponentPanel.gbConstraints.ipady = 0;
		missionComponentPanel.addGB(objectiveLabel, 0, 0);
		
		missionComponentPanel.addGB(whereLabel, 2, 0);
		missionComponentPanel.addGB(initialCooldownLabel, 0, 2);
		missionComponentPanel.addGB(cooldownLabel, 2, 2);
		missionComponentPanel.addGB(beginLabel, 0, 3);
		missionComponentPanel.addGB(runLabel, 2, 3);	
		missionComponentPanel.addGB(desiredLabel, 0, 4);
		missionComponentPanel.addGB(desiredSpinner, 1, 4);
		
		missionComponentPanel.gbConstraints.gridheight = 2;
		missionComponentPanel.addGB(wherePanel, 3, 0);
			
		missionComponentPanel.gbConstraints.gridheight = 1;
		missionComponentPanel.gbConstraints.ipadx = 9;
		missionComponentPanel.addGB(initialCooldownSpinner, 1, 2);
		missionComponentPanel.addGB(cooldownSpinner, 3, 2);
		missionComponentPanel.addGB(beginSpinner, 1, 3);
		missionComponentPanel.addGB(runSpinner, 3, 3);
		
		buttonPanel.addGB(addMission, 1, 0);
		buttonPanel.addGB(removeMission, 1, 2);
		buttonPanel.gbConstraints.gridheight = 3;
		buttonPanel.addGB(missionScroll, 0, 0);
		
		gbConstraints.gridwidth = 2;
		gbConstraints.weighty = 0.01;
		addGB(missionComponentPanel.getDisabledPanel(), 0, 0);
		//this.addGB(spawnerPanel, 0, 2);
		gbConstraints.weighty = 0.99;
		addGB(botTankPanel.getDisabledPanel(), 0, 1);
		
		gbConstraints.weighty = 0;
		gbConstraints.weightx = 0.25;
		gbConstraints.gridwidth = 1;
		addGB(buttonPanel, 2, 0);
		//addGB(listPanel, 2, 1);
		
		missionComponentPanel.getDisabledPanel().setEnabled(false);
		botTankPanel.getDisabledPanel().setEnabled(false);
		missionBLManager.changeButtonState(States.EMPTY);
		spawnerListManager.setButtonState(States.DISABLE);
		initListeners();
	}
	
	private void initListeners() {
		missionList.addListSelectionListener(event -> {
			int index = missionList.getSelectedIndex();
			
			//spawnerListManager.setSelectedButton(SpawnerType.NONE);
			
			if(index != -1) { 
				//spawnerListManager.checkSpawner(missionArray.get(index));
				currentMissionNode = (MissionNode) missionArray.get(index);
				spawnerListManager.setParentNode(currentMissionNode);
				
				missionBLManager.changeButtonState(States.SELECTED);
				missionComponentPanel.getDisabledPanel().setEnabled(true);
				
				if(currentMissionNode.hasChildren()) {
					currentBotNode = (TFBotNode) currentMissionNode.getChildren().get(0);
					botTankPanel.getDisabledPanel().setEnabled(true);
				}
				else {
					currentBotNode = new TFBotNode();
					botTankPanel.getDisabledPanel().setEnabled(false);
				}
				
				updatePanel();
			}
			else { //only happens when no nodes
				missionBLManager.changeButtonState(States.EMPTY);
				spawnerListManager.setButtonState(States.DISABLE);
				botTankPanel.getDisabledPanel().setEnabled(false); //for some reason adding botPanel instead of botTank errors
				missionComponentPanel.getDisabledPanel().setEnabled(false);
			}
		});
		
		addMission.addActionListener(event -> {
			currentMissionNode = new MissionNode();
			currentBotNode = new TFBotNode();
			currentBotNode.connectNodes(currentMissionNode);
			missionArray.add(currentMissionNode);
			
			missionListModel.addElement(MissionNode.DESTROYSENTRIES);
			spawnerListManager.setParentNode(currentMissionNode);
			missionList.setSelectedIndex(missionListModel.getSize() - 1);
		});
		/*
		updateMission.addActionListener(event -> {
			updateNode(currentMissionNode);
			
			String name = (String) currentMissionNode.getValue(MissionNode.OBJECTIVE) + " " +
					currentMissionNode.getValue(MissionNode.BEGINATWAVE).toString();
			missionListModel.set(missionList.getSelectedIndex(), name);
		});
		*/
		removeMission.addActionListener(event -> {
			missionArray.remove(missionList.getSelectedIndex());
			missionListModel.remove(missionList.getSelectedIndex());
			
			missionList.setSelectedIndex(missionArray.size() - 1);
		});
		
		wherePanel.getTable().getSelectionModel().addListSelectionListener(event -> {
			if(isNodeResetting) {
				return;
			}
			List<String> wheres = wherePanel.updateNode();
			if(wheres != null) {
				currentMissionNode.putKey(WaveSpawnNode.WHERE, wherePanel.updateNode());
			}
		});
		
		objectiveBox.addActionListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.OBJECTIVE, objectiveBox.getSelectedItem());
			updateMissionName();
		});
		
		initialCooldownSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.INITIALCOOLDOWN, initialCooldownSpinner.getValue());
		});
		
		cooldownSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.COOLDOWNTIME, cooldownSpinner.getValue());
		});
		
		beginSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.BEGINATWAVE, beginSpinner.getValue());
			updateMissionName();
		});
		
		runSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.RUNFORTHISMANYWAVES, runSpinner.getValue());
		});
		
		desiredSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			currentMissionNode.putKey(MissionNode.DESIREDCOUNT, desiredSpinner.getValue());
		});
	}
	
	private void updatePanel() {
		isNodeResetting = true;
		if(currentMissionNode.containsKey(WaveSpawnNode.WHERE)) {
			wherePanel.updateWhere(currentMissionNode.getListValue(MissionNode.WHERE));
		}
		else {
			wherePanel.clearSelection();
		}
		
		objectiveBox.setSelectedItem(currentMissionNode.getValue(MissionNode.OBJECTIVE));
		initialCooldownSpinner.setValue(currentMissionNode.getValue(MissionNode.INITIALCOOLDOWN));
		cooldownSpinner.setValue(currentMissionNode.getValue(MissionNode.COOLDOWNTIME));
		beginSpinner.setValue(currentMissionNode.getValue(MissionNode.BEGINATWAVE));
		runSpinner.setValue(currentMissionNode.getValue(MissionNode.RUNFORTHISMANYWAVES));
		if(currentMissionNode.containsKey(MissionNode.DESIREDCOUNT)) {
			desiredSpinner.setValue(currentMissionNode.getValue(MissionNode.DESIREDCOUNT));
		}
		else {
			desiredSpinner.setValue(0);
		}
		
		spawnerListManager.loadBot(false, currentBotNode);
		isNodeResetting = false;
	}
	
	private void updateMissionName() {
		String name = (String) currentMissionNode.getValue(MissionNode.OBJECTIVE) + " " +
				currentMissionNode.getValue(MissionNode.BEGINATWAVE).toString();
		missionListModel.set(missionList.getSelectedIndex(), name);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		PopNode popNode = Engipop.getPopNode();
		missionArray = popNode.getListValue(PopNode.MISSION);
		
		isNodeResetting = true;
		missionListModel.clear();
		
		for(Object node : missionArray) {
			MissionNode mission = (MissionNode) node;
			
			missionListModel.addElement((String) mission.getValue(MissionNode.OBJECTIVE));
		}
		
		if(missionArray.size() > 0) {
			missionList.setSelectedIndex(0);
		}
		else {
			missionList.setSelectedIndex(-1);
		}
		isNodeResetting = false;
	}
}
