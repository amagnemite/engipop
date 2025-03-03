package engipop;

import javax.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;

import engipop.Node.*;

//main class
@SuppressWarnings("serial")
public class MainWindow extends EngiWindow implements PropertyChangeListener {
	public static final String BOTTEMPLATEMAP = "BOTTEMPLATEMAP";
	
	JMenuBar menuBar = new JMenuBar();
	JMenu optionsMenu = new JMenu("Options");
	JMenu utilitiesMenu = new JMenu("Utilities");
	
	JTabbedPane tabbedPane = new JTabbedPane();
	EngiPanel mainPanel = new EngiPanel();
	WaveBarPanel wavebar = new WaveBarPanel();
	
	//todo: update botpanel
	JMenuItem settings = new JMenuItem("Settings");
	JMenuItem timeline = new JMenuItem("Minimum timeline viewer");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem saveAs = new JMenuItem("Save as");
	
	WavePanel wavePanel;
	WaveSpawnPanel wsPanel;
	WaveNodePanelManager waveNodeManager;
	TemplateTree templateTree;
	
	JLabel feedback = new JLabel("");
	
	PopNode popNode;
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private File fileLocation = null;
	
	public MainWindow() {
		super("Engipop");
		//this.setBackground(new Color(193, 161, 138));
		
		setLayout(new BorderLayout());
		
		popNode = Engipop.getPopNode();
		setSize(1200, 850);
		
		mainPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		SettingsWindow settingsWindow = new SettingsWindow(this);
		PopulationPanel populationPanel = new PopulationPanel(this, settingsWindow);
		TemplatePanel tempPanel = new TemplatePanel(this, populationPanel);
		MissionPanel missionPanel = new MissionPanel(this, populationPanel, wavebar);
		
		populationPanel.addPropertyChangeListener("POPNODE", this);
		
		String[] valvePops = {"robot_standard.pop", "robot_giant.pop", "robot_gatebot.pop"};
		for(String pop : valvePops) {
			URL popURL = MainWindow.class.getResource("/" + pop);
			PopulationParser popParser = new PopulationParser(this, settingsWindow);
			
			popParser.parseTemplates(pop, popURL, PopulationPanel.IMPORTED);
		}
		
		optionsMenu.add(settings);
		optionsMenu.addSeparator();
		optionsMenu.add(save);
		optionsMenu.add(saveAs);
		utilitiesMenu.add(timeline);
		menuBar.add(optionsMenu);
		menuBar.add(utilitiesMenu);
		setJMenuBar(menuBar);
		
		wsPanel = new WaveSpawnPanel(populationPanel, this);
		wavePanel = new WavePanel(populationPanel);
		
		waveNodeManager = new WaveNodePanelManager(this, wavePanel, wsPanel, populationPanel, wavebar);
		templateTree = new TemplateTree(populationPanel);
		JPanel listPanel = waveNodeManager.getListPanel();
		EngiPanel spawnerPanel = waveNodeManager.getSpawnerPanel();
		EngiPanel populationFillerPanel = new EngiPanel();
		JScrollPane templateTreePane = templateTree.getTreePane();
		JScrollPane panelScroll = new JScrollPane(mainPanel);
		//EngiPanel wavebarPanel = new EngiPanel();
		
		//templateTreePane.setMinimumSize(new Dimension(225, templateTreePane.getPreferredSize().height));
		templateTreePane.setPreferredSize(new Dimension(225, templateTreePane.getPreferredSize().height));
		//wavebar.setPreferredSize(new Dimension(WaveBarIcon.WIDTH, WaveBarIcon.HEIGHT));
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		initListeners(settingsWindow);
		
		//for positioning populationpanel since it's much smaller than the rest
		populationFillerPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		populationFillerPanel.gbConstraints.weightx = 1;
		populationFillerPanel.gbConstraints.weighty = 1;
		populationFillerPanel.addGB(populationPanel, 0, 0);
		
		//wavebarPanel.setBorder(BorderFactory.createTitledBorder("Wavebar preview"));
		/*
		wavebarPanel.gbConstraints.anchor = GridBagConstraints.WEST;
		wavebarPanel.addGB(new JLabel("Wavebar preview"), 0, 0);
		wavebarPanel.addGB(waveNodeManager.getRefreshButton(), 0, 2);
		
		wavebarPanel.gbConstraints.anchor = GridBagConstraints.CENTER;
		wavebarPanel.gbConstraints.gridwidth = 2;
		wavebarPanel.addGB(wavebar, 0, 1);
		*/
		
		mainPanel.gbConstraints.gridwidth = 2;
		mainPanel.addGB(wavebar, 0, 0);
		//mainPanel.addGB(wavebarPanel, 0, 0);
		mainPanel.gbConstraints.gridwidth = 1;
		//mainPanel.addGB(waveNodeManager.getRefreshButton(), 0, 1);
		
		mainPanel.gbConstraints.gridwidth = 2;
		mainPanel.addGB(wavePanel.getDisabledPanel(), 0, 2);
		mainPanel.addGB(wsPanel.getDisabledPanel(), 0, 3);
		mainPanel.addGB(spawnerPanel.getDisabledPanel(), 0, 4);
				
		mainPanel.gbConstraints.gridheight = 2;
		mainPanel.gbConstraints.weighty = 1;
		mainPanel.addGB(waveNodeManager.getBotTankPanel().getDisabledPanel(), 0, 5);
		//add insets?
		
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
		tabbedPane.addTab("Population settings", populationFillerPanel);
		tabbedPane.addTab("Templates", tempPanel);
		tabbedPane.addTab("Missions", missionPanel);
		
		add(feedback, BorderLayout.PAGE_START);
		add(tabbedPane, BorderLayout.CENTER);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void initListeners(SettingsWindow settingsWindow) {
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
		
		save.addActionListener(event -> {
			if(fileLocation == null) {
				generateFile();
			}
			else {
				new TreeParse().parseTree(fileLocation, popNode);
				feedback.setText("Popfile successfully generated!");
			}
		});
		
		saveAs.addActionListener(event -> {
			generateFile();
		});
	}
	
	private void generateFile() { //get filename/place to save pop at
		String error = new TreeParse().treeCheck(popNode);
		if(!error.isEmpty()) {
			feedback.setText(error);
			return;
		} 
		
		JFileChooser c = new JFileChooser();
		boolean write = false;
		boolean appendExt = false;
		
		c.setFileFilter(new PopFileFilter());
		if(c.showSaveDialog(this) == JFileChooser.CANCEL_OPTION) {
			return;
		}
		//try { //double check
			File file = c.getSelectedFile();
			if(file.exists()) { //confirm overwrite
				int op = JOptionPane.showConfirmDialog(this, "Overwrite this file?", "Select an Option",
						JOptionPane.YES_NO_OPTION);
				if(op == JOptionPane.YES_OPTION) {
					write = true;
				}
			}
			else { //if it doesn't exist, no overwrite check needed
				write = true;
				
				//make sure the filename actually ends in .pop
				String extension = file.getName();
				int i = extension.lastIndexOf('.');
				if (i > 0 && i < extension.length() - 1) {
					extension = extension.substring(i+1).toLowerCase();
		        }
				else {
					appendExt = true;
				}
				
				if(!extension.equals("pop")) {
					appendExt = true;
				}
				
				if(appendExt) { //TODO: make sure this works on linux
					file = new File(file.getPath() + ".pop");
				}
			}
			
			if(write) {
				new TreeParse().parseTree(file, popNode);
				feedback.setText("Popfile successfully generated!");
				fileLocation = file;
			}
		//}
		//catch(IOException i) {
			
		//}
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			//Font font = new Font("TF2 Build", Font.PLAIN, 12);
			
			//UIDefaults defaultUI = UIManager.getDefaults();
			//defaultUI.put("Label.font", new Font("TF2 Build", Font.PLAIN, 12));
			
			/*
			Font sec = new Font("TF2 secondary", Font.PLAIN, 12);
			defaultUI.put("Button.font", sec);
			defaultUI.put("ComboBox.font", sec);
			defaultUI.put("TextArea.font", sec); 
			defaultUI.put("Spinner.font", sec);
			*/
		}
		catch (Exception e) {
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

	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = Engipop.getPopNode();
	}
}