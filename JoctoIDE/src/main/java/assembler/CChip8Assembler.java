package assembler;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;

import disass.C8DisassEmitter;
import disass.C8LabelType;
import disass.CC8Label;
import disass.Tools;
import emulator.C8DebugSource;

public class CChip8Assembler {
	public C8DebugSource mDebugSource = new C8DebugSource();
	C8DisassEmitter mEmitter = new C8DisassEmitter();
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
	
	void initData() {
		mStackLoopData.clear();
		mBeginEndStack.clear();
		mMapMacros.clear();
		mLabels.clear();

	}

	public String getErrors() {
		return mSBErrors == null ? "" : mSBErrors.toString();
	}

	public void Assemble(String code, String filename) {
		try {

			mTokenizer.start(code);
			pc = 0x200;
			mSBErrors = null;
			mPass = 1;
			System.out.println("Pass 1");
			while (mTokenizer.hasData()) {
				try {
					assembleLine();
				} catch (Exception e) {
					error(e.getLocalizedMessage());
					e.printStackTrace();
					break;
				}
			}
			mTokenizer.start(code);
			pc = 0x200;
			mPass = 2;
			mSBErrors = new StringBuilder();
			// mTokenizer.deleteAllAlias();
			System.out.println("Pass 1");
			while (mTokenizer.hasData()) {
				try {
					assembleLine();
				} catch (Exception e) {
					error(e.getLocalizedMessage());
					e.printStackTrace();
					break;
				}
			}
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

	private void assembleLine() {
		try {
			while (mTokenizer.getToken(token)) {
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
		int reg1;
		int reg2;
		int nr;

		String astr;
		CBeginEndData beginEndData;

		if (mOptAnnotateAllLines) {
			if (token.token != Token.comment && token.token != Token.alias && token.token != Token.macro)
				writeSourceLine();
		}
		switch (token.token) {
		case label:
			label = mLabels.get(token.literal);
			if (label == null) {
				label = new CC8Label();
				label.mName = token.literal;
				label.mTarget = pc;
				System.out.println(String.format("Label %s = %04x", token.literal, pc));
				mLabels.put(token.literal, label);
			}
			break;
		case org:
			expr(token);
			if (token.token == Token.number) {
				pc = token.iliteral;
			} else
				error("Expected: Number");
			break;
		case ld:
			mTokenizer.getToken(token);
			token1 = token.token;
			reg1 = regNr(token1);
			expect(Token.comma);
			// mTokenizer.getToken(token);
			expr(token);
			token2 = token.token;
			reg2 = regNr(token2);
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
			// mTokenizer.getToken(token);
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
			} else if (token.token == Token.v0) {
				expect(Token.comma);
				mTokenizer.getToken(token);
				if (token.token == Token.literal) {
					writeCode(0x0b, labelTarget(token.literal)); // bnnn JP nnn+V0
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
			} else
				error("Expected label or number");
			break;

		case call:
			expr(token);
			if (token.token == Token.number) {
				writeCode(0x02, token.iliteral); // 2nnn CALL
			} else if (token.token == Token.literal) {
				writeCode(0x02, labelTarget(token.literal)); // 2nnn CALL
			} else
				error("Expected label or number");
			break;
		case se:
//    3xkk - SE Vx, byte
//     5xy0 - SE Vx, Vy

			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (reg1 == -1) {
					error("Expected Register");
					break;
				}
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					if (token.token == Token.number) {
						writeCode(0x03, reg1, token.iliteral); // 3xkk SE Vx, kk
					} else {
						reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (reg1 == -1) {
					error("Expected Register");
					break;
				}
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {

					if (token.token == Token.number) {
						writeCode(0x04, reg1, token.iliteral); // 9xkk SNE Vx, kk
					} else {
						reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (token.token == Token.i) {
					mTokenizer.getToken(token);
					reg1 = regNr(token.token);
					if (reg1 == -1) {
						error("Expected expression");
						break;
					}
					// Fx1E - ADD I, Vx

					writeCode(0xF, reg1, 0x1e);

				} else {
					if (mTokenizer.getToken(token)) {
						if (token.token == Token.number) {
							writeCode(0x07, reg1, token.iliteral); // 7xkk ADD Vx,kk
						} else {
							reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				if (!expect(Token.comma))
					break;
				if (mTokenizer.getToken(token)) {
					reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				reg2 = reg1;
				if (mTokenizer.getToken(token)) {
					if (token.token == Token.comma) {
						if (mTokenizer.getToken(token)) {

							reg2 = regNr(token.token);
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
			if (mTokenizer.getToken(token)) {
				reg1 = regNr(token.token);
				reg2 = reg1;
				if (mTokenizer.getToken(token)) {
					if (token.token == Token.comma) {
						if (mTokenizer.getToken(token)) {

							reg2 = regNr(token.token);
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
			while (mTokenizer.getToken(token)) {
				if (token.token != Token.number) {
					mTokenizer.ungetToken(token);
					break;
				}
				mCode[pc++] = (byte) (token.iliteral & 0xff);
				mTokenizer.getToken(token);
				if (token.token != Token.comma) {
					mTokenizer.ungetToken(token);
					break;
				}

			}
			break;
		case number:
			mCode[pc++] = (byte) (token.iliteral & 0xff);
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
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			writeCode(0xf, reg1, 0x15);
			break;
		case buzz: // Fx07 - LD Vx, DT
			expect(Token.assign);
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			writeCode(0xf, reg1, 0x18);
			break;

		case bcd:
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			writeCode(0xf, reg1, 0x33);
			break;
		case load:
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			writeCode(0xf, reg1, 0x65);
			break;

		case save:
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
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
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
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
			if (!mOptAnnotateAllLines)
				writeSourceLine();
			CToken tokenb = new CToken();
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			if (reg1 == -1) {
				error("Expected register");
				break;
			}
			mTokenizer.getToken(token);
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
			mTokenizer.getToken(tokenb);

			beginEndData = null;
			switch (tokenb.token) {
			case octobegin:
				mLevel++;
				beginEndData = new CBeginEndData();
				mBeginEndStack.push(beginEndData);
				break;
			case octothen:
				break;
			default:
				error("Expcted begin or then");

			}
			compileCompare(reg1, token, compareToken, beginEndData);

		}
			break;
		case macro:
			compileMacro();
			break;

		case comment:
			break;
		case calc:
			compileCalc(token);
			break;
		case octobyte:
			compileByte(token);
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
			mTokenizer.getToken(token);
			if (!check(token, Token.literal, "Name"))
				break;
			astr = token.literal;
			expr(token);
			if (!check(token, Token.number, "Value (Number)"))
				break;
			CC8Label newlabel = new CC8Label();
			newlabel.mName = astr;
			newlabel.mTarget = token.iliteral;
			mLabels.put(astr, newlabel);
		}
			break;

		case octowhile: {
			CToken tokenb = new CToken();
			mTokenizer.getToken(token);
			reg1 = regNr(token.token);
			if (reg1 == -1) {
				error("Expected register");
				break;
			}
			mTokenizer.getToken(token);
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
			if (mPass == 2)
				System.out.println("break");

			mTokenizer.getToken(token, false);
			if (!check(token, Token.literal, "literal"))
				break;
			strA = token.literal;
			mTokenizer.getToken(token, false);
			mTokenizer.setAlias(strA, token.literal);
//		mTokenizer.addAlias(strA, token.literal);
		}
			break;
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
			mTokenizer.getToken(token);
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
			label = mLabels.get(astr);
			if (label == null) {
				label = new CC8Label();
				mLabels.put(astr, label);
			}
			label.mName = astr;
			label.mTarget = token.iliteral;
			break;
		case dotif:
			expr(token);
			if (token.token != Token.number) {
				error("Expected constant");
				break;
			}
			mbCodegen = token.iliteral != 0;
			break;
			
		case dotelse:
			mbCodegen = !mbCodegen;
			break;
		case dotend:
			mbCodegen = true;
			break;
			

		case newline:
		case none:
			break;

		default:
			error("Undef token " + token.toString());
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
		mTokenizer.getToken(token);
		if (!check(token, Token.literal, "Label name"))
			return;
		varname = token.literal;
		mTokenizer.getToken(token);
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
			CC8Label label = mLabels.get(token.literal);
			if (label != null) {
				if (label.mValue != null)
					return label.mValue.doubleValue();
				return label.mTarget;
			}
			break;
		case number:
			return token.iliteral;
		case bracketopen:
			mTokenizer.getToken(token);
			result = parseCalc(token);
			mTokenizer.getToken(token);
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
			mTokenizer.getToken(token);
			return unaryFunction.calc(compileCalcTerminal(token));
		}
		double t = compileCalcTerminal(token);
		mTokenizer.getToken(token);
		binaryFunction = mMapBinaryFunctions.get(token.token);
		if (binaryFunction != null) {
			mTokenizer.getToken(token);
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

		mTokenizer.getToken(token);
		if (token.token != Token.literal) {
			error("Expected name");
			return;
		}
		CC8Label label = new CC8Label();
		label.mName = token.literal;
		mLabels.put(token.literal, label);
		expect(Token.curlybracketopen);
		mTokenizer.getToken(token);

		double result = parseCalc(token);
		label.mTarget = (int) result;
		label.mValue = result;

		expect(Token.curlybracketclose);

	}

	private void compileByte(CToken token2) {
		expect(Token.curlybracketopen);
		mTokenizer.getToken(token);
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
				CC8Label label = mLabels.get(token.literal);
				if (label != null) {
					if (label.mLabelType == C8LabelType.STRINGMODE) {
						compileString(label);
					} else {
						writeCode(0x2, label.mTarget);
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
		mTokenizer.getToken(token);
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

	private void compileMacroExpansion(CMacroData macroData, CToken token) {

		if (!mOptAnnotateAllLines)
			writeSourceLine();
		System.out.println("------------ Macro expansion at line " + token.line);
		CTokenizer tempTokenizer = new CTokenizer();
		tempTokenizer.mBaseline = mTokenizer.mLine;
		for (int i = 0; i < macroData.parameters.size(); i++) {
			// mTokenizer.getToken(token);
			mTokenizer.getToken(token);
			// expr(token);
			tempTokenizer.replace(macroData.parameters.get(i), token.literal);
			System.out.println(
					String.format("Param %d = %s at %d", i, token.literal, mTokenizer.mLine + mTokenizer.mBaseline));
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
		mTokenizer.getToken(token);
		if (token.token != Token.literal) {
			error("Expcted macro name");
			return;
		}
		macroData.name = token.literal;
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
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

	private void patch(int patchAdr, int pc) {
		int command = (mCode[patchAdr] << 8) & 0xf000;
		if (command != 0x1000) {
			error(String.format("Invalid patch %04x to %04x", patchAdr, pc));
			return;
		}
		command |= pc;
		mCode[patchAdr] = (byte) (command >> 8);
		mCode[patchAdr + 1] = (byte) (command & 0xff);

	}

	private void compileWhile(int reg1, CToken token, Token compareToken) {
		int reg2 = regNr(token.token);
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

	private void compileCompare(int reg1, CToken token, Token compareToken, CBeginEndData beginEndData) {
		int reg2 = regNr(token.token);
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
		int reg1 = regNr(token.token);
		int reg2;
		mTokenizer.getToken(token);
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
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 7); // 8xy7 - SUBN Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;
		case andassign:
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 2); // 8xy2 - AND Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;
		case orassign:
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 1); // 8xy1 - OR Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;

		case xorassign:
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 3); // 8xy3 - XOR Vx, Vy
			else
				error("Ecxpected Register v0..vf");
			break;

		case shrassign:
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 6); // 8xy6 - SHR Vx {, Vy}
			else
				error("Ecxpected Register v0..vf");
			break;

		case shlassign:
			mTokenizer.getToken(token);
			reg2 = regNr(token.token);
			if (reg2 != -1)
				writeCode(0x8, reg1, reg2, 0xE); // 8xyE - SHL Vx {, Vy}
			else
				error("Ecxpected Register v0..vf");
			break;

		}

	}

	private void compileFor(CToken token) {
		CBeginEndData forData = new CBeginEndData();
		mTokenizer.getToken(token);
		int regnr = regNr(token.token);
		if (regnr == -1) {
			error("Expected register");
		}
		forData.forRegister = regnr;
		expect(Token.assign);
		// mTokenizer.getToken(token);
		expr(token);
		// for v0 := 1 to ... or
		// for v0 := v1 to ... or
		if (token.token == Token.number) {
			writeCode(0x6, regnr, token.iliteral); // vx = nn
		} else {
			int reg2 = regNr(token.token);
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
			forData.forTargetReg = regNr(token.token);
			if (forData.forTargetNr == -1) {
				error("Expected number or register");
				return;
			}
		}
		mTokenizer.getToken(token);
		if (token.token == Token.octostep) {
			mTokenizer.getToken(token);
			if (token.token == Token.number) {
				forData.forStepNr = token.iliteral;
				forData.forStepReg = -1;
			} else {
				forData.forStepReg = regNr(token.token);
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
		mTokenizer.getToken(token);
		if (token.token == Token.octobegin) {
			mBeginEndStack.push(forData);
		} else {
			
			assembleStatement(token);
			compileForEnd(forData);
		}

	}

	private void compileForEnd(CBeginEndData forData) {
		if (forData.forStepReg != -1) {
			writeCode(0x8, forData.forRegister, forData.forStepReg, 4); // 8xy4 - ADD Vx, Vy
		} else {
			writeCode(0x7, forData.forRegister, forData.forStepNr); // 7xkk - ADD Vx, byte
		}
		if (forData.forTargetReg >= 0) {
			writeCode(0x5, forData.forRegister, forData.forTargetReg, 0); // 5xy0 - SE Vx, Vy
		} else {
			writeCode(0x3, forData.forRegister, forData.forTargetNr); // 3xkk - SE Vx, byte
		}
		writeCode(0x1, forData.pc);
	}

	// Annn - LD I, addr
	// Fx1E - ADD I, Vx
	// i := hex vx
	// i := bighex vx
	private void compileI(CToken token) {
		int reg1;
		mTokenizer.getToken(token);
		switch (token.token) {
		case assign: // Annn - LD I, addr
			expr(token);
			switch (token.token) {
			case number:
				writeCode(0xA, token.iliteral);
				break;
			case hex:
				mTokenizer.getToken(token);
				reg1 = regNr(token.token);
				writeCode(0xf, reg1, 0x29);
				break;
			case bighex:
				mTokenizer.getToken(token);
				reg1 = regNr(token.token);
				writeCode(0xf, reg1, 0x30);
				break;

			default:
				error("Expected constant expression");
			}
			break;
		case plusassign: // Fx1E - ADD I, Vx
			mTokenizer.getToken(token);
			int ireg = regNr(token.token);
			if (ireg == -1) {
				error("Expected register");

			} else {
				writeCode(0xF, ireg, 0x1e);
			}
		}
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
		if (mTokenizer.getToken(token)) {

			token.register = regNr(token.token);

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
			label = mLabels.get(token.literal);
			if (mPass == 2 && label == null)
				error("Label " + token.literal + " not found");
			stack.push(label == null ? 0 : label.mTarget);
			match(token, Token.literal);
			break;
		}
	}

	private void match(CToken token, Token matchtoken) {
		if (token.token == matchtoken) {
			mTokenizer.getToken(token);
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
		if (mTokenizer.getToken(token)) {
			r = regNr(token.token);
		}
		if (r == -1 && force) {
			error("Expected Regsiter");
		}
		return r;
	}

	private int labelTarget(String literal) {
		int ret = 0;
		CC8Label lbl = mLabels.get(literal);
		if (lbl != null)
			ret = lbl.mTarget;
		return ret;
	}

	private void writeCode(int code, int iliteral) {
		int code1;
		if (mbCodegen) {
			code1 = (code << 4) + iliteral / 256;
			mCode[pc] = (byte) (code1 & 0xff);
			mCode[pc + 1] = (byte) (iliteral & 0xff);
			// writeSourceLine();
			pc += 2;
		}

	}

	private void writeSourceLine() {
		// System.out.println(String.format("write code %04x %02x %02x",pc,
		// (int)(mCode[pc] & 0xff), (int)(mCode[pc+1] & 0xff)));
		if (mbCodegen) {
			if (mPass == 2) {
				String line = levelSpace() + mTokenizer.getCurrentLine().trim();
				mDebugSource.addSourceLine(pc + 2, line);
			}
		}

	}

	private String levelSpace() {
		String result = "";// TODO Auto-generated method stub
		if (mOptIncludeSourceLine)
			result = String.format("%-4d", mTokenizer.mLine + mTokenizer.mBaseline);
		for (int i = 0; i < mLevel; i++)
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
		}

	}

	private boolean expect(Token expected, CToken token) {
		if (mTokenizer.getToken(token)) {
			if (token.token != expected) {
				error("Erwarte " + expected.toString());
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean expect(Token expected) {
		CToken token = new CToken();
		if (mTokenizer.getToken(token)) {
			if (token.token != expected) {
				error("Erwarte " + expected.toString());
				return false;
			}
			return true;
		}
		return false;
	}

	private int regNr(Token token) {
		int r = -1;
		switch (token) {
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
		}
		return r;
	}

	private void error(String string) {
		if (mPass == 2) {
			System.out.println(
					String.format("Error %d/%d:%s %s", mTokenizer.mLine, mTokenizer.mPosInLine, string, mContext));
			System.out.println(mTokenizer.toString());
			if (mSBErrors != null) {
				mSBErrors.append(String.format("Error %d/%d:%s %s\n", mTokenizer.mLine, mTokenizer.mPosInLine, string,
						mContext));
				mSBErrors.append(mTokenizer.toString() + "\n");

			}
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
