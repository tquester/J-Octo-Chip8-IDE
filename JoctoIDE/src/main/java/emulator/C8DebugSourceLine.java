package emulator;

public class C8DebugSourceLine {
	public C8DebugSourceLine(int address, String line) {
		org = address;
		this.line = line;
	}
	public int org;
	public String line;
}
