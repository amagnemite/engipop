package engipop;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import engipop.Node.PopNode;

public class PopulationTreeModel implements TreeModel {
	private PopNode root;
	private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
	
	public PopulationTreeModel(PopNode root) {
		this.root = root;
	}

	protected void fireTreeStructureChanged(Object oldNode) {
		System.out.println("1");
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {oldNode});
		for(TreeModelListener l : treeModelListeners) {
			l.treeStructureChanged(e);
		}
	}
	
	protected void fireTreeNodesInserted(Object oldNode) {
		System.out.println("2");
		Node n = (Node) oldNode;
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {n.getParent()}, new int[] {getIndexOfChild(n.getParent(), oldNode)},
				new Object[] {n});
		for(TreeModelListener l : treeModelListeners) {
			l.treeNodesInserted(e);
		}
	}
	
	protected void fireTreeNodesRemoved(Object oldNode) {
		System.out.println("2");
		Node n = (Node) oldNode;
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {n.getParent()}, new int[] {getIndexOfChild(n.getParent(), oldNode)},
				new Object[] {n});
		for(TreeModelListener l : treeModelListeners) {
			l.treeNodesRemoved(e);
		}
	}
	
	protected void fireTreeNodesChanged(Object oldNode) {
		System.out.println("3");
		Node n = (Node) oldNode;
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {n.getParent()}, new int[] {getIndexOfChild(n.getParent(), oldNode)}, new Object[] {n});
		for(TreeModelListener l : treeModelListeners) {
			l.treeNodesChanged(e);
		}
	}
	
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		System.out.println(parent + " " + index);
		Node node = (Node) parent;
		return node.getChildren().get(index);
	}

	public int getChildCount(Object parent) {
		//used to make tree
		Node node = (Node) parent;
		return node.getChildren().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		Node node = (Node) parent;
		return node.getChildren().indexOf(child);
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		// TODO Auto-generated method stub
		Node n = (Node) node;
		if(n.isPlaceholder()) {
			return true;
		}
		return false;
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newVal) {
	}
	
	public static class PopulationTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			System.out.println("test");
		}

		public void treeNodesInserted(TreeModelEvent e) {
			//tell popnode whenever nodes are added/removed
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			System.out.println("test2");
		}

		public void treeStructureChanged(TreeModelEvent e) {
			System.out.println("test");
		}
	}
}
