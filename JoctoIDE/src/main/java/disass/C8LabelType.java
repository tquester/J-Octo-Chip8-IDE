package disass;

public enum C8LabelType {
	NONE,
	CODE,				// code label, jump, call
	DATA,				// data label, i := ___
	SKIP,				// target for a skip operation, usually invisible
	SPRITE8,			// 8 bit wide sprite with graphical comment: db $xx 	; ####....
	SPRITE16,			// 8 bit wide sprite with graphical comment: db $xx 	; ####....####....
	ASCII,				// Ascii characters
	LETTERS,
	HEX,
	STRINGMODE,
	STRUCT,
	CONST
	;
};

