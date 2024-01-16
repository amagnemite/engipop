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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WaveBarPanel extends EngiPanel {
	
	//private WaveBarIcon[] giantIcons = new WaveBarIcon[22];
	//private WaveBarIcon[] commonIcons = new WaveBarIcon[22];
	
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
		"spy"));
	private List<WaveBarIcon> iconArray = new ArrayList<WaveBarIcon>();
	private int giantIndex = -1; //index of last giant icon
	private int commonIndex = -1;
	
	public enum BotType {
		GIANT,
		COMMON,
		SUPPORT,
		MISSION
	}
	
	public void addIcon(BotType botType, String iconName, boolean isCrit, int count) {
		String fileName = "leaderboard_class_" + iconName + ".vtf";
		File iconFile = null;
		
		if(valveIcons.contains(iconName)) {
			fileName = "/" + fileName;
			try {
				iconFile = new File(MainWindow.class.getResource(fileName).toURI());
			} catch (URISyntaxException e) {
				//
			}
		}
		else {
			//find file here
		}
		
		WaveBarIcon icon = new WaveBarIcon(botType, iconFile, isCrit, count);
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
	
	public void updateIcon() {}
	
	public class WaveBarIcon extends JLayeredPane {
		private JPanel iconPanel = new JPanel();
		
		private JLabel bg;
		private JLabel icon;
		private JLabel crit;
		private JLabel countLabel;
		
		private static URL redURL = MainWindow.class.getResource("/redbg.png");
		private static URL whiteURL = MainWindow.class.getResource("/whitebg.png");
		private static URL critURL = MainWindow.class.getResource("/crit.png");
		
		public WaveBarIcon(BotType botType, File iconFile, boolean isCrit, int count) {
			iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
			
			setPreferredSize(new Dimension(64, 128));
			
			if(botType == BotType.GIANT) {
				bg = new JLabel(new ImageIcon(redURL));
			}
			else {
				bg = new JLabel(new ImageIcon(whiteURL));
			}
			
			icon = parseIcon(iconFile);
			countLabel = new JLabel(Integer.toString(count));
			iconPanel.add(icon);
			iconPanel.add(countLabel);
			
			if(isCrit) {
				crit = new JLabel(new ImageIcon(critURL));
				crit.setBounds(0, 0, 32, 32);
				add(crit, Integer.valueOf(3));
			}
			
			bg.setBounds(0, 0, 32, 32);
			iconPanel.setBounds(0, 0, 32, 64);
			iconPanel.setOpaque(false);
			
			add(bg, Integer.valueOf(0));
			add(iconPanel, Integer.valueOf(1));
		}
		
		private JLabel parseIcon(File icon) {
			IconReader reader = new IconReader();
			byte[] data = reader.getImageData(icon);
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
