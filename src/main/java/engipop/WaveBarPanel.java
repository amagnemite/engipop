package engipop;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
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
import engipop.Node.MissionNode;
import engipop.Node.PopNode;
import engipop.Node.RandomChoiceNode;
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
		"pyro", "pyro_flare",
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
	
	public void modifyIcon(TFBotNode bot, int count, BotType type, boolean isAddition) {
		String iconName = (String) bot.getValue(TFBotNode.CLASSICON);
		boolean isCrit = false;
		
		if(bot.getListValue(TFBotNode.ATTRIBUTES) != null) {
			if(bot.getListValue(TFBotNode.ATTRIBUTES).contains("AlwaysCrit")) {
				isCrit = true;
			}
			if(bot.getListValue(TFBotNode.ATTRIBUTES).contains("MiniBoss")) {
				type = BotType.GIANT;
			}
		}
		
		if(bot.getValue(TFBotNode.TEMPLATE) != null && (iconName == null || isCrit == false || type == null)) {
			//need to go through the whole list if not miniboss/minicrits
			Node node = bot;
			
			while(node != null && node.getValue(TFBotNode.TEMPLATE) != null) {
				node = Engipop.findTemplateNode((String) node.getValue(TFBotNode.TEMPLATE));
				//if template isn't found it returns null so just terminate early
				if(node == null) {
					break;
				}
				
				if(iconName == null) {
					iconName = node != null ? (String) node.getValue(TFBotNode.CLASSICON) : null;
				}
				
				if(node.getListValue(TFBotNode.ATTRIBUTES) != null) {
					if(node.getListValue(TFBotNode.ATTRIBUTES).contains("AlwaysCrit")) {
						isCrit = true;
					}
					if(node.getListValue(TFBotNode.ATTRIBUTES).contains("MiniBoss")) {
						type = BotType.GIANT;
					}
				}
			}
			
			
			if(iconName == null) {
				//if we reached the base template without finding a classicon, it probably
				//uses the class one
				iconName = getClassIconName(node.getValue(TFBotNode.CLASSNAME).toString());
			}
		}
		
		if(iconName == null) {
			if(bot.getValue(TFBotNode.CLASSNAME) != Classes.None) {
				String value = bot.getValue(TFBotNode.CLASSNAME).toString();
				
				iconName = getClassIconName(value);
			}
			else {
				//no class name, couldn't find template
				iconName = "null";
			}
		}
		
		if(type == null) {
			type = BotType.COMMON;
		}
		
		if(isAddition) {
			addIcon(iconName, isCrit, count, type);
		}
		else {
			removeIcon(iconName, isCrit, count, type);
		}
	}
	
	private String getClassIconName(String value) {
		switch(value) {
			case "Heavyweapons":
				return "heavy";
			case "Demoman":
				return "demo";
			default:
				return value;
		}
	}
	
	public void addIcon(String iconName, boolean isCrit, int count, BotType type) {
		WaveBarIcon icon = new WaveBarIcon(type, iconName, isCrit, count);
		String mapName = iconName + "_" + type.toString();
		int indexShift = 0;
		
		//support don't have counts + visible crits so just skip them
		//TODO: make sure this doesn't cause odd things with the if
		if(iconNames.containsKey(mapName)) {
			if(type != BotType.SUPPORT) {
				if(isCrit && !iconNames.get(mapName).getCrit()) {
					iconNames.get(mapName).setCrit(isCrit);
				}
				iconNames.get(mapName).addToCount(count);
			}
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
	}
	
	public void removeIcon(String iconName, boolean isCrit, int count, BotType type) {
		String mapName = null;
		
		mapName = iconName + "_" + type.toString();
		
		iconNames.get(mapName).subtractFromCount(count);
		if(iconNames.get(mapName).getCount() == 0) {
			WaveBarIcon removedIcon = iconNames.remove(mapName);
			iconArray.remove(removedIcon);
			remove(removedIcon);
			
			switch(type) {
				case GIANT:
					giantIndex--;
				case COMMON:
					commonIndex--;
				case SUPPORT:
					supportIndex--;
				case MISSION:
					missionIndex--;
					break;
			}
		}
	}
	
	public void rebuildWavebar(WaveNode wave) {
		iconArray.clear();
		iconNames.clear();
		removeAll();
		
		giantIndex = -1; //index of last giant icon
		commonIndex = -1;
		supportIndex = -1;
		missionIndex = -1;
		
		for(Node node : wave.getChildren()) {
			WaveSpawnNode ws = (WaveSpawnNode) node;
			BotType type = null;
			int count = (int) ws.getValue(WaveSpawnNode.TOTALCOUNT);
			int spawnCount = (int) ws.getValue(WaveSpawnNode.SPAWNCOUNT);
			int batches = spawnCount != 0 ? count / spawnCount : spawnCount; //tank ws can have 0 spawncount
			if(count == 0 || ws.getSpawnerType() == null) {
				continue;
			}
			if((boolean) ws.getValue(WaveSpawnNode.SUPPORT)) {
				type = BotType.SUPPORT;
			}
			
			switch(ws.getSpawnerType()) {
				case RANDOMCHOICE:
					List<Node> spawners = ws.getSpawner().getChildren();
					int childrenCount = spawners.size();
					
					if(!((RandomChoiceNode) ws.getSpawner()).hasNestedSquadRC()) {
						for(int i = 0; i < batches; i++) {
							modifyIcon((TFBotNode) (spawners.get(ThreadLocalRandom.current().nextInt(0, childrenCount))), spawnCount, type, true);
						}
					}
					break;
				case SQUAD:
					for(Node bot : ws.getSpawner().getChildren()) {
						modifyIcon((TFBotNode) bot, batches, type, true);
					}
					break;
				case TANK:
					if(type == null) {
						type = BotType.GIANT;
					}
					addIcon("tank", false, count, type);
					break;
				case TFBOT:
					modifyIcon((TFBotNode) ws.getSpawner(), count, type, true);
					break;
				case NONE:
				default:
					//skip anything else since they won't be on wavebar anyway
					break;
			}
		}
		if(Engipop.getPopNode().getListValue(PopNode.MISSION) != null) {
			int waveNum = Engipop.getPopNode().getChildren().indexOf(wave) + 1;
			
			for(Object node : Engipop.getPopNode().getListValue(PopNode.MISSION)) {
				MissionNode mission = (MissionNode) node;
				int begin = (int) mission.getValue(MissionNode.BEGINATWAVE);
				int run = (int) mission.getValue(MissionNode.RUNFORTHISMANYWAVES);
				if(mission.getValue(MissionNode.OBJECTIVE).equals(MissionNode.DESTROYSENTRIES)) {
					continue;
				}
				
				if(begin == waveNum || (waveNum > begin && waveNum < (begin + run - 1))) {
					modifyIcon((TFBotNode) mission.getChildren().get(0), 0, BotType.SUPPORT, true);
				}
			}
		}
		System.out.println(getSize());
		//setPreferredSize(new Dimension(iconArray.size() * WaveBarIcon.WIDTH + (iconArray.size() - 1) * gbConstraints.ipadx, WaveBarIcon.HEIGHT));
		validate();
		repaint();
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
		
		public static final int WIDTH = 32;
		public static final int HEIGHT = 48;
		private static final URL REDURL = MainWindow.class.getResource("/redbg.png");
		private static final URL WHITEURL = MainWindow.class.getResource("/whitebg.png");
		private static final URL CRITURL = MainWindow.class.getResource("/crit.png");
		private static final URL MISSINGURL = MainWindow.class.getResource("/missing.png");
		
		public WaveBarIcon(BotType botType, String iconName, boolean isCrit, int count) {
			iconName = iconName.endsWith("_giant") ? iconName.substring(0, iconName.length() - 6) : iconName;
			this.iconName = iconName;
			this.count = count;
			this.isCrit = isCrit;
			type = botType;
			String fullIconName = "leaderboard_class_" + iconName + ".vtf";
			File iconFile = null;
			
			if(valveIcons.contains(iconName.toLowerCase())) {
				fullIconName = "/" + fullIconName;
				iconFile = new File(MainWindow.class.getResource(fullIconName).getFile());
			}
			else {
				iconFile = Engipop.getIconPath().resolve(fullIconName).toFile();
				if(!iconFile.exists()) {
					iconFile = Engipop.getDownloadIconPath().resolve(fullIconName).toFile();
				}
			}
			
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			
			if(botType == BotType.GIANT) {
				bg = new JLabel(new ImageIcon(REDURL));
			}
			else {
				bg = new JLabel(new ImageIcon(WHITEURL));
			}
			
			if(iconFile.exists()) {
				icon = parseIcon(iconFile);
			}
			else {
				icon = new JLabel(new ImageIcon(MISSINGURL));
			}
			
			iconPanel.add(icon);
			
			if(type != BotType.SUPPORT) {
				countLabel = new JLabel(Integer.toString(count));
				iconPanel.add(countLabel);
			}
			
			if(isCrit) {
				crit = new JLabel(new ImageIcon(CRITURL));
				crit.setBounds(0, 0, 32, 32);
				add(crit, Integer.valueOf(3));
			}
			
			bg.setBounds(0, 0, 32, 32);
			iconPanel.setBounds(0, 0, WIDTH, HEIGHT);
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
				return null;
				//error
			}
			int height = reader.getHeight(data);
			int width = reader.getWidth(data);
			int[] pixels = reader.readIcon(data);
			if(pixels == null) {
				return null;
				//error
			}
			
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, width, height, pixels, 0, width);
			
			return new JLabel(new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
		}
	}
}
