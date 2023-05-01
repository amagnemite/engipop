package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

import engipop.Node.RelayNode;
import engipop.Node.WaveSpawnNode;

@SuppressWarnings("serial")
public class WaveSpawnPanel extends EngiPanel implements PropertyChangeListener { //panel for creating wavespawns
	
	private static final int MIN = 0;
	
	JTextField wsNameField = new JTextField(20);
	JTextField wsDeadField = new JTextField(20);
	JTextField wsSpawnField = new JTextField(20);
	JTextField templateField = new JTextField(20);
	
	DefaultComboBoxModel<String> whereModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> firstModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> lastModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	
	JComboBox<String> wsWhereBox = new JComboBox<String>(whereModel);
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
		
		secondaryWindow.addPropertyChangeListener(MainWindow.WAVESPAWN, this);
		
		JLabel label = new JLabel("WaveSpawn editor");
		
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
		JLabel templateLabel = new JLabel("Template: ");
		
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
		
		wsNameField.setMinimumSize(wsNameField.getPreferredSize());
		wsWhereBox.setMinimumSize(wsWhereBox.getPreferredSize());
		wsDeadField.setMinimumSize(wsDeadField.getPreferredSize());
		wsSpawnField.setMinimumSize(wsSpawnField.getPreferredSize());
		
		addGB(label, 0, 0);
		
		addGB(wsName, 0, 1);
		addGB(wsWhere, 2, 1);
		addGB(wsNameField, 1, 1);
		addGB(wsWhereBox, 3, 1);	
		
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
		wsNameField.setText((String) wsn.getValueSingular(WaveSpawnNode.NAME));
		wsWhereBox.setSelectedItem(wsn.getValueSingular(WaveSpawnNode.WHERE));
		wsTotalSpin.setValue(wsn.getValueSingular(WaveSpawnNode.TOTALCOUNT));
		wsMaxSpin.setValue(wsn.getValueSingular(WaveSpawnNode.MAXACTIVE));
		wsSpawnSpin.setValue(wsn.getValueSingular(WaveSpawnNode.SPAWNCOUNT));
		wsStartSpin.setValue(wsn.getValueSingular(WaveSpawnNode.WAITBEFORESTARTING));
		wsBetweenSpin.setValue(wsn.getValueSingular(WaveSpawnNode.WAITBETWEENSPAWNS));
		wsDeaths.setSelected(wsn.getBetweenDeaths());
		updateBetweenSpawns();
		wsCurrSpin.setValue(wsn.getValueSingular(WaveSpawnNode.TOTALCURRENCY));
		wsDeadField.setText((String) wsn.getValueSingular(WaveSpawnNode.WAITFORALLDEAD));
		wsSpawnField.setText((String) wsn.getValueSingular(WaveSpawnNode.WAITFORALLSPAWNED));
		isSupport.setSelected((Boolean) wsn.getValueSingular(WaveSpawnNode.SUPPORT));
		isLimited.setSelected(wsn.getSupportLimited());
		
		//relays aren't mandatory, so only show them if they exist
		if(wsn.getValueSingular(WaveSpawnNode.STARTWAVEOUTPUT) != null) {
			doStart.setSelected(true);
			startRelay.setSelectedItem(((RelayNode) wsn.getValueSingular(WaveSpawnNode.STARTWAVEOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
		else {
			doStart.setSelected(false);
		}
		if(wsn.getValueSingular(WaveSpawnNode.FIRSTSPAWNOUTPUT) != null) { //first
			doFirst.setSelected(true);
			firstRelay.setSelectedItem(((RelayNode) wsn.getValueSingular(WaveSpawnNode.FIRSTSPAWNOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
		else {
			doFirst.setSelected(false);
		}
		if(wsn.getValueSingular(WaveSpawnNode.LASTSPAWNOUTPUT) != null) { //last
			doLast.setSelected(true);
			lastRelay.setSelectedItem(((RelayNode) wsn.getValueSingular(WaveSpawnNode.LASTSPAWNOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
		else {
			doLast.setSelected(false);
		}
		if(wsn.getValueSingular(WaveSpawnNode.DONEOUTPUT) != null) { //done
			doDone.setSelected(true);
			doneRelay.setSelectedItem(((RelayNode) wsn.getValueSingular(WaveSpawnNode.DONEOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
		else {
			doDone.setSelected(false);
		}
	}
	
	public void updateNode(WaveSpawnNode wsn) { //update node to reflect panel
		wsn.putKey(WaveSpawnNode.NAME, wsNameField.getText());
		wsn.putKey(WaveSpawnNode.WHERE, wsWhereBox.getSelectedItem());
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
			wsn.putKey(WaveSpawnNode.SUPPORT, "Limited");
		}
		else {
			wsn.putKey(WaveSpawnNode.SUPPORT, true);
		}
		wsn.setSupportLimited(isLimited.isSelected());
		
		
		if(doStart.isSelected()) {
			if(wsn.getValueSingular(WaveSpawnNode.STARTWAVEOUTPUT) == null) { //make relays if data is entered and no relay exists
				wsn.putKey(WaveSpawnNode.STARTWAVEOUTPUT, new RelayNode()); 
			}
			((RelayNode) wsn.getValueSingular(WaveSpawnNode.STARTWAVEOUTPUT)).putKey(RelayNode.TARGET, (String) startRelay.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			//this does mean the node itself is now thrown out, so may consider removing checking if the
			//node is null above
			wsn.removeKey(WaveSpawnNode.STARTWAVEOUTPUT);
		}
		if(doFirst.isSelected()) { //first
			if(wsn.getValueSingular(WaveSpawnNode.FIRSTSPAWNOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.FIRSTSPAWNOUTPUT, new RelayNode()); 
			}
			((RelayNode) wsn.getValueSingular(WaveSpawnNode.FIRSTSPAWNOUTPUT)).putKey(RelayNode.TARGET, (String) firstRelay.getSelectedItem());
		}
		else { 
			wsn.removeKey(WaveSpawnNode.FIRSTSPAWNOUTPUT);
		}
		if(doLast.isSelected()) { //last
			if(wsn.getValueSingular(WaveSpawnNode.LASTSPAWNOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.LASTSPAWNOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValueSingular(WaveSpawnNode.LASTSPAWNOUTPUT)).putKey(RelayNode.TARGET, (String) lastRelay.getSelectedItem());
		}
		else {
			wsn.removeKey(WaveSpawnNode.LASTSPAWNOUTPUT);
		}
		if(doDone.isSelected()) { //done
			if(wsn.getValueSingular(WaveSpawnNode.DONEOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.DONEOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValueSingular(WaveSpawnNode.DONEOUTPUT)).putKey(RelayNode.TARGET, (String) doneRelay.getSelectedItem());
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
