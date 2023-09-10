package assembler;

public class CWordParser {
	public String text;
	public String lowercaseText;
	public int pos;
	public int prevpos;
	int len;
	
	public void start(String text) {
		pos=0;
		this.text = text;
		this.lowercaseText = text.toLowerCase();
		len = text.length();
		
	}
	
	public String getWord() {
		String word="";
		prevpos = pos;
		char c=0;
		if (pos >= len) return null;
		while (pos < len) {
			c = text.charAt(pos++);
			if (!Character.isWhitespace(c)) break;
		}
		if (c == 0) return null;
		word += c;
		if (c == '"') {
			while (pos < len) {
				c = text.charAt(pos++);
				word += c;
				if (c == '"') break;
			}
		} else {
			while (pos < len) {
				c = text.charAt(pos++);
				if (Character.isWhitespace(c)) break;
				word += c;
			}
		}
		
		return word;
	}

	public int findignorecase(String str) {
		if (pos != 0) pos++;
		pos = lowercaseText.indexOf(str, pos);
		return pos;
	}

	public int find(String str) {
		if (pos != 0) pos++;
		pos = text.indexOf(str, pos);
		return pos;
	}

	public boolean hasData() {
		return pos < len;
	}

}
