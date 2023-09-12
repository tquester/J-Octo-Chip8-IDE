package assembler;

import java.util.ArrayList;


public class CDebugEntry {	
	public static class CDebugElem {
		public String text = null;
		public int register = -1;
		public int memory = -1;			// -1 = kein, -2 = i
		public int memorySize = -1;
	}

	public int mPc;
	public boolean mIsBreakpoint;
	public ArrayList<CDebugElem> mLog = null;
	public void addElem(CDebugElem elem) {
		if (mLog == null) mLog = new ArrayList<CDebugElem>();
		mLog.add(elem);
		
	}
}
