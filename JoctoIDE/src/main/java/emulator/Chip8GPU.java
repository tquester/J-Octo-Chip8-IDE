package emulator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;

public class Chip8GPU {

	static final int screenSize = 256 * 192;

	public boolean hires = false;
	public int width = 64;
	public int height = 32;
	public boolean mega = false;
	static String OS = System.getProperty("os.name").toLowerCase();
	public RGBA palette[] = new RGBA[256];

	public int drawMask = 0x01;
	GC gc = null;

	public byte mScreen[] = new byte[screenSize];
	public IEmulator mIEmulator;
	public Image mImage;
	public byte memory[];
	public boolean dirty = false;
	private Color colors[];

	private boolean mSemaphore;

	public int tileWidth;

	public int tileHeight;

	public int megaSpriteH;

	public int megaSpriteW;

	public int megaAlpha;

	public int megaBlend;

	public int megaCollisionColor;

	private boolean mLowres=false;

	public void initImage(Device device, int width, int height) {
		mImage = new Image(device, width, height);
		colors = new Color[4];
		colors[0] = device.getSystemColor(SWT.COLOR_WHITE);
		colors[1] = device.getSystemColor(SWT.COLOR_BLACK);
		colors[2] = device.getSystemColor(SWT.COLOR_DARK_CYAN);
		colors[3] = device.getSystemColor(SWT.COLOR_DARK_RED);
		// gc = new GC(mImage);

	}

	public void cls() {
		if (mega)
			updateMegaScreen();
		for (int i = 0; i < screenSize; i++) {
			mScreen[i] = 0;
		}
		if (!mega)
			updateImage();
	}

	public void scrollRight() {
		// TODO Auto-generated method stub

	}

	public void scrollLeft() {
		// TODO Auto-generated method stub

	}

	public void lowres() {
		mLowres = true;
		width = 64;
		height = 32;

	}

	public void hires() {
		mLowres = false;
		if (mega) {
			width = 256;
			height = 192;
		} else {
			width = 128;
			height = 64;
		}

	}

	public void scrollDown(int n) {
		int iy1 = (this.height) * this.width;
		int iy2 = (this.height - 1) * this.width;
		int removeMask = 255 ^ drawMask;
		for (int y = 1; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				int screenByte = mScreen[iy1 + x];
				screenByte = (screenByte & removeMask) | (mScreen[iy2 + x] & drawMask);
				mScreen[iy1 + x] = (byte) (screenByte & 0xff);
			}
			iy1 -= this.width;
			iy2 -= this.width;
		}
		for (int x = 0; x <= this.width * 2; x++) {
			int screenByte = mScreen[x];
			screenByte = (screenByte & removeMask);
			mScreen[x] = (byte) (screenByte & 0xff);
		}
		updateImage();

	}

	public void scrollUp(int n) {
		int iy1 = 0; // dest
		int iy2 = this.width * n; // source
		int removeMask = 255 ^ drawMask;
		System.out.println(String.format("dm=%s\nrm=%s\n", bin(drawMask), bin(removeMask)));
		for (int y = 1; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {

				mScreen[iy1] = (byte) (((mScreen[iy1] & removeMask) | (mScreen[iy2] & drawMask)) & 0xff);
				mScreen[iy2] = (byte) ((mScreen[iy2] & removeMask) & 0xff);

				// mScreen[iy1] = screenByte;
				// screenByte |= (mScreen[iy2] & drawMask);
				// mScreen[iy2] = screenByte;

				// int screenByte = mScreen[iy2];
				// mScreen[iy1] = (byte) (screenByte & 0xff);
				iy1++;
				iy2++;
			}
		}
		for (int x = 0; x <= this.width * n; x++) {
			int screenByte = mScreen[iy1 + x];
			screenByte = (screenByte & removeMask) | (mScreen[iy2] & drawMask);
			mScreen[iy1 + x] = (byte) (screenByte & 0xff);
		}
		updateImage();

	}

	private Object bin(int drawMask2) {
		String str = Integer.toString(drawMask2, 2);
		while (str.length() < 8)
			str = "0" + str;
		return str;
	}

	void updateImage() {
		int chip8Width = width;
		int chip8Height = height;
		Rectangle bounds = mImage.getBounds();
		int tileWidth = bounds.width / chip8Width;
		int tileHeight = bounds.height / chip8Height;
		int x = 0;
		int y = 0;
		if (colors == null)
			return;
		if (mIEmulator == null)
			return;
		GC gc = new GC(mImage);

		int adr = 0;
		y = 0;
		for (int iy = 0; iy < height; iy++) {
			x = 0;
			for (int ix = 0; ix < width; ix++) {
				int screenByte = mScreen[adr + ix];
				gc.setBackground(colors[screenByte & 0x03]);
				gc.fillRectangle(x, y, tileWidth, tileHeight);
				x += tileWidth;
			}
			adr += width;
			y += tileHeight;
		}
		gc.dispose();
	}

	public int draw(int address, int x, int y, int height) {
		if (mega) {
			return drawMega(address, x, y);

		}
		int collision = 0;
		try {
			int chip8Width = this.width;
			int chip8Height = this.height;
			Rectangle bounds = mImage.getBounds();
			tileWidth = (bounds.width) / chip8Width;
			tileHeight = (bounds.height) / chip8Height;
			int imgx, imgy, imgx1;
			x &= (width - 1);
			y &= (this.height - 1);
			int shift = x & 0x07;
			int screenLineBytes = width / 8;
			int screenX = x / 8;
			int screenAddr = y * screenLineBytes + screenX;
			waitSemaphore();
			GC gc = new GC(mImage);
			setSemaphore(true);
			if (height != 0) {
				if (y + height > this.height)
					height = this.height - y;

				imgy = tileHeight * y;
				imgx = tileWidth * x;
				for (int iy = 0; iy < height; iy++) {
					imgx1 = imgx;
					int mask = 0x80;
					if (address < 0)
						break;
					int spriteByte = memory[address++];
					for (int ix = 0; ix <= 7; ix++) {
						if (x + ix < this.width) {
							if ((spriteByte & mask) != 0) {
								collision |= setScreenBit(x + ix, y + iy);
							}
							mask >>= 1;
							gc.setBackground(colors[getScreenBit(x + ix, y + iy) & 3]);
							gc.fillRectangle(imgx1, imgy, tileWidth, tileHeight);
							imgx1 += tileWidth;
						}
					}
					imgy += tileHeight;
				}
			} else {
				imgy = tileHeight * y;
				imgx = tileWidth * x;
				int y1 = y;
				for (int iy = 0; iy < 16; iy++) {
					if (y1 >= 0 && y1 < this.height) {
						imgx1 = imgx;
						int mask;

						int x1 = x;
						for (int hbyte = 0; hbyte <= 1; hbyte++) {
							int spriteByte = memory[address++];
							mask = 0x80;
							for (int ix = 0; ix <= 7; ix++) {
								if (x1 >= this.width)
									break;
								if ((spriteByte & mask) != 0) {
									collision |= setScreenBit(x1, y1);
								}
								gc.setBackground(colors[getScreenBit(x1, y1) & 3]);
								gc.fillRectangle(imgx1, imgy, tileWidth, tileHeight);
								imgx1 += tileWidth;
								x1++;
								mask >>= 1;
							}
						}
					}
					y1++;
					imgy += tileHeight;
				}

			}
			gc.dispose();
			setSemaphore(false);
			// updateImage();
			dirty = true;
			// if (mIEmulator != null)
			// mIEmulator.updateScreen();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collision;
	}

	public int updateMegaScreen() {
		if (mega == false) return 0;
		int collision = 0;
		int chip8Width = this.width;
		int chip8Height = this.height;
		int tileWidth;
		int tileHeight;
		int itileWidth;
		int itileHeight;

		RGBA rgba = null;
		Color col = null;
		int prevpix = -1;
		int iy = 0;
		int ix;
		Rectangle bounds = mImage.getBounds();
		tileWidth = (bounds.width) / chip8Width;
		tileHeight = (bounds.height) / chip8Height;
		itileHeight = tileHeight;
		itileWidth = tileWidth;
		waitSemaphore();
		GC gc;
		while (true) {
			try {
				gc = new GC(mImage);
				break;
			}
			catch(Exception ex) {
				
			}
		}
			
		setSemaphore(true);

		col = new Color(gc.getDevice(), 255, 255, 255, 255);
		// clear screen
		gc.setBackground(col);
		gc.setForeground(col);
		gc.drawRectangle(0, 0, bounds.width, bounds.height);
		col = new Color(gc.getDevice(), 0, 0, 255, 255);

		int ptr = 0;
		int baseptr=0;
		iy = 0;
		for (int y = 0; y < chip8Height; y++) {
			ix = 0;
			ptr = baseptr;
			baseptr+=256;
			for (int x = 0; x < chip8Width; x++) {
				int pix = mScreen[ptr++]  & 0xff;
				if (pix != 0) {
					if (pix != prevpix) {
						prevpix = pix;
						rgba = palette[pix];
						if (rgba != null) {
							col = new Color(gc.getDevice(), rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
							
							gc.setBackground(col);
							gc.setForeground(col);
						}
					}
					gc.fillRectangle((int)ix, (int)iy,itileWidth,itileHeight);
				}
				ix += tileWidth;
			}
			iy += tileHeight;
		}

		gc.dispose();
		setSemaphore(false);
		dirty = true;
		return collision;
	}

	private int drawMega(int address, int x, int y) {
		int collision = 0;
		//x &= 255;
		//y &= 255;
		if (y > 191) y = 191;

		int sy = y * 256;
		for (int ih = 0; ih < megaSpriteH; ih++) {
			int sx = sy+x;
			for (int iw = 0; iw < megaSpriteW; iw++) {
				int pix = (memory[address++] & 0xff);
				if (sx < mScreen.length) {
					int screenPix = mScreen[sx];
					if (screenPix == megaCollisionColor)
						collision = 1;
					mScreen[sx] = (byte)( pix & 0xff);
				}
				sx++;
			}
			sy += 256;
		}
		dirty=true;
		//updateMegaScreen();
		return 0;
	}

	public void setSemaphore(boolean b) {
		mSemaphore = b;

	}

	public void waitSemaphore() {
		if (OS.compareTo("linux") == 0)
			return;
		while (mSemaphore) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Auto-generated method stub

	}

	private int setScreenBit(int x, int y) {
		int r = 0;
		x &= (width - 1);
		y &= (height - 1);
		int adr = y * width + x;
		r = (mScreen[adr] & drawMask) != 0 ? 1 : 0;
		mScreen[adr] ^= drawMask;
		return r;
	}

	private int getScreenBit(int x, int y) {
		int r = 0;
		x &= (width - 1);
		y &= (height - 1);
		int adr = y * width + x;
		r = (mScreen[adr] & drawMask) != 0 ? 1 : 0;
		return mScreen[adr];
	}

	public boolean key(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPlanes(int i) {
		drawMask = i;

	}

	public void setAudio(int regI, int i) {
		// TODO Auto-generated method stub

	}

	public int waitKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAudioPitch(int i) {
		// TODO Auto-generated method stub

	}

	public boolean hasKey() {
		// TODO Auto-generated method stub
		return false;
	}

	public int lastKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void megaLoadPalette(Chip8CPU cpu, int count) {
		int ptr = cpu.regI;
		int ipal = 1;
		palette[0] = new RGBA(0, 0, 0, 0);
		for (int i = 0; i < count; i++) {
			int a = cpu.memory[ptr] & 0xff;
			int r = cpu.memory[ptr + 1] & 0xff;
			int g = cpu.memory[ptr + 2] & 0xff;
			int b = cpu.memory[ptr + 3] & 0xff;
			ptr += 4;
			palette[ipal] = new RGBA(r, g, b, a);
			System.out.println(String.format("Color %d = %d/%d/%d a=%d", ipal, r, g, b, a));
			ipal++;
		}

	}

	public void megaOff() {
		mega = false;
		width = 64;
		height = 32;

	}

	public void megaOn() {
		mega = true;
		width=256;
		height=192;
		if (mLowres) {
			width = 64;
			height = 32;
		}
		

	}

}
