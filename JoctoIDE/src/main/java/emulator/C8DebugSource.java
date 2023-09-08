package emulator;

import java.util.ArrayList;
import java.util.TreeMap;

public class C8DebugSource extends ArrayList<C8DebugSourceLine> {
	TreeMap<Integer, Integer> mAddressMap = new TreeMap<>();				// Stores maps op code address to array index
	
	public void addSourceLine(int address, String line) {
		C8DebugSourceLine sline = new C8DebugSourceLine(address, line);
		add(sline);
		Integer iline = mAddressMap.get(address);
		if (iline == null)
			mAddressMap.put(address, size());
	}

	public int getLineForCode(int pc) {
		Integer line = mAddressMap.get(pc);
		return line == null ? -1 : line.intValue();
	}

	public int getPCForLine(int line) {
		if (line >= 0 && line < size()) {
			C8DebugSourceLine obj = get(line);
			return obj.org;
		}
		return 0;
	}
}
