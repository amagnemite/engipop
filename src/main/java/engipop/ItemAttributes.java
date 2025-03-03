package engipop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

//read itemattributes from file and return the list
public class ItemAttributes {

	public ItemAttributes() {
		
	}
	
	public String[] getItemAttributes() {
		URL url = ItemAttributes.class.getResource("/itemattributes.txt");
		
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			Reader ir = new InputStreamReader(url.openStream());
			BufferedReader in = new BufferedReader(ir);
			
			String line;
			
			while((line = in.readLine()) != null) {
				//System.out.println(line);
				list.add(line.substring(0, line.indexOf("-")));
				//separator from comments
			}
			in.close();		
		}
		catch (IOException e) {

		}
		
		return list.toArray(new String[list.size()]); //convert here since no point converting over and over
	}
}
