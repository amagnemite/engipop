package engipop;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	private static final String REMOVEWS = "Remove wavespawn template";
	
	private static final String ADDBOT = "Add TFBot template";
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
	JButton removeTemplateButton = new JButton(REMOVEBOT);
	
	ButtonListManager templateBLManager = new ButtonListManager(addTemplateButton, removeTemplateButton);
	
	JLabel templateNameLabel = new JLabel("Template name: ");
	JTextField templateNameField = new JTextField("", 14);
	
	private int generated = 1; //attempt to make sure names are unique
	private boolean isTemplateListResetting = false;
	//TODO: needs to be cleaned up due to template handling changes
	
	public TemplatePanel(MainWindow mainWindow, PopulationPanel popPanel) {
		gbConstraints.anchor = GridBagConstraints.NORTHWEST;
		//setBackground(new Color(11, 97, 163));
		
		this.mainWindow = mainWindow;
		this.popPanel = popPanel;
		popPanel.addPropertyChangeListener("POPNODE", this);
		
		PopNode popNode = Engipop.getPopNode();
		wsTemplateMap = popNode.getWSTemplateMap();
    	botTemplateMap = popNode.getBotTemplateMap();
		
		wsPanel = new WaveSpawnPanel(popPanel, mainWindow);
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
		
		templateButtonPanel.gbConstraints.anchor = GridBagConstraints.WEST;
		templateButtonPanel.gbConstraints.insets = new Insets(0, 0, 5, 0);
		
		templateBLManager.changeButtonState(States.EMPTY);
		
		templateNameField.setMinimumSize(templateNameField.getPreferredSize());
		
		templateButtonPanel.addGB(templateNameLabel, 0, 0);
		templateButtonPanel.addGB(templateNameField, 1, 0);
		
		templateButtonPanel.gbConstraints.gridwidth = 2;
		templateButtonPanel.addGB(addTemplateButton, 0 , 2);
		templateButtonPanel.addGB(removeTemplateButton, 0 , 3);
		
		templateButtonPanel.gbConstraints.ipady = 1;
		templateButtonPanel.addGB(templateComboBox, 0, 1);
		
		addGB(modePanel, 0, 0);
		
		addGB(wsPanel.getDisabledPanel(), 0, 3);
		addGB(spawnerPanel, 0, 2);
		
		gbConstraints.weighty = 1;
		addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 1);
		
		gbConstraints.weighty = 0;
		gbConstraints.weightx = 1;
		gbConstraints.gridwidth = 1;
		addGB(templateButtonPanel, 2, 1);
		addGB(listPanel, 2, 3);
		gbConstraints.weightx = 0;
		
		templateNameField.setEnabled(false);
		initListeners();
		//force a load/hide ws cycle
		modeGroup.setSelected(wsModeButton.getModel(), true);
		modeGroup.setSelected(botModeButton.getModel(), true);
		wsPanel.getDisabledPanel().setEnabled(false);
		spawnerListManager.getBotTankPanel().getDisabledPanel().setEnabled(false);
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
			int op = JOptionPane.showConfirmDialog(this, "Overwrite the existing template?", "Select an Option",
					JOptionPane.YES_NO_OPTION);
			if(op == JOptionPane.YES_OPTION) {
				overwrite = true;
			}
		}
		else {
			overwrite = true;
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
	private void initListeners() {
		wsModeButton.addItemListener(event -> {
			wsPanel.getDisabledPanel().setVisible(wsModeButton.isSelected());
			spawnerPanel.setVisible(wsModeButton.isSelected());
			listPanel.setVisible(wsModeButton.isSelected());
			
			isTemplateListResetting = true;
			templateNameField.setText("");
			isTemplateListResetting = false;
			
			if(wsModeButton.isSelected()) {
				addTemplateButton.setText(ADDWS);
				removeTemplateButton.setText(REMOVEWS);
				
				updateTemplateComboBox(wsTemplateMap);

				gbConstraints.weighty = 1;
				addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 3);
				gbConstraints.weighty = 0;
				addGB(wsPanel.getDisabledPanel(), 0, 1); //this needs to be shuffled around to not cause visual issues
			}
			else { //if botmode
				addTemplateButton.setText(ADDBOT);
				removeTemplateButton.setText(REMOVEBOT);
				
				updateTemplateComboBox(botTemplateMap);
				
				gbConstraints.weighty = 1;
				addGB(spawnerListManager.getBotTankPanel().getDisabledPanel(), 0, 1);
				gbConstraints.weighty = 0;
				addGB(wsPanel.getDisabledPanel(), 0, 3);
				
				spawnerListManager.setSelectedButton(SpawnerType.TFBOT);
			}
		});
		
		templateComboBox.addActionListener(event -> {
			int index = templateComboBox.getSelectedIndex();
			
			spawnerListManager.setSelectedButton(SpawnerType.NONE);
			if(isTemplateListResetting) {
				return;
			}

			if(index != -1) {
				templateNameField.setEnabled(true);
				spawnerListManager.getBotTankPanel().getDisabledPanel().setEnabled(true);
				wsPanel.getDisabledPanel().setEnabled(true);
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
				
				isTemplateListResetting = true;
				templateNameField.setText((String) templateComboBox.getSelectedItem());
				isTemplateListResetting = false;
				templateBLManager.changeButtonState(States.SELECTED);
			}
			else { //if deselected manually or via some list reset
				//this includes removing elements from the model
				isTemplateListResetting = true;
				templateNameField.setText("");
				isTemplateListResetting = false;
				
				templateNameField.setEnabled(false);
				templateBLManager.changeButtonState(States.EMPTY);
				spawnerListManager.setButtonState(States.DISABLE);
				modeGroup.setSelected(botModeButton.getModel(), true);
				wsPanel.getDisabledPanel().setEnabled(false);
				spawnerListManager.getBotTankPanel().getDisabledPanel().setEnabled(false);
			}
		});
		
		//ignore panel, create fresh node
		addTemplateButton.addActionListener(event -> {
			boolean addNode = false;
			String type;
			Map<String, Node> map;
			Node currentNode = null;
			String name;
			
			if(wsModeButton.isSelected()) {		
				type = WaveNode.WAVESPAWN;
				map = wsTemplateMap;
			}
			else {
				type = WaveSpawnNode.TFBOT;
				map = botTemplateMap;
			}
			
			//if(templateNameField.getText().isEmpty()) {
				name = JOptionPane.showInputDialog("Enter a template name");
			//}
			//else {
				//name = templateNameField.getText();
			//}
			
			if(name != null && !name.isEmpty()) {
				if(checkTemplateName(name, type)) { //if doesn't exist or overwrite
					addNode = true;
				} //does nothing if user selects no
			}
			else { //generate a name if one isn't entered
				name = null;
				addNode = true;
			}
			
			if(addNode) {
				if(wsModeButton.isSelected()) { //do it down here in case user rejects
					currentNode = new WaveSpawnNode();
					spawnerListManager.setParentNode((WaveSpawnNode) currentNode);
				}
				else {
					currentNode = new TFBotNode();
				}
				if(name == null) {
					name = generateTemplateName(currentNode);
				}
				
				isTemplateListResetting = true;
				if(templateComboModel.getIndexOf(name) == -1) {
					templateComboModel.addElement(name);
				}
				isTemplateListResetting = false;
				
				map.put(name, currentNode);
				templateComboBox.setSelectedItem(name);
				popPanel.fireTemplateChange(type, null, currentNode);
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
			
			isTemplateListResetting = true;
			templateComboModel.removeElement(removed);
			isTemplateListResetting = false;
			templateComboBox.setSelectedIndex(templateComboModel.getSize() - 1);
			
			Node node = map.remove(removed);
			popPanel.fireTemplateChange(type, node, null);
		});
		
		//this only fires when the namefield actually updates
		templateNameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				if(isTemplateListResetting) {
					return;
				}
				isTemplateListResetting = true;
				boolean overwrite = false;
				String newName = templateNameField.getText();
				String oldName = (String) templateComboBox.getSelectedItem();
				String oldNodeName;
				String type;
				Map<String, Node> map;
				
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
					if(checkTemplateName(newName, type)) { 
						overwrite = true;
					} //otherwise do nothing
					
					if(overwrite) {
						map.remove(oldName); //make sure old name isn't floating around
						map.put(newName, currentBotNode);
						int index = templateComboModel.getIndexOf(oldName);
						templateComboModel.removeElementAt(index);
						templateComboModel.insertElementAt(newName, index);
						templateComboModel.setSelectedItem(newName);
						
						//TODO: may need to always send
						//figure out how to update now
						if(oldNodeName != currentBotNode.getValue(TFBotNode.NAME) || !oldName.equals(newName)) {
							popPanel.fireTemplateChange(type, currentBotNode, currentBotNode);
						}					
					}
				}
				else {
					mainWindow.setFeedback("Failed to update template; name must be not be blank");
				}
				isTemplateListResetting = false;
			}
		});
	}
	
	//returns last index
	private int updateTemplateComboBox(Map<String, Node> map) {
		isTemplateListResetting = true;
		templateComboModel.removeAllElements();
		
		if(map != null) {
			map.forEach((k, v) -> {
				templateComboModel.addElement(k);
			});
		}
		isTemplateListResetting = false;
		
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
