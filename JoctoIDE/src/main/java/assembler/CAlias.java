package assembler;

public class CAlias {
	public String mName;
	public int    mRegister;
	public String struct=null;
	String getRegister() {
		return String.format("v%s", Integer.toString(mRegister,16));
	}

}