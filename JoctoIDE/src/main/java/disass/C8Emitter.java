package disass;

import java.util.TreeMap;
import java.util.TreeSet;

import emulator.C8DebugSource;
import emulator.C8DebugSourceLine;



public abstract class C8Emitter {
	
	C8LabelType mLastType;
	private int miRow;
	private int miRows;
	private StringBuilder mSB = new StringBuilder();
	C8DebugSource		  mDebugSource = null;
	
	public void createDebugSource() {
		mDebugSource = new C8DebugSource();
	}
	
	public C8DebugSource getDebugSource() {
		return mDebugSource;
	}

	public void emitLine(int address, String text) {
		if (mSB != null) mSB.append(text+"\n");
		if (mDebugSource != null) mDebugSource.addSourceLine(address, text);
		
	}

	TreeMap<Integer, CC8Label> mLabels = new TreeMap<>();
	public TreeSet<Integer> mSetVisited;
	public abstract int emitOpcode(byte[] code, int pos);
	public boolean wantsSkipLabels() {
		return false;
	}
	protected void clear() {
		mSB = new StringBuilder();
	}
	
	public void setStringBuilder(StringBuilder sb) {
		mSB = sb;
		
	}

	protected abstract int emitdb(byte[] chip8Memory, int pc, CC8Label label);
	public String getText() {
		return mSB.toString();
	}
	
	public String disassLine(byte[] code, int pc) {
		mSB = new StringBuilder();
		emitOpcode(code, pc);
		return mSB.toString();
	}
	
	
	
	void checkLineSpace(CC8Label label) {
		if (mLastType != label.mLabelType) {
			mLastType = label.mLabelType;
			miRow = 0;
			miRows = label.mItemsPerRow;
		} else {
			miRow++;
			if (miRow >= miRows) {
				miRow = 0;
				mSB.append("\n");
			}
		}
	}

}
