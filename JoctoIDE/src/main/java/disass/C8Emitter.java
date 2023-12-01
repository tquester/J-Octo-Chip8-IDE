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
	public boolean showAlias = false;
	public boolean commenAlias = false;
	public boolean replaceAlias = false;
	public boolean skipData = false;

	public C8DebugSource mDebugSource=null;
	public C8DebugSource mSourceHints=null;


	public void startSourcecode(int pos) {
		
	}

	public void writeSourcecode() {
		
	}
	public void createDebugSource() {
		mDebugSource = new C8DebugSource();
			
	}
	
	public C8DebugSource getDebugSource(C8DebugSource sourceHints) {
		
		if (sourceHints != null) {
			for (C8DebugSourceLine hint : sourceHints) {
				for (C8DebugSourceLine disass: mDebugSource) {
					if (disass.org == hint.org) {
						disass.line = String.format("%-85s # %s", disass.line.replaceAll("\t"," ").trim(), hint.line.trim());
					}
				}
			}
		}
		return mDebugSource;
	}

	public void emitLine(int address, String text) {
		//System.out.println(String.format("%04x %s", address, text));
		if (mSB != null) mSB.append(text+"\n");
		if (mDebugSource != null) mDebugSource.addSourceLine(address, text);
		
	}

	TreeMap<Integer, CC8Label> mLabels = new TreeMap<>();
	public TreeSet<Integer> mSetVisited;
	public abstract int emitOpcode(byte[] code, int pos);
	public abstract int emitOpcode(boolean usecomments, byte[] code, int pos);
	
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
	
	public String disassLine(boolean usecomments, byte[] code, int pc) {
		mSB = new StringBuilder();
		emitOpcode(usecomments, code, pc);
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
