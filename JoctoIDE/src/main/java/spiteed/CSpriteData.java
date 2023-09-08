package spiteed;

import assembler.CToken;

public class CSpriteData extends CBinaryData {

	
	public CSpriteData(CToken token) {
		line = token.line;
		posinline = token.posinline;
	}
	public String text;
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
	public boolean tiles;
	public String toString() {
		return String.format("line %d",line);
	}

}
