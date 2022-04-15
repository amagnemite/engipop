package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.Document;

import engipop.Tree.WaveNode;
import engipop.Tree.WaveSpawnNode;

public class WaveSpawnPanel extends JPanel { //panel for creating wavespawns
	
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gb = new GridBagConstraints();
	
	JTextField wsNameField = new JTextField(20);
	JTextField wsDeadField = new JTextField(20);
	JTextField wsSpawnField = new JTextField(20);
	JComboBox<String> wsWhereField = new JComboBox<String>(); //add spawn names
	
	JSpinner wsTotalSpin = new JSpinner();
	JSpinner wsMaxSpin = new JSpinner();
	JSpinner wsSpawnSpin = new JSpinner();
	JSpinner wsStartSpin = new JSpinner();
	JSpinner wsBetweenSpin = new JSpinner();
	JSpinner wsCurrSpin = new JSpinner();
	
	JCheckBox wsDeaths = new JCheckBox("AfterDeath?");
	JLabel wsBetwSpawns = new JLabel("WaitBetweenSpawns: ");
	
	private int nodeCount = 0;
	
	//wavespawn models/docs
	/*
	SpinnerNumberModel totalModel = new SpinnerNumberModel(); //totalcount bots
	SpinnerNumberModel spawnModel = new SpinnerNumberModel(); //spawncount
	SpinnerNumberModel maxModel = new SpinnerNumberModel(); //maxactive
	SpinnerNumberModel currModel = new SpinnerNumberModel(); //money
	SpinnerNumberModel deadModel = new SpinnerNumberModel(); //foralldead
	SpinnerNumberModel allSpawnedModel = new SpinnerNumberModel(); //forallspawned
	ToggleButtonModel deathsToggleModel = new ToggleButtonModel(); 
	*/
	
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
		
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		
		JLabel wsName = new JLabel("Name: ");
		JLabel wsWhere = new JLabel("Where: ");
		JLabel wsTotalCount = new JLabel("TotalCount: ");
		JLabel wsMaxActive = new JLabel("MaxActive: ");
		JLabel wsSpawnCount = new JLabel("SpawnCount: ");
		JLabel wsBeforeStart = new JLabel("WaitBeforeStarting: ");
		JLabel wsCurrency = new JLabel("TotalCurrency: ");
		JLabel wsAllDead = new JLabel("WaitForAllDead: ");
		JLabel wsAllSpawned = new JLabel("WaitForAllSpawned: ");
		
		wsDeaths.addItemListener(new ItemListener() { //update betweenspawns as appropriate
			public void itemStateChanged(ItemEvent e) {
				updateBetweenSpawns();
			}
		});
		
		wsWhereField.setEditable(true);
		
		addGB(label, 0, 0);
		
		addGB(wsName, 0, 1);
		
		addGB(wsWhere, 3, 1);
		
		addGB(wsTotalCount, 0, 3);
		addGB(wsTotalSpin, 1, 3);
		addGB(wsMaxActive, 2, 3);
		addGB(wsMaxSpin, 3, 3);		
		addGB(wsSpawnCount, 4, 3);
		addGB(wsSpawnSpin, 5, 3);	
		
		addGB(wsBeforeStart, 0, 6);
		addGB(wsStartSpin, 1, 6);			
		addGB(wsBetwSpawns, 0, 7);
		addGB(wsBetweenSpin, 1, 7);		
		addGB(wsDeaths, 2, 7);
		addGB(wsCurrency, 0, 8);
		addGB(wsCurrSpin, 1, 8);	
		addGB(wsAllDead, 0, 9);

		addGB(wsAllSpawned, 0, 10);
			
		addGB(wsNameField, 1, 1);
		addGB(wsWhereField, 4, 1);
		addGB(wsDeadField, 1, 9);	
		addGB(wsSpawnField, 1, 10);
	}
	
	private void addGB(Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		add(comp, gb);
	}	
	
	public void updatePanel(WaveSpawnNode wsn) { //sets panel components to reflect the node
		wsNameField.setText(wsn.getName());
		wsSpawnField.setText(wsn.getWhere());
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
	}
	
	public void clearPanel() { //clears out the panel
		//prob shouldn't be hard code
		wsNameField.setText("");
		wsSpawnField.setText("");
		wsTotalSpin.setValue(1);
		wsMaxSpin.setValue(1);
		wsSpawnSpin.setValue(1);
		wsStartSpin.setValue(0.0);
		wsBetweenSpin.setValue(0.0);
		wsDeaths.setSelected(false);
		updateBetweenSpawns();
		wsCurrSpin.setValue(0);
		wsDeadField.setText("");
		wsSpawnField.setText("");
	}
	
	public void updateNode(WaveSpawnNode wsn) { //update node to reflect panel
		if(wsNameField.getText().equals("")) { //prevent funny unclickable list
			wsn.setName(Integer.toString(nodeCount));
			nodeCount++;
		}
		else {
			wsn.setName(wsNameField.getText());
		}	
		wsn.setWhere((String) wsWhereField.getSelectedItem());
		wsn.setTotalCount((int) wsTotalSpin.getValue());
		wsn.setMaxActive((int) wsMaxSpin.getValue());
		wsn.setSpawnCount((int) wsSpawnSpin.getValue());
		wsn.setBeforeStarting((double) wsStartSpin.getValue());
		wsn.setBetweenSpawns((double) wsBetweenSpin.getValue());
		wsn.setBetweenDeaths(wsDeaths.isSelected()); //this needs sanity checking
		wsn.setCurrency((int) wsCurrSpin.getValue());
		wsn.setWaitDead(wsDeadField.getText());
		wsn.setWaitSpawned(wsSpawnField.getText());
	}
	
	private void updateBetweenSpawns() {
		if(wsDeaths.isSelected()) {
			wsBetwSpawns.setText("WaitBetweenSpawnsAfterDeath: ");
			//currentWSNode.setBetweenDeaths(true);
		}
		else {
			wsBetwSpawns.setText("WaitBetweenSpawns: ");
			//currentWSNode.setBetweenDeaths(false);
		}
	}
	
	void makeWSPanel() { //panel for ws creation
		
		
		
		
		/*nameDoc.addDocumentListener(new DocumentListener() { //update the name based on user input/removal
			public void insertUpdate(DocumentEvent d) {
				currentWSNode.setName(wsNameField.getText());
				getWaveSpawnList();
			}

			public void removeUpdate(DocumentEvent e) {
				currentWSNode.setName(wsNameField.getText());
				getWaveSpawnList();
			}

			public void changedUpdate(DocumentEvent e) {
				//irrelevant here 
			}
		});*/
		

		
		/*wsWhereField.addActionListener(new ActionListener() { //update where based on selection
			public void actionPerformed(ActionEvent a) {
				currentWSNode.setWhere((String) wsWhereField.getSelectedItem());
			}
		});*/
		
		
		
		/*
		 * wsTotalSpin.addChangeListener(new ChangeListener() { //update totalcount
		 * public void stateChanged(ChangeEvent c) { currentWSNode.setTotalCount((int)
		 * wsTotalSpin.getValue()); } });
		 */
		
		
		
		/*
		 * wsMaxSpin.addChangeListener(new ChangeListener() { //update maxactive public
		 * void stateChanged(ChangeEvent c) { currentWSNode.setMaxActive((int)
		 * wsMaxSpin.getValue()); } });
		 */	
		
		
		
		/*
		 * wsCountSpin.addChangeListener(new ChangeListener() { //update spawncount
		 * public void stateChanged(ChangeEvent c) { currentWSNode.setSpawnCount((int)
		 * wsCountSpin.getValue()); } });
		 */
		
		
		/*
		 * wsStartSpin.addChangeListener(new ChangeListener() { //update
		 * waitbeforestarting public void stateChanged(ChangeEvent c) {
		 * currentWSNode.setBeforeStarting((int) wsStartSpin.getValue()); } });
		 */	
		
		
		
		/*
		 * wsBetweenSpin.addChangeListener(new ChangeListener() { //update
		 * waitbetweenspawns public void stateChanged(ChangeEvent c) {
		 * currentWSNode.setBetweenSpawns((int) wsBetweenSpin.getValue()); } });
		 */	
		
		
		/*
		 * wsCurrSpin.addChangeListener(new ChangeListener() { //update totalcurrency
		 * public void stateChanged(ChangeEvent c) { currentWSNode.setCurrency((int)
		 * wsCurrSpin.getValue()); } });
		 */

		
		/*
		 * deadDoc.addDocumentListener(new DocumentListener() { //update the
		 * waitforalldead based on user input/removal public void
		 * insertUpdate(DocumentEvent d) {
		 * currentWSNode.setWaitDead(wsDeadField.getText()); }
		 * 
		 * public void removeUpdate(DocumentEvent e) {
		 * currentWSNode.setWaitDead(wsDeadField.getText()); }
		 * 
		 * public void changedUpdate(DocumentEvent e) { //irrelevant here } });
		 */
		
		
		
		/*
		 * spawnDoc.addDocumentListener(new DocumentListener() { //update the
		 * waitforalldead based on user input/removal public void
		 * insertUpdate(DocumentEvent d) {
		 * currentWSNode.setWaitSpawned(wsSpawnField.getText()); }
		 * 
		 * public void removeUpdate(DocumentEvent e) {
		 * currentWSNode.setWaitSpawned(wsSpawnField.getText()); }
		 * 
		 * public void changedUpdate(DocumentEvent e) { //irrelevant here } });
		 */
	}
}