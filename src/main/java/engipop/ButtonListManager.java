package engipop;

import javax.swing.JButton;
import javax.swing.JList;

//class for managing trios of add/update/remove buttons
//currently doesn't do anything with list
public class ButtonListManager {
	JList<String> list;
	JButton add;
	JButton remove;
	
	public static enum States {
		NOSELECTION, SELECTED, EMPTY, REMOVEONLY, DISABLE, FILLEDSLOT;
	}

	public ButtonListManager(JButton add, JButton remove) {
		this.add = add;
		this.remove = remove;
	}
	
	public void changeButtonState(States state) {
		switch (state) {
			case EMPTY:
				add.setEnabled(true);
				remove.setEnabled(false);
				break;
			case SELECTED: //this should be renamed
				add.setEnabled(true);
				remove.setEnabled(true);
				break;	
			case FILLEDSLOT:
				add.setEnabled(false);
				remove.setEnabled(true);
				break;
			case DISABLE:
				add.setEnabled(false);
				remove.setEnabled(false);
				break;
			default:
				break;
		}
	}
}
