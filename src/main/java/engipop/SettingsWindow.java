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
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private static File cfgFileName = new File("engiconfig.cfg");
	private static final String tfPath = "TFPath";
	
	private Map<String, String> modifiedConfig = new HashMap<String, String>();
	private Map<String, String> oldConfig = new HashMap<String, String>();
	
	private JTextField itemsTxtBox = new JTextField(45);
	private MainWindow window;
	
	public SettingsWindow(MainWindow window) {
		super("Settings");
		gbConstraints.anchor = GridBagConstraints.WEST;
		setSize(800, 200);
		
		this.window = window;
		JLabel itemsTxtLabel = new JLabel("tf path: ");
		itemsTxtBox.setEditable(false);
		
		readFromConfig();
		
		if(modifiedConfig.isEmpty() || modifiedConfig.get(tfPath) == null) {
			modifiedConfig.put(tfPath, null);
		} //there may be other possibly null values, in which case it'd be better handle nulls generically
		
		if(modifiedConfig.get(tfPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(tfPath));
			Engipop.setTFPath(Paths.get(modifiedConfig.get(tfPath)));
			//possibly fix length here
		}
		else {
			itemsTxtBox.setText(" ");
		}
		
		//if new items_game is selected or nothing is selected
		//update map
		JButton updateItemsPath = new JButton("...");
		updateItemsPath.addActionListener(event -> {
			setTFPath(promptTFPath());
		});
		
		JButton updateConfig = new JButton("Update settings");
		updateConfig.addActionListener(event -> {
			writeToConfig();
			
			if(modifiedConfig.get(tfPath) != null) {
				Engipop.setTFPath(Paths.get(modifiedConfig.get(tfPath)));
				parseItems(Engipop.getItemSchemaPath().toFile());	
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
							if(modifiedConfig.get(tfPath) != null) {
								parseItems(Engipop.getItemSchemaPath().toFile());
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
	
	public void setTFPath(Path path) {
		if(path != null) {
			modifiedConfig.put(tfPath, path.toString());
			itemsTxtBox.setText(modifiedConfig.get(tfPath));
			Engipop.setTFPath(path);
		}
		else {
			modifiedConfig.put(tfPath, null);
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
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
			}
			catch (IOException e) {
				window.setFeedback("Engiconfig.cfg could not be read");
			}
		}
		catch (FileNotFoundException f) {
			window.setFeedback("Engiconfig.cfg could not be found");
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
			window.setFeedback("Could not write to engiconfig.cfg");
		}
	}
	
	public void updateWindow() {
		if(modifiedConfig.get(tfPath) != null) {
			itemsTxtBox.setText(modifiedConfig.get(tfPath));
		}
		else {
			itemsTxtBox.setText(" ");
		}
		this.validate(); //updates textbox size
		oldConfig.clear();
		oldConfig.putAll(modifiedConfig);
	}
	
	//check for itemstxtpath on load
	public void initConfig() {
		File cfg = new File("engiconfig.cfg");
		try {
			if(cfg.createNewFile() || Engipop.getTFPath() == null) { //if no cfg existed or cfg existed but has no path set
				int op = JOptionPane.showConfirmDialog(this, "The TF2 scripts path is currently unset. Set it?");
				
				if(op == JOptionPane.YES_OPTION) {
					Path tfPath = promptTFPath(); //try to get item path, then parse
					if(tfPath != null) {
						setTFPath(tfPath);
						writeToConfig();
						//sw.updateWindow();
						parseItems(Engipop.getItemSchemaPath().toFile());
					}
				}
			}
			else {
				parseItems(Engipop.getItemSchemaPath().toFile());
			}
		}
		catch (IOException io) {
			window.setFeedback("engiconfig.cfg was not found or is unable to be written to");
		}
	}
	
	private Path promptTFPath() { //get file object of scripts
		Path defaultScriptsPath = null;
		if(System.getProperty("os.name").contains("Windows")) {
			defaultScriptsPath =
				Paths.get("C:", "Program Files (x86)", "Steam", "steamapps", "common", "Team Fortress 2", "tf");
		}
		else {
			// Linux/OSX
			defaultScriptsPath =
				Paths.get(System.getProperty("user.home"), ".steam", "steam", "steamapps", "common", "Team Fortress 2", "tf");
		}
		JFileChooser c = new JFileChooser(defaultScriptsPath.toFile());
		c.setDialogTitle("Select tf/ folder");
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		c.showOpenDialog(this);
		File selection = c.getSelectedFile();
		if (selection == null) {
			return null;
		}
		return c.getSelectedFile().toPath();
	}
	
	private void parseItems(File itemsTxt) { //take items file and parse into lists
		ItemParser itemParser = new ItemParser();
		itemParser.parse(itemsTxt, window);
		BotPanel.setItemParser(itemParser);
	}
}
