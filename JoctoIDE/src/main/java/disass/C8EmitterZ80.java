package disass;

import java.util.ArrayList;

import disass.COpcodeTable.Dialect;

public class C8EmitterZ80 extends C8DisassEmitter {
	COpcodeTable mOpcodeTable = new COpcodeTable();
	public boolean newShift=true;
	public boolean newAddI=false;


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
		switch (highnib) {
		case 0x00:
			switch (low) {
			case 0xE0:
				listCmds.add("call clearScreenChip8");
				listCmds.add("call updateGameScreen");
				break;
			case 0xEE:
				listCmds.add("ret");
				break;
			case 0xff:
				listCmds.add("ld a,1");
				listCmds.add("call setRes");
				break;
			default:
				listCmds.add(String.format("unknown %02x%02x", high, low));

			}
			break;
		case 0x01: {
			int adr = high2 * 256 + low;
			listCmds.add(String.format("jp     %s", lbladr(adr)));
			break;
		}
		case 0x02: {
			int adr = high2 * 256 + low;
			listCmds.add(String.format("call   %s", lbladr(adr)));
			break;
		}
		case 0x03:
			listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("cp  %s",number(low)));
			listCmds.add(String.format("jr  z,%s",lbladr(skip(pos,code))));
			break;
		case 0x04:
			listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("cp  %s",number(low)));
			listCmds.add(String.format("jr  nz,%s",lbladr(skip(pos,code))));
			break;
		case 0x05:
			listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("cp  (ix+reg_%s)",reg(lownib1)));
			listCmds.add(String.format("jr  z,%s",lbladr(skip(pos,code))));
			break;
		case 0x06:
			listCmds.add(String.format("ld  (ix+reg_%s),%s",reg(high2),number(low)));
			break;
		case 0x07:
			listCmds.add(String.format("ld  a, %s",number(low)));
			listCmds.add(String.format("add (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
			break;
		case 0x08:
			switch (lownib2) {
			case 0x00:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld  (ix+reg_%s),a",reg(high2)));
				break;
			case 0x01:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("or  (ix+reg_%s)",reg(low)));
				listCmds.add(String.format("ld  (ix+reg_%s),a",reg(high2)));
				break;
			case 0x02:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("and (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld  (ix+reg_%s),a",reg(high2)));
				break;
			case 0x03:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("xor (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld  (ix+reg_%s),a",reg(high2)));
				break;
			case 0x04:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("adc (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("add 0"));
				listCmds.add(String.format("ld  (ix+reg_vf),a"));
				break;
			case 0x05:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("sub (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc 0"));
				listCmds.add(String.format("xor 1"));
				listCmds.add(String.format("ld  (ix+reg_vf),a"));
				break;
			case 0x06:
				if (newShift)
					listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				else
					listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("sra a"));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc 0"));
				listCmds.add(String.format("ld  (ix+reg_vf),a"));
				break;
			case 0x07: // todo check what 0x07 does
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("ld  b, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("sub b"));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc 0"));
				listCmds.add(String.format("xor 1"));
				listCmds.add(String.format("ld  (ix+reg_vf),a"));
				break;
			case 0x0E:
				if (newShift)
					listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				else
					listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(lownib1)));
				listCmds.add(String.format("sla a"));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				listCmds.add(String.format("ld  a,0"));
				listCmds.add(String.format("adc 0"));
				listCmds.add(String.format("ld  (ix+reg_vf),a"));
				break;
			}
			break;
		case 0x09:
			listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("cp  (ix+reg_%s)",reg(lownib1)));
			listCmds.add(String.format("jr  nz,%s",lbladr(skip(pos,code))));
			break;
		case 0x0A: {
			int adr = high2 * 256 + low;
			listCmds.add(String.format("ld  iy, %s",lbladr(adr)));
			break;
		}

		case 0x0B: {
			int adr = high2 * 256 + low;
			listCmds.add("Not supported: bxxxx");
			break;
		}
		case 0x0C:
			//listCmds.add(String.format("ld  a, r"));
			listCmds.add(String.format("call xrnd"));
			listCmds.add(String.format("ld  a, l"));
			listCmds.add(String.format("and %s", number(low)));
			listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
			break;
		case 0x0D:
			listCmds.add(String.format("ld  a, %d",lownib2));
			listCmds.add(String.format("ld  b, (ix+reg_%s)",reg(high2)));
			listCmds.add(String.format("ld  c, (ix+reg_%s)",reg(lownib1)));
			listCmds.add(String.format("ld	hl, iy"));
			listCmds.add(String.format("call  chip8sprite"));
			break;
		case 0x0E:
			switch (low) {
			case 0x9e:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("call checkKey"));
				listCmds.add(String.format("jr  z,%s",lbladr(skip(pos,code))));
				break;

			case 0xa1:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("call checkKey"));
				listCmds.add(String.format("jr  nz,%s",lbladr(skip(pos,code))));
				break;
			}
			break;
		case 0x0F:
			switch (low) {
			case 0x07:
				listCmds.add(String.format("call vinterrupt"));
				listCmds.add(String.format("ld  a,(ix+reg_delay)"));
				listCmds.add(String.format("ld  (ix+reg_%s),a",reg(high2)));
				break;
			case 0x0a:
				listCmds.add(String.format("call  GetKey"));
				listCmds.add(String.format("ld	(ix+reg_%s),a",reg(high2)));
				break;
			case 0x15:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("ld  (ix+reg_delay),a"));
				break;
			case 0x18:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("ld  (ix+reg_sound),a"));
				break;
			case 0x1e:
				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,(ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("add iy,de"));
				comment = String.format("i += %s", reg(high2));
				break;
			case 0x29:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("ld  b,a"));
				listCmds.add(String.format("add a; *2"));
				listCmds.add(String.format("add a; *4"));
				listCmds.add(String.format("add  b; *5"));
				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,a"));
				listCmds.add(String.format("ld  hl,chip8Font"));
				listCmds.add(String.format("ld  hl,de"));
				listCmds.add(String.format("ld  iy,hl"));
				comment = String.format("i := char %s", reg(high2));
				break;
			case 0x30:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("add a; *2"));
				listCmds.add(String.format("ld  b,a"));
				listCmds.add(String.format("add a; *4"));
				listCmds.add(String.format("add a; *8"));				
				listCmds.add(String.format("add  b; *10"));
				listCmds.add(String.format("ld  d,0"));
				listCmds.add(String.format("ld  e,a"));
				listCmds.add(String.format("ld  hl,bigfont"));
				listCmds.add(String.format("ld  hl,de"));
				listCmds.add(String.format("ld  iy,hl"));
				comment = String.format("i := char %s", reg(high2));
				break;				
			case 0x33:
				listCmds.add(String.format("ld  a, (ix+reg_%s)",reg(high2)));
				listCmds.add(String.format("ld  hl, iy"));
				listCmds.add(String.format("call bcd"));

				comment = String.format("bcd    %s", reg(high2));
				break;

			case 0x55:
				if (high2 == 0) {
					listCmds.add(String.format("ld  a,(ix)"));
					listCmds.add(String.format("ld  (iy),a"));
					
				} else {
					listCmds.add(String.format("ld  hl, ix"));
					listCmds.add(String.format("ld  de, iy"));
					listCmds.add(String.format("ld  bc,%d", high2+1));
					listCmds.add(String.format("ldir"));
				}
				comment = String.format("save   %s", reg(high2));
				break;
			case 0x65:
				if (high2 == 0) {
					listCmds.add(String.format("ld  a,(iy)"));
					listCmds.add(String.format("ld  (ix),a"));
					
				} else {
					listCmds.add(String.format("ld  de, ix"));
					listCmds.add(String.format("ld  hl, iy"));
					listCmds.add(String.format("ld  bc,%d", high2+1));
					listCmds.add(String.format("ldir"));
				}
				comment = String.format("load   %s", reg(high2));
				break;
			}
			break;

		}
		CC8Label lbl = mLabels.get(startpos);
		String strlbl = "";
		if (lbl != null) {
			if (lbl.mTarget == 0x027f) {
				System.out.println("stop");
			}
			strlbl = lbl.toString() + ":";
		}
		
		int op = high*256+low;
		String disass = mOpcodeTable.decode(op, Dialect.OCTO, mLabels);
		comment = String.format("%04x %02x %02x\t%s", startpos, high, low, disass);
//		mSB.append("\t;"+comment+"\n");

		

		
		
		
		if (listCmds.size() > 0) {
			String cmd = listCmds.get(0);
			line = String.format("%-10s        %s", strlbl, cmd);
			if (comment != null) line = String.format("%-65s;%s",line,comment);
			emitLine(pos,line);
			for (int i=1;i<listCmds.size();i++) {
				line = String.format("                  %s", listCmds.get(i));
				emitLine(pos,line);
			}
		} else
			emitLine(pos,String.format("***** unknown command %02x %02x**** \n", high, low));
	//	System.out.println(line);
//		mSB.append(line + "\n");

		return pos;
	}

	private int skip(int pos, byte[] code) {
		
		return pos+2;
	}

	
	String bin(int data) {
		String comment=Integer.toBinaryString(data).replaceAll("0", " ").replaceAll("1", String.format("%c", 0x2588));
		while (comment.length() < 8) comment = "_"+comment;
		return comment;
	}
	
	char chr(int data) {
		if (data >= 32 && data <= 127) return (char)data;
		return 32;
	}

	@Override
	protected int emitdb(byte[] chip8Memory, int pc, CC8Label label) {
		try {
		int data = chip8Memory[pc] & 0xff;
		int data2;
		int itemsPerRow=1;
		CC8Label lbl = mLabels.get(pc);
		pc++;
		String strlbl = "";
		if (lbl != null) {
			strlbl = lbl.toLabelString();
			if (lbl.mLabelType == C8LabelType.SKIP) strlbl = "";
			label = lbl;
		}
		
		C8LabelType type = C8LabelType.DATA;
		if (disassFormat) {
			if (strlbl.length() < 16)
				line = String.format("%04x %-16s             ", pc, strlbl);
			else {
				line = String.format("%04x %s             ", pc, strlbl);
				emitLine(pc,line);
				line = String.format("%04x %-16s             ", pc, "");
			}
		}
		else {
			if (strlbl.length() < 16) 
				line = String.format("%-16s        ", strlbl);
			else {
				line = String.format("%s        ", strlbl);
				emitLine(pc,line);
				line = String.format("%-16s        ", "");
				
			}
		}
				
		if (label != null) {
			type = label.mLabelType;
			itemsPerRow = label.mItemsPerRow;
		}
		String strCmd="";
		String strComment="";
		
		switch(type) {
			case HEX:
			case DATA:
				 if (itemsPerRow == 1) {
					 strCmd = String.format("db 0x%02x", data);
					 strComment = String.format(";%s %c", bin(data),chr(data));
				 } else {
					 strCmd = String.format("db 0x%02x", data);
					 strComment = String.format("\t;%c",chr(data));
					 for (int i=1;i<itemsPerRow;i++) {
						 CC8Label lbl2 = mLabels.get(pc);
						 if (lbl2 != null) break;
						 if (mSetVisited.contains(pc)) break;
						 
						 data = chip8Memory[pc] & 0xff;
						 pc++;
						 strCmd += String.format(", 0x%02x", data);
						 strComment += String.format("%c",chr(data));
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
				strCmd = String.format("db 0x%02x, 0x%02x", data,data2);
				strComment = String.format(";%s%s", bin(data),bin(data2));
				break;
			default:
				 strCmd = String.format("db 0x%02x", data);
				 strComment = String.format(";%s %c", bin(data),chr(data));
				 break;
		}
		line += strCmd+"\t"+strComment;
			

		
	//	System.out.println(line);
		emitLine(pc,line);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return pc;
	}

}
