package engipop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.*;

import engipop.Tree.RelayNode;
import engipop.Tree.TankNode;

//panel for tank
@SuppressWarnings("serial")
public class TankPanel extends EngiPanel {
	
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
	
	public TankPanel() {
		setLayout(new GridBagLayout());
		gb.anchor = GridBagConstraints.WEST;
		gb.insets = new Insets(0, 0, 0, 5);
		
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
		health.setValue(node.getHealth());
		finalTank.setSelected(node.getSkin());
		startingNode.setSelectedItem(node.getStartingPath());
		if(node.getOnKilled() != null) {
			onKilledBox.setSelectedItem(node.getOnKilled().getTarget());
		}
		if(node.getOnBomb() != null) {
			onBombBox.setSelectedItem(node.getOnBomb().getTarget());
		}
	}
	
	public void updateNode(TankNode node) {
		node.setHealth((int) health.getValue());
		node.setSkin(finalTank.isSelected());
		node.setStartingPath((String) startingNode.getSelectedItem());
		if(onKilledCheck.isSelected()) {
			if(node.getOnKilled() == null) { //make relay if data is entered and no relay exists
				node.setOnKilled(new RelayNode());
			}
			node.getOnKilled().setTarget((String) onKilledBox.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			node.setOnKilled(null); 
		}
		if(onBombCheck.isSelected()) {
			if(node.getOnBomb() == null) { //make relay if data is entered and no relay exists
				node.setOnBomb(new RelayNode());
			}
			node.getOnBomb().setTarget((String) onBombBox.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			node.setOnBomb(null); 
		}
	}
	
	public void setMapInfo(List<String> spawns, List<String> relays){ 
		pathTrackModel.removeAllElements();
		onKilledModel.removeAllElements();
		onBombModel.removeAllElements();
		
		for(String s : spawns) {
			pathTrackModel.addElement(s);
		}
		for(String s : relays) {
			onKilledModel.addElement(s);
			onBombModel.addElement(s);
		}
	}
}
