package engipop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import engipop.Node.PopNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;
import engipop.PopulationParser.TemplateData;

//handles the template jtree / communicates with the nodepanelmanager
public class TemplateTree implements PropertyChangeListener {
	private DefaultMutableTreeNode templateRoot = new DefaultMutableTreeNode("Templates");
	private DefaultTreeModel model = new DefaultTreeModel(templateRoot);
	private JTree tree = new JTree(model);
	
	private DefaultMutableTreeNode botNode = new DefaultMutableTreeNode("TFBot templates");
	private DefaultMutableTreeNode wsNode = new DefaultMutableTreeNode("WaveSpawn templates");
	//private DefaultMutableTreeNode otherRoot = new DefaultMutableTreeNode();
	
	private DefaultMutableTreeNode internalBotNode = new DefaultMutableTreeNode("Created TFBot templates");
	private DefaultMutableTreeNode includedBotNode = new DefaultMutableTreeNode("Included TFBot templates");
	private DefaultMutableTreeNode importedBotNode = new DefaultMutableTreeNode("Imported TFBot templates");
	
	private DefaultMutableTreeNode noClassNode = new DefaultMutableTreeNode("No class");
	private DefaultMutableTreeNode scoutNode = new DefaultMutableTreeNode("Scout");
	private DefaultMutableTreeNode soldierNode = new DefaultMutableTreeNode("Soldier");
	private DefaultMutableTreeNode pyroNode = new DefaultMutableTreeNode("Pyro");
	private DefaultMutableTreeNode demoNode = new DefaultMutableTreeNode("Demoman");
	private DefaultMutableTreeNode heavyNode = new DefaultMutableTreeNode("HeavyWeapons");
	private DefaultMutableTreeNode engieNode = new DefaultMutableTreeNode("Engineer");
	private DefaultMutableTreeNode medicNode = new DefaultMutableTreeNode("Medic");
	private DefaultMutableTreeNode sniperNode = new DefaultMutableTreeNode("Sniper");
	private DefaultMutableTreeNode spyNode = new DefaultMutableTreeNode("Spy");
	
	private DefaultMutableTreeNode internalWSNode = new DefaultMutableTreeNode("Created WaveSpawn templates");
	private DefaultMutableTreeNode includedWSNode = new DefaultMutableTreeNode("Included WaveSpawn templates");
	private DefaultMutableTreeNode importedWSNode = new DefaultMutableTreeNode("Imported WaveSpawn templates");
		
	public TemplateTree(SecondaryWindow secWin) {
		secWin.addPropertyChangeListener(this);
		
		templateRoot.add(botNode);
		templateRoot.add(wsNode);
		
		botNode.add(internalBotNode);
		botNode.add(includedBotNode);
		botNode.add(importedBotNode);
		
		internalBotNode.add(noClassNode);
		internalBotNode.add(scoutNode);
		internalBotNode.add(soldierNode);
		internalBotNode.add(pyroNode);
		internalBotNode.add(demoNode);
		internalBotNode.add(heavyNode);
		internalBotNode.add(engieNode);
		internalBotNode.add(medicNode);
		internalBotNode.add(sniperNode);
		internalBotNode.add(spyNode);
		
		wsNode.add(internalWSNode);
		wsNode.add(includedWSNode);
		wsNode.add(importedWSNode);
	}
	
	public JScrollPane getTreePane() {
		return new JScrollPane(tree);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		switch(evt.getPropertyName()) {
			case SecondaryWindow.INCLUDED:
				addNonInternalTemplates((Map<String, List<TemplateData>>) evt.getNewValue(), SecondaryWindow.INCLUDED);
				break;
			case WaveSpawnNode.TFBOT:
				updateInternalTemplate((TemplateData) evt.getOldValue(), (TemplateData) evt.getNewValue());
				break;
			case WaveNode.WAVESPAWN:
				
				break;
			case SecondaryWindow.IMPORTED:
				addNonInternalTemplates((Map<String, List<TemplateData>>) evt.getNewValue(), SecondaryWindow.IMPORTED);
				break;
			default:
				break;
		}
	}
	
	private void updateInternalTemplate(TemplateData oldData, TemplateData newData) {
		
		if(oldData == null) { //add to list
			((DefaultMutableTreeNode) internalBotNode.getChildAt(newData.getClassSlot())).add(new DefaultMutableTreeNode(newData));
		}
		else if(newData == null) {
			//if the template was removed, loop through its class node and remove it
			for(Enumeration<? extends TreeNode> e = internalBotNode.getChildAt(oldData.getClassSlot()).children(); e.hasMoreElements();) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
				
				if(node.getUserObject().equals(oldData)) {
					internalBotNode.remove(node);
					break;
				}
			}
		}
		else {
			
		}
	}
	
	private void addNonInternalTemplates(Map<String, List<TemplateData>> map, String category) {
		DefaultMutableTreeNode parentBotNode;
		DefaultMutableTreeNode parentWSNode;
		
		if(category.equals(SecondaryWindow.INCLUDED)) {
			parentBotNode = includedBotNode;
			parentWSNode = includedWSNode;
		}
		else {
			parentBotNode = importedBotNode;
			parentWSNode = importedWSNode;
		}
		
		for(Entry<String, List<TemplateData>> entry : map.entrySet()) {
			DefaultMutableTreeNode newBotNode = new DefaultMutableTreeNode(entry.getKey());
			DefaultMutableTreeNode newWSNode = new DefaultMutableTreeNode(entry.getKey());
			
			parentBotNode.add(newBotNode);
			parentWSNode.add(newWSNode);
			
			DefaultMutableTreeNode newNoClassNode = new DefaultMutableTreeNode("No class");
			DefaultMutableTreeNode newScoutNode = new DefaultMutableTreeNode("Scout");
			DefaultMutableTreeNode newSoldierNode = new DefaultMutableTreeNode("Soldier");
			DefaultMutableTreeNode newPyroNode = new DefaultMutableTreeNode("Pyro");
			DefaultMutableTreeNode newDemoNode = new DefaultMutableTreeNode("Demoman");
			DefaultMutableTreeNode newHeavyNode = new DefaultMutableTreeNode("HeavyWeapons");
			DefaultMutableTreeNode newEngieNode = new DefaultMutableTreeNode("Engineer");
			DefaultMutableTreeNode newMedicNode = new DefaultMutableTreeNode("Medic");
			DefaultMutableTreeNode newSniperNode = new DefaultMutableTreeNode("Sniper");
			DefaultMutableTreeNode newSpyNode = new DefaultMutableTreeNode("Spy");
			
			newBotNode.add(newNoClassNode);
			newBotNode.add(newScoutNode);
			newBotNode.add(newSoldierNode);
			newBotNode.add(newPyroNode);
			newBotNode.add(newDemoNode);
			newBotNode.add(newHeavyNode);
			newBotNode.add(newEngieNode);
			newBotNode.add(newMedicNode);
			newBotNode.add(newSniperNode);
			newBotNode.add(newSpyNode);
			
			for(TemplateData data : entry.getValue()) {
				if(data.getType().equals(WaveSpawnNode.TFBOT)) {
					((DefaultMutableTreeNode) newBotNode.getChildAt(data.getClassSlot())).add(new DefaultMutableTreeNode(data));
					
					/*
					switch(data.getTFClass()) {
						case Scout:
							newScoutNode.add(new DefaultMutableTreeNode(name));
							break;
						case Soldier:
							newSoldierNode.add(new DefaultMutableTreeNode(name));
							break;
						case Pyro:
							newPyroNode.add(new DefaultMutableTreeNode(name));
							break;
						case Demoman:
							newDemoNode.add(new DefaultMutableTreeNode(name));
							break;
						case Heavyweapons:
							newHeavyNode.add(new DefaultMutableTreeNode(name));
							break;
						case Engineer:
							newEngieNode.add(new DefaultMutableTreeNode(name));
							break;
						case Medic:
							newMedicNode.add(new DefaultMutableTreeNode(name));
							break;
						case Sniper:
							newSniperNode.add(new DefaultMutableTreeNode(name));
							break;
						case Spy:
							newSpyNode.add(new DefaultMutableTreeNode(name));
							break;
						case None:
						default:
							newNoClassNode.add(new DefaultMutableTreeNode(name));
							break;
					}*/
				}
				else if(data.getType().equals(WaveNode.WAVESPAWN)) {
					newWSNode.add(new DefaultMutableTreeNode(data));
				}
			} //end of inner for
			//clean up empty nodes
			//if(newBotNode.getChildCount() == 0) {
				//includedBotNode.remove(newBotNode);
			//}
			if(newWSNode.getChildCount() == 0) {
				parentWSNode.remove(newWSNode);
			}
		}
	}
}
