package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import disabledpanel.DisabledPanel;

@SuppressWarnings("serial")
public class EngiPanel extends JPanel {
	protected GridBagLayout gbLayout = new GridBagLayout();
	protected GridBagConstraints gbConstraints = new GridBagConstraints();
	
	private DisabledPanel disabledpanel = new DisabledPanel(this);
	
	//method to position on grid bag layout
	protected void addGB(Component comp, int x, int y) {
		gbConstraints.gridx = x;
		gbConstraints.gridy = y;
		add(comp, gbConstraints);
	}
	
	//set visibility of a component and its paired label
	protected void setComponentAndLabelVisible(JLabel label, JComponent box, boolean state) {
		label.setVisible(state);
		box.setVisible(state);
	}
	
	public DisabledPanel getDisabledPanel() {
		return disabledpanel;
	}
}
