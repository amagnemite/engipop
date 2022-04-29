package engipop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.*;

import engipop.Tree.RelayNode;
import engipop.Tree.WaveSpawnNode;

public class WaveSpawnPanel extends EngiPanel { //panel for creating wavespawns
	
	GridBagLayout gbLayout = new GridBagLayout();
	
	JTextField wsNameField = new JTextField(20);
	JTextField wsDeadField = new JTextField(20);
	JTextField wsSpawnField = new JTextField(20);
	
	DefaultComboBoxModel<String> whereModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> firstModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> lastModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	
	JComboBox<String> wsWhereBox = new JComboBox<String>(whereModel); //add spawn names
	JComboBox<String> startRelay = new JComboBox<String>(startModel);
	JComboBox<String> firstRelay = new JComboBox<String>(firstModel);
	JComboBox<String> lastRelay = new JComboBox<String>(lastModel);
	JComboBox<String> doneRelay = new JComboBox<String>(doneModel);
	
	JSpinner wsTotalSpin = new JSpinner();
	JSpinner wsMaxSpin = new JSpinner();
	JSpinner wsSpawnSpin = new JSpinner();
	JSpinner wsStartSpin = new JSpinner();
	JSpinner wsBetweenSpin = new JSpinner();
	JSpinner wsCurrSpin = new JSpinner();
	
	JCheckBox wsDeaths = new JCheckBox("AfterDeath?");
	JLabel wsBetwSpawns = new JLabel("WaitBetweenSpawns: ");
	
	JCheckBox isSupport = new JCheckBox("Support?");
	JCheckBox isLimited = new JCheckBox("Limited?");
	JCheckBox doStart = new JCheckBox("StartWaveOutput?");
	JCheckBox doFirst = new JCheckBox("FirstSpawnOutput?");
	JCheckBox doLast = new JCheckBox("LastSpawnOutput?");
	JCheckBox doDone = new JCheckBox("DoneOutput?");
	
	public WaveSpawnPanel() {
		int initial = 1, min = 0, totalMax = 999, activeMax = 22, incr = 1, currMax = 30000, currIncr = 50;
		double initWait = 0.0, minWait = 0.0, maxWait = 1000.0, incrWait = 1.0;
		
		JLabel label = new JLabel("WaveSpawn editor");
		
		SpinnerNumberModel totalModel = new SpinnerNumberModel(initial, min, totalMax, incr); //totalcount bots
		SpinnerNumberModel spawnModel = new SpinnerNumberModel(initial, min, activeMax, incr); //spawncount
		SpinnerNumberModel maxModel = new SpinnerNumberModel(initial, min, activeMax, incr); //maxactive
		SpinnerNumberModel currModel = new SpinnerNumberModel(min, min, currMax, currIncr); //money
		SpinnerNumberModel betweenModel = new SpinnerNumberModel(initWait, minWait, maxWait, incrWait); //waitbeforestarting
		SpinnerNumberModel startModel = new SpinnerNumberModel(initWait, minWait, maxWait, incrWait); //waitbetweenspawns
		
		wsTotalSpin.setModel(totalModel);
		wsMaxSpin.setModel(maxModel);
		wsSpawnSpin.setModel(spawnModel);
		wsCurrSpin.setModel(currModel);
		wsStartSpin.setModel(startModel);
		wsBetweenSpin.setModel(betweenModel);
		
		gb = new GridBagConstraints();
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		
		startRelay.setEditable(true);
		firstRelay.setEditable(true);
		lastRelay.setEditable(true);
		doneRelay.setEditable(true);
		wsWhereBox.setEditable(true);
		
		JLabel wsName = new JLabel("Name: ");
		JLabel wsWhere = new JLabel("Where: ");
		JLabel wsTotalCount = new JLabel("TotalCount: ");
		JLabel wsMaxActive = new JLabel("MaxActive: ");
		JLabel wsSpawnCount = new JLabel("SpawnCount: ");
		JLabel wsBeforeStart = new JLabel("WaitBeforeStarting: ");
		JLabel wsCurrency = new JLabel("TotalCurrency: ");
		JLabel wsAllDead = new JLabel("WaitForAllDead: ");
		JLabel wsAllSpawned = new JLabel("WaitForAllSpawned: ");
		JLabel startWaveLabel = new JLabel("StartWaveOutput: ");
		JLabel firstLabel = new JLabel("FirstSpawnOutput: ");
		JLabel lastLabel = new JLabel("LastSpawnOutput: ");
		JLabel doneLabel = new JLabel("DoneOutput: ");
		
		wsDeaths.addItemListener(new ItemListener() { //update betweenspawns as appropriate
			public void itemStateChanged(ItemEvent e) {
				updateBetweenSpawns();
			}
		});
		
		startWaveLabel.setVisible(false); //all the relays are optional so set invis by default
		startRelay.setVisible(false);
		doStart.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doStart.isSelected()) {
					startWaveLabel.setVisible(true);
					startRelay.setVisible(true);
				}
				else {
					startWaveLabel.setVisible(false);
					startRelay.setVisible(false);
				}
			}
		});
		
		firstLabel.setVisible(false); //all the relays are optional so set invis by default
		firstRelay.setVisible(false);
		doFirst.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doFirst.isSelected()) {
					firstLabel.setVisible(true); 
					firstRelay.setVisible(true);
				}
				else {
					firstLabel.setVisible(false);
					firstRelay.setVisible(false);
				}
			}
		});
		
		lastLabel.setVisible(false); //all the relays are optional so set invis by default
		lastRelay.setVisible(false);
		doLast.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doLast.isSelected()) {
					lastLabel.setVisible(true);
					lastRelay.setVisible(true);
				}
				else {
					lastLabel.setVisible(false);
					lastRelay.setVisible(false);
				}
			}
		});
		
		doneLabel.setVisible(false); //all the relays are optional so set invis by default
		doneRelay.setVisible(false);
		doDone.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doDone.isSelected()) {
					doneLabel.setVisible(true); 
					doneRelay.setVisible(true);
				}
				else {
					doneLabel.setVisible(false); 
					doneRelay.setVisible(false);
				}
			}
		});
		
		isLimited.setVisible(false);
		isSupport.addItemListener(new ItemListener() { //don't need to show limited unless support
			public void itemStateChanged(ItemEvent e) {
				if(isSupport.isSelected()) {
					isLimited.setVisible(true);
				}
				else {
					isLimited.setVisible(false);
				}
			}
		});
		
		addGB(label, 0, 0);
		
		addGB(wsName, 0, 1);
		
		addGB(wsWhere, 2, 1);
		
		addGB(wsTotalCount, 0, 3);
		addGB(wsTotalSpin, 1, 3);
		addGB(wsMaxActive, 2, 3);
		addGB(wsMaxSpin, 3, 3);		
		addGB(wsSpawnCount, 4, 3);
		addGB(wsSpawnSpin, 5, 3);	
		
		addGB(wsBeforeStart, 0, 6);
		addGB(wsStartSpin, 1, 6);
		
		addGB(isSupport, 2, 6);
		addGB(isLimited, 3, 6);
		
		addGB(wsBetwSpawns, 0, 7);
		addGB(wsBetweenSpin, 1, 7);		
		addGB(wsDeaths, 2, 7);
		addGB(wsCurrency, 0, 8);
		addGB(wsCurrSpin, 1, 8);	
		addGB(wsAllDead, 0, 9);

		addGB(wsAllSpawned, 0, 10);
			
		addGB(wsNameField, 1, 1);
		addGB(wsWhereBox, 3, 1);
		addGB(wsDeadField, 1, 9);	
		addGB(wsSpawnField, 1, 10);
		
		addGB(doFirst, 0, 11);
		addGB(doStart, 1, 11);
		addGB(doLast, 2, 11);
		addGB(doDone, 3, 11);
		
		addGB(startWaveLabel, 0, 12);
		addGB(startRelay, 1, 12);
		addGB(firstLabel, 2, 12);
		addGB(firstRelay, 3, 12);
		
		addGB(lastLabel, 0, 13);
		addGB(lastRelay, 1, 13);
		addGB(doneLabel, 2, 13);
		addGB(doneRelay, 3, 13);
	}
	
	public void updatePanel(WaveSpawnNode wsn) { //sets panel components to reflect the node
		wsNameField.setText(wsn.getName());
		wsWhereBox.setSelectedItem(wsn.getWhere());
		wsTotalSpin.setValue(wsn.getTotalCount());
		wsMaxSpin.setValue(wsn.getMaxActive());
		wsSpawnSpin.setValue(wsn.getSpawnCount());
		wsStartSpin.setValue(wsn.getBeforeStarting());
		wsBetweenSpin.setValue(wsn.getBetweenSpawns());
		wsDeaths.setSelected(wsn.getBetweenDeaths());
		updateBetweenSpawns();
		wsCurrSpin.setValue(wsn.getCurrency());
		wsDeadField.setText(wsn.getWaitDead());
		wsSpawnField.setText(wsn.getWaitSpawned());
		isSupport.setSelected(wsn.getSupport());
		isLimited.setSelected(wsn.getSupportLimited());
		
		//relays aren't mandatory, so only show them if they exist
		if(wsn.getStart() != null) {
			doStart.setSelected(true);
			startRelay.setSelectedItem(wsn.getStart().getTarget());
		}
		else {
			doStart.setSelected(false);
		}
		if(wsn.getFirst() != null) { //first
			doFirst.setSelected(true);
			firstRelay.setSelectedItem(wsn.getFirst().getTarget());
		}
		else {
			doFirst.setSelected(false);
		}
		if(wsn.getLast() != null) { //last
			doLast.setSelected(true);
			lastRelay.setSelectedItem(wsn.getLast().getTarget());
		}
		else {
			doLast.setSelected(false);
		}
		if(wsn.getDone() != null) { //done
			doDone.setSelected(true);
			doneRelay.setSelectedItem(wsn.getDone().getTarget());
		}
		else {
			doDone.setSelected(false);
		}
	}
	
	public void updateNode(WaveSpawnNode wsn) { //update node to reflect panel
		/*
		if(wsNameField.getText().equals("")) { //prevent funny unclickable list
			wsn.setName(Integer.toString(nodeCount));
			nodeCount++;
		}
		else {
			wsn.setName(wsNameField.getText());
		} */
		wsn.setName(wsNameField.getText());
		wsn.setWhere((String) wsWhereBox.getSelectedItem());
		wsn.setTotalCount((int) wsTotalSpin.getValue());
		wsn.setMaxActive((int) wsMaxSpin.getValue());
		wsn.setSpawnCount((int) wsSpawnSpin.getValue());
		wsn.setBeforeStarting((double) wsStartSpin.getValue());
		wsn.setBetweenSpawns((double) wsBetweenSpin.getValue());
		wsn.setBetweenDeaths(wsDeaths.isSelected()); //this needs sanity checking
		wsn.setCurrency((int) wsCurrSpin.getValue());
		wsn.setWaitDead(wsDeadField.getText());
		wsn.setWaitSpawned(wsSpawnField.getText());
		wsn.setSupport(isSupport.isSelected());
		wsn.setSupportLimited(isLimited.isSelected());
		
		if(doStart.isSelected()) {
			if(wsn.getStart() == null) { //make relays if data is entered and no relay exists
				wsn.setStart(new RelayNode()); 
			}
			wsn.getStart().setTarget((String) startRelay.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			wsn.setStart(null); 
		}
		if(doFirst.isSelected()) { //first
			if(wsn.getFirst() == null) {
				wsn.setFirst(new RelayNode()); 
			}
			wsn.getFirst().setTarget((String) firstRelay.getSelectedItem());
		}
		else { 
			wsn.setFirst(null); 
		}
		if(doLast.isSelected()) { //last
			if(wsn.getLast() == null) {
				wsn.setLast(new RelayNode()); 
			}
			wsn.getLast().setTarget((String) lastRelay.getSelectedItem());
		}
		else {
			wsn.setLast(null);
		}
		if(doDone.isSelected()) { //done
			if(wsn.getDone() == null) {
				wsn.setDone(new RelayNode()); 
			}
			wsn.getDone().setTarget((String) doneRelay.getSelectedItem());
		}
		else {
			wsn.setDone(null);
		}
	}
	
	private void updateBetweenSpawns() { //update text to match wsdeaths state
		if(wsDeaths.isSelected()) {
			wsBetwSpawns.setText("WaitBetweenSpawnsAfterDeath: ");
		}
		else {
			wsBetwSpawns.setText("WaitBetweenSpawns: ");
		}
	}
	
	public void setWhere(List<String> spawns) {
		whereModel.removeAllElements();
		
		for(String s : spawns) {
			whereModel.addElement(s);
		}
	}
	
	public void setRelay(List<String> list) { //update relay list and attach to all the boxes
		startModel.removeAllElements();
		firstModel.removeAllElements();
		lastModel.removeAllElements();
		doneModel.removeAllElements();
		
		for(String s : list) {
			startModel.addElement(s);
			firstModel.addElement(s);
			lastModel.addElement(s);
			doneModel.addElement(s);
		}
	}
}
