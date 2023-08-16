package engipop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import engipop.ButtonListManager.States;
import engipop.EngiPanel.Classes;
import engipop.Node.PopNode;
import engipop.Node.RandomChoiceNode;
import engipop.Node.SpawnerType;
import engipop.Node.SquadNode;
import engipop.Node.TFBotNode;
import engipop.Node.TankNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;
import engipop.PopulationParser.TemplateData;

//window to edit bot and ws templates
//similar structure to main window
@SuppressWarnings("serial")
public class TemplateWindow extends EngiWindow implements PropertyChangeListener {
	private static final String ADDWS = "Add wavespawn template";
	private static final String UPDATEWS = "Update wavespawn template";
	private static final String REMOVEWS = "Remove wavespawn template";
	
	private static final String ADDBOT = "Add TFBot template";
	private static final String UPDATEBOT = "Update TFBot template";
	private static final String REMOVEBOT = "Remove TFBot template";
	
	SecondaryWindow w2;
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	EngiPanel templateButtonPanel = new EngiPanel();
	TankPanel tankPanel;
	NodePanelManager spawnerListManager;
	JPanel listPanel;
	JPanel spawnerPanel;

	PopNode popNode;
	WaveSpawnNode currentWSNode = new WaveSpawnNode();
	TFBotNode currentBotNode = new TFBotNode();
	TankNode currentTankNode = new TankNode();
	SquadNode currentSquadNode = new SquadNode();
	RandomChoiceNode currentRCNode = new RandomChoiceNode();
	
	JRadioButton botModeButton = new JRadioButton("Bot");
	JRadioButton wsModeButton = new JRadioButton("WaveSpawn");
	
	JPanel modePanel = new JPanel();
	ButtonGroup modeGroup = new ButtonGroup();
	JLabel modeLabel = new JLabel("Template type: ");
	
	DefaultComboBoxModel<String> templateComboModel = new DefaultComboBoxModel<String>();
	JComboBox<String> templateComboBox = new JComboBox<String>(templateComboModel);
	
	//consider using shorter strings
	JButton addTemplateButton = new JButton(ADDBOT);
	JButton updateTemplateButton = new JButton(UPDATEBOT);
	JButton removeTemplateButton = new JButton(REMOVEBOT);
	
	ButtonListManager templateBLManager = new ButtonListManager(addTemplateButton,
			updateTemplateButton, removeTemplateButton);
	
	JLabel templateNameLabel = new JLabel("Template name: ");
	JTextField templateNameField = new JTextField("", 10);
	
	int generated = 1; //attempt to make sure names are unique
	
	//TODO: needs to be cleaned up due to template handling changes
	
	public TemplateWindow(MainWindow mainWindow, SecondaryWindow w2) {
		super("Template editor");
		setSize(1300, 800);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		this.setBackground(new Color(11, 97, 163));
		
		this.w2 = w2;
		w2.addPropertyChangeListener("POPNODE", this);
		
		feedback = new JLabel(" ");
		
		this.popNode = mainWindow.getPopNode();
		wsPanel = new WaveSpawnPanel(w2);
		botPanel = new BotPanel(this, mainWindow, w2);
		tankPanel = new TankPanel(w2);
		spawnerListManager = new NodePanelManager(this, botPanel, tankPanel);
		spawnerPanel = spawnerListManager.getSpawnerPanel();
		listPanel = spawnerListManager.getListPanel();
		
		tankPanel.setVisible(false);
		
		modePanel.add(modeLabel);
		modePanel.add(botModeButton);
		modePanel.add(wsModeButton);
		
		modeGroup.add(botModeButton);
		modeGroup.add(wsModeButton);
		
		spawnerListManager.setButtonState(States.DISABLE);
		
		templateComboBox.setMinimumSize(templateNameField.getPreferredSize());
		
		wsModeButton.addItemListener(event -> {
			if(wsModeButton.isSelected()) {
				addTemplateButton.setText(ADDWS);
				updateTemplateButton.setText(UPDATEWS);
				removeTemplateButton.setText(REMOVEWS);
				
				updateTemplateComboBox(popNode.getWSTemplateMap());
				
				wsPanel.setVisible(true);
				spawnerPanel.setVisible(true);
				listPanel.setVisible(true);
			}
			else { //if botmode
				addTemplateButton.setText(ADDBOT);
				updateTemplateButton.setText(UPDATEBOT);
				removeTemplateButton.setText(REMOVEBOT);
				
				updateTemplateComboBox(popNode.getBotTemplateMap());
				
				wsPanel.setVisible(false);
				spawnerPanel.setVisible(false);
				listPanel.setVisible(false);
				spawnerListManager.setSelectedButton(SpawnerType.TFBOT);
			}
		});
		
		initTemplateButtonPanel();
		
		//force a load/hide ws cycle
		modeGroup.setSelected(wsModeButton.getModel(), true);
		modeGroup.setSelected(botModeButton.getModel(), true);
		
		addGB(feedback, 0, 0);
		addGB(modePanel, 0, 1);
		
		gbConstraints.gridwidth = 2;
		addGB(wsPanel, 0, 2);
		addGB(spawnerPanel, 0, 3);
		addGB(botPanel, 0, 4);
		addGB(tankPanel, 0, 4);
		
		//addGB(botTemplatePanel, 1, 4);
		gbConstraints.gridwidth = 1;
		gbConstraints.gridheight = 2;
		addGB(templateButtonPanel, 2, 1);
		addGB(listPanel, 2, 4);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private void initTemplateButtonPanel() {
		templateButtonPanel.setLayout(gbLayout);
		templateButtonPanel.gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		templateButtonPanel.gbConstraints.insets = new Insets(0, 0, 5, 5);
		
		templateBLManager.changeButtonState(States.EMPTY);
		
		//updateWSTemplate.setToolTipText("The currently loaded spawner will also be included in the template");
		
		templateNameField.setMinimumSize(templateNameField.getPreferredSize());
		
		initTemplateListeners();
		
		templateButtonPanel.addGB(templateNameLabel, 0, 0);
		templateButtonPanel.addGB(templateNameField, 1, 0);
		templateButtonPanel.addGB(addTemplateButton, 0 , 1);
		templateButtonPanel.addGB(updateTemplateButton, 0 , 2);
		templateButtonPanel.addGB(removeTemplateButton, 0 , 3);
		
		templateButtonPanel.gbConstraints.gridheight = 3;
		templateButtonPanel.addGB(templateComboBox, 1, 1);
	}
	
	//check if map contains the entered template name already
	protected boolean checkTemplateName(String newName, String type) {
		boolean overwrite = false;
		
		Set<String> keyset;
		
		if(type == WaveSpawnNode.TFBOT) {
			keyset = popNode.getBotTemplateMap().keySet();
		}
		else { //ws
			keyset = popNode.getWSTemplateMap().keySet();
		}
		
		if(keyset.contains(newName)) { //todo: fix 
			int op = JOptionPane.showConfirmDialog(this, "Overwrite the existing template?");
			if(op == JOptionPane.YES_OPTION) {
				overwrite = true;
			}
		}
		return overwrite;
	}
	
	//generate a template name if user doesn't pick one
	protected String generateTemplateName(Node node) {
		String header = "T_";
		String type;
		String body = "";
		String full;
		
		Set<String> keyset;
		
		if(node.getClass() == WaveSpawnNode.class) { //returns format of T_WaveSpawn_[wavespawn name]_[spawner]
			type = "WaveSpawn_";
			WaveSpawnNode wsNode = (WaveSpawnNode) node; //redundant but code clarity
			
			if(wsNode.getMap().containsKey(WaveSpawnNode.NAME)) { //this currently doesn't get used
				body = (String) wsNode.getValue(WaveSpawnNode.NAME);
			}
			
			if(wsNode.hasChildren()) { //same here
				body = body + "_" + wsNode.getSpawnerType();
			}
			keyset = popNode.getWSTemplateMap().keySet();
		}
		else { //returns format of T_TFBot_[bot class]_[bot name]
			type = "TFBot_";
			TFBotNode botNode = (TFBotNode) node;
			
			body = botNode.getValue(TFBotNode.CLASSNAME).toString();
			
			if((String) botNode.getValue(TFBotNode.NAME) != null) {
				body = body + "_" + (String) botNode.getValue(TFBotNode.NAME) + "_";
			}
			keyset = popNode.getBotTemplateMap().keySet();
		}
		full = header + type + body;
		
		if(keyset.contains(full)) {
			boolean invalidName = true;
			
			while (invalidName) { //this sucks
				String suffix = Integer.toString(generated);
				
				if(keyset.contains(full + suffix)) {
					generated++;
				}
				else {
					full = full + suffix;
					invalidName = false;
				}
			}
		}
		return full;
	}

	public void setListIndex(int index) {
		templateComboBox.setSelectedIndex(index);
	}
	
	//ws and bot counterparts do essentially the same thing
	private void initTemplateListeners() {
		templateComboBox.addActionListener(event -> {
			int index = templateComboBox.getSelectedIndex();
			
			spawnerListManager.setSelectedButton(SpawnerType.NONE);
			
			if(index != -1) {
				if(wsModeButton.isSelected()) {
					//TODO: update to match current map setup
					currentWSNode = (WaveSpawnNode) popNode.getWSTemplateMap().get(templateComboBox.getSelectedItem());
					wsPanel.updatePanel(currentWSNode);
					
					if(currentWSNode.hasChildren()) {
						spawnerListManager.checkSpawner(currentWSNode.getSpawner());
						spawnerListManager.setButtonState(States.FILLEDSLOT);
					}
					else {
						spawnerListManager.setSelectedButton(SpawnerType.TFBOT);
						spawnerListManager.setButtonState(States.EMPTY);
					}
				}
				else {
					currentBotNode = (TFBotNode) popNode.getBotTemplateMap().get(templateComboBox.getSelectedItem());
					botPanel.updatePanel(currentBotNode);
				}
				templateNameField.setText((String) templateComboBox.getSelectedItem());
				templateBLManager.changeButtonState(States.SELECTED);
			}
			else { //if deselected manually or via some list reset
				templateNameField.setText("");
				templateBLManager.changeButtonState(States.EMPTY);
				spawnerListManager.setButtonState(States.DISABLE);
			}
		});
		
		//ignore panel, create fresh node
		addTemplateButton.addActionListener(event -> {
			boolean addNode = false;
			feedback.setText("");
			String type;
			Map<String, Node> map;
			Node currentNode;
			String name;
			TemplateData data;
			
			if(wsModeButton.isSelected()) {		
				type = WaveNode.WAVESPAWN;
				
				if(popNode.getWSTemplateMap() == null) {
					popNode.setWSTemplateMap(new TreeMap<String, Node>()); //forces no nulls and sorts
				}
				map = popNode.getWSTemplateMap();
				
				currentNode = new WaveSpawnNode();
				wsPanel.updateNode((WaveSpawnNode) currentNode);
				spawnerListManager.setWSNode((WaveSpawnNode) currentNode);
			}
			else {
				type = WaveSpawnNode.TFBOT;
				
				if(popNode.getBotTemplateMap() == null) {
					popNode.setBotTemplateMap(new TreeMap<String, Node>()); //forces no nulls and sorts
				}
				map = popNode.getBotTemplateMap();
				
				currentNode = new TFBotNode();
				botPanel.updateNode((TFBotNode) currentNode);
			}			
			
			if(templateNameField.getText().isEmpty()) {
				name = JOptionPane.showInputDialog("Enter a template name");
			}
			else {
				name = templateNameField.getText();
			}
			
			if(name != null && !name.isEmpty()) {
				if(map.containsKey(name)) { //overwrite check
					if(checkTemplateName(templateNameField.getText(), type)) {  //possibly unnecessary check
						addNode = true;
					} //does nothing if user selects no
				}
				else { //otherwise just shove it in
					addNode = true;
				}
			}
			else { //generate a name if one isn't entered
				name = generateTemplateName(currentNode);
				addNode = true;
			}
			
			if(addNode) {
				map.put(name, currentNode);
				templateComboModel.addElement(name);
				templateComboBox.setSelectedItem(name);
				
				if(type.equals(WaveSpawnNode.TFBOT)) {
					data = new TemplateData(name, (Classes) currentNode.getValue(TFBotNode.CLASSNAME), 
							(String) currentNode.getValue(TFBotNode.NAME), WaveSpawnNode.TFBOT);
					
				}
				else {
					data = new TemplateData(name, (String) currentNode.getValue(TFBotNode.NAME), WaveNode.WAVESPAWN);
				}
				
				w2.fireTemplateChange(type, null, data);
			}	
		});
		
		//update currently selected template
		updateTemplateButton.addActionListener(event -> {
			boolean overwrite = false;
			boolean sameName = false;
			String newName = templateNameField.getText();
			String oldName = (String) templateComboBox.getSelectedItem();
			String oldNodeName;
			String type;
			Map<String, Node> map;
			Node currentNode;
			
			//need to update and be able to remove nodes
			//might need fresh nodes here
			if(wsModeButton.isSelected()) {
				type = WaveNode.WAVESPAWN;
				map = popNode.getWSTemplateMap();
			}
			else {
				type = WaveSpawnNode.TFBOT;
				map = popNode.getBotTemplateMap();
			}
			
			oldNodeName = (String) map.get(oldName).getValue(TFBotNode.NAME);
			
			if(!newName.isEmpty()) {
				if(!newName.equals(oldName)) { //if user updated template name
					if(map.containsKey(newName)) { //named template with a name already in map
						if(checkTemplateName(newName, type)) { 
							overwrite = true;
						} //otherwise do nothing
					}
					else {
						overwrite = true;
					}
				}
				else { //otherwise just shove in
					sameName = true;
				}
				
				if(sameName || overwrite) {
					TemplateData oldData;
					TemplateData newData;
					
					
					if(overwrite) {
						map.remove(oldName); //make sure old name isn't floating around
					}
					if(type.equals(WaveSpawnNode.TFBOT)) {
						oldData = new TemplateData(oldName, (Classes) currentBotNode.getValue(TFBotNode.CLASSNAME), 
								(String) currentBotNode.getValue(TFBotNode.NAME), WaveSpawnNode.TFBOT);
						
						botPanel.updateNode(currentBotNode);
						currentNode = currentBotNode;
						
						newData = new TemplateData(newName, (Classes) currentBotNode.getValue(TFBotNode.CLASSNAME), 
								(String) currentBotNode.getValue(TFBotNode.NAME), WaveSpawnNode.TFBOT);
					}
					else {
						oldData = new TemplateData(oldName, (String) currentWSNode.getValue(TFBotNode.NAME), WaveNode.WAVESPAWN);
						
						wsPanel.updateNode(currentWSNode);
						currentNode = currentWSNode;
						
						newData = new TemplateData(newName, (String) currentWSNode.getValue(TFBotNode.NAME), WaveNode.WAVESPAWN);
					}
					
					map.put(newName, currentNode);
					int index = templateComboModel.getIndexOf(oldName);
					templateComboModel.removeElementAt(index);
					templateComboModel.insertElementAt(newName, index);
					
					templateComboModel.setSelectedItem(newName);
					feedback.setText("Template successfully updated");
					
					if(oldNodeName != currentNode.getValue(TFBotNode.NAME) || !oldName.equals(newName)) {
						//bot/ws's actual name or templatename was updated
						//String oldValue = oldNodeName != null ? oldNodeName + " (" + oldName + ")" : "(" + oldName + ")";
						//String newValue = currentNode.getValue(TFBotNode.NAME) != null ? 
						//		currentNode.getValue(TFBotNode.NAME) + " (" + newName + ")" : "(" + newName + ")";
						
						w2.fireTemplateChange(type, oldData, newData);
					}					
				}
			}
			else {
				feedback.setText("Failed to update template; name must be not be blank");
			}
		});
		
		removeTemplateButton.addActionListener(event -> {
			String removed = (String) templateComboBox.getSelectedItem();
			Map<String, Node> map;
			String type;
			String nodeName;
			TemplateData data;
			
			if(wsModeButton.isSelected()) {
				map = popNode.getWSTemplateMap();
				type = WaveNode.WAVESPAWN;
				nodeName = (String) currentWSNode.getValue(TFBotNode.NAME);
				
				data = new TemplateData(removed, (String) currentBotNode.getValue(TFBotNode.NAME), WaveNode.WAVESPAWN);
			}
			else {
				map = popNode.getBotTemplateMap();
				type = WaveSpawnNode.TFBOT;
				nodeName = (String) currentBotNode.getValue(TFBotNode.NAME);
				
				data = new TemplateData(removed, (Classes) currentBotNode.getValue(TFBotNode.CLASSNAME), 
						(String) currentBotNode.getValue(TFBotNode.NAME), WaveSpawnNode.TFBOT);
			}
			
			//String oldValue = nodeName != null ? nodeName + " (" + removed + ")" : "(" + removed + ")";
			
			templateComboModel.removeElement(removed);
			map.remove(removed);
			
			w2.fireTemplateChange(type, data, null);
		});
	}
	
	//returns last index
	private int updateTemplateComboBox(Map<String, Node> map) {
		templateComboModel.removeAllElements();
		
		if(map != null) {
			map.forEach((k, v) -> {
				templateComboModel.addElement(k);
			});
		}
		
		return templateComboModel.getSize() - 1;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		this.popNode = (PopNode) evt.getNewValue();
		updateTemplateComboBox(popNode.getBotTemplateMap());
		//force a reload
	}
}
