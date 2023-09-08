package spiteed;

public class CBinaryData {
	StringBuilder sb = new StringBuilder();
	int line;
	int posinline;
	int bytes=0;
	public boolean dirty = false;
	public int width=8;
	public int height=8;
	void addByte(int data) {
		sb.append(String.format("0x%02x ",data));
		bytes++;
		if (bytes == height) {
			bytes=0;
			sb.append("\n");
		}
	}
	
	void addLabel(String label) {
		sb.append(String.format("\n: %s\n", label));
	}
}
