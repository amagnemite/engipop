package engipop;

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

import engipop.Tree.RelayNode;
import engipop.Tree.WaveNode;

@SuppressWarnings("serial")
//panel for wave
public class WavePanel extends EngiPanel implements PropertyChangeListener { //is basically only relays
	private DefaultComboBoxModel<String> startModel = new DefaultComboBoxModel<String>();
	private DefaultComboBoxModel<String> doneModel = new DefaultComboBoxModel<String>();
	private DefaultComboBoxModel<String> initModel = new DefaultComboBoxModel<String>();
	
	private JComboBox<String> startRelay = new JComboBox<String>(startModel);
	private JComboBox<String> doneRelay = new JComboBox<String>(doneModel);
	private JComboBox<String> initRelay = new JComboBox<String>(initModel);
	
	private JCheckBox doInit = new JCheckBox("InitWaveOutput?");
	
	//String sound; 
	
	public WavePanel(SecondaryWindow secondaryWindow) {
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		gb.insets = new Insets(0, 0, 0, 5);
		
		secondaryWindow.addPropertyChangeListener(SecondaryWindow.WAVERELAY, this);
		
		JLabel waveLabel = new JLabel("Wave editor");
		JLabel startLabel = new JLabel("StartWaveOutput: ");
		JLabel doneLabel = new JLabel("DoneOutput: ");
		JLabel initLabel = new JLabel("InitWaveOutput: ");
		
		startRelay.setEditable(true);
		doneRelay.setEditable(true);
		initRelay.setEditable(true);
		
		initLabel.setVisible(false); //init is optional so invis by default
		initRelay.setVisible(false);
		doInit.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {
				if(doInit.isSelected()) {
					initLabel.setVisible(true);
					initRelay.setVisible(true);
				}
				else {
					initLabel.setVisible(false);
					initRelay.setVisible(false);
				}
			}
		});
		
		addGB(waveLabel, 0, 0);
		
		addGB(startLabel, 0, 1);
		addGB(startRelay, 1, 1);
		addGB(doneLabel, 2, 1);
		addGB(doneRelay, 3, 1);
		
		addGB(doInit, 4, 1);
		addGB(initLabel, 5, 1);
		addGB(initRelay, 6, 1);
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
		startRelay.setSelectedItem(wave.getStart().getTarget());
		doneRelay.setSelectedItem(wave.getDone().getTarget()); //possibly make this not mandatory
		
		if(wave.getInit() != null) { //not mandatory, so may not exist
			doInit.setSelected(true);
			initRelay.setSelectedItem(wave.getInit().getTarget());
		}
		else {
			doInit.setSelected(false);
		}
	}
	
	public void updateNode(WaveNode wave) {
		wave.getStart().setTarget((String) startRelay.getSelectedItem());
		wave.getDone().setTarget((String) doneRelay.getSelectedItem());
		
		if(doInit.isSelected()) {
			if(wave.getInit() == null) { //make relay if data is entered and no relay exists
				wave.setInit(new RelayNode()); 
			}
			wave.getInit().setTarget((String) initRelay.getSelectedItem());
		}
		else { //if it isn't selected, throw out old data
			wave.setInit(null); 
		}
	}
	
	//get relay list from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		setRelay((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
		this.invalidate();
	}
}
