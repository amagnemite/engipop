package engipop;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.*;

import engipop.Tree.PopNode;

public class SecondaryWindow extends JFrame { //window for less important/one off deals
	
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gb = new GridBagConstraints();

	JPanel popPanel;
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
	JLabel feedback;
	PopNode pn;
	
	public SecondaryWindow(PopNode pn, window w) {
		super("Population settings");
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.NORTHWEST;
		setSize(800, 200);
		
		URL iconURL = getClass().getResource("/icon.png");
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		
		this.pn = pn;
		popPanel = new JPanel();
		makePopPanel(w);
		
		addGB(this, gb, popPanel, 0, 0);
		
		setVisible(true);
		requestFocus();
	}
	
	void makePopPanel(window w) { //makes population panel
		popPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		gb.anchor = GridBagConstraints.EAST;
		
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
		
		updatePop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				updateNode();
				feedback.setText("Population settings updated");
				if(pn.getMapIndex() > -1) { //if user entered a map
					w.loadMap(pn.getMapIndex());
				}
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
		JLabel mapLabel = new JLabel("Map ");
		
		addGB(popPanel, c, feedback, 0, 0);
		
		addGB(popPanel, c, mapLabel, 0, 1);
		addGB(popPanel, c, new JScrollPane(maps), 1, 1);
		
		addGB(popPanel, c, currLabel, 0, 2);
		addGB(popPanel, c, currSpinner, 1, 2);
		addGB(popPanel, c, respawnWaveLabel, 2, 2);
		addGB(popPanel, c, respawnWaveSpinner, 3, 2);
		addGB(popPanel, c, waveTimeBox, 4, 2);
		
		addGB(popPanel, c, busterDmgLabel, 0, 3);
		addGB(popPanel, c, busterDmgSpinner, 1, 3);
		addGB(popPanel, c, busterKillLabel, 2, 3);
		addGB(popPanel, c, busterKillSpinner, 3, 3);
		
		addGB(popPanel, c, eventBox, 0, 4);
		addGB(popPanel, c, atkSpawnBox, 1, 4);
		addGB(popPanel, c, advancedBox, 2, 4);
		addGB(popPanel, c, updatePop, 2, 5);
	}
	
	private void clearFeedback() { //clears feedback so things don't get stuck on it
		if(!feedback.getText().equals(" ")) {
			feedback.setText(" ");
		}
	}
	
	private void addGB(Container cont, GridBagConstraints gb, Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		cont.add(comp, gb);
	}
	
	private void updateNode() {
		pn.setMapIndex(maps.getSelectedIndex());
		pn.setCurrency((int) currSpinner.getValue());
		pn.setWaveTime((int) respawnWaveSpinner.getValue());
		pn.setFixedWaveTime(waveTimeBox.isSelected());
		pn.setEventPop(eventBox.isSelected());
		pn.setBusterDmg((int) busterDmgSpinner.getValue());
		pn.setBusterKills((int) busterKillSpinner.getValue());
		pn.setAtkInSpawn(atkSpawnBox.isSelected());
		pn.setAdvanced(advancedBox.isSelected());
	}
	
	public void updatePopPanel() {
		currSpinner.setValue(pn.getCurrency());
		respawnWaveSpinner.setValue(pn.getWaveTime());
		waveTimeBox.setSelected(pn.getFixedWaveTime());
		eventBox.setSelected(pn.getEventPop());
		busterDmgSpinner.setValue(pn.getBusterDmg());
		busterKillSpinner.setValue(pn.getBusterKills());
		atkSpawnBox.setSelected(pn.getAtkInSpawn());
		advancedBox.setSelected(pn.getAdvanced());
	}
	/*
	public void fillMap(MapInfo info) {
		for(String s : info.getMapNames()) {
			mapsModel.addElement(s);
		}
	} */
}
