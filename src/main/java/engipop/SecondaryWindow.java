package engipop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String, String> botTemplateStringMap = new HashMap<String, String>();
	private Map<String, String> wsTemplateStringMap = new HashMap<String, String>();
	//key: bot/ws name + template name, value: file name
	
	EngiPanel popPanel = new EngiPanel();
	DefaultComboBoxModel<String> mapsModel = new DefaultComboBoxModel<String>();
	JComboBox<String> maps = new JComboBox<String>();
	
	JSpinner currSpinner = new JSpinner();
	JSpinner respawnWaveSpinner = new JSpinner();
	JCheckBox eventBox = new JCheckBox("Halloween?");
	JCheckBox waveTimeBox = new JCheckBox("Fixed respawn wave times?");
	JSpinner busterDmgSpinner = new JSpinner();
	JSpinner busterKillSpinner = new JSpinner();
	JCheckBox atkSpawnBox = new JCheckBox("Can bots attack in spawn?");
	//JCheckBox advancedBox = new JCheckBox("Advanced?");
	
	JButton loadPop = new JButton("Load a population file");
	JButton loadTemplate = new JButton("Load a template file");
	JButton updatePop = new JButton("Update population settings");
	
	SettingsWindow setWin;
	PopNode popNode;
	
	private PopulationParser popParser;
	private PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
	
	public SecondaryWindow(SettingsWindow setWin, PopNode popNode) {
		super("Population settings");
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		setSize(800, 200);
		
		this.popNode = popNode;
		this.setWin = setWin;
		popParser = new PopulationParser(this, setWin);
		
		makePopPanel();
		
		addGB(popPanel, 0, 0);
		
		setVisible(true);
		//requestFocus();
	}
	
	//listen to all changes
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
	
	//listen to a specific change
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
	
    //take map info index, get map's info and tell the relevant listeners
    private void loadMapInfo(int index) {
    	MapInfo info = new MapInfo();
    	info.getMapData(index);
    	
    	propertySupport.firePropertyChange(WAVERELAY, null, info.getWaveRelay());
    	propertySupport.firePropertyChange(WAVESPAWNRELAY, null, info.getWSRelay());
    	propertySupport.firePropertyChange(BOTSPAWNS, null, info.getBotSpawns());
    	propertySupport.firePropertyChange(TAGS, null, info.getTags());
    	propertySupport.firePropertyChange(TANKSPAWNS, null, info.getTankSpawns());
    	propertySupport.firePropertyChange(TANKRELAY, null, info.getTankRelays());
    }
    
	private void makePopPanel() { //makes population panel
		popPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		
		MapInfo mapinfo = new MapInfo();
		
		int min = 0, currMax = 99999, currIncr = 50, currInit = 400,
				respawnInit = 6, respawnIncr = 1, respawnMax = 100,
				busterDmgInit = 3000, busterDmgIncr = 100, busterDmgMax = 100000,
				busterKillInit = 15, busterKillIncr = 1, busterKillMax = 100;
		//arbitary numbers
		
		JLabel currLabel = new JLabel("StartingCurrency: ");
		JLabel respawnWaveLabel = new JLabel("RespawnWaveTime: ");
		JLabel busterDmgLabel = new JLabel("AddSentryBusterWhenDamageDealtExceeds: ");
		JLabel busterKillLabel = new JLabel("AddSentryBusterWhenKillCountExceeds: ");
		JLabel mapLabel = new JLabel("Map: ");
		
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
		
		loadPop.addActionListener(event -> {
			JFileChooser fileChooser;
			if(setWin.getScriptPathString() != null) {
				fileChooser = new JFileChooser(setWin.getScriptPathString());
			}
			else {
				fileChooser = new JFileChooser();
			}			
			File file = null;
			fileChooser.setFileFilter(new PopFileFilter());
			
			fileChooser.showOpenDialog(this);
			file = fileChooser.getSelectedFile();
			if(file != null) {
				popNode = popParser.parsePopulation(file, botTemplateStringMap, wsTemplateStringMap);
				propertySupport.firePropertyChange("POPNODE", null, popNode);
				propertySupport.firePropertyChange(MainWindow.BOTTEMPLATEMAP, null, botTemplateStringMap);
			}
		});
		popPanel.addGB(maps, 1, 1);
		
		popPanel.addGB(loadPop, 2, 1);
		popPanel.addGB(loadTemplate, 3, 1);
		
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
		//popPanel.addGB(advancedBox, 2, 4);
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
		//popNode.putKey(PopNode.ADVANCED, advancedBox.isSelected());
	}
	
	public void updatePopPanel() {
		currSpinner.setValue(popNode.getValueSingular(PopNode.STARTINGCURRENCY));
		respawnWaveSpinner.setValue(popNode.getValueSingular(PopNode.RESPAWNWAVETIME));
		waveTimeBox.setSelected((boolean) popNode.getValueSingular(PopNode.FIXEDRESPAWNWAVETIME));
		eventBox.setSelected((boolean) popNode.getValueSingular(PopNode.EVENTPOPFILE));
		busterDmgSpinner.setValue(popNode.getValueSingular(PopNode.BUSTERDAMAGE));
		busterKillSpinner.setValue(popNode.getValueSingular(PopNode.BUSTERKILLS));
		atkSpawnBox.setSelected((boolean) popNode.getValueSingular(PopNode.BOTSATKINSPAWN));
		//advancedBox.setSelected((boolean) popNode.getValueSingular(PopNode.ADVANCED));
	}
	
	//wrapper
	public void fireTemplateChange(String type, String oldName, String newName) {
		propertySupport.firePropertyChange(type, oldName, newName);
	}
}
