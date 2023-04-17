package engipop;

import java.awt.GridBagConstraints;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

//window for managing settings/config
//todo: unjank this
@SuppressWarnings("serial")
public class SettingsWindow extends EngiWindow {
	static File cfgFileName = new File("engiconfig.cfg");
	private static final String scriptsPath = "tf2 scripts path";
	private static final String itemsTxtPath = "\\items\\items_game.txt";
	
	//String itemsTxtPath;
	Map<String, String> modifiedConfig = new HashMap<String, String>();
	Map<String, String> oldConfig = new HashMap<String, String>();
	
	JTextField itemsTxtBox = new JTextField(45);
	MainWindow window;
	
	public SettingsWindow(MainWindow window) {
		super("Settings");
		gbConstraints.anchor = GridBagConstraints.WEST;
		setSize(800, 200);
		
		this.window = window;
		JLabel itemsTxtLabel = new JLabel("tf2 scripts path: ");
		
		readFromConfig();
		
		itemsTxtBox.setEditable(false);
		
		if(modifiedConfig.get(scriptsPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(scriptsPath));
			//possibly fix length here
		}
		else {
			itemsTxtBox.setText(" ");
		}
		
		//if new items_game is selected or nothing is selected
		//update map
		JButton updateItemsPath = new JButton("...");
		updateItemsPath.addActionListener(event -> {
			File file = getScriptPath();
			setScriptPathString(file);
		});
		
		JButton updateConfig = new JButton("Update settings");
		updateConfig.addActionListener(event -> {
			writeToConfig();
			
			if(modifiedConfig.get(scriptsPath) != null) {
				window.parseItems(new File(modifiedConfig.get(scriptsPath) + itemsTxtPath));
			}
			
			oldConfig.clear();
			oldConfig.putAll(modifiedConfig);
		});
		
		windowClosing();
		
		addGB(itemsTxtLabel, 0, 0);
		addGB(itemsTxtBox, 1, 0);
		addGB(updateItemsPath, 2, 0);
		
		addGB(updateConfig, 2, 1);
		
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
							if(modifiedConfig.get(scriptsPath) != null) {
								window.parseItems(new File(modifiedConfig.get(scriptsPath) + itemsTxtPath));
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
	
	public void setScriptPathString(File file) {
		if(file != null) {
			modifiedConfig.put(scriptsPath, file.getPath());
			itemsTxtBox.setText(modifiedConfig.get(scriptsPath));
		}
		else {
			modifiedConfig.put(scriptsPath, null);
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
	}
	
	public String getScriptPathString() {
		return modifiedConfig.get(scriptsPath);
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
				if(modifiedConfig.isEmpty() || modifiedConfig.get(scriptsPath).equals("null")) {
					modifiedConfig.put(scriptsPath, null);
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
		if(modifiedConfig.get(scriptsPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(scriptsPath));
		}
		else {
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
		oldConfig.clear();
		oldConfig.putAll(modifiedConfig);
	}
	
	//check for itemstxtpath on load
	public void initConfig(MainWindow mw) {
		File cfg = new File("engiconfig.cfg");
		try {
			if(cfg.createNewFile() || this.getScriptPathString() == null) { //if no cfg existed or cfg existed but has no path set
				int op = JOptionPane.showConfirmDialog(this, "The TF2 scripts path is currently unset. Set it?");
				
				if(op == JOptionPane.YES_OPTION) {
					File itemsTxt = getScriptPath(); //try to get item path, then parse
					if(itemsTxt != null) {
						setScriptPathString(itemsTxt);
						writeToConfig();
						//sw.updateWindow();
						mw.parseItems(new File(getScriptPathString() + itemsTxtPath));
					}
				}
			}
			else {	
				mw.parseItems(new File(getScriptPathString() + itemsTxtPath));
			}
		}
		catch (IOException io) {
			mw.updateFeedback("engiconfig.cfg was not found or is unable to be written to");
		}
	}
	
	private File getScriptPath() { //get file object of scripts
		JFileChooser c;
		File file = null; //prob shouldn't do this but no defaults for linux/osx
		//boolean selectingFile = true;
		
		if(System.getProperty("os.name").contains("Windows")) { //for windows, default to standard items_game path
			file = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Team Fortress 2\\tf\\scripts");
			c = new JFileChooser(file);
		}
		else { //make linux and rare osx people suffer
			//file = new File();
			c = new JFileChooser();
		}
		
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		//while(selectingFile) {
			c.showOpenDialog(this);
			file = c.getSelectedFile();
			/*
			if(file != null && !file.getName().equals("items_game.txt")) {
				int op = JOptionPane.showConfirmDialog(this, "File selected is not items_game.txt. Select a new file?");
				if (op != JOptionPane.YES_OPTION) { //if cancelled or no, leave
					selectingFile = false;
					file = null; //clear out
				} //otherwise it just opens the dialogue again
			}
			else { //if it turns out it's a different file named items_game.txt, the parser will handle it
				selectingFile = false;
			} */
		//}
		return file;
	}
}
