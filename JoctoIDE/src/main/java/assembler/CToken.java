package assembler;

import java.util.TreeMap;

public class CToken {
	public Token token;
	public char cliteral;
	public int iliteral;
	public String literal;
	public int line;
	public int register; // 0..15 after expr
	public int posinline;
	public int pos;
	String replacement=null;
	int    replacementReg=0;
	public CToken copy() {
		CToken r = new CToken();
		r.token = token;
		r.cliteral = cliteral;
		r.iliteral = iliteral;
		r.literal = literal;
		r.line = line;
		r.posinline = posinline;
		r.register = register;
		return r;
	}
	public void copyFrom(CToken r) {
		token = r.token;
		cliteral = r.cliteral;
		iliteral = r.iliteral;
		literal = r.literal;
		line = r.line;
		posinline = r.posinline;
		register = r.register;
		
	}
	
	public void addReplacement(int register, String alias) {
		replacement=alias;
		replacementReg = register;
	}
	
	public String toString() {
		String s = token.toString();
		if (token.literal != null) s += " "+literal;
		return s;
	}
	public int length() {
		if (token == Token.label) return literal.length()+2;
		if (token == Token.comment) return literal.length()+1;
		return literal.length();

	}
	public static boolean isBegin(Token token) {
		if (token == Token.curlybracketopen || token == Token.octobegin) return true;
		return false;
	}
	
	public static boolean isEnd(Token token) {
		if (token == Token.curlybracketclose || token == Token.octoend) return true;
		return false;
	}

}
