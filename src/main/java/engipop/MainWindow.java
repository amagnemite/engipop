package engipop;

import javax.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import engipop.Node.*;

//main class
@SuppressWarnings("serial")
public class MainWindow extends EngiWindow implements PropertyChangeListener {
	public static final String BOTTEMPLATEMAP = "BOTTEMPLATEMAP";
	
	JMenuBar menuBar = new JMenuBar();
	JMenu optionsMenu = new JMenu("Options");
	JMenu editorsMenu = new JMenu("Editors");
	JMenu utilitiesMenu = new JMenu("Utilities");
	
	JTabbedPane tabbedPane = new JTabbedPane();
	EngiPanel mainPanel = new EngiPanel();
	
	//todo: update botpanel
	//JMenuItem popSet = new JMenuItem("Open population settings");
	JMenuItem settings = new JMenuItem("Engipop settings");
	//JMenuItem templateSet = new JMenuItem("Template editor");
	//JMenuItem missionSet = new JMenuItem("Mission editor");
	JMenuItem timeline = new JMenuItem("Minimum timeline viewer");
	
	WavePanel wavePanel;
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	TankPanel tankPanel;
	WaveNodePanelManager waveNodeManager;
	TemplateTree templateTree;
	
	JButton createPop = new JButton("Create popfile"); //may consider putting this in window
	
	PopNode popNode = new PopNode();
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	//todo: add a vertical scrollbar
	public MainWindow() {
		super("Engipop main");
		//this.setBackground(new Color(193, 161, 138));
	
		setSize(1500, 1000);
		//gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		mainPanel.setLayout(mainPanel.gbLayout);
		mainPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		mainPanel.feedback = new JLabel(" ");
		
		//may want to reconsider this
		SettingsWindow settingsWindow = new SettingsWindow(this);
		PopulationPanel populationPanel = new PopulationPanel(settingsWindow, popNode);
		TemplatePanel tempPanel = new TemplatePanel(this, populationPanel);
		MissionPanel missionPanel = new MissionPanel(this, populationPanel);
			
		settingsWindow.initConfig();
		populationPanel.addPropertyChangeListener("POPNODE", this);
		
		/*
		popSet.addActionListener(event -> {
			secondaryWindow.updatePanel();
			secondaryWindow.setVisible(true);
		});
		templateSet.addActionListener(event -> {
			if(!tempPanel.isVisible()) {
				tempPanel.setVisible(true);
			}
		});
		missionSet.addActionListener(event -> {
			if(!missionWindow.isVisible()) {
				missionWindow.setVisible(true);
			}
		}); */
		settings.addActionListener(event -> {
			if(!settingsWindow.isVisible()) {
				settingsWindow.updateWindow();
				settingsWindow.setVisible(true);
			}
		});
		timeline.addActionListener(event -> {
			JFileChooser c = new JFileChooser();
			c.setFileFilter(new PopFileFilter());
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
		//optionsMenu.add(popSet);
		//editorsMenu.add(templateSet);
		//editorsMenu.add(missionSet);
		utilitiesMenu.add(timeline);
		menuBar.add(optionsMenu);
		menuBar.add(editorsMenu);
		menuBar.add(utilitiesMenu);
		setJMenuBar(menuBar);
		
		botPanel = new BotPanel(mainPanel, this, populationPanel);
		wsPanel = new WaveSpawnPanel(populationPanel);
		wavePanel = new WavePanel(populationPanel);
		tankPanel = new TankPanel(populationPanel);
		
		waveNodeManager = new WaveNodePanelManager(this, wavePanel, wsPanel, botPanel, tankPanel, populationPanel);
		templateTree = new TemplateTree(populationPanel);
		JPanel listPanel = waveNodeManager.getListPanel();
		JPanel spawnerPanel = waveNodeManager.getSpawnerPanel();
		JScrollPane templateTreePane = templateTree.getTreePane();
		JScrollPane panelScroll = new JScrollPane(mainPanel);
	
		templateTreePane.setMinimumSize(new Dimension(225, templateTreePane.getPreferredSize().height));
		templateTreePane.setPreferredSize(new Dimension(225, templateTreePane.getPreferredSize().height));
		//botPanel.setMinimumSize(botPanel.getPreferredSize());
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
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		mainPanel.addGB(mainPanel.feedback, 0, 1);
		mainPanel.addGB(createPop, 2, 6);
		
		mainPanel.gbConstraints.gridwidth = 2;
		mainPanel.addGB(wavePanel, 0, 2);
		mainPanel.addGB(wsPanel, 0, 3);
		mainPanel.addGB(spawnerPanel, 0, 4);
				
		mainPanel.gbConstraints.gridheight = 2;
		mainPanel.gbConstraints.weighty = 1;
		mainPanel.addGB(botPanel, 0, 5);
		//add insets?
		mainPanel.addGB(tankPanel, 0, 5);
		
		mainPanel.gbConstraints.weighty = 0;
		mainPanel.gbConstraints.gridheight = 4;
		mainPanel.gbConstraints.gridwidth = 1;
		mainPanel.addGB(listPanel, 2, 2);
		
		//mainPanel.gbConstraints.gridheight = 4;
		mainPanel.addGB(templateTreePane, 3, 2);
		
		panelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelScroll.setMinimumSize(mainPanel.getMinimumSize());
		panelScroll.setPreferredSize(mainPanel.getPreferredSize());
		
		tabbedPane.addTab("Main", panelScroll);
		tabbedPane.addTab("Population", populationPanel);
		tabbedPane.addTab("Templates", tempPanel);
		tabbedPane.addTab("Missions", missionPanel);
		
		this.add(tabbedPane);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    	//support.firePropertyChange(BOTTEMPLATELISTFIXED, null, popParse.getBotTemplateList());
    }
	
	public PopNode getPopNode() {
		return this.popNode;
	}
	
	public EngiPanel getMainPanel() {
		return this.mainPanel;
	}
	
	private void generateFile() { //get filename/place to save pop at
		JFileChooser c = new JFileChooser();
		c.setFileFilter(new PopFileFilter());
		c.showSaveDialog(this);
		//if(result == JFileChooser.CANCEL_OPTION) return;
		//try { //double check
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
		//}
		//catch(IOException i) {
			
		//}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = (PopNode) evt.getNewValue();
	}
}