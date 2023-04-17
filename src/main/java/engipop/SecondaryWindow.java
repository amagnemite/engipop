package engipop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.*;

import engipop.Node.PopNode;

@SuppressWarnings("serial")
public class SecondaryWindow extends EngiWindow { //window for less important/one off deals
	
	public static final String WAVERELAY = "waverelay";
	public static final String WAVESPAWNRELAY = "wavespawnrelay";
	public static final String BOTSPAWNS = "botspawn";
	public static final String TAGS = "tags";
	public static final String TANKSPAWNS = "tankspawn";
	public static final String TANKRELAY = "tankrelay";
	
	EngiPanel popPanel = new EngiPanel();
	DefaultComboBoxModel<String> mapsModel = new DefaultComboBoxModel<String>();
	JComboBox<String> maps = new JComboBox<String>();
	
	JSpinner currSpinner = new JSpinner();
	JSpinner respawnWaveSpinner = new JSpinner();
	JCheckBox eventBox;
	JCheckBox waveTimeBox;
	JSpinner busterDmgSpinner = new JSpinner();
	JSpinner busterKillSpinner = new JSpinner();
	JCheckBox atkSpawnBox;
	JCheckBox advancedBox;
	
	JButton updatePop;
	PopNode popNode;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	public SecondaryWindow(PopNode popNode, MainWindow w) {
		super("Population settings");
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		setSize(800, 200);
		
		this.popNode = popNode;
		makePopPanel(w);
		
		addGB(popPanel, 0, 0);
		
		setVisible(true);
		//requestFocus();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
	
    //take map info index, get map's info and tell the relevant listeners
    private void loadMapInfo(int index) {
    	MapInfo info = new MapInfo();
    	info.getMapData(index);
    	
    	support.firePropertyChange(WAVERELAY, null, info.getWaveRelay());
    	support.firePropertyChange(WAVESPAWNRELAY, null, info.getWSRelay());
    	support.firePropertyChange(BOTSPAWNS, null, info.getBotSpawns());
    	support.firePropertyChange(TAGS, null, info.getTags());
    	support.firePropertyChange(TANKSPAWNS, null, info.getTankSpawns());
    	support.firePropertyChange(TANKRELAY, null, info.getTankRelays());
    }
    
	private void makePopPanel(MainWindow w) { //makes population panel
		popPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		
		MapInfo mapinfo = new MapInfo();
		
		int min = 0, currMax = 99999, currIncr = 50, currInit = 400,
				respawnInit = 6, respawnIncr = 1, respawnMax = 100,
				busterDmgInit = 3000, busterDmgIncr = 100, busterDmgMax = 100000,
				busterKillInit = 15, busterKillIncr = 1, busterKillMax = 100;
		//arbitary numbers
		
		SpinnerNumberModel currModel = new SpinnerNumberModel(currInit, min, currMax, currIncr);
		SpinnerNumberModel respawnWaveModel = new SpinnerNumberModel(respawnInit, min, respawnMax, respawnIncr);
		SpinnerNumberModel dmgModel = new SpinnerNumberModel(busterDmgInit, min, busterDmgMax, busterDmgIncr);
		SpinnerNumberModel killModel = new SpinnerNumberModel(busterKillInit, min, busterKillMax, busterKillIncr);
		
		currSpinner.setModel(currModel);
		respawnWaveSpinner.setModel(respawnWaveModel);
		busterDmgSpinner.setModel(dmgModel);
		busterKillSpinner.setModel(killModel);
		
		maps.setEditable(true);
		maps.setPrototypeDisplayValue("mvm_waterlogged_rc4g");
		
		feedback = new JLabel(" ");
		
		eventBox = new JCheckBox("Halloween?");
		waveTimeBox = new JCheckBox("Fixed respawn wave times?");
		atkSpawnBox = new JCheckBox("Can bots attack in spawn?");
		advancedBox = new JCheckBox("Advanced?");
		
		updatePop = new JButton("Update population settings");
		
		updatePop.addActionListener(event -> {
			updateNode();
			feedback.setText("Population settings updated");
			if(popNode.getMapIndex() > -1) { //if user entered a map
				loadMapInfo(popNode.getMapIndex());
			}
		});
		
		for(String s : mapinfo.getMapNames()) {
			mapsModel.addElement(s);
		}
		maps.setModel(mapsModel);
		
		popPanel.addMouseListener(new MouseListener() { //this is wonky, fix
			public void mousePressed(MouseEvent e) {
				clearFeedback();
			}
			public void mouseEntered(MouseEvent e) {
				//clearFeedback();
			}
			public void mouseClicked(MouseEvent e) {
				clearFeedback();
			}
			public void mouseReleased(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
				clearFeedback();
			}
		});
		
		JLabel currLabel = new JLabel("StartingCurrency: ");
		JLabel respawnWaveLabel = new JLabel("RespawnWaveTime: ");
		JLabel busterDmgLabel = new JLabel("AddSentryBusterWhenDamageDealtExceeds: ");
		JLabel busterKillLabel = new JLabel("AddSentryBusterWhenKillCountExceeds: ");
		JLabel mapLabel = new JLabel("Map: ");
		
		popPanel.addGB(feedback, 0, 0);
		
		popPanel.addGB(mapLabel, 0, 1);
		popPanel.addGB(maps, 1, 1);
		
		popPanel.addGB(currLabel, 0, 2);
		popPanel.addGB(currSpinner, 1, 2);
		popPanel.addGB(respawnWaveLabel, 2, 2);
		popPanel.addGB(respawnWaveSpinner, 3, 2);
		popPanel.addGB(waveTimeBox, 4, 2);
		
		popPanel.addGB(busterDmgLabel, 0, 3);
		popPanel.addGB(busterDmgSpinner, 1, 3);
		popPanel.addGB(busterKillLabel, 2, 3);
		popPanel.addGB(busterKillSpinner, 3, 3);
		
		popPanel.addGB(eventBox, 0, 4);
		popPanel.addGB(atkSpawnBox, 1, 4);
		popPanel.addGB(advancedBox, 2, 4);
		popPanel.addGB(updatePop, 2, 5);
	}
	
	private void clearFeedback() { //clears feedback so things don't get stuck on it
		if(!feedback.getText().equals(" ")) {
			feedback.setText(" ");
		}
	}
	
	private void updateNode() {
		popNode.setMapIndex(maps.getSelectedIndex());
		popNode.putKey(PopNode.STARTINGCURRENCY, currSpinner.getValue());
		popNode.putKey(PopNode.RESPAWNWAVETIME, respawnWaveSpinner.getValue());
		popNode.putKey(PopNode.FIXEDRESPAWNWAVETIME, waveTimeBox.isSelected());
		popNode.putKey(PopNode.EVENTPOPFILE, eventBox.isSelected());
		popNode.putKey(PopNode.BUSTERDAMAGE, busterDmgSpinner.getValue());
		popNode.putKey(PopNode.BUSTERKILLS, busterKillSpinner.getValue());
		popNode.putKey(PopNode.BOTSATKINSPAWN, atkSpawnBox.isSelected());
		popNode.putKey(PopNode.ADVANCED, advancedBox.isSelected());
	}
	
	public void updatePopPanel() {
		currSpinner.setValue(popNode.getValueSingular(PopNode.STARTINGCURRENCY));
		respawnWaveSpinner.setValue(popNode.getValueSingular(PopNode.RESPAWNWAVETIME));
		waveTimeBox.setSelected((boolean) popNode.getValueSingular(PopNode.FIXEDRESPAWNWAVETIME));
		eventBox.setSelected((boolean) popNode.getValueSingular(PopNode.EVENTPOPFILE));
		busterDmgSpinner.setValue(popNode.getValueSingular(PopNode.BUSTERDAMAGE));
		busterKillSpinner.setValue(popNode.getValueSingular(PopNode.BUSTERKILLS));
		atkSpawnBox.setSelected((boolean) popNode.getValueSingular(PopNode.BOTSATKINSPAWN));
		advancedBox.setSelected((boolean) popNode.getValueSingular(PopNode.ADVANCED));
	}
}
