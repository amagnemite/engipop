package engipop;

import javax.swing.*;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import engipop.Node.*;

//main class
@SuppressWarnings("serial")
public class MainWindow extends EngiWindow {
	public static final String ITEMPARSE = "itemparse";
	public static final String TFBOT = "TFBOT";
	public static final String WAVESPAWN = "WAVESPAWN";
	public static final String BOTTEMPLATEMAP = "BOTTEMPLATEMAP";
	
	JMenuBar menuBar = new JMenuBar();
	JMenu optionsMenu = new JMenu("Options");
	JMenu editorsMenu = new JMenu("Editors");
	JMenu utilitiesMenu = new JMenu("Utilities");
	
	//todo: update botpanel
	JMenuItem popSet = new JMenuItem("Open population settings");
	JMenuItem settings = new JMenuItem("Open Engipop settings");
	JMenuItem templateSet = new JMenuItem("Template editor");
	JMenuItem timeline = new JMenuItem("Minimum timeline viewer");
	
	WavePanel wavePanel;
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	TankPanel tankPanel;
	WaveNodePanelManager waveNodeManager;
	//JPanel listPanel;
	//JPanel spawnerPanel;
	
	JButton createPop = new JButton("Create popfile"); //may consider putting this in window
	
	PopNode popNode = new PopNode();
	
	ItemParser itemParser = new ItemParser();
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	//todo: add a vertical scrollbar
	public MainWindow() {
		super("Engipop main");
	
		setSize(1400, 1000);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		feedback = new JLabel(" ");
		
		//may want to reconsider this
		SettingsWindow settingsWindow = new SettingsWindow(this);
		SecondaryWindow secondaryWindow = new SecondaryWindow(settingsWindow, popNode);
		TemplateWindow tempWindow = new TemplateWindow(this, secondaryWindow);
			
		settingsWindow.initConfig();	
		
		popSet.addActionListener(event -> {
			secondaryWindow.updatePopPanel();
			secondaryWindow.setVisible(true);
		});
		templateSet.addActionListener(event -> {
			if(!tempWindow.isVisible()) {
				tempWindow.setVisible(true);
			}
		});
		settings.addActionListener(event -> {
			if(!settingsWindow.isVisible()) {
				settingsWindow.updateWindow();
				settingsWindow.setVisible(true);
			}
		});
		timeline.addActionListener(event -> {
			JFileChooser c = new JFileChooser();
			c.showSaveDialog(this);
			//if(result == JFileChooser.CANCEL_OPTION) return;
			try { //double check
				File file = c.getSelectedFile();
				new MinTimeline().parsePopulation(file);
			}
			catch(Exception e) {
				
			}
		});
		
		optionsMenu.add(settings);
		optionsMenu.add(popSet);
		editorsMenu.add(templateSet);
		utilitiesMenu.add(timeline);
		menuBar.add(optionsMenu);
		menuBar.add(editorsMenu);
		menuBar.add(utilitiesMenu);
		setJMenuBar(menuBar);		

		botPanel = new BotPanel(this, this, secondaryWindow);
		wsPanel = new WaveSpawnPanel(secondaryWindow);
		wavePanel = new WavePanel(secondaryWindow);
		tankPanel = new TankPanel(secondaryWindow);
		
		waveNodeManager = new WaveNodePanelManager(this, wavePanel, wsPanel, botPanel, tankPanel, secondaryWindow);
		JPanel listPanel = waveNodeManager.getListPanel();
		JPanel spawnerPanel = waveNodeManager.getSpawnerPanel();
		
		tankPanel.setVisible(false);
		//tankPanel.setPreferredSize(botPanel.getPreferredSize());
		
		createPop.addActionListener(event -> { //potentially move this
			String error = new TreeParse().treeCheck(popNode);
			if(error.isEmpty()) {
				generateFile();
			}
			else {
				feedback.setText(error);
			}	 
		});
		
		addGB(feedback, 0, 1);
		
		gbConstraints.gridwidth = 2;
		addGB(wavePanel, 0, 2);
		
		//gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		addGB(wsPanel, 0, 3);
		
		addGB(spawnerPanel, 0, 4);
				
		gbConstraints.gridheight = 2;
		gbConstraints.weighty = 1;
		addGB(botPanel, 0, 5);
		//add insets?
		addGB(tankPanel, 0, 5);
		
		gbConstraints.weighty = 0;
		gbConstraints.gridheight = 3;
		gbConstraints.gridwidth = 1;
		addGB(listPanel, 2, 3);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		secondaryWindow.requestFocus();
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    //
		}
		
		MainWindow w = new MainWindow();
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
	

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
	
    public void updatePropertyListeners() {
    	support.firePropertyChange(ITEMPARSE, null, itemParser);
    	//support.firePropertyChange(BOTTEMPLATELISTFIXED, null, popParse.getBotTemplateList());
    }
	
	public PopNode getPopNode() {
		return this.popNode;
	}
	
	//take file, parse it, let botpanels know
	public void parseItems(File itemsTxt) { 
		//itemparser = 
		itemParser.parse(itemsTxt, this);
		updatePropertyListeners();
		
		//try {
			//new PopulationParser(this, setW).parseTemplates(Paths.get((this.getClass().getResource("/robot_standard.pop")).toURI()).toFile());
		//} catch (URISyntaxException e) {
		//}
		//popParse.parseTemplates(itemParser, new File());
	}
	
	private void generateFile() { //get filename/place to save pop at
		JFileChooser c = new JFileChooser();
		c.showSaveDialog(this);
		//if(result == JFileChooser.CANCEL_OPTION) return;
		try { //double check
			File file = c.getSelectedFile();
			if(file.exists()) { //confirm overwrite
				int op = JOptionPane.showConfirmDialog(this, "Overwrite this file?");
				if (op == JOptionPane.YES_OPTION) {
					new TreeParse().parseTree(file, popNode);
					feedback.setText("Popfile successfully generated!");
				}
			}
			else { //if it doesn't exist, no overwrite check needed
				new TreeParse().parseTree(file, popNode);
				feedback.setText("Popfile successfully generated!");
			}
		}
		catch(Exception e) {
			
		}
	}
}