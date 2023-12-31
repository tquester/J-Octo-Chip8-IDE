package assembler;

import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.Flow.Subscriber;

import org.eclipse.swt.graphics.Point;

import disass.C8LabelType;
import disass.CC8Label;

public class CTokenizer {
	String mInput;
	public int mPos;
	int mEnd;
	public boolean modeOcto = true;
	public int mPosInLine, mLine;
	TreeMap<String, Token> mMapTokens = new TreeMap<>();
	CTokens mTokens = new CTokens();
	private CToken mUngetToken;
	TreeMap<String, String> mMapReplace=null;
	String[] sourceLines;
	private int mLastLine;
	TreeMap<String, String> mMapAlias = new TreeMap<>();
	public int mBaseline=0;
	private Object mPrevPos;
	public boolean deliverWhite=false;
	private Stack<CC8Label> mStackStruct = new Stack<>();
	public Stack<CAliases> mStackAliases = new Stack<>();
	public String mFilename = null;
	public String mHint=null;
	public String mPackage;
	public boolean mPublic;
	

	public String toString() {
		try {
			int start = mPos - 40;
			int end = mPos + 50;
			if (start < 0)
				start = 0;
			if (end > mEnd)
				end = mEnd;
			return mInput.substring(start, mPos) + "|" + mInput.substring(mPos, end);
		} catch (Exception e) {
			return e.toString();
		}

	}

	public String getSourceLine(int line) {
		if (line >= 0 && line < sourceLines.length) return sourceLines[line];
		return "";
	}
	public void start(String text) {
		int pos;


		mInput = text;
		sourceLines = text.split("\n");
		mPos = 0;
		mEnd = text.length();
		mPosInLine = 1;
		mLine = 1;
	}

	char nextChar() {
		char c = mInput.charAt(mPos++);
		switch (c) {
		case '\n':
			mLine++;
			mPosInLine = 1;
			break;
		case '\r':
			break;
		default:
			mPosInLine++;

		}
		return c;

	}

	public boolean getToken(CToken token) {
		boolean r = getToken(token,true);
		return r;
	}
	public boolean getToken(CToken token, boolean replaceTokens) {
		token.replacement = null;
		mLastLine = mLine;
		mPrevPos = mPos;
		token.token = Token.none;
		if (mUngetToken != null) {
			token.copyFrom(mUngetToken);
			mUngetToken = null;
			myassert(token.token != null);
			return true;
		}
		if (mPos >= mEnd - 1) {
			return false;
		}
		char c = 0;
		int nibble;
		StringBuilder sb;
		char c1, c2;
		token.line = mLine+mBaseline;

		token.posinline = mPosInLine;
		String white="";
		while (mPos < mEnd) {
			c = nextChar();
			
			if (c == '\n') {
				token.token = Token.newline;
				myassert(token.token != null);
				return true;				
			}
			
			if (!Character.isSpace(c))
				break;
			white += c;
		}
		if (deliverWhite && white.length() > 0) {
			mPos--;
			token.literal = white;
			token.token = Token.whitespace;
			return true;
		}
		token.pos = mPos;
		switch (c) {
		case '\n':
			token.token = Token.newline;
			myassert(token.token != null);
			return true;
		case '[':
			token.token = Token.arrayopen;
			return true;
		case ']':
			token.token = Token.arraytclose;
			return true;
		case '.':
			token.token = Token.dot;
			return true;
		case '#':
			token.literal = skipToEndOfLine();
			token.token = Token.comment;
			myassert(token.token != null);
			return true;
		case ';':
			token.literal = skipToEndOfLine();
			token.token = Token.comment;
			if (token.literal.length() == 0 && modeOcto) 
				token.token = Token.rts;
			myassert(token.token != null);
			return true;
		case ',':
			token.token = Token.comma;
			myassert(token.token != null);
			return true;
		case '\'':
			if (mPos + 1 < mEnd) {
				c1 = mInput.charAt(mPos + 1);
				c2 = mInput.charAt(mPos + 2);
				if (c2 == '\'') {
					token.cliteral = c1;
					token.token = Token.octochar;
					myassert(token.token != null);
					return true;
				}
				token.cliteral = c;
				token.token = Token.octochar;
				myassert(token.token != null);
				return true;
			}
		case '"':
			sb = new StringBuilder();
			while (mPos < mEnd) {
				c = nextChar();
				if (c == '"')
					break;
				sb.append(c);
			}
			token.token = Token.string;
			token.literal = sb.toString();
			myassert(token.token != null);
			return true;
		default:
			c2 = 0;
			try {
			if (mPos+1 < mInput.length())
				c2 = mInput.charAt(mPos);
			} catch(Exception e) {
				e.printStackTrace();
			}

			sb = new StringBuilder();
			sb.append(c);
			if (c == ':' && mPos < mEnd + 2) {
				if (mInput.charAt(mPos) == ' ' && Character.isLetter(mInput.charAt(mPos + 1))) {
					mPos++;
					token.token = Token.label;
					sb = new StringBuilder();
					while (mPos < mEnd) {
						c = nextChar();
						if (Character.isWhitespace(c))
							break;
						sb.append(c);
					}
					token.literal = replace(sb.toString());
					myassert(token.token != null);
					return true;
				}
			}

			if (!(c == ':' && Character.isLetter(c2))) {

				if (isMathChar(c)) {

					while (mPos < mEnd) {
						if (c == ')' || c == '(' || c == '[' || c == ']') {

							break;
						}
						c = nextChar();
			
							if (!isMathChar(c)) {
								mPos--;
								break;
							}
						sb.append(c);
					}
					token.literal = replace(sb.toString());
					token.token = mTokens.getToken(token.literal);
					return true;
				}
			}

			while (mPos < mEnd) {
				c = nextChar();
				if (Character.isWhitespace(c))
					break;
				if (c != ':' && c != '-' && isMathChar(c)) {
					mPos--;
					break;
				}
				if (c == '#' || c == ';' || c == ',' || c == '.' || c== '[' || c == ']') {
					mPos--;
					break;
				}
				sb.append(c);

			}
			if (c == '\n') 
				mPos--;
			token.literal = replace(sb.toString().trim());
			
			if (findStructSymbol(token)) {
				return true;
			}
			if (replaceTokens) {
				String strAlias = mMapAlias.get(token.literal.toLowerCase());
				if (strAlias != null) {
					int regnr = regNr(strAlias);
					if (regnr != -1) {
						token.addReplacement(regnr, token.literal);
					}
					token.literal = strAlias;
					
				}
			}
			token.token = mTokens.getToken(token.literal.toLowerCase());
			if (token.token == null) {
				if (token.literal.endsWith(":")) {
					token.literal = token.literal.substring(0, token.literal.length() - 1);
					token.token = Token.label;
				} else {
					Integer number = parseNumber(token.literal);
					if (number != null) {
						token.token = Token.number;
						token.iliteral = number.intValue();
					} else {
						if (token.literal.length() > 0)
							token.token = Token.literal;
						else {
							token.token = Token.none;
							return false;
						}
					}
				}
			}
			if (token.token != null)
				return true;
		}
		myassert(token.token != null);
		return false;
	}

	private int regNr(String literal) {
		int r = -1;
		literal = literal.toLowerCase();
		if (literal.compareTo("v0") == 0)  r = 0;
		else if (literal.compareTo("v1") == 0)  r = 1;
		else if (literal.compareTo("v2") == 0)  r = 2;
		else if (literal.compareTo("v3") == 0)  r = 3;
		else if (literal.compareTo("v4") == 0)  r = 4;
		else if (literal.compareTo("v5") == 0)  r = 5;
		else if (literal.compareTo("v6") == 0)  r = 6;
		else if (literal.compareTo("v7") == 0)  r = 7;
		else if (literal.compareTo("v8") == 0)  r = 8;
		else if (literal.compareTo("v9") == 0)  r = 9;
		else if (literal.compareTo("va") == 0)  r = 10;
		else if (literal.compareTo("vb") == 0)  r = 11;
		else if (literal.compareTo("vc") == 0)  r = 12;
		else if (literal.compareTo("vd") == 0)  r = 13;
		else if (literal.compareTo("ve") == 0)  r = 14;
		else if (literal.compareTo("vf") == 0)  r = 15;
		return r;
	}

	private void myassert(boolean b) {
		if (false) {
			System.out.println("stop");
		}
		
	}

	boolean isMathChar(char c) {
		switch (c) {
		case '=':
		case '>':
		case '<':
		case '!':
		case '(':
		case ')':
		case ':':
		case '+':
		case '-':
		case '/':
		case '*':
		case '^':
		case '|':
		case '%':
		case '&':
			return true;
		default:
			return false;
		}
	}

	private Integer parseNumber(String literal) {
		Integer r = null;
		try {
			if (literal.startsWith("$"))
				r = Integer.parseInt(literal.substring(1), 16);
			else if (literal.startsWith("%"))
				r = Integer.parseInt(literal.substring(1), 2);
			else if (literal.startsWith("0x"))
				r = Integer.parseInt(literal.substring(2), 16);
			else if (literal.startsWith("0b"))
				r = Integer.parseInt(literal.substring(2), 2);
			else
				r = Integer.parseInt(literal);
		} catch (Exception e) {
		}
		return r;
	}

	private String skipToEndOfLine() {
		char c;
		StringBuilder sb = new StringBuilder();
		while (mPos < mEnd) {
			c = nextChar();
			if (c == '\n') {
				if (mPos < mEnd) {
					if (mInput.charAt(mPos) == '\r')
						mPos++;
				}
				break;
			}
			if (c != '\r')
				sb.append(c);
		}
		return sb.toString();

	}

	private int readNibble(char c) {
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		if (c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		return -1;
	}

	public boolean hasData() {
		return mPos < mEnd - 1;
	}

	public void ungetToken(CToken token) {
		mUngetToken = token.copy();

	}
	
	private String replace(String str) {
		if (mMapReplace == null) return str;
		String other = mMapReplace.get(str);
		return other != null ? other : str;
	}

	public void replace(String string, String literal) {
		if (mMapReplace == null)
			mMapReplace = new TreeMap<String, String>();
		mMapReplace.put(string, literal);
		// TODO Auto-generated method stub
		
	}

	public String getCurrentLine() {
		int start = mPos-1;
		int end = mPos;
		char c;
		while (start > 0) {
			c = mInput.charAt(start);
			if (c == '\n') {
				start ++;
				break;
			}
			start--;
		}
		while (end < mInput.length()) {
			c = mInput.charAt(end);
			if (c == '\n') {
				end--;
				break;
			}
			end++;
		}
		if (start < end) {
			return mInput.substring(start, end);
		} else 
			return "";
		
	}

	public void addAlias(String strA, String literal) {
		mMapAlias.put(strA, literal);
		
	}

	public void setAlias(String key, String value) {
		key = key.toLowerCase();
		String test = mMapAlias.get(key);
		if (test != null) mMapAlias.remove(key);
		mMapAlias.put(key, value);
		
	}

	public boolean hasUngetToken() {
		return mUngetToken != null;
	}

	public void deleteAllAlias() {
		mMapAlias.clear();
		
	}
	
	boolean findStructSymbol(CToken token) {
		if (mStackStruct.size() != 0) {
			for (int i = mStackStruct.size()-1;i>=0;i--) {
				CC8Label structlabel = mStackStruct.get(i);
				int reg = structlabel.regFromVar(token.literal);
				if (reg != -1) {
					token.addReplacement(reg, String.format("%s.%s", structlabel.mName,token.literal));
					token.token = structlabel.TokenForReg(reg);
					token.literal = token.token.toString();
					return true;
				}
			}
		}
		if (mStackAliases.size() != 0) {
			for (int i = mStackAliases.size()-1;i>=0;i--) {
				CAliases aliases = mStackAliases.get(i);
				CAlias alias = aliases.get(token.literal);
				if (alias != null) {
					if (alias.struct != null)
						token.addReplacement(alias.mRegister, String.format("%s.%s", alias.struct,token.literal));
					else if (alias.mFunctionName != null) {
						token.addReplacement(alias.mRegister, String.format("%s_%s", alias.mFunctionName, token.literal));
					}
						
					token.token = TokenForReg(alias.mRegister);
					token.literal = token.token.toString();
					return true;
					
				}
			}
			
		}
		return false;
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


	public void pushStruct(CC8Label structLabel) {
		mStackStruct.push(structLabel);
		
	}

	public void popStruct() {
		mStackStruct.pop();
		
	}

	public Point getLineFromPos() {
		int line=1;
		int posInLine=1;
		int size=mInput.length();
		int i;
		for (i=0;i<mPos;i++) {
			char c = mInput.charAt(i);
			if (c == '\n') {
				line++;
				posInLine=1;
			} else
				posInLine++;
			
		}
		return new Point(posInLine, line);
		
	}

}
