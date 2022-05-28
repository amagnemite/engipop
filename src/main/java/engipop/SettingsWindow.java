package engipop;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

//window for managing settings/config
//todo: unjank this
@SuppressWarnings("serial")
public class SettingsWindow extends JFrame {
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gb = new GridBagConstraints();
	
	static File cfgFileName = new File("engiconfig.cfg");
	static final String itemsPath = "items_path.txt path";
	
	//String itemsTxtPath;
	Map<String, String> modifiedConfig = new HashMap<String, String>();
	Map<String, String> oldConfig = new HashMap<String, String>();
	
	JTextField itemsTxtBox = new JTextField();
	window window;
	
	public SettingsWindow(window window) {
		super("Settings");
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		setSize(800, 200);
		
		this.setIconImage(engipop.window.icon.getImage());
		
		this.window = window;
		JLabel itemsTxtLabel = new JLabel("items_game.txt path: ");
		
		readFromConfig();
		
		itemsTxtBox.setEditable(false);
		if(modifiedConfig.get(itemsPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(itemsPath));
		}
		else {
			itemsTxtBox.setText(" ");
		}
		
		//if new items_game is selected or nothing is selected
		//update map
		JButton updateItemsPath = new JButton("...");
		updateItemsPath.addActionListener(event -> {
			File file = window.getItemsTxtPath();
			setItemsTxtPath(file);
		});
		
		JButton updateConfig = new JButton("Update settings");
		updateConfig.addActionListener(event -> {
			writeToConfig();
			
			if(modifiedConfig.get(itemsPath) != null) {
				window.parseItems(new File(modifiedConfig.get(itemsPath)));
			}
			
			oldConfig.clear();
			oldConfig.putAll(modifiedConfig);
		});
		
		windowClosing();
		
		addGB(this, gb, itemsTxtLabel, 0, 0);
		addGB(this, gb, itemsTxtBox, 1, 0);
		addGB(this, gb, updateItemsPath, 2, 0);
		
		addGB(this, gb, updateConfig, 2, 1);
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//makes it so canceling in windowClosing() does nothing
	}
	
	private void windowClosing() { //listener for when user attempts to close window
		JFrame parent = this;
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(!oldConfig.equals(modifiedConfig)) {
					int op = JOptionPane.showConfirmDialog(parent, "Save settings?");
					switch (op) {
						case JOptionPane.YES_OPTION: 
							writeToConfig();
							if(modifiedConfig.get(itemsPath) != null) {
								window.parseItems(new File(modifiedConfig.get(itemsPath)));
							}
							oldConfig.clear();
							oldConfig.putAll(modifiedConfig); //copies modified into old without making them point the same place
							parent.dispose();
							break;
						case JOptionPane.NO_OPTION:
							modifiedConfig.clear();
							modifiedConfig.putAll(oldConfig);
							parent.dispose();
							break;
						case JOptionPane.CANCEL_OPTION:
							break;
					}
				}
				else { //skip check if nothing was changed
					parent.dispose();
				}
			}
		});
	}
	
	public void setItemsTxtPath(File file) {
		if(file != null) {
			modifiedConfig.put(itemsPath, file.getPath());
			itemsTxtBox.setText(modifiedConfig.get(itemsPath));
		}
		else {
			modifiedConfig.put(itemsPath, null);
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
	}
	
	public String getItemsTxtPath() {
		return modifiedConfig.get(itemsPath);
	}
	
	//write map from config file
	public void readFromConfig() {
		Reader ir;
		BufferedReader in;
		
		try {
			ir = new InputStreamReader(new FileInputStream(cfgFileName));
			in = new BufferedReader(ir);
			String line;
			try {
				while((line = in.readLine()) != null) {
					modifiedConfig.put((line.substring(0, line.indexOf('='))), line.substring(line.indexOf('=') + 1));
					//key = substring of length (where the = is) - 1 to 0
					//value = substring starting at where the = is + 1
				}
				if(modifiedConfig.isEmpty() || modifiedConfig.get(itemsPath).equals("null")) {
					modifiedConfig.put(itemsPath, null);
				} //there may be othe rpossibly null values, in which case it'd be better handle nulls generically
			}
			catch (IOException e) {
				window.updateFeedback("Engiconfig.cfg could not be read");
			}
		}
		catch (FileNotFoundException f) {
			window.updateFeedback("Engiconfig.cfg could not be found");
		}
	}
	
	//write config file from map
	//ideally we would not write to file every time a var is changed, only when large amounts are
	public void writeToConfig() {
		FileWriter fw;
		PrintWriter pw;
		
		try {
			fw = new FileWriter(cfgFileName);
			pw = new PrintWriter(fw, true);
			
			modifiedConfig.forEach((k, v) -> {
				pw.println(k + "=" + v);
			});
			pw.close();
			
			oldConfig.clear();
			oldConfig.putAll(modifiedConfig);
		}
		catch (IOException e) {
			window.updateFeedback("Could not write to engiconfig.cfg");
		}
	}
	
	public void updateWindow() {
		if(modifiedConfig.get(itemsPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(itemsPath));
		}
		else {
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
		oldConfig.clear();
		oldConfig.putAll(modifiedConfig);
	}
	
	private void addGB(Container cont, GridBagConstraints gb, Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		cont.add(comp, gb);
	}
}
