package spiteed;

import java.util.ArrayList;

import assembler.CToken;

public class CSpriteData extends CBinaryData {
	public String text;
	public String name = null;
	public boolean tiles;
	public int w,h;

	
	public CSpriteData(CToken token) {
		line = token.line;
		posinline = token.posinline;
	}
	public String getText() {
		if (sb != null) {
			text = sb.toString();
			sb = null;
		} 
		return text;
	}
	public void setText(String text) {
		this.text = text;
		this.dirty = true;
	}
	public String toString() {
		if (name != null) return name;
		return String.format("line %d",line);
	}
	
	private int readDez(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 10);
 		} catch(Exception e) {
 			
 		}
		return r;
	}

	private int readHex(String literal) {
		int r =0;
		try {
			r = Integer.parseInt(literal, 16);
 		} catch(Exception e) {
 			
 		}
		return r;
	}
	
	public int[] parse(String text) {
		String literal;
		int result[] = null;
		if (text == null) return result;
		try {
			ArrayList<Integer> data = new ArrayList<>();
			char c;
			int pos = 0;
			int len = text.length();
			while (pos < len) {
				c = text.charAt(pos++);
				if (c == '\n' || c == '\r') continue;
				literal = "";
				while (pos < len) {
					if (!Character.isWhitespace(c)) break;
					c = text.charAt(pos++);
				}
				if (c == '#' || c == ';' || c == ':')  {
					int c1 = c;
					while (pos < len) {
						c = text.charAt(pos++);
						if (c == '\n' || c == '\r') break;
						literal += c;
					}
					if (c1 == ':') {
					}
				
					continue;
				}
				literal = "";
				while (pos < len) {
					if (Character.isWhitespace(c)) break;
					if (c == '#' || c == ';' || c == ':') break;
					literal += c;
					c = text.charAt(pos++);
				}
				int newbyte=0;
				if (literal.length() > 0) {
					literal = literal.toLowerCase();
					if (literal.charAt(0) == '$') {
						newbyte = readHex(literal.substring(1));
					} else if (literal.startsWith("0x")) {
						newbyte = readHex(literal.substring(2));
					} else
						newbyte = readDez(literal);
					data.add(newbyte);
				}
			}
			result = new int[data.size()];
			for (int i=0;i<data.size();i++) {
				result[i] = data.get(i).intValue();
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			
		}
		return result;
		
	}


}
