package assembler;
/*
 *       3.1 - Standard Chip-8 Instructions
            00E0 - CLS
            00EE - RET
            0nnn - SYS addr
            1nnn - JP addr
            2nnn - CALL addr
            3xkk - SE Vx, byte
            4xkk - SNE Vx, byte
            5xy0 - SE Vx, Vy
            6xkk - LD Vx, byte
            7xkk - ADD Vx, byte
            8xy0 - LD Vx, Vy
            8xy1 - OR Vx, Vy
            8xy2 - AND Vx, Vy
            8xy3 - XOR Vx, Vy
            8xy4 - ADD Vx, Vy
            8xy5 - SUB Vx, Vy
            8xy6 - SHR Vx {, Vy}
            8xy7 - SUBN Vx, Vy
            8xyE - SHL Vx {, Vy}
            9xy0 - SNE Vx, Vy
            Annn - LD I, addr
            Bnnn - JP V0, addr
            Cxkk - RND Vx, byte
            Dxyn - DRW Vx, Vy, nibble
            Ex9E - SKP Vx
            ExA1 - SKNP Vx
            Fx07 - LD Vx, DT
            Fx0A - LD Vx, K
            Fx15 - LD DT, Vx
            Fx18 - LD ST, Vx
            Fx1E - ADD I, Vx
            Fx33 - LD B, Vx
            Fx29 - LD F, Vx
            Fx55 - LD [I], Vx
            Fx65 - LD Vx, [I]
      3.2 - Super Chip-48 Instructions
            00Cn - SCD nibble
            00FB - SCR
            00FC - SCL
            00FD - EXIT
            00FE - LOW
            00FF - HIGH
            Dxy0 - DRW Vx, Vy, 0
            Fx30 - LD HF, Vx
            Fx75 - LD R, Vx
            Fx85 - LD Vx, R
*/

// see: http://devernay.free.fr/hacks/chip8/C8TECH10.HTM

public enum Token {
	octo, chipper,
	org,
	colon,
	comma,
	octochar,
	string,
	number,
	assign,				// :=
	equals,
	db,
	call,
	jp,
	cls,
	hires,
	lowres,
	v0,
	v1,
	v2,
	v3,
	v4,
	v5,
	v6,
	v7,
	v8,
	v9,
	va,
	vb,
	vc,
	vd,
	ve,
	vf,
	i,
	delay,
	buzz,
	sprite,
	rts,
	load,
	save,
	se,
	sne,
	ld,
	add,
	or,
	xor,
	sub,
	subn,
	shl,
	shr,
	rnd,
	skp,
	sknp,
	iindirect,
	scr,
	scd,
	scl,
	hf,
	r, label, literal, and, unequal, andassign, orassign, xorassign, minusassign, assignminus, plusassign, 
	shrassign, shlassign, smaller, bigger, lessequal, biggerequal, key, minuskey, hex, bighex, breakpoint,
	proto, alias, octoconst, semikolon, bcd, octoif, octothen, octobegin, octoelse, octoend, jump0,
	octonative, loop, octowhile, again, scu,
	loadflags, saveflags, audio, plane, macro, calc, octobyte, stringmode, octoassert, monitor, octopointer, pitch, comment, curlybracketopen, curlybracketclose, newline, exit, invalid, bracketopen, bracketclose, plus, minus, divide, mult, mod,
	
	tilde, exclamation, sin, cos, tan, exp, log, abs, sqrt, sign, ceil, floor, atsym, pow, min, max, strlen, none, notkey, 
	dotif, dotelse, dotend, dotunless, dotinclude, dotsegment, data, code, dotlog, dodumpoptions, dotconst, octofor, octoto, octostep, dotStruct, dot, internaldefs, whitespace, octowith, dotTileset, dotTiles, dotSprites, octoswitch, octocase, 
	octobreak, octoPackage, octoPublic, unalias, length, dotFunction, octovar, octoextends,
	dotifdef, dotVector, arrayopen, arraytclose, using,

	octoLong, megaOn, megaOff, megaPal, megaSpritew, megaSpriteh, megaAlpha, megaBlend, megaCollisioncolor, megaplay, megastop, octoldhi
	
};

