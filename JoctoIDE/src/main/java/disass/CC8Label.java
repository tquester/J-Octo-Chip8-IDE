package disass;


public class CC8Label {
	static int labelNr=0;
	public C8LabelType 		mLabelType = C8LabelType.NONE;
	public int 				mNr;
	public String			mName = null;
	public String			mAlphabet = null;
	public int				mTarget=0;
	public Double			mValue=null;
	public int				mEnd=0;
	public int 				mItemsPerRow=1;
	public String			mMacro=null;
	
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
}
