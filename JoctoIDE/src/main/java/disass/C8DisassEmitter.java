package disass;

import java.util.List;
import java.util.TreeMap;

import emulator.C8DebugSource;
import emulator.C8DebugSource.CAliasRange;



public class C8DisassEmitter extends C8Emitter {

	public String line;
	public boolean disassFormat = true;
	public boolean hexadecimal = false;
	String autocomment;
	
	void emitAliase(int pos) {
		
		
		
		if (mSourceHints != null) {
			List<String> lcomment = mSourceHints.getComments(pos);
			if (lcomment != null) {
				for (String str: lcomment) {
					emitLine(pos, "# "+str);
				}
			}
			
			
			
			List<CAliasRange> aliase = mSourceHints.getAliasesAtAddress(pos);
			if (aliase != null) {
				for (CAliasRange range: aliase) {
						emitLine(pos, String.format("\t:alias %s v%s", range.name, Integer.toHexString(range.register)));
				}
			}
		}
		

	}

	@Override
	public int emitOpcode(byte[] code, int pos) {
		
		emitAliase(pos);
		autocomment = null;
		mLastType = C8LabelType.CODE;
		try {
		String cmd = "";
		int startpos = pos;
		int high = code[pos] & 0xff;
		int low = code[pos + 1] & 0xff;
		byte highnib = (byte) (high >>> 4);
		byte lownib1 = (byte) (low >> 4);
		byte lownib2 = (byte) (low & 0x0f);
		byte high2 = (byte) (high & 0xf);

		switch (highnib) {
		case 0x00:
			switch (low) {
			case 0xE0:
				cmd = "clear";
				break;
			case 0xEE:
				cmd = "return";
				break;
			case 0xfe:
				cmd = "lores";
				break;
			case 0xff:
				cmd = "hires";
				break;
			case 0xfb:
				cmd = String.format("scroll-right");
				break;
			case 0xfc:
				cmd = String.format("scroll-left");
				break;
			default:
				if ((low & 0xc0) == 0xc0) {
					cmd = String.format("scroll-down %d", low & 0x0f);
				} 
				else				
					cmd = String.format("unknown %02x%02x", high, low);

			}
			break;
		case 0x01: {
			int adr = high2 * 256 + low;
			cmd = String.format("jump %s", lbladr(adr));
			break;
		}
		case 0x02: {
			int adr = high2 * 256 + low;
			cmd = String.format("%s", lbladr(adr));
			break;
		}
		case 0x03:
			cmd = String.format("if %s != %s then", reg(pos,high2), number(low));
			break;
		case 0x04:
			cmd = String.format("if %s == %s then", reg(pos,high2), number(low));
			break;
		case 0x05:
			cmd = String.format("if %s != %s then", reg(pos,high2), reg(pos,lownib1));
			break;
		case 0x06:
			cmd = String.format("%s := %s", reg(pos,high2), number(low));
			break;
		case 0x07:
			cmd = String.format("%s += %s", reg(pos,high2), number(low));
			break;
		case 0x08:
			switch (lownib2) {
			case 0x00:
				cmd = String.format("%s := %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x01:
				cmd = String.format("%s |= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x02:
				cmd = String.format("%s &= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x03:
				cmd = String.format("%s ^= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x04:
				cmd = String.format("%s += %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x05:
				cmd = String.format("%s -= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x06:
				cmd = String.format("%s >>= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x07:
				cmd = String.format("%s =- %s", reg(pos,high2), reg(pos,lownib1));
				break;
			case 0x0E:
				cmd = String.format("%s <<= %s", reg(pos,high2), reg(pos,lownib1));
				break;
			}
			break;
		case 0x09:
			cmd = String.format("if %s == %s then", reg(pos,high2), reg(pos,lownib1));
			break;
		case 0x0A: {
			int adr = high2 * 256 + low;
			cmd = String.format("i := %s", lbladr(adr));
			break;
		}

		case 0x0B: {
			int adr = high2 * 256 + low;
			cmd = String.format("jump0  %s", lbladr(adr));
			break;
		}
		case 0x0C:
			cmd = String.format("%s := random %s", reg(pos,high2), number(low));
			break;
		case 0x0D:
			cmd = String.format("sprite %s %s %d", reg(pos,high2), reg(pos,lownib1), lownib2);
			break;
		case 0x0E:
			switch (low) {
			case 0x9e:
				cmd = String.format("if %s -key then", reg(pos,high2));
				break;

			case 0xa1:
				cmd = String.format("if %s key then", reg(pos,high2));
				break;
			}
			break;
		case 0x0F:
			switch (low) {
			case 0x07:
				cmd = String.format("%s := delay", reg(pos,high2));
				break;
			case 0x0a:
				cmd = String.format("%s := key", reg(pos,high2));
				break;
			case 0x15:
				cmd = String.format("delay := %s", reg(pos,high2));
				break;
			case 0x18:
				cmd = String.format("buzzer := %s", reg(pos,high2));
				break;
			case 0x1e:
				cmd = String.format("i += %s", reg(pos,high2));
				break;
			case 0x29:
				cmd = String.format("i := hex %s", reg(pos,high2));
				break;
			case 0x33:
				cmd = String.format("bcd %s", reg(pos,high2));
				break;

			case 0x55:
				cmd = String.format("save   %s", reg(pos,high2));
				break;
			case 0x65:
				cmd = String.format("load   %s", reg(pos,high2));
				break;

				
			}
			break;

		}
		CC8Label lbl = mLabels.get(startpos);
		String strlbl = "";
		if (lbl != null) {
			strlbl = ": " + lbl.toString();
			//if (lbl.mLabelType == C8LabelType.SKIP) strlbl = "";
		}
		if (disassFormat) {
			if (strlbl.length() < 16) 	
				line = String.format("%04x %-16s %02x %02x       %s", startpos, strlbl, high, low, cmd);
			else {
				line = String.format("%04x %s", startpos, strlbl);
				emitLine(pos, line);
				line = String.format("%04x %-16s %02x %02x       %s", startpos, "", high, low, cmd);
			}
		}
		else {
			if (strlbl.length() < 16)
				line = String.format("%-16s        %s", strlbl, cmd);
			else {
				line = String.format("%s        ", strlbl);
				emitLine(pos, line);
				line = String.format("%-16s        %s", "", cmd);
			}
				
		}
		
		if (autocomment != null) {
			while(line.length() < 60) line += " ";
			line += " # "+autocomment;
		}
			
	//	System.out.println(line);
		emitLine(pos, line);
		} catch(Exception ex) 
		
		{
				ex.printStackTrace();
		}

		pos += 2;

		return pos;
	}

	String reg(int pc, int reg) {
		String alias = null;
		String result = null;
		if (reg < 10)
			result = String.format("v%d", reg);
		else
			result = String.format("v%c", reg + 'a'-10);

		if (mSourceHints != null) {
			String strAlias = mSourceHints.getRegisterAlias(pc, reg);
		    if (strAlias != null) {
				if (showAlias || replaceAlias) 
					alias = strAlias;
				if (commenAlias) {
					String str = String.format("%s=%s", result, strAlias);
					if (autocomment == null) 
						autocomment = str;
					 else
						autocomment += ", "+str;
				}
		    }
		}
		
		
		if (alias != null) {
			if (showAlias)
				result = String.format("%s (%s)", alias, result);
			else
				result = alias;
		}
		
			
		return result;
	}
	
	String lbladr(int adr) {
		CC8Label lbl = mLabels.get(adr);
		if (lbl == null) {
			return String.format("%x", adr);
		}
		else {
			if (disassFormat)
				return String.format("%s\t;%x",  lbl.toString(),adr);
			else {
				if (lbl.mLabelType == C8LabelType.CODE && lbl.mNr == 1) 
					return "main\t";
				return String.format("%s", lbl.toString());
			}
		}
			
	}

	

	String number(int nr) {
		if (hexadecimal) return String.format("$%02x", nr);
		int nr2 = nr;
		if (nr > 127) nr = -(256-nr);
		return String.format("%d", nr);
	}

	@Override
	protected int emitdb(byte[] chip8Memory, int pc, CC8Label label) {
		try {
		int data = chip8Memory[pc] & 0xff;
		int data2;
		int itemsPerRow=1;
		emitAliase(pc);
		CC8Label lbl = mLabels.get(pc);
		pc++;
		String strlbl = "";
		if (lbl != null) {
			strlbl = ": "+ lbl.toString();
			if (lbl.mLabelType == C8LabelType.SKIP) strlbl = "";
			label = lbl;
		}
		
		C8LabelType type = C8LabelType.DATA;
		if (disassFormat) {
			if (strlbl.length() < 16)
				line = String.format("%04x %-16s             ", pc, strlbl);
			else {
				line = String.format("%04x %s             ", pc, strlbl);
				emitLine(pc, line);
				line = String.format("%04x %-16s             ", pc, "");
			}
		}
		else {
			if (strlbl.length() < 16) 
				line = String.format("%-16s        ", strlbl);
			else {
				line = String.format("%s        ", strlbl);
				emitLine(pc, line);
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
				mLastType = type;
				 if (itemsPerRow == 1) {
					 strCmd = String.format("0x%02x", data);
					 strComment = String.format("#%s %c", bin(data),chr(data));
				 } else {
					 strCmd = String.format("0x%02x", data);
					 strComment = String.format("\t#%c",chr(data));
					 for (int i=1;i<itemsPerRow;i++) {
						 CC8Label lbl2 = mLabels.get(pc);
						 if (lbl2 != null) break;
						 if (mSetVisited.contains(pc)) break;
						 
						 data = chip8Memory[pc] & 0xff;
						 pc++;
						 strCmd += String.format(" 0x%02x", data);
						 strComment += String.format("%c",chr(data));
					 }
				 }
				 break;
			case SPRITE8:
				checkLineSpace(label);
				strCmd = String.format(" 0x%02x", data);
				strComment = String.format("\t#%s", bin(data));
				break;
			case SPRITE16:
				checkLineSpace(label);
				data2 = chip8Memory[pc] & 0xff;
				pc++;
				strCmd = String.format("0x%02x 0x%02x", data,data2);
				strComment = String.format("\t#%s%s", bin(data),bin(data2));
				break;
			default:
				mLastType = type;
				 strCmd = String.format("0x%02x", data);
				 strComment = String.format("\t#%s %c", bin(data),chr(data));
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
		


	String bin(int data) {
		//String comment=Integer.toBinaryString(data).replaceAll("0", " ").replaceAll("1", String.format("%c", 0x2588));
		String comment=Integer.toBinaryString(data).replaceAll("0", " ").replaceAll("1", String.format("%c", '#'));
		while (comment.length() < 8) comment = " "+comment;
		return comment;
	}
	
	char chr(int data) {
		if (data >= 32 && data <= 127) return (char)data;
		return 32;
	}



}
