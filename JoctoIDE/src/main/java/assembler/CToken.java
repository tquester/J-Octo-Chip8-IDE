package assembler;

public class CToken {
	public Token token;
	public char cliteral;
	public int iliteral;
	public String literal;
	public int line;
	public int register; // 0..15 after expr
	public int posinline;
	public int pos;
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
	
	public String toString() {
		String s = token.toString();
		if (token.literal != null) s += " "+literal;
		return s;
	}

}
