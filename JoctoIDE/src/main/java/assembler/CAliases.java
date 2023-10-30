package assembler;

import java.util.TreeMap;

public class CAliases extends TreeMap<String, CAlias> {

	public void addAlias(String literal, int register) {
		CAlias alias = new CAlias();
		alias.mRegister = register;
		alias.mName = literal;
		put(literal, alias);
		
	}

	public void addAlias(String structName, String variableName, int register) {
		CAlias alias = new CAlias();
		alias.struct = structName;
		alias.mRegister = register;
		alias.mName = variableName;
		put(variableName, alias);
		
	}

}
