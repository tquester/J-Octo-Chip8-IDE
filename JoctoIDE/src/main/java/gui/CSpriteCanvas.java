package gui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import spiteed.CSpriteData;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class CSpriteCanvas extends Composite {
	
	
	public byte[] mBytes=null;
	public int spriteHeight=8;
	public int mOffset=0;
	public boolean mReadonly=true;
	public CSpriteCanvasData mSpriteData = null;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CSpriteCanvas(Composite parent, int style) {
		super(parent, style);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				onMouseDown(e);
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});
		

	}
	
	public void setData(CSpriteCanvasData data) {
		mSpriteData = data;
	}

	


	protected void onPaint(PaintEvent e) {

		Rectangle bounds = getBounds();
		Display display = getParent().getDisplay();
		
	    Color white = display.getSystemColor(SWT.COLOR_WHITE);
	    Color black = display.getSystemColor(SWT.COLOR_BLACK);
	    Color gray1 = display.getSystemColor(SWT.COLOR_GRAY);
	    Color gray2 = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	    Color red = display.getSystemColor(SWT.COLOR_RED);
		
	    int width = spriteHeight == 0 ? 16 : 8;
	    int height = spriteHeight == 0 ? 16 : 15;
		int x = 0;
		int y = 0;
		int dx = bounds.width / width;
		int dy = bounds.height / height;
		int w = dx * 16;
		if (height > width) {
			dx = dy;
			w = dx * 8;
		}
		int h = dy * height;
		for (int i = 0; i < width+1; i++) {
			if (i == 8) {
				e.gc.setForeground(black);
			} else {
				if ((i & 3) == 0) {
					e.gc.setForeground(gray2);
				} else
					e.gc.setForeground(gray1);
			}
			e.gc.drawLine(x, 0, x, h);
	
			x += dx;
		}
		for (int i = 0; i < height+1; i++) {
			if (i == 8) {
				e.gc.setForeground(black);
			} else {
				if ((i & 3) == 0) {
					e.gc.setForeground(gray2);
				} else
					e.gc.setForeground(gray1);
			}			
			e.gc.drawLine(0, y, w, y);
			y += dy;
		}
		int bytepos = mOffset;
		
		y = 0;
		int spheight = spriteHeight == 0 ? 16 : spriteHeight;
		
		for (int iy = 0; iy < spheight; iy++) {
			x = 0;
			if (spriteHeight == 0) {
				int word = getSpriteWord(bytepos);
				bytepos += 2;
				int mask = 0x8000;
				for (int ix=0;ix<16;ix++) {
					if ((word & mask) == 0) {
						e.gc.setForeground(white);
						e.gc.setBackground(white);
					} else {
						e.gc.setForeground(black);
						e.gc.setBackground(black);
					}
						
					e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
					x += dx;
					mask >>= 1;
				}
			} else {
				int word = getSpriteByte(bytepos);
				bytepos += 1;
				int mask = 0x80;
				for (int ix=0;ix<8;ix++) {
					if ((word & mask) == 0) {
						e.gc.setForeground(white);
						e.gc.setBackground(white);
					} else {
						e.gc.setForeground(black);
						e.gc.setBackground(black);
					}
						
					e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
					x += dx;
					mask >>= 1;
				}
				
			}
			y += dy;
		}
		
		e.gc.setForeground(red);
		e.gc.setBackground(red);
		for (int iy = spheight+1; iy <= height;iy++) {
			x = 0;
			for (int ix = 0; ix < width; ix++) {
				e.gc.fillRectangle(x+2, y+2, dx-4, dy-4);
				x += dx;
			}
			y += dy;
		}
		
			
	}
	
	
	protected void onMouseDown(MouseEvent e) {

		if (mReadonly) return;
		Rectangle bounds = getBounds();
		int pos;
		int x = 0;
		int y = 0;
	    int width = spriteHeight == 0 ? 16 : 8;
	    int height = spriteHeight == 0 ? 16 : 15;
		int dx = bounds.width / width;
		int dy = bounds.height / height;
		int w = dx * 16;
		if (height > width) {
			dx = dy;
			w = dx * 8;
		}
		x = e.x / dx;
		y = e.y / dy;
		if (spriteHeight == 0) {
			pos = mOffset + y * 2;
			if (x > 7) {
				pos++;
				x -= 8;
			}
			int mask = 0x80 >> x;
			setSpriteByte(pos, getSpriteByte(pos) ^ mask);
				 
		} else {
			pos = mOffset + y;
			if (x <= 8) {
				int mask = 0x80 >> x;
				setSpriteByte(pos, getSpriteByte(pos) ^ mask);
			}
		}
		redraw();
		updateText();
		
		
		System.out.println(String.format("%d/%d", x,y));
		}
	/**
	 * Gets a byte from mBytes.
	 * Overwrite this function for own memory management
	 * 
	 * @param bytepos Position in memory
	 * @return
	 */
	public int getSpriteByte(int bytepos) {
		if (mSpriteData != null) return mSpriteData.getSpriteByte(bytepos);
		int result=0;
		if (mBytes != null) {
			if (bytepos+mOffset < mBytes.length) {
				result = mBytes[bytepos+mOffset];
			}
		}
		return result;
	}

	/**
	 * Gets a word from mBytes.
	 * Overwrite this function for own memory management

	 * @param bytepos
	 * @return
	 */
	public int getSpriteWord(int bytepos) {
		if (mSpriteData != null) return mSpriteData.getSpriteWord(bytepos);
		int result=0;
		if (mBytes != null) {
			if (bytepos+mOffset+1 < mBytes.length) {
				result = mBytes[bytepos+mOffset] * 256 + mBytes[bytepos+mOffset+1];
			}
		}
		return result;
	}
	
	private void setSpriteWord(int bytepos, int word) {
		if (mSpriteData != null) mSpriteData.setSpriteWord(bytepos, word);
		int result=0;
		if (mBytes != null) {
			if (bytepos+mOffset+1 < mBytes.length) {
				mBytes[bytepos] = (byte)(word >> 8);
				mBytes[bytepos+1] = (byte)(word & 0xff);
			}
		}
	}
	
	/**
	 * Called after Bytes have been changed, overwrite if nessary
	 */
	public void updateText() {
		if (mSpriteData != null) mSpriteData.updateText();
	}

	/**Sets a byte in memory. Overwrite for own memory management
	 * 
	 * @param bytepos
	 * @param newbyte
	 */
	public void setSpriteByte(int bytepos, int newbyte) {
		if (mSpriteData != null) {
			mSpriteData.setSpriteByte(bytepos, newbyte);
			return;
		}
			
		if (mBytes != null) {
			if (bytepos+mOffset < mBytes.length) {
				mBytes[bytepos+mOffset] = (byte)(newbyte & 0xff);
			}
		}
		
	}
	
	public void setHeight(int height) {
		spriteHeight = height;
		redraw();
	}
	
	public void setOffset(int offset) {
		mOffset = offset;
		redraw();
	}
	
	public void shiftRight() {
		int i;
		int word;
		int pos = mOffset;
		if (spriteHeight == 0) {
			for (i=0;i<16;i++) {
				word = getSpriteWord(pos);
				word >>= 1;
				setSpriteWord(pos, word);
				pos+=2;
			}
		} else {
			for (i=0;i<spriteHeight;i++) {
				word = getSpriteByte(pos);
				word >>= 1;
				setSpriteByte(pos,word);
				pos++;
			}
		}
		updateText();
		redraw();		
	}



	public void shiftLeft() {
		int i;
		int word;
		int pos = mOffset;
		if (spriteHeight == 0) {
			for (i=0;i<16;i++) {
				word = getSpriteWord(pos);
				word <<= 1;
				setSpriteWord(pos, word);
				pos+=2;
			}
		} else {
			for (i=0;i<spriteHeight;i++) {
				word = getSpriteByte(pos);
				word <<= 1;
				setSpriteByte(pos,word);
				pos++;
			}
		}
		updateText();
		redraw();
		
	}
	
	public void onShiftDown() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos1 = mOffset+rows*bytes;
		int pos = pos1-bytes;
		pos1--;
		pos--;
		for (int i=0;i<rows-1;i++) {
			for (int j=0;j<bytes;j++) {
				setSpriteByte(pos1--, getSpriteByte(pos--));
			}
			
			
		}
		for (int i=0;i<bytes;i++) setSpriteByte(mOffset+i,0);
		updateText();
		redraw();
		
	}

	protected void onShiftUp() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int pos = mOffset;
		int pos1 = mOffset+bytes;
		for (int i=0;i<rows-1;i++) {
			for (int j=0;j<bytes;j++) {
				setSpriteByte(pos++, getSpriteByte(pos1++));
			}
		}
		for (int i=0;i<bytes;i++) setSpriteByte(pos++,0);
		updateText();
		redraw();
	}

	protected void flipW() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int temp[] = new int[bytes];
		int pos = mOffset;
		for (int i=0;i<rows;i++) {
			int d = bytes-1;
			for (int j=0;j<bytes;j++) {
				int v = getSpriteByte(pos+j);
				v = reverse(v);
				temp[d] = v;
				d--;	
			}
			for (int j=0;j<bytes;j++) {
				setSpriteByte(pos+j,temp[j]);
			}
			pos+=bytes;
		}
		redraw();
		updateText();
		
	}

	int reverse(int b) {
		int r =0;
		int bit = 1;
		for (int i=1;i<=8;i++) {
			int nextbit = (b & bit) != 0 ? 1 : 0;
			r = r+r+nextbit;
			bit = bit << 1;
		}
		
		return r;
	}

	protected void flipH() {
		int rows = spriteHeight == 0 ? 16 : spriteHeight;
		int bytes = spriteHeight == 0 ? 2 : 1;
		int temp[] = new int[bytes];
		int pos = mOffset;
		int j;
		int pos2 = mOffset+bytes*(rows-1);
		for (int i=0;i<rows/2;i++) {
			for (j=0;j<bytes;j++) temp[j] = getSpriteByte(pos+j);
			for (j=0;j<bytes;j++) setSpriteByte(pos+j, getSpriteByte(pos2+j));
			for (j=0;j<bytes;j++) setSpriteByte(pos2+j,temp[j]);
			pos+=bytes;
			pos2-=bytes;
			
		}
		redraw();
		updateText();
		
	}




	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}

