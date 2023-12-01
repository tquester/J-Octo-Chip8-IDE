package disass;

import disass.COpcodeTable.Dialect;

// download json table: https://chip8.gulrak.net/data/opcode-table.json

public class C8TableEmitter extends C8Emitter {
	public boolean disassFormat = false;
	COpcodeTable mOpcodeTable = new COpcodeTable();
	public Dialect mDialect = Dialect.OCTO;
	StringBuilder mSB = new StringBuilder();
	C8DisassEmitter emitterOcto = new C8DisassEmitter();
	C8DisassEmitterCowgod emiiterChipper = new C8DisassEmitterCowgod();
	
	
	@Override
	public int emitOpcode(byte[] code, int pos) {
		String line;
		int startpos = pos;
		int high = code[pos] & 0xff;
		int low = code[pos + 1] & 0xff;
		int op = high*256+low;
		String cmd = mOpcodeTable.decode(op, mDialect, mLabels);
		String strlbl = "";
		CC8Label lbl = mLabels.get(pos);
		if (lbl != null) {
			switch(mDialect) {
				case OCTO: strlbl = ": "+lbl.toString();
					break;
				case CHIPPER: strlbl = lbl.toLabelString();
					break;
			}
			strlbl = lbl.toLabelString();
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
				line = String.format("%s        %s", strlbl);
				mSB.append(line + "\n");
				line = String.format("%-16s        %s", "", cmd);
			}
				
		}
		mSB.append(line+"\n");


		return pos+2;
	}
	@Override
	protected void clear() {
		mSB = new StringBuilder();
		
	}
	@Override
	protected int emitdb(byte[] chip8Memory, int pc, CC8Label label) {
		switch(mDialect) {
			case OCTO:
					emitterOcto.disassFormat = disassFormat;
					emitterOcto.setStringBuilder(mSB);
					emitterOcto.mLabels = mLabels;
					return emitterOcto.emitdb(chip8Memory, pc, label);
			case CHIPPER:
					emiiterChipper.mSB = mSB;
					emiiterChipper.disassFormat = disassFormat;
					emiiterChipper.mLabels = mLabels;
					return emiiterChipper.emitdb(chip8Memory, pc, label);
		}
		return 1;
	}
	@Override
	public String getText() {
		return mSB.toString();
	}
	@Override
	public int emitOpcode(boolean usecomments, byte[] code, int pos) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}
