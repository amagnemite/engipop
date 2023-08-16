package engipop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import engipop.Node.RelayNode;
import engipop.Node.WaveNode;

@SuppressWarnings("serial")
//panel for wave
public class WavePanel extends EngiPanel implements PropertyChangeListener { //is basically only relays
	private DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	private DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	private DefaultComboBoxModel<String> initModel = new DefaultComboBoxModel<String>();
	
	private JComboBox<String> startNameBox = new JComboBox<String>(startModel);
	private JComboBox<String> doneNameBox = new JComboBox<String>(doneModel);
	private JComboBox<String> initNameBox = new JComboBox<String>(initModel);
	
	private JTextField startAction = new JTextField(13);
	private JTextField doneAction = new JTextField(13);
	private JTextField initAction = new JTextField(13);
	
	private JCheckBox doInit = new JCheckBox("InitWaveOutput?");
	
	//String sound; 
	
	public WavePanel(SecondaryWindow secondaryWindow) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 5);
		this.setBackground(new Color(208, 169, 107));
		
		secondaryWindow.addPropertyChangeListener(SecondaryWindow.WAVERELAY, this);
		
		JLabel waveLabel = new JLabel("Wave editor");
		JLabel startLabel = new JLabel("StartWaveOutput");
		JLabel doneLabel = new JLabel("DoneOutput");
		JLabel initLabel = new JLabel("InitWaveOutput");
		JLabel initTargetLabel = new JLabel("Target: ");
		JLabel initActionLabel = new JLabel("Action: ");
		
		startNameBox.setEditable(true);
		doneNameBox.setEditable(true);
		initNameBox.setEditable(true);
		
		startAction.setMinimumSize(startAction.getPreferredSize());
		doneAction.setMinimumSize(doneAction.getPreferredSize());
		initAction.setMinimumSize(initAction.getPreferredSize());
		
		initLabel.setVisible(false); //init is optional so invis by default
		initTargetLabel.setVisible(false);
		initNameBox.setVisible(false);
		initActionLabel.setVisible(false);
		initAction.setVisible(false);
		
		initNameBox.setVisible(false);
		doInit.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doInit.isSelected()) {
					initLabel.setVisible(true);
					initTargetLabel.setVisible(true);
					initNameBox.setVisible(true);
					initActionLabel.setVisible(true);
					initAction.setVisible(true);
				}
				else {
					initLabel.setVisible(false);
					initTargetLabel.setVisible(false);
					initNameBox.setVisible(false);
					initActionLabel.setVisible(false);
					initAction.setVisible(false);
				}
			}
		});
		
		addGB(waveLabel, 0, 0);
		
		addGB(startLabel, 0, 1);
		addGB(new JLabel("Target: "), 0, 2);
		addGB(startNameBox, 1, 2);
		addGB(new JLabel("Action: "), 0, 3);
		addGB(startAction, 1, 3);
		
		addGB(doneLabel, 2, 1);
		addGB(new JLabel("Target: "), 2, 2);
		addGB(doneNameBox, 3, 2);
		addGB(new JLabel("Action: "), 2, 3);
		addGB(doneAction, 3, 3);

		addGB(doInit, 5, 1);
		
		addGB(initLabel, 4, 1);
		addGB(initTargetLabel, 4, 2);
		addGB(initNameBox, 5, 2);
		addGB(initActionLabel, 4, 3);
		addGB(initAction, 5, 3);
	}
	
	public void setRelay(List<String> list) { //update relay list and attach to all the boxes
		startModel.removeAllElements();
		doneModel.removeAllElements();
		initModel.removeAllElements();
		
		for(String s : list) {
			startModel.addElement(s);
			doneModel.addElement(s);
			initModel.addElement(s);
		}
	}
	
	public void updatePanel(WaveNode wave) { 
		if(wave.containsKey(WaveNode.STARTWAVEOUTPUT)) {
			startNameBox.setSelectedItem(((RelayNode) wave.getValue(WaveNode.STARTWAVEOUTPUT)).getValue(RelayNode.TARGET));
		}
		
		if(wave.containsKey(WaveNode.DONEOUTPUT)) {
			doneNameBox.setSelectedItem(((RelayNode) wave.getValue(WaveNode.DONEOUTPUT)).getValue(RelayNode.TARGET)); //possibly make this not mandatory
		}
		
		if(wave.containsKey(WaveNode.INITWAVEOUTPUT)) { //not mandatory, so may not exist
			doInit.setSelected(true);
			initNameBox.setSelectedItem(((RelayNode) wave.getValue(WaveNode.INITWAVEOUTPUT)).getValue(RelayNode.TARGET));
		}
		else {
			doInit.setSelected(false);
		}
	}
	
	public void updateNode(WaveNode wave) {
		((RelayNode) wave.getValue(WaveNode.STARTWAVEOUTPUT)).updateNode((String) startNameBox.getSelectedItem(), startAction.getText());
		((RelayNode) wave.getValue(WaveNode.DONEOUTPUT)).putKey(RelayNode.TARGET, (String) doneNameBox.getSelectedItem());
		
		if(doInit.isSelected()) {
			if(wave.getValue(WaveNode.INITWAVEOUTPUT) == null) { //make relay if data is entered and no relay exists
				wave.putKey(WaveNode.INITWAVEOUTPUT, new RelayNode());
			}
			((RelayNode) wave.getValue(WaveNode.INITWAVEOUTPUT)).putKey(RelayNode.TARGET, (String) initNameBox.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			wave.removeKey(WaveNode.INITWAVEOUTPUT);
		}
	}
	
	//get relay list from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		setRelay((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
		this.invalidate();
	}
}
