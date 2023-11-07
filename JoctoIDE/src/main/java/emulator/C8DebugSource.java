package emulator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.transform.dom.DOMSource;

public class C8DebugSource extends ArrayList<C8DebugSourceLine> {
	
	public class CAliasRange {
		public int start;
		public int end=0xffff;
		public int register;
		public String name;
	}
	
	public class CRegisterAlias {
		int register;
		int pc;
		String name;
	}
	
	

	public class CRegisterAliase extends ArrayList<CRegisterAlias> {
		
	}
	
	public ArrayList<CAliasRange> mAliasRanges = new ArrayList<>();
	public TreeMap<Integer, CRegisterAliase> mRegisterAliase = new TreeMap<>();
	public TreeMap<Integer, List<CAliasRange>> mMapAliasRanges = null;
	TreeMap<String, CAliasRange> mMapOpenAliases = new TreeMap<>();			// stores Aliases which have start but no end.
	TreeMap<Integer, Integer> mAddressMap = new TreeMap<>();				// Stores maps op code address to array index
	TreeMap<Integer, List<String>> mMapComments = new TreeMap<>();

	// The debugger shoud show registers with their alias names.
	// All aliases are removed from the symbol tables (the tokenizer holds it) at the end of an include
	// or with the unalias command.
	// if there is no end in the range, the alias starts from that point and never ends.
	public void startAlias(int start, int register, String name) {
		CAliasRange range = mMapOpenAliases.get(name);
		if (range == null) {
			range = new CAliasRange();
			range.start = start;
			range.register = register;
			range.name = name;
			mMapOpenAliases.put(name, range);
			mAliasRanges.add(range);
		}
	}
	
	public void addComment(int pc, String comment) {
		List<String> lcomment = mMapComments.get(pc);
		if (lcomment == null) {
			lcomment = new ArrayList<String>();
			mMapComments.put(pc,  lcomment);
		}
		lcomment.add(comment);
	}
	
	public void stopAlias(int end, String name) {
		CAliasRange range = mMapOpenAliases.get(name);
		if (range != null) {
			range.end = end;
			mMapOpenAliases.remove(name);
		}
	}
	
	public List<CAliasRange> getAliasesAtAddress(int pc) {
		if (mMapAliasRanges == null) {
			mMapAliasRanges = new TreeMap<>();
			for (CAliasRange range: mAliasRanges) {
				System.out.println(String.format("Alias %04x-%04x %s -> %d",range.start, range.end, range.name, range.register, null));
				List<CAliasRange> list = mMapAliasRanges.get(range.start);
				if (list == null) {
					list = new ArrayList<>();
					mMapAliasRanges.put(range.start, list);
				}
				list.add(range);
			}
			System.out.println("-----------------------------");
		}
		if (pc == 0x40c) 
			System.out.println("Stop");
		return mMapAliasRanges.get(pc);

	}
	
	public String getRegisterAlias(int pc, int register) {
		String name = null;
		CRegisterAliase aliase = mRegisterAliase.get(pc);
		if (aliase != null) {
			for (CRegisterAlias alias: aliase) {
				if (alias.register == register) {
					name = alias.name;
					break;
				}
			}
		}
		
		return name;
		/*
		String name=null;
		for (CAliasRange range: mAliasRanges) {
			if (range.register == register) {
				if (range.start <= pc && range.end >= pc) {
					name = range.name;
					break;
				}
			}
		}
		return name;
		*/
	}

	public String getRegisterAliases(int pc, int register) {
		String name="v"+Integer.toString(register,16);
		TreeSet<String> set = new TreeSet<>();
		for (CAliasRange range: mAliasRanges) {
			if (range.register == register) {
				if (range.start <= pc && range.end >= pc) {
					if (!set.contains(range.name)) {
						name += ", "+range.name;
						set.add(range.name);
					}
				}
			}
		}
		return name;
	}

	public void addRegisterAlias(int pc, int register, String name) {
		CRegisterAliase aliase = mRegisterAliase.get(pc);
		if (aliase == null) {
			aliase = new CRegisterAliase();
			mRegisterAliase.put(pc, aliase);
		}
		CRegisterAlias ra = new CRegisterAlias();
		ra.register = register;
		ra.pc = pc;
		ra.name = name;
		aliase.add(ra);
		
	}
	
	public void addSourceLine(int address, String line) {
		C8DebugSourceLine sline = new C8DebugSourceLine(address, line);
		add(sline);
		Integer iline = mAddressMap.get(address);
		if (iline == null)
			mAddressMap.put(address, size());
	}

	public int getLineForCode(int pc) {
		Integer line = mAddressMap.get(pc);
		return line == null ? -1 : line.intValue();
	}

	public int getPCForLine(int line) {
		if (line >= 0 && line < size()) {
			C8DebugSourceLine obj = get(line);
			return obj.org;
		}
		return 0;
	}

	public List<String> getComments(int pos) {
		return mMapComments.get(pos);
	}

}
