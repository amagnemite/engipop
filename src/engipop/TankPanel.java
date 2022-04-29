package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.*;

import engipop.Tree.TankNode;

public class TankPanel extends EngiPanel {

	GridBagLayout gbLayout = new GridBagLayout();
	
	JSpinner health; 
	JSpinner speed;
	JTextField name;
	JCheckBox finalTank;
	JComboBox<String> startingNode;
	DefaultComboBoxModel<String> pathTrackModel = new DefaultComboBoxModel<String>();
	
	public TankPanel() {
		gb = new GridBagConstraints();
		setLayout(new GridBagLayout());
		gb.anchor = GridBagConstraints.WEST;
		
		int tankInit = EngiPanel.tankDefaultHealth, min = 0, tankMax = 300000, tankIncr = 1000;
		//double speedInit = Values.tankDefaultSpeed, dMin = 0.0, speedMax = 300.0, speedIncr = 1.0;
		
		SpinnerNumberModel healthModel = new SpinnerNumberModel(tankInit, min, tankMax, tankIncr);
		//SpinnerNumberModel speedModel = new SpinnerNumberModel(speedInit, dMin, speedMax, speedIncr);
		
		health = new JSpinner(healthModel);
		//speed = new JSpinner(speedModel);
		//name = new JTextField(Values.tankDefaultName, 20);
		finalTank = new JCheckBox("Final tank?");
		startingNode = new JComboBox<String>(pathTrackModel);
		startingNode.setEditable(true);
		
		JLabel healthLabel = new JLabel("Health: ");
		JLabel startLabel = new JLabel("StartingPathTrackNode: ");
		
		addGB(healthLabel, 0, 0);
		addGB(health, 1, 0);
		addGB(finalTank, 2, 0);
		
		addGB(startLabel, 0, 1);
		addGB(startingNode, 1, 1);
	}
	
	public void updatePanel(TankNode node) {
		health.setValue(node.getHealth());
		finalTank.setSelected(node.getSkin());
		startingNode.setSelectedItem(node.getStartingPath());
	}
	
	public void updateNode(TankNode node) {
		node.setHealth((int) health.getValue());
		node.setSkin(finalTank.isSelected());
		node.setStartingPath((String) startingNode.getSelectedItem());
	}
	
	public void setSpawnModel(List<String> list) { //update relay list and attach to all the boxes
		pathTrackModel.removeAllElements();
		
		for(String s : list) {
			pathTrackModel.addElement(s);
		}
	}
}
