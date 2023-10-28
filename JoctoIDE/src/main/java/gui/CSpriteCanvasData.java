package gui;

public interface CSpriteCanvasData {
		abstract public int getSpriteByte(int bytepos);
		abstract public int getSpriteWord(int bytepos);
		abstract public void updateText();
		abstract public void setSpriteByte(int bytepos, int newbyte);
		abstract public void setSpriteWord(int bytepos, int word);
}
