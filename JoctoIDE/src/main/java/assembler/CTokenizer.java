package assembler;

import java.util.TreeMap;
import java.util.concurrent.Flow.Subscriber;

public class CTokenizer {
	String mInput;
	int mPos;
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

	public String toString() {
		try {
			int start = mPos - 20;
			int end = mPos + 20;
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
		return getToken(token, true);
	}
	public boolean getToken(CToken token, boolean replaceTokens) {
		mLastLine = mLine;
		token.token = Token.none;
		if (mUngetToken != null) {
			token.copyFrom(mUngetToken);
			mUngetToken = null;
			return true;
		}
		if (mPos >= mEnd - 1)
			return false;
		char c = 0;
		int nibble;
		StringBuilder sb;
		char c1, c2;
		token.line = mLine+mBaseline;

		token.posinline = mPosInLine;
		while (mPos < mEnd) {
			c = nextChar();
			if (!Character.isSpace(c))
				break;
		}
		token.pos = mPos;
		switch (c) {
		case '\n':
			token.token = Token.newline;
			return true;
		case '#':
			token.literal = skipToEndOfLine();
			token.token = Token.comment;
			return true;
		case ';':
			token.literal = skipToEndOfLine();
			token.token = Token.comment;
			if (token.literal.length() == 0 && modeOcto) 
				token.token = Token.rts;
			return true;
		case ',':
			token.token = Token.comma;
			return true;
		case '\'':
			if (mPos + 1 < mEnd) {
				c1 = mInput.charAt(mPos + 1);
				c2 = mInput.charAt(mPos + 2);
				if (c2 == '\'') {
					token.cliteral = c1;
					token.token = Token.octochar;
					return true;
				}
				token.cliteral = c;
				token.token = Token.octochar;
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
					return true;
				}
			}

			if (!(c == ':' && Character.isLetter(c2))) {

				if (isMathChar(c)) {

					while (mPos < mEnd) {
						if (c == ')' || c == '(') {

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
				if (c == '#' || c == ';' || c == ',') {
					mPos--;
					break;
				}
				sb.append(c);

			}
			token.literal = replace(sb.toString().trim());
			if (replaceTokens) {
				String strAlias = mMapAlias.get(token.literal);
				if (strAlias != null)
					token.literal = strAlias;
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
					}
				}
			}
			if (token.token != null)
				return true;
		}
		return false;
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
		return getSourceLine(mLastLine-1);
	}

	public void addAlias(String strA, String literal) {
		mMapAlias.put(strA, literal);
		
	}

	public void setAlias(String key, String value) {
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

}
