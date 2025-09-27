package engivdf;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.TreeMap;

public class VDFParser {
	public VDFParser() {
		
	}
	
	public void parse(String vdfString) {
		//assume vdfs are static for now since things can reparse to make them more flexible (like for pops)
		TreeMap<String, Object[]> root = new TreeMap<String, Object[]>();
		Deque<TreeMap<String, Object[]>> nodeStack = new ArrayDeque<TreeMap<String, Object[]>>();
		
		char[] charArray = vdfString.toCharArray();
		StringBuilder currentString = new StringBuilder();
		
		
		for(char c : charArray) {
			switch(c) {
				case '"':
					
				case '{':
				
				case '}':
				
				case '[':
				
				case ']':
				
				case '\\':
				
				default:
					if(Character.isWhitespace(c)) {
						
					}
					else {
						currentString.append(c);
					}
					break;
			}
		}
	}
}
