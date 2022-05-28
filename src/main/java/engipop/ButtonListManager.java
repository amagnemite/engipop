package engipop;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import engipop.Tree.Node;

//class for managing trios of add/update/remove buttons
//currently doesn't do anything with list
public class ButtonListManager {
	JList<String> list;
	JButton add;
	JButton update;
	JButton remove;
	
	public static enum States {
		NOSELECTION, SELECTED, EMPTY, REMOVEONLY, DISABLE;
	}

	public ButtonListManager(JList<String> list, JButton add, JButton update, JButton remove) {
		this.list = list;
		this.add = add;
		this.update = update;
		this.remove = remove;
		
		//initListeners();
	}
	/*
	private String initListeners() {
		String status = " ";
		
		list.addListSelectionListener(new ListSelectionListener() { //when a wave is selected from list
			 public void valueChanged(ListSelectionEvent l) {
				 if(list.getSelectedIndex() != -1) {
					 //set node at index of list
					 processSelection();
				 }
				 else {
					 changeButtonState(States.NOSELECTION);
				 }
			 }
		}); 
		
		return status;
	} */
	/*
	public void processSelection() {
		
	} */

	
	public void changeButtonState(States state) {
		switch (state) {
			case EMPTY:
				add.setEnabled(true);
				update.setEnabled(false);
				remove.setEnabled(false);
				break;
			case NOSELECTION: //potentially merge this with empty
				add.setEnabled(true);
				update.setEnabled(false);
				remove.setEnabled(true);
				break;
			case REMOVEONLY:
				add.setEnabled(false);
				update.setEnabled(false);
				remove.setEnabled(true);
				break;
			case SELECTED:
				add.setEnabled(false);
				update.setEnabled(true);
				remove.setEnabled(true);
				break;
			case DISABLE:
				add.setEnabled(false);
				update.setEnabled(false);
				remove.setEnabled(false);
				break;
			default:
				break;
		}
	}
	
	/*
	public class WaveButtonListManager extends ButtonListManager {

		public WaveButtonListManager(JList<String> list, JButton add, JButton update, JButton remove) {
			super(list, add, update, remove);
		}
		
		public void processSelection(Node current, Node parent) {
			//current = 
		}
	} */
}
