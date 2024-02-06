package engipop;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URL;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

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

	public static class NoDeselectionModel extends DefaultListSelectionModel {
	    public void removeSelectionInterval(int index0, int index1) {
	    	//intentionally does nothing
	    }
	}
	
	public static class PopFileFilter extends FileFilter {
		public boolean accept(File file) {
			if(file.isDirectory()) {
				return true;
			}
			
			String extension = file.getName();
			int i = extension.lastIndexOf('.');
			if (i > 0 && i < extension.length() - 1) {
				extension = extension.substring(i+1).toLowerCase();
	        }
			else {
				return false;
			}
			
			if(extension.equals("pop")) {
				return true;
			}
			return false;
		}

		public String getDescription() {
			return "Population files (.pop)";
		}
		
	}
}