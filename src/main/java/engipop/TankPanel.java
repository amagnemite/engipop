package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

import engipop.Node.RelayNode;
import engipop.Node.TankNode;

//panel for tank
@SuppressWarnings("serial")
public class TankPanel extends EngiPanel implements PropertyChangeListener {
	
	JSpinner health; 
	JSpinner speed;
	JTextField name;
	JCheckBox finalTank = new JCheckBox("Final tank?");
	JCheckBox onKilledCheck = new JCheckBox("OnKilledOutput?");
	JCheckBox onBombCheck = new JCheckBox("OnBombDroppedOutput?");
	
	DefaultComboBoxModel<String> pathTrackModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> onKilledModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> onBombModel = new DefaultComboBoxModel<String>();
	JComboBox<String> startingNode = new JComboBox<String>(pathTrackModel);
	JComboBox<String> killedTargetBox = new JComboBox<String>(onKilledModel);
	JComboBox<String> bombTargetBox = new JComboBox<String>(onBombModel);
	JTextField killedActionField = new JTextField(13);
	JTextField bombActionField = new JTextField(13);
	
	JLabel killedTargetLabel = new JLabel("Target:");
	JLabel bombTargetLabel = new JLabel("Target:");
	JLabel killedActionLabel = new JLabel("Action:");
	JLabel bombActionLabel = new JLabel("Action:");
	
	public TankPanel(PopulationPanel SecondaryWindow) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 5);
		
		int tankInit = Engipop.TANKDEFAULTHEALTH, min = 0, tankMax = 300000, tankIncr = 1000;
		//double speedInit = Values.tankDefaultSpeed, dMin = 0.0, speedMax = 300.0, speedIncr = 1.0;
		
		SpinnerNumberModel healthModel = new SpinnerNumberModel(tankInit, min, tankMax, tankIncr);
		//SpinnerNumberModel speedModel = new SpinnerNumberModel(speedInit, dMin, speedMax, speedIncr);
		
		health = new JSpinner(healthModel);
		//speed = new JSpinner(speedModel);
		//name = new JTextField(Values.tankDefaultName, 20);
		
		startingNode.setEditable(true);
		killedTargetBox.setEditable(true);
		bombTargetBox.setEditable(true);
		
		JLabel healthLabel = new JLabel("Health: ");
		JLabel startLabel = new JLabel("StartingPathTrackNode: ");
		
		addListeners();
		setPairVisible(killedTargetLabel, killedTargetBox, false);
		setPairVisible(killedActionLabel, killedActionField, false);
		setPairVisible(bombTargetLabel, bombTargetBox, false);
		setPairVisible(bombActionLabel, bombActionField, false);
		
		addGB(healthLabel, 0, 0);
		addGB(health, 1, 0);
		addGB(finalTank, 2, 0);
		
		addGB(startLabel, 0, 1);
		addGB(startingNode, 1, 1);
		
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
	private void addListeners() {
		onKilledCheck.addItemListener(event -> {
			setPairVisible(killedTargetLabel, killedTargetBox, onKilledCheck.isSelected());
			setPairVisible(killedActionLabel, killedActionField, onKilledCheck.isSelected());
		});
		onBombCheck.addItemListener(event -> {
			setPairVisible(bombTargetLabel, bombTargetBox, onBombCheck.isSelected());
			setPairVisible(bombActionLabel, bombActionField, onBombCheck.isSelected());
		});
	}
	
	//don't really think combobox needs parameter
	private void setPairVisible(JLabel label, JComponent box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
	
	public void updatePanel(TankNode node) {
		health.setValue(node.getValue(TankNode.HEALTH));
		finalTank.setSelected((boolean) node.getValue(TankNode.SKIN));
		startingNode.setSelectedItem(node.getValue(TankNode.STARTINGPATHTRACKNODE));
		if(node.getValue(TankNode.ONKILLEDOUTPUT) != null) {
			RelayNode relay = (RelayNode) node.getValue(TankNode.ONKILLEDOUTPUT);
			
			killedTargetBox.setSelectedItem(relay.getValue(RelayNode.TARGET));
			killedActionField.setText((String) relay.getValue(RelayNode.ACTION));
		}
		if(node.getValue(TankNode.ONBOMBDROPPEDOUTPUT) != null) {
			RelayNode relay = (RelayNode) node.getValue(TankNode.ONBOMBDROPPEDOUTPUT);
			
			bombTargetBox.setSelectedItem(relay.getValue(RelayNode.TARGET));
			bombActionField.setText((String) relay.getValue(RelayNode.ACTION));
		}
	}
	
	public void updateNode(TankNode node) {
		node.putKey(TankNode.HEALTH, health.getValue());
		node.putKey(TankNode.SKIN, finalTank.isSelected());
		node.putKey(TankNode.STARTINGPATHTRACKNODE, startingNode.getSelectedItem());
		if(onKilledCheck.isSelected()) {
			if(node.getValue(TankNode.ONKILLEDOUTPUT) == null) { //make relay if data is entered and no relay exists
				node.putKey(TankNode.ONKILLEDOUTPUT, new RelayNode());
			}
			RelayNode relay = (RelayNode) node.getValue(TankNode.ONKILLEDOUTPUT);
			
			relay.putKey(RelayNode.TARGET, killedTargetBox.getSelectedItem());
			relay.putKey(RelayNode.ACTION, killedActionField.getText());
		}
		else { //if it isn't selected, throw out old data
			node.removeKey(TankNode.ONKILLEDOUTPUT);
		}
		if(onBombCheck.isSelected()) {
			if(node.getValue(TankNode.ONBOMBDROPPEDOUTPUT) == null) { //make relay if data is entered and no relay exists
				node.putKey(TankNode.ONBOMBDROPPEDOUTPUT, new RelayNode());
			}
			RelayNode relay = (RelayNode) node.getValue(TankNode.ONBOMBDROPPEDOUTPUT);
			
			relay.putKey(RelayNode.TARGET, bombTargetBox.getSelectedItem());
			relay.putKey(RelayNode.ACTION, bombActionField.getText());
		}
		else { //if it isn't selected, throw out old data
			node.removeKey(TankNode.ONBOMBDROPPEDOUTPUT);
		}
	}
	
	public void setPathModel(List<String> spawns){ 
		pathTrackModel.removeAllElements();
		
		for(String s : spawns) {
			pathTrackModel.addElement(s);
		}
	}
	
	public void setRelays(List<String> relays) {
		onKilledModel.removeAllElements();
		onBombModel.removeAllElements();
		
		for(String s : relays) {
			onKilledModel.addElement(s);
			onBombModel.addElement(s);
		}
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
