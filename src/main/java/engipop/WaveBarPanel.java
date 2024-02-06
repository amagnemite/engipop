package engipop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import engipop.Engipop.Classes;
import engipop.Node.PopNode;
import engipop.Node.TFBotNode;
import engipop.Node.WaveNode;
import engipop.Node.WaveSpawnNode;

@SuppressWarnings("serial")
public class WaveBarPanel extends EngiPanel {
	
	private static Set<String> valveIcons = new HashSet<String>(Arrays.asList(
		"scout", "scout_bat", "scout_bonk",
		"scout_fan", "scout_giant_fast", "scout_jumping",
		"scout_shortstop", "scout_stun", "scout_stun_armored",		
		"soldier", "soldier_backup", "soldier_barrage",
		"soldier_blackbox", "soldier_buff", "soldier_burstfire",
		"soldier_conch", "soldier_crit", "soldier_libertylauncher",
		"soldier_major_crits", "soldier_sergeant_crits", "soldier_spammer",
		"demo", "demo_bomber", "demo_burst",
		"demoknight", "demoknight_samurai",
		"heavy", "heavy_champ", "heavy_chief",
		"heavy_deflector", "heavy_deflector_healonkill", "heavy_deflector_push",
		"heavy_gru", "heavy_heater", "heavy_mittens", "heavy_shotgun",
		"heavy_steelfist", "heavy_urgent",
		"engineer",
		"medic", "medic_uber",
		"sniper", "sniper_bow", "sniper_multi",
		"sniper_jarate", "sniper_sydneysleeper",
		"spy",
		"tank"));
	private List<WaveBarIcon> iconArray = new ArrayList<WaveBarIcon>();
	private Map<String, WaveBarIcon> iconNames = new HashMap<String, WaveBarIcon>();
	private int giantIndex = -1; //index of last giant icon
	private int commonIndex = -1;
	private int supportIndex = -1;
	private int missionIndex = -1;
	
	public enum BotType {
		GIANT,
		COMMON,
		SUPPORT,
		MISSION
	}
	
	public void addIcon(TFBotNode bot, int count, BotType type) {
		String iconName = (String) bot.getValue(TFBotNode.CLASSICON);
		String mapName = null;
		boolean isCrit = false;
		int indexShift = 0;
		
		if(bot.getListValue(TFBotNode.ATTRIBUTES) != null && bot.getListValue(TFBotNode.ATTRIBUTES).contains("AlwaysCrit")) {
			isCrit = true;
		}
		
		if(iconName == null) {
			//resolve templates here
			if(bot.getValue(TFBotNode.CLASSNAME) != Classes.None) {
				iconName = bot.getValue(TFBotNode.CLASSNAME).toString();
			}
			else {
				//iconName = "random_lite";
				iconName = (String) bot.getValue(TFBotNode.TEMPLATE);
			}
		}
		
		if(type == null) {
			List<Object> attributes = bot.getListValue(TFBotNode.ATTRIBUTES);
			if(attributes != null && attributes.contains("MINIBOSS")) { //TODO: standardize attribute strings
				type = BotType.GIANT;
			}
			else {
				type = BotType.COMMON;
			}
		}
		
		WaveBarIcon icon = new WaveBarIcon(type, iconName, isCrit, count);
		mapName = iconName + "_" + type.toString();
		
		if(iconNames.containsKey(mapName)) {
			if(isCrit && !iconNames.get(mapName).getCrit()) {
				iconNames.get(mapName).setCrit(isCrit);
			}
			iconNames.get(mapName).addToCount(count);
		}
		else {
			iconNames.put(mapName, icon);
			
			switch(type) {
				case GIANT:
					giantIndex++;
				case COMMON:
					commonIndex++;
				case SUPPORT:
					supportIndex++;
				case MISSION:
					missionIndex++;
					break;
			}
			
			switch(type) {
				case GIANT:
					iconArray.add(giantIndex, icon);
					indexShift = giantIndex;
					break;
				case COMMON:
					iconArray.add(commonIndex, icon);
					indexShift = commonIndex;
					break;
				case SUPPORT:
					iconArray.add(supportIndex, icon);
					indexShift = supportIndex;
					break;
				case MISSION:
					iconArray.add(missionIndex, icon);
					indexShift = missionIndex;
					break;
			}
			
			for(int i = indexShift; i < iconArray.size(); i++) {
				addGB(iconArray.get(i), 0, i);
			}
		}
	}
	
	public void addIcon(String iconName, boolean isCrit, int count, BotType botType) {
		WaveBarIcon icon = new WaveBarIcon(botType, iconName, isCrit, count);
		int indexShift = 0;
			
		if(botType == BotType.GIANT) {
			giantIndex++;
			iconArray.add(giantIndex, icon);
			indexShift = giantIndex;
		}
		else if(botType == BotType.COMMON) {
			commonIndex++;
			iconArray.add(commonIndex, icon);
			indexShift = commonIndex;
		}
		
		for(int i = indexShift; i < iconArray.size(); i++) {
			addGB(iconArray.get(i), 0, i);
		}
	}
	
	public void updateIcon(boolean oldCrit, Classes oldClass, BotType oldType, int oldCount, boolean isCrit, 
			Classes cclass, BotType type, int count) {
		
		String oldIconName = null;
		String iconName = null;
		
		//resolve no class/templates here
		if(oldClass != Classes.None) {
			iconName = oldClass.toString();
		}
		else {
			oldIconName = "random_lite";
		}
		
		if(cclass != Classes.None) {
			iconName = cclass.toString();
		}
		else {
			iconName = "random_lite";
		}
		
		updateIcon(oldCrit, oldIconName, oldType, oldCount, isCrit, iconName, type, count);
	}
	
	public void updateIcon(boolean oldCrit, String oldIconName, BotType oldType, int oldCount, boolean isCrit, 
			String iconName, BotType type, int count) {
		
		boolean sameCrit = oldCrit == isCrit;
		boolean sameIcon = oldIconName.equals(iconName);
		boolean sameType = oldType.equals(type);
		boolean sameCount = oldCount == count;
		
		if(sameCrit && sameIcon && sameType) {
			if(sameCount) {
				return;
			}
			else {
				for(WaveBarIcon icon : iconArray) {
					if(icon.isEquivalent(oldIconName, oldCount, oldCrit, oldType)) {
						if(oldCount > count) {
							icon.subtractFromCount(oldCount - count);
						}
						else {
							icon.addToCount(count - oldCount);
						}
						break;
					}
				}
			}
		}
		else {
			int oldIndex = 0;
			int newIndex = -1;
			
			for(int i = 0; i < iconArray.size(); i++) {
				if(iconArray.get(i).isEquivalent(iconName, count, isCrit, type)) { //new icon is already present
					newIndex = i;
				}
				else if(iconArray.get(i).isEquivalent(oldIconName, oldCount, oldCrit, oldType)) {
					oldIndex = i;
				}
			}
			
			if(newIndex > oldIndex) { //icon is already present but happens later in list
				WaveBarIcon oldExistingIcon = iconArray.get(oldIndex); //icon we're changing from
				WaveBarIcon existingIcon = iconArray.get(newIndex); //icon we're changing to
				
				iconArray.set(oldIndex, existingIcon);
				//existingIcon.updateCount(existingIcon.getCount() - count);
				iconArray.remove(newIndex);
				
				//oldExistingIcon.updateCount(oldExistingIcon.getCount() - oldCount);
				if(oldExistingIcon.getCount() == 0) { //only remove old icon if none of it exists anymore
					iconArray.remove(oldIndex);
					
					if(!sameType) {
						switch (oldType) {
							case GIANT: 
								giantIndex--;
								break;
							case COMMON:
								commonIndex--;
								break;
							case SUPPORT:
								supportIndex--;
								break;
							case MISSION:
								supportIndex--;
								break;
						}
					}
				}
			}
			else if(newIndex > -1) { //icon is already present but happens earlier in list
				WaveBarIcon existingIcon = iconArray.get(newIndex);
				
			//	existingIcon.updateCount(existingIcon.getCount() - count);
				iconArray.remove(oldIndex);
			}
			else { //icon is not already present
				WaveBarIcon newIcon = new WaveBarIcon(type, iconName, isCrit, count);
				
				if(sameType) {
					iconArray.set(oldIndex, newIcon);
				}
				
				addGB(newIcon, 0, oldIndex);
			}
		}
	}
	
	public void removeIcon(BotType botType, Classes cclass, boolean isCrit, int count) {
		String iconName = null;
		
		if(cclass != Classes.None) {
			iconName = cclass.toString();
		}
		else {
			iconName = "random_lite";
		}
		
		removeIcon(botType, iconName, isCrit, count);
	}
	
	public void removeIcon(BotType botType, String iconName, boolean isCrit, int count) {
		for(WaveBarIcon icon : iconArray) {
			if(icon.isEquivalent(iconName, count, isCrit, botType)) {
				iconArray.remove(icon); //TODO: check this is actually allowed
				break;
			}
		}
	}
	
	public void rebuildWavebar(WaveNode wave, PopNode pop) {
		iconArray.clear();
		iconNames.clear();
		removeAll();
		
		giantIndex = -1; //index of last giant icon
		commonIndex = -1;
		supportIndex = -1;
		missionIndex = -1;
		
		for(Node node : wave.getChildren()) {
			//TODO: need to pull missions as well
			WaveSpawnNode ws = (WaveSpawnNode) node;
			BotType type = null;
			int count = (int) ws.getValue(WaveSpawnNode.TOTALCOUNT);
			int spawnCount = (int) ws.getValue(WaveSpawnNode.SPAWNCOUNT);
			int batches = spawnCount != 0 ? count / spawnCount : spawnCount; //tank ws can have 0 spawncount
			if(count == 0) {
				continue;
			}
			if((boolean) ws.getValue(WaveSpawnNode.SUPPORT)) {
				type = BotType.SUPPORT;
			}
			
			switch(ws.getSpawnerType()) {
				case RANDOMCHOICE:
					List<Node> spawners = ws.getSpawner().getChildren();
					int childrenCount = spawners.size();
					
					for(int i = 0; i < batches; i++) {
						addIcon((TFBotNode) (spawners.get(ThreadLocalRandom.current().nextInt(0, childrenCount))), spawnCount, type);
					}
					break;
				case SQUAD:
					for(Node bot : ws.getSpawner().getChildren()) {
						addIcon((TFBotNode) bot, batches, type);
					}
					break;
				case TANK:
					if(type == null) {
						type = BotType.GIANT;
					}
					addIcon("tank", false, count, type);
					break;
				case TFBOT:
					addIcon((TFBotNode) ws.getSpawner(), count, type);
					break;
				case NONE:
				default:
					//deal with these later
					break;
			}
		}
	}
	
	
	public class WaveBarIcon extends JLayeredPane {
		private JPanel iconPanel = new JPanel();
		private JLabel bg;
		private JLabel icon;
		private JLabel crit;
		private JLabel countLabel;
		
		private String iconName;
		private int count;
		private boolean isCrit;
		private BotType type;
		
		private static final URL REDURL = MainWindow.class.getResource("/redbg.png");
		private static final URL WHITEURL = MainWindow.class.getResource("/whitebg.png");
		private static final URL CRITURL = MainWindow.class.getResource("/crit.png");
		
		public WaveBarIcon(BotType botType, String iconName, boolean isCrit, int count) {
			this.iconName = iconName;
			this.count = count;
			this.isCrit = isCrit;
			type = botType;
			String fullIconName = "leaderboard_class_" + iconName + ".vtf";
			File iconFile = null;
			
			if(valveIcons.contains(iconName.toLowerCase())) {
				fullIconName = "/" + fullIconName;
				try {
					iconFile = new File(MainWindow.class.getResource(fullIconName).toURI());
				} catch (URISyntaxException e) {
					//
				}
			}
			else {
				iconFile = Engipop.getIconPath().resolve(iconName).toFile();
				if(!iconFile.exists()) {
					iconFile = Engipop.getDownloadIconPath().resolve(iconName).toFile();
					if(!iconFile.exists()) {
						iconFile = Engipop.getDownloadIconPath().resolve("leaderboard_class_random_lite.vtf").toFile();
					}
				}
			}
			
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
			setPreferredSize(new Dimension(64, 128));
			
			if(botType == BotType.GIANT) {
				bg = new JLabel(new ImageIcon(REDURL));
			}
			else {
				bg = new JLabel(new ImageIcon(WHITEURL));
			}
			
			icon = parseIcon(iconFile);
			countLabel = new JLabel(Integer.toString(count));
			iconPanel.add(icon);
			iconPanel.add(countLabel);
			
			if(isCrit) {
				crit = new JLabel(new ImageIcon(CRITURL));
				crit.setBounds(0, 0, 32, 32);
				add(crit, Integer.valueOf(3));
			}
			
			bg.setBounds(0, 0, 32, 32);
			iconPanel.setBounds(0, 0, 32, 48);
			iconPanel.setOpaque(false);
			
			add(bg, Integer.valueOf(0));
			add(iconPanel, Integer.valueOf(1));
		}
		
		public boolean equals(WaveBarIcon icon2) {
			if(!iconName.equals(icon2.getIconName())) {
				return false;
			}
			if(count != icon2.getCount()) {
				return false;
			}
			if(isCrit != icon2.getCrit()) {
				return false;
			}
			if(!type.equals(icon2.getType())) {
				return false;
			}
			return true;
		}
		
		public boolean isEquivalent(String iconName, int count, boolean isCrit, BotType type) {
			if(!this.iconName.equals(iconName)) {
				return false;
			}
			if(this.count != count) {
				return false;
			}
			if(this.isCrit != isCrit) {
				return false;
			}
			if(!this.type.equals(type)) {
				return false;
			}
			return true;
		}
		
		public void addToCount(int addition) {
			count += addition;
			countLabel.setText(Integer.toString(count));
		}
		
		public void subtractFromCount(int difference) {
			count -= difference;
			countLabel.setText(Integer.toString(count));
		}
		
		public String getIconName() {
			return iconName;
		}
		
		public int getCount() {
			return count;
		}
		
		public boolean getCrit() {
			return isCrit;
		}
		
		public void setCrit(boolean isCrit) {
			this.isCrit = isCrit;
			if(isCrit) {
				crit = new JLabel(new ImageIcon(CRITURL));
				crit.setBounds(0, 0, 32, 32);
				add(crit, Integer.valueOf(3));
			}
			else {
				crit = null;
			}
		}
		
		public BotType getType() {
			return type;
		}
		
		private JLabel parseIcon(File icon) {
			IconReader reader = new IconReader();
			byte[] data = reader.getImageData(icon);
			if(data == null) {
				System.out.println("bad input");
				return null;
			}
			if((data[0] & 0xFF) != 0x56 && (data[1] & 0xFF) != 0x54 && (data[2] & 0xFF) != 0x46) {
				//error
			}
			int height = reader.getHeight(data);
			int width = reader.getWidth(data);
			int[] pixels = reader.readIcon(data);
			if(pixels == null) {
				//error
			}
			
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, width, height, pixels, 0, width);
			
			return new JLabel(new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
		}
	}
}
