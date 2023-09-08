package spiteed;

import java.util.ArrayList;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;

public class CResources {
	CTokenizer mTokenizer = new CTokenizer();
	ArrayList<CTileData> mTiles = new ArrayList<>();
	public ArrayList<CSpriteData> mSprites = new ArrayList<>();

	
	public void readSourcecode(String text) {
		int p;
		CBinaryData data = null;
		
		CToken token = new CToken();
		mTokenizer.start(text);
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (token.token == Token.comment) {
				String lit = token.literal.trim();
				if (lit.startsWith("tiles:")) {
					CSpriteData sdata = new CSpriteData(token);
					mSprites.add(sdata);
					sdata.tiles = true;
					getDimen(sdata, lit);
					data = sdata;
				} else if (lit.startsWith("end")) {
					data = null;
				} else if (lit.startsWith("tileset")) {
					CTileData tdata = new CTileData(token);
					mTiles.add(tdata);
					data = tdata;
					
				} else if (lit.startsWith("sprite:")) {
					CSpriteData sdata = new CSpriteData(token);
					mSprites.add(sdata);
					sdata.tiles = false;
					getDimen(sdata, lit);
					data = sdata;					
				}
			}
			if (data != null) {
				if (token.token == Token.label) 
					data.addLabel(token.literal);
				if (token.token == Token.number)
					data.addByte(token.iliteral);
			}

		}
	}


	private void getDimen(CSpriteData sdata, String lit) {
		int p = lit.indexOf(':');
		if (p != -1) {
			int nr = 0;
			sdata.width = 0;
			sdata.height = 0;
			int pos=0;
			while (p < lit.length()) {
				char c = lit.charAt(p++);
				if (c == 'x') pos++;
				if (c >= '0' && c <= '9') {
					if (pos == 0) 
						sdata.width = sdata.width*10 + c-'0';
					else
						sdata.height = sdata.height*10 + c-'0';
				}
					
			}
		}
		
	}

}
