package disass;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.ranges.Range;

import assembler.CAlias;
import assembler.Token;

public class CC8Label {
	
	class Range {
		int start;
		int end;
	}
	
	static int labelNr=0;
	public C8LabelType 			mLabelType = C8LabelType.DATA;
	public int 					mNr=-1;
	public String				mName = null;
	public String				mAlphabet = null;
	public int					mTarget=0;
	public Double				mValue=null;
	public int					mEnd=0;
	public int 					mItemsPerRow=1;
	public int					mReferences=0;					// Add 1 each time the label has been used (i := xxx, jump or call)
	public String				mMacro=null;
	public ArrayList<String>	mVariables=null;
	public TreeMap<String, CAlias> mAliase=null;
	public ArrayList<Range>		mValidInRange = null;
	public TreeMap<String, CC8Label>
								mMapSubFunctions = null;
	
	private Range 				mCurrentRange = null;
	public String               mRegister = null;				// for alias: the register
	public String               mPackage=null;					// inside a include, if a package is defined, the label is package private
	public boolean              mSkipCompiling = false;			// if the compiler found out, that a function is unused, it skips it from compiling
	
	public void addVar(String var) {
		if (mVariables == null) mVariables = new ArrayList<>();
		if (regFromVar(var) == -1) {
			mVariables.add(var);
		}
			
	}
	
	public void startRange(int pc) {
		saveRange();
		mCurrentRange = new Range();
		mCurrentRange.start = pc;
	}
	
	public void endRange(int pc) {
		if (mCurrentRange != null) {
			mCurrentRange.end = pc;
			saveRange();
		}
	}
	
	private void saveRange() {
		if (mCurrentRange != null) {
			if (mCurrentRange.start != 0 && mCurrentRange.end != 0) {
				if (mValidInRange == null) mValidInRange = new ArrayList<>();
				mValidInRange.add(mCurrentRange);
				mCurrentRange = null;
			}
		}

	}
	
	
	
	public int regFromVar(String var) {
		if (mVariables == null) return -1;
		int r = -1;
		for (int i=0;i<mVariables.size();i++) {
			if (mVariables.get(i).compareTo(var) == 0) {
				r = i;
				break;
			}
		}
		return r;
			
	}
	
	public Token TokenForReg(int reg) {
		switch(reg) {
			case 0: return Token.v0; 
			case 1: return Token.v1; 
			case 2: return Token.v2; 
			case 3: return Token.v3; 
			case 4: return Token.v4; 
			case 5: return Token.v5; 
			case 6: return Token.v6; 
			case 7: return Token.v7; 
			case 8: return Token.v8; 
			case 9: return Token.v9; 
			case 10: return Token.va; 
			case 11: return Token.vb; 
			case 12: return Token.vc; 
			case 13: return Token.vd; 
			case 14: return Token.ve; 
			case 15: return Token.vf;
			default: return Token.none;
		}
	}
	
	public CC8Label() {
		mNr = labelNr++;
	}
	
	CC8Label(C8LabelType type) {
		mNr = labelNr++;
		mLabelType = type;
		if (mLabelType == C8LabelType.SKIP) {
			System.out.println("debug");
		}
	}
	
	public String toLabelString() {
		return toString() + ":";
	}
	public String toString() {
		if (mName != null) return mName;
		switch(mLabelType) {
			case CODE:	return String.format("label%04d", mNr);
			case DATA:	return String.format("data%04d", mNr);
			case FUNCTION: return String.format("func%04d", mNr);
			case SKIP:	
				return String.format("skip%04d", mNr);
			default:
				return String.format("undef%04d", mNr);
		}
	}
	
	String save() {
		return String.format("%d,%d,%s,%s,%s,%s,%d",
				mTarget,
				mNr, 
				mLabelType.toString(),
				mItemsPerRow, 
				str(mName), 
				str(mAlphabet), 
				mEnd);
	}
	
	void store(String text, int field) {
		int i;
		switch(field) {
			case 0: mTarget = Integer.parseInt(text);	break;
			case 1: mNr = Integer.parseInt(text); break;
			case 2: mLabelType = mLabelType.valueOf(text);
					break;
				
			case 3: mItemsPerRow = Integer.parseInt(text); break;
			case 4: mName = text.compareTo("null") == 0 ? null : text; break;
			case 5: mAlphabet = text.compareTo("null") == 0 ? null : text; break;
			case 6: mEnd = Integer.parseInt(text);
		}
	}
	


	void load(String str) {
		try {
		str += ",";
		StringBuilder sb = new StringBuilder();
		int len = str.length();
		int pos=0;
		int field=0;
		String text;
		char c;
		while (pos < len) {
			c = str.charAt(pos);
			pos++;
			if (c == '"') {
				while (pos < len) {
					c = str.charAt(pos);
					pos++;
					if (c == '"') break;
					sb.append(c);
				}
				continue;
			}
			if (c == ',') {
				try {
					store(sb.toString().trim(), field);
					sb = new StringBuilder();
					field++;
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
				continue;
			}
			sb.append(c);
		}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	String str(String str) {
		if (str == null) return null;
		return "\""+str+"\"";
	}

	public void addSubFunction(CC8Label label) {
		if (mMapSubFunctions == null) mMapSubFunctions = new TreeMap<>();
		mMapSubFunctions.put(label.mName, label);
		
	}
	
}
