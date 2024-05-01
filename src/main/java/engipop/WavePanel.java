package engipop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	
	JLabel initLabel = new JLabel("InitWaveOutput");
	JLabel initTargetLabel = new JLabel("Target: ");
	JLabel initActionLabel = new JLabel("Action: ");
	
	private WaveNode waveNode;
	private RelayNode startNode;
	private RelayNode doneNode;
	private RelayNode initNode;
	
	//String sound;
	private boolean isNodeResetting = false;
	private boolean isRelayResetting = false;
	
	public WavePanel(PopulationPanel popPanel) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.WEST;
		gbConstraints.insets = new Insets(0, 0, 0, 5);
		setBackground(new Color(208, 169, 107));
		
		popPanel.addPropertyChangeListener(PopulationPanel.WAVERELAY, this);
		
		JLabel waveLabel = new JLabel("Wave editor");
		JLabel startLabel = new JLabel("StartWaveOutput");
		JLabel doneLabel = new JLabel("DoneOutput");
		
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
		
		initListeners();
		
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
	
	private void initListeners() {
		doInit.addItemListener(event -> { 
			initLabel.setVisible(doInit.isSelected());
			initTargetLabel.setVisible(doInit.isSelected());
			initNameBox.setVisible(doInit.isSelected());
			initActionLabel.setVisible(doInit.isSelected());
			initAction.setVisible(doInit.isSelected());
			
			if(isNodeResetting) {
				return;
			}
			
			if(!doInit.isSelected()) {
				initNode = new RelayNode();
				waveNode.removeKey(WaveNode.INITWAVEOUTPUT);
			}
		});
		
		startNameBox.addActionListener(event -> {
			if(isNodeResetting || isRelayResetting) {
				return;
			}
			
			String text = (String) startNameBox.getSelectedItem();
			updateRelayKey(WaveNode.STARTWAVEOUTPUT, text, RelayNode.TARGET, waveNode, startNode);
		});
		
		doneNameBox.addActionListener(event -> {
			if(isNodeResetting || isRelayResetting) {
				return;
			}
			
			String text = (String) doneNameBox.getSelectedItem();
			updateRelayKey(WaveNode.DONEOUTPUT, text, RelayNode.TARGET, waveNode, doneNode);
		});
		
		initNameBox.addActionListener(event -> {
			if(isNodeResetting || isRelayResetting) {
				return;
			}
			
			String text = (String) initNameBox.getSelectedItem();
			updateRelayKey(WaveNode.INITWAVEOUTPUT, text, RelayNode.TARGET, waveNode, initNode);
		});
		
		startAction.getDocument().addDocumentListener(new DocumentListener() {
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
				
				String text = startAction.getText();
				updateRelayKey(WaveNode.STARTWAVEOUTPUT, text, RelayNode.ACTION, waveNode, startNode);
			}
		});
		
		doneAction.getDocument().addDocumentListener(new DocumentListener() {
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
				
				String text = doneAction.getText();
				updateRelayKey(WaveNode.DONEOUTPUT, text, RelayNode.ACTION, waveNode, doneNode);
			}
		});
		
		initAction.getDocument().addDocumentListener(new DocumentListener() {
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
				
				String text = initAction.getText();
				updateRelayKey(WaveNode.INITWAVEOUTPUT, text, RelayNode.ACTION, waveNode, initNode);
			}
		});
	}
	
	public void setRelay(List<String> list) { //update relay list and attach to all the boxes
		isRelayResetting = true;
		startModel.removeAllElements();
		doneModel.removeAllElements();
		initModel.removeAllElements();
		
		for(String s : list) {
			startModel.addElement(s);
			doneModel.addElement(s);
			initModel.addElement(s);
		}
		isRelayResetting = false;
	}
	
	public void updatePanel(WaveNode wave) {
		waveNode = wave;
		isNodeResetting = true;
		
		if(waveNode.containsKey(WaveNode.STARTWAVEOUTPUT)) {
			startNode = (RelayNode) waveNode.getValue(WaveNode.STARTWAVEOUTPUT);
			
			startNameBox.setSelectedItem(startNode.getValue(RelayNode.TARGET));
			startAction.setText((String) startNode.getValue(RelayNode.ACTION));
		}
		else {
			startNode = new RelayNode();
		}
		
		if(waveNode.containsKey(WaveNode.DONEOUTPUT)) {
			doneNode = (RelayNode) waveNode.getValue(WaveNode.DONEOUTPUT);
			
			doneNameBox.setSelectedItem(doneNode.getValue(RelayNode.TARGET));
			doneAction.setText((String) doneNode.getValue(RelayNode.ACTION));
		}
		else {
			doneNode = new RelayNode();
		}
		
		if(waveNode.containsKey(WaveNode.INITWAVEOUTPUT)) {
			doInit.setSelected(true);
			initNode = (RelayNode) waveNode.getValue(WaveNode.INITWAVEOUTPUT);
			
			initNameBox.setSelectedItem(initNode.getValue(RelayNode.TARGET));
			initAction.setText((String) initNode.getValue(RelayNode.ACTION));
		}
		else {
			doInit.setSelected(false);
			initNode = new RelayNode();
		}
		isNodeResetting = false;
	}
	
	//get relay list from secondarywindow
	public void propertyChange(PropertyChangeEvent evt) {
		setRelay((List<String>) evt.getNewValue()); //this should always be a list<string>, may want to sanity check though
		invalidate();
	}
}
