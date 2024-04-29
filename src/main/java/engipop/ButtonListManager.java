package engipop;

import javax.swing.JButton;
import javax.swing.JList;

//class for managing trios of add/update/remove buttons
//currently doesn't do anything with list
public class ButtonListManager {
	JList<String> list;
	JButton add;
	//JButton update;
	JButton remove;
	
	public static enum States {
		NOSELECTION, SELECTED, EMPTY, REMOVEONLY, DISABLE, FILLEDSLOT;
	}

	public ButtonListManager(JButton add, JButton remove) {
		this.add = add;
		//this.update = update;
		this.remove = remove;
	}
	
	public void changeButtonState(States state) {
		switch (state) {
			case EMPTY:
				add.setEnabled(true);
				//update.setEnabled(false);
				remove.setEnabled(false);
				break;
			/*
			case NOSELECTION: //potentially merge this with empty
				add.setEnabled(true);
				//update.setEnabled(false);
				remove.setEnabled(true);
				break;
			case REMOVEONLY:
				add.setEnabled(false);
				//update.setEnabled(false);
				remove.setEnabled(true);
				break;
			*/
			case SELECTED:
				add.setEnabled(true);
				//update.setEnabled(true);
				remove.setEnabled(true);
				break;
			
			case FILLEDSLOT:
				add.setEnabled(false);
				//update.setEnabled(true);
				remove.setEnabled(true);
				break;
			case DISABLE:
				add.setEnabled(false);
				//update.setEnabled(false);
				remove.setEnabled(false);
				break;
			default:
				break;
		}
	}
}
