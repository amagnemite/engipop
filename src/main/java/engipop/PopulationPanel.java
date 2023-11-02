package engipop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;

import engipop.Node.PopNode;
import engipop.PopulationParser.TemplateData;
import engipop.EngiWindow;

@SuppressWarnings("serial")
public class PopulationPanel extends EngiPanel { //window for less important/one off deals
	
	public static final String WAVERELAY = "waverelay";
	public static final String WAVESPAWNRELAY = "wavespawnrelay";
	public static final String BOTSPAWNS = "botspawn";
	public static final String TAGS = "tags";
	public static final String TANKSPAWNS = "tankspawn";
	public static final String TANKRELAY = "tankrelay";
	
	public static final String INCLUDED = "included";
	public static final String IMPORTED = "imported";
	public static final String INTERNAL = "internal";
	
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
	
	public PopulationPanel(SettingsWindow setWin, PopNode popNode) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		//this.setBackground(new Color(.86f, .22f, .22f, 1.0f));
		//189.0, 59.0, 59.0,
		
		this.popNode = popNode;
		this.setWin = setWin;
		popParser = new PopulationParser(this, setWin);
		
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
		
		initListeners();
		
		for(String s : mapinfo.getMapNames()) {
			mapsModel.addElement(s);
		}
		maps.setModel(mapsModel);
		
		addGB(maps, 1, 1);
		
		addGB(loadPop, 2, 1);
		addGB(loadTemplate, 3, 1);
		
		addGB(currLabel, 0, 2);
		addGB(currSpinner, 1, 2);
		addGB(respawnWaveLabel, 2, 2);
		addGB(respawnWaveSpinner, 3, 2);
		addGB(waveTimeBox, 4, 2);
		
		addGB(busterDmgLabel, 0, 3);
		addGB(busterDmgSpinner, 1, 3);
		addGB(busterKillLabel, 2, 3);
		addGB(busterKillSpinner, 3, 3);
		
		addGB(eventBox, 0, 4);
		addGB(atkSpawnBox, 1, 4);
		//this.addGB(advancedBox, 2, 4);
		addGB(updatePop, 2, 5);
	}
	
	private void initListeners() {
		this.addMouseListener(new MouseListener() { //this is wonky, fix
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
		
		updatePop.addActionListener(event -> {
			updateNode();
			feedback.setText("Population settings updated");
			if(popNode.getMapIndex() > -1) { //if user entered a map
				loadMapInfo(popNode.getMapIndex());
			}
		});
		
		loadPop.addActionListener(event -> {
			File file = getPopFile();
			Map<String, List<TemplateData>> templateMap = new HashMap<String, List<TemplateData>>();
			
			if(file != null) {
				popNode = popParser.parsePopulation(file, templateMap);
				updatePanel();
				propertySupport.firePropertyChange("POPNODE", null, popNode);
				propertySupport.firePropertyChange(INCLUDED, null, templateMap);
			}
		});
		
		loadTemplate.addActionListener(event -> {
			File file = getPopFile();
			Map<String, List<TemplateData>> templateMap = new HashMap<String, List<TemplateData>>();
			
			if(file != null) {
				//Entry<String, List<TemplateData>> entry = popParser.parseTemplates(file, templateMap);
				popParser.parseTemplates(file, templateMap);
				//templateMap.put(entry.getKey(), entry.getValue());
				
				propertySupport.firePropertyChange(IMPORTED, null, templateMap);
			}
		});
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
	
	public void updatePanel() {
		currSpinner.setValue(popNode.getValue(PopNode.STARTINGCURRENCY));
		respawnWaveSpinner.setValue(popNode.getValue(PopNode.RESPAWNWAVETIME));
		waveTimeBox.setSelected((boolean) popNode.getValue(PopNode.FIXEDRESPAWNWAVETIME));
		eventBox.setSelected((boolean) popNode.getValue(PopNode.EVENTPOPFILE));
		busterDmgSpinner.setValue(popNode.getValue(PopNode.BUSTERDAMAGE));
		busterKillSpinner.setValue(popNode.getValue(PopNode.BUSTERKILLS));
		atkSpawnBox.setSelected((boolean) popNode.getValue(PopNode.BOTSATKINSPAWN));
		//advancedBox.setSelected((boolean) popNode.getValueSingular(PopNode.ADVANCED));
	}
	
	//wrapper
	public void fireTemplateChange(String type, TemplateData oldData, TemplateData newData) {
		propertySupport.firePropertyChange(type, oldData, newData);
	}
	
	private File getPopFile() {
		JFileChooser fileChooser;
		if(setWin.getTFPathString() != null) {
			fileChooser = new JFileChooser(setWin.getTFPathString());
		}
		else {
			fileChooser = new JFileChooser();
		}	
		fileChooser.setFileFilter(new EngiWindow.PopFileFilter());
		fileChooser.showOpenDialog(this);
		
		return fileChooser.getSelectedFile();
	}
}
