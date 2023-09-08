package gui;

import assembler.CWordParser;

public class CSearchReplace {
	public CWordParser wordParaser = new CWordParser();
	
	public void start(String str) {
		wordParaser.start(str);
	}
	
	int find(String str, boolean word, boolean ignorecase) {
		String w;
		if (ignorecase) str = str.toLowerCase();
		if (word) {			
			while ((w = wordParaser.getWord()) != null) {
				if (ignorecase) w = w.toLowerCase();
				if (w.compareTo(str) == 0) 
					return wordParaser.prevpos;
			}
			return -1;
		} else {
			if (ignorecase)
				return wordParaser.findignorecase(str);
			else
				return wordParaser.find(str);
		}
	}
	

}
