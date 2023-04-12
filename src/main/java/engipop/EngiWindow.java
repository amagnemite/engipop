package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
//generic jframe time
public class EngiWindow extends JFrame {
	private static URL iconURL = MainWindow.class.getResource("/icon.png");
	public static ImageIcon icon = new ImageIcon(iconURL); //same icon across all windows
	
	//common node check types
	/*
	public final static int NONE = -1;
	public final static int WAVESPAWN = 0;
	public final static int TFBOT = 1;
	public final static int TANK = 2;
	public final static int SQUAD = 3;
	public final static int RANDOMCHOICE = 4;
	*/
	
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gbConstraints = new GridBagConstraints();
	
	protected JLabel feedback; //output text feedback to user, not all windows need this

	public EngiWindow(String frameText) {
		super(frameText);
		setLayout(gbLayout);
		setIconImage(icon.getImage());
		gbConstraints.insets = new Insets(0, 0, 5, 0);
	}
	
	//add component to frame
	public void addGB(Component comp, int x, int y) {
		gbConstraints.gridx = x;
		gbConstraints.gridy = y;
		this.add(comp, gbConstraints);
	}
	
	public void updateFeedback(String string) {
		feedback.setText(string);
	}
	
	public static class NoDeselectionModel extends DefaultListSelectionModel {

	    @Override
	    public void removeSelectionInterval(int index0, int index1) {
	    	
	    }
	}
}