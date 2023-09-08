package disass;

import java.util.TreeMap;



public class C8DisassEmitterCowgod extends C8Emitter {

	public StringBuilder mSB = new StringBuilder();
	public String line;
	public boolean disassFormat = true;
	public boolean hexadecimal = false;

	@Override
	public int emitOpcode(byte[] code, int pos) {

		try {
		String cmd = "";
		int startpos = pos;
		int high = code[pos] & 0xff;
		int low = code[pos + 1] & 0xff;
		pos += 2;
		byte highnib = (byte) (high >>> 4);
		byte lownib1 = (byte) (low >> 4);
		byte lownib2 = (byte) (low & 0x0f);
		byte high2 = (byte) (high & 0xf);

		switch (highnib) {
		case 0x00:
			switch (low) {
			case 0xE0:
				cmd = "cls";
				break;
			case 0xEE:
				cmd = "ret";
				break;
			case 0xff:
				cmd = "high";
				break;
			default:
				cmd = String.format("unknown %02x%02x", high, low);

			}
			break;
		case 0x01: {
			int adr = high2 * 256 + low;
			cmd = String.format("jp     %s", lbladr(adr));
			break;
		}
		case 0x02: {
			int adr = high2 * 256 + low;
			cmd = String.format("call   %s", lbladr(adr));
			break;
		}
		case 0x03:
			cmd = String.format ("se     %s, %s", reg(high2), number(low));
			break;
		case 0x04:
			cmd = String.format ("sne    %s, %s", reg(high2), number(low));
			break;
		case 0x05:
			cmd = String.format ("se     %s, %s", reg(high2), reg(lownib1));
			break;
		case 0x06:
			cmd = String.format ("ld     %s, %s", reg(high2), number(low));
			break;
		case 0x07:
			cmd = String.format ("add    %s, %s", reg(high2), number(low));
			break;
		case 0x08:
			switch (lownib2) {
			case 0x00:
				cmd = String.format("ld     %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x01:
				cmd = String.format("or     %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x02:
				cmd = String.format("and    %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x03:
				cmd = String.format("xor    %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x04:
				cmd = String.format("add    %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x05:
				cmd = String.format("sub    %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x06:
				cmd = String.format("shr    %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x07:
				cmd = String.format("subn   %s, %s", reg(high2), reg(lownib1));
				break;
			case 0x0E:
				cmd = String.format("shl    %s, %s", reg(high2), reg(lownib1));
				break;
			}
			break;
		case 0x09:
			cmd = String.format    ("sne    %s, %s", reg(high2), reg(lownib1));
			break;
		case 0x0A: {
			int adr = high2 * 256 + low;
			cmd = String.format    ("ld     i, %s", lbladr(adr));
			break;
		}

		case 0x0B: {
			int adr = high2 * 256 + low;
			cmd = String.format("jp     v0, %s", lbladr(adr));
			break;
		}
		case 0x0C:
			cmd = String.format("rnd    %s, %s", reg(high2), number(low));
			break;
		case 0x0D:
			cmd = String.format("drw    %s %s %d", reg(high2), reg(lownib1), lownib2);
			break;
		case 0x0E:
			switch (low) {
			case 0x9e:
				cmd = String.format("skp    %s", reg(high2));
				break;

			case 0xa1:
				cmd = String.format("sknp   %s", reg(high2));
				break;
			}
			break;
		case 0x0F:
			switch (low) {
			case 0x07:
				cmd = String.format("ld     %s, dt", reg(high2));
				break;
			case 0x0a:
				cmd = String.format("ld     %s, k", reg(high2));
				break;
			case 0x15:
				cmd = String.format("ld     dt, %s", reg(high2));
				break;
			case 0x18:
				cmd = String.format("ld     st, %s", reg(high2));
				break;
			case 0x1e:
				cmd = String.format("add    i, %s", reg(high2));
				break;
			case 0x29:
				cmd = String.format("ld     f, %s", reg(high2));
				break;
			case 0x33:
				cmd = String.format("ld     b, %s", reg(high2));
				break;

			case 0x55:
				cmd = String.format("ld     [i], %s", reg(high2));
				break;
			case 0x65:
				cmd = String.format("ld     %s, [i]", reg(high2));
				break;
			}
			break;

		}
		CC8Label lbl = mLabels.get(startpos);
		String strlbl = "";
		if (lbl != null) {
			strlbl = lbl.toString() + ":";
			//if (lbl.mLabelType == C8LabelType.SKIP) strlbl = "";
		}
		if (disassFormat) {
			if (strlbl.length() < 16) 	
				line = String.format("%04x %-16s %02x %02x       %s", startpos, strlbl, high, low, cmd);
			else {
				line = String.format("%04x %s", startpos, strlbl);
				mSB.append(line + "\n");
				line = String.format("%04x %-16s %02x %02x       %s", startpos, "", high, low, cmd);
			}
		}
		else {
			if (strlbl.length() < 16)
				line = String.format("%-16s        %s", strlbl, cmd);
			else {
				line = String.format("%s", strlbl);
				mSB.append(line + "\n");
				line = String.format("%-16s        %s", "", cmd);
			}
				
		}
			
	//	System.out.println(line);
		mSB.append(line + "\n");
		} catch(Exception ex) 
		
		{
				ex.printStackTrace();
		}

		return pos;
	}

	String reg(int reg) {
		if (reg < 10)
			return String.format("v%d", reg);
		else
			return String.format("v%c", reg + 'a'-10);
	}
	
	String lbladr(int adr) {
		CC8Label lbl = mLabels.get(adr);
		if (lbl == null)
			return String.format("%x", adr);
		else {
			if (disassFormat)
				return String.format("%s\t;%x", lbl.toString(),adr);
			else
				return String.format("%s\t", lbl.toString());
		}
			
	}

	@Override
	protected void clear() {
		mSB = new StringBuilder();
		
	}
	
	String number(int nr) {
		if (hexadecimal) return String.format("$%02x", nr);
		int nr2 = nr;
		if (nr > 127) nr = -(256-nr);
		return String.format("%d\t;%02x", nr, nr2);
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
			strlbl = lbl.toString() + ":";
			if (lbl.mLabelType == C8LabelType.SKIP) strlbl = "";
			label = lbl;
		}
		
		C8LabelType type = C8LabelType.DATA;
		if (disassFormat) {
			if (strlbl.length() < 16)
				line = String.format("%04x %-16s             ", pc, strlbl);
			else {
				line = String.format("%04x %s             ", pc, strlbl);
				mSB.append(line + "\n");
				line = String.format("%04x %-16s             ", pc, "");
			}
		}
		else {
			if (strlbl.length() < 16) 
				line = String.format("%-16s        ", strlbl);
			else {
				line = String.format("%s        ", strlbl);
				mSB.append(line + "\n");
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
					 strCmd = String.format("db $%02x", data);
					 strComment = String.format(";%s %c", bin(data),chr(data));
				 } else {
					 strCmd = String.format("db $%02x", data);
					 strComment = String.format("\t;%c",chr(data));
					 for (int i=1;i<itemsPerRow;i++) {
						 CC8Label lbl2 = mLabels.get(pc);
						 if (lbl2 != null) break;
						 if (mSetVisited.contains(pc)) break;
						 
						 data = chip8Memory[pc] & 0xff;
						 pc++;
						 strCmd += String.format(", $%02x", data);
						 strComment += String.format("%c",chr(data));
					 }
				 }
				 break;
			case SPRITE8:
				checkLineSpace(label);
				strCmd = String.format("db $%02x", data);
				strComment = String.format(";%s", bin(data));
				break;
			case SPRITE16:
				checkLineSpace(label);
				data2 = chip8Memory[pc] & 0xff;
				pc++;
				strCmd = String.format("db $%02x, $%02x", data,data2);
				strComment = String.format(";%s%s", bin(data),bin(data2));
				break;
			default:
				 strCmd = String.format("db $%02x", data);
				 strComment = String.format(";%s %c", bin(data),chr(data));
				 break;
		}
		line += strCmd+"\t"+strComment;
			

		
	//	System.out.println(line);
		mSB.append(line + "\n");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return pc;
	}
		
	String bin(int data) {
		String comment=Integer.toBinaryString(data).replaceAll("0", " ").replaceAll("1", "#");
		while (comment.length() < 8) comment = " "+comment;
		return comment;
	}
	
	char chr(int data) {
		if (data >= 32 && data <= 127) return (char)data;
		return 32;
	}

	@Override
	public String getText() {
		return mSB.toString();
	}

}
