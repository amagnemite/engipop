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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private static final String TFPATH = "TFPath";
	
	private Map<String, String> modifiedConfig = new HashMap<String, String>();
	private Map<String, String> oldConfig = new HashMap<String, String>();
	
	private JTextField pathText = new JTextField(45);
	private MainWindow window;
	
	public SettingsWindow(MainWindow window) {
		super("Settings");
		gbConstraints.anchor = GridBagConstraints.WEST;
		setSize(800, 200);
		
		this.window = window;
		JLabel itemsTxtLabel = new JLabel("tf path: ");
		pathText.setEditable(false);
		
		readFromConfig(); //checks if config exists
		initConfig(); //inits config if it doesn't
		
		Path path = modifiedConfig.get(TFPATH) != null ? Paths.get(modifiedConfig.get(TFPATH)) : null;
		setTFPath(path);
		
		oldConfig.putAll(modifiedConfig);
		
		//there may be other possibly null values, in which case it'd be better handle nulls generically
		
		//if new items_game is selected or nothing is selected
		//update map
		JButton updateItemsPath = new JButton("...");
		updateItemsPath.addActionListener(event -> {
			Path newPath = promptTFPath();
			modifiedConfig.put(TFPATH, newPath.toString());
			if(path != null) {
				pathText.setText(newPath.toString());
			}
			else {
				pathText.setText(" ");
			}
			validate();
		});
		
		JButton updateConfig = new JButton("Update settings");
		updateConfig.addActionListener(event -> {
			writeToConfig();
			
			if(modifiedConfig.get(TFPATH) != null) {
				setTFPath(Paths.get(modifiedConfig.get(TFPATH)));
			}
		});
		
		windowClosing();
		
		addGB(itemsTxtLabel, 0, 0);
		addGB(pathText, 1, 0);
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
							if(modifiedConfig.get(TFPATH) != null) {
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
			//modifiedConfig.put(tfPath, path.toString());
			//itemsTxtBox.setText(modifiedConfig.get(tfPath));
			pathText.setText(path.toString());
			Engipop.setTFPath(path);
			parseItems(Engipop.getItemSchemaPath().toFile());
		}
		else {
			//modifiedConfig.put(tfPath, null);
			pathText.setText(" ");
		}
		validate(); //updates textbox size
	}
	
	public void updateWindow() { //reloads text to old if window was previously closed with unsaved changes
		if(oldConfig.get(TFPATH) != null) {
			pathText.setText(oldConfig.get(TFPATH));
		}
		else {
			pathText.setText(" ");
		}
		validate(); //updates textbox size
		modifiedConfig.clear();
		modifiedConfig.putAll(oldConfig);
	}
	
	//write map from config file
	public void readFromConfig() {
		Reader ir;
		BufferedReader in;
		Set<String> validCfg = new HashSet<String>(Arrays.asList(TFPATH));
		Boolean updateConfig = false;
		
		try {
			ir = new InputStreamReader(new FileInputStream(cfgFileName));
			in = new BufferedReader(ir);
			String line;
			try {
				while((line = in.readLine()) != null) {
					String key = line.substring(0, line.indexOf('='));
					if(validCfg.contains(key)) {
						modifiedConfig.put(key, line.substring(line.indexOf('=') + 1));
					}
					else {
						updateConfig = true;
					}
					
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
		
		if(updateConfig) {
			writeToConfig();
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
				if(v == null) {
					pw.println(k + "=");
				}
				else {
					pw.println(k + "=" + v);
				}
			});
			pw.close();
			
			oldConfig.clear();
			oldConfig.putAll(modifiedConfig);
		}
		catch (IOException e) {
			window.setFeedback("Could not write to engiconfig.cfg");
		}
	}
	
	//check for itemstxtpath on load
	public void initConfig() {
		File cfg = new File("engiconfig.cfg");
		try {
			if(cfg.createNewFile() || modifiedConfig.get(TFPATH) == null) { //if no cfg existed or cfg existed but has no path set
				int op = JOptionPane.showConfirmDialog(this, "The TF2 scripts path is currently unset. Set it?", "Select an Option",
						JOptionPane.YES_NO_OPTION);
				
				if(op == JOptionPane.YES_OPTION) {
					Path path = promptTFPath(); //try to get item path, then parse
					if(path != null) {
						//setTFPath(tfPath);
						modifiedConfig.put(TFPATH, path.toString());
						writeToConfig();
					}
				}
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
		if(selection == null) {
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
