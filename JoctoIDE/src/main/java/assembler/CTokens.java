package assembler;

import java.util.TreeMap;

public class CTokens {

	TreeMap<String, Token> mMapTokens = new TreeMap<>();
	CTokens() {
		for (Object[] token: AllTokens) {
			mMapTokens.put((String)token[0], (Token)token[1]);
		}
	}
	public Token parse(String str) {
		return mMapTokens.get(str.toUpperCase());
	}
	
	Token getToken(String str) {
		return mMapTokens.get(str);
	}
	
	public  Object AllTokens[][] = {

			{"octo", Token.octo },
			{"chipper", Token.chipper },
	        {"ld", 	Token.ld},
	        {"db", Token.db },
	        {"call", Token.call},
	        {"jp", Token.jp},
	        {"cls", Token.cls},
	        {"hires", Token.hires},
	        {"high", Token.hires},
	        {"lowres", Token.lowres},
	        {"low", Token.lowres},
	        {"v0", Token.v0},
	        {"v1", Token.v1},
	        {"v2", Token.v2},
	        {"v3", Token.v3},
	        {"v4", Token.v4},
	        {"v5", Token.v5},
	        {"v6", Token.v6},
	        {"v7", Token.v7},
	        {"v8", Token.v8},
	        {"v9", Token.v9},
	        {"va", Token.va},
	        {"vb", Token.vb},
	        {"vc", Token.vc},
	        {"vd", Token.vd},
	        {"ve", Token.ve},
	        {"vf", Token.vf},
	        {"i", Token.i},
	        {"dt", Token.delay },
	        {"delay", Token.delay},
	        {"buzz", Token.buzz},
	        {"sprite", Token.sprite},
	        {"drw", Token.sprite},
	        {"rts", Token.rts},
	        {"ret", Token.rts},
	        {"load", Token.load},
	        {"save", Token.save},
	        {"se", Token.se},
	        {"seq", Token.se},
	        {"sne", Token.sne},
	        {"ld", Token.ld},
	        {"add", Token.add},
	        {"or", Token.or},
	        {"xor", Token.xor},
	        {"sub", Token.sub},
	        {"subn", Token.subn},
	        {"shl", Token.shl},
	        {"shr", Token.shr},
	        {"rnd", Token.rnd},
	        {"skp", Token.skp},
	        {"sknp", Token.sknp},
	        {"skipkey", Token.skp},
	        {"skipnkey", Token.sknp},
	        {"k", Token.key},
	        {"key", Token.key},
	        {"scr", Token.scr},
	        {"scd", Token.scd},
	        {"scl", Token.scl},
	        {"scu", Token.scl},
	        {"scroll-down", Token.scd},
	        {"scroll-left", Token.scl},
	        {"scroll-right", Token.scr},
	        {"scroll-up", Token.scu},
	        {"scrollr", Token.scr},
	        {"scrolld", Token.scd},
	        {"scrolll", Token.scl},
	        {"scrollu", Token.scu},
	        {"hf", Token.hf},
	        {"r",  Token.r,},
	        {"{",  Token.curlybracketopen},
	        {"}",  Token.curlybracketclose},
	        {"(",  Token.bracketopen},
	        {")",  Token.bracketclose},
	        {"[i]",  Token.iindirect},
	        
	        // octo keywords
	        
	        {"+", Token.plus},
	        {"-", Token.minus},
	        {"*", Token.mult},
	        {"/", Token.divide},
	        {"mod", Token.divide},
	        {"|", Token.or},
	        {"^", Token.xor},
	        {"&", Token.and},
	        {":=", Token.assign},
	        {"|=", Token.orassign},
	        {"!=", Token.unequal},
	        {"&=", Token.andassign},
	        {"^=", Token.xorassign},
	        {"-=", Token.minusassign},
	        {"=-", Token.assignminus},
	        {"+=", Token.plusassign},
	        {">>=", Token.shrassign},
	        {"<<=", Token.shlassign},
	        {"==", Token.equals},
	        {"<", Token.smaller},
	        {">", Token.bigger},
	        {"<=", Token.lessequal},
	        {">=", Token.biggerequal},
	        {"key", Token.key},
	        {"-key", Token.minuskey},
	        {"hex", Token.hex},
	        {"bighex", Token.bighex},
	        {"random", Token.rnd},
	        {"rnd", Token.rnd},
	        {":breakpoint", Token.breakpoint},
	        {":proto", Token.proto},
	        {":alias", Token.alias},
	        {":const", Token.octoconst},
	        {":org", Token.org},
	        {";", Token.semikolon},
	        {"return", Token.rts},
	        {"clear", Token.cls},
	        {"bcd", Token.bcd},
	        {"save", Token.save},
	        {"load", Token.load},
	        {"buzzer",Token.buzz},
	        {"if", Token.octoif},
	        {"then", Token.octothen},
	        {"begin", Token.octobegin},
	        {"else", Token.octoelse},
	        {"end", Token.octoend},
	        {"jump", Token.jp},
	        {"jump0", Token.jump0},
	        {"native", Token.octonative},
	        {"sprite", Token.sprite},
	        {"loop", Token.loop},
	        {"while", Token.octowhile},
	        {"again", Token.again},
	        {"scroll-down", Token.scd},
	        {"scroll-up", Token.scu},
	        {"scroll-right", Token.scr},
	        {"scroll-left", Token.scl},
	        {"lores", Token.lowres},
	        {"exit", Token.exit},
	        	
	        {"loadflags", Token.loadflags},
	        {"saveflags", Token.saveflags},
	        {"audio", Token.audio},
	        {"plane", Token.plane},
	        {":macro", Token.macro},
	        {":calc", Token.calc},
	        {":byte", Token.octobyte},
	        {":call", Token.call},
	        {":stringmode", Token.stringmode},
	        {":assert", Token.octoassert},
	        {":monitor", Token.monitor},
	        {":pointer", Token.octopointer},
	        {"pitch", Token.pitch},
	        
	        
	        {"~", Token.tilde },
	        {"!", Token.exclamation },
	        {"sin", Token.sin },
	        {"cos", Token.cos },
	        {"tan", Token.tan },
	        {"exp", Token.exp },
	        {"log", Token.log },
	        {"abs", Token.abs },
	        {"sqrt", Token.sqrt },
	        {"sign", Token.sign },
	        {"ceil", Token.floor },
	        {"strlen", Token.strlen },
	        {"@", Token.atsym },
	        {"pow", Token.pow },
	        {"min", Token.min },
	        {"max", Token.max }
	        
	};

	
}
