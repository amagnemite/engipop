package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import disabledpanel.DisabledPanel;
import engipop.Node.RelayNode;
import engipop.Node.WaveNode;

@SuppressWarnings("serial")
public class EngiPanel extends JPanel {
	private GridBagLayout gbLayout = new GridBagLayout();
	protected GridBagConstraints gbConstraints = new GridBagConstraints();
	
	private DisabledPanel disabledpanel = new DisabledPanel(this);
	
	public EngiPanel() {
		setLayout(gbLayout);
	}
	
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
	
	public void updateRelayKey(String relayName, String value, String relayKey, Node parent, RelayNode relay) {
		if(!parent.containsKey(relayName) && (value == null || value.isBlank())) {
			return;
		}
		
		if(!parent.containsKey(relayName)) {
			parent.putKey(relayName, relay);
		}
		relay.putKey(relayKey, value);
	}
}
