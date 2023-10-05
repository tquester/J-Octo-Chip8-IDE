package spiteed;

import java.util.ArrayList;

import org.eclipse.swt.custom.StyledText;

import assembler.CToken;
import assembler.CTokenizer;
import assembler.Token;

public class CResources {
	CTokenizer mTokenizer = new CTokenizer();
	ArrayList<CTileData> mTiles = new ArrayList<>();
	public ArrayList<CSpriteData> mSprites = new ArrayList<>();
	public ArrayList<CSpriteData> mTilesets = new ArrayList<>();
	
	StyledText mCurrentEditor = null;

	
	public void readSourcecode(StyledText editor) {
		int p;
		mCurrentEditor = editor;
		String text = editor.getText();
		CBinaryData data = null;
		
		CToken token = new CToken();
		mTokenizer.start(text);
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			switch(token.token) {
			case dotTiles:
			case dotSprites:
			case dotTileset:
				compileTiles(token);
				break;
			case comment:
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
				break;

			}
			
			if (data != null) {
				if (token.token == Token.label) 
					data.addLabel(token.literal);
				if (token.token == Token.number)
					data.addByte(token.iliteral);
			}

		}
	}


	private void compileTiles(CToken token) {
		Token typ = token.token;
		CSpriteData data = new CSpriteData(token);
		switch(typ) {
		case dotTileset: mTilesets.add(data); break;
		default:
			mSprites.add(data);
		}
		mTokenizer.getToken(token);		// labelname;
		if (token.token != Token.literal) return;
		
		if (token.token != Token.literal) return;
		data.name = token.literal;
		
		mTokenizer.getToken(token);		
		if (token.token != Token.comma) return;
		
		mTokenizer.getToken(token);		// w
		if (token.token != Token.number) return;
		data.w = token.iliteral;
		mTokenizer.getToken(token);		
		if (token.token != Token.comma) return;
	
		mTokenizer.getToken(token);		// h
		if (token.token != Token.number) return;
		data.h = token.iliteral;
		mTokenizer.getToken(token);
		if (!CToken.isBegin(token.token)) return;
		
		StringBuilder sb = data.sb;
		while (mTokenizer.hasData()) {
			mTokenizer.getToken(token);
			if (CToken.isEnd(token.token)) break;
			switch(token.token) {
				case number: sb.append(token.literal+" "); break;
				case comma: sb.append(", "); break;
				case label: sb.append(": "+token.literal); break;
				case newline: sb.append("\n"); break;
				case comment: sb.append("# "+token.literal+"\n"); break;
			}
		}
		data.text = sb.toString();
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


	public boolean save(CSpriteData mCurrentTileMap) {
		if (mCurrentTileMap.name == null) return false;
		String text = mCurrentEditor.getText();
		String such = ":tileset "+mCurrentTileMap.name;
		int p = text.indexOf(such);
		if (p == -1) return false;
		int p1;
		p1 = text.indexOf('}', p+1);
		if (p1 == -1) return false;
		String left = text.substring(0,p);
		String right = text.substring(p1+1);
		text = left + String.format(":tileset %s, %d, %d {\n%s\n}\n", 
				mCurrentTileMap.name,
				mCurrentTileMap.w,
				mCurrentTileMap.h,
				mCurrentTileMap.text) + right;
				
		int caret = mCurrentEditor.getCaretOffset();
		mCurrentEditor.setText(text);
		mCurrentEditor.setCaretOffset(caret);
		mCurrentEditor.setSelection(caret, caret+1);
		return true;
		
	}

}
