package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import engipop.Node.RelayNode;
import engipop.Node.TankNode;
import engipop.Node.WaveNode;

//panel for tank
@SuppressWarnings("serial")
public class TankPanel extends EngiPanel implements PropertyChangeListener {
	
	JSpinner healthSpinner; 
	JSpinner speed;
	JTextField name;
	JCheckBox finalTankCheck = new JCheckBox("Final tank?");
	JCheckBox onKilledCheck = new JCheckBox("OnKilledOutput?");
	JCheckBox onBombCheck = new JCheckBox("OnBombDroppedOutput?");
	
	DefaultComboBoxModel<String> pathTrackModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> onKilledModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> onBombModel = new DefaultComboBoxModel<String>();
	JComboBox<String> startingNodeCombo = new JComboBox<String>(pathTrackModel);
	JComboBox<String> killedTargetBox = new JComboBox<String>(onKilledModel);
	JComboBox<String> bombTargetBox = new JComboBox<String>(onBombModel);
	JTextField killedActionField = new JTextField(13);
	JTextField bombActionField = new JTextField(13);
	
	JLabel killedTargetLabel = new JLabel("Target:");
	JLabel bombTargetLabel = new JLabel("Target:");
	JLabel killedActionLabel = new JLabel("Action:");
	JLabel bombActionLabel = new JLabel("Action:");
	
	TankNode tankNode;
	RelayNode killedNode;
	RelayNode bombNode;
	
	private boolean isNodeResetting = false;
	private boolean isRelayResetting = false;
	private boolean isPathResetting = false;
	
	public TankPanel(PopulationPanel SecondaryWindow) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 5);
		
		int tankInit = Engipop.TANKDEFAULTHEALTH, min = 0, tankMax = 300000, tankIncr = 1000;
		//double speedInit = Values.tankDefaultSpeed, dMin = 0.0, speedMax = 300.0, speedIncr = 1.0;
		
		SpinnerNumberModel healthModel = new SpinnerNumberModel(tankInit, min, tankMax, tankIncr);
		//SpinnerNumberModel speedModel = new SpinnerNumberModel(speedInit, dMin, speedMax, speedIncr);
		
		healthSpinner = new JSpinner(healthModel);
		//speed = new JSpinner(speedModel);
		//name = new JTextField(Values.tankDefaultName, 20);
		
		startingNodeCombo.setEditable(true);
		killedTargetBox.setEditable(true);
		bombTargetBox.setEditable(true);
		
		JLabel healthLabel = new JLabel("Health: ");
		JLabel startLabel = new JLabel("StartingPathTrackNode: ");
		
		setPairVisible(killedTargetLabel, killedTargetBox, false);
		setPairVisible(killedActionLabel, killedActionField, false);
		setPairVisible(bombTargetLabel, bombTargetBox, false);
		setPairVisible(bombActionLabel, bombActionField, false);
		
		initListeners();
		
		addGB(healthLabel, 0, 0);
		addGB(healthSpinner, 1, 0);
		addGB(finalTankCheck, 2, 0);
		
		addGB(startLabel, 0, 1);
		addGB(startingNodeCombo, 1, 1);
		
		addGB(onKilledCheck, 0, 2);
		addGB(killedTargetLabel, 1, 2);
		addGB(killedTargetBox, 2, 2);
		addGB(killedActionLabel, 3, 2);
		addGB(killedActionField, 4, 2);
		
		addGB(onBombCheck, 0, 3);
		addGB(bombTargetLabel, 1, 3);
		addGB(bombTargetBox, 2, 3);
		addGB(bombActionLabel, 3, 3);
		addGB(bombActionField, 4, 3);
	}
	
	//currently just the relay checkboxes
	private void initListeners() {
		onKilledCheck.addItemListener(event -> {
			setPairVisible(killedTargetLabel, killedTargetBox, onKilledCheck.isSelected());
			setPairVisible(killedActionLabel, killedActionField, onKilledCheck.isSelected());
			
			if(isNodeResetting) {
				return;
			}
			
			if(!onKilledCheck.isSelected()) {
				killedNode = new RelayNode();
				tankNode.removeKey(TankNode.ONKILLEDOUTPUT);
			}
		});
		onBombCheck.addItemListener(event -> {
			setPairVisible(bombTargetLabel, bombTargetBox, onBombCheck.isSelected());
			setPairVisible(bombActionLabel, bombActionField, onBombCheck.isSelected());
			
			if(isNodeResetting) {
				return;
			}
			
			if(!onBombCheck.isSelected()) {
				bombNode = new RelayNode();
				tankNode.removeKey(TankNode.ONBOMBDROPPEDOUTPUT);
			}
		});
		
		healthSpinner.addChangeListener(event -> {
			if(isNodeResetting) {
				return;
			}
			tankNode.putKey(TankNode.HEALTH, healthSpinner.getValue());
		});
		
		finalTankCheck.addItemListener(event -> {
			if(isNodeResetting) {
				return;
			}
			tankNode.putKey(TankNode.SKIN, finalTankCheck.isSelected());
		});
		
		startingNodeCombo.addActionListener(event -> {
			if(isNodeResetting || isPathResetting) {
				return;
			}
			tankNode.putKey(TankNode.STARTINGPATHTRACKNODE, startingNodeCombo.getSelectedItem());
		});
		
		killedTargetBox.addActionListener(event -> {
			if(isNodeResetting || isRelayResetting) {
				return;
			}
			String text = (String) killedTargetBox.getSelectedItem();
			updateRelayKey(TankNode.ONKILLEDOUTPUT, text, RelayNode.TARGET, tankNode, killedNode);
		});
		
		bombTargetBox.addActionListener(event -> {
			if(isNodeResetting || isRelayResetting) {
				return;
			}
			String text = (String) bombTargetBox.getSelectedItem();
			updateRelayKey(TankNode.ONBOMBDROPPEDOUTPUT, text, RelayNode.TARGET, tankNode, bombNode);
		});
		
		killedActionField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				if(isNodeResetting) {
					return;
				}
				String text = killedActionField.getText();
				updateRelayKey(TankNode.ONKILLEDOUTPUT, text, RelayNode.ACTION, tankNode, killedNode);
			}
		});
		
		bombActionField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				if(isNodeResetting) {
					return;
				}
				String text = killedActionField.getText();
				updateRelayKey(TankNode.ONBOMBDROPPEDOUTPUT, text, RelayNode.ACTION, tankNode, bombNode);
			}
		});
	}
	
	//don't really think combobox needs parameter
	private void setPairVisible(JLabel label, JComponent box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
	
	public void updatePanel(TankNode node) {
		tankNode = node;
		isNodeResetting = true;
		
		healthSpinner.setValue(node.getValue(TankNode.HEALTH));
		finalTankCheck.setSelected((boolean) node.getValue(TankNode.SKIN));
		startingNodeCombo.setSelectedItem(node.getValue(TankNode.STARTINGPATHTRACKNODE));
		if(node.containsKey(TankNode.ONKILLEDOUTPUT)) {
			killedNode = (RelayNode) node.getValue(TankNode.ONKILLEDOUTPUT);
			
			onKilledCheck.setSelected(true);
			killedTargetBox.setSelectedItem(killedNode.getValue(RelayNode.TARGET));
			killedActionField.setText((String) killedNode.getValue(RelayNode.ACTION));
		}
		else {
			onKilledCheck.setSelected(false);
			killedNode = new RelayNode();
		}
		if(node.containsKey(TankNode.ONBOMBDROPPEDOUTPUT)) {
			bombNode = (RelayNode) node.getValue(TankNode.ONBOMBDROPPEDOUTPUT);
			
			onBombCheck.setSelected(true);
			bombTargetBox.setSelectedItem(bombNode.getValue(RelayNode.TARGET));
			bombActionField.setText((String) bombNode.getValue(RelayNode.ACTION));
		}
		else {
			onBombCheck.setSelected(false);
			bombNode = new RelayNode();
		}
		isNodeResetting = false;
	}
	
	public void setPathModel(List<String> spawns){
		isPathResetting = true;
		pathTrackModel.removeAllElements();
		
		for(String s : spawns) {
			pathTrackModel.addElement(s);
		}
		isPathResetting = false;
	}
	
	public void setRelays(List<String> relays) {
		isRelayResetting = true;
		onKilledModel.removeAllElements();
		onBombModel.removeAllElements();
		
		for(String s : relays) {
			onKilledModel.addElement(s);
			onBombModel.addElement(s);
		}
		isRelayResetting = false;
	}

	//get tankspawn and tankrelays from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(PopulationPanel.TANKSPAWNS)) {
			setPathModel((List<String>) evt.getNewValue());
		}
		else if(evt.getPropertyName().equals(PopulationPanel.TANKRELAY)) {
			setRelays((List<String>) evt.getNewValue());
		}
	}
}
