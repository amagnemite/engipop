package engipop;

import javax.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

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
	WaveBarPanel wavebar = new WaveBarPanel();
	
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
	
	JLabel feedback = new JLabel("");
	JButton createPop = new JButton("Create popfile"); //may consider putting this in window
	
	PopNode popNode;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	//todo: add a vertical scrollbar
	public MainWindow() {
		super("Engipop main");
		//this.setBackground(new Color(193, 161, 138));
		
		popNode = Engipop.getPopNode();
		setSize(1500, 1000);
		
		mainPanel.setLayout(mainPanel.gbLayout);
		mainPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		//may want to reconsider this
		SettingsWindow settingsWindow = new SettingsWindow(this);
		PopulationPanel populationPanel = new PopulationPanel(this, settingsWindow);
		TemplatePanel tempPanel = new TemplatePanel(this, populationPanel);
		MissionPanel missionPanel = new MissionPanel(this, populationPanel, wavebar);
			
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
		
		waveNodeManager = new WaveNodePanelManager(this, wavePanel, wsPanel, botPanel, tankPanel, populationPanel, wavebar);
		templateTree = new TemplateTree(populationPanel);
		JPanel listPanel = waveNodeManager.getListPanel();
		JPanel spawnerPanel = waveNodeManager.getSpawnerPanel();
		EngiPanel populationFillerPanel = new EngiPanel();
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
		
		//for positioning populationpanel since it's much smaller than the rest
		populationFillerPanel.setLayout(populationFillerPanel.gbLayout);
		populationFillerPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		populationFillerPanel.gbConstraints.weightx = 1;
		populationFillerPanel.gbConstraints.weighty = 1;
		populationFillerPanel.addGB(populationPanel, 0, 0);
		
		mainPanel.addGB(wavebar, 0, 0);
		mainPanel.addGB(feedback, 0, 1);
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
		
		populationFillerPanel.setMinimumSize(mainPanel.getMinimumSize());
		populationFillerPanel.setPreferredSize(mainPanel.getPreferredSize());
		//populationFillerPanel.setBackground(Color.BLUE);
		
		tabbedPane.addTab("Main", panelScroll);
		tabbedPane.addTab("Population", populationFillerPanel);
		tabbedPane.addTab("Templates", tempPanel);
		tabbedPane.addTab("Missions", missionPanel);
		
		add(tabbedPane);
		
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
		//SwingUtilities.updateComponentTreeUI(w);
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
	
	public EngiPanel getMainPanel() {
		return this.mainPanel;
	}
	
	public void setFeedback(String string) {
		feedback.setText(string);
	}
	
	/* TODO: automate clearing feedback
	private void clearFeedback() { //clears feedback so things don't get stuck on it
		if(!feedback.getText().equals(" ")) {
			feedback.setText(" ");
		}
	} */
	
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
		this.popNode = Engipop.getPopNode();
	}
}