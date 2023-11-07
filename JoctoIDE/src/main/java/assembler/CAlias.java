package assembler;

public class CAlias {
	public String mName;
	public int    mRegister;
	public String struct=null;
	public String mFunctionName=null;
	String getRegister() {
		return String.format("v%s", Integer.toString(mRegister,16));
	}
	public String getAliasName() {
		if (struct != null) return String.format("%s.%s", struct, mName);
		if (mFunctionName != null) return String.format("%s_%s", mFunctionName,mName);
		return mName;
	}

}
