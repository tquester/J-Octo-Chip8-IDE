package spiteed;

import assembler.CToken;

public class CTileData extends CBinaryData{

	public CTileData(CToken token) {
		line = token.line;
		posinline = token.posinline;
	}

}
