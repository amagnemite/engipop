package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import engipop.Node.RelayNode;
import engipop.Node.TFBotNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;

@SuppressWarnings("serial")
public class WaveSpawnPanel extends EngiPanel implements PropertyChangeListener { //panel for creating wavespawns
	
	private static final int MIN = 0;
	
	JTextField wsNameField = new JTextField(20);
	JTextField wsDeadField = new JTextField(20);
	JTextField wsSpawnField = new JTextField(20);
	JTextField templateField = new JTextField(20);
	
	//DefaultComboBoxModel<String> whereModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> firstModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> lastModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	DefaultTableModel whereModel = new DefaultTableModel(0, 1);
	
	//JComboBox<String> wsWhereBox = new JComboBox<String>(whereModel);
	JTable whereTable = new JTable(whereModel);
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
	
	public WaveSpawnPanel(SecondaryWindow secondaryWindow) {
		int initial = 1, totalMax = 999, activeMax = 22, incr = 1, currMax = 30000, currIncr = 50;
		double initWait = 0.0, minWait = 0.0, maxWait = 1000.0, incrWait = 1.0;
		
		secondaryWindow.addPropertyChangeListener(WaveNode.WAVESPAWN, this);
		
		JButton addWhereRow = new JButton("+");
		JButton removeWhereRow = new JButton("-");
		
		JLabel label = new JLabel("WaveSpawn editor");
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
		JLabel templateLabel = new JLabel("Template: ");
		
		SpinnerNumberModel totalModel = new SpinnerNumberModel(initial, MIN, totalMax, incr); //totalcount bots
		SpinnerNumberModel spawnModel = new SpinnerNumberModel(initial, MIN, activeMax, incr); //spawncount
		SpinnerNumberModel maxModel = new SpinnerNumberModel(initial, MIN, activeMax, incr); //maxactive
		SpinnerNumberModel currModel = new SpinnerNumberModel(MIN, MIN, currMax, currIncr); //money
		SpinnerNumberModel betweenModel = new SpinnerNumberModel(initWait, minWait, maxWait, incrWait); //waitbeforestarting
		SpinnerNumberModel startModel = new SpinnerNumberModel(initWait, minWait, maxWait, incrWait); //waitbetweenspawns
		
		wsTotalSpin.setModel(totalModel);
		wsMaxSpin.setModel(maxModel);
		wsSpawnSpin.setModel(spawnModel);
		wsCurrSpin.setModel(currModel);
		wsStartSpin.setModel(startModel);
		wsBetweenSpin.setModel(betweenModel);
		
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 10);
		
		startRelay.setEditable(true);
		firstRelay.setEditable(true);
		lastRelay.setEditable(true);
		doneRelay.setEditable(true);
		removeWhereRow.setEnabled(false);
		
		wsNameField.setMinimumSize(wsNameField.getPreferredSize());
		whereTable.setMinimumSize(whereTable.getPreferredSize());
		wsDeadField.setMinimumSize(wsDeadField.getPreferredSize());
		wsSpawnField.setMinimumSize(wsSpawnField.getPreferredSize());
		
		whereModel.addRow(new String[] {""});
		
		wsDeaths.addItemListener(event -> { //update betweenspawns as appropriate
			updateBetweenSpawns();
		});
		
		//all the relays are optional so set invis by default
		setComponentAndLabelVisible(startWaveLabel, startRelay, false);
		doStart.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doStart.isSelected()) {
					setComponentAndLabelVisible(startWaveLabel, startRelay, true);
				}
				else {
					setComponentAndLabelVisible(startWaveLabel, startRelay, false);
				}
			}
		});
		
		setComponentAndLabelVisible(firstLabel, firstRelay, false);
		doFirst.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doFirst.isSelected()) {
					setComponentAndLabelVisible(firstLabel, firstRelay, true);
				}
				else {
					setComponentAndLabelVisible(firstLabel, firstRelay, false);
				}
			}
		});
		
		setComponentAndLabelVisible(lastLabel, lastRelay, false);
		doLast.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doLast.isSelected()) {
					setComponentAndLabelVisible(lastLabel, lastRelay, true);
				}
				else {
					setComponentAndLabelVisible(lastLabel, lastRelay, false);
				}
			}
		});
		
		setComponentAndLabelVisible(doneLabel, doneRelay, false);
		doDone.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doDone.isSelected()) {
					setComponentAndLabelVisible(doneLabel, doneRelay, true);
				}
				else {
					setComponentAndLabelVisible(doneLabel, doneRelay, false);
				}
			}
		});
		
		isLimited.setVisible(false);
		isSupport.addItemListener(event -> { //don't need to show limited unless support
			if(isSupport.isSelected()) {
				isLimited.setVisible(true);
			}
			else {
				isLimited.setVisible(false);
			}
		});
		
		whereTable.getSelectionModel().addListSelectionListener(event -> {
			if(whereTable.getSelectedRowCount() == 1) {
				removeWhereRow.setEnabled(true);
			}
			else {
				removeWhereRow.setEnabled(false);
			}
		});
		addWhereRow.addActionListener(event -> {
			whereModel.addRow(new String[] {""});
		});
		removeWhereRow.addActionListener(event -> {
			whereModel.removeRow(whereTable.getSelectedRow());
		});
		
		addGB(label, 0, 0);
		
		addGB(wsName, 0, 1);
		addGB(wsWhere, 2, 1);
		addGB(wsNameField, 1, 1);
		addGB(whereTable, 3, 1);	
		
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
		addGB(wsAllSpawned, 2, 9);
		addGB(wsDeadField, 1, 9);	
		addGB(wsSpawnField, 3, 9);
		
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
		//wsn.printKeyVals();
		wsNameField.setText((String) wsn.getValue(WaveSpawnNode.NAME));
		
		whereTable.clearSelection();
		if(wsn.containsKey(WaveSpawnNode.WHERE)) {
			List<Object> wheres = new ArrayList<Object>();
			wheres.addAll(wsn.getListValue(WaveSpawnNode.WHERE));
			
			//select all the wheres already in model
			for(int i = 0; i < whereModel.getRowCount(); i++) {
				if(wheres.contains(whereModel.getValueAt(i, 0))) {
					whereTable.changeSelection(i, 1, true, false);
					wheres.remove(whereModel.getValueAt(i, 0));
				}
			}
			
			//then add new tags that weren't added
			for(Object newWhere : wheres) {
				whereModel.addRow(new String[] {(String) newWhere});
				whereTable.changeSelection(whereModel.getRowCount() - 1, 0, true, false);
			}
		}
		
		wsTotalSpin.setValue(wsn.getValue(WaveSpawnNode.TOTALCOUNT));
		wsMaxSpin.setValue(wsn.getValue(WaveSpawnNode.MAXACTIVE));
		wsSpawnSpin.setValue(wsn.getValue(WaveSpawnNode.SPAWNCOUNT));
		wsStartSpin.setValue(wsn.getValue(WaveSpawnNode.WAITBEFORESTARTING));
		wsBetweenSpin.setValue(wsn.getValue(WaveSpawnNode.WAITBETWEENSPAWNS));
		wsDeaths.setSelected(wsn.getBetweenDeaths());
		updateBetweenSpawns();
		wsCurrSpin.setValue(wsn.getValue(WaveSpawnNode.TOTALCURRENCY));
		wsDeadField.setText((String) wsn.getValue(WaveSpawnNode.WAITFORALLDEAD));
		wsSpawnField.setText((String) wsn.getValue(WaveSpawnNode.WAITFORALLSPAWNED));
		isSupport.setSelected((Boolean) wsn.getValue(WaveSpawnNode.SUPPORT));
		isLimited.setSelected(wsn.getSupportLimited());
		
		//relays aren't mandatory, so only show them if they exist
		if(wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT) != null) {
			doStart.setSelected(true);
			startRelay.setSelectedItem(((RelayNode) wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT)).getValue(RelayNode.TARGET));
		}
		else {
			doStart.setSelected(false);
		}
		if(wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT) != null) { //first
			doFirst.setSelected(true);
			firstRelay.setSelectedItem(((RelayNode) wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT)).getValue(RelayNode.TARGET));
		}
		else {
			doFirst.setSelected(false);
		}
		if(wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT) != null) { //last
			doLast.setSelected(true);
			lastRelay.setSelectedItem(((RelayNode) wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT)).getValue(RelayNode.TARGET));
		}
		else {
			doLast.setSelected(false);
		}
		if(wsn.getValue(WaveSpawnNode.DONEOUTPUT) != null) { //done
			doDone.setSelected(true);
			doneRelay.setSelectedItem(((RelayNode) wsn.getValue(WaveSpawnNode.DONEOUTPUT)).getValue(RelayNode.TARGET));
		}
		else {
			doDone.setSelected(false);
		}
	}
	
	public void updateNode(WaveSpawnNode wsn) { //update node to reflect panel
		wsn.putKey(WaveSpawnNode.NAME, wsNameField.getText());
		
		List<String> wheres = new ArrayList<String>(4);
		for(int row : whereTable.getSelectedRows()) {
			wheres.add((String) whereTable.getValueAt(row, 0));
		}
		wsn.putKey(WaveSpawnNode.WHERE, wheres);
		
		wsn.putKey(WaveSpawnNode.TOTALCOUNT, wsTotalSpin.getValue());
		wsn.putKey(WaveSpawnNode.MAXACTIVE, wsMaxSpin.getValue());
		wsn.putKey(WaveSpawnNode.SPAWNCOUNT, wsSpawnSpin.getValue());
		wsn.putKey(WaveSpawnNode.WAITBEFORESTARTING, wsStartSpin.getValue());
		wsn.putKey(WaveSpawnNode.TOTALCURRENCY, wsCurrSpin.getValue());
		wsn.putKey(WaveSpawnNode.WAITFORALLDEAD, wsDeadField.getText());
		wsn.putKey(WaveSpawnNode.WAITFORALLSPAWNED, wsSpawnField.getText());
		wsn.putKey(WaveSpawnNode.SUPPORT, isSupport.isSelected());
		
		if(wsDeaths.isSelected()) { //this needs sanity checking
			wsn.removeKey(WaveSpawnNode.WAITBETWEENSPAWNS);
			wsn.putKey(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH, wsBetweenSpin.getValue());
		}
		else {
			wsn.removeKey(WaveSpawnNode.WAITBETWEENSPAWNSAFTERDEATH);
			wsn.putKey(WaveSpawnNode.WAITBETWEENSPAWNS, wsBetweenSpin.getValue());
		}
		wsn.setBetweenDeaths(wsDeaths.isSelected());
		
		if(isLimited.isSelected()) {
			wsn.putKey(WaveSpawnNode.SUPPORT, false);
		}
		else {
			wsn.putKey(WaveSpawnNode.SUPPORT, true);
		}
		wsn.setSupportLimited(isLimited.isSelected());
		
		
		if(doStart.isSelected()) {
			if(wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT) == null) { //make relays if data is entered and no relay exists
				wsn.putKey(WaveSpawnNode.STARTWAVEOUTPUT, new RelayNode()); 
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT)).putKey(RelayNode.TARGET, (String) startRelay.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			//this does mean the node itself is now thrown out, so may consider removing checking if the
			//node is null above
			wsn.removeKey(WaveSpawnNode.STARTWAVEOUTPUT);
		}
		if(doFirst.isSelected()) { //first
			if(wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.FIRSTSPAWNOUTPUT, new RelayNode()); 
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT)).putKey(RelayNode.TARGET, (String) firstRelay.getSelectedItem());
		}
		else { 
			wsn.removeKey(WaveSpawnNode.FIRSTSPAWNOUTPUT);
		}
		if(doLast.isSelected()) { //last
			if(wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.LASTSPAWNOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT)).putKey(RelayNode.TARGET, (String) lastRelay.getSelectedItem());
		}
		else {
			wsn.removeKey(WaveSpawnNode.LASTSPAWNOUTPUT);
		}
		if(doDone.isSelected()) { //done
			if(wsn.getValue(WaveSpawnNode.DONEOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.DONEOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.DONEOUTPUT)).putKey(RelayNode.TARGET, (String) doneRelay.getSelectedItem());
		}
		else {
			wsn.removeKey(WaveSpawnNode.DONEOUTPUT);
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
		for(int i = 0; i < whereModel.getRowCount(); i++) {
			whereModel.removeRow(0);
		}
		
		for(String s : spawns) {
			whereModel.addRow(new String[] {s});
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

	//get ws relay and where from secondarywindow
	//once again consider checking casts
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(SecondaryWindow.WAVESPAWNRELAY)) {
			setRelay((List<String>) evt.getNewValue());
		}
		else if(evt.getPropertyName().equals(SecondaryWindow.BOTSPAWNS)) {
			setWhere((List<String>) evt.getNewValue());
		}
	}
}
