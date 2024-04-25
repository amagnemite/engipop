package engipop;

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
import engipop.Node.PopNode;
import engipop.Node.RandomChoiceNode;
import engipop.Node.SpawnerType;
import engipop.Node.SquadNode;
import engipop.Node.TFBotNode;
import engipop.Node.TankNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;

//window to edit bot and ws templates
//similar structure to main window
@SuppressWarnings("serial")
public class TemplatePanel extends EngiPanel implements PropertyChangeListener {
	private static final String ADDWS = "Add wavespawn template";
	private static final String UPDATEWS = "Update wavespawn template";
	private static final String REMOVEWS = "Remove wavespawn template";
	
	private static final String ADDBOT = "Add TFBot template";
	private static final String UPDATEBOT = "Update TFBot template";
	private static final String REMOVEBOT = "Remove TFBot template";
	
	MainWindow mainWindow;
	PopulationPanel popPanel;
	WaveSpawnPanel wsPanel;
	BotPanel botPanel;
	EngiPanel templateButtonPanel = new EngiPanel();
	NodePanelManager spawnerListManager;
	JPanel listPanel;
	JPanel spawnerPanel;

	private Map<String, Node> wsTemplateMap;
	private Map<String, Node> botTemplateMap;
	
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
	
	public TemplatePanel(MainWindow mainWindow, PopulationPanel popPanel) {
		setLayout(gbLayout);
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		//setBackground(new Color(11, 97, 163));
		
		this.mainWindow = mainWindow;
		this.popPanel = popPanel;
		popPanel.addPropertyChangeListener("POPNODE", this);
		
		PopNode popNode = Engipop.getPopNode();
		this.wsTemplateMap = popNode.getWSTemplateMap();
    	this.botTemplateMap = popNode.getBotTemplateMap();
		
		wsPanel = new WaveSpawnPanel(popPanel);
		spawnerListManager = new NodePanelManager(mainWindow, popPanel, null);
		botPanel = spawnerListManager.getBotPanel();
		spawnerPanel = spawnerListManager.getSpawnerPanel();
		listPanel = spawnerListManager.getListPanel();
		
		modePanel.add(modeLabel);
		modePanel.add(botModeButton);
		modePanel.add(wsModeButton);
		
		modeGroup.add(botModeButton);
		modeGroup.add(wsModeButton);
		
		spawnerListManager.setButtonState(States.DISABLE);
		
		templateComboBox.setPrototypeDisplayValue("T_TFBot_Giant_Demo_Spammer_Reload_Chief");
		templateComboBox.setMinimumSize(templateNameField.getPreferredSize());
		
		wsModeButton.addItemListener(event -> {
			wsPanel.setVisible(wsModeButton.isSelected());
			spawnerPanel.setVisible(wsModeButton.isSelected());
			listPanel.setVisible(wsModeButton.isSelected());
			
			if(wsModeButton.isSelected()) {
				addTemplateButton.setText(ADDWS);
				updateTemplateButton.setText(UPDATEWS);
				removeTemplateButton.setText(REMOVEWS);
				
				updateTemplateComboBox(popNode.getWSTemplateMap());
					
				gbConstraints.weighty = 1;
				addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 3);
				
				gbConstraints.weighty = 0;
			}
			else { //if botmode
				addTemplateButton.setText(ADDBOT);
				updateTemplateButton.setText(UPDATEBOT);
				removeTemplateButton.setText(REMOVEBOT);
				
				updateTemplateComboBox(popNode.getBotTemplateMap());
				
				gbConstraints.weighty = 1;
				addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 1);
				
				gbConstraints.weighty = 0;
				
				spawnerListManager.setSelectedButton(SpawnerType.TFBOT);
			}
		});
		
		initTemplateButtonPanel();
		
		//force a load/hide ws cycle
		modeGroup.setSelected(wsModeButton.getModel(), true);
		modeGroup.setSelected(botModeButton.getModel(), true);
		
		addGB(modePanel, 0, 0);
		
		addGB(wsPanel, 0, 1);
		addGB(spawnerPanel, 0, 2);
		
		gbConstraints.weighty = 1;
		addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 1);
		
		gbConstraints.weighty = 0;
		gbConstraints.gridwidth = 1;
		addGB(templateButtonPanel, 2, 1);
		addGB(listPanel, 2, 3);
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
		templateButtonPanel.addGB(addTemplateButton, 0 , 2);
		templateButtonPanel.addGB(updateTemplateButton, 0 , 3);
		templateButtonPanel.addGB(removeTemplateButton, 0 , 4);
		
		//templateButtonPanel.gbConstraints.gridheight = 3;
		templateButtonPanel.addGB(templateComboBox, 0, 1);
	}
	
	//check if map contains the entered template name already
	protected boolean checkTemplateName(String newName, String type) {
		boolean overwrite = false;
		
		Set<String> keyset;
		
		if(type == WaveSpawnNode.TFBOT) {
			keyset = botTemplateMap.keySet();
		}
		else { //ws
			keyset = wsTemplateMap.keySet();
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
			keyset = wsTemplateMap.keySet();
		}
		else { //returns format of T_TFBot_[bot class]_[bot name]
			type = "TFBot_";
			TFBotNode botNode = (TFBotNode) node;
			
			body = botNode.getValue(TFBotNode.CLASSNAME).toString(); 
			
			if((String) botNode.getValue(TFBotNode.NAME) != null) {
				body = body + "_" + (String) botNode.getValue(TFBotNode.NAME) + "_";
			}
			keyset = botTemplateMap.keySet();
		}
		full = header + type + body;
		
		if(keyset.contains(full)) {
			boolean invalidName = true;
			
			while(invalidName) { //this sucks
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
					currentWSNode = (WaveSpawnNode) wsTemplateMap.get(templateComboBox.getSelectedItem());
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
					currentBotNode = (TFBotNode) botTemplateMap.get(templateComboBox.getSelectedItem());
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
			String type;
			Map<String, Node> map;
			Node currentNode;
			String name;
			
			if(wsModeButton.isSelected()) {		
				type = WaveNode.WAVESPAWN;
				
				map = wsTemplateMap;
				
				currentNode = new WaveSpawnNode();
				//wsPanel.updateNode((WaveSpawnNode) currentNode);
				spawnerListManager.setParentNode((WaveSpawnNode) currentNode);
			}
			else {
				type = WaveSpawnNode.TFBOT;
				
				map = botTemplateMap;
				
				currentNode = new TFBotNode();
				//botPanel.updateNode((TFBotNode) currentNode);
			}			
			
			//if(templateNameField.getText().isEmpty()) {
				name = JOptionPane.showInputDialog("Enter a template name");
			//}
			//else {
				//name = templateNameField.getText();
			//}
			
			if(name != null && !name.isEmpty()) {
				if(checkTemplateName(name, type)) {
					addNode = true;
				} //does nothing if user selects no
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
				
				popPanel.fireTemplateChange(type, null, currentNode);
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
			Node oldNode = null;
			
			//need to update and be able to remove nodes
			//might need fresh nodes here
			if(wsModeButton.isSelected()) {
				type = WaveNode.WAVESPAWN;
				map = wsTemplateMap;
			}
			else {
				type = WaveSpawnNode.TFBOT;
				map = botTemplateMap;
			}
			
			oldNodeName = (String) map.get(oldName).getValue(TFBotNode.NAME);
			
			if(!newName.isEmpty()) {
				if(!newName.equals(oldName)) { //if user updated template name
					if(checkTemplateName(newName, type)) { 
						overwrite = true;
					} //otherwise do nothing
				}
				else { //otherwise just shove in
					sameName = true;
				}
				
				if(sameName || overwrite) {
					if(overwrite) {
						oldNode = map.remove(oldName); //make sure old name isn't floating around
					}
					
					if(type.equals(WaveSpawnNode.TFBOT)) {
						if(!overwrite) {
							oldNode = currentBotNode;
							currentBotNode = new TFBotNode();
						}
						
						botPanel.updateNode(currentBotNode);
						currentNode = currentBotNode;
					}
					else {
						if(!overwrite) {
							oldNode = currentWSNode;
							currentWSNode = new WaveSpawnNode();
						}
						
						wsPanel.updateNode(currentWSNode);
						currentNode = currentWSNode;
					}
					
					map.put(newName, currentNode);
					int index = templateComboModel.getIndexOf(oldName);
					templateComboModel.removeElementAt(index);
					templateComboModel.insertElementAt(newName, index);
					
					templateComboModel.setSelectedItem(newName);
					mainWindow.setFeedback("Template successfully updated");
					
					//TODO: may need to always send
					if(oldNodeName != currentNode.getValue(TFBotNode.NAME) || !oldName.equals(newName)) {
						popPanel.fireTemplateChange(type, oldNode, currentNode);
					}					
				}
			}
			else {
				mainWindow.setFeedback("Failed to update template; name must be not be blank");
			}
		});
		
		removeTemplateButton.addActionListener(event -> {
			String removed = (String) templateComboBox.getSelectedItem();
			Map<String, Node> map;
			String type;
			String nodeName;
			
			if(wsModeButton.isSelected()) {
				map = wsTemplateMap;
				type = WaveNode.WAVESPAWN;
				nodeName = (String) currentWSNode.getValue(TFBotNode.NAME);
			}
			else {
				map = botTemplateMap;
				type = WaveSpawnNode.TFBOT;
				nodeName = (String) currentBotNode.getValue(TFBotNode.NAME);
			}
			
			//String oldValue = nodeName != null ? nodeName + " (" + removed + ")" : "(" + removed + ")";
			
			templateComboModel.removeElement(removed);
			Node node = map.remove(removed);
			
			popPanel.fireTemplateChange(type, node, null);
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
		PopNode popNode = Engipop.getPopNode();
		botTemplateMap = popNode.getBotTemplateMap();
		wsTemplateMap = popNode.getWSTemplateMap();
		updateTemplateComboBox(botTemplateMap);
		//force a reload
	}
}
