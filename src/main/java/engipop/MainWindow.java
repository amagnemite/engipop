package engipop;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import engipop.Node.*;

//main class
@SuppressWarnings("serial")
public class MainWindow extends EngiWindow {
	public static final String ITEMPARSE = "itemparse";
	public static final String BOTTEMPLATELISTFIXED = "bottemplatelistfixed";
	public static final String WSTEMPLATELIST = "wstemplatelist";
	
	JMenuBar menuBar = new JMenuBar();
	JMenu optionsMenu = new JMenu("Options");
	
	//todo: update botpanel
	JMenuItem popSet = new JMenuItem("Open population settings");
	JMenuItem settings = new JMenuItem("Open Engipop settings");
	JMenuItem templateSet = new JMenuItem("Template settings");
	
	WavePanel wavePanel;
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	TankPanel tankPanel;
	WaveNodePanelManager waveNodeManager;
	
	JPanel spawnerPanel;
	JPanel listPanel;
	
	JButton createPop = new JButton("Create popfile"); //may consider putting this in window
	
	PopNode popNode = new PopNode(); //minimum working pop
	
	ItemParser itemParser = new ItemParser();
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	//todo: add a vertical scrollbar
	public MainWindow() {
		super("Engipop main");
	
		setSize(1400, 1000);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		
		feedback = new JLabel(" ");
		
		//may want to reconsider this
		SecondaryWindow w2 = new SecondaryWindow(popNode, this);
		TemplateWindow tempWindow = new TemplateWindow(this, w2);
		
		popSet.addActionListener(event -> {
			w2.updatePopPanel();
			w2.setVisible(true);
		});
		templateSet.addActionListener(event -> {
			if(!tempWindow.isVisible()) {
				tempWindow.setVisible(true);
			}
		});
		
		templateSet.setMaximumSize(new Dimension(150, 50));
		
		optionsMenu.add(settings);
		optionsMenu.add(popSet);
		menuBar.add(optionsMenu);
		menuBar.add(templateSet);
		setJMenuBar(menuBar);		

		botPanel = new BotPanel(this, this, w2);
		wsPanel = new WaveSpawnPanel(w2);
		wavePanel = new WavePanel(w2);
		tankPanel = new TankPanel(w2);
		
		waveNodeManager = new WaveNodePanelManager(this, wavePanel, wsPanel, botPanel, tankPanel);
		listPanel = waveNodeManager.makeListPanel();
		spawnerPanel = waveNodeManager.makeSpawnerPanel();
		
		tankPanel.setVisible(false);
		
		/*
		JPanel filler = new JPanel();
		filler.setSize(new Dimension(100, 200));
		filler.setMinimumSize(filler.getPreferredSize()); */
		
		createPop.addActionListener(event -> { //potentially move this
			String error = new TreeParse().treeCheck(tree);
			if(error.isEmpty()) {
				generateFile();
			}
			else {
				feedback.setText(error);
			}	 
		});
		
		//addGB(filler, 0, 0);
		addGB(feedback, 0, 1);
		
		gbConstraints.gridwidth = 2;
		addGB(wavePanel, 0, 2);
		addGB(wsPanel, 0, 3);
		
		addGB(spawnerPanel, 0, 4);
		
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbConstraints.gridwidth = 2;
		addGB(tankPanel, 0, 5);
		
		gbConstraints.gridheight = 2;
		gbConstraints.weighty = 1;
		addGB(botPanel, 0, 5);
		
		gbConstraints.weighty = 0;
		gbConstraints.gridwidth = 1;
		gbConstraints.gridheight = 3;
		addGB(listPanel, 2, 3);
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		w2.requestFocus();
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    //
		}
		
		MainWindow w = new MainWindow();
		SettingsWindow setW = new SettingsWindow(w);
		
		setW.initConfig(w);
		
		w.listen(setW); //could change this
		
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
	
	//listeners that interact with other windows
	void listen(SettingsWindow sw) {
		settings.addActionListener(event -> {
			if(!sw.isVisible()) {
				sw.updateWindow();
				sw.setVisible(true);
			}
		});
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
					new TreeParse().parseTree(file, tree);
					feedback.setText("Popfile successfully generated!");
				} //kinda jank
			}
			else { //if it doesn't exist, no overwrite check needed
				new TreeParse().parseTree(file, tree);
				feedback.setText("Popfile successfully generated!");
			}
		}
		catch(Exception e) {
			
		}
	}
}