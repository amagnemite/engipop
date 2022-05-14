package engipop;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;

//window for managing settings/config
public class SettingsWindow extends JFrame {
	GridBagLayout gbLayout = new GridBagLayout();
	GridBagConstraints gb = new GridBagConstraints();
	
	static File cfgFileName = new File("engiconfig.cfg");
	static final String itemsPath = "items_path.txt path";
	
	//String itemsTxtPath; 
	Map<String, String> config = new HashMap<String, String>();
	
	JTextField itemsTxtBox = new JTextField();
	window window;
	
	public SettingsWindow(window window) {
		super("Settings");
		setLayout(gbLayout);
		gb.anchor = GridBagConstraints.WEST;
		setSize(800, 200);
		
		//File file;
		
		this.window = window;
		JLabel itemsTxtLabel = new JLabel("items_game.txt path: ");
		
		readFromConfig();
		
		itemsTxtBox.setEditable(false);
		if(config.get(itemsPath) != null) {
			itemsTxtBox.setText(config.get(itemsPath));
		}
		else {
			itemsTxtBox.setText(" ");
		}
		
		JButton updateItemsPath = new JButton("...");
		updateItemsPath.addActionListener(event -> {
			File file = window.getItemsTxtPath();
			if(file != null) {
				setItemsTxtPath(file);
				window.parseItems(file);
				writeToConfig();
			}
		});
		
		
		addGB(this, gb, itemsTxtLabel, 0, 0);
		addGB(this, gb, itemsTxtBox, 1, 0);
		addGB(this, gb, updateItemsPath, 2, 0);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//don't need this always open
	}
	
	public void setItemsTxtPath(File file) {
		config.put(itemsPath, file.getPath());
		itemsTxtBox.setText(config.get(itemsPath));
	}
	
	public String getItemsTxtPath() {
		return config.get(itemsPath);
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
					config.put((line.substring(0, line.indexOf('='))), line.substring(line.indexOf('=') + 1));
					//key = substring of length (where the = is) - 1 to 0
					//value = substring starting at where the = is + 1
				}
			}
			catch (IOException e) {
				
			}
		}
		catch (FileNotFoundException f) {
			
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
			
			config.forEach((k, v) -> {
				pw.println(k + "=" + v);
			});
			pw.close();
		}
		catch (IOException e) {
			//
		}
	}
	
	private void addGB(Container cont, GridBagConstraints gb, Component comp, int x, int y) {
		gb.gridx = x;
		gb.gridy = y;
		cont.add(comp, gb);
	}
}
