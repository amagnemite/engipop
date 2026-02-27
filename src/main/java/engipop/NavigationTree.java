package engipop;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import engipop.Node.*;

@SuppressWarnings("serial")
public class NavigationTree extends JTree {
	private PopulationTreeModel model;
	private MainWindow mainWindow;
	
	//private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
	
	public NavigationTree(MainWindow mainWindow) {
		this.mainWindow = mainWindow;

		model = new PopulationTreeModel(Engipop.getPopNode());
		setModel(model);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		
		initListeners();
	}
	
	public void initListeners() {
		addTreeSelectionListener(event -> {
			Object selectedObject = getLastSelectedPathComponent();
			if(selectedObject == null) {
				return;
			}
			Node node = (Node) selectedObject;
			
			if(!node.isPlaceholder() && !node.isDisplayOnly()) {
				mainWindow.loadNode(node);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 2) {
						Object selectedObject = getLastSelectedPathComponent();
						Node node = (Node) selectedObject;
						int index = model.getIndexOfChild(node.getParent(), node);
						//generate new node to replace this one
						
					}
				}
			}
		});
	}
	
	/*
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propChangeSupport.addPropertyChangeListener(listener);
    }
	
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	propChangeSupport.removePropertyChangeListener(listener);
    }
    */
}