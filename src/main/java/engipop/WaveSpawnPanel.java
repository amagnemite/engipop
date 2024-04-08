package engipop;

import java.awt.Color;
import java.awt.Dimension;
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
	
	DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> firstModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> lastModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	WherePanel wherePanel = new WherePanel();
	
	JComboBox<String> startTarget = new JComboBox<String>(startModel);
	JComboBox<String> firstTarget = new JComboBox<String>(firstModel);
	JComboBox<String> lastTarget = new JComboBox<String>(lastModel);
	JComboBox<String> doneTarget = new JComboBox<String>(doneModel);
	
	JTextField startAction = new JTextField(13);
	JTextField firstAction = new JTextField(13);
	JTextField lastAction = new JTextField(13);
	JTextField doneAction = new JTextField(13);
	
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
	
	public WaveSpawnPanel(PopulationPanel secondaryWindow) {
		int initial = 1, totalMax = 999, activeMax = 22, incr = 1, currMax = 30000, currIncr = 50;
		double initWait = 0.0, minWait = 0.0, maxWait = 1000.0, incrWait = 1.0;
		setBackground(new Color(96, 139, 165));
		
		secondaryWindow.addPropertyChangeListener(this);
		
		JLabel label = new JLabel("WaveSpawn editor");
		JLabel wsName = new JLabel("Name: ");
		JLabel whereLabel = new JLabel("Where: ");
		JLabel wsTotalCount = new JLabel("TotalCount: ");
		JLabel wsMaxActive = new JLabel("MaxActive: ");
		JLabel wsSpawnCount = new JLabel("SpawnCount: ");
		JLabel wsBeforeStart = new JLabel("WaitBeforeStarting: ");
		JLabel wsCurrency = new JLabel("TotalCurrency: ");
		JLabel wsAllDead = new JLabel("WaitForAllDead: ");
		JLabel wsAllSpawned = new JLabel("WaitForAllSpawned: ");
		JLabel templateLabel = new JLabel("Template: ");
		
		JLabel startWaveLabel = new JLabel("StartWaveOutput");
		JLabel firstLabel = new JLabel("FirstSpawnOutput");
		JLabel lastLabel = new JLabel("LastSpawnOutput");
		JLabel doneLabel = new JLabel("DoneOutput");
		
		JLabel startTargetLabel = new JLabel("Target:");
		JLabel firstTargetLabel = new JLabel("Target:");
		JLabel lastTargetLabel = new JLabel("Target:");
		JLabel doneTargetLabel = new JLabel("Target:");
		
		JLabel startActionLabel = new JLabel("Action:");
		JLabel firstActionLabel = new JLabel("Action:");
		JLabel lastActionLabel = new JLabel("Action:");
		JLabel doneActionLabel = new JLabel("Action:");
		
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
		
		startTarget.setEditable(true);
		firstTarget.setEditable(true);
		lastTarget.setEditable(true);
		doneTarget.setEditable(true);
		
		wsNameField.setMinimumSize(wsNameField.getPreferredSize());
		templateField.setMinimumSize(templateField.getPreferredSize());
		
		wsDeadField.setMinimumSize(wsDeadField.getPreferredSize());
		wsSpawnField.setMinimumSize(wsSpawnField.getPreferredSize());
		
		wsDeaths.addItemListener(event -> { //update betweenspawns as appropriate
			updateBetweenSpawns();
		});
		
		//all the relays are optional so set invis by default
		startWaveLabel.setVisible(false);
		setComponentAndLabelVisible(startTargetLabel, startTarget, false);
		setComponentAndLabelVisible(startActionLabel, startAction, false);
		doStart.addItemListener(event -> {
			startWaveLabel.setVisible(doStart.isSelected());
			setComponentAndLabelVisible(startTargetLabel, startTarget, doStart.isSelected());
			setComponentAndLabelVisible(startActionLabel, startAction, doStart.isSelected());
		});
		
		firstLabel.setVisible(false);
		setComponentAndLabelVisible(firstTargetLabel, firstTarget, false);
		setComponentAndLabelVisible(firstActionLabel, firstAction, false);
		doFirst.addItemListener(event -> {
			firstLabel.setVisible(doFirst.isSelected());
			setComponentAndLabelVisible(firstTargetLabel, firstTarget, doFirst.isSelected());
			setComponentAndLabelVisible(firstActionLabel, firstAction, doFirst.isSelected());
		});
		
		lastLabel.setVisible(false);
		setComponentAndLabelVisible(lastTargetLabel, lastTarget, false);
		setComponentAndLabelVisible(lastActionLabel, lastAction, false);
		doLast.addItemListener(event -> {
			lastLabel.setVisible(doLast.isSelected());
			setComponentAndLabelVisible(lastTargetLabel, lastTarget, doLast.isSelected());
			setComponentAndLabelVisible(lastActionLabel, lastAction, doLast.isSelected());
		});
		
		doneLabel.setVisible(doDone.isSelected());
		setComponentAndLabelVisible(doneTargetLabel, doneTarget, doDone.isSelected());
		setComponentAndLabelVisible(doneActionLabel, doneAction, doDone.isSelected());
		doDone.addItemListener(event -> {
			doneLabel.setVisible(doDone.isSelected());
			setComponentAndLabelVisible(doneTargetLabel, doneTarget, doDone.isSelected());
			setComponentAndLabelVisible(doneActionLabel, doneAction, doDone.isSelected());
		});
		
		isLimited.setVisible(false);
		isSupport.addItemListener(event -> { //don't need to show limited unless support
			isLimited.setVisible(isSupport.isSelected());
		});
		
		addGB(label, 0, 0);
		
		addGB(wsName, 0, 1);
		addGB(wsNameField, 1, 1);
		addGB(whereLabel, 2, 1);
		
		addGB(templateLabel, 0, 2);
		addGB(templateField, 1, 2);
		
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
		
		addGB(doStart, 0, 11);
		addGB(doFirst, 1, 11);
		addGB(doLast, 2, 11);
		addGB(doDone, 3, 11);
		
		addGB(startWaveLabel, 0, 12);
		addGB(startTargetLabel, 0, 13);
		addGB(startTarget, 1, 13);
		addGB(startActionLabel, 0, 14);
		addGB(startAction, 1, 14);
		
		addGB(firstLabel, 2, 12);
		addGB(firstTargetLabel, 2, 13);
		addGB(firstTarget, 3, 13);
		addGB(firstActionLabel, 2, 14);
		addGB(firstAction, 3, 14);
		
		addGB(lastLabel, 0, 15);
		addGB(lastTargetLabel, 0, 16);
		addGB(lastTarget, 1, 16);
		addGB(lastActionLabel, 0, 17);
		addGB(lastAction, 1, 17);
		
		addGB(doneLabel, 2, 15);
		addGB(doneTargetLabel, 2, 16);
		addGB(doneTarget, 3, 16);
		addGB(doneActionLabel, 2, 17);
		addGB(doneAction, 3, 17);
		
		gbConstraints.gridwidth = 3;
		gbConstraints.gridheight = 2;
		addGB(wherePanel, 3, 1);
	}
	
	public void updatePanel(WaveSpawnNode wsn) { //sets panel components to reflect the node
		//wsn.printKeyVals();
		wsNameField.setText((String) wsn.getValue(WaveSpawnNode.NAME));
		
		if(wsn.containsKey(WaveSpawnNode.WHERE)) {
			wherePanel.updateWhere(wsn.getListValue(WaveSpawnNode.WHERE));
		}
		else {
			wherePanel.clearSelection();
		}
	
		templateField.setText((String) wsn.getValue(TFBotNode.TEMPLATE));
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
		if(wsn.containsKey(WaveSpawnNode.STARTWAVEOUTPUT)) {
			doStart.setSelected(true);
			
			RelayNode relay = (RelayNode) wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT);
			startTarget.setSelectedItem(relay.getValue(RelayNode.TARGET));
			startAction.setText((String) relay.getValue(RelayNode.ACTION));
		}
		else {
			doStart.setSelected(false);
		}
		
		if(wsn.containsKey(WaveSpawnNode.FIRSTSPAWNOUTPUT)) { //first
			doFirst.setSelected(true);

			RelayNode relay = (RelayNode) wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT);
			firstTarget.setSelectedItem(relay.getValue(RelayNode.TARGET));
			firstAction.setText((String) relay.getValue(RelayNode.ACTION));
		}
		else {
			doFirst.setSelected(false);
		}
		
		if(wsn.containsKey(WaveSpawnNode.LASTSPAWNOUTPUT)) { //last
			doLast.setSelected(true);
			
			RelayNode relay = (RelayNode) wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT);
			lastTarget.setSelectedItem(relay.getValue(RelayNode.TARGET));
			lastAction.setText((String) relay.getValue(RelayNode.ACTION));
		}
		else {
			doLast.setSelected(false);
		}
		
		if(wsn.containsKey(WaveSpawnNode.DONEOUTPUT)) { //done
			doDone.setSelected(true);
			
			RelayNode relay = (RelayNode) wsn.getValue(WaveSpawnNode.DONEOUTPUT);
			doneTarget.setSelectedItem(relay.getValue(RelayNode.TARGET));
			doneAction.setText((String) relay.getValue(RelayNode.ACTION));
		}
		else {
			doDone.setSelected(false);
		}
	}
	
	public void updateNode(WaveSpawnNode wsn) { //update node to reflect panel
		wsn.putKey(WaveSpawnNode.NAME, wsNameField.getText());
		wsn.putKey(TFBotNode.TEMPLATE, templateField.getText());
		
		wsn.putKey(WaveSpawnNode.WHERE, wherePanel.updateNode());
		
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
		
		if(isSupport.isSelected()) {
			wsn.putKey(WaveSpawnNode.SUPPORT, true);
			wsn.setSupportLimited(isLimited.isSelected());
		}
		else {
			wsn.putKey(WaveSpawnNode.SUPPORT, false);
		}
		
		if(doStart.isSelected()) {
			if(wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT) == null) { //make relays if data is entered and no relay exists
				wsn.putKey(WaveSpawnNode.STARTWAVEOUTPUT, new RelayNode()); 
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.STARTWAVEOUTPUT)).putKey((String) startTarget.getSelectedItem(), startAction.getText());
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
			((RelayNode) wsn.getValue(WaveSpawnNode.FIRSTSPAWNOUTPUT)).putKey((String) firstTarget.getSelectedItem(), firstAction.getText());
		}
		else { 
			wsn.removeKey(WaveSpawnNode.FIRSTSPAWNOUTPUT);
		}
		if(doLast.isSelected()) { //last
			if(wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.LASTSPAWNOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.LASTSPAWNOUTPUT)).putKey((String) lastTarget.getSelectedItem(), lastAction.getText());
		}
		else {
			wsn.removeKey(WaveSpawnNode.LASTSPAWNOUTPUT);
		}
		if(doDone.isSelected()) { //done
			if(wsn.getValue(WaveSpawnNode.DONEOUTPUT) == null) {
				wsn.putKey(WaveSpawnNode.DONEOUTPUT, new RelayNode());
			}
			((RelayNode) wsn.getValue(WaveSpawnNode.DONEOUTPUT)).putKey((String) doneTarget.getSelectedItem(), doneAction.getText());
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
		if(evt.getPropertyName().equals(PopulationPanel.WAVESPAWNRELAY)) {
			setRelay((List<String>) evt.getNewValue());
		}
		else if(evt.getPropertyName().equals(PopulationPanel.BOTSPAWNS)) {
			wherePanel.updateModel((List<String>) evt.getNewValue());
		}
	}
}
