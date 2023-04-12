package engipop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

import engipop.Tree.RelayNode;
import engipop.Tree.TankNode;

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
	JComboBox<String> onKilledBox = new JComboBox<String>(onKilledModel);
	JComboBox<String> onBombBox = new JComboBox<String>(onBombModel);
	
	JLabel killedLabel = new JLabel("OnKilledOutput");
	JLabel bombLabel = new JLabel("OnBombDroppedOutput");
	
	public TankPanel(SecondaryWindow SecondaryWindow) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 5);
		
		int tankInit = EngiPanel.tankDefaultHealth, min = 0, tankMax = 300000, tankIncr = 1000;
		//double speedInit = Values.tankDefaultSpeed, dMin = 0.0, speedMax = 300.0, speedIncr = 1.0;
		
		SpinnerNumberModel healthModel = new SpinnerNumberModel(tankInit, min, tankMax, tankIncr);
		//SpinnerNumberModel speedModel = new SpinnerNumberModel(speedInit, dMin, speedMax, speedIncr);
		
		health = new JSpinner(healthModel);
		//speed = new JSpinner(speedModel);
		//name = new JTextField(Values.tankDefaultName, 20);
		
		startingNode.setEditable(true);
		onKilledBox.setEditable(true);
		onBombBox.setEditable(true);
		
		JLabel healthLabel = new JLabel("Health: ");
		JLabel startLabel = new JLabel("StartingPathTrackNode: ");
		
		addListeners();
		setBoxVisible(killedLabel, onKilledBox, false);
		setBoxVisible(bombLabel, onBombBox, false);
		
		addGB(healthLabel, 0, 0);
		addGB(health, 1, 0);
		addGB(finalTank, 2, 0);
		
		addGB(startLabel, 0, 1);
		addGB(startingNode, 1, 1);
		
		addGB(onKilledCheck, 0, 2);
		addGB(killedLabel, 1, 2);
		addGB(onKilledBox, 2, 2);
		
		addGB(onBombCheck, 0, 3);
		addGB(bombLabel, 1, 3);
		addGB(onBombBox, 2, 3);
	}
	
	//currently just the relay checkboxes
	private void addListeners() {
		onKilledCheck.addItemListener(event -> {
			if(onKilledCheck.isSelected()) {
				setBoxVisible(killedLabel, onKilledBox, true);
			}
			else {
				setBoxVisible(killedLabel, onKilledBox, false);
			}
		});
		onBombCheck.addItemListener(event -> {
			if(onBombCheck.isSelected()) {
				setBoxVisible(bombLabel, onBombBox, true);
			}
			else {
				setBoxVisible(bombLabel, onBombBox, false);
			}
		});
	}
	
	//don't really think combobox needs parameter
	private void setBoxVisible(JLabel label, JComboBox<String> box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
	
	public void updatePanel(TankNode node) {
		health.setValue(node.getValueSingular(TankNode.HEALTH));
		finalTank.setSelected((boolean) node.getValueSingular(TankNode.SKIN));
		startingNode.setSelectedItem(node.getValueSingular(TankNode.STARTINGPATHTRACKNODE));
		if(node.getValueSingular(TankNode.ONKILLEDOUTPUT) != null) {
			onKilledBox.setSelectedItem(((RelayNode) node.getValueSingular(TankNode.ONKILLEDOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
		if(node.getValueSingular(TankNode.ONBOMBDROPPEDOUTPUT) != null) {
			onBombBox.setSelectedItem(((RelayNode) node.getValueSingular(TankNode.ONBOMBDROPPEDOUTPUT)).getValueSingular(RelayNode.TARGET));
		}
	}
	
	public void updateNode(TankNode node) {
		node.putKey(TankNode.HEALTH, health.getValue());
		node.putKey(TankNode.SKIN, finalTank.isSelected());
		node.putKey(TankNode.STARTINGPATHTRACKNODE, startingNode.getSelectedItem());
		if(onKilledCheck.isSelected()) {
			if(node.getValueSingular(TankNode.ONKILLEDOUTPUT) == null) { //make relay if data is entered and no relay exists
				node.putKey(TankNode.ONKILLEDOUTPUT, new RelayNode());
			}
			((RelayNode) node.getValueSingular(TankNode.ONKILLEDOUTPUT)).putKey(RelayNode.TARGET, onBombBox.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			node.putKey(TankNode.ONKILLEDOUTPUT, null);
		}
		if(onBombCheck.isSelected()) {
			if(node.getValueSingular(TankNode.ONBOMBDROPPEDOUTPUT) == null) { //make relay if data is entered and no relay exists
				node.putKey(TankNode.ONBOMBDROPPEDOUTPUT, new RelayNode());
			}
			((RelayNode) node.getValueSingular(TankNode.ONBOMBDROPPEDOUTPUT)).putKey(RelayNode.TARGET, onKilledBox.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			node.putKey(TankNode.ONBOMBDROPPEDOUTPUT, null);
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
		if(evt.getPropertyName().equals(SecondaryWindow.TANKSPAWNS)) {
			setPathModel((List<String>) evt.getNewValue());
		}
		else if(evt.getPropertyName().equals(SecondaryWindow.TANKRELAY)) {
			setRelays((List<String>) evt.getNewValue());
		}
	}
}
