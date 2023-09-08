package disass;

import java.io.File;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;






public class CC8Decoder {

	int labelNr = 0;
	long bytesRead;
	

	byte chip8Memory[] = new byte[65536];
	public TreeMap<Integer, CC8Label> mLabels = new TreeMap<>();
	TreeSet<Integer> mSetVisited = new TreeSet();
	Stack<Integer> mStackCodeBlocks = new Stack<>();
	public C8Emitter emitter;

	public void start(String filename, String outfile) {
		int size = loadGame(filename);
		int pc = 0x200;
		
		//emitter = new C8DisassEmitter();
		emitter.mLabels = mLabels;
		mSetVisited.clear();
		mStackCodeBlocks.clear();
		
		//mLabels.clear();
		crawl(pc);
		while (!mStackCodeBlocks.isEmpty()) {
			Integer adr = mStackCodeBlocks.pop();
			if (adr == 0x4c2) {
				System.out.println("debug");
			}
			if (adr == null) break;
			if (mSetVisited.contains(adr)) continue;
			crawl(adr);
		}
		System.out.println("============ complete dump ==================");
		dumpAll(0x200);
		if (outfile != null)
			saveText(outfile, emitter.getText());
	}
	
	public void start(byte[] memory, int codesize) {
		int size = memory.length;
		for (int i=0;i<memory.length;i++) {
			chip8Memory[i] = memory[i];
		}
		int pc = 0x200;
		bytesRead = codesize;
		
		//emitter = new C8DisassEmitter();
		emitter.mLabels = mLabels;
		mSetVisited.clear();
		mStackCodeBlocks.clear();
		
		//mLabels.clear();
		crawl(pc);
		while (!mStackCodeBlocks.isEmpty()) {
			Integer adr = mStackCodeBlocks.pop();
			if (adr == 0x4c2) {
				System.out.println("debug");
			}
			if (adr == null) break;
			if (mSetVisited.contains(adr)) continue;
			crawl(adr);
		}
		System.out.println("============ complete dump ==================");
		dumpAll(0x200);
	}

	private void saveText(String outfile, String string) {
		try {
			PrintWriter pw = new PrintWriter(outfile);
			pw.println(string);
			pw.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	void addCodeLabel(int adr) {
		CC8Label lbl;
		mStackCodeBlocks.push(adr);
		lbl = mLabels.get(adr);
		if (lbl != null) return;
		if (adr == 0xc42) {
			System.out.println("debug");
		}
		
		lbl	= new CC8Label(C8LabelType.CODE);
		mLabels.put(adr, lbl);

	}
	
	void addDataLabel(int adr) {
		CC8Label lbl;
		lbl = mLabels.get(adr);
		if (lbl != null) {
			if (lbl.mLabelType == C8LabelType.CODE) {
				mStackCodeBlocks.push(adr);
				if (lbl.mEnd != 0) {
					for (int i=lbl.mTarget;i<=lbl.mEnd;i++) {
						mSetVisited.add(i);
					}
				}
			}
				
			return;
		}
		//System.out.println(String.format("Add data Label %4x %d",adr,adr));
			
		lbl	= new CC8Label(C8LabelType.DATA);
		mLabels.put(adr, lbl);

	}
	
	private CC8Label saveSkipLabel(CC8Label skipLabel, int pc) {
		if (skipLabel != null) {
			skipLabel.mTarget = pc;
			mLabels.put(pc, skipLabel);
		}
		return null;
	}
	
	/* crawl walks through a block of code until we find an unconditional jp or a ret
	 * if we find any i := const a data label is produced
	 * if we find any jp or call, a code label is produced and the target is put onto the stack
	 * for each skip, we produce a skip label. this is not really helpful in chip8 disassembly but we need it for 
	 * other emitters, for example the z80 assembly emitter.
	 *  
	 */

	void crawl(int pc) {

		//System.out.println(String.format("---- crawling %x ------",pc));
		addCodeLabel(pc);
		boolean inSkip = false;
		boolean stop = false;
		CC8Label skipLabel = null;
		while (stop == false) {
			if (pc == chip8Memory.length) 
				break;
		/*	if (pc == 0x029e) {
				System.out.println("debug");
			} 
			emitter.emitOpcode(chip8Memory, pc);
			*/
			mSetVisited.add(pc);
			if (pc+2 > chip8Memory.length) break;
			int high = chip8Memory[pc] & 0xff;
			int low  = chip8Memory[pc+1] & 0xff;
			byte highnib = (byte) (high >>> 4);
			byte lownib1 = (byte)(low >> 4);
			byte lownib2 = (byte) (low & 0x0f);
			byte high2 	 = (byte) (high & 0xf);
			
			
			switch(highnib) {
			case 0x00:
					if (low == 0xee) { // return
						if (!inSkip) stop = true;
					}
					break;
			case 0x01:
			case 0x0b:
			case 0x02: {
					int adr = high2 * 256 + low;
					if (adr == 0x4c2) {
						System.out.println("debug");
					}
					
					//System.out.println(String.format("Pushing adr %x",adr));
					
					addCodeLabel(adr);
				}
				if ((highnib == 0x01 || highnib == 0x0b) && !inSkip) stop=true;
				//skipLabel = saveSkipLabel(skipLabel, pc);
				inSkip = false;
				break;
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x09:
				//saveSkipLabel(skipLabel, pc);
				if (emitter.wantsSkipLabels()) {
					skipLabel = new CC8Label(C8LabelType.SKIP);
					skipLabel.mTarget = pc+4;
					mLabels.put(pc+4, skipLabel);
				}
				inSkip = true;
				break;
			case 0x0a:  {
				int adr = high2 * 256 + low;
				addDataLabel(adr);
				//skipLabel = saveSkipLabel(skipLabel, pc);
				inSkip = false;
			}
				break;
			case 0x0e:
				switch(low) {
				case 0x9e: 
				case 0xa1:
					inSkip = true;
					if (emitter.wantsSkipLabels()) {
						//saveSkipLabel(skipLabel, pc);
						skipLabel = new CC8Label(C8LabelType.SKIP);
						skipLabel.mTarget = pc+4;
						mLabels.put(pc+4, skipLabel);
					}
					break;
				default:
					////skipLabel = saveSkipLabel(skipLabel, pc);
					inSkip = false;
				}
				break;
				
			default:
				//skipLabel = saveSkipLabel(skipLabel, pc);
				inSkip = false;
			}
		
			pc+=2;
		}
		
	}
	


	void dumpAll(int pc) {
		emitter.clear();
		emitter.mLabels = mLabels;
		emitter.mSetVisited = mSetVisited;
		int lbl=1;
		for (Integer adr: mLabels.keySet()) {
			CC8Label clbl = mLabels.get(adr);
			clbl.mNr = lbl++;
		}
		CC8Label lastLabel = null;
		CC8Label label;
		
		while (pc < 0x200+bytesRead ) {
			if (mSetVisited.contains(pc)) {
				label = mLabels.get(pc);
				if (label != null)
					lastLabel = label;
					 
				emitter.emitOpcode(chip8Memory, pc);
				pc+=2;
			} else {
				label = mLabels.get(pc);
				if (label != null)
					lastLabel = label;
				pc = emitter.emitdb(chip8Memory, pc, lastLabel);
			}
			
			
		}
	}

	int loadGame(String filename) {
		bytesRead = 0;
		try (InputStream inputStream = new FileInputStream(filename);) {
			long fileSize = new File(filename).length();
			byte gameBytes[] = new byte[(int) fileSize];
			bytesRead = inputStream.read(gameBytes);
			inputStream.close();
			int source = 0;
			int target = 0x200;
			for (int i = 0; i < bytesRead; i++) {
				chip8Memory[target + i] = gameBytes[source + i];
			}
			return (int) bytesRead;

		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public void saveHints(String filename) {
		StringBuilder sb = new StringBuilder();
		for (Integer adr : mLabels.keySet()) {
			CC8Label lbl = mLabels.get(adr);
			lbl.mTarget = adr;
			sb.append(lbl.save()+"\n");
		}
		Tools.writeTextFile(filename,sb.toString());
	}
	
	public void loadHints(String filename) {
		try {
			File file = new File(filename);
			if (file.exists()) {
			String text = Tools.loadTextFile(filename);
			if (text == null) return;
			if (text.length() == 0) return;
			mLabels = new TreeMap<>();
			String lines[] = text.split("\n");
			for (String line: lines) {
				CC8Label label = new CC8Label();
				label.load(line);;
				mLabels.put(label.mTarget, label);
			}
			}
		}
		catch(Exception e) {
			
		}
	}

	public void setMemory(byte[] memory) {
		this.chip8Memory = memory;
		
	}

	public void setAssemblerLabels(TreeMap<String, CC8Label> labels) {
		mLabels = new TreeMap<>();
		for (CC8Label label : labels.values()) {
			mLabels.put(label.mTarget, label);
		}
	}

	public void setCodeSize(int codeSize) {
		bytesRead = codeSize;
		
	}

}
