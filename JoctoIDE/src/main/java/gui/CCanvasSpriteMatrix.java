package gui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import gui.CDialogTileEditor.SpriteLocation;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

public class CCanvasSpriteMatrix extends Composite {

	
	public int mTileW=8;
	public int mTileH=8;
	private int mSpriteSelected = -1;
	private int mSpritePointed = -1;
	private int mSpriteSelectedX = -1;
	private int mSpriteSelectedY = -1;
	private int[] tilesetBytes;
	ICanvasSpriteMatrixData mData = null;
	
	public void setData(int[] data, int height) {
		tilesetBytes = data;
		if (height == 0) {
			mTileW = 16;
			mTileH = 16;
		} else {
			mTileW = 8;
			mTileH = height;
		}
		redraw();
	}
	
	
	
	public class SpriteLocation {
		public SpriteLocation(int x2, int y2, int w2, int h2, int index) {
			x = x2;
			y = y2;
			w = w2;
			h = h2;
			ofs = index;
		}

		int x, y, w, h; // screen coordinates in tiles window
		int ofs; // byte offset in tilesetBytes;
	}


	ArrayList<SpriteLocation> mListSpriteLocations = new ArrayList<>();
	private SpriteLocation mCurrentSprite;
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CCanvasSpriteMatrix(Composite parent, int style) {
		super(parent, SWT.BORDER);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				onMouseMoveTile(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
					mSpriteSelected = mSpritePointed;
					if (mData != null) 
						mData.onSpriteSelected(mSpriteSelected);
					redraw();
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});

	}

	protected void onPaint(PaintEvent e) {
		int faktor = 4;
		int tileh = getTileH();
		int tilew = getTileW();
		int x = 0;
		int y = 0;
		Rectangle rect = getBounds();
		int w = tilew * faktor + 4;
		int h = tileh * faktor + 4;
		int spriteByteW = tileh;
		if (tilew == 16)
			spriteByteW *= 2;
		int index = 0;
		if (getTilesetBytes() == null)
			return;
		mListSpriteLocations.clear();
		for (index = 0; index < getTilesetBytes().length; index += spriteByteW) {
			mListSpriteLocations.add(new SpriteLocation(x, y, w, h, index));
			drawSprite(e.gc, index, x, y, tilew, tileh, tilew * faktor, tileh * faktor);
			// index += spriteByteW;
			x += w;
			if (x + w > rect.width) {
				x = 0;
				y += h;
			}
		}	
	}
	
	protected void onMouseMoveTile(MouseEvent e) {
		Rectangle rect = getBounds();
		int x = e.x;
		int y = e.y;
		int ofs = -1;

		int oldofs=-1;
		if (mCurrentSprite != null) oldofs = mCurrentSprite.ofs;
		for (SpriteLocation loc : mListSpriteLocations) {
			if (loc.x <= x && loc.x + loc.w > x && loc.y <= y && loc.y + loc.h > y) {
				ofs = loc.ofs;
				if (ofs != mSpritePointed) {
					mSpritePointed = ofs;
					redraw();
				}
				
				mCurrentSprite = loc;
				if (loc.ofs != oldofs)
					redraw();
				break;
			}
		}

	}
	
	private void drawSpriteWhite(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash) {
		drawSpriteSub(gc, index, x, y, w, h, canvasw, canvash, false);
	}

	private void drawSprite(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash) {
		drawSpriteSub(gc, index, x, y, w, h, canvasw, canvash, true);
	}

	private void drawSpriteSub(GC gc, int index, int x, int y, int w, int h, int canvasw, int canvash, boolean color) {
		int iy;
		Display display = getParent().getDisplay();
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		Color whiteCol = white;
		int pw = canvasw / w;
		int ph = canvash / h;
		int b, mask;
		if (color) {
			if (index == mSpritePointed)
				whiteCol = gray;
			if (index == mSpriteSelected)
				whiteCol = blue;
		}
		int pos = 0;
		if (w == 8) {
			// 8xn Sprite
			int sy = y;

			for (iy = 0; iy < h; iy++) {
				int sx = x;
				if (index + pos >= getTilesetBytes().length)
					break;
				b = getTilesetBytes()[index + pos];
				pos++;
				mask = 0x80;
				for (int bit = 0; bit < 8; bit++) {
					if ((b & mask) != 0)
						gc.setBackground(black);
					else
						gc.setBackground(whiteCol);
					gc.fillRectangle(sx, sy, pw, ph);
					sx += pw;
					mask /= 2;
				}
				sy += ph;
			}
		} else {
			int sy = y;
			for (iy = 0; iy < 16; iy++) {
				int sx = x;
				if (index + pos >= getTilesetBytes().length)
					break;
				b = getTilesetBytes()[index + pos] * 256 + getTilesetBytes()[index + pos + 1];
				pos+=2;
				mask = 0x8000;
				for (int bit = 0; bit < 16; bit++) {
					if ((b & mask) != 0)
						gc.setBackground(black);
					else
						gc.setBackground(whiteCol);
					gc.fillRectangle(sx, sy, pw, ph);
					sx += pw;
					mask /= 2;
				}
				sy += ph;
			}

		}

	}

	private int getTileW() {
		if (mData != null) return mData.getTileW();
		return mTileW;
	}

	private int getTileH() {
		if (mData != null) return mData.getTileH();
		return mTileH;
	}
	
	public int[] getTilesetBytes() {
		if (mData != null) return mData.getTileData();
		return tilesetBytes;
	}
	
	public void setTileData(ICanvasSpriteMatrixData data) {
		mData = data;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
