package assembler;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Point;

import assembler.CDebugEntry.CDebugElem;
import disass.C8DisassEmitter;
import disass.C8LabelType;
import disass.CC8Label;
import disass.CC8Label.CC8SubSubFunctionLabel;
import disass.Tools;
import emulator.C8DebugSource;

public class CChip8Assembler {

	public class CMemoryStatistic {
		public String file = "editor";
		public int sizeCode = 0;
		public int sizeData = 0;
	}

	public TreeMap<String, Integer> mMapFunctionSize = new TreeMap<>();

	CMemoryStatistic mMemoryStatistic = new CMemoryStatistic();
	public ArrayList<CMemoryStatistic> mMemoryStatistics = new ArrayList<>();

	public C8DebugSource mDebugSource = new C8DebugSource();
	C8DisassEmitter mEmitter = new C8DisassEmitter();
	public CDebugEntries mDebugEntries = new CDebugEntries();
	int mLevel = 0;

	StringBuilder mSBErrors = null;

	class CLoopData {
		public CLoopData(int pc) {
			mLoopAdr = pc;
		}

		int id;
		int mLoopAdr = 0;
		int mExitAdr = 0;
		ArrayList<Integer> patchAddresses = new ArrayList<>();
	}

	class CStringMode {
		String name;
		String alphabet;
		String text;
	}

	class CBeginEndData {
		public int patchAdr;
		public int pc;
		public int forRegister = -1;
		public int forStepNr;
		public int forStepReg = -1;
		public int forTargetNr;
		public int forTargetReg = -1;
	}

	class CMacroData {
		String name;
		ArrayList<String> parameters = new ArrayList<>();
		String macro;
	}

	interface UnaryFunction {
		double calc(double par);
	}

	interface BinaryFunction {
		double calc(double par1, double par2);
	}

	TreeMap<Token, UnaryFunction> mMapUnaryFunctions = new TreeMap<>();;
	TreeMap<Token, BinaryFunction> mMapBinaryFunctions = new TreeMap<>();;
	TreeMap<String, Integer> mMapConstants = new TreeMap<>();

	public CChip8Assembler() {
		mMapUnaryFunctions.put(Token.min, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return -par;
			}
		});
		mMapUnaryFunctions.put(Token.tilde, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return ~((int) par);
			}
		});
		mMapUnaryFunctions.put(Token.exclamation, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return par == 0 ? 1 : 0;
			}
		});

		mMapUnaryFunctions.put(Token.sin, new UnaryFunction() {
			@Override
			public double calc(double par) {
				System.out.println(String.format("sin(%f) = %f", par, Math.sin(par)));
				return Math.sin(par);
			}
		});

		mMapUnaryFunctions.put(Token.cos, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.cos(par);
			}
		});

		mMapUnaryFunctions.put(Token.tan, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.tan(par);
			}
		});
		mMapUnaryFunctions.put(Token.exp, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.exp(par);
			}
		});
		mMapUnaryFunctions.put(Token.log, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.log(par);
			}
		});
		mMapUnaryFunctions.put(Token.abs, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.abs(par);
			}
		});
		mMapUnaryFunctions.put(Token.sqrt, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.sqrt(par);
			}
		});
		mMapUnaryFunctions.put(Token.sign, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.signum(par);
			}
		});
		mMapUnaryFunctions.put(Token.ceil, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.ceil(par);
			}
		});
		mMapUnaryFunctions.put(Token.floor, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return Math.floor(par);
			}
		});
		mMapUnaryFunctions.put(Token.atsym, new UnaryFunction() {
			@Override
			public double calc(double par) {
				return pc;
			}
		});

		mMapBinaryFunctions.put(Token.plus, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 + par2;
			}
		});
		mMapBinaryFunctions.put(Token.minus, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 - par2;
			}
		});
		mMapBinaryFunctions.put(Token.mult, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 * par2;
			}
		});
		mMapBinaryFunctions.put(Token.divide, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 / par2;
			}
		});

		mMapBinaryFunctions.put(Token.mod, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 % par2;
			}
		});

		mMapBinaryFunctions.put(Token.and, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return ((int) par1) & ((int) par2);
			}
		});

		mMapBinaryFunctions.put(Token.or, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return ((int) par1) | ((int) par2);
			}
		});

		mMapBinaryFunctions.put(Token.xor, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return ((int) par1) ^ ((int) par2);
			}
		});

		mMapBinaryFunctions.put(Token.shl, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return ((int) par1) << ((int) par2);
			}
		});

		mMapBinaryFunctions.put(Token.shr, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return ((int) par1) >> ((int) par2);
			}
		});

		mMapBinaryFunctions.put(Token.pow, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return Math.pow(par1, par2);
			}
		});

		mMapBinaryFunctions.put(Token.min, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return Math.min(par1, par2);
			}
		});

		mMapBinaryFunctions.put(Token.max, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return Math.max(par1, par2);
			}
		});

		mMapBinaryFunctions.put(Token.smaller, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 < par2 ? 1 : 0;
			}
		});
		mMapBinaryFunctions.put(Token.bigger, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 > par2 ? 1 : 0;
			}
		});
		mMapBinaryFunctions.put(Token.lessequal, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 <= par2 ? 1 : 0;
			}
		});
		mMapBinaryFunctions.put(Token.biggerequal, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 >= par2 ? 1 : 0;
			}
		});
		mMapBinaryFunctions.put(Token.equals, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 == par2 ? 1 : 0;
			}
		});
		mMapBinaryFunctions.put(Token.unequal, new BinaryFunction() {
			@Override
			public double calc(double par1, double par2) {
				return par1 != par2 ? 1 : 0;
			}
		});
		mMapConstants.put("OCTO_KEY_1", 0x01);
		mMapConstants.put("OCTO_KEY_1", 0x02);
		mMapConstants.put("OCTO_KEY_1", 0x03);
		mMapConstants.put("OCTO_KEY_1", 0x0C);

		mMapConstants.put("OCTO_KEY_Q", 0x04);
		mMapConstants.put("OCTO_KEY_W", 0x05);
		mMapConstants.put("OCTO_KEY_E", 0x06);
		mMapConstants.put("OCTO_KEY_R", 0x0D);

		mMapConstants.put("OCTO_KEY_A", 0x07);
		mMapConstants.put("OCTO_KEY_S", 0x08);
		mMapConstants.put("OCTO_KEY_D", 0x09);
		mMapConstants.put("OCTO_KEY_F", 0x0E);

		mMapConstants.put("OCTO_KEY_Z", 0x0A);
		mMapConstants.put("OCTO_KEY_X", 0x00);
		mMapConstants.put("OCTO_KEY_C", 0x0B);
		mMapConstants.put("OCTO_KEY_V", 0x0F);

	}

	Stack<CLoopData> mStackLoopData = new Stack<>();
	Stack<CBeginEndData> mBeginEndStack = new Stack<>();
	ArrayList<Integer> mBeginEndData = new ArrayList<>();
	TreeMap<String, CMacroData> mMapMacros = new TreeMap<>();
	public TreeMap<String, CC8Label> mLabels = new TreeMap<>();

	String mCodeLines[] = null;
	byte mCode[] = new byte[65536];
	int pc = 0x200;
	CToken token = new CToken();
	CTokenizer mTokenizer = new CTokenizer();
	private int mPass;
	private boolean modeOcto = true;
	private boolean mOptAnnotateAllLines = true;
	private boolean mOptIncludeSourceLine = true;
	String mContext = "";
	private boolean mbCodegen = true;
	public String mFolder;
	private CC8Label mFunctionLabel = null;

	public boolean mbError = false;

	private CC8Label mExprLabel;

	private int mExprLabelCount;

	private CC8Label mLastLabel = null;

	private boolean mSecondGo = false;

	void initData() {
		mStackLoopData.clear();
		mBeginEndStack.clear();
		mMapMacros.clear();
		mLabels.clear();

	}

	public String getErrors() {
		return mSBErrors == null ? "" : mSBErrors.toString();
	}

	boolean nextNonWhiteToken(CToken token) {
		boolean r = false;
		while (mTokenizer.hasData()) {
			r = nextToken(token);
			if (token.token == Token.newline || token.token == Token.comment)
				continue;
			break;
		}
		return r;
	}

	boolean nextToken(CToken token) {
		CToken token2 = new CToken();
		boolean r = mTokenizer.getToken(token);
		if (token.token == Token.literal) {
			CC8Label label = getLabel(token.literal);
			if (label != null) {
				if (label.mLabelType == C8LabelType.STRUCT) {
					r = mTokenizer.getToken(token2);
					if (token2.token == Token.dot) {
						r = mTokenizer.getToken(token, false);
						int regnr = label.regFromVar(token.literal);
						if (regnr != -1) {
							token.addReplacement(regnr, String.format("%s.%s", label.mName, token.literal));
						}
						if (regnr == -1) {
							if (token.token == Token.length) {
								token.token = Token.number;
								token.iliteral = label.mVariables.size();
								token.literal = String.format("%d", token.iliteral);
							} else if (token.token == Token.octobyte && label.mVariables != null) {
								token.token = Token.internaldefs;
								token.literal = label.mName;
								token.iliteral = label.mVariables.size();

							} else {
								error("Undefined " + token.literal + " in struct " + label.mName);
							}

						} else {
							token.token = regFromNr(regnr);
						}
					} else
						mTokenizer.ungetToken(token2);
				}
			}
		}
		return r;
	}

	private Token regFromNr(int regnr) {
		switch (regnr) {
		case 0:
			return Token.v0;
		case 1:
			return Token.v1;
		case 2:
			return Token.v2;
		case 3:
			return Token.v3;
		case 4:
			return Token.v4;
		case 5:
			return Token.v5;
		case 6:
			return Token.v6;
		case 7:
			return Token.v7;
		case 8:
			return Token.v8;
		case 9:
			return Token.v9;
		case 10:
			return Token.va;
		case 11:
			return Token.vb;
		case 12:
			return Token.vc;
		case 13:
			return Token.vd;
		case 14:
			return Token.ve;
		case 15:
			return Token.vf;
		}
		return Token.none;
	}

	public void Assemble(String code, String filename) {
		try {

			mbError = false;
			mMemoryStatistic = new CMemoryStatistic();
			mMemoryStatistics = new ArrayList<>();
			mMemoryStatistics.add(mMemoryStatistic);
			mDebugSource = new C8DebugSource();
			mSecondGo = false;
			mTokenizer.start(code);
			pc = 0x200;
			mSBErrors = new StringBuilder();
			mPass = 1;
			System.out.println("Pass 1");
			compileCode(code);
			mPass = 2;

			// mTokenizer.deleteAllAlias();
			System.out.println("Pass 2");
			compileCode(code);

			displayEmptyFunctions();
			// while (true) {
			System.out.println("-----------------------------------");
			cleanupSymbols();
			// }
			displayEmptyFunctions();
//			if (cleanupSymbols()) {
			for (CC8Label lbl : mLabels.values())
				lbl.clearRef();
			mSecondGo = true;
			mDebugSource = new C8DebugSource();
			System.out.println("Pass 2a");
			mPass = 1;
			compileCode(code);
			mPass = 2;
			compileCode(code);
			// } else {
			// if (mSBErrors != null) {
			// mSBErrors.append("Skipping second compile\n");
			// }
			// }

			// removeUnusedFunctionLabels();

			System.out.println(String.format("Length =%d", pc - 0x200));
			if (filename != null) {

				int p = filename.lastIndexOf('.');
				if (p != -1)
					filename = filename.substring(0, p);
				filename += ".ch8";
				File file = new File(filename);
				System.out.println("Writing " + file.getAbsolutePath());
				byte[] code2 = new byte[pc - 0x200 + 1];
				int src = 0x200;
				int tar = 0;
				for (int i = 0x200; i <= pc; i++) {
					code2[tar++] = mCode[src++];
				}
				Tools.writeBinary(code2, filename);
			}
			System.out.println("Finished assembling");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(mTokenizer.toString());
		}
	}

	private void displayEmptyFunctions() {
		System.out.println("******************* Functions will not be compiled");
		for (CC8Label lbl : mLabels.values()) {
			if (lbl.mSkipCompiling)
				System.out.println(String.format("\t%s", lbl.mName));
		}
		System.out.println("********************");
		for (CC8Label lbl : mLabels.values()) {
			if (lbl.mLabelType == C8LabelType.FUNCTION && lbl.getRef() > 0) {
				System.out.println(String.format("\t%s has %d refs", lbl.mName, lbl.getRef()));
				if (lbl.mMapSubFunctions != null) {
					for (CC8SubSubFunctionLabel sub : lbl.mMapSubFunctions.values()) {
						System.out.println(String.format("                -> %s %d", sub.label.mName, sub.count));
					}
				}
				for (CC8Label albl: mLabels.values()) {
					if (albl.mLabelType == C8LabelType.FUNCTION) {
						if (albl.mMapSubFunctions != null) {
							for (CC8SubSubFunctionLabel blbl: albl.mMapSubFunctions.values()) {
								if (blbl.label.mName.compareTo(lbl.mName) == 0) {
									System.out.println(String.format("                <-- %s (%d)", albl.mName, blbl.count));
								}
							}
						}
					}
				}
			}
		}
		System.out.println("********************");

	}

	private void compileCode(String code) {
		System.out.println(String.format("Compiling pass %d", mPass));
		if (mSBErrors != null) {
			mSBErrors.append(String.format("Compiling pass %d\n", mPass));
		}

		pc = 0x200;
		mTokenizer.start(code);
		mTokenizer.mMapAlias.clear();
		while (mTokenizer.hasData()) {
			if (mbError)
				break;
			try {
				assembleLine();
			} catch (Exception e) {
				error(e.getLocalizedMessage());
				e.printStackTrace();
				break;
			}
		}
	}

	private boolean cleanupSymbols() {
		boolean changed = false;

		changed = false;
		for (CC8Label label : mLabels.values()) {
			if (label.mLabelType == C8LabelType.FUNCTION) {
				if (label.getRef() == 0) {
					boolean c = cleanUpSubSymbols(label);
				}
				System.out.println(String.format("Function %s used %d times", label.mName, label.getRef()));
			}
		}
		System.out.println("--------- cleanup -------------");
		changed = false;
		for (CC8Label label : mLabels.values()) {
			if (label.mLabelType == C8LabelType.FUNCTION) {
				if (label.getRef() == 0 && label.mSkipCompiling == false) {
					label.mSkipCompiling = true;
					changed = true;
				}
				System.out.println(String.format("Function %s used %d times", label.mName, label.getRef()));
			}
		}
		return changed;
	}

	private void removeUnusedFunctionLabels() {
		ArrayList<String> removeList = new ArrayList<>();
		for (String key : mLabels.keySet()) {
			CC8Label label = getLabel(key);
			if (label.mLabelType == C8LabelType.FUNCTION) {
				System.out.println(String.format("Label: %s references:%d", label.mName, label.getRef()));
				if (label.getRef() == 0) {
					removeList.add(key);
				}
			}
		}
		for (String key : removeList) {
			mLabels.remove(key);
			System.out.println("Removing label: " + key);
		}

	}

	private boolean cleanUpSubSymbols(CC8Label label) {

		System.out.println(String.format("Cleaning up sub symbol %s ref=%d", label.mName, label.getRef()));
		boolean changed = false;
		if (label.mMapSubFunctions != null) {
			for (CC8SubSubFunctionLabel sublabel : label.mMapSubFunctions.values()) {
				CC8Label sub = sublabel.label;

				if (sub.getRef() > 0) {
					sub.subRef(sublabel.count);
					System.out.println(String.format("  Sub Symbol %s ref=%d", sub.mName, sub.getRef()));
					if (sub.getRef() == 0)
						changed = true;
					boolean c = cleanUpSubSymbols(sub);
					changed |= c;
				}
			}
		}
		return changed;
	}

	private void assembleLine() {
		try {
			while (nextToken(token)) {
				if (token.token == null)
					continue;
//				 System.out.println(mTokenizer.toString());
//				 printToken(token);
				assembleStatement(token);

			}
		} catch (Exception e) {
			e.printStackTrace();
			error(e.getLocalizedMessage());
			System.out.println(mTokenizer.toString());
			throw e;
		}
	}

	private void assembleStatement(CToken token) {

		Token token1, token2, token3;
		CC8Label label;
		String key;
		int reg1;
		int reg2;
		int nr;

		String astr;
		CBeginEndData beginEndData;

		if (mOptAnnotateAllLines) {
			if (token.token != Token.comment && token.token != Token.alias && token.token != Token.macro
					&& token.token != Token.newline)
				writeSourceLine();
		}
		switch (token.token) {
		case label:

			label = getLabel(token.literal);
			if (label == null) {
				label = new CC8Label();
				label.mName = labelName(token.literal);

				if (mTokenizer.mPublic == false)
					label.mPackage = mTokenizer.mPackage;
				mTokenizer.mPublic = false;
				System.out.println(String.format("Label %s = %04x", token.literal, pc));
				mLabels.put(label.mName, label);
			}
			label.mTarget = pc;
			mLastLabel = label;
			break;
		case org:
			expr(token);
			if (token.token == Token.number) {
				pc = token.iliteral;
			} else
				error("Expected: Number");
			break;
		case ld:
			nextToken(token);
			token1 = token.token;
			reg1 = regNr(token);
			expect(Token.comma);
			// nextToken(token);
			expr(token);
			token2 = token.token;
			reg2 = regNr(token);
			if (reg1 != -1 && reg2 != -1) { // 8xy0 LD Vx, Vx
				writeCode(0x08, reg1, reg2, 0x00);
			} else if (reg1 != -1 && token2 == Token.number) { // 6xkk LD Vx, number
				writeCode(0x06, reg1, token.iliteral);
			} else if (reg1 != -1 && token2 == Token.key) { // Fx0A - LD Vx, K
				writeCode(0x0f, reg1, 0x0a);
			} else if (reg1 != -1 && token2 == Token.delay) { // Fx07 - LD Vx, DT
				writeCode(0x0f, reg1, 0x07);
			} else if (token1 == Token.i && token2 == Token.number) { // Annn ld I, number
				writeCode(0x0a, token.iliteral / 256, token.iliteral & 255);
			} else if (token1 == Token.buzz && reg2 != -1) { // Fx18 ld ST, Vx
				writeCode(0x0f, reg2, 0x18);
			} else if (token1 == Token.delay && reg2 != -1) { // Fx15 ld DT, Vx
				writeCode(0x0f, reg2, 0x15);
			} else if (reg1 != -1 && token2 == Token.delay) { // Fx07 LD Vx, DT
				writeCode(0x0f, reg1, 0x07);
			} else if (token1 == Token.iindirect && reg2 != -1) { // Fx55 LD [i], Vx
				writeCode(0x0f, reg2, 0x55);
			} else if (reg1 != -1 && token2 == Token.iindirect) { // Fx65 LD Vx, [i]
				writeCode(0x0f, reg1, 0x65);
			} else if (token1 == Token.i && token2 == Token.number) {
				writeCode(0x0a, token.iliteral);
			} else if (token1 == Token.i && token2 == Token.literal) {
				writeCode(0x0a, labelTarget(token.literal));
				CC8Label clabel = labelFromString(token.literal);
				if (mbCodegen && mPass == 2) {
					clabel.addRef();
				}
			} else
				error("Unknown ld statement");
			break;
		case rts: // 00EE RET
			writeCode(0x00, 0, 0xEE);
			break;
		// 00Cn - SCD nibble
		// 00FB - SCR
		// 00FC - SCL
		// 00FD - EXIT
		// 00FE - LOW
		// 00FF - HIGH
		// 00E0 - CLS
		// 00EE - RET
		case exit:
			writeCode(0x00, 0, 0xFD);
			break;

		case scl:
			writeCode(0x00, 0, 0xFC);
			break;
		case scr:
			writeCode(0x00, 0, 0xFB);
			break;
		case scd:
			// nextToken(token);
			expr(token);
			if (token.token != Token.number) {
				error("Expected number");
				break;
			}
			writeCode(0x00, 0, 0xc0 + token.iliteral);
			break;

		case hires:
			writeCode(0x00, 0, 0xFF);
			break;
		case lowres:
			writeCode(0x00, 0, 0xFE);
			break;

		case cls: // 00E0 CLS
			writeCode(0x00, 0, 0xE0);
			break;
		case jp:
			expr(token);
			if (token.token == Token.number) {
				writeCode(0x01, token.iliteral); // 1nnn JP
			} else if (token.token == Token.literal) {
				writeCode(0x01, labelTarget(token.literal)); // 1nnn JP
				CC8Label clabel = labelFromString(token.literal);
				if (mFunctionLabel != null) {
					if (clabel.mLabelType == C8LabelType.FUNCTION && mPass == 1 && mbCodegen) {
						mFunctionLabel.addSubFunction(clabel);
					}
				}
			} else if (token.token == Token.v0) {
				expect(Token.comma);
				nextToken(token);
				if (token.token == Token.literal) {
					writeCode(0x0b, labelTarget(token.literal)); // bnnn JP nnn+V0
					CC8Label clabel = labelFromString(token.literal);
					if (mPass == 2 && mbCodegen)
						clabel.addRef();
					if (mFunctionLabel != null) {
						if (clabel.mLabelType == C8LabelType.FUNCTION && mPass == 1 && mbCodegen) {
							mFunctionLabel.addSubFunction(clabel);
						}
					}
				} else if (token.token == Token.number) {
					writeCode(0x0b, token.iliteral); // bnnn JP nnn+V0
				}
			} else
				error("Expected label or number");
			break;
		case jump0:
			expr(token);
			if (token.token == Token.number) {
				writeCode(0x0b, token.iliteral); // 1nnn JP
			} else if (token.token == Token.literal) {
				writeCode(0x0b, labelTarget(token.literal)); // 1nnn JP
				CC8Label clabel = labelFromString(token.literal);
				if (mPass == 2 && mbCodegen)
					clabel.addRef();
				if (mFunctionLabel != null) {
					if (clabel.mLabelType == C8LabelType.FUNCTION && mPass == 1 && mbCodegen) {
						mFunctionLabel.addSubFunction(clabel);
					}
				}
			} else
				error("Expected label or number");
			break;

		case call:
			expr(token);
			if (token.token == Token.number) {
				writeCode(0x02, token.iliteral); // 2nnn CALL
				if (mLastLabel != null) {
					if (mPass == 2 && mbCodegen) {
						mLastLabel.addRef();
					}
					logReference("call", mLastLabel, token);
				}
			} else if (token.token == Token.literal) {
				writeCode(0x02, labelTarget(token.literal)); // 2nnn CALL
				CC8Label clabel = labelFromString(token.literal);
				if (mPass == 2 && mbCodegen) {

					clabel.addRef();
				}
				if (mFunctionLabel != null) {
					if (clabel.mLabelType == C8LabelType.FUNCTION && mPass == 1 && mbCodegen) {
						mFunctionLabel.addSubFunction(clabel);
					}
				}
				logReference("call", clabel, token);
			} else
				error("Expected label or number");
			break;
		case se:
//    3xkk - SE Vx, byte
//     5xy0 - SE Vx, Vy

			if (nextToken(token)) {
				reg1 = regNr(token);
				if (reg1 == -1) {
					error("Expected Register");
					break;
				}
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					if (token.token == Token.number) {
						writeCode(0x03, reg1, token.iliteral); // 3xkk SE Vx, kk
					} else {
						reg2 = regNr(token);
						if (reg2 == -1) {
							error("Expected Register");
							break;
						}
						writeCode(0x05, reg1, reg2, 0); // 5xy0 SE Vx, Vy
					}
				}
				break;

			} else
				error("Expected Register");
			break;
		case sne:
//    4xkk - SNE Vx, byte
//    9xy0 - SNE Vx, Vy
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (reg1 == -1) {
					error("Expected Register");
					break;
				}
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {

					if (token.token == Token.number) {
						writeCode(0x04, reg1, token.iliteral); // 9xkk SNE Vx, kk
					} else {
						reg2 = regNr(token);
						if (reg2 == -1) {
							error("Expected Register");
							break;
						}
						writeCode(0x09, reg1, reg2, 0); // 4xy0 SNE Vx, Vy
					}
				}

			} else
				error("Expected Register");
			break;
		case add:
			// 8xy4 - ADD Vx, Vy
			// 7xkk - ADD Vx, byte
			// Fx1E - ADD I, Vx
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (token.token == Token.i) {
					nextToken(token);
					reg1 = regNr(token);
					if (reg1 == -1) {
						error("Expected expression");
						break;
					}
					// Fx1E - ADD I, Vx

					writeCode(0xF, reg1, 0x1e);

				} else {
					if (nextToken(token)) {
						if (token.token == Token.number) {
							writeCode(0x07, reg1, token.iliteral); // 7xkk ADD Vx,kk
						} else {
							reg2 = regNr(token);
							if (reg2 != -1) {
								writeCode(0x08, reg1, reg2, 0x04); // 8xy4 ADD Vx, Vy
							} else {
								error("Expected Number or Register");
								break;
							}
						}
					} else {
						error("Expected Register");
						break;
					}
					break;
				}

			}
			break;
		case and:
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					reg2 = regNr(token);
					writeCode(0x08, reg1, reg2, 2); // 8xy2 AND Vx, Vy
				} else {
					error("Expected register");
					break;
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case or:
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					reg2 = regNr(token);
					writeCode(0x08, reg1, reg2, 2); // 8xy1 OR Vx, Vy
				} else {
					error("Expected register");
					break;
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case xor:
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					reg2 = regNr(token);
					writeCode(0x08, reg1, reg2, 2); // 8xy3 XOR Vx, Vy
				} else {
					error("Expected register");
					break;
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case sub:
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					reg2 = regNr(token);
					writeCode(0x08, reg1, reg2, 5); // 8xy5 SUB Vx, Vy
				} else {
					error("Expected register");
					break;
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case subn:
			if (nextToken(token)) {
				reg1 = regNr(token);
				if (!expect(Token.comma))
					break;
				if (nextToken(token)) {
					reg2 = regNr(token);
					writeCode(0x08, reg1, reg2, 7); // 8xy7 SUBN Vx, Vy
				} else {
					error("Expected register");
					break;
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case shl:
			if (nextToken(token)) {
				reg1 = regNr(token);
				reg2 = reg1;
				if (nextToken(token)) {
					if (token.token == Token.comma) {
						if (nextToken(token)) {

							reg2 = regNr(token);
						}
					}
				}
				if (reg1 != -1 && reg2 != -1) {
					writeCode(0x08, reg1, reg2, 0x0e); // 8xyE SHL Vx{, Vy}
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case shr:
			if (nextToken(token)) {
				reg1 = regNr(token);
				reg2 = reg1;
				if (nextToken(token)) {
					if (token.token == Token.comma) {
						if (nextToken(token)) {

							reg2 = regNr(token);
						}
					}
				}
				if (reg1 != -1 && reg2 != -1) {
					writeCode(0x08, reg1, reg2, 0x06); // 8xy6 SHL Vx{, Vy}
				}
			} else {
				error("Expected register");
				break;
			}
			break;
		case sprite:
			reg1 = nextRegister(token, true);
			reg2 = nextRegister(token, true);
			nr = nextNumber(token);
			writeCode(0x0d, reg1, reg2, nr); // Dxyk Draw Vx, Vy, k
			break;
		case skp:
			reg1 = nextRegister(token, true);
			writeCode(0x0e, reg1, 0x9E);
			break;
		case sknp:
			reg1 = nextRegister(token, true);
			writeCode(0x0e, reg1, 0xA1);
			break;
		case db:
			while (mTokenizer.hasData()) {
				expr(token);
				if (token.token != Token.number) {
					mTokenizer.ungetToken(token);
					break;
				}
				mCode[pc++] = (byte) (token.iliteral & 0xff);
				nextToken(token);
				if (token.token != Token.comma) {
					mTokenizer.ungetToken(token);
					break;
				}

			}
			break;
		case number:
			// if (mbCodegen) {
			mCode[pc++] = (byte) (token.iliteral & 0xff);
			mMemoryStatistic.sizeData++;
			// }
			break;

		case i:
			compileI(token);
			break;
		case v0:
		case v1:
		case v2:
		case v3:
		case v4:
		case v5:
		case v6:
		case v7:
		case v8:
		case v9:
		case va:
		case vb:
		case vc:
		case vd:
		case ve:
		case vf:
			compileVx(token);
			break;
		case delay: // Fx07 - LD Vx, DT
			expect(Token.assign);
			nextToken(token);
			reg1 = regNr(token);
			writeCode(0xf, reg1, 0x15);
			break;
		case buzz: // Fx07 - LD Vx, DT
			expect(Token.assign);
			nextToken(token);
			reg1 = regNr(token);
			writeCode(0xf, reg1, 0x18);
			break;

		case bcd:
			nextToken(token);
			reg1 = regNr(token);
			writeCode(0xf, reg1, 0x33);
			break;
		case load:
			nextToken(token);
			reg1 = regNr(token);
			writeCode(0xf, reg1, 0x65);
			break;

		case save:
			nextToken(token);
			reg1 = regNr(token);
			writeCode(0xf, reg1, 0x55);
			break;

		case octoend: {
			mLevel--;
			// writeSourceLine();
			beginEndData = mBeginEndStack.pop();
			if (beginEndData.forRegister != -1)
				compileForEnd(beginEndData);
			else
				patch(beginEndData.patchAdr, pc);
			break;
		}

		case octoelse: {
			if (!mOptAnnotateAllLines)
				writeSourceLine();
			beginEndData = mBeginEndStack.pop();
			int pc2 = pc;
			writeCode(0x1, 0);
			patch(beginEndData.patchAdr, pc);
			beginEndData.patchAdr = pc2;
			mBeginEndStack.push(beginEndData);
			break;
		}

		case rnd: // Cxkk - RND Vx, byte
			nextToken(token);
			reg1 = regNr(token);
			expect(Token.comma);
			expr(token);
			if (token.token != Token.number) {
				error("Expected expression");
			} else {
				writeCode(0xC, reg1, token.iliteral);
			}
			break;

		// if vx operator vy then
		// if vx operator byte then
		// if vx operator vx begin ... end
		// if vx operator byte begin ... end
		case octoif: {
			compileIf(token);
			/*
			 * if (!mOptAnnotateAllLines) writeSourceLine(); CToken tokenb = new CToken();
			 * nextToken(token); reg1 = regNr(token); if (reg1 == -1) {
			 * error("Expected register"); break; } nextToken(token); Token compareToken =
			 * token.token; if (token.token == Token.key) { compareToken = Token.key; } else
			 * if (token.token == Token.minus) { expect(Token.key); compareToken =
			 * Token.notkey; token.token = Token.notkey; } else { expr(token); }
			 * nextToken(tokenb);
			 * 
			 * beginEndData = null; switch (tokenb.token) { case octobegin: mLevel++;
			 * beginEndData = new CBeginEndData(); mBeginEndStack.push(beginEndData); break;
			 * case octothen: break; default: error("Expcted begin or then");
			 * 
			 * } compileCompare(reg1, token, compareToken, beginEndData);
			 */
		}
			break;
		case macro:
			compileMacro();
			break;

		case comment:
			if (mPass == 2 && mbCodegen)
				mDebugSource.addComment(pc, token.literal);
			break;
		case calc:
			compileCalc(token);
			break;
		case octobyte:
			compileByte(token);
			break;
		case internaldefs:
			compileStructByte(token);
			break;
		case literal:
			compileLiteral(token);
			break;
		case loop: {
			mLevel++;
			CLoopData loopData = new CLoopData(pc);
			mStackLoopData.push(loopData);
		}
			break;
		case again: {
			mLevel--;
			CLoopData loopData = mStackLoopData.pop();
			writeCode(0x01, loopData.mLoopAdr); // 1nnn JP
			for (Integer addr : loopData.patchAddresses) {
				patch(addr.intValue(), pc);
			}
		}
			break;
		case octoconst: {
			nextToken(token);
			if (!check(token, Token.literal, "Name"))
				break;
			astr = token.literal;
			expr(token);
			if (!check(token, Token.number, "Value (Number)"))
				break;
			CC8Label newlabel = new CC8Label();
			newlabel.mName = astr;
			newlabel.mTarget = token.iliteral;
			newlabel.mLabelType = C8LabelType.CONST;
			mLabels.put(astr, newlabel);
		}
			break;

		case octowhile: {
			CToken tokenb = new CToken();
			nextToken(token);
			reg1 = regNr(token);
			if (reg1 == -1) {
				error("Expected register");
				break;
			}
			nextToken(token);
			Token compareToken = token.token;
			expr(token);
			compileWhile(reg1, token, compareToken);
			CLoopData loopData = mStackLoopData.peek();
			loopData.patchAddresses.add(pc);

			writeCode(1, 0);

		}
			break;
		case alias: {
			String strA;

			mTokenizer.getToken(token, false);
			if (!check(token, Token.literal, "literal"))
				break;
			strA = token.literal;
			mTokenizer.getToken(token, false);
			mTokenizer.setAlias(strA, token.literal);
			if (mPass == 2) {
				mDebugSource.startAlias(pc, regNr(token), strA);
				String name = "label_" + token.literal;
				CC8Label clabel = getLabel(name);
				if (clabel == null) {
					clabel = new CC8Label();
					clabel.mName = name;
					clabel.mRegister = strA;
					clabel.startRange(pc);
				}

			}

//		mTokenizer.addAlias(strA, token.literal);
		}
			break;
		case unalias: {
			while (mTokenizer.hasData()) {
				mTokenizer.getToken(token);
				if (token.token == Token.newline || token.token == Token.semikolon)
					break;
				unalias(pc, token.literal);
			}
		}
		case octoPackage: {
			nextToken(token);
			mTokenizer.mPackage = token.literal;
			break;
		}

		case octoPublic: {
			mTokenizer.mPublic = true;
		}
		case stringmode:
			compileStringmode();
			break;
		case octo:
			modeOcto = true;
			mTokenizer.modeOcto = true;
			break;
		case chipper:
			modeOcto = false;
			mTokenizer.modeOcto = true;
		case octofor:
			compileFor(token);
			break;
		case dotconst:
			nextToken(token);
			if (token.token != Token.literal) {
				error("Expected name");
				break;
			}
			astr = token.literal;
			expr(token);
			if (token.token != Token.number) {
				error("Expected number");
				break;
			}
			label = getLabel(astr);
			if (label == null) {
				label = new CC8Label();
				mLabels.put(astr, label);
			}
			label.mName = astr;
			label.mTarget = token.iliteral;
			label.mLabelType = C8LabelType.CONST;
			break;
		case dotif:
			expr(token);
			if (token.token != Token.number) {
				error("Expected constant");
				break;
			}
			mbCodegen = token.iliteral != 0;
			break;
		case dotifdef:
			mTokenizer.getToken(token);
			if (token.token != Token.literal) {
				error("Expected label name");
				break;
			}
			label = getLabel(token.literal);
			if (label == null)
				skipToDotEnd(token);
			break;

		case dotelse:
			mbCodegen = !mbCodegen;
			break;
		case dotend:
			mbCodegen = true;
			break;

		case breakpoint: {
			CDebugEntry entry = new CDebugEntry();
			entry.mPc = pc;
			entry.mIsBreakpoint = true;
			mDebugEntries.put(pc, entry);
		}
			break;
		case dotlog: {
			CDebugElem elem;
			CDebugEntry entry = new CDebugEntry();
			entry.mPc = pc;
			entry.mIsBreakpoint = false;
			if (mbCodegen) {
				mDebugEntries.put(pc, entry);
			}
			while (mTokenizer.hasData()) {
				nextToken(token);
				if (token.token == Token.newline)
					break;
				if (token.token == Token.semikolon)
					break;
				int regnr = regNr(token);
				if (regnr != -1) {
					elem = new CDebugElem();
					elem.register = regnr;
					entry.addElem(elem);
				} else {
					elem = new CDebugElem();
					elem.text = token.literal;
					entry.addElem(elem);
				}
			}

			break;
		}
		case dotinclude:
			compileInclude(token);
			break;
		case octowith:
			compileWith(token);
			break;
		case dotStruct:
			compileStruct(token);
			break;
		case dotTiles:
		case dotSprites:
		case dotVector:
		case dotTileset: {
			compileTiles(token);
			break;

		}
		case dotFunction: {
			compileFunction(token);
			break;
		}
		case octovar: {
			compileOctoVar(token);
			break;
		}
		case octoswitch:
			compileSwitch(token);
			break;

		case newline:
		case none:
			break;

// *************************** Mega Chip ********************************

		case megaOn:
			compile(token, "0011");
			break;
		case megaOff:
			compile(token, "0010");
			break;
		case megaplay:
			compile(token, "060n");
			break;
		case megastop:
			compile(token, "0700");
			break;
		case megaPal:
			compile(token, "02nn");
			break;
		case megaBlend:
			compile(token, "080n");
			break;
		case megaSpriteh:
			compile(token, "03nn");
			break;
		case megaSpritew:
			compile(token, "04nn");
			break;
		case megaAlpha:
			compile(token, "05nn");
			break;
		case octoldhi:
			compileLdHi(token);
			break;

		default:
			error("Undef token " + token.toString());
		}

	}

	private void compileLdHi(CToken token) {
		int opcode1, opcode2;
		expr(token);
		if (token.token != Token.number) {
			error("Expected number");
			return;
		}
		int number = token.iliteral;
		opcode2 = number & 0xffff;
		opcode1 = number & 0xff0000;
		opcode1 >>= 16;
		opcode1 |= 0x0100;
		writeCode(opcode1);
		writeCode(opcode2);
	}

	private void compile(CToken token, String pattern) {
		int opcode = 0;
		int pos = 0;
		int number;
		int maxnr;
		char c;
		while (pos < 4) {
			opcode <<= 4;
			c = pattern.charAt(pos++);
			if (c >= '0' && c <= '9') {
				opcode |= c - '0';
			} else if (c >= 'a' && c <= 'f') {
				opcode |= c - 'a' + 10;
			} else if (c >= 'A' && c <= 'F') {
				opcode |= c - 'A' + 10;
			} else if (c == 'x' || c == 'y') {
				mTokenizer.getToken(token);
				number = regNr(token);
				if (number == -1) {
					error("Expected register");
					return;
				}
				opcode |= number;
			} else if (c == 'n') {
				maxnr = 15;
				if (pos <= 3) {
					if (pattern.charAt(pos) == 'n') {
						pos++;
						maxnr = 255;
					}
				}

				expr(token);
				if (token.token != Token.number) {
					error("Expected number");
					return;
				}
				number = token.iliteral;
				if (maxnr == 15) {
					opcode |= number;
				} else {
					opcode <<= 4;
					opcode |= number;
				}
			}
		}
		System.out.println(String.format("Compile pattern=%s, opcode = 0x%04x", pattern, opcode));
		writeCode(opcode);

	}

	private void logReference(String typ, CC8Label clabel, CToken token2) {
//		if (mPass == 2 && mSecondGo && mbCodegen) {
		// mSBErrors.append(String.format("label %s is called from %s(%d) with %s\n",
		// clabel.mName, mTokenizer.mFilename, token.line,typ));
		// }
		// TODO Auto-generated method stub

	}

	private void skipToDotEnd(CToken token) {
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.dotend || token.token == Token.dotelse)
				break;

		}

	}

	private String labelName(String literal) {
		if (mFunctionLabel == null)
			return literal;
		else
			return String.format("%s_%s", mFunctionLabel, literal);
	}

	private void compileOctoVar(CToken token) {
		if (mFunctionLabel == null) {
			error("var only works inside :function");
		}
		CToken token2 = new CToken();
		CC8Label label;
		CAliases aliases = new CAliases();
		mTokenizer.mStackAliases.push(aliases);

		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.newline)
				break;
			switch (token.token) {
			case comment:
				break;
			case dot:
				expect(Token.octobyte);
				if (mbCodegen) {
					for (int i = 0; i < mFunctionLabel.mNextRegister; i++) {
						mCode[pc++] = 0;
					}
				}
				break;
			case literal:
				label = getLabel(token.literal);
				if (label != null) {
					if (label.mLabelType == C8LabelType.STRUCT) {
						if (mFunctionLabel.mNextRegister != 0) {
							error("A struct must be the first variable in a var");
							return;
						}
						for (String variable : label.mVariables) {
							aliases.addAlias(label.mName, variable, mFunctionLabel.mNextRegister++);

						}

					} else {
						error("var accepts Structs or literal " + token.literal + " is a label");
						return;
					}
				} else {
					mTokenizer.getToken(token2);
					if (token2.token == Token.assign) {
						mTokenizer.getToken(token2);

						if (token.token != Token.literal) {
							error("Var alias needs a literal");
							return;
						}
						int regnr = regNr(token2);
						if (regnr == -1) {
							CAlias alias = aliases.get(token2.literal);
							if (alias == null) {
								error(String.format("Alias for Register %s not found", token2.literal));
								return;
							}
							aliases.addAlias(token.literal, alias.mRegister);
						} else
							aliases.addAlias(token.literal, regnr);
					} else {
						mTokenizer.ungetToken(token2);
						aliases.addFunctionAlias(token.literal, mFunctionLabel.mName, mFunctionLabel.mNextRegister);
						mDebugSource.startAlias(pc, mFunctionLabel.mNextRegister,
								String.format("%s_%s", mFunctionLabel.mName, token.literal));

						mFunctionLabel.mNextRegister++;
					}
				}
			}
		}
	}

	private void unalias(int pc, String literal) {
		mTokenizer.mMapAlias.remove(literal);
		if (mPass == 2) {
			CC8Label label = getLabel("alias_" + token.literal);
			if (label != null)
				label.endRange(pc);
			mDebugSource.stopAlias(pc, literal);
		}
	}

	private void compileSwitch(CToken token) {
		int blockPatchAddr = 0;
		nextToken(token);
		int patchAdr = 0;
		ArrayList<Integer> patchAddresses = new ArrayList<Integer>();
		int regnr = regNr(token);
		if (regnr == -1) {
			if (token.token != Token.key) {
				error("Expected register or key");
				return;
			}
		}
		if (!expect(Token.octobegin))
			return;
		while (mTokenizer.hasData()) {
			nextNonWhiteToken(token); // must be case or end
			if (token.token == Token.octoend)
				break;
			if (token.token != Token.octocase) {
				error("Expected case");
				return;
			}
			expr(token);
			if (token.token != Token.number) {
				error("Expected constant");
			}
			int ilit = token.iliteral;
			nextNonWhiteToken(token);
			mTokenizer.ungetToken(token);
			if (token.token == Token.octobegin) {
				if (regnr == -1) {
					writeCode(0x6, 0xf, ilit); // vf := token.iliteral
					writeCode(0xe, 0xf, 0x9e); // skip key not vf
				} else {
					writeCode(0x03, regnr, ilit); // skip if reg != token.iliteral
				}
				patchAdr = pc;
				writeCode(0x1, 0); // jump
				compileBlock(token);
				if (regnr != -1) {
					patchAddresses.add(pc);
					writeCode(0x1, 0);
				}
				patch(patchAdr, pc);
			} else {
				if (regnr == -1) {
					writeCode(0x6, 0xf, ilit); // vf := token.iliteral
					writeCode(0xe, 0xf, 0xa1); // skip key not vf
				} else {
					writeCode(0x03, regnr, ilit); // skip if reg != token.iliteral
				}
				if (regnr != -1) {
					patchAdr = pc;
					writeCode(0x1, 0); // jump
				}
				compileBlock(token);
				if (regnr != -1) {
					patchAddresses.add(pc);
					writeCode(0x1, 0);
					patch(patchAdr, pc);
				}
			}

		}
		for (Integer adr : patchAddresses) {
			patch(adr, pc);
		}

	}

	private void compileBlock(CToken token) {
		int level = 0;
		while (mTokenizer.hasData()) {
			nextNonWhiteToken(token);
			if (token.token == Token.octobegin) {
				level++;
				nextNonWhiteToken(token);
			}
			if (token.token == Token.octoend) {
				level--;
				if (level == 0)
					break;
			}
			assembleStatement(token);
			if (level == 0)
				break;
		}

	}

	private void compileTiles(CToken token) {
		CC8Label label = new CC8Label();
		int w, h;
		switch (token.token) {
		case dotVector:
			label.mLabelType = C8LabelType.VECTOR;
			break;
		case dotTiles:
			label.mLabelType = C8LabelType.TILES;
			break;
		case dotSprites:
			label.mLabelType = C8LabelType.SPRITES;
			break;
		case dotTileset:
			label.mLabelType = C8LabelType.TILESET;
			break;
		}
		nextToken(token);
		if (token.token != Token.literal) {
			error("Expected label name");
			return;
		}
		label.mName = token.literal;
		label.mTarget = pc;
		mLabels.put(label.mName, label);
		if (label.mLabelType != C8LabelType.VECTOR) {
			expect(Token.comma);
			expr(token);
			if (!(token.token == Token.number)) {
				error("Expected width");
				return;
			}
			expect(Token.comma);
			expr(token);
			if (!(token.token == Token.number)) {
				error("Expected height");
				return;
			}
		}
		expectBegin();

		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.curlybracketclose || token.token == Token.octoend) {
				break;
			}
			if (token.token == Token.comma || token.token == Token.comment || token.token == Token.newline)
				continue;
			if (token.token == Token.label) {
				CC8Label label2 = new CC8Label();
				label2.mName = token.literal;
				label2.mLabelType = label.mLabelType;
				label2.mTarget = pc;
				mLabels.put(label2.mName, label2);
				continue;
			}
			if (token.token == Token.number /* && mbCodegen */) {
				mCode[pc++] = (byte) (token.iliteral & 0xff);
			}
		}
	}

	private void compileInclude(CToken token) {
		String filename;
		mTokenizer.getToken(token);
		if (token.token == Token.string || token.token == Token.literal) {
			filename = token.literal;
			String text = Tools.loadTextFile(filename);
			if (text == null) {
				filename = mFolder.trim() + "/" + token.literal;
				text = Tools.loadTextFile(filename);
			}
			if (text == null) {
				error("File " + filename + " not found");
			}
			CTokenizer tokenizer = mTokenizer;
			CMemoryStatistic memoryStatistic = mMemoryStatistic;
			mMemoryStatistic = new CMemoryStatistic();
			if (mPass == 2)
				mMemoryStatistics.add(mMemoryStatistic);
			mMemoryStatistic.file = filename;
			mTokenizer = new CTokenizer();
			mTokenizer.mFilename = filename;
			mTokenizer.start(text);
			while (mTokenizer.hasData()) {
				try {
					assembleLine();
				} catch (Exception e) {
					error(e.getLocalizedMessage());
					e.printStackTrace();
					break;
				}
			}
			pc += 2;
			mTokenizer = tokenizer;
			mMemoryStatistic = memoryStatistic;

		}

	}

	private void compileStructByte(CToken token) {

		CC8Label label = getLabel(token.literal);
		if (label == null) {
			for (int i = 0; i < token.iliteral; i++) {
				mCode[pc++] = 0;
			}
			return;

		}
		int startPC = pc;

		int count = label.mVariables.size();
		int data[] = new int[count];

		for (int i = 0; i < count; i++)
			data[i] = 0;
		mTokenizer.getToken(token, false);
		if (token.token == Token.arrayopen) {
			expr(token);
			if (token.token == Token.number) {
				count = count * token.iliteral;
				data = new int[count];
				for (int i = 0; i < count; i++)
					data[i] = 0;
				expect(Token.arraytclose);
			} else {
				error("Expected number");
				return;
			}
		} else if (token.token == Token.curlybracketopen) {
			// now we find x = number until }
			while (mTokenizer.hasData()) {
				mTokenizer.getToken(token, false);
				if (token.token == Token.newline || token.token == Token.whitespace || token.token == Token.comment)
					continue;
				if (token.token == Token.curlybracketclose)
					break;
				if (token.token != Token.literal) {
					error("Expected struct member name");
					return;
				}
				int reg = label.regFromVar(token.literal);
				if (reg == -1) {
					error("Struct " + label.mName + " does not contain varible " + token.literal);
					return;
				}
				expect(Token.assign);
				expr(token);
				if (token.token != Token.number) {
					error("Expected constant");
					return;
				}
				data[reg] = token.iliteral;
			}
		} else {
			mTokenizer.ungetToken(token);
		}

		for (int i = 0; i < count; i++) {
			mCode[pc++] = (byte) data[i];
		}

		if (mLastLabel != null) {
			if (mLastLabel.mTarget == startPC) {
				mLastLabel.mElementSize = label.mVariables.size();
			}
		}

	}

	private void compileWith(CToken token) {
		CC8Label structLabel;
		mTokenizer.getToken(token, false);
		if (token.token != Token.literal) {
			error("Erwarte: Name");
			return;
		}
		structLabel = getLabel(token.literal);
		if (structLabel == null) {
			error("Struct " + token.literal + " not found");
			return;
		}
		if (structLabel.mLabelType != C8LabelType.STRUCT) {
			error("Struct " + token.literal + " is not a struct");
			return;
		}
		mTokenizer.pushStruct(structLabel);
		// expect(Token.curlybracketopen);
		if (!expectBegin())
			return;
		while (mTokenizer.hasData()) {
			nextToken(token);
			if (token.token == Token.curlybracketclose || token.token == Token.octoend)
				break;
			assembleStatement(token);
		}
		mTokenizer.popStruct();

	}

	private void compileStruct(CToken token) {
		int register = 0;
		mTokenizer.getToken(token);
		if (token.token != Token.literal) {
			error("Expected name");
			return;
		}
		CC8Label label = getLabel(token.literal);
		if (label == null) {
			label = new CC8Label();
			label.mName = token.literal;
			label.mLabelType = C8LabelType.STRUCT;
			mLabels.put(token.literal, label);
		}

		mTokenizer.getToken(token);
		if (token.token == Token.octoextends) {
			mTokenizer.getToken(token, false);
			CC8Label elabel = getLabel(token.literal);
			if (elabel == null) {
				error(String.format(":struct %s not found", token.literal));
				return;
			}
			if (elabel.mLabelType != C8LabelType.STRUCT) {
				error(String.format("label %s is not a struct", token.literal));
				return;

			}
			for (String var : elabel.mVariables) {
				label.addVar(var);
				mDebugSource.startAlias(pc, register++, String.format("%s.%s", label.mName, var));

			}
			mTokenizer.getToken(token);
		}

		if (token.token != Token.curlybracketopen) {
			error("Expected {");
			return;
		}

		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token, false);
			if (token.token == Token.curlybracketclose)
				break;
			if (token.token == Token.literal) {
				label.addVar(token.literal);
				mDebugSource.startAlias(pc, register++, String.format("%s.%s", label.mName, token.literal));
			} else if (!(token.token == Token.comment || token.token == Token.newline)) {
				error("Expected name");
				return;
			}
		}

	}

	private boolean check(CToken token, Token expected, String text) {
		if (!(token.token == expected)) {
			error("Expected " + text);
			return false;
		}
		return true;
	}

	/*
	 * :stringmode tinytext
	 * "#/ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.!?,'- " {
	 * :byte { 1 * VALUE } }
	 * 
	 */
	private void compileStringmode() {
		String varname;
		String alphabet;
		String macro;
		nextToken(token);
		if (!check(token, Token.literal, "Label name"))
			return;
		varname = token.literal;
		nextToken(token);
		if (!check(token, Token.string, "Alphabet"))
			return;
		;
		alphabet = token.literal;
		if (expect(Token.curlybracketopen)) {
			int level = 0;
			StringBuilder sb = new StringBuilder();
			while (mTokenizer.hasData()) {
				char c = mTokenizer.nextChar();
				if (c == '{')
					level++;
				if (c == '}') {
					level--;
					if (level == -1)
						break;
				}
				sb.append(c);
			}
			CC8Label label = new CC8Label();
			label.mName = varname;
			label.mAlphabet = alphabet;
			label.mMacro = sb.toString();
			label.mLabelType = C8LabelType.STRINGMODE;
			mLabels.put(varname, label);
		}
	}

	private double compileCalcTerminal(CToken token) {
		double result;

		switch (token.token) {
		case literal:
			if (token.literal.compareToIgnoreCase("E") == 0) {
				return Math.E;
			}
			if (token.literal.compareToIgnoreCase("PI") == 0) {
				return Math.PI;
			}
			if (token.literal.compareToIgnoreCase("HERE") == 0) {
				System.out.println("Here = " + Integer.toString(pc));
				return pc;
			}
			Integer value = mMapConstants.get(token.literal.toUpperCase());
			if (value != null) {
				return value.intValue();
			}
			CC8Label label = getLabel(token.literal);
			if (label != null) {
				if (label.mValue != null)
					return label.mValue.doubleValue();
				return label.mTarget;
			}
			break;
		case number:
			return token.iliteral;
		case bracketopen:
			nextToken(token);
			result = parseCalc(token);
			nextToken(token);
			if (token.token != Token.bracketclose) {
				error("Expected )");
			}
			return result;
		default:
			error("Expected literal or number");
		}
		return 0;
	}

	private double parseCalc(CToken token) {
		UnaryFunction unaryFunction;
		BinaryFunction binaryFunction;
		unaryFunction = mMapUnaryFunctions.get(token.token);
		if (unaryFunction != null) {
			nextToken(token);
			return unaryFunction.calc(compileCalcTerminal(token));
		}
		double t = compileCalcTerminal(token);
		nextToken(token);
		binaryFunction = mMapBinaryFunctions.get(token.token);
		if (binaryFunction != null) {
			nextToken(token);
			t = binaryFunction.calc(t, parseCalc(token));
		} else
			mTokenizer.ungetToken(token);
		return t;
	}

	/*
	 * unary: - ~ ! sin cos tan exp log abs sqrt sign ceil floor @ strlen binary: -
	 * + * / % & | ^ << >> pow min max < <= == != >= >
	 */

	private void compileCalc(CToken token) {

		nextToken(token);
		if (token.token != Token.literal) {
			error("Expected name");
			return;
		}
		CC8Label label = new CC8Label();
		label.mName = token.literal;
		mLabels.put(token.literal, label);
		expect(Token.curlybracketopen);
		nextToken(token);

		double result = parseCalc(token);
		label.mTarget = (int) result;
		label.mValue = result;
		label.mLabelType = C8LabelType.CONST;

		expect(Token.curlybracketclose);

	}

	private void compileByte(CToken token2) {
		expect(Token.curlybracketopen);
		nextToken(token);
		double result = parseCalc(token);
		mCode[pc++] = (byte) result;
		expect(Token.curlybracketclose);
	}

	// compile Macro expansion or call
	private void compileLiteral(CToken token) {
		try {
			CMacroData macroData = mMapMacros.get(token.literal);
			if (macroData != null) {
				if (mTokenizer.mBaseline == 247) {
					System.out.println("break");
				}
				compileMacroExpansion(macroData, token);

			} else {
				if (token.literal.compareTo("flagsnocarry") == 0)
					System.out.println("Stop");
				CC8Label label = getLabel(token.literal);
				if (label == null && mPass == 2) {
					error("Label " + token.literal + " not found");
				}
				if (label != null) {
					switch (label.mLabelType) {
					case STRINGMODE:
						compileString(label);
						break;
					case CONST:
						mCode[pc++] = (byte) (label.mTarget & 0xff);
						break;
					case STRUCT: {
						expect(Token.dot);
						expect(Token.octobyte);
						if (mbCodegen) {
							for (int i = 0; i < label.mVariables.size(); i++) {
								mCode[pc++] = 0;
							}
						}
						break;
					}
					default:
						writeCode(0x2, label.mTarget);
						if (mPass == 2 && mbCodegen)
							label.addRef();
						logReference("call", label, token);
						if (mFunctionLabel != null) {
							if (label.mLabelType == C8LabelType.FUNCTION && mPass == 1 && mbCodegen) {
								mFunctionLabel.addSubFunction(label);
							}
						}
						break;
					}
				} else {
					writeCode(0x2, 0);
				}
			}
		} catch (Exception e) {
			error(e.getLocalizedMessage());
		}

	}

	private void compileString(CC8Label label) {
		nextToken(token);
		if (!check(token, Token.string, "Text"))
			return;
		CTokenizer saveTokenizer = mTokenizer;
		mTokenizer = new CTokenizer();
		String text = token.literal;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			mTokenizer.start(label.mMacro);
			mTokenizer.setAlias("CHAR", String.format("%d", (int) c));
			mTokenizer.setAlias("INDEX", String.format("%d", i));
			int p = label.mAlphabet.indexOf(c);
			if (p == -1) {
				error(String.format("%c is not in alphabet %s", c, label.mAlphabet));
				break;
			}
			mTokenizer.setAlias("VALUE", String.format("%d", p));
			while (mTokenizer.hasData()) {
				assembleLine();
			}
		}
		mTokenizer = saveTokenizer;

	}

	private void compileFunction(CToken token) {
		mLevel = 0;
		int startPC = pc;
		if (!mOptAnnotateAllLines)
			writeSourceLine();

		mTokenizer.getToken(token);
		if (token.token != Token.literal) {
			error("Expected function name");
			return;
		}
		expect(Token.curlybracketopen);
		CC8Label saveFunctionLabel = mFunctionLabel;
		Stack saveAlias = mTokenizer.mStackAliases;
		mTokenizer.mStackAliases = new Stack<>();
		boolean saveCodegen = mbCodegen;

		String functionName = token.literal;
		CC8Label functionLabel = getLabel(token.literal);

		if (functionLabel == null) {
			functionLabel = new CC8Label();
			functionLabel.mName = token.literal;
			functionLabel.mLabelType = C8LabelType.FUNCTION;
		}
		functionLabel.mTarget = pc;
		functionLabel.mNextRegister = 0;
		if (functionLabel.mSkipCompiling)
			mbCodegen = false;

		mLabels.put(functionLabel.mName, functionLabel);
		mFunctionLabel = functionLabel;
		int aliasStackSize = mTokenizer.mStackAliases.size();

		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.curlybracketclose)
				break;
			assembleStatement(token);
		}

		while (mTokenizer.mStackAliases.size() > aliasStackSize) {
			CAliases aliases = mTokenizer.mStackAliases.pop();

			for (CAlias alias : aliases.values()) {
				mDebugSource.stopAlias(pc, alias.getAliasName());
			}
		}

		mFunctionLabel = saveFunctionLabel;
		mbCodegen = saveCodegen;
		mTokenizer.mStackAliases = saveAlias;
		mMapFunctionSize.put(functionName, new Integer(pc - startPC));

	}

	private void compileMacroExpansion(CMacroData macroData, CToken token) {

		if (!mOptAnnotateAllLines)
			writeSourceLine();
//		System.out.println("------------ Macro expansion at line " + token.line);
		CTokenizer tempTokenizer = new CTokenizer();
		tempTokenizer.mHint = macroData.name;
		tempTokenizer.mBaseline = mTokenizer.mLine;
		for (int i = 0; i < macroData.parameters.size(); i++) {
			nextToken(token);
			// mTokenizer.getToken(token);
			// mTokenizer.findStructSymbol(token);
			if (token.token == Token.curlybracketopen) {
				expr(token);
				expect(Token.curlybracketclose);
			} else {
				if (token.token == Token.bracketopen) {
					expr(token);

					expect(Token.bracketclose);
				}

			}
			// expr(token);
			tempTokenizer.replace(macroData.parameters.get(i), token.literal);
//			System.out.println(
//					String.format("Param %d = %s at %d", i, token.literal, mTokenizer.mLine + mTokenizer.mBaseline));
		}

		TreeMap<String, CMacroData> saveMapMacros = (TreeMap<String, CMacroData>) mMapMacros.clone();
		TreeMap<String, CC8Label> saveLabels = (TreeMap<String, CC8Label>) mLabels.clone();
		CTokenizer saveTokenizer = mTokenizer;
		mTokenizer = tempTokenizer;
		mTokenizer.mMapAlias = saveTokenizer.mMapAlias;
		mTokenizer.start(macroData.macro);

		mTokenizer = tempTokenizer;
		mLevel++;
		String prevContext = mContext;
		mContext = macroData.name;

		while (mTokenizer.hasData()) {
			assembleLine();
		}
		mContext = prevContext;
		mLevel--;
		mTokenizer = saveTokenizer;
		mMapMacros = saveMapMacros;
		mLabels = saveLabels;

	}

	private void compileMacro() {
		CMacroData macroData = new CMacroData();
		nextToken(token);
		if (token.token != Token.literal) {
			error("Expcted macro name");
			return;
		}
		macroData.name = token.literal;
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token, false);
			if (isWhiteToken(token))
				continue;
			if (token.token == Token.curlybracketopen) {
				StringBuilder sb = new StringBuilder();
				int level = 0;
				while (mTokenizer.hasData()) {
					char c = mTokenizer.nextChar();
					if (c == '{')
						level++;
					if (c == '}') {
						level--;
						if (level < 0)
							break;
					}
					sb.append(c);
				}
				macroData.macro = sb.toString();
				mMapMacros.put(macroData.name, macroData);
				break;

			}
			if (token.token != Token.literal) {
				error("Expcted macro name");
				return;
			}
			macroData.parameters.add(token.literal);
		}

	}

	private boolean isWhiteToken(CToken token2) {
		if (token.token == Token.newline || token.token == Token.comment)
			return true;
		return false;
	}

	private void patch(int patchAdr, int pc) {
		if (mbCodegen) {
			int command = (mCode[patchAdr] << 8) & 0xf000;
			if (command != 0x1000) {
				error(String.format("Invalid patch %04x to %04x", patchAdr, pc));
				return;
			}
			command |= pc;
			mCode[patchAdr] = (byte) (command >> 8);
			mCode[patchAdr + 1] = (byte) (command & 0xff);
		}

	}

	private void compileWhile(int reg1, CToken token, Token compareToken) {
		int reg2 = regNr(token);
		if (reg2 != -1) {
			switch (compareToken) {
			case equals:
				writeCode(0x5, reg1, reg2, 0); // skip if equal
				break;
			case unequal:
				writeCode(0x9, reg1, reg2, 0); // skip if unequal
				break;
			case smaller:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf -= rx
				writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				break;
			case bigger:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				break;

			case lessequal:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				break;

			case biggerequal:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf =- rx
				writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				break;
			}
		} else if (token.token == Token.number) {
			int number = token.iliteral;
			switch (compareToken) {
			case unequal:
				/*
				 * 0230 45 00 if v5 == 0x0 then
				 * 
				 */
				writeCode(0x4, reg1, number); // skip if unequal
				break;
			case equals:
				/*
				 * 0236 35 00 if v5 != 0x0 then
				 * 
				 */
				writeCode(0x3, reg1, number); // skip if equal
				break;
			case smaller:
				/*
				 * 0212 6f 00 vf := 0x0 0214 8f 57 vf =- v5 0216 3f 00 if vf != 0x0 then 0218 12
				 * 50 jump label0004
				 * 
				 */
				writeCode(0x6, 0xf, number); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x07); // vf -= rx
				writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				break;
			case lessequal:
				/*
				 * 0226 6f 00 vf := 0x0 0228 8f 55 vf -= v5 022a 4f 00 if vf == 0x0 then 022c 12
				 * 50 jump label0004
				 * 
				 */
				writeCode(0x6, 0xf, number); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf =- rx
				writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				break;
			case bigger:
				/*
				 * 0208 6f 00 vf := 0x0 020a 8f 55 vf -= v5 020c 3f 00 if vf != 0x0 then 020e 12
				 * 50 jump label0004
				 * 
				 */
				writeCode(0x6, 0xf, number); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf =- rx
				writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				break;

			case biggerequal:
				/*
				 * 021c 6f 00 vf := 0x0 021e 8f 57 vf =- v5 0220 4f 00 if vf == 0x0 then 0222 12
				 * 50 jump label0004
				 */
				writeCode(0x6, 0xf, number); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				break;
			}
		}

	}

	private void compileIf(CToken token) {
		int reg1;
		CBeginEndData beginEndData;
		if (!mOptAnnotateAllLines)
			writeSourceLine();
		CToken tokenb = new CToken();
		nextToken(token);
		reg1 = regNr(token);
		if (reg1 == -1) {
			error("Expected register");
			return;
		}
		nextToken(token);
		Token compareToken = token.token;
		if (token.token == Token.key) {
			compareToken = Token.key;
		} else if (token.token == Token.minus) {
			expect(Token.key);
			compareToken = Token.notkey;
			token.token = Token.notkey;
		} else {
			expr(token);
		}
		nextToken(tokenb);

		beginEndData = null;
		switch (tokenb.token) {
		case octobegin:
			mLevel++;
			beginEndData = new CBeginEndData();
			// mBeginEndStack.push(beginEndData);
			break;
		case octothen:
			break;
		default:
			error("Expcted begin or then");

		}
		compileCompare(reg1, token, compareToken, beginEndData);

		if (tokenb.token == Token.octobegin) {
			while (mTokenizer.hasData()) {
				nextNonWhiteToken(token);
				if (token.token == Token.octoelse) {
					if (!mOptAnnotateAllLines)
						writeSourceLine();
					int pc2 = pc;
					writeCode(0x1, 0);
					patch(beginEndData.patchAdr, pc);
					beginEndData.patchAdr = pc2;
				} else if (token.token == Token.octoend) {
					patch(beginEndData.patchAdr, pc);
					break;
				} else {
					assembleStatement(token);
				}
			}
		}

	}

	private void compileCompare(int reg1, CToken token, Token compareToken, CBeginEndData beginEndData) {
		int reg2 = regNr(token);
		if (reg2 != -1) {
			switch (compareToken) {
			case equals:
				if (beginEndData == null)
					writeCode(0x9, reg1, reg2, 0); // skip if unequal
				else {
					writeCode(0x05, reg1, reg2, 0); // skip if equal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case unequal:
				if (beginEndData == null)
					writeCode(0x5, reg1, reg2, 0); // skip if equal
				else {
					writeCode(0x9, reg1, reg2, 0); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case bigger:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf -= rx
				if (beginEndData == null)
					writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x3, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case biggerequal:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				if (beginEndData == null)
					writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x4, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case smaller:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				if (beginEndData == null)
					writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x3, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;

			case lessequal:
				writeCode(0x8, 0xf, reg2, 0); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf =- rx
				if (beginEndData == null)
					writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x4, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			}
		} else if (token.token == Token.number || token.token == Token.key || token.token == Token.notkey) {
			switch (compareToken) {
			case key:
				if (beginEndData == null)
					writeCode(0xe, reg1, 0xA1);
				else {
					writeCode(0xe, reg1, 0x9E);
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case notkey:
				if (beginEndData == null)
					writeCode(0xe, reg1, 0x9E);
				else {
					writeCode(0xe, reg1, 0xA1);
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case equals:
				if (beginEndData == null)
					writeCode(0x4, reg1, token.iliteral); // skip if unequal
				else {
					writeCode(0x03, reg1, token.iliteral); // skip if equal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case unequal:
				if (beginEndData == null)
					writeCode(0x3, reg1, token.iliteral); // skip if equal
				else {
					writeCode(0x4, reg1, token.iliteral); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case bigger:
				/*
				 * 0206 6f 0c vf := 0xc 0208 8f 05 vf -= v0 020a 4f 00 if vf == 0x0 then
				 */
				writeCode(0x6, 0xf, token.iliteral); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x05); // vf -= rx
				if (beginEndData == null)
					writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x3, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case biggerequal:
				/*
				 * 021e 6f 0c vf := 0xc 0220 8f 05 vf -= v0 0222 3f 00 if vf != 0x0 then
				 * 
				 */
				writeCode(0x6, 0xf, token.iliteral); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				if (beginEndData == null)
					writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x4, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			case smaller:
				/*
				 * 020e 6f 0c vf := 0xc 0210 8f 07 vf =- v0 0212 4f 00 if vf == 0x0 then
				 * 
				 */
				writeCode(0x6, 0xf, token.iliteral); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x7); // vf =- rx
				if (beginEndData == null)
					writeCode(0x4, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x3, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;

			case lessequal:
				/*
				 * 0216 6f 0c vf := 0xc 0218 8f 07 vf =- v0 021a 3f 00 if vf != 0x0 then
				 * 
				 */
				writeCode(0x6, 0xf, token.iliteral); // vf = ry
				writeCode(0x8, 0xf, reg1, 0x5); // vf =- rx
				if (beginEndData == null)
					writeCode(0x3, 0xf, 0x00); // skip if fx != 0
				else {
					writeCode(0x4, 0xf, 0x00); // skip if unequal
					beginEndData.patchAdr = pc;
					writeCode(0x01, 0); // jump to unknown
				}
				break;
			}

		}

	}

	private void compileVx(CToken token) {
		int reg1 = regNr(token);
		int reg2;
		nextToken(token);
		switch (token.token) {
		case assign:
			expr(token);
			switch (token.token) {
			case number: // 6xkk - LD Vx, byte
				writeCode(0x6, reg1, token.iliteral);
				break;
			case rnd: // Cxkk - RND Vx, byte
				expr(token);
				if (token.token != Token.number) {
					error("Expected expression");
				} else {
					writeCode(0xC, reg1, token.iliteral);
				}
				break;
			case delay: // Fx07 - LD Vx, DT
				writeCode(0xf, reg1, 0x07);
				break;
			case key: // Fx0A - LD Vx, key
				writeCode(0xf, reg1, 0x0a);
				break;
			default:
				if (token.register != -1) { // 8xy0 - LD Vx, Vy
					writeCode(0x8, reg1, token.register, 0);
					break;
				} else {
					error("Expected expression, register, random or deleay");
				}
			}
			break;

		case plusassign:
			expr(token);
			if (token.register != -1)
				writeCode(0x8, reg1, token.register, 4); // 8xy4 - ADD Vx, Vy
			else
				writeCode(0x7, reg1, token.iliteral); // 7xkk - ADD Vx, byte
			break;
		case minusassign:
			expr(token);
			if (token.register != -1)
				writeCode(0x8, reg1, token.register, 5); // 8xy5 - SUB Vx, Vy
			else
				writeCode(0x7, reg1, (-token.iliteral) & 0xff); // 7xkk - SUB Vx, Vy (add but negative value)
			break;
		case assignminus:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 7); // 8xy7 - SUBN Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;
		case andassign:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 2); // 8xy2 - AND Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;
		case orassign:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 1); // 8xy1 - OR Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;

		case xorassign:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 3); // 8xy3 - XOR Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;

		case shrassign:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 6); // 8xy6 - SHR Vx {, Vy}
			else
				error("Ecxpected Register v0..vf");
			break;

		case shlassign:
			nextToken(token);
			reg2 = regNr(token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 0xE); // 8xyE - SHL Vx {, Vy}
			else
				error("Ecxpected Register v0..vf");
			break;

		}

	}

	private void compileFor(CToken token) {
		CBeginEndData forData = new CBeginEndData();
		nextToken(token);
		int regnr = regNr(token);
		if (regnr == -1) {
			error("Expected register");
		}
		forData.forRegister = regnr;
		expect(Token.assign);
		// nextToken(token);
		expr(token);
		// for v0 := 1 to ... or
		// for v0 := v1 to ... or
		if (token.token == Token.number) {
			writeCode(0x6, regnr, token.iliteral); // vx = nn
		} else {
			int reg2 = regNr(token);
			if (regnr == -1) {
				error("Expected number or register");
				return;
			}
			writeCode(0x08, regnr, reg2, 0); // vx = vy
		}
		expect(Token.octoto);
		expr(token);
		if (token.token == Token.number) {
			forData.forTargetNr = token.iliteral;
			forData.forTargetReg = -1;
		} else {
			forData.forTargetReg = regNr(token);
			if (forData.forTargetNr == -1) {
				error("Expected number or register");
				return;
			}
		}
		nextToken(token);
		if (token.token == Token.octostep) {
			nextToken(token);
			if (token.token == Token.number) {
				forData.forStepNr = token.iliteral;
				forData.forStepReg = -1;
			} else {
				forData.forStepReg = regNr(token);
				if (forData.forStepReg == -1) {
					error("Expected number or register");
					return;
				}
			}
		} else {
			mTokenizer.ungetToken(token);
			forData.forStepReg = -1;
			forData.forStepNr = 1;
		}
		forData.pc = pc;
		compileBlock(token);
		compileForEnd(forData);
		/*
		 * nextToken(token); if (token.token == Token.octobegin) {
		 * mBeginEndStack.push(forData); } else {
		 * 
		 * assembleStatement(token); compileForEnd(forData); }
		 */

	}

	private void compileForEnd(CBeginEndData forData) {

		if (forData.forTargetReg >= 0) {
			writeCode(0x9, forData.forRegister, forData.forTargetReg, 0); // 9xy0 - SNE Vx, Vy
		} else {
			writeCode(0x4, forData.forRegister, forData.forTargetNr); // 3xkk - SNE Vx, byte
		}
		int patchadr = pc;
		writeCode(0x1, 0);

		if (forData.forStepReg != -1) {
			writeCode(0x8, forData.forRegister, forData.forStepReg, 4); // 8xy4 - ADD Vx, Vy
		} else {
			writeCode(0x7, forData.forRegister, forData.forStepNr); // 7xkk - ADD Vx, byte
		}
		/*
		 * if (forData.forTargetReg >= 0) { writeCode(0x5, forData.forRegister,
		 * forData.forTargetReg, 0); // 5xy0 - SE Vx, Vy } else { writeCode(0x3,
		 * forData.forRegister, forData.forTargetNr); // 3xkk - SE Vx, byte }
		 */
		writeCode(0x1, forData.pc);
		if (mbCodegen)
			patch(patchadr, pc);
	}

	// Annn - LD I, addr
	// Fx1E - ADD I, Vx
	// i := hex vx
	// i := bighex vx
	private void compileI(CToken token) {
		int reg1;
		boolean islong = false;
		int usereg1 = -1;
		int usereg2 = -1;
		int index = -1;
		int indexreg = -1;
		CToken token2 = new CToken();
		nextToken(token);
		switch (token.token) {
		case assign: // Annn - LD I, addr
			CC8Label label = null;
			mTokenizer.getToken(token);
			if (token.token == Token.octoLong)
				islong = true;
			else
				mTokenizer.ungetToken(token);
			;
			expr(token);
			if (mExprLabelCount == 1)
				label = mExprLabel;

			mTokenizer.getToken(token2);
			if (token2.token == Token.arrayopen) {

				expr(token2);
				if (token2.token == Token.number) {
					index = token2.iliteral;
				} else {
					indexreg = regNr(token2);
					if (indexreg == -1) {
						error("Expected register or number");
						return;
					}
				}
				expect(Token.arraytclose);
				mTokenizer.getToken(token2);
				if (token2.token == Token.using) {
					mTokenizer.getToken(token2);
					usereg1 = regNr(token2);
					if (usereg1 == -1) {
						error("Expected register or number");
						return;
					}
					mTokenizer.getToken(token2);
					usereg2 = regNr(token2);
					if (usereg2 == -1)
						mTokenizer.ungetToken(token2);
				} else
					mTokenizer.ungetToken(token2);

			} else {
				mTokenizer.ungetToken(token2);
			}

			switch (token.token) {
			case number:
				if (islong) {
					writeCode(0xf000);
					writeCode(token.iliteral);
				} else {
					writeCode(0xA, token.iliteral);
				}
				break;
			case hex: // i := hex vx
				nextToken(token);
				reg1 = regNr(token);
				writeCode(0xf, reg1, 0x29);
				break;
			case bighex: // i := bighex vx
				nextToken(token);
				reg1 = regNr(token);
				writeCode(0xf, reg1, 0x30);
				break;

			default:
				if (mPass == 1)
					writeCode(0xA, 0);
				else
					error("Expected constant expression");

			}
			int faktor = 1;
			if (label != null) {
				faktor = label.mElementSize;
			}

			if (index != -1) {
				writeCode(0x6, 0xf, faktor * index); // 6f 64 vf := $64
				writeAddI(15);

			}
			if (indexreg != -1) {
				writeLdRegV0(usereg1, indexreg);
				switch (faktor) {
				case 1:
					writeAddI(15);
					break;
				case 2:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeAddI(usereg1);
					break;
				case 3:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeAddvxvy(usereg1, indexreg);
					writeAddI(usereg1);
					break;
				case 4:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeAddI(usereg1);
					break;
				case 5:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeAddvxvy(usereg1, indexreg);
					writeAddI(usereg1);
					break;
				case 6:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *4
						writeAddvxvy(usereg1, usereg2); // *6
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeAddvxvy(usereg1, indexreg); // *5
						writeAddvxvy(usereg1, indexreg); // *6
						writeAddI(usereg1);

					}
					break;
				case 7:
					if (usereg2 != -1) {
						checkusereg1(usereg1);
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *4
						writeAddvxvy(usereg1, usereg2); // *6
//						writeAddvxvy(usereg1, indexreg); // *6
						writeAddvxvy(usereg1, 0);
						writeAddI(usereg1);
					} else {
						checkusereg1(usereg1);
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeAddvxvy(usereg1, indexreg); // *5^
						writeAddvxvy(usereg1, indexreg); // *6
						writeAddvxvy(usereg1, indexreg); // *7
						writeAddI(usereg1);

					}
					break;
				case 8:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeAddI(usereg1);
					break;
				case 9:
					checkusereg1(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeDup(usereg1);
					writeAddvxvy(usereg1, indexreg); // *6
					writeAddI(usereg1);
					break;
				case 10:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1);
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1);
						writeDup(usereg1);
						writeAddvxvy(usereg1, usereg2); // *6
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, indexreg); // *9
						writeAddvxvy(usereg1, indexreg); // *10
						writeAddI(usereg1);

					}
					break;
				case 11:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, usereg2); // *10
						writeAddvxvy(usereg1, indexreg); // *11
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, indexreg); // *9
						writeAddvxvy(usereg1, indexreg); // *10
						writeAddvxvy(usereg1, indexreg); // *11
						writeAddI(usereg1);
					}
					break;
				case 12:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, usereg2); // *12
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, indexreg); // *9
						writeAddvxvy(usereg1, indexreg); // *9
						writeAddvxvy(usereg1, indexreg); // *10
						writeAddvxvy(usereg1, indexreg); // *11
						writeAddvxvy(usereg1, indexreg); // *12
						writeAddI(usereg1);

					}
					break;
				case 13:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, usereg2); // *12
						writeAddvxvy(usereg1, indexreg); // *13
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeDup(usereg1); // *16
						writeSubvxvy(usereg1, indexreg); // *15
						writeSubvxvy(usereg1, indexreg); // *14
						writeSubvxvy(usereg1, indexreg); // *13
						writeAddI(usereg1);

					}
					break;
				case 14:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, usereg2); // *12
						writeAddvxvy(usereg1, usereg2); // *14
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeDup(usereg1); // *16
						writeSubvxvy(usereg1, indexreg); // *15
						writeSubvxvy(usereg1, indexreg); // *15
						writeAddI(usereg1);

					}
					break;
				case 15:
					if (usereg2 != -1) {
						checkusereg2(usereg1, usereg2);
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeLdRegV0(usereg2, usereg1);
						writeDup(usereg1); // *8
						writeAddvxvy(usereg1, usereg2); // *12
						writeAddvxvy(usereg1, usereg2); // *14
						writeAddvxvy(usereg1, indexreg); // *15
						writeAddI(usereg1);
					} else {
						writeDup(usereg1); // *2
						writeDup(usereg1); // *4
						writeDup(usereg1); // *8
						writeDup(usereg1); // *16
						writeSubvxvy(usereg1, indexreg); // *15
						writeAddI(usereg1);

					}
					break;
				}

			}

			break;
		case plusassign: // Fx1E - ADD I, Vx
			nextToken(token);
			int ireg = regNr(token);
			if (ireg == -1) {
				error("Expected register");

			} else {
				writeCode(0xF, ireg, 0x1e);
			}
		}
	}

	private void writeSubvxvy(int reg1, int reg2) {
		writeCode(0x8, reg1, reg2, 5);

	}

	private void checkusereg2(int usereg1, int usereg2) {
		if (usereg1 == -1 || usereg2 == -1)
			error("expected using with at leat one register");

	}

	private void writeAddvxvy(int reg1, int reg2) {
		writeCode(0x8, reg1, reg2, 4);

	}

	private void writeLdRegV0(int reg1, int reg2) {
		writeCode(0x8, reg1, reg2, 0); // vx := v0
	}

	private void writeAddI(int usereg1) {
		writeCode(0xf, usereg1, 0x1e);

	}

	private void writeDup(int usereg1) {
		writeCode(0x8, usereg1, usereg1, 4);
	}

	private void checkusereg1(int usereg1) {
		if (usereg1 == -1)
			error("expected using with at leat one register");

	}

	/*
	 * Expr in this case is a constant expression, we return a number. If the first
	 * token is a register, we return the token but set the register number in the
	 * token to a value 0..15 instead of -1
	 * 
	 * The expression constist of numbers and labels, for example sprite1: db 1,2,3
	 * sprite2: db 3,4,5 ld v0, sprite2-sprite1 ; = 3 ld vo, (sprite2-sprite1) * 10;
	 * = 30
	 */

	private void expr(CToken token) {
		mExprLabel = null;
		mExprLabelCount = 0;
		if (nextToken(token)) {

			token.register = regNr(token);

			if (token.register != -1) {
				return;
			}
			Stack<Integer> stack = new Stack<>();

			expr0(stack, token);

			if (stack.size() == 1) {
				if (!mTokenizer.hasUngetToken())
					mTokenizer.ungetToken(token);
				token.token = Token.number;
				token.iliteral = stack.pop().intValue();
				token.literal = String.format("%d", token.iliteral);
			}
			return;
			// todo: Expression evaluation here
		} else
			token.token = Token.invalid;
	}

	private void expr0(Stack<Integer> stack, CToken token) {
		try {
			term(stack, token);
			int a, b;
			while (true) {
				switch (token.token) {
				case plus:
					match(token, token.token);
					term(stack, token);
					a = stack.pop().intValue();
					b = stack.pop().intValue();
					stack.push(a + b);
					break;
				case minus:
					match(token, token.token);
					term(stack, token);
					a = stack.pop().intValue();
					b = stack.pop().intValue();
					stack.push(b - a);
					break;
				case and:
					match(token, token.token);
					term(stack, token);
					a = stack.pop().intValue();
					b = stack.pop().intValue();
					stack.push(a & b);
					break;
				case or:
					match(token, token.token);
					term(stack, token);
					a = stack.pop().intValue();
					b = stack.pop().intValue();
					stack.push(a | b);
					break;
				case xor:
					match(token, token.token);
					term(stack, token);
					a = stack.pop().intValue();
					b = stack.pop().intValue();
					stack.push(a ^ b);
					break;
				default:

					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(mTokenizer.toString());
			error(e.toString());
			throw e;
		}
	}

	private void term(Stack<Integer> stack, CToken token) {
		factor(stack, token);
		int a, b;
		while (true) {
			assert (token.token != null);
			switch (token.token) {
			case mult:
				match(token, token.token);
				factor(stack, token);
				a = stack.pop().intValue();
				b = stack.pop().intValue();
				stack.push(a * b);
				break;
			case divide:
				match(token, token.token);
				factor(stack, token);
				a = stack.pop().intValue();
				b = stack.pop().intValue();
				stack.push(b / a);
				break;
			case mod:
				match(token, token.token);
				factor(stack, token);
				a = stack.pop().intValue();
				b = stack.pop().intValue();
				stack.push(b % a);
				break;
			default:
				return;

			}
		}
	}

	private void factor(Stack<Integer> stack, CToken token) {
		CC8Label label;
		switch (token.token) {
		case minus:
			expr(token);
			if (token.token == Token.number) {
				stack.push(-token.iliteral);
			}
			break;
		case bracketopen:
			match(token, Token.bracketopen);
			expr0(stack, token);
			match(token, Token.bracketclose);
			break;
		case number:
			stack.push(token.iliteral);
			match(token, Token.number);
			break;
		case literal:

			label = getLabel(token.literal);
			mExprLabel = label;
			mExprLabelCount++;
			if (mPass == 2 && label == null)
				error("Label " + token.literal + " not found");
			stack.push(label == null ? 0 : label.mTarget);
			match(token, Token.literal);
			break;
		}
	}

	private void match(CToken token, Token matchtoken) {
		if (token.token == matchtoken) {
			nextToken(token);
		} else {
			error("Expected " + matchtoken.toString());
		}
	}

	private int nextNumber(CToken token) {
		expr(token);
		if (token.token == Token.number)
			return token.iliteral;
		else
			error("Expected Number");
		return 0;
	}

	private int nextRegister(CToken token, boolean force) {
		int r = -1;
		if (nextToken(token)) {
			r = regNr(token);
		}
		if (r == -1 && force) {
			error("Expected Regsiter");
		}
		return r;
	}

	private int labelTarget(String literal) {
		int ret = 0;
		CC8Label lbl = getLabel(literal);
		if (lbl == null && mPass == 2) {
			error("Label " + literal + " not found");
		}
		if (lbl != null)
			ret = lbl.mTarget;
		return ret;
	}

	private CC8Label labelFromString(String literal) {
		int ret = 0;
		CC8Label lbl = getLabel(literal);
		if (lbl == null && mPass == 2) {
			error("Label " + literal + " not found");
		}
		return lbl;
	}

	private void writeCode(int code, int iliteral) {
		int code1;
		if (mbCodegen) {
			code1 = (code << 4) + iliteral / 256;
			mCode[pc] = (byte) (code1 & 0xff);
			mCode[pc + 1] = (byte) (iliteral & 0xff);
			// writeSourceLine();
			pc += 2;
			mMemoryStatistic.sizeCode += 2;
		}

	}

	private void writeCode(int opcode) {
		int code1;
		if (mbCodegen) {
			mCode[pc] = (byte) (opcode >> 8);
			mCode[pc + 1] = (byte) (opcode & 0xff);
			pc += 2;
		}

	}

	private void writeSourceLine() {
		// System.out.println(String.format("write code %04x %02x %02x",pc,
		// (int)(mCode[pc] & 0xff), (int)(mCode[pc+1] & 0xff)));
		if (mbCodegen) {
			if (mPass == 2) {
				String line = levelSpace() + mTokenizer.getCurrentLine().trim();
				mDebugSource.addSourceLine(pc, line);
			}
		}

	}

	private String levelSpace() {
		String result = "";// TODO Auto-generated method stub
		if (mOptIncludeSourceLine)
			result = String.format("%-4d", mTokenizer.mLine + mTokenizer.mBaseline);
		for (int i = 0; i < Math.min(mLevel, 3); i++)
			result += "│ ";
		return result;
	}

	private void writeCode(int hihi, int hilow, int bit8) {
		int code1;
		if (mbCodegen) {
			code1 = (hihi << 4) + hilow;
			mCode[pc] = (byte) (code1 & 0xff);
			mCode[pc + 1] = (byte) (bit8 & 0xff);
			// writeSourceLine();
			pc += 2;
			mMemoryStatistic.sizeCode += 2;
		}

	}

	private void writeCode(int hihi, int hilow, int lowhi, int lowlo) {
		int code1, code2;

		if (mbCodegen) {
			code1 = (hihi << 4) + hilow;
			code2 = (lowhi << 4) + lowlo;
			mCode[pc] = (byte) (code1 & 0xff);
			mCode[pc + 1] = (byte) (code2 & 0xff);
			// writeSourceLine();
			pc += 2;
			mMemoryStatistic.sizeCode += 2;
		}

	}

	private boolean expect(Token expected, CToken token) {
		if (nextToken(token)) {
			if (token.token != expected) {
				error("Erwarte " + expected.toString());
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean expectBegin() {
		CToken token = new CToken();
		if (nextToken(token)) {
			if (token.token == Token.curlybracketopen || token.token == Token.octobegin)
				return true;
		}
		return false;

	}

	private boolean expectEnd() {
		CToken token = new CToken();
		if (nextToken(token)) {
			if (token.token == Token.curlybracketclose || token.token == Token.octoend)
				return true;
		}
		return false;

	}

	private boolean expect(Token expected) {
		CToken token = new CToken();
		if (nextNonWhiteToken(token)) {
			if (token.token != expected) {
				error("Erwarte " + expected.toString());
				return false;
			}
			return true;
		}
		return false;
	}

	private int regNr(CToken token) {
		int r = -1;
		CC8Label label;

		switch (token.token) {
		case v0:
			r = 0;
			break;
		case v1:
			r = 1;
			break;
		case v2:
			r = 2;
			break;
		case v3:
			r = 3;
			break;
		case v4:
			r = 4;
			break;
		case v5:
			r = 5;
			break;
		case v6:
			r = 6;
			break;
		case v7:
			r = 7;
			break;
		case v8:
			r = 8;
			break;
		case v9:
			r = 9;
			break;
		case va:
			r = 10;
			break;
		case vb:
			r = 11;
			break;
		case vc:
			r = 12;
			break;
		case vd:
			r = 13;
			break;
		case ve:
			r = 14;
			break;
		case vf:
			r = 15;
			break;
		case octovar:
			if (mFunctionLabel != null) {
				r = mFunctionLabel.mNextRegister - 1;
			}
		case literal:
			label = getLabel(token.literal);
			if (label != null) {
				if (label.mLabelType == C8LabelType.STRUCT) {
					return label.mVariables.size() - 1;
				}
			}
		}
		if (r != -1 && mPass == 2) {
			if (token.replacement != null) {
				mDebugSource.addRegisterAlias(pc, r, token.replacement);
			}
		}
		return r;
	}

	private CC8Label getLabel(String literal) {
		String key = labelName(literal);
		CC8Label result = mLabels.get(key);
		if (result == null && mFunctionLabel != null)
			result = mLabels.get(literal);
		/*
		 * if (result != null) { if (result.mLabelType != C8LabelType.FUNCTION)
		 * result.mReferences++; }
		 */
		return result;
	}

	private void error(String string) {
		mbError = true;
		// if (mPass == 2) {
		{
			String file = "(editor)";
			if (mTokenizer.mFilename != null)
				file = mTokenizer.mFilename;
			if (mTokenizer.mHint != null)
				file += " (" + mTokenizer.mHint + ") ";
			System.out.println(String.format("Error %s(%d)/%d:%s %s", file, mTokenizer.mLine, mTokenizer.mPosInLine,
					string, mContext));
			System.out.println(mTokenizer.toString());
			if (mSBErrors != null) {
				Point p = mTokenizer.getLineFromPos();
				mSBErrors.append(String.format("Error %s %d/%d:%s %s\nline %d+%d=%d", file, p.y, p.x, string, mContext,
						mTokenizer.mBaseline, mTokenizer.mLine, mTokenizer.mBaseline + mTokenizer.mLine));
				mSBErrors.append(mTokenizer.toString() + "\n");

			}
		}
		CToken token = new CToken();
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.newline)
				break;
		}

	}

	void printToken(CToken token) {
		String line;
		switch (token.token) {
		case number:
			line = String.format("Number %d (%x)", token.iliteral, token.iliteral);
			break;
		case string:
			line = String.format("String '%s'", token.literal);
			break;
		case literal:
			line = String.format("Literal %s", token.literal);
			break;
		case comment:
			line = String.format("Comment %s", token.literal);
			break;
		case label:
			line = String.format("Label %s", token.literal);
			break;
		default:
			if (token.token == null)
				line = "Null Token";
			else
				line = String.format("Token: %s", token.token.toString());
		}
		line = String.format("%d:%d %s", mTokenizer.mLine + mTokenizer.mBaseline, mTokenizer.mPosInLine, line);
		System.out.println(line);

	}

	public void assemble(String text, String filename) {
		Assemble(text, filename);
	}

	public int getCodeSize() {
		return pc - 0x200;
	}

	public byte[] getCode() {
		return mCode;
	}

}
