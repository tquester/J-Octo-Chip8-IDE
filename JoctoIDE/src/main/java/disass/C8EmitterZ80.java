package disass;

import java.util.ArrayList;

import disass.COpcodeTable.Dialect;

public class C8EmitterZ80 extends C8DisassEmitter {

	static final boolean newFormat = true;
	COpcodeTable mOpcodeTable = new COpcodeTable();
	static boolean vfIsB = true; // optimization: uses b register instead of (ix+reg_vf)
	static boolean v0isC = true;
	public boolean newShift = true;
	public boolean newAddI = false;

	ArrayList<CZ80Command> mZ80Commands = new ArrayList<>();

	class CZ80Command {
		String label = null;
		String command = null;
		String par1 = null;
		String par2 = null;
		String comment = null;	
		public int pos;

		public CZ80Command(int pos, String label, String command, String par1, String par2, String comment) {
			this.pos = pos;
			this.label = label;
			this.command = command;
			this.par1 = par1;
			this.par2 = par2;
			this.comment = comment;
		}

		
		public String toString() {
			String result = String.format("%-25s %-8s ", label == null ? "" : label, command == null ? "" : command);
			if (par1 != null)
				result += " " + par1.trim();
			if (par2 != null)
				result += ", " + par2.trim();
			if (comment != null) {
				result = String.format("%-60s;%s", result, comment);
			}

			return result;

		}

	}


	CZ80Command addCmd(int pos, String command, String par1, String par2) {
		
		if (mZ80Commands.size() > 0) {
			CZ80Command lastCmd = mZ80Commands.get(mZ80Commands.size()-1);
			if (command.compareTo("ld") == 0 && 
				lastCmd.command.compareTo("ld") == 0 &&
				par1.compareTo(lastCmd.par2) == 0 &&
				par2.compareTo(lastCmd.par1) == 0) {
					return lastCmd;
			}
		}
		
		
		CZ80Command result = new CZ80Command(pos, null, command, par1, par2, null);
		mZ80Commands.add(result);
		return result;

	}

	CZ80Command addCmd(int pos, String command, String par1) {
		String par2 = null;
		
		if (mZ80Commands.size() > 0) {
			CZ80Command lastCmd = mZ80Commands.get(mZ80Commands.size()-1);
			
			if ((	command.compareTo("call") == 0 || 
					command.compareTo("jp") == 0 || 
					command.compareTo("ret") == 0)) {
				if (lastCmd.par2 != null) {
					if (lastCmd.par2.startsWith("zskip")) {
						String cond = null;
						if (lastCmd.par1.compareTo("z") == 0) cond = "nz";
						else if (lastCmd.par1.compareTo("nz") == 0) cond = "z";
						if (cond != null) {
							par2 = par1;
							par1 = cond;
							//lastCmd.comment = lastCmd.toString();
							lastCmd.command = null;
							lastCmd.par1 = null;
							lastCmd.par2 = null;
						}
					}
				}
			}
		}		
		CZ80Command result = new CZ80Command(pos, null, command, par1, par2, null);
		mZ80Commands.add(result);
		return result;

	}

	CZ80Command addCmd(int pos, String command) {
		CZ80Command result = new CZ80Command(pos, null, command, null, null, null);
		mZ80Commands.add(result);
		return result;

	}

	public C8EmitterZ80() {
		disassFormat = false;
		hexadecimal = true;
	}

	@Override
	public boolean wantsSkipLabels() {
		return true;
	}

	@Override
	protected void clear() {

	}

	@Override
	public int emitOpcode(byte[] code, int pos) {

		int startpos = pos;
		int high = code[pos] & 0xff;
		int low = code[pos + 1] & 0xff;
		pos += 2;
		byte highnib = (byte) (high >>> 4);
		byte lownib1 = (byte) (low >> 4);
		byte lownib2 = (byte) (low & 0x0f);
		byte high2 = (byte) (high & 0xf);
		ArrayList<String> listCmds = new ArrayList<>();
		String comment = null;
		int firstCmd = mZ80Commands.size();
		switch (highnib) {
		case 0x00:
			switch (low) {
			case 0xE0:
				addCmd(pos,"call", "clearScreenChip8");
				addCmd(pos,"call", "updateGameScreen");
				listCmds.add("call clearScreenChip8");
				listCmds.add("call updateGameScreen");
				break;
			case 0xEE:
				addCmd(pos,"ret");
				listCmds.add("ret");
				break;
			case 0xff:
				addCmd(pos,"ld", "a", "1");
				addCmd(pos,"call", "setRes");
				listCmds.add("ld a,1");
				listCmds.add("call setRes");
				break;
			default:
				listCmds.add(String.format("unknown %02x%02x", high, low));

			}
			break;
		case 0x01: {
			int adr = high2 * 256 + low;
			addCmd(pos,"jp", z80lbl(lbladr(adr)));
			listCmds.add(String.format("jp     %s", z80lbl(lbladr(adr))));
			break;
		}
		case 0x02: {
			int adr = high2 * 256 + low;
			addCmd(pos,"call", z80lbl(lbladr(adr)));
			listCmds.add(String.format("call   %s", z80lbl(lbladr(adr))));
			break;
		}
		case 0x03:
			addCmd(pos,"ld", "a", ixreg(high2));
			listCmds.add(String.format("ld  a, %s", ixreg(high2)));
			emitCp(pos, listCmds, low);

			addCmd(pos,"jr", "z", z80lbl(lbladr(skip(pos, code))));
			listCmds.add(String.format("jr  z,%s", z80lbl(lbladr(skip(pos, code)))));
			break;
		case 0x04:

			addCmd(pos,"ld", "a", ixreg(high2));
			listCmds.add(String.format("ld  a, %s", ixreg(high2)));
			emitCp(pos, listCmds, low);
			addCmd(pos,"jr", "nz", z80lbl(lbladr(skip(pos, code))));
			listCmds.add(String.format("jr  nz,%s", z80lbl(lbladr(skip(pos, code)))));
			break;
		case 0x05:
			addCmd(pos,"ld", "a", ixreg(high2));
			addCmd(pos,"cp", ixreg(lownib1));
			addCmd(pos,"jr", "z", z80lbl(lbladr(skip(pos, code))));

			listCmds.add(String.format("ld  a, %s", ixreg(high2)));
			listCmds.add(String.format("cp %s", ixreg(lownib1)));
			listCmds.add(String.format("jr  z,%s", z80lbl(lbladr(skip(pos, code)))));
			break;
		case 0x06:
			addCmd(pos,"ld", ixreg(high2), number(low));

			listCmds.add(String.format("ld  %s,%s", ixreg(high2), number(low)));
			break;
		case 0x07:
			if (low == 1) {
				addCmd(pos,"inc", ixreg(high2));
			} else if (low == 255) {
				addCmd(pos,"dec", ixreg(high2));
			} else {
				addCmd(pos,"ld", "a", number(low));
				addCmd(pos,"add", ixreg(high2));
				addCmd(pos,"ld", ixreg(high2), "a");
			}

			listCmds.add(String.format("ld  a, %s", number(low)));
			listCmds.add(String.format("add %s", ixreg(high2)));
			listCmds.add(String.format("ld	%s,a", ixreg(high2)));
			break;
		case 0x08:
			switch (lownib2) {
			case 0x00: // 0x8xy0
				if ((v0isC && lownib1 == 0) || (vfIsB && lownib1 == 15)) {
					addCmd(pos,"ld", ixreg(high2), ixreg(lownib1));
					listCmds.add(String.format("ld  %s,%s", ixreg(high2), ixreg(lownib1)));

				} else {
					addCmd(pos,"ld", "a", ixreg(lownib1)); // vx := vy
					addCmd(pos,"ld", ixreg(high2), "a");

					listCmds.add(String.format("ld  a, %s", ixreg(lownib1)));
					listCmds.add(String.format("ld  %s,a", ixreg(high2)));
				}
				break;
			case 0x01: // 0x8xy1 vx := vx or vy

				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"or", ixreg(lownib1));
				addCmd(pos,"ld", ixreg(high2), "a");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("or  %s", ixreg(lownib1)));
				listCmds.add(String.format("ld  %s,a", ixreg(high2)));
				break;
			case 0x02: // 0x8xy2 vx := vx and vy
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"and", ixreg(lownib1));
				addCmd(pos,"ld", ixreg(high2), "a");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("and %s", ixreg(lownib1)));
				listCmds.add(String.format("ld  %s,a", ixreg(high2)));
				break;
			case 0x03: // 0x8xy3 vx := vx xor vy
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"xor", ixreg(lownib1));
				addCmd(pos,"ld", ixreg(high2), "a");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("xor %s", ixreg(lownib1)));
				listCmds.add(String.format("ld  %s,a", ixreg(high2)));
				break;
			case 0x04: // 0x8xy3 vx := vx add vy
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"add", ixreg(lownib1));

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("add %s", ixreg(lownib1)));
				if (high2 != 15) { // Do not store the result if it is vf,
					addCmd(pos,"ld", ixreg(high2), "a");
					listCmds.add(String.format("ld	%s,a", ixreg(high2))); // because it will be overwritten by flag
																			// calculation
				}

				addCmd(pos,"ld", "a", "0");
				addCmd(pos,"adc", "a");
				addCmd(pos,"ld", ixreg(15), "a");

				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc a"));
				listCmds.add(String.format("ld  %s,a", ixreg(15)));
				break;
			case 0x05: // 0x8xy3 vx := vx sub vy
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"sub", ixreg(lownib1));

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("sub %s", ixreg(lownib1)));
				if (high2 != 15) { // Do not store the result if it is vf,
					addCmd(pos,"ld", ixreg(high2), "a");
					listCmds.add(String.format("ld	%s,a", ixreg(high2))); // because it will be overwritten by flag
																			// calculation
				}

				addCmd(pos,"ld", "a", "0");
				addCmd(pos,"adc", "a");
				addCmd(pos,"xor", "1");
				addCmd(pos,"ld", ixreg(15), "a");

				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc a"));
				listCmds.add(String.format("xor 1"));
				listCmds.add(String.format("ld  %s,a", ixreg(15)));
				break;
			case 0x06: // 0x8xy3 vx := vx >> vy
				if (newShift) {
					addCmd(pos,"ld", "a", ixreg(high2));
					listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				} else {
					addCmd(pos,"ld", "a", ixreg(lownib1));
					listCmds.add(String.format("ld  a, %s", ixreg(lownib1)));
				}

				addCmd(pos,"sra", "a");
				listCmds.add(String.format("sra a"));
				if (high2 != 15) { // Do not store the result if it is vf,
					addCmd(pos,"ld", ixreg(high2), "a");
					listCmds.add(String.format("ld	%s,a", ixreg(high2)));
				}

				addCmd(pos,"ld", "a", "0");
				addCmd(pos,"adc", "a");
				addCmd(pos,"ld", ixreg(15), "a");

				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc a"));
				listCmds.add(String.format("ld  %s,a", ixreg(15)));
				break;
			case 0x07: // 0x8xy7 vx := vx << vy

				addCmd(pos,"ld", "a", ixreg(lownib1));
				addCmd(pos,"sub", ixreg(high2));
				listCmds.add(String.format("ld  a, %s", ixreg(lownib1)));
				listCmds.add(String.format("sub %s", ixreg(high2)));
				if (high2 != 15) { // Do not store the result if it is vf,
					addCmd(pos,"ld", ixreg(high2), "a");
					listCmds.add(String.format("ld	%s,a", ixreg(high2)));
				}
				addCmd(pos,"ld", "a", "0");
				addCmd(pos,"adc", "a");
				addCmd(pos,"xor", "1");
				addCmd(pos,"ld", ixreg(15), "a");

				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc a"));
				listCmds.add(String.format("xor 1"));
				listCmds.add(String.format("ld  %s,a", ixreg(15)));
				break;
			case 0x0E:
				if (newShift) {
					addCmd(pos,"ld", "a", ixreg(high2));
					listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				} else {
					addCmd(pos,"ld", "a", ixreg(lownib1));
					listCmds.add(String.format("ld  a, %s", ixreg(lownib1)));
				}
				listCmds.add(String.format("sla a"));

				addCmd(pos,"sla", "a");
				addCmd(pos,"ld", ixreg(high2), "a");
				addCmd(pos,"ld", "a", "0");
				addCmd(pos,"adc", "a");
				addCmd(pos,"ld", ixreg(15), "a");

				listCmds.add(String.format("ld	%s,a", ixreg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc a"));
				listCmds.add(String.format("ld  %s,a", ixreg(15)));
				break;
			}
			break;
		case 0x09: // 9xy0 SNE Vx, Vy

			addCmd(pos,"ld", "a", ixreg(high2));
			addCmd(pos,"cp", ixreg(lownib1));
			addCmd(pos,"jr", "nz", z80lbl(lbladr(skip(pos, code))));

			listCmds.add(String.format("ld  a, %s", ixreg(high2)));
			listCmds.add(String.format("cp  %s", ixreg(lownib1)));
			listCmds.add(String.format("jr  nz,%s", z80lbl(lbladr(skip(pos, code)))));
			break;
		case 0x0A: { // Annn - LD I, addr
			int adr = high2 * 256 + low;
			addCmd(pos,"ld", "iy", z80lbl(lbladr(adr)));

			listCmds.add(String.format("ld  iy, %s", z80lbl(lbladr(adr))));
			break;
		}

		case 0x0B: {
			int adr = high2 * 256 + low;
			addCmd(pos,"error", "not supported", "bxxx");
			listCmds.add("Not supported: bxxxx");
			break;
		}
		case 0x0C:
			addCmd(pos,"call", "xrnd");
			addCmd(pos,"ld", "a", "l");
			addCmd(pos,"and", number(low));
			addCmd(pos,"ld", ixreg(high2), "a");

			// listCmds.add(String.format("ld a, r"));
			listCmds.add(String.format("call xrnd"));
			listCmds.add(String.format("ld  a, l"));
			listCmds.add(String.format("and %s", number(low)));
			listCmds.add(String.format("ld	%s,a", ixreg(high2)));
			break;
		case 0x0D:
			if (v0isC) {
				addCmd(pos,"push", "bc");
				listCmds.add("push  bc");
			}

			addCmd(pos,"ld", "a", String.format("%d", lownib2));
			addCmd(pos,"ld", "b", ixreg(high2));
			addCmd(pos,"ld", "c", ixreg(lownib1));
			addCmd(pos,"ld", "hl", "iy");
			addCmd(pos,"call", "chip8sprite");

			listCmds.add(String.format("ld  a, %d", lownib2));
			listCmds.add(String.format("ld  b, %s", ixreg(high2)));
			listCmds.add(String.format("ld  c, %s", ixreg(lownib1)));
			listCmds.add(String.format("ld	hl, iy"));
			listCmds.add(String.format("call  chip8sprite"));

			if (v0isC) {
				addCmd(pos,"pop", "bc");
				listCmds.add("pop  bc");
			}
			if (vfIsB) {
				addCmd(pos,"ld", "b", "(ix+reg_vf)");
				listCmds.add(String.format("ld  b, (ix+reg_vf)"));
			}
			break;
		case 0x0E:
			switch (low) {
			case 0x9e:
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"call", "checkKey");
				addCmd(pos,"jr", "z", z80lbl(lbladr(skip(pos, code))));

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("call checkKey"));
				listCmds.add(String.format("jr  z,%s", z80lbl(lbladr(skip(pos, code)))));
				break;

			case 0xa1:
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"call", "checkKey");
				addCmd(pos,"jr", "nz", z80lbl(lbladr(skip(pos, code))));

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("call checkKey"));
				listCmds.add(String.format("jr  nz,%s", z80lbl(lbladr(skip(pos, code)))));
				break;
			}
			break;
		case 0x0F:
			switch (low) {
			case 0x07:
				addCmd(pos,"call", "vinterrupt");
				addCmd(pos,"ld", "a", "(ix+reg_delay)");
				addCmd(pos,"ld", ixreg(high2), "a");

				listCmds.add(String.format("call vinterrupt"));
				listCmds.add(String.format("ld  a,(ix+reg_delay)"));
				listCmds.add(String.format("ld  %s,a", ixreg(high2)));
				break;
			case 0x0a:
				addCmd(pos,"call", "GetKey");
				addCmd(pos,"ld", ixreg(high2), "a");

				listCmds.add(String.format("call  GetKey"));
				listCmds.add(String.format("ld	%s,a", ixreg(high2)));
				break;
			case 0x15:

				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"ld", "(ix+reg_delay)", "a");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("ld  (ix+reg_delay),a"));
				break;
			case 0x18:
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"ld", "(ix+reg_sound)", "a");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("ld  (ix+reg_sound),a"));
				break;
			case 0x1e: // Fx1E - ADD I, Vx
				addCmd(pos,"ld", "d", "0");
				addCmd(pos,"ld", "e", ixreg(high2));
				addCmd(pos,"add", "iy", "de");

				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,%s", ixreg(high2)));
				listCmds.add(String.format("add iy,de"));
				comment = String.format("i += %s", ixreg(high2));
				break;
			case 0x29: // Fx29 - LD F, Vx
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"push", "bc");
				addCmd(pos,"ld", "b", "a");
				addCmd(pos,"add", "a");
				addCmd(pos,"add", "a");
				addCmd(pos,"add", "b");
				addCmd(pos,"ld", "d", "0");
				addCmd(pos,"ld", "e", "a");
				addCmd(pos,"ld", "hl", "chip8Font");
				addCmd(pos,"add", "hl", "de");
				addCmd(pos,"ld", "iy", "hl");
				addCmd(pos,"pop", "bc");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("ld  b,a"));
				listCmds.add(String.format("add a; *2"));
				listCmds.add(String.format("add a; *4"));
				listCmds.add(String.format("add  b; *5"));
				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,a"));
				listCmds.add(String.format("ld  hl,chip8Font"));
				listCmds.add(String.format("add  hl,de"));
				listCmds.add(String.format("ld  iy,hl"));
				comment = String.format("i := char %s", ixreg(high2));
				break;
			case 0x30:
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"push", "bc");
				addCmd(pos,"add", "a");
				addCmd(pos,"ld", "b", "a");
				addCmd(pos,"add", "a");
				addCmd(pos,"add", "a");
				addCmd(pos,"add", "b");
				addCmd(pos,"ld", "d", "0");
				addCmd(pos,"ld", "e", "a");
				addCmd(pos,"ld", "hl", "bigfont");
				addCmd(pos,"add", "hl", "de");
				addCmd(pos,"ld", "iy", "hl");
				addCmd(pos,"pop", "bc");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("add a; *2"));
				listCmds.add(String.format("ld  b,a"));
				listCmds.add(String.format("add a; *4"));
				listCmds.add(String.format("add a; *8"));
				listCmds.add(String.format("add  b; *10"));
				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,a"));
				listCmds.add(String.format("ld  hl,bigfont"));
				listCmds.add(String.format("add  hl,de"));
				listCmds.add(String.format("ld  iy,hl"));
				comment = String.format("i := char %s", ixreg(high2));
				break;
			case 0x33:
				addCmd(pos,"ld", "a", ixreg(high2));
				addCmd(pos,"ld", "hl", "iy");
				addCmd(pos,"call", "bcd");

				listCmds.add(String.format("ld  a, %s", ixreg(high2)));
				listCmds.add(String.format("ld  hl, iy"));
				listCmds.add(String.format("call bcd"));

				comment = String.format("bcd    %s", ixreg(high2));
				break;

			case 0x55:
				if (high2 == 0) {
					if (v0isC) {
						addCmd(pos,"ld", "(iy)", "c");
						addCmd(pos,"inc", "iy");

						listCmds.add("ld  (iy),c");
						listCmds.add("inc   iy");
					} else {
						addCmd(pos,"ld", "a", ixreg(0));
						addCmd(pos,"ld", "(iy)", "a");
						addCmd(pos,"inc", "iy");

						listCmds.add(String.format("ld  a,%s", ixreg(0)));
						listCmds.add(String.format("ld  (iy),a"));
						listCmds.add(String.format("inc  iy"));
					}

				} else {
					if (v0isC) {
						addCmd(pos,"ld", "(ix+reg_v0)", "c");
						listCmds.add("ld  (ix+reg_v0),c");
					}
					if (vfIsB) {
						addCmd(pos,"push", "bc");
						listCmds.add("push  bc");
					}

					addCmd(pos,"ld", "hl", "ix");
					addCmd(pos,"ld", "de", "iy");
					addCmd(pos,"ld", "bc", String.format("%d", high2 + 1));
					addCmd(pos,"add", "iy", "bc");

					listCmds.add(String.format("ld  hl, ix"));
					listCmds.add(String.format("ld  de, iy"));
					listCmds.add(String.format("ld  bc,%d", high2 + 1));
					listCmds.add(String.format("add iy, bc"));
					if (high2 == 15 && vfIsB) {
						addCmd(pos,"ld", "(ix+reg_vf", "b");
						listCmds.add(String.format("ld  (ix+reg_vf),b"));
					}
					addCmd(pos,"ldir");
					listCmds.add(String.format("ldir"));
					if (vfIsB) {
						addCmd(pos,"pop", "bc");
						listCmds.add("pop  bc");
					}
				}
				comment = String.format("save   %s", ixreg(high2));
				break;
			case 0x65:
				if (high2 == 0) {
					if (v0isC) {
						addCmd(pos,"ld", "c", "(iy)");
						addCmd(pos,"inc", "iy");

						listCmds.add("ld  c,(iy)");
						listCmds.add("inc   iy");

					} else {
						addCmd(pos,"ld", "a", "(iy)");
						addCmd(pos,"ld", ixreg(0), "a");
						addCmd(pos,"inc", "iy");

						listCmds.add(String.format("ld  a,(iy)"));
						listCmds.add(String.format("ld  %s,a", ixreg(0)));
						listCmds.add(String.format("inc  iy"));
					}

				} else {

					if (vfIsB) {
						addCmd(pos,"push", "bc");
						listCmds.add("push  bc");
					}

					addCmd(pos,"ld", "de", "ix");
					addCmd(pos,"ld", "hl", "iy");
					addCmd(pos,"ld", "bc", String.format("%d", high2 + 1));
					addCmd(pos,"add", "iy", "bc");
					addCmd(pos,"ldir");

					listCmds.add(String.format("ld  de, ix"));
					listCmds.add(String.format("ld  hl, iy"));
					listCmds.add(String.format("ld  bc,%d", high2 + 1));
					listCmds.add(String.format("add iy, bc"));
					listCmds.add(String.format("ldir"));
					if (high2 == 15 && vfIsB) {
						addCmd(pos,"ld", "b", "(ix+reg_vf)");
						listCmds.add(String.format("ld  b, (ix+reg_vf)"));
					}
					if (vfIsB) {
						addCmd(pos,"pop", "bc");
						listCmds.add("pop  bc");
					}
					if (v0isC) {
						addCmd(pos,"ld", "c", "(ix+reg_v0)");
						listCmds.add("ld  c,(ix+reg_v0)");
					}
				}
				comment = String.format("load   %s", ixreg(high2));
				break;
			case 0xE0:
				switch (high2) {
				case 0:
					addCmd(pos,"call", "f0fast");
					listCmds.add(String.format("call f0fast"));
					comment = "fast";
					break;
				case 1:
					addCmd(pos,"call", "f0slow");
					listCmds.add(String.format("call f0slow"));
					comment = "slow";
					break;
				case 2:
					addCmd(pos,"call", "f0redraw");
					listCmds.add(String.format("call f0redraw"));
					comment = "redraw";
					break;

				}

			}
			break;

		}
		CZ80Command startCommand = mZ80Commands.get(firstCmd);
		CC8Label lbl = mLabels.get(startpos);
		String strlbl = "";
		if (lbl != null) {
			if (lbl.mTarget == 0x027f) {
				System.out.println("stop");
			}
			strlbl = z80lbl(lbl.toString()) + ":";
		}
		if (!strlbl.isEmpty())
			startCommand.label = strlbl;

		int op = high * 256 + low;
		String disass = mOpcodeTable.decode(op, Dialect.OCTO, mLabels);
		if (disass == null)
			disass = comment;
		comment = String.format("%04x %02x %02x\t%s", startpos, high, low, disass);
		startCommand.comment = comment;
		
//		mSB.append("\t;"+comment+"\n");

		if (newFormat) {
			//for (int i = firstCmd; i < mZ80Commands.size(); i++) {
			//	emitLine(pos, mZ80Commands.get(i).toString());
			//}
		} else {
			if (listCmds.size() > 0) {
				String cmd = listCmds.get(0);
				line = String.format("%-10s        %s", strlbl, cmd);
				if (comment != null)
					line = String.format("%-65s;%s", line, comment);
				emitLine(pos, line);
				for (int i = 1; i < listCmds.size(); i++) {
					line = String.format("                  %s", listCmds.get(i));
					emitLine(pos, line);
				}
			} else
				emitLine(pos, String.format("***** unknown command %02x %02x**** \n", high, low));
		}
		// System.out.println(line);
//		mSB.append(line + "\n");

		return pos;
	}
	
	public void writeSourcecode() {
		for (int i = 0; i < mZ80Commands.size(); i++) {
			CZ80Command cmd = mZ80Commands.get(i);
			if (cmd.command != null)
				emitLine(cmd.pos, cmd.toString());
		}
		
	}

	String ixreg(int regnr) {
		String result;
		result = String.format(" (ix+reg_%s)", reg(regnr));
		if (regnr == 15 && vfIsB)
			result = "b";
		if (regnr == 0 && v0isC)
			result = "c";
		return result;
	}

	private void emitCp(int pos, ArrayList<String> listCmds, int param) {
		switch (param) {
		case 0:
			addCmd(pos,"or", "a");
			listCmds.add(String.format("or  a"));
			break;
		case 1:
			addCmd(pos,"dec", "a");
			listCmds.add(String.format("dec a"));
			break;
		default:
			addCmd(pos,"cp", number(param));
			listCmds.add(String.format("cp  %s", number(param)));
		}

	}

	private String z80lbl(String label) {
		return "z" + label.replaceAll("-", "_");
	}

	private int skip(int pos, byte[] code) {

		return pos + 2;
	}

	String bin(int data) {
		String comment = Integer.toBinaryString(data).replaceAll("0", " ").replaceAll("1", String.format("%c", 0x2588));
		while (comment.length() < 8)
			comment = "_" + comment;
		return comment;
	}

	char chr(int data) {
		if (data >= 32 && data <= 127)
			return (char) data;
		return 32;
	}

	@Override
	protected int emitdb(byte[] chip8Memory, int pc, CC8Label label) {
		try {
			int data = chip8Memory[pc] & 0xff;
			int data2;
			int itemsPerRow = 1;
			CC8Label lbl = mLabels.get(pc);
			pc++;
			String strlbl = "";
			if (lbl != null) {
				strlbl = z80lbl(lbl.toLabelString());
				if (lbl.mLabelType == C8LabelType.SKIP)
					strlbl = "";
				label = lbl;
			}

			C8LabelType type = C8LabelType.DATA;
			if (newFormat) {
				
			} else {
				type = C8LabelType.DATA;
				if (disassFormat) {
					if (strlbl.length() < 16)
						line = String.format("%04x %-16s             ", pc, strlbl);
					else {
						line = String.format("%04x %s             ", pc, strlbl);
						emitLine(pc, line);
						line = String.format("%04x %-16s             ", pc, "");
					}
				} else {
					if (strlbl.length() < 16)
						line = String.format("%-16s        ", strlbl);
					else {
						line = String.format("%s        ", strlbl);
						emitLine(pc, line);
						line = String.format("%-16s        ", "");

					}
				}
				
			}

			if (label != null) {
				type = label.mLabelType;
				itemsPerRow = label.mItemsPerRow;
			}
			String strCmd = "";
			String strComment = "";

			switch (type) {
			case HEX:
			case DATA:
				if (itemsPerRow == 1) {
					strCmd = String.format("db 0x%02x", data);
					strComment = String.format(";%s %c", bin(data), chr(data));
				} else {
					strCmd = String.format("db 0x%02x", data);
					strComment = String.format("\t;%c", chr(data));
					for (int i = 1; i < itemsPerRow; i++) {
						CC8Label lbl2 = mLabels.get(pc);
						if (lbl2 != null)
							break;
						if (mSetVisited.contains(pc))
							break;

						data = chip8Memory[pc] & 0xff;
						pc++;
						strCmd += String.format(", 0x%02x", data);
						strComment += String.format("%c", chr(data));
					}
				}
				break;
			case SPRITE8:
				strCmd = String.format("db 0x%02x", data);
				strComment = String.format(";%s", bin(data));
				break;
			case SPRITE16:
				data2 = chip8Memory[pc] & 0xff;
				pc++;
				strCmd = String.format("db 0x%02x, 0x%02x", data, data2);
				strComment = String.format(";%s%s", bin(data), bin(data2));
				break;
			default:
				strCmd = String.format("db 0x%02x", data);
				strComment = String.format(";%s %c", bin(data), chr(data));
				break;
			}
			if (newFormat) {
				CZ80Command cmd = addCmd(pc,strCmd);
				if (strlbl != null) cmd.label = strlbl;
				if (strComment != null) cmd.comment = strComment;
				//emitLine(pc,cmd.toString());
			} else {
				line += strCmd + "\t" + strComment;

				// System.out.println(line);
				emitLine(pc, line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pc;
	}

}
