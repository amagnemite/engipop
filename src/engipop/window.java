package engipop;

import javax.swing.*;
import javax.swing.JToggleButton.*;
import javax.swing.UIManager.*;
import javax.swing.event.*;
import javax.swing.text.Document;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

import engipop.Tree.*;
import engipop.TreeParse.*;

public class window {
	JFrame frame = new JFrame("Engipop main");
	GridBagConstraints constraints = new GridBagConstraints();
	GridBagLayout frameGB = new GridBagLayout();
	
	JMenuBar menuBar = new JMenuBar();
	JMenu options;
	JMenuItem popSet;
	
	BotPanel botPanel;
	WaveSpawnPanel wsPanel;
	
	JPanel listPanel = new JPanel();
	JPanel waveSpawnListPanel = new JPanel();
	
	DefaultListModel<String> waveListModel = new DefaultListModel<String>();
	DefaultListModel<String> waveSpawnListModel = new DefaultListModel<String>();
	
	public static final String[] CLASSES = {"Scout", "Soldier", "Pyro",
									"Demoman", "HeavyWeapons", "Engineer",
									"Medic", "Sniper", "Spy"};
	
	PopNode popNode = new PopNode(); //minimum working pop
	
	WaveNode currentWaveNode = new WaveNode();
	WaveSpawnNode currentWSNode = new WaveSpawnNode();
	TFBotNode currentBotNode = new TFBotNode();
	
	JLabel feedback;
	
	int waveIndex = 0;
	int waveSpawnIndex = 0;
	
	Tree tree = new Tree(popNode);
	
	public window() {

		try {
			/* for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		       
		    }*/
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    	//meatloaf said nimbus is ugly  
		} catch (Exception e) {
		    //
		}
		
		URL iconURL = getClass().getResource("/icon.png");
		ImageIcon icon = new ImageIcon(iconURL);
		frame.setIconImage(icon.getImage());
		
		frame.setLayout(frameGB);
		frame.setSize(1000, 800);
		
		popSet = new JMenuItem("Open population settings");
		options = new JMenu("Options");
		options.add(popSet);
		menuBar.add(options);
		frame.setJMenuBar(menuBar);
		
		botPanel = new BotPanel();
		wsPanel = new WaveSpawnPanel();
		feedback = new JLabel("	");
		
		constraints.anchor = GridBagConstraints.WEST;
		
		//instead of immediately creating, allow user to create as necessary
		//and at parse check if one of each exists
		//currentWaveNode.connectNodes(popNode);
		//currentWSNode.connectNodes(currentWaveNode);

		makeListPanel();

		constraints.insets = new Insets(5, 0, 0, 0);
		
		addGB(frame, constraints, feedback, 0, 1);
		constraints.anchor = GridBagConstraints.NORTHWEST;
		
		addGB(frame, constraints, wsPanel, 0, 2);
		addGB(frame, constraints, botPanel, 0, 3);
		
		constraints.gridheight = 2;
		addGB(frame, constraints, listPanel, 2, 2);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String args[]) {
		window w = new window();
		SecondaryWindow w2 = new SecondaryWindow(w.getPopNode());
		
		w.listen(w2);
	}
	
	public PopNode getPopNode() {
		return this.popNode;
	}
	
	void listen(SecondaryWindow w) { //reshow pop settings if selected
		popSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				w.updatePopPanel();
				w.setVisible(true);
			}
		});
	}
	
	void updateWaveSpawn(int index) { //refreshes the list, updates the current node, updates the panel
		getWaveSpawnList();
		currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(index);
		wsPanel.updatePanel(currentWSNode);
	}
	
	void updateBot() {
		currentBotNode = (TFBotNode) currentWSNode.getChildren().get(0);
		botPanel.updatePanel(currentBotNode);
	}
	
	void makeListPanel() { //panel for all the lists and various buttons to edit them
		listPanel.setLayout(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.anchor = GridBagConstraints.NORTHWEST;
		gb.insets = new Insets(5, 0, 5, 5);
		
		String createWaveMsg = "Create empty wave";
		String addWaveMsg = "Add wave";
		
		String createWSMsg = "Create empty wavespawn";
		String addWSMsg = "Add wavespawn"; //to population?
		
		String updateBotMsg = "Update bot";
		String addBotMsg = "Add bot";
		
		JList<String> listWaveList = new JList<String>(waveListModel);
		listWaveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//listWaveList.setPrototypeCellValue("wave7a");
		
		JLabel listCurrentWave = new JLabel("Editing wave 1");
		JButton addWave = new JButton(addWaveMsg);
		JButton removeWave = new JButton("Remove wave");
		removeWave.setEnabled(false);
		
		JList<String> waveSpawnList = new JList<String>(waveSpawnListModel);
		waveSpawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JLabel listCurrentWSLabel = new JLabel("Editing new wavespawn");
		
		JButton listAddWaveSpawn = new JButton(addWSMsg);
		listAddWaveSpawn.setPreferredSize(new Dimension(159, 22));
		//+2 for padding or something
		
		JButton listRemoveWaveSpawn = new JButton("Remove wavespawn");
		listRemoveWaveSpawn.setEnabled(false);
		
		JButton listUpdateWaveSpawn = new JButton("Update wavespawn");
		listUpdateWaveSpawn.setEnabled(false);
		
		JButton addBot = new JButton(addBotMsg); //can be linked to unlinked wavespawns
		
		JButton createPop = new JButton("Create popfile");
		//JFileChooser chooser;
		
		listWaveList.addListSelectionListener(new ListSelectionListener() { //when a wave is selected from list
			 public void valueChanged(ListSelectionEvent l) {
				waveIndex = listWaveList.getSelectedIndex();
				feedback.setText(" ");
				
				if(waveIndex != -1) { //prevents listener fits
					currentWaveNode = (WaveNode) popNode.getChildren().get(waveIndex); //populate subwave list, subwave panel with first subwave, first tfbot 
					listCurrentWave.setText("Editing wave " + Integer.toString(waveIndex + 1));
					addWave.setText(createWaveMsg);
					
					//if user decided to add wave before making a wavespawn 
					if(currentWaveNode.getChildren().size() > 0) {
						updateWaveSpawn(0);
						listAddWaveSpawn.setText(createWSMsg);
						if(currentWSNode.getChildren().size() > 0) {
							updateBot();
							addBot.setText(updateBotMsg);
						}
						else {
							wsPanel.clearPanel();
							listAddWaveSpawn.setText(createWSMsg);
						}
					}
					else { //don't have a wavelist getting stuck
						waveSpawnListModel.clear();
					}
				}
			} 
		});
		
		addWave.addActionListener(new ActionListener () { //adds a new wave to end of list 
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				
				if(addWave.getText().equals(addWaveMsg)) {
					currentWaveNode.connectNodes(popNode);
					listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size())); //indexed from 1 
				}
				else {
					addWave.setText(addWaveMsg);
					listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size() + 1));
				}
				currentWaveNode = new WaveNode(); 
				
				getWaveList();
				//getWaveSpawnList(); //need to hide list if no items
				waveSpawnListModel.clear();
				wsPanel.clearPanel();
				botPanel.clearPanel();
				//may not be necessary, but makes sure existing subnodes can't get linked
				currentWSNode = new WaveSpawnNode();
				currentBotNode = new TFBotNode();
				
				addBot.setText(addBotMsg);
				
				removeWave.setEnabled(true);
			} 
		});
		
		removeWave.addActionListener(new ActionListener () { //remove the selected wave 
			public void actionPerformed(ActionEvent a) {
				List<Node> list = popNode.getChildren();
				feedback.setText(" ");
				
				if(listWaveList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node 
					list.remove(list.size() - 1); 
				} 
				else {
					list.remove(listWaveList.getSelectedIndex()); 
				}				
				getWaveList();
				
				if(list.size() == 0) { //double check subnode states here
					currentWaveNode = new WaveNode();
					listCurrentWave.setText("Editing wave 1");
					removeWave.setEnabled(false);
				}
				else { //set current wave and its subnodes
					currentWaveNode = (WaveNode) list.get(list.size() - 1);
					listCurrentWave.setText("Editing wave " + Integer.toString(popNode.getChildren().size()));
					if(currentWaveNode.getChildren().size() > 0) {
						updateWaveSpawn(0);
						listAddWaveSpawn.setText(createWSMsg);
						listCurrentWSLabel.setText("Editing wavespawn " + currentWSNode.getName());
						if(currentWSNode.getChildren().size() > 0) {
							updateBot();
							addBot.setText(updateBotMsg);
						}
						else {
							addBot.setText(addBotMsg);
						}
					}
					else {
						waveSpawnListModel.clear();
					}
					
				}
			}
		});
		
		waveSpawnList.addListSelectionListener(new ListSelectionListener() { //when wavespawn is selected from list
			public void valueChanged(ListSelectionEvent l) {
				waveSpawnIndex = waveSpawnList.getSelectedIndex();
				feedback.setText(" ");
				
				//prevent fits whenever wavespawnlistmodel is updated
				if(waveSpawnIndex != -1) {
					currentWSNode = (WaveSpawnNode) currentWaveNode.getChildren().get(waveSpawnIndex);
					System.out.println(currentWSNode.getChildren());
					listCurrentWSLabel.setText("Editing wavespawn " + currentWSNode.getName());
					listUpdateWaveSpawn.setEnabled(true);
					listAddWaveSpawn.setText(createWSMsg);
					listRemoveWaveSpawn.setEnabled(true);
					wsPanel.updatePanel(currentWSNode);
					if(currentWSNode.getChildren().size() > 0) {
						updateBot();
						addBot.setText(updateBotMsg);
					}
					else {
						botPanel.clearPanel();
						currentBotNode = new TFBotNode();
						addBot.setText(addBotMsg);
					}
				}
				else { //disable updating when there is not a subwave explicitly selected
					listUpdateWaveSpawn.setEnabled(false);
				}
			}
		});
		
		listAddWaveSpawn.addActionListener(new ActionListener() { //add/update button is clicked
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				System.out.println(currentWSNode);
				
				if(listAddWaveSpawn.getText().equals(addWSMsg)) { //if we're adding a wavespawn
					wsPanel.updateNode(currentWSNode);
					currentWSNode.connectNodes(currentWaveNode);
					
					if(currentWaveNode.getParent() == null) {
						feedback.setText("WaveSpawn was successfully linked to the current wave, but the wave itself is unlinked");
					}
				}
				else { //creating a new one
					listAddWaveSpawn.setText(addWSMsg);
				}
				currentWSNode = new WaveSpawnNode();
				
				listRemoveWaveSpawn.setEnabled(true); //might put a check for this
				
				getWaveSpawnList();
				listCurrentWSLabel.setText("Editing new wavespawn");
				wsPanel.clearPanel();
				botPanel.clearPanel();
				
				currentBotNode = new TFBotNode();
				addBot.setText(addBotMsg);
			}
		});
		
		listRemoveWaveSpawn.addActionListener(new ActionListener() { //remove button clicked
			public void actionPerformed(ActionEvent a) {
				List<Node> list = currentWaveNode.getChildren();
				feedback.setText(" ");
				
				if(waveSpawnList.getSelectedIndex() == -1) { //if there's nothing selected, fallback to removing the last node 
					list.remove(list.size() - 1); 
				} 
				else {
					list.remove(waveSpawnList.getSelectedIndex()); 
				}				
				getWaveSpawnList();
				
				if(list.size() == 0) { //if no wavespawns again
					listRemoveWaveSpawn.setEnabled(false);
					currentWSNode = new WaveSpawnNode();
					listCurrentWSLabel.setText("Editing new wavespawn");
				}
				else {
					currentWSNode = (WaveSpawnNode) list.get(list.size() - 1);
					listCurrentWSLabel.setText("Editing wavespawn " + currentWSNode.getName());
					listAddWaveSpawn.setText(createWSMsg);
					if(currentWSNode.getChildren().size() > 0) {
						updateBot();
						addBot.setText(updateBotMsg);
					}
					else {
						currentBotNode = new TFBotNode();
						botPanel.clearPanel();
						addBot.setText(addBotMsg);
					}
				}	
			}
		});
		
		listUpdateWaveSpawn.addActionListener(new ActionListener() { //update wavespawn button clicked
			public void actionPerformed(ActionEvent a) {
				feedback.setText(" ");
				wsPanel.updateNode(currentWSNode);
				getWaveSpawnList();
			}
		});
		
		addBot.addActionListener(new ActionListener() { //adds bot when clicked
			public void actionPerformed(ActionEvent a) { //will need new contexts later
				if(addBot.getText().equals(updateBotMsg)) {
					botPanel.updateNode(currentBotNode);
					feedback.setText("Bot successfully updated");
				}
				else {
					botPanel.updateNode(currentBotNode);
					currentBotNode.connectNodes(currentWSNode);
					currentBotNode = new TFBotNode();
					if(currentBotNode.getParent() == null && listAddWaveSpawn.getText().equals(addWSMsg)) {
						feedback.setText("Bot successfully created, but the wavespawn it is linked to is currently unadded");
					}
					else {
						feedback.setText("Bot successfully created");
					}
				}
			}
		});
		
		createPop.addActionListener(new ActionListener() { //check if min valid pop
			public void actionPerformed(ActionEvent a) { 
				Node temp;
				try {
					temp = popNode.getChildren().get(0); //wave
					try {
						temp = temp.getChildren().get(0); //wavespawn
						try {
							temp = temp.getChildren().get(0); //tfbot
							getFile();
						}
						catch (IndexOutOfBoundsException i) {
							feedback.setText("Popfile generation failed, no bots to generate");
						}
					}
					catch (IndexOutOfBoundsException i) {
						feedback.setText("Popfile generation failed, no wavespawns to generate");
					}
				}
				catch (IndexOutOfBoundsException i) {
					feedback.setText("Popfile generation failed, no waves to generate");
				}				
			}
		});
		
		//todo: template list
		
		addGB(listPanel, gb, listCurrentWave, 0, 0);
		addGB(listPanel, gb, addWave, 0, 1);
		addGB(listPanel, gb, removeWave, 0, 2);
		
		addGB(listPanel, gb, listCurrentWSLabel, 0, 3);
		addGB(listPanel, gb, listAddWaveSpawn, 0, 4);
		addGB(listPanel, gb, listRemoveWaveSpawn, 0, 5);
		addGB(listPanel, gb, listUpdateWaveSpawn, 0, 6);
		
		addGB(listPanel, gb, addBot, 0, 7);
		addGB(listPanel, gb, createPop, 0, 9);
		
		gb.gridheight = 2;
		addGB(listPanel, gb, listWaveList, 1, 1);
		
		gb.gridheight = 3;
		addGB(listPanel, gb, waveSpawnList, 1, 4);		
	} 
	
	void getFile() { //get filename/place to save pop at
		JFileChooser c = new JFileChooser();
		int result = c.showSaveDialog(frame);
		//if(result == JFileChooser.CANCEL_OPTION) return;
		try { //double check
			File file = c.getSelectedFile();
			if(file.exists()) { //confirm overwrite
				int op = JOptionPane.showConfirmDialog(frame, "Overwrite this file?");
				if (op == JOptionPane.YES_OPTION) {
					TreeParse.parseTree(file, tree);
					feedback.setText("Popfile successfully generated!");
				} //kinda jank
			}
			else { //if it doesn't exist, no overwrite check needed
				TreeParse.parseTree(file, tree);
				feedback.setText("Popfile successfully generated!");
			}
		}
		catch(Exception e) {
			
		}
	}
	
	void getWaveList() { //since waves don't have real names, just approx by naming them wave 1, wave 2, etc
		int length = popNode.getChildren().size();
		
		waveListModel.clear();
		
		for(int i = 0; i < length; i++) {
			waveListModel.addElement("Wave " + Integer.toString(i + 1));
		}
	}
	
	void getWaveSpawnList() { //similar to the above, just gets actual names 
		int length = currentWaveNode.getChildren().size();

		waveSpawnListModel.clear();

		for(int i = 0; i < length; i++) {
			WaveSpawnNode t = (WaveSpawnNode) currentWaveNode.getChildren().get(i);
			waveSpawnListModel.addElement(t.getName()); //this is extremely awful
		}
	}
	 
	public void addGB(Container cont, GridBagConstraints gb, Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		cont.add(comp, gb);
	}
}
